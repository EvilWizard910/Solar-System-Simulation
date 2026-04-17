package com.example.planetsimdemo;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import java.util.HashMap;
import java.util.Map;

import java.util.ArrayList;
import java.util.List;

import static com.example.planetsimdemo.Conversions.*;

public class SolarSystem {
    private final Group root = new Group();
    private final List<Body> bodies = new ArrayList<>();
    private  Body saturnBody;
    private Cylinder ring;
    private Rotate ringSpin = new Rotate(0, Rotate.Y_AXIS);
    private static final double niceScale = 0.00003;
    private static final double realScale = 0.0000005;
    //Radii for spheres in km
    double sunRadius = 700000;
    double mercuryRadius= 2439.7;
    double venusRadius=6051.8;
    double earthRadius = 6371;
    double marsRadius=3389.5;
    double jupiterRadius=69911;
    double saturnRadius=58232;
    double uranusRadius=25362;
    double neptuneRadius=24622;
    //moons
    double moonRadius = 1737.4;
    double ioRadius = 1821.6;
    double europaRadius = 1560.8;
    double ganymedeRadius =2631.2;
    double callistoRadius=2410.3;

    private final Map<Body, Double> baseBodyRadii = new HashMap<>();
    private double baseRingRadius;
    private double baseRingHeight;




    public SolarSystem() {
        createBodies();
    }


