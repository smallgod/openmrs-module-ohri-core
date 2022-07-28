package org.openmrs.module.ohricore;

/**
 * @author smallGod date: 27/07/2022
 */
public final class OhriCoreConstant {
	
	public static final String MUTEX_QUERY_LABRESULTS = "QUERY LAB RESULTS MUTEX";
	
	public static final String FHIR_SANDBOX_URL = "https://oh-route.gicsandbox.org/fhir";
	
	public static final String FHIR_COMPLETED_TASKS = "/Task?status=completed";
	
	public static final String FHIR_DIAGNOSTIC_REPORT = "/DiagnosticReport/";
	
	public static final String FHIR_DIAGNOSTIC_REPORT_OBS = "/Observation/";
	
	public static final String FHIR_HIV_RECENCY_TEST_CONDUCTED = "HIV-RECENCY-TEST-CONDUCTED";
	
	public static final String CONCEPT_HIV_RECENCY_TEST = "4fe5857e-c804-41cf-b3c9-0acc1f516ab7";
	
	public static final String CONCEPT_TEST_PATIENT = "8dce4f19-b34b-4d56-aba8-1a4c68c049bc";
	
}
