package com.redhat.services.ae.controllers;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.annotations.jaxrs.PathParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Preconditions;
import com.redhat.services.ae.MapBuilder;
import com.redhat.services.ae.model.Survey;
import com.redhat.services.ae.utils.Json;
import com.redhat.services.ae.utils.Pair;

@Path("/api/purge/surveys")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CleanupController{
	public static final Logger log=LoggerFactory.getLogger(CleanupController.class);

	/**
	 * This class is intended to expose endpoints to trigger database data cleanup, such as prior months visitor/page entried to prevent duplicate metrics 
	 * 
	 */
	
	
	@GET
	@Path("/{surveyId}/metrics/purgeOlderThan")
	public Response purgeMetrics(@PathParam("surveyId") String surveyId, @QueryParam("date") String pTargetDate, @QueryParam("testMode") String pTestMode) throws JsonProcessingException, ParseException{
		SimpleDateFormat sdf=new SimpleDateFormat("yy-MMM");
		Pair<Boolean,Date> checks=getParametersForPurge(pTestMode, pTargetDate, sdf);
		Boolean testMode=checks.getFirst();
		Date targetDate=checks.getSecond();

		Survey s=Survey.findById(surveyId);
		Map<String,Object> result=new LinkedHashMap<>();
		for (Entry<String, Object> e:s.getMetrics().entrySet()){
			String metricCategory=e.getKey();
			log.debug("Looking in '"+metricCategory+"' metrics...");
			// level 2 should always be a YY-MMM
			for (Entry<String, Object> met:((Map<String,Object>)e.getValue()).entrySet()){
				Date yearMonth=sdf.parse(met.getKey());
				log.debug("check: is '"+sdf.format(yearMonth)+"' before or equal to '"+sdf.format(targetDate)+"' ? "+ (yearMonth.equals(targetDate) || yearMonth.before(targetDate)));
				if (yearMonth.equals(targetDate) || yearMonth.before(targetDate)){
					// add to purge list
					result.put(metricCategory, new MapBuilder<String,Object>().put(met.getKey(), met.getValue()).build());
				}
			}
		}
		
		for (Entry<String, Object> e:result.entrySet()){
			String metricCategory=e.getKey();
			for (Entry<String, Object> toPurge:((Map<String,Object>)e.getValue()).entrySet()){
				
				log.debug("[testMode="+testMode+"] purging "+metricCategory+"."+toPurge.getKey()+"");
				if (!testMode)
					((Map<String,Object>)s.getMetrics().get(metricCategory)).remove(toPurge.getKey());
				
			}
		}
		s.saveMetrics();
		
		return Response.ok().entity(Json.toJson(result)).build();
	}
	
	@GET
	@Path("/{surveyId}/results/purgeOlderThan")
	public Response purgeReports(@PathParam("surveyId") String surveyId, @QueryParam("date") String pTargetDate, @QueryParam("testMode") String pTestMode) throws ParseException, IOException{
		SimpleDateFormat sdf=new SimpleDateFormat("yy-MMM");
		Pair<Boolean,Date> checks=getParametersForPurge(pTestMode, pTargetDate, sdf);
		Boolean testMode=checks.getFirst();
		Date targetDate=checks.getSecond();
		
		Survey s=Survey.findById(surveyId);
		Map<String, Object> results=s.getResults();
		List<String> toPurgeKeys=new ArrayList<>();
		log.debug("results.size()="+results.size());
		for (Entry<String, Object> e:results.entrySet()){
			String resultId=e.getKey();
			
			if (resultId.matches("[0-9]{6}.+")){ // check that the resultId contains the timestamp at the start
				Date resultDate=new SimpleDateFormat("yyMM").parse(resultId.substring(0,4));
				
				log.debug("check: is '"+sdf.format(resultDate)+"' before or equal to '"+sdf.format(targetDate)+"' ? "+ (resultDate.equals(targetDate) || resultDate.before(targetDate)));
				if (resultDate.equals(targetDate) || resultDate.before(targetDate)){
					toPurgeKeys.add(resultId);
				}
				
			}else{
				toPurgeKeys.add(resultId); // if resultId doesnt start with a datestamp then it's legacy and can be purged
			}
		}
		
		for(String toPurge:toPurgeKeys){
			log.debug("[testMode="+testMode+"] purging "+s.id+".Results."+toPurge+"");
			if (!testMode)
				results.remove(toPurge);
		}
		s.saveResults();
		
		return Response.ok().entity(Json.toJson(toPurgeKeys)).build();
	}
	
	private Pair<Boolean,Date> getParametersForPurge(String pTestMode, String pTargetDate, SimpleDateFormat sdf) throws ParseException{
		Preconditions.checkArgument(pTargetDate!=null && pTargetDate.matches("[0-9]{2}-[a-zA-Z]{3}"), "'date' parameter needs to be provided and in 'YY-MMM' format. Found "+pTargetDate);
		boolean testMode=pTestMode!=null && pTestMode.equalsIgnoreCase("true");
		System.out.println("xxx"+pTargetDate);
		Date targetDate=sdf.parse(pTargetDate);
		log.debug("before date found: "+sdf.format(targetDate));
		return new Pair<Boolean, Date>(testMode, targetDate);
	}
	
//@GET
//@Path("/{surveyId}/purgeOlderThan")
//public Response purgeAll(@PathParam("surveyId") String surveyId, @QueryParam("date") String pTargetDate, @QueryParam("testMode") String testMode, @QueryParam("purgeWhat") String purgeWhat) throws ParseException, IOException{
//	Preconditions.checkArgument(purgeWhat!=null, "must specifiy what to purge. A comma separated list of 'metrics' and/or 'results'");
//			
//	List<String> purgeWhatList=Lists.newArrayList(purgeWhat.toLowerCase().split(","));
//	Map<String,Object> result=new HashMap<String, Object>();
//	if (purgeWhatList.contains("metrics"))
//		result.put("metricsPurged", purgeMetrics(surveyId, pTargetDate, testMode).getEntity());
//	if (purgeWhatList.contains("reports"))
//		result.put("resultsPurged", purgeReports(surveyId, pTargetDate, testMode).getEntity());
//	
//	return Response.ok().entity(Json.toJson(result)).build();
//}

}
