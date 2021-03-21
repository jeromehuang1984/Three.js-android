package edu.cs4730.opengl30cube;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.lang.ref.WeakReference;

import edu.cs4730.opengl30cube.renderer.BaseRender;
import edu.cs4730.opengl30cube.renderer.LambertPhongLightRender;
import edu.cs4730.opengl30cube.renderer.RaycastCameraControlView;
import edu.cs4730.opengl30cube.renderer.SpriteTextDemo;
import edu.cs4730.opengl30cube.renderer.TextureDemo;
import edu.three.renderers.GLRenderer;
import edu.util.RawShaderLoader;

import static android.opengl.GLSurfaceView.DEBUG_CHECK_GL_ERROR;
import static android.opengl.GLSurfaceView.DEBUG_LOG_GL_CALLS;

public class MainActivity extends Activity implements AdapterView.OnItemClickListener {

    GLSurfaceView glSurfaceView;
    BaseRender renderer;
    ViewGroup root;
    ListView listView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RawShaderLoader.mContext = new WeakReference<>(getApplicationContext());
        root = findViewById(R.id.main_root);
        listView = findViewById(R.id.main_list);
        ArrayAdapter<MainListItems.Item> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
                android.R.id.text1, MainListItems.ITEMS);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);

//        int position = MainListItems.getIndex(SpriteTextDemo.class);
////        int position = MainListItems.getIndex(LambertPhongLightRender.class);
//        listView.performItemClick(adapter.getView(position, null, listView), position,
//                adapter.getItemId(position));

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (glSurfaceView != null) {
            glSurfaceView.onTouchEvent(event);
        }
        return true;
    }

    @Override
    protected void onPause() {
        if (glSurfaceView != null && renderer != null) {
            glSurfaceView.queueEvent(new Runnable() {
                @Override
                public void run() {
                    renderer.onPause();
                }
            });
        }
        super.onPause();

        if (glSurfaceView != null) {
            glSurfaceView.onPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (glSurfaceView != null) {
            glSurfaceView.onResume();
        }
    }

    @Override
    public void onBackPressed() {
        if (glSurfaceView != null) {
            int childCount = root.getChildCount();
            int i = 0;
            while (i < childCount) {
                if (root.getChildAt(i) != listView) {
                    root.removeViewAt(i);
                    childCount--;
                    i--;
                }
                i++;
            }
            glSurfaceView = null;
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Class clickClass = MainListItems.getClass(position);
        glSurfaceView = new GLSurfaceView(this);
        root.addView(glSurfaceView);

        // Create an OpenGL ES 3.0 context.
        glSurfaceView.setEGLContextClientVersion(3);
        glSurfaceView.setEGLConfigChooser(true);
        glSurfaceView.setDebugFlags(BuildConfig.DEBUG ? DEBUG_LOG_GL_CALLS : DEBUG_CHECK_GL_ERROR);

        renderer = (BaseRender) MainListItems.getRenderer(clickClass, glSurfaceView);
        if (renderer == null) {
            Toast.makeText(this, "java reflection construction failure.", Toast.LENGTH_SHORT).show();
            return;
        }
        glSurfaceView.setRenderer(renderer);
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        glSurfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                renderer.onTouchEvent(event);
                return true;
            }
        });
    }
}
