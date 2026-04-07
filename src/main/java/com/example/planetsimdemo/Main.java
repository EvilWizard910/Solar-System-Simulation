package com.example.planetsimdemo;

import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
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
                1920, 1080,
                true,
                SceneAntialiasing.BALANCED
        );

        subScene.setFill(Color.BLACK);

        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setTranslateZ(-600);
        camera.setNearClip(1);
        camera.setFarClip(20000);
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
        Rotate tilt = new Rotate(90, Rotate.X_AXIS);
        Rotate spin = new Rotate(0, Rotate.Y_AXIS);
        Rotate pitch = new Rotate(0, Rotate.X_AXIS);
        root3D.getTransforms().addAll(tilt, spin, pitch);

        boolean enableRotation = true;

        long[] lastTime = {0};
        double simulationSpeed = 86400; // 1 real second = 1 simulated day

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastTime[0] == 0) {
                    lastTime[0] = now;
                    return;
                }

                double dt = (now - lastTime[0]) / 1_000_000_000.0;
                lastTime[0] = now;

                solarSystem.updatePhysics(dt * simulationSpeed);
            }
        };

        /* AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                solarSystem.update();

                if (enableRotation) {
                    spin.setAngle(spin.getAngle() + 0.1);
                }
            }
        };*/

        timer.start();

        BorderPane root = new BorderPane();

        // put the 3D scene in the center
        root.setCenter(subScene);

        // create controls
        VBox controlsBox = new VBox(10);

        Button startStopButton = new Button("⏹️");

        final boolean[] isRunning = {true};

        startStopButton.setOnAction(_ -> {
            if (isRunning[0]) {
                timer.stop();
                startStopButton.setText("▶️");
                isRunning[0] = false;
            } else {
                timer.start();
                startStopButton.setText("⏹️");
                isRunning[0] = true;
            }
        });

        controlsBox.getChildren().add(startStopButton);

        // place controls on the right side
        root.setRight(controlsBox);

        Scene scene = new Scene(root, 3200, 3200, true);

        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {

                // Zoom
                case UP -> camera.setTranslateZ(camera.getTranslateZ() + 20);
                case DOWN -> camera.setTranslateZ(camera.getTranslateZ() - 20);

//                // Pan
               case A -> camera.setTranslateX(camera.getTranslateX() - 20);
               case D -> camera.setTranslateX(camera.getTranslateX() + 20);
               case W -> camera.setTranslateY(camera.getTranslateY() - 20);
               case S -> camera.setTranslateY(camera.getTranslateY() + 20);

                // Rotate LEFT / RIGHT (Y axis)
                //case LEFT -> spin.setAngle(spin.getAngle() - 5);
                //case RIGHT -> spin.setAngle(spin.getAngle() + 5);

                // Rotate UP / DOWN (X axis)
                case  LEFT-> pitch.setAngle(pitch.getAngle() - 5);
                case RIGHT -> pitch.setAngle(pitch.getAngle() + 5);
            }
        });

        stage.setTitle("Planetary Simulation");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}