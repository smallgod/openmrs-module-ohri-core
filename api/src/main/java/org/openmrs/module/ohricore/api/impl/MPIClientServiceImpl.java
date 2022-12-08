package org.openmrs.module.ohricore.api.impl;

import org.hl7.fhir.r4.model.Patient;
import org.openmrs.module.fhir2.api.dao.FhirDao;
import org.openmrs.module.fhir2.api.impl.BaseFhirService;
import org.openmrs.module.fhir2.api.translators.PatientTranslator;
import org.openmrs.module.ohricore.api.MPIClientService;
import org.openmrs.module.ohricore.fhir.FhirClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;

/**
 * @author smallGod
 */

@Component
@Transactional
public class MPIClientServiceImpl extends BaseFhirService<Patient, org.openmrs.Patient> implements MPIClientService {
	
	@Autowired
	private PatientTranslator translator;
	
	@Override
	protected FhirDao<org.openmrs.Patient> getDao() {
		return null;
	}
	
	@Override
	public Patient getByHealthId(@Nonnull String healthId) {
		System.out.println("getByHealthId(" + healthId + ")");
		
		org.openmrs.Patient patient = FhirClient.getPatient(healthId);
		return getTranslator().toFhirResource(patient);
	}
	
	@Override
	public PatientTranslator getTranslator() {
		return translator;
	}
	
	public void setTranslator(PatientTranslator translator) {
		this.translator = translator;
	}
}
