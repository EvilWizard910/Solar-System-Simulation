 package com.example.planetsimdemo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/*This class pulls all variables in the SolarSystemState and maps it into a string for a JSON file for firebase. That is all bodies, there positon
 velocities, accelerations(all in x,y and z), orbital elements, radius, mass, color, typem parent and orbital speed. It does save file paths for
 uploaded textures, but it does not save the jpeg/jpg/png that is used as the texture. It creates a snapshot of the SolarSystemState. This class
 can also fetch the JSON file and read it, turning the string in the json file back into a SolarSystemState*/
public final class FirestoreJsonMapper {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private FirestoreJsonMapper() {}

    public static String toFirestoreDocument(String systemName, SolarSystemState state) {
        ObjectNode root = MAPPER.createObjectNode();
        ObjectNode fields = root.putObject("fields");

        fields.set("name", stringField(systemName));
        fields.set("savedAt", timestampNowField());

        ArrayNode bodyValues = MAPPER.createArrayNode();
        for (SolarSystemState.BodySnapshot snapshot : state.toSnapshots()) {
            bodyValues.add(bodySnapshotValue(snapshot));
        }

        ObjectNode bodiesField = MAPPER.createObjectNode();
        bodiesField.set("arrayValue", MAPPER.createObjectNode().set("values", bodyValues));
        fields.set("bodies", bodiesField);

        try {
            return MAPPER.writeValueAsString(root);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to serialize Firestore document", e);
        }
    }

    public static SolarSystemState fromFirestoreDocument(String json) {
        try {
            JsonNode root = MAPPER.readTree(json);
            JsonNode fields = root.path("fields");
            JsonNode values = fields.path("bodies").path("arrayValue").path("values");

            List<SolarSystemState.BodySnapshot> snapshots = new ArrayList<>();
            if (values.isArray()) {
                for (JsonNode valueNode : values) {
                    snapshots.add(parseBodySnapshot(valueNode));
                }
            }

            return SolarSystemState.fromSnapshots(snapshots);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to parse Firestore document", e);
        }
    }

    public static List<String> extractDocumentIds(String json) {
        try {
            JsonNode root = MAPPER.readTree(json);
            JsonNode documents = root.path("documents");

            List<String> ids = new ArrayList<>();
            if (documents.isArray()) {
                for (JsonNode doc : documents) {
                    String fullName = doc.path("name").asText("");
                    if (!fullName.isBlank()) {
                        int slash = fullName.lastIndexOf('/');
                        ids.add(slash >= 0 ? fullName.substring(slash + 1) : fullName);
                    }
                }
            }
            return ids;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to parse Firestore document list", e);
        }
    }

    private static JsonNode bodySnapshotValue(SolarSystemState.BodySnapshot snapshot) {
        ObjectNode fields = MAPPER.createObjectNode();

        fields.set("name", stringField(snapshot.name()));
        fields.set("type", stringField(snapshot.type()));
        fields.set("parent", nullableStringField(snapshot.parent()));
        fields.set("mass", doubleField(snapshot.mass()));
        fields.set("radiusKm", doubleField(snapshot.radiusKm()));
        fields.set("color", stringField(toColorString(snapshot.color())));
        fields.set("rotationSpeedDegPerSecond", doubleField(snapshot.rotationSpeedDegPerSecond()));
        fields.set("texturePath", nullableStringField(snapshot.texturePath()));

        fields.set("x", doubleField(snapshot.x()));
        fields.set("y", doubleField(snapshot.y()));
        fields.set("z", doubleField(snapshot.z()));
        fields.set("vx", doubleField(snapshot.vx()));
        fields.set("vy", doubleField(snapshot.vy()));
        fields.set("vz", doubleField(snapshot.vz()));
        fields.set("ax", doubleField(snapshot.ax()));
        fields.set("ay", doubleField(snapshot.ay()));
        fields.set("az", doubleField(snapshot.az()));


        fields.set("orbit", orbitValue(snapshot.orbit()));

        ObjectNode mapValue = MAPPER.createObjectNode();
        mapValue.set("fields", fields);

        ObjectNode wrapper = MAPPER.createObjectNode();
        wrapper.set("mapValue", mapValue);
        return wrapper;
    }

