package com.example.planetsimdemo;

import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;

import java.util.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static com.example.planetsimdemo.Conversions.*;

/*This class will create/edit the solar system. That means any add/edit/remove/place body related methods */
public class SolarSystemState {
    private static final String TYPE_STAR = "Star";
    private static final String TYPE_PLANET = "Planet";
    private static final String TYPE_MOON = "Moon";

    public record OrbitElements(
            double semiMajorAxisAu,
            double eccentricity,
            double inclinationDeg,
            double ascendingNodeDeg,
            double argumentOfPeriapsisDeg,
            double trueAnomalyDeg
    ) {}

    public record InitialCondition(
            String name,
            String type,
            String parent,
            double mass,
            double radiusKm,
            Color color,
            OrbitElements orbit
    ){}

    public record BodyMetaData(
            String type,
            String parent,
            double radiusKm,
            Color color,
            OrbitElements orbit
    ){}

    private record OrbitalState(
            double x,
            double y,
            double z,
            double vx,
            double vy,
            double vz
    ) { }

    private final List<Body> bodies = new ArrayList<>();
    private final Map<String, Body> byName = new LinkedHashMap<>();
    private final Map<String, BodyMetaData> metadataByName = new HashMap<>();

    public SolarSystemState(){this(defaultInitialConditions());}

    public SolarSystemState(Collection<InitialCondition> initialConditions){
        init(initialConditions);
    }

    public List<Body> getBodies() {return bodies;}
    public Body getBody(String name) {return byName.get(name);}
    public Set<String> getBodyNames() {return new TreeSet<>(byName.keySet());}

    public Set<String> getPlanetNames() { TreeSet<String> names = new TreeSet<>();
        for (String name : byName.keySet()) {
            if (TYPE_PLANET.equals(getBodyType(name))) {
                names.add(name);
            }
        }
        return names;
    }

    public String getBodyType(String name) {
        BodyMetaData metadata = metadataByName.get(name);
        return metadata == null ? TYPE_PLANET: metadata.type();
    }

    public String getOrbitParent(String name) {
      BodyMetaData metadata = metadataByName.get(name);
      return metadata == null ? null: metadata.parent;
    }

    public double getBodyRadiusKm(String name) {
        BodyMetaData metadata = metadataByName.get(name);
        return metadata == null ? 0.0: metadata.radiusKm;
    }

    public Color getBodyColor(String name) {
        BodyMetaData metadata = metadataByName.get(name);
        return metadata == null ? Color.WHITE : metadata.color;
    }

    public OrbitElements getOrbitElements(String name) {
        BodyMetaData metadata = metadataByName.get(name);
        return metadata == null ? null : metadata.orbit;
    }

    public List<InitialCondition> toInitialConditions() {
        List<InitialCondition> conditions = new ArrayList<>();
        for(String name : getBodyNames()){
            Body body = byName.get(name);
            BodyMetaData metadata = metadataByName.get(name);

            conditions.add(new InitialConditon(
                    name,
                    metadata.type(),
                    metadata.parent(),
                    body.getMass(),
                    metadata.radiusKm(),
                    metadata.color(),
                    metadata.orbit()
            ));
        }
        return conditions;
    }

    public boolean addNewBody(String name, String type, String parentName,
                              double mass, double radiusKm, double semiMajorAxisAu,double eccentricity,
                              double inclinationDeg, double ascendingNodeDeg, double argumentOfPeriapsisDeg,
                              double trueAnomalyDeg, Color color) {
        if (name == null || name.isBlank() || byName.containsKey(name)) {
            return false;
        }

        if(mass<= 0.0 || radiusKm <= 0.0){
            return false;
        }
        String normalizedType = normalizeType(type);
        if (TYPE_MOON.equals(normalizedType)) {
            if (parentName == null || parentName.isBlank()) {
                return false;
            }
            if(!TYPE_PLANET.equals(getBodyType(parentName))){
            return false;}
        } else {
            parentName = null;
        }

        OrbitElements orbit = null;
        if (TYPE_STAR.equals(normalizedType)||hasOrbitInput(semiMajorAxisAu,eccentricity,inclinationDeg,ascendingNodeDeg,
                argumentOfPeriapsisDeg,trueAnomalyDeg)) {
            if (semiMajorAxisAu <= 0.0) {
                return false;
            }
            if (eccentricity <= 0.0 || eccentricity >= 1) {
                return false;
            }

            orbit = new OrbitElements(
                    semiMajorAxisAu,
                    eccentricity,
                    inclinationDeg,
                    ascendingNodeDeg,
                    argumentOfPeriapsisDeg,
                    trueAnomalyDeg);
        }
        Body body = createBody(name,normalizedType, parentName, mass, orbit);
        if(body==null){return false;}

        registerBody(body, normalizedType, parentName, radiusKm, color ==null ? Color.WHITE: color, orbit);
        return true;
    }

