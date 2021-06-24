package org.openmrs.module.ohricore;

/**
 * @author smallGod date: 24/06/2021
 */
public interface NamedConstant {
	
	/**
	 * Thread pool configuration Mutex
	 **/
	String CONFIG_MUTEX = "CONFIG_MUTEX";
	
	String UPDATE_ENCOUNTER_MUTEX = "UPDATE_ENCOUNTER_MUTEX";
	
	/**
	 * Duration in minutes beyond which a task should be terminated if it hasn't completed
	 */
	int TASK_TIMEOUT = 5;
}
