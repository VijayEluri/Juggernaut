package core;

import java.util.ArrayList;

import util.IChangedListener;
import util.Task;

import logger.Logger;
import logger.Logger.Module;

/** provides access to the application's heap */
public class HeapManager implements ISystemComponent {

	private Logger logger;
	private Runtime runtime;
	private HeapStatus heap;
	private HeapStatusUpdater updater;
	private ArrayList<IChangedListener> listeners;
	
	public HeapManager(Logger logger){
		
		this.logger = logger;
		runtime = Runtime.getRuntime();
		heap = new HeapStatus();
		listeners = new ArrayList<IChangedListener>();
	}
	
	public void addListener(IChangedListener listener){ listeners.add(listener); }
	
	public void notifyListeners(){
		for(IChangedListener listener : listeners){
			listener.changed(this);
		}
	}
	
	@Override
	public void init() throws Exception {
		if(updater == null){
			updater = new HeapStatusUpdater();
			updater.asyncRun(0, 0);
		}
	}
	
	@Override
	public void shutdown() throws Exception {
		if(updater != null){
			updater.syncKill();
			updater = null;
		}
	}

	public HeapStatus getHeapStatus(){
		synchronized(heap){
			return new HeapStatus(heap);
		}
	}
	
	private void setHeapStatus(HeapStatus status){
		synchronized(heap){
			heap = new HeapStatus(status);
		}
		notifyListeners();
	}
	
	private class HeapStatusUpdater extends Task {

		public static final long CYCLE = 30 * 1000; // 30 sec

		public HeapStatusUpdater() {
			super("HeapStatus", logger);
			setCycle(CYCLE);
		}

		@Override
		protected void runTask() {
			HeapStatus status = new HeapStatus();
			status.usedMemory = (runtime.totalMemory() - runtime.freeMemory());
			status.freeMemory = runtime.freeMemory();
			status.totalMemory = runtime.totalMemory();
			status.maxMemory = runtime.maxMemory();
			setHeapStatus(status);
		}
	}
	
	public class HeapStatus {
		
		public long usedMemory; 		// bytes
		public long freeMemory; 		// bytes
		public long totalMemory; 		// bytes
		public long maxMemory; 			// bytes
		
		public HeapStatus(){
			usedMemory = 0;
			freeMemory = 0;
			totalMemory = 0;
			maxMemory = 0;
		}
		
		public HeapStatus(HeapStatus status){
			this.usedMemory = status.usedMemory;
			this.freeMemory = status.freeMemory;
			this.totalMemory = status.totalMemory;
			this.maxMemory = status.maxMemory;
		}
	}
	
	/** runs the garbage collector */
	public void cleanup(){
		
		GarbageCollector task = new GarbageCollector();
		task.asyncRun(0, GarbageCollector.TIMEOUT);
	}
	
	private class GarbageCollector extends Task {
		
		public static final long TIMEOUT = 10 * 60 * 1000; // 10 min
		
		public GarbageCollector() {
			super("GarbageCollector", logger);
		}

		@Override
		protected void runTask() {
			logger.log(Module.COMMON, "Running Garbage Collector");
			runtime.gc();
		}
	}
}