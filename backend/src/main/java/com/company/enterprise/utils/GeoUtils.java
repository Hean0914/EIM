package com.company.enterprise.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class GeoUtils {

    private static final double EARTH_RADIUS_METERS = 6_371_000D;

    private GeoUtils() {
    }

    public static BigDecimal distanceMeters(BigDecimal latitude1, BigDecimal longitude1,
                                            BigDecimal latitude2, BigDecimal longitude2) {
        double lat1 = Math.toRadians(latitude1.doubleValue());
        double lat2 = Math.toRadians(latitude2.doubleValue());
        double deltaLat = lat2 - lat1;
        double deltaLon = Math.toRadians(longitude2.doubleValue() - longitude1.doubleValue());
        double haversine = Math.pow(Math.sin(deltaLat / 2), 2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(deltaLon / 2), 2);
        double distance = 2 * EARTH_RADIUS_METERS * Math.atan2(Math.sqrt(haversine), Math.sqrt(1 - haversine));
        return BigDecimal.valueOf(distance).setScale(2, RoundingMode.HALF_UP);
    }
}
