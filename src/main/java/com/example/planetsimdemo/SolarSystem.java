package com.example.planetsimdemo;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;

import java.util.*;

import static com.example.planetsimdemo.Conversions.*;

public class SolarSystem {

    private static final String TYPE_STAR = "Star";
    private static final String TYPE_PLANET = "Planet";
    private static final String TYPE_MOON = "Moon";

    private final Group root = new Group();
    private final List<Body> bodies = new ArrayList<>();
    private final Map<String, Body> map = new HashMap<>();
    private final Map<Body, Double> baseRadii = new HashMap<>();

    private final Map<String, Double> logicalRadiiKm = new HashMap<>();
    private final Map<String, Double> logicalDistancesAu = new HashMap<>();
    private final Map<String, Double> orbitalAnglesDeg = new HashMap<>();
    private final Map<String, String> bodyTypes = new HashMap<>();
    private final Map<String, String> orbitParents = new HashMap<>();
    private final Map<String, Color> bodyColors = new HashMap<>();

    private double currentViewScale = 0.0;

    private static class BodyState {
        String name;
        String type;
        String parent;
        double mass;
        double radiusKm;
        double distanceAu;
        double angleDeg;
        Color color;

        BodyState(String name, String type, String parent,
                  double mass, double radiusKm, double distanceAu,
                  double angleDeg, Color color) {
            this.name = name;
            this.type = type;
            this.parent = parent;
            this.mass = mass;
            this.radiusKm = radiusKm;
            this.distanceAu = distanceAu;
            this.angleDeg = angleDeg;
            this.color = color;
        }
    }

    public SolarSystem() {
        init();
    }

    public static double toSceneRadiusFromKm(String bodyName, double radiusKm) {
        return Conversions.metersToScene(radiusKm * 1000.0);
    }

    public static double toSceneRadiusFromKm(double radiusKm) {
        return toSceneRadiusFromKm("Body", radiusKm);
    }

    private static double sceneRadiusToKm(double sceneRadius) {
        return sceneRadius * 2.0e9 / 1000.0;
    }

    private String normalizeType(String type) {
        if (TYPE_STAR.equalsIgnoreCase(type)) return TYPE_STAR;
        if (TYPE_MOON.equalsIgnoreCase(type)) return TYPE_MOON;
        return TYPE_PLANET;
    }

    private double normalizeAngle(double angleDeg) {
        double a = angleDeg % 360.0;
        return a < 0 ? a + 360.0 : a;
    }

    private double bodyScaleMultiplier() {
        return 1.0 + currentViewScale * 50.0;
    }

    private void applyScaleToBody(Body body) {
        double base = baseRadii.get(body);
        body.getView().setRadius(base * bodyScaleMultiplier());
    }

    private Color extractColor(Body body) {
        if (body != null && body.getView().getMaterial() instanceof PhongMaterial material) {
            Color c = material.getDiffuseColor();
            if (c != null) return c;
        }
        return Color.WHITE;
    }

    private void registerBody(Body body, double radiusKm, double distanceAu,
                              double angleDeg, String type, String parentName, Color color) {
        bodies.add(body);
        map.put(body.getName(), body);
        root.getChildren().add(body.getView());

        double sceneRadius = toSceneRadiusFromKm(body.getName(), radiusKm);
        baseRadii.put(body, sceneRadius);
        logicalRadiiKm.put(body.getName(), radiusKm);
        logicalDistancesAu.put(body.getName(), distanceAu);
        orbitalAnglesDeg.put(body.getName(), normalizeAngle(angleDeg));
        bodyTypes.put(body.getName(), type);
        orbitParents.put(body.getName(), parentName);
        bodyColors.put(body.getName(), color);

        applyScaleToBody(body);
    }

    private void removeBodyInternal(String name) {
        Body body = map.remove(name);
        if (body == null) return;

        bodies.remove(body);
        root.getChildren().remove(body.getView());
        baseRadii.remove(body);

        logicalRadiiKm.remove(name);
        logicalDistancesAu.remove(name);
        orbitalAnglesDeg.remove(name);
        bodyTypes.remove(name);
        orbitParents.remove(name);
        bodyColors.remove(name);
    }

