package com.example.planetsimdemo;

import static com.example.planetsimdemo.Conversions.kmToPixel;
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
        Sphere sun = new Sphere(kmToPixel(1391400)/2);

        PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(Color.YELLOW);
        material.setSpecularColor(Color.WHITE);

        sun.setMaterial(material);
        root.getChildren().add(sun);
    }


    private void createPlanets() {
/*size = diameter in KM
* Orbit radius = distance from sun in au*/
        Planet mercury = new Planet(4879,.39, Color.SADDLEBROWN);
        Planet venus = new Planet(12104, .72, Color.TAN);
        Planet earth = new Planet(12756, 1,  Color.BLUE);
        Planet mars  = new Planet(6792, 1.52,  Color.RED);
        Planet jupiter = new Planet(142984, 5.2,  Color.ORANGE);
        Planet saturn = new Planet(120536, 9.54,  Color.BEIGE);
        Planet uranus = new Planet(51118, 19.2,  Color.TURQUOISE);
        Planet neptune = new Planet(49528, 30.06,  Color.DARKBLUE);

       /*takes size in km and radius in km*/
        Moon moon = new Moon(earth, 3480, 384000,  Color.LIGHTGRAY);

        Moon io =new Moon(jupiter,1821.6,422000, Color.YELLOWGREEN);
        Moon europa =new Moon(jupiter,1560.8,671000, Color.WHITE);
        Moon ganymede =new Moon(jupiter,2631,1070000, Color.DARKSLATEGRAY);
        Moon callisto =new Moon(jupiter,2410,1833000, Color.GRAY);

        Moon titan =new Moon(saturn, 5150, 1221830, Color.WHEAT);
        Moon rhea =new Moon(saturn, 1528, 527040, Color.WHEAT);
        Moon iapetus =new Moon(saturn, 1436, 3561300, Color.WHEAT);
        Moon dione =new Moon(saturn, 1120, 377400, Color.WHEAT);
        Moon tethys =new Moon(saturn, 1061, 294660, Color.WHEAT);
        Moon enceladus =new Moon(saturn, 504, 238020, Color.WHEAT);
        Moon mimas =new Moon(saturn, 396, 185520, Color.WHEAT);

        planets.add(mercury);
        planets.add(venus);
        planets.add(earth);
        planets.add(mars);
        planets.add(jupiter);
        planets.add(saturn);
        planets.add(uranus);
        planets.add(neptune);

        earth.addMoon(moon);

        jupiter.addMoon(io);
        jupiter.addMoon(europa);
        jupiter.addMoon(ganymede);
        jupiter.addMoon(callisto);

        saturn.addMoon(titan);
        saturn.addMoon(rhea);
        saturn.addMoon(iapetus);
        saturn.addMoon(dione);
        saturn.addMoon(tethys);
        saturn.addMoon(enceladus);
        saturn.addMoon(mimas);



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