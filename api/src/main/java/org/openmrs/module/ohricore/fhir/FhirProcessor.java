package org.openmrs.module.ohricore.fhir;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Task;
import org.openmrs.Concept;
import org.openmrs.ConceptMap;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.openmrs.module.ohricore.OhriCoreConstant.CONCEPT_CODE_ANTIGEN;
import static org.openmrs.module.ohricore.OhriCoreConstant.CONCEPT_CODE_LAB_ORDER_STATUS;
import static org.openmrs.module.ohricore.OhriCoreConstant.CONCEPT_CODE_LAB_ORDER_STATUS_COMPLETED;
import static org.openmrs.module.ohricore.OhriCoreConstant.CONCEPT_CODE_LAB_ORDER_STATUS_NOT_DONE;
import static org.openmrs.module.ohricore.OhriCoreConstant.CONCEPT_CODE_LAB_ORDER_STATUS_NOT_DONE_REASON;
import static org.openmrs.module.ohricore.OhriCoreConstant.CONCEPT_CODE_LAB_TEST_PERFORMED;
import static org.openmrs.module.ohricore.OhriCoreConstant.CONCEPT_CODE_PCR;
import static org.openmrs.module.ohricore.OhriCoreConstant.CONCEPT_COVID_RESULT_ANTIGEN;
import static org.openmrs.module.ohricore.OhriCoreConstant.CONCEPT_COVID_RESULT_PCR;
import static org.openmrs.module.ohricore.OhriCoreConstant.CONCEPT_DATE_TEST_COMPLETED;
import static org.openmrs.module.ohricore.OhriCoreConstant.CONCEPT_FALSE;
import static org.openmrs.module.ohricore.OhriCoreConstant.CONCEPT_MAPPING_CIEL;
import static org.openmrs.module.ohricore.OhriCoreConstant.CONCEPT_MAPPING_LOINC;
import static org.openmrs.module.ohricore.OhriCoreConstant.CONCEPT_MAPPING_OCT;
import static org.openmrs.module.ohricore.OhriCoreConstant.CONCEPT_MAPPING_SNOMED_CT;
import static org.openmrs.module.ohricore.OhriCoreConstant.CONCEPT_TEST_SAMPLE_REJECTED;
import static org.openmrs.module.ohricore.OhriCoreConstant.CONCEPT_TRUE;
import static org.openmrs.module.ohricore.OhriCoreConstant.CONCEPT_VL_RESULT;
import static org.openmrs.module.ohricore.OhriCoreConstant.CONCEPT_VL_RESULT_DATE;
import static org.openmrs.module.ohricore.OhriCoreConstant.CONCEPT_YES;
import static org.openmrs.module.ohricore.OhriCoreConstant.FHIR_OBS_COVID_RESULT;
import static org.openmrs.module.ohricore.OhriCoreConstant.FHIR_OBS_COVID_RESULT_ANTIGEN;
import static org.openmrs.module.ohricore.OhriCoreConstant.FHIR_OBS_COVID_RESULT_PCR;
import static org.openmrs.module.ohricore.OhriCoreConstant.FHIR_OBS_VL_RESULT;
import static org.openmrs.module.ohricore.OhriCoreConstant.OHRI_ENCOUNTER_SYSTEM;
import static org.openmrs.module.ohricore.fhir.FhirClient.CTX;
import static org.openmrs.module.ohricore.fhir.FhirClient.fetchFhirDiagnosticReport;
import static org.openmrs.module.ohricore.fhir.FhirClient.fetchFhirObservation;
import static org.openmrs.module.ohricore.fhir.FhirClient.fetchFhirTasksThatAreCompleted;
import static org.openmrs.module.ohricore.fhir.FhirClient.fetchFhirTasksThatAreRejected;

/**
 * @author Arthur D.M, Amos Laboso date: 23/08/2022
 */
public class FhirProcessor {

    private static final Concept conceptTestPerformed = Context.getConceptService().getConceptByMapping(
            CONCEPT_CODE_LAB_TEST_PERFORMED, CONCEPT_MAPPING_OCT);

    private static final Concept conceptFalse = Context.getConceptService().getConceptByUuid(CONCEPT_FALSE);

    private static final Concept conceptTrue = Context.getConceptService().getConceptByUuid(CONCEPT_TRUE);

    Concept conceptLabOrderStatus = Context.getConceptService().getConceptByMapping(CONCEPT_CODE_LAB_ORDER_STATUS,
            CONCEPT_MAPPING_OCT);

