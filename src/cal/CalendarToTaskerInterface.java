package cal;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.*;

public class CalendarToTaskerInterface{
	static Calendar service;
	static CalendarList calendarList;
	static List<CalendarListEntry> items;
	
	public static final String taskListHeader = "TaskList";
	static Map<String,TaskList> taskLists;
	
	static Scanner in = new Scanner(System.in);
	
	private static void setUp() throws IOException, GeneralSecurityException {
		HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
		JacksonFactory jsonFactory = JacksonFactory.getDefaultInstance();
		
		// The clientId and clientSecret can be found in Google Developers Console
		String clientId = "541247774273-4eus8tio7pmig97bn84crnc80s46k81v.apps.googleusercontent.com";
		String clientSecret = "f4b-szhSYrXJz7en_dRLUImp";
		
		// Or your redirect URL for web based applications.
		String redirectUrl = "https://www.example.com/oauth2callback";
		String scope = "https://www.googleapis.com/auth/calendar";
		
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow(
		    httpTransport, jsonFactory, clientId, clientSecret, Collections.singleton(scope));
		// Step 1: Authorize
		String authorizationUrl = flow.newAuthorizationUrl().setRedirectUri(redirectUrl).build();
		
		// Point or redirect your user to the authorizationUrl.
		System.out.println("Go to the following link in your browser:");
		System.out.println(authorizationUrl);
		
		// Read the authorization code from the standard input stream.
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("What is the authorization code?");
		String code = in.readLine();
		// End of Step 1
		
		// Step 2: Exchange
		GoogleTokenResponse response = flow.newTokenRequest(code).setRedirectUri(redirectUrl)
		    .execute();
		// End of Step 2
		
		Credential credential = new GoogleCredential.Builder()
		    .setTransport(httpTransport)
		    .setJsonFactory(jsonFactory)
		    .setClientSecrets(clientId, clientSecret)
		    .build().setFromTokenResponse(response);
	
		service = new Calendar.Builder(httpTransport, jsonFactory, credential)
			.setApplicationName("CalendarToTaskerInterface").build();
	}
	
	private static void getTaskList() throws IOException {
		String pageToken = null;
		do {
			calendarList = service.calendarList().list().setPageToken(pageToken).execute();
			items = calendarList.getItems();
			
			taskLists = new HashMap<String,TaskList>();
			
			for (CalendarListEntry calendarListEntry : items) {
				String calName = calendarListEntry.getSummary();
				if(calName.startsWith(taskListHeader)){
					TaskList taskList = new TaskList(calendarListEntry);
					taskLists.put(taskList.getName(), taskList);
				}
			}
			pageToken = calendarList.getNextPageToken();
		} while (pageToken != null);
	}
	
	/**
	 * ENTRY POINT
	 * @param args
	 * @throws IOException
	 * @throws GeneralSecurityException
	 */
	public static void main(String args[]) throws IOException, GeneralSecurityException{
		setUp();
		getTaskList();
		interact();
	}
	
	/**
	 * Console Interface for creating/managing tasklists n stuff
	 * @throws IOException
	 */
	public static void interact() throws IOException{
		final String commands = "------Commands------\n" +
								"0 - Exit\n" +
								"1 - Create TaskList\n" +
								"2 - View TaskList\n" +
								"3 - View Calendars\n" +
								"4 - Update";
		
		boolean working = true;
		int num;
		
		while(working){
			System.out.println(commands);
			num = in.nextInt();
			in.nextLine();					//Sets Scanner to wait for the next input
			switch(num){
				case 0:{
					working = false;
					break;
				}
				case 1:{
					createTaskList();
					break;
				}
				case 2:{
					viewTaskList();
					break;
				}
				case 3:{
					printCalendars();
					break;
				}
				case 4:{
					refreshCalendars();
					break;
				}
				default:
					break;
			}
			System.out.println();
		}
		in.close();
	}
	
	/**
	 * @throws IOException
	 */
	private static void createTaskList() throws IOException{
		System.out.println("Enter TaskList name:");
		String name = in.nextLine();

		TaskList taskList = new TaskList(name, service);
		taskLists.put(name, taskList);
		refreshCalendars();
	}
	
	private static void viewTaskList(){
		if(taskLists.isEmpty()){
			System.out.println("No Existing TaskLists");
			return;
		}
		System.out.println("0: Main Menu");
		System.out.println("1: Open TaskList");
		printTaskLists();
		int num = in.nextInt();
		in.nextLine();
		switch(num){
		case 0:{
			return;
		}
		case 1:{
			while (true) {
				System.out.println("Pick a TaskList");
				String tasklistname = in.nextLine();
				if(taskLists.containsKey(tasklistname)){
					showTaskList(tasklistname);
					break;
				}
			}
		}
		default:
			break;
		}
	}
	
	private static void printTaskLists(){
		System.out.println("TaskLists:");
		for(String taskList:taskLists.keySet()){
			System.out.println(taskList);
		}
	}
	
	private static void showTaskList(String name){
		
	}
	
	private static void printCalendars(){
		System.out.println("Calendars:");
		for(CalendarListEntry calendar:items){
			System.out.println(calendar.getSummary());
		}
	}
	
	private static void refreshCalendars() throws IOException{
		String pageToken = null;
		do {
			calendarList = service.calendarList().list().setPageToken(pageToken).execute();
			items = calendarList.getItems();
			
			for (CalendarListEntry calendarListEntry : items) {
				String calName = calendarListEntry.getSummary();
				if(calName.startsWith(taskListHeader)){
					TaskList taskList = new TaskList(calendarListEntry);
					if(!taskLists.containsKey(taskList.getName())){
						taskLists.put(taskList.getName(), taskList);
					}
				}
			}
			pageToken = calendarList.getNextPageToken();
		} while (pageToken != null);
	}
}