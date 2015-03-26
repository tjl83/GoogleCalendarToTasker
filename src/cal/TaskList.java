package cal;

import java.io.IOException;
import java.util.*;

import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

public class TaskList {
	private String name;
	private Preferences preferences;
	private ArrayList<Task> tasklist;
	private Map<String, Task> tasklistmap;
	
	private CalendarListEntry calendar;
	
	public TaskList(String name, Calendar service) throws IOException{
		this.name = name;
		this.tasklistmap = new HashMap<String, Task>();
		
		// Create a new calendar
		com.google.api.services.calendar.model.Calendar calendar = new com.google.api.services.calendar.model.Calendar();
		calendar.setSummary(CalendarToTaskerInterface.taskListHeader + "-" + name);
		
		// Insert the new calendar
		com.google.api.services.calendar.model.Calendar createdCalendar = service.calendars().insert(calendar).execute();
		
		// Create a new calendar list entry
		CalendarListEntry calendarListEntry = new CalendarListEntry();
		calendarListEntry.setId(createdCalendar.getId());
		
		// Insert the new calendar list entry
		this.calendar = service.calendarList().insert(calendarListEntry).execute();
	}
	
	public TaskList(CalendarListEntry cal) throws IOException{
		this.calendar = cal;
		String calName = cal.getSummary();
		assert calName.startsWith(CalendarToTaskerInterface.taskListHeader);
		String name = calName.substring(calName.indexOf("-")+1);
		this.name = name;
		
		updateTasks();
	}
	
	private void updateTasks() throws IOException{
		String pageToken = null;
		do {
		  Events events = CalendarToTaskerInterface.service.events().list(this.calendar.getId()).setPageToken(pageToken).execute();
		  List<Event> items = events.getItems();
		  for (Event event : items) {
			  Task task = new Task(event);
			  this.tasklistmap.put(event.getSummary(), task);
		  }
		  pageToken = events.getNextPageToken();
		} while (pageToken != null);
	}
	
	public String getName(){
		return this.name;
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public ArrayList<Task> getTasks(){
		return null;
	}
	
	public Task addTask(String name){
		Task task = new Task(name);
		this.tasklistmap.put(name, task);
		return task;
	}
	
	public void addTask(Task task){
		this.tasklistmap.put(task.getName(), task);
	}
	
	public Task getTask(String name){
		return this.tasklistmap.get(name);
	}
	
	public void removeTask(String name){
		this.tasklistmap.remove(name);
	}

	public CalendarListEntry getCal(){
		return this.calendar;
	}
}
