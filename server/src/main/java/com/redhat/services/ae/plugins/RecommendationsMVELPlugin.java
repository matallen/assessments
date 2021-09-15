package com.redhat.services.ae.plugins;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.mvel2.MVEL;
import org.mvel2.integration.impl.CachingMapVariableResolverFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.redhat.services.ae.Initialization;
import com.redhat.services.ae.dt.GoogleDrive3_1;
import com.redhat.services.ae.recommendations.domain.Recommendation;

public class RecommendationsMVELPlugin extends RecommendationsExecutor{
	public static final Logger logger=LoggerFactory.getLogger(RecommendationsMVELPlugin.class);
	public static final MyLogger log=MyLogger.getLogger(RecommendationsMVELPlugin.class);
	static class MyLogger{
		public static MyLogger getLogger(Class clz){
			return new MyLogger();
		}
		public void error(String msg){
			logger.error(msg);
			System.err.println(this.getClass().getSimpleName()+":: ERROR:: "+msg);
		}
		public void info(String msg){
			logger.info(msg);
			System.out.println(this.getClass().getSimpleName()+":: INFO:: "+msg);
		}
		public void debug(String msg){
			logger.debug(msg);
			System.out.println(this.getClass().getSimpleName()+":: DEBUG:: "+msg);
		}
	}
	private static final GoogleDrive3_1 drive=Initialization.newGoogleDrive();
//	public Type getType(){ return Type.recommender; }
	public List<String> getMandatoryConfigs(){ return Lists.newArrayList("decisionTableId","sheetName"); }

	public RecommendationsMVELPlugin(){
		if (!drive.isInitialised()) new Initialization().onStartup(null);
	}
	
	public Map<String,Object> execute(String surveyId, Map<String,Object> wm) throws Exception{
		log.info(this.getClass().getSimpleName()+":: execute()");
		String[] sheets=new String[]{getConfig("sheetName")};
		File sheet=drive.downloadFile(getConfig("decisionTableId"));
		List<Map<String, String>> parseExcelDocument=null;
		SimpleDateFormat dateFormatter=null;
		
		log.debug("Answers/facts in:");
		for(Entry<String, Object> e:wm.entrySet())
			log.debug(" - "+e.getKey()+" = "+e.getValue());
		
		if (!wm.containsKey("insights")) wm.put("insights", Lists.newArrayList());
		if (!wm.containsKey("recommendations")) wm.put("recommendations", Lists.newArrayList());
		
		for(String sheetName:sheets){
			System.out.println("MVEL: executing sheet: "+sheetName);
			parseExcelDocument=drive.parseExcelDocumentAsStrings(sheet, sheetName, new GoogleDrive3_1.HeaderRowFinder(){ public int getHeaderRow(XSSFSheet s){
				return GoogleDrive3_1.SheetSearch.get(s).find(0, "Rule name").getRowIndex();
			}}, dateFormatter);
			for(Map<String, String> rows:parseExcelDocument){
				String ruleName=rows.get("Rule name");
				String preParsedLhs=rows.get("Logic");
				String section=rows.get("Section");
				String title1=rows.get("Title 1");
				String title2=rows.get("Title 2");
				String insight=rows.get("Insight");
				String scoreGroup=rows.get("Score group");
				String score=rows.get("Score");
				String recommendation=rows.get("Recommendation Text");
				
				preParsedLhs=improveExpression(preParsedLhs, wm); // convert some too "cody" syntax to more english syntax
				
				try{
					Object eval=MVEL.eval(preParsedLhs, new MissingVariableResolvableFactory(wm));
					log.debug("MVELEval():: "+preParsedLhs+" = "+eval);
					if (Boolean.class.isAssignableFrom(eval.getClass()) && (Boolean)eval){
						
						if (null!=insight){
							log.debug("MVEL:: Adding Insight to working memory - "+insight);
							((List<String>)(wm.get("insights"))).add(insight);
						}
						if (null!=recommendation){
							Recommendation r=new Recommendation.builder()
									.section(section)
									.header1(title1)
									.header2(title2)
									.text(recommendation)
									.build();
//							((List<Recommendation>)(wm.get("recommendations"))).add(r);
							log.debug("MVEL:: Adding Recommendation to working memory - "+r);
							wm.put(r.toString(), r);
						}
						if (null!=scoreGroup && null!=score){
							Double cumulativeScore=wm.containsKey(scoreGroup)?(Double)wm.get(scoreGroup):0;
							wm.put(scoreGroup, cumulativeScore+Double.parseDouble(score));
							log.debug("MVEL:: updating scoregroup ["+scoreGroup+"] - adding ["+score+"] to make ["+wm.get(scoreGroup)+"]");
						}
						
					}
					
				}catch(Exception sink){
					log.error("error processing expression: "+ preParsedLhs);
				}
			}
		}
		
		// just for info purposes
		List<Recommendation> recommendations=wm.values().stream()
			.filter(e->Recommendation.class.isAssignableFrom(e.getClass()))
			.map(e->(Recommendation)e)
			.collect(Collectors.toList());
		log.info(this.getClass().getSimpleName()+":: Added "+recommendations.size()+" recommendation(s) from this plugin");
		
		
		return wm;
	}
	
