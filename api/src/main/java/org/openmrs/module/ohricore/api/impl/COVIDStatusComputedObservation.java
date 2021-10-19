package org.openmrs.module.ohricore.api.impl;

import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.ohricore.api.OHRIComputedObservation;
import org.openmrs.module.ohricore.engine.COVIDStatusConceptUUID;
import org.openmrs.module.ohricore.engine.CommonsUUID;
import org.springframework.stereotype.Component;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.openmrs.module.ohricore.engine.ComputedObservationUtil.dateWithinPeriodFromNow;

/**
 * @author MayanjaXL, Amos, Stephen, smallGod date: 18/10/2021
 */
@Component("covidStatusComputedConcept")
public class COVIDStatusComputedObservation implements OHRIComputedObservation {

    @Override
    public Obs compute(Encounter triggeringEncounter) {
        return compute(triggeringEncounter.getPatient());
    }

    @Override
    public Obs compute(Patient patient) {

        Concept finalCovidTestConcept = getConcept(COVIDStatusConceptUUID.FINAL_COVID_TEST_RESULT);
        List<Obs> finalCovidObs = Context.getObsService().getObservationsByPersonAndConcept(patient.getPerson(), finalCovidTestConcept);

        Concept newComputedConcept = computeCovidStatusConcept(finalCovidObs);

        Obs newComputedObs = initialiseAnObs(patient, newComputedConcept);
        Obs savedComputedObs = getSavedComputedObs(patient);

        return compareSavedComputedObs(savedComputedObs, newComputedObs);
    }

    private Obs computeCovidStatusConcept(List<Obs> finalCovidTestObs) {

        List<Concept> covidOutcomes = new ArrayList<>(Arrays.asList(getConcept(CommonsUUID.DIED), getConcept(CommonsUUID.SYMPTOMS_RESOLVED)));

        Supplier<Stream<Obs>> finalCovidTestObsStream = finalCovidTestObs::stream;
        return finalCovidTestObsStream.get()
                .filter(obs -> obs.getValueCoded() == getConcept(CommonsUUID.POSITIVE))
                .filter(obs -> {
                    Date testResultDate  = getObsTestResultDate(obs.getPerson(), obs, COVIDStatusConceptUUID.FINAL_COVID_TEST_RESULT_DATE);
                    return dateWithinPeriodFromNow(testResultDate, ChronoUnit.DAYS, -15);
                })
                .filter(obs -> !covidOutcomes.contains(obs.getValueCoded()))
                .findAny()
                .map(obs -> initialiseAnObs(obs.getPerson(), getConcept(COVIDStatusConceptUUID.ACTIVE_COVID)))
                .orElse(finalCovidTestObsStream.get()
                        .filter(obs -> obs.getValueCoded() == getConcept(CommonsUUID.NEGATIVE))
                        .filter(obs -> dateWithinPeriodFromNow(finalCOVIDTestDate, ChronoUnit.DAYS, -90))
                        .findAny()
                        .map(Obs::getValueCoded)
                        .orElse(getConcept(CommonsUUID.INDETERMINATE))
                );
    }

    @Override
    public Obs compareSavedComputedObs(Obs savedComputedObs, Obs newComputedObs) {

        if (savedComputedObs.getValueCoded() == newComputedObs.getValueCoded()) {
            return null;
        }
        return newComputedObs;
    }

    @Override
    public List<Patient> getPatientCohort() {
        return null;
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
        return Context.getConceptService().getConceptByUuid(COVIDStatusConceptUUID.COVID_STATUS);
    }
}