    private void createBodies() {
        //creates the sun :)
        Sphere sunView = new Sphere(sunRadius);
        sunView.setMaterial(new PhongMaterial(Color.YELLOW));
        Body sun = new Body(
                "Sun",
                Conversions.massOfSun,
                sunView,
                0, 0, 0,
                0, 0, 0
        );

        double earthDistance = Conversions.AU_IN_METERS;
        double earthSpeed = Math.sqrt(Conversions.G * Conversions.massOfSun / earthDistance);
        //enter earth scale
        Sphere earthView = new Sphere(earthRadius);
        earthView.setMaterial(new PhongMaterial(Color.DODGERBLUE));
       //EARTH!!!!
        Body earth = new Body(
                "Earth",
                Conversions.EARTH_MASS,
                earthView,
                earthDistance, 0, 0,
                0, 0, earthSpeed
        );

        //added mercury
        double mercuryDistance = Conversions.AU_IN_METERS*0.39;
        double mercurySpeed = Math.sqrt(Conversions.G * Conversions.massOfSun / mercuryDistance);
        Sphere mercuryView = new Sphere(mercuryRadius);
        mercuryView.setMaterial(new PhongMaterial(Color.MISTYROSE));
        Body mercury = new Body(
                "Mercury",
                    Conversions.Mercury_Mass,
                mercuryView,
                mercuryDistance, 0, 0, 0, 0, mercurySpeed
        );

        double venusDistance = Conversions.AU_IN_METERS*0.72;
        double venusSpeed = Math.sqrt(Conversions.G * Conversions.massOfSun / venusDistance);
        Sphere venusView = new Sphere(venusRadius);
        venusView.setMaterial(new PhongMaterial(Color.BURLYWOOD));
        Body venus = new Body(
                "Venus",
                Conversions.Venus_Mass,
                venusView,
                venusDistance, 0, 0, 0, 0, venusSpeed
        );

        double marsDistance = Conversions.AU_IN_METERS*1.52;
        double marsSpeed = Math.sqrt(Conversions.G * Conversions.massOfSun / marsDistance);
        Sphere marsView = new Sphere(marsRadius);
        marsView.setMaterial(new PhongMaterial(Color.ORANGERED));
        Body  mars = new Body(
                "Mars",
                Conversions.Mars_Mass,
                marsView,
                marsDistance, 0, 0, 0, 0, marsSpeed
        );


        double jupiterDistance = Conversions.AU_IN_METERS*5.2;
        double jupiterSpeed = Math.sqrt(Conversions.G * Conversions.massOfSun / jupiterDistance);
        Sphere jupiterView = new Sphere(jupiterRadius);
        jupiterView.setMaterial(new PhongMaterial(Color.CORAL));
        Body jupiter = new Body(
                "Jupiter",
                Conversions.Jupiter_Mass,
                jupiterView,
                jupiterDistance, 0, 0, 0, 0, jupiterSpeed
        );

        double saturnDistance = Conversions.AU_IN_METERS*9.54;
        double saturnSpeed = Math.sqrt(Conversions.G * Conversions.massOfSun / saturnDistance);
        Sphere saturnView = new Sphere(saturnRadius);
        saturnView.setMaterial(new PhongMaterial(Color.DARKSALMON));
        Body saturn = new Body(
                "Saturn",
                Conversions.Saturn_Mass,
                saturnView,
                saturnDistance, 0, 0, 0, 0, saturnSpeed
        );

        double uranusDistance = Conversions.AU_IN_METERS*19.2;
        double uranusSpeed = Math.sqrt(Conversions.G * Conversions.massOfSun / uranusDistance);
        Sphere uranusView = new Sphere(uranusRadius);
        uranusView.setMaterial(new PhongMaterial(Color.DARKTURQUOISE));
        Body uranus = new Body(
                "Uranus",
                Conversions.Uranus_Mass,
                uranusView,
                uranusDistance, 0, 0, 0, 0, uranusSpeed
        );

        double neptuneDistance = Conversions.AU_IN_METERS*30.02;
        double neptuneSpeed = Math.sqrt(Conversions.G * Conversions.massOfSun / neptuneDistance);
        Sphere neptuneView = new Sphere(neptuneRadius);
        neptuneView.setMaterial(new PhongMaterial(Color.MIDNIGHTBLUE));
        Body neptune = new Body(
                "Neptune",
                Conversions.Neptune_Mass,
                neptuneView,
                neptuneDistance, 0, 0, 0, 0, neptuneSpeed
        );

        //add moons
        double moonDistance=  0.0025695*AU_IN_METERS;
        double moonSpeed =  Math.sqrt(Conversions.G * Conversions.EARTH_MASS / moonDistance);
        Sphere moonView = new Sphere(moonRadius);
        moonView.setMaterial(new PhongMaterial(Color.LIGHTGRAY));
        Body moon = new Body(
                "Moon",
                Moon_Mass,
                moonView,
                (earthDistance+moonDistance), 0, 0, 0, 0, earthSpeed+moonSpeed
        );

        double ioDistance=  421800000.0;
        double ioSpeed =  Math.sqrt(Conversions.G * Jupiter_Mass / ioDistance);
        Sphere ioView = new Sphere(ioRadius);
        ioView.setMaterial(new PhongMaterial(Color.LIMEGREEN));
        Body io = new Body(
                "Io",
                Io_Mass,
                ioView,
                (jupiterDistance+ioDistance), 0, 0, 0, 0, jupiterSpeed+ioSpeed
        );

        double europaDistance=  671100000.0;
        double europaSpeed =  Math.sqrt(Conversions.G * Jupiter_Mass / europaDistance);
        Sphere europaView = new Sphere(europaRadius);
        europaView.setMaterial(new PhongMaterial(Color.WHITE));
        Body europa = new Body(
                "Europa",
                Europa_mass,
                europaView,
                (jupiterDistance+europaDistance), 0, 0, 0, 0, jupiterSpeed+europaSpeed
        );
        double ganymedeDistance=  1070400000.0;
        double ganymedeSpeed =  Math.sqrt(Conversions.G * Jupiter_Mass / ganymedeDistance);
        Sphere ganymedeView = new Sphere(ganymedeRadius);
        europaView.setMaterial(new PhongMaterial(Color.GRAY));
        Body ganymede = new Body(
                "Ganymede",
                Ganymede_mass,
                ganymedeView,
                (jupiterDistance+ganymedeDistance), 0, 0, 0, 0, jupiterSpeed+ganymedeSpeed
        );
        double callistoDistance=  1882700000.0;
        double callistoSpeed =  Math.sqrt(Conversions.G * Jupiter_Mass / callistoDistance);
        Sphere callistoView = new Sphere(europaRadius);
        callistoView.setMaterial(new PhongMaterial(Color.DARKGRAY));
        Body callisto = new Body(
                "Callisto",
                Callisto_mass,
                callistoView,
                (jupiterDistance+callistoDistance), 0, 0, 0, 0, jupiterSpeed+callistoSpeed
        );


         saturnBody = saturn;
        double aWidth = 480000+(saturnRadius);
        double aThickness=30;
        ring = new Cylinder(saturnRadius+aWidth,aThickness);
        ring.setMaterial(new PhongMaterial(Color.rgb(255, 228, 196,0.1)));
        ring.setRotationAxis(Rotate.X_AXIS);
        ring.setRotate(90);

        ring.getTransforms().addAll(
        new Rotate(90,Rotate.X_AXIS),
        new Rotate(27,Rotate.Z_AXIS), ringSpin
        );


        //add all bodies:Sun, 8 planets, a number of moons
        bodies.add(sun);
        bodies.add(earth);
        bodies.add(mercury);
        bodies.add(venus);
        bodies.add(mars);
        bodies.add(jupiter);
        bodies.add(saturn);
        bodies.add(uranus);
        bodies.add(neptune);
        //moons
        bodies.add(moon);
        bodies.add(io);
        bodies.add(europa);
        bodies.add(callisto);
        bodies.add(ganymede);

        root.getChildren().add(sunView);
        root.getChildren().add(earthView);
        root.getChildren().add(mercuryView);
        root.getChildren().add(venusView);
        root.getChildren().add(marsView);
        root.getChildren().add(jupiterView);
        root.getChildren().add(saturnView);
        root.getChildren().add(uranusView);
        root.getChildren().add(neptuneView);
        //moons
        root.getChildren().add(moonView);
        root.getChildren().add(ioView);
        root.getChildren().add(europaView);
        root.getChildren().add(callistoView);
        root.getChildren().add(ganymedeView);


        //saturns rings
        root.getChildren().add(ring);

        cacheBaseSizes();
        renderBodies();
    }


