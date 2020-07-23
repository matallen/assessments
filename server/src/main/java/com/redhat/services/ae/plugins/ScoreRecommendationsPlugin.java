package com.redhat.services.ae.plugins;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.drools.decisiontable.ExternalSpreadsheetCompiler;
import org.drools.decisiontable.InputType;
import org.drools.decisiontable.SpreadsheetCompiler;
import org.drools.template.ObjectDataCompiler;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieRepository;
import org.kie.api.builder.Message.Level;
import org.kie.api.builder.ReleaseId;
import org.kie.api.definition.KiePackage;
import org.kie.api.definition.rule.Rule;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.ObjectFilter;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.api.runtime.rule.FactHandle;
import org.kie.internal.KnowledgeBase;
import org.kie.internal.KnowledgeBaseFactory;
import org.kie.internal.io.ResourceFactory;
//import org.kie.kogito.rules.KieRuntimeBuilder;
import org.kie.internal.utils.KieHelper;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.redhat.services.ae.MapBuilder;
import com.redhat.services.ae.dt.GoogleDrive3;
import com.redhat.services.ae.model.Answer;
import com.redhat.services.ae.model.AnswerSurvey;
import com.redhat.services.ae.model.Recommendation;
import com.redhat.services.ae.model.Recommendation.RecommendationBuilder;

public class ScoreRecommendationsPlugin implements Plugin{

//	@Inject
//  private KieRuntimeBuilder runtimeBuilder;
	
	String decisionTableId;
	String decisionTableLocation;
	
	@Override
	public void setConfig(Map<String, Object> config){
		decisionTableLocation=(String)config.get("decisionTableLocation");
		decisionTableId=decisionTableLocation.substring(decisionTableLocation.lastIndexOf("/")+1);
	}

	
	private KieServices kieServices = KieServices.Factory.get();
	

  
//	public class Recommendation{
//		protected String text;
//		protected String section;
//		public Recommendation(){}
//		public Recommendation(String section, String text){
//			this.section=section;
//			this.text=text;
//		}
//		public String getText(){
//			return text;
//		}
//		public String getSection(){
//			return section;
//		}
//		public class RecommendationBuilder extends Recommendation{
//			public Recommendation build(){
//				return new Recommendation(section, text);
//			}
//			public RecommendationBuilder text(String value){
//				this.text=value; return this;
//			}
//			public RecommendationBuilder section(String value){
//				this.section=value; return this;
//			}
//		}
//	}
//  public class RecommendationConfig{
//  	public RecommendationConfig(){}
//  	public RecommendationConfig(String salience, String description, String overallScoreLow, String overallScoreHigh, String pageId, String questionId, Integer scoreLow , Integer scoreHigh, String recommendation){
//  		this.salience=salience;
//  		this.description=description;
//  		this.overallScoreLow=overallScoreLow;
//  		this.overallScoreHigh=overallScoreHigh;
//  		this.pageId=pageId;
//  		this.questionId=questionId;
//  		this.scoreLow=scoreLow;
//  		this.scoreHigh=scoreHigh;
//  		this.recommendation=recommendation;
//  	}
//  	
//  	private String salience;
//  	private String description;
//  	private String overallScoreLow;
//  	private String overallScoreHigh;
//  	private String pageId;
//  	private String questionId;
//  	private Integer scoreLow=null;
//  	private Integer scoreHigh=null;
//  	private String recommendation;
//  	
//  	
//		public String getSalience(){
//			return salience;
//		}
//		public void setSalience(String salience){
//			this.salience=salience;
//		}
//		public String getDescription(){
//			return description;
//		}
//		public void setDescription(String description){
//			this.description=description;
//		}
//		public String getOverallScoreLow(){
//			return overallScoreLow;
//		}
//		public void setOverallScoreLow(String overallScoreLow){
//			this.overallScoreLow=overallScoreLow;
//		}
//		public String getOverallScoreHigh(){
//			return overallScoreHigh;
//		}
//		public void setOverallScoreHigh(String overallScoreHigh){
//			this.overallScoreHigh=overallScoreHigh;
//		}
//		public String getPageId(){
//			return pageId;
//		}
//		public void setPageId(String pageId){
//			this.pageId=pageId;
//		}
//		public String getQuestionId(){
//			return questionId;
//		}
//		public void setQuestionId(String questionId){
//			this.questionId=questionId;
//		}
//		public Integer getScoreLow(){
//			return scoreLow;
//		}
//		public void setScoreLow(Integer scoreLow){
//			this.scoreLow=scoreLow;
//		}
//		public Integer getScoreHigh(){
//			return scoreHigh;
//		}
//		public void setScoreHigh(Integer scoreHigh){
//			this.scoreHigh=scoreHigh;
//		}
//		public String getRecommendation(){
//			return recommendation;
//		}
//		public void setRecommendation(String recommendation){
//			this.recommendation=recommendation;
//		}
//  	
//  }
  
