package com.redhat.services.ae.plugins;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.drools.template.ObjectDataCompiler;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieRepository;
import org.kie.api.builder.Message.Level;
import org.kie.api.definition.KiePackage;
import org.kie.api.definition.rule.Rule;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.ObjectFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.client.util.Maps;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.redhat.services.ae.Initialization;
import com.redhat.services.ae.MapBuilder;
import com.redhat.services.ae.dt.GoogleDrive3_1;
import com.redhat.services.ae.dt.RegExHelper;
import com.redhat.services.ae.model.Survey;
import com.redhat.services.ae.plugins.droolsscore.DroolsSurveySection;
import com.redhat.services.ae.plugins.droolsscore.ResultsBuilderTabsOverview;
import com.redhat.services.ae.recommendations.domain.Answer;
import com.redhat.services.ae.recommendations.domain.Recommendation;
import com.redhat.services.ae.utils.Json;

public class XAccountCompassRecommendationsPlugin extends Plugin{
	public static final Logger log=LoggerFactory.getLogger(XAccountCompassRecommendationsPlugin.class);
	private static final GoogleDrive3_1 drive=Initialization.newGoogleDrive();
	
	private List<String> drls=null;
	boolean extraDebug=false;
	private String configSheetName;
	private String thresholdSection;
	private boolean includeOverviewTab;
	
	private String decisionTableId;
	private String decisionTableLocation;
	private KieServices kieServices = KieServices.Factory.get();
//	private Integer noScore=-1000; // the score for a question if there is no score specified/found (it cannot be null as it throws NPE)
	
	public XAccountCompassRecommendationsPlugin(){
		if (!drive.isInitialised())
			new Initialization().onStartup(null);
	}
	
	@Override
	public Plugin setConfig(Map<String, Object> config){
		decisionTableLocation=getConfigValueAsString(config, "decisionTableLocation", null);
		decisionTableId=decisionTableLocation.substring(decisionTableLocation.lastIndexOf("/")+1);
		thresholdSection=getConfigValueAsString(config, "thresholdSection", null);
		configSheetName=getConfigValueAsString(config, "configSheetName", null);
		extraDebug=getBooleanFromConfig(config, "extraDebug", false);
		includeOverviewTab=getBooleanFromConfig(config, "includeOverviewTab", false);
		
		if (null==configSheetName) throw new RuntimeException("'configSheetName' in "+this.getClass().getSimpleName()+" plugin must be set");
		if (null==thresholdSection) throw new RuntimeException("'thresholdSection' in "+this.getClass().getSimpleName()+" plugin must be set");
		return this;
	}
	
  
	public KieSession newKieSession(String surveyId, String sheetId) throws IOException, InterruptedException{
		if (null==drls) drls=Lists.newArrayList();
		drls.add(compileDrlRules(surveyId));
		drls.addAll(compileSpreadsheetRules(surveyId, sheetId));
		drls.addAll(compileCustomSpreadsheetRules(sheetId));
		
		try{
			return newKieSession(drls.toArray(new String[drls.size()]));
		}catch(DroolsCompilationException e){
			System.err.println(e.getMessage());
			e.printStackTrace();
			return null;
		}
	}
	
	
	private String compileDrlRules(String surveyId){
		Survey s=Survey.findById(surveyId);
		String result=null;
		if (StringUtils.isNotBlank(s.getRules())){
			if (extraDebug) log.debug("Technical rules are not blank, compiling:\n"+s.getRules());
			result=s.getRules(); // technical drl rules
		}
		return result;
	}
	
	private List<String> compileCustomSpreadsheetRules(String sheetId) throws IOException, InterruptedException{
		List<String> result=Lists.newArrayList();
		File sheet=drive.downloadFile(sheetId);
		SimpleDateFormat dateFormatter=null;
		List<Map<String, String>> parseExcelDocument=null;
		List<String> sheets=Lists.newArrayList("Mats Sandbox2");
		
		String drl="package com.redhat.services.ae\n\n";
		drl+="import com.redhat.services.ae.recommendations.domain.Insight;\n";
		drl+="import com.redhat.services.ae.recommendations.domain.Answer;\n";
		drl+="import com.redhat.services.ae.recommendations.domain.Recommendation;\n\n";
		
		for(String sheetName:sheets){
			parseExcelDocument=drive.parseExcelDocumentAsStrings(sheet, sheetName, new GoogleDrive3_1.HeaderRowFinder(){ public int getHeaderRow(XSSFSheet s){
				return GoogleDrive3_1.SheetSearch.get(s).find(0, "Rule name").getRowIndex();
			}}, dateFormatter);
			
			for(Map<String, String> rows:parseExcelDocument){
				String ruleName=rows.get("Rule name");
				String preParsedLhs=rows.get("Logic");
				String section=rows.get("Section");
				String title1=rows.get("Title 1");
				String title2=rows.get("Title 2");
				String recommendation=rows.get("Recommendation Text");
				
				List<String> lhs=parseLHS(preParsedLhs, Lists.newArrayList("subscriptions", "orgSize", "happiness"));
				
				drl+=String.format("rule \"%s\"\nwhen\n", ruleName);
				for (String f:lhs)
					drl+="\t"+f+"\n";
				drl+="then\n\t";
				drl+=String.format("insert(new Recommendation(%s));", "\""+Joiner.on("\",\"").skipNulls().join(Lists.newArrayList(section,title1,title2,recommendation))+"\"");
				drl+="\nend\n\n";
			}
			result.add(drl);
		}
		System.out.println(result);
		return result;
	}
	
