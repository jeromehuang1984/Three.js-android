package edu.cs4730.opengl30cube.renderer;

import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import edu.cs4730.opengl30cube.R;
import edu.three.buffergeometries.BoxBufferGeometry;
import edu.three.cameras.PerspectiveCamera;
import edu.three.control.Screen;
import edu.three.control.TrackballControls;
import edu.three.core.BufferAttribute;
import edu.three.core.BufferGeometry;
import edu.three.core.InstancedBufferGeometry;
import edu.three.core.InterleavedBuffer;
import edu.three.core.InterleavedBufferAttribute;
import edu.three.geometries.param.BoxParam;
import edu.three.helpers.AxesHelper;
import edu.three.loaders.TextureLoader;
import edu.three.materials.Material;
import edu.three.materials.MeshBasicMaterial;
import edu.three.math.Matrix4;
import edu.three.math.Quaternion;
import edu.three.math.Vector3;
import edu.three.objects.InstancedMesh;
import edu.three.objects.Mesh;
import edu.three.renderers.GLRenderer;
import edu.three.scenes.Scene;
import edu.three.textures.Texture;

public class InstancingDemo extends BaseRender {
  private String TAG = getClass().getSimpleName();
  private PerspectiveCamera camera;
  private Scene scene = new Scene();

  private static final float Z_NEAR = 0.1f;
  private static final float Z_FAR = 1000f;

  int instances = 5000;
  long lastTime = 0;
  Quaternion moveQ = new Quaternion(0.5, 0.5, 0.5, 0).normalize();
  Quaternion tmpQ = new Quaternion();
  Matrix4 tmpM = new Matrix4();
  Matrix4 currentM = new Matrix4();
  InstancedMesh mesh;

  //
  public InstancingDemo(GLSurfaceView view) {
    super(view);
  }

  ///
  // Initialize the shader and program object
  //
  public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
    mWidth = mView.getWidth();
    mHeight = mView.getHeight();
    aspect = (float) mWidth / mHeight;

    scene = new Scene();
    GLRenderer.Param param = new GLRenderer.Param();
    param.antialias = true;
    renderer = new GLRenderer(param, mWidth, mHeight);
    renderer.setClearColor(0x101010, 1);

    InstancedBufferGeometry geometry = new InstancedBufferGeometry();
    // per mesh data x,y,z,w,u,v,s,t for 4-element alignment
    // only use x,y,z and u,v; but x, y, z, nx, ny, nz, u, v would be a good layout
    InterleavedBuffer vertexBuffer = new InterleavedBuffer(new float[] {
        // Front
        - 1, 1, 1, 0, 0, 0, 0, 0,
        1, 1, 1, 0, 1, 0, 0, 0,
        - 1, - 1, 1, 0, 0, 1, 0, 0,
        1, - 1, 1, 0, 1, 1, 0, 0,
        // Back
        1, 1, - 1, 0, 1, 0, 0, 0,
        - 1, 1, - 1, 0, 0, 0, 0, 0,
        1, - 1, - 1, 0, 1, 1, 0, 0,
        - 1, - 1, - 1, 0, 0, 1, 0, 0,
        // Left
        - 1, 1, - 1, 0, 1, 1, 0, 0,
        - 1, 1, 1, 0, 1, 0, 0, 0,
        - 1, - 1, - 1, 0, 0, 1, 0, 0,
        - 1, - 1, 1, 0, 0, 0, 0, 0,
        // Right
        1, 1, 1, 0, 1, 0, 0, 0,
        1, 1, - 1, 0, 1, 1, 0, 0,
        1, - 1, 1, 0, 0, 0, 0, 0,
        1, - 1, - 1, 0, 0, 1, 0, 0,
        // Top
        - 1, 1, 1, 0, 0, 0, 0, 0,
        1, 1, 1, 0, 1, 0, 0, 0,
        - 1, 1, - 1, 0, 0, 1, 0, 0,
        1, 1, - 1, 0, 1, 1, 0, 0,
        // Bottom
        1, - 1, 1, 0, 1, 0, 0, 0,
        - 1, - 1, 1, 0, 0, 0, 0, 0,
        1, - 1, - 1, 0, 1, 1, 0, 0,
        - 1, - 1, - 1, 0, 0, 1, 0, 0
    },8);

