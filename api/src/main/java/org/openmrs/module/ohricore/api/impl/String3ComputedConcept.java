package org.openmrs.module.ohricore.api.impl;

import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.ohricore.api.OHRIComputedConcept;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Set;

/**
 * @author smallGod
 * date: 28/06/2021
 */
@Component
public class String3ComputedConcept implements OHRIComputedConcept {

    @Override
    public Obs computeObs(Encounter encounter) {

        Set<Obs> obs = encounter.getObs();

        String string1 = "";
        String string2 = "";
        for (Obs observation : obs) {
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
        Obs string3 = new Obs();
        string3.setEncounter(encounter);
        string3.setObsDatetime(new Date());
        string3.setPerson(encounter.getPatient());
        string3.setConcept(Context.getConceptService().getConcept(20));
        string3.setValueText(string1 + " - " + string2);
        Location location = Context.getLocationService().getDefaultLocation();
        string3.setLocation(location);

        return string3;
    }

    @Override
    public void persistObs(Obs obs) {
        Context.getObsService().saveObs(obs, "updated by Encounter interceptor");
    }
}
