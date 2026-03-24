package com.example.planetsimdemo;

import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import java.util.ArrayList;
import java.util.List;

public class Planet {

    private final Sphere body;
    private final double orbitRadius;
    private final double speed;
    private double angle;
    private final List<Moon> moons = new ArrayList<>();


    public Planet(double size, double orbitRadius, double speed, Color color) {
        this.body = new Sphere(size);
        this.orbitRadius = orbitRadius;
        this.speed = speed;
        this.angle = 0;

        PhongMaterial material = new PhongMaterial(color);
        body.setMaterial(material);
    }

    public void update() {
        angle += speed;

        double x = orbitRadius * Math.cos(angle);
        double z = orbitRadius * Math.sin(angle);

        body.setTranslateX(x);
        body.setTranslateZ(z);

        // Update moons AFTER planet moves
        for (Moon moon : moons) {
            moon.update();
        }
    }

    public Sphere getBody() {
        return body;
    }

    public double getOrbitRadius() {
        return orbitRadius;
    }

    public void addMoon(Moon moon) {
        moons.add(moon);
    }

    public List<Moon> getMoons() {
        return moons;
    }
}