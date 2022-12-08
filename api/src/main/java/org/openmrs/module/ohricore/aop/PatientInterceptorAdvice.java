package org.openmrs.module.ohricore.aop;

import org.openmrs.PatientIdentifier;
import org.openmrs.api.context.Context;
import org.openmrs.module.ohricore.engine.ConceptComputeTrigger;
import org.openmrs.module.ohricore.fhir.MPIFhirClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.AfterReturningAdvice;

import java.lang.reflect.Method;
import java.util.Set;

/**
 * @author smallGod
 * @date 30/11/2022
 */
public class PatientInterceptorAdvice implements AfterReturningAdvice {
	
	private static final Logger log = LoggerFactory.getLogger(PatientInterceptorAdvice.class);
	
	@Override
	public void afterReturning(Object returnValue, Method methodInvoked, Object[] methodArgs, Object target)
	        throws Throwable {
		
		System.out.println("After returning Patient Save in EMR");
		try {
			if (methodInvoked.getName().equals(ConceptComputeTrigger.SAVE_PATIENT)) {
				
				System.out.println("Save Patient method()...");
				
				for (Object arg : methodArgs) {
					if (arg instanceof org.openmrs.Patient) {
						
						org.openmrs.Patient patient = (org.openmrs.Patient) arg;
						MPIFhirClientUtil fhirClient = new MPIFhirClientUtil();
						Set<PatientIdentifier> identifiers = fhirClient.registerPatient(patient);
						
						for (PatientIdentifier patientId : identifiers) {
							patientId.setPatient(patient);
							Context.getPatientService().savePatientIdentifier(patientId);
						}
					}
				}
			}
		}
		catch (Exception e) {
			log.error("An un-expected Error occurred while registering a patient: " + e.getMessage());
			e.printStackTrace();
		}
	}
}

//check if health id exists on MPI (send if doesn't exist) otherwise don't
// -> add global property for health ID
// expose endpoint for search
