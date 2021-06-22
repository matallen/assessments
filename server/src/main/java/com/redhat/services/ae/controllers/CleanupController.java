package com.redhat.services.ae.controllers;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.annotations.jaxrs.PathParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
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
	
	// TODO: add dataformat to the params so we can take some complexity out the front end
	// TOO: add exclusions, so we can purge metrics EXCEPT the industry averages for example
	
	@GET
	@Path("/{surveyId}/metrics/purgeOlderThan")
	public Response purgeMetrics(
			@PathParam("surveyId") String surveyId, 
			@QueryParam("date") String pTargetDate, 
			@QueryParam("except") String pExcept, 
			@QueryParam("testMode") String pTestMode
			) throws JsonProcessingException, ParseException{
		SimpleDateFormat sdf=new SimpleDateFormat("yy-MMM");
		Pair<Boolean,Date> checks=getParametersForPurge(pTestMode, pTargetDate, sdf);
		Boolean testMode=checks.getFirst();
		Date targetDate=checks.getSecond();
		
		
		List<String> except=pExcept!=null?Splitter.on(",").splitToList(pExcept):Lists.newArrayList();
		
		Survey s=Survey.findById(surveyId);
		Map<String,Object> result=new LinkedHashMap<>();
		for (Entry<String, Object> e:s.getMetrics().entrySet()){
			String metricCategory=e.getKey();
			log.debug("Looking in '"+metricCategory+"' metrics...");
			
			if (except.contains(metricCategory)){
				log.debug("Skipping: '"+metricCategory+"' is in exception list");
				continue;
			}
			
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
	
	
	@DELETE
	@Path("/{surveyId}/results")
	public Response deleteResults(@PathParam("surveyId") String surveyId, String payloadJson) throws JsonParseException, JsonMappingException, IOException, ParseException{
		System.out.println("deleteResults:: payload="+payloadJson);
		Survey s=Survey.findById(surveyId);
		List<String> resultIdsToDelete=Json.toObject(payloadJson, new TypeReference<ArrayList<String>>(){});
		for (String id:resultIdsToDelete){
			s.getResults().remove(id);
			System.out.println("deletResults:: delete result with id "+id);
		}
		s.save();
		s.saveResults();
		return listResults(surveyId);
	}
	
	@GET
	@Path("/{surveyId}/results")
	public Response listResults(
			@PathParam("surveyId") String surveyId
//			@QueryParam("fields") String fieldsList,
//			@QueryParam("dateRange") String pDateRange
			) throws ParseException, IOException{
		
		Survey s=Survey.findById(surveyId);
		Map<String, Object> results=s.getResults();
		
		List<Map<String,Object>> result=new ArrayList<>();
		for (Entry<String, Object> e:results.entrySet()){
			Map<String,Object> entry=new HashMap<>();
			entry.put("id", e.getKey());
			Map<String,Object> surveyResults=Json.toObject((String)e.getValue(), new TypeReference<Map<String,Object>>(){});
			// add all informational fields (ie. those starting with an underscore)
			for(Entry<String, Object> f:surveyResults.entrySet()){
				if (f.getKey().startsWith("_"))
					entry.put(f.getKey(), f.getValue());
			}
			
			// remove any unnecessary fields
			entry.remove("_report");
			
			result.add(entry);
		}
		
		return Response.ok().entity(Json.toJson(result)).build();
	}
	
	
	
	@GET
	@Path("/{surveyId}/results/purgeOlderThan")
	public Response purgeReports(
			@PathParam("surveyId") String surveyId, 
			@QueryParam("date") String pTargetDate, 
			@QueryParam("testMode") String pTestMode,
			
			@QueryParam("fields") String fieldsList,
			@QueryParam("filters") String filtersList
			
			) throws ParseException, IOException{
		SimpleDateFormat sdf=new SimpleDateFormat("yy-MMM");
		Pair<Boolean,Date> checks=getParametersForPurge(pTestMode, pTargetDate, sdf);
		Boolean testMode=checks.getFirst();
		Date targetDate=checks.getSecond();
		List<String> filters=(filtersList!=null && !"".equals(filtersList))?Stream.of(filtersList.split(",")).map(String::trim).collect(Collectors.toList()):Lists.newArrayList();
		List<String> fields=(fieldsList!=null && !"".equals(fieldsList))?Stream.of(fieldsList.split(",")).map(String::trim).collect(Collectors.toList()):Lists.newArrayList();
		
		Survey s=Survey.findById(surveyId);
		Map<String, Object> results=s.getResults();
		List<String> toPurgeKeys=new ArrayList<>();
		Map<String, Object> toPurgeObjects=new HashMap<String, Object>();
		log.debug("results.size()="+results.size());
		for (Entry<String, Object> e:results.entrySet()){
			String resultId=e.getKey();
			Map<String,Object> surveyResults=Json.toObject((String)e.getValue(), new TypeReference<Map<String,Object>>(){});
			
			// if it has a date embedded in the resultId (most should now), then ignore it if the date is outside the date filter
			if (resultId.matches("[0-9]{6}.+")){ // check that the resultId contains the timestamp at the start
				Date resultDate=new SimpleDateFormat("yyMM").parse(resultId.substring(0,4));
				
				//log.debug("check: is '"+sdf.format(resultDate)+"' before or equal to '"+sdf.format(targetDate)+"' ? "+ (resultDate.equals(targetDate) || resultDate.before(targetDate)));
				if (!(resultDate.equals(targetDate) || resultDate.before(targetDate))){ // it's outside the dates, ignore it
					continue;
				}
			}
			
			// check the other (non-date) filters to find included results
			if (satisfiedFilters(filters, surveyResults)){
				toPurgeKeys.add(resultId); // if resultId doesnt start with a datestamp then it's legacy and can be purged
				toPurgeObjects.put(resultId, reducedResults(fields, e.getKey(), surveyResults));
			}
			
		}
		
		for(String toPurge:toPurgeKeys){
			log.debug("[testMode="+testMode+"] purging "+s.id+".Results."+toPurge+"");
			if (!testMode){
				results.remove(toPurge);
			}
		}
		s.saveResults();
		
		return Response.ok().entity(Json.toJson(toPurgeObjects)).build();
	}
	
	private boolean satisfiedFilters(List<String> filters, Map<String, Object> surveyResults){
		boolean result=true;
		for (String filter:filters){
			List<String> filterNameValue=Stream.of(filter.split("=")).map(String::trim).collect(Collectors.toList());
			String filterName=filterNameValue.get(0);
			String filterValue=filterNameValue.get(1);
//			System.out.println("applying filter "+filterName+"="+filterValue+" === "+filterValue.equals(surveyResults.get(filterName)) +" ("+surveyResults.get(filterName)+") ");
			
			result=result && filterValue.equals(surveyResults.get(filterName));
		}
		return result;
	}
	
	public Map<String,String> reducedResults(List<String> fields, String id, Map<String,Object> results){
//		try{
//		if ()
//			List<String> fields=Stream.of(fieldsCommaSep.split(",")).map(String::trim).collect(Collectors.toList());
//			Map<String,Object> results=Json.toObject((String)o, new TypeReference<Map<String,Object>>(){});
			
			Map<String,String> result=new LinkedHashMap<>();
			for (String field:fields){
				result.put(field, (String)results.get(field));
			}
			return result;
			
//			System.out.println("o="+o);
//			return new MapBuilder<String,String>()
////				.put("id",id)
//					.put("timestamp", (String)results.get("_timestamp"))
//					.put("country", (String)results.get("_Country"))
//					.put("industry", (String)results.get("_Industry"))
//					.put("company", (String)results.get("_Company"))
//					
//					.build();
			
//		}catch(Exception e){
//			System.out.println("o was "+o);
//			e.printStackTrace();
//			return new MapBuilder<String,String>().build();
//		}
	}
	
	private Pair<Boolean,Date> getParametersForPurge(String pTestMode, String pTargetDate, SimpleDateFormat sdf) throws ParseException{
		Preconditions.checkArgument(pTargetDate!=null && pTargetDate.matches("[0-9]{2}-[a-zA-Z]{3}"), "'date' parameter needs to be provided and in 'YY-MMM' format. Found "+pTargetDate);
		boolean testMode=pTestMode!=null && pTestMode.equalsIgnoreCase("true");
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