    private static final Concept conceptLabOrderStatusCompleted = Context.getConceptService().getConceptByMapping(
            CONCEPT_CODE_LAB_ORDER_STATUS_COMPLETED, CONCEPT_MAPPING_SNOMED_CT);

    private static final Concept conceptLabOrderStatusNotDone = Context.getConceptService().getConceptByMapping(
            CONCEPT_CODE_LAB_ORDER_STATUS_NOT_DONE, CONCEPT_MAPPING_SNOMED_CT);

    private static final Concept conceptLabOrderStatusNotDoneReason = Context.getConceptService().getConceptByMapping(
            CONCEPT_CODE_LAB_ORDER_STATUS_NOT_DONE_REASON, CONCEPT_MAPPING_CIEL);

    private static final Comparator<Obs> OBS_ID_COMPARATOR = Comparator.comparing(Obs::getId);

    public void fetchCompletedLabResults() throws URISyntaxException {

        Map<String, List<Obs>> completedObsResults = getCompletedTaskObs();
        System.out.println("Number of Encounters from DISI: " + completedObsResults.size() + " (same as tasks)");
        saveObs(completedObsResults);
    }

    public void fetchRejectedLoadRequests() throws URISyntaxException {

        Map<String, List<Obs>> rejectedResults = getRejectedRequestObs();
        System.out.println("Number of Encounters from DISI: " + rejectedResults.size() + " (same as tasks)");
        saveObs(rejectedResults);
    }

    private void saveObs(Map<String, List<Obs>> observationsMap) {
        for (Map.Entry<String, List<Obs>> obsToSave : observationsMap.entrySet()) {
            saveObsHelper(obsToSave.getKey(), obsToSave.getValue());
        }
    }

    private Map<String, List<Obs>> getCompletedTaskObs() throws URISyntaxException {

        Map<String, List<Obs>> patientEncounterObs = new ConcurrentHashMap<>();
        Bundle taskBundle = fetchFhirTasksThatAreCompleted();

        List<Bundle.BundleEntryComponent> bundleEntryComponents = taskBundle.getEntry();
        System.out.println("Number of completed Tasks from DISI: " + bundleEntryComponents.size());

        for (Bundle.BundleEntryComponent bundleEntry : bundleEntryComponents) {

            Task task = (Task) bundleEntry.getResource();
            System.out.println("Task: " + CTX.newJsonParser().encodeResourceToString(task));

            //Get the Encounter & Patient Details
            Encounter encounter = getOhriEncounterFromDisi(task);

            if (encounter == null) {
                System.out.println("Failed to find an Encounter from a DISI Task Result. " +
                        "Make sure the 'OHRI_ENCOUNTER_UUID' tag is part of this Task (when submitting Lab Results)");
            } else {

                org.openmrs.Patient patient = encounter.getPatient();

                //Get the Diagnostic Report ID for this Task
                String diagnosticReportId = null;
                for (Task.TaskOutputComponent output : task.getOutput()) {

                    Reference referenceToDiagnosticResource = (Reference) output.getValue();
                    String urlRef = referenceToDiagnosticResource.getReference();
                    diagnosticReportId = urlRef.split("/")[1];
                }

                if (diagnosticReportId != null) {

                    //Fetch a Diagnostic Report containing Observation references
                    DiagnosticReport diagnosticReport = fetchFhirDiagnosticReport(diagnosticReportId);
                    System.out.println("DiagnosticReport: " + CTX.newJsonParser().encodeResourceToString(diagnosticReport));

                    List<String> obsIdList = new ArrayList<>();
                    for (Reference obsReference : diagnosticReport.getResult()) {
                        String urlRef = obsReference.getReference();
                        obsIdList.add(urlRef.split("/")[1]);
                    }

                    for (String obsId : obsIdList) {

                        Observation obs = fetchFhirObservation(obsId);
                        List<Obs> obsFound = findObs(patient, encounter, obs);

                        patientEncounterObs.merge(
                                encounter.getUuid(),
                                obsFound,
                                (prevList, newList) -> {
                                    prevList.addAll(newList);
                                    return prevList;
                                }
                        );
                    }
                }
            }
        }
        return patientEncounterObs;
    }

