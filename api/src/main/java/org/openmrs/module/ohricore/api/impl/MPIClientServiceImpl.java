package org.openmrs.module.ohricore.api.impl;

import org.openmrs.module.fhir2.api.dao.FhirPatientDao;
import org.openmrs.module.fhir2.api.impl.BaseFhirService;
import org.openmrs.module.ohricore.api.MPIClientService;
import org.openmrs.module.ohricore.api.model.MPIClientPatient;
import org.openmrs.module.ohricore.api.translator.MPIClientPatientTranslator;
import org.openmrs.module.ohricore.fhir.MPIFhirClientUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * @author smallGod
 */

@Component
@Transactional
public class MPIClientServiceImpl extends BaseFhirService<MPIClientPatient, org.openmrs.Patient> implements MPIClientService {
	
	@Autowired
	private MPIClientPatientTranslator translator;

	@Autowired
	private FhirPatientDao dao;

	@Autowired
	private MPIFhirClientUtil fhirClient;
	
	public MPIClientServiceImpl() {
	}
	
	@Override
	public MPIClientPatient getByIdentifier(@Nonnull String identifier) {
		org.openmrs.Patient patient = fhirClient.getPatientByIdentifier(identifier);
		return getTranslator().toFhirResource(patient);
	}
	
	@Override
    public List<MPIClientPatient> searchWithFreeText(@Nonnull String freeText) {

        List<org.openmrs.Patient> patients = fhirClient.getPatientByFreeText(freeText);
        List<MPIClientPatient> mpiPatients = new ArrayList<>();

        for (org.openmrs.Patient patient : patients) {
            mpiPatients.add(getTranslator().toFhirResource(patient));
        }
        return mpiPatients;
    }
	
	@Override
	public MPIClientPatientTranslator getTranslator() {
		return translator;
	}
	
	public void setTranslator(MPIClientPatientTranslator translator) {
		this.translator = translator;
	}
	
	@Override
	protected FhirPatientDao getDao() {
		return this.dao;
	}
	
	public void setDao(FhirPatientDao dao) {
		this.dao = dao;
	}
}
