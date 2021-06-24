package org.openmrs.module.ohricore.engine;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.api.context.Context;

import java.util.Date;
import java.util.Set;

/**
 * @author smallGod date: 24/06/2021
 */
public class String3ComputedConcept extends OHRIComputedConcept {
	
	private Log log = LogFactory.getLog(this.getClass());
	
	@Override
	public String call() throws Exception {
		
		log.info("trigger: " + super.trigger);
		System.out.println("trigger: " + super.trigger);
		if (super.trigger == EventTrigger.SAVE_ENCOUNTER) {
			
			for (Object arg : super.methodArgs) {
				
				log.info("Here.. " + arg);
				
				Encounter encounter = (Encounter) arg;
				Set<Obs> obs = encounter.getObs();
				
				String string1 = "";
				String string2 = "";
				for (Obs observation : obs) {
					
					log.info("obs.. " + observation.getValueNumeric());
					log.info("obs.. " + observation.getValueText());
					System.out.println("obs  .. " + observation.getValueNumeric());
					System.out.println("obs  .. " + observation.getValueText());
					
					Integer conceptId = observation.getConcept().getId();
					String value = observation.getValueText();
					if (value == null || value.trim().isEmpty()) {
						value = String.valueOf(observation.getValueNumeric());
					}
					
					if (conceptId == 18) { //can pass UUIDs instead (consider ENUMS for all the concepts)
						string1 = value;
					} else if (conceptId == 19) {
						string2 = value;
					}
				}
				
				System.out.println("about to save...");
				Obs string3 = new Obs();
				string3.setEncounter(encounter);
				string3.setObsDatetime(new Date());
				string3.setPerson(encounter.getPatient());
				string3.setConcept(Context.getConceptService().getConcept(20));
				string3.setValueText(string1 + " - " + string2);
				System.out.println("about to fetch location...");
				Location location = Context.getLocationService().getDefaultLocation();
				string3.setLocation(location);
				System.out.println("about to save obs...");
				Context.getObsService().saveObs(string3, "updated by Encounter interceptor");
			}
		}
		return null;
	}
}
