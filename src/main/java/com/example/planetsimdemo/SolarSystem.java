package com.example.planetsimdemo;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import java.util.Comparator;


import java.util.*;

import static com.example.planetsimdemo.Conversions.*;


public class SolarSystem {

    /*private static final String TYPE_STAR = "Star";
    private static final String TYPE_PLANET = "Planet";
    private static final String TYPE_MOON = "Moon";*/

    private final Group root = new Group();


    private double currentViewScale = 0.0;

    private static class BodyState {
        String name;
        String type;
        String parent;
        double mass;
        double radiusKm;
        Color color;
        OrbitElements orbit;

        BodyState(String name, String type, String parent,
                  double mass, double radiusKm, Color color, OrbitElements orbit) {
            this.name = name;
            this.type = type;
            this.parent = parent;
            this.mass = mass;
            this.radiusKm = radiusKm;
            this.color = color;
            this.orbit = orbit;
        }
    }

    /*
    The semi major axis is the maxiumum distance an orbiting body will be from it's center of orbit
    * eccentricity defines how circular or how much an oval the orbit is, 0 is a circle, higher eccentricity makes an oval
    * inclination refers to the tilt of an objects orbit using Earths orbit as 0 degrees, so 90 or 270 degress would be a perpendicular orbit to earth
    * ascending node is the degree in an orbit where an inclined orbit heads above Earths orbit(the reference frame for this scenario)
    * argument of periapsis is the angle between the ascending node and the point where the orbiting body is closest to its center relative to its center
    * true anomaly is a degree used to tell where a body is in its orbit
    */





    public SolarSystem(){this(defaultInitialConditions());}

    public SolarSystem(Collection<InitialCondition> initialConditions){
        init(initialConditions);
    }

    public static double toSceneRadiusFromKm(String bodyName, double radiusKm) {
        return Conversions.metersToScene(radiusKm * 1000.0);
    }

    private static double sceneRadiusToKm(double sceneRadius) {
        return sceneRadius * 2.0e9 / 1000.0;
    }



    private double normalizeAngle(double angleDeg) {
        double a = angleDeg % 360.0;
        return a < 0 ? a + 360.0 : a;
    }

    private double bodyScaleMultiplier() {
        return 1.0 + currentViewScale * 50.0;
    }

    private void applyScaleToBody(Body body) {
        double base = baseRadii.get(body);
        body.getView().setRadius(base * bodyScaleMultiplier());
    }

    private Color extractColor(Body body) {
        if (body != null && body.getView().getMaterial() instanceof PhongMaterial material) {
            Color c = material.getDiffuseColor();
            if (c != null) return c;
        }
        return Color.WHITE;
    }




    private List<BodyState> getChildStates(String parentName) {
        List<BodyState> children = new ArrayList<>();
        for (String name : map.keySet()) {
            if (parentName.equals(orbitParents.get(name))) {
                Body body = map.get(name);
                children.add(new BodyState(
                        name,
                        bodyTypes.get(name),
                        orbitParents.get(name),
                        body.getMass(),
                        logicalRadiiKm.getOrDefault(name,0.0),
                        bodyColors.getOrDefault(name, extractColor(body)),
                        orbitElements.get(name)
                ));
            }
        }
        return children;
    }
    private Body createStar(String name, double mass, double radiusKm ) {
        Sphere sphere = new Sphere(toSceneRadiusFromKm(name,radiusKm));
        return new Body(name,mass,sphere,0,0,0,0,0,0);
    }
    private Body createStar(String name, double mass, double radiusKm, OrbitElements orbit) {
        if (orbit == null) {
            return createStar(name, mass, radiusKm);
        }
        return createOrbitingBody(name, mass, radiusKm, massOfSun, orbit);
    }









     /*
        make("Io", TYPE_MOON, "Jupiter", Io_Mass, 1821.6, 421800000.0 / AU_IN_METERS, 0.0, Color.LIMEGREEN);
        make("Europa", TYPE_MOON, "Jupiter", Europa_mass, 1560.8, 671100000.0 / AU_IN_METERS, 45.0, Color.WHITE);
        make("Ganymede", TYPE_MOON, "Jupiter", Ganymede_mass, 2631.2, 1070400000.0 / AU_IN_METERS, 90.0, Color.GRAY);
        make("Callisto", TYPE_MOON, "Jupiter", Callisto_mass, 2410.3, 1882700000.0 / AU_IN_METERS, 135.0, Color.DARKGRAY);
    */



    private void addInitialCondition(InitialCondition condition){
        String normalizedType = normalizeType(condition.type());
        String parentName = TYPE_MOON.equals(normalizedType) ? condition.parent() : null;

        Body body;
        if (TYPE_STAR.equals(normalizedType)){
            body=createStar(condition.name(), condition.mass(), condition.radiusKm(),condition.orbit());
        }else if (TYPE_MOON.equals(normalizedType)){
            body = createMoon(condition.name(), condition.mass(), condition.radiusKm(), parentName,condition.orbit());
        }else {
            body=createOrbitingBody(condition.name(), condition.mass(),condition.radiusKm(),massOfSun,condition.orbit());
        }
        if (body == null) {
            throw new IllegalArgumentException("Unable to create body: "+condition.name());
        }
        registerBody(body,normalizedType,parentName,condition.color() == null ? Color.WHITE : condition.color(), condition.orbit());
    }

