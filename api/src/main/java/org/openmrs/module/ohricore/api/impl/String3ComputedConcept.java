package org.openmrs.module.ohricore.api.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.*;
import org.openmrs.api.context.Context;
import org.openmrs.module.ohricore.engine.String3ConceptUUID;
import org.openmrs.module.ohricore.api.OHRIComputedConcept;
import org.springframework.stereotype.Component;

/**
 * @author MayanjaXL, Amos, Stephen, smallGod
 * date: 28/06/2021
 */

@Component("string3ComputedConcept")
public class String3ComputedConcept implements OHRIComputedConcept {

    @Override
    public Obs compute(Encounter triggeringEncounter) {

        String string1 = null;
        String string2 = null;
        for (Obs observation : triggeringEncounter.getObs()) {

            String value = observation.getValueText();
            if (value == null || value.trim().isEmpty()) {
                value = String.valueOf(observation.getValueNumeric());
            }

            String conceptUIID = observation.getConcept().getUuid();
            if (conceptUIID.equals(String3ConceptUUID.STRING1)) {
                string1 = value;
            } else if (conceptUIID.equals(String3ConceptUUID.STRING2)) {
                string2 = value;
            }
        }

        String string3Val = null;
        if (!(string1 == null || string2 == null)) {
            string3Val = string1 + " - " + string2;
        }
        return createOrUpdate(triggeringEncounter.getPatient(), string3Val);//voids str3 if either str1 or str2 is Null
    }

    @Override
    public Concept getConcept() {
        return Context.getConceptService().getConceptByUuid(String3ConceptUUID.STRING3);
    }

    @Override
    public EncounterType getTargetEncounterType() {
        return null;
    }
}
