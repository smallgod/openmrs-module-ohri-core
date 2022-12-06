package org.openmrs.module.ohricore.fhir;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.AdditionalRequestHeadersInterceptor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.Task;
import org.openmrs.Location;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.context.Context;
import org.openmrs.module.ohricore.OhriCoreConstant;
import org.openmrs.module.ohricore.Token;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

import static org.openmrs.module.ohricore.OhriCoreConstant.FHIR_OBS_VL_RESULT;
import static org.openmrs.module.ohricore.OhriCoreConstant.HEALTH_ID_SYSTEM;
import static org.openmrs.module.ohricore.OhriCoreConstant.NATIONAL_ID_SYSTEM;
import static org.openmrs.module.ohricore.OhriCoreConstant.OHRI_ENCOUNTER_SYSTEM;

/**
 * @author Arthur D. Mugume, Amos date: 18/08/2022
 */
public class FhirClient {
	
	static FhirContext CTX = FhirContext.forR4();
	
	static ObjectMapper mapper;
	
	static {
		FhirClient.mapper = new ObjectMapper();
		FhirClient.mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
	}
	
	public static IGenericClient getClient() throws URISyntaxException {
		
		String url = Context.getAdministrationService().getGlobalProperty(OhriCoreConstant.GP_PARENT_SERVER_URL);
		String username = Context.getAdministrationService().getGlobalProperty(OhriCoreConstant.GP_PARENT_SERVER_USERNAME);
		String password = Context.getAdministrationService().getGlobalProperty(OhriCoreConstant.GP_PARENT_SERVER_PASSWORD);
		URI uri = new URI(url);
		//URI uri = new URI(url + "/ws/fhir2/R4");
		
		String auth = username + ":" + password;
		String base64Creds = Base64.getEncoder().encodeToString(auth.getBytes());
		
		IGenericClient client = CTX.newRestfulGenericClient(uri.toString());
		AdditionalRequestHeadersInterceptor interceptor = new AdditionalRequestHeadersInterceptor();
		// interceptor.addHeaderValue("Authorization", "Basic " + base64Creds);
		interceptor.addHeaderValue("Authorization", "Custom test");
		client.registerInterceptor(interceptor);
		
		return client;
	}
	
	public static IGenericClient getMPIClient() throws URISyntaxException {
		
		String url = Context.getAdministrationService().getGlobalProperty(OhriCoreConstant.GP_MPI_SERVER_URL);
		String clientId = Context.getAdministrationService().getGlobalProperty(OhriCoreConstant.GP_MPI_CLIENT_ID);
		String clientSecret = Context.getAdministrationService().getGlobalProperty(OhriCoreConstant.GP_MPI_CLIENT_SECRET);
		
		String bearer = getToken(url, clientId, clientSecret);
		
		System.out.println("bearer: " + bearer);
		
		URI uri = new URI(url);
		
		//		FhirContext ctx = FhirContext.forR4();
		//		// Set how long to try and establish the initial TCP connection (in ms)
		//		ctx.getRestfulClientFactory().setConnectTimeout(20 * 1000);
		//		// Set how long to block for individual read/write operations (in ms)
		//		ctx.getRestfulClientFactory().setSocketTimeout(20 * 1000);
		//		// Create the client
		//		IGenericClient client = ctx.newRestfulGenericClient(url);
		IGenericClient client = CTX.newRestfulGenericClient(uri.toString());
		AdditionalRequestHeadersInterceptor interceptor = new AdditionalRequestHeadersInterceptor();
		interceptor.addHeaderValue("Authorization", "Bearer " + bearer);
		client.registerInterceptor(interceptor);
		
		return client;
	}
	
	public static String getToken(String url, String clientId, String clientSecret) {
		
		String bearer = null;
		try {
			
			String line;
			BufferedReader reader;
			StringBuilder responseContent = new StringBuilder();
			
			URL urlPath = new URL(url + "/auth");
			HttpURLConnection conn = (HttpURLConnection) urlPath.openConnection();
			conn.setRequestProperty("Accept", "*/*");
			conn.setRequestProperty("client_id", clientId);
			conn.setRequestProperty("client_secret", clientSecret);
			conn.setRequestProperty("grant_type", "client_credentials");
			conn.setRequestMethod("POST");
			conn.setConnectTimeout(5000);// 5 seconds
			conn.setReadTimeout(5000);
			
			int status = conn.getResponseCode();
			
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
			
			JSONObject root = new JSONObject(responseContent.toString().trim());
			bearer = root.getString("access_token");
			
		}
		catch (Exception exc) {
			System.err.println("Failed to get Token");
			exc.printStackTrace();
		}
		return bearer;
	}
	
