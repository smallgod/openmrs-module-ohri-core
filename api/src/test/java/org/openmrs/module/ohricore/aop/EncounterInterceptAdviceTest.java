package org.openmrs.module.ohricore.aop;


import javassist.compiler.MemberResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Encounter;
import org.openmrs.api.EncounterService;
import org.openmrs.api.context.Context;
import org.openmrs.module.ohricore.engine.ConceptComputeTrigger;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;

import java.lang.reflect.Method;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author MayanjaXL, Amos, Stephen, smallGod
 * date: 18/06/2021
 */

public class EncounterInterceptAdviceTest extends BaseContextSensitiveTest {

    protected static final String ENCOUNTER_DATA_XML = "org/openmrs/module/ohricore/include/HIVStatusComputedConceptTest-saveEncounterInterceptorTests.xml";
    protected static final String TEST_ENCOUNTER_UUID = "";//TODO: add UUID
    private Method methodInvoked;

    @BeforeEach
    public void setUpObservations() throws ClassNotFoundException, NoSuchMethodException {

        executeDataSet(ENCOUNTER_DATA_XML);

        Class clazz = Class.forName("org.openmrs.api.impl.EncounterServiceImpl");
        methodInvoked = clazz.getDeclaredMethod(ConceptComputeTrigger.SAVE_ENCOUNTER, Encounter.class);
    }

    @Test
    public void afterReturning_shouldCompute() throws Throwable {

        Encounter encounter = Context.getEncounterService().getEncounterByUuid(TEST_ENCOUNTER_UUID);
        assertNotNull(encounter, "Test Encounter should be available");
        assertNotNull(methodInvoked);
        assertEquals("saveEncounter", methodInvoked.getName());
        new EncounterInterceptorAdvice().afterReturning(null, methodInvoked, new Object[]{encounter}, null);
    }
}
