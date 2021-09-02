package com.redhat.services.ae.plugins;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
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
import com.google.protobuf.Value;
import com.redhat.services.ae.Initialization;
import com.redhat.services.ae.MapBuilder;
import com.redhat.services.ae.dt.GoogleDrive3;
import com.redhat.services.ae.model.Survey;
import com.redhat.services.ae.plugins.droolsscore.DroolsSurveySection;
import com.redhat.services.ae.plugins.droolsscore.ResultsBuilderTabsOverview;
import com.redhat.services.ae.recommendations.domain.Answer;
import com.redhat.services.ae.recommendations.domain.Recommendation;

/*
  "ResultsProducer": {
    "active": true,
    "extraDebug": true,
    "className": "com.redhat.services.ae.plugins.ResultsProducerPlugin",
    "executors": [
      {
        "className": "com.redhat.services.ae.plugins.MVELExecutor"
      }
    ],
    "decisionTableLocation": "https://docs.google.com/spreadsheets/d/1eZ6_Yy7Go7RK2yh3SsUGJZ2jvXbVk3IdPiTO5emTzz0",
    "configSheetName": "Config",
    "thresholdSection": "Report Thresholds",
    "includeOverviewTab": false
  },
 */

public class RecommendationsPlugin extends Plugin{
	public static final Logger log=LoggerFactory.getLogger(RecommendationsPlugin.class);
	private static final int DEFAULT_CACHE_EXPIRY_IN_MS=10000;
	private static final GoogleDrive3 drive=new GoogleDrive3(null!=System.getenv("GDRIVE_CACHE_EXPIRY_IN_MS")?Integer.parseInt(System.getenv("GDRIVE_CACHE_EXPIRY_IN_MS")):DEFAULT_CACHE_EXPIRY_IN_MS);
	private List<RecommendationsExecutor> executors=Lists.newArrayList();
	private boolean extraDebug=false;
	private boolean includeOverviewTab;
	public static Integer noScore=null;//-1000; // the score for a question if there is no score specified/found (it cannot be null as it throws NPE)
	private Map<String,Integer> sectionScores=Maps.newHashMap();
	private Map<String,String> kvReplacements=Maps.newHashMap();
	
	public RecommendationsPlugin(){
		if (!drive.isInitialised()) new Initialization().onStartup(null);
	}