	public static void main(String[] args) throws IOException, InterruptedException, DroolsCompilationException{
		XAccountCompassRecommendationsPlugin p=new XAccountCompassRecommendationsPlugin();
		List<String> drls=p.compileCustomSpreadsheetRules("1eZ6_Yy7Go7RK2yh3SsUGJZ2jvXbVk3IdPiTO5emTzz0");
		KieSession kSession=p.newKieSession(drls.toArray(new String[]{}));
		
//		for(String s:drls)
//			System.out.println(s);
		
		if (true) return;
		List<String> questionNames=Lists.newArrayList("subscriptions", "orgSize", "happiness");
		System.out.println(new XAccountCompassRecommendationsPlugin().parseLHS("subscriptions contains \"OpenShift\" and subscriptions not contains \"ACS\" and orgSize==\">500\"", questionNames));
	}
	private List<String> parseLHS(String rawLhs, List<String> questionNames){
		// split by operator (&&, ||, and, or)
		List<String> result=Lists.newArrayList();
		if (rawLhs.trim().startsWith("Answer") || rawLhs.trim().startsWith("Insight")){
			result.add(rawLhs);
		}else{
			String[] fragments=rawLhs.split("(?=( and | or | \\&\\& | \\|\\| ))");
			for(String f:fragments){
				String op=null;
				if (f.matches("( and | \\&\\& ).+")){ 
					op=" and "; 
					f=f.replaceFirst("( and | \\&\\& )", "");
				}
				if (f.matches("( or| \\|\\| ).+")){   
					op=" or";   
					f=f.replaceFirst("( or | \\|\\| )", "");
				}
				
				for(String q:questionNames)
					if (f.contains(q)){
						String condition=f.replaceFirst(q, "");
						String fragment=(null!=op?" "+op+" ":"")+"Answer(question==\""+q+"\", answer "+condition+")";
						result.add(fragment);
					}
			}
		}
		if (true) return result;
		
		for(String f:result)
			System.out.println(f);
		
		
		Pattern p=Pattern.compile("( and | or | \\&\\& | \\|\\|)");
		Matcher m=p.matcher(rawLhs);
		int start=0;
		while (m.find()){
			String operation=m.group();
			String command=rawLhs.substring(start, m.start());
			start=m.end();
//			String command=rawLhs.substring(m.start(), m.end());
			System.out.println("op="+operation+", cmd="+command);
			
		}
		return null;
	}
	
