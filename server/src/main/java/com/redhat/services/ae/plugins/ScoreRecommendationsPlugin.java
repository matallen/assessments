package com.redhat.services.ae.plugins;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.drools.decisiontable.InputType;
import org.drools.decisiontable.SpreadsheetCompiler;
import org.drools.template.ObjectDataCompiler;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieRepository;
import org.kie.api.builder.ReleaseId;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
//import org.kie.kogito.rules.KieRuntimeBuilder;
import org.kie.internal.utils.KieHelper;

import com.redhat.services.ae.model.Answer;

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
	
  public KieSession newKieSession(Resource dt) {
    KieFileSystem kieFileSystem = kieServices.newKieFileSystem().write(dt);
    KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem).buildAll();
    KieRepository kieRepository = kieServices.getRepository();
    ReleaseId krDefaultReleaseId = kieRepository.getDefaultReleaseId();
    KieContainer kieContainer = kieServices.newKieContainer(krDefaultReleaseId);
    KieSession ksession = kieContainer.newKieSession();
    return ksession;
	}
  
  
  class RecommendationConfig{
  	public RecommendationConfig(Integer scoreLow , Integer scoreHeigh, String recommendation){
  		this.scoreLow=scoreLow;
  		this.scoreHeigh=scoreHeigh;
  		this.recommendation=recommendation;
  	}
  	Integer scoreLow=null;
  	Integer scoreHeigh=null;
  	String recommendation;
  }
  
	@Override
	public Map<String, Object> execute(String surveyId, String visitorId, Map<String, Object> surveyResults) throws Exception{
		
		
		try{
			
			
			InputStream template = ScoreRecommendationsPlugin.class.getClassLoader().getResourceAsStream("scorePlugin.drt");

			
			ObjectDataCompiler compiler = new ObjectDataCompiler();
			List<RecommendationConfig> dataTableConfigList = new ArrayList<>();
			dataTableConfigList.add(new RecommendationConfig(1, 20, "this recommendation"));
			dataTableConfigList.add(new RecommendationConfig(21, 40, "the other recommendation"));
		  
//			for(Entry<String, Object> e:surveyResults.entrySet()){
//				Map<String, Object> value=(Map<String, Object>)e.getValue();
//				factList.add(new Answer(e.getKey(), (Integer)value.get("score"), (String)value.get("title")));
//			}
			
			
			String drl = compiler.compile(dataTableConfigList, template);
			
			System.out.println(drl);
			
			
//			KieContainer kieContainer = KieServices.Factory.get().getKieClasspathContainer();
//			KieBase kieBase = kieContainer.getKieBase("RecommendationsKieBase");
//			KieSession kieSession = kieBase.newKieSession();
//			
//			kieSession.insert(new Answer(3));
//			kieSession.fireAllRules();
			if (true)
				return surveyResults;
			
			
//			GoogleDrive3.initialise("/home/%s/google_drive", GoogleDrive3.DriverType.gdrive, "v2.1.1PreRelease");
//			GoogleDrive3 drive=new GoogleDrive3(3000);
//			File sheet=drive.downloadFile("19d03Qi0mr-9mcfYp9__sjNkFJcGCx2zT4D26NYH1US4");
//			String sheetName="Sheet1";
			File sheet=new File("/home/mallen/google_drive/19d03Qi0mr-9mcfYp9__sjNkFJcGCx2zT4D26NYH1US4/Red Hat Assessment Recommendation Rules - DMZZIW.xls");
			
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
			
			KieSession kSession = new KieHelper().addContent(drlRules, ResourceType.DRL).build().newKieSession();
			
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
