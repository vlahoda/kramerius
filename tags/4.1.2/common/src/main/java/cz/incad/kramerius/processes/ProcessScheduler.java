package cz.incad.kramerius.processes;


/**
 * Scheduler for scheduling and starting processes
 * @author pavels
 */
public interface ProcessScheduler {
    
    /**
     * Schedule next task
     */
	public void scheduleNextTask();

	/**
	 * Initialize process scheduler
	 * @param applicationLib
	 */
	public void init(String applicationLib);
	
	/**
	 * Returns application libs for creating CLASSPATH
	 * @return
	 */
	public String getApplicationLib();
	
}
