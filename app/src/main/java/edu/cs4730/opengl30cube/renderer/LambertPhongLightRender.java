package edu.cs4730.opengl30cube.renderer;

import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.MotionEvent;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import edu.three.buffergeometries.BoxBufferGeometry;
import edu.three.buffergeometries.PlaneBufferGeometry;
import edu.three.buffergeometries.SphereBufferGeometry;
import edu.three.cameras.PerspectiveCamera;
import edu.three.control.Screen;
import edu.three.control.TrackballControls;
import edu.three.geometries.param.BoxParam;
import edu.three.geometries.param.PlaneParam;
import edu.three.geometries.param.SphereParam;
import edu.three.helpers.AxesHelper;
import edu.three.lights.AmbientLight;
import edu.three.lights.DirectionalLight;
import edu.three.lights.PointLight;
import edu.three.lights.SpotLight;
import edu.three.materials.MeshLambertMaterial;
import edu.three.materials.MeshPhongMaterial;
import edu.three.math.Color;
import edu.three.math.Vector3;
import edu.three.objects.Mesh;
import edu.three.renderers.GLRenderer;
import edu.three.scenes.Scene;

import static edu.three.constant.Constants.BasicShadowMap;
import static edu.three.constant.Constants.DoubleSide;

public class LambertPhongLightRender extends BaseRender {
    private String TAG = getClass().getSimpleName();
    private PerspectiveCamera camera;
    private Scene scene;
    private Mesh mCube;
    private Mesh plane;
    private Mesh sphere;
    SpotLight cameraLight;
    private static final float Z_NEAR = 0.1f;
    private static final float Z_FAR = 1000f;

    float step = 0;

    //
    public LambertPhongLightRender(GLSurfaceView view) {
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
        renderer.setClearColor(0x999999, 1);
        renderer.shadowMap.enabled = true;
        renderer.shadowMap.type = BasicShadowMap;

        float density = mView.getContext().getResources().getDisplayMetrics().density;
        Log.d(TAG, "screen density:" + density);
//            renderer.setPixelRatio(1);

        //create the ground plane
        PlaneBufferGeometry planeGeometry = new PlaneBufferGeometry(new PlaneParam(40, 20, 1, 1));
        MeshLambertMaterial planceMaterial = new MeshLambertMaterial();
        planceMaterial.color = new Color(0x6d6565);
        planceMaterial.side = DoubleSide;
        plane = new Mesh(planeGeometry, planceMaterial);
        plane.receiveShadow = true;
        plane.rotateX(-0.5f * (float) Math.PI);
        plane.position.set(0, 0, 0);
        scene.add(plane);

        BoxBufferGeometry cubeGeometry = new BoxBufferGeometry(new BoxParam(4, 4, 4));
//            MeshLambertMaterial cubeMaterial = new MeshLambertMaterial();
        MeshPhongMaterial cubeMaterial = new MeshPhongMaterial();
        cubeMaterial.color = new Color(0xff0000);
        mCube = new Mesh(cubeGeometry, cubeMaterial);
        mCube.castShadow = true;
        mCube.receiveShadow = true;
        mCube.position.set(-4, 3, 0);
        scene.add(mCube);

        SphereBufferGeometry sphereGeometry = new SphereBufferGeometry(new SphereParam(4, 20, 20));
//            MeshLambertMaterial sphereMaterial = new MeshLambertMaterial();
        MeshPhongMaterial sphereMaterial = new MeshPhongMaterial();
        sphereMaterial.color = new Color(0x7777ff);
        sphere = new Mesh(sphereGeometry, sphereMaterial);
//            sphere.position.set(20, 4, 2);    // for spot light
        sphere.position.set(8, 4, 0); // for directional light
        sphere.castShadow = true;
//            sphere.receiveShadow = true;
        scene.add(sphere);

        camera = new PerspectiveCamera(45, aspect, Z_NEAR, Z_FAR);
        camera.position = new Vector3(-30, 40, 30);
        camera.lookAt(scene.position);
        controls = new TrackballControls(new Screen(0, 0, mWidth, mHeight), camera);

        AxesHelper axesHelper = new AxesHelper(20);
        scene.add(axesHelper);

        AmbientLight ambientLight = new AmbientLight(0x0cccc);
        scene.add(ambientLight);

        SpotLight spotLight = new SpotLight(0xffffff);
        spotLight.position.set(-40, 60, -10);
        spotLight.castShadow = true;
        spotLight.getShadow().mapSize.x = 2048;
        spotLight.getShadow().mapSize.y = 2048;
        scene.add(spotLight);

        DirectionalLight directionalLight = new DirectionalLight();
        directionalLight.position.set(-20, 15, 10);
        directionalLight.castShadow = true;
        directionalLight.getShadow().mapSize.x = 2048;
        directionalLight.getShadow().mapSize.y = 2048;
        directionalLight.target = sphere;
//            scene.add(directionalLight);

        PointLight pointLight = new PointLight(0xffffff, 2, 100, 1);
        pointLight.position.set(-40, 60, -10);
        pointLight.castShadow = true;
//            scene.add(pointLight);

        cameraLight = new SpotLight(0xffffff);
        cameraLight.position.set(555, 555, 555);
//            scene.add(cameraLight);
    }

    // /
    // Draw a triangle using the shader pair created in onSurfaceCreated()
    //
    public void onDrawFrame(GL10 glUnused) {
        mCube.rotateX(0.02f);
        mCube.rotateY(0.02f);
        mCube.rotateZ(0.02f);

        step += 0.04f;
        sphere.position.x = -4 + (10 * (float) Math.cos(step));
        sphere.position.y = 4 + (20 * Math.abs((float) Math.sin(step)));

        cameraLight.position.copy(camera.position);
        renderer.render(scene, camera);
        ((TrackballControls) controls).update();
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
}
