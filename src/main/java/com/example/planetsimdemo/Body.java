package com.example.planetsimdemo;

public class Body {
    private final String name;
    private final double mass;

    private double x, y, z;
    private double vx, vy, vz;
    private double ax, ay, az;

   //a Body object has all things needed for physics equations only. It has a name, position, velocity, acceleration and mass.
   //these parameters are used in the physics equations in PhysicsEngine
    public Body(String name, double mass,
                       double x, double y, double z,
                       double vx, double vy, double vz) {
        this.name = name;
        this.mass = mass;
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

    //getters
    public String getName() { return name; }
    public double getMass() { return mass; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public double getVx() { return vx; }
    public double getVy() { return vy; }
    public double getVz() { return vz; }
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
    public void setAcceleration(double ax, double ay, double az) {
        this.ax = ax;
        this.ay = ay;
        this.az = az;
    }

    //part of Verlet integration
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

