package edu.three.control;

import android.view.MotionEvent;

import edu.three.cameras.Camera;
import edu.three.core.Event;
import edu.three.core.EventDispatcher;
import edu.three.interfaces.ITouch;
import edu.three.math.Quaternion;
import edu.three.math.Vector2;
import edu.three.math.Vector3;

public class TrackballControls extends EventDispatcher implements ITouch {
    State _state = State.NONE;
    float EPS = 0.000001f;
    public boolean enabled = true;
    public float rotateSpeed = 1f;
    public float zoomSpeed = 1.2f;
    public float panSpeed = 0.3f;
    boolean noRotate = false;
    boolean noZoom = false;
    boolean noPan = false;
    float dynamicDampingFactor = 0.2f;
    float minDistance = 0;
    float maxDistance = Float.POSITIVE_INFINITY;
//    public Vector3 position, target, up;
    Vector3 target;
    Vector3 position0, target0, up0;
    Vector3 lastPosition = new Vector3();
    float _lastAngle = 0f;
    Vector3 _lastAxis = new Vector3();
    Vector3 _eye = new Vector3();
    Vector2 _movePrev = new Vector2();
    Vector2 _moveCurr = new Vector2();
    float _touchZoomDistanceEnd, _touchZoomDistanceStart;
    Vector2 _panStart = new Vector2();
    Vector2 _panEnd = new Vector2();
    Screen screen;
    boolean listeningMoveUp = false;
    Event changeEvent = new Event("change");
    Camera object;

    public TrackballControls(Screen scr, Camera camera) {
        screen = scr;
        object = camera;
        target = new Vector3();
        target0 = target.clone();
        position0 = camera.position.clone();
        up0 = object.up.clone();
        lastPosition.copy(object.position);
    }

    public void handleResize(int left, int top, int width, int height) {
        screen.left = left;
        screen.top = top;
        screen.width = width;
        screen.height = height;
    }

    private Vector2 getTouchOnScreen(float touchX, float touchY) {
        return new Vector2((touchX - screen.left) / screen.width, (touchY - screen.top) / screen.height);
    }
    private Vector2 getTouchOnCircle(float touchX, float touchY) {
        return new Vector2((touchX - screen.width * 0.5f - screen.left) / (screen.width * 0.5f),
                (screen.height + 2 * (screen.top - touchY)) / screen.width);
    }

    public void touchDown(MotionEvent event) {
        if (!enabled) {
            return;
        }
        int pointerCount = event.getPointerCount();
        if (pointerCount == 1) {
            _state = State.ROTATE;
        } else if (pointerCount == 2) {
            _state = State.ZOOM;
        } else if (pointerCount == 3) {
            _state = State.PAN;
        } else if (pointerCount > 3) {
            reset();
            return;
        }
        if (_state == State.ROTATE && !noRotate) {
            _moveCurr.copy(getTouchOnCircle(event.getX(), event.getY()));
            _movePrev.copy(_moveCurr);
        } else if (_state == State.ZOOM && !noZoom) {
            float dx = event.getX(0) - event.getX(1);
            float dy = event.getY(0) - event.getY(1);
            _touchZoomDistanceEnd = _touchZoomDistanceStart = (float) Math.sqrt( dx * dx + dy * dy );
        } else if (_state == State.PAN && !noPan) {
            _panStart.copy(getTouchOnScreen(event.getX(), event.getY()));
            _panEnd.copy(_panStart);
        }
        listeningMoveUp = true;
    }

    public void touchMove(MotionEvent event) {
        if (!enabled || !listeningMoveUp) {
            return;
        }
        if ( _state == State.ROTATE && ! noRotate ) {
            _movePrev.copy( _moveCurr );
            _moveCurr.copy( getTouchOnCircle( event.getX(), event.getY() ) );
        } else if ( _state == State.ZOOM && ! noZoom ) {
            if (event.getPointerCount() > 1) {
                float dx = event.getX(0) - event.getX(1);
                float dy = event.getY(0) - event.getY(1);
                _touchZoomDistanceEnd = (float) Math.sqrt( dx * dx + dy * dy );
            }
        } else if ( _state == State.PAN && ! noPan ) {
            _panEnd.copy( getTouchOnScreen( event.getX(), event.getY() ) );
        }
    }

    public void touchUp() {
        if (!enabled || !listeningMoveUp) {
            return;
        }
        _state = State.NONE;
        listeningMoveUp = false;
    }

    private void rotateCamera() {
        Vector3 axis = new Vector3();
        Vector3 eyeDirection = new Vector3();
        Vector3 upDirection = new Vector3();
        Vector3 sidewayDirection = new Vector3();
        Vector3 moveDirection = new Vector3();
        Quaternion quaternion = new Quaternion();
        moveDirection.set(_moveCurr.x - _movePrev.x, _moveCurr.y - _movePrev.y, 0);
        float angle = moveDirection.length();
        if (angle > 0) {
            _eye.copy(object.position).sub(target);
            eyeDirection.copy(_eye).normalize();
            upDirection.copy(object.up).normalize();
            sidewayDirection.crossVectors(upDirection, eyeDirection).normalize();

            upDirection.setLength(_moveCurr.y - _movePrev.y);
            sidewayDirection.setLength((_moveCurr.x - _movePrev.x));

            moveDirection.copy(upDirection.add(sidewayDirection));

            axis.crossVectors(moveDirection, _eye).normalize();
            angle *= rotateSpeed;
            quaternion.setFromAxisAngle(axis, angle);
            _eye.applyQuaternion(quaternion);
            object.up.applyQuaternion(quaternion);

            _lastAxis.copy(axis);
            _lastAngle = angle;
        }
        _movePrev.copy( _moveCurr );
    }

    private void zoomCamera() {
        if (_state == State.ZOOM) {
            float factor = _touchZoomDistanceStart / _touchZoomDistanceEnd;
            _touchZoomDistanceStart = _touchZoomDistanceEnd;
            _eye.multiplyScalar( factor );
        }
    }

    private void panCamera() {
        Vector2 touchChange = _panEnd.clone().sub(_panStart);
        if (touchChange.lengthSq() > 0) {
            touchChange.multiplyScalar(_eye.length() * panSpeed);
            Vector3 pan = _eye.clone().cross(object.up).setLength(touchChange.x);
            pan.add(object.up.clone().setLength(touchChange.y) );
            object.position.add(pan);
            target.add(pan);
            _panStart.add(touchChange.subVectors(_panEnd, _panStart).multiplyScalar(dynamicDampingFactor) );
        }
    }

    public void update() {
        _eye.subVectors(object.position, target);
        if (!noRotate) {
            rotateCamera();
        }
        if (!noZoom) {
            zoomCamera();
        }
        if (!noPan) {
            panCamera();
        }
        object.position.addVectors(target, _eye);

        object.lookAt(target);
        if (lastPosition.distanceToSquared(object.position) > EPS) {
            dispatchEvent(changeEvent);
            lastPosition.copy(object.position);
        }
    }

    public void reset() {
        _state = State.NONE;
        target.copy(target0);
        object.position.copy(position0);
        object.up.copy(up0);
        _eye.subVectors(object.position, target);
        object.lookAt(target);
        dispatchEvent(changeEvent);
        lastPosition.copy(object.position);
    }
}
