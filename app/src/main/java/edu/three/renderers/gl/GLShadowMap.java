package edu.three.renderers.gl;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

import edu.three.cameras.Camera;
import edu.three.constant.Constants;
import edu.three.core.BufferAttribute;
import edu.three.core.BufferGeometry;
import edu.three.core.Object3D;
import edu.three.lights.DirectionalLight;
import edu.three.lights.Light;
import edu.three.lights.LightShadow;
import edu.three.lights.PointLight;
import edu.three.lights.SpotLight;
import edu.three.lights.SpotLightShadow;
import edu.three.materials.Material;
import edu.three.materials.MeshDepthMaterial;
import edu.three.materials.MeshDistanceMaterial;
import edu.three.math.Frustum;
import edu.three.math.Matrix3;
import edu.three.math.Matrix4;
import edu.three.math.Vector2;
import edu.three.math.Vector3;
import edu.three.math.Vector4;
import edu.three.objects.GroupItem;
import edu.three.objects.Line;
import edu.three.objects.Mesh;
import edu.three.objects.Points;
import edu.three.objects.SkinnedMesh;
import edu.three.renderers.GLRenderTarget;
import edu.three.renderers.GLRenderer;
import edu.three.scenes.Scene;
import edu.three.textures.Texture;

public class GLShadowMap {
    public boolean enabled = false;
    public boolean autoUpdate = true;
    public boolean needsUpdate = false;
    public int type = Constants.PCFShadowMap;
    GLRenderer renderer;
    GLObjects objects;
    Frustum frustum = new Frustum();
    Matrix4 projScreenMatrix = new Matrix4();
    Vector2 shadowMapSize = new Vector2();
    Vector2 maxShadowMapSize;
    Vector3 lookTarget = new Vector3();
    Vector3 lightPositionWorld = new Vector3();
    int morphingFlag = 1;
    int skinningFlag = 2;
    int numberOfMaterialVariants = ( morphingFlag | skinningFlag ) + 1;

    MeshDepthMaterial[] depthMaterials;
    MeshDistanceMaterial[] distanceMaterials;

    HashMap<String, HashMap<String, Material>> materialCache = new HashMap<>();
    int[] shadowSide = new int[] {
            Constants.BackSide, Constants.FrontSide, Constants.DoubleSide
    };

    Vector3[] cubeDirections = new Vector3[] {
            new Vector3( 1, 0, 0 ), new Vector3( - 1, 0, 0 ), new Vector3( 0, 0, 1 ),
            new Vector3( 0, 0, - 1 ), new Vector3( 0, 1, 0 ), new Vector3( 0, - 1, 0 )
    };

    Vector3[] cubeUps = new Vector3[] {
            new Vector3( 0, 1, 0 ), new Vector3( 0, 1, 0 ), new Vector3( 0, 1, 0 ),
            new Vector3( 0, 1, 0 ), new Vector3( 0, 0, 1 ),	new Vector3( 0, 0, - 1 )
    };

    Vector4[] cube2DViewPorts = new Vector4[] {
            new Vector4(), new Vector4(), new Vector4(),
            new Vector4(), new Vector4(), new Vector4()
    };

    public GLShadowMap(GLRenderer renderer, GLObjects objects, int maxTextureSize) {
        this.renderer = renderer;
        this.objects = objects;
        maxShadowMapSize = new Vector2(maxTextureSize, maxTextureSize);
        depthMaterials = new MeshDepthMaterial[numberOfMaterialVariants];
        distanceMaterials = new MeshDistanceMaterial[numberOfMaterialVariants];

        // init
        for (int i = 0; i < numberOfMaterialVariants; i++) {
            boolean useMorphing = (i & morphingFlag) != 0;
            boolean useSkinning = (i & skinningFlag) != 0;
            MeshDepthMaterial depthMaterial = new MeshDepthMaterial();
            depthMaterial.depthPacking = Constants.RGBADepthPacking;
            depthMaterial.morphTargets = useMorphing;
            depthMaterial.skinning = useSkinning;
            depthMaterials[ i ] = depthMaterial;

            MeshDistanceMaterial distanceMaterial = new MeshDistanceMaterial();
            distanceMaterial.morphTargets = useMorphing;
            distanceMaterial.skinning = useSkinning;
            distanceMaterials[ i ] = distanceMaterial;
        }
    }

