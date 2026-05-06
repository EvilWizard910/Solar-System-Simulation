package com.example.planetsimdemo;

import javafx.beans.property.*;

public class CameraViewModel {
    private final DoubleProperty orbitYaw = new SimpleDoubleProperty(35.0);
    private final DoubleProperty orbitPitch =  new SimpleDoubleProperty(25.0);
    private final DoubleProperty orbitDistance =  new SimpleDoubleProperty(7.0);

    public void resetForBody(Body body) {
        orbitYaw.set(35.0);
        orbitPitch.set(25.0);
        orbitDistance.set(body == null ? 7.0 : Math.max(0.02, Math.min(20.0,body.getMass())));
    }

    public DoubleProperty orbitYawProperty() {return orbitYaw;}
    public DoubleProperty orbitPitchProperty() {return orbitPitch;}
    public DoubleProperty orbitDistanceProperty() {return orbitDistance;}
}
