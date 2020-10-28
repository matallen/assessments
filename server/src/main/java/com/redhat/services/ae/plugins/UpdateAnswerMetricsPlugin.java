package com.redhat.services.ae.plugins;

import java.util.List;
import java.util.Map;

import org.mvel2.MVEL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.services.ae.controllers.AnswerProcessor;
import com.redhat.services.ae.model.MetricsDecorator;
import com.redhat.services.ae.model.Survey;
import com.redhat.services.ae.utils.FluentCalendar;

public class UpdateAnswerMetricsPlugin extends Plugin{
	public static final Logger log=LoggerFactory.getLogger(UpdateAnswerMetricsPlugin.class);
//	boolean disabled=false;
//	private String disabledIfExpression;
//	private boolean disabledIfResult=false;
	
	@Override
	public Plugin setConfig(Map<String, Object> cfg){
//		disabledIfExpression=(String)cfg.get("disabledIf");
//		disabled=super.getBooleanFromConfig(cfg, "disabled", false);
		return this;
	}

	
	@Override
	public Map<String, Object> execute(String surveyId, String visitorId, Map<String, Object> surveyResults) throws Exception{
		Survey o=Survey.findById(surveyId);
		if (null==o) throw new RuntimeException("Survey ID doesn't exist! :"+surveyId);
		
		if (!super.isDisabled(surveyResults)){
			String YYMMM=FluentCalendar.now().getString("yy-MMM");
			updateAnswerMetrics(o, YYMMM, surveyResults);
			o.saveMetrics();
		}
		
//		log.info("'disabledIfExpression' is "+disabledIfExpression);
//		
//		if (null!=disabledIfExpression){
//			try{
//				Object eval=MVEL.eval(disabledIfExpression, surveyResults);
//				disabledIfResult=disabledIfResult || (eval instanceof Boolean && (boolean)eval);
//				log.info("'disabledIfResult' is "+disabledIfResult);
//			}catch(Exception ex){
//				log.error("Not disabling "+UpdateAnswerMetricsPlugin.class.getSimpleName()+", however an expression error occured: "+ex.getMessage());
//				ex.printStackTrace();
//			}
//		}else
//			log.info("No disabledIf config found");
//		
//		if (disabledIfResult){
//			log.warn("Skipping "+UpdateAnswerMetricsPlugin.class.getSimpleName()+" because disabledIf expression '"+disabledIfExpression+"' evaluated to true");
//		}else{
//
////			o.persist(); // not sure this is needed but save it anyway
//		}
		
		return surveyResults;
	}
	
	private void updateAnswerMetrics(Survey s, String YYMMM, Map<String,Object> surveyResults){
	  // Metrics: log how many times a specific answer was provided to a question, for reporting % of answers per question
		MetricsDecorator m=new MetricsDecorator(s.getMetrics());
		new AnswerProcessor(false){
			@Override public void onStringAnswer(String questionId, String answerId, Integer score){ // radiobuttons
				log.debug(UpdateAnswerMetricsPlugin.class.getSimpleName()+": Adding answers for question '"+questionId+"' to metrics");
				m.increment(1, "answerDistribution", YYMMM, questionId, answerId);
			}
			@Override
			public void onArrayListAnswer(String questionId, List<Answer> answerList, Integer averageScore){ // multi-checkboxes
				log.debug(UpdateAnswerMetricsPlugin.class.getSimpleName()+": Adding answers for question '"+questionId+"' to metrics");
				for (Answer answer:answerList){
					// Increment the metrics for each item selected
					m.increment(1, "answerDistribution", YYMMM, questionId, answer.id);
				}
			}
			@Override
			public void onMapAnswer(String question, Answer answer){ // only seen this as a panel in surveyjs?
				// ignore for the purpose of metrics because it's most likely a contact form
			}
		}.process(surveyResults);
	}
	
	

}