	@Override
	public List<Recommendation> getListOfRecommendations(String surveyId, Map<String, Object> surveyResults) throws Exception{
		log.info(this.getClass().getSimpleName()+":: getListOfRecommendations()");
		
		String[] sheets=new String[]{getConfig("sheetName")};
		String sheetId=getConfig("decisionTableId");
		
		List<Recommendation> recommendations=Lists.newArrayList();
		List<Map<String, String>> parseExcelDocument=null;
		File sheet=drive.downloadFile(sheetId);
		SimpleDateFormat dateFormatter=null;
		
		Map<String,Object> facts=Maps.newHashMap();
		// convert all strings to list<string>, leave boolean and numeric alone
		for (Entry<String, Object> e:surveyResults.entrySet()){
			if (String.class.isAssignableFrom(e.getValue().getClass())){
				log.debug("wrapping ("+e.getKey()+") in a string list");
				// if the string contains commas, split them up
				facts.put(e.getKey(), Splitter.on(",").trimResults().splitToList(((String)e.getValue())));// this is so all strings are lists and therefore "contains" can be consistently used in the mvel expressions
				
//				facts.put(e.getKey(), Lists.newArrayList(e.getValue())); 
//			}else if (Boolean.class.isAssignableFrom(e.getValue().getClass())){
//			}else if (Integer.class.isAssignableFrom(e.getValue().getClass())){
//			}else if (List.class.isAssignableFrom(e.getValue().getClass())){
			}else
				facts.put(e.getKey(), e.getValue());
		}
		
//		facts.putAll(surveyResults);
		facts.put("insights", Lists.newArrayList());
		
		log.debug("Answers/facts in:");
		for(Entry<String, Object> e:facts.entrySet()){
			log.debug(" - "+e.getKey()+" = "+e.getValue());
		}
		
		
		for(String sheetName:sheets){
			System.out.println("MVEL: executing sheet: "+sheetName);
			parseExcelDocument=drive.parseExcelDocumentAsStrings(sheet, sheetName, new GoogleDrive3_1.HeaderRowFinder(){ public int getHeaderRow(XSSFSheet s){
				return GoogleDrive3_1.SheetSearch.get(s).find(0, "Rule name").getRowIndex();
			}}, dateFormatter);
			for(Map<String, String> rows:parseExcelDocument){
				String ruleName=rows.get("Rule name");
				String preParsedLhs=rows.get("Logic");
				String section=rows.get("Section");
				String title1=rows.get("Title 1");
				String title2=rows.get("Title 2");
				String insight=rows.get("Insight");
				String scoreGroup=rows.get("Score group");
				String score=rows.get("Score");
				
				String recommendation=rows.get("Recommendation Text");
				
				preParsedLhs=improveExpression(preParsedLhs, facts); // convert some too "cody" syntax to more english syntax
				
				try{
					Object eval=MVEL.eval(preParsedLhs, new MissingVariableResolvableFactory(facts));
					log.debug("MVELEval():: "+preParsedLhs+" = "+eval);
					if (Boolean.class.isAssignableFrom(eval.getClass()) && (Boolean)eval){
						
						if (null!=insight){
							log.debug("Adding Insight to working memory - "+insight);
							((List<String>)(facts.get("insights"))).add(insight);
						}
						if (null!=recommendation){
							Recommendation r=new Recommendation.builder()
									.section(section)
									.header1(title1)
									.header2(title2)
									.text(recommendation)
									.build();
							recommendations.add(r);
						}
						if (null!=scoreGroup && null!=score){
							Integer cumulativeScore=facts.containsKey(scoreGroup)?(Integer)facts.get(scoreGroup):0;
							facts.put(scoreGroup, cumulativeScore+Integer.parseInt(score));
							log.debug("MVEL: updating scoregroup ["+scoreGroup+"] to ["+facts.get(scoreGroup)+"]");
						}
						
					}
					
				}catch(Exception sink){
					log.error("error processing expression: "+ preParsedLhs);
				}
			}
		}
		
		log.info(this.getClass().getSimpleName()+":: Added "+recommendations.size()+" recommendation(s) from this plugin");
		
		return recommendations;
	}
	
	private String improveExpression(String exp, Map<String, Object> facts){
		exp=exp.replaceAll(" (?i)or ", " || "); // case-insentive replacement of " or " to " || "
		exp=exp.replaceAll(" (?i)and ", " && "); // case-insentive replacement of "and" to &&'s
		exp=exp.replaceAll(" (?i)includes ", " contains "); // case-insentive replacement of "and" to &&'s
		
//		// Logic to try to cover when someone forgets to quote wrap an insight - doesnt quite work yet, commenting for now, low priority
//		List<String> factsAndInsights=Lists.newArrayList();
//		factsAndInsights.addAll(facts.keySet());
//		factsAndInsights.addAll(((List<String>)facts.get("insights")));
//		
//		for(String fact:facts.keySet()){
//			if (!exp.matches("\""+fact+"\"") && exp.matches(" "+fact+" ")){
//				exp=exp.replaceAll(fact, "\""+fact+"\"");
//			}
//		}
		return exp;
	}
	

	// variables now always exist, they can be compared to "x == null", or  "x == empty" .. scratch that, it's now a list rather than null so you can say .size<=0
	public class MissingVariableResolvableFactory extends CachingMapVariableResolverFactory{
		public MissingVariableResolvableFactory(Map variables) { super(variables); }
		@Override
		public boolean isResolveable(String name) {
			if(!super.isResolveable(name))
				variables.put(name, /*null*/ Lists.newArrayList());
			return true;
		}
	}
	
}