    /*public static List<InitialCondition> defaultInitialConditions() {
        List<InitialCondition> defaults = new ArrayList<>();

        defaults.add(new InitialCondition("Sun", "Star", null, massOfSun, 700000, Color.YELLOW, null));
        defaults.add(new InitialCondition("Mercury", "Planet", null, Mercury_Mass, 2439.7, Color.MISTYROSE,
                new OrbitElements(0.3870993, 0.20564, 7.005, 48.3, 29.13, 193)));
        defaults.add(new InitialCondition("Venus", "Planet", null, Venus_Mass, 6051.8, Color.BURLYWOOD,
                new OrbitElements(0.7233336, 0.00678, 3.3947, 76.7, 54.9, 125)));
        defaults.add(new InitialCondition("Earth", "Planet", null, EARTH_MASS, 6371, Color.DODGERBLUE,
                new OrbitElements(1.0000, 0.0167, 0.00005, -11.26064, 114.20783, 100.0)));
        defaults.add(new InitialCondition("Mars", "Planet", null, Mars_Mass, 3389.5, Color.ORANGERED,
                new OrbitElements(1.52371, 0.09339, 1.85, 49.6, 286.5, 355)));
        defaults.add(new InitialCondition("Jupiter", "Planet", null, Jupiter_Mass, 69911.0, Color.CORAL,
                new OrbitElements(5.2029, 0.0484, 1.304, 100.4, 274.3, 185)));
        defaults.add(new InitialCondition("Saturn", "Planet", null, Saturn_Mass, 58232, Color.DARKGRAY,
                new OrbitElements(9.537, 0.0539, 2.486, 113.7, 338.9, 317)));
        defaults.add(new InitialCondition("Uranus", "Planet", null, Uranus_Mass, 25362, Color.DARKTURQUOISE,
                new OrbitElements(19.189, 0.04726, 0.773, 74.02, 96.9, 142)));
        defaults.add(new InitialCondition("Neptune", "Planet", null, Neptune_Mass, 24622, Color.MIDNIGHTBLUE,
                new OrbitElements(30.0699, 0.00859, 1.77, 131.784, 273.2, 260.5)));
        defaults.add(new InitialCondition("Moon", "Moon", "Earth", Moon_Mass, 1737.4, Color.LIGHTGRAY,
                new OrbitElements(0.00257, 0.0549, 5.1, 0, 0, 327)));

        return defaults;
    }*/












    public double getBodyDistanceAu(String name) {
        return logicalDistancesAu.getOrDefault(name, 0.0);
    }

    public double getBodyAngleDeg(String name) {
        return orbitalAnglesDeg.getOrDefault(name, 0.0);
    }





    public void setViewScale(double slider) {
        currentViewScale = Math.max(0.0, Math.min(1.0, slider));
        for (Body body : bodies) {
            applyScaleToBody(body);
        }
    }

    /*public void updatePhysics(double dt) {
        for (Body body : bodies) {
            body.resetAcceleration();
        }

        for (int i = 0; i < bodies.size(); i++) {
            for (int j = i + 1; j < bodies.size(); j++) {
                applyGravity(bodies.get(i), bodies.get(j));
            }
        }

        double[] axOld = new double[bodies.size()];
        double[] ayOld = new double[bodies.size()];
        double[] azOld = new double[bodies.size()];

        for (int i = 0; i < bodies.size(); i++) {
            Body body = bodies.get(i);
            axOld[i] = body.getAx();
            ayOld[i] = body.getAy();
            azOld[i] = body.getAz();
        }

        for (Body body : bodies) {
            body.updatePosition(dt);
        }

        for (Body body : bodies) {
            body.resetAcceleration();
        }

        for (int i = 0; i < bodies.size(); i++) {
            for (int j = i + 1; j < bodies.size(); j++) {
                applyGravity(bodies.get(i), bodies.get(j));
            }
        }

        for (int i = 0; i < bodies.size(); i++) {
            bodies.get(i).updateVelocity(dt, axOld[i], ayOld[i], azOld[i]);
        }

        for (Body body : bodies) {
            body.getView().setTranslateX(Conversions.metersToScene(body.getX()));
            body.getView().setTranslateY(Conversions.metersToScene(body.getY()));
            body.getView().setTranslateZ(Conversions.metersToScene(body.getZ()));
        }
    }*/

    private void applyGravity(Body a, Body b) {
        double dx = b.getX() - a.getX();
        double dy = b.getY() - a.getY();
        double dz = b.getZ() - a.getZ();

        double distSq = dx * dx + dy * dy + dz * dz;
        double dist = Math.sqrt(distSq);

        if (dist < 1.0) return;

        double accelA = G * b.getMass() / distSq;
        double accelB = G * a.getMass() / distSq;

        double nx = dx / dist;
        double ny = dy / dist;
        double nz = dz / dist;

        a.addAcceleration(accelA * nx, accelA * ny, accelA * nz);
        b.addAcceleration(-accelB * nx, -accelB * ny, -accelB * nz);
    }

    public Group getRoot() {
        return root;
    }







}