	public KieSession newKieSession(Path... ps) throws IOException {
  	KieServices ks = KieServices.Factory.get();
    KieFileSystem kfs = ks.newKieFileSystem();
    KieRepository kieRepository = kieServices.getRepository();
    ReleaseId krDefaultReleaseId = kieRepository.getDefaultReleaseId();
    kfs.generateAndWritePomXML(krDefaultReleaseId);
    kfs.generateAndWritePomXML(kieServices.getRepository().getDefaultReleaseId());
    for(Path p:ps){
    	System.out.println("adding file: "+p.getFileName());
    	kfs.write("/src/main/resources/"+p.getFileName().toString(), ks.getResources().newInputStreamResource(java.nio.file.Files.newInputStream(p, StandardOpenOption.READ)));
    	
    }
    KieBuilder kb = ks.newKieBuilder(kfs).buildAll();
    
    if (kb.getResults().getMessages(Level.ERROR).size() != 0) {
      System.out.println(String.format("File compilation error: %s", kb.getResults().getMessages()));
    }
    KieContainer kieContainer = kieServices.newKieContainer(krDefaultReleaseId);
    KieSession ksession = kieContainer.newKieSession();
    return ksession;
	}
	
	public KieSession newKieSession(String... drls) throws IOException {
  	
  	KieServices ks = KieServices.Factory.get();
    KieFileSystem kfs = ks.newKieFileSystem();
    kfs.generateAndWritePomXML(kieServices.getRepository().getDefaultReleaseId());
    int i=0;
    for(String drl:drls){
//    	kfs.write( "src/main/resources/rule.xslx", kieServices.getResources().newInputStreamResource( fis ) );
//    	kfs.write( "src/main/resources/"+"drl"+(i+=1)+".drl", kieServices.getResources().newInputStreamResource(new ByteArrayInputStream(drl.getBytes())));
    	kfs.write( "src/main/resources/"+"drl"+(i+=1)+".drl", drl.getBytes());
//    	kfs.write("drl"+(i+=1)+".drl", ks.getResources().newInputStreamResource(new ByteArrayInputStream(drl.getBytes())));
    }
    
//    kfs.write(ks.getResources().newInputStreamResource(java.nio.file.Files.newInputStream(Paths.get(""), StandardOpenOption.READ)));
    KieBuilder kb = ks.newKieBuilder(kfs).buildAll();
    if (kb.getResults().getMessages(Level.ERROR).size() != 0) {
        System.out.println(String.format("File compilation error: %s", kb.getResults().getMessages()));
    }
    
//    KieFileSystem kieFileSystem = kieServices.newKieFileSystem().write(dt);
//    KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem).buildAll();
    
    KieRepository kieRepository = kieServices.getRepository();
    ReleaseId krDefaultReleaseId = kieRepository.getDefaultReleaseId();
    KieContainer kieContainer = kieServices.newKieContainer(krDefaultReleaseId);
    KieSession ksession = kieContainer.newKieSession();
    return ksession;
	}
	
	
	private KieSession newKSessionAgain(String drl){
		KieServices ks = KieServices.Factory.get();
//    Resource dt = ResourceFactory.newByteArrayResource(bytes)  ClassPathResource("dtables/StateInterest.xls");
		Resource dt = ResourceFactory.newByteArrayResource(drl.getBytes());
		KieFileSystem kfs=ks.newKieFileSystem();
		kfs.generateAndWritePomXML(kieServices.getRepository().getDefaultReleaseId());
		KieFileSystem kieFileSystem = kfs.write("rules.drl", dt);
    KieBuilder kieBuilder = ks.newKieBuilder(kieFileSystem);
    kieBuilder.buildAll();
    KieRepository kieRepository = ks.getRepository();
    ReleaseId krDefaultReleaseID = kieRepository.getDefaultReleaseId();
    KieContainer kieContainer = ks.newKieContainer(krDefaultReleaseID);
    KieSession kSession = kieContainer.newKieSession();
    return kSession;
	}

