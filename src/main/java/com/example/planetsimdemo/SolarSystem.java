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
    private   Body saturnBody;
    private Cylinder ring;
    private Rotate ringSpin = new Rotate(0, Rotate.Y_AXIS);
    private double scale=0.00001 ;
    double newScale = 0.0000005/scale;
    private static final double niceScale = 0.00003;
    private static final double realScale = 0.0000005;
    // 0.0000005 for realistic, 0.00001
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
                (earthDistance+moonDistance+earthRadius), 0, 0, 0, 0, earthSpeed+moonSpeed
        );

        double ioDistance=  421800000.0;
        double ioSpeed =  Math.sqrt(Conversions.G * Jupiter_Mass / ioDistance);
        Sphere ioView = new Sphere(ioRadius);
        ioView.setMaterial(new PhongMaterial(Color.LIMEGREEN));
        Body io = new Body(
                "Io",
                Io_Mass,
                ioView,
                (jupiterDistance+ioDistance+ioRadius+jupiterRadius), 0, 0, 0, 0, jupiterSpeed+ioSpeed
        );

         saturnBody = saturn;
        double aWidth = 480000*scale+(saturnRadius);
        double aThickness=30*scale;
        ring = new Cylinder(saturnRadius+aWidth,aThickness);
        ring.setMaterial(new PhongMaterial(Color.BISQUE));
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


    //resets acceleration and recalculates it every time signature
    public void updatePhysics(double dt) {
        for (Body body : bodies) {
            body.resetAcceleration();
        }

        for (int i = 0; i < bodies.size(); i++) {
            for (int j = i + 1; j < bodies.size(); j++) {
                applyGravity(bodies.get(i), bodies.get(j));
            }
        }

        for (Body body : bodies) {
            body.integrate(dt);
        }
        renderBodies();
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
        double currentScale = realScale * clampedValue;

        double multiplier = realScale
                + (niceScale - realScale) * clampedValue;

        ring.setRadius(baseRingRadius * multiplier);
        ring.setHeight(baseRingHeight * multiplier);

        for (Body body : bodies) {
            double baseRadius = baseBodyRadii.get(body);
            body.getView().setRadius(baseRadius * multiplier);
        }
    }
 }


























/*public class SolarSystem {


    private final List<Planet> planets = new ArrayList<>();

    private final Group root = new Group();

    public SolarSystem() {
        createSun();
        createPlanets();
    }

    private void createSun() {
        Sphere sun = new Sphere(kmToPixel(1391400)/2);

        PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(Color.YELLOW);
        material.setSpecularColor(Color.WHITE);

        sun.setMaterial(material);
        root.getChildren().add(sun);
    }


    private void createPlanets() {
//size = diameter in KM
//Orbit radius = distance from sun in au
        Planet mercury = new Planet(4879,.39, Color.SADDLEBROWN);
        Planet venus = new Planet(12104, .72, Color.TAN);
        Planet earth = new Planet(12756, 1,  Color.BLUE);
        Planet mars  = new Planet(6792, 1.52,  Color.RED);
        Planet jupiter = new Planet(142984, 5.2,  Color.ORANGE);
        Planet saturn = new Planet(120536, 9.54,  Color.BEIGE);
        Planet uranus = new Planet(51118, 19.2,  Color.TURQUOISE);
        Planet neptune = new Planet(49528, 30.06,  Color.DARKBLUE);

       //takes size in km and radius in km
        Moon moon = new Moon(earth, 3480, 384000,  Color.LIGHTGRAY);

        Moon io =new Moon(jupiter,1821.6,422000, Color.YELLOWGREEN);
        Moon europa =new Moon(jupiter,1560.8,671000, Color.WHITE);
        Moon ganymede =new Moon(jupiter,2631,1070000, Color.DARKSLATEGRAY);
        Moon callisto =new Moon(jupiter,2410,1833000, Color.GRAY);

        Moon titan =new Moon(saturn, 5150, 1221830, Color.WHEAT);
        Moon rhea =new Moon(saturn, 1528, 527040, Color.WHEAT);
        Moon iapetus =new Moon(saturn, 1436, 3561300, Color.WHEAT);
        Moon dione =new Moon(saturn, 1120, 377400, Color.WHEAT);
        Moon tethys =new Moon(saturn, 1061, 294660, Color.WHEAT);
        Moon enceladus =new Moon(saturn, 504, 238020, Color.WHEAT);
        Moon mimas =new Moon(saturn, 396, 185520, Color.WHEAT);

        planets.add(mercury);
        planets.add(venus);
        planets.add(earth);
        planets.add(mars);
        planets.add(jupiter);
        planets.add(saturn);
        planets.add(uranus);
        planets.add(neptune);

        earth.addMoon(moon);

        jupiter.addMoon(io);
        jupiter.addMoon(europa);
        jupiter.addMoon(ganymede);
        jupiter.addMoon(callisto);

        saturn.addMoon(titan);
        saturn.addMoon(rhea);
        saturn.addMoon(iapetus);
        saturn.addMoon(dione);
        saturn.addMoon(tethys);
        saturn.addMoon(enceladus);
        saturn.addMoon(mimas);



        for (Planet p : planets) {
            root.getChildren().add(createOrbitRing(p.getOrbitRadius()));
            root.getChildren().add(p.getBody());

            // Add moons
            for (Moon m : p.getMoons()) {
                root.getChildren().add(createOrbitRingAroundPlanet(m));
                root.getChildren().add(m.getBody());
            }
        }
    }

    // Create thin circular orbit ring
    private Group createOrbitRing(double radius) {
        Group ring = new Group();

        int segments = 100; // more = smoother circle
        double thickness = 0.5;
        if (radius > 200) {
             thickness = 4;
        }
        for (int i = 0; i < segments; i++) {
            double angle1 = 2 * Math.PI * i / segments;
            double angle2 = 2 * Math.PI * (i + 1) / segments;

            double x1 = radius * Math.cos(angle1);
            double z1 = radius * Math.sin(angle1);

            double x2 = radius * Math.cos(angle2);
            double z2 = radius * Math.sin(angle2);

            double dx = x2 - x1;
            double dz = z2 - z1;
            double length = Math.sqrt(dx * dx + dz * dz);

            Box segment = new Box(length, thickness, thickness);

            // position midpoint
            segment.setTranslateX((x1 + x2) / 2);
            segment.setTranslateZ((z1 + z2) / 2);

            // rotate segment to match angle
            double angleDeg = Math.toDegrees(Math.atan2(dz, dx));
            segment.setRotationAxis(Rotate.Y_AXIS);
            segment.setRotate(-angleDeg);

            // material (subtle gray)
            PhongMaterial mat = new PhongMaterial(Color.rgb(180, 180, 180, 0.3));
            segment.setMaterial(mat);

            ring.getChildren().add(segment);
        }

        return ring;
    }

    private Group createOrbitRingAroundPlanet(Moon moon) {
        Group ring = createOrbitRing(moon.getOrbitRadius());

        // Bind ring to PLANET position (not moon)
        ring.translateXProperty().bind(
                moon.getParent().getBody().translateXProperty()
        );

        ring.translateZProperty().bind(
                moon.getParent().getBody().translateZProperty()
        );

        return ring;
    }

    public void update() {
        for (Planet p : planets) {
            p.update();
        }
    }

    public Group getRoot() {
        return root;
    }
}
  */