    public boolean updateBody(String originalName, String newName, String type, String parentName,
                              double mass, double radiusKm, double semiMajorAxisAu,
                              double eccentricity,
                              double inclinationDeg,
                              double ascendingNodeDeg,
                              double argumentOfPeriapsisDeg,
                              double trueAnomalyDeg, Color color) {
        Body existing = byName.get(originalName);
        if (existing == null || newName == null || newName.isBlank()) {
            return false;
        }

        String normalizedType = normalizeType(type);

        if (!originalName.equals(newName) && byName.containsKey(newName)) {
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

            if (!TYPE_PLANET.equals(getBodyType(parentName))) {
                return false;
            }
        } else {
            parentName = null;
        }

        List<InitialCondition> children = getChildStates(originalName);

        if (!children.isEmpty() && !TYPE_PLANET.equals(normalizedType)) {
            return false;
        }
        OrbitElements newOrbit=null;

        if (!TYPE_STAR.equals(normalizedType) || hasOrbitInput(semiMajorAxisAu, eccentricity, inclinationDeg,
                ascendingNodeDeg, argumentOfPeriapsisDeg, trueAnomalyDeg)) {
            if (semiMajorAxisAu <= 0.0) {
                return false;
            }
            if (eccentricity <= 0.0 || eccentricity >= 1) {
                return false;
            }

        newOrbit = new OrbitElements(semiMajorAxisAu, eccentricity,
                inclinationDeg, ascendingNodeDeg, argumentOfPeriapsisDeg, trueAnomalyDeg);
    }

        Body updatedBody=createBody(newName,normalizedType,parentName,mass,newOrbit);
        if (updatedBody == null) {
            return false;
        }

        removeBodyInternal(originalName);
        registerBody(updatedBody, normalizedType, parentName, radiusKm, color==null?Color.WHITE:color, newOrbit);

        for(InitialCondition child : children) {
            removeBodyInternal(child.name());

            Body rebuiltChild = createBody(
                    child.name(), child.type(), newName, child.mass(), child.orbit());
            if (rebuiltChild != null) {
                registerBody(rebuiltChild, child.type(), newName, child.radiusKm(), child.color(), child.orbit());
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

    private void init(Collection<InitialCondition> initialConditions) {
        for (InitialCondition condition : orderInitialConditions(initialConditions)) {
            String normalizedType = normalizeType(condition.type());
            String parentName = TYPE_MOON.equals(normalizedType) ? condition.parent():null;

            Body body = createBody(condition.name(), normalizedType, parentName, condition.mass(), condition.orbit());
            if (body == null) {
                throw new IllegalStateException("Cannot create body for " + condition.name());
            }
            registerBody(body, normalizedType, parentName, condition.radiusKm(), condition.color() == null ? Color.WHITE : condition.color(), condition.orbit());
        }
    }
    private List<InitialCondition> orderInitialConditions(Collection<InitialCondition> initialConditions){
        List<InitialCondition> ordered = new ArrayList<>(initialConditions);
        ordered.sort(Comparator
                .comparingInt((InitialCondition condition) -> typeOrder(condition.type()))
                .thenComparing(InitialCondition::name,String.CASE_INSENSITIVE_ORDER));
        return ordered;
    }


    //sets an order to add in objects: add stars, planets, then moons
    private int typeOrder(String type) {
        String normalizedType = normalizeType(type);
        if (TYPE_STAR.equals(normalizedType)) return 0;
        if(TYPE_PLANET.equals(normalizedType)) return 1;
        return 2;
    }
    private String normalizeType(String type) {
        if (TYPE_STAR.equalsIgnoreCase(type)) return TYPE_STAR;
        if (TYPE_MOON.equalsIgnoreCase(type)) return TYPE_MOON;
        return TYPE_PLANET;
    }

    private boolean hasOrbitInput(double sMAA,double e, double iD, double aND, double aOPD,double tAD) {
        return sMAA !=0.0
                ||e!=0.0
                || iD != 0.0
                || aND != 0.0
                || aOPD != 0.0
                || tAD != 0.0;
    }

    private Body createBody(String name, String type, String parentName, double mass, OrbitElements orbit) {
        if (TYPE_STAR.equals(type)) {
            return orbit == null
               ?  new Body(name,mass,0,0,0,0,0,0) :
                createOrbitingBody(name,mass,massOfSun,orbit);
            }
            if(TYPE_MOON.equals(type)) {
                return createMoon(name, mass, parentName, orbit);
            }
        return createOrbitingBody(name,mass,massOfSun,orbit);
    }

    private Body createMoon(String name, double mass, String parentName, OrbitElements orbit) {
        Body parent = byName.get(parentName);
        if (parent ==null){
            return null;
        }
        Body moon = createOrbitingBody(name,mass,parent.getMass(),orbit);
        moon.setPosition(parent.getX()+moon.getX(),parent.getY()+moon.getY(),parent.getZ()+moon.getZ());
        moon.setVelocity(parent.getVx()+moon.getVx(),parent.getVy()+moon.getVy(),parent.getVz()+moon.getVz());
        return moon;
    }

    private Body createOrbitingBody(String name, double mass, double centralMass, OrbitElements orbit) {
        if(orbit==null){return null;}
        OrbitalState state = stateFromOrbitalElements(centralMass, mass,orbit);

        return new Body(name,mass,state.x(),state.y(),state.z(),state.vx(),state.vy(),state.vz());
    }

    private void registerBody(Body body, String type, String parentName, double radiusKm, Color color, OrbitElements orbit) {
        bodies.add(body);
        byName.put(body.getName(), body);
        metadataByName.put(body.getName(), new BodyMetaData(type, parentName, radiusKm, color, orbit));
    }

    private void removeBodyInternal(String name) {
        Body body = byName.remove(name);
        if (body != null) {bodies.remove(body);}

        metadataByName.remove(name);
    }

    private List<InitialCondition> getChildStates(String parentName){
        List<InitialCondition> children = new ArrayList<>();
        for (String name:byName.keySet()){
            BodyMetaData metadata = metadataByName.get(name);
            if(metadata!=null && parentName.equals(metadata.parent())) {
                Body body = byName.get(name);
                children.add(new InitialCondition(name, metadata.type(), metadata.parent(), body.getMass(), metadata.radiusKm(), metadata.color, metadata.orbit()));
            }
        }
        return children;
    }




    /*semi major axis is the farthest distance between the center or orbit and the body orbiting
    eccentricity changes the orbital shape
    nu will change the starting position and it is the angle from periapsis, or angle of the body from its center of orbit
    r is the distance of a body from its center of orbit
    mu is the force of gravity between two bodies
    h is the angular momentum magnitude, it controls orbital speed
    omega small is used as an argument of periapsis and rotates the ellipse
    Inclination tilts the orbital plane
    omega big rotates the tilted orbital plane
    */
    private OrbitalState stateFromOrbitalElements(double centralMass, double bodyMass, OrbitElements orbit) {
        double i = Math.toRadians(orbit.inclinationDeg);
        double omegaBig = Math.toRadians(orbit.ascendingNodeDeg());
        double omegaSmall = Math.toRadians(orbit.argumentOfPeriapsisDeg());
        double nu = Math.toRadians(orbit.trueAnomalyDeg());
        double mu = G*(centralMass+bodyMass);
        double semiMajorAxis = orbit.semiMajorAxisAu() * AU_IN_METERS;
        double eccentricity = orbit.eccentricity();
        double r = semiMajorAxis * (1 - eccentricity * eccentricity) / (1 + eccentricity * Math.cos(nu));
        double xOrb = r*Math.cos(nu);
        double yOrb = r*Math.sin(nu);
        double h = Math.sqrt(mu*semiMajorAxis*(1-eccentricity*eccentricity));
        double vxOrb = -mu/h*Math.sin(nu);
        double vyOrb = mu/h*(eccentricity + Math.cos(nu));

        double[] pos = rotateToWorld(xOrb, yOrb, 0.0, omegaBig, i, omegaSmall);
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

    public static List<InitialCondition> defaultInitialConditions() {
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
    }
}
