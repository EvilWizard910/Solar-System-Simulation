package com.example.planetsimdemo;

public final class PhysicsEngine {
    public void update(SolarSystemState state, double dt){
        var bodies = state.getBodies();
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
}
