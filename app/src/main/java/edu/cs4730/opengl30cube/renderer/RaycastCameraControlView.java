package edu.cs4730.opengl30cube.renderer;

import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.MotionEvent;

import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import edu.three.buffergeometries.BoxBufferGeometry;
import edu.three.buffergeometries.CylinderBufferGeometry;
import edu.three.buffergeometries.PlaneBufferGeometry;
import edu.three.buffergeometries.SphereBufferGeometry;
import edu.three.cameras.PerspectiveCamera;
import edu.three.control.Screen;
import edu.three.control.TrackballControls;
import edu.three.core.Object3D;
import edu.three.core.Raycaster;
import edu.three.geometries.param.BoxParam;
import edu.three.geometries.param.CylinderParam;
import edu.three.geometries.param.PlaneParam;
import edu.three.geometries.param.SphereParam;
import edu.three.helpers.AxesHelper;
import edu.three.materials.MeshBasicMaterial;
import edu.three.materials.MeshNormalMaterial;
import edu.three.math.Color;
import edu.three.math.Matrix4;
import edu.three.math.Triangle;
import edu.three.math.Vector2;
import edu.three.math.Vector3;
import edu.three.objects.Arrow;
import edu.three.objects.Mesh;
import edu.three.objects.RaycastItem;
import edu.three.renderers.GLRenderer;
import edu.three.scenes.Scene;

public class RaycastCameraControlView extends BaseRender {
    private static String TAG = "RaycastCameraControlView";
    private PerspectiveCamera camera;
    private Scene scene;
    private AxesHelper axesHelper;
    private Mesh mCube;
    private Mesh cylinder;
    private Mesh plane;
    private Mesh sphere;
    private Mesh arrow = null;
    private ArrayList<Object3D> raycastLst;
    private static final float Z_NEAR = 1f;
    private static final float Z_FAR = 40f;

    //
    public RaycastCameraControlView(GLSurfaceView view) {
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
        camera.position = new Vector3(10, 10, 10);
        controls = new TrackballControls(new Screen(0, 0, mWidth, mHeight), camera);
        scene = new Scene();

        MeshBasicMaterial material = new MeshBasicMaterial();
        material.wireframe = true;
        material.color = new Color().setHex(0x00cc00);
        mCube = new Mesh(new BoxBufferGeometry(new BoxParam()) );
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
        mCube.applyMatrix(new Matrix4().makeRotationAxis(new Vector3(1, 1, 1).normalize(), (float)Math.PI/3));
        scene.add(mCube);
        raycastLst.add(mCube);

        axesHelper = new AxesHelper(20);
        scene.add(axesHelper);

//            material = new MeshBasicMaterial();
//            material.wireframe = true;
//            material.color = new Color(0x00cccc);   //cyan
        cylinder = new Mesh(new CylinderBufferGeometry(new CylinderParam()), new MeshNormalMaterial());
        cylinder.translateX(4).translateY(2);
        scene.add(cylinder);
        raycastLst.add(cylinder);

        material = new MeshBasicMaterial();
        material.wireframe = true;
        material.color = new Color(0x99cc99);
        plane = new Mesh(new PlaneBufferGeometry(new PlaneParam(3, 3, 2, 2)),
                material);
        plane.translateY(-2).rotateX( (float)-Math.PI/2 );
        scene.add(plane);

        MeshNormalMaterial normalMaterial = new MeshNormalMaterial();
        normalMaterial.wireframe = true;
        sphere = new Mesh(new SphereBufferGeometry(new SphereParam()), normalMaterial);
        sphere.translateX(-2).translateY(1).translateZ(-1);
        scene.add(sphere);
        raycastLst.add(sphere);

        arrow = new Arrow(new MeshBasicMaterial().setWireFrame(true));

        GLRenderer.Param param = new GLRenderer.Param();
        param.antialias = true;
        renderer = new GLRenderer(param, mWidth, mHeight);
        renderer.setClearColor(0x000000, 1);
        float density = mView.getContext().getResources().getDisplayMetrics().density;
        Log.d(TAG, "screen density:" + density);
//            renderer.setPixelRatio(1);
    }

    // /
    // Draw a triangle using the shader pair created in onSurfaceCreated()
    //
    public void onDrawFrame(GL10 glUnused) {
        if (renderer != null) {
            renderer.render(scene, camera);
            controls.update();
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

    public void onTouchEvent(MotionEvent event) {
        if (controls != null) {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    final ArrayList<RaycastItem> selected = getIntersectsByEvent(event);
                    if (selected.size() > 0) {
                        mView.queueEvent(new Runnable() {
                            @Override
                            public void run() {
                                //remove arrow first
                                if (arrow.getParent() != null) {
                                    arrow.getParent().remove(arrow);
                                }
                                RaycastItem item = selected.get(0);
                                Log.d("selected face: ", item.faceIndex + "");
                                Triangle tri = item.triangle;

                                ((Arrow) arrow).set(tri.getNormal(), tri.getCenter(), 0.3f);
                                item.object.add(arrow);
                            }
                        });
                    }
                    controls.touchDown(event);
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    controls.touchDown(event);
                    break;
                case MotionEvent.ACTION_MOVE:
                    controls.touchMove(event);
                    break;
                case MotionEvent.ACTION_UP:
                    controls.touchUp();
                    break;
            }
        }
    }

    private ArrayList<RaycastItem> getIntersectsByEvent(MotionEvent event) {
        Vector2 mouse = new Vector2();
        mouse.x = (event.getX() / mWidth) * 2 - 1;
        mouse.y = -(event.getY() / mHeight) * 2 + 1;
        Raycaster raycaster = new Raycaster();
        raycaster.setFromCamera(mouse, camera);
        return raycaster.intersectObjects(raycastLst, false);
    }
}