	@Override
	public Plugin setConfig(Map<String, Object> config){
		// add these to configMap instead
		String decisionTableLocation=getConfigValueAsString(config, "decisionTableLocation", null);
		includeOverviewTab=getBooleanFromConfig(config, "includeOverviewTab", true);
		extraDebug=getBooleanFromConfig(config, "extraDebug", false);
		List<String> configErrors=Lists.newLinkedList();
		
		try{
			if (List.class.isAssignableFrom(config.get("executors").getClass())){
				List<Map<String,String>> executorsConfig=(List<Map<String,String>>)config.get("executors");
				for(Map<String, String> executorConfig:executorsConfig){
					RecommendationsExecutor executor=(RecommendationsExecutor)Class.forName((String)executorConfig.get("className")).newInstance();
					if (!executorConfig.containsKey("decisionTableLocation") && null!=decisionTableLocation){
						executorConfig.put("decisionTableLocation", decisionTableLocation); // add this from parent plugin if iti snt specified in executor
						executorConfig.put("decisionTableId", decisionTableLocation.substring(decisionTableLocation.lastIndexOf("/")+1));
					}
					executor.setConfig(executorConfig);
					executor.extraDebug=extraDebug;
					configErrors.addAll(executor.getConfigErrors());
					executors.add(executor);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		if (executors.size()<=0) throw new RuntimeException("no 'executors' configured for "+this.getClass().getSimpleName()+" plugin");
		if (configErrors.size()>0) throw new RuntimeException(Joiner.on(",").join(configErrors));
		return this;
	}
	
	
	@Override
	@SuppressWarnings("rawtypes")
	public Map<String, Object> execute(String surveyId, String visitorId, Map<String, Object> surveyResults) throws Exception{
		
		// fire the executors so they update the survey results _result entity
		List<Recommendation> recommendations=Lists.newArrayList();
		for(RecommendationsExecutor e:executors){
			recommendations.addAll(e.execute(surveyId, surveyResults));
		}
		
		// check if there are technical rules on the Survey and execute those too
		if (StringUtils.isNotBlank(getSurveyDrlRules(surveyId))){
			recommendations.addAll(executeDrlRules(surveyResults, new String[]{getSurveyDrlRules(surveyId)}));
		}
		
		log.info("Found "+recommendations.size()+" recommendation"+(recommendations.size()!=1?"s":"")+" in total:");
		for (Recommendation r:recommendations)
			if (extraDebug) log.debug(" - "+r);

		
		Map<String,String> surveyResults2=surveyResults.entrySet().stream()
				.collect(Collectors.toMap(Map.Entry::getKey, e -> List.class.isAssignableFrom(e.getValue().getClass())?Joiner.on(", ").join((List)e.getValue()):e.getValue().toString()));
		kvReplacements.putAll(surveyResults2);
		doRecommendationTextReplacements(recommendations, kvReplacements);
		
		Map<String,Integer> thresholds=new MapBuilder<String,Integer>().put("Basic", 32).put("Improving", 50).put("Accelerating", 100).build();
//		Map<String,Integer> thresholds=getThresholdRanges(decisionTableId);
		
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
	}
	
//	public static Predicate<Map.Entry<String, Object>> isNotNullAndNotDoesntStartWith(String... startsWithAny) {
//		return e -> e.getValue()!=null && !( Stream.of(startsWithAny).anyMatch(s -> e.getKey().startsWith(s)) );
//	}
//	public static Predicate<Map.Entry<String, Object>> isSectionScore() {
//		return e -> e.getValue()!=null && e.getKey().equals("_sectionScore");
//	}
	public static Predicate<Map.Entry<String, Object>> startsWith(String... startsWithAny) {
		return e -> ( Stream.of(startsWithAny).anyMatch(s -> e.getKey().startsWith(s)) );
	}
	public static Predicate<Map.Entry<String, Object>> isNotNull() {
		return e -> e.getValue()!=null;
	}
	
	@SuppressWarnings("unchecked")
	protected List<Recommendation> executeDrlRules(Map<String, Object> surveyResults, String... drls) throws IOException, DroolsCompilationException{
		
		KieSession kSession=newKieSession(drls);
		
		if (extraDebug){ // make sure we have some rule packages to execute
			log.debug("Rule Packages/Rules:");
			for(KiePackage pkg:kSession.getKieBase().getKiePackages()){
				for (Rule r:pkg.getRules()) log.debug(" - "+pkg.getName()+"."+r.getName());
			}
		}
		String language=surveyResults.containsKey("language")?(String)surveyResults.get("language"):"en"; // default to en
		
		// Insert the sections (if they exist)
		for(Entry<String, Object> e: surveyResults.entrySet().stream().filter(isNotNull().and(startsWith("_sectionScore"))).collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue())).entrySet() ){
			for(Entry<String, Integer> e2:((Map<String, Integer>)e.getValue()).entrySet()){
				String sectionName=e2.getKey();
				DroolsSurveySection a=new DroolsSurveySection(sectionName, language, e2.getValue());
				log.info("Inserting fact: "+a);
				kvReplacements.put("score_"+sectionName.replaceAll(" ", "_"), String.valueOf(e2.getValue()));
				sectionScores.put(sectionName, e2.getValue());
				kSession.insert(a);
			}
		}
		
		// Insert the question/answer facts into the drools session
		for(Entry<String, Object> e: surveyResults.entrySet().stream().filter(isNotNull().and(startsWith("_","C_").negate())).collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue())).entrySet() ){
			
			Answer a=null;
			if (Map.class.isAssignableFrom(e.getValue().getClass())){
				Map<String, Object> value=(Map<String, Object>)e.getValue(); // enriched structure - it should have a pageId, navTitle, score (optional) & an answer
				Integer score=value.containsKey("score") && Integer.class.isAssignableFrom(value.get("score").getClass())?(Integer)value.get("score"):noScore;
				if (value.containsKey("answer")) // it's a single string answer
					a=new Answer.builder().questionId(e.getKey()).language(language).score(score).addAnswer((String)value.get("answer"))/*.pageId((String)value.get("pageId")).title((String)value.get("title"))*/.build();
				if (value.containsKey("answers")) // it's a list of strings answer
					a=new Answer.builder().questionId(e.getKey()).language(language).score(score).answer((List<String>)value.get("answers"))/*.pageId((String)value.get("pageId")).title((String)value.get("title"))*/.build();
				
			}else if (List.class.isAssignableFrom(e.getValue().getClass())){ // non-enriched structure - plain list of answers (radio or checkbox) (no scoring possible)
				a=new Answer.builder().questionId(e.getKey()).language(language).answer((List<String>)e.getValue()).build();
				
			}else if (String.class.isAssignableFrom(e.getValue().getClass())){ // non-enriched structure - plain string answer (boolean or textbox?) (no scoring possible)
				a=new Answer.builder().questionId(e.getKey()).language(language).addAnswer((String)e.getValue()).build();
			}
			
			if (null!=a){
				log.debug("Inserting fact: "+a);
				kSession.insert(a);
			}
		}
		
