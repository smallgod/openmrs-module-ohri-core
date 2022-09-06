package org.openmrs.module.ohricore.fhir;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Task;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.api.context.Context;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.openmrs.module.ohricore.OhriCoreConstant.CONCEPT_COVID_RESULT_ANTIGEN;
import static org.openmrs.module.ohricore.OhriCoreConstant.CONCEPT_COVID_RESULT_DATE;
import static org.openmrs.module.ohricore.OhriCoreConstant.CONCEPT_VL_RESULT;
import static org.openmrs.module.ohricore.OhriCoreConstant.CONCEPT_VL_RESULT_DATE;
import static org.openmrs.module.ohricore.OhriCoreConstant.CONCEPT_VL_SAMPLE_REJECTED;
import static org.openmrs.module.ohricore.OhriCoreConstant.CONCEPT_YES;
import static org.openmrs.module.ohricore.OhriCoreConstant.FHIR_OBS_COVID_RESULT;
import static org.openmrs.module.ohricore.OhriCoreConstant.FHIR_OBS_VL_RESULT;
import static org.openmrs.module.ohricore.OhriCoreConstant.INCONCLUSIVE;
import static org.openmrs.module.ohricore.OhriCoreConstant.NEGATIVE;
import static org.openmrs.module.ohricore.OhriCoreConstant.OHRI_ENCOUNTER_SYSTEM;
import static org.openmrs.module.ohricore.OhriCoreConstant.POSITIVE;
import static org.openmrs.module.ohricore.fhir.FhirClient.CTX;
import static org.openmrs.module.ohricore.fhir.FhirClient.fetchFhirDiagnosticReport;
import static org.openmrs.module.ohricore.fhir.FhirClient.fetchFhirObservation;
import static org.openmrs.module.ohricore.fhir.FhirClient.fetchFhirTasksThatAreCompleted;
import static org.openmrs.module.ohricore.fhir.FhirClient.fetchFhirTasksThatAreRejected;

/**
 * @author Arthur D.M, Amos Laboso date: 23/08/2022
 */
public class FhirProcessor {
	
	public void fetchCompletedViralLoadLabResults() throws URISyntaxException {
		List<Obs> viralLoadResults = getCompletedViralLoadObs();
		saveOrUpdateObs(viralLoadResults);
	}
	
	public void fetchRejectedViralLoadRequests() throws URISyntaxException {
		List<Obs> viralLoadResults = getRejectedRequestObs();
		saveOrUpdateObs(viralLoadResults);
	}
	
	private void saveOrUpdateObs(List<Obs> observations) {
		
		for (Obs obsToSave : observations) {
			System.out.println("Going to Save Obs: " + obsToSave.getUuid() + " - id: " + obsToSave.getId());
			Context.getObsService().saveObs(obsToSave, "Fetched from DISI");
		}
	}
	
	private List<Obs> getRejectedRequestObs() throws URISyntaxException {

        List<Obs> observations = new ArrayList<>();
        Bundle taskBundle = fetchFhirTasksThatAreRejected();
        List<Bundle.BundleEntryComponent> bundleEntryComponents = taskBundle.getEntry();
        System.out.println("Running processFhirObs(): size - " + bundleEntryComponents.size());

        for (Bundle.BundleEntryComponent bundleEntry : bundleEntryComponents) {

            Task task = (Task) bundleEntry.getResource();
            System.out.println("Task: " + CTX.newJsonParser().encodeResourceToString(task));

            //Get the Encounter & Patient Details
            org.openmrs.Patient patient = null;
            Encounter encounter = null;
            for (Identifier identifier : task.getIdentifier()) {

                if (identifier.getSystem().equals(OHRI_ENCOUNTER_SYSTEM)) {

                    String encounterUuid = identifier.getValue();
                    System.out.println("Encounter UUID: " + encounterUuid);
                    encounter = Context.getEncounterService().getEncounterByUuid(encounterUuid);
                    patient = encounter.getPatient();
                }
            }
            System.out.println("Patient  : " + patient);

            if (patient != null) {

                List<Coding> coding = task.getStatusReason().getCoding();
                String display = coding.get(0).getDisplay();
                String code = coding.get(0).getCode();
                Date lastModified = task.getLastModifiedElement().getValue();

                Concept sampleRejected = Context.getConceptService().getConceptByUuid(CONCEPT_VL_SAMPLE_REJECTED);
                Obs obs = createObs(patient, sampleRejected, encounter);
                obs.setObsDatetime(lastModified);
                obs.setValueCoded(Context.getConceptService().getConceptByUuid(CONCEPT_YES));
                obs.setComment(code + " | " + display);
                observations.add(obs);
            }
        }
        return observations;
    }
	
