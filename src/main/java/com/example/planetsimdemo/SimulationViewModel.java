package com.example.planetsimdemo;

import javafx.beans.property.*;

/*This class will handle methods for current the current solar system being views. That includes
* the body to focus on, speed and scale methods, pause/start method, and reset/load save methods*/
public class SimulationViewModel {
    private final ObjectProperty<SolarSystem> solarSystem=new SimpleObjectProperty<SolarSystem>(new SolarSystem(SolarSystemState.defaultInitialConditions()));

    private final StringProperty focusedBodyName = new SimpleStringProperty("Sun");
    private final DoubleProperty bodyScale = new SimpleDoubleProperty(0.0);
    private final DoubleProperty simulationSpeed = new SimpleDoubleProperty(1.0);
    private final BooleanProperty running = new SimpleBooleanProperty(true);

    public ObjectProperty<SolarSystem> solarSystemProperty(){return solarSystem;}
    public StringProperty focusedBodyNameProperty(){return focusedBodyName;}
    public DoubleProperty bodyScaleProperty(){return bodyScale;}
    public DoubleProperty simulationSpeedProperty(){return simulationSpeed;}
    public BooleanProperty runningProperty(){return running;}

    public void resetToDefaultSystem(){
        SolarSystemState newSystem = new SolarSystemState(SolarSystemState.defaultInitialConditions());
        focusedBodyName.set("Sun");
        bodyScale.set(0.0);
        simulationSpeed.set(1.0);
        running.set(true);
    }
    public Body getFocusedBody(){
        SolarSystem s = solarSystem.get();
        return s == null ? null : s.getBody(focusedBodyName.get());
    }
}
