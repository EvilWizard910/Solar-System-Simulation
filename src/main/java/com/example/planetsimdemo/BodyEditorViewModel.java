package com.example.planetsimdemo;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;

public class BodyEditorViewModel {
    private final ObjectProperty<SolarSystem> solarSystem=new SimpleObjectProperty<>();
    private final ObservableList<String> availableBodies = FXCollections.observableArrayList();
    private final ObservableList<String> availableParents = FXCollections.observableArrayList();
    private final ObservableList<String> availableTypes = FXCollections.observableArrayList("Star", "Planet", "Moon");

    private final StringProperty selectedBodyName = new SimpleStringProperty();
    private final StringProperty name = new SimpleStringProperty("");
    private final StringProperty type = new SimpleStringProperty("Planet");
    private final StringProperty parent = new SimpleStringProperty();
    private final DoubleProperty mass = new SimpleDoubleProperty(0.0);
    private final DoubleProperty radiusKm = new SimpleDoubleProperty(0.0);
    private final DoubleProperty semiMajorAxisAu = new SimpleDoubleProperty(0.0);
    private final DoubleProperty eccentricity = new SimpleDoubleProperty(0.0);
    private final DoubleProperty inclinationDeg = new SimpleDoubleProperty(0.0);
    private final DoubleProperty ascendingNodeDeg = new SimpleDoubleProperty(0.0);
    private final DoubleProperty argumentOfPeriapsisDeg = new SimpleDoubleProperty(0.0);
    private final DoubleProperty trueAnomalyDeg = new SimpleDoubleProperty(0.0);
    private final ObjectProperty<Color> color = new SimpleObjectProperty<>(Color.WHITE);
    private final StringProperty errorMessage = new SimpleStringProperty("");

    public void clearForm(){
        selectedBodyName.set("null");
        name.set("");
        type.set("Planet");
        mass.set(0.0);
        radiusKm.set(0.0);
        semiMajorAxisAu.set(0.0);
        eccentricity.set(0.0);
        inclinationDeg.set(0.0);
        ascendingNodeDeg.set(0.0);
        argumentOfPeriapsisDeg.set(0.0);
        trueAnomalyDeg.set(0.0);
        color.set(Color.WHITE);
        errorMessage.set("");
    }

    public BodyEditorViewModel(SolarSystem solarSystem) {
        this.solarSystem.set(solarSystem);
        refreshLists();
    }
    public void refreshLists(){
        SolarSystem system =  solarSystem.get();
        availableBodies.clear();
        availableParents.clear();
        if(system == null){
            return;
        }
        availableBodies.addAll(system.getBodyNames());
        availableParents.addAll(system.getPlanetNames());
    }

    public void setSolarSystem(SolarSystem solarSystem){
        this.solarSystem.set(solarSystem);
        refreshLists();
        clearForm();
    }

    public void loadSelectedBody(){
        SolarSystem system =  solarSystem.get();
        String selected = selectedBodyName.get();

        if(selected == null||system == null||selected.isBlank()){
            return;
        }
        Body body = system.getBody(selected);
        if(body == null){
            return;
        }
        SolarSystemState.OrbitElements orbit = system.getOrbitElements(selected);

        name.set(selected);
        type.set(system.getBodyType(selected));
        parent.set(system.getOrbitParent(selected));
        mass.set(body.getMass());
        radiusKm.set(system.getBodyRadiusKm(selected));
        color.set(system.getBodyColor(selected));

        if(orbit != null){
            semiMajorAxisAu.set(orbit.semiMajorAxisAu());
            eccentricity.set(orbit.eccentricity());
            inclinationDeg.set(orbit.inclinationDeg());
            ascendingNodeDeg.set(orbit.ascendingNodeDeg());
            argumentOfPeriapsisDeg.set(orbit.argumentOfPeriapsisDeg());
            trueAnomalyDeg.set(orbit.trueAnomalyDeg());
        }else{
            semiMajorAxisAu.set(0.0);
            eccentricity.set(0.0);
            inclinationDeg.set(0.0);
            ascendingNodeDeg.set(0.0);
            argumentOfPeriapsisDeg.set(0.0);
            trueAnomalyDeg.set(0.0);
        }
    }

