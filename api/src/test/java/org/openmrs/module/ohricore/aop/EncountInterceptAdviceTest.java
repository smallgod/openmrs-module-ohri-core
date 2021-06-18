package org.openmrs.module.ohricore.aop;

import org.junit.jupiter.api.Test;
import org.openmrs.Encounter;
import org.openmrs.api.EncounterService;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseContextSensitiveTest;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author smallGod date: 18/06/2021
 */
//public class EncountInterceptAdviceTest extends BaseContextSensitiveTest {
//
//	protected static final String ENC_INITIAL_DATA_XML = "org/openmrs/api/include/EncounterServiceTest-initialData.xml";
//
//	@Test
//	public void shouldSaveEncounterWithBasicDetails() {
//
//		Encounter encounter = buildEncounter();
//
//		EncounterService es = Context.getEncounterService();
//		es.saveEncounter(encounter);
//
//		assertNotNull(encounter.getEncounterId(), "The saved encounter should have an encounter id now");
//		Encounter newSavedEncounter = es.getEncounter(encounter.getEncounterId());
//		assertNotNull(newSavedEncounter, "We should get back an encounter");
//		assertTrue(encounter.equals(newSavedEncounter), "The created encounter needs to equal the pojo encounter");
//	}
//
//	private Encounter buildEncounter() {
//
//		//Create an Encounter
//		Encounter enc = new Encounter();
//		enc.setLocation(Context.getLocationService().getLocation(1));
//		enc.setEncounterType(Context.getEncounterService().getEncounterType(1));
//		enc.setEncounterDatetime(new Date());
//		enc.setPatient(Context.getPatientService().getPatient(3));
//		enc.addProvider(Context.getEncounterService().getEncounterRole(1), Context.getProviderService().getProvider(1));
//		return enc;
//	}
//}
