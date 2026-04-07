package com.example.planetsimdemo;

public class Conversions {
    //store these in firebase in future
    public static final double G = 0.00000000006674;
    public static final double massOfSun = 1.98847e30;
    public static final double EARTH_MASS = 5.9722e24;
    public static final double AU_IN_METERS = 1.496e11;
    public static final double Mercury_Mass = 330104e18;
    public static final double Venus_Mass = 486732e20;
    public static final double Mars_Mass = 641693000000000000000000.0;
    public static final double jupiter_Mass = 1898130000000000000000000000.0;
    public static final double Saturn_Mass = 568319000000000000000000000.0;
    public static final double Uranus_mass = 86810300000000000000000000.0;
    public static final double Neptune_Mass = 102410000000000000000000000.0;

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
