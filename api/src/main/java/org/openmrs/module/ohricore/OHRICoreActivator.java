/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 * <p>
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.ohricore;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.BaseModuleActivator;

import java.util.concurrent.TimeUnit;

/**
 * This class contains the logic that is run every time this module is either started or shutdown
 */
public class OHRICoreActivator extends BaseModuleActivator {
	
	private Log log = LogFactory.getLog(this.getClass());
	
	private static final int NUM_OF_THREADS = 50; //TO-DO: Put these in props file
	
	private static final int STOP_DELAY = 5;
	
	private static final TimeUnit TIME_UNIT = TimeUnit.MINUTES;
	
	private TaskExecutor taskExecutor;
	
	/**
	 * @see #started()
	 */
	public void started() {
		
		taskExecutor = TaskExecutor.getInstance().configurePool(NUM_OF_THREADS, STOP_DELAY, TIME_UNIT);
		
		log.info("Started OHRICore");
	}
	
	/**
	 * @see #shutdown()
	 */
	public void shutdown() {
		this.taskExecutor.shutdownPool();
		log.info("Shutdown OHRICore");
	}
	
}
