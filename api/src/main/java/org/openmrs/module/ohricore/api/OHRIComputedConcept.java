package org.openmrs.module.ohricore.api;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.*;
import org.openmrs.api.context.Context;
import org.openmrs.module.ohricore.engine.CommonsUUID;
import org.openmrs.module.ohricore.engine.String3ConceptUUID;

import java.util.Date;

/**
 * @author MayanjaXL, Amos, Stephen, smallGod
 * date: 28/06/2021
 */
public interface OHRIComputedConcept {

    Log log = LogFactory.getLog(OHRIComputedConcept.class);

    public Obs compute(Encounter triggeringEncounter);

    public Concept getConcept();

    public default Concept getConcept(String UUID) {
        return Context.getConceptService().getConceptByUuid(UUID);
    }

    public EncounterType getTargetEncounterType();

    public default Encounter getTargetEncounter() {
        return Context.getEncounterService().getEncounterByUuid(CommonsUUID.COMPUTED_CONCEPT_TARGET_ENCOUNTER);
    }

    public default void persist(Obs obs) {
        Context.getObsService().saveObs(obs, "updated by Encounter interceptor");
    }

    public default void computeAndPersistObs(Encounter triggeringEncounter) {
        //TODO: throw an OHRI custom exception
        Obs obs = compute(triggeringEncounter);
        if (obs != null) {
            persist(obs);
        }
    }

    public default boolean keepHistory() {
        return false;
    }

    public default boolean isTimeBased() {
        return false;
    }

    default Obs createOrUpdate(Patient patient, String targetValueCodedUUID) {

        Obs computedObs = new Obs(); //TODO: Check if an obs exists for the getConcept() and this patient -> update or create new
        computedObs.setObsDatetime(new Date());
        computedObs.setPerson(patient);
        computedObs.setConcept(getConcept());
        computedObs.setValueCoded(getConcept(targetValueCodedUUID));
        Location location = Context.getLocationService().getDefaultLocation();
        computedObs.setLocation(location);

        return computedObs;
    }

    /*
    default Obs createOrUpdate(Encounter triggeringEncounter, String string3Val) {

        Obs string3 = new Obs();
        string3.setEncounter(triggeringEncounter);
        string3.setObsDatetime(new Date());
        string3.setPerson(triggeringEncounter.getPatient());
        string3.setConcept(getConcept());
        string3.setValueText(string3Val);
        Location location = Context.getLocationService().getDefaultLocation();
        string3.setLocation(location);

        return string3;
    }
     */
}
