package org.openmrs.module.ohricore.api;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.ohricore.engine.CommonsUUID;

import java.util.Date;
import java.util.List;

/**
 * @author MayanjaXL, Amos, Stephen, smallGod
 * date: 28/06/2021
 */
public interface OHRIComputedConcept {

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

    default Obs createOrUpdate(Patient patient, Concept targetConcept) {

        //TODO: Check if an obs exists for the getConcept() and this patient -> update or create new
        Obs computedObs = new Obs();
        computedObs.setObsDatetime(new Date());
        computedObs.setPerson(patient);
        computedObs.setConcept(getConcept());
        computedObs.setValueCoded(targetConcept);
        Location location = Context.getLocationService().getDefaultLocation();
        computedObs.setLocation(location);

        return computedObs;
    }

    default List<Obs> getObs(Patient patient, Concept obsConcept) {

        return Context.getObsService()
                .getObservationsByPersonAndConcept(patient.getPerson(), obsConcept);
    }
}
