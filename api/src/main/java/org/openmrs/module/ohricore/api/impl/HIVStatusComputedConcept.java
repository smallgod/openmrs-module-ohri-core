package org.openmrs.module.ohricore.api.impl;

import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.ohricore.api.OHRIComputedConcept;
import org.openmrs.module.ohricore.engine.CommonsUUID;
import org.openmrs.module.ohricore.engine.HIVStatusConceptUUID;
import org.springframework.stereotype.Component;

import static org.openmrs.module.ohricore.engine.ComputedConceptUtil.dateWithinPeriodFromNow;

import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * @author MayanjaXL, Amos, Stephen, smallGod date: 28/06/2021
 */
@Component("hivStatusComputedConcept")
public class HIVStatusComputedConcept implements OHRIComputedConcept {

    @Override
    public Obs compute(Encounter triggeringEncounter) {

        Patient patient = triggeringEncounter.getPatient();

        Optional<Obs> priorComputedHivPositiveStatusObs = getPositiveComputedHivStatus(patient);
        if (priorComputedHivPositiveStatusObs.isPresent()) {
            return priorComputedHivPositiveStatusObs.get();
        }

        Concept hivFinalTestConcept = getConcept(HIVStatusConceptUUID.FINAL_HIV_TEST_RESULT);
        List<Obs> hivTestObs = Context.getObsService().getObservationsByPersonAndConcept(patient.getPerson(),
                hivFinalTestConcept);

        Concept newComputedConcept = computeHivStatusConcept(hivTestObs, getHIVFinalTestResultDate(patient));

        Obs savedComputedObs = getSavedComputedObs(patient);
        Obs newComputedObs = initialiseAnObs(patient, newComputedConcept);

        return compareObs(savedComputedObs, newComputedObs);
    }


    @Override
    public Obs compareObs(Obs savedComputedObs, Obs newComputedObs) {

        if (savedComputedObs.getValueCoded() == newComputedObs.getValueCoded()) {
            return null;
        }

        if (newComputedObs.getValueCoded().equals(getConcept(CommonsUUID.POSITIVE))
                || savedComputedObs.getValueCoded().equals(getConcept(CommonsUUID.UNKNOWN))) {
            savedComputedObs.setValueCoded(newComputedObs.getValueCoded());
            return savedComputedObs;
        }

        return newComputedObs;
    }

    private Concept computeHivStatusConcept(List<Obs> hivTestObs, Date finalHivTestResultDate) {

        Supplier<Stream<Obs>> hivTestObsStream = hivTestObs::stream;
        return hivTestObsStream.get()
                .filter(obs -> obs.getValueCoded() == getConcept(CommonsUUID.POSITIVE))
                .findAny()
                .map(Obs::getValueCoded)
                .orElse(hivTestObsStream.get()
                        .filter(obs -> obs.getValueCoded() == getConcept(CommonsUUID.NEGATIVE))
                        .filter(obs -> dateWithinPeriodFromNow(finalHivTestResultDate, ChronoUnit.DAYS, -90))
                        .findAny()
                        .map(Obs::getValueCoded)
                        .orElse(getConcept(CommonsUUID.UNKNOWN))
                );
    }

    public Date getHIVFinalTestResultDate(Patient patient) {

        Concept hivFinalTestDateConcept = getConcept(HIVStatusConceptUUID.HIV_TEST_RESULT_DATE);

        List<Obs> hivTestObs = Context.getObsService()
                .getObservationsByPersonAndConcept(patient.getPerson(), hivFinalTestDateConcept);

        Supplier<Stream<Obs>> hivTestObsStream = hivTestObs::stream;
        return hivTestObsStream.get()
                .findAny()//TODO: Might need to filter out the exact concept for this test date
                .map(Obs::getValueDate)
                .orElse(null);
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
