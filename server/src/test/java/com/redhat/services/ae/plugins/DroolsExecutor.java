package com.redhat.services.ae.plugins;

import java.util.Collection;
import java.util.List;

import org.drools.core.ObjectFilter;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieRepository;
import org.kie.api.builder.Message.Level;
import org.kie.api.builder.ReleaseId;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

import com.google.common.collect.Lists;

public class DroolsExecutor{
	private KieServices kieServices=KieServices.get();
	private List<String> drls=Lists.newArrayList();
	private List<Object> facts=Lists.newArrayList();
	
	public DroolsExecutor addDrl(String... drls){
		for(String drl:drls) this.drls.add(drl);
		return this;
	}
	
	public DroolsExecutor addFact(Object... facts){
		for(Object fact:facts) this.facts.add(fact);
		return this;
	}
	
	public KieSession newKSession(){
//		String[] drls=new String[]{IOUtils.toString(this.getClass().getClassLoader().getResource("ocpmigration-test-2.drl"), "UTF-8")};
		KieServices ks = KieServices.Factory.get();
	  KieFileSystem kfs = ks.newKieFileSystem();
	  kfs.generateAndWritePomXML(kieServices.getRepository().getDefaultReleaseId());
	  int i=0;
	  for(String drl:drls)
	  	kfs.write( "src/main/resources/com/redhat/services/ae/"+"drl"+(i+=1)+".drl", drl.getBytes());
	  
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
	
	public Collection<? extends Object> execute(ObjectFilter filter){
		KieSession ksession=newKSession();
		for(Object fact:facts)
			ksession.insert(fact);
		ksession.fireAllRules();
		
		Collection<? extends Object> results=ksession.getObjects(filter);
		
		return results;
		
	}
}
