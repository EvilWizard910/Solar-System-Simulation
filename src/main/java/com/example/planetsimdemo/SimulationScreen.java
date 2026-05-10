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
import javafx.scene.image.Image;
import javafx.scene.transform.Rotate;
import java.nio.file.Files;
import java.nio.file.Path;


import java.util.HashMap;
import java.util.Map;

import static com.example.planetsimdemo.Conversions.metersToScene;

public class SimulationScreen {
    private SolarSystem solarSystem;
    private final Group world = new Group();
    private final Map<String, Sphere> bodyViews = new HashMap<>();
    private final Map<String, PointLight> starLights = new HashMap<>();
    private final Map<String, PhongMaterial> materialCache = new HashMap<>();
    private final PerspectiveCamera camera = new PerspectiveCamera(true);
    private final Map<String, Rotate> bodyRotations = new HashMap<>();
    private final Map<String, Double> bodySpinAngles = new HashMap<>();

    private AnimationTimer timer;
    private long lastTime = 0L;

    private double timeScale = 1;
    private double sizeScale = 0.0;
    private String focusedBodyName = "Sun";

    private double orbitYaw = 0.0;
    private double orbitPitch = 0.0;
    private double orbitDistance = 10.0;

    private double lastMouseX;
    private double lastMouseY;

    public SimulationScreen(SolarSystem solarSystem) {
        this.solarSystem = solarSystem;
    }

    public Parent build() {
        buildBodies();


        AmbientLight ambientLight = new AmbientLight(Color.color(.25, .25, .25));
        world.getChildren().add(ambientLight);

        SubScene subScene = new SubScene(world, 1200, 900, true, SceneAntialiasing.BALANCED);
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
                orbitDistance = Math.max(0.10, Math.min(200000.0, orbitDistance));
            }

            lastMouseX = mouseX;
            lastMouseY = mouseY;

