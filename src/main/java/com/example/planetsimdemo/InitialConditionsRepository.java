package com.example.planetsimdemo;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InitialConditionsRepository {
    private final Firestore firestore;

    public InitialConditionsRepository(Firestore firestore){
        this.firestore = firestore;
    }

    public List<SolarSystem.InitialCondition> loadInitialConditions()throws Exception{
        ApiFuture<QuerySnapshot> future = firestore.collection("initialConditions").get();
        List<QueryDocumentSnapshot> docs = future.get().getDocuments();

        List<SolarSystem.InitialCondition> result = new ArrayList<>();

        for (QueryDocumentSnapshot doc : docs){
            String name = doc.getString("name");
            String type = doc.getString("type");
            String parent = doc.getString("parent");
            double mass = getDouble(doc.get("mass"));
            double radiusKm = getDouble(doc.get("radiusKm"));
            Color color = parseColor(doc.getString("color"));

            SolarSystem.OrbitElements orbit=null;
            Object orbitRaw = doc.get("orbit");
            if(orbitRaw instanceof Map<?, ?> orbitMap){
                orbit = new SolarSystem.OrbitElements(
                        getDouble(orbitMap.get("semiMajorAxisAu")),
                        getDouble(orbitMap.get("eccentricity")),
                        getDouble(orbitMap.get("inclinationDeg")),
                        getDouble(orbitMap.get("ascendingNodeDeg")),
                        getDouble(orbitMap.get("argumentOfPeriapsisDeg")),
                        getDouble(orbitMap.get("trueAnomalyDeg"))
                );
            }

            result.add(new SolarSystem.InitialCondition(name,type,parent,mass,radiusKm,color,orbit));
        }
        return result;
    }
    private static double getDouble(Object value){
        return value instanceof Number number ? number.doubleValue() : 0.0;
    }
    private static Color parseColor(String value){
        if(value==null || value.isBlank()){
            return Color.WHITE;
        }
        return Color.web(value);
    }
}