	private List<String> compileSpreadsheetRules(String surveyId, String sheetId) throws IOException, InterruptedException{
		List<String> result=Lists.newArrayList();
		// Add rules from configured sheet
		File sheet=drive.downloadFile(sheetId);
		SimpleDateFormat dateFormatter=null;
		List<String> sheets=Lists.newArrayList("Section Recommendations", "Question Recommendations");
		int salience=65535;
		for(String sheetName:sheets){
			
			// Load the excel sheet with a retry loop?
			List<Map<String, String>> parseExcelDocument=null;
			parseExcelDocument=drive.parseExcelDocumentAsStrings(sheet, sheetName, new GoogleDrive3_1.HeaderRowFinder(){ public int getHeaderRow(XSSFSheet s){
				return GoogleDrive3_1.SheetSearch.get(s).find(0, "Description").getRowIndex();
			}}, dateFormatter);
			
			List<Map<String,Object>> dataTable = new ArrayList<>();
			for(Map<String, String> rows:parseExcelDocument){
				dataTable.add(
						new MapBuilder<String,Object>()
							.put("salience", salience-=1)// 65534-Integer.parseInt(rows.get("ROW_#")))
							.put("language", Utils.defaultTo(rows.get("Language"), "en"))
							.put("description", Utils.makeTextSafeForCompilation(rows.get("Description"), "NoDescription")) // default to prevent rule compilation error on rule name
							
							.put("section", rows.get("Section"))
							.put("subSection", rows.get("Sub-section"))
							.put("scoreLow", rows.get("Score >=")!=null?(int)Double.parseDouble(rows.get("Score >=")):null)
							.put("scoreHigh", rows.get("Score <=")!=null?(int)Double.parseDouble(rows.get("Score <=")):null)
							.put("answerValue", rows.get("Answer Value")!=null?rows.get("Answer Value"):null)
							
							.put("questionId", rows.get("Question ID"))
							
							.put("resultLevel1", rows.get("Result Level 1"))
							.put("resultLevel2", rows.get("Result Level 2"))
							.put("resultText", Utils.makeTextSafeForCompilation(rows.get("Text")))
							
							.build()
						);
			}
			
			String templateName="scorePlugin_"+sheetName.replaceAll(" ", "")+".drt";
			InputStream template=XAccountCompassRecommendationsPlugin.class.getClassLoader().getResourceAsStream(templateName);
			String drl=new ObjectDataCompiler().compile(dataTable, template);
			if (extraDebug){
				log.debug(drl);
			}
			result.add(drl);
		}
		return result;
	}
	
	public void invalidateRules(){
		drls=null;
	}
		
//	private Map<String, Integer> getThresholdRanges(String decisionTableId) throws IOException, InterruptedException{
//		SimpleDateFormat dateFormatter=null;
//		File sheet=drive.downloadFile(decisionTableId);
//		
//		List<Map<String, String>> parseExcelDocument=drive.parseExcelDocument(sheet, configSheetName, new GoogleDrive3.HeaderRowFinder(){ public int getHeaderRow(XSSFSheet s){
//			return GoogleDrive3.SheetSearch.get(s).find(0, thresholdSection).getRowIndex();
//		}}, dateFormatter);
//		
//		Map<String,Integer> ranges=new LinkedHashMap<>();
//		for (Map<String, String> row:parseExcelDocument){
//			ranges.put(row.get("Report Thresholds"), (int)Double.parseDouble(row.get("To")));
//		}
//		
//		return ranges;
//	}
	