	public static List<PatientIdentifier> postPatient(Patient newPatient) {

        List<PatientIdentifier> patientIds = new ArrayList<>();

        try {

            String url = Context.getAdministrationService().getGlobalProperty(OhriCoreConstant.GP_MPI_SERVER_URL);
            String clientId = Context.getAdministrationService().getGlobalProperty(OhriCoreConstant.GP_MPI_CLIENT_ID);
            String clientSecret = Context.getAdministrationService().getGlobalProperty(OhriCoreConstant.GP_MPI_CLIENT_SECRET);

            String bearer = getToken(url, clientId, clientSecret);
            String patient = CTX.newJsonParser().encodeResourceToString(newPatient);
            URI urlPath = new URI(Context.getAdministrationService().getGlobalProperty(OhriCoreConstant.GP_MPI_SERVER_URL) + "/Patient");

            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer " + bearer);
            headers.add("Content-Type", "application/json");

            HttpEntity<String> request = new HttpEntity<>(patient, headers);
            ResponseEntity<String> response = new RestTemplate().postForEntity(urlPath, request, String.class);

            JSONObject root = new JSONObject(response.getBody().trim());
            JSONArray identifiers = root.getJSONArray("identifier");

            for (int i = 0; i < identifiers.length(); i++) {

                JSONObject identifier = identifiers.getJSONObject(i);
                String system = identifier.getString("system");

                if (system.equalsIgnoreCase(HEALTH_ID_SYSTEM)) {
                    patientIds.add(new PatientIdentifier(
                            identifier.getString("value"),
                            Context.getPatientService().getPatientIdentifierType(7),
                            Context.getLocationService().getDefaultLocation()));
                }
            }

        } catch (Exception exc) {
            System.err.println("Failed to get Token");
            exc.printStackTrace();
        }
        System.out.println(patientIds);
        return patientIds;
    }
	
	public static String postFhirResource(Resource resource) throws Exception {
		
		return getClient().create().resource(resource).prettyPrint().encodedJson().execute().getOperationOutcome()
		        .toString();
	}
	
	public static String postMPIRequest(Resource resource) throws Exception {
		
		System.out.println("Sending...");
		
		return getMPIClient().create().resource(resource).prettyPrint().encodedJson().execute().getOperationOutcome()
		        .toString();
	}
	
	public static Bundle fetchFhirTasks() throws URISyntaxException {
		
		return getClient().search().forResource(Task.class).returnBundle(Bundle.class).execute();
	}
	
	public static Bundle fetchFhirTasksThatAreCompleted() throws URISyntaxException {
		
		return getClient().search().forResource(Task.class)
		        .where(Task.STATUS.exactly().codes(Task.TaskStatus.COMPLETED.toCode()))
		        .and(Task.IDENTIFIER.hasSystemWithAnyCode(OHRI_ENCOUNTER_SYSTEM)).returnBundle(Bundle.class).execute();
	}
	
	public static Bundle fetchFhirTasksThatAreRejected() throws URISyntaxException {
		
		return getClient().search().forResource(Task.class)
		        .where(Task.STATUS.exactly().codes(Task.TaskStatus.REJECTED.toCode()))
		        .and(Task.IDENTIFIER.hasSystemWithAnyCode(OHRI_ENCOUNTER_SYSTEM)).returnBundle(Bundle.class).execute();
	}
	
	public static DiagnosticReport fetchFhirDiagnosticReport(String diagnosticReportId) throws URISyntaxException {
		
		return getClient().read().resource(DiagnosticReport.class).withId(diagnosticReportId).execute();
	}
	
	public static Observation fetchFhirObservation(String observationId) throws URISyntaxException {
		
		return getClient().read().resource(Observation.class).withId(observationId).execute();
	}
	
	public static Bundle fetchFhirObservationsWithVlResult() throws URISyntaxException {
		
		return getClient().search().forResource(Observation.class).where(Task.CODE.exactly().code(FHIR_OBS_VL_RESULT))
		        .returnBundle(Bundle.class).execute();
		//.where(DiagnosticReport.hasChainedProperty)
	}
	
	public static Bundle fetchFhirDiagnosticReports2(String... diagnosticReportIds) throws URISyntaxException {
		
		return getClient().search().forResource(DiagnosticReport.class)
		        .where(DiagnosticReport.RESULT.hasAnyOfIds(diagnosticReportIds))//pass ids - {114343, 233444}
		        .returnBundle(Bundle.class).execute();
	}
	
	public static Bundle fetchFhirObservations() throws URISyntaxException {
		
		return getClient().search().forResource(Observation.class).returnBundle(Bundle.class).execute();
	}
	
	public static Bundle fetchFhirPatients() throws URISyntaxException {
		
		return getClient().search().forResource(Patient.class).returnBundle(Bundle.class).execute();
	}
	
	public static Bundle fetchFhirPatients(Bundle bundle) throws URISyntaxException {
		
		return getClient().loadPage().next(bundle).execute();
	}
}
