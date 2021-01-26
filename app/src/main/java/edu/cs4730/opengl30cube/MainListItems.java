package edu.cs4730.opengl30cube;

import android.opengl.GLSurfaceView;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;

import edu.cs4730.opengl30cube.renderer.CanvasTextureDemo;
import edu.cs4730.opengl30cube.renderer.LambertPhongLightRender;
import edu.cs4730.opengl30cube.renderer.RaycastCameraControlView;
import edu.cs4730.opengl30cube.renderer.SpriteDemo;
import edu.cs4730.opengl30cube.renderer.SpriteTextDemo;
import edu.cs4730.opengl30cube.renderer.TestPerformanceView;
import edu.cs4730.opengl30cube.renderer.TextureDemo;
import edu.three.objects.Sprite;

public class MainListItems {
    static ArrayList<Item> ITEMS = new ArrayList<Item>();
    static private HashMap<Class, Item> ITEM_MAP = new HashMap<>();
    static {
        addItem(new Item(RaycastCameraControlView.class, "光线碰撞，摄像机的轨迹球控制，法线材质，线框材质"));
        addItem(new Item(LambertPhongLightRender.class, "lambert材质, phong材质, 光照及阴影"));
        addItem(new Item(TextureDemo.class, "带纹理的立方体"));
        addItem(new Item(CanvasTextureDemo.class, "带canvas纹理的立方体"));
        addItem(new Item(SpriteDemo.class, "Sprite demo"));
        addItem(new Item(SpriteTextDemo.class, "Sprite text demo"));
        addItem(new Item(TestPerformanceView.class, "性能测试"));
    }

    public static class Item {
        public Class className;
        public String content;

        public Item(Class className, String content) {
            this.className = className;
            this.content = content;
        }
        public String toString() {
            return content;
        }
    }

    static private void addItem(Item item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.className, item);
    }

    static public int getIndex(Class className) {
        return ITEMS.indexOf(ITEM_MAP.get(className));
    }

    static public Class getClass(int index) {
        return ITEMS.get(index).className;
    }

    static public GLSurfaceView.Renderer getRenderer(Class className, GLSurfaceView context) {
        try {
            Constructor constructor = className.getConstructor(context.getClass());
            return (GLSurfaceView.Renderer) constructor.newInstance(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
