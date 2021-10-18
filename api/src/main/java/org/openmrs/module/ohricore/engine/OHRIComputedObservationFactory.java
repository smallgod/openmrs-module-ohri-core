package org.openmrs.module.ohricore.engine;

import org.openmrs.Encounter;
import org.openmrs.api.context.Context;
import org.openmrs.module.ohricore.api.OHRIComputedObservation;

import java.util.List;

/**
 * @author MayanjaXL, Amos, Stephen, smallGod date: 28/06/2021
 */
public class OHRIComputedObservationFactory {
	
	public static List<OHRIComputedObservation> getComputedObservations(Encounter encounter) {
		//TODO:Use the encounter passed to narrow the list of ohriComputedConcepts based on an encounterType
		List<OHRIComputedObservation> ohriComputedObservations = Context
		        .getRegisteredComponents(OHRIComputedObservation.class);
		return ohriComputedObservations;
	}
}
