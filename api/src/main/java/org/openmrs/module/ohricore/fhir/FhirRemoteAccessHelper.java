package org.openmrs.module.ohricore.fhir;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.ohricore.OhriCoreConstant;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static org.openmrs.module.ohricore.OhriCoreConstant.CONCEPT_TEST_PATIENT_TWO;
import static org.openmrs.module.ohricore.OhriCoreConstant.CONCEPT_VL_RESULT;
import static org.openmrs.module.ohricore.OhriCoreConstant.CONCEPT_TEST_PATIENT_ONE;
import static org.openmrs.module.ohricore.OhriCoreConstant.CONCEPT_VL_RESULT_DATE;
import static org.openmrs.module.ohricore.OhriCoreConstant.CONCEPT_VL_RESULT_DETECTED;
import static org.openmrs.module.ohricore.OhriCoreConstant.CONCEPT_VL_RESULT_DETECTION;
import static org.openmrs.module.ohricore.OhriCoreConstant.CONCEPT_VL_SAMPLE_REJECTED;
import static org.openmrs.module.ohricore.OhriCoreConstant.CONCEPT_YES;
import static org.openmrs.module.ohricore.OhriCoreConstant.ENCOUNTER_PATIENT_ONE;
import static org.openmrs.module.ohricore.OhriCoreConstant.ENCOUNTER_PATIENT_TWO;
import static org.openmrs.module.ohricore.OhriCoreConstant.FHIR_COMPLETED_OHRI_TASKS;
import static org.openmrs.module.ohricore.OhriCoreConstant.FHIR_COMPLETED_TASKS;
import static org.openmrs.module.ohricore.OhriCoreConstant.FHIR_DIAGNOSTIC_REPORT;
import static org.openmrs.module.ohricore.OhriCoreConstant.FHIR_DIAGNOSTIC_REPORT_OBS;
import static org.openmrs.module.ohricore.OhriCoreConstant.FHIR_OBS_VL_RESULT;
import static org.openmrs.module.ohricore.OhriCoreConstant.FHIR_REJECTED_TASKS_TEST;
import static org.openmrs.module.ohricore.OhriCoreConstant.OHRI_ENCOUNTER_SYSTEM_IDENTIFIER;

/**
 * @author smallGod date: 27/07/2022
 */
public class FhirRemoteAccessHelper {

    protected final Log log = LogFactory.getLog(getClass());

    String getRequest(String urlPath) throws Exception {

        String line;
        BufferedReader reader;
        StringBuilder responseContent = new StringBuilder();

        URL url = new URL(OhriCoreConstant.FHIR_SANDBOX_URL + urlPath);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("Authorization", "Custom test");
        conn.setRequestProperty("Content-Type", "application/fhir+json");
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);// 5 seconds
        conn.setReadTimeout(5000);

        int status = conn.getResponseCode();
        System.out.println("FHIR Status: " + status);