    // Use vertexBuffer, starting at offset 0, 3 items in position attribute
    InterleavedBufferAttribute positions = new InterleavedBufferAttribute(vertexBuffer, 3, 0);
    geometry.position = positions;
    // Use vertexBuffer, starting at offset 4, 2 items in uv attribute
    InterleavedBufferAttribute uvs = new InterleavedBufferAttribute(vertexBuffer, 2, 4);
    geometry.uv = uvs;

    int[] indices = {
        0, 2, 1,
        2, 3, 1,
        4, 6, 5,
        6, 7, 5,
        8, 10, 9,
        10, 11, 9,
        12, 14, 13,
        14, 15, 13,
        16, 17, 18,
        18, 17, 19,
        20, 21, 22,
        22, 21, 23
    };
    geometry.setIndex(new BufferAttribute(indices, 1));

    MeshBasicMaterial material = new MeshBasicMaterial();
    material.map = new TextureLoader().loadTexture(mView.getContext(), R.raw.t_crate);
    material.map.flipY = false;

    // per instance data
    Matrix4 matrix = new Matrix4();
    Vector3 offset = new Vector3();
    Quaternion orientation = new Quaternion();
    Vector3 scale = new Vector3(1,1,1);
    double x,y,z,w;

    mesh = new InstancedMesh(geometry, material, instances);
    for (int i = 0; i < instances; i++) {
      // offsets
      x = Math.random() * 100 - 50;
      y = Math.random() * 100 - 50;
      z = Math.random() * 100 - 50;

      offset.set(x,y,z).normalize();
      offset.multiplyScalar(5);// move out at least 5 units from center in current direction
      offset.set(x+offset.x, y+offset.y, z+offset.z);

      // orientations
      x = Math.random() * 2 - 1;
      y = Math.random() * 2 - 1;
      z = Math.random() * 2 - 1;
      w = Math.random() * 2 - 1;

      orientation.set( x, y, z, w ).normalize();

      matrix.compose( offset, orientation, scale );

      mesh.setMatrixAt( i, matrix );
    }

    scene.add(mesh);

    camera = new PerspectiveCamera(45, aspect, Z_NEAR, Z_FAR);
    camera.position = new Vector3(0, 0, 30);
    camera.lookAt(scene.position);
    controls = new TrackballControls(new Screen(0, 0, mWidth, mHeight), camera);

    AxesHelper axesHelper = new AxesHelper(20);
    scene.add(axesHelper);
  }

  // /
  // Draw a triangle using the shader pair created in onSurfaceCreated()
  //
  public void onDrawFrame(GL10 glUnused) {
    long time = System.currentTimeMillis();
    mesh.rotation.y(time * 0.00005);

    long delta = (time - lastTime) / 5000;
    tmpQ.set(moveQ.x() * delta, moveQ.y() * delta, moveQ.z() * delta, 1).normalize();
    tmpM.makeRotationFromQuaternion(tmpQ);
    for (int i = 0; i < instances; i++) {
      mesh.getMatrixAt(i, currentM);
      currentM.multiply(tmpM);
      mesh.setMatrixAt(i, currentM);
    }
    mesh.instanceMatrix.setNeedsUpdate(true);
    lastTime = time;

    renderer.render(scene, camera);
    ((TrackballControls) controls).update();
  }

  public void onSurfaceChanged(GL10 glUnused, int width, int height) {
    mWidth = width;
    mHeight = height;
    aspect = (float) mWidth / mHeight;
    camera.aspect = aspect;
    camera.updateProjectionMatrix();
    renderer.setSize(mWidth, mHeight);
  }
}
