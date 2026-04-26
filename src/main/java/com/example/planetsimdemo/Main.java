package com.example.planetsimdemo;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.Locale;

public class Main extends Application {

    private static String formatSimulationSpeed(double secondsPerSecond) {
        if (secondsPerSecond < 60) {
            return String.format(Locale.US, "%.1fx real time", secondsPerSecond);
        }
        if (secondsPerSecond < 3600) {
            return String.format(Locale.US, "%.1f min/sec", secondsPerSecond / 60.0);
        }
        if (secondsPerSecond < 86400) {
            return String.format(Locale.US, "%.1f hr/sec", secondsPerSecond / 3600.0);
        }
        return String.format(Locale.US, "%.2f days/sec", secondsPerSecond / 86400.0);
    }

    private static void disableKeyboardFocus(Control... controls) {
        for (Control control : controls) {
            control.setFocusTraversable(false);
        }
    }

    private static double autoDistanceForRadius(double sceneRadius) {
        return Math.max(0.02, Math.min(20.0, sceneRadius * 20.0 + 0.05));
    }

    private static String formatNumber(double value) {
        return Double.toString(value);
    }

    private static void updateTypeState(
            ComboBox<String> typeBox,
            ComboBox<String> parentBox,
            TextField semiMajorAxisField,
            TextField eccentricityField,
            TextField inclinationField,
            TextField ascendingNodeField,
            TextField argumentOfPeriapsisField,
            TextField trueAnomalyField
    ) {
        String type = typeBox.getValue();
        boolean isMoon = "Moon".equals(type);
        boolean isStar = "Star".equals(type);

        parentBox.setDisable(!isMoon);
        if (!isMoon) {
            parentBox.setValue(null);
        }

        semiMajorAxisField.setDisable(isStar);
        eccentricityField.setDisable(isStar);
        inclinationField.setDisable(isStar);
        ascendingNodeField.setDisable(isStar);
        argumentOfPeriapsisField.setDisable(isStar);
        trueAnomalyField.setDisable(isStar);

        if (isStar) {
            semiMajorAxisField.setText("0.0");
            eccentricityField.setText("0.0");
            inclinationField.setText("0.0");
            ascendingNodeField.setText("0.0");
            argumentOfPeriapsisField.setText("0.0");
            trueAnomalyField.setText("0.0");
        }
    }

    private static void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Invalid Operation");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

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
        StackPane viewport = new StackPane(subScene);
        viewport.setFocusTraversable(true);
        subScene.widthProperty().bind(viewport.widthProperty());
        subScene.heightProperty().bind(viewport.heightProperty());

        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setNearClip(0.0001);
        camera.setFarClip(100_000);

        javafx.scene.transform.Rotate cameraYawRotate =
                new javafx.scene.transform.Rotate(0, javafx.scene.transform.Rotate.Y_AXIS);
        javafx.scene.transform.Rotate cameraPitchRotate =
                new javafx.scene.transform.Rotate(0, javafx.scene.transform.Rotate.X_AXIS);
        camera.getTransforms().addAll(cameraYawRotate, cameraPitchRotate);

        subScene.setCamera(camera);

        PointLight light = new PointLight(Color.WHITE);
        light.setTranslateX(0);
        light.setTranslateY(0);
        light.setTranslateZ(0);
        root3D.getChildren().add(light);

        AmbientLight ambient = new AmbientLight(Color.color(0.2, 0.2, 0.2));
        root3D.getChildren().add(ambient);

        javafx.scene.transform.Rotate tilt =
                new javafx.scene.transform.Rotate(90, javafx.scene.transform.Rotate.X_AXIS);
        javafx.scene.transform.Rotate spin =
                new javafx.scene.transform.Rotate(0, javafx.scene.transform.Rotate.Y_AXIS);
        javafx.scene.transform.Rotate pitch =
                new javafx.scene.transform.Rotate(0, javafx.scene.transform.Rotate.X_AXIS);
        root3D.getTransforms().addAll(tilt, spin, pitch);

        long[] lastTime = {0};
        final double minSimulationSpeed = 1.0;
        final double maxSimulationSpeed = 604800.0;
        final double[] simulationSpeed = {1.0};

        Body[] currentFocus = {solarSystem.getBody("Sun")};
        double[] orbitYaw = {35.0};
        double[] orbitPitch = {25.0};
        double[] orbitDistance = {7.0};

        solarSystem.setViewScale(0.0);

        BorderPane root = new BorderPane();
        root.setCenter(viewport);