    private List<BodyState> getChildStates(String parentName) {
        List<BodyState> children = new ArrayList<>();
        for (String name : map.keySet()) {
            if (parentName.equals(orbitParents.get(name))) {
                Body body = map.get(name);
                children.add(new BodyState(
                        name,
                        bodyTypes.get(name),
                        orbitParents.get(name),
                        body.getMass(),
                        logicalRadiiKm.get(name),
                        logicalDistancesAu.get(name),
                        orbitalAnglesDeg.getOrDefault(name, 0.0),
                        bodyColors.getOrDefault(name, extractColor(body))
                ));
            }
        }
        return children;
    }

    private Body createBodyByType(String name, String type, String parentName,
                                  double mass, double radiusKm, double distanceAu,
                                  double angleDeg, Color color) {
        String normalizedType = normalizeType(type);
        double sceneRadius = toSceneRadiusFromKm(name, radiusKm);

        Sphere sphere = new Sphere(sceneRadius);
        sphere.setMaterial(new PhongMaterial(color));

        if (TYPE_STAR.equals(normalizedType)) {
            return new Body(name, mass, sphere, 0, 0, 0, 0, 0, 0);
        }

        double angleRad = Math.toRadians(normalizeAngle(angleDeg));

        if (TYPE_MOON.equals(normalizedType)) {
            if (parentName == null || parentName.isBlank()) return null;

            Body parent = map.get(parentName);
            if (parent == null) return null;

            String parentType = bodyTypes.get(parentName);
            if (!TYPE_PLANET.equals(parentType)) return null;

            double distanceMeters = distanceAu * AU_IN_METERS;
            double orbitalSpeed = Math.sqrt(G * parent.getMass() / distanceMeters);

            double relX = Math.cos(angleRad) * distanceMeters;
            double relZ = Math.sin(angleRad) * distanceMeters;

            double relVx = -Math.sin(angleRad) * orbitalSpeed;
            double relVz = Math.cos(angleRad) * orbitalSpeed;

            return new Body(
                    name,
                    mass,
                    sphere,
                    parent.getX() + relX,
                    parent.getY(),
                    parent.getZ() + relZ,
                    parent.getVx() + relVx,
                    parent.getVy(),
                    parent.getVz() + relVz
            );
        }

        double distanceMeters = distanceAu * AU_IN_METERS;
        double orbitalSpeed = Math.sqrt(G * massOfSun / distanceMeters);

        double x = Math.cos(angleRad) * distanceMeters;
        double z = Math.sin(angleRad) * distanceMeters;

        double vx = -Math.sin(angleRad) * orbitalSpeed;
        double vz = Math.cos(angleRad) * orbitalSpeed;

        return new Body(
                name,
                mass,
                sphere,
                x, 0, z,
                vx, 0, vz
        );
    }

    private void make(String name, String type, String parentName,
                      double mass, double radiusKm, double distanceAu,
                      double angleDeg, Color color) {
        Body body = createBodyByType(name, type, parentName, mass, radiusKm, distanceAu, angleDeg, color);
        if (body != null) {
            registerBody(body, radiusKm, distanceAu, angleDeg, normalizeType(type), parentName, color);
        }
    }

    private void init() {
        make("Sun", TYPE_STAR, null, massOfSun, 700000, 0.0, 0.0, Color.YELLOW);

        make("Mercury", TYPE_PLANET, null, Mercury_Mass, 2439.7, 0.39, 0.0, Color.MISTYROSE);
        make("Venus", TYPE_PLANET, null, Venus_Mass, 6051.8, 0.72, 25.0, Color.BURLYWOOD);
        make("Earth", TYPE_PLANET, null, EARTH_MASS, 6371, 1.0, 50.0, Color.DODGERBLUE);
        make("Mars", TYPE_PLANET, null, Mars_Mass, 3389.5, 1.52, 75.0, Color.ORANGERED);
        make("Jupiter", TYPE_PLANET, null, Jupiter_Mass, 69911, 5.2, 120.0, Color.CORAL);
        make("Saturn", TYPE_PLANET, null, Saturn_Mass, 58232, 9.54, 160.0, Color.DARKSALMON);
        make("Uranus", TYPE_PLANET, null, Uranus_Mass, 25362, 19.2, 210.0, Color.DARKTURQUOISE);
        make("Neptune", TYPE_PLANET, null, Neptune_Mass, 24622, 30.02, 260.0, Color.MIDNIGHTBLUE);

        make("Moon", TYPE_MOON, "Earth", Moon_Mass, 1737.4, 0.0025695, 0.0, Color.LIGHTGRAY);
        make("Io", TYPE_MOON, "Jupiter", Io_Mass, 1821.6, 421800000.0 / AU_IN_METERS, 0.0, Color.LIMEGREEN);
        make("Europa", TYPE_MOON, "Jupiter", Europa_mass, 1560.8, 671100000.0 / AU_IN_METERS, 45.0, Color.WHITE);
        make("Ganymede", TYPE_MOON, "Jupiter", Ganymede_mass, 2631.2, 1070400000.0 / AU_IN_METERS, 90.0, Color.GRAY);
        make("Callisto", TYPE_MOON, "Jupiter", Callisto_mass, 2410.3, 1882700000.0 / AU_IN_METERS, 135.0, Color.DARKGRAY);
    }

