package org.openmrs.module.ohricore.api;

import org.hl7.fhir.r4.model.Patient;
import org.openmrs.module.fhir2.api.FhirService;

import javax.annotation.Nonnull;

/**
 * @author smallGod
 */
public interface MPIClientService extends FhirService<MPIClientPatient> {
	
	MPIClientPatient getByHealthId(@Nonnull String healthId);
}
