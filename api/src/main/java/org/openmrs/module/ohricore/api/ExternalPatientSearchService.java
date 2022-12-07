package org.openmrs.module.ohricore.api;

import org.openmrs.Patient;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.APIException;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.ohricore.model.ExternalPatient;
import org.openmrs.util.PrivilegeConstants;

/**
 * @author smallGod date: 06/12/2022
 */
public interface ExternalPatientSearchService extends OpenmrsService {
	
	@Authorized({ PrivilegeConstants.GET_PATIENTS })
	ExternalPatient getPatientByHealthId(String healthId) throws APIException;
}
