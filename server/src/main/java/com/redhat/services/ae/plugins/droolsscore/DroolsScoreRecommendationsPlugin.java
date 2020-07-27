package com.redhat.services.ae.plugins.droolsscore;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import com.redhat.services.ae.MapBuilder;
import com.redhat.services.ae.dt.GoogleDrive3;
import com.redhat.services.ae.plugins.Plugin;

public class DroolsScoreRecommendationsPlugin implements Plugin{
	public static final Logger log=LoggerFactory.getLogger(DroolsScoreRecommendationsPlugin.class);
	
//	@Inject
//  private KieRuntimeBuilder runtimeBuilder;
	
	String decisionTableId;
	String decisionTableLocation;
	private KieServices kieServices = KieServices.Factory.get();
	
	@Override
	public void setConfig(Map<String, Object> config){
		decisionTableLocation=(String)config.get("decisionTableLocation");
		decisionTableId=decisionTableLocation.substring(decisionTableLocation.lastIndexOf("/")+1);
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
	
	
		private List<String> drls=null;
		
		public KieSession newKieSession(String sheetId) throws IOException, InterruptedException{
			
			if (null==drls){
				GoogleDrive3.initialise("/home/%s/google_drive", GoogleDrive3.DriverType.gdrive, "v2.1.1PreRelease");
				GoogleDrive3 drive=new GoogleDrive3(3000);
				File sheet=drive.downloadFile(sheetId);
				SimpleDateFormat dateFormatter=null;
				
				List<String> sheets=Lists.newArrayList("Survey Recommendations", "Question Recommendations");
				drls=Lists.newArrayList();
				for(String sheetName:sheets){
					
					// Load the excel sheet with a retry loop?
					List<Map<String, String>> parseExcelDocument=null;
					parseExcelDocument=drive.parseExcelDocument(sheet, sheetName, new GoogleDrive3.HeaderRowFinder(){ public int getHeaderRow(XSSFSheet s){
						return GoogleDrive3.SheetSearch.get(s).find(0, "Description").getRowIndex();
					}}, dateFormatter);
					
					
					List<Map<String,Object>> dataTableConfigList2 = new ArrayList<>();
					for(Map<String, String> rows:parseExcelDocument){
						dataTableConfigList2.add(
								new MapBuilder<String,Object>()
								.put("salience", 65534-Integer.parseInt(rows.get("ROW_#")))
								.put("language", rows.get("Language"))
								.put("description", rows.get("Description"))
								.put("overallScoreLow", rows.get("Overall Score >="))
								.put("overallScoreHigh", rows.get("Overall Score <="))
								.put("pageId", rows.get("Category / Page"))
								.put("questionId", rows.get("QuestionId"))
								.put("scoreLow", rows.get("Question Score >=")!=null?(int)Double.parseDouble(rows.get("Question Score >=")):null)
								.put("scoreHigh", rows.get("Question Score <=")!=null?(int)Double.parseDouble(rows.get("Question Score <=")):null)
								.put("section", rows.get("Section"))
								.put("recommendation", rows.get("Recommedation"))
								.build()
								);
					}
					
					String templateName="scorePlugin_"+sheetName.replaceAll(" ", "")+".drt";
					InputStream template = DroolsScoreRecommendationsPlugin.class.getClassLoader().getResourceAsStream(templateName);
					ObjectDataCompiler compiler = new ObjectDataCompiler();
					String drl = compiler.compile(dataTableConfigList2, template);
					System.out.println(drl);
					drls.add(drl);
				}
			}
			
			return newKieSession(drls.toArray(new String[drls.size()]));
			
		}
		
		public void invalidateRules(){
			drls=null;
		}
		
	
	
	@Override
	public Map<String, Object> execute(String surveyId, String visitorId, Map<String, Object> surveyResults) throws Exception{
		try{
			
			
//			GoogleDrive3.initialise("/home/%s/google_drive", GoogleDrive3.DriverType.gdrive, "v2.1.1PreRelease");
//			GoogleDrive3 drive=new GoogleDrive3(3000);
//			File sheet=drive.downloadFile("19d03Qi0mr-9mcfYp9__sjNkFJcGCx2zT4D26NYH1US4");
//			SimpleDateFormat dateFormatter=null;
//			
//			KieSession kSession;
//			
//			List<String> sheets=Lists.newArrayList("Survey Recommendations", "Question Recommendations");
//			List<String> drls=Lists.newArrayList();
//			for(String sheetName:sheets){
//				
//				// Load the excel sheet with a retry loop?
//				List<Map<String, String>> parseExcelDocument=null;
//				parseExcelDocument=drive.parseExcelDocument(sheet, sheetName, new GoogleDrive3.HeaderRowFinder(){ public int getHeaderRow(XSSFSheet s){
//					return GoogleDrive3.SheetSearch.get(s).find(0, "Description").getRowIndex();
//				}}, dateFormatter);
//				
//				
//				List<Map<String,Object>> dataTableConfigList2 = new ArrayList<>();
//				for(Map<String, String> rows:parseExcelDocument){
//					dataTableConfigList2.add(
//						new MapBuilder<String,Object>()
//						.put("salience", 65534-Integer.parseInt(rows.get("ROW_#")))
//						.put("language", rows.get("Language"))
//						.put("description", rows.get("Description"))
//						.put("overallScoreLow", rows.get("Overall Score >="))
//						.put("overallScoreHigh", rows.get("Overall Score <="))
//						.put("pageId", rows.get("Category / Page"))
//						.put("questionId", rows.get("QuestionId"))
//						.put("scoreLow", rows.get("Question Score >=")!=null?(int)Double.parseDouble(rows.get("Question Score >=")):null)
//						.put("scoreHigh", rows.get("Question Score <=")!=null?(int)Double.parseDouble(rows.get("Question Score <=")):null)
//						.put("section", rows.get("Section"))
//						.put("recommendation", rows.get("Recommedation"))
//						.build()
//					);
//				}
//				
//				String templateName="scorePlugin_"+sheetName.replaceAll(" ", "")+".drt";
//				InputStream template = DroolsScoreRecommendationsPlugin.class.getClassLoader().getResourceAsStream(templateName);
//				ObjectDataCompiler compiler = new ObjectDataCompiler();
//				String drl = compiler.compile(dataTableConfigList2, template);
//				System.out.println(drl);
//				drls.add(drl);
//			}
//			kSession = newKieSession(drls.toArray(new String[drls.size()]));
			
			KieSession kSession=newKieSession("19d03Qi0mr-9mcfYp9__sjNkFJcGCx2zT4D26NYH1US4");
			
			// DEBUG ONLY - make sure we have some rule packages to execute
			log.debug("Rule Packages/Rules:");
			for(KiePackage pkg:kSession.getKieBase().getKiePackages()){
				for (Rule r:pkg.getRules()){
					log.debug(" - "+pkg.getName()+"."+r.getName());
				}
			}
			
			
			// build a map of question/value for string replacement in the recommendations
			Map<String,String> kvReplacement=new HashMap<String, String>();
			
			String language=surveyResults.containsKey("language")?(String)surveyResults.get("language"):"en";
			
			// Insert the question/answer facts into the drools session
			for(Entry<String, Object> e:surveyResults.entrySet()){
				Object val=e.getValue();
				
				if (Map.class.isAssignableFrom(val.getClass())){
					Map<String, Object> value=(Map<String, Object>)val;
					if (value.containsKey("score")){
						DroolsSurveyAnswer a=new DroolsSurveyAnswer(e.getKey(), (String)value.get("pageId"), language, (Integer)value.get("score"), (String)value.get("title"));
						System.out.println("Inserting fact: "+a);//(String.format("Answer: id=%s, page=%s, lang=%s, score=%s, text=%s", e.getKey(), language, (Integer)value.get("score"), (String)value.get("title") )));
						kSession.insert(a);
					}
					if (value.containsKey("answer") && String.class.isAssignableFrom(value.get("answer").getClass()))
						kvReplacement.put(e.getKey(), (String)value.get("answer"));
					
					if (value.containsKey("answers") && List.class.isAssignableFrom(value.get("answers").getClass()))
						kvReplacement.put(e.getKey(), Joiner.on(",").join((List)value.get("answers")));
					
				}else{// if (Integer.class.isAssignableFrom(val.getClass())){
					if ("averageScore".equals(e.getKey()) && Integer.class.isAssignableFrom(val.getClass())){
						kSession.insert(new DroolsSurveyScore((Integer)val, language));
					}
				}
				
			}
			
			kSession.fireAllRules();
			
			Map<String,List<String>> sections=new MapBuilder<String,List<String>>().build();
			
			// extract the recommendations from the drools session
			List<DroolsRecommendation> recommendations=Lists.newArrayList();
			for(FactHandle fh : kSession.getFactHandles(new ObjectFilter(){
				public boolean accept(Object object){
					return object instanceof DroolsRecommendation;
			}}).stream().toArray(FactHandle[]::new)){
				System.out.println(
						kSession.getObject(fh)
				);
				DroolsRecommendation recommendation=(DroolsRecommendation)kSession.getObject(fh);
				
				// replace any key/values from the answers in the recommendation strings
				for (Entry<String, String> e:kvReplacement.entrySet()){
					if (recommendation.getText().contains("$"+e.getKey())){
						recommendation=DroolsRecommendation.builder().section(recommendation.getSection()).text(recommendation.getText().replaceFirst("\\$"+e.getKey(), e.getValue())).build();
					}
				}
				
				if (!sections.containsKey(recommendation.getSection()))
					sections.put(recommendation.getSection(), Lists.newArrayList());
				
				sections.get(recommendation.getSection()).add(recommendation.getText());
				
				recommendations.add(recommendation);
			}
			
			
			// add recommendations to the results
			surveyResults.put("report", sections);
			
			return surveyResults;
			
			
		}catch(Throwable t){
			t.printStackTrace();
		}
		
		return null;
	}

}
