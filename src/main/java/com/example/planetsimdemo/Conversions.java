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
    public static final double Jupiter_Mass = 1898130000000000000000000000.0;
    public static final double Saturn_Mass = 568319000000000000000000000.0;
    public static final double Uranus_Mass = 86810300000000000000000000.0;
    public static final double Neptune_Mass = 102410000000000000000000000.0;
    //moons
    public static final double Moon_Mass =      73500000000000000000000.0;
    public static final double Io_Mass =        89319379731108900000000.0;
    public static final double Europa_mass =    47998438387492700000000.0;
    public static final double Ganymede_mass =  148000000000000000000000.0;
    public static final double Callisto_mass =  108000000000000000000000.0;

    //using meters as pixels
    public static double metersToScene(double meters) {
        return meters / 2.0e9;
    }
}
