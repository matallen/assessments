package com.redhat.services.ae.plugins;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.drools.core.ObjectFilter;
import org.junit.jupiter.api.Test;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieRepository;
import org.kie.api.builder.Message.Level;
import org.kie.api.builder.ReleaseId;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.redhat.services.ae.MapBuilder;
import com.redhat.services.ae.recommendations.domain.Answer;
import com.redhat.services.ae.recommendations.domain.Recommendation;

public class DroolsTest{
	private KieServices kieServices=KieServices.get();
	
	@Test
	public void runDrl() throws IOException{
		String drl=IOUtils.toString(this.getClass().getClassLoader().getResource("drools-test.drl"), "UTF-8");
//		DroolsSurveyAnswer answer1=new DroolsSurveyAnswer("question1", "page1", "en", 0, Lists.newArrayList(new String[]{"answer1","answer2"}), "title");
		Collection<? extends Object> results=new DroolsExecutor().addDrl(drl)
		.addFact(new Answer.builder().questionId("subscriptions").addAnswer("OpenShift","ACM").build())
		.addFact(new Answer.builder().questionId("unhappyWithProducts").addAnswer("OpenShift").build())
		.execute(new ObjectFilter(){
			@Override
			public boolean accept(Object fact){
				return fact instanceof Recommendation;
			}
		});
		
		Map<String,String> kvReplacement=new MapBuilder<String,String>()
				.put("subscriptions",Joiner.on(",").join(Lists.newArrayList("OpenShift","ACM")))
				.put("unhappyWithProducts",Joiner.on(",").join(Lists.newArrayList("OpenShift")))
				.build();
		
		for(Recommendation r:(Collection<Recommendation>)results){
			for(Entry<String, String> e:kvReplacement.entrySet()){
				r.doKeyValueReplacement(e.getKey(), e.getValue());
			}
			
			System.out.println(r);
		}
		
	}
	
	
	
	@Test
	public void simpleTest() throws IOException{
		String[] drls=new String[]{IOUtils.toString(this.getClass().getClassLoader().getResource("ocpmigration-test-2.drl"), "UTF-8")};
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
		
	}
	
	@Test
	public void xtest(){
		Map<String,Object> surveyResults=new MapBuilder<String,Object>()
				.put("_timestamp", "2020-11-10T12:00:00")
				.put("_tacticId", "12345")
				.put("_FirstName", "Mat")
				.put("p1q1", "item1")
				.put("p1q2", "item2")
				.build();
		
		Map<String,String> answers=surveyResults.entrySet().stream()
				.filter(x->x.getKey().startsWith("_"))
				.collect(Collectors.toMap(y->y.getKey(),y->(String)y.getValue()));
//				surveyResults.entrySet().stream().filter(x->x.getKey().startsWith("_")).forEach(x->answers.put(x.getKey(), x.getValue()));;
		
		
		System.out.println(answers);
	}
}
