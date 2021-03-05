package edu.three.control;

import android.view.MotionEvent;

public interface ITouchable {
  void touchDown(MotionEvent event);
  void pointerDown(MotionEvent event);
  void touchMove(MotionEvent event);
  void touchUp();
}
