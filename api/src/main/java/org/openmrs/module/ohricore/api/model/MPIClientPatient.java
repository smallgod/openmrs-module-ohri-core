package org.openmrs.module.ohricore.api.model;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import org.hl7.fhir.r4.model.Patient;

/**
 * @author smallGod
 * @date 08/12/2022
 */

@ResourceDef(id = "MPIPatient", name = "MPIPatient", profile = "http://hl7.org/fhir/StructureDefinition/Patient")
public class MPIClientPatient extends Patient {}
