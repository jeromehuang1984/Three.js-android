package edu.three.renderers.gl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import edu.three.cameras.Camera;
import edu.three.core.BufferGeometry;
import edu.three.core.Event;
import edu.three.core.IListener;
import edu.three.core.Object3D;
import edu.three.materials.Material;
import edu.three.objects.GroupItem;
import edu.three.scenes.Scene;

import static edu.three.math.MathTool.sign;

public class GLRenderLists {
    HashMap<Long, List> lists = new HashMap<>();
    IListener onSceneDispose = new IListener() {
        @Override
        public void onEvent(Event event) {
            Scene scene = (Scene) event.target;
            scene.removeEventListener("dispose", onSceneDispose);
            lists.remove(scene.id);
        }
    };

    public List get(Scene scene, Camera camera) {
        List cameras = lists.get(scene.id);
        List list;
        if (cameras == null) {
            list = new List();
            list.children.put(camera.id, new List());
            lists.put(scene.id, list);
            scene.addEventListener("dispose", onSceneDispose);
        } else {
            list = cameras.children.get(camera.id);
            if (list == null) {
                list = new List();
                cameras.children.put(camera.id, list);
            }
        }
        return list;
    }

    public void dispose() {
        lists.clear();
    }

    public static class List {
        ArrayList<RenderItem> renderItems = new ArrayList<>();
        int renderItemsIndex = 0;
        public ArrayList<RenderItem> opaque = new ArrayList<>();
        public ArrayList<RenderItem> transparent = new ArrayList<>();

        GLProgram defaultProgram = new GLProgram(-1);

        public HashMap<Long, List> children = new HashMap<>();

        Comparator<RenderItem> painterSortStable = new Comparator<RenderItem>() {
            @Override
            public int compare(RenderItem a, RenderItem b) {
                if ( a.groupOrder != b.groupOrder ) {
                    return a.groupOrder - b.groupOrder;
                } else if ( a.renderOrder != b.renderOrder ) {
                    return a.renderOrder - b.renderOrder;
                } else if ( a.program != b.program ) {
                    return a.program.id - b.program.id;
                } else if ( a.material.id != b.material.id ) {
                    return sign(a.material.id - b.material.id);
                } else if ( a.z != b.z ) {
                    return sign(a.z - b.z);
                } else {
                    return sign(a.id - b.id);
                }
            }
        };

        Comparator<RenderItem> reversePainterSortStable = new Comparator<RenderItem>() {
            @Override
            public int compare(RenderItem a, RenderItem b) {
                if ( a.groupOrder != b.groupOrder ) {
                    return a.groupOrder - b.groupOrder;
                } else if ( a.renderOrder != b.renderOrder ) {
                    return a.renderOrder - b.renderOrder;
                } else if ( a.z != b.z ) {
                    return sign(b.z - a.z);
                } else {
                    return sign(a.id - b.id);
                }
            }
        };

        public void init() {
            renderItemsIndex = 0;
            opaque.clear();
            transparent.clear();
        }

        public RenderItem getNextRenderItem(Object3D object, BufferGeometry geometry, Material material,
                                            int groupOrder, float z, GroupItem group) {
            RenderItem renderItem = null;
            if (renderItems.size() > renderItemsIndex) {
                renderItem = renderItems.get(renderItemsIndex);
            } else {
                renderItem = new RenderItem();
                renderItems.add(renderItem);
            }
            renderItem.id = object.id;
            renderItem.object = object;
            renderItem.geometry = geometry;
            renderItem.material = material;
            renderItem.program = material.program!= null ? material.program : defaultProgram;
            renderItem.groupOrder = groupOrder;
            renderItem.renderOrder = object.renderOrder;
            renderItem.z = z;
            renderItem.group = group;
            renderItemsIndex ++;

            return renderItem;
        }

        public void push(Object3D object, BufferGeometry geometry, Material material, int groupOrder, double z, GroupItem group) {
            RenderItem renderItem = getNextRenderItem(object, geometry, material, groupOrder, (float)z, group);
            if (material.transparent) {
                transparent.add(renderItem);
            } else {
                opaque.add(renderItem);
            }
        }

        public void unshift(Object3D object, BufferGeometry geometry, Material material, int groupOrder, float z, GroupItem group) {
            RenderItem renderItem = getNextRenderItem(object, geometry, material, groupOrder, z, group);
            if (material.transparent) {
                transparent.add(0, renderItem);
            } else {
                opaque.add(0, renderItem);
            }
        }

        public void sort() {
            if (opaque.size() > 1) {
                Collections.sort(opaque, painterSortStable);
            }
            if (transparent.size() > 1) {
                Collections.sort(transparent, reversePainterSortStable);
            }
        }

        public static class RenderItem {
            public long id;
            public Object3D object;
            public BufferGeometry geometry;
            public Material material;
            public GLProgram program;
            public int groupOrder;
            public int renderOrder;
            public float z;
            public GroupItem group;
        }
    }
}
