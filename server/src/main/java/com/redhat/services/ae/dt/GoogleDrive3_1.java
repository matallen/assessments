package com.redhat.services.ae.dt;

/**
 * 
 * V3 is for Team Drive compatibility
 * 
 * For this we need to move from odeke-em's drive, to gdrive-org/gdrive + a customized version of...
 * 
 * 
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFHyperlink;
import org.apache.poi.ss.formula.functions.Hyperlink;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFHyperlink;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.api.client.util.Lists;
import com.redhat.services.ae.MapBuilder;
import com.redhat.services.ae.utils.ChatNotification;
import com.redhat.services.ae.utils.Json;
//import com.redhat.services.portfolio.Heartbeat;
//import com.redhat.services.portfolio.Heartbeat.Utils;
//import com.redhat.services.portfolio.utils.ChatNotification;
import com.redhat.services.ae.utils.ChatNotification.ChatEvent;
//import com.redhat.services.portfolio.utils.DownloadFile;
//import com.redhat.services.portfolio.utils.Json;
//import com.redhat.services.portfolio.utils.TimeUtils;
import com.redhat.services.ae.utils.TimeUtils;


public class GoogleDrive3_1 {
  private static final Logger log=LoggerFactory.getLogger(GoogleDrive3_1.class);
  
  public static String DEFAULT_EXECUTABLE="/home/%s/drive_linux";
//  public static final String DEFAULT_PULL_COMMAND=DEFAULT_EXECUTABLE+" pull -export xls -quiet=true --id %s"; //worked with 0.3.1
  public static String DEFAULT_PULL_COMMAND=DEFAULT_EXECUTABLE+" pull -export xls -no-prompt --id %s"; // 0.3.7+ changed its output that we parse
  public static String DEFAULT_WORKING_FOLDER="/home/%s/google_drive";
  public static Integer DEFAULT_MAX_COLUMNS=22;
  private static String gdriveType;
  private long cacheExpiryInMs;
  private static Map<String,File> cache=new HashMap<String, File>();
  private static Map<String,Long> cacheExpiry=new HashMap<String, Long>();
  
  public static String getDefaultExecutable(){
    return String.format(DEFAULT_EXECUTABLE, System.getProperty("user.name"));
  }
  public synchronized static void clearCache(){
  	log.info(String.format("Clearing %s items from googleDrive cache", cache.size()));
  	cache.clear();
  }
  
  public GoogleDrive3_1(){
  	this.cacheExpiryInMs=-1;
    if (null!=System.getenv("CACHE_EXPIRY") && System.getenv("CACHE_EXPIRY").matches("\\d+")) this.cacheExpiryInMs=Long.parseLong(System.getenv("CACHE_EXPIRY"));
    log.info("Cache set to "+TimeUtils.msToSensibleString(this.cacheExpiryInMs));
  }
  public GoogleDrive3_1(long cacheExpiryInMs){
    this.cacheExpiryInMs=cacheExpiryInMs;
    // ENV takes precedence
    if (null!=System.getenv("CACHE_EXPIRY") && System.getenv("CACHE_EXPIRY").matches("\\d+")) this.cacheExpiryInMs=Long.parseLong(System.getenv("CACHE_EXPIRY"));
    log.info("Cache set to "+TimeUtils.msToSensibleString(this.cacheExpiryInMs));
  }
  
  public interface HeaderRowFinder{
  	public int getHeaderRow(XSSFSheet s);
  }
  
  public enum DriverType{drive,gdrive}
  
  public boolean isInitialised(){
  	return null!=gdriveType;
  }

  public static void main(String[] args) throws Exception{
//  	String PortfolioDatabase="1aPR0_uNRJCVLT9c8mqfEpNvQ2FZBdcD9pL0u6mksu2U";
//  	String FeedbackResponsesTeamDrive="1cyVtpYUMW26JBoqbVJJTD79ay--6F7EMAllEVKesn1Q";
//  	String portfolioDB2="1akun5-QdlWfIfFm0_MKNfOZXUdZZGEFd-wEvsH4-rvg";
//  	
//  	// Test using odeke's drive (no team drive capability)
////  	GoogleDrive3.initialise("/home/%s/drive_linux_odeke", DriverType.drive, "v0.3.9");
//  	
//  	// Test using gdrive (with team drive capability)
//  	GoogleDrive3_1.initialise("/home/%s/google_drive", DriverType.gdrive, "v2.1.1PreRelease");
//  	
  	GoogleDrive3_1.initialise("/home/%s/google_drive", DriverType.gdrive, "v2.1.1PreRelease");
  	
  	String SAPResponses="1lbM6AQJNVlaHSRJ4YC8eWBF00rD5nJDo7dji3uto2D";//A;
  	SimpleDateFormat dateFormatter=null;
  	String sheetName="Section Recommendations";
  	
  	GoogleDrive3_1 gd=new GoogleDrive3_1();
  	File file=gd.downloadFile(SAPResponses);
//  	System.out.println("file exists="+(file!=null?file.exists():"Nope!"));
  	List<Map<String, String>> test=gd.parseExcelDocumentAsStrings(file, sheetName, new GoogleDrive3_1.HeaderRowFinder(){ public int getHeaderRow(XSSFSheet s){
			return GoogleDrive3_1.SheetSearch.get(s).find(0, "Description").getRowIndex();
		}}, dateFormatter);
  	System.out.println(Json.toJson(test));
  }
  
  public static void initialise(DriverType type, String version) {
	  String workingFolder=("linux".equals(getOS())?"/home/":"/Users/")+"%s/google_drive";
	  initialise(workingFolder, type, version);
  }

  public static void initialise(String workingFolder, DriverType type, String version) {
  	try{
  		
  		// load the config
  		String cfgString=IOUtils.toString(GoogleDrive3_1.class.getClassLoader().getResourceAsStream("GoogleDrive3_initialize.json"));
//  		Map<String,Map<String,String>> cfgs=Json.newObjectMapper(true).readValue(cfgString, new TypeReference<Map<String, Map<String,String>>>(){});
  		Map<String,Map<String,String>> cfgs=Json.toObject(cfgString, new TypeReference<Map<String, Map<String,String>>>(){});
  		Map<String,String> cfg=cfgs.get(type+"/"+version+"/"+getOS());
  		
  		if (null==cfg) throw new RuntimeException("Unable to find config for: "+(type+"/"+version+"/"+getOS()));
  		
//  		String exe=RegExHelper.extract(cfg.get("url"), ".+/(.+)$");
  		String exe=String.format("%s-%s-%s", type, getOS(), version);
  		gdriveType=type.name();
  		DEFAULT_WORKING_FOLDER=String.format(workingFolder, System.getProperty("user.name"));
  		DEFAULT_EXECUTABLE=new File(new File(DEFAULT_WORKING_FOLDER, gdriveType).getAbsolutePath(), exe).getAbsolutePath();
  		DEFAULT_PULL_COMMAND=cfg.get("commandTemplate");
  		
  		
	    if (!new File(GoogleDrive3_1.getDefaultExecutable()).exists()){
	    	File credsFile=null;
	    	try{
		    	
	    		// attempt to download the binary
	    		log.info("Downloading '"+type+"/"+version+"' from: "+cfg.get("url"));
	    		new DownloadFile().get(cfg.get("url"), DEFAULT_EXECUTABLE, PosixFilePermission.OTHERS_EXECUTE);
	    		
	    		// set the creds file location
	    		credsFile=new File(String.format(cfg.get("credentialsLocation"), "home", System.getProperty("user.name")));
	    		
	    		log.info("Deploying credentials.json in: "+credsFile);
	    		credsFile.getParentFile().mkdirs();
	        InputStream is=GoogleDrive3_1.class.getClassLoader().getResourceAsStream("/gd_credentials.json");
	        if (null!=is){
	        	log.info("... from internal classloader path of '/gd_credentials.json'");
	        	IOUtils.copy(is, new FileOutputStream(credsFile));
	        }else if (null!=System.getenv("GD_CREDENTIALS")){
	        	log.info("... from env variable 'GD_CREDENTIALS'");
	        	IOUtils.write(System.getenv("GD_CREDENTIALS").getBytes(), new FileOutputStream(credsFile));
	        }else{
	        	log.error("no gdrive creds specified in either resources, or system props");
	        }
	        log.info("drive credentials file contains: "+IOUtils.toString(new FileInputStream(credsFile)));
	    		
	    		
	    	}catch(IOException e){
	        System.err.println("Failed to initialise gdrive and/or credentials, cleaning up exe and creds");
	        if (null!=credsFile) credsFile.delete();
	        new File(GoogleDrive3_1.getDefaultExecutable()).delete();
	        e.printStackTrace();
	    	}
	    }else{
	      log.info("gdrive already initialised. Existing binary is here: "+GoogleDrive3_1.getDefaultExecutable());
	    }
	    
  	}catch(IOException e){
  		log.error("Initialization failed: "+e.getMessage());
  		e.printStackTrace();
  	}
  }
  
  private static Pattern drive_pattern = Pattern.compile("to '(.+)'$");;
  private static Pattern gdrive_pattern = Pattern.compile("'(.+?)'");;
    
  public synchronized File downloadFile(String fileId) throws IOException, InterruptedException {
  	return downloadFile(fileId, -1);
  }
  public synchronized File downloadFile(String fileId, int cacheOverride) throws IOException, InterruptedException {
    long _cacheExpiryInMs=this.cacheExpiryInMs;
  	
  	if (null==gdriveType) throw new RuntimeException("Not yet Initialized");
  	
  	if (cacheOverride>0){
  		log.debug("Cache override of "+TimeUtils.msToSensibleString(cacheOverride)+" set in http request");
  		_cacheExpiryInMs=cacheOverride;
  	}
  	
  	if (_cacheExpiryInMs>0){ // ie. need to check the cache
//  		log.debug("downloadFile():: Cache timeout is "+TimeUtils.msToSensibleString(_cacheExpiryInMs));
  		if (cache.containsKey(fileId) && cacheExpiry.get(fileId)>System.currentTimeMillis()){
  			log.info(String.format("downloadFile():: cacheExp[%s]:: - returning cached copy - %s",TimeUtils.msToSensibleString(_cacheExpiryInMs), fileId));
  			return cache.get(fileId);
  		}else{
  			cache.remove(fileId);
  			cacheExpiry.remove(fileId);
  		}
  	}
  	
  	String command = String.format(DEFAULT_PULL_COMMAND, DEFAULT_EXECUTABLE, fileId);
  	
    String googleDrivePath=String.format(DEFAULT_WORKING_FOLDER, System.getProperty("user.name"));
    File workingFolder=new File(googleDrivePath, fileId);
    log.debug("downloadFile():: Downloading google file: "+fileId + " [workingFolder="+workingFolder.getAbsolutePath()+"]");
    workingFolder.mkdirs(); // just in case it's not there
//    System.out.println(this.getClass().getName()+"::downloadFile() - Using working folder: "+workingFolder.getAbsolutePath());
//    System.out.println(this.getClass().getName()+"::downloadFile() - Downloading google file: "+fileId);
    log.info(String.format("downloadFile():: Command: %s", command));
    
    
    if (googleDrivePath.contains("google")){
    	File[] files=workingFolder.listFiles(new FilenameFilter(){
    		@Override public boolean accept(File parentFile, String filename){
					return filename.endsWith(".xlsx");
			}});
    	if (files.length>0){
    		File src=files[0];
    		File dest=new File(src.getParentFile(), src.getName()+".bak");
    		if (src.isFile() && dest.isFile()) Files.delete(Paths.get(dest.getAbsolutePath())); // cleanup if there's an old backup
    		if (src.isFile() && !dest.isFile()) Files.move(Paths.get(src.getAbsolutePath()), Paths.get(dest.getAbsolutePath())); // move xlsx into the backup location
    	}
    	
    	// if file exists, move it (dont delete it) just in case the google pull doesnt work
//    	File f=workingFolder.listFiles()[0];
//    	Files.move(Paths.get(f.getAbsolutePath()), Paths.get(f.getAbsolutePath()+".bak"));
      //recursivelyDelete(workingFolder, new File(DEFAULT_EXECUTABLE).getName());
    }else
      log.warn("downloadFile():: Not cleaning working folder unless it contains the name 'google' - for safety reasons");
    
    
    Process exec = Runtime.getRuntime().exec(command, null, workingFolder);
    
    exec.waitFor();
    String syserr = IOUtils.toString(exec.getErrorStream());
    String sysout = IOUtils.toString(exec.getInputStream());
    log.debug("downloadFile():: sysout=\""+sysout.replaceAll("\\n", " ")+"\"; syserr=\""+syserr+"\"");
    
    try{
    	if (gdriveType.equals("drive")){
    		if (!sysout.contains("Resolving...") && !sysout.contains("Everything is up-to-date")) throw new RuntimeException("Error running google drive script: " + sysout);
    		if (!sysout.contains("Everything is up-to-date")){
    			Matcher matcher = drive_pattern.matcher(sysout);
    			if (matcher.find()){
    				String preFilePath = matcher.group(1);
    				File result=new File(workingFolder, preFilePath);
    				if (_cacheExpiryInMs>0){
    					cache.put(fileId, result);
    					cacheExpiry.put(fileId, System.currentTimeMillis()+_cacheExpiryInMs);
    				}
    				return result;
    			}
    		}
    	}
    	
    	if (gdriveType.equals("gdrive")){
    		if (!sysout.contains("Exported")) throw new RuntimeException("Error running google drive script: " + sysout);
    		Matcher matcher = gdrive_pattern.matcher(sysout);
    		if (matcher.find()){
    			String preFilePath = matcher.group(1);
    			File result=new File(workingFolder, preFilePath);
    			if (_cacheExpiryInMs>0){
    				cache.put(fileId, result);
    				cacheExpiry.put(fileId, System.currentTimeMillis()+_cacheExpiryInMs);
    			}
    			return result;
    		}
    	}
    }catch(RuntimeException e){
    	e.printStackTrace();
    }
    
    // should only get here if something went wrong with the download, so replace the ".bak" files and use those for now
    File f=workingFolder.listFiles()[0];
    log.error(this.getClass().getName()+"::downloadFile() - ERROR:: Problem occured downloading, returning old file: "+f.getAbsolutePath());
    Path newPath=Paths.get(f.getAbsolutePath().replaceAll("\\.bak$", ""));
		Files.move(Paths.get(f.getAbsolutePath()), newPath);
    
		new ChatNotification().send(ChatEvent.onWarning, "WARNING: ("+System.getenv("HOSTNAME")+") Unable to pull google file (USING OLD FILE UNTIL FIXED): "+command);
		
    return newPath.toFile();
    // System.out.println(exec.exitValue());
  }
  
  private static String getOS(){
  	if (System.getProperty("os.name").indexOf("win")>0) return "windows";
  	if (System.getProperty("os.name").indexOf("mac")>0) return "mac";
  	if (System.getProperty("os.name").indexOf("nux")>0 || System.getProperty("os.name").indexOf("nix")>0) return "linux";
  	return null;
  }
  
  
  public int getHeaderRow(XSSFSheet s){
//  	// example to search for a row where in column 0 there is text "State"
//  	return SheetSearch.get().col(0).text("State").find(s).getRowIndex();
    return 0;
  }
  
  public boolean valid(Map<String,Object> entry){
    return true;
  }
  
  public static class SheetSearch{
//  	private int col;
//  	private String text;
  	private XSSFSheet sheet;
  	public SheetSearch(XSSFSheet sheet){
  		this.sheet=sheet;
  	}
  	static public SheetSearch get(XSSFSheet sheet){
  		return new SheetSearch(sheet);
  	}
//  	public SheetSearch col(int col){ this.col=col; return this; }
//  	public SheetSearch text(String text){ this.text=text; return this; }
//  	public SheetSearch sheet(XSSFSheet sheet){ this.sheet=sheet; return this; }
  	
  	public XSSFCell find(String text){
  		return find(0, text);
  	}
  	public XSSFCell find(int col, String text){
  		
  		for(int iRow=0;iRow<=sheet.getLastRowNum();iRow++){
  			XSSFCell cell=sheet.getRow(iRow).getCell(col);
  			
  			if (cell.getCellType()==CellType.STRING){
//  				if (text.equals(cell.getStringCellValue())){
					if (cell.getStringCellValue().matches(text)){
  					return cell;
  				}
  			}
  		}
  		return null;
  	}
  }

  private List<Map<String,String>> toListStringString(List<Map<String,Object>> in){
  	List<Map<String,String>> result=Lists.newArrayList();
  	for(Map<String, Object> m:in){
  		Map<String,String> map=new HashMap<>();
  		for (Entry<String, Object> e:m.entrySet())
  			map.put(e.getKey(), (String)e.getValue());
  		result.add(map);
  	}
  	return result;
  }
  
  
  public List<Map<String,String>> parseExcelDocumentAsStrings(File file, HeaderRowFinder finder, SimpleDateFormat dateFormatter) throws FileNotFoundException, IOException{
  	return toListStringString(
  		parseExcelDocument(file, null, finder, dateFormatter, DEFAULT_MAX_COLUMNS)
  	);
  }
  public List<Map<String,String>> parseExcelDocumentAsStrings(File file, String sheetName, HeaderRowFinder finder, SimpleDateFormat dateFormatter) throws FileNotFoundException, IOException{
  	return toListStringString(
  		parseExcelDocument(file, sheetName, finder, dateFormatter, DEFAULT_MAX_COLUMNS)
		);
  }
  public List<Map<String,String>> parseExcelDocumentAsStrings(File file, String sheetName, HeaderRowFinder finder, SimpleDateFormat dateFormatter, int maxColumns) throws FileNotFoundException, IOException{
  	return toListStringString(
  			parseExcelDocument(file, sheetName, finder, dateFormatter, maxColumns)
		);
  }
  
  public List<Map<String,Object>> parseExcelDocument(File file, HeaderRowFinder finder, SimpleDateFormat dateFormatter) throws FileNotFoundException, IOException{
  	return parseExcelDocument(file, null, finder, dateFormatter, DEFAULT_MAX_COLUMNS);
  }
  public List<Map<String,Object>> parseExcelDocument(File file, String sheetName, HeaderRowFinder finder, SimpleDateFormat dateFormatter) throws FileNotFoundException, IOException{
  	return parseExcelDocument(file, sheetName, finder, dateFormatter, DEFAULT_MAX_COLUMNS);
  }
  public List<Map<String,Object>> parseExcelDocument(File file, String sheetName, HeaderRowFinder finder, SimpleDateFormat dateFormatter, int maxColumns) throws FileNotFoundException, IOException{
    // parse excel file using apache poi
    // read out "tasks" and create/update solutions
    // use timestamp (column A) as the unique identifier (if in doubt i'll hash it with the requester's username)
    List<Map<String,Object>> entries=new ArrayList<Map<String,Object>>();
    FileInputStream in=null;
    if (file==null || !file.exists()) return new ArrayList<Map<String,Object>>();
    try{
    	log.debug("parseExcelDocument():: file is "+file.getAbsolutePath() +" (exists="+file.exists()+", size="+(file.length()/1024)+"k)");
      in=new FileInputStream(file);
      XSSFWorkbook wb=new XSSFWorkbook(in);
      
      Map<String,String> linkReferences=new HashMap<>(); // this map contains cell references that reference cells containing hyperlinks that we want to transfer to the source formula cell
      int sheetIndex=0;
      if (null!=sheetName) sheetIndex=wb.getSheetIndex(sheetName);
      
      if (sheetIndex<0) throw new RuntimeException("Unable to find sheet with name '"+sheetName+"'");
      
      XSSFSheet s=wb.getSheetAt(sheetIndex);
//      int maxColumns=25;
//      int headerRow=getHeaderRow(s);
      int headerRow=finder.getHeaderRow(s);
      
      for(int iRow=headerRow+1;iRow<=s.getLastRowNum();iRow++){
//        Map<String,String> e=new HashMap<String,String>();
        Map<String,Object> e2=new HashMap<String,Object>();
        boolean allRowCellsEmpty=true;
        for(int iColumn=0;iColumn<=maxColumns;iColumn++){
          if (s.getRow(headerRow).getCell(iColumn)==null) continue;
          String header=s.getRow(headerRow).getCell(iColumn).getStringCellValue();
          XSSFRow row = s.getRow(iRow);
          if (row==null) break; // next line/row
          XSSFCell cell=row.getCell(iColumn);
          if (cell==null) continue; // try next cell/column
          
            try{
            	switch(cell.getCellType()){
            	case NUMERIC: 
            		if (HSSFDateUtil.isCellDateFormatted(cell)){
            			e2.put(header, null!=dateFormatter?dateFormatter.format(cell.getDateCellValue()):cell.getDateCellValue().toString());
//            			System.out.println("parseExcepDocument():: formatting date to -> "+e2.get(header));
            			break;
            		}else{
            			e2.put(header, String.valueOf(cell.getNumericCellValue())); break;
            		}
            	case BOOLEAN: e2.put(header, String.valueOf(cell.getBooleanCellValue())); break;
            	case FORMULA: 
            		
            		String link=readFormulaToHyperlink(cell);
            		if (null!=link) linkReferences.put(cell.getCellFormula(), link);
            		
            		if (cell.getCellFormula().matches("[A-Z]+[0-9]+")){ // it's a cell reference - do does that cell contain a link?
            			CellReference ref=new CellReference(cell.getCellFormula());
            			Row r=s.getRow(ref.getRow());
            			if (r!=null) {
            				XSSFCell c=(XSSFCell)r.getCell(ref.getCol());
            				try{
            					link=readFormulaToHyperlink(c);
            					if (null!=link) linkReferences.put(cell.getCellFormula(), link);
            				}catch(Exception sink){
            					sink.printStackTrace();
            				}
            			}
            		}
            		
            		
//            		e2.put(header, cell.getCellFormula()); break;
            	default:
            		String value=null;//cell.getRawValue() getRichStringCellValue() Hyperlink()
            		if (cell.getCellType().equals(CellType.FORMULA) && linkReferences.containsKey(cell.getCellFormula()))
            		  value=linkReferences.get(cell.getCellFormula());
            			
            		try{
            			// get hyperlink if you can
            			XSSFHyperlink hlink=cell.getHyperlink();
            			if (hlink!=null)
            				value=cell.getStringCellValue()+"|"+hlink.getAddress();
            		}catch(Exception exx){}
            		
            		try{
            			if (null==value) value=cell.getStringCellValue();
            		}catch(Exception e){
            			value=cell.getRawValue();
            		}
            		if (value!=null && !"".equals(value))
            			e2.put(header, value);
            		break;
            	}
            	
            	allRowCellsEmpty=allRowCellsEmpty && (e2.get(header)==null || "".equals(e2.get(header)));
            }catch(Exception ex){}
            if (!e2.containsKey(header))
              try{
                e2.put(header, cell.getDateCellValue().toString());
              }catch(Exception ex){}
          
        }
        
        if (allRowCellsEmpty) break;
        
        if (valid(e2)){
          e2.put("ROW_#", String.valueOf(iRow-1));
          entries.add(e2);
        }
      }
    }finally{
      IOUtils.closeQuietly(in);
    }
    
//    System.out.println("GoogleDrive3::parseExcelDocument() - leaving method, file "+file.getAbsolutePath() +" is "+(file.length()/1024)+"k in size");
    
    return entries;
  }
  
  private String readFormulaToHyperlink(XSSFCell cell){
//  	List<String> result=Lists.newArrayList();
  	
  	if (null!=cell.getHyperlink()){
//			result.add(cell.getStringCellValue()+"|"+cell.getHyperlink().getAddress());
  		return cell.getStringCellValue()+"|"+cell.getHyperlink().getAddress();
  	}
  	
		if (cell.getCellType().equals(CellType.FORMULA) && cell.getCellFormula().contains("HYPERLINK")){
			Pattern p=Pattern.compile("\"(.+?)\".*\"(.+?)\"");
			Matcher m=p.matcher(cell.getCellFormula());
			if (m.find()){
				String url=m.group(1);
				String name=m.group(2);
//				e2.put(header, name+"|"+url); break;
//				result.add(name+"|"+url);
				return name+"|"+url;
			}
		}
		
		
		return null;
  }
  
  private void recursivelyDelete(File file, String excluding){
    for(File f:file.listFiles()){
      if (!f.getName().startsWith(".") && f.isDirectory())
        recursivelyDelete(f, excluding);
      if (!f.getName().startsWith(".") && !f.getName().equals(excluding)){
        log.info("recursivelyDelete():: deleting file: "+f.getAbsolutePath());
      	f.delete();
      }
    }
  }
  
}
