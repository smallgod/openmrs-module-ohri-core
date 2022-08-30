package org.openmrs.module.ohricore.fhir;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.AdditionalRequestHeadersInterceptor;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.DiagnosticReport;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.Task;
import org.openmrs.api.context.Context;
import org.openmrs.module.ohricore.OhriCoreConstant;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Base64;

import static org.openmrs.module.ohricore.OhriCoreConstant.FHIR_OBS_VL_RESULT;
import static org.openmrs.module.ohricore.OhriCoreConstant.OHRI_ENCOUNTER_SYSTEM;

/**
 * @author Arthur D. Mugume, Amos date: 18/08/2022
 */
public class FhirClient {
	
	static FhirContext CTX = FhirContext.forR4();
	
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
	
	public static String postFhirResource(Resource resource) throws Exception {
		
		return getClient().create().resource(resource).prettyPrint().encodedJson().execute().getOperationOutcome()
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
