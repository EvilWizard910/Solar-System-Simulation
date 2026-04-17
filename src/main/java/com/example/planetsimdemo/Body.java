package com.example.planetsimdemo;
import javafx.scene.shape.Sphere;

public class Body {
    private final String name;
    private final double mass;
    private final Sphere view;

    private double x;
    private double y;
    private double z;

    private double vx;
    private double vy;
    private double vz;

    private double ax;
    private double ay;
    private double az;

   //Creates a body with mass, position x,y,z with velocity and acceleration on all 3 dimensions
    public Body(String name, double mass, Sphere view,
                       double x, double y, double z,
                       double vx, double vy, double vz) {
        this.name = name;
        this.mass = mass;
        this.view = view;
        this.x = x;
        this.y = y;
        this.z = z;
        this.vx = vx;
        this.vy = vy;
        this.vz = vz;
    }


    //set acceleration to 0 in all directions
    public void resetAcceleration() {
        ax = 0;
        ay = 0;
        az = 0;
    }

   //change acceleration
    public void addAcceleration(double dax, double day, double daz) {
        ax += dax;
        ay += day;
        az += daz;
    }

  //calculate instantaneous velocity and position


    //getters
    public String getName() { return name; }
    public double getMass() { return mass; }
    public Sphere getView() { return view; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public double getAx() { return ax; }
    public double getAy() { return ay; }
    public double getAz() { return az; }

    //more setters
    public void setPosition(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void setVelocity(double vx, double vy, double vz) {
        this.vx = vx;
        this.vy = vy;
        this.vz = vz;
    }

    public void updatePosition(double dt) {
       x += vx*dt+.5*ax*dt*dt;
       y += vy*dt+.5*ay*dt*dt;
       z += vz*dt+.5*az*dt*dt;
    }
    public void updateVelocity(double dt, double axOld, double ayOld, double azOld) {
        vx += 0.5 * (axOld + ax) * dt;
        vy += 0.5 * (ayOld + ay) * dt;
        vz += 0.5 * (azOld + az) * dt;
    }
}

