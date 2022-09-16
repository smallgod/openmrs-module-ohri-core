package org.openmrs.module.ohricore.task;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.ohricore.fhir.FhirProcessor;
import org.openmrs.scheduler.tasks.AbstractTask;

import static org.openmrs.module.ohricore.OhriCoreConstant.MUTEX_QUERY_LABRESULTS;

/**
 * @author smallGod date: 27/07/2022
 */
public class QueryLabResultsTask extends AbstractTask {
	
	protected final Log logger = LogFactory.getLog(getClass());
	
	@Override
	public void execute() {
		
		logger.debug("QueryLabResultsTask execute() called...");
		System.out.println("QueryLabResultsTask execute() called...");
		
		if (!isExecuting) {
			
			if (logger.isDebugEnabled())
				logger.debug("QueryLabResultsTask running...");
			System.out.println("QueryLabResultsTask running...");
			
			startExecuting();
			
			try {
				
				FhirProcessor fhirAccess = new FhirProcessor();
				synchronized (MUTEX_QUERY_LABRESULTS) {
					fhirAccess.fetchCompletedLabResults();
					fhirAccess.fetchRejectedLoadRequests();
				}
				
			}
			catch (Exception e) {
				logger.error("Error while running QueryLabResultsTask: ", e);
				System.err.println("Error while running QueryLabResultsTask: " + e.getMessage());
			}
			finally {
				stopExecuting();
			}
		} else {
			logger.error("Error, Task already running, can't execute again");
			System.err.println("Error, Task already running, can't execute again");
		}
	}
}
