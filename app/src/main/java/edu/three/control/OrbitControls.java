package edu.three.control;

import android.view.MotionEvent;

import edu.three.cameras.Camera;
import edu.three.cameras.PerspectiveCamera;
import edu.three.core.Event;
import edu.three.core.EventDispatcher;
import edu.three.math.Matrix4;
import edu.three.math.Quaternion;
import edu.three.math.Spherical;
import edu.three.math.Vector2;
import edu.three.math.Vector3;

public class OrbitControls extends EventDispatcher implements ITouchable {
    State _state = State.NONE;
    State[] touches = new State[] {State.ROTATE, State.DOLLY_PAN};
    double EPS = 0.000001f;
    public boolean enabled = true;

    // Set to true to enable damping (inertia)
    // If damping is enabled, you must call controls.update() in your animation loop
    boolean enableDamping = false;
    double dampingFactor = 0.05f;

    double minDistance = 0;
    double maxDistance = Float.POSITIVE_INFINITY;
    // How far you can orbit vertically, upper and lower limits.
    // Range is 0 to Math.PI radians.
    double minPolarAngle = 0; //radians
    double maxPolarAngle =  Math.PI; //radians

    // How far you can orbit horizontally, upper and lower limits.
    // If set, must be a sub-interval of the interval [ - Math.PI, Math.PI ].
    double minAzimuthAngle = Float.NEGATIVE_INFINITY;
    double maxAzimuthAngle = Float.POSITIVE_INFINITY;


    // This option actually enables dollying in and out; left as "zoom" for backwards compatibility.
    // Set to false to disable zooming
	boolean enableZoom = true;
    double zoomSpeed = 1.0f;

    // Set to false to disable rotating
    boolean enableRotate = true;
    double rotateSpeed = 1.0f;

    // Set to false to disable panning
	boolean enablePan = true;
	double panSpeed = 1.0f;
    boolean screenSpacePanning = false; // if true, pan in screen-space

    //    public Vector3 position, target, up;
    Vector3 target;
    Vector3 position0, target0, zoom0;
    Spherical spherical = new Spherical();
    Spherical sphericalDelta = new Spherical();
    double scale = 1f;
    Vector3 panOffset = new Vector3();

    Screen screen;
    boolean listeningTouchUp = false;
    Event changeEvent = new Event("change");
    Camera object;

    public OrbitControls(Screen scr, Camera camera) {
        screen = scr;
        object = camera;
        target = new Vector3();
        target0 = target.clone();
        position0 = camera.position.clone();

        quat = new Quaternion().setFromUnitVectors(object.up, new Vector3(0,1,0));
        quatInverse = quat.clone().inverse();
    }

    Vector3 offset = new Vector3();
    Quaternion quat;
    Quaternion quatInverse;
    Vector3 lastPosition = new Vector3();
    Quaternion lastQuaternion = new Quaternion();
    double PI =  Math.PI, twoPI = 2 * PI;

    public boolean update() {
        Vector3 position = object.position;
        offset.copy(position).sub(target);
        // rotate offset to "y-axis-is-up" space
        offset.applyQuaternion(quat);
        // angle from z-axis around y-axis
        spherical.setFromVector3(offset);
        if (enableDamping) {
            spherical.theta += sphericalDelta.theta * dampingFactor;
            spherical.phi += sphericalDelta.phi * dampingFactor;
        } else {
            spherical.theta += sphericalDelta.theta;
            spherical.phi += sphericalDelta.phi;
        }
        // restrict theta to be between desired limits
        double min = minAzimuthAngle, max = maxAzimuthAngle;
        if (min != Float.NEGATIVE_INFINITY && max != Float.POSITIVE_INFINITY &&
                min != Float.POSITIVE_INFINITY && max != Float.NEGATIVE_INFINITY) {
            if ( min < - Math.PI )
                min += twoPI;
            else if ( min > Math.PI )
                min -= twoPI;

            if ( max < - Math.PI )
                max += twoPI;
            else if ( max > Math.PI )
                max -= twoPI;

            if ( min <= max ) {
                spherical.theta = Math.max( min, Math.min( max, spherical.theta ) );
            } else {
                spherical.theta = ( spherical.theta > ( min + max ) / 2 ) ?
                        Math.max( min, spherical.theta ) :
                        Math.min( max, spherical.theta );
            }
        }
        // restrict phi to be between desired limits
        spherical.phi = Math.max( minPolarAngle, Math.min( maxPolarAngle, spherical.phi ) );

        spherical.makeSafe();


        spherical.radius *= scale;

        // restrict radius to be between desired limits
        spherical.radius = Math.max( minDistance, Math.min( maxDistance, spherical.radius ) );

        // move target to panned location
        if (enableDamping) {
            target.addScaledVector(panOffset, dampingFactor);
        } else {
            target.add(panOffset);
        }
        offset.setFromSpherical(spherical);
        // rotate offset back to "camera-up-vector-is-up" space
        offset.applyQuaternion( quatInverse );

        position.copy( target ).add( offset );

        object.lookAt( target );
        if (enableDamping) {
            sphericalDelta.theta *= ( 1 - dampingFactor );
            sphericalDelta.phi *= ( 1 - dampingFactor );

            panOffset.multiplyScalar( 1 - dampingFactor );
        } else {
            sphericalDelta.set( 0, 0, 0 );
            panOffset.set( 0, 0, 0 );
        }
        scale = 1;
        // update condition is:
        // min(camera displacement, camera rotation in radians)^2 > EPS
        // using small-angle approximation cos(x/2) = 1 - x^2 / 8

        if (lastPosition.distanceToSquared( object.position ) > EPS ||
                8 * ( 1 - lastQuaternion.dot( object.quaternion ) ) > EPS ) {
            dispatchEvent( changeEvent );

            lastPosition.copy( object.position );
            lastQuaternion.copy( object.quaternion );
            return true;
        }
        return false;
    }

