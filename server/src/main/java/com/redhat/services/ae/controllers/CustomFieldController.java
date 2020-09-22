package com.redhat.services.ae.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.security.PermitAll;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.jboss.resteasy.annotations.jaxrs.PathParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.github.benmanes.caffeine.cache.Cache;
import com.google.common.collect.Lists;
import com.redhat.services.ae.Database;
import com.redhat.services.ae.MapBuilder;
import com.redhat.services.ae.model.Survey;
import com.redhat.services.ae.plugins.Plugin;
import com.redhat.services.ae.utils.CacheHelper;
import com.redhat.services.ae.utils.FluentCalendar;
import com.redhat.services.ae.utils.Json;
import com.redhat.services.ae.utils.Pair;

import io.reactivex.internal.util.LinkedArrayList;

@Path("/api/surveys")
//@Produces(MediaType.APPLICATION_JSON)
public class CustomFieldController{
	
	private static List<String> departments=Lists.newArrayList(
		"IT - Applications / Development",
		"IT - Business Intelligence",
		"IT - Database",
		"IT - Desktop / Help Desk",
		"IT - Network",
		"IT - Operations",
		"IT - Project Management",
		"IT - Quality / Testing",
		"IT - Risk / Compliance / Security",
		"IT - Server / Storage",
		"IT - Telecom",
		"IT - Web",
		"IT - All",
		"Customer Service / Call Center",
		"Executive Office",
		"Finance",
		"Human Resources",
		"Legal",
		"Marketing Communications",
		"Research & Development",
		"Sales",
		"Technical Support",
		"Other"
		);

	// list got from near "UpdateJobRoleList" in here: https://www.redhat.com/forms/scripts/jquery.gatedform.js
	private static Map<String,List<String>> jobRoles=new MapBuilder<String,List<String>>(true)
			.put("IT \\- .*", Lists.newArrayList("Analyst", "Architect", "Assistant", "Chief Architect", "Chief Security/Compliance Officer", "CIO", "Consultant", "Database Administrator", "Director", "Engineer", "Manager", "Network Administrator", "Programmer/Developer", "Specialist/Staff", "Student", "System Administrator", "Vice President", "Webmaster"))
			.put("Customer Service/Call Center", Lists.newArrayList("Consultant", "Director", "Manager", "Representative/Specialist", "Vice President"))
			.put("Executive Office", Lists.newArrayList("Assistant", "CEO", "CFO", "Chairman", "Chief Architect", "Chief Security/Compliance Officer", "CIO", "CMO", "COO", "CTO", "General Counsel", "General Manager", "Owner", "Partner/Principal", "President"))
			.put("Finance", Lists.newArrayList("CFO", "Consultant", "Finance/Accounting", "Procurement/Purchasing", "Treasurer/Comptroller", "Vice President"))
			.put("Human Resources", Lists.newArrayList("Consultant", "Director", "Manager", "Representative/Specialist", "Vice President"))
			.put("Legal", Lists.newArrayList("Consultant", "General Counsel", "Lawyer/Solicitor", "Legal Services/Paralegal", "Partner/Principal"))
			.put("Marketing Communications", Lists.newArrayList("CMO", "Consultant", "Director", "Industry Analyst", "Manager", "Press/Media", "Representative/Specialist", "Vice President"))
			.put("Research . Development", Lists.newArrayList("Architect", "Chief Architect/Chief Scientist", "Consultant", "CTO", "Director", "Engineer", "Manager", "Product Manager", "Programmer/Developer", "Student", "Vice President"))
			.put("Sales", Lists.newArrayList("Account Executive/Manager", "Consultant", "Director", "General Manager", "Manager", "Vice President"))
			.put("Technical Support", Lists.newArrayList("Consultant", "Director", "Engineer/Specialist", "Manager", "Vice President"))
			.put(".*", Lists.newArrayList())
			.build();
	
	private static List<String> industries=Lists.newArrayList(
		"Aerospace & Defense",
		"Agriculture",
		"Apparel",
		"Associations",
		"Automotive",
		"Biotech",
		"Business Services",
		"Construction",
		"Consumer Goods & Services",
		"Education",
		"Energy & Utilities",
		"Financial Services",
		"Food & Beverage",
		"Furniture",
		"Government",
		"Hardware",
		"Healthcare & Medical",
		"Home & Garden",
		"Hospitality & Travel",
		"Manufacturing",
		"Media & Entertainment",
		"Mining",
		"Pharmaceuticals",
		"Printing & Publishing",
		"Real Estate",
		"Recreation",
		"Retail & Distribution",
		"Software & Technology",
		"Telecommunications",
		"Textiles",
		"Transportation & Logistics"
		);
	
	
	private static Map<String,String> country(String code, String name, String optInEmail, String optInPhone){
		return new MapBuilder<String, String>().put("name", name).put("alpha2Code", code).put("optInEmail", optInEmail).put("optInPhone", optInPhone).build();
	}
	