	private List<Obs> getCompletedViralLoadObs() throws URISyntaxException {

        List<Obs> observations = new ArrayList<>();
        Bundle taskBundle = fetchFhirTasksThatAreCompleted();

        List<Bundle.BundleEntryComponent> bundleEntryComponents = taskBundle.getEntry();
        System.out.println("Running processFhirObs(): size - " + bundleEntryComponents.size());

        for (Bundle.BundleEntryComponent bundleEntry : bundleEntryComponents) {

            Task task = (Task) bundleEntry.getResource();
            System.out.println("Task: " + CTX.newJsonParser().encodeResourceToString(task));

            //Get the Encounter & Patient Details
            org.openmrs.Patient patient = null;
            Encounter encounter = null;
            for (Identifier identifier : task.getIdentifier()) {

                if (identifier.getSystem().equals(OHRI_ENCOUNTER_SYSTEM)) {

                    String encounterUuid = identifier.getValue();
                    System.out.println("Encounter UUID: " + encounterUuid);
                    try {
                        encounter = Context.getEncounterService().getEncounterByUuid(encounterUuid);
                        patient = encounter.getPatient();
                    } catch (NullPointerException npe) {
                        System.err.println("Failed to initialise Encounter/Patient object(s)");
                    }
                }
            }
            System.out.println("Patient  : " + patient);

            if (patient != null) {

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
                    List<String> obsIdList = new ArrayList<>();
                    for (Reference obsReference : diagnosticReport.getResult()) {
                        String urlRef = obsReference.getReference();
                        obsIdList.add(urlRef.split("/")[1]);
                    }

                    for (String obsId : obsIdList) {
                        Observation obs = fetchFhirObservation(obsId);
                        List<Obs> obsList = findObs(patient, encounter, obs);
                        observations.addAll(obsList);
                    }
                }
            }
        }
        return observations;
    }
	
	private List<Obs> findObs(org.openmrs.Patient patient, Encounter encounter, Observation obs) {
		
		String code = obs.getCode().getCoding().get(0).getCode();
		String resultValue = obs.getValueStringType().getValue();
		Date resultDate = obs.getEffectiveDateTimeType().getValue();
		
		switch (code) {
			case FHIR_OBS_VL_RESULT:
				return readVlObsHelper(patient, encounter, resultValue, resultDate);
				
			case FHIR_OBS_COVID_RESULT:
				return readCovidObsHelper(patient, encounter, resultValue, resultDate);
				
				//case FHIR_OBS_HIV_RESULT:
				//	return readCovidObsHelper(patient, encounter, resultValue, resultDate);
				
		}
		return Collections.emptyList();
	}
	
	private List<Obs> readVlObsHelper(org.openmrs.Patient patient, Encounter encounter, String resultValue, Date resultDate) {

        List<Obs> obs = new ArrayList<>();

        Concept resultConcept = Context.getConceptService().getConceptByUuid(CONCEPT_VL_RESULT);
        Obs resultObs = createObs(patient, resultConcept, encounter);
        resultObs.setObsDatetime(resultDate);
        resultObs.setValueNumeric(Double.valueOf(resultValue));

        Obs dateObs = readDateObsHelper(patient, encounter, CONCEPT_VL_RESULT_DATE, resultDate);

        obs.add(resultObs);
        obs.add(dateObs);
        return obs;
    }
	
	private List<Obs> readCovidObsHelper(org.openmrs.Patient patient, Encounter encounter, String resultValue, Date resultDate) {

        List<Obs> obsList = new ArrayList<>();

        Concept resultConcept = Context.getConceptService().getConceptByUuid(CONCEPT_COVID_RESULT_ANTIGEN);//TODO: there are different kind of Tests (PCR, Antigen)
        Obs resultObs = createObs(patient, resultConcept, encounter);
        resultObs.setObsDatetime(resultDate);
        resultObs.setValueCoded(interpretTestValue(resultValue));//TODO: set depending on value received

        Obs dateObs = readDateObsHelper(patient, encounter, CONCEPT_COVID_RESULT_DATE, resultDate);

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
	
	private Concept interpretTestValue(String testResult) {
		
		switch (testResult) {
		
			case "positive":
				return Context.getConceptService().getConceptByUuid(POSITIVE);
			case "negative":
				return Context.getConceptService().getConceptByUuid(NEGATIVE);
			case "inconclusive":
				return Context.getConceptService().getConceptByUuid(INCONCLUSIVE);
		}
		return Context.getConceptService().getConceptByUuid(INCONCLUSIVE);
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
}
