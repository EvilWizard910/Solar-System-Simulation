package com.example.planetsimdemo;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.shape.Box;

import java.util.ArrayList;
import java.util.List;

public class SolarSystem {

    private final List<Planet> planets = new ArrayList<>();
    private final Group root = new Group();

    public SolarSystem() {
        createSun();
        createPlanets();
    }

    private void createSun() {
        Sphere sun = new Sphere(40);

        PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(Color.YELLOW);
        material.setSpecularColor(Color.WHITE);

        sun.setMaterial(material);
        root.getChildren().add(sun);
    }

    private void createPlanets() {

        Planet earth = new Planet(15, 150, 0.01, Color.BLUE);
        Planet mars  = new Planet(10, 220, 0.008, Color.RED);
        Planet venus = new Planet(12, 100, 0.015, Color.ORANGE);

        Moon moon = new Moon(earth, 4, 30, 0.03, Color.LIGHTGRAY);

        earth.addMoon(moon);

        planets.add(earth);
        planets.add(mars);
        planets.add(venus);

        for (Planet p : planets) {
            root.getChildren().add(createOrbitRing(p.getOrbitRadius()));
            root.getChildren().add(p.getBody());

            // Add moons
            for (Moon m : p.getMoons()) {
                root.getChildren().add(createOrbitRingAroundPlanet(m));
                root.getChildren().add(m.getBody());
            }
        }
    }

    // Create thin circular orbit ring
    private Group createOrbitRing(double radius) {
        Group ring = new Group();

        int segments = 100; // more = smoother circle
        double thickness = 0.5;

        for (int i = 0; i < segments; i++) {
            double angle1 = 2 * Math.PI * i / segments;
            double angle2 = 2 * Math.PI * (i + 1) / segments;

            double x1 = radius * Math.cos(angle1);
            double z1 = radius * Math.sin(angle1);

            double x2 = radius * Math.cos(angle2);
            double z2 = radius * Math.sin(angle2);

            double dx = x2 - x1;
            double dz = z2 - z1;
            double length = Math.sqrt(dx * dx + dz * dz);

            Box segment = new Box(length, thickness, thickness);

            // position midpoint
            segment.setTranslateX((x1 + x2) / 2);
            segment.setTranslateZ((z1 + z2) / 2);

            // rotate segment to match angle
            double angleDeg = Math.toDegrees(Math.atan2(dz, dx));
            segment.setRotationAxis(Rotate.Y_AXIS);
            segment.setRotate(-angleDeg);

            // material (subtle gray)
            PhongMaterial mat = new PhongMaterial(Color.rgb(180, 180, 180, 0.3));
            segment.setMaterial(mat);

            ring.getChildren().add(segment);
        }

        return ring;
    }

    private Group createOrbitRingAroundPlanet(Moon moon) {
        Group ring = createOrbitRing(moon.getOrbitRadius());

        // Bind ring to PLANET position (not moon)
        ring.translateXProperty().bind(
                moon.getParent().getBody().translateXProperty()
        );

        ring.translateZProperty().bind(
                moon.getParent().getBody().translateZProperty()
        );

        return ring;
    }

    public void update() {
        for (Planet p : planets) {
            p.update();
        }
    }

    public Group getRoot() {
        return root;
    }
}