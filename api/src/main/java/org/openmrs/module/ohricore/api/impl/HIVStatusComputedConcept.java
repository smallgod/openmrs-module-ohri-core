package org.openmrs.module.ohricore.api.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Obs;
import org.openmrs.Patient;
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
    public Obs compute(Encounter triggeringEncounter) {

        Concept hivFinalTestConcept = getConcept(HIVStatusConceptUUID.FINAL_HIV_TEST_RESULT);
        Concept hivPositiveConcept = getConcept(CommonsUUID.POSITIVE);
        Concept hivNegativeConcept = getConcept(CommonsUUID.NEGATIVE);

        Patient patient = triggeringEncounter.getPatient();
        List<Obs> hivTestObs = Context.getObsService().getObservationsByPersonAndConcept(patient.getPerson(),
                hivFinalTestConcept);

        boolean isNegative = false;

        for (Obs obs : hivTestObs) {

            if (obs.getVoided()) {
                continue;
            }

            Concept obsValueCoded = obs.getValueCoded();
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