		kSession.setGlobal("list", new LinkedList<>());
		kSession.fireAllRules();
		
		
		List<Recommendation> recommendations=Lists.newArrayList();
		if (null!=(LinkedList<Recommendation>)kSession.getGlobal("list")){
			List<Recommendation> recsInList=(LinkedList<Recommendation>)kSession.getGlobal("list");
			log.debug(String.format("%s:: Discovered %s recommendations in the drools global LIST", this.getClass().getSimpleName(), recsInList.size()));
			recommendations.addAll(recsInList);
		}
		
		
		// if there are no recommendations in the list, then perhaps they're in the working session as facts
		if (null==recommendations || recommendations.size()<=0){
			Collection<? extends Object> resultFacts=kSession.getObjects(new ObjectFilter(){public boolean accept(Object fact){ return fact instanceof Recommendation; }});
			log.debug(String.format("%s:: Discovered %s recommendations in the drools working session", this.getClass().getSimpleName(), resultFacts.size()));
			recommendations.addAll(resultFacts.stream()
			.map(Recommendation.class::cast)
			.collect(Collectors.toList()));
		}
		
		
		return recommendations;
	}
	
	private void doRecommendationTextReplacements(List<Recommendation> recommendations, Map<String,String> kvReplacement){
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
	}
	
	private String getSurveyDrlRules(String surveyId){
		Survey s=Survey.findById(surveyId);
		String result=null;
		if (StringUtils.isNotBlank(s.getRules())){
			if (extraDebug) log.debug("The Survey's ("+surveyId+") Technical rules are not blank, compiling:\n"+s.getRules());
			result=s.getRules(); // technical drl rules
		}
		return result;
	}
	
	private KieServices kieServices = KieServices.Factory.get();
	private KieSession newKieSession(String... drls) throws IOException,DroolsCompilationException {
//  	KieServices ks = KieServices.Factory.get();
    KieFileSystem kfs = kieServices.newKieFileSystem();
    kfs.generateAndWritePomXML(kieServices.getRepository().getDefaultReleaseId());
    int i=0;
    for(String drl:drls) kfs.write( "src/main/resources/com/redhat/services/ae/"+"drl"+(i+=1)+".drl", drl.getBytes());
    KieBuilder kb = kieServices.newKieBuilder(kfs).buildAll();
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
