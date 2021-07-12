package org.openmrs.module.ohricore.aop;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Encounter;
import org.openmrs.api.EncounterService;
import org.openmrs.module.ohricore.engine.ConceptComputeTrigger;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author MayanjaXL, Amos, Stephen, smallGod
 * date: 18/06/2021
 */

public class EncounterInterceptAdviceTest extends BaseModuleContextSensitiveTest {

    protected static final String ENCOUNTER_DATA_XML = "org/openmrs/module/ohricore/include/BasicEncounter-initialData.xml";

    protected static final String CONCEPTS_DATA_XML = "org/openmrs/module/ohricore/include/HIVComputedConcept-initConceptData.xml";

    protected static final String TEST_ENCOUNTER_UUID = "430bbb70-6a9c-4e1e-badb-9d1034b1b5e9";

    private Method methodInvoked;

    @Autowired
    private EncounterService encounterService;

    @Before
    public void setUpEncounter() throws ClassNotFoundException, NoSuchMethodException {

        executeDataSet(ENCOUNTER_DATA_XML);
        executeDataSet(CONCEPTS_DATA_XML);

        Class clazz = Class.forName("org.openmrs.api.impl.EncounterServiceImpl");
        methodInvoked = clazz.getDeclaredMethod(ConceptComputeTrigger.SAVE_ENCOUNTER, Encounter.class);
    }

    @Test
    public void afterReturning_shouldCompute() throws Throwable {

        Encounter encounter = encounterService.getEncounterByUuid(TEST_ENCOUNTER_UUID);
        assertNotNull(encounter, "Test Encounter should be available");
        assertNotNull(methodInvoked);
        assertEquals("saveEncounter", methodInvoked.getName());
        new EncounterInterceptorAdvice().afterReturning(null, methodInvoked, new Object[]{encounter}, null);
    }
}
