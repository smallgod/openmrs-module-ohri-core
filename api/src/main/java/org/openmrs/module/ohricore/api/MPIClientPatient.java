package org.openmrs.module.ohricore.api;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import org.hl7.fhir.r4.model.Patient;

/**
 * @author smallGod
 */

@ResourceDef(name = "MPIPatient", profile = "http://hl7.org/fhir/StructureDefinition/MPIPatient")
public class MPIClientPatient extends Patient {}
