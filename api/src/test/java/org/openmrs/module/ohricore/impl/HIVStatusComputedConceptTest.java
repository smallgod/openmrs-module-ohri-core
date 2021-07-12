package org.openmrs.module.ohricore.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.module.ohricore.api.impl.HIVStatusComputedConcept;
import org.openmrs.module.ohricore.engine.CommonsUUID;
import org.openmrs.module.ohricore.engine.HIVStatusConceptUUID;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author MayanjaXL, Amos, Stephen, smallGod date: 07/07/2021
 */
public class HIVStatusComputedConceptTest extends BaseContextSensitiveTest {
	
	protected static final String OBS_ATLEAST_ONE_POSITIVE_DATA_XML = "org/openmrs/module/ohricore/include/HIVComputedConcept-AtleastOnePositiveObs.xml";
	
	protected static final String OBS_ALL_VOIDED_DATA_XML = "org/openmrs/module/ohricore/include/BasicEncounter-initialData.xml";
	
	protected static final String OBS_ONE_NEGATIVE_BEYOND_90_DAYS_DATA_XML = "org/openmrs/module/ohricore/include/BasicEncounter-initialData.xml";
	
	protected static final String OBS_ONE_NEGATIVE_IN_90_DAYS_NO_POSITIVE_DATA_XML = "org/openmrs/module/ohricore/include/BasicEncounter-initialData.xml";
	
	protected static final String OBS_ONE_NEGATIVE_IN_90_DAYS_PLUS_POSITIVE_DATA_XML = "org/openmrs/module/ohricore/include/BasicEncounter-initialData.xml";
	
	protected static final String OBS_ONE_NEGATIVE_BEYOND_90_DAYS_PLUS_POSITIVE_DATA_XML = "org/openmrs/module/ohricore/include/BasicEncounter-initialData.xml";
	
	protected static final String OBS_ONE_NEGATIVE_IN_90_DAYS_PLUS_MULTIPLE_NEGATIVE_BEYOND_90_DAYS_DATA_XML = "org/openmrs/module/ohricore/include/BasicEncounter-initialData.xml";
	
	protected static final String OBS_NO_OBS_DATA_XML = "org/openmrs/module/ohricore/include/BasicEncounter-initialData.xml";
	
	@Resource
	private HIVStatusComputedConcept computedConcept;
	
	//@BeforeAll
	void setUp() {
	}
	
	@Test
	void compute_withAtleastOnePositiveShouldSetPositiveHIVConcept() {
		
		executeDataSet(OBS_ATLEAST_ONE_POSITIVE_DATA_XML); //TODO: cleanup the asserts, retain only one per method
		
		Concept hivStatus = Context.getConceptService().getConceptByUuid(HIVStatusConceptUUID.HIV_STATUS);
		Assertions.assertNotNull(hivStatus, "Computed HIV status Concept should exist");
		
		Encounter encounter = Context.getEncounterService().getEncounter(1);
		Assertions.assertNotNull(encounter);
		
		Obs computedObs = computedConcept.compute(encounter);
		Assertions.assertNotNull(computedObs);
		Assertions.assertEquals(computedConcept.getConcept(CommonsUUID.POSITIVE), computedObs.getValueCoded());
		
		List<Obs> hivTestObs = Context.getObsService().getObservationsByPersonAndConcept(encounter.getPatient().getPerson(),
		    hivStatus);
		Assertions.assertEquals(1, hivTestObs.size(), "Expected only one computed HIV Status obs for this patient");
		Assertions.assertEquals(computedConcept.getConcept(CommonsUUID.POSITIVE), hivTestObs.get(0).getValueCoded());
	}
	
	@Test
	void confirmPatientHasAtmostOneHIVComputedConcept() {
		
	}
	
	@Test
	void compute_withAllVoidedShouldSetUnknownConcept() {
		executeDataSet(OBS_ALL_VOIDED_DATA_XML);
	}
	
	@Test
	void compute_withOneNegativeBeyond90DaysShouldSetUnkownConcept() {
		executeDataSet(OBS_ONE_NEGATIVE_BEYOND_90_DAYS_DATA_XML);
	}
	
	@Test
	void compute_withOneNegativeIn90DaysNoPositiveShouldSetNegativeHIV() {
		executeDataSet(OBS_ONE_NEGATIVE_IN_90_DAYS_NO_POSITIVE_DATA_XML);
	}
	
	@Test
	void compute_withOneNegativeIn90DaysPlusPositiveShouldSetPositiveHIV() {
		executeDataSet(OBS_ONE_NEGATIVE_IN_90_DAYS_PLUS_POSITIVE_DATA_XML);
	}
	
	@Test
	void compute_withOneNegativeBeyond90DaysPlusPositiveShouldSetPositiveHIV() {
		executeDataSet(OBS_ONE_NEGATIVE_BEYOND_90_DAYS_PLUS_POSITIVE_DATA_XML);
	}
	
	@Test
	void compute_withOneNegativeIn90DaysPlusMultipleNegativeBeyond90DaysShouldSetNegativeHIV() {
		executeDataSet(OBS_ONE_NEGATIVE_IN_90_DAYS_PLUS_MULTIPLE_NEGATIVE_BEYOND_90_DAYS_DATA_XML);
	}
	
	@Test
	void compute_withNoObservationShouldSetUnknown() {
		executeDataSet(OBS_NO_OBS_DATA_XML);
	}
}
