package org.openmrs.module.ohricore.api.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.*;
import org.openmrs.api.context.Context;
import org.openmrs.module.ohricore.ConceptUUID;
import org.openmrs.module.ohricore.api.OHRIComputedConcept;
import org.springframework.stereotype.Component;

/**
 * @author MayanjaXL, Amos, Stephen, smallGod date: 28/06/2021
 */
@Component("hivStatusComputedConcept")
public class HIVStatusComputedConcept implements OHRIComputedConcept {
	
	private Log log = LogFactory.getLog(this.getClass());
	
	@Override
	public Obs compute(Encounter triggeringEncounter) {
		
		log.info("HIVStatusComputedConcept compute() called");
		return null;
	}
	
	@Override
	public EncounterType getTargetEncounterType() {
		return null;
	}
	
	@Override
	public Concept getConcept() {
		return Context.getConceptService().getConceptByUuid(ConceptUUID.HIV_STATUS.getUUID());
	}
}
