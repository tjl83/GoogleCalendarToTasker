package cal;

import com.google.api.services.calendar.model.Event;

public class Task {
	private String name;
	private String description;
	
	private Event event;
	
	public Task(String name){
		this.name = name;
	}
	
	public Task(Event event){
		this.name = event.getSummary();
		this.event = event;
	}
	
	public String getName(){
		return name;
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public String getDescription(){
		return description;
	}
	
	public void setDescription(String description){
		this.description = description;
	}
}