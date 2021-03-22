package edu.cs4730.opengl30cube.renderer;

import android.opengl.GLSurfaceView;

import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import edu.cs4730.opengl30cube.R;
import edu.three.buffergeometries.BoxBufferGeometry;
import edu.three.cameras.PerspectiveCamera;
import edu.three.control.Screen;
import edu.three.control.TrackballControls;
import edu.three.core.BufferAttribute;
import edu.three.core.InstancedBufferGeometry;
import edu.three.core.InterleavedBuffer;
import edu.three.core.InterleavedBufferAttribute;
import edu.three.extras.lib.lines.LineGeometry;
import edu.three.extras.lib.lines.LineMaterial;
import edu.three.extras.lib.lines.LineSegments2;
import edu.three.geometries.param.BoxParam;
import edu.three.helpers.AxesHelper;
import edu.three.loaders.TextureLoader;
import edu.three.materials.MeshBasicMaterial;
import edu.three.math.Color;
import edu.three.math.MathTool;
import edu.three.math.Matrix4;
import edu.three.math.Quaternion;
import edu.three.math.Vector2;
import edu.three.math.Vector3;
import edu.three.objects.InstancedMesh;
import edu.three.objects.Mesh;
import edu.three.renderers.GLRenderer;
import edu.three.scenes.Scene;

public class InstancingSegments extends BaseRender {
  private String TAG = getClass().getSimpleName();
  private PerspectiveCamera camera;
  private Scene scene = new Scene();

  private static final float Z_NEAR = 1f;
  private static final float Z_FAR = 1000f;

  //
  public InstancingSegments(GLSurfaceView view) {
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
    renderer.setClearColor(0x000000, 1);

    LineMaterial lineMaterial = new LineMaterial();
    lineMaterial.setColor(0x00bb00);
    lineMaterial.setLinewidth(32);
//    lineMaterial.transparent = true;
//    lineMaterial.dashed = false;
//    lineMaterial.depthWrite = lineMaterial.depthTest = false;
    lineMaterial.setResolution(new Vector2(mWidth, mHeight));

    LineGeometry lineGeo = new LineGeometry();
    Vector3[] points = {new Vector3(-1,1,0), new Vector3(1,1,0), new Vector3(-1,-1,0), new Vector3(1,-1,0)};
    lineGeo.setPositions(MathTool.flatten(points));

    LineSegments2 lineSegments = new LineSegments2(lineGeo, lineMaterial);
    lineSegments.computeLineDistances();
    scene.add(lineSegments);

    Mesh mCube = new Mesh(new BoxBufferGeometry(new BoxParam()) );
    int[] colors = new int[] {
        0xcccc00, 0xffffff, 0x0000cc, 0x009900, 0xbb0000, 0xcc6600
    };
    for (int i = 0; i < 6; i++) {
      MeshBasicMaterial materialFace = new MeshBasicMaterial();
      materialFace.color = new Color(colors[i]);
      materialFace.transparent = true;
      materialFace.opacity = 0.5f;
      mCube.material.add(materialFace);
    }
    scene.add(mCube);

    camera = new PerspectiveCamera(50, aspect, Z_NEAR, Z_FAR);
    camera.position = new Vector3(0, 0, 6);
    camera.lookAt(scene.position);
    controls = new TrackballControls(new Screen(0, 0, mWidth, mHeight), camera);

    AxesHelper axesHelper = new AxesHelper(20);
    scene.add(axesHelper);
  }

  // /
  // Draw a triangle using the shader pair created in onSurfaceCreated()
  //
  public void onDrawFrame(GL10 glUnused) {

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
