package com.example.planetsimdemo;

import javafx.scene.paint.Color;
import java.util.Collection;
import java.util.List;
import java.util.Set;

//Builds the solar sytems that the users wants to see
public final class SolarSystem {
    private final SolarSystemState state;
    private final PhysicsEngine physicsEngine;

    public SolarSystem(){
        this(SolarSystemState.defaultInitialConditions());
    }
    public SolarSystem(SolarSystemState state) {
        this.state = state;
        this.physicsEngine = new PhysicsEngine();
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

    public Body getBody(String name){
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

    public double getBodyRotationSpeedDegPerSecond(String name) {
        return state.getBodyRotationSpeedDegPerSecond(name);
    }

    public String getBodyTexturePath(String name) {
        return state.getBodyTexturePath(name);
    }

    public SolarSystemState.OrbitElements getOrbitElements(String name){return state.getOrbitElements(name);}

    public List<SolarSystemState.InitialCondition> toInitialConditions(){return state.toInitialConditions();}

    public boolean updateBody(String originalName, String newName, String type, String parentName,
                              double mass, double radiusKm, double semiMajorAxisAu, double eccentricity,
                              double inclinationDegree, double ascendingNodeDegree, double argumentOfPeriapsisDegree,
                              double trueAnomalyDeg, Color color,
                              double rotationSpeedDegPerSecond, String texturePath) {
        return state.updateBody(
                originalName,
                newName,
                type,
                parentName,
                mass,
                radiusKm,
                semiMajorAxisAu,
                eccentricity,
                inclinationDegree,
                ascendingNodeDegree,
                argumentOfPeriapsisDegree,
                trueAnomalyDeg,
                color,
                rotationSpeedDegPerSecond,
                texturePath
        );

    }

    public boolean addNewBody(String name, String type, String parentName,
                              double mass, double radiusKm, double semiMajorAxisAu, double eccentricity,
                              double inclinationDegree, double ascendingNodeDegree, double argumentOfPeriapsisDegree,
                              double trueAnomalyDeg, Color color,
                              double rotationSpeedDegPerSecond, String texturePath) {
        return state.addNewBody(
                name,
                type,
                parentName,
                mass,
                radiusKm,
                semiMajorAxisAu,
                eccentricity,
                inclinationDegree,
                ascendingNodeDegree,
                argumentOfPeriapsisDegree,
                trueAnomalyDeg,
                color,
                rotationSpeedDegPerSecond,
                texturePath
        );
    }
    public boolean removeBody(String name){
        return state.removeBody(name);
    }

}

