package org.openmrs.module.ohricore.fhir;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.ohricore.OhriCoreConstant;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.openmrs.module.ohricore.OhriCoreConstant.FHIR_COMPLETED_TASKS;
import static org.openmrs.module.ohricore.OhriCoreConstant.FHIR_DIAGNOSTIC_REPORT;
import static org.openmrs.module.ohricore.OhriCoreConstant.FHIR_DIAGNOSTIC_REPORT_OBS;
import static org.openmrs.module.ohricore.OhriCoreConstant.FHIR_HIV_RECENCY_TEST_CONDUCTED;

/**
 * @author smallGod
 * date: 27/07/2022
 */
public class FhirRemoteAccessHelper {

    protected final Log log = LogFactory.getLog(getClass());

    String getRequest(String urlPath) throws Exception {

        String line;
        BufferedReader reader;
        StringBuilder responseContent = new StringBuilder();

        URL url = new URL(OhriCoreConstant.FHIR_SANDBOX_URL + urlPath);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);// 5 seconds
        conn.setReadTimeout(5000);

        int status = conn.getResponseCode();

        if (status >= 300) {
            reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            while ((line = reader.readLine()) != null) {
                responseContent.append(line);
            }
            reader.close();
        } else {
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            while ((line = reader.readLine()) != null) {
                responseContent.append(line);
            }
            reader.close();
        }
        return responseContent.toString();
    }

    public void fetchLabResults() {

        try {

            String completedLabResultsResponse = getRequest(FHIR_COMPLETED_TASKS);
            List<String> diagnosticReportIds = getLabResultsDiagnosticReportIds(completedLabResultsResponse);

            for (String diagnosticReportId : diagnosticReportIds) {
                String diagnosticReportResponse = getRequest(FHIR_DIAGNOSTIC_REPORT + diagnosticReportId);
                List<String> diagnosticReportObsIds = getLabResultsDiagnosticReportObsIds(diagnosticReportResponse);

                for (String diagnosticReportObsId : diagnosticReportObsIds) {

                    String diagnosticReportObsResponse = getRequest(FHIR_DIAGNOSTIC_REPORT_OBS + diagnosticReportObsId);
                    FhirObsResourceType obsResourceType = getLabResultsDiagnosticReportObs(diagnosticReportObsResponse);

                    Obs obs = getViralLoadConcept(obsResourceType.getCoding(), obsResourceType.getValueString());
                    Context.getObsService().saveObs(obs, "Fetched from DISI");
                }
            }

        } catch (Exception e) {
            log.error("Error fetching Lab Results: ", e);
        }
    }

    Obs createObs(Integer patientId, Concept concept) {

        Patient patient = Context.getPatientService().getPatient(patientId);
        Location location = Context.getLocationService().getDefaultLocation();

        Obs obs = new Obs();
        obs.setDateCreated(new Date());
        obs.setObsDatetime(new Date());
        obs.setPerson(patient);
        obs.setConcept(concept);
        obs.setLocation(location);

        return obs;
    }

    Obs getViralLoadConcept(List<FhirCoding> codingList, String valueString) {

        for (FhirCoding coding : codingList) {
            if (coding.getCode().equals(FHIR_HIV_RECENCY_TEST_CONDUCTED)) {

                Obs obs = createObs(patientId, Context.getConceptService().getConcept());
                obs.setValueText(valueString);
                return obs;
            }
        }
        return null;
    }

    private List<String> getLabResultsDiagnosticReportIds(String completedLabResultsResponse) {

        List<String> diagnosticReportIds = new ArrayList<>();

        JSONObject root = new JSONObject(completedLabResultsResponse);
        JSONArray labResultTasks = root.getJSONArray("entry");

        for (int i = 0; i < labResultTasks.length(); i++) {

            JSONObject labResult = labResultTasks.getJSONObject(i);
            JSONArray diagnosticReports = labResult.getJSONObject("resource").getJSONArray("output");

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

    private FhirObsResourceType getLabResultsDiagnosticReportObs(String diagnosticReportObsResponse) {

        FhirObsResourceType obsResourceType = new FhirObsResourceType();

        JSONObject root = new JSONObject(diagnosticReportObsResponse);
        String[] refTokens = root.getJSONObject("subject").getString("reference").split("/");
        String patientId = refTokens[refTokens.length - 1];
        String valueString = root.getString("valueString");

        obsResourceType.setValueString(valueString);
        obsResourceType.setPatientId(patientId);

        JSONArray obsCodes = root.getJSONObject("code").getJSONArray("coding");
        for (int i = 0; i < obsCodes.length(); i++) {

            JSONObject report = obsCodes.getJSONObject(i);
            String code = report.getString("code");
            String display = report.getString("display");

            obsResourceType.getCoding().add(new FhirCoding(code, display));
        }
        return obsResourceType;
    }
}
