package org.openmrs.module.ohricore.api.impl;

import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.ohricore.api.OHRIComputedConcept;
import org.openmrs.module.ohricore.engine.CommonsUUID;
import org.openmrs.module.ohricore.engine.HIVStatusConceptUUID;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * @author MayanjaXL, Amos, Stephen, smallGod date: 28/06/2021
 */
@Component("hivStatusComputedConcept")
public class HIVStatusComputedConcept implements OHRIComputedConcept {
	
	@Override
	public Obs compute(Encounter triggeringEncounter) {
		
		Concept hivFinalTestConcept = getConcept(HIVStatusConceptUUID.FINAL_HIV_TEST_RESULT);
		
		Patient patient = triggeringEncounter.getPatient();
		List<Obs> hivTestObs = Context.getObsService().getObservationsByPersonAndConcept(patient.getPerson(),
		    hivFinalTestConcept);
		
		Concept hivStatus = computeHivStatusConcept(hivTestObs);
		return createOrUpdateObs(patient, hivStatus);
	}
	
	private Concept computeHivStatusConcept(List<Obs> hivTestObs) {

        Supplier<Stream<Obs>> hivTestObsStream  = hivTestObs::stream;
        return hivTestObsStream.get()
                .filter(obs -> obs.getValueCoded() == getConcept(CommonsUUID.POSITIVE))
                .findAny()
                .map(Obs::getValueCoded)
                .orElse(hivTestObsStream.get()
                        .filter(obs -> obs.getValueCoded() == getConcept(CommonsUUID.NEGATIVE))
                        .filter(obs -> valueDateIsWithin90Days(obs.getValueDate()))
                        .findAny()
                        .map(Obs::getValueCoded)
                        .orElse(getConcept(CommonsUUID.UNKNOWN))
                );
    }
	
	@Override
	public EncounterType getTargetEncounterType() {
		return null;
	}
	
	@Override
	public Concept getConcept() {
		return Context.getConceptService().getConceptByUuid(HIVStatusConceptUUID.HIV_STATUS);
	}
	
	private boolean valueDateIsWithin90Days(Date valueDate) {
		
		LocalDateTime date90DaysAgo = LocalDateTime.now().minusDays(90);
		LocalDateTime obsValueDate = LocalDateTime.ofInstant(valueDate.toInstant(), ZoneId.systemDefault());
		return obsValueDate.isAfter(date90DaysAgo);
	}
}
