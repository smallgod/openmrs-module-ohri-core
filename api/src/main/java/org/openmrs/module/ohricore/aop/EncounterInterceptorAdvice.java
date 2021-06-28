package org.openmrs.module.ohricore.aop;

import org.openmrs.Encounter;
import org.openmrs.module.ohricore.api.OHRIComputedConcept;
import org.openmrs.module.ohricore.engine.EventTrigger;
import org.openmrs.module.ohricore.engine.OHRIComputedConceptsFactory;
import org.springframework.aop.AfterReturningAdvice;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @author smallGod date: 16/06/2021
 */
public class EncounterInterceptorAdvice implements AfterReturningAdvice {
	
	@Override
	public void afterReturning(Object returnValue, Method methodInvoked, Object[] methodArgs, Object target)
	        throws Throwable {
		
		// if the method is save encounter, retrieve that encounter
		// and parse it to ComputedConcept interface
		if (methodInvoked.getName().equals(EventTrigger.SAVE_ENCOUNTER)) {
			List<OHRIComputedConcept> ohriComputedConcepts = OHRIComputedConceptsFactory.getComputedConcepts();
			for (Object arg : methodArgs) {
				Encounter encounter = (Encounter) arg;
				for (OHRIComputedConcept computedConcept : ohriComputedConcepts) {
					computedConcept.computeAndPersistObs(encounter);
				}
			}
		}
	}
}
