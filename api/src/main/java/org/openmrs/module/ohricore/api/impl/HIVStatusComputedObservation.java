package org.openmrs.module.ohricore.api.impl;

import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.ohricore.api.OHRIComputedObservation;
import org.openmrs.module.ohricore.engine.CommonsUUID;
import org.openmrs.module.ohricore.engine.HIVStatusConceptUUID;
import org.springframework.stereotype.Component;

import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.openmrs.module.ohricore.engine.ComputedObservationUtil.dateWithinPeriodFromNow;

/**
 * @author MayanjaXL, Amos, Stephen, smallGod date: 28/06/2021
 */
@Component("hivStatusComputedConcept")
public class HIVStatusComputedObservation implements OHRIComputedObservation {

    @Override
    public Obs compute(Encounter triggeringEncounter) {

        Patient patient = triggeringEncounter.getPatient();

        Concept hivFinalTestConcept = getConcept(HIVStatusConceptUUID.FINAL_HIV_TEST_RESULT);
        List<Obs> hivTestObs = Context.getObsService().getObservationsByPersonAndConcept(patient.getPerson(),
                hivFinalTestConcept);

        Concept newComputedConcept = computeHivStatusConcept(hivTestObs);

        Obs newComputedObs = initialiseAnObs(patient, newComputedConcept);
        Obs savedComputedObs = getSavedComputedObs(patient);

        return compareSavedComputedObs(savedComputedObs, newComputedObs);
    }

    @Override
    public Obs compareSavedComputedObs(Obs savedComputedObs, Obs newComputedObs) {

        if (savedComputedObs == null) {
            return newComputedObs;
        }
        if (savedComputedObs.getValueCoded() == newComputedObs.getValueCoded()
                || savedComputedObs.getValueCoded().equals(getConcept(CommonsUUID.POSITIVE))) {
            return null;
        }
        if (newComputedObs.getValueCoded().equals(getConcept(CommonsUUID.POSITIVE))
                || savedComputedObs.getValueCoded().equals(getConcept(CommonsUUID.INDETERMINATE))) {
            savedComputedObs.setValueCoded(newComputedObs.getValueCoded());
            return savedComputedObs;
        }
        return newComputedObs;
    }

    private Concept computeHivStatusConcept(List<Obs> hivTestObs) {

        Supplier<Stream<Obs>> hivTestObsStream = hivTestObs::stream;
        return hivTestObsStream.get()
                .filter(obs -> obs.getValueCoded() == getConcept(CommonsUUID.POSITIVE))
                .findAny()
                .map(Obs::getValueCoded)
                .orElse(hivTestObsStream.get()
                        .filter(obs -> obs.getValueCoded() == getConcept(CommonsUUID.NEGATIVE))
                        .filter(obs -> {
                            Date testResultDate = getLatestTestResultDate(obs.getPerson(), obs, HIVStatusConceptUUID.FINAL_HIV_TEST_RESULT_DATE);
                            return dateWithinPeriodFromNow(testResultDate, ChronoUnit.DAYS, -90);
                        })
                        .findAny()
                        .map(Obs::getValueCoded)
                        .orElse(getConcept(CommonsUUID.INDETERMINATE))
                );
    }

    public Optional<Obs> getPositiveComputedHivStatus(Patient patient) {

        List<Obs> computedHivObs = Context.getObsService()
                .getObservationsByPersonAndConcept(patient.getPerson(), getConcept());

        Supplier<Stream<Obs>> hivTestObsStream = computedHivObs::stream;

        return hivTestObsStream.get()
                .filter(obs -> obs.getValueCoded() == getConcept(CommonsUUID.POSITIVE))
                .findFirst();
    }

    @Override
    public Concept getConcept() {
        return Context.getConceptService().getConceptByUuid(HIVStatusConceptUUID.HIV_STATUS);
    }
}
