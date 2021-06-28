package org.openmrs.module.ohricore.engine;

/**
 * @author smallGod date: 24/06/2021
 */
public interface OHRIComputedConceptPublisher {

    /**
     * A list of ComputedConcept processes to publish
     */
    OHRIComputedConceptOLD[] SERVICES = {
            new String3ComputedConcept()
    };

    /**
     * Number of published services available
     */
    int NUM_OF_SERVICES = SERVICES.length;
}
