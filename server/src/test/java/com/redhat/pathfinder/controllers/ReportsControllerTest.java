package com.redhat.pathfinder.controllers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.redhat.services.ae.Database;
import com.redhat.services.ae.controllers.ReportsController;
import com.redhat.services.ae.model.Survey;

public class ReportsControllerTest{

	@BeforeEach
	public void init() throws FileNotFoundException, IOException{
		Database.STORAGE="target/test/"+Database.STORAGE;
		if (new File(Database.STORAGE).exists())
			new File(Database.STORAGE).delete();
		IOUtils.write(setupDatabase().getBytes(), new FileOutputStream(new File(Database.STORAGE)));
		Survey s=Survey.findById("TESTING");
		s.setQuestions(setupQuestions());
	}
	
	
	@Test
	public void testSurveyCountGraph() throws JsonProcessingException, ParseException{
		System.out.println(
			new ReportsController().getSurveyCount("TESTING", "2020-Jan", "2020-Apr").getEntity()
		);
	}
	
	@Test
	public void testPageGraph() throws JsonProcessingException, ParseException{
		System.out.println(
			new ReportsController().getPageCount("TESTING", "2020-Jan", "2020-Apr").getEntity()
		);
	}
	@Test
	public void testPageGraphWithPageNameChange() throws JsonProcessingException, ParseException{
		Database.get().getSurveys().get("TESTING").getMetrics().getByMonth("page", "20-May").put("new page name", 2);
		Database.get().save();
		System.out.println(
			new ReportsController().getPageCount("TESTING", "2020-Jan", "2020-Apr").getEntity()
		);
	}
	
	@Test
	public void testGeoGraph() throws JsonProcessingException, ParseException{
		System.out.println(
			new ReportsController().getSurveyCountByGeo("TESTING", "2020-Apr", "2020-Apr").getEntity()
		);
	}
	
	
	@Test
	public void testResultsGathering() throws ParseException, IOException{
		String testPayload="{\n" + 
				"  \"automation-development\" : \"item3\",\n" + 
				"  \"question7\" : \"item5\",\n" + 
				"  \"question6\" : \"item1\",\n" + 
				"  \"question9\" : \"item5\",\n" + 
				"  \"automation-operations\" : \"item3\",\n" + 
				"  \"question8\" : \"item3\",\n" + 
				"  \"question10\" : \"item1\",\n" + 
				"  \"methodology-operations\" : \"item4\",\n" + 
				"  \"question5\" : \"item5\",\n" + 
				"  \"methodology-development\" : \"item4\"\n" + 
				"}";
		
		
		System.out.println(
				new ReportsController().onResults("TESTING", "visitorId-123", testPayload)
		);
	}
	
