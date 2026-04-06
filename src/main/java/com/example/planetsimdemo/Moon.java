package com.example.planetsimdemo;

import static com.example.planetsimdemo.Conversions.kmToPixel;
import static com.example.planetsimdemo.Conversions.orbitalMoonSpeed;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;

public class Moon {

    private final Sphere body;
    private final Planet parent;

    private final double orbitRadius;
    private final double speed;
    private double angle;

    public double kmToPixel(double km) {
        return km / 1500000;
    }

    public Moon(Planet parent, double size, double orbitRadius, Color color) {
        this.parent = parent;
        this.orbitRadius = kmToPixel(orbitRadius);
        this.speed = orbitalMoonSpeed(orbitRadius);
        this.angle = 0;

        this.body = new Sphere(kmToPixel(size));
        //this.body = new Sphere(2);
        PhongMaterial material = new PhongMaterial(color);
        body.setMaterial(material);
    }

    public void update() {
        angle += speed;

        double localX = orbitRadius * Math.cos(angle);
        double localZ = orbitRadius * Math.sin(angle);

        // Offset from planet position
        double planetX = parent.getBody().getTranslateX();
        double planetZ = parent.getBody().getTranslateZ();

        body.setTranslateX(planetX + localX);
        body.setTranslateZ(planetZ + localZ);
        body.setTranslateY(-3);
    }

    public Sphere getBody() {
        return body;
    }

    public double getOrbitRadius() {
        return orbitRadius;
    }

    public Planet getParent() {
        return parent;
    }
}