    private Map<String, List<Obs>> getRejectedRequestObs() throws URISyntaxException {

        Map<String, List<Obs>> patientEncounterObs = new ConcurrentHashMap<>();
        Bundle taskBundle = fetchFhirTasksThatAreRejected();

        List<Bundle.BundleEntryComponent> bundleEntryComponents = taskBundle.getEntry();
        System.out.println("Number of completed Tasks from DISI: " + bundleEntryComponents.size());

        for (Bundle.BundleEntryComponent bundleEntry : bundleEntryComponents) {

            Task task = (Task) bundleEntry.getResource();
            System.out.println("Task: " + CTX.newJsonParser().encodeResourceToString(task));

            //Get the Encounter & Patient Details
            Encounter encounter = getOhriEncounterFromDisi(task);

            List<Obs> observations = new ArrayList<>();
            if (encounter == null) {
                System.out.println("Failed to find an Encounter from a DISI Task Result. " +
                        "Make sure the 'OHRI_ENCOUNTER_UUID' tag is part of this Task (when submitting Lab Results)");
            } else {

                List<Coding> coding = task.getStatusReason().getCoding();
                String display = coding.get(0).getDisplay();
                String code = coding.get(0).getCode();
                Date lastModified = task.getLastModifiedElement().getValue();

                Concept rejectionReasonConcept = Context.getConceptService().getConceptByMapping(code, CONCEPT_MAPPING_CIEL);

                org.openmrs.Patient patient = encounter.getPatient();

                //Was Test Performed - False
                Obs testPerformedObs = createObs(patient, conceptTestPerformed, encounter);
                testPerformedObs.setObsDatetime(lastModified);
                testPerformedObs.setValueCoded(conceptFalse);

                //Lab Order Status - Not Done
                Obs labOrderStatusNotDoneObs = createObs(patient, conceptLabOrderStatus, encounter);
                labOrderStatusNotDoneObs.setObsDatetime(lastModified);
                labOrderStatusNotDoneObs.setValueCoded(conceptLabOrderStatusNotDone);

                //Lab Order Status Not Done - Reason
                Obs labOrderStatusNotDoneReasonObs = createObs(patient, conceptLabOrderStatusNotDoneReason, encounter);
                labOrderStatusNotDoneReasonObs.setObsDatetime(lastModified);
                labOrderStatusNotDoneReasonObs.setValueCoded(rejectionReasonConcept);

                Concept sampleRejected = Context.getConceptService().getConceptByUuid(CONCEPT_TEST_SAMPLE_REJECTED);
                Obs obs = createObs(patient, sampleRejected, encounter);
                obs.setObsDatetime(lastModified);
                obs.setValueCoded(Context.getConceptService().getConceptByUuid(CONCEPT_YES));
                obs.setComment(code + " | " + display);

                observations.add(testPerformedObs);
                observations.add(labOrderStatusNotDoneObs);
                observations.add(labOrderStatusNotDoneReasonObs);
                observations.add(obs);

                patientEncounterObs.merge(
                        encounter.getUuid(),
                        observations,
                        (prevList, newList) -> {
                            prevList.addAll(newList);
                            return prevList;
                        }
                );
            }
        }
        return patientEncounterObs;
    }

    private static Encounter getOhriEncounterFromDisi(Task task) {

        Encounter encounter = null;
        for (Identifier identifier : task.getIdentifier()) {

            if (identifier.getSystem().equals(OHRI_ENCOUNTER_SYSTEM)) {
                String encounterUuid = identifier.getValue();
                try {
                    encounter = Context.getEncounterService().getEncounterByUuid(encounterUuid);
                } catch (NullPointerException npe) {
                    System.err.println("Failed to initialise Encounter/Patient object(s)");
                }
            }
        }
        return encounter;
    }

    private List<Obs> findObs(org.openmrs.Patient patient, Encounter encounter, Observation fhirObs) {

        String code = fhirObs.getCode().getCoding().get(0).getCode();
        Date resultDate = fhirObs.getEffectiveDateTimeType().getValue();

        switch (code) {

            case FHIR_OBS_VL_RESULT:
                return readVlObsHelper(patient, encounter, fhirObs, resultDate);

            case FHIR_OBS_COVID_RESULT:
            case FHIR_OBS_COVID_RESULT_PCR:
            case FHIR_OBS_COVID_RESULT_ANTIGEN:
                return readCovidObsHelper(patient, encounter, fhirObs, resultDate);

            // case FHIR_OBS_HIV_RESULT:
            // return readCovidObsHelper(patient, encounter, resultValue, resultDate);
        }
        return Collections.emptyList();
    }

    private List<Obs> readVlObsHelper(org.openmrs.Patient patient, Encounter encounter, Observation fhirObs, Date resultDate) {

        List<Obs> obs = new ArrayList<>();
        String resultValue = fhirObs.getValueStringType().getValue();

        Concept resultConcept = Context.getConceptService().getConceptByUuid(CONCEPT_VL_RESULT);
        Obs resultObs = createObs(patient, resultConcept, encounter);
        resultObs.setObsDatetime(resultDate);
        resultObs.setValueNumeric(Double.valueOf(resultValue));

        Obs dateObs = readDateObsHelper(patient, encounter, CONCEPT_VL_RESULT_DATE, resultDate);

        obs.add(resultObs);
        obs.add(dateObs);
        return obs;
    }