        if (status >= 300) {
            reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            while ((line = reader.readLine()) != null) {
                responseContent.append(line);
            }
        } else {
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            while ((line = reader.readLine()) != null) {
                responseContent.append(line);
            }
        }
        reader.close();
        return responseContent.toString().trim();
    }

    public void fetchRejectedLabResults() {

        try {

            String rejectedLabResultsResponse = getRequest(FHIR_REJECTED_TASKS_TEST);
            System.out.println("FHIR Rejected Tasks:" + rejectedLabResultsResponse);

            JSONObject root = new JSONObject(rejectedLabResultsResponse);
            JSONArray failedTasks = root.getJSONArray("entry");

            for (int i = 0; i < failedTasks.length(); i++) {

                JSONObject labResult = failedTasks.getJSONObject(i).getJSONObject("resource");
                JSONObject statusReason = labResult.getJSONObject("statusReason");

                JSONArray codingArr = statusReason.getJSONArray("coding");
                JSONObject coding = codingArr.getJSONObject(0);
                String code = coding.getString("code");
                String display = coding.getString("display");

                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                Date obsDate;
                try {
                    obsDate = formatter.parse(labResult.getString("lastModified"));

                } catch (ParseException e) {
                    obsDate = new Date();
                    System.err.println("Query Lab Results ParseException: " + e.getLocalizedMessage());
                }

                Concept sampleRejected = Context.getConceptService().getConceptByUuid(CONCEPT_VL_SAMPLE_REJECTED);
                Encounter encounter = Context.getEncounterService().getEncounterByUuid(ENCOUNTER_PATIENT_TWO);
                Obs obs = createObs(CONCEPT_TEST_PATIENT_TWO, sampleRejected, encounter);
                obs.setObsDatetime(obsDate);
                obs.setValueCoded(Context.getConceptService().getConceptByUuid(CONCEPT_YES));
                obs.setComment(code + " | " + display);
                Context.getObsService().saveObs(obs, "Fetched from DISI");
            }

        } catch (Exception e) {
            log.error("Error fetching Lab Results: ", e);
        }
    }

    public void fetchCompletedLabResults() {

        try {

            String completedLabResultsResponse = getRequest(FHIR_COMPLETED_OHRI_TASKS);
            System.out.println("FHIR Completed Tasks:" + completedLabResultsResponse);

            List<String> diagnosticReportIds = getLabResultsDiagnosticReportIds(completedLabResultsResponse);

            for (String diagnosticReportId : diagnosticReportIds) {
                String diagnosticReportResponse = getRequest(FHIR_DIAGNOSTIC_REPORT + diagnosticReportId);
                List<String> diagnosticReportObsIds = getLabResultsDiagnosticReportObsIds(diagnosticReportResponse);

                for (String diagnosticReportObsId : diagnosticReportObsIds) {

                    //String diagnosticReportObsResponse = getRequest(FHIR_DIAGNOSTIC_REPORT_OBS + diagnosticReportObsId);
                    String diagnosticReportObsResponse = getRequest(FHIR_DIAGNOSTIC_REPORT_OBS);
                    List<FhirObsResourceType> obsResources = getLabResultsDiagnosticReportObs(diagnosticReportObsResponse);

                    for (FhirObsResourceType obsResource : obsResources) {
                        List<Obs> obsList = getViralLoadConcept(obsResource);
                        for (Obs obs : obsList) {
                            if (obs != null) {
                                Context.getObsService().saveObs(obs, "Fetched from DISI");
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            log.error("Error fetching Lab Results: ", e);
        }
    }

    String getOhriEncounterUUID(JSONObject resourceTag) {

        JSONArray identifiers = resourceTag.getJSONArray("identifier");
        for (int m = 0; m < identifiers.length(); m++) {

            JSONObject identifier = identifiers.getJSONObject(m);
            if (identifier.getString("system").equalsIgnoreCase(OHRI_ENCOUNTER_SYSTEM_IDENTIFIER)) {
                return identifier.getString("system");
            }
        }
        return null;
    }


    Obs createObs(String patientUuid, Concept concept, Encounter encounter) {

        Patient patient = Context.getPatientService().getPatientByUuid(patientUuid);
        Location location = Context.getLocationService().getDefaultLocation();

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

    List<Obs> getViralLoadConcept(FhirObsResourceType obsResourceType) {

        List<Obs> observations = new ArrayList<>();
        Encounter encounter = Context.getEncounterService().getEncounterByUuid(ENCOUNTER_PATIENT_ONE);

        if (obsResourceType != null) {

            for (FhirCoding coding : obsResourceType.getCoding()) {

                if (coding.getCode().equals(FHIR_OBS_VL_RESULT)) {

                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                    Date obsDate = null;

                    JSONObject root = obsResourceType.getRoot();
                    try {

                        obsDate = formatter.parse(root.getString("effectiveDateTime"));
                    } catch (ParseException e) {
                        System.err.println("Query Lab Results Error: " + e.getLocalizedMessage());
                    }

                    //String[] refTokens = root.getJSONObject("subject").getString("reference").split("/");
                    //String patientId = refTokens[refTokens.length - 1];

                    Concept vlResultConcept = Context.getConceptService().getConceptByUuid(CONCEPT_VL_RESULT);
                    Obs vlResult = createObs(CONCEPT_TEST_PATIENT_ONE, vlResultConcept, encounter);
                    vlResult.setObsDatetime(obsDate);
                    vlResult.setValueNumeric((double) root.getInt("valueInteger"));

                    Concept vlResultDateConcept = Context.getConceptService().getConceptByUuid(CONCEPT_VL_RESULT_DATE);
                    Obs vlResultDate = createObs(CONCEPT_TEST_PATIENT_ONE, vlResultDateConcept, encounter);
                    vlResultDate.setObsDatetime(obsDate);
                    vlResultDate.setValueDate(obsDate);

                    Concept vlResultDetectedConcept = Context.getConceptService().getConceptByUuid(CONCEPT_VL_RESULT_DETECTION);
                    Obs vlResultDetected = createObs(CONCEPT_TEST_PATIENT_ONE, vlResultDetectedConcept, encounter);
                    vlResultDetected.setObsDatetime(obsDate);
                    vlResultDetected.setValueCoded(Context.getConceptService().getConceptByUuid(CONCEPT_VL_RESULT_DETECTED));

                    observations.add(vlResult);
                    observations.add(vlResultDate);
                    observations.add(vlResultDetected);
                    return observations;
                }
            }
        }
        return observations;
    }

    private List<String> getLabResultsDiagnosticReportIds(String completedLabResultsResponse) {

        List<String> diagnosticReportIds = new ArrayList<>();

        JSONObject root = new JSONObject(completedLabResultsResponse);
        JSONArray labResultTasks = root.getJSONArray("entry");

        for (int i = 0; i < labResultTasks.length(); i++) {

            JSONObject labResult = labResultTasks.getJSONObject(i);
            JSONObject resource = labResult.getJSONObject("resource");

            //interested in only OHRI encounter txns & completed
            String ohriEncounterSystemId = getOhriEncounterUUID(resource);
            String status = resource.getString("status");
            if (ohriEncounterSystemId == null || !status.equalsIgnoreCase("completed")) {
                continue;
            }

            JSONArray diagnosticReports = resource.getJSONArray("output");

            for (int j = 0; j < diagnosticReports.length(); j++) {

                JSONObject report = diagnosticReports.getJSONObject(j);
                String diagnosticsReference = report.getJSONObject("valueReference").getString("reference");
                String[] refTokens = diagnosticsReference.split("/");
                diagnosticReportIds.add(refTokens[refTokens.length - 1]);
            }
        }
        return diagnosticReportIds;
    }

    private List<String> getLabResultsDiagnosticReportObsIds(String diagnosticReportResponse) {

        List<String> diagnosticReportObsIds = new ArrayList<>();

        JSONObject root = new JSONObject(diagnosticReportResponse);
        JSONArray obsResults = root.getJSONArray("result");

        for (int i = 0; i < obsResults.length(); i++) {

            JSONObject result = obsResults.getJSONObject(i);
            String obsReference = result.getString("reference");
            String[] refTokens = obsReference.split("/");
            diagnosticReportObsIds.add(refTokens[refTokens.length - 1]);

        }
        return diagnosticReportObsIds;
    }

    private List<FhirObsResourceType> getLabResultsDiagnosticReportObs(String diagnosticReportObsResponse) {

        List<FhirObsResourceType> obsResources = new ArrayList<>();
        JSONObject root = new JSONObject(diagnosticReportObsResponse);

        //obsResourceType.setPatientId(patientId);
        //obsResourceType.setValueInteger(valueInteger);
        //obsResourceType.setEffectiveDateTime(effectiveDateTime);

        String bundleType = root.getString("resourceType");
        if (bundleType.equals("Bundle")) {

            JSONArray obsResults = root.getJSONArray("entry");

            for (int i = 0; i < obsResults.length(); i++) {

                root = obsResults.getJSONObject(i).getJSONObject("resource");

                if (root.getString("resourceType").equals("Observation")) {

                    FhirObsResourceType obsResourceType = new FhirObsResourceType();
                    obsResourceType.setRoot(root);

                    JSONObject rootCode;
                    try {
                        rootCode = root.getJSONObject("code");
                    } catch (JSONException e) {
                        System.err.println("JsonObject Node 'Code' Not Found");
                        return null;
                    }
                    JSONArray obsCodes = rootCode.getJSONArray("coding");
                    for (int j = 0; j < obsCodes.length(); j++) {

                        JSONObject report = obsCodes.getJSONObject(j);
                        String code = report.getString("code");
                        String display = report.getString("display");

                        obsResourceType.getCoding().add(new FhirCoding(code, display));
                    }
                    obsResources.add(obsResourceType);
                }
            }

        }
        return obsResources;
    }
}
