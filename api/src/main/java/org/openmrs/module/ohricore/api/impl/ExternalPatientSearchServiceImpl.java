package org.openmrs.module.ohricore.api.impl;

import org.openmrs.Patient;
import org.openmrs.api.APIException;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.ohricore.api.ExternalPatientSearchService;
import org.openmrs.module.ohricore.fhir.FhirClient;
import org.openmrs.module.ohricore.model.ExternalPatient;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author smallGod date: 06/12/2022
 */
public class ExternalPatientSearchServiceImpl extends BaseOpenmrsService implements ExternalPatientSearchService {
	
	@Override
	@Transactional(readOnly = true)
	public ExternalPatient getPatientByHealthId(String healthId) throws APIException {
		
		try {
			Patient patient = FhirClient.getPatient(healthId);
			
			ExternalPatient extPatient = new ExternalPatient();
			extPatient.setFamilyName(patient.getFamilyName());
			extPatient.setGivenName(patient.getGivenName());
			extPatient.setName(patient.getGivenName() + " " + patient.getFamilyName());
			
			return extPatient;
		}
		catch (Exception exc) {
			System.err.println("Failed to get Patient");
			exc.printStackTrace();
		}
		return null;
	}
}
