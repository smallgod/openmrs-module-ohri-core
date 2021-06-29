package org.openmrs.module.ohricore.aop;

import org.openmrs.Encounter;
import org.openmrs.module.ohricore.api.OHRIComputedConcept;
import org.openmrs.module.ohricore.engine.EventTrigger;
import org.openmrs.module.ohricore.engine.OHRIComputedConceptsFactory;
import org.springframework.aop.AfterReturningAdvice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @author MayanjaXL, Amos, Stephen, smallGod date: 16/06/2021
 */
public class EncounterInterceptorAdvice implements AfterReturningAdvice {
	
	@Autowired
	@Qualifier("string3ComputedConcept")
	OHRIComputedConcept string3ComputedConcept;
	
	@Autowired
	@Qualifier("hivStatusComputedConcept")
	OHRIComputedConcept hivStatusComputedConcept;
	
	@Override
	public void afterReturning(Object returnValue, Method methodInvoked, Object[] methodArgs, Object target)
	        throws Throwable {
		
		if (methodInvoked.getName().equals(EventTrigger.SAVE_ENCOUNTER.getValue())) {
			for (Object arg : methodArgs) {
				if (arg instanceof Encounter) {
					Encounter encounter = (Encounter) arg;
					//string3ComputedConcept.computeAndPersistObs(encounter);
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
