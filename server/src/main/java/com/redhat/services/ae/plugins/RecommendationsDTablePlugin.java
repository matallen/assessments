package com.redhat.services.ae.plugins;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.validation.metadata.ExecutableDescriptor;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.drools.template.ObjectDataCompiler;
import org.mortbay.log.Log;
import org.mvel2.MVEL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.redhat.services.ae.Initialization;
import com.redhat.services.ae.MapBuilder;
import com.redhat.services.ae.dt.GoogleDrive3_1;
import com.redhat.services.ae.plugins.XAccountCompassRecommendationsPlugin.Utils;
import com.redhat.services.ae.recommendations.domain.Recommendation;
import static com.redhat.services.ae.dt.GoogleDrive3_1.*;

public class RecommendationsDTablePlugin extends RecommendationsExecutor{
	public static final Logger log=LoggerFactory.getLogger(RecommendationsDTablePlugin.class);
	private static final GoogleDrive3_1 drive=Initialization.newGoogleDrive();
//	public Type getType(){ return Type.drlBuilder; }
	
	public List<String> getMandatoryConfigs(){ return Lists.newArrayList("decisionTableId","sheetName"); }
	
	
	@Override
	public List<String> getListOfDrlRules(String surveyId) throws Exception{
		log.info(this.getClass().getSimpleName()+":: getListOfDrlRules()");

		List<String> sheets=Splitter.on(",").trimResults().splitToList(getConfig("sheetName"));
		String sheetId=getConfig("decisionTableId");
		return compileSpreadsheetToDrls(surveyId, sheetId, sheets);
	}
	
//	@Override
//	public List<Recommendation> getListOfRecommendations(String surveyId, Map<String, Object> surveyResults) throws Exception{
//		log.info(this.getClass().getSimpleName()+":: getListOfRecommendations()");
//		
//		List<String> sheets=Splitter.on(",").trimResults().splitToList(getConfig("sheetName"));
//		String sheetId=getConfig("decisionTableId");
//		List<String> drls=compileSpreadsheetToDrls(surveyId, sheetId, sheets);
//		
//		List<Recommendation> recommendations=new RecommendationsPlugin().executeDrlRules(surveyResults, drls);
//		
//		log.debug(this.getClass().getSimpleName()+":: Added "+recommendations.size()+" recommendation(s) from this plugin");
//		return recommendations;
//	}
	
	private List<String> compileSpreadsheetToDrls(String surveyId, String sheetId, List<String> sheets) throws IOException, InterruptedException{
		List<String> result=Lists.newArrayList();
		// Add rules from configured sheet
		File sheet=drive.downloadFile(sheetId);
		SimpleDateFormat dateFormatter=null;
//		List<String> sheets=Lists.newArrayList("Section Recommendations", "Question Recommendations");
		int salience=65535;
		for(String sheetName:sheets){
			log.debug("TDablePlugin:: parsing sheet: "+sheetName);
			// Load the excel sheet with a retry loop?
			List<Map<String, String>> parseExcelDocument=null;
			parseExcelDocument=drive.parseExcelDocumentAsStrings(sheet, sheetName, new HeaderRowFinder(){ public int getHeaderRow(XSSFSheet s){
				return SheetSearch.get(s).find(0, "Description").getRowIndex();
			}}, dateFormatter);
			
			List<Map<String,Object>> dataTable = new ArrayList<>();
			for(Map<String, String> rows:parseExcelDocument){
				
				String answerWithLogic=null;
				if (null!=rows.get("Answer Value")){ // turn comma separated values into a 'contains' expression with &&'s
					List<String> answers=Splitter.on(",").splitToList(rows.get("Answer Value"));
					List<String> answersWithLogic=Lists.newArrayList();
					for (String a:answers)
						answersWithLogic.add("answers contains \""+a.trim()+"\"");
					answerWithLogic=Joiner.on(" || ").join(answersWithLogic);
					answerWithLogic="("+answerWithLogic+")";
				}
				
				dataTable.add(
						new MapBuilder<String,Object>()
							.put("salience", salience-=1)// 65534-Integer.parseInt(rows.get("ROW_#")))
							.put("language", Utils.defaultTo(rows.get("Language"), "en"))
							.put("description", Utils.makeTextSafeForCompilation(rows.get("Description"), "NoDescription")) // default to prevent rule compilation error on rule name
							
							.put("section", rows.get("Section"))
							.put("subSection", rows.get("Sub-section"))
							.put("scoreLow", rows.get("Score >=")!=null?(int)Double.parseDouble(rows.get("Score >=")):null)
							.put("scoreHigh", rows.get("Score <=")!=null?(int)Double.parseDouble(rows.get("Score <=")):null)
//							.put("answerValue", rows.get("Answer Value")!=null?rows.get("Answer Value"):null)
							.put("answerValue", answerWithLogic)
							
							.put("questionId", rows.get("Question ID"))
							
							.put("resultLevel1", rows.get("Result Level 1"))
							.put("resultLevel2", rows.get("Result Level 2"))
							.put("resultText", Utils.makeTextSafeForCompilation(rows.get("Text")))
							
							.build()
						);
			}
			
			String templateName="recommendationsPlugin_"+sheetName.replaceAll(" ", "")+".drt";
			InputStream template=XAccountCompassRecommendationsPlugin.class.getClassLoader().getResourceAsStream(templateName);
			String drl=new ObjectDataCompiler().compile(dataTable, template);
			if (extraDebug){
				log.debug("DRL rules produced from spreadsheet:\n"+drl);
			}
			result.add(drl);
		}
		return result;
	}
	
}