    private List<Obs> readCovidObsHelper(org.openmrs.Patient patient, Encounter encounter, Observation fhirObs, Date resultDate) {

        List<Obs> obsList = new ArrayList<>();

        List<Coding> resultValues = fhirObs.getValueCodeableConcept().getCoding();
        String testResultValueCode = resultValues.get(0).getCode();
        Concept valueConcept = Context.getConceptService().getConceptByMapping(testResultValueCode, CONCEPT_MAPPING_SNOMED_CT);

        //Was Test Performed
        Obs testPerformedObs = createObs(patient, conceptTestPerformed, encounter);
        testPerformedObs.setObsDatetime(resultDate);
        testPerformedObs.setValueCoded(conceptTrue);

        //Lab Order Status
        Obs labOrderStatusCompletedObs = createObs(patient, conceptLabOrderStatus, encounter);
        labOrderStatusCompletedObs.setObsDatetime(resultDate);
        labOrderStatusCompletedObs.setValueCoded(conceptLabOrderStatusCompleted);

        //Lab Result Value
        Concept resultConcept = getCovidConceptForTestDone(fhirObs);
        Obs resultObs = createObs(patient, resultConcept, encounter);
        resultObs.setObsDatetime(resultDate);
        resultObs.setValueCoded(valueConcept);

        //The actual Covid Lab Test Type Result that was Done - populate its value
        Concept finalCovidLabResultTypeConcept = getFinalCovidLabResultObsToSave(resultConcept);
        Obs finalCovidLabResultTypeObs = createObs(patient, finalCovidLabResultTypeConcept, encounter);
        finalCovidLabResultTypeObs.setObsDatetime(resultDate);
        finalCovidLabResultTypeObs.setValueCoded(valueConcept);

        //Lab Test Result Date
        Obs dateObs = readDateObsHelper(patient, encounter, CONCEPT_DATE_TEST_COMPLETED, resultDate);

        obsList.add(testPerformedObs);
        obsList.add(labOrderStatusCompletedObs);
        obsList.add(finalCovidLabResultTypeObs);
        obsList.add(resultObs);
        obsList.add(dateObs);
        return obsList;
    }

    private Obs readDateObsHelper(org.openmrs.Patient patient, Encounter encounter, String dateUUID, Date resultDate) {

        Concept dateConcept = Context.getConceptService().getConceptByUuid(dateUUID);
        Obs dateObs = createObs(patient, dateConcept, encounter);
        dateObs.setObsDatetime(resultDate);
        dateObs.setValueDate(resultDate);
        return dateObs;
    }

    private Concept getCovidConceptForTestDone(Observation fhirObs) {

        Supplier<Stream<Coding>> coding = fhirObs.getCode().getCoding()::stream;
        String covidTestConceptCode = coding.get()
                .filter(code -> code.getCode().equals(CONCEPT_CODE_ANTIGEN))
                .findAny()
                .map(Coding::getCode)
                .orElse(coding.get()
                        .filter(code -> code.getCode().equals(CONCEPT_CODE_PCR))
                        .findAny()
                        .map(Coding::getCode)
                        .orElse(null));

        return Context.getConceptService()
                .getConceptByMapping(covidTestConceptCode, CONCEPT_MAPPING_LOINC);
    }

    private Concept getFinalCovidLabResultObsToSave(Concept covidLabTestTypeCode) {

        Supplier<Stream<ConceptMap>> conceptMappings = covidLabTestTypeCode.getConceptMappings()::stream;
        return conceptMappings.get()
                .filter(conceptMap -> conceptMap.getConceptReferenceTerm().getCode().equals(CONCEPT_CODE_ANTIGEN))
                .findAny()
                .map((concept) -> Context.getConceptService().getConceptByUuid(CONCEPT_COVID_RESULT_ANTIGEN))
                .orElse(conceptMappings.get()
                        .filter(conceptMap -> conceptMap.getConceptReferenceTerm().getCode().equals(CONCEPT_CODE_PCR))
                        .findAny()
                        .map((concept) -> Context.getConceptService().getConceptByUuid(CONCEPT_COVID_RESULT_PCR))
                        .orElse(null));
    }

