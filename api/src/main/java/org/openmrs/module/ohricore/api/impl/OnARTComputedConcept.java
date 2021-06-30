package org.openmrs.module.ohricore.api.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.*;
import org.openmrs.module.ohricore.api.OHRIComputedConcept;
import org.springframework.stereotype.Component;

/**
 * @author MayanjaXL, Amos, Stephen, smallGod
 * date: 28/06/2021
 */
@Component
public class OnARTComputedConcept implements OHRIComputedConcept {
	
	private Log log = LogFactory.getLog(this.getClass());
	
	@Override
	public Obs compute(Encounter triggeringEncounter) {
		return null;
	}
	
	@Override
	public Concept getConcept() {
		return null;
	}
	
	@Override
	public EncounterType getTargetEncounterType() {
		return null;
	}
}
