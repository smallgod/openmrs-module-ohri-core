package org.openmrs.module.ohricore.api;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.ohricore.engine.CommonsUUID;

import java.util.Date;
import java.util.List;

/**
 * @author MayanjaXL, Amos, Stephen, smallGod
 * date: 28/06/2021
 */
public interface OHRIComputedConcept {

    Log log = LogFactory.getLog(OHRIComputedConcept.class);

    public org.openmrs.Obs compute(org.openmrs.Encounter triggeringEncounter);

    public org.openmrs.Concept getConcept();

    public default org.openmrs.Concept getConcept(String UUID) {
        return Context.getConceptService().getConceptByUuid(UUID);
    }

    public org.openmrs.EncounterType getTargetEncounterType();

    public default org.openmrs.Encounter getTargetEncounter() {
        return Context.getEncounterService().getEncounterByUuid(CommonsUUID.COMPUTED_CONCEPT_TARGET_ENCOUNTER);
    }

    public default void persist(org.openmrs.Obs obs) {
        Context.getObsService().saveObs(obs, "updated by Encounter interceptor");
    }

    public default void computeAndPersistObs(org.openmrs.Encounter triggeringEncounter) {
        //TODO: throw an OHRI custom exception
        org.openmrs.Obs obs = compute(triggeringEncounter);
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

    default org.openmrs.Obs createOrUpdate(org.openmrs.Patient patient, org.openmrs.Concept targetConcept) {

        //TODO: Check if an obs exists for the getConcept() and this patient -> update or create new
        org.openmrs.Obs computedObs = new org.openmrs.Obs();
        computedObs.setObsDatetime(new Date());
        computedObs.setPerson(patient);
        computedObs.setConcept(getConcept());
        computedObs.setValueCoded(targetConcept);
        org.openmrs.Location location = Context.getLocationService().getDefaultLocation();
        computedObs.setLocation(location);

        return computedObs;
    }

    default List<org.openmrs.Obs> getObs(org.openmrs.Patient patient, org.openmrs.Concept obsConcept) {

        return Context.getObsService()
                .getObservationsByPersonAndConcept(patient.getPerson(), obsConcept);
    }
}
