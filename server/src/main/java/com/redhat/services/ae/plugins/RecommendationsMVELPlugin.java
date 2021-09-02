package com.redhat.services.ae.plugins;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.mvel2.MVEL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.redhat.services.ae.dt.GoogleDrive3;
import com.redhat.services.ae.recommendations.domain.Recommendation;

public class RecommendationsMVELPlugin extends RecommendationsExecutor{
	public static final Logger log=LoggerFactory.getLogger(RecommendationsMVELPlugin.class);
	private static final int DEFAULT_CACHE_EXPIRY_IN_MS=10000;
	private static final GoogleDrive3 drive=new GoogleDrive3(null!=System.getenv("GDRIVE_CACHE_EXPIRY_IN_MS")?Integer.parseInt(System.getenv("GDRIVE_CACHE_EXPIRY_IN_MS")):DEFAULT_CACHE_EXPIRY_IN_MS);
	public List<String> getMandatoryConfigs(){ return Lists.newArrayList("decisionTableId","sheetName"); }
	
	@Override
	public List<Recommendation> execute(String surveyId, Map<String, Object> surveyResults) throws Exception{
		log.info(this.getClass().getSimpleName()+":: Executing");
		
		String[] sheets=new String[]{getConfig("sheetName")};
		String sheetId=getConfig("decisionTableId");
		
		List<Recommendation> recommendations=Lists.newArrayList();
		List<Map<String, String>> parseExcelDocument=null;
		File sheet=drive.downloadFile(sheetId);
		SimpleDateFormat dateFormatter=null;
		List<String> insights=Lists.newArrayList();
		
		Map<String,Object> facts=Maps.newHashMap();
		facts.putAll(surveyResults);
		facts.put("insights", insights);
		
		for(String sheetName:sheets){
			parseExcelDocument=drive.parseExcelDocument(sheet, sheetName, new GoogleDrive3.HeaderRowFinder(){ public int getHeaderRow(XSSFSheet s){
				return GoogleDrive3.SheetSearch.get(s).find(0, "Rule name").getRowIndex();
			}}, dateFormatter);
			for(Map<String, String> rows:parseExcelDocument){
				String ruleName=rows.get("Rule name");
				String preParsedLhs=rows.get("Logic");
				String section=rows.get("Section");
				String title1=rows.get("Title 1");
				String title2=rows.get("Title 2");
				String insight=rows.get("Insight");
				String recommendation=rows.get("Recommendation Text");
				
				
				Object eval=MVEL.eval(preParsedLhs, facts);
//				System.out.println("eval="+eval);
				if (Boolean.class.isAssignableFrom(eval.getClass()) && (Boolean)eval){
					
					if (null!=insight){
						insights.add(insight);
						facts.put("insights", insights);
					}else{ //its a recommendation
						Recommendation r=new Recommendation.builder()
								.section(section)
								.header1(title1)
								.header2(title2)
								.text(recommendation)
								.build();
						recommendations.add(r);
					}
				}
			}
		}
		
		log.info(this.getClass().getSimpleName()+":: Added "+recommendations.size()+" recommendation(s) from this plugin");
		
		return recommendations;
	}
	
}