    public void render(ArrayList<Light> lights, Scene scene, Camera camera) {
        if (!enabled) return;
        if (!autoUpdate && !needsUpdate) return;

        if (lights.size() == 0) return;

        GLRenderTarget currentRenderTarget = renderer.getRenderTarget();
        int activeCubeFace = renderer.getActiveCubeFace();
        int activeMipMapLevel = renderer.getActiveMipMapLevel();

        GLState state = renderer.state;
        // Set GL state for depth map.
        state.setBlending(Constants.NoBlending);
        state.colorBuffer.setClear( 1, 1, 1, 1, false);
        state.depthBuffer.setTest( true );
        state.setScissorTest( false );

        // render depth map
        int faceCount;
        for (int i = 0; i < lights.size(); i++) {
            Light light = lights.get(i);
            LightShadow shadow = light.getShadow();
            boolean isPointLight = light instanceof PointLight;

            if (shadow == null) {
                Log.w(getClass().getSimpleName(), "THREE.GLShadowMap: " + light + " has no shadow");
                continue;
            }
            Camera shadowCamera = shadow.camera;
            shadowMapSize.copy( shadow.mapSize );
            shadowMapSize.min( maxShadowMapSize );
            if (isPointLight) {
                double vpWidth = shadowMapSize.x;
                double vpHeight = shadowMapSize.y;

                // These viewports map a cube-map onto a 2D texture with the
                // following orientation:
                //
                //  xzXZ
                //   y Y
                //
                // X - Positive x direction
                // x - Negative x direction
                // Y - Positive y direction
                // y - Negative y direction
                // Z - Positive z direction
                // z - Negative z direction

                // positive X
                cube2DViewPorts[ 0 ].set( vpWidth * 2, vpHeight, vpWidth, vpHeight );
                // negative X
                cube2DViewPorts[ 1 ].set( 0, vpHeight, vpWidth, vpHeight );
                // positive Z
                cube2DViewPorts[ 2 ].set( vpWidth * 3, vpHeight, vpWidth, vpHeight );
                // negative Z
                cube2DViewPorts[ 3 ].set( vpWidth, vpHeight, vpWidth, vpHeight );
                // positive Y
                cube2DViewPorts[ 4 ].set( vpWidth * 3, 0, vpWidth, vpHeight );
                // negative Y
                cube2DViewPorts[ 5 ].set( vpWidth, 0, vpWidth, vpHeight );

                shadowMapSize.x *= 4.0;
                shadowMapSize.y *= 2.0;
            }
            if (shadow.map == null) {
                Texture.Option pars = new Texture.Option();
                pars.minFilter = Constants.NearestFilter;
                pars.magFilter = Constants.NearestFilter;
                pars.format = Constants.RGBAFormat;

                shadow.map = new GLRenderTarget((int) shadowMapSize.x, (int) shadowMapSize.y, pars);
                shadow.map.texture.name = light.name + ".shadowMap";
                shadowCamera.updateProjectionMatrix();
            }

            if (shadow instanceof SpotLightShadow) {
                ((SpotLightShadow) shadow).update((SpotLight) light);
            }
            GLRenderTarget shadowMap = shadow.map;
            Matrix4 shadowMatrix = shadow.matrix;

            lightPositionWorld.setFromMatrixPosition( light.getWorldMatrix() );
            shadowCamera.position.copy(lightPositionWorld);

            if (isPointLight) {
                faceCount = 6;
                // for point lights we set the shadow matrix to be a translation-only matrix
                // equal to inverse of the light's position

                shadowMatrix.makeTranslation( - lightPositionWorld.x, - lightPositionWorld.y, - lightPositionWorld.z );
            } else {
                faceCount = 1;
                Object3D lightTarget = null;
                if (light instanceof SpotLight) {
                    lightTarget = ((SpotLight) light).target;
                } else if (light instanceof DirectionalLight) {
                    lightTarget = ((DirectionalLight) light).target;
                }
                if (lightTarget != null) {
                    lookTarget.setFromMatrixPosition( lightTarget.getWorldMatrix() );
                    shadowCamera.lookAt( lookTarget );
                    shadowCamera.updateMatrixWorld(false);
                }
                // compute shadow matrix

                shadowMatrix.set(
                        0.5f, 0.0f, 0.0f, 0.5f,
                        0.0f, 0.5f, 0.0f, 0.5f,
                        0.0f, 0.0f, 0.5f, 0.5f,
                        0.0f, 0.0f, 0.0f, 1.0f
                );

                shadowMatrix.multiply( shadowCamera.projectionMatrix );
                shadowMatrix.multiply( shadowCamera.matrixWorldInverse );
            }

            renderer.setRenderTarget( shadowMap );
            renderer.clear();
            // render shadow map for each cube face (if omni-directional) or
            // run a single pass if not
            for (int face = 0; face < faceCount; face++) {
                if (isPointLight) {
                    lookTarget.copy(shadowCamera.position);
                    lookTarget.add(cubeDirections[face]);
                    shadowCamera.up.copy( cubeUps[ face ] );
                    shadowCamera.lookAt( lookTarget );
                    shadowCamera.updateMatrixWorld(false);

                    Vector4 vpDimensions = cube2DViewPorts[ face ];
                    state.viewport( vpDimensions );
                }
                // update camera matrices and frustum
                projScreenMatrix.multiplyMatrices(shadowCamera.projectionMatrix, shadowCamera.matrixWorldInverse);
                frustum.setFromMatrix(projScreenMatrix);
                // set object matrices & frustum culling
                renderObject( scene, camera, shadowCamera, isPointLight );
            }
        }
        needsUpdate = false;
        renderer.setRenderTarget( currentRenderTarget, activeCubeFace, activeMipMapLevel );
    }

