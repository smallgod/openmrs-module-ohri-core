package org.openmrs.module.ohricore.engine;

import org.openmrs.module.ohricore.engine.OHRIComputedConcept;
import org.openmrs.module.ohricore.engine.String3ComputedConcept;

/**
 * @author smallGod date: 24/06/2021
 */
public interface OHRIComputedConceptPublisher {
	
	/**
	 * A list of ComputedConcept processes to publish
	 */
	OHRIComputedConcept[] SERVICES = { new String3ComputedConcept() };
	
	/**
	 * Number of published services available
	 */
	int NUM_OF_SERVICES = SERVICES.length;
}