	private static List<Map<String,String>> countries2=Lists.newArrayList(					
			country(	"AX",	"Ãland Islands",	"opt-in",	"opt-out"	),
			country(	"AL",	"Albania",	"opt-in",	"opt-out"	),
			country(	"DZ",	"Algeria",	"opt-in",	"opt-out"	),
			country(	"AS",	"American Samoa",	"opt-out",	"opt-out"	),
			country(	"AD",	"Andorra",	"opt-in",	"opt-out"	),
			country(	"AO",	"Angola",	"opt-in",	"opt-out"	),
			country(	"AR",	"Argentina",	"opt-in",	"opt-out"	),
			country(	"AM",	"Armenia",	"opt-in",	"opt-in"	),
			country(	"AU",	"Australia",	"opt-in",	"opt-out"	),
			country(	"AT",	"Austria",	"opt-in",	"opt-in"	),
			country(	"AZ",	"Azerbaijan",	"opt-in",	"opt-in"	),
			country(	"BH",	"Bahrain",	"opt-out",	"opt-out"	),
			country(	"BD",	"Bangladesh",	"opt-out",	"opt-out"	),
			country(	"BY",	"Belarus",	"opt-in",	"opt-out"	),
			country(	"BE",	"Belgium",	"opt-in",	"opt-out"	),
			country(	"BZ",	"Belize",	"opt-out",	"opt-out"	),
			country(	"BJ",	"Benin",	"opt-in",	"opt-out"	),
			country(	"BT",	"Bhutan",	"opt-out",	"opt-out"	),
			country(	"BO",	"Bolivia",	"opt-out",	"opt-out"	),
			country(	"BA",	"Bosnia and Herzegovina",	"opt-in",	"opt-out"	),
			country(	"BW",	"Botswana",	"opt-in",	"opt-out"	),
			country(	"BV",	"Bouvet Island",	"opt-in",	"opt-out"	),
			country(	"BR",	"Brazil",	"opt-in",	"opt-in"	),
			country(	"IO",	"British Indian Ocean Territory",	"opt-in",	"opt-out"	),
			country(	"BN",	"Brunei Darussalam",	"opt-out",	"opt-out"	),
			country(	"BG",	"Bulgaria",	"opt-in",	"opt-in"	),
			country(	"BF",	"Burkina Faso",	"opt-in",	"opt-out"	),
			country(	"BI",	"Burundi",	"opt-in",	"opt-out"	),
			country(	"KH",	"Cambodia",	"opt-out",	"opt-out"	),
			country(	"CM",	"Cameroon",	"opt-in",	"opt-out"	),
			country(	"CA",	"Canada",	"opt-in",	"opt-out"	),
			country(	"CV",	"Cape Verde",	"opt-in",	"opt-out"	),
			country(	"CF",	"Central African Republic",	"opt-in",	"opt-out"	),
			country(	"TD",	"Chad",	"opt-in",	"opt-out"	),
			country(	"CL",	"Chile",	"opt-out",	"opt-out"	),
			country(	"CN",	"China",	"opt-out",	"opt-out"	),
			country(	"CX",	"Christmas Island",	"opt-out",	"opt-out"	),
			country(	"CC",	"Cocos (Keeling) Islands",	"opt-out",	"opt-out"	),
			country(	"CO",	"Colombia",	"opt-in",	"opt-in"	),
			country(	"KM",	"Comoros",	"opt-in",	"opt-out"	),
			country(	"CG",	"Congo",	"opt-in",	"opt-out"	),
			country(	"CD",	"Congo, The Democratic Republic of the",	"opt-in",	"opt-out"	),
			country(	"CK",	"Cook Islands",	"opt-out",	"opt-out"	),
			country(	"CR",	"Costa Rica",	"opt-out",	"opt-out"	),
			country(	"HR",	"Croatia",	"opt-in",	"opt-out"	),
			country(	"CU",	"Cuba",	"opt-out",	"opt-out"	),
			country(	"CY",	"Cyprus",	"opt-in",	"opt-in"	),
			country(	"CZ",	"Czech Republic",	"opt-in",	"opt-in"	),
			country(	"DK",	"Denmark",	"opt-in",	"opt-out"	),
			country(	"DJ",	"Djibouti",	"opt-in",	"opt-out"	),
			country(	"DO",	"Dominican Republic",	"opt-out",	"opt-out"	),
			country(	"EC",	"Ecuador",	"opt-out",	"opt-out"	),
			country(	"EG",	"Egypt",	"opt-out",	"opt-out"	),
			country(	"SV",	"El Salvador",	"opt-out",	"opt-out"	),
			country(	"GQ",	"Equatorial Guinea",	"opt-in",	"opt-out"	),
			country(	"ER",	"Eritrea",	"opt-in",	"opt-out"	),
			country(	"EE",	"Estonia",	"opt-in",	"opt-out"	),
			country(	"ET",	"Ethiopia",	"opt-out",	"opt-out"	),
			country(	"FK",	"Falkland Islands (Malvinas)",	"opt-in",	"opt-out"	),
			country(	"FO",	"Faroe Islands",	"opt-in",	"opt-out"	),
			country(	"FJ",	"Fiji",	"opt-out",	"opt-out"	),
			country(	"FI",	"Finland",	"opt-in",	"opt-out"	),
			country(	"FR",	"France",	"opt-in",	"opt-out"	),
			country(	"GF",	"French Guiana",	"opt-out",	"opt-out"	),
			country(	"PF",	"French Polynesia",	"opt-in",	"opt-out"	),
			country(	"TF",	"French Southern Territories",	"opt-out",	"opt-out"	),
			country(	"GA",	"Gabon",	"opt-in",	"opt-out"	),
			country(	"GM",	"Gambia",	"opt-in",	"opt-out"	),
			country(	"GE",	"Georgia",	"opt-in",	"opt-out"	),
			country(	"DE",	"Germany",	"opt-in",	"opt-in"	),
			country(	"GH",	"Ghana",	"opt-in",	"opt-out"	),
			country(	"GI",	"Gibraltar",	"opt-in",	"opt-out"	),
			country(	"GR",	"Greece",	"opt-in",	"opt-out"	),
			country(	"GL",	"Greenland",	"opt-in",	"opt-out"	),
			country(	"GP",	"Guadeloupe",	"opt-out",	"opt-out"	),
			country(	"GU",	"Guam",	"opt-out",	"opt-out"	),
			country(	"GT",	"Guatemala",	"opt-out",	"opt-out"	),
			country(	"GG",	"Guernsey",	"opt-in",	"opt-out"	),
			country(	"GN",	"Guinea",	"opt-in",	"opt-out"	),
			country(	"GW",	"Guinea-Bissau",	"opt-in",	"opt-out"	),
			country(	"HT",	"Haiti",	"opt-out",	"opt-out"	),
			country(	"HM",	"Heard Island and McDonald Islands",	"opt-out",	"opt-out"	),
			country(	"VA",	"Holy See (Vatican City State)",	"opt-in",	"opt-in"	),
			country(	"HN",	"Honduras",	"opt-out",	"opt-out"	),
			country(	"HK",	"Hong Kong",	"opt-out",	"opt-out"	),
			country(	"HU",	"Hungary",	"opt-in",	"opt-out"	),
			country(	"IS",	"Iceland",	"opt-in",	"opt-out"	),
			country(	"IN",	"India",	"opt-out",	"opt-out"	),
			country(	"ID",	"Indonesia",	"opt-in",	"opt-out"	),
			country(	"IR",	"Iran, Islamic Republic of",	"opt-in",	"opt-out"	),
			country(	"IQ",	"Iraq",	"opt-out",	"opt-out"	),
			country(	"IE",	"Ireland",	"opt-in",	"opt-out"	),
			country(	"IM",	"Isle of Man",	"opt-in",	"opt-out"	),
			country(	"IL",	"Israel",	"opt-in",	"opt-out"	),
			country(	"IT",	"Italy",	"opt-in",	"opt-in"	),
			country(	"CI",	"Ivory Coast",	"opt-out",	"opt-out"	),
			country(	"JM",	"Jamaica",	"opt-out",	"opt-out"	),
			country(	"JP",	"Japan",	"opt-in",	"opt-out"	),
			country(	"JE",	"Jersey",	"opt-in",	"opt-out"	),
			country(	"JO",	"Jordan",	"opt-out",	"opt-out"	),
			country(	"KZ",	"Kazakhstan",	"opt-in",	"opt-out"	),
			country(	"KE",	"Kenya",	"opt-in",	"opt-out"	),
			country(	"KI",	"Kiribati",	"opt-out",	"opt-out"	),
			country(	"KP",	"Korea, Democratic People's Republic of",	"opt-out",	"opt-out"	),
			country(	"KR",	"Korea, Republic of",	"opt-in",	"opt-in"	),
			country(	"KW",	"Kuwait",	"opt-out",	"opt-out"	),
			country(	"KG",	"Kyrgyzstan",	"opt-in",	"opt-out"	),
			country(	"LA",	"Lao People's Democratic Republic",	"opt-out",	"opt-out"	),
			country(	"LV",	"Latvia",	"opt-in",	"opt-in"	),
			country(	"LB",	"Lebanon",	"opt-out",	"opt-out"	),
			country(	"LS",	"Lesotho",	"opt-in",	"opt-out"	),
			country(	"LR",	"Liberia",	"opt-in",	"opt-out"	),
			country(	"LY",	"Libyan Arab Jamahiriya",	"opt-in",	"opt-out"	),
			country(	"LI",	"Liechtenstein",	"opt-in",	"opt-out"	),
			country(	"LT",	"Lithuania",	"opt-in",	"opt-in"	),
			country(	"LU",	"Luxembourg",	"opt-in",	"opt-in"	),
			country(	"MO",	"Macao",	"opt-out",	"opt-out"	),
			country(	"MK",	"Macedonia, The former Yugoslav Republic of",	"opt-in",	"opt-out"	),
			country(	"MG",	"Madagascar",	"opt-in",	"opt-out"	),
			country(	"MW",	"Malawi",	"opt-in",	"opt-out"	),
			country(	"MY",	"Malaysia",	"opt-in",	"opt-out"	),
			country(	"MV",	"Maldives",	"opt-out",	"opt-out"	),
			country(	"ML",	"Mali",	"opt-in",	"opt-out"	),
			country(	"MT",	"Malta",	"opt-in",	"opt-in"	),
			country(	"MH",	"Marshall Islands",	"opt-out",	"opt-out"	),
			country(	"MQ",	"Martinique",	"opt-out",	"opt-out"	),
			country(	"MR",	"Mauritania",	"opt-in",	"opt-out"	),
			country(	"MU",	"Mauritius",	"opt-out",	"opt-out"	),
			country(	"YT",	"Mayotte",	"opt-in",	"opt-out"	),
			country(	"MX",	"Mexico",	"opt-out",	"opt-out"	),
			country(	"FM",	"Micronesia, Federated States of",	"opt-out",	"opt-out"	),
			country(	"MD",	"Moldova, Republic of",	"opt-in",	"opt-out"	),
			country(	"MC",	"Monaco",	"opt-out",	"opt-out"	),
			country(	"MN",	"Mongolia",	"opt-out",	"opt-out"	),
			country(	"ME",	"Montenegro",	"opt-in",	"opt-out"	),
			country(	"MA",	"Morocco",	"opt-in",	"opt-out"	),
			country(	"MZ",	"Mozambique",	"opt-in",	"opt-out"	),
			country(	"MM",	"Myanmar",	"opt-out",	"opt-out"	),
			country(	"NA",	"Namibia",	"opt-in",	"opt-out"	),
			country(	"NR",	"Nauru",	"opt-out",	"opt-out"	),
			country(	"NP",	"Nepal",	"opt-out",	"opt-out"	),
			country(	"NL",	"Netherlands",	"opt-in",	"opt-out"	),
			country(	"NC",	"New Caledonia",	"opt-in",	"opt-out"	),
			country(	"NZ",	"New Zealand",	"opt-in",	"opt-out"	),
			country(	"NI",	"Nicaragua",	"opt-out",	"opt-out"	),
			country(	"NE",	"Niger",	"opt-in",	"opt-out"	),
			country(	"NG",	"Nigeria",	"opt-in",	"opt-out"	),
			country(	"NU",	"Niue",	"opt-out",	"opt-out"	),
			country(	"NF",	"Norfolk Island",	"opt-out",	"opt-out"	),
			country(	"MP",	"Northern Mariana Islands",	"opt-out",	"opt-out"	),
			country(	"NO",	"Norway",	"opt-in",	"opt-out"	),
			country(	"OM",	"Oman",	"opt-out",	"opt-out"	),
			country(	"PK",	"Pakistan",	"opt-in",	"opt-out"	),
			country(	"PW",	"Palau",	"opt-out",	"opt-out"	),
			country(	"PS",	"Palestinian Territory, Occupied",	"opt-out",	"opt-out"	),
			country(	"PA",	"Panama",	"opt-out",	"opt-out"	),
			country(	"PG",	"Papua New Guinea",	"opt-out",	"opt-out"	),
			country(	"PY",	"Paraguay",	"opt-out",	"opt-out"	),
			country(	"PE",	"Peru",	"opt-in",	"opt-in"	),
			country(	"PH",	"Philippines",	"opt-in",	"opt-out"	),
			country(	"PN",	"Pitcairn",	"opt-in",	"opt-out"	),
			country(	"PL",	"Poland",	"opt-in",	"opt-in"	),
			country(	"PT",	"Portugal",	"opt-in",	"opt-in"	),
			country(	"PR",	"Puerto Rico",	"opt-out",	"opt-out"	),
			country(	"QA",	"Qatar",	"opt-in",	"opt-out"	),
			country(	"RE",	"RÃ©union",	"opt-out",	"opt-out"	),
			country(	"RO",	"Romania",	"opt-in",	"opt-out"	),
			country(	"RU",	"Russian Federation",	"opt-in",	"opt-in"	),
			country(	"RW",	"Rwanda",	"opt-in",	"opt-in"	),
			country(	"ST",	"SÃ£o Tome and Principe",	"opt-in",	"opt-out"	),
			country(	"SH",	"Saint Helena",	"opt-in",	"opt-out"	),
			country(	"LC",	"Saint Lucia",	"opt-out",	"opt-out"	),
			country(	"PM",	"Saint Pierre and Miquelon",	"opt-out",	"opt-out"	),
			country(	"WS",	"Samoa",	"opt-out",	"opt-out"	),
			country(	"SM",	"San Marino",	"opt-in",	"opt-out"	),
			country(	"SA",	"Saudi Arabia",	"opt-in",	"opt-in"	),
			country(	"SN",	"Senegal",	"opt-in",	"opt-out"	),
			country(	"RS",	"Serbia",	"opt-in",	"opt-out"	),
			country(	"SC",	"Seychelles",	"opt-in",	"opt-out"	),
			country(	"SL",	"Sierra Leone",	"opt-in",	"opt-out"	),
			country(	"SG",	"Singapore",	"opt-in",	"opt-in"	),
			country(	"SK",	"Slovakia",	"opt-in",	"opt-out"	),
			country(	"SI",	"Slovenia",	"opt-in",	"opt-in"	),
			country(	"SB",	"Solomon Islands",	"opt-out",	"opt-out"	),
			country(	"SO",	"Somalia",	"opt-in",	"opt-out"	),
			country(	"ZA",	"South Africa",	"opt-in",	"opt-out"	),
			country(	"GS",	"South Georgia and the South Sandwich Islands",	"opt-in",	"opt-out"	),
			country(	"ES",	"Spain",	"opt-in",	"opt-in"	),
			country(	"LK",	"Sri Lanka",	"opt-out",	"opt-out"	),
			country(	"SD",	"Sudan",	"opt-in",	"opt-out"	),
			country(	"SJ",	"Svalbard and Jan Mayen",	"opt-in",	"opt-out"	),
			country(	"SZ",	"Swaziland",	"opt-in",	"opt-out"	),
			country(	"SE",	"Sweden",	"opt-in",	"opt-out"	),
			country(	"CH",	"Switzerland",	"opt-in",	"opt-out"	),
			country(	"SY",	"Syrian Arab Republic",	"opt-in",	"opt-out"	),
			country(	"TW",	"Taiwan",	"opt-out",	"opt-out"	),
			country(	"TJ",	"Tajikistan",	"opt-in",	"opt-out"	),
			country(	"TZ",	"Tanzania, United Republic of",	"opt-out",	"opt-out"	),
			country(	"TH",	"Thailand",	"opt-in",	"opt-out"	),
			country(	"TL",	"Timor-Leste",	"opt-out",	"opt-out"	),
			country(	"TG",	"Togo",	"opt-in",	"opt-out"	),
			country(	"TK",	"Tokelau",	"opt-out",	"opt-out"	),
			country(	"TO",	"Tonga",	"opt-out",	"opt-out"	),
			country(	"TN",	"Tunisia",	"opt-in",	"opt-out"	),
			country(	"TR",	"Turkey",	"opt-in",	"opt-out"	),
			country(	"TM",	"Turkmenistan",	"opt-in",	"opt-out"	),
			country(	"TV",	"Tuvalu",	"opt-out",	"opt-out"	),
			country(	"UG",	"Uganda",	"opt-in",	"opt-in"	),
			country(	"UA",	"Ukraine",	"opt-in",	"opt-out"	),
			country(	"AE",	"United Arab Emirates",	"opt-out",	"opt-out"	),
			country(	"GB",	"United Kingdom",	"opt-in",	"opt-out"	),
			country(	"US",	"United States",	"opt-out",	"opt-out"	),
			country(	"UY",	"Uruguay",	"opt-out",	"opt-out"	),
			country(	"UZ",	"Uzbekistan",	"opt-in",	"opt-out"	),
			country(	"VU",	"Vanuatu",	"opt-in",	"opt-out"	),
			country(	"VE",	"Venezuela",	"opt-out",	"opt-out"	),
			country(	"VN",	"Vietnam",	"opt-in",	"opt-out"	),
			country(	"VI",	"Virgin Islands, U.S.",	"opt-out",	"opt-out"	),
			country(	"WF",	"Wallis and Futuna",	"opt-in",	"opt-out"	),
			country(	"EH",	"Western Sahara",	"opt-in",	"opt-out"	),
			country(	"YE",	"Yemen",	"opt-out",	"opt-out"	),
			country(	"ZM",	"Zambia",	"opt-in",	"opt-out"	),
			country(	"ZW",	"Zimbabwe",	"opt-in",	"opt-out"	)
			);
	