    private void renderObject(Object3D object, Camera camera, Camera shadowCamera, boolean isPointLight) {
        if (!object.visible) return;

        boolean visible = object.layers.test(camera.layers);
        if (visible && (object instanceof Mesh || object instanceof Line || object instanceof Points)) {
            if (object.castShadow && (!object.frustumCulled || frustum.intersectsObject(object))) {
                object.modelViewMatrix.multiplyMatrices(shadowCamera.matrixWorldInverse, object.getModelMatrix());

                BufferGeometry geometry = objects.update(object);
                ArrayList<Material> material = object.material;
                if (material.size() > 1) {//is array
                    ArrayList<GroupItem> groups = geometry.getGroups();
                    for (int k = 0; k < groups.size(); k++) {
                        GroupItem group = groups.get(k);
                        Material groupMaterial = object.material.get(group.materialIndex);

                        if (groupMaterial != null && groupMaterial.vertexTangents) {
                            Material depthMaterial = getDepthMaterial(object, groupMaterial, isPointLight, lightPositionWorld,
                                    shadowCamera.near, shadowCamera.far );
                            renderer.renderBufferDirect( shadowCamera, null, geometry, depthMaterial, object, group );
                        }
                    }
                } else if (material.size() == 1 && material.get(0).visible) {
                    Material depthMaterial = getDepthMaterial( object, material.get(0), isPointLight, lightPositionWorld,
                            shadowCamera.near, shadowCamera.far );
                    renderer.renderBufferDirect( shadowCamera, null, geometry, depthMaterial, object, null );
                }
            }
        }
        ArrayList<Object3D> children = object.children;
        for (int i = 0; i < children.size(); i++) {
            renderObject(children.get(i), camera, shadowCamera, isPointLight);
        }
    }

    private Material getDepthMaterial(Object3D object, Material material, boolean isPointLight,
                                               Vector3 lightPositionWorld, double shadowCameraNear, double shadowCameraFar) {
        BufferGeometry geometry = object.geometry;
        Material ret = null;
        Material[] materialVariants = depthMaterials;
        Material customMaterial = object.customDepthMaterial;

        if ( isPointLight ) {
            materialVariants = distanceMaterials;
            customMaterial = object.customDistanceMaterial;
        }

        if ( customMaterial == null) {

            boolean useMorphing = false;

            if ( material.morphTargets ) {
                ArrayList<BufferAttribute> morphPostions = geometry.morphAttributeMap.get("position");
                useMorphing = morphPostions != null && morphPostions.size() > 0;
            }

            boolean useSkinning = (object instanceof SkinnedMesh) && material.skinning;

            int variantIndex = 0;

            if ( useMorphing ) variantIndex |= morphingFlag;
            if ( useSkinning ) variantIndex |= skinningFlag;

            ret = materialVariants[ variantIndex ];
        } else {
            ret = customMaterial;
        }

        if (renderer.localClippingEnabled && material.clipShadows && material.clippingPlanes.length == 0) {
            // in this case we need a unique material instance reflecting the
            // appropriate state
            String keyA = ret.uuid, keyB = material.uuid;
            HashMap<String, Material> materialsForVariant = materialCache.get(keyA);
            if (materialsForVariant == null) {
                materialsForVariant = new HashMap<>();
                materialCache.put(keyA, materialsForVariant);
            }
            Material cachedMaterial = materialsForVariant.get(keyB);

            if (cachedMaterial == null) {
                cachedMaterial = ret.clone();
                materialsForVariant.put(keyB, cachedMaterial);
            }
            ret = cachedMaterial;
        }
        ret.visible = material.visible;
        ret.wireframe = material.wireframe;

        ret.side = ( material.shadowSide != null ) ? material.shadowSide : shadowSide[ material.side ];

        ret.clipShadows = material.clipShadows;
        ret.clippingPlanes = material.clippingPlanes;
        ret.clipIntersection = material.clipIntersection;

        ret.wireframeLinewidth = material.wireframeLinewidth;
        ret.linewidth = material.linewidth;

        if ( isPointLight && (ret instanceof  MeshDistanceMaterial) ) {
            MeshDistanceMaterial distanceMaterial = (MeshDistanceMaterial) ret;
            distanceMaterial.referencePosition.copy( lightPositionWorld );
            distanceMaterial.nearDistance = shadowCameraNear;
            distanceMaterial.farDistance = shadowCameraFar;
        }

        return ret;
    }
}
