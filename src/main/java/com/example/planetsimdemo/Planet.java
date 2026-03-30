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
    double g = 0.00000000006674;
    double massOfSun = 4385000000000000000000000000000.0;
    private final List<Moon> moons = new ArrayList<>();

     double auToPixel(double au){
        return au*100.0;
    }
     double kmToPixel(double km){
        return km/1500000;
    }
    double orbitalSpeed( double au){
        return 2* Math.PI * Math.sqrt( (this.orbitRadius * this.orbitRadius * this.orbitRadius)/ (g*massOfSun) ) ;
    }

    double auToMeters( double au){
         return au * 150000000000.0;
    }




    public Planet(double size, double orbitRadius, Color color) {
        this.body = new Sphere(kmToPixel(size)/2);
        this.orbitRadius = auToPixel(orbitRadius);
        this.speed = (Math.PI * 2 * auToMeters(orbitRadius) )/orbitalSpeed(auToMeters(orbitRadius));
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