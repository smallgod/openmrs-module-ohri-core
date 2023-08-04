package org.openmrs.module.ohricore.fhir;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.AdditionalRequestHeadersInterceptor;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.Task;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openmrs.Address;
import org.openmrs.PatientIdentifier;
import org.openmrs.Person;
import org.openmrs.PersonAddress;
import org.openmrs.PersonName;
import org.openmrs.api.context.Context;
import org.openmrs.module.fhir2.providers.r4.PatientFhirResourceProvider;
import org.openmrs.module.ohricore.OhriCoreConstant;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.openmrs.module.ohricore.OhriCoreConstant.FHIR_OBS_VL_RESULT;
import static org.openmrs.module.ohricore.OhriCoreConstant.HEALTH_FACILITY_ID_SYSTEM;
import static org.openmrs.module.ohricore.OhriCoreConstant.HEALTH_ID_SYSTEM;
import static org.openmrs.module.ohricore.OhriCoreConstant.NATIONAL_ID_SYSTEM;
import static org.openmrs.module.ohricore.OhriCoreConstant.OHRI_ENCOUNTER_SYSTEM;

/**
 * @author Arthur D. Mugume, Amos date: 18/08/2022
 */
public class FhirClient {

    private static final String url = Context.getAdministrationService().getGlobalProperty(
            OhriCoreConstant.GP_MPI_SERVER_URL);

    private static final String clientId = Context.getAdministrationService().getGlobalProperty(
            OhriCoreConstant.GP_MPI_CLIENT_ID);

    private static final String clientSecret = Context.getAdministrationService().getGlobalProperty(
            OhriCoreConstant.GP_MPI_CLIENT_SECRET);

    private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

    static FhirContext CTX = FhirContext.forR4();

    static ObjectMapper mapper;

    static {
        FhirClient.mapper = new ObjectMapper();
        FhirClient.mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

    }

    public static IGenericClient getClient() throws URISyntaxException {

        URI uri = new URI(FhirClient.url);

        IGenericClient client = CTX.newRestfulGenericClient(uri.toString());
        AdditionalRequestHeadersInterceptor interceptor = new AdditionalRequestHeadersInterceptor();
        interceptor.addHeaderValue("Authorization", "Custom test");
        client.registerInterceptor(interceptor);

        // String username = Context.getAdministrationService().getGlobalProperty(OhriCoreConstant.GP_PARENT_SERVER_USERNAME);
        // String password = Context.getAdministrationService().getGlobalProperty(OhriCoreConstant.GP_PARENT_SERVER_PASSWORD);
        // URI uri = new URI(url + "/ws/fhir2/R4");
        // String auth = username + ":" + password;
        // String base64Creds = Base64.getEncoder().encodeToString(auth.getBytes());
        // interceptor.addHeaderValue("Authorization", "Basic " + base64Creds);

        return client;
    }