	@Test
	public void testAnswerPercentages() throws JsonProcessingException, ParseException{
		System.out.println(
				new ReportsController().answerPercentages("TESTING", "2019-Jan", "2022-Dec").getEntity()
		);
	}
	
	
	private String setupQuestions(){
		return 
				"{\"pages\":[{\"name\":\"1-automation\",\"elements\":[{\"type\":\"radiogroup\",\"name\":\"automation-development\",\"title\":{\"default\":\"Development\",\"de\":\"Entwicklung\"},\"isRequired\":true,\"choices\":[{\"value\":\"item1\",\"text\":\"Manual deployment, no process or automation\"},{\"value\":\"item2\",\"text\":\"Minimal deployment with ad-hoc scripting, not repeatable\"},{\"value\":\"item3\",\"text\":\"Baseline continuous integration (CI) processing (unit tests, manual testing)\"},{\"value\":\"item4\",\"text\":\"Advanced CI, greater than 90% automated testing, pipelines, approval gateways\"},{\"value\":\"item5\",\"text\":\"Full continuous integration / continuous delivery (CI/CD) from development into production (greater than 90%)\"}]},{\"type\":\"radiogroup\",\"name\":\"automation-operations\",\"title\":{\"default\":\"Operations\",\"de\":\"Operationen\"},\"isRequired\":true,\"choices\":[{\"value\":\"item2\",\"text\":\"Core build for operating system (OS), only basic (manual) provisioning\"},{\"value\":\"item3\",\"text\":\"Patch and release management (OS)\"},{\"value\":\"item4\",\"text\":\"Automated quality assurance (QA) staging process (standard operating environment, SOE)\"},{\"value\":\"item5\",\"text\":\"Automated OS builds\"},{\"value\":\"item6\",\"text\":\"Automatically managed and provisioned infrastructure through self-service\"}]}],\"title\":{\"default\":\"Automation ashkdsjkjdsfhkjdsf\",\"de\":\"Automatisierung\"},\"description\":\"Change is the new constant, and agile business practices are key to remaining competitive. Automation is often the space most influenced by our technology and methodology.  Please remember that these questions are not based on your aspirational state but on your current workflows.\n\nIn this section, please choose the following that best describes your current level of automation. \"},{\"name\":\"2-methodology\",\"elements\":[{\"type\":\"radiogroup\",\"name\":\"methodology-development\",\"title\":\"Development\",\"isRequired\":true,\"choices\":[{\"value\":\"item1\",\"text\":\"Ad-hoc development approach\"},{\"value\":\"item2\",\"text\":\"Defined waterfall approach\"},{\"value\":\"item3\",\"text\":\"Limited agile development on new projects (not including operations)\"},{\"value\":\"item4\",\"text\":\"Agile development through to production and operations\"},{\"value\":\"item5\",\"text\":\"Agile development in a DevOps culture\"}]},{\"type\":\"radiogroup\",\"name\":\"methodology-operations\",\"title\":\"Operations\",\"isRequired\":true,\"choices\":[{\"value\":\"item1\",\"text\":\"Hosting/management only\"},{\"value\":\"item2\",\"text\":\"Defined service-level agreements (SLAs) and IT infrastructure library (ITIL)\"},{\"value\":\"item3\",\"text\":\"Compliance and security auditing\"},{\"value\":\"item4\",\"text\":\"Standard operating environment (SOE)\"},{\"value\":\"item5\",\"text\":\"Full DevOps culture\"}]}],\"title\":\"Methodology\",\"description\":\"In today’s market, large organizations must rely on more than just technology and tools.\n\nIn this section, methodology refers to the way in which you are running your IT projects. Please remember that these questions are not based on your aspirational state but on your current workflows.   In this section, please choose the following that best describes your current level of methodology.  \"},{\"name\":\"3-architecture\",\"elements\":[{\"type\":\"radiogroup\",\"name\":\"question5\",\"title\":{\"default\":\"Development\",\"de\":\"Entwicklung\"},\"isRequired\":true,\"choices\":[{\"value\":\"item1\",\"text\":\"Ad-hoc choice of application platforms and tooling, limited understanding of contemporary architectural approaches\"},{\"value\":\"item2\",\"text\":\"Selected vendor technology roadmap, initial understanding of new architectures and designs\"},{\"value\":\"item3\",\"text\":\"Iterative development of existing applications, limited legacy strategy, and beginnings of new development architectures\"},{\"value\":\"item4\",\"text\":\"Focus on new application platforms and limited legacy platforms, well-defined architecture for new development projects and operating models\"},{\"value\":\"item5\",\"text\":\"Holistic and defined overall development strategy, good designs and architectures in place and under regular review\"}]},{\"type\":\"radiogroup\",\"name\":\"question6\",\"title\":{\"default\":\"Operations\",\"de\":\"Operationen\"},\"isRequired\":true,\"choices\":[{\"value\":\"item1\",\"text\":\"Ad-hoc choice of future platforms\"},{\"value\":\"item2\",\"text\":\"Selected vendor technology roadmap\"},{\"value\":\"item3\",\"text\":\"Focus on maintaining existing infrastructure\"},{\"value\":\"item4\",\"text\":\"Primary focus on new applications\"},{\"value\":\"item5\",\"text\":\"Defined strategy for existing and new architectures\"}]}],\"title\":{\"default\":\"Architecture\",\"de\":\"\nDie Architektur\"},\"description\":\"As systems expand, you need to effectively manage your IT environment so all the parts work together to get the quickest return on investment (ROI). The following questions cover the long term architectural motivations, aims, and advances to your current state architecture. Please remember that these questions are not based on your aspirational state but on your current workflows.\n\nIn this section, please choose the following that best describes the architecture you currently use.\"},{\"name\":\"4-strategy\",\"elements\":[{\"type\":\"radiogroup\",\"name\":\"question7\",\"title\":\"Development\",\"choices\":[{\"value\":\"item1\",\"text\":\"Ad-hoc, tactical, and one-off requirements, poorly understood and communicated strategy\"},{\"value\":\"item2\",\"text\":\"Repeatable requirement-gathering approach, traditional system analysis\"},{\"value\":\"item3\",\"text\":\"Minimal viable product approach\"},{\"value\":\"item4\",\"text\":\"Exploration of multiple approaches to solve business needs\"},{\"value\":\"item5\",\"text\":\"Business value-driven IT innovation, IT working in a collaborative manner with the business\"}]},{\"type\":\"radiogroup\",\"name\":\"question8\",\"title\":\"Operations\",\"choices\":[{\"value\":\"item1\",\"text\":\"Instances of negative business impact\"},{\"value\":\"item2\",\"text\":\"Good functioning service operations, few unscheduled outages but slow to deploy\"},{\"value\":\"item3\",\"text\":\"Project-based service offerings, no unscheduled outages and rapid deployment\"},{\"value\":\"item4\",\"text\":\"Self-service operations for development and the business\"},{\"value\":\"item5\",\"text\":\"Transparent integration with project IT\"}]}],\"title\":\"Strategy\",\"description\":\"Defining a strategy is one of the most challenging areas for an organization. Often, the ability to interpret and translate business ideas to solutions can be complex.  Please remember that these questions are not based on your aspirational state but on your current workflows.\n\nIn this section, please choose the following that best describes the strategy you currently use.\"},{\"name\":\"5-environment\",\"elements\":[{\"type\":\"radiogroup\",\"name\":\"question9\",\"title\":\"Development\",\"isRequired\":true,\"choices\":[{\"value\":\"item1\",\"text\":\"Traditional programming techniques in a heavily segmented structure\"},{\"value\":\"item2\",\"text\":\"Sporadic agile adoption with limited cross-team collaboration\"},{\"value\":\"item3\",\"text\":\"Multiteam collaboration through formalized communication channel\"},{\"value\":\"item4\",\"text\":\"Early adoption of cross-functional teams and shared delivery goals\"},{\"value\":\"item5\",\"text\":\"100% DevOps collaborative culture with energized cross-functional teams and constant improvement\"}]},{\"type\":\"radiogroup\",\"name\":\"question10\",\"title\":\"Operations\",\"isRequired\":true,\"choices\":[{\"value\":\"item1\",\"text\":\"Standard Unix approach in a heavily segmented structure\"},{\"value\":\"item2\",\"text\":\"Reactive rather than proactive operations teams, water cooler discussions get more done\"},{\"value\":\"item3\",\"text\":\"Collaboration across segments are just beginning, agile is understood, sense of urgency for change is established\"},{\"value\":\"item4\",\"text\":\"Collaborative, smaller, cross-functional teams, open source solutions actively encouraged, communities of practice forming, new collaborative way of working established\"},{\"value\":\"item5\",\"text\":\"100% DevOps collaborative culture with energized cross-functional teams and constant improvement, positive change realized, individuals contributing, open organization understood\"}]}],\"title\":\"Environment\",\"description\":\"In this section, environment is defined as the mixture of staff, culture, training, and skill level within each area. Please remember that these questions are not based on your aspirational state but on your current workflows. \n\nIn this section, please choose the following that best describes your current resources.\"}],\"showQuestionNumbers\":\"off\",\"showProgressBar\":\"bottom\",\"progressBarType\":\"questions\"}"
				;
	}
	private String setupDatabase(){
		return                                    
		"	{                                       "+
		"  \"surveys\" : {                        "+
		"    \"TESTING\" : {                      "+
		"      \"id\" : \"DUWHSK\",               "+
		"      \"name\" : \"Ready to Innovate\",  "+
		"      \"description\" : \"\",            "+
		"      \"metrics\" : {                    "+
		"        \"completedByMonth\" : {         "+
		"          \"20-Mar\" : 7,                "+
		"          \"20-Apr\" : 4,                "+
		"          \"20-May\" : 4                 "+
		"        },                               "+
		"        \"byMonth\" : {                  "+
		"          \"geo\" : {                    "+
		"            \"20-Mar\" : {               "+
		"              \"NA\" : 3,                "+
		"              \"EMEA\" : 2,              "+
		"              \"APAC\" : 2               "+
		"            },                           "+
		"            \"20-Apr\" : {               "+
		"              \"NA\" : 2,                "+
		"              \"APAC\" : 2               "+
		"            },                           "+
		"            \"20-May\" : {               "+
		"              \"NA\" : 2,                "+
		"              \"EMEA\" : 1,              "+
		"              \"APAC\" : 1               "+
		"            }                            "+
		"          },                             "+
		"          \"page\" : {                   "+
		"            \"20-Mar\" : {               "+
		"              \"environment\" : 8,       "+
		"              \"automation\" : 8,        "+
		"              \"strategy\" : 8,          "+
		"              \"methodology\" : 8,       "+
		"              \"architecture\" : 7       "+
		"            },                           "+
		"            \"20-Apr\" : {               "+
		"              \"environment\" : 5,       "+
		"              \"automation\" : 5,        "+
		"              \"strategy\" : 5,          "+
		"              \"methodology\" : 4,       "+
		"              \"architecture\" : 4       "+
		"            },                           "+
		"            \"20-May\" : {               "+
		"              \"environment\" : 6,       "+
		"              \"automation\" : 6,        "+
		"              \"strategy\" : 6,          "+
		"              \"methodology\" : 6,       "+
		"              \"architecture\" : 4,      "+
		"              \"automation\" : 1         "+
		"            }                            "+
		"          },                             "+
		"          \"country\" : {                "+
		"            \"20-May\" : {               "+
		"              \"US\" : 1                 "+
		"            }                            "+
		"          }                              "+
		"        },                               "+
		"        \"answersByMonth\" : {                   "+
    "          \"answers\" : {                        "+
    "            \"20-May\" : {                       "+
    "              \"automation-development\" : {     "+
    "                \"item1\" : 1                    "+
    "              },                                 "+
    "              \"question7\" : {                  "+
    "                \"item2\" : 1                    "+
    "              },                                 "+
    "              \"question6\" : {                  "+
    "                \"item1\" : 1                    "+
    "              },                                 "+
    "              \"question9\" : {                  "+
    "                \"item1\" : 1                    "+
    "              },                                 "+
    "              \"automation-operations\" : {      "+
    "                \"item2\" : 1                    "+
    "              },                                 "+
    "              \"question8\" : {                  "+
    "                \"item2\" : 1                    "+
    "              },                                 "+
    "              \"question10\" : {                 "+
    "                \"item4\" : 1                    "+
    "              },                                 "+
    "              \"methodology-operations\" : {     "+
    "                \"item1\" : 1                    "+
    "              },                                 "+
    "              \"question5\" : {                  "+
    "                \"item1\" : 1                    "+
    "              },                                 "+
    "              \"methodology-development\" : {    "+
    "                \"item1\" : 1                    "+
    "              }                                  "+
    "            }                                    "+
    "          }                                      "+
    "        }                                      "+
		"      }                                  "+
		"    }                                    "+
		"  }}                                      "
				;
				
	}
}
