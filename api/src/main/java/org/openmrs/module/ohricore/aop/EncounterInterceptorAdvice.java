package org.openmrs.module.ohricore.aop;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.module.ohricore.api.OHRIComputedConcept;
import org.openmrs.module.ohricore.api.impl.String3ComputedConcept;
import org.openmrs.module.ohricore.engine.EventTrigger;
import org.openmrs.module.ohricore.engine.OHRIComputedConceptOLD;
import org.openmrs.module.ohricore.engine.OHRIComputedConceptsFactory;
import org.springframework.aop.AfterReturningAdvice;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.Set;

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
