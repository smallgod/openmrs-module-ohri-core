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
	
	public static final String FHIR_SERVICE_REQUEST = "/ServiceRequest/";
	
	public static final String FHIR_REJECTED_TASKS_TEST = "/Task?owner=Organization/101283&status=rejected";
	
	public static final String FHIR_REJECTED_TASKS = "/Task?status=rejected";
	
	public static final String FHIR_DIAGNOSTIC_REPORT = "/DiagnosticReport/";
	
	public static final String FHIR_DIAGNOSTIC_REPORT_OBS = "/Observation/";
	
	public static final String FHIR_OBS_VL_RESULT = "VL-MOST-RECENT-TEST-RESULT";
	
	public static final String FHIR_OBS_COVID_RESULT = "COVID-MOST-RECENT-TEST-RESULT";
	
	public static final String FHIR_OBS_COVID_RESULT_PCR = "94745-7";
	
	public static final String FHIR_OBS_COVID_RESULT_ANTIGEN = "94558-4";
	
	public static final String CONCEPT_VL_RESULT = "856AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	public static final String CONCEPT_VL_RESULT_DETECTION = "1305AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	public static final String CONCEPT_VL_RESULT_NOT_DETECTED = "1302AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	public static final String CONCEPT_VL_RESULT_DETECTED = "1301AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	public static final String CONCEPT_VL_RESULT_DATE = "163724AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	public static final String CONCEPT_DATE_TEST_COMPLETED = "163724AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	public static final String CONCEPT_TEST_SAMPLE_REJECTED = "c1ec84ed-f9df-4ea1-b58b-2381457c3848";
	
	public static final String CONCEPT_YES = "1065AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	public static final String CONCEPT_NO = "1066AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	
	public static final String CONCEPT_TRUE = "cf82933b-3f3f-45e7-a5ab-5d31aaee3da3";
	
	public static final String CONCEPT_FALSE = "488b58ff-64f5-4f8a-8979-fa79940b1594";
	
	//	public static final String ENCOUNTER = "50e0757e-050d-11ed-9289-000d3a8ce6b8";
	
	public static final String ENCOUNTER_PATIENT_ONE = "4fa7b8f6-125a-11ed-9289-000d3a8ce6b8";
	
	public static final String ENCOUNTER_PATIENT_TWO = "4fa7bf11-125a-11ed-9289-000d3a8ce6b8";
	
	public static final String CONCEPT_TEST_PATIENT_ONE = "8dce4f19-b34b-4d56-aba8-1a4c68c049bc";//Mobuto
	
	public static final String CONCEPT_TEST_PATIENT_TWO = "704c58ac-b7c2-44d2-b20e-1ff985bbadba"; //Abert Einstein
	
	public static final String OHRI_ENCOUNTER_SYSTEM = "OHRI_ENCOUNTER_UUID";
	
	public static final String GP_PARENT_SERVER_URL = "ohricore.fhir.sandbox.url";
	
	public static final String GP_PARENT_SERVER_USERNAME = "";
	
	public static final String CONCEPT_MAPPING_CIEL = "CIEL";
	
	public static final String CONCEPT_MAPPING_LOINC = "LOINC";
	
	public static final String CONCEPT_MAPPING_SNOMED_CT = "SNOMED-CT";
	
	public static final String CONCEPT_MAPPING_OCT = "OCT";
	
	public static final String CONCEPT_CODE_LAB_ORDER_STATUS = "LabOrderStatus";//oct
	
	public static final String CONCEPT_CODE_LAB_ORDER_STATUS_COMPLETED = "255594003";//snomed-ct
	
	public static final String CONCEPT_CODE_LAB_ORDER_STATUS_CANCELLED = "89925002";//snomed-ct
	
	public static final String CONCEPT_CODE_LAB_ORDER_STATUS_NOT_DONE = "385660001";//snomed-ct
	
	public static final String CONCEPT_CODE_LAB_TEST_PERFORMED = "LabTestDone";
	
	public static final String CONCEPT_CODE_LAB_ORDER_STATUS_NOT_DONE_REASON = "165182";
	
	public static final String CONCEPT_CODE_ANTIGEN = "94558-4";
	
	public static final String CONCEPT_CODE_PCR = "94745-7";
	
	public static final String CONCEPT_COVID_RESULT_ANTIGEN = "cbcbb029-f11f-4437-9d53-1d0f0a170433";
	
	public static final String CONCEPT_COVID_RESULT_PCR = "3f4ee14b-b4ab-4597-9fe9-406883b63d76";
	
	public static final String CONCEPT_CODE_POSITIVE = "10828004";
	
	public static final String CONCEPT_CODE_NEGATIVE = "260385009";
	
	public static final String CONCEPT_CODE_INCONCLUSIVE = "419984006"; //165649
	
	public static final String GP_PARENT_SERVER_PASSWORD = "";
	
	//test requested - 069f6dfe-88c1-4a45-a894-0d99549c8718
}
