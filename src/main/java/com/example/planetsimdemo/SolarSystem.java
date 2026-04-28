package com.example.planetsimdemo;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;


import java.util.*;

import static com.example.planetsimdemo.Conversions.*;

public class SolarSystem {

    private static final String TYPE_STAR = "Star";
    private static final String TYPE_PLANET = "Planet";
    private static final String TYPE_MOON = "Moon";

    private final Group root = new Group();
    private final List<Body> bodies = new ArrayList<>();
    private final Map<String, Body> map = new HashMap<>();
    private final Map<Body, Double> baseRadii = new HashMap<>();
    private final Map<String, OrbitElements> orbitElements = new HashMap<>();

    private final Map<String, Double> logicalRadiiKm = new HashMap<>();
    private final Map<String, Double> logicalDistancesAu = new HashMap<>();
    private final Map<String, Double> orbitalAnglesDeg = new HashMap<>();
    private final Map<String, String> bodyTypes = new HashMap<>();
    private final Map<String, String> orbitParents = new HashMap<>();
    private final Map<String, Color> bodyColors = new HashMap<>();

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
    public record OrbitElements(
            double semiMajorAxisAu,
            double eccentricity,
            double inclinationDeg,
            double ascendingNodeDeg,
            double argumentOfPeriapsisDeg,
            double trueAnomalyDeg
    ) {}

    private record OrbitalState(
            double x,
            double y,
            double z,
            double vx,
            double vy,
            double vz
    ) {
    }

    public SolarSystem() {
        init();
    }

    public static double toSceneRadiusFromKm(String bodyName, double radiusKm) {
        return Conversions.metersToScene(radiusKm * 1000.0);
    }

    private static double sceneRadiusToKm(double sceneRadius) {
        return sceneRadius * 2.0e9 / 1000.0;
    }

