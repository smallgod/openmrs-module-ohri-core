package org.openmrs.module.ohricore.fhir;

import ca.uhn.fhir.context.FhirContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.ResourceType;
import org.json.JSONObject;
import org.openmrs.PatientIdentifier;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.fhir2.api.translators.PatientTranslator;
import org.openmrs.module.fhir2.api.translators.impl.PatientTranslatorImpl;
import org.openmrs.module.ohricore.OhriCoreConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * @author smallGod date: 08/12/2022
 */
@Component
@Transactional
public class MPIFhirClientUtil {
	
	// interface IMpiFhirClient getToken() freeTextSearch(), postPatient()
	// DefaultMpiFhirClientImpl
	//

	@Autowired
	private PatientTranslator patientTranslator;
	
	private static final String url = Context.getAdministrationService().getGlobalProperty(
	    OhriCoreConstant.GP_MPI_SERVER_URL);
	
	private static final String clientId = Context.getAdministrationService().getGlobalProperty(
	    OhriCoreConstant.GP_MPI_CLIENT_ID);
	
	private static final String clientSecret = Context.getAdministrationService().getGlobalProperty(
	    OhriCoreConstant.GP_MPI_CLIENT_SECRET);
	
	private static final String SANTE_AUTH_URL = url + "/auth";
	
	private static final String SANTE_PATIENT_URL = url + "/Patient";
	
	private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
	
	private static final FhirContext CTX = FhirContext.forR4();
	
	private static ObjectMapper mapper;
	
	static {
		MPIFhirClientUtil.mapper = new ObjectMapper();
		MPIFhirClientUtil.mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
	}

	public Set<PatientIdentifier> registerPatient(org.openmrs.Patient patient) {
		
		Map<String, String> headers = new HTTPHeadersBuilder().addBearerToken(getToken()).getHeaders();
		
		String response = remoteRequest(SANTE_PATIENT_URL, HttpMethod.POST, headers);
		System.out.println("response: " + response);
		
		Bundle fhirPatient = convertFhirBundleResource(response);
		org.openmrs.Patient registeredPatient = retrievePatient(fhirPatient);
		
		return registeredPatient.getIdentifiers();
	}
	
	public org.openmrs.Patient getPatientByIdentifier(String identifier) {
		
		Map<String, String> headers = new HTTPHeadersBuilder().addBearerToken(getToken()).getHeaders();
		
		String response = remoteRequest(SANTE_PATIENT_URL + "/" + identifier, HttpMethod.GET, headers);
		System.out.println("response: " + response);
		
		Bundle fhirPatient = convertFhirBundleResource(response);
		return retrievePatient(fhirPatient);
	}
	
	public List<org.openmrs.Patient> getPatientByFreeText(String searchText) {
		
		Map<String, String> headers = new HTTPHeadersBuilder().addBearerToken(getToken()).getHeaders();
		
		String response = remoteRequest(SANTE_PATIENT_URL + "?freetext=" + searchText, HttpMethod.GET, headers);
		System.out.println("response: " + response);
		
		Bundle fhirPatient = convertFhirBundleResource(response);
		return retrievePatients(fhirPatient);
	}
	
	public List<org.openmrs.Patient> retrievePatients(Bundle fhirPatientBundle) {

        List<org.openmrs.Patient> patients = new ArrayList<>();

        List<Bundle.BundleEntryComponent> entries = fhirPatientBundle.getEntry();
        for (Bundle.BundleEntryComponent entry : entries) {

            Resource resource = entry.getResource();
            if (resource.getResourceType() == ResourceType.Patient) {
                patients.add(patientTranslator.toOpenmrsType((Patient) resource));
            }
        }
        return patients;
    }
	
	public org.openmrs.Patient retrievePatient(Bundle fhirPatientBundle) {
		
		List<Bundle.BundleEntryComponent> entries = fhirPatientBundle.getEntry();
		for (Bundle.BundleEntryComponent entry : entries) {
			
			Resource resource = entry.getResource();
			if (resource.getResourceType() == ResourceType.Patient) {
				return patientTranslator.toOpenmrsType((Patient) resource);
			}
			break;
		}
		return null;
	}
	
	public String getToken() {
		
		Map<String, String> headers = new HTTPHeadersBuilder().addClientCredentials(clientId, clientSecret,
		    "client_credentials").getHeaders();
		
		String response = remoteRequest(SANTE_AUTH_URL, HttpMethod.POST, headers);
		return new JSONObject(response).getString("access_token");
	}
	
	public String remoteRequest(String url, HttpMethod method, Map<String, String> headerProperties) {
		return remoteRequest(url, method, headerProperties, null);
	}
	
	public String remoteRequest(String url,
                                HttpMethod method,
                                Map<String, String> headerProperties,
                                String bodyContent) throws APIException {

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        for (Map.Entry<String, String> property : headerProperties.entrySet()) {
            headers.add(property.getKey(), property.getValue());
        }

        HttpEntity<String> entity = new HttpEntity<>(bodyContent, headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(url, method, entity, String.class);

        if (!HttpStatus.OK.equals(response.getStatusCode())) {
            handleError(response.getStatusCode());
        }
        return response.getBody().trim();
    }
	
	public static Bundle convertFhirBundleResource(String jsonFhirResource) {
		return (Bundle) CTX.newJsonParser().parseResource(jsonFhirResource);
	}
	
	public static Patient convertFhirPatientResource(String jsonFhirResource) {
		return (Patient) CTX.newJsonParser().parseResource(jsonFhirResource);
	}
	
	public static String convertFhirResource(Resource fhirResource) {
		return CTX.newJsonParser().encodeResourceToString(fhirResource);
	}
	
	private static final class HTTPHeadersBuilder {
		
		private final Map<String, String> properties;
		
		private HTTPHeadersBuilder() {
            this.properties = new HashMap<>();
        }
		
		private HTTPHeadersBuilder addClientCredentials(String clientId, String clientSecret, String grantType) {
			
			properties.put("client_id", clientId);
			properties.put("client_secret", clientSecret);
			properties.put("grant_type", grantType);
			return this;
		}
		
		private HTTPHeadersBuilder addBearerToken(String bearer) {
			properties.put("Authorization", "Bearer " + bearer);
			return this;
		}
		
		private HTTPHeadersBuilder addContentType(String contentType) {
			properties.put("Content-Type", contentType);
			return this;
		}
		
		private HTTPHeadersBuilder addContentTypeAccepted(String contentTypeAccepted) {
			properties.put("Accept", contentTypeAccepted);
			return this;
		}
		
		private Map<String, String> getHeaders() {
			return properties;
		}
	}
	
	private void handleError(HttpStatus status) {
		
		if (status == HttpStatus.FORBIDDEN) {
			throw new APIException("Error, forbidden request ");
		} else {
			throw new APIException("Error, on remote request " + status.value());
		}
	}
}
