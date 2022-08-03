package org.openmrs.module.ohricore;

/**
 * @author smallGod date: 27/07/2022
 */
public final class OhriCoreConstant {
	
	public static final String MUTEX_QUERY_LABRESULTS = "QUERY LAB RESULTS MUTEX";
	
	public static final String FHIR_SANDBOX_URL = "https://oh-route.gicsandbox.org/fhir";
	
	public static final String FHIR_COMPLETED_OHRI_TASKS = "/Task?owner=Organization/101283&status=completed&_count=999";
	
	public static final String FHIR_REJECTED_OHRI_TASKS = "/Task?owner=Organization/101283&status=rejected&_count=999";
	
	public static final String FHIR_COMPLETED_TASKS = "/Task?status=completed";
	
	public static final String FHIR_REJECTED_TASKS_TEST = "/Task?owner=Organization/101283&status=rejected";
	
	public static final String FHIR_REJECTED_TASKS = "/Task?status=rejected";
	
	public static final String FHIR_DIAGNOSTIC_REPORT = "/DiagnosticReport/";
	
	public static final String FHIR_DIAGNOSTIC_REPORT_OBS = "/Observation/";
	
	public static final String FHIR_OBS_VL_RESULT = "VL-RESULT";
	
	public static final String CONCEPT_VL_RESULT = "856AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	public static final String CONCEPT_VL_RESULT_DETECTION = "1305AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	public static final String CONCEPT_VL_RESULT_NOT_DETECTED = "1302AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	public static final String CONCEPT_VL_RESULT_DETECTED = "1301AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	public static final String CONCEPT_VL_RESULT_DATE = "163724AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	public static final String CONCEPT_VL_SAMPLE_REJECTED = "c1ec84ed-f9df-4ea1-b58b-2381457c3848";
	
	public static final String CONCEPT_YES = "1065AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	public static final String CONCEPT_NO = "1066AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	//	public static final String ENCOUNTER = "50e0757e-050d-11ed-9289-000d3a8ce6b8";
	
	public static final String ENCOUNTER_PATIENT_ONE = "4fa7b8f6-125a-11ed-9289-000d3a8ce6b8";
	
	public static final String ENCOUNTER_PATIENT_TWO = "4fa7bf11-125a-11ed-9289-000d3a8ce6b8";
	
	public static final String CONCEPT_TEST_PATIENT_ONE = "8dce4f19-b34b-4d56-aba8-1a4c68c049bc";//Mobuto
	
	public static final String CONCEPT_TEST_PATIENT_TWO = "704c58ac-b7c2-44d2-b20e-1ff985bbadba"; //Abert Einstein
	
	public static final String OHRI_ENCOUNTER_SYSTEM_IDENTIFIER = "OHRI_ENCOUNTER_UUID";
	
}
