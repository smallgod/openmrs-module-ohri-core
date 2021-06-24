package org.openmrs.module.ohricore.aop;

import org.openmrs.module.ohricore.engine.EventTrigger;
import org.openmrs.module.ohricore.engine.OHRIComputedConcept;
import org.springframework.aop.AfterReturningAdvice;

import java.lang.reflect.Method;

/**
 * @author smallGod date: 16/06/2021
 */
public class EncounterInterceptorAdvice implements AfterReturningAdvice {
	
	@Override
	public void afterReturning(Object returnValue, Method methodInvoked, Object[] methodArgs, Object target)
	        throws Throwable {
		
		EventTrigger trigger = EventTrigger.convertToEnum(methodInvoked.getName());
		
		final OHRIComputedConcept processorInitialiser = new OHRIComputedConcept() {};
		processorInitialiser.moveToNext(-1, returnValue, methodInvoked, methodArgs, target, trigger);
	}
}
