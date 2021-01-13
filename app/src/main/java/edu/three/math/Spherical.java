package edu.three.math;

public class Spherical {
    public float radius, phi, theta;
    float PI = (float) Math.PI;

    float clamp(float value, float min, float max) {
        return Math.max( min, Math.min( max, value ) );
    }
    public Spherical() {

    }
    public Spherical(float radius, float phi, float theta) {
        set(radius, phi, theta);
    }

    public Spherical set(float radius, float phi, float theta) {
        this.radius = radius;
        this.phi = phi;
        this.theta = theta;
        return this;
    }

    public Spherical copy(Spherical other) {
        this.radius = other.radius;
        this.phi = other.phi;
        this.theta = other.theta;
        return this;
    }

    public Spherical clone() {
        return new Spherical().copy(this);
    }

    // restrict phi to be betwee EPS and PI-EPS
    public Spherical makeSafe() {
        float EPS = 0.000001f;
        this.phi = Math.max(EPS, Math.min(PI - EPS, this.phi) );
        return  this;
    }

    public Spherical setFromCartesianCoords(float x, float y, float z) {
        this.radius = (float) Math.sqrt(x*x +y*y +z*z);
        if (this.radius == 0) {
            this.theta = 0;
            this.phi = 0;
        } else {
            this.theta = (float)Math.atan2(x, z);
            this.phi = (float) Math.acos(clamp(y/this.radius, -1f, 1f));
        }
        return this;
    }

    public Spherical setFromVector3(Vector3 v) {
        return this.setFromCartesianCoords(v.x, v.y, v.z);
    }
}
