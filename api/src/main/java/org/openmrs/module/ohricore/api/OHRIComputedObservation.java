package org.openmrs.module.ohricore.api;

import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.api.context.Context;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * @author MayanjaXL, Amos, Stephen, smallGod
 * date: 28/06/2021
 */
public interface OHRIComputedObservation {

    Obs compute(Encounter triggeringEncounter);

    Concept getConcept();

    default Concept getConcept(String UUID) {
        return Context.getConceptService().getConceptByUuid(UUID);
    }

    default void persist(Obs obs) {
        Context.getObsService().saveObs(obs, "updated by Encounter interceptor");
    }

    default void computeAndPersistObs(Encounter triggeringEncounter) {
        //TODO: throw an OHRI custom exception
        Obs obs = compute(triggeringEncounter);
        if (obs != null) {
            persist(obs);
        }
    }

    default boolean keepHistory() {
        return false;
    }

    default boolean isTimeBased() {
        return false;
    }

    default Obs initialiseAnObs(Person patient, Concept targetConcept) {

        Obs computedObs = new Obs();
        computedObs.getDateCreated();
        computedObs.setObsDatetime(new Date());
        computedObs.setPerson(patient);
        computedObs.setConcept(getConcept());
        computedObs.setValueCoded(targetConcept);
        Location location = Context.getLocationService().getDefaultLocation();
        computedObs.setLocation(location);

        return computedObs;
    }

    default Date getObsTestResultDate(Person person, Obs obsTestResult, String obsTestResultDateUUID) {

        Concept obsTestResultDateConcept = getConcept(obsTestResultDateUUID);

        List<Obs> recordedTestResultDates = Context.getObsService()
                .getObservationsByPersonAndConcept(person, obsTestResultDateConcept);

        Supplier<Stream<Obs>> datesStream = recordedTestResultDates::stream;
        return datesStream.get()
                .filter(obs -> obs.getEncounter() == obsTestResult.getEncounter())
                .findAny()
                .map(Obs::getValueDate)
                .orElse(null);
    }

    default Obs getSavedComputedObs(Patient patient) {

        List<Obs> computedObsList = getObs(patient, getConcept());

        if (computedObsList == null || computedObsList.isEmpty()) {
            return null;
        }
        return Collections.max(computedObsList, Comparator.comparing(Obs::getDateCreated));
    }

    default List<Obs> getObs(Patient patient, Concept obsConcept) {

        return Context.getObsService()
                .getObservationsByPersonAndConcept(patient.getPerson(), obsConcept);
    }

    Obs compareSavedComputedObs(Obs savedComputedObs, Obs newComputedObs);

    default boolean shouldRunForEncounter(EncounterType encounterType) {
        return true;
    }

    default boolean shouldRunForPatient(Patient patient) {
        return true;
    }

    // Computes the required computed observation for the patient
    Obs compute(Patient patient);

    // Calls getPatientCohort and computes for each patient in the patientCohort
    default void compute() {
        List<Patient> patientCohort = getPatientCohort();
        patientCohort.forEach(this::compute);
    }

    // Retrieves the cohort of patients for which this computed concept needs to be computed.
    // The type of computed concept determines what patient is eligble. For example, if the
    // computed concept is HIV Status, you would only return a list of patients who have at
    // least one observation relevant to the computation of  HIV Status.
    //
    // In addition, this method should also return patients who have a status set, to cater for
    // the scenario where relevant observations have been deleted/voided.
    List<Patient> getPatientCohort();
}
