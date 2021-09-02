package com.redhat.services.ae.plugins;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.validation.metadata.ExecutableDescriptor;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.drools.template.ObjectDataCompiler;
import org.mortbay.log.Log;
import org.mvel2.MVEL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.redhat.services.ae.MapBuilder;
import com.redhat.services.ae.dt.GoogleDrive3;
import com.redhat.services.ae.plugins.AccountCompassRecommendationsPlugin.Utils;
import com.redhat.services.ae.recommendations.domain.Recommendation;

public class RecommendationsNaturalLanguagePlugin extends RecommendationsExecutor{
	public static final Logger log=LoggerFactory.getLogger(RecommendationsNaturalLanguagePlugin.class);
	private static final int DEFAULT_CACHE_EXPIRY_IN_MS=10000;
	private static final GoogleDrive3 drive=new GoogleDrive3(null!=System.getenv("GDRIVE_CACHE_EXPIRY_IN_MS")?Integer.parseInt(System.getenv("GDRIVE_CACHE_EXPIRY_IN_MS")):DEFAULT_CACHE_EXPIRY_IN_MS);
	public List<String> getMandatoryConfigs(){ return Lists.newArrayList("decisionTableId","sheetName"); }
	
	@Override
	public List<Recommendation> execute(String surveyId, Map<String, Object> surveyResults) throws Exception{
		log.info(this.getClass().getSimpleName()+":: Executing");
		String[] sheets=new String[]{getConfig("sheetName")};
		String sheetId=getConfig("decisionTableId");
		
		List<Recommendation> recommendations=Lists.newArrayList();
		
		List<String> drls=compileNaturalLanguageToDrls(sheetId, sheets);
		List<Recommendation> recommendations2=new RecommendationsPlugin().executeDrlRules(surveyResults, drls.toArray(new String[drls.size()]));
		
		log.info(this.getClass().getSimpleName()+":: Added "+recommendations.size()+" recommendation(s) from this plugin");
		
		recommendations.addAll(recommendations2);
		return recommendations;
	}
	
	private List<String> compileNaturalLanguageToDrls(String sheetId, String...sheets) throws IOException, InterruptedException{
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
			parseExcelDocument=drive.parseExcelDocument(sheet, sheetName, new GoogleDrive3.HeaderRowFinder(){ public int getHeaderRow(XSSFSheet s){
				return GoogleDrive3.SheetSearch.get(s).find(0, "Rule name").getRowIndex();
			}}, dateFormatter);
			
			for(Map<String, String> rows:parseExcelDocument){
				String ruleName=rows.get("Rule name");
				String preParsedLhs=rows.get("Logic");
				String section=rows.get("Section");
				String title1=rows.get("Title 1");
				String title2=rows.get("Title 2");
				String recommendation=rows.get("Recommendation Text");
				
				List<String> lhs=parseLHS(preParsedLhs, Lists.newArrayList("subscriptions", "orgSize", "happiness"));
				
				drl+=String.format("rule \"%s\"\nwhen\n", ruleName);
				for (String f:lhs)
					drl+="\t"+f+"\n";
				drl+="then\n\t";
				drl+=String.format("insert(new Recommendation(%s));", "\""+Joiner.on("\",\"").skipNulls().join(Lists.newArrayList(section,title1,title2,recommendation))+"\"");
				drl+="\nend\n\n";
			}
			result.add(drl);
		}
		
		if (extraDebug)
			for(String r:result)
				log.debug(r);
		return result;
	}
	
	private List<String> parseLHS(String rawLhs, List<String> questionNames){
		// split by operator (&&, ||, and, or)
		List<String> result=Lists.newArrayList();
		if (rawLhs.trim().startsWith("Answer") || rawLhs.trim().startsWith("Insight")){
			result.add(rawLhs);
		}else{
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
				
				for(String q:questionNames)
					if (f.contains(q)){
						String condition=f.replaceFirst(q, "");
						String fragment=(null!=op?" "+op+" ":"")+"Answer(question==\""+q+"\", answers "+condition+")";
						result.add(fragment);
					}
			}
		}
		return result;
	}
	
}