	private Map<String, Object> values=new MapBuilder<String, Object>()
			.put("industries", industries)
			.put("countries", countries2)
			.put("departments", departments)
			.build();
	
	public static final Logger log=LoggerFactory.getLogger(CustomFieldController.class);

	
	/** This satisfies the dropdowns for industries, countries and departments (anything listed in the "values" object above. */
	@GET
	@PermitAll
	@Path("/{surveyId}/{type}")
	public Response getDrowDownValues(@PathParam("surveyId") String surveyId, @PathParam("type") String type) throws IOException{
		return Response.ok(Json.newObjectMapper(false).writeValueAsString(values.get(type))).type(MediaType.APPLICATION_JSON).build();
	}

	/** This satisfies the dependent dropdown of job role based on selected department */
	@GET
	@PermitAll
	@Path("/{surveyId}/jobroles/{dept}")
	public Response getJobRoles(@PathParam("surveyId") String surveyId, @PathParam("dept") String dept) throws IOException{
		List<String> result=Lists.newArrayList();
		for(String k:jobRoles.keySet()){
//			System.out.println("jobroles:: "+dept+".matches("+k+") = "+dept.matches(k));
			if (dept.matches(k)){
				result=jobRoles.get(k);
				break;
			}
		}
		return Response.ok(Json.toJson(result)).build();
	}
	
	
	
