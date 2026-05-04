package com.example.planetsimdemo;

import javafx.scene.paint.Color;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public final class SolarSystem {
    private final SolarSystemState state;
    private final PhysicsEngine physicsEngine;

    public SolarSystem(){
        this(SolarSystemState.defaultInitialConditions());
    }
    public SolarSystem(Collection<SolarSystemState.InitialCondition> initialConditions){
        this.state = new SolarSystemState(initialConditions);
        this.physicsEngine = new PhysicsEngine();
    }

    public SolarSystemState getState(){
        return state;
    }
    public void updatePhysics(double dt){physicsEngine.update(state, dt);
    }

    private Body getBody(String name){
        return state.getBody(name);
    }

    public Set<String> getBodyNames(){
        return state.getBodyNames();
    }

    public Set<String> getPlanetNames(){
        return state.getPlanetNames();
    }

    public String getBodyType(String name){return state.getBodyType(name);}

    public String getOrbitParent(String name){return state.getOrbitParent(name);}

    public double getBodyRadiusKm(String name){return state.getBodyRadiusKm(name);}

    public Color getBodyColor(String name){return state.getBodyColor(name);}

    public SolarSystemState.OrbitElements getOrbitElements(String name){return state.getOrbitElements(name);}

    public List<SolarSystemState.InitialCondition> toInitialConditions(){return state.toInitialConditions();}

    public boolean addNewBody(String originalName, String newName, String type, String parentName,
                              double mass, double radiusKm, double semiMajorAxisAu, double eccentricity,
                              double inclinationDegree, double ascendingNodeDegree, double arguementOfPeriapsisDegree,
                              double trueAnomalyDegree, Color color){
        return state.updateBody(originalName, newName, type, parentName,mass,radiusKm,semiMajorAxisAu,eccentricity,inclinationDegree,ascendingNodeDegree,arguementOfPeriapsisDegree,trueAnomalyDegree,color);
    }
    public boolean removeBody(String name){
        return state.removeBody(name);
    }



    /*
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


        make("Io", TYPE_MOON, "Jupiter", Io_Mass, 1821.6, 421800000.0 / AU_IN_METERS, 0.0, Color.LIMEGREEN);
        make("Europa", TYPE_MOON, "Jupiter", Europa_mass, 1560.8, 671100000.0 / AU_IN_METERS, 45.0, Color.WHITE);
        make("Ganymede", TYPE_MOON, "Jupiter", Ganymede_mass, 2631.2, 1070400000.0 / AU_IN_METERS, 90.0, Color.GRAY);
        make("Callisto", TYPE_MOON, "Jupiter", Callisto_mass, 2410.3, 1882700000.0 / AU_IN_METERS, 135.0, Color.DARKGRAY);




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
    */

}

