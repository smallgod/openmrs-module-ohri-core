package org.openmrs.module.ohricore.api;

import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.springframework.stereotype.Component;

/**
 * @author smallGod
 * date: 28/06/2021
 */
public interface OHRIComputedConcept {

    default void computeAndPersistObs(Encounter encounter) {
        Obs obs = computeObs(encounter);
        persistObs(obs);
    }

    Obs computeObs(Encounter encounter);

    void persistObs(Obs obs);
}
