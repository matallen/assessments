package com.redhat.services.ae.plugins;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.redhat.services.ae.Initialization;
import com.redhat.services.ae.dt.GoogleDrive3_1;
import com.redhat.services.ae.model.Survey;
import com.redhat.services.ae.recommendations.domain.Answer;
import com.redhat.services.ae.recommendations.domain.Insight;
import com.redhat.services.ae.recommendations.domain.Recommendation;
import static com.redhat.services.ae.dt.GoogleDrive3_1.*;

import mjson.Json;

public class RecommendationsNaturalLanguagePlugin extends RecommendationsExecutor{
	public static final Logger log=LoggerFactory.getLogger(RecommendationsNaturalLanguagePlugin.class);
	private static final GoogleDrive3_1 drive=Initialization.newGoogleDrive();
//	public Type getType(){ return Type.drlBuilder; }
	
	public List<String> getMandatoryConfigs(){ return Lists.newArrayList("decisionTableId","sheetName"); }
	
	public RecommendationsNaturalLanguagePlugin(){
		if (!drive.isInitialised())
			new Initialization().onStartup(null);
	}
	
	
	@Override
	public List<String> getListOfDrlRules(String surveyId) throws Exception{
		log.info(this.getClass().getSimpleName()+":: getListOfDrlRules()");

		List<String> sheets=Splitter.on(",").trimResults().splitToList(getConfig("sheetName"));
		String sheetId=getConfig("decisionTableId");
		return compileNaturalLanguageToDrls(surveyId, sheetId, sheets);
	}
	
//	@Override
//	public List<Recommendation> getListOfRecommendations(String surveyId, Map<String, Object> surveyResults) throws Exception{
//		log.info(this.getClass().getSimpleName()+":: getListOfRecommendations()");
//		String[] sheets=new String[]{getConfig("sheetName")};
//		String sheetId=getConfig("decisionTableId");
//		
//		List<Recommendation> recommendations=Lists.newArrayList();
//		
//		List<String> drls=compileNaturalLanguageToDrls(surveyId, sheetId, sheets);
//		List<Recommendation> recommendations2=new RecommendationsPlugin().executeDrlRules(surveyResults, drls);
//		
//		log.info(this.getClass().getSimpleName()+":: Added "+recommendations.size()+" recommendation(s) from this plugin");
//		
//		recommendations.addAll(recommendations2);
//		return recommendations;
//	}
	
	protected List<String> getQuestionNames(String surveyId) throws JsonProcessingException{
		List<String> result=Lists.newArrayList();
		Survey s=Survey.findById(surveyId);
		Json json=mjson.Json.read(s.getQuestionsAsString());
		for (mjson.Json p:json.at("pages").asJsonList()){
			for (mjson.Json q:p.at("elements").asJsonList()){
				String name=q.at("name").asString();
				result.add(name);
			}
		}
		return result;
//		return Lists.newArrayList("subscriptions", "orgSize", "happiness");
	}
	
	private List<String> compileNaturalLanguageToDrls(String surveyId, String sheetId, List<String> sheets) throws IOException, InterruptedException{
		List<String> result=Lists.newArrayList();
		File sheet=drive.downloadFile(sheetId);
		SimpleDateFormat dateFormatter=null;
		List<Map<String, String>> parseExcelDocument=null;
		
		String drl="package com.redhat.services.ae\n\n";
		drl+="import com.redhat.services.ae.recommendations.domain.Insight;\n";
		drl+="import com.redhat.services.ae.recommendations.domain.Answer;\n";
		drl+="import com.redhat.services.ae.recommendations.domain.Recommendation;\n";
		drl+="global java.util.LinkedList list\n\n";
		for(String sheetName:sheets){
			parseExcelDocument=drive.parseExcelDocumentAsStrings(sheet, sheetName, new HeaderRowFinder(){ public int getHeaderRow(XSSFSheet s){
				return SheetSearch.get(s).find(0, "Rule name").getRowIndex();
			}}, dateFormatter);
			
			int ruleCount=0;
			for(Map<String, String> rows:parseExcelDocument){
				String ruleName=rows.get("Rule name");
				String preParsedLhs=rows.get("Logic");
				String section=rows.get("Section");
				String title1=rows.get("Title 1");
				String title2=rows.get("Title 2");
				String recommendation=rows.get("Recommendation Text");
				
				List<String> lhs=parseLHS(preParsedLhs, getQuestionNames(surveyId));
				
				drl+=String.format("rule \"%s\"\nwhen\n", ruleName);
				for (String f:lhs)
					drl+="\t"+f+"\n";
				drl+="then\n\t";
				drl+=String.format("insert(new Recommendation(%s));", "\""+Joiner.on("\",\"").skipNulls().join(Lists.newArrayList(section,title1,title2,recommendation))+"\"");
				drl+="\nend\n\n";
				ruleCount+=1;
			}
			if (extraDebug)
				log.debug(Recommendation.class.getSimpleName()+":: Added "+ruleCount+" drl rules from "+sheetId+"/"+sheetName);
			
			result.add(drl);
		}
		
		if (extraDebug)
			for(String r:result){
				log.debug(r);
//				System.out.println(r);
			}
		return result;
	}
	
	private List<String> parseLHS(String rawLhs, List<String> questionNames){
		// split by operator (&&, ||, and, or)
		List<String> result=Lists.newArrayList();
		
		// eg. Answer(question=="test", answers contains "value")
		if (rawLhs.trim().startsWith(Answer.class.getSimpleName()+"(") || rawLhs.trim().startsWith(Insight.class.getSimpleName()+"(")){
			result.add(rawLhs);
		}else{
			// parse out the condition lines so we can convert from natural language to DRL
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
				
				int resultSizeBefore=result.size();
				for(String q:questionNames)
					if (f.contains(q)){
						String condition=f.replaceFirst(q, "");
						String answerField=condition.matches(".*(equals|==).*")?"answer":"answers";
						String fragment=(null!=op?" "+op+" ":"")+Answer.class.getSimpleName()+"(question==\""+q+"\", "+answerField+" "+condition+")";
						result.add(fragment);
					}
				
				if (result.size()==resultSizeBefore){
					Matcher m=Pattern.compile("(.+)(contains|not contains|includes|not includes|equals|==)(.+)").matcher(f);
					if (m.find()){
						String entity=m.group(1).trim();
						String op2=m.group(2);
//						if (op2.contains("includes")) op2=op2.replaceAll("includes", "contains");
						String comparison=m.group(3).trim();
						String answerField=op2.matches(".*(equals|==).*")?"answer":"answers";
						result.add((null!=op?" "+op+" ":"")+Answer.class.getSimpleName()+"(question==\""+entity+"\", "+answerField+" "+op2+" "+comparison+")");
					}
				}
			}
		}
		return result;
	}
	
}
