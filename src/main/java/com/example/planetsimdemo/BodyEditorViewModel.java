package com.example.planetsimdemo;

import javafx.beans.property.*;
import javafx.scene.paint.Color;

public class BodyEditorViewModel {
    private final StringProperty selectedBodyName = new SimpleStringProperty();
    private final StringProperty name = new SimpleStringProperty("");
    private final StringProperty type = new SimpleStringProperty("Planet");
    private final StringProperty parent = new SimpleStringProperty();
    private final DoubleProperty mass = new SimpleDoubleProperty();
    private final DoubleProperty radiusKm = new SimpleDoubleProperty();
    private final DoubleProperty semiMajorAxisAu = new SimpleDoubleProperty();
    private final DoubleProperty eccentricity = new SimpleDoubleProperty();
    private final DoubleProperty inclinationDeg = new SimpleDoubleProperty();
    private final DoubleProperty ascendingNodeDeg = new SimpleDoubleProperty();
    private final DoubleProperty argumentOfPeriapsisDeg = new SimpleDoubleProperty();
    private final DoubleProperty trueAnomalyDeg = new SimpleDoubleProperty();
    private final ObjectProperty<Color> color = new SimpleObjectProperty<>(Color.WHITE);
    private final StringProperty errorMessage = new SimpleStringProperty("");

    public void clear(){
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

    public StringProperty selectedBodyNameProperty(){return selectedBodyName;}
    public StringProperty nameProperty(){return name;}
    public StringProperty typeProperty(){return type;}
    public StringProperty parentProperty(){return parent;}
    public DoubleProperty massProperty(){return mass;}
    public DoubleProperty radiusKmProperty(){return radiusKm;}
    public DoubleProperty semiMajorAxisAuProperty(){return semiMajorAxisAu;}
    public DoubleProperty eccentricityProperty(){return eccentricity;}
    public DoubleProperty inclinationDegProperty(){return inclinationDeg;}
    public DoubleProperty ascendingNodeDegProperty(){return ascendingNodeDeg;}
    public DoubleProperty trueAnomalyDegProperty(){return trueAnomalyDeg;}
    public ObjectProperty<Color> colorProperty(){return color;}
    public StringProperty errorMessageProperty(){return errorMessage;}

}