        VBox controlsBox = new VBox(10);
        controlsBox.setPrefWidth(360);
        controlsBox.setMinWidth(320);

        Button startStopButton = new Button("⏹️");

        Label scaleLabel = new Label("Body Scale");
        Slider scaleSlider = new Slider(0, 1, 0.0);
        scaleSlider.setShowTickLabels(true);
        scaleSlider.setShowTickMarks(true);
        scaleSlider.setMajorTickUnit(0.5);
        scaleSlider.setBlockIncrement(0.1);
        scaleSlider.valueProperty().addListener((obs, oldValue, newValue) ->
                solarSystem.setViewScale(newValue.doubleValue()));

        Label dtLabel = new Label("Simulation Speed");
        Slider dtSlider = new Slider(0, 1, 0);
        dtSlider.setShowTickLabels(true);
        dtSlider.setShowTickMarks(true);
        dtSlider.setMajorTickUnit(0.25);
        dtSlider.setBlockIncrement(0.05);
        dtSlider.valueProperty().addListener((obs, oldValue, newValue) -> {
            double t = newValue.doubleValue();
            double speed = minSimulationSpeed * Math.pow(maxSimulationSpeed / minSimulationSpeed, t);
            simulationSpeed[0] = speed;
            dtLabel.setText("Simulation Speed: " + formatSimulationSpeed(speed));
        });

        Label focusLabel = new Label("Focus Body");
        ComboBox<String> focusBox = new ComboBox<>();
        focusBox.getItems().setAll(solarSystem.getBodyNames());
        focusBox.setValue("Sun");

        Label yawLabel = new Label("Camera Yaw");
        Slider yawSlider = new Slider(-180, 180, orbitYaw[0]);
        yawSlider.setShowTickLabels(true);
        yawSlider.setShowTickMarks(true);
        yawSlider.setMajorTickUnit(90);
        yawSlider.setBlockIncrement(5);
        yawSlider.valueProperty().addListener((obs, oldValue, newValue) ->
                orbitYaw[0] = newValue.doubleValue());

        Label pitchLabel = new Label("Camera Pitch");
        Slider pitchSlider = new Slider(-80, 80, orbitPitch[0]);
        pitchSlider.setShowTickLabels(true);
        pitchSlider.setShowTickMarks(true);
        pitchSlider.setMajorTickUnit(20);
        pitchSlider.setBlockIncrement(2);
        pitchSlider.valueProperty().addListener((obs, oldValue, newValue) ->
                orbitPitch[0] = newValue.doubleValue());

        Label distanceLabel = new Label("Camera Distance");
        Slider distanceSlider = new Slider(0.1, 500.0, orbitDistance[0]);
        distanceSlider.setShowTickLabels(true);
        distanceSlider.setShowTickMarks(true);
        distanceSlider.setMajorTickUnit(5);
        distanceSlider.setBlockIncrement(0.1);
        distanceSlider.valueProperty().addListener((obs, oldValue, newValue) ->
                orbitDistance[0] = newValue.doubleValue());

        ComboBox<String> bodyBox = new ComboBox<>();
        bodyBox.getItems().setAll(solarSystem.getBodyNames());

        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.getItems().setAll("Star", "Planet", "Moon");
        typeBox.setValue("Planet");

        ComboBox<String> parentBox = new ComboBox<>();
        parentBox.getItems().setAll(solarSystem.getBodyNames());
        parentBox.setDisable(true);

        Button refreshBodiesButton = new Button("Refresh Bodies");
        Runnable refreshLists = () -> {
            String selectedBody = bodyBox.getValue();
            String selectedFocus = focusBox.getValue();
            String selectedParent = parentBox.getValue();

            bodyBox.getItems().setAll(solarSystem.getBodyNames());
            focusBox.getItems().setAll(solarSystem.getBodyNames());
            parentBox.getItems().setAll(solarSystem.getBodyNames());

            if (selectedBody != null && solarSystem.getBody(selectedBody) != null) {
                bodyBox.setValue(selectedBody);
            }
            if (selectedFocus != null && solarSystem.getBody(selectedFocus) != null) {
                focusBox.setValue(selectedFocus);
            } else {
                focusBox.setValue("Sun");
            }
            if (selectedParent != null && solarSystem.getBodyNames().contains(selectedParent)) {
                parentBox.setValue(selectedParent);
            }
        };
        refreshBodiesButton.setOnAction(e -> refreshLists.run());

        TextField nameField = new TextField();
        nameField.setPromptText("Name");

