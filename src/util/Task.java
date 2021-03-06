package util;

import java.util.ArrayList;
import java.util.Date;

import core.runtime.TaskManager;

public abstract class Task implements IChangeable {
	
	public enum State { INITIAL, START, RUNNING, IDLE, FINISH, INTERRUPT }
	
	private TaskManager taskManager;
	private Thread thread;
	private ArrayList<IChangeListener> listeners;

	private State state;
	private long delay;
	protected long cycle;
	protected long timeout;
	private Date start;
	
	public Task(String name, TaskManager taskManager){
		
		this.taskManager = taskManager;
		thread = new Thread(name){
			@Override
			public void run(){
				Task.this.run();
			}
		};
		listeners = new ArrayList<IChangeListener>();
		
		state = State.INITIAL;
		delay = 0;
		cycle = 0;
		timeout = 0;
		start = null;
	}
	
	@Override
	public void addListener(IChangeListener listener){ listeners.add(listener); }
	@Override
	public void removeListener(IChangeListener listener){ listeners.remove(listener); }
	@Override
	public void notifyListeners(){
		for(IChangeListener listener : listeners){ listener.changed(this); }
	}
	
	public long getThreadId() {return thread.getId();}
	public String getThreadName(){ return thread.getName(); }
	
	private void setState(State state){
		this.state = state;
		taskManager.log(state.toString()+"\t"+thread.getName()+" <"+thread.getId()+">");
		notifyListeners();
	}
	public State getState(){ return state; }
	
	public void setCyclic(long cycle){ this.cycle = cycle; }
	private boolean isCyclic(){ return cycle > 0; }
	
	public boolean isExpired(){
		
		if(start != null && timeout > 0){
			if((new Date()).getTime() >= start.getTime()+timeout){
				return true;
			}else{
				return false;
			}
		}else{
			return false;
		}
	}
	
	private void run(){
		
		taskManager.register(this);
		setState(State.START);
		try{
			Thread.sleep(delay);
			if(isCyclic()){
				runCyclic();
			}else{
				runOnce();
			}
		}catch(InterruptedException e){ 
			setState(State.INTERRUPT);
		}finally{
			setState(State.FINISH);
			taskManager.deregister(this);
		}
	}
	
	private void runCyclic() throws InterruptedException {
		
		while(isCyclic() && !thread.isInterrupted()){
			runOnce();
			Thread.sleep(cycle);
		}
	}
	
	private void runOnce(){
		
		start = new Date();
		setState(State.RUNNING);
		try{
			runTask();
		}finally{
			setState(State.IDLE);
			start = null;
		}
	}
	
	protected abstract void runTask();
	
	public void asyncRun(long delay, long timeout){
		
		this.delay = delay;
		this.timeout = timeout;
		thread.start();
	}
	
	public void syncRun(long delay, long timeout) throws InterruptedException {
		
		asyncRun(delay, timeout);
		thread.join();
	}
	
	@SuppressWarnings("deprecation")
	public void asyncStop(final long timeout){
		
		Thread kill = new Thread(new Runnable(){
			@Override
			public void run() {
				try{
					if(thread.isAlive()){ thread.interrupt(); }
					if(timeout > 0){
						Thread.sleep(timeout);
						if(thread.isAlive()){ thread.stop(); }
					}
				}catch(Exception e){
					taskManager.error(e);
				}
			}
		});
		kill.start();
	}
	
	public void syncStop(long timeout) throws InterruptedException {
		
		asyncStop(timeout);
		thread.join();
	}
}
