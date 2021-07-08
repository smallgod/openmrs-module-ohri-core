package org.openmrs.module.ohricore.impl;

import org.junit.jupiter.api.Test;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;

/**
 * @author MayanjaXL, Amos, Stephen, smallGod
 * date: 07/07/2021
 */
public class HIVStatusComputedConceptTest extends BaseContextSensitiveTest {

    protected static final String OBS_ATLEAST_ONE_POSITIVE_DATA_XML = "org/openmrs/module/ohricore/include/HIVStatusComputedConceptTest-saveEncounterInterceptorTests.xml";
    protected static final String OBS_ALL_VOIDED_DATA_XML = "org/openmrs/module/ohricore/include/HIVStatusComputedConceptTest-saveEncounterInterceptorTests.xml";
    protected static final String OBS_ONE_NEGATIVE_BEYOND_90_DAYS_DATA_XML = "org/openmrs/module/ohricore/include/HIVStatusComputedConceptTest-saveEncounterInterceptorTests.xml";
    protected static final String OBS_ONE_NEGATIVE_IN_90_DAYS_NO_POSITIVE_DATA_XML = "org/openmrs/module/ohricore/include/HIVStatusComputedConceptTest-saveEncounterInterceptorTests.xml";
    protected static final String OBS_ONE_NEGATIVE_IN_90_DAYS_PLUS_POSITIVE_DATA_XML = "org/openmrs/module/ohricore/include/HIVStatusComputedConceptTest-saveEncounterInterceptorTests.xml";
    protected static final String OBS_ONE_NEGATIVE_BEYOND_90_DAYS_PLUS_POSITIVE_DATA_XML = "org/openmrs/module/ohricore/include/HIVStatusComputedConceptTest-saveEncounterInterceptorTests.xml";
    protected static final String OBS_ONE_NEGATIVE_IN_90_DAYS_PLUS_MULTIPLE_NEGATIVE_BEYOND_90_DAYS_DATA_XML = "org/openmrs/module/ohricore/include/HIVStatusComputedConceptTest-saveEncounterInterceptorTests.xml";
    protected static final String OBS_NO_OBS_DATA_XML = "org/openmrs/module/ohricore/include/HIVStatusComputedConceptTest-saveEncounterInterceptorTests.xml";

    @Test
    void compute_withAtleastOnePositiveShouldSetPositiveHIVConcept() {
        executeDataSet(OBS_ATLEAST_ONE_POSITIVE_DATA_XML);
        //TODO: write test code here..
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
