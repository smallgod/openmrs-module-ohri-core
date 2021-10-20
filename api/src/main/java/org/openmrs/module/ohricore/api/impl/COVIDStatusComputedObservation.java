package org.openmrs.module.ohricore.api.impl;

import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.PatientDAO;
import org.openmrs.module.ohricore.api.OHRIComputedObservation;
import org.openmrs.module.ohricore.engine.COVIDStatusConceptUUID;
import org.openmrs.module.ohricore.engine.CommonsUUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.openmrs.module.ohricore.engine.COVIDStatusConceptUUID.ACTIVE_COVID;
import static org.openmrs.module.ohricore.engine.COVIDStatusConceptUUID.COVID_OUTCOME;
import static org.openmrs.module.ohricore.engine.COVIDStatusConceptUUID.EVER_HAD_COVID;
import static org.openmrs.module.ohricore.engine.COVIDStatusConceptUUID.FINAL_COVID_TEST_RESULT;
import static org.openmrs.module.ohricore.engine.COVIDStatusConceptUUID.FINAL_COVID_TEST_RESULT_DATE;
import static org.openmrs.module.ohricore.engine.COVIDStatusConceptUUID.LONG_COVID;
import static org.openmrs.module.ohricore.engine.CommonsUUID.POSITIVE;
import static org.openmrs.module.ohricore.engine.CommonsUUID.SYMPTOMS_RESOLVED;
import static org.openmrs.module.ohricore.engine.ComputedObservationUtil.dateWithinPeriodFromNow;

/**
 * @author MayanjaXL, Amos, Stephen, smallGod date: 18/10/2021
 */
@Component("covidStatusComputedConcept")
public class COVIDStatusComputedObservation implements OHRIComputedObservation {

    @Autowired
    private PatientDAO dao;

    @Autowired
    private PatientService patientService;

    @Override
    public Obs compute(Encounter triggeringEncounter) {
        return compute(triggeringEncounter.getPatient());
    }

    @Override
    public Obs compute(Patient patient) {

        Concept finalCovidTestConcept = getConcept(FINAL_COVID_TEST_RESULT);
        Concept covidOutcomeConcept = getConcept(COVID_OUTCOME);

        List<Obs> finalCovidObs = Context.getObsService().getObservationsByPersonAndConcept(patient.getPerson(), finalCovidTestConcept);
        List<Obs> covidOutcomeObs = Context.getObsService().getObservationsByPersonAndConcept(patient.getPerson(), covidOutcomeConcept);
        finalCovidObs.addAll(covidOutcomeObs);

        Obs newComputedObs = computeHelper(finalCovidObs);
        Obs savedComputedObs = getSavedComputedObs(patient);

        return compareSavedComputedObs(savedComputedObs, newComputedObs);
    }

    private Obs computeHelper(List<Obs> finalCovidTestObs) {

        List<Concept> covidOutcomes = new ArrayList<>(Arrays.asList(getConcept(LONG_COVID), getConcept(SYMPTOMS_RESOLVED)));

        Supplier<Stream<Obs>> finalCovidTestObsStream = finalCovidTestObs::stream;

        return finalCovidTestObsStream.get()
                .filter(obs -> !covidOutcomes.contains(obs.getValueCoded()))
                .map(obs -> {
                    Obs latestTestDateObs = getLatestTestResultDateObs(obs.getPerson(), obs, FINAL_COVID_TEST_RESULT_DATE);
                    if (dateWithinPeriodFromNow(latestTestDateObs.getValueDate(), ChronoUnit.DAYS, -15)) {
                        return obs;
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .filter(obs -> obs.getValueCoded() == getConcept(POSITIVE))
                .findAny()
                .map(obs -> initialiseAnObs(obs.getPerson(), getConcept(ACTIVE_COVID)))
                .orElse(finalCovidTestObsStream.get()
                        .filter(obs -> obs.getValueCoded() == getConcept(CommonsUUID.POSITIVE))
                        .findAny()
                        .map(obs -> initialiseAnObs(obs.getPerson(), getConcept(EVER_HAD_COVID)))
                        .orElse(null)
                );
    }

    @Override
    public List<Patient> getPatientCohort() {
        //TODO: Exploring available methods in patientService and patientDAO/HibernatePatientDAO first
        return null;
    }

    @Override
    public Obs compareSavedComputedObs(Obs savedComputedObs, Obs newComputedObs) {

        if (savedComputedObs.getValueCoded() == newComputedObs.getValueCoded()) {
            return null;
        }
        return newComputedObs;
    }

    @Override
    public Concept getConcept() {
        return Context.getConceptService().getConceptByUuid(COVIDStatusConceptUUID.COVID_STATUS);
    }
}
