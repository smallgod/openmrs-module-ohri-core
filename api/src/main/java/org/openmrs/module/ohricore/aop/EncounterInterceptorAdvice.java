package org.openmrs.module.ohricore.aop;

import org.openmrs.Encounter;
import org.openmrs.module.ohricore.api.OHRIComputedObservation;
import org.openmrs.module.ohricore.engine.ObservationComputeTrigger;
import org.openmrs.module.ohricore.engine.OHRIComputedObservationFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.AfterReturningAdvice;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @author MayanjaXL, Amos, Stephen, smallGod date: 16/06/2021
 */
public class EncounterInterceptorAdvice implements AfterReturningAdvice {
	
	private static final Logger log = LoggerFactory.getLogger(EncounterInterceptorAdvice.class);
	
	@Override
	public void afterReturning(Object returnValue, Method methodInvoked, Object[] methodArgs, Object target)
	        throws Throwable {
		
		try {
			if (methodInvoked.getName().equals(ObservationComputeTrigger.SAVE_ENCOUNTER)) {
				for (Object arg : methodArgs) {
					if (arg instanceof Encounter) {
						Encounter encounter = (Encounter) arg;
						List<OHRIComputedObservation> ohriComputedObservations = OHRIComputedObservationFactory
						        .getComputedObservations(encounter);
						for (OHRIComputedObservation computedConcept : ohriComputedObservations) {
							computedConcept.computeAndPersistObs(encounter);
						}
					}
				}
			}
		}
		catch (Exception e) {
			log.error("An un-expected Error occurred while computing for a computed concept");
		}
	}
}
