package com.example.planetsimdemo;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.*;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) {

        SolarSystem solarSystem = new SolarSystem();
        Group root3D = solarSystem.getRoot();

        SubScene subScene = new SubScene(
                root3D,
                800, 600,
                true,
                SceneAntialiasing.BALANCED
        );

        subScene.setFill(Color.BLACK);

        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setTranslateZ(-600);
        camera.setNearClip(0.1);
        camera.setFarClip(10000);
        subScene.setCamera(camera);

        // Light positioned at the sun (center)
        PointLight light = new PointLight(Color.WHITE);
        light.setTranslateX(0);
        light.setTranslateY(0);
        light.setTranslateZ(0);
        root3D.getChildren().add(light);

        // Optional ambient light to soften shadows slightly
        AmbientLight ambient = new AmbientLight(Color.color(0.2, 0.2, 0.2));
        root3D.getChildren().add(ambient);

        // View angle
        Rotate tilt = new Rotate(-45, Rotate.X_AXIS);
        Rotate spin = new Rotate(0, Rotate.Y_AXIS);
        Rotate pitch = new Rotate(0, Rotate.X_AXIS);
        root3D.getTransforms().addAll(tilt, spin, pitch);

        boolean enableRotation = true;

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                solarSystem.update();

                if (enableRotation) {
                    spin.setAngle(spin.getAngle() + 0.1);
                }
            }
        };
        timer.start();

        Group root = new Group(subScene);
        root.setFocusTraversable(true);
        root.requestFocus();

        Scene scene = new Scene(root, 800, 600, true);

        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {

                // Zoom
                case W -> camera.setTranslateZ(camera.getTranslateZ() + 20);
                case S -> camera.setTranslateZ(camera.getTranslateZ() - 20);

//                // Pan
//                case A -> camera.setTranslateX(camera.getTranslateX() - 20);
//                case D -> camera.setTranslateX(camera.getTranslateX() + 20);
//                case Q -> camera.setTranslateY(camera.getTranslateY() - 20);
//                case E -> camera.setTranslateY(camera.getTranslateY() + 20);

                // Rotate LEFT / RIGHT (Y axis)
                case LEFT -> spin.setAngle(spin.getAngle() - 5);
                case RIGHT -> spin.setAngle(spin.getAngle() + 5);

                // Rotate UP / DOWN (X axis)
                case UP -> pitch.setAngle(pitch.getAngle() - 5);
                case DOWN -> pitch.setAngle(pitch.getAngle() + 5);
            }
        });

        stage.setTitle("Planetary Simulation");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}