    public boolean addBody(){
        SolarSystem system =  solarSystem.get();
        if(system == null){
            errorMessage.set("No SolarSystem selected");
            return false;
        }
        errorMessage.set("");
        boolean added = system.addNewBody(
                name.get(),
                type.get(),
                parent.get(),
                mass.get(),
                radiusKm.get(),
                semiMajorAxisAu.get(),
                eccentricity.get(),
                inclinationDeg.get(),
                ascendingNodeDeg.get(),
                argumentOfPeriapsisDeg.get(),
                trueAnomalyDeg.get(),
                color.get()
                );
        if(!added){
            errorMessage.set("Could not add body. Check parameters.");
            return false;
        }
        refreshLists();
        clearForm();
        return true;
    }

    public boolean updateBody(){
        SolarSystem system =  solarSystem.get();
        String originalName = selectedBodyName.get();
        if(system == null){
            errorMessage.set("No SolarSystem selected");
            return false;
        }
        if(originalName == null||originalName.isBlank()){
            errorMessage.set("Select a body");
            return false;
        }
        errorMessage.set("");
        boolean updated = system.updateBody(
                originalName,
                name.get(),
                type.get(),
                parent.get(),
                mass.get(),
                radiusKm.get(),
                semiMajorAxisAu.get(),
                eccentricity.get(),
                inclinationDeg.get(),
                ascendingNodeDeg.get(),
                argumentOfPeriapsisDeg.get(),
                trueAnomalyDeg.get(),
                color.get()
        );
        if(!updated){
            errorMessage.set("Could not update body. Check parameters.");
            return false;
        }
        refreshLists();
        selectedBodyName.set(name.get());
        return true;
    }

    public boolean removeSelectedBody(){
        SolarSystem system =  solarSystem.get();
        String selected = selectedBodyName.get();
        if(system == null){
            errorMessage.set("No SolarSystem selected");
            return false;
        }
        if(selected == null||selected.isBlank()){
            errorMessage.set("Select a body");
            return false;
        }
        errorMessage.set("");

        boolean removed = system.removeBody(selected);
        if(!removed) {
            errorMessage.set("Could not remove body. Check parameters.");
            return false;
        }
        refreshLists();
        clearForm();
        return true;
    }

    public boolean isMoonSelected(){
        return "Moon".equals(type.get());
    }

    public ObjectProperty<SolarSystem> solarSystemProperty(){return solarSystem;}
    public ObservableList<String> getAvailableBodies(){return availableBodies;}
    public ObservableList<String> getAvailableParents(){return availableParents;}
    public ObservableList<String> getAvailableTypes(){return availableTypes;}

    //body properties
    public StringProperty selectedBodyNameProperty(){return selectedBodyName;}
    public StringProperty nameProperty(){return name;}
    public StringProperty typeProperty(){return type;}
    public StringProperty parentProperty(){return parent;}
    public DoubleProperty massProperty(){return mass;}
    public DoubleProperty radiusKmProperty(){return radiusKm;}

    //view property
    public ObjectProperty<Color> colorProperty(){return color;}

    //orbit properties
    public DoubleProperty semiMajorAxisAuProperty(){return semiMajorAxisAu;}
    public DoubleProperty eccentricityProperty(){return eccentricity;}
    public DoubleProperty inclinationDegProperty(){return inclinationDeg;}
    public DoubleProperty ascendingNodeDegProperty(){return ascendingNodeDeg;}
    public DoubleProperty argumentOfPeriapsisDeg(){return argumentOfPeriapsisDeg;}
    public DoubleProperty trueAnomalyDegProperty(){return trueAnomalyDeg;}

    public StringProperty errorMessageProperty(){return errorMessage;}

}
