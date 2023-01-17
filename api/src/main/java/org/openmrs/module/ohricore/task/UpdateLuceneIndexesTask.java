package org.openmrs.module.ohricore.task;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.ContextDAO;
import org.openmrs.scheduler.tasks.AbstractTask;

import static org.openmrs.module.ohricore.OhriCoreConstant.MUTEX_UPDATE_LUCENE_INDEXES;

/**
 * @author smallGod date: 19/12/2022
 */
public class UpdateLuceneIndexesTask extends AbstractTask {
	
	protected final Log logger = LogFactory.getLog(getClass());
	
	@Override
	public void execute() {
		
		if (!isExecuting) {
			
			System.out.println("UpdateLuceneIndexesTask running...");
			
			startExecuting();
			
			try {
				
				synchronized (MUTEX_UPDATE_LUCENE_INDEXES) {
					ContextDAO contextDAO = Context.getRegisteredComponents(ContextDAO.class).get(0);
					contextDAO.updateSearchIndex();// Refresh Lucene indexes
				}
				
			}
			catch (Exception e) {
				System.err.println("Error while running UpdateLuceneIndexesTask: " + e.getMessage());
			}
			finally {
				stopExecuting();
			}
		} else {
			System.err.println("Error, Task already running, can't execute again");
		}
	}
}
