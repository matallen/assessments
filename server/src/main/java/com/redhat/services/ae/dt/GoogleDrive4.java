package com.redhat.services.ae.dt;

import java.io.InputStreamReader;
import java.util.Collections;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;

public class GoogleDrive4{
	
	public static void main(String[] args){
		new GoogleDrive4().test();
	}
	
	public void test(){
		
	}
	
//  private static InputStream downloadFile(Drive service, File file) {
//    if (file.getDownloadUrl() != null && file.getDownloadUrl().length() > 0) {
//      try {
//        HttpResponse resp =
//            service.getRequestFactory().buildGetRequest(new GenericUrl(file.getDownloadUrl()))
//                .execute();
//        return resp.getContent();
//      } catch (IOException e) {
//        // An error occurred.
//        e.printStackTrace();
//        return null;
//      }
//    } else {
//      // The file doesn't have any content stored on Drive.
//      return null;
//    }
//  }
	
	private static String GOOGLE_CLIENT_ID="664385754396-p6calid35i7dff010epltvgjmj169qd6.apps.googleusercontent.com";
	private static String GOOGLE_CLIENT_SECRET="r0g8plu_z6B6n8V3yUy6gtQI";
	
//	private void asd(){
//		
//		NetHttpTransport httpTransport=GoogleNetHttpTransport.newTrustedTransport();
//		Drive driveService = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT)
//				.setApplicationName(APPLICATION_NAME)
//				.build();
//		
//		driveService.files().export(fileId, "application/pdf").executeMediaAndDownloadTo(outputStream);
//		
//		GoogleCredential credential = new GoogleCredential().setAccessToken(accessToken);
//		
//	}
//	
//	
//	private static Credential authorize() throws Exception {
//	  // load client secrets
//	  GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(PlusSample.class.getResourceAsStream("/client_secrets.json")));
//	  // set up authorization code flow
//	  GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
//	      httpTransport, JSON_FACTORY, clientSecrets,
//	      Collections.singleton(PlusScopes.PLUS_ME)).setDataStoreFactory(
//	      dataStoreFactory).build();
//	  // authorize
//	  return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
//	}
	
}