    public boolean addNewBody(String name, String type, String parentName,
                              double mass, double radiusKm, double distanceAu,
                              double angleDeg, Color color) {
        if (name == null || name.isBlank() || map.containsKey(name)) {
            return false;
        }

        String normalizedType = normalizeType(type);
        if (!TYPE_MOON.equals(normalizedType)) {
            parentName = null;
        }

        Body body = createBodyByType(name, normalizedType, parentName, mass, radiusKm, distanceAu, angleDeg, color);
        if (body == null) return false;

        registerBody(body, radiusKm, distanceAu, angleDeg, normalizedType, parentName, color);
        return true;
    }

    public boolean updateBody(String originalName, String newName, String type, String parentName,
                              double mass, double radiusKm, double distanceAu,
                              double angleDeg, Color color) {
        Body existing = map.get(originalName);
        if (existing == null || newName == null || newName.isBlank()) {
            return false;
        }

        String originalType = bodyTypes.get(originalName);
        String originalParent = orbitParents.get(originalName);
        Double originalRadius = logicalRadiiKm.get(originalName);
        Double originalDistance = logicalDistancesAu.get(originalName);
        Double originalAngle = orbitalAnglesDeg.get(originalName);
        Color originalColor = bodyColors.getOrDefault(originalName, extractColor(existing));

        String normalizedType = normalizeType(type);

        if (!originalName.equals(newName) && map.containsKey(newName)) {
            return false;
        }

        List<BodyState> children = getChildStates(originalName);
        if (!children.isEmpty() && !TYPE_PLANET.equals(normalizedType)) {
            return false;
        }

        if (TYPE_MOON.equals(normalizedType)) {
            if (parentName == null || parentName.isBlank()) return false;
            if (originalName.equals(parentName)) return false;
        } else {
            parentName = null;
        }

        Body updated = createBodyByType(newName, normalizedType, parentName, mass, radiusKm, distanceAu, angleDeg, color);
        if (updated == null) {
            return false;
        }

        removeBodyInternal(originalName);
        registerBody(updated, radiusKm, distanceAu, angleDeg, normalizedType, parentName, color);

        for (BodyState child : children) {
            removeBodyInternal(child.name);
            Body rebuiltChild = createBodyByType(
                    child.name,
                    child.type,
                    newName,
                    child.mass,
                    child.radiusKm,
                    child.distanceAu,
                    child.angleDeg,
                    child.color
            );
            if (rebuiltChild != null) {
                registerBody(rebuiltChild, child.radiusKm, child.distanceAu, child.angleDeg, child.type, newName, child.color);
            }
        }

        return true;
    }

    public boolean removeBody(String name) {
        if (!getChildStates(name).isEmpty()) {
            return false;
        }

        removeBodyInternal(name);
        return true;
    }

    public void addBody(Body body) {
        bodies.add(body);
        map.put(body.getName(), body);
        root.getChildren().add(body.getView());

        double baseSceneRadius = body.getView().getRadius() / bodyScaleMultiplier();
        baseRadii.put(body, baseSceneRadius);
        logicalRadiiKm.put(body.getName(), sceneRadiusToKm(baseSceneRadius));

        double distanceAu = Math.sqrt(
                body.getX() * body.getX() +
                        body.getY() * body.getY() +
                        body.getZ() * body.getZ()
        ) / AU_IN_METERS;

        logicalDistancesAu.put(body.getName(), distanceAu);
        orbitalAnglesDeg.put(body.getName(), normalizeAngle(Math.toDegrees(Math.atan2(body.getZ(), body.getX()))));
        bodyTypes.put(body.getName(), TYPE_PLANET);
        orbitParents.put(body.getName(), null);
        bodyColors.put(body.getName(), extractColor(body));

        applyScaleToBody(body);
    }

