package org.openmrs.module.ohricore.api.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.*;
import org.openmrs.api.context.Context;
import org.openmrs.module.ohricore.ConceptUUID;
import org.openmrs.module.ohricore.api.OHRIComputedConcept;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Set;

/**
 * @author MayanjaXL, Amos, Stephen, smallGod date: 28/06/2021
 */

@Component("string3ComputedConcept")
public class String3ComputedConcept implements OHRIComputedConcept {
	
	private Log log = LogFactory.getLog(this.getClass());
	
	@Override
	public Obs compute(Encounter triggeringEncounter) {
		
		log.info("compute called...");
		
		String string1 = null;
		String string2 = null;
		for (Obs observation : triggeringEncounter.getObs()) {
			
			String value = observation.getValueText();
			if (value == null || value.trim().isEmpty()) {
				value = String.valueOf(observation.getValueNumeric());
			}
			
			ConceptUUID conceptUIID = ConceptUUID.convert(observation.getConcept().getUuid());
			switch (conceptUIID) {
				case STRING1:
					string1 = value;
					break;
				case STRING2:
					string2 = value;
					break;
				default:
					break;
			}
		}
		
		String string3Val = null;
		if (!(string1 == null || string2 == null)) {
			string3Val = string1 + " - " + string2;
		}
		return createOrUpdate(triggeringEncounter, string3Val);//voids str3 if either str1 or str2 is Null
	}
	
	@Override
	public Concept getConcept() {
		return Context.getConceptService().getConceptByUuid(ConceptUUID.STRING3.getUUID());
	}
	
	@Override
	public EncounterType getTargetEncounterType() {
		return null;
	}
}
