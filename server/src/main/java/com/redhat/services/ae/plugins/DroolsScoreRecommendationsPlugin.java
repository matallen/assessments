package com.redhat.services.ae.plugins;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.UUID;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.drools.template.ObjectDataCompiler;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieRepository;
import org.kie.api.builder.Message.Level;
import org.kie.api.builder.ReleaseId;
import org.kie.api.definition.KiePackage;
import org.kie.api.definition.rule.Rule;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.ObjectFilter;
import org.kie.api.runtime.rule.FactHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.redhat.services.ae.Initialization;
import com.redhat.services.ae.MapBuilder;
import com.redhat.services.ae.dt.GoogleDrive3_1;
import com.redhat.services.ae.plugins.droolsscore.DroolsRecommendation;
import com.redhat.services.ae.plugins.droolsscore.DroolsSurveyAnswer;
import com.redhat.services.ae.plugins.droolsscore.DroolsSurveySection;
import com.redhat.services.ae.plugins.droolsscore.ResultsBuilderTabsOverview;
import com.redhat.services.ae.utils.Json;

public class DroolsScoreRecommendationsPlugin extends Plugin{
	public static final Logger log=LoggerFactory.getLogger(DroolsScoreRecommendationsPlugin.class);
	
//	private static final int CACHE_EXPIRY_IN_MS=10000;
	private static final int DEFAULT_CACHE_EXPIRY_IN_MS=10000;
//	private static final GoogleDrive3 drive=new GoogleDrive3(CACHE_EXPIRY_IN_MS);
	private static final GoogleDrive3_1 drive=new GoogleDrive3_1(null!=System.getenv("GDRIVE_CACHE_EXPIRY_IN_MS")?Integer.parseInt(System.getenv("GDRIVE_CACHE_EXPIRY_IN_MS")):DEFAULT_CACHE_EXPIRY_IN_MS);
	
	private List<String> drls=null;
	boolean extraDebug=false;
	private String configSheetName;
	private String thresholdSection;
	private boolean includeOverviewTab;
	
//	@Inject
//  private KieRuntimeBuilder runtimeBuilder;
	
	String decisionTableId;
	String decisionTableLocation;
	private KieServices kieServices = KieServices.Factory.get();
	
	public DroolsScoreRecommendationsPlugin(){
		if (!drive.isInitialised())
			new Initialization().onStartup(null);
	}
	
