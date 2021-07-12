package org.openmrs.module.ohricore.aop;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.openmrs.Encounter;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.openmrs.module.ohricore.engine.ConceptComputeTrigger;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Autowired;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author MayanjaXL, Amos, Stephen, smallGod date: 18/06/2021
 */
//TODO: Request Eudson to create upstream branch: "feature/ohri-146-patient-state-tests"
@RunWith(PowerMockRunner.class)
@PrepareForTest(Context.class)
public class EncounterInterceptAdviceTest extends BaseModuleContextSensitiveTest {
	
	protected static final String ENCOUNTER_DATA_XML = "org/openmrs/module/ohricore/include/BasicEncounter-initialData.xml";
	
	protected static final String CONCEPTS_DATA_XML = "org/openmrs/module/ohricore/include/HIVComputedConcept-initConceptData.xml";
	
	protected static final String TEST_ENCOUNTER_UUID = "430bbb70-6a9c-4e1e-badb-9d1034b1b5e9";
	
	private Method methodInvoked;
	
	@Autowired
	EncounterService encounterService;
	
	@Before
	public void setUpEncounter() throws ClassNotFoundException, NoSuchMethodException {
		
		mockStatic(Context.class);
		EncounterService encounterService = mock(EncounterService.class);
		when(Context.getEncounterService()).thenReturn(encounterService);
		
		executeDataSet(ENCOUNTER_DATA_XML);
		executeDataSet(CONCEPTS_DATA_XML);
		
		Class clazz = Class.forName("org.openmrs.api.impl.EncounterServiceImpl");
		methodInvoked = clazz.getDeclaredMethod(ConceptComputeTrigger.SAVE_ENCOUNTER, Encounter.class);
	}
	
	@Test
	public void afterReturning_shouldCompute() throws Throwable {
		
		Encounter encounter = encounterService.getEncounterByUuid(TEST_ENCOUNTER_UUID);
		System.out.println("encounter: " + encounter.getUuid());
		assertNotNull(encounter, "Test Encounter should be available");
		assertNotNull(methodInvoked);
		assertEquals("saveEncounter", methodInvoked.getName());
		new EncounterInterceptorAdvice().afterReturning(null, methodInvoked, new Object[] { encounter }, null);
	}
}

//public class EncounterInterceptAdviceTest extends BaseContextSensitiveTest {
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
