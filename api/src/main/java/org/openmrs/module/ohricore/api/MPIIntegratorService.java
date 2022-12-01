package org.openmrs.module.ohricore.api;

import org.openmrs.annotation.Authorized;
import org.openmrs.api.OpenmrsService;

/**
 * @author smallGod date: 29/11/2022
 */
public class MPIIntegratorService implements OpenmrsService {
	
	/**
	 * Gets an OrderTemplate by id
	 * 
	 * @param orderTemplateId the OrderTemplate id
	 * @return the OrderTemplate with given id, or null if none exists
	 */
	//@Authorized({ OrderTemplatesConstants.MANAGE_ORDER_TEMPLATES })
	//OrderTemplate getOrderTemplate(Integer orderTemplateId);
	
	@Override
	public void onStartup() {
		
	}
	
	@Override
	public void onShutdown() {
		
	}
}
