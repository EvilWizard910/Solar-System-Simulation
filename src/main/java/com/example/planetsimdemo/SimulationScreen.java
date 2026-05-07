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
    private final SolarSystem solarSystem;
    private final Group world= new Group();
    private final Map<String, Sphere> bodyViews = new HashMap<>();
    private final PerspectiveCamera camera=new PerspectiveCamera(true);

    private AnimationTimer timer;
    private long lastTime = 0L;

    public SimulationScreen(SolarSystem solarSystem){
        this.solarSystem = solarSystem;
    }

    public Parent build(){
        buildBodies();

        PointLight light = new PointLight(Color.WHITE);
        light.setTranslateX(0);
        light.setTranslateY(0);
        light.setTranslateZ(0);

        AmbientLight ambientLight = new AmbientLight(Color.color(.25,.25,.25));
        world.getChildren().addAll(light,ambientLight);

        SubScene subScene=new SubScene(world, 1200,900, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.BLACK);

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

    public void buildBodies(){
        world.getChildren().clear();
        bodyViews.clear();

        for(String name:solarSystem.getBodyNames()){
            Body body=solarSystem.getBody(name);
            if(body==null){
                continue;
            }
            double radiusKm= solarSystem.getBodyRadiusKm(name);
            Color color=solarSystem.getBodyColor(name);

            Sphere sphere=new Sphere(toSceneRadius(radiusKm));
            sphere.setMaterial(new PhongMaterial(color==null? Color.WHITE : color));

            updateSpherePosition(sphere,body);
            bodyViews.put(name,sphere);
            world.getChildren().add(sphere);
        }
    }

    private void startAnimation(){
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
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
            }
        };
        timer.start();
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
