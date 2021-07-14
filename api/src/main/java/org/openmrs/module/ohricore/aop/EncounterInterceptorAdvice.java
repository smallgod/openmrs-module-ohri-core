package org.openmrs.module.ohricore.aop;

import org.openmrs.Encounter;
import org.openmrs.module.ohricore.api.OHRIComputedConcept;
import org.openmrs.module.ohricore.engine.ConceptComputeTrigger;
import org.openmrs.module.ohricore.engine.OHRIComputedConceptsFactory;
import org.springframework.aop.AfterReturningAdvice;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @author MayanjaXL, Amos, Stephen, smallGod date: 16/06/2021
 */
public class EncounterInterceptorAdvice implements AfterReturningAdvice {
	
	@Override
	public void afterReturning(Object returnValue, Method methodInvoked, Object[] methodArgs, Object target)
	        throws Throwable {
		
		if (methodInvoked.getName().equals(ConceptComputeTrigger.SAVE_ENCOUNTER)) {
			for (Object arg : methodArgs) {
				if (arg instanceof Encounter) {
					Encounter encounter = (Encounter) arg;
					List<OHRIComputedConcept> ohriComputedConcepts = OHRIComputedConceptsFactory
					        .getComputedConcepts(encounter);
					for (OHRIComputedConcept computedConcept : ohriComputedConcepts) {
						computedConcept.computeAndPersistObs(encounter);
					}
				}
			}
		}
	}
}
