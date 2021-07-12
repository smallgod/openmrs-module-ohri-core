package org.openmrs.module.ohricore.api.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.ohricore.api.OHRIComputedConcept;
import org.openmrs.module.ohricore.engine.CommonsUUID;
import org.openmrs.module.ohricore.engine.HIVStatusConceptUUID;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author MayanjaXL, Amos, Stephen, smallGod
 * date: 28/06/2021
 */
@Component("hivStatusComputedConcept")
public class HIVStatusComputedConcept implements OHRIComputedConcept {

    private Log log = LogFactory.getLog(this.getClass());

    @Override
    public org.openmrs.Obs compute(org.openmrs.Encounter triggeringEncounter) {

        org.openmrs.Concept hivFinalTestConcept = getConcept(HIVStatusConceptUUID.FINAL_HIV_TEST_RESULT);
        org.openmrs.Concept hivPositiveConcept = getConcept(CommonsUUID.POSITIVE);
        org.openmrs.Concept hivNegativeConcept = getConcept(CommonsUUID.NEGATIVE);

        org.openmrs.Patient patient = triggeringEncounter.getPatient();
        List<org.openmrs.Obs> hivTestObs = Context.getObsService().getObservationsByPersonAndConcept(patient.getPerson(),
                hivFinalTestConcept);

        boolean isNegative = false;

        for (org.openmrs.Obs obs : hivTestObs) {

            if (obs.getVoided()) {
                continue;
            }

            org.openmrs.Concept obsValueCoded = obs.getValueCoded();
            if (obsValueCoded == hivPositiveConcept) {
                return createOrUpdate(patient, hivPositiveConcept);

            } else if (obsValueCoded == hivNegativeConcept && valueDateIsWithin90Days(obs.getValueDate())) {
                isNegative = true;
            }
        }
        return isNegative ? createOrUpdate(patient, hivNegativeConcept) : createOrUpdate(patient,
                getConcept(CommonsUUID.UNKNOWN));
    }

    @Override
    public org.openmrs.EncounterType getTargetEncounterType() {
        return null;
    }

    @Override
    public org.openmrs.Concept getConcept() {
        return Context.getConceptService().getConceptByUuid(HIVStatusConceptUUID.HIV_STATUS);
    }

    boolean valueDateIsWithin90Days(Date valueDate) {

        Calendar cal = Calendar.getInstance();
        cal.setTime(valueDate);
        cal.add(Calendar.DATE, -90);

        return valueDate.compareTo(cal.getTime()) > 0; //valueDate occurs after (90days ago)
    }
}
