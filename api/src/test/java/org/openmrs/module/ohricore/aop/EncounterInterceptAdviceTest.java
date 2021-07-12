package org.openmrs.module.ohricore.aop;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Encounter;
import org.openmrs.api.EncounterService;
import org.openmrs.module.ohricore.engine.ConceptComputeTrigger;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Method;

/**
 * @author MayanjaXL, Amos, Stephen, smallGod date: 18/06/2021
 */

public class EncounterInterceptAdviceTest extends BaseModuleContextSensitiveTest {

    protected static final String OHRI_XML_TEST_DATASET_PATH = "org/openmrs/module/ohricore/include/OHRI-StandardDataset.xml";

    protected static final String TEST_ENCOUNTER_UUID = "6519d653-393b-4118-9c83-a3715b82d4ac";

    private Method methodInvoked;

    @Autowired
    private EncounterService encounterService;

    @Before
    public void setUpEncounter() throws ClassNotFoundException, NoSuchMethodException {

        executeDataSet(OHRI_XML_TEST_DATASET_PATH);

        Class clazz = Class.forName("org.openmrs.api.impl.EncounterServiceImpl");
        methodInvoked = clazz.getDeclaredMethod(ConceptComputeTrigger.SAVE_ENCOUNTER, Encounter.class);

    }

    @Test
    public void afterReturning_shouldCompute() throws Throwable {

        Encounter encounter = encounterService.getEncounterByUuid(TEST_ENCOUNTER_UUID);

        assertNotNull(encounter, "Encounter cannot be NULL");
        assertNotNull(methodInvoked);
        assertEquals("saveEncounter", methodInvoked.getName());
        //new EncounterInterceptorAdvice().afterReturning(null, methodInvoked, new Object[] { encounter }, null);
    }
}
