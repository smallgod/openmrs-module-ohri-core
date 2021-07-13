package org.openmrs.module.ohricore.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ObsService;
import org.openmrs.module.ohricore.aop.EncounterInterceptorAdvice;
import org.openmrs.module.ohricore.api.impl.HIVStatusComputedConcept;
import org.openmrs.module.ohricore.engine.CommonsUUID;
import org.openmrs.module.ohricore.engine.ConceptComputeTrigger;
import org.openmrs.module.ohricore.engine.HIVStatusConceptUUID;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.List;

/**
 * @author MayanjaXL, Amos, Stephen, smallGod
 * date: 07/07/2021
 */
public class HIVStatusComputedConceptTest extends BaseModuleContextSensitiveTest {

    protected static final String OHRI_INIT_XML_TEST_DATASET_PATH = "org/openmrs/module/ohricore/include/OHRI-StandardDataset.xml";

    protected static final String OHRI_HIV_STATUS_ATLEAST_ONE_POSITIVE_XML_TEST_DATASET_PATH = "org/openmrs/module/ohricore/include/OHRI-AtleastOnePositiveObsDataset.xml";

    protected static final String OHRI_HIV_STATUS_ATLEAST_ONE_NEGATIVE_BEYOND_90DAYS_XML_TEST_DATASET_PATH = "org/openmrs/module/ohricore/include/OHRI-OneNegativeObsBeyond90DaysDataset.xml";

    protected static final String OHRI_HIV_STATUS_ATLEAST_ONE_NEGATIVE_WITHIN_90DAYS_XML_TEST_DATASET_PATH = "org/openmrs/module/ohricore/include/OHRI-OneNegativeObsWithin90DaysDataset.xml";

    protected static final String OHRI_HIV_STATUS_ONE_NEGATIVE_WITHIN_90DAYS_PLUS_POSITIVE_XML_TEST_DATASET_PATH = "org/openmrs/module/ohricore/include/OHRI-OneNegativeObsWithin90DaysPlusPositiveDataset.xml";

    protected static final String OHRI_HIV_STATUS_NO_FINAL_HIV_TEST_RESULT_XML_TEST_DATASET_PATH = "org/openmrs/module/ohricore/include/OHRI-NoFinalHIVTestObsDataset.xml";

    @Resource
    private HIVStatusComputedConcept computedConcept;

    @Autowired
    private EncounterService encounterService;

    @Autowired
    private ConceptService conceptService;

    @Autowired
    private ObsService obsService;

    private Method methodInvoked;

    @Before
    public void initialiseCommonTestData() throws ClassNotFoundException, NoSuchMethodException {

        executeDataSet(OHRI_INIT_XML_TEST_DATASET_PATH);

        Class clazz = Class.forName("org.openmrs.api.impl.EncounterServiceImpl");
        methodInvoked = clazz.getDeclaredMethod(ConceptComputeTrigger.SAVE_ENCOUNTER, Encounter.class);
    }

    @Test
    public void compute_withAtleastOnePositiveHIVObsShouldBePositive() throws Throwable {

        executeDataSet(OHRI_HIV_STATUS_ATLEAST_ONE_POSITIVE_XML_TEST_DATASET_PATH);

        List<Obs> hivTestObs = computeComputedConceptHelper();

        Assertions.assertEquals(computedConcept.getConcept(CommonsUUID.POSITIVE), hivTestObs.get(0).getValueCoded());
    }

    @Test
    public void compute_withOneNegativeBeyond90DaysShouldSetUnkownConcept() throws Throwable {

        executeDataSet(OHRI_HIV_STATUS_ATLEAST_ONE_NEGATIVE_BEYOND_90DAYS_XML_TEST_DATASET_PATH);

        List<Obs> hivTestObs = computeComputedConceptHelper();

        Assertions.assertEquals(computedConcept.getConcept(CommonsUUID.UNKNOWN), hivTestObs.get(0).getValueCoded());
    }

    @Test
    public void compute_withOneNegativeIn90DaysNoPositiveShouldSetNegativeHIV() throws Throwable {

        executeDataSet(OHRI_HIV_STATUS_ATLEAST_ONE_NEGATIVE_WITHIN_90DAYS_XML_TEST_DATASET_PATH);

        List<Obs> hivTestObs = computeComputedConceptHelper();

        Assertions.assertEquals(computedConcept.getConcept(CommonsUUID.NEGATIVE), hivTestObs.get(0).getValueCoded());
    }

    @Test
    public void compute_withOneNegativeIn90DaysPlusPositiveShouldSetPositiveHIV() throws Throwable {

        executeDataSet(OHRI_HIV_STATUS_ONE_NEGATIVE_WITHIN_90DAYS_PLUS_POSITIVE_XML_TEST_DATASET_PATH);

        List<Obs> hivTestObs = computeComputedConceptHelper();

        Assertions.assertEquals(computedConcept.getConcept(CommonsUUID.POSITIVE), hivTestObs.get(0).getValueCoded());
    }

    @Test
    public void compute_withOneNegativeIn90DaysPlusMultipleNegativeBeyond90DaysShouldSetNegativeHIV() throws Throwable {

        executeDataSet(OHRI_HIV_STATUS_ATLEAST_ONE_NEGATIVE_WITHIN_90DAYS_XML_TEST_DATASET_PATH);

        List<Obs> hivTestObs = computeComputedConceptHelper();

        Assertions.assertEquals(computedConcept.getConcept(CommonsUUID.NEGATIVE), hivTestObs.get(0).getValueCoded());
    }

    @Test
    public void compute_withNoFinalTestHIVObsShouldSetUnknown() throws Throwable {

        executeDataSet(OHRI_HIV_STATUS_NO_FINAL_HIV_TEST_RESULT_XML_TEST_DATASET_PATH);

        List<Obs> hivTestObs = computeComputedConceptHelper();

        Assertions.assertEquals(computedConcept.getConcept(CommonsUUID.UNKNOWN), hivTestObs.get(0).getValueCoded());
    }

    @Test
    public void compute_withAllVoidedShouldSetUnknownConcept() {
        //executeDataSet(OBS_ALL_VOIDED_DATA_XML);
    }

    @Test
    public void compute_withPositiveObsVoidedShouldSetUnknownConcept() {
        //executeDataSet(OBS_ALL_VOIDED_DATA_XML);
    }

    @Test
    public void compute_withNegativeObsIn90DaysVoidedShouldSetUnknownConcept() {
        //executeDataSet(OBS_ALL_VOIDED_DATA_XML);
    }

    List<Obs> computeComputedConceptHelper() throws Throwable {

        Encounter encounter = encounterService.getEncounter(100);
        new EncounterInterceptorAdvice().afterReturning(null, methodInvoked, new Object[]{encounter}, null);

        Concept hivStatus = conceptService.getConceptByUuid(HIVStatusConceptUUID.HIV_STATUS);
        List<Obs> hivTestObs = obsService.getObservationsByPersonAndConcept(encounter.getPatient().getPerson(), hivStatus);

        Assertions.assertEquals(1, hivTestObs.size(), "Expected only one computed HIV Status obs for this patient");

        return hivTestObs;
    }
}