    public Body getBody(String name) {
        return map.get(name);
    }

    public Set<String> getBodyNames() {
        return new TreeSet<>(map.keySet());
    }

    public Set<String> getPlanetNames() {
        TreeSet<String> names = new TreeSet<>();
        for (String name : map.keySet()) {
            if (TYPE_PLANET.equals(bodyTypes.get(name))) {
                names.add(name);
            }
        }
        return names;
    }

    public double getBodyRadiusKm(String name) {
        return logicalRadiiKm.getOrDefault(name, 0.0);
    }

    public double getBodyDistanceAu(String name) {
        return logicalDistancesAu.getOrDefault(name, 0.0);
    }

    public double getBodyAngleDeg(String name) {
        return orbitalAnglesDeg.getOrDefault(name, 0.0);
    }

    public String getBodyType(String name) {
        return bodyTypes.getOrDefault(name, TYPE_PLANET);
    }

    public String getOrbitParent(String name) {
        return orbitParents.get(name);
    }

    public Color getBodyColor(String name) {
        return bodyColors.getOrDefault(name, Color.WHITE);
    }

    public void updateBodyRadius(String name, double radiusKm) {
        Body body = map.get(name);
        if (body == null) return;

        double sceneRadius = toSceneRadiusFromKm(name, radiusKm);
        baseRadii.put(body, sceneRadius);
        logicalRadiiKm.put(name, radiusKm);
        applyScaleToBody(body);
    }

    public void setViewScale(double slider) {
        currentViewScale = Math.max(0.0, Math.min(1.0, slider));
        for (Body body : bodies) {
            applyScaleToBody(body);
        }
    }

    public void updatePhysics(double dt) {
        for (Body body : bodies) {
            body.resetAcceleration();
        }

        for (int i = 0; i < bodies.size(); i++) {
            for (int j = i + 1; j < bodies.size(); j++) {
                applyGravity(bodies.get(i), bodies.get(j));
            }
        }

        double[] axOld = new double[bodies.size()];
        double[] ayOld = new double[bodies.size()];
        double[] azOld = new double[bodies.size()];

        for (int i = 0; i < bodies.size(); i++) {
            Body body = bodies.get(i);
            axOld[i] = body.getAx();
            ayOld[i] = body.getAy();
            azOld[i] = body.getAz();
        }

        for (Body body : bodies) {
            body.updatePosition(dt);
        }

        for (Body body : bodies) {
            body.resetAcceleration();
        }

        for (int i = 0; i < bodies.size(); i++) {
            for (int j = i + 1; j < bodies.size(); j++) {
                applyGravity(bodies.get(i), bodies.get(j));
            }
        }

        for (int i = 0; i < bodies.size(); i++) {
            bodies.get(i).updateVelocity(dt, axOld[i], ayOld[i], azOld[i]);
        }

        for (Body body : bodies) {
            body.getView().setTranslateX(Conversions.metersToScene(body.getX()));
            body.getView().setTranslateY(Conversions.metersToScene(body.getY()));
            body.getView().setTranslateZ(Conversions.metersToScene(body.getZ()));
        }
    }

    private void applyGravity(Body a, Body b) {
        double dx = b.getX() - a.getX();
        double dy = b.getY() - a.getY();
        double dz = b.getZ() - a.getZ();

        double distSq = dx * dx + dy * dy + dz * dz;
        double dist = Math.sqrt(distSq);

        if (dist < 1.0) return;

        double accelA = G * b.getMass() / distSq;
        double accelB = G * a.getMass() / distSq;

        double nx = dx / dist;
        double ny = dy / dist;
        double nz = dz / dist;

        a.addAcceleration(accelA * nx, accelA * ny, accelA * nz);
        b.addAcceleration(-accelB * nx, -accelB * ny, -accelB * nz);
    }

    public Group getRoot() {
        return root;
    }
}