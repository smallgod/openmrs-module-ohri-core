package org.openmrs.module.ohricore.aop;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.openmrs.*;
import org.openmrs.api.context.Context;
import org.springframework.aop.AfterReturningAdvice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @author smallGod date: 16/06/2021
 */
public class EncounterInterceptorAdvice implements AfterReturningAdvice {

    private static final Logger log = LoggerFactory.getLogger(EncounterInterceptorAdvice.class);

    @Override
    public void afterReturning(Object o, Method method, Object[] objects, Object o1) throws Throwable {

        if (method.getName().equals("saveEncounter")) {

            for (Object object : objects) {

                Encounter encounter = (Encounter) object;
                Set<Obs> obs = encounter.getObs();

                String string1 = "";
                String string2 = "";
                for (Obs observation : obs) {

                    Integer conceptId = observation.getConcept().getId();
                    String value = observation.getValueText();
                    if (value == null || value.trim().isEmpty()) {
                        value = String.valueOf(observation.getValueNumeric());
                    }

                    if (conceptId == 18) { //can pass UUIDs instead
                        string1 = value;
                    } else if (conceptId == 19) {
                        string2 = value;
                    }
                }

                Obs string3 = new Obs();
                string3.setEncounter(encounter);
                string3.setObsDatetime(new Date());
                string3.setPerson(encounter.getPatient());
                string3.setConcept(Context.getConceptService().getConcept(20));
                string3.setValueText(string1 + " - " + string2);
                Location location = Context.getLocationService().getDefaultLocation();
                string3.setLocation(location);
                Context.getObsService().saveObs(string3, "updated by Encounter interceptor");
            }
        }
    }
	
	/*	@Override
		public Object invoke(MethodInvocation invocation) throws Throwable {

			Method method = invocation.getMethod();
			String name = method.getName();

			System.out.println("EncounterInterceptorAdvice: " + method.getName());

			Class<?>[] types = method.getParameterTypes();
			Object[] values = invocation.getArguments();

			if (name.equals("saveEncounter")) {

				for (int x = 0; x < types.length; x++) {

					Encounter encounter = (Encounter) values[x];
					Set<Obs> obs = encounter.getObs();

					System.out.println("Saving Encounter.. [" + encounter + "]");

					String string1 = "";
					String string2 = "";
					for (Obs observation : obs) {

						Collection<ConceptAnswer> answers = observation.getConcept().getAnswers();
						System.out.println("Observation name: " + x + ": " + observation.getConcept().getName().getName());
						System.out.println("Observation valT: " + observation.getValueText());
						System.out.println("Observation valN: " + observation.getValueNumeric());

						Integer conceptId = observation.getConcept().getId();
						String value = observation.getValueText();
						if (value == null || value.trim().isEmpty()) {
							value = String.valueOf(observation.getValueNumeric());
						}

						if (conceptId == 18) {
							string1 = value;
						} else if (conceptId == 19) {
							string2 = value;
						}
					}

					Obs string3 = new Obs();
					string3.setEncounter(encounter);
					string3.setObsDatetime(new Date());
					string3.setPerson(encounter.getPatient());
					string3.setConcept(Context.getConceptService().getConcept(20));
					string3.setValueText(string1 + " - " + string2);
					Location location = Context.getLocationService().getDefaultLocation();
					System.out.println("Location: " + location.getName());
					string3.setLocation(location);
					Context.getObsService().saveObs(string3, "updated by Encounter interceptor");
				}

			}
	 */
}
