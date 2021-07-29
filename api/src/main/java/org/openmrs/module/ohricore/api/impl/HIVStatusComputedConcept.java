package org.openmrs.module.ohricore.api.impl;

import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.ohricore.api.OHRIComputedConcept;
import org.openmrs.module.ohricore.engine.CommonsUUID;
import org.openmrs.module.ohricore.engine.HIVStatusConceptUUID;
import org.springframework.stereotype.Component;

import static org.openmrs.module.ohricore.engine.ComputedConceptUtil.dateWithinPeriodFromNow;

import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * @author MayanjaXL, Amos, Stephen, smallGod date: 28/06/2021
 *         <p>
 *         create HIV test date concept, check for it: 140414BBBBBBBBBBBBBBBBBBBBBBBBBBBBBB
 */
@Component("hivStatusComputedConcept")
public class HIVStatusComputedConcept implements OHRIComputedConcept {
	
	@Override
	public Obs compute(Encounter triggeringEncounter) {
		
		Concept hivFinalTestConcept = getConcept(HIVStatusConceptUUID.FINAL_HIV_TEST_RESULT);
		
		Patient patient = triggeringEncounter.getPatient();
		List<Obs> hivTestObs = Context.getObsService().getObservationsByPersonAndConcept(patient.getPerson(),
		    hivFinalTestConcept);
		
		Concept hivStatus = computeHivStatusConcept(hivTestObs, getHIVFinalTestResultDate(patient));
		return createOrUpdateObs(patient, hivStatus);
	}
	
	private Concept computeHivStatusConcept(List<Obs> hivTestObs, Date finalHivTestResultDate) {

        Supplier<Stream<Obs>> hivTestObsStream = hivTestObs::stream;
        return hivTestObsStream.get()
                .filter(obs -> obs.getValueCoded() == getConcept(CommonsUUID.POSITIVE))
                .findAny()
                .map(Obs::getValueCoded)
                .orElse(hivTestObsStream.get()
                        .filter(obs -> obs.getValueCoded() == getConcept(CommonsUUID.NEGATIVE))
                        .filter(obs -> dateWithinPeriodFromNow(finalHivTestResultDate, ChronoUnit.DAYS, -90))
                        .findAny()
                        .map(Obs::getValueCoded)
                        .orElse(getConcept(CommonsUUID.UNKNOWN))
                );
    }
	
	public Date getHIVFinalTestResultDate(Patient patient) {

        Concept hivFinalTestDateConcept = getConcept(HIVStatusConceptUUID.HIV_TEST_RESULT_DATE);

        List<Obs> hivTestObs = Context.getObsService().getObservationsByPersonAndConcept(patient.getPerson(),
                hivFinalTestDateConcept);

        Supplier<Stream<Obs>> hivTestObsStream = hivTestObs::stream;
        return hivTestObsStream.get()
                .findAny()//TODO: Might need to filter out the exact concept for this test date
                .map(Obs::getValueDate)
                .orElse(null);
    }
	
	@Override
	public Concept getConcept() {
		return Context.getConceptService().getConceptByUuid(HIVStatusConceptUUID.HIV_STATUS);
	}
}
