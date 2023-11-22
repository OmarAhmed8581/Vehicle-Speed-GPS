package com.example.meleor;

import java.util.Date;

public class VehicleSpeed {

    private static final double EARTH_RADIUS = 6371.0; // Earth radius in kilometers

    public static double calculateSpeed(double previousLatitude, double previousLongitude, double latitude, double longitude, long previousTime,long currentTime) {
//        long currentTime = new Date().getTime();
        double deltaTime = (currentTime - previousTime) / 1000.0; // Time difference in seconds
        // Convert latitude and longitude to radians
        double lat1 = Math.toRadians(previousLatitude);
        double lon1 = Math.toRadians(previousLongitude);
        double lat2 = Math.toRadians(latitude);
        double lon2 = Math.toRadians(longitude);

        // Calculate distance using Haversine formula
        double deltaLat = lat2 - lat1;
        double deltaLon = lon2 - lon1;
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = EARTH_RADIUS * c;

        // Calculate speed by dividing distance by time
        double speed = distance / deltaTime;
        return speed;
    }
}