	@Override
	public Map<String, Object> execute(String surveyId, String visitorId, Map<String, Object> surveyResults) throws Exception{
		try{
			log.debug(this.getClass().getSimpleName()+": data = "+Json.toJson(surveyResults));
			
			KieSession kSession=newKieSession(surveyId, decisionTableId);
			
			if (extraDebug){ // make sure we have some rule packages to execute
				log.debug("Rule Packages/Rules:");
				for(KiePackage pkg:kSession.getKieBase().getKiePackages()){
					for (Rule r:pkg.getRules()) log.debug(" - "+pkg.getName()+"."+r.getName());
				}
			}
			
			// build a map of question/value for string replacement in the recommendations
			Map<String,String> kvReplacement=new HashMap<String, String>();
			
			String language=surveyResults.containsKey("language")?(String)surveyResults.get("language"):"en"; // default to en
			
			Map<String,Integer> sectionScores=new HashMap<>();
			
			// Insert the question/answer facts into the drools session
			for(Entry<String, Object> e:surveyResults.entrySet()){
				
				if (e.getValue()==null){
					log.debug("answer to "+e.getKey()+" had null value, skipping...");
					continue;
				}
				
				if (e.getKey().startsWith("_") || e.getKey().startsWith("C_")){
					
					// Insert section scores into drools session
					if (e.getKey().equalsIgnoreCase("_sectionScore")){
						Map<String, Integer> values=(Map<String, Integer>)e.getValue();
						for(Entry<String, Integer> e2:values.entrySet()){
							String sectionName=e2.getKey();
							DroolsSurveySection a=new DroolsSurveySection(sectionName, language, e2.getValue());
							log.debug("Inserting fact: "+a);
							kvReplacement.put("score_"+sectionName.replaceAll(" ", "_"), String.valueOf(e2.getValue()));
							sectionScores.put(sectionName, e2.getValue());
							kSession.insert(a);
						}
					}
					
				}else{
					
					// Insert answer scores into drools session
					if (Map.class.isAssignableFrom(e.getValue().getClass())){
						Map<String, Object> value=(Map<String, Object>)e.getValue();
						int score=null!=value.get("score")?(Integer)value.get("score"):RecommendationsPlugin.noScore;
//						Answer a=new Answer(e.getKey(), (String)value.get("pageId"), language, score, (List<String>)value.get("answers"), (String)value.get("title"));
						
						Answer a=new Answer.builder().questionId(e.getKey()).language(language).score(score).answer((List<String>)value.get("answers")).build();
						
//						DroolsSurveyAnswer a=new DroolsSurveyAnswer(e.getKey(), (String)value.get("pageId"), language, (Integer)value.get("score"), (List<String>)value.get("answers"), (String)value.get("title"));
						log.debug("Inserting fact: "+a);//(String.format("Answer: id=%s, page=%s, lang=%s, score=%s, text=%s", e.getKey(), language, (Integer)value.get("score"), (String)value.get("title") )));
						kSession.insert(a);
						
						try{
							if (value.containsKey("answer") && String.class.isAssignableFrom(value.get("answer").getClass()))
								kvReplacement.put(e.getKey(), (String)value.get("answer"));
							
							if (value.containsKey("answers") && List.class.isAssignableFrom(value.get("answers").getClass()))
								kvReplacement.put(e.getKey(), Joiner.on(",").join((List)value.get("answers")));
							
						}catch(Exception sink){
							log.error("Error with: "+Json.toJson(e.getValue()));
							throw sink;
						}
						
					}else{
						log.debug("Found answer named ["+e.getKey()+"], however "+e.getValue().getClass()+" type answers are not yet supported - answer ignored");
					}
				}
			}
			
			kSession.setGlobal("list", new LinkedList<>());
			
			kSession.fireAllRules();
			
			// A global list was used to retain the order of the DroolsRecommendation objects. fact insertions and extractions through geFactHandles does not retain order
			List<Recommendation> recommendations=(LinkedList<Recommendation>)kSession.getGlobal("list");
			
			// if there are no recommendations in the list, then perhaps they're in the working session as facts
			if (null==recommendations || recommendations.size()<=0){
				Collection<? extends Object> resultFacts=kSession.getObjects(new ObjectFilter(){public boolean accept(Object fact){ return fact instanceof Recommendation; }});
				recommendations=resultFacts.stream()
				.map(Recommendation.class::cast)
				.collect(Collectors.toList());
			}
			
			if (extraDebug) log.debug("Found "+recommendations.size()+" recommendation"+(recommendations.size()!=1?"s":"")+":");
			
			// key/values replacements
			for (Recommendation r:recommendations){
				if (extraDebug) log.debug(" - "+r);
				
				// replace any key/values from the answers in the recommendation strings
				for (Entry<String, String> e:kvReplacement.entrySet()){
					if (null!=r.getText() && r.getText().contains("$"+e.getKey()))
						r.doKeyValueReplacement(e.getKey(), e.getValue());
				}
				
				if (r.getText().contains("$score"))
					r.doKeyValueReplacement("$score", kvReplacement.get("score_"+r.getSection().replaceAll(" ", "_")));
			}
			
			Map<String,Integer> thresholds=Maps.newHashMap();//getThresholdRanges(decisionTableId);
			
			Object resultSections=new ResultsBuilderTabsOverview()
					.includeOverviewTab(includeOverviewTab)
					.build(recommendations, sectionScores, thresholds);
			
			
			// add recommendations to the results
			surveyResults.put("_report", resultSections);
			
			// Generate a unique. date-stamped result ID so we can identify old data easily later on
			String ts=new SimpleDateFormat("yyMMdd").format(new Date());
			String uniqueReportId=surveyResults.containsKey("_reportId")?(String)surveyResults.get("_reportId"):ts+UUID.randomUUID().toString().replaceAll("-", "");
			log.debug("Adding _reportId: "+uniqueReportId);
			surveyResults.put("_reportId", uniqueReportId);
			
			return surveyResults;
			
			
		}catch(Throwable t){
			t.printStackTrace();
		}
		
		log.error("Returning survey results but an error occured in the DroolsReport plugin that needs investigating");
		return surveyResults;
	}
	
	
	
	private KieSession newKieSession(String... drls) throws IOException,DroolsCompilationException {
  	KieServices ks = KieServices.Factory.get();
    KieFileSystem kfs = ks.newKieFileSystem();
    kfs.generateAndWritePomXML(kieServices.getRepository().getDefaultReleaseId());
    int i=0;
    for(String drl:drls) kfs.write( "src/main/resources/com/redhat/services/ae/"+"drl"+(i+=1)+".drl", drl.getBytes());
    KieBuilder kb = ks.newKieBuilder(kfs).buildAll();
    if (kb.getResults().getMessages(Level.ERROR).size() != 0) {
    	throw new DroolsCompilationException(kb.getResults());
//        System.out.println(String.format("File compilation error: %s", kb.getResults().getMessages()));
    }
    
    KieRepository kieRepository = kieServices.getRepository();
    KieContainer kieContainer = kieServices.newKieContainer(kieRepository.getDefaultReleaseId());
    KieSession ksession = kieContainer.newKieSession();
    return ksession;
	}
	