    //places the body in the proper x, y and z coordinates
    //also keeps the ring aligned w/ Saturn and keeps it faced towards the sun
    private void renderBodies() {
        for (Body body : bodies) {
            body.getView().setTranslateX(Conversions.metersToScene(body.getX()));
            body.getView().setTranslateY(Conversions.metersToScene(body.getY()));
            body.getView().setTranslateZ(Conversions.metersToScene(body.getZ()));
        }
        if (saturnBody != null && ring != null) {
            ring.setTranslateX(Conversions.metersToScene(saturnBody.getX()));
            ring.setTranslateY(Conversions.metersToScene(saturnBody.getY()));
            ring.setTranslateZ(Conversions.metersToScene(saturnBody.getZ()));
        }
        ringSpin.setAngle(ringSpin.getAngle() + 1);
    }


    //resets acceleration and recalculates it every time signature using verlet integration
    public void updatePhysics(double dt) {
        if (Double.isNaN(dt) || Double.isInfinite(dt) || dt <= 0) {
            System.out.println("Bad dt: " + dt);
        }
       resetAllAcceleration();
       applyAllGravity();

       double[] axOld = new double[bodies.size()];
       double[] ayOld = new double[bodies.size()];
       double[] azOld = new double[bodies.size()];

      for (int i =0; i<bodies.size(); i++) {
          Body body = bodies.get(i);
          axOld[i] = body.getAx();
          ayOld[i] = body.getAy();
          azOld[i] = body.getAz();
      }

       for (Body body : bodies) {
            body.updatePosition(dt);
        }
       resetAllAcceleration();
       applyAllGravity();

        for (int i = 0; i < bodies.size(); i++) {
          Body body = bodies.get(i);
          body.updateVelocity(dt, axOld[i], ayOld[i], azOld[i]);
        }
        renderBodies();
    }

    private void resetAllAcceleration() {
        for(Body body : bodies){
            body.resetAcceleration();
        }
    }

    // calculates gravitation pull of each body on one another
    private void applyAllGravity() {
        for (int i = 0; i<bodies.size(); i++) {
            for (int j = i+1; j < bodies.size(); j++) {
                applyGravity(bodies.get(i), bodies.get(j));
            }
        }
    }

    //Uses Newtonian physics to calculate gravity
    private void applyGravity(Body a, Body b) {
        double dx = b.getX() - a.getX();
        double dy = b.getY() - a.getY();
        double dz = b.getZ() - a.getZ();

        double distSq = dx * dx + dy * dy + dz * dz;
        double dist = Math.sqrt(distSq);

        if (dist < 1.0) {
            return;
        }

        double accelA = Conversions.G * b.getMass() / distSq;
        double accelB = Conversions.G * a.getMass() / distSq;

        double nx = dx / dist;
        double ny = dy / dist;
        double nz = dz / dist;

        a.addAcceleration(accelA * nx, accelA * ny, accelA * nz);
        b.addAcceleration(-accelB * nx, -accelB * ny, -accelB * nz);
    }
    public Group getRoot() {
        return root;
    }


     //stores original size of bodies
    private void cacheBaseSizes() {
        for (Body body : bodies) {
            baseBodyRadii.put(body, body.getView().getRadius());
        }
        baseRingRadius = ring.getRadius();
        baseRingHeight = ring.getHeight();
    }

    //multiplies the size of the bodies, not the distance in between them
    public void setViewScale(double sliderValue){
        double clampedValue = Math.max(0.0, Math.min(1.0,sliderValue));
        double planetMultiplier = realScale  + ((niceScale*2) - realScale)* clampedValue;

        // Sun scales less than the planets.
        double sunSliderValue = Math.pow(clampedValue, 2.8);
        double sunMultiplier = realScale
                + ((niceScale*0.6) - realScale) * sunSliderValue;


        ring.setRadius(baseRingRadius * planetMultiplier);
        ring.setHeight(baseRingHeight * planetMultiplier);

        for (Body body : bodies) {
            double baseRadius = baseBodyRadii.get(body);

            if ("Sun".equals(body.getName())) {
                body.getView().setRadius(baseRadius * sunMultiplier);
            } else {
                body.getView().setRadius(baseRadius * planetMultiplier);
            }
        }
    }
 }
