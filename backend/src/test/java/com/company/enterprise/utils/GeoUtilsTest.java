package com.company.enterprise.utils;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeoUtilsTest {

    @Test
    void returnsZeroForSameCoordinate() {
        BigDecimal distance = GeoUtils.distanceMeters(
                BigDecimal.valueOf(31.2304160),
                BigDecimal.valueOf(121.4737010),
                BigDecimal.valueOf(31.2304160),
                BigDecimal.valueOf(121.4737010)
        );

        assertEquals(BigDecimal.ZERO.setScale(2), distance);
    }

    @Test
    void calculatesDistanceWithinReasonableTolerance() {
        BigDecimal distance = GeoUtils.distanceMeters(
                BigDecimal.valueOf(31.2304160),
                BigDecimal.valueOf(121.4737010),
                BigDecimal.valueOf(31.2313160),
                BigDecimal.valueOf(121.4737010)
        );

        assertTrue(distance.doubleValue() > 95);
        assertTrue(distance.doubleValue() < 105);
    }
}