	static class Utils{
		public static String defaultTo(String value, String defaultTo){
			if (value==null || "".equals(value)) return defaultTo;
			return value;
		}
		public static String makeTextSafeForCompilation(String text){
			return makeTextSafeForCompilation(text, text);
		}
		public static String makeTextSafeForCompilation(String text, String defaultTo){
			if (null==text || "".equals(text)) return defaultTo;
			return text
					.replaceAll(System.getProperty("line.separator"), "<br/>")
					.replaceAll("\r\n", "<br/>")
					.replaceAll("\\r\\n", "<br/>")
					.replaceAll("(\n|\r)", "<br/>")
					.replaceAll("(\\n|\\r)", "<br/>")
					.replaceAll("\"", "\\\\\"")
					;
		}
	}
}



//private void compileDrls(String surveyId, String sheetId){
//
//if (null==drls){
//	drls=Lists.newArrayList();
//	
//	// Add rules for survey entity
//	Survey s=Survey.findById(surveyId);
//	if (StringUtils.isNotBlank(s.getRules())){
//		if (extraDebug)
//			log.debug("Technical rules are not blank, compiling:\n"+s.getRules());
//		drls.add(s.getRules()); // technical drl rules
//	}
//	
//	// Add rules from configured sheet
//	File sheet=drive.downloadFile(sheetId);
//	SimpleDateFormat dateFormatter=null;
//	List<String> sheets=Lists.newArrayList("Section Recommendations", "Question Recommendations");
//	int salience=65535;
//	for(String sheetName:sheets){
//		
//		// Load the excel sheet with a retry loop?
//		List<Map<String, String>> parseExcelDocument=null;
//		parseExcelDocument=drive.parseExcelDocument(sheet, sheetName, new GoogleDrive3.HeaderRowFinder(){ public int getHeaderRow(XSSFSheet s){
//			return GoogleDrive3.SheetSearch.get(s).find(0, "Description").getRowIndex();
//		}}, dateFormatter);
//		
//		
//		List<Map<String,Object>> dataTableConfigList2 = new ArrayList<>();
//		for(Map<String, String> rows:parseExcelDocument){
//			dataTableConfigList2.add(
//					new MapBuilder<String,Object>()
//					.put("salience", salience-=1)// 65534-Integer.parseInt(rows.get("ROW_#")))
//					.put("language", Utils.defaultTo(rows.get("Language"), "en"))
//					.put("description", Utils.makeTextSafeForCompilation(rows.get("Description"), "NoDescription")) // default to prevent rule compilation error on rule name
//					
//					.put("section", rows.get("Section"))
//					.put("subSection", rows.get("Sub-section"))
//					.put("scoreLow", rows.get("Score >=")!=null?(int)Double.parseDouble(rows.get("Score >=")):null)
//					.put("scoreHigh", rows.get("Score <=")!=null?(int)Double.parseDouble(rows.get("Score <=")):null)
//					.put("answerValue", rows.get("Answer Value")!=null?rows.get("Answer Value"):null)
//					
//					.put("questionId", rows.get("Question ID"))
//					
//					
//					.put("resultLevel1", rows.get("Result Level 1"))
//					.put("resultLevel2", rows.get("Result Level 2"))
//					.put("resultText", Utils.makeTextSafeForCompilation(rows.get("Text")))
//					
//					.build()
//					);
//		}
//		
//		String templateName="scorePlugin_"+sheetName.replaceAll(" ", "")+".drt";
//		InputStream template = AccountCompassRecommendationsPlugin.class.getClassLoader().getResourceAsStream(templateName);
//		ObjectDataCompiler compiler = new ObjectDataCompiler();
//		String drl = compiler.compile(dataTableConfigList2, template);
//		if (extraDebug){
//			System.out.println(drl);
//		}
//		drls.add(drl);
//	}
//}
//
//try{
//	return newKieSession(drls.toArray(new String[drls.size()]));
//}catch(DroolsCompilationException e){
//	System.err.println(e.getMessage());
//	e.printStackTrace();
//	return null;
//}
//
//}