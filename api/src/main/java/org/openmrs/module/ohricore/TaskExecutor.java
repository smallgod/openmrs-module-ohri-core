package org.openmrs.module.ohricore;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

//import java.util.concurrent.*;
import java.util.concurrent.*;

/**
 * @author MayanjaXL, Amos, Stephen, smallGod date: 24/06/2021
 */
public class TaskExecutor {
	
	private ExecutorService executorService;
	
	private int stopDelay;
	
	private TimeUnit timeUnit;
	
	private final Log logger = LogFactory.getLog(this.getClass());
	
	/**
	 * Thread pool configuration Mutex
	 **/
	static final String CONFIG_MUTEX = "CONFIG_MUTEX";
	
	/**
	 * Duration in minutes beyond which a task should be terminated if it hasn't completed
	 */
	final int TASK_TIMEOUT = 5;
	
	private TaskExecutor() {
	}
	
	public static TaskExecutor getInstance() {
		return TaskExecutorSingletonHolder.INSTANCE;
	}
	
	private static class TaskExecutorSingletonHolder {
		
		private static final TaskExecutor INSTANCE = new TaskExecutor();
	}
	
	/**
	 * Thread-safe configuration of a Thread pool to handle server requests
	 * 
	 * @param numOfThreads
	 * @param stopDelay
	 * @param timeUnit
	 * @return
	 * @throws NullPointerException
	 * @throws IllegalArgumentException
	 */
	public TaskExecutor createThreadPool(final int numOfThreads, final int stopDelay, final TimeUnit timeUnit)
	        throws NullPointerException, IllegalArgumentException {
		
		synchronized (CONFIG_MUTEX) {
			
			if (this.executorService == null) {
				this.stopDelay = stopDelay;
				this.timeUnit = timeUnit;
				this.executorService = Executors.newFixedThreadPool(numOfThreads);
			}
			return this;
		}
	}
	
	/**
	 * Submit a task to the thread pool
	 * 
	 * @param task
	 * @return
	 * @throws RejectedExecutionException
	 * @throws CancellationException
	 * @throws InterruptedException
	 * @throws TimeoutException
	 * @throws ExecutionException
	 */
	public String submitTask(Callable<String> task) throws RejectedExecutionException, CancellationException,
	        InterruptedException, TimeoutException, ExecutionException {
		
		Future<String> future = this.executorService.submit(task);
		return future.get(TASK_TIMEOUT, TimeUnit.MINUTES);
	}
	
	/**
	 * The following method shuts down an ExecutorService in two phases, first, by calling shutdown
	 * to reject incoming tasks, and then calling shutdownNow, if necessary, to cancel any lingering
	 * tasks once (timeToWait time) elapses.
	 */
	public void shutdownPool() {

        try {

            this.executorService.shutdown(); // New tasks can't be submitted
            if (!this.executorService.awaitTermination(stopDelay, timeUnit)) {
                // Wait for tasks to cancel
                if (!this.executorService.awaitTermination(++stopDelay, timeUnit)) {
                    this.executorService.shutdownNow();
                }
            }
        } catch (InterruptedException | SecurityException exc) {
            logger.error("Error shutting down pool, attempting forced shutdown");
            this.executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