            event.consume();
        });

        camera.setNearClip(.001);
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

    public void setSolarSystem(SolarSystem solarSystem) {
        this.solarSystem = solarSystem;
        buildBodies();
    }

    public void setTimeScale(double timeScale) {
        this.timeScale = timeScale;
    }

    public void setSizeScale(double sizeScale) {
        this.sizeScale = Math.max(0.0, Math.min(1.0, sizeScale));
        updateBodyRadii();
    }

    public void setFocusedBody(String bodyName) {
        if (bodyName != null && solarSystem.getBody(bodyName) != null) {
            this.focusedBodyName = bodyName;
        }
    }

    public void buildBodies() {
        world.getChildren().removeIf(node -> node instanceof Sphere || node instanceof PointLight);
        bodyViews.clear();
        starLights.clear();
        bodyRotations.clear();
        bodySpinAngles.clear();

        for (String name : solarSystem.getBodyNames()) {
            Body body = solarSystem.getBody(name);
            if (body == null) {
                continue;
            }

            double radiusKm = solarSystem.getBodyRadiusKm(name);
            Color color = solarSystem.getBodyColor(name);
            Sphere sphere = new Sphere(toSceneRadius(radiusKm));
            sphere.setMaterial(createMaterialForBody(name, color));

            Rotate spinRotation = new Rotate(0, Rotate.Y_AXIS);
            sphere.getTransforms().add(spinRotation);

            bodyRotations.put(name, spinRotation);
            bodySpinAngles.put(name, 0.0);

            updateSpherePosition(sphere, body);
            bodyViews.put(name, sphere);
            world.getChildren().add(sphere);

            if("Star".equals(solarSystem.getBodyType(name))) {
                PointLight starLight = new PointLight(color==null? Color.WHITE : color);
                updateLightPosition(starLight,body);
                starLights.put(name, starLight);
                world.getChildren().add(starLight);
            }
        }
    }

    private void updateLightPosition(PointLight pointLight, Body body) {
        pointLight.setTranslateX(metersToScene(body.getX()));
        pointLight.setTranslateY(metersToScene(body.getY()));
        pointLight.setTranslateZ(metersToScene(body.getZ()));
    }

    private void startAnimation() {
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastTime == 0L) {
                    lastTime = now;
                    return;
                }
                double dt = (now - lastTime) / 1_000_000_000.0;
                lastTime = now;

                dt = Math.min(dt, .25);
                solarSystem.updatePhysics(dt * timeScale);

                for (String name : solarSystem.getBodyNames()) {
                    Body body = solarSystem.getBody(name);
                    Sphere sphere = bodyViews.get(name);

                    PointLight light = starLights.get(name);
                    if (body != null && sphere != null) {
                        updateSpherePosition(sphere, body);
                        updateBodySpin(name, dt * timeScale);

                        if (light != null) {
                            updateLightPosition(light, body);
                        }
                    }
                }
                updateCamera();
            }
        };
        timer.start();
    }

    private void updateBodySpin(String bodyName, double scaledDt) {
        Rotate rotation = bodyRotations.get(bodyName);
        if (rotation == null) {
            return;
        }

        double spinSpeed = getSpinSpeedDegreesPerSecond(bodyName);
        double currentAngle = bodySpinAngles.getOrDefault(bodyName, 0.0);

        double nextAngle = (currentAngle + spinSpeed * scaledDt) % 360.0;

        bodySpinAngles.put(bodyName, nextAngle);
        rotation.setAngle(nextAngle);
    }

    private double getSpinSpeedDegreesPerSecond(String bodyName) {
        return solarSystem.getBodyRotationSpeedDegPerSecond(bodyName);
    }

    private void updateBodyRadii() {
        for (String name : bodyViews.keySet()) {
            Sphere sphere = bodyViews.get(name);
            if (sphere == null) {
                continue;
            }

            double radiusKm = solarSystem.getBodyRadiusKm(name);
            sphere.setRadius(toSceneRadius(radiusKm));
        }
    }

    private void updateCamera() {
        Body focused = solarSystem.getBody(focusedBodyName);
        if (focused == null) {
            return;
        }

        double targetX = metersToScene(focused.getX());
        double targetY = metersToScene(focused.getY());
        double targetZ = metersToScene(focused.getZ());

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

    private void updateSpherePosition(Sphere sphere, Body body) {
        sphere.setTranslateX(metersToScene(body.getX()));
        sphere.setTranslateY(metersToScene(body.getY()));
        sphere.setTranslateZ(metersToScene(body.getZ()));
    }

    private PhongMaterial createMaterialForBody(String name, Color fallbackColor) {
        String texturePath = solarSystem.getBodyTexturePath(name);

        if (texturePath == null || texturePath.isBlank()) {
            texturePath = getTexturePath(name);
        }

        if (texturePath != null) {
            PhongMaterial cachedMaterial = materialCache.get(texturePath);
            if (cachedMaterial != null) {
                return cachedMaterial;
            }

            Image texture = loadTexture(texturePath);

            if (texture != null) {
                PhongMaterial material = new PhongMaterial();
                material.setDiffuseMap(texture);
                materialCache.put(texturePath, material);
                return material;
            }
        }

        Color color = fallbackColor == null ? Color.WHITE : fallbackColor;
        String colorKey = "color:" + color;

        return materialCache.computeIfAbsent(colorKey, key -> {
            PhongMaterial material = new PhongMaterial();
            material.setDiffuseColor(color);
            return material;
        });
    }

    private Image loadTexture(String texturePath) {
        try {
            if (texturePath.startsWith("/")) {
                var stream = getClass().getResourceAsStream(texturePath);
                return stream == null ? null : new Image(stream);
            }

            Path path = Path.of(texturePath);

            if (!Files.exists(path)) {
                return null;
            }

            return new Image(path.toUri().toString());
        } catch (Exception e) {
            return null;
        }
    }

    private String getTexturePath(String name) {
        return switch (name.toLowerCase()) {
            case "sun" -> "/textures/sun.jpg";
            case "mercury" -> "/textures/mercury.jpg";
            case "venus" -> "/textures/venus.jpg";
            case "earth" -> "/textures/earth.jpg";
            case "mars" -> "/textures/mars.jpg";
            case "jupiter" -> "/textures/jupiter.jpg";
            case "saturn" -> "/textures/saturn.jpg";
            case "uranus" -> "/textures/uranus.jpg";
            case "neptune" -> "/textures/neptune.jpg";
            case "moon" -> "/textures/moon.jpg";
            default -> null;
        };
    }

    private double toSceneRadius(double radiusKm) {
        double t = sizeScale;

        double realisticRadius = radiusKm / 2_000_000.0;
        double uniformRadius = 10;

        return realisticRadius + t * (uniformRadius - realisticRadius);
    }
}
