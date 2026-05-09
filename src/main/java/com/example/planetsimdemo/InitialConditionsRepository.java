package com.example.planetsimdemo;


import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.SetOptions;
import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InitialConditionsRepository {
    private final Firestore firestore;

    public InitialConditionsRepository(Firestore firestore){
        this.firestore = firestore;
    }

    public List<SolarSystemState.InitialCondition> loadDefaultSystem() throws Exception {
        QuerySnapshot snapshot = firestore.collection("solarSystem")
                .document("defaultSystem")
                .collection("bodies")
                .get()
                .get();

        List<SolarSystemState.InitialCondition> conditions = new ArrayList<>();
        for (QueryDocumentSnapshot doc : snapshot.getDocuments()) {
            conditions.add(fromInitialConditionMap(doc.getData(), doc.getId()));
        }

        if (!conditions.isEmpty()) {
            return conditions;
        }

        snapshot = firestore.collection("initialConditions").get().get();
        for (QueryDocumentSnapshot doc : snapshot.getDocuments()) {
            conditions.add(fromInitialConditionMap(doc.getData(), doc.getId()));
        }
        if (conditions.isEmpty()) {
            return SolarSystemState.defaultInitialConditions();
        }
        return conditions;
    }

    public void saveDefaultSystemTemplate(Collection<SolarSystemState.InitialCondition> conditions) throws Exception {
        Map<String,Object> systemPayload = new HashMap<>();
        systemPayload.put("name", "defaultSystem");
        systemPayload.put("updatedAt", Timestamp.now());
        systemPayload.put("createdBy", "seed-script");
        systemPayload.put("schemaVersion", "realtime-physics-v2");

        firestore.collection("solarSystem").document("defaultSystem").set(systemPayload, SetOptions.merge()).get();
        deleteCollection(firestore.collection("solarSystem").document("defaultSystem").collection("bodies"));

        for (SolarSystemState.InitialCondition condition : conditions) {
            firestore.collection("solarSystem")
                    .document("defaultSystem")
                    .collection("bodies")
                    .document(condition.name())
                    .set(toInitialConditionMap(condition))
                    .get();
        }
    }

    public void deletePlaceholderUserDocument() throws Exception {
        DocumentReference placeholder = firestore.collection("users").document("{uid}");
        deleteCollection(placeholder.collection("systems"));
        placeholder.delete().get();
    }

    public List<String> listSystems(String uid) throws Exception{
        ApiFuture<QuerySnapshot> future = firestore.collection("users").document(uid).collection("systems").get();

        List<String> names = new ArrayList<>();
        for(QueryDocumentSnapshot doc : future.get().getDocuments()){
            names.add(doc.getId());
        }
        names.sort(String.CASE_INSENSITIVE_ORDER);
        return names;
    }

    public void ensureUser(String uid, String email) throws Exception {
        Map<String,Object> userPayload = new HashMap<>();
        userPayload.put("uid", uid);
        userPayload.put("email", email);
        userPayload.put("lastSignedInAt", Timestamp.now());

        firestore.collection("users").document(uid).set(userPayload, SetOptions.merge()).get();
    }

    public void saveSystem(String uid, String systemName, SolarSystemState state) throws Exception{
        Map<String,Object> userPayload = new HashMap<>();
        userPayload.put("uid", uid);
        userPayload.put("lastSavedAt", Timestamp.now());

        DocumentReference userDoc = firestore.collection("users").document(uid);
        userDoc.set(userPayload, SetOptions.merge()).get();

        Map<String,Object> payload = new HashMap<>();
        payload.put("name",systemName);
        payload.put("savedAt",Timestamp.now());
        payload.put("schemaVersion", "user-system-v1");
        payload.put("bodyCount", state.toSnapshots().size());

        DocumentReference systemDoc = userDoc.collection("systems").document(systemName);
        systemDoc.set(payload).get();

        QuerySnapshot existingBodies = systemDoc.collection("bodies").get().get();
        for (QueryDocumentSnapshot doc : existingBodies.getDocuments()) {
            doc.getReference().delete().get();
        }

        for(SolarSystemState.BodySnapshot snapshot : state.toSnapshots()){
            systemDoc.collection("bodies").document(snapshot.name()).set(toMap(snapshot)).get();
        }
    }


    public SolarSystemState loadSystem (String uid, String systemName) throws Exception {
        DocumentReference systemDoc = firestore.collection("users").document(uid).collection("systems").document(systemName);
        DocumentSnapshot snapshot = systemDoc.get().get();
        if (!snapshot.exists()) {
            return new SolarSystemState();
        }

        List<SolarSystemState.BodySnapshot> snapshots = new ArrayList<>();
        QuerySnapshot bodySnapshot = systemDoc.collection("bodies").get().get();
        for (QueryDocumentSnapshot doc : bodySnapshot.getDocuments()) {
            snapshots.add(fromMap(doc.getData(), doc.getId()));
        }
        if (!snapshots.isEmpty()) {
            return SolarSystemState.fromSnapshots(snapshots);
        }

        Object rawBodies = snapshot.get("bodies");
        if (!(rawBodies instanceof List<?> bodyList)) {
            return new SolarSystemState();
        }
        for (Object raw : bodyList) {
            if (raw instanceof Map<?, ?> rawMap) {
                Map<String, Object> map = new HashMap<>();
                for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
                    if (entry.getKey() instanceof String key) {
                        map.put(key, entry.getValue());
                    }
                }
                snapshots.add(fromMap(map, null));
            }
        }
        return SolarSystemState.fromSnapshots(snapshots);
    }

    public void deleteSystem(String uid,String systemName)throws Exception {
        DocumentReference systemDoc = firestore.collection("users").document(uid).collection("systems").document(systemName);
        QuerySnapshot bodySnapshot = systemDoc.collection("bodies").get().get();
        for (QueryDocumentSnapshot doc : bodySnapshot.getDocuments()) {
            doc.getReference().delete().get();
        }
        systemDoc.delete().get();
    }

    private void deleteCollection(CollectionReference collection) throws Exception {
        QuerySnapshot snapshot = collection.get().get();
        for (QueryDocumentSnapshot doc : snapshot.getDocuments()) {
            for (CollectionReference childCollection : doc.getReference().listCollections()) {
                deleteCollection(childCollection);
            }
            doc.getReference().delete().get();
        }
    }

    private Map<String,Object>toMap(SolarSystemState.BodySnapshot snapshot){
        Map<String,Object> map = new HashMap<>();
        map.put("name",snapshot.name());
        map.put("type",snapshot.type());
        map.put("parent",snapshot.parent());
        map.put("mass",snapshot.mass());
        map.put("radiusKm",snapshot.radiusKm());
        map.put("color",toColorString(snapshot.color()));

        map.put("x",snapshot.x());
        map.put("y",snapshot.y());
        map.put("z",snapshot.z());
        map.put("vx",snapshot.vx());
        map.put("vy",snapshot.vy());
        map.put("vz",snapshot.vz());
        map.put("ax",snapshot.ax());
        map.put("ay",snapshot.ay());
        map.put("az",snapshot.az());


        if (snapshot.orbit() != null) {
            Map<String, Object> orbit = new HashMap<>();
            orbit.put("semiMajorAxisAu", snapshot.orbit().semiMajorAxisAu());
            orbit.put("eccentricity", snapshot.orbit().eccentricity());
            orbit.put("inclinationDeg", snapshot.orbit().inclinationDeg());
            orbit.put("ascendingNodeDeg", snapshot.orbit().ascendingNodeDeg());
            orbit.put("argumentOfPeriapsisDeg", snapshot.orbit().argumentOfPeriapsisDeg());
            orbit.put("trueAnomalyDeg", snapshot.orbit().trueAnomalyDeg());
            map.put("orbit", orbit);
        }else{map.put("orbit",null);}
        return map;
    }

    private Map<String,Object> toInitialConditionMap(SolarSystemState.InitialCondition condition) {
        Map<String,Object> map = new HashMap<>();
        map.put("name", condition.name());
        map.put("type", condition.type());
        map.put("parent", condition.parent());
        map.put("mass", condition.mass());
        map.put("radiusKm", condition.radiusKm());
        map.put("color", toColorString(condition.color()));

        if (condition.orbit() != null) {
            Map<String,Object> orbit = new HashMap<>();
            orbit.put("semiMajorAxisAu", condition.orbit().semiMajorAxisAu());
            orbit.put("eccentricity", condition.orbit().eccentricity());
            orbit.put("inclinationDeg", condition.orbit().inclinationDeg());
            orbit.put("ascendingNodeDeg", condition.orbit().ascendingNodeDeg());
            orbit.put("argumentOfPeriapsisDeg", condition.orbit().argumentOfPeriapsisDeg());
            orbit.put("trueAnomalyDeg", condition.orbit().trueAnomalyDeg());
            map.put("orbit", orbit);
        } else {
            map.put("orbit", null);
        }

        return map;
    }

    private SolarSystemState.InitialCondition fromInitialConditionMap(Map<String,Object> map, String fallbackName) {
        SolarSystemState.OrbitElements orbit = null;
        Object orbitRaw = map.get("orbit");

        if(orbitRaw instanceof Map<?,?> orbitMap){
            orbit = new SolarSystemState.OrbitElements(
                    getDouble(orbitMap.get("semiMajorAxisAu")),
                    getDouble(orbitMap.get("eccentricity")),
                    getDouble(orbitMap.get("inclinationDeg")),
                    getDouble(orbitMap.get("ascendingNodeDeg")),
                    getDouble(orbitMap.get("argumentOfPeriapsisDeg")),
                    getDouble(firstNonNull(orbitMap.get("trueAnomalyDeg"), orbitMap.get("trueAnomolyDeg"))));
        } else if (hasOrbitFields(map)) {
            orbit = new SolarSystemState.OrbitElements(
                    getDouble(map.get("semiMajorAxisAu")),
                    getDouble(map.get("eccentricity")),
                    getDouble(map.get("inclinationDeg")),
                    getDouble(map.get("ascendingNodeDeg")),
                    getDouble(map.get("argumentOfPeriapsisDeg")),
                    getDouble(firstNonNull(map.get("trueAnomalyDeg"), map.get("trueAnomolyDeg"))));
        }

        return new SolarSystemState.InitialCondition(
                getString(map.get("name"), fallbackName),
                getString(map.get("type"), "Planet"),
                normalizeParent(getString(map.get("parent"), null)),
                getDouble(map.get("mass")),
                getDouble(map.get("radiusKm")),
                parseColor(getString(map.get("color"), null)),
                orbit);
    }

   private SolarSystemState.BodySnapshot fromMap(Map<String,Object> map, String fallbackName){
       SolarSystemState.OrbitElements orbit =null;
       Object orbitRaw = map.get("orbit");

       if(orbitRaw instanceof Map<?,?> orbitMap){
           orbit=new SolarSystemState.OrbitElements(
                   getDouble(orbitMap.get("semiMajorAxisAu")),
                   getDouble(orbitMap.get("eccentricity")),
                   getDouble(orbitMap.get("inclinationDeg")),
                   getDouble(orbitMap.get("ascendingNodeDeg")),
                   getDouble(orbitMap.get("argumentOfPeriapsisDeg")),
                   getDouble(firstNonNull(orbitMap.get("trueAnomalyDeg"), orbitMap.get("trueAnomolyDeg"))));
       }
       return new SolarSystemState.BodySnapshot(
               getString(map.get("name"), fallbackName),
               getString(map.get("type"), "Planet"),
               normalizeParent(getString(map.get("parent"), null)),
               getDouble(map.get("mass")),
               getDouble(map.get("radiusKm")),
               parseColor(getString(map.get("color"), null)),
               getDouble(map.get("x")),
               getDouble(map.get("y")),
               getDouble(map.get("z")),
               getDouble(map.get("vx")),
               getDouble(map.get("vy")),
               getDouble(map.get("vz")),
               getDouble(map.get("ax")),
               getDouble(map.get("ay")),
               getDouble(map.get("az")),
               orbit);
   }

    private static Object firstNonNull(Object first, Object second) {
        return first != null ? first : second;
    }

    private static boolean hasOrbitFields(Map<String,Object> map) {
        return map.containsKey("semiMajorAxisAu")
                || map.containsKey("eccentricity")
                || map.containsKey("inclinationDeg")
                || map.containsKey("ascendingNodeDeg")
                || map.containsKey("argumentOfPeriapsisDeg")
                || map.containsKey("trueAnomalyDeg")
                || map.containsKey("trueAnomolyDeg");
    }

    private static double getDouble(Object value) {
        return value instanceof Number number ? number.doubleValue() : 0.0;
    }

    private static String getString(Object value, String fallback) {
        return value instanceof String text ? text : fallback;
    }

    private static String normalizeParent(String parent) {
        if (parent == null || parent.isBlank() || "null".equalsIgnoreCase(parent)) {
            return null;
        }
        return parent;
    }

    private static Color parseColor(String color) {
        if (color == null || color.isBlank()) {
            return Color.WHITE;
        }
        return Color.web(color);
    }
    private static String toColorString(Color color) {
        if (color == null) {return "#ffffff";}
        int r = (int) Math.round(color.getRed() * 255);
        int g = (int) Math.round(color.getGreen() * 255);
        int b = (int) Math.round(color.getBlue() * 255);

        return String.format("#%02x%02x%02x", r, g, b);
    }
}
