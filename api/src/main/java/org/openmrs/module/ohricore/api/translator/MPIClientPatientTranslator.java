package org.openmrs.module.ohricore.api.translator;

import org.hl7.fhir.r4.model.Patient;
import org.openmrs.module.fhir2.api.translators.OpenmrsFhirUpdatableTranslator;
import org.openmrs.module.ohricore.api.model.MPIClientPatient;

import javax.annotation.Nonnull;

/**
 * @author smallGod date: 08/12/2022
 */
public interface MPIClientPatientTranslator extends OpenmrsFhirUpdatableTranslator<org.openmrs.Patient, MPIClientPatient> {
	
	/**
	 * Maps a {@link org.openmrs.Patient} to a {@link Patient}
	 * 
	 * @param patient the patient to translate
	 * @return the corresponding FHIR patient
	 */
	@Override
	MPIClientPatient toFhirResource(@Nonnull org.openmrs.Patient patient);
	
	/**
	 * Maps a {@link Patient} to a {@link org.openmrs.Patient}
	 * 
	 * @param patient the FHIR patient to map
	 * @return the corresponding OpenMRS patient
	 */
	@Override
	org.openmrs.Patient toOpenmrsType(@Nonnull MPIClientPatient patient);
	
	/**
	 * Maps a {@link Patient} to an existing {@link org.openmrs.Patient}
	 * 
	 * @param currentPatient the patient to update
	 * @param patient the FHIR patient to map
	 * @return the updated OpenMRS patient
	 */
	@Override
	org.openmrs.Patient toOpenmrsType(@Nonnull org.openmrs.Patient currentPatient, @Nonnull MPIClientPatient patient);
	
}
