package edu.cs4730.opengl30cube.renderer;

import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.MotionEvent;

import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import edu.three.buffergeometries.BoxBufferGeometry;
import edu.three.buffergeometries.PlaneBufferGeometry;
import edu.three.cameras.PerspectiveCamera;
import edu.three.control.OrbitControls;
import edu.three.control.Screen;
import edu.three.core.Object3D;
import edu.three.core.Raycaster;
import edu.three.geometries.param.BoxParam;
import edu.three.geometries.param.PlaneParam;
import edu.three.helpers.AxesHelper;
import edu.three.lights.DirectionalLight;
import edu.three.materials.MeshBasicMaterial;
import edu.three.materials.MeshLambertMaterial;
import edu.three.math.Color;
import edu.three.math.Triangle;
import edu.three.math.Vector2;
import edu.three.math.Vector3;
import edu.three.objects.Arrow;
import edu.three.objects.Mesh;
import edu.three.objects.RaycastItem;
import edu.three.renderers.GLRenderer;
import edu.three.scenes.Scene;

import static edu.three.constant.Constants.DoubleSide;

public class TestPerformanceView extends BaseRender {
    private static String TAG = "TestPerformanceView";
    private PerspectiveCamera camera;
    private Scene scene;
    private AxesHelper axesHelper;

    private Mesh plane;
    private Mesh arrow = null;
    private ArrayList<Object3D> raycastLst;
    private static final float Z_NEAR = 0.1f;
    private static final float Z_FAR = 10000f;

    //
    public TestPerformanceView(GLSurfaceView view) {
        super(view);
    }

    ///
    // Initialize the shader and program object
    //
    public void onSurfaceCreated(GL10 glUnused, EGLConfig config) {
        mWidth = mView.getWidth();
        mHeight = mView.getHeight();
        aspect = (float) mWidth / mHeight;
        raycastLst = new ArrayList<>();

        camera = new PerspectiveCamera(53.13f, aspect, Z_NEAR, Z_FAR);
        camera.position = new Vector3(0, 60, 20);
        controls = new OrbitControls(new Screen(0, 0, mWidth, mHeight), camera);
        scene = new Scene();

        DirectionalLight directionalLight = new DirectionalLight();
        directionalLight.position.set(1, 1, 0.5f);
        directionalLight.target = scene;
        scene.add(directionalLight);

        float sceneWidth = 33f, sceneHeight = 33f;
        float hw = sceneWidth/2, hh = sceneHeight/2;
        for (int i = 0; i < 1000; i++) {
            BoxBufferGeometry cubeGeo = new BoxBufferGeometry(new BoxParam((float)(0.3 + 0.7* Math.random()),
                    1f, (float)(0.3 + 0.7* Math.random()) ) );
            MeshLambertMaterial mat = new MeshLambertMaterial();
            mat.color = new Color().setHex((int) (0xffffff * Math.random()));
            Mesh box = new Mesh(cubeGeo, mat);
            box.position.set((float)(sceneWidth*Math.random()) - hw, 0.5f, (float)(sceneHeight*Math.random()) - hh );
            scene.add(box);
            raycastLst.add(box);
        }

        axesHelper = new AxesHelper(20);
        scene.add(axesHelper);

        MeshLambertMaterial material = new MeshLambertMaterial();
        material.color = new Color(0x99cc99);
        material.side = DoubleSide;
        plane = new Mesh(new PlaneBufferGeometry(new PlaneParam(sceneWidth, sceneHeight, 2, 2)),
                material);
        plane.rotateX( (float)-Math.PI/2 );
        scene.add(plane);

        arrow = new Arrow(new MeshBasicMaterial().setWireFrame(true));

        GLRenderer.Param param = new GLRenderer.Param();
        param.antialias = true;
        renderer = new GLRenderer(param, mWidth, mHeight);
        renderer.setClearColor(0x000000, 1);
        float density = mView.getContext().getResources().getDisplayMetrics().density;
        Log.d(TAG, "screen density:" + density);
//            renderer.setPixelRatio(1);
        ((OrbitControls) controls).update();
    }

    // /
    // Draw a triangle using the shader pair created in onSurfaceCreated()
    //
    public void onDrawFrame(GL10 glUnused) {
        if (renderer != null) {
            renderer.render(scene, camera);
        }
    }

    // /
    // Handle surface changes
    //
    public int dh;
    public void onSurfaceChanged(GL10 glUnused, int width, int height) {
        mWidth = width;
        mHeight = height;
        aspect = (float) mWidth / mHeight;
        camera.aspect = aspect;
        camera.updateProjectionMatrix();
        renderer.setSize(mWidth, mHeight);
    }

//    public void onTouchEvent(MotionEvent event) {
//        if (controls != null) {
//            switch (event.getAction() & MotionEvent.ACTION_MASK) {
//                case MotionEvent.ACTION_DOWN:
//                    final ArrayList<RaycastItem> selected = getIntersectsByEvent(event);
//                    if (selected.size() > 0) {
//                        mView.queueEvent(new Runnable() {
//                            @Override
//                            public void run() {
//                                //remove arrow first
//                                if (arrow.getParent() != null) {
//                                    arrow.getParent().remove(arrow);
//                                }
//                                RaycastItem item = selected.get(0);
//                                Log.d("selected face: ", item.faceIndex + "");
//                                Triangle tri = item.triangle;
//
//                                ((Arrow) arrow).set(tri.getNormal(), tri.getCenter(), 0.3f);
//                                item.object.add(arrow);
//                            }
//                        });
//                    }
//                    controls.touchDown(event);
//                    break;
//                case MotionEvent.ACTION_POINTER_DOWN:
//                    controls.touchDown(event);
//                    break;
//                case MotionEvent.ACTION_MOVE:
//                    controls.touchMove(event);
//                    break;
//                case MotionEvent.ACTION_UP:
//                    controls.touchUp();
//                    break;
//            }
//        }
//    }

    private ArrayList<RaycastItem> getIntersectsByEvent(MotionEvent event) {
        Vector2 mouse = new Vector2();
        mouse.x = (event.getX() / mWidth) * 2 - 1;
        mouse.y = -(event.getY() / mHeight) * 2 + 1;
        Raycaster raycaster = new Raycaster();
        raycaster.setFromCamera(mouse, camera);
        return raycaster.intersectObjects(raycastLst, false);
    }
}
