package task;


import java.util.*;

public class TaskList {
	private String name;
	private String id;
	private List<Task> tasklist = new ArrayList<Task>();
	private Map<String, Task> tasklistmap = new HashMap<String, Task>();

	public TaskList(String name){
		this.name = name;
	}
	
	public TaskList(String name, String id){
		this.name = name;
		this.id = id;
	}
	
	public String getName(){
		return new String(this.name);
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public String getId(){
		return new String(this.id);
	}
	
	public void setId(String id){
		this.id = id;
	}
	
	public void addTask(Task task){
		this.tasklist.add(task);
		this.tasklistmap.put(task.getName(),task);
	}
	
	public List<Task> getTasks(){
		return this.tasklist;
	}
	
	public Task get(String task){
		return this.tasklistmap.get(task);
	}
}