    Vector2 rotateStart = new Vector2();
    Vector2 rotateEnd = new Vector2();
    Vector2 rotateDelta = new Vector2();

    Vector2 panStart = new Vector2();
    Vector2 panEnd = new Vector2();
    Vector2 panDelta = new Vector2();

    Vector2 dollyStart = new Vector2();
    Vector2 dollyEnd = new Vector2();
    Vector2 dollyDelta = new Vector2();
    double getZoomScale() {
        return  Math.pow(0.95, zoomSpeed);
    }
    void rotateLeft(double angle) {
        sphericalDelta.theta -= angle;
    }
    void rotateUp(double angle) {
        sphericalDelta.phi -= angle;
    }
    Vector3 v = new Vector3();
    void panLeft(double distance, Matrix4 objectMatrix) {
        v.setFromMatrixColumn(objectMatrix, 0);// get X column of objectMatrix
        v.multiplyScalar(- distance);
        panOffset.add(v);
    }
    void panUp(double distance, Matrix4 objectMatrix) {
        if (screenSpacePanning) {
            v.setFromMatrixColumn(objectMatrix, 1);
        } else {
            v.setFromMatrixColumn(objectMatrix, 0);
            v.crossVectors(object.up, v);
        }
        v.multiplyScalar(distance);
        panOffset.add(v);
    }

    // deltaX and deltaY are in pixels; right and down are positive
    // only for perspective camera
    void pan(double deltaX, double deltaY) {
        Vector3 position = object.position;
        offset.copy(position).sub(target);
        double targetDistance = offset.length();
        // half of the fov is center to top of screen
        targetDistance *= Math.tan( ( ((PerspectiveCamera) object).fov / 2 ) * Math.PI / 180.0 );

        // we use only clientHeight here so aspect ratio does not distort speed
        panLeft( 2 * deltaX * targetDistance / screen.height, object.getModelMatrix() );
        panUp( 2 * deltaY * targetDistance / screen.height, object.getModelMatrix() );
    }

    void dollyOut(double dollyScale) {
        scale /= dollyScale;
    }
    void dollyIn(double dollyScale) {
        scale *= dollyScale;
    }

    void handleTouchStartRotate(MotionEvent event) {
        if (event.getPointerCount() == 1) {
            rotateStart.set(event.getX(), event.getY());
        } else {
            double x = 0.5f * ( event.getX(0) + event.getX(1) );
            double y = 0.5f * ( event.getY(0) + event.getY(1) );
            rotateStart.set( x, y );
        }
    }
    void handleTouchStartPan(MotionEvent event) {
        if (event.getPointerCount() == 1) {
            panStart.set(event.getX(), event.getY());
        } else {
            double x = 0.5f * ( event.getX(0) + event.getX(1) );
            double y = 0.5f * ( event.getY(0) + event.getY(1) );
            panStart.set( x, y );
        }
    }
    void handleTouchStartDolly(MotionEvent event) {
        double dx = event.getX(0) - event.getX(1);
        double dy = event.getY(0) - event.getY(1);
        double distance = Math.sqrt(dx*dx + dy*dy);
        dollyStart.set(0, distance);
    }
    void handleTouchStartDollyPan(MotionEvent event) {
        if ( enableZoom ) handleTouchStartDolly( event );

        if ( enablePan ) handleTouchStartPan( event );
    }
    void handleTouchStartDollyRotate(MotionEvent event) {
        if ( enableZoom ) handleTouchStartDolly( event );

        if ( enableRotate ) handleTouchStartRotate( event );
    }
    void handleTouchMoveRotate(MotionEvent event) {
        if (event.getPointerCount() == 1) {
            rotateEnd.set(event.getX(), event.getY());
        } else {
            double x = 0.5f * ( event.getX(0) + event.getX(1) );
            double y = 0.5f * ( event.getY(0) + event.getY(1) );
            rotateEnd.set( x, y );
        }
        rotateDelta.subVectors(rotateEnd, rotateStart).multiplyScalar(rotateSpeed);
        rotateLeft( 2 * PI * rotateDelta.x / screen.height ); // yes, height

        rotateUp( 2 * PI * rotateDelta.y / screen.height );

        rotateStart.copy( rotateEnd );
    }

