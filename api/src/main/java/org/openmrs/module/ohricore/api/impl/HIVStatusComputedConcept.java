package org.openmrs.module.ohricore.api.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.*;
import org.openmrs.api.context.Context;
import org.openmrs.module.ohricore.engine.CommonsUUID;
import org.openmrs.module.ohricore.engine.HIVStatusConceptUUID;
import org.openmrs.module.ohricore.api.OHRIComputedConcept;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author MayanjaXL, Amos, Stephen, smallGod
 * date: 28/06/2021
 */
@Component("hivStatusComputedConcept")
public class HIVStatusComputedConcept implements OHRIComputedConcept {

    private Log log = LogFactory.getLog(this.getClass());

    @Override
    public Obs compute(Encounter triggeringEncounter) {

        /* Requirement 1:
        The value of this concept should be Positive if any of the following conditions is true:
        From HTS: The client has at least one Final HIV Test that turned positive
         */
        Concept hivTestConcept = getConcept(HIVStatusConceptUUID.FINAL_HIV_TEST_RESULT);
        Person person = triggeringEncounter.getPatient().getPerson();
        List<Obs> hivTestObs = Context.getObsService().getObservationsByPersonAndConcept(person, hivTestConcept);

        for (Obs obs : hivTestObs) {
            Concept obsValueCoded = obs.getValueCoded();
            if (obsValueCoded.getUuid().equals(CommonsUUID.POSITIVE)) {
                return createOrUpdate(triggeringEncounter.getPatient(), CommonsUUID.POSITIVE);
            }
        }
        return null;
    }

    @Override
    public EncounterType getTargetEncounterType() {
        return null;
    }

    @Override
    public Concept getConcept() {
        return Context.getConceptService().getConceptByUuid(HIVStatusConceptUUID.HIV_STATUS);
    }
}
