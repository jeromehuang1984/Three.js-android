package edu.three.math;

import edu.three.core.IUpdate;

public class Euler {
  public static String DefaultOrder = "XYZ";
  public static String[] RotationOrders = {"XYZ", "YZX", "ZXY", "XZY", "YXZ", "ZYX"};

  double _x, _y, _z;
  public String _order = DefaultOrder;
  Matrix4 _matrix = new Matrix4();
  Quaternion __quaternion = new Quaternion();
  IUpdate _onChangeCallback = null;

  public Euler() {}
  public Euler(double x, double y, double z) {
    this._x = x;
    this._y = y;
    this._z = z;
  }

  void update() {
    if (_onChangeCallback != null)
      _onChangeCallback.updated();
  }
  public void x(double val) {
    _x = val;
    update();
  }
  public double x() {
    return _x;
  }

  public void y(double val) {
    _y = val;
    update();
  }
  public double y() {
    return _y;
  }

  public void z(double val) {
    _z = val;
    update();
  }
  public double z() {
    return _z;
  }

  public void onChange(IUpdate callback) {
    _onChangeCallback = callback;
  }

  public Euler copy(Euler euler) {
    this._x = euler._x;
    this._y = euler._y;
    this._z = euler._z;
    this._order = euler._order;
    return this;
  }

  public Euler clone() {
    return new Euler().copy(this);
  }

  public Euler set(double x, double y, double z, String order) {
    this._x = x;
    this._y = y;
    this._z = z;
    _order = order;
    return this;
  }

  public Euler setFromRotationMatrix(Matrix4 m) {
    return setFromRotationMatrix(m, DefaultOrder);
  }
  public Euler setFromRotationMatrix(Matrix4 m, String order) {
    double[] te = m.te;
    double m11 = te[ 0 ], m12 = te[ 4 ], m13 = te[ 8 ];
    double m21 = te[ 1 ], m22 = te[ 5 ], m23 = te[ 9 ];
    double m31 = te[ 2 ], m32 = te[ 6 ], m33 = te[ 10 ];
    if (order == null)
      order = _order;
    if (order.equals("XYZ") ) {
      this._y = Math.asin( MathTool.clamp( m13, - 1, 1 ) );

      if ( Math.abs( m13 ) < 0.9999999 ) {

        this._x = Math.atan2( - m23, m33 );
        this._z = Math.atan2( - m12, m11 );

      } else {

        this._x = Math.atan2( m32, m22 );
        this._z = 0;

      }
    } else if (order.equals("YXZ") ) {
      this._x = Math.asin( - MathTool.clamp( m23, - 1, 1 ) );

      if ( Math.abs( m23 ) < 0.9999999 ) {

        this._y = Math.atan2( m13, m33 );
        this._z = Math.atan2( m21, m22 );

      } else {

        this._y = Math.atan2( - m31, m11 );
        this._z = 0;

      }
    } else if (order.equals("ZXY") ) {
      this._x = Math.asin( MathTool.clamp( m32, - 1, 1 ) );

      if ( Math.abs( m32 ) < 0.9999999 ) {

        this._y = Math.atan2( - m31, m33 );
        this._z = Math.atan2( - m12, m22 );

      } else {

        this._y = 0;
        this._z = Math.atan2( m21, m11 );

      }
    } else if (order.equals("ZYX")) {
      this._y = Math.asin( - MathTool.clamp( m31, - 1, 1 ) );

      if ( Math.abs( m31 ) < 0.9999999 ) {

        this._x = Math.atan2( m32, m33 );
        this._z = Math.atan2( m21, m11 );

      } else {

        this._x = 0;
        this._z = Math.atan2( - m12, m22 );

      }
    } else if (order.equals("YZX")) {
      this._z = Math.asin( MathTool.clamp( m21, - 1, 1 ) );

      if ( Math.abs( m21 ) < 0.9999999 ) {

        this._x = Math.atan2( - m23, m22 );
        this._y = Math.atan2( - m31, m11 );

      } else {

        this._x = 0;
        this._y = Math.atan2( m13, m33 );

      }
    } else if (order.equals("XZY")) {
      this._z = Math.asin( - MathTool.clamp( m12, - 1, 1 ) );

      if ( Math.abs( m12 ) < 0.9999999 ) {

        this._x = Math.atan2( m32, m22 );
        this._y = Math.atan2( m13, m11 );

      } else {

        this._x = Math.atan2( - m23, m33 );
        this._y = 0;

      }
    } else {
      //THREE.Euler: .setFromRotationMatrix() encountered an unknown order:
    }
    return this;
  }

  public Euler setFromQuaternion(Quaternion q) {
    return setFromQuaternion(q, _order);
  }
  public Euler setFromQuaternion(Quaternion q, String order) {
    _matrix.makeRotationFromQuaternion(q);
    return setFromRotationMatrix(_matrix, order);
  }

  public Euler setFromVector3(Vector3 v, String order) {
    return set(v.x, v.y, v.z, order == null ? _order : order);
  }

  public Euler reorder(String newOrder) {
    __quaternion.setFromEuler(this);
    return setFromQuaternion(__quaternion, newOrder);
  }

  public boolean equals(Euler euler) {
    return ( euler._x == this._x ) && ( euler._y == this._y ) && ( euler._z == this._z ) && ( euler._order == this._order );
  }

  public Euler fromArray(double[] array) {
    _x = array[0];
    _y = array[1];
    _z = array[2];
    _order = RotationOrders[(int) array[3]];
    return this;
  }

  public double[] toArray(double[] array) {
    return toArray(array, 0);
  }
  public double[] toArray(double[] array, int offset) {
    array[ offset ] = this._x;
    array[ offset + 1 ] = this._y;
    array[ offset + 2 ] = this._z;
    array[ offset + 3 ] = MathTool.indexOf(RotationOrders, _order);

    return array;
  }

  public Vector3 toVector3(Vector3 optionalResult) {
    if (optionalResult != null) {
      return optionalResult.set(_x, _y, _z);
    } else {
      return new Vector3(_x, _y, _z);
    }
  }
}