    void handleTouchMovePan(MotionEvent event) {
        if (event.getPointerCount() == 1) {
            panEnd.set(event.getX(), event.getY());
        } else {
            double x = 0.5f * ( event.getX(0) + event.getX(1) );
            double y = 0.5f * ( event.getY(0) + event.getY(1) );
            panEnd.set( x, y );
        }
        panDelta.subVectors(panEnd, panStart).multiplyScalar(panSpeed);
        pan( panDelta.x, panDelta.y );

        panStart.copy( panEnd );
    }
    void handleTouchMoveDolly(MotionEvent event) {
        if (event.getPointerCount() == 2 && dollyStart.y != 0) {
            double dx = event.getX(0) - event.getX(1);
            double dy = event.getY(0) - event.getY(1);
            double distance = Math.sqrt(dx*dx + dy*dy);
            dollyEnd.set( 0, distance );

            dollyDelta.set( 0, Math.pow( dollyEnd.y / dollyStart.y, zoomSpeed ) );

            dollyOut( dollyDelta.y );

            dollyStart.copy( dollyEnd );
        }
    }
    void handleTouchMoveDollyPan(MotionEvent event) {
        if ( enableZoom ) handleTouchMoveDolly( event );

        if ( enablePan ) handleTouchMovePan( event );
    }
    void handleTouchMoveDollyRotate(MotionEvent event) {
        if ( enableZoom ) handleTouchMoveDolly( event );

        if ( enableRotate ) handleTouchMoveRotate( event );
    }

    public void touchDown(MotionEvent event) {
        if (!enabled) {
            return;
        }
        int pointerCount = event.getPointerCount();
        switch (pointerCount) {
            case 1:
                switch (touches[0]) {
                    case ROTATE:
                        if (!enableRotate)
                            return;
                        handleTouchStartRotate( event );
                        _state = State.ROTATE;
                        break;
                    case PAN:
                        if (!enablePan)
                            return;
                        handleTouchStartPan(event);
                        _state = State.PAN;
                        break;
                    default:
                        _state = State.NONE;
                }
                break;
            case 2:
                switch (touches[1]) {
                    case DOLLY_PAN:
                        if (!enableZoom && !enablePan)
                            return;
                        handleTouchStartDollyPan( event );

                        _state = State.DOLLY_PAN;

                        break;
                    case DOLLY_ROTATE:
                        if (!enableZoom && !enableRotate)
                            return;
                        handleTouchStartDollyRotate( event );
                        _state = State.DOLLY_ROTATE;
                        break;
                    default:
                        _state = State.NONE;
                }
                break;
            default:
                _state = State.NONE;
        }
        listeningTouchUp = true;
    }

    public void touchMove(MotionEvent event) {
        if (!enabled || !listeningTouchUp) {
            return;
        }
        if ( _state == State.ROTATE && enableRotate ) {
            handleTouchMoveRotate( event );
            update();
        } else if ( _state == State.PAN && enablePan ) {
            handleTouchMovePan( event );
            update();
        } else if ( _state == State.DOLLY_PAN ) {
            if (!enableZoom && !enablePan)
                return;
            handleTouchMoveDollyPan( event );
            update();
        } else if (_state == State.DOLLY_ROTATE) {
            if (!enableZoom && !enableRotate) {
                return;
            }
            handleTouchMoveDollyRotate( event );
            update();
        } else {
            _state = State.NONE;
        }
    }

    public void touchUp() {
        if (!enabled || !listeningTouchUp) {
            return;
        }
        _state = State.NONE;
        listeningTouchUp = false;
    }

    public void pointerDown(MotionEvent event) {

    }

    public void reset() {
        target.copy(target0);
        object.position.copy(position0);

        object.lookAt(target);
        object.updateProjectionMatrix();
        dispatchEvent(changeEvent);
        update();
        _state = State.NONE;
    }
}