    public static IGenericClient getMPIClient() throws URISyntaxException {

        String bearer = getToken();
        URI uri = new URI(url);

        System.out.println("bearer: " + bearer);

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

    private static class RequestProperty {

        private final String key;

        private final String value;

        public RequestProperty(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }
    }

    public static String sendRequest(String path, String method, List<RequestProperty> requestProperties) {

        try {

            String line;
            BufferedReader reader;
            StringBuilder responseContent = new StringBuilder();

            URL urlPath = new URL(FhirClient.url + path);
            HttpURLConnection conn = (HttpURLConnection) urlPath.openConnection();
            conn.setRequestProperty("Accept", "*/*");
            conn.setConnectTimeout(5000);// 5 seconds
            conn.setReadTimeout(5000);
            conn.setRequestMethod(method);
            for (RequestProperty property : requestProperties) {
                conn.setRequestProperty(property.getKey(), property.getValue());
            }

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
            return responseContent.toString().trim();

        } catch (Exception exc) {
            System.err.println("Failed to get Submit request");
            exc.printStackTrace();
        }
        return null;
    }

    public static String getToken() {

        List<RequestProperty> requestProperties = new ArrayList<>();
        requestProperties.add(new RequestProperty("client_id", FhirClient.clientId));
        requestProperties.add(new RequestProperty("client_secret", FhirClient.clientSecret));
        requestProperties.add(new RequestProperty("grant_type", "client_credentials"));

        String response = sendRequest("/auth", "POST", requestProperties);

        JSONObject root = new JSONObject(response);
        return root.getString("access_token");
    }

    public static org.openmrs.Patient getPatient(String healthId) {

        List<RequestProperty> requestProperties = new ArrayList<>();
        requestProperties.add(new RequestProperty("Authorization", "Bearer " + getToken()));

        String response = sendRequest("/Patient/" + healthId, "GET", requestProperties);
        System.out.println("response: " + response);

        JSONObject root = new JSONObject(response);
        Integer totalRecords = root.getInt("total");
        if (totalRecords == null || totalRecords < 1) {
            throw new ResourceNotFoundException("Could not find patient with Health ID: " + healthId);
        }

        JSONArray entries = root.getJSONArray("entry");
        for (int i = 0; i < entries.length(); i++) {

            JSONObject entry = entries.getJSONObject(i);
            JSONObject resource = entry.getJSONObject("resource");
            String resourceType = resource.getString("resourceType");

            if (resourceType.equals("Patient")) {

                JSONArray nameArray = resource.getJSONArray("name");
                org.openmrs.Patient patient = new org.openmrs.Patient();

                String gender = resource.getString("gender");
                String patientGender = null;
                if (gender != null) {
                    if (gender.equalsIgnoreCase("female")) {
                        patientGender = "F";
                    } else if (gender.equalsIgnoreCase("male")) {
                        patientGender = "M";
                    }
                }
                patient.setGender(patientGender);

                try {
                    patient.setBirthdate(formatter.parse(resource.getString("birthDate")));
                } catch (Exception exc) {
                    System.err.println("Error setting birth date");
                }

                for (int j = 0; j < nameArray.length(); j++) {
                    JSONObject name = nameArray.getJSONObject(j);
                    PersonName personName = new PersonName(name.getJSONArray("given").getString(0), null, name.getString("family"));
                    patient.getNames().add(personName);
                }

                JSONArray addresses = null;

                try {
                    addresses = resource.getJSONArray("address");
                } catch (JSONException exc) {
                    System.err.println("Address is null");
                }

                if (addresses != null) {

                    JSONObject address = addresses.getJSONObject(0);
                    PersonAddress personAddress = new PersonAddress();

                    if (address != null) {

                        String district = null;
                        String state = null;
                        String postalCode = null;
                        String country = null;

                        try {
                            district = address.getString("district");
                        } catch (JSONException exc) {
                            System.err.println("District is null");
                        }
                        try {
                            state = address.getString("state");
                        } catch (JSONException exc) {
                            System.err.println("State is null");
                        }
                        try {
                            postalCode = address.getString("postalCode");
                        } catch (JSONException exc) {
                            System.err.println("PostalCode is null");
                        }
                        try {
                            country = address.getString("country");
                        } catch (JSONException exc) {
                            System.err.println("Country is null");
                        }

                        if (district != null) {
                            personAddress.setCountyDistrict(district);
                        }
                        if (state != null) {
                            personAddress.setStateProvince(state);
                        }
                        if (postalCode != null) {
                            personAddress.setPostalCode(postalCode);
                        }
                        if (country != null) {
                            personAddress.setCountry(country);
                        }
                    }

                    Set<PersonAddress> personAddresses = new HashSet<>();
                    personAddresses.add(personAddress);
                    patient.setAddresses(personAddresses);
                }

                JSONArray identifiers = resource.getJSONArray("identifier");
                Set<PatientIdentifier> patientIds = new HashSet<>();

                for (int k = 0; k < identifiers.length(); k++) {

                    JSONObject identifier = identifiers.getJSONObject(k);
                    String system = identifier.getString("system");
                    String value = identifier.getString("value");

                    int id;
                    switch (system) {

                        case HEALTH_ID_SYSTEM:
                            id = 7;
                            break;
                        case HEALTH_FACILITY_ID_SYSTEM:
                            id = 3;
                            break;
                        case NATIONAL_ID_SYSTEM:
                            id = 6;
                            break;
                        default:
                            id = 3;
                            break;
                    }

                    patientIds.add(new PatientIdentifier(
                            value,
                            Context.getPatientService().getPatientIdentifierType(id),
                            Context.getLocationService().getDefaultLocation()));
                }
                patient.setIdentifiers(patientIds);
                return patient;
            }
        }
        return null;
    }

    public static String sendFhirRequest(Resource resource) {

        try {

            String bearer = getToken();
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer " + bearer);
            headers.add("Content-Type", "application/fhir+json");

            HttpEntity<String> request;
            if (resource != null && !resource.isEmpty()) {
                request = new HttpEntity<>(CTX.newJsonParser().encodeResourceToString(resource), headers);
            } else {
                request = new HttpEntity<>(headers);
            }

            URI urlPath = new URI(FhirClient.url + "/Patient");
            System.out.println("URL: " + urlPath);
            System.out.println("URL: " + urlPath.getPath());
            ResponseEntity<String> response = new RestTemplate().postForEntity(urlPath, request, String.class);

            return response.getBody().trim();

        } catch (Exception exc) {
            System.err.println("Failed to get Token");
            exc.printStackTrace();
        }
        return null;
    }

    public static List<PatientIdentifier> postPatient(Patient newPatient) {

        List<PatientIdentifier> patientIds = new ArrayList<>();

        try {

            String response = sendFhirRequest(newPatient);

            JSONObject root = new JSONObject(response);
            JSONArray identifiers = root.getJSONArray("identifier");

            for (int i = 0; i < identifiers.length(); i++) {

                JSONObject identifier = identifiers.getJSONObject(i);
                String system = identifier.getString("system");

                if (system.equalsIgnoreCase(HEALTH_ID_SYSTEM)) {
                    patientIds.add(new PatientIdentifier(
                            identifier.getString("value"),
                            Context.getPatientService().getPatientIdentifierTypeByName("Health ID"),//TODO: move this to global properties & use iniz to set the UUID so it is static
                            Context.getLocationService().getDefaultLocation()));
                }
            }
        } catch (Exception exc) {
            System.err.println("Failed to get Token");
            exc.printStackTrace();
        }
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