	//$server/api/surveys/$surveyId/consentagreement/{_Country}
	
//	@GET
//	@PermitAll
//	@Path("/{surveyId}/consentagreement/{countryCode}")
//	public Response getConsentAgreement(@PathParam("surveyId") String surveyId, @PathParam("countryCode") String countryCode) throws IOException{
////		if (true) return Response.ok(Json.toJson("{}")).build();
////		Map<String,String result=Lists.newArrayList();
//		for(String k:consentCountries.keySet()){
////			System.out.println("jobroles:: "+dept+".matches("+k+") = "+dept.matches(k));
//			if (countryCode.matches(k)){
////				Map result=new MapBuilder<String,String>().put("key","value").build();
//				Pair<String,String> p=consentCountries.get(k);
//				
//				List<Map<String,Object>> result=Lists.newArrayList(
//						new MapBuilder<String,Object>().put("name", "by Email").put("checked",p.getFirst() .equalsIgnoreCase("opt-out")).build(),
//						new MapBuilder<String,Object>().put("name", "by Phone").put("checked",p.getSecond().equalsIgnoreCase("opt-out")).build()
//						);
//				return Response.ok(Json.toJson(result)).build();
////				result=new MapBuilder<String,String>().build();// consentCountries.get(k);
////				break;
//			}
//		}
//		return Response.ok(Json.toJson("{}")).build();
//	}
	
}
