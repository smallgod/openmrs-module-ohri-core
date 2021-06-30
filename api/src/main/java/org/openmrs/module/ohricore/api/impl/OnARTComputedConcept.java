package org.openmrs.module.ohricore.api.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.ohricore.api.OHRIComputedConcept;
import org.springframework.stereotype.Component;

/**
 * @author MayanjaXL, Amos, Stephen, smallGod
 * date: 28/06/2021
 */
@Component
public class OnARTComputedConcept implements OHRIComputedConcept {
	
	@Override
	public org.openmrs.Obs compute(org.openmrs.Encounter triggeringEncounter) {
		return null;
	}
	
	@Override
	public org.openmrs.Concept getConcept() {
		return null;
	}
	
	@Override
	public org.openmrs.EncounterType getTargetEncounterType() {
		return null;
	}
}
