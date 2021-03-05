package edu.three.math;

public class Spherical2 {
    public double radius = 1,
        phi,    // polar angle
        theta;  // azimuthal angle
    double PI = Math.PI;

    double clamp(double value, double min, double max) {
        return Math.max( min, Math.min( max, value ) );
    }
    public Spherical2() {

    }
    public Spherical2(double radius, double phi, double theta) {
        set(radius, phi, theta);
    }

    public Spherical2 set(double radius, double phi, double theta) {
        this.radius = radius;
        this.phi = phi;
        this.theta = theta;
        return this;
    }

    public Spherical2 copy(Spherical2 other) {
        this.radius = other.radius;
        this.phi = other.phi;
        this.theta = other.theta;
        return this;
    }

    public Spherical2 clone() {
        return new Spherical2().copy(this);
    }

    // restrict phi to be betwee EPS and PI-EPS
    public Spherical2 makeSafe() {
        double EPS = 0.000001f;
        this.phi = Math.max(EPS, Math.min(PI - EPS, this.phi) );
        return  this;
    }

    public Spherical2 setFromCartesianCoords(double x, double y, double z) {
        this.radius = Math.sqrt(x*x +y*y +z*z);
        if (this.radius == 0) {
            this.theta = 0;
            this.phi = 0;
        } else {
            this.theta = Math.atan2(x, y);
            this.phi = Math.acos(clamp(z/this.radius, -1f, 1f));
        }
        return this;
    }

    public Spherical2 setFromVector3(Vector3 v) {
        return this.setFromCartesianCoords(v.x, v.y, v.z);
    }
}
