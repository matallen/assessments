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
	
	
	private static Map<String,String> country(String code, String name){
		return new MapBuilder<String, String>().put("name", name).put("alpha2Code", code).build();
	}
	private static List<Map<String,String>> countries=Lists.newArrayList(
			country("US","United States                               ".trim()),
			country("AF","Afghanistan                                 ".trim()),
			country("AX","Åland Islands                               ".trim()),
			country("AL","Albania                                     ".trim()),
			country("DZ","Algeria                                     ".trim()),
			country("AS","American Samoa                              ".trim()),
			country("AD","Andorra                                     ".trim()),
			country("AO","Angola                                      ".trim()),
			country("AI","Anguilla                                    ".trim()),
			country("AQ","Antarctica                                  ".trim()),
			country("AG","Antigua and Barbuda                         ".trim()),
			country("AR","Argentina                                   ".trim()),
			country("AM","Armenia                                     ".trim()),
			country("AW","Aruba                                       ".trim()),
			country("AU","Australia                                   ".trim()),
			country("AT","Austria                                     ".trim()),
			country("AZ","Azerbaijan                                  ".trim()),
			country("BS","Bahamas                                     ".trim()),
			country("BH","Bahrain                                     ".trim()),
			country("BD","Bangladesh                                  ".trim()),
			country("BB","Barbados                                    ".trim()),
			country("BY","Belarus                                     ".trim()),
			country("BE","Belgium                                     ".trim()),
			country("BZ","Belize                                      ".trim()),
			country("BJ","Benin                                       ".trim()),
			country("BM","Bermuda                                     ".trim()),
			country("BT","Bhutan                                      ".trim()),
			country("BO","Bolivia                                     ".trim()),
			country("BA","Bosnia and Herzegovina                      ".trim()),
			country("BW","Botswana                                    ".trim()),
			country("BV","Bouvet Island                               ".trim()),
			country("BR","Brazil                                      ".trim()),
			country("IO","Brit/Indian Ocean Terr.                     ".trim()),
			country("BN","Brunei Darussalam                           ".trim()),
			country("BG","Bulgaria                                    ".trim()),
			country("BF","Burkina Faso                                ".trim()),
			country("BI","Burundi                                     ".trim()),
			country("KH","Cambodia                                    ".trim()),
			country("CM","Cameroon                                    ".trim()),
			country("CA","Canada                                      ".trim()),
			country("CV","Cape Verde                                  ".trim()),
			country("KY","Cayman Islands                              ".trim()),
			country("CF","Central African Republic                    ".trim()),
			country("TD","Chad                                        ".trim()),
			country("CL","Chile                                       ".trim()),
			country("CN","China                                       ".trim()),
			country("CX","Christmas Island                            ".trim()),
			country("CC","Cocos (Keeling) Islands                     ".trim()),
			country("CO","Colombia                                    ".trim()),
			country("KM","Comoros                                     ".trim()),
			country("CG","Congo                                       ".trim()),
			country("CD","Congo, The Dem. Republic Of                 ".trim()),
			country("CK","Cook Islands                                ".trim()),
			country("CR","Costa Rica                                  ".trim()),
			country("CI","Côte D'Ivore                                ".trim()),
			country("HR","Croatia                                     ".trim()),
			country("CY","Cyprus                                      ".trim()),
			country("CZ","Czech Republic                              ".trim()),
			country("DK","Denmark                                     ".trim()),
			country("DJ","Djibouti                                    ".trim()),
			country("DM","Dominica                                    ".trim()),
			country("DO","Dominican Republic                          ".trim()),
			country("EC","Ecuador                                     ".trim()),
			country("ER","Eritrea                                     ".trim()),
			country("EG","Egypt                                       ".trim()),
			country("SV","El Salvador                                 ".trim()),
			country("GQ","Equatorial Guinea                           ".trim()),
			country("EE","Estonia                                     ".trim()),
			country("ET","Ethiopia                                    ".trim()),
			country("FK","Falkland Islands                            ".trim()),
			country("FO","Faroe Islands                               ".trim()),
			country("FJ","Fiji                                        ".trim()),
			country("FI","Finland                                     ".trim()),
			country("FR","France                                      ".trim()),
			country("GF","French Guiana                               ".trim()),
			country("PF","French Polynesia                            ".trim()),
			country("TF","French Southern Terr.                       ".trim()),
			country("GA","Gabon                                       ".trim()),
			country("GM","Gambia                                      ".trim()),
			country("GE","Georgia                                     ".trim()),
			country("DE","Germany                                     ".trim()),
			country("GH","Ghana                                       ".trim()),
			country("GI","Gibraltar                                   ".trim()),
			country("GR","Greece                                      ".trim()),
			country("GL","Greenland                                   ".trim()),
			country("GD","Grenada                                     ".trim()),
			country("GP","Guadeloupe                                  ".trim()),
			country("GU","Guam                                        ".trim()),
			country("GT","Guatemala                                   ".trim()),
			country("GN","Guinea                                      ".trim()),
			country("GW","Guinea-Bissau                               ".trim()),
			country("GY","Guyana                                      ".trim()),
			country("HT","Haiti                                       ".trim()),
			country("HM","Heard/McDonald Isls.                        ".trim()),
			country("HN","Honduras                                    ".trim()),
			country("HK","Hong Kong                                   ".trim()),
			country("HU","Hungary                                     ".trim()),
			country("IS","Iceland                                     ".trim()),
			country("IN","India                                       ".trim()),
			country("ID","Indonesia                                   ".trim()),
			country("IQ","Iraq                                        ".trim()),
			country("IE","Ireland                                     ".trim()),
			country("IL","Israel                                      ".trim()),
			country("IT","Italy                                       ".trim()),
			country("JM","Jamaica                                     ".trim()),
			country("JP","Japan                                       ".trim()),
			country("JO","Jordan                                      ".trim()),
			country("KZ","Kazakhstan                                  ".trim()),
			country("KE","Kenya                                       ".trim()),
			country("KI","Kiribati                                    ".trim()),
			country("KR","Korea (South)                               ".trim()),
			country("KW","Kuwait                                      ".trim()),
			country("KG","Kyrgyzstan                                  ".trim()),
			country("LA","Laos                                        ".trim()),
			country("LV","Latvia                                      ".trim()),
			country("LB","Lebanon                                     ".trim()),
			country("LS","Lesotho                                     ".trim()),
			country("LR","Liberia                                     ".trim()),
			country("LY","Libya                                       ".trim()),
			country("LI","Liechtenstein                               ".trim()),
			country("LT","Lithuania                                   ".trim()),
			country("LU","Luxembourg                                  ".trim()),
			country("MO","Macau                                       ".trim()),
			country("MK","Macedonia                                   ".trim()),
			country("MG","Madagascar                                  ".trim()),
			country("MW","Malawi                                      ".trim()),
			country("MY","Malaysia                                    ".trim()),
			country("MV","Maldives                                    ".trim()),
			country("ML","Mali                                        ".trim()),
			country("MT","Malta                                       ".trim()),
			country("MH","Marshall Islands                            ".trim()),
			country("MQ","Martinique                                  ".trim()),
			country("MR","Mauritania                                  ".trim()),
			country("MU","Mauritius                                   ".trim()),
			country("YT","Mayotte                                     ".trim()),
			country("MX","Mexico                                      ".trim()),
			country("FM","Micronesia                                  ".trim()),
			country("MD","Moldova                                     ".trim()),
			country("MC","Monaco                                      ".trim()),
			country("MN","Mongolia                                    ".trim()),
			country("MS","Montserrat                                  ".trim()),
			country("MA","Morocco                                     ".trim()),
			country("MZ","Mozambique                                  ".trim()),
			country("MM","Myanmar                                     ".trim()),
			country("MP","N. Mariana Isls.                            ".trim()),
			country("NA","Namibia                                     ".trim()),
			country("NR","Nauru                                       ".trim()),
			country("NP","Nepal                                       ".trim()),
			country("NL","Netherlands                                 ".trim()),
			country("??","Netherlands Antilles                        ".trim()),
			country("NC","New Caledonia                               ".trim()),
			country("NZ","New Zealand                                 ".trim()),
			country("NI","Nicaragua                                   ".trim()),
			country("NE","Niger                                       ".trim()),
			country("NG","Nigeria                                     ".trim()),
			country("NU","Niue                                        ".trim()),
			country("NF","Norfolk Island                              ".trim()),
			country("NO","Norway                                      ".trim()),
			country("OM","Oman                                        ".trim()),
			country("PK","Pakistan                                    ".trim()),
			country("PW","Palau                                       ".trim()),
			country("PS","Palestinian Territory, Occupied             ".trim()),
			country("PA","Panama                                      ".trim()),
			country("PG","Papua New Guinea                            ".trim()),
			country("PY","Paraguay                                    ".trim()),
			country("PE","Peru                                        ".trim()),
			country("PH","Philippines                                 ".trim()),
			country("PN","Pitcairn                                    ".trim()),
			country("PL","Poland                                      ".trim()),
			country("PT","Portugal                                    ".trim()),
			country("PR","Puerto Rico                                 ".trim()),
			country("QA","Qatar                                       ".trim()),
			country("RE","Reunion                                     ".trim()),
			country("RO","Romania                                     ".trim()),
			country("RU","Russian Federation                          ".trim()),
			country("RW","Rwanda                                      ".trim()),
			country("KN","Saint Kitts and Nevis                       ".trim()),
			country("LC","Saint Lucia                                 ".trim()),
			country("WS","Samoa                                       ".trim()),
			country("SM","San Marino                                  ".trim()),
			country("ST","Sao Tome/Principe                           ".trim()),
			country("SA","Saudi Arabia                                ".trim()),
			country("SN","Senegal                                     ".trim()),
			country("RS","Serbia and Montenegro                       ".trim()),
			country("SC","Seychelles                                  ".trim()),
			country("SL","Sierra Leone                                ".trim()),
			country("SG","Singapore                                   ".trim()),
			country("SK","Slovak Republic                             ".trim()),
			country("SI","Slovenia                                    ".trim()),
			country("SB","Solomon Islands                             ".trim()),
			country("SO","Somalia                                     ".trim()),
			country("ZA","South Africa                                ".trim()),
			country("ES","Spain                                       ".trim()),
			country("LK","Sri Lanka                                   ".trim()),
			country("SH","St. Helena                                  ".trim()),
			country("PM","St. Pierre and Miquelon                     ".trim()),
			country("VC","St. Vincent and Grenadines                  ".trim()),
			country("SR","Suriname                                    ".trim()),
			country("SJ","Svalbard/Jan Mayen Isls.                    ".trim()),
			country("SZ","Swaziland                                   ".trim()),
			country("SE","Sweden                                      ".trim()),
			country("CH","Switzerland                                 ".trim()),
			country("TW","Taiwan                                      ".trim()),
			country("TJ","Tajikistan                                  ".trim()),
			country("TZ","Tanzania                                    ".trim()),
			country("TH","Thailand                                    ".trim()),
			country("TL","Timor-Leste                                 ".trim()),
			country("TG","Togo                                        ".trim()),
			country("TK","Tokelau                                     ".trim()),
			country("TO","Tonga                                       ".trim()),
			country("TT","Trinidad and Tobago                         ".trim()),
			country("TN","Tunisia                                     ".trim()),
			country("TR","Turkey                                      ".trim()),
			country("TM","Turkmenistan                                ".trim()),
			country("TC","Turks/Caicos Isls.                          ".trim()),
			country("TV","Tuvalu                                      ".trim()),
			country("UG","Uganda                                      ".trim()),
			country("UA","Ukraine                                     ".trim()),
			country("AE","United Arab Emirates                        ".trim()),
			country("GB","United Kingdom                              ".trim()),
			country("UY","Uruguay                                     ".trim()),
			country("UM","US Minor Outlying Is.                       ".trim()),
			country("UZ","Uzbekistan                                  ".trim()),
			country("VU","Vanuatu                                     ".trim()),
			country("VA","Vatican City                                ".trim()),
			country("VE","Venezuela                                   ".trim()),
			country("VN","Viet Nam                                    ".trim()),
			country("VG","Virgin Islands (British)                    ".trim()),
			country("VI","Virgin Islands (U.S.)                       ".trim()),
			country("WF","Wallis/Futuna Isls.                         ".trim()),
			country("EH","Western Sahara                              ".trim()),
			country("YE","Yemen                                       ".trim()),
			country("ZM","Zambia                                      ".trim()),
			country("ZW","Zimbabwe                                    ".trim())
			);
	
	private Map<String, Object> values=new MapBuilder<String, Object>()
			.put("industries", industries)
			.put("countries", countries)
			.put("departments", departments)
			.build();
	
	
	private Map<String,Pair<String,String>> consentCountries=new MapBuilder<String,Pair<String,String>>()
			.put("ZW", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("ZM", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("ZA", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("YT", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("WF", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("VU", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("VN", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("VA", new Pair<>("opt-in ".trim(),"opt-in ".trim()))
			.put("UZ", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("UG", new Pair<>("opt-in ".trim(),"opt-in ".trim()))
			.put("UA", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("TR", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("TN", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("TM", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("TJ", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("TH", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("TG", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("TD", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("SZ", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("SY", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("ST", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("SO", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("SN", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("SM", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("SL", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("SK", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("SJ", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("SI", new Pair<>("opt-in ".trim(),"opt-in ".trim()))
			.put("SH", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("SG", new Pair<>("opt-in ".trim(),"opt-in ".trim()))
			.put("SE", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("SD", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("SC", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("SA", new Pair<>("opt-in ".trim(),"opt-in ".trim()))
			.put("RW", new Pair<>("opt-in ".trim(),"opt-in ".trim()))
			.put("RU", new Pair<>("opt-in ".trim(),"opt-in ".trim()))
			.put("RS", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("RO", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("QA", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("PT", new Pair<>("opt-in ".trim(),"opt-in ".trim()))
			.put("PN", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("PL", new Pair<>("opt-in ".trim(),"opt-in ".trim()))
			.put("PK", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("PH", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("PF", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("PE", new Pair<>("opt-in ".trim(),"opt-in ".trim()))
			.put("NZ", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("NO", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("NL", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("NG", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("NE", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("NC", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("NA", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("MZ", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("MY", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("MW", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("MT", new Pair<>("opt-in ".trim(),"opt-in ".trim()))
			.put("MR", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("ML", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("MK", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("MG", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("ME", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("MD", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("MA", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("LY", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("LV", new Pair<>("opt-in ".trim(),"opt-in ".trim()))
			.put("LU", new Pair<>("opt-in ".trim(),"opt-in ".trim()))
			.put("LT", new Pair<>("opt-in ".trim(),"opt-in ".trim()))
			.put("LS", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("LR", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("LI", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("KZ", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("KR", new Pair<>("opt-in ".trim(),"opt-in ".trim()))
			.put("KM", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("KG", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("KE", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("JP", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("JE", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("IT", new Pair<>("opt-in ".trim(),"opt-in ".trim()))
			.put("IS", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("IR", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("IO", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("IM", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("IL", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("IE", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("ID", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("HU", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("HR", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("GW", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("GS", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("GR", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("GQ", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("GN", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("GM", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("GL", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("GI", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("GH", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("GG", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("GE", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("GB", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("GA", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("FR", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("FO", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("FK", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("FI", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("ES", new Pair<>("opt-in ".trim(),"opt-in ".trim()))
			.put("ER", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("EH", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("EE", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("DZ", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("DK", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("DJ", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("DE", new Pair<>("opt-in ".trim(),"opt-in ".trim()))
			.put("CZ", new Pair<>("opt-in ".trim(),"opt-in ".trim()))
			.put("CY", new Pair<>("opt-in ".trim(),"opt-in ".trim()))
			.put("CV", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("CO", new Pair<>("opt-in ".trim(),"opt-in ".trim()))
			.put("CM", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("CH", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("CG", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("CF", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("CD", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("CA", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("BY", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("BW", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("BV", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("BJ", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("BI", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("BG", new Pair<>("opt-in ".trim(),"opt-in ".trim()))
			.put("BF", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("BE", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("BA", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("AZ", new Pair<>("opt-in ".trim(),"opt-in ".trim()))
			.put("AX", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("AU", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("AT", new Pair<>("opt-in ".trim(),"opt-in ".trim()))
			.put("AR", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("AO", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("AM", new Pair<>("opt-in ".trim(),"opt-in ".trim()))
			.put("AL", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("AD", new Pair<>("opt-in ".trim(),"opt-out".trim()))
			.put("YE", new Pair<>("opt-out".trim(),"opt-out".trim()))
			.put("WS", new Pair<>("opt-out".trim(),"opt-out".trim()))
			.put("VI", new Pair<>("opt-out".trim(),"opt-out".trim()))
			.put("VE", new Pair<>("opt-out".trim(),"opt-out".trim()))
			.put("UY", new Pair<>("opt-out".trim(),"opt-out".trim()))
			.put("US", new Pair<>("opt-out".trim(),"opt-out".trim()))
			.put("TZ", new Pair<>("opt-out".trim(),"opt-out".trim()))
			.put("TW", new Pair<>("opt-out".trim(),"opt-out".trim()))
			.put("TV", new Pair<>("opt-out".trim(),"opt-out".trim()))
			.put("TO", new Pair<>("opt-out".trim(),"opt-out".trim()))
			.put("TL", new Pair<>("opt-out".trim(),"opt-out".trim()))
			.put("TK", new Pair<>("opt-out".trim(),"opt-out".trim()))
			.put("TF", new Pair<>("opt-out".trim(),"opt-out".trim()))
			.put("SV", new Pair<>("opt-out".trim(),"opt-out".trim()))
			.put("SB", new Pair<>("opt-out".trim(),"opt-out".trim()))
			.put("RE", new Pair<>("opt-out".trim(),"opt-out".trim()))
			.put("PY", new Pair<>("opt-out".trim(),"opt-out".trim()))
			.put("PW", new Pair<>("opt-out".trim(),"opt-out".trim()))
			.put("PS", new Pair<>("opt-out".trim(),"opt-out".trim()))
			.put("PR", new Pair<>("opt-out".trim(),"opt-out".trim()))
			.put("PM", new Pair<>("opt-out".trim(),"opt-out".trim()))
			.put("PG", new Pair<>("opt-out".trim(),"opt-out".trim()))
			.put("PA", new Pair<>("opt-out".trim(),"opt-out".trim()))
			.put("OM", new Pair<>("opt-out".trim(),"opt-out".trim()))
			.put("NU", new Pair<>("opt-out".trim(),"opt-out".trim()))
			.put("NR", new Pair<>("opt-out".trim(),"opt-out".trim()))
			.put("NP", new Pair<>("opt-out".trim(),"opt-out".trim()))
			.put("NI", new Pair<>("opt-out".trim(),"opt-out".trim()))
			.put("NF", new Pair<>("opt-out".trim(),"opt-out".trim()))
			.put("MX", new Pair<>("opt-out".trim(),"opt-out".trim()))
			.put("MV", new Pair<>("opt-out".trim(),"opt-out".trim()))
			.put("MU", new Pair<>("opt-out".trim(),"opt-out".trim()))
			.put("MQ", new Pair<>("opt-out".trim(),"opt-out".trim()))
			.put("MP", new Pair<>("opt-out".trim(),"opt-out".trim()))
			.put("MO", new Pair<>("opt-out".trim(),"opt-out".trim()))
			.put("MN", new Pair<>("opt-out".trim(),"opt-out".trim()))
			.put("MM", new Pair<>("opt-out".trim(),"opt-out".trim()))
			.put("MH", new Pair<>("opt-out".trim(),"opt-out".trim()))
			.put("MC", new Pair<>("opt-out".trim(),"opt-out".trim()))
			.put("LK", new Pair<>("opt-out".trim(),"opt-out".trim()))
			.put("LC", new Pair<>("opt-out".trim(),"opt-out".trim()))
			.put("LB", new Pair<>("opt-out".trim(),"opt-out".trim()))
			.put("LA", new Pair<>("opt-out".trim(),"opt-out".trim()))
			.put("KW", new Pair<>("opt-out".trim(),"opt-out".trim()))
			.put("KP", new Pair<>("opt-out".trim(),"opt-out".trim()))
			.put("KI", new Pair<>("opt-out".trim(),"opt-out".trim()))
			.put("KH", new Pair<>("opt-out".trim(),"opt-out".trim()))
			.put("JO", new Pair<>("opt-out".trim(),"opt-out".trim()))
			.put("JM", new Pair<>("opt-out".trim(),"opt-out".trim()))
			.put("IQ", new Pair<>("opt-out".trim(),"opt-out".trim()))
			.put("IN", new Pair<>("opt-out".trim(),"opt-out".trim()))
			.put("HT", new Pair<>("opt-out".trim(),"opt-out".trim()))
			.put("HN", new Pair<>("opt-out".trim(),"opt-out".trim()))
			.put("HM", new Pair<>("opt-out".trim(),"opt-out".trim()))
			.put("HK", new Pair<>("opt-out".trim(),"opt-out".trim()))
			.put("GU", new Pair<>("opt-out".trim(),"opt-out".trim()))
			.put("GT", new Pair<>("opt-out".trim(),"opt-out".trim()))
			.put("GP", new Pair<>("opt-out".trim(),"opt-out".trim()))
			.put("GF", new Pair<>("opt-out".trim(),"opt-out".trim()))
			.put("FM", new Pair<>("opt-out".trim(),"opt-out".trim()))
			.put("FJ", new Pair<>("opt-out".trim(),"opt-out".trim()))
			.put("ET", new Pair<>("opt-out".trim(),"opt-out".trim()))
			.put("EG", new Pair<>("opt-out".trim(),"opt-out".trim()))
			.put("EC", new Pair<>("opt-out".trim(),"opt-out".trim()))
			.put("DO", new Pair<>("opt-out".trim(),"opt-out".trim()))
			.put("CX", new Pair<>("opt-out".trim(),"opt-out".trim()))
			.put("CU", new Pair<>("opt-out".trim(),"opt-out".trim()))
			.put("CR", new Pair<>("opt-out".trim(),"opt-out".trim()))
			.put("CN", new Pair<>("opt-out".trim(),"opt-out".trim()))
			.put("CL", new Pair<>("opt-out".trim(),"opt-out".trim()))
			.put("CK", new Pair<>("opt-out".trim(),"opt-out".trim()))
			.put("CI", new Pair<>("opt-out".trim(),"opt-out".trim()))
			.put("CC", new Pair<>("opt-out".trim(),"opt-out".trim()))
			.put("BZ", new Pair<>("opt-out".trim(),"opt-out".trim()))
			.put("BT", new Pair<>("opt-out".trim(),"opt-out".trim()))
			.put("BR", new Pair<>("opt-in ".trim(),"opt-in ".trim()))
			.put("BO", new Pair<>("opt-out".trim(),"opt-out".trim()))
			.put("BN", new Pair<>("opt-out".trim(),"opt-out".trim()))
			.put("BH", new Pair<>("opt-out".trim(),"opt-out".trim()))
			.put("BD", new Pair<>("opt-out".trim(),"opt-out".trim()))
			.put("AS", new Pair<>("opt-out".trim(),"opt-out".trim()))
			.put("AF", new Pair<>("opt-out".trim(),"opt-out".trim()))
			.put("AE", new Pair<>("opt-out".trim(),"opt-out".trim()))
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
	
	
	@GET
	@PermitAll
	@Path("/{surveyId}/consentagreement/{countryCode}")
	public Response getConsentAgreement(@PathParam("surveyId") String surveyId, @PathParam("countryCode") String countryCode) throws IOException{
//		Map<String,String result=Lists.newArrayList();
		for(String k:consentCountries.keySet()){
//			System.out.println("jobroles:: "+dept+".matches("+k+") = "+dept.matches(k));
			if (countryCode.matches(k)){
//				Map result=new MapBuilder<String,String>().put("key","value").build();
				Pair<String,String> p=consentCountries.get(k);
				
				List<Map<String,Object>> result=Lists.newArrayList(
						new MapBuilder<String,Object>().put("name", "by Email").put("checked",p.getFirst() .equalsIgnoreCase("opt-out")).build(),
						new MapBuilder<String,Object>().put("name", "by Phone").put("checked",p.getSecond().equalsIgnoreCase("opt-out")).build()
						);
				return Response.ok(Json.toJson(result)).build();
//				result=new MapBuilder<String,String>().build();// consentCountries.get(k);
//				break;
			}
		}
		return Response.ok(Json.toJson("{}")).build();
	}
	
}
