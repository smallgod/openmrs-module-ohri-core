package org.openmrs.module.ohricore.api.impl;

import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.module.ohricore.api.OHRIComputedConcept;

/**
 * @author smallGod date: 28/06/2021
 */
public class OnARTComputedConcept implements OHRIComputedConcept {
	
	@Override
	public Obs computeObs(Encounter encounter) {
		return null;
	}
	
	@Override
	public void persistObs(Obs obs) {
		
	}
}
