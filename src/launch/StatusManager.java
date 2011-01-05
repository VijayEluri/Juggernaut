package launch;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import data.Error;
import util.DateTools;

import launch.ILifecycleListener.Lifecycle;

public class StatusManager {

	public enum Status {
		UNDEFINED, PROCESSING, SUCCEED, ERROR, FAILURE, CANCEL
	}
	
	private LifecycleObject parent;
	private Status status;
	private int progress;
	private int progressMax;
	private Date start;
	private Date end;
	private ArrayList<Error> errors;
	
	public StatusManager(LifecycleObject parent){
		
		this.parent = parent;
		status = Status.UNDEFINED;
		progress = 0;
		progressMax = 0;
		start = null;
		end = null;
		errors = new ArrayList<Error>();
	}
	
	public long getHash(){
		return (parent.getId()+status.toString()).hashCode();
	}
	
	public Status getStatus(){ return status; }
	public void setStatus(Status status){
		
		if(getStatusValue(this.status) < getStatusValue(status)){
			this.status = status;
			parent.notifyListeners(Lifecycle.PROCESSING);
		}
	}
	
	public void setProgressMax(int progress){ progressMax = progress; }
	public void addProgress(int progress){ 
		
		if(progress > 0 && (this.progress + progress) <= progressMax){
			this.progress += progress; 
			parent.notifyListeners(Lifecycle.PROCESSING);
		}
	}
	public int getProgress(){ 
		
		if(progress > 0 && progress <= progressMax){
			return (int)Math.round(((double)progress / (double)progressMax) * 100);
		}else{
			return 0;
		}
	}
	
	public void setStart(Date start){ 
		this.start = start; 
		setStatus(Status.PROCESSING);
	}
	public Date getStart(){ return start; }
	public void setEnd(Date end){ 
		this.end = end; 
		if(status == Status.PROCESSING){
			setStatus(Status.SUCCEED);
		}
	}
	public Date getEnd(){ return end; }
	
	public ArrayList<Error> getErrors(){ return errors; }
	public void addError(String id, String message){
		errors.add(new Error(id, message));
		setStatus(Status.ERROR);
	}
	
	public HashMap<String, String> getProperties(){
		
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("Status", status.toString());
		if(start != null){
			map.put("Start", DateTools.getTextDate(start));
		}
		if(end != null){
			map.put("End", DateTools.getTextDate(end));
		}
		if(start != null &&end != null){
			map.put("Time", DateTools.getTimeDiff(start, end)+" min");
		}
		return map;
	}

	public static int getStatusValue(Status status){
		
		if(status == Status.UNDEFINED){
			return 0;
		}else if(status == Status.PROCESSING){
			return 1;
		}else if(status == Status.SUCCEED){
			return 2;
		}else if(status == Status.ERROR){
			return 3;
		}else if(status == Status.FAILURE){
			return 4;
		}else if(status == Status.CANCEL){
			return 4;
		}else{
			return -1;
		}
	}
	
	public static boolean isError(Status status) {
		return 
		status != Status.SUCCEED && status != Status.CANCEL;
	}
	
	public static String getStatusHtml(Status status) {

		String color = "black";
		if(status == Status.UNDEFINED){
			color = "yellow";
		}else if(status == Status.SUCCEED){
			color = "green";
		}else if(status == Status.ERROR){
			color = "red";
		}else if(status == Status.FAILURE){
			color = "purple";
		}
		return "<font color='"+color+"'>"+status.toString()+"</font>";
	}
}