        TextField massField = new TextField();
        massField.setPromptText("Mass");

        TextField radiusField = new TextField();
        radiusField.setPromptText("Radius (km)");

        TextField semiMajorAxisField = new TextField();
        semiMajorAxisField.setPromptText("Semi-major axis (AU)");

        TextField eccentricityField = new TextField();
        eccentricityField.setPromptText("Eccentricity");

        TextField inclinationField = new TextField();
        inclinationField.setPromptText("Inclination (deg)");

        TextField ascendingNodeField = new TextField();
        ascendingNodeField.setPromptText("Ascending node (deg)");

        TextField argumentOfPeriapsisField = new TextField();
        argumentOfPeriapsisField.setPromptText("Argument of periapsis (deg)");

        TextField trueAnomalyField = new TextField();
        trueAnomalyField.setPromptText("True anomaly (deg)");


        ColorPicker colorPicker = new ColorPicker(Color.WHITE);

        Runnable clearBodySelection = () -> {
            bodyBox.setValue(null);
            nameField.clear();
            massField.clear();
            radiusField.clear();
            semiMajorAxisField.clear();
            eccentricityField.clear();
            inclinationField.clear();
            ascendingNodeField.clear();
            argumentOfPeriapsisField.clear();
            trueAnomalyField.clear();
            colorPicker.setValue(Color.WHITE);
            typeBox.setValue("Planet");
            parentBox.setValue(null);
            updateTypeState(typeBox, parentBox, semiMajorAxisField, eccentricityField, inclinationField,
                    ascendingNodeField, argumentOfPeriapsisField, trueAnomalyField);
        };

        Button clearSelectionButton = new Button("Clear Selection");
        clearSelectionButton.setOnAction(e -> clearBodySelection.run());

        typeBox.valueProperty().addListener((obs, oldValue, newValue) ->
                updateTypeState(typeBox, parentBox, semiMajorAxisField, eccentricityField, inclinationField,
                        ascendingNodeField, argumentOfPeriapsisField, trueAnomalyField));

