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
import org.openmrs.api.context.Context;
import org.openmrs.api.db.ContextDAO;
import org.openmrs.module.BaseModuleActivator;
import org.openmrs.module.ohricore.task.QueryLabResultsTask;
import org.openmrs.module.ohricore.task.UpdateLuceneIndexesTask;
import org.openmrs.scheduler.SchedulerService;
import org.openmrs.scheduler.TaskDefinition;

/**
 * This class contains the logic that is run every time this module is either started or shutdown
 */
public class OHRICoreActivator extends BaseModuleActivator {
	
	private Log log = LogFactory.getLog(this.getClass());
	
	public OHRICoreActivator() {
		super();
	}
	
	/**
	 * @see #started()
	 */
	public void started() {
		log.info("Started OHRICore");
	}
	
	/**
	 * @see #shutdown()
	 */
	public void shutdown() {
		log.info("Shutdown OHRICore");
	}
	
	@Override
	public void contextRefreshed() {
		
		String taskName = "Query Lab Results";
		Long repeatInterval = 60L; //second
		String taskClassName = QueryLabResultsTask.class.getName();
		String description = "Query Lab Results Task - DISI";
		
		String namibiaLuceneIndexUpdateTaskName = "Update Namibia Lucene Indexes";
		Long namibiaLuceneRepeatInterval = 31536000L; //second
		String namibiaLuceneTaskClassName = UpdateLuceneIndexesTask.class.getName();
		String namibiaLuceneDescription = "Update Namibia Lucene Indexes task";
		
		addTask(taskName, taskClassName, repeatInterval, description);
		addTask(namibiaLuceneIndexUpdateTaskName, namibiaLuceneTaskClassName, namibiaLuceneRepeatInterval,
		    namibiaLuceneDescription);
	}
	
	@Override
	public void stopped() {
		super.stopped();
	}
	
	@Override
	public void willRefreshContext() {
		
		super.willRefreshContext();
		/*
		System.out.println("Refresh Lucene indexes...");
		ContextDAO contextDAO = Context.getRegisteredComponents(ContextDAO.class).get(0);
		contextDAO.updateSearchIndex();// Refresh Lucene indexes
		*/
	}
	
	@Override
	public void willStart() {
		super.willStart();
	}
	
	@Override
	public void willStop() {
		super.willStop();
	}
	
	void addTask(String name, String className, Long repeatInterval, String description) {
		
		SchedulerService scheduler = Context.getSchedulerService();
		TaskDefinition taskDefinition = scheduler.getTaskByName(name);
		if (taskDefinition == null) {
			
			taskDefinition = new TaskDefinition(null, name, description, className);
			taskDefinition.setStartOnStartup(Boolean.TRUE);
			taskDefinition.setRepeatInterval(repeatInterval);
			scheduler.saveTaskDefinition(taskDefinition);
		}
	}
}