	@Override
	public Plugin setConfig(Map<String, Object> config){
		decisionTableLocation=getConfigValueAsString(config, "decisionTableLocation", null);
		decisionTableId=decisionTableLocation.substring(decisionTableLocation.lastIndexOf("/")+1);
		thresholdSection=getConfigValueAsString(config, "thresholdSection", null);
		configSheetName=getConfigValueAsString(config, "configSheetName", null);
		extraDebug=hasExtraDebug(config, "extraDebug");
		includeOverviewTab=getBooleanFromConfig(config, "includeOverviewTab", true);
		
		if (null==configSheetName) throw new RuntimeException("'configSheetName' in "+this.getClass().getSimpleName()+" plugin must be set");
		if (null==thresholdSection) throw new RuntimeException("'thresholdSection' in "+this.getClass().getSimpleName()+" plugin must be set");
		return this;
	}
	
  
	public KieSession newKieSession(String... drls) throws IOException {
  	KieServices ks = KieServices.Factory.get();
    KieFileSystem kfs = ks.newKieFileSystem();
    kfs.generateAndWritePomXML(kieServices.getRepository().getDefaultReleaseId());
    int i=0;
    for(String drl:drls){
    	kfs.write( "src/main/resources/com/redhat/services/ae/"+"drl"+(i+=1)+".drl", drl.getBytes());
    }
    
    KieBuilder kb = ks.newKieBuilder(kfs).buildAll();
    if (kb.getResults().getMessages(Level.ERROR).size() != 0) {
        System.out.println(String.format("File compilation error: %s", kb.getResults().getMessages()));
    }
    
    KieRepository kieRepository = kieServices.getRepository();
    ReleaseId krDefaultReleaseId = kieRepository.getDefaultReleaseId();
    KieContainer kieContainer = kieServices.newKieContainer(krDefaultReleaseId);
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
	
	public KieSession newKieSession(String sheetId) throws IOException, InterruptedException{
		
		if (null==drls){
			File sheet=drive.downloadFile(sheetId);
			SimpleDateFormat dateFormatter=null;
			
			List<String> sheets=Lists.newArrayList("Section Recommendations", "Question Recommendations");
			drls=Lists.newArrayList();
			int salience=65535;
			for(String sheetName:sheets){
				
				// Load the excel sheet with a retry loop?
				List<Map<String, String>> parseExcelDocument=null;
				parseExcelDocument=drive.parseExcelDocumentAsStrings(sheet, sheetName, new GoogleDrive3_1.HeaderRowFinder(){ public int getHeaderRow(XSSFSheet s){
					return GoogleDrive3_1.SheetSearch.get(s).find(0, "Description").getRowIndex();
				}}, dateFormatter);
				
				
				List<Map<String,Object>> dataTableConfigList2 = new ArrayList<>();
				for(Map<String, String> rows:parseExcelDocument){
					dataTableConfigList2.add(
							new MapBuilder<String,Object>()
							.put("salience", salience-=1)// 65534-Integer.parseInt(rows.get("ROW_#")))
							.put("language", Utils.defaultTo(rows.get("Language"), "en"))
							.put("description", Utils.makeTextSafeForCompilation(rows.get("Description"), "NoDescription")) // default to prevent rule compilation error on rule name
							
							.put("section", rows.get("Section"))
							.put("subSection", rows.get("Sub-section"))
							.put("scoreLow", rows.get("Score >=")!=null?(int)Double.parseDouble(rows.get("Score >=")):null)
							.put("scoreHigh", rows.get("Score <=")!=null?(int)Double.parseDouble(rows.get("Score <=")):null)
							.put("answerValue", rows.get("Answer")!=null?rows.get("Answer"):null)
							
							.put("questionId", rows.get("Question ID"))
							
							.put("resultLevel1", rows.get("Result Level 1"))
							.put("resultLevel2", rows.get("Result Level 2"))
							.put("resultText", Utils.makeTextSafeForCompilation(rows.get("Text")))
							
							.build()
							);
				}
				
				String templateName="scorePlugin_"+sheetName.replaceAll(" ", "")+".drt";
				InputStream template = DroolsScoreRecommendationsPlugin.class.getClassLoader().getResourceAsStream(templateName);
				ObjectDataCompiler compiler = new ObjectDataCompiler();
				String drl = compiler.compile(dataTableConfigList2, template);
				if (extraDebug){
					System.out.println(drl);
				}
				drls.add(drl);
			}
		}
		
		return newKieSession(drls.toArray(new String[drls.size()]));
		
	}
	
	public void invalidateRules(){
		drls=null;
	}
		
	
	private Map<String, Integer> getThresholdRanges(String decisionTableId) throws IOException, InterruptedException{
		SimpleDateFormat dateFormatter=null;
		File sheet=drive.downloadFile(decisionTableId);
		
		List<Map<String, String>> parseExcelDocument=drive.parseExcelDocumentAsStrings(sheet, configSheetName, new GoogleDrive3_1.HeaderRowFinder(){ public int getHeaderRow(XSSFSheet s){
			return GoogleDrive3_1.SheetSearch.get(s).find(0, thresholdSection).getRowIndex();
		}}, dateFormatter);
		
		Map<String,Integer> ranges=new LinkedHashMap<>();
		for (Map<String, String> row:parseExcelDocument){
			ranges.put(row.get("Report Thresholds"), (int)Double.parseDouble(row.get("To")));
		}
		
		return ranges;
	}
	
	@Override
	public Map<String, Object> execute(String surveyId, String visitorId, Map<String, Object> surveyResults) throws Exception{
		try{
			
			log.debug("DroolsScorePlugin: data = "+Json.toJson(surveyResults));
			
			KieSession kSession=newKieSession(decisionTableId);
			
			// DEBUG ONLY - make sure we have some rule packages to execute
			if (extraDebug){
				log.debug("Rule Packages/Rules:");
				for(KiePackage pkg:kSession.getKieBase().getKiePackages()){
					for (Rule r:pkg.getRules()){
						log.debug(" - "+pkg.getName()+"."+r.getName());
					}
				}
			}
			
			
			// build a map of question/value for string replacement in the recommendations
			Map<String,String> kvReplacement=new HashMap<String, String>();
			
			String language=surveyResults.containsKey("language")?(String)surveyResults.get("language"):"en"; // default to en
			
			Map<String,Integer> sectionScores=new HashMap<>();
			
			// Insert the question/answer facts into the drools session
			for(Entry<String, Object> e:surveyResults.entrySet()){
				String key=e.getKey();
				Object val=e.getValue();
				
				
				if (key.startsWith("_") || key.startsWith("C_")){
					
					// Insert section scores into drools session
					if (key.equalsIgnoreCase("_sectionScore")){
						Map<String, Integer> values=(Map<String, Integer>)val;
						for(Entry<String, Integer> e2:values.entrySet()){
							String sectionName=e2.getKey();
							DroolsSurveySection a=new DroolsSurveySection(sectionName, "subSection", language, e2.getValue());
							log.debug("Inserting fact: "+a);
							kvReplacement.put("score_"+sectionName.replaceAll(" ", "_"), String.valueOf(e2.getValue()));
							sectionScores.put(sectionName, e2.getValue());
							kSession.insert(a);
						}
					}
					
				}else{
					
					// Insert answer scores into drools session
					if (Map.class.isAssignableFrom(val.getClass())){
						Map<String, Object> value=(Map<String, Object>)val;
						if (value!=null){
							Integer score=value!=null && null!=value.get("score")? (Integer)value.get("score"):-1;
//							String pageId=value!=null && null!=value.get("pageId")?(String)value.get("pageId"):null;
//							String title= value!=null && null!=value.get("title")?( String)value.get("title"):null;
//							System.out.println("key="+e.getKey()+", pageId="+pageId+", lang="+language+", title="+title+", score="+score);
							DroolsSurveyAnswer a=new DroolsSurveyAnswer(e.getKey(), null, language, score, Lists.newArrayList((String)value.get("answer")), null);
							log.debug("Inserting fact: "+a);//(String.format("Answer: id=%s, page=%s, lang=%s, score=%s, text=%s", e.getKey(), language, (Integer)value.get("score"), (String)value.get("title") )));
							kSession.insert(a);
						}else{
						  System.out.println("hmm. value was null!");
						}
						
						try{
							if (value.containsKey("answer") && String.class.isAssignableFrom(value.get("answer").getClass()))
								kvReplacement.put(e.getKey(), (String)value.get("answer"));
							
							if (value.containsKey("answers") && List.class.isAssignableFrom(value.get("answers").getClass()))
								kvReplacement.put(e.getKey(), Joiner.on(",").join((List)value.get("answers")));
							
						}catch(Exception sink){
							log.error("Error with: "+Json.toJson(val));
							throw sink;
						}
						
						
					}else{// if (Integer.class.isAssignableFrom(val.getClass())){
						
					}
					 
				}
				
			}
			
			kSession.setGlobal("list", new LinkedList<>());
			
			kSession.fireAllRules();
			
			
			// A global list was used to retain the order of the DroolsRecommendation objects. fact insertions and extractions through geFactHandles does not retain order
			List<DroolsRecommendation> recommendations=(LinkedList<DroolsRecommendation>)kSession.getGlobal("list");
			
			if (extraDebug) log.debug("Found "+recommendations.size()+" recommendations:");
			
			// key/values replacements
			for (DroolsRecommendation r:recommendations){
				if (extraDebug) log.debug("Found Recommendation: "+r);
				
				// replace any key/values from the answers in the recommendation strings
				for (Entry<String, String> e:kvReplacement.entrySet()){
					if (null!=r.getText() && r.getText().contains("$"+e.getKey()))
						r.doKeyValueReplacement(e.getKey(), e.getValue());
				}
				
				if (r.getText().contains("$score"))
					r.doKeyValueReplacement("$score", kvReplacement.get("score_"+r.getSection().replaceAll(" ", "_")));
			}
			
			Map<String,Integer> thresholds=getThresholdRanges(decisionTableId);
			
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

}
