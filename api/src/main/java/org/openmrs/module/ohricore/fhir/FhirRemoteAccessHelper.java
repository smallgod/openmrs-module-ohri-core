//package org.openmrs.module.ohricore.fhir;
//
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
//import org.hl7.fhir.r4.model.Bundle;
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
//import org.openmrs.Concept;
//import org.openmrs.Encounter;
//import org.openmrs.Location;
//import org.openmrs.Obs;
//import org.openmrs.Patient;
//import org.openmrs.api.context.Context;
//import org.openmrs.module.ohricore.OhriCoreConstant;
//
//import java.io.BufferedReader;
//import java.io.InputStreamReader;
//import java.net.HttpURLConnection;
//import java.net.URISyntaxException;
//import java.net.URL;
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.Date;
//import java.util.List;
//import java.util.Locale;
//
//import static org.openmrs.module.ohricore.OhriCoreConstant.CONCEPT_VL_RESULT;
//import static org.openmrs.module.ohricore.OhriCoreConstant.CONCEPT_VL_RESULT_DATE;
//import static org.openmrs.module.ohricore.OhriCoreConstant.CONCEPT_VL_RESULT_DETECTED;
//import static org.openmrs.module.ohricore.OhriCoreConstant.CONCEPT_VL_RESULT_DETECTION;
//import static org.openmrs.module.ohricore.OhriCoreConstant.CONCEPT_VL_SAMPLE_REJECTED;
//import static org.openmrs.module.ohricore.OhriCoreConstant.CONCEPT_YES;
//import static org.openmrs.module.ohricore.OhriCoreConstant.FHIR_COMPLETED_OHRI_TASKS;
//import static org.openmrs.module.ohricore.OhriCoreConstant.FHIR_DIAGNOSTIC_REPORT;
//import static org.openmrs.module.ohricore.OhriCoreConstant.FHIR_DIAGNOSTIC_REPORT_OBS;
//import static org.openmrs.module.ohricore.OhriCoreConstant.FHIR_OBS_VL_RESULT;
//import static org.openmrs.module.ohricore.OhriCoreConstant.FHIR_REJECTED_OHRI_TASKS;
//import static org.openmrs.module.ohricore.OhriCoreConstant.OHRI_ENCOUNTER_SYSTEM;
//import static org.openmrs.module.ohricore.fhir.FhirClient.fetchFhirTasksThatAreCompletedOrRejected;
//
///**
// * @author smallGod date: 27/07/2022
// */
//public class FhirRemoteAccessHelper {
//
//    protected final Log log = LogFactory.getLog(getClass());
//
//    private String ohriEncounterUUID = null;
//
//    private String getRequest(String urlPath) throws Exception {
//
//        String line;
//        BufferedReader reader;
//        StringBuilder responseContent = new StringBuilder();
//
//        URL url = new URL(OhriCoreConstant.FHIR_SANDBOX_URL + urlPath);
//        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//        conn.setRequestProperty("Authorization", "Custom test");
//        conn.setRequestProperty("Content-Type", "application/fhir+json");
//        conn.setRequestProperty("Accept", "application/json");
//        conn.setRequestMethod("GET");
//        conn.setConnectTimeout(5000);// 5 seconds
//        conn.setReadTimeout(5000);
//
//        int status = conn.getResponseCode();
//        System.out.println("FHIR Status: " + status);
//
//        if (status >= 300) {
//            reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
//            while ((line = reader.readLine()) != null) {
//                responseContent.append(line);
//            }
//        } else {
//            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//            while ((line = reader.readLine()) != null) {
//                responseContent.append(line);
//            }
//        }
//        reader.close();
//        return responseContent.toString().trim();
//    }
//
//    public void fetchTasks() throws URISyntaxException {
//
//        Bundle tasks = fetchFhirTasksThatAreCompletedOrRejected();
//    }
//
//
//
//    public void fetchRejectedLabResults() {
//
//        try {
//
//            String rejectedLabTasksResponse = getRequest(FHIR_REJECTED_OHRI_TASKS);
//            System.out.println("FHIR Rejected Tasks:" + rejectedLabTasksResponse);
//
//            JSONArray failedTasks = getLabResultsTasks(rejectedLabTasksResponse);
//            Concept sampleRejected = Context.getConceptService().getConceptByUuid(CONCEPT_VL_SAMPLE_REJECTED);
//
//            for (int i = 0; i < failedTasks.length(); i++) {
//
//                this.ohriEncounterUUID = null;
//                JSONObject labResultResource = failedTasks.getJSONObject(i).getJSONObject("resource");
//
//                //interested in only OHRI encounter txns & completed
//                setOhriEncounterUUID(labResultResource);
//                String status = labResultResource.getString("status");
//                if (this.ohriEncounterUUID == null || !status.equalsIgnoreCase("rejected")) {
//                    continue;
//                }
//
//                //Fetch Patient Details/UUID
//                //JSONArray basedOn = labResultResource.getJSONArray("basedOn");
//                //JSONObject serviceRequestTag = basedOn.getJSONObject(0);
//                //String serviceRequestId = serviceRequestTag.getString("reference").split("/")[1];
//                //String serviceRequestResp = getRequest(FHIR_SERVICE_REQUEST + serviceRequestId);
//                //String patientReference = new JSONObject(serviceRequestResp).getJSONObject("subject").getString("reference");
//                //Patient patient = Context.getPatientService().getPatientByUuid(patientReference.split("/")[1]);
//
//                JSONObject statusReason = labResultResource.getJSONObject("statusReason");
//
//                JSONArray codingArr = statusReason.getJSONArray("coding");
//                JSONObject coding = codingArr.getJSONObject(0);
//                String code = coding.getString("code");
//                String display = coding.getString("display");
//
//                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
//                Date obsDate;
//                try {
//                    obsDate = formatter.parse(labResultResource.getString("lastModified"));
//
//                } catch (ParseException e) {
//                    obsDate = new Date();
//                    System.err.println("Query Lab Results ParseException: " + e.getLocalizedMessage());
//                }
//
//                Encounter encounter = Context.getEncounterService().getEncounterByUuid(this.ohriEncounterUUID);
//                Patient patient = encounter.getPatient();
//
//                Obs obs = createObs(patient, sampleRejected, encounter);
//                obs.setObsDatetime(obsDate);
//                obs.setValueCoded(Context.getConceptService().getConceptByUuid(CONCEPT_YES));
//                obs.setComment(code + " | " + display);
//                Context.getObsService().saveObs(obs, "Fetched from DISI");
//            }
//
//        } catch (Exception e) {
//            log.error("Error fetching Lab Results: ", e);
//        }
//    }
//
//    public void fetchCompletedLabResults() {
//
//        try {
//
//            String completedLabResultsResponse = getRequest(FHIR_COMPLETED_OHRI_TASKS);
//            System.out.println("FHIR Completed Tasks:" + completedLabResultsResponse);
//
//            JSONArray labTasks = getLabResultsTasks(completedLabResultsResponse);
//
//            for (int i = 0; i < labTasks.length(); i++) {
//
//                this.ohriEncounterUUID = null;
//                JSONObject labTask = labTasks.getJSONObject(i);
//                List<String> diagnosticReportIds = getLabResultsDiagnosticReportIds(labTask);
//
//                for (String diagnosticReportId : diagnosticReportIds) {
//
//                    String diagnosticReportResponse = getRequest(FHIR_DIAGNOSTIC_REPORT + diagnosticReportId);
//                    List<String> diagnosticReportObsIds = getLabResultsDiagnosticReportObsIds(diagnosticReportResponse);
//
//                    for (String observationId : diagnosticReportObsIds) {
//
//                        String observationResponse = getRequest(FHIR_DIAGNOSTIC_REPORT_OBS + observationId);
//                        FhirObsResourceType obsResource = readLabObservation(observationResponse);
//
//                        List<Obs> observations = getViralLoadConcept(obsResource);
//                        for(Obs obsToSave : observations){
//                            System.out.println("Going to Save Obs: " + obsToSave.getUuid() + " - id: " + obsToSave.getId());
//                            Context.getObsService().saveObs(obsToSave, "Fetched from DISI");
//                        }
//
//
////                        Supplier<Stream<Obs>> obsListStream = getViralLoadConcept(obsResource)::stream;
////                        obsListStream.get()
////                                .findAny()
////                                .map(obs -> {
////                                    return Context.getObsService().saveObs(obs, "Fetched from DISI");
////                                });
//
//                        //List<Obs> existingObs = Context.getObsService() //TODO: if not empty, need to update obs (not insert)
//                        //        .getObservationsByPersonAndConcept(obs.getPerson(), obs.getConcept());
//                    }
//                }
//            }
//        } catch (Exception e) {
//            log.error("Error fetching Lab Results: ", e);
//        }
//    }
//
//    private void setOhriEncounterUUID(JSONObject resourceTag) {
//
//        JSONArray identifiers = resourceTag.getJSONArray("identifier");
//        for (int m = 0; m < identifiers.length(); m++) {
//
//            JSONObject identifier = identifiers.getJSONObject(m);
//            if (identifier.getString("system").equalsIgnoreCase(OHRI_ENCOUNTER_SYSTEM)) {
//                this.ohriEncounterUUID = identifier.getString("value");
//            }
//        }
//    }
//
//    private Obs createObs(Patient patient, Concept concept, Encounter encounter) {
//
//        Location location = Context.getLocationService().getDefaultLocation();
//
//        Obs obs = new Obs();
//        obs.setDateCreated(new Date());
//        obs.setObsDatetime(new Date());
//        obs.setPerson(patient);
//        obs.setConcept(concept);
//        obs.setLocation(location);
//        obs.setStatus(Obs.Status.FINAL);
//        obs.setEncounter(encounter);
//        return obs;
//    }
//
//    private List<Obs> getViralLoadConcept(FhirObsResourceType obsResourceType) {
//
//        List<Obs> observations = new ArrayList<>();
//
//        if (obsResourceType != null) {
//
//            Encounter encounter = Context.getEncounterService().getEncounterByUuid(this.ohriEncounterUUID);
//            Patient patient = encounter.getPatient();
//
//            for (FhirCoding coding : obsResourceType.getCoding()) {
//
//                if (coding.getCode().equals(FHIR_OBS_VL_RESULT)) {
//
//                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
//                    Date obsDate = null;
//
//                    JSONObject root = obsResourceType.getRoot();
//                    try {
//
//                        obsDate = formatter.parse(root.getString("effectiveDateTime"));
//                    } catch (ParseException e) {
//                        System.err.println("Query Lab Results Error: " + e.getLocalizedMessage());
//                    }
//
//                    Concept vlResultConcept = Context.getConceptService().getConceptByUuid(CONCEPT_VL_RESULT);
//                    Obs vlResult = createObs(patient, vlResultConcept, encounter);
//                    vlResult.setObsDatetime(obsDate);
//                    vlResult.setValueNumeric(Double.valueOf(root.getString("valueString")));
//
//                    Concept vlResultDateConcept = Context.getConceptService().getConceptByUuid(CONCEPT_VL_RESULT_DATE);
//                    Obs vlResultDate = createObs(patient, vlResultDateConcept, encounter);
//                    vlResultDate.setObsDatetime(obsDate);
//                    vlResultDate.setValueDate(obsDate);
//
//                    Concept vlResultDetectedConcept = Context.getConceptService().getConceptByUuid(CONCEPT_VL_RESULT_DETECTION);
//                    Obs vlResultDetected = createObs(patient, vlResultDetectedConcept, encounter);
//                    vlResultDetected.setObsDatetime(obsDate);
//                    vlResultDetected.setValueCoded(Context.getConceptService().getConceptByUuid(CONCEPT_VL_RESULT_DETECTED));
//
//                    observations.add(vlResult);
//                    observations.add(vlResultDate);
//                    observations.add(vlResultDetected);
//                    return observations;
//                }
//            }
//        }
//        return observations;
//    }
//
//    private JSONArray getLabResultsTasks(String labTaskResults) {
//
//        JSONObject root = new JSONObject(labTaskResults);
//        return root.getJSONArray("entry");
//    }
//
//    private List<String> getLabResultsDiagnosticReportIds(JSONObject labResultTask) {
//
//        JSONObject resource = labResultTask.getJSONObject("resource");
//
//        //interested in only OHRI encounter txns & completed
//        setOhriEncounterUUID(resource);
//        String status = resource.getString("status");
//        if (this.ohriEncounterUUID == null || !status.equalsIgnoreCase("completed")) {
//            return Collections.emptyList();
//        }
//
//        JSONArray diagnosticReports = resource.getJSONArray("output");
//        List<String> diagnosticReportIds = new ArrayList<>();
//        for (int j = 0; j < diagnosticReports.length(); j++) {
//
//            JSONObject report = diagnosticReports.getJSONObject(j);
//            String diagnosticsReference = report.getJSONObject("valueReference").getString("reference");
//            String[] refTokens = diagnosticsReference.split("/");
//            diagnosticReportIds.add(refTokens[refTokens.length - 1]);
//        }
//        return diagnosticReportIds;
//    }
//
//    private List<String> getLabResultsDiagnosticReportObsIds(String diagnosticReportResponse) {
//
//        List<String> diagnosticReportObsIds = new ArrayList<>();
//
//        JSONObject root = new JSONObject(diagnosticReportResponse);
//        JSONArray obsResults = root.getJSONArray("result");
//
//        for (int i = 0; i < obsResults.length(); i++) {
//
//            JSONObject result = obsResults.getJSONObject(i);
//            String obsReference = result.getString("reference");
//            String[] refTokens = obsReference.split("/");
//            diagnosticReportObsIds.add(refTokens[refTokens.length - 1]);
//
//        }
//        return diagnosticReportObsIds;
//    }
//
//    private FhirObsResourceType readLabObservation(String observationResponse) {
//
//        JSONObject root = new JSONObject(observationResponse);
//        if (root.getString("resourceType").equals("Observation")) {
//
//            FhirObsResourceType obsResourceType = new FhirObsResourceType();
//            obsResourceType.setRoot(root);
//
//            JSONObject rootCode;
//            try {
//                rootCode = root.getJSONObject("code");
//            } catch (JSONException e) {
//                System.err.println("JsonObject Node 'Code' Not Found");
//                return null;
//            }
//            JSONArray obsCodes = rootCode.getJSONArray("coding");
//            for (int j = 0; j < obsCodes.length(); j++) {
//
//                JSONObject report = obsCodes.getJSONObject(j);
//                String code = report.getString("code");
//                String display = report.getString("display");
//
//                obsResourceType.getCoding().add(new FhirCoding(code, display));
//            }
//            return obsResourceType;
//        }
//        return null;
//    }
//}
