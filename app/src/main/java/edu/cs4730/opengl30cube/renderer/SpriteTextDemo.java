package edu.cs4730.opengl30cube.renderer;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import edu.three.buffergeometries.PlaneBufferGeometry;
import edu.three.cameras.PerspectiveCamera;
import edu.three.control.Screen;
import edu.three.control.TrackballControls;
import edu.three.geometries.param.PlaneParam;
import edu.three.helpers.AxesHelper;
import edu.three.materials.MeshBasicMaterial;
import edu.three.materials.SpriteMaterial;
import edu.three.math.MathTool;
import edu.three.math.Vector3;
import edu.three.objects.Mesh;
import edu.three.objects.Sprite;
import edu.three.renderers.GLRenderer;
import edu.three.scenes.Scene;
import edu.three.textures.Texture;

public class SpriteTextDemo extends BaseRender {
  private PerspectiveCamera camera;
  private Scene scene = new Scene();

  private static final float Z_NEAR = 0.1f;
  private static final float Z_FAR = 1000f;

  class PointXY {
    float x, y;

    public PointXY(float x, float y) {
      this.x = x;
      this.y = y;
    }
  }

  class TextStyleVO {
    int color;
    int size;
    Typeface font = Typeface.SERIF;

    public TextStyleVO(int color, int size) {
      this.color = color;
      this.size = size;
    }
  }
  //
  public SpriteTextDemo(GLSurfaceView view) {
    super(view);
  }

  Bitmap drawText(String text, TextStyleVO vo) {
    Paint p = new Paint();
    p.setColor(vo.color);
    p.setTypeface(vo.font);
    p.setTextSize(vo.size);

    //获取高度
    Paint.FontMetricsInt metrics = p.getFontMetricsInt();
    int height=metrics.bottom-metrics.top;
    //获取宽度
    Rect rect=new Rect();
    p.getTextBounds(text, 0, text.length(), rect);
    int width = rect.width();//文本的宽度

    Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(bitmap);
    canvas.drawText(text, 0,-metrics.ascent, p);
    return bitmap;
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

    Bitmap bitmap = drawText("模型120", new TextStyleVO(0xff00bbaa, 24));
    Texture texture = new Texture();
    texture.setImage(bitmap);

    SpriteMaterial material = new SpriteMaterial();
    material.map = texture;
    material.depthTest = material.depthWrite = false;
    Sprite sprite = new Sprite(material);
    sprite.scale.set(3, 3*bitmap.getHeight()/((float) bitmap.getWidth()), 1);
//    sprite.scale.set(bitmap.getWidth()/(mHeight*2f), bitmap.getHeight()/(mHeight*2f), 1);
    scene.add(sprite);

    PlaneBufferGeometry planeGeo = new PlaneBufferGeometry(new PlaneParam(5, 5, 2,2));
    MeshBasicMaterial basicMaterial = new MeshBasicMaterial(0xffbb5cdd);
    basicMaterial.wireframe = true;
    Mesh planeMesh = new Mesh(planeGeo, basicMaterial);
    planeMesh.rotateX(-MathTool.PI/2);
    planeMesh.position.y = -2;
    scene.add(planeMesh);

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