	@Override
	public Map<String, Object> execute(String surveyId, String visitorId, Map<String, Object> surveyResults) throws Exception{
		try{

			GoogleDrive3.initialise("/home/%s/google_drive", GoogleDrive3.DriverType.gdrive, "v2.1.1PreRelease");
			GoogleDrive3 drive=new GoogleDrive3(3000);
			File sheet=drive.downloadFile("19d03Qi0mr-9mcfYp9__sjNkFJcGCx2zT4D26NYH1US4");
			SimpleDateFormat dateFormatter=null;
			List<Map<String, String>> parseExcelDocument=drive.parseExcelDocument(sheet, "Sheet2", new GoogleDrive3.HeaderRowFinder(){ public int getHeaderRow(XSSFSheet s){
//				return 0;
				return GoogleDrive3.SheetSearch.get(s).find(0, "Description").getRowIndex();
			}}, dateFormatter);
			
			
			KieSession kSession;
			boolean drls=true;
			if (!drls){
				
				kSession = newKieSession(sheet.toPath());
				
			}else{
				
				List<Map<String,Object>> dataTableConfigList2 = new ArrayList<>();
				for(Map<String, String> rows:parseExcelDocument){
					dataTableConfigList2.add(
						new MapBuilder<String,Object>()
						.put("salience", rows.get("ROW_#"))
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
				
//				List<RecommendationConfig> dataTableConfigList = new ArrayList<>();
//				for(Map<String, String> rows:parseExcelDocument){
//					
//					System.out.println("adding a recConfig");
//					
//					RecommendationConfig rc=new RecommendationConfig();
//					rc.salience=rows.get("ROW_#");
//					rc.description=rows.get("Description");
//					rc.overallScoreLow=rows.get("Overall Score >=");
//					rc.overallScoreHigh=rows.get("Overall Score <=");
//					rc.pageId=rows.get("Category / Page");
//					rc.questionId=rows.get("QuestionId");
//					rc.scoreLow=rows.get("Question Score >=")!=null?(int)Double.parseDouble(rows.get("Question Score >=")):null; // the int appears to arrive out the sheet as a double. downcasting is ok here because the int data structure is smaller
//					rc.scoreHigh=rows.get("Question Score <=")!=null?(int)Double.parseDouble(rows.get("Question Score <=")):null;
//					rc.recommendation=rows.get("Recommedation");
//					dataTableConfigList.add(rc);
//				}
				InputStream template = ScoreRecommendationsPlugin.class.getClassLoader().getResourceAsStream("scorePlugin.drt");
				ObjectDataCompiler compiler = new ObjectDataCompiler();
//				ExternalSpreadsheetCompiler compiler2=new ExternalSpreadsheetCompiler();
				String drl = compiler.compile(dataTableConfigList2, template);
				System.out.println(drl);
				
				kSession = newKieSession(drl);
			}
			
			// DEBUG ONLY - make sure we have some rule packages to execute
			System.out.println("Rule Packages/Rules:");
			for(KiePackage pkg:kSession.getKieBase().getKiePackages()){
				for (Rule r:pkg.getRules()){
					System.out.println(" - "+pkg.getName()+"."+r.getName());
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
						Answer a=new Answer(e.getKey(), language, (Integer)value.get("score"), (String)value.get("title"));
						System.out.println("Inserting fact: "+(String.format("Answer: id=%s, lang=%s, score=%s, text=%s", e.getKey(), language, (Integer)value.get("score"), (String)value.get("title") )));
						kSession.insert(a);
					}
					if (value.containsKey("answer") && String.class.isAssignableFrom(value.get("answer").getClass()))
						kvReplacement.put(e.getKey(), (String)value.get("answer"));
					
					if (value.containsKey("answers") && List.class.isAssignableFrom(value.get("answers").getClass()))
						kvReplacement.put(e.getKey(), Joiner.on(",").join((List)value.get("answers")));
					
				}else{// if (Integer.class.isAssignableFrom(val.getClass())){
					if ("averageScore".equals(e.getKey()) && Integer.class.isAssignableFrom(val.getClass())){
						kSession.insert(new AnswerSurvey((Integer)val, language));
					}
				}
				
			}
			
			kSession.fireAllRules();
			
			Map<String,List<String>> sections=new MapBuilder<String,List<String>>().build();
			
			// extract the recommendations from the drools session
			List<Recommendation> recommendations=Lists.newArrayList();
			for(FactHandle fh : kSession.getFactHandles(new ObjectFilter(){
				public boolean accept(Object object){
					return object instanceof Recommendation;
			}}).stream().toArray(FactHandle[]::new)){
				System.out.println(
						kSession.getObject(fh)
				);
				Recommendation recommendation=(Recommendation)kSession.getObject(fh);
				
				// replace any key/values from the answers in the recommendation strings
				for (Entry<String, String> e:kvReplacement.entrySet()){
					if (recommendation.getText().contains("$"+e.getKey())){
						recommendation=Recommendation.builder().section(recommendation.getSection()).text(recommendation.getText().replaceFirst("\\$"+e.getKey(), e.getValue())).build();
					}
				}
				
				
				if (!sections.containsKey(recommendation.getSection()))
					sections.put(recommendation.getSection(), Lists.newArrayList());
				
//				sections.get(recommendation.getSection()).put("recommendation", recommendation);
				sections.get(recommendation.getSection()).add(recommendation.getText());
				
				
				recommendations.add(recommendation);
			}
			
			
			
			
			
			// add recommendations to the results
//			surveyResults.put("report", new MapBuilder<String,Object>().put("recommendations",recommendations).build());
			surveyResults.put("report", sections);
			
			
			if (true)
				return surveyResults;
			
			
//			dataTableConfigList.add(new RecommendationConfig(null, null, "platforms_q1", 1, 20, "this question recommendation"));
//			dataTableConfigList.add(new RecommendationConfig(null, "page1", null, 21, 40, "this is a page recommendation"));
		  
//			for(Entry<String, Object> e:surveyResults.entrySet()){
//				Map<String, Object> value=(Map<String, Object>)e.getValue();
//				factList.add(new Answer(e.getKey(), (Integer)value.get("score"), (String)value.get("title")));
//			}
			
			
			
			
//			KieSession kSession = newKieSession(drl);//ResourceFactory.newByteArrayResource(drl.getBytes()));
//			KieContainer kieContainer = KieServices.Factory.get().getKieClasspathContainer();
//			KieBase kieBase = kieContainer.getKieBase("RecommendationsKieBase");
//			KieSession kieSession = kieBase.newKieSession();
			
//			KieContainer kieContainer = KieServices.Factory.get().getKieClasspathContainer();
//			KieBase kieBase = kieContainer.getKieBase("RecommendationsKieBase");
//			KieSession kieSession = kieBase.newKieSession();
//			
//			kieSession.insert(new Answer(3));
//			kieSession.fireAllRules();
			
			
//			GoogleDrive3.initialise("/home/%s/google_drive", GoogleDrive3.DriverType.gdrive, "v2.1.1PreRelease");
//			GoogleDrive3 drive=new GoogleDrive3(3000);
//			File sheet=drive.downloadFile("19d03Qi0mr-9mcfYp9__sjNkFJcGCx2zT4D26NYH1US4");
//			String sheetName="Sheet1";
			File sheetx=new File("/home/mallen/google_drive/19d03Qi0mr-9mcfYp9__sjNkFJcGCx2zT4D26NYH1US4/Red Hat Assessment Recommendation Rules - DMZZIW.xls");
			
//			sheet=new XlsxToXLs().convert(sheet, new File(sheet.getParentFile(), "Red Hat Assessment Recommendation Rules - DMZZIW.xls"));
			
			
			System.out.println("datatable: "+sheet.getAbsolutePath());
			// xls to drl
//			Resource dt = ResourceFactory.newClassPathResource("rules.xls", getClass());
//			KieSession kSession = newKieSession(dt);
			String drlRules=new SpreadsheetCompiler().compile(new FileInputStream(sheet), sheet.getName().toLowerCase().endsWith("csv")?InputType.CSV:InputType.XLS);
			System.out.println("\n"+drlRules);
			
//			String drlRules="";
//			kieFileSystem kieFileSystem = kieServices.newKieFileSystem().write(ResourceFactory.newInputStreamResource(new ByteArrayInputStream(drlRules.getBytes())));
//	    KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem).buildAll();
//	    Results buildResults=kieBuilder.getResults();
//	    for (Message msg:buildResults.getMessages()){
//	    	System.out.println("Build Message: "+msg.getText());
//	    }
//			KieContainer kieContainer = kieServices.newKieContainer(kieServices.getRepository().getDefaultReleaseId());
//	    KieSession kSession = kieContainer.newKieSession();
//			Resource dt=ResourceFactory.newInputStreamResource(new ByteArrayInputStream(drlRules.getBytes()));
			
			KieSession kieSession = new KieHelper().addContent(drlRules, ResourceType.DRL).build().newKieSession();
			
//			KieSession kSession=newKieSession(dt);
			
			// now add drl rules to ksession
			
			for(Entry<String, Object> e:surveyResults.entrySet()){
				System.out.println(e.getKey());
//				kSession.insert(new Answer());
			}
			
//			
//			KieServices ks = KieServices.Factory.get();
//			KieContainer kContainer = ks.getKieClasspathContainer();
//			KieSession kSession = kContainer.newKieSession("ksession-rule");
			
			Object fact1=new Object();
			
//			KieSession kSession=runtimeBuilder.newKieSession();
			
//			FactHandle fact1handle = kSession.insert(fact1);
//			
//			kSession.fireAllRules();
			
		}catch(Throwable t){
			t.printStackTrace();
		}
		
		return null;
	}

}
