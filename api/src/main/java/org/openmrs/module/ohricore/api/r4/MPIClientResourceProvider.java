package org.openmrs.module.ohricore.api.r4;

import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.IdType;
import org.openmrs.module.fhir2.api.annotations.R4Provider;
import org.openmrs.module.ohricore.api.model.MPIClientPatient;
import org.openmrs.module.ohricore.api.MPIClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

/**
 * @author smallGod
 * @date 08/12/2022
 */

@Component("mpiClientFhirR4ResourceProvider")
@R4Provider
public class MPIClientResourceProvider implements IResourceProvider {
	
	@Autowired
	private MPIClientService mpiClientService;
	
	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return MPIClientPatient.class;
	}
	
	@Read
	@SuppressWarnings("unused")
	public MPIClientPatient getByHealthId(@IdParam @Nonnull IdType healthId) {
		
		MPIClientPatient patient = mpiClientService.getByIdentifier(healthId.getIdPart());
		if (patient == null) {
			throw new ResourceNotFoundException("Could not find patient with Health ID: " + healthId);
		}
		return patient;
	}
}
