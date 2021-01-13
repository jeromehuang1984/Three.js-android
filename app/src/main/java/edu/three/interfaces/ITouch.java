package edu.three.interfaces;

import android.view.MotionEvent;

public interface ITouch {
    void touchDown(MotionEvent event);
    void touchMove(MotionEvent event);
    void touchUp();
}
