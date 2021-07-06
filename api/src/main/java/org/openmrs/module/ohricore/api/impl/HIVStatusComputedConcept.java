package org.openmrs.module.ohricore.api.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.*;
import org.openmrs.api.context.Context;
import org.openmrs.module.ohricore.engine.CommonsUUID;
import org.openmrs.module.ohricore.engine.HIVStatusConceptUUID;
import org.openmrs.module.ohricore.api.OHRIComputedConcept;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
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

        /*
        Is Positive (condition 1):
        The value of this concept should be Positive if any of the following conditions is true:
        From HTS:
            - The client has at least one Final HIV Test that turned positive

        Is Positive (condition 2):
        From Other programmes:
            - The client has an ART initiation Date
            - The client is receiving ARVs
            - The client has a detectable viral load

        Is Negative:
        The value of this concept should be Negative if all of the following conditions are true:
            - The client is NOT HIV positive
            - The client has at-least one Final HIV Test that turned Negative within the last 90 days

        Is Unknown:
            - All else
         */

        Concept hivTestConcept = getConcept(HIVStatusConceptUUID.FINAL_HIV_TEST_RESULT);
        Patient patient = triggeringEncounter.getPatient();
        List<Obs> hivTestObs = Context.getObsService().getObservationsByPersonAndConcept(patient.getPerson(), hivTestConcept);

        boolean isNegative = false;

        for (Obs obs : hivTestObs) {

            if (obs.getVoided()) return null;

            Concept obsValueCoded = obs.getValueCoded();
            if (obsValueCoded.getUuid().equals(CommonsUUID.POSITIVE)) {
                return createOrUpdate(patient, CommonsUUID.POSITIVE);

            } else if (obsValueCoded.getUuid().equals(CommonsUUID.NEGATIVE)
                    && valueDateIsWithin90Days(obs.getValueDate())) {
                isNegative = true;
            }
        }
        return isNegative ? createOrUpdate(patient, CommonsUUID.NEGATIVE) : createOrUpdate(patient, CommonsUUID.UNKNOWN);
    }

    @Override
    public EncounterType getTargetEncounterType() {
        return null;
    }

    @Override
    public Concept getConcept() {
        return Context.getConceptService().getConceptByUuid(HIVStatusConceptUUID.HIV_STATUS);
    }

    boolean valueDateIsWithin90Days(Date valueDate) {

        Calendar cal = Calendar.getInstance();
        cal.setTime(valueDate);
        cal.add(Calendar.DATE, -90);

        return valueDate.compareTo(cal.getTime()) > 0; //valueDate occurs after (90days ago)
    }
}
