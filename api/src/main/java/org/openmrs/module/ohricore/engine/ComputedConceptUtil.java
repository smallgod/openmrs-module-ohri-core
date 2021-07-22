package org.openmrs.module.ohricore.engine;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * @author MayanjaXL, Amos, Stephen, smallGod date: 21/07/2021
 */
public final class ComputedConceptUtil {

    /**
     * Check if a date falls within a given period/duration
     *
     * @param valueDate    the date to check if it falls within a certain period/duration
     * @param periodUnit   Can be hours, days, years etc
     * @param periodAmount Period amount is negative if in the past or Positive if in the
     *                     future
     * @return true if given date is within duration, otherwise false
     */
    public static boolean valueDateWithinPeriod(Date valueDate, ChronoUnit periodUnit, int periodAmount) {

        LocalDateTime targetDateTime = LocalDateTime.now().plus(periodAmount, periodUnit);
        LocalDateTime obsValueDate = LocalDateTime.ofInstant(valueDate.toInstant(), ZoneId.systemDefault());
        return obsValueDate.isAfter(targetDateTime);
    }
}
