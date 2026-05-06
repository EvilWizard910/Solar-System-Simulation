package com.example.planetsimdemo;


import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InitialConditionsRepository {
    private final Firestore firestore;

    public InitialConditionsRepository(Firestore firestore){
        this.firestore = firestore;
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

    public void saveSystem(String uid, String systemName, SolarSystemState state) throws Exception{
        List<Map<String,Object>> bodyMaps = new ArrayList<>();
        for(SolarSystemState.BodySnapshot snapshot : state.toSnapshots()){
            bodyMaps.add(toMap(snapshot));
        }
        Map<String,Object> payload = new HashMap<>();
        payload.put("name",systemName);
        payload.put("savedAt",Timestamp.now());
        payload.put("bodies",bodyMaps);

        firestore.collection("users").document(uid).collection("systems").document(systemName).set(payload).get();
    }


    public SolarSystemState loadSystem (String uid, String systemName) throws Exception {
        DocumentSnapshot snapshot = firestore.collection("users").document(uid).collection("systems").document(systemName).get().get();
        if (!snapshot.exists()) {
            return new SolarSystemState();
        }

        Object rawBodies = snapshot.get("bodies");
        if (!(rawBodies instanceof List<?> bodyList)) {
            return new SolarSystemState();
        }
        List<SolarSystemState.BodySnapshot> snapshots = new ArrayList<>();
        for (Object raw : bodyList) {
            if (raw instanceof Map<?, ?> rawMap) {
                Map<String, Object> map = new HashMap<>();
                for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
                    if (entry.getKey() instanceof String key) {
                        map.put(key, entry.getValue());
                    }
                }
                snapshots.add(fromMap(map));
            }
        }
        return SolarSystemState.fromSnapshots(snapshots);
    }

    public void deleteSystem(String uid,String systemName)throws Exception {
        firestore.collection("users").document(uid).collection("systems").document(systemName).delete().get();
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

   private SolarSystemState.BodySnapshot fromMap(Map<String,Object> map){
       SolarSystemState.OrbitElements orbit =null;
       Object orbitRaw = map.get("orbit");

       if(orbitRaw instanceof Map<?,?> orbitMap){
           orbit=new SolarSystemState.OrbitElements(
                   getDouble(orbitMap.get("semiMajorAxisAu")),
                   getDouble(orbitMap.get("eccentricity")),
                   getDouble(orbitMap.get("inclinationDeg")),
                   getDouble(orbitMap.get("ascendingNodeDeg")),
                   getDouble(orbitMap.get("argumentOfPeriapsisDeg")),
                   getDouble(orbitMap.get("trueAnomalyDeg")));
       }
       return new SolarSystemState.BodySnapshot(
               (String) map.get("name"),
               (String) map.get("type"),
               (String) map.get("parent"),
               getDouble(map.get("mass")),
               getDouble(map.get("radiusKm")),
               parseColor((String)map.get("color")),
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

    private static double getDouble(Object value) {
        return value instanceof Number number ? number.doubleValue() : 0.0;
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