    private static JsonNode orbitValue(SolarSystemState.OrbitElements orbit) {
        if (orbit == null) {
            return nullField();
        }

        ObjectNode fields = MAPPER.createObjectNode();
        fields.set("semiMajorAxisAu", doubleField(orbit.semiMajorAxisAu()));
        fields.set("eccentricity", doubleField(orbit.eccentricity()));
        fields.set("inclinationDeg", doubleField(orbit.inclinationDeg()));
        fields.set("ascendingNodeDeg", doubleField(orbit.ascendingNodeDeg()));
        fields.set("argumentOfPeriapsisDeg", doubleField(orbit.argumentOfPeriapsisDeg()));
        fields.set("trueAnomalyDeg", doubleField(orbit.trueAnomalyDeg()));

        ObjectNode mapValue = MAPPER.createObjectNode();
        mapValue.set("fields", fields);

        ObjectNode wrapper = MAPPER.createObjectNode();
        wrapper.set("mapValue", mapValue);
        return wrapper;
    }

    private static SolarSystemState.BodySnapshot parseBodySnapshot(JsonNode valueNode) {
        JsonNode fields = valueNode.path("mapValue").path("fields");

        return new SolarSystemState.BodySnapshot(
                getString(fields, "name"),
                getString(fields, "type"),
                getNullableString(fields, "parent"),
                getDouble(fields, "mass"),
                getDouble(fields, "radiusKm"),
                parseColor(getString(fields, "color")),
                getDouble(fields, "rotationSpeedDegPerSecond"),
                getNullableString(fields, "texturePath"),
                getDouble(fields, "x"),
                getDouble(fields, "y"),
                getDouble(fields, "z"),
                getDouble(fields, "vx"),
                getDouble(fields, "vy"),
                getDouble(fields, "vz"),
                getDouble(fields, "ax"),
                getDouble(fields, "ay"),
                getDouble(fields, "az"),
                parseOrbit(fields.path("orbit"))
        );
    }

    private static SolarSystemState.OrbitElements parseOrbit(JsonNode orbitNode) {
        if (orbitNode.isMissingNode() || orbitNode.has("nullValue")) {
            return null;
        }

        JsonNode fields = orbitNode.path("mapValue").path("fields");
        if (fields.isMissingNode()) {
            return null;
        }

        return new SolarSystemState.OrbitElements(
                getDouble(fields, "semiMajorAxisAu"),
                getDouble(fields, "eccentricity"),
                getDouble(fields, "inclinationDeg"),
                getDouble(fields, "ascendingNodeDeg"),
                getDouble(fields, "argumentOfPeriapsisDeg"),
                getDouble(fields, "trueAnomalyDeg")
        );
    }

    private static ObjectNode stringField(String value) {
        ObjectNode node = MAPPER.createObjectNode();
        node.put("stringValue", value);
        return node;
    }

    private static ObjectNode nullableStringField(String value) {
        return value == null ? nullField() : stringField(value);
    }

    private static ObjectNode doubleField(double value) {
        ObjectNode node = MAPPER.createObjectNode();
        node.put("doubleValue", value);
        return node;
    }

    private static ObjectNode nullField() {
        ObjectNode node = MAPPER.createObjectNode();
        node.putNull("nullValue");
        return node;
    }

    private static ObjectNode timestampNowField() {
        ObjectNode node = MAPPER.createObjectNode();
        node.put("timestampValue", java.time.Instant.now().toString());
        return node;
    }

    private static String getString(JsonNode fields, String key) {
        JsonNode node = fields.path(key);
        if (node.has("stringValue")) {
            return node.path("stringValue").asText();
        }
        return "";
    }

    private static String getNullableString(JsonNode fields, String key) {
        JsonNode node = fields.path(key);
        if (node.has("nullValue")) {
            return null;
        }
        if (node.has("stringValue")) {
            String value = node.path("stringValue").asText();
            return value.isBlank() ? null : value;
        }
        return null;
    }

    private static double getDouble(JsonNode fields, String key) {
        JsonNode node = fields.path(key);
        if (node.has("doubleValue")) {
            return node.path("doubleValue").asDouble();
        }
        if (node.has("integerValue")) {
            return node.path("integerValue").asDouble();
        }
        return 0.0;
    }

    private static Color parseColor(String color) {
        if (color == null || color.isBlank()) {
            return Color.WHITE;
        }
        return Color.web(color);
    }

    private static String toColorString(Color color) {
        if (color == null) {
            return "#ffffff";
        }
        int r = (int) Math.round(color.getRed() * 255);
        int g = (int) Math.round(color.getGreen() * 255);
        int b = (int) Math.round(color.getBlue() * 255);
        return String.format("#%02x%02x%02x", r, g, b);
    }
}