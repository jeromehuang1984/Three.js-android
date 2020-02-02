package edu.three.renderers.gl;

import java.util.ArrayList;
import java.util.Arrays;

import edu.three.buffergeometries.BoxBufferGeometry;
import edu.three.buffergeometries.PlaneBufferGeometry;
import edu.three.cameras.Camera;
import edu.three.constant.Constants;
import edu.three.core.ICallback;
import edu.three.geometries.param.BoxParam;
import edu.three.geometries.param.PlaneParam;
import edu.three.materials.Material;
import edu.three.materials.ShaderMaterial;
import edu.three.math.Color;
import edu.three.objects.Mesh;
import edu.three.renderers.GLRenderTargetCube;
import edu.three.renderers.GLRenderer;
import edu.three.renderers.shaders.ShaderLib;
import edu.three.scenes.Scene;
import edu.three.textures.CubeTexture;
import edu.three.textures.Texture;

public class GLBackground {
    Color clearColor = new Color(0x000000);
    float clearAlpha = 0;
    Mesh planeMesh = null;
    Mesh boxMesh = null;

    // Store the current background texture and its `version`
    // so we can recompile the material accordingly.
    Object currentBackground = null; // THREE.Color Texture
    int currentBackgroundVersion = 0;

    GLRenderer renderer;
    GLState state;
    GLObjects objects;
    boolean premultipliedAlpha;

    public GLBackground(GLRenderer renderer, GLState state, GLObjects objects, boolean premultipliedAlpha) {
        this.renderer = renderer;
        this.state = state;
        this.objects = objects;
        this.premultipliedAlpha = premultipliedAlpha;
    }


    public void render(GLRenderLists.List renderList, Scene scene, Camera camera, boolean forceClear) {
        Object background = scene.background;
        if (background == null) {
            setClear(clearColor, clearAlpha);
            currentBackground = null;
            currentBackgroundVersion = 0;
        }
        if (background instanceof Color) {
            forceClear = true;
            currentBackground = null;
            currentBackgroundVersion = 0;
        }
        if (renderer.autoClear || forceClear) {
            renderer.clear(renderer.autoClearColor, renderer.autoClearDepth, renderer.autoClearStencil);
        }
        if (background != null && (background instanceof CubeTexture || background instanceof GLRenderTargetCube) ) {
            if (boxMesh == null) {
                ShaderMaterial shaderMaterial = createShaderMaterial(ShaderLib.cube(),"BackgroundCubeMaterial", Constants.BackSide);
                boxMesh = new Mesh(
                        new BoxBufferGeometry(new BoxParam(1, 1, 1)),
                        new ArrayList<Material>(Arrays.asList(shaderMaterial))
                );

                boxMesh.geometry.normal = null;
                boxMesh.geometry.uv = null;

                boxMesh.onBeforeRender = new ICallback() {
                    @Override
                    public void call(GLRenderer renderer, Scene scene, Camera camera) {
                        boxMesh.setWorldMatrix(camera.getWorldMatrix());
                    }
                };
                objects.update(boxMesh);
            }
            Texture texture;
            boolean isGLRenderTargetCube = false;
            if (background instanceof  GLRenderTargetCube) {
                texture = ((GLRenderTargetCube) background).texture;
                isGLRenderTargetCube = true;
            } else {
                texture = (Texture) background;
            }
            boxMesh.material.get(0).uniforms.put("tCube", texture);
            boxMesh.material.get(0).uniforms.put("tFlip", isGLRenderTargetCube ? 1 : -1);

            if (currentBackground != background || currentBackgroundVersion != texture.version) {
                boxMesh.material.get(0).needsUpdate = true;
                currentBackground = background;
                currentBackgroundVersion = texture.version;
            }
            // push to the pre-sorted opaque render list
            renderList.unshift(boxMesh, boxMesh.geometry, boxMesh.material.get(0), 0, 0, null);
        } else if (background != null && (background instanceof  Texture) ) {
            Texture texture = (Texture) background;
            if (planeMesh == null) {
                ShaderMaterial shaderMaterial = createShaderMaterial(ShaderLib.background(),"BackgroundMaterial", Constants.FrontSide);
                planeMesh = new Mesh(
                        new PlaneBufferGeometry(new PlaneParam(2, 2)),
                        new ArrayList<Material>(Arrays.asList(shaderMaterial))
                );
                planeMesh.geometry.normal = null;

                objects.update(planeMesh);
            }
            planeMesh.material.get(0).uniforms.put("t2D", texture);
            if (texture.matrixAutoUpdate) {
                texture.updateMatrix();
            }
            planeMesh.material.get(0).uniforms.put("uvTransform", texture.getMatrix().clone());

            if (currentBackground != background ||currentBackgroundVersion != texture.version) {
                planeMesh.material.get(0).needsUpdate = true;
                currentBackground = background;
                currentBackgroundVersion = texture.version;
            }

            // push to the pre-sorted opaque render list
            renderList.unshift( planeMesh, planeMesh.geometry, planeMesh.material.get(0), 0, 0, null );
        }
    }

    private ShaderMaterial createShaderMaterial(ShaderLib shader, String type, int side) {
        ShaderMaterial shaderMaterial = new ShaderMaterial();
        shaderMaterial.type = "BackgroundCubeMaterial";
        shaderMaterial.uniforms = shader.uniforms;
        shaderMaterial.vertexShader = shader.vertexShader;
        shaderMaterial.fragmentShader = shader.fragmentShader;
        shaderMaterial.side = side;
        shaderMaterial.depthTest = false;
        shaderMaterial.depthWrite = false;
        shaderMaterial.fog = false;
        return shaderMaterial;
    }

    public void setClear(Color color, float alpha) {
        state.colorBuffer.setClear(color.r, color.g, color.b, alpha, premultipliedAlpha);
    }

    public Color getClearColor() {
        return clearColor;
    }

    public void setClearColor(Color color, float alpha) {
        clearColor = color;
        clearAlpha = alpha >= 0 ? alpha : 1;
        setClear(clearColor, alpha);
    }

    public float getClearAlpha() {
        return clearAlpha;
    }

    public void setClearAlpha(float alpha) {
        clearAlpha = alpha;
        setClear(clearColor, clearAlpha);
    }
}
