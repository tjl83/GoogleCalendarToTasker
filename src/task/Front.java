package task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.*;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.google.api.services.calendar.model.EventDateTime;

public class Front {
	private static Calendar service;
	private static Scanner in;
	
	private static final String taskListHeader = "TaskList-";
	
	private static List<TaskList> taskLists;
	private static Set<TaskList> newTaskLists = new HashSet<TaskList>();
	private static Set<TaskList> modifiedTaskLists = new HashSet<TaskList>();
	
	private static Set<Task> newTasks = new HashSet<Task>();
	private static Set<Task> modifiedTasks = new HashSet<Task>();
	
	public static void main(String args[]) throws IOException, GeneralSecurityException{
		setUp();
		
		in = new Scanner(System.in);
		
		pickTaskList();
		
		in.close();
	}
	
	private static void pickTaskList() throws IOException{
		boolean working = true;
		int taskListNum;
		
		while(working){
			printTaskLists();
			taskListNum = in.nextInt();
			in.nextLine();
			
			if(taskListNum == 0){
				working = false;
				pushChanges();
			}
			else if(taskListNum == taskLists.size()+1){
				newTaskList();
			}
			else{
				taskListNum--;
				TaskList taskList = taskLists.get(taskListNum);
				pickTask(taskList);
			}
		}
	}
	
	private static void printTaskLists(){
		System.out.println("0: Exit");
		int count = 1;
		for(TaskList taskList : taskLists){
			System.out.println(count + ": " + taskList.getName());
			count++;
		}
		System.out.println(count + ": New TaskList");
	}
	
	private static void pushChanges() throws IOException{
		if(!newTaskLists.isEmpty()){
			createCalendars();
		}
		if(!modifiedTaskLists.isEmpty()){
			updateCalendars();
		}
	}
	
	private static void createCalendars() throws IOException{
		for(TaskList taskList:newTaskLists){
			String name = taskList.getName();
			
			// Create a new calendar
			com.google.api.services.calendar.model.Calendar calendar = new com.google.api.services.calendar.model.Calendar();
			calendar.setSummary(taskListHeader + "-" + name);
			
			// Insert the new calendar
			com.google.api.services.calendar.model.Calendar createdCalendar = service.calendars().insert(calendar).execute();
			
			// Create a new calendar list entry
			CalendarListEntry calendarListEntry = new CalendarListEntry();
			calendarListEntry.setId(createdCalendar.getId());
			
			// Insert the new calendar list entry
			service.calendarList().insert(calendarListEntry).execute();
			
			modifiedTaskLists.add(taskList);
		}
	}
	
	private static void updateCalendars() throws IOException{
		for(TaskList taskList:modifiedTaskLists){
			for(Task task: taskList.getTasks()){
				if(newTasks.contains(task)){
					// Create and initialize a new event
					Event event = new Event();
					event.setSummary(task.getName());

					Date startDate = new Date();
					Date endDate = new Date(startDate.getTime() + 3600000);
					DateTime start = new DateTime(startDate, TimeZone.getTimeZone("UTC"));
					event.setStart(new EventDateTime().setDateTime(start));
					DateTime end = new DateTime(endDate, TimeZone.getTimeZone("UTC"));
					event.setEnd(new EventDateTime().setDateTime(end));

					// Insert the new event
					service.events().insert(taskList.getId(), event).execute();
				}
				else if(modifiedTasks.contains(task)){
					
				}
			}
		}
	}
	
	private static void newTaskList(){
		System.out.println("Enter TaskList Name:");
		String name = in.nextLine();
		TaskList taskList = new TaskList(name);
		taskLists.add(taskList);
		newTaskLists.add(taskList);
	}
	
	private static void pickTask(TaskList taskList){
		List<Task> tasks = taskList.getTasks();
		
		boolean working = true;
		int taskNum;
		
		while(working){
			printTaskList(taskList);
			taskNum = in.nextInt();
			in.nextLine();
			
			if(taskNum == 0){
				working = false;
			}
			else if(taskNum == tasks.size()+1){
				newTask(taskList);
			}
			else{
				taskNum--;
				Task task = tasks.get(taskNum);
			}
		}
	}
	
	private static void printTaskList(TaskList taskList){
		System.out.println("0: TaskLists");
		int count = 1;
		for(Task task : taskList.getTasks()){
			System.out.println(count + ": " + task.getName());
		}
		System.out.println(count + ": New Task");
	}
	
	private static void newTask(TaskList taskList){
		System.out.println("Enter Task Name:");
		String name = in.nextLine();
		Task task = new Task(name);
		taskList.addTask(task);
		newTasks.add(task);
		modifiedTaskLists.add(taskList);
	}
	
	/**
	 * This initializes the authentication process and fetches the calendars that begin with TaskList-
	 * @throws IOException
	 * @throws GeneralSecurityException
	 */
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
		
		getTaskLists();
	}
	
	/**
	 * Fetches the calendars that begin with "TaskList-" and creates TaskLists and populates them with events from their respective calendar as Tasks.
	 * @throws IOException
	 */
	private static void getTaskLists() throws IOException{
		String pageToken = null;
		do {
			CalendarList calendarList = service.calendarList().list().setPageToken(pageToken).execute();
			List<CalendarListEntry> items = calendarList.getItems();
			
			taskLists = new ArrayList<TaskList>();
			
			for (CalendarListEntry calendarListEntry : items) {
				String calName = calendarListEntry.getSummary();
				if(calName.startsWith(taskListHeader)){
					TaskList taskList = parseCalendarToTaskList(calendarListEntry);
					populateTaskList(taskList, calendarListEntry.getId());
					taskLists.add(taskList);
				}
			}
			pageToken = calendarList.getNextPageToken();
		} while (pageToken != null);
		
	}
	
	/**
	 * Parses a calendar for any information we store there into a TaskList object
	 * @param calendar
	 * @return
	 */
	private static TaskList parseCalendarToTaskList(CalendarListEntry calendar){
		String calName = calendar.getSummary();
		String name = calName.substring(taskListHeader.length());
		TaskList taskList = new TaskList(name, calendar.getId());
		return taskList;
	}
	
	/**
	 * Populates a TaskList with events from its respective calendar as Tasks
	 * @param taskList
	 * @param id
	 * @throws IOException
	 */
	private static void populateTaskList(TaskList taskList, String id) throws IOException{
		String pageToken = null;
		do {
		  Events events = service.events().list(id).setPageToken(pageToken).execute();
		  List<Event> items = events.getItems();
		  for (Event event : items) {
			  Task task = parseEventToTask(event);
			  taskList.addTask(task);
		  }
		  pageToken = events.getNextPageToken();
		} while (pageToken != null);
	}
	
	/**
	 * Parses an event for any information we store there into a Task object
	 * @param event
	 * @return
	 */
	private static Task parseEventToTask(Event event){
		String name = event.getSummary();
		Task task = new Task(name);
		return task;
	}
}