        bodyBox.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null) {
                return;
            }

            Body selected = solarSystem.getBody(newValue);
            if (selected == null) {
                return;
            }
            SolarSystem.OrbitElements orbit = solarSystem.getOrbitElements(newValue);
            nameField.setText(selected.getName());
            massField.setText(formatNumber(selected.getMass()));
            radiusField.setText(formatNumber(solarSystem.getBodyRadiusKm(newValue)));
            colorPicker.setValue(solarSystem.getBodyColor(newValue));
            typeBox.setValue(solarSystem.getBodyType(newValue));
            parentBox.getItems().setAll(solarSystem.getBodyNames());
            parentBox.setValue(solarSystem.getOrbitParent(newValue));

            if (orbit != null) {
                semiMajorAxisField.setText(formatNumber(orbit.semiMajorAxisAu()));
                eccentricityField.setText(formatNumber(orbit.eccentricity()));
                inclinationField.setText(formatNumber(orbit.inclinationDeg()));
                ascendingNodeField.setText(formatNumber(orbit.ascendingNodeDeg()));
                argumentOfPeriapsisField.setText(formatNumber(orbit.argumentOfPeriapsisDeg()));
                trueAnomalyField.setText(formatNumber(orbit.trueAnomalyDeg()));
            } else {
                semiMajorAxisField.setText("0.0");
                eccentricityField.setText("0.0");
                inclinationField.setText("0.0");
                ascendingNodeField.setText("0.0");
                argumentOfPeriapsisField.setText("0.0");
                trueAnomalyField.setText("0.0");
            }
            updateTypeState(
                    typeBox, parentBox,
                    semiMajorAxisField, eccentricityField, inclinationField,
                    ascendingNodeField, argumentOfPeriapsisField, trueAnomalyField
            );
            /*String bodyType = solarSystem.getBodyType(newValue);
            typeBox.setValue(bodyType);

            String parent = solarSystem.getParent(newValue);
            parentBox.getItems().setAll(solarSystem.getBodyNames());
            parentBox.setValue(parent);

            updateTypeState(typeBox, parentBox, distanceAuField, eccentricityField);*/
        });

        Button addButton = new Button("Add");
        Button editButton = new Button("Edit");
        Button removeButton = new Button("Remove");

        addButton.setOnAction(e -> {
            try {
                String name = nameField.getText().trim();
                if (name.isEmpty()) {
                    showError("Name is required.");
                    return;
                }

                String type = typeBox.getValue();
                String parent = "Moon".equals(type) ? parentBox.getValue() : null;
                if ("Moon".equals(type) && (parent == null || parent.isBlank())) {
                    showError("Moons must have a parent planet.");
                    return;
                }

                double mass = Double.parseDouble(massField.getText().trim());
                double radiusKm = Double.parseDouble(radiusField.getText().trim());
                double semiMajorAxisAu = "Star".equals(type) ? 0.0 : Double.parseDouble(semiMajorAxisField.getText().trim());
                double eccentricity = "Star".equals(type) ? 0.0 : Double.parseDouble(eccentricityField.getText().trim());
                double inclinationDeg = "Star".equals(type) ? 0.0 : Double.parseDouble(inclinationField.getText().trim());
                double ascendingNodeDeg = "Star".equals(type) ? 0.0 : Double.parseDouble(ascendingNodeField.getText().trim());
                double argumentOfPeriapsisDeg = "Star".equals(type) ? 0.0 : Double.parseDouble(argumentOfPeriapsisField.getText().trim());
                double trueAnomalyDeg = "Star".equals(type) ? 0.0 : Double.parseDouble(trueAnomalyField.getText().trim());

                boolean added=solarSystem.addNewBody(
                        name, type, parent, mass, radiusKm,
                        semiMajorAxisAu, eccentricity, inclinationDeg,
                        ascendingNodeDeg, argumentOfPeriapsisDeg, trueAnomalyDeg,
                        colorPicker.getValue()
                );

                /*double distanceAu = "Star".equals(type) ? 0.0 : Double.parseDouble(distanceAuField.getText().trim());
                double angleDeg = "Star".equals(type) ? 0.0 : Double.parseDouble(angleField.getText().trim());
                Color color = colorPicker.getValue();

                boolean added = solarSystem.addNewBody(name, type, parent, mass, radiusKm, distanceAu, angleDeg, color);*/
                if (!added) {
                    showError("Could not add body. Check for duplicate names or invalid moon parent.");
                    return;
                }

                refreshLists.run();
                bodyBox.setValue(name);
                focusBox.setValue(name);
            } catch (Exception ex) {
                showError("Please enter valid numeric values.");
            }
        });

        editButton.setOnAction(e -> {
            String selected = bodyBox.getValue();
            if (selected == null) {
                showError("Select a body to edit.");
                return;
            }

            try {
                String newName = nameField.getText().trim();
                if (newName.isEmpty()) {
                    showError("Name is required.");
                    return;
                }

                String type = typeBox.getValue();
                String parent = "Moon".equals(type) ? parentBox.getValue() : null;
                if ("Moon".equals(type) && (parent == null || parent.isBlank())) {
                    showError("Moons must have a parent planet.");
                    return;
                }

                double mass = Double.parseDouble(massField.getText().trim());
                double radiusKm = Double.parseDouble(radiusField.getText().trim());
                double semiMajorAxisAu = "Star".equals(type) ? 0.0 : Double.parseDouble(semiMajorAxisField.getText().trim());
                double eccentricity = "Star".equals(type) ? 0.0 : Double.parseDouble(eccentricityField.getText().trim());
                double inclinationDeg = "Star".equals(type) ? 0.0 : Double.parseDouble(inclinationField.getText().trim());
                double ascendingNodeDeg = "Star".equals(type) ? 0.0 : Double.parseDouble(ascendingNodeField.getText().trim());
                double argumentOfPeriapsisDeg = "Star".equals(type) ? 0.0 : Double.parseDouble(argumentOfPeriapsisField.getText().trim());
                double trueAnomalyDeg = "Star".equals(type) ? 0.0 : Double.parseDouble(trueAnomalyField.getText().trim());

                Color color = colorPicker.getValue();

                boolean wasFocused = currentFocus[0] != null && selected.equals(currentFocus[0].getName());

                boolean updated = solarSystem.updateBody(selected, newName, type, parent, mass, radiusKm, semiMajorAxisAu, eccentricity, inclinationDeg,
                        ascendingNodeDeg, argumentOfPeriapsisDeg, trueAnomalyDeg, color);
                if (!updated) {
                    showError("Could not edit body. Names must be unique, moons need a planet parent, and parents with moons cannot stop being planets.");
                    return;
                }

                refreshLists.run();
                bodyBox.setValue(newName);

                if (wasFocused) {
                    currentFocus[0] = solarSystem.getBody(newName);
                    focusBox.setValue(newName);
                }
            } catch (Exception ex) {
                showError("Please enter valid numeric values.");
            }
        });

        removeButton.setOnAction(e -> {
            String selected = bodyBox.getValue();
            if (selected == null) {
                showError("Select a body to remove.");
                return;
            }

            boolean wasFocused = currentFocus[0] != null && selected.equals(currentFocus[0].getName());

            boolean removed = solarSystem.removeBody(selected);
            if (!removed) {
                showError("Remove moons orbiting this body first.");
                return;
            }

            refreshLists.run();

            if (wasFocused) {
                currentFocus[0] = solarSystem.getBody("Sun");
                focusBox.setValue("Sun");
            }

            clearBodySelection.run();
        });

        Runnable resetCamera = () -> {
            currentFocus[0] = solarSystem.getBody("Sun");
            focusBox.setValue("Sun");

            orbitYaw[0] = 35.0;
            orbitPitch[0] = 25.0;

            Body sun = solarSystem.getBody("Sun");
            orbitDistance[0] = sun == null ? 7.0 : autoDistanceForRadius(sun.getView().getRadius());

            yawSlider.setValue(orbitYaw[0]);
            pitchSlider.setValue(orbitPitch[0]);
            distanceSlider.setValue(orbitDistance[0]);
        };

        Button resetCameraButton = new Button("Reset Camera");
        resetCameraButton.setOnAction(e -> resetCamera.run());

        focusBox.setOnAction(e -> {
            String selected = focusBox.getValue();
            if (selected == null) return;

            Body focused = solarSystem.getBody(selected);
            if (focused == null) return;

            currentFocus[0] = focused;
            orbitDistance[0] = autoDistanceForRadius(focused.getView().getRadius());
            distanceSlider.setValue(orbitDistance[0]);
        });

        final boolean[] isRunning = {true};

        final AnimationTimer timer = new AnimationTimer() {
            private static final double MAX_PHYSICS_STEP = 120.0; // 5 minutes
            private static final double MAX_FRAME_DT = 0.25;      // avoid giant jumps after lag

            @Override
            public void handle(long now) {
                if (lastTime[0] == 0) {
                    lastTime[0] = now;
                    return;
                }

                double dt = (now - lastTime[0]) / 1_000_000_000.0;
                lastTime[0] = now;
                dt = Math.min(dt, MAX_FRAME_DT);

                double simulatedDt = dt * simulationSpeed[0];

                while (simulatedDt > 0) {
                    double step = Math.min(simulatedDt, MAX_PHYSICS_STEP);
                    solarSystem.updatePhysics(step);
                    simulatedDt -= step;
                }

                if (currentFocus[0] != null) {
                    double targetX = Conversions.metersToScene(currentFocus[0].getX());
                    double targetY = -Conversions.metersToScene(currentFocus[0].getZ());
                    double targetZ = Conversions.metersToScene(currentFocus[0].getY());

                    double yawRad = Math.toRadians(orbitYaw[0]);
                    double pitchRad = Math.toRadians(orbitPitch[0]);

                    double horizontal = orbitDistance[0] * Math.cos(pitchRad);

                    double camX = targetX + Math.sin(yawRad) * horizontal;
                    double camY = targetY - Math.sin(pitchRad) * orbitDistance[0];
                    double camZ = targetZ - Math.cos(yawRad) * horizontal;

                    camera.setTranslateX(camX);
                    camera.setTranslateY(camY);
                    camera.setTranslateZ(camZ);

                    double dx = targetX - camX;
                    double dy = targetY - camY;
                    double dz = targetZ - camZ;

                    double lookYaw = Math.toDegrees(Math.atan2(dx, dz));
                    double lookPitch = -Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));

                    cameraYawRotate.setAngle(lookYaw);
                    cameraPitchRotate.setAngle(lookPitch);
                }
            }
        };

        startStopButton.setOnAction(e -> {
            if (isRunning[0]) {
                timer.stop();
                startStopButton.setText("▶️");
                isRunning[0] = false;
            } else {
                lastTime[0] = 0;
                timer.start();
                startStopButton.setText("⏹️");
                isRunning[0] = true;
            }
        });

        disableKeyboardFocus(
                startStopButton,
                scaleSlider,
                dtSlider,
                focusBox,
                yawSlider,
                pitchSlider,
                distanceSlider,
                bodyBox,
                typeBox,
                parentBox,
                colorPicker,
                refreshBodiesButton,
                clearSelectionButton,
                addButton,
                editButton,
                removeButton,
                resetCameraButton
        );

        resetCamera.run();
        timer.start();

        controlsBox.getChildren().addAll(
                startStopButton,
                scaleLabel,
                scaleSlider,
                dtLabel,
                dtSlider,
                new Separator(),
                focusLabel,
                focusBox,
                yawLabel,
                yawSlider,
                pitchLabel,
                pitchSlider,
                distanceLabel,
                distanceSlider,
                new Separator(),
                new Label("Bodies"),
                bodyBox,
                new HBox(5, refreshBodiesButton, clearSelectionButton),
                nameField,
                massField,
                radiusField,
                semiMajorAxisField,
                eccentricityField,
                inclinationField,
                ascendingNodeField,
                argumentOfPeriapsisField,
                trueAnomalyField,
                new Label("Type"),
                typeBox,
                new Label("Parent Planet"),
                parentBox,
                new Label("Color"),
                colorPicker,
                new HBox(5, addButton, editButton, removeButton),
                resetCameraButton
        );

        root.setRight(controlsBox);

        Scene scene = new Scene(root, 1400, 900, true);

        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (scene.getFocusOwner() instanceof TextInputControl) {
                return;
            }

            boolean handled = true;
            KeyCode code = event.getCode();

            switch (code) {
                case UP -> distanceSlider.setValue(Math.max(distanceSlider.getMin(), distanceSlider.getValue() - 0.1));
                case DOWN -> distanceSlider.setValue(Math.min(distanceSlider.getMax(), distanceSlider.getValue() + 0.1));

                case LEFT -> yawSlider.setValue(yawSlider.getValue() - 5);
                case RIGHT -> yawSlider.setValue(yawSlider.getValue() + 5);

                case W -> pitchSlider.setValue(Math.min(pitchSlider.getMax(), pitchSlider.getValue() + 3));
                case S -> pitchSlider.setValue(Math.max(pitchSlider.getMin(), pitchSlider.getValue() - 3));

                case A -> yawSlider.setValue(yawSlider.getValue() - 1);
                case D -> yawSlider.setValue(yawSlider.getValue() + 1);

                case I -> distanceSlider.setValue(Math.max(distanceSlider.getMin(), distanceSlider.getValue() - 0.01));
                case K -> distanceSlider.setValue(Math.min(distanceSlider.getMax(), distanceSlider.getValue() + 0.01));

                case J -> pitchSlider.setValue(Math.min(pitchSlider.getMax(), pitchSlider.getValue() + 1));
                case L -> pitchSlider.setValue(Math.max(pitchSlider.getMin(), pitchSlider.getValue() - 1));

                default -> handled = false;
            }

            if (handled) {
                event.consume();
            }
        });

        final double[] lastMouseX = {0};
        final double[] lastMouseY = {0};

        viewport.setOnMouseClicked(event -> {
            viewport.requestFocus();
            event.consume();
        });

        viewport.setOnMousePressed(event -> {
            viewport.requestFocus();

            lastMouseX[0] = event.getSceneX();
            lastMouseY[0] = event.getSceneY();

            event.consume();
        });

        viewport.setOnMouseDragged(event -> {
            double mouseX = event.getSceneX();
            double mouseY = event.getSceneY();

            double deltaX = mouseX - lastMouseX[0];
            double deltaY = mouseY - lastMouseY[0];

            if (event.isPrimaryButtonDown()) {
                double rotateSensitivity = 0.25;

                double newYaw = yawSlider.getValue() + deltaX * rotateSensitivity;
                double newPitch = pitchSlider.getValue() - deltaY * rotateSensitivity;

                yawSlider.setValue(
                        Math.max(yawSlider.getMin(), Math.min(yawSlider.getMax(), newYaw))
                );

                pitchSlider.setValue(
                        Math.max(pitchSlider.getMin(), Math.min(pitchSlider.getMax(), newPitch))
                );
            }

            if (event.isSecondaryButtonDown()) {
                double zoomSensitivity = 0.01;
                double zoomFactor = Math.pow(1.0 + zoomSensitivity, deltaY);

                double newDistance = distanceSlider.getValue() * zoomFactor;

                distanceSlider.setValue(
                        Math.max(distanceSlider.getMin(), Math.min(distanceSlider.getMax(), newDistance))
                );
            }

            lastMouseX[0] = mouseX;
            lastMouseY[0] = mouseY;

            event.consume();
        });

        viewport.setOnContextMenuRequested(event -> event.consume());

        stage.setTitle("Planetary Simulation");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();

        viewport.requestFocus();
    }

    public static void main(String[] args) {
        launch();
    }
}