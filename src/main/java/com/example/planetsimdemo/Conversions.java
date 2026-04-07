package com.example.planetsimdemo;

public class Conversions {
    public static final double G = 0.00000000006674;
    public static final double massOfSun = 1.98847e30;
    public static final double EARTH_MASS = 5.9722e24;
    public static final double AU_IN_METERS = 1.496e11;

    //using meters as pixels
    public static double metersToScene(double meters) {
        return meters / 2.0e9;
    }

    public static double auToPixel(double au) {
        return au * 100.0;
    }
//km / 1500000
    public static double kmToPixel(double km) {
        return km / 1500000;
    }

    public static double auToMeters(double au) {
        return au * 150000000000.0;
    }

    public static double mToPixel(double m) {
        return m / 1500;
    }

    //takes Au
    public static double orbitalPlanetSpeed( double radiusAU) {
        return mToPixel(Math.sqrt( (G*massOfSun)/(auToMeters(radiusAU)))) ;
    }

    //takes km
    public static double orbitalMoonSpeed( double radiusKM) {
        return mToPixel(Math.sqrt( (G*massOfSun)/(radiusKM/1000))) ;
    }


}