    private String normalizeType(String type) {
        if (TYPE_STAR.equalsIgnoreCase(type)) return TYPE_STAR;
        if (TYPE_MOON.equalsIgnoreCase(type)) return TYPE_MOON;
        return TYPE_PLANET;
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

    private void registerBody(Body body, String type,
                              String parentName,
                              Color color,
                              OrbitElements orbit) {
        double radiusKm =body.getRadius();
        bodies.add(body);
        map.put(body.getName(), body);
        root.getChildren().add(body.getView());

        double sceneRadius = toSceneRadiusFromKm(body.getName(), radiusKm);
        baseRadii.put(body, sceneRadius);

        logicalRadiiKm.put(body.getName(), radiusKm);
        bodyTypes.put(body.getName(), type);
        orbitParents.put(body.getName(), parentName);
        bodyColors.put(body.getName(), color);
        body.getView().setMaterial(new PhongMaterial(color));

        if (orbit != null) {
            orbitElements.put(body.getName(), orbit);
            logicalDistancesAu.put(body.getName(), orbit.semiMajorAxisAu());
            orbitalAnglesDeg.put(body.getName(), orbit.trueAnomalyDeg());
        } else {
            orbitElements.remove(body.getName());
            logicalDistancesAu.put(body.getName(), 0.0);
            orbitalAnglesDeg.put(body.getName(), 0.0);
        }
        applyScaleToBody(body);
    }

    private void removeBodyInternal(String name) {
        Body body = map.remove(name);
        if (body == null) return;

        bodies.remove(body);
        root.getChildren().remove(body.getView());
        baseRadii.remove(body);

        orbitElements.remove(name);
        logicalRadiiKm.remove(name);
        logicalDistancesAu.remove(name);
        orbitalAnglesDeg.remove(name);
        bodyTypes.remove(name);
        orbitParents.remove(name);
        bodyColors.remove(name);
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

    private Body createOrbitingBody(String name, double mass, double radiusKm, double centralMass, OrbitElements orbit) {
        Sphere sphere = new Sphere(toSceneRadiusFromKm(name,radiusKm));
        OrbitalState state = stateFromOrbitalElements(
                centralMass,
                mass,
                orbit.semiMajorAxisAu()*AU_IN_METERS,
                orbit.eccentricity(),
                orbit.inclinationDeg(),
                orbit.ascendingNodeDeg(),
                orbit.argumentOfPeriapsisDeg(),
                orbit.trueAnomalyDeg()
                );
        return new Body(name,mass,sphere,state.x(),state.y(),state.z(),state.vx(),state.vy(),state.vz());
    }

    private Body createMoon(String name, double mass, double radiusKm, String parentName, OrbitElements orbit) {
        Body parent = map.get(parentName);
        if (parent ==null){
            return null;
        }
        Body moon = createOrbitingBody(name,mass,radiusKm,parent.getMass(),orbit);
        moon.setPosition(parent.getX()+moon.getX(),parent.getY()+moon.getY(),parent.getZ()+moon.getZ());
        moon.setVelocity(parent.getVx()+moon.getVx(),parent.getVy()+moon.getVy(),parent.getVz()+moon.getVz());
        return moon;
    }


    private void init() {
       Body sun = createStar("Sun",massOfSun,700000);
       registerBody(sun, TYPE_STAR, null, Color.YELLOW, null);

        OrbitElements mercuryOrbit = new OrbitElements(0.3870993,0.20564,7.005,48.3, 29.13, 193);
        Body mercury = createOrbitingBody("Mercury",Mercury_Mass,2439.7, massOfSun,mercuryOrbit );
        registerBody(mercury, TYPE_PLANET,null, Color.MISTYROSE, mercuryOrbit);

        OrbitElements venusOrbit = new OrbitElements(0.7233336,0.00678,3.3947,76.7,54.9,125);
        Body venus = createOrbitingBody("Venus", Venus_Mass, 6051.8, massOfSun,venusOrbit);
        registerBody(venus, TYPE_PLANET,null, Color.BURLYWOOD, venusOrbit);

       OrbitElements earthOrbit = new OrbitElements(1.0000, 0.0167, 0.00005, -11.26064, 114.20783, 100.0);
        Body earth = createOrbitingBody("Earth", EARTH_MASS, 6371, massOfSun, earthOrbit);
        registerBody(earth, TYPE_PLANET, null, Color.DODGERBLUE, earthOrbit);

        OrbitElements marsOrbit = new OrbitElements(1.52371, 0.09339, 1.85,49.6,286.5,355);
        Body mars = createOrbitingBody("Mars", Mars_Mass,3389.5, massOfSun,marsOrbit);
        registerBody(mars, TYPE_PLANET, null, Color.ORANGERED, marsOrbit);

        OrbitElements jupiterOrbit = new OrbitElements(5.2029, 0.0484, 1.304, 100.4,274.3,185);
        Body jupiter = createOrbitingBody("Jupiter", Jupiter_Mass, 69911.0,massOfSun,jupiterOrbit);
        registerBody(jupiter, TYPE_PLANET, null, Color.CORAL,jupiterOrbit);

        OrbitElements saturnOrbit = new OrbitElements(9.537,0.0539,2.486,113.7,338.9,317);
        Body saturn = createOrbitingBody("Saturn",Saturn_Mass,58232,massOfSun,saturnOrbit);
        registerBody(saturn, TYPE_PLANET, null, Color.DARKGRAY, saturnOrbit);

        OrbitElements uranusOrbit = new OrbitElements(19.189,0.04726,0.773,74.02,96.9,142);
        Body uranus = createOrbitingBody("Uranus",Uranus_Mass,25362,massOfSun,uranusOrbit);
        registerBody(uranus,TYPE_PLANET, null, Color.DARKTURQUOISE,uranusOrbit);

        OrbitElements neptuneOrbit = new OrbitElements(30.0699,0.00859,1.77,131.784,273.2,260.5);
        Body neptune = createOrbitingBody("Neptune", Neptune_Mass,24622,massOfSun,neptuneOrbit);
        registerBody(neptune,TYPE_PLANET, null, Color.MIDNIGHTBLUE,neptuneOrbit);

        OrbitElements moonOrbit = new OrbitElements(0.00257,0.0549,5.1,0,0,327);
        Body moon = createMoon("Moon",Moon_Mass,1737.4, "Earth", moonOrbit);
        registerBody(moon, TYPE_MOON, "Earth", Color.LIGHTGRAY,moonOrbit);


     /*
        make("Io", TYPE_MOON, "Jupiter", Io_Mass, 1821.6, 421800000.0 / AU_IN_METERS, 0.0, Color.LIMEGREEN);
        make("Europa", TYPE_MOON, "Jupiter", Europa_mass, 1560.8, 671100000.0 / AU_IN_METERS, 45.0, Color.WHITE);
        make("Ganymede", TYPE_MOON, "Jupiter", Ganymede_mass, 2631.2, 1070400000.0 / AU_IN_METERS, 90.0, Color.GRAY);
        make("Callisto", TYPE_MOON, "Jupiter", Callisto_mass, 2410.3, 1882700000.0 / AU_IN_METERS, 135.0, Color.DARKGRAY);
    */}

    public boolean addNewBody(String name, String type, String parentName,
                              double mass, double radiusKm, double semiMajorAxisAu,double eccentricity,
                              double inclinationDeg, double ascendingNodeDeg, double argumentOfPeriapsisDeg,
                              double trueAnomalyDeg, Color color) {
        if (name == null || name.isBlank() || map.containsKey(name)) {
            return false;
        }

        String normalizedType = normalizeType(type);
        if (TYPE_MOON.equals(normalizedType)) {
            if (parentName == null || parentName.isBlank()) {
                return false;
            }
            Body parent = map.get(parentName);
            if (parent == null || !TYPE_PLANET.equals(bodyTypes.get(parentName))) {
                return false;
            }
        } else {
            parentName = null;
        }
        if (radiusKm<=0 || mass <=0){return false;}

        boolean givenOrbit = (semiMajorAxisAu!=0.0
                ||eccentricity!=0.0
                || inclinationDeg!=0.0
                ||ascendingNodeDeg!=0.0
                ||argumentOfPeriapsisDeg!=0.0
                ||trueAnomalyDeg!=0.0
        );

        OrbitElements orbit = null;
        Body body;
        if (TYPE_STAR.equals(normalizedType)){
            if(givenOrbit) {
                orbit = new OrbitElements(
                        semiMajorAxisAu,
                        eccentricity,
                        inclinationDeg,
                        ascendingNodeDeg,
                        argumentOfPeriapsisDeg,
                        trueAnomalyDeg);
            body=createStar(name, mass, radiusKm, orbit);}
            else{
            body = createStar(name, mass, radiusKm);}
        } else {
            if(semiMajorAxisAu <= 0.0){return false;}
            if(eccentricity <= 0.0 || eccentricity>=1){return false;}

            orbit = new OrbitElements(
                    semiMajorAxisAu,
                    eccentricity,
                    inclinationDeg,
                    ascendingNodeDeg,
                    argumentOfPeriapsisDeg,
                    trueAnomalyDeg
            );

             if (TYPE_MOON.equals(normalizedType)) {
                body = createMoon(name, mass, radiusKm, parentName, orbit);
            } else {
                body = createOrbitingBody(name, mass, radiusKm, massOfSun, orbit);
            }
        }

        if (body == null) {
            return false;
        }

        registerBody(body, normalizedType, parentName, color, orbit);
        return true;
    }




     public boolean updateBody(String originalName, String newName, String type, String parentName,
                              double mass, double radiusKm, double semiMajorAxisAu,
                               double eccentricity,
                               double inclinationDeg,
                               double ascendingNodeDeg,
                               double argumentOfPeriapsisDeg,
                               double trueAnomalyDeg, Color color) {
         Body existing = map.get(originalName);
         if (existing == null || newName == null || newName.isBlank()) {
             return false;
         }

         String normalizedType = normalizeType(type);

         if (!originalName.equals(newName) && map.containsKey(newName)) {
             return false;
         }

         if (mass <= 0 || radiusKm <= 0) {
             return false;
         }

         if (TYPE_MOON.equals(normalizedType)) {
             if (parentName == null || parentName.isBlank()) {
                 return false;
             }

             if (originalName.equals(parentName)) {
                 return false;
             }

             if (!map.containsKey(parentName)) {
                 return false;
             }

             if (!TYPE_PLANET.equals(bodyTypes.get(parentName))) {
                 return false;
             }
         }else {
             parentName = null;
         }

         List<BodyState> children = getChildStates(originalName);

         if (!children.isEmpty() && !TYPE_PLANET.equals(normalizedType)) {
             return false;
         }
         OrbitElements newOrbit = null;
         Body updatedBody;

         boolean givenOrbit = (semiMajorAxisAu != 0.0
                 || eccentricity != 0.0
                 || inclinationDeg != 0.0
                 || ascendingNodeDeg != 0.0
                 || argumentOfPeriapsisDeg != 0.0
                 || trueAnomalyDeg != 0.0);
         if(TYPE_STAR.equals(normalizedType) && !givenOrbit){
             updatedBody = createStar(newName, mass, radiusKm);
         }else {
             if(semiMajorAxisAu <= 0.0){return false;}
             if(eccentricity <= 0.0 || eccentricity >= 1){return false;}
         }

         newOrbit = new OrbitElements(semiMajorAxisAu, eccentricity,
                 inclinationDeg, ascendingNodeDeg, argumentOfPeriapsisDeg, trueAnomalyDeg);

         if (TYPE_STAR.equals(normalizedType)) {
             updatedBody = createStar(newName, mass, radiusKm, newOrbit);
         } else if (TYPE_MOON.equals(normalizedType)) {
                 updatedBody = createMoon(newName, mass, radiusKm, parentName, newOrbit);
             } else {
                 updatedBody = createOrbitingBody(newName, mass, radiusKm, massOfSun, newOrbit);
         }
         if (updatedBody == null) {
             return false;
         }

         removeBodyInternal(originalName);
         registerBody(updatedBody, normalizedType, parentName, color, newOrbit);

         for (BodyState child : children) {
             removeBodyInternal(child.name);

             Body rebuiltChild = createMoon(
                     child.name,
                     child.mass,
                     child.radiusKm,
                     newName,
                     child.orbit
             );

             if (rebuiltChild != null) {
                 registerBody(
                         rebuiltChild,
                         child.type,
                         newName,
                         child.color,
                         child.orbit
                     );
                 }
             }

         return true;

     }

    public boolean removeBody(String name) {
        if (!getChildStates(name).isEmpty()) {
            return false;
        }

        removeBodyInternal(name);
        return true;
    }


    public Body getBody(String name) {
        return map.get(name);
    }

    public Set<String> getBodyNames() {
        return new TreeSet<>(map.keySet());
    }

    public Set<String> getPlanetNames() {
        TreeSet<String> names = new TreeSet<>();
        for (String name : map.keySet()) {
            if (TYPE_PLANET.equals(bodyTypes.get(name))) {
                names.add(name);
            }
        }
        return names;
    }

    public double getBodyRadiusKm(String name) {
        return logicalRadiiKm.getOrDefault(name, 0.0);
    }

    public double getBodyDistanceAu(String name) {
        return logicalDistancesAu.getOrDefault(name, 0.0);
    }

    public double getBodyAngleDeg(String name) {
        return orbitalAnglesDeg.getOrDefault(name, 0.0);
    }

    public String getBodyType(String name) {
        return bodyTypes.getOrDefault(name, TYPE_PLANET);
    }

    public String getOrbitParent(String name) {
        return orbitParents.get(name);
    }

    public Color getBodyColor(String name) {
        return bodyColors.getOrDefault(name, Color.WHITE);
    }

    public OrbitElements getOrbitElements(String name) {
        return orbitElements.get(name);
    }


    public void setViewScale(double slider) {
        currentViewScale = Math.max(0.0, Math.min(1.0, slider));
        for (Body body : bodies) {
            applyScaleToBody(body);
        }
    }

    public void updatePhysics(double dt) {
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
    }

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




    /*semi major axis is the farthest distance between the center or orbit and the body orbiting
    eccentricity changes the orbital shape
    nu will change the starting position and it is the angle from periapsis, or angle of the body from the it's center of orbit
    r is the distance of a body from its center of orbiy
    mu is the force of gravity between two bodies
    h is the angular momentum magnitude, it controls orbital speed
    omega small is used as an argument of periapsis and rotates the ellipse
    Inclination tilts the orbital plane
    omega big rotates the tilted orbital plane
    */
    private OrbitalState stateFromOrbitalElements(
            double centralMass,
            double bodyMass,
            double semiMajorAxis,
            double eccentricity,
            double inclinationDeg,
            double ascendingNodeDeg,
            double argumentOfPeriapsisDeg,
            double trueAnomalyDeg
    ) {
        double i = Math.toRadians(inclinationDeg);
        double omegaBig = Math.toRadians(ascendingNodeDeg);
        double omegaSmall = Math.toRadians(argumentOfPeriapsisDeg);
        double nu = Math.toRadians(trueAnomalyDeg);
        double mu = Conversions.G*(centralMass+bodyMass);
        double r = semiMajorAxis*(1-eccentricity*eccentricity)/(1+eccentricity*Math.cos(nu));
        double xOrb = r*Math.cos(nu);
        double yOrb = r*Math.sin(nu);
        double h = Math.sqrt(mu*semiMajorAxis*(1-eccentricity*eccentricity));
        double vxOrb = -mu/h*Math.sin(nu);
        double vyOrb = mu/h*(eccentricity + Math.cos(nu));

        double[] pos = rotateToWorld(xOrb,yOrb,0.0,omegaBig,i,omegaSmall);
        double[] vel = rotateToWorld(vxOrb, vyOrb, 0.0, omegaBig, i, omegaSmall);

        return new OrbitalState(
                pos[0], pos[1], pos[2], vel[0],vel[1],vel[2]
        );
    }

    private double[] rotateToWorld(
            double x, double y, double z,
            double ascendingNode,
            double inclination,
            double argumentOfPeriapsis
    ){
        double cos0=Math.cos(ascendingNode);
        double sin0=Math.sin(ascendingNode);
        double cosI=Math.cos(inclination);
        double sinI=Math.sin(inclination);
        double cosW=Math.cos(argumentOfPeriapsis);
        double sinW=Math.sin(argumentOfPeriapsis);

        double x1 =cosW*x-sinW*y;
        double y1 =sinW*x+cosW*y;
        double z1=z;
        double x2=x1;
        double y2=cosI*y1-sinI*z1;
        double z2=sinI*y1+cosI*z1;
        double x3=cos0*x2-sin0*y2;
        double y3=sin0*x2+cos0*y2;
        double z3=z2;
        return new double[]{x3,y3,z3};
    }
}

