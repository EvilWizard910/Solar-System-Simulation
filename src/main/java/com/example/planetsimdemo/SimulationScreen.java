package com.example.planetsimdemo;

import javafx.animation.Animation;
import javafx.animation.AnimationTimer;
import javafx.scene.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.scene.PointLight;
import javafx.scene.AmbientLight;

import java.util.HashMap;
import java.util.Map;

public class SimulationScreen {
    private  SolarSystem solarSystem;
    private final Group world= new Group();
    private final Map<String, Sphere> bodyViews = new HashMap<>();
    private final PerspectiveCamera camera=new PerspectiveCamera(true);

    private AnimationTimer timer;
    private long lastTime = 0L;

    private double timeScale = 5000.0;
    private double sizeScale = 1.0;
    private String focusedBodyName = "Sun";

    private double orbitYaw = 35.0;
    private double orbitPitch = 25.0;
    private double orbitDistance = 200.0;

    private double lastMouseX;
    private double lastMouseY;

    public SimulationScreen(SolarSystem solarSystem){
        this.solarSystem = solarSystem;
    }

    public Parent build(){
        buildBodies();

        PointLight light = new PointLight(Color.WHITE);
        AmbientLight ambientLight = new AmbientLight(Color.color(.25,.25,.25));
        world.getChildren().addAll(light,ambientLight);

        SubScene subScene=new SubScene(world, 1200,900, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.BLACK);

        subScene.setOnMousePressed(event -> {
            lastMouseX = event.getSceneX();
            lastMouseY = event.getSceneY();
            event.consume();
        });

        subScene.setOnMouseDragged(event -> {
            double mouseX = event.getSceneX();
            double mouseY = event.getSceneY();

            double deltaX = mouseX - lastMouseX;
            double deltaY = mouseY - lastMouseY;

            if (event.isPrimaryButtonDown()) {
                double rotateSensitivity = 0.25;

                orbitYaw += deltaX * rotateSensitivity;
                orbitPitch -= deltaY * rotateSensitivity;

                orbitPitch = Math.max(-80, Math.min(80, orbitPitch));
            }

            if (event.isSecondaryButtonDown()) {
                double zoomSensitivity = 0.01;
                double zoomFactor = Math.pow(1.0 + zoomSensitivity, deltaY);

                orbitDistance *= zoomFactor;
                orbitDistance = Math.max(10.0, Math.min(2000.0, orbitDistance));
            }

            lastMouseX = mouseX;
            lastMouseY = mouseY;

            event.consume();
        });

        camera.setNearClip(.01);
        camera.setFarClip(100000);
        camera.setTranslateZ(-200);
        camera.setTranslateY(-30);
        subScene.setCamera(camera);

        BorderPane root = new BorderPane(subScene);
        subScene.widthProperty().bind(root.widthProperty());
        subScene.heightProperty().bind(root.heightProperty());

        startAnimation();
        return root;
    }

    public void setSolarSystem(SolarSystem solarSystem){
        this.solarSystem=solarSystem;
        buildBodies();
    }

    public void setTimeScale(double timeScale){
        this.timeScale=timeScale;
    }

    public void setSizeScale(double sizeScale){
        this.sizeScale=sizeScale;
        buildBodies();
    }

    public void setFocusedBody(String bodyName){
        if(bodyName != null && solarSystem.getBody(bodyName) !=null){
            this.focusedBodyName=bodyName;
        }
    }

    public void buildBodies() {
        world.getChildren().removeIf(node -> node instanceof Sphere);
        bodyViews.clear();

        for (String name : solarSystem.getBodyNames()) {
            Body body = solarSystem.getBody(name);
            if (body == null) {
                continue;
            }

            double radiusKm = solarSystem.getBodyRadiusKm(name);
            Color color = solarSystem.getBodyColor(name);
            Sphere sphere = new Sphere(toSceneRadius(radiusKm));
            sphere.setMaterial(new PhongMaterial(color == null ? Color.WHITE : color));

            updateSpherePosition(sphere, body);
            bodyViews.put(name, sphere);
            world.getChildren().add(sphere);
        }
    }

    private void startAnimation(){
        timer = new AnimationTimer() {
                @Override
                public void handle(long now){
                    if(lastTime == 0l){
                        lastTime = now;
                        return;
                    }
                    double dt = (now-lastTime)/1_000_000_000.0;
                    lastTime=now;

                dt=Math.min(dt,.25);
                solarSystem.updatePhysics(dt*5000);

                for(String name : solarSystem.getBodyNames()){
                    Body body=solarSystem.getBody(name);
                    Sphere sphere=bodyViews.get(name);

                    if(body!=null && sphere!=null){
                        updateSpherePosition(sphere, body);
                    }
                }
                updateCamera();
            }
        };
        timer.start();
    }

    private void updateCamera() {
        Body focused = solarSystem.getBody(focusedBodyName);
        if (focused == null) {
            return;
        }

        double targetX = Conversions.metersToScene(focused.getX());
        double targetY = Conversions.metersToScene(focused.getY());
        double targetZ = Conversions.metersToScene(focused.getZ());

        double yawRad = Math.toRadians(orbitYaw);
        double pitchRad = Math.toRadians(orbitPitch);

        double horizontal = orbitDistance * Math.cos(pitchRad);

        double camX = targetX + Math.sin(yawRad) * horizontal;
        double camY = targetY - Math.sin(pitchRad) * orbitDistance;
        double camZ = targetZ - Math.cos(yawRad) * horizontal;

        camera.setTranslateX(camX);
        camera.setTranslateY(camY);
        camera.setTranslateZ(camZ);

        camera.setRotationAxis(javafx.geometry.Point3D.ZERO); // optional to remove if using transforms
        camera.getTransforms().clear();

        javafx.scene.transform.Rotate yawRotate =
                new javafx.scene.transform.Rotate(Math.toDegrees(Math.atan2(targetX - camX, targetZ - camZ)),
                        javafx.scene.transform.Rotate.Y_AXIS);

        double dx = targetX - camX;
        double dy = targetY - camY;
        double dz = targetZ - camZ;

        javafx.scene.transform.Rotate pitchRotate =
                new javafx.scene.transform.Rotate(
                        -Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz))),
                        javafx.scene.transform.Rotate.X_AXIS
                );

        camera.getTransforms().addAll(yawRotate, pitchRotate);
    }

    private void updateSpherePosition(Sphere sphere, Body body){
        sphere.setTranslateX(Conversions.metersToScene(body.getX()));
        sphere.setTranslateY(Conversions.metersToScene(body.getY()));
        sphere.setTranslateZ(Conversions.metersToScene(body.getZ()));
    }

    private double toSceneRadius(double radiusKm){
        return Math.max(0.5, radiusKm/5000);
    }
}