    private void saveObsHelper(String encounterUuid, List<Obs> obsToSaveList) {

        Encounter encounter = Context.getEncounterService()
                .getEncounterByUuid(encounterUuid);

        System.out.println("==================== Save Obs - START ====================");
        System.out.println("Person ID     : " + encounter.getPatient().getPatientId());
        System.out.println("Person Name   : " + encounter.getPatient().getGivenName() + ", " + encounter.getPatient().getFamilyName());
        System.out.println("Encounter UUID: " + encounterUuid);
        System.out.println("Obs Count     : " + obsToSaveList.size());
        System.out.println("--------------");

        Supplier<Stream<Obs>> savedObsStream = Context.getEncounterService()
                .getEncounterByUuid(encounterUuid).getObs()::stream;

        for (Obs obsToSave : obsToSaveList) {

            System.out.println("Concept Name  : " + obsToSave.getConcept().getName());
            System.out.println("Concept UUID  : " + obsToSave.getConcept().getUuid());
            System.out.println("Concept ID    : " + obsToSave.getConcept().getId());

            if (isObsExists(obsToSave, savedObsStream)) {

                Obs savedObs = getSavedObsToAmend(obsToSave, savedObsStream);
                if (savedObs == null) {
                    System.out.println("Alert! Similar Obs with value Exists, NOT saving...");
                    continue;
                }
                //Obs newObs = Obs.newInstance(oldObs); //copies values from oldObs
                obsToSave.setPreviousVersion(savedObs);
                Context.getObsService().saveObs(obsToSave, "Lab Test Results (DISI)");
                Context.getObsService().voidObs(savedObs, "Updated with Lab Test Results (DISI)");

            } else {
                Context.getObsService().saveObs(obsToSave, "Lab Test Results Test(DISI)");
            }
            System.out.println("--------------");
        }
        System.out.println("===================== Save Obs - END ======================");
        System.out.println();
    }

    private boolean isObsExists(final Obs obsToSave, Supplier<Stream<Obs>> savedObsStream) {

        return savedObsStream
                .get()
                .anyMatch(savedObs -> savedObs.getConcept().equals(obsToSave.getConcept()));
    }

    private Obs getSavedObsToAmend(final Obs obsToSave, Supplier<Stream<Obs>> savedObsStream) {

        return savedObsStream.get()
                .filter(savedObs -> !savedObs.getVoided())
                .filter(savedObs -> savedObs.getConcept().equals(obsToSave.getConcept()))
                .filter(savedObs -> !savedObs.getValueAsString(Locale.ENGLISH).equals(obsToSave.getValueAsString(Locale.ENGLISH)))
                .max(OBS_ID_COMPARATOR)
                .orElse(null);
    }

    private Obs createObs(org.openmrs.Patient patient, Concept concept, Encounter encounter) {

        org.openmrs.Location location = Context.getLocationService().getDefaultLocation();

        Obs obs = new Obs();
        obs.setDateCreated(new Date());
        obs.setObsDatetime(new Date());
        obs.setPerson(patient);
        obs.setConcept(concept);
        obs.setLocation(location);
        obs.setStatus(Obs.Status.FINAL);
        obs.setEncounter(encounter);
        return obs;
    }

    private void processFhirObs(Bundle obsBundle) {

        List<Bundle.BundleEntryComponent> bundleEntryComponents = obsBundle.getEntry();
        System.out.println("Running processFhirObs(): size - " + bundleEntryComponents.size());

        for (Bundle.BundleEntryComponent bundleEntry : bundleEntryComponents) {

            Observation obs = (Observation) bundleEntry.getResource();
            System.out.println("Obs: " + CTX.newJsonParser().encodeResourceToString(obs));

            String vlResult = obs.getValueStringType().getValue();
            Date dateOfVlResult = obs.getEffectiveDateTimeType().getValue();
        }
    }

    private static final class PatientEncounter {

        private final Patient patient;

        private final Encounter encounter;

        public PatientEncounter(Patient patient, Encounter encounter) {
            this.patient = patient;
            this.encounter = encounter;
        }

        public Patient getPatient() {
            return patient;
        }

        public Encounter getEncounter() {
            return encounter;
        }

        @Override
        public String toString() {
            return "Patient Encounter { patient Id: " + patient.getPatientId() + ", encounter UUID: '" + encounter.getUuid()
                    + '\'' + '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof PatientEncounter))
                return false;
            PatientEncounter that = (PatientEncounter) o;
            return getPatient().getPatientId().equals(that.getPatient().getPatientId())
                    && getEncounter().getUuid().equals(that.getEncounter().getUuid());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getPatient().getPatientId(), getEncounter().getUuid());
        }
    }
}
