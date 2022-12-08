package org.openmrs.module.ohricore.api;

import org.openmrs.module.fhir2.api.FhirService;
import org.openmrs.module.ohricore.api.model.MPIClientPatient;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * @author smallGod
 */
public interface MPIClientService extends FhirService<MPIClientPatient> {
	
	MPIClientPatient getByIdentifier(@Nonnull String identifier);
	
	List<MPIClientPatient> searchWithFreeText(@Nonnull String freeText);
}
