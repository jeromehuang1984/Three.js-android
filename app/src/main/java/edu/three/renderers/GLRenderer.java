package edu.three.renderers;

import android.opengl.GLES30;
import android.util.Log;

import java.nio.Buffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import edu.three.cameras.ArrayCamera;
import edu.three.cameras.Camera;
import edu.three.cameras.PerspectiveCamera;
import edu.three.core.BufferAttribute;
import edu.three.core.BufferGeometry;
import edu.three.core.Event;
import edu.three.core.IListener;
import edu.three.core.InstancedBufferAttribute;
import edu.three.core.InstancedBufferGeometry;
import edu.three.core.InstancedInterleavedBuffer;
import edu.three.core.InterleavedBuffer;
import edu.three.core.InterleavedBufferAttribute;
import edu.three.core.Object3D;
import edu.three.lights.Light;
import edu.three.materials.LineBasicMaterial;
import edu.three.materials.LineDashedMaterial;
import edu.three.materials.Material;
import edu.three.materials.MeshBasicMaterial;
import edu.three.materials.MeshDepthMaterial;
import edu.three.materials.MeshDistanceMaterial;
import edu.three.materials.MeshLambertMaterial;
import edu.three.materials.MeshMatcapMaterial;
import edu.three.materials.MeshNormalMaterial;
import edu.three.materials.MeshPhongMaterial;
import edu.three.materials.MeshPhysicalMaterial;
import edu.three.materials.MeshStandardMaterial;
import edu.three.materials.MeshToonMaterial;
import edu.three.materials.PointsMaterial;
import edu.three.materials.RawShaderMaterial;
import edu.three.materials.ShaderMaterial;
import edu.three.materials.ShadowMaterial;
import edu.three.materials.SpriteMaterial;
import edu.three.math.Color;
import edu.three.math.Frustum;
import edu.three.math.Matrix4;
import edu.three.math.Plane;
import edu.three.math.MathTool;
import edu.three.math.Vector2;
import edu.three.math.Vector3;
import edu.three.math.Vector4;
import edu.three.objects.Bone;
import edu.three.objects.Group;
import edu.three.objects.GroupItem;
import edu.three.objects.ImmediateRenderObject;
import edu.three.objects.LOD;
import edu.three.objects.Line;
import edu.three.objects.LineLoop;
import edu.three.objects.LineSegments;
import edu.three.objects.Mesh;
import edu.three.objects.Points;
import edu.three.objects.Skeleton;
import edu.three.objects.SkinnedMesh;
import edu.three.objects.Sprite;
import edu.three.renderers.gl.GLAttributes;
import edu.three.renderers.gl.GLBackground;
import edu.three.renderers.gl.GLBufferRenderer;
import edu.three.renderers.gl.GLCapabilities;
import edu.three.renderers.gl.GLClipping;
import edu.three.renderers.gl.GLGeometries;
import edu.three.renderers.gl.GLIndexedBufferRenderer;
import edu.three.renderers.gl.GLInfo;
import edu.three.renderers.gl.GLLights;
import edu.three.renderers.gl.GLMorphtargets;
import edu.three.renderers.gl.GLObjects;
import edu.three.renderers.gl.GLProgram;
import edu.three.renderers.gl.GLPrograms;
import edu.three.renderers.gl.GLProperties;
import edu.three.renderers.gl.GLRenderLists;
import edu.three.renderers.gl.GLRenderStates;
import edu.three.renderers.gl.GLShadowMap;
import edu.three.renderers.gl.GLState;
import edu.three.renderers.gl.GLTextures;
import edu.three.renderers.gl.GLUniforms;
import edu.three.renderers.gl.GLUtils;
import edu.three.renderers.gl.uniform.Uniform;
import edu.three.renderers.shaders.ShaderLib;
import edu.three.renderers.shaders.UniformsLib;
import edu.three.scenes.Fog;
import edu.three.scenes.FogExp2;
import edu.three.scenes.Scene;
import edu.three.textures.CubeTexture;
import edu.three.textures.DataTexture;
import edu.three.textures.Texture;

import static edu.three.constant.Constants.BackSide;
import static edu.three.constant.Constants.FloatType;
import static edu.three.constant.Constants.HalfFloatType;
import static edu.three.constant.Constants.LinearToneMapping;
import static edu.three.constant.Constants.RGBAFormat;
import static edu.three.constant.Constants.TriangleFanDrawMode;
import static edu.three.constant.Constants.TriangleStripDrawMode;
import static edu.three.constant.Constants.TrianglesDrawMode;
import static edu.three.constant.Constants.UnsignedByteType;

public class GLRenderer {
    public String TAG = getClass().getSimpleName();
    public boolean contextLoss = false;
    public float gammaFactor;
    public boolean gammaOutput = false;
    public boolean gammaInput = false;
    public boolean checkShaderErrors = true;
    public GLShadowMap shadowMap;
    public int toneMapping = LinearToneMapping;
    public float toneMappingExposure = 1;
    public float toneMappingWhitePoint = 1;
    // physical lights
    public boolean physicallyCorrectLights = false;

    // clearing
    public boolean autoClear = true;
    public boolean autoClearColor = true;
    public boolean autoClearDepth = true;
    public boolean autoClearStencil = true;
    public boolean sortObjects = true;

    public GLState state;

    // user-defined clipping
    public Plane[] clippingPlanes = new Plane[0];
    public boolean localClippingEnabled;

    // morphs
    public int maxMorphTargets = 8;
    public int maxMorphNormals = 4;

    // internal properties
    int _framebuffer = 0;
    int _currentActiveCubeFace = 0;
    int _currentActiveMipmapLevel = 0;
    GLRenderTarget _currentRenderTarget;
    int _currentFramebuffer;
    long _currentMaterialId = -1;
    // geometry and program caching
    GeometryProgram _currentGeometryProgram = new GeometryProgram();
    Camera _currentCamera = null;
    ArrayCamera _currentArrayCamera = null;
    Vector4 _currentViewport = new Vector4();
    Vector4 _currentScissor = new Vector4();
    Boolean _currentScissorTest = null;
    int _width, _height; //_width = _canvas.width
    float _pixelRatio = 1;
    boolean _scissorTest = false;
    Vector4 _viewport, _scissor;
    Frustum _frustum = new Frustum();
    // clipping
    GLClipping _clipping = new GLClipping();
    boolean _clippingEnabled = false;
    boolean _localClippingEnabled = false;
    // camera matrices cache
    Matrix4 _projScreenMatrix = new Matrix4();
    Vector3 _vector3 = new Vector3();
    Color _color = new Color();

    GLCapabilities capabilities;
    GLInfo info;
    GLProperties properties;
    GLTextures textures;
    GLAttributes attributes;
    GLGeometries geometries;
    GLObjects objects;
    GLPrograms programCache;
    GLRenderLists renderLists = new GLRenderLists();
    GLRenderLists.List currentRenderList = null;
    GLRenderStates renderStates = new GLRenderStates();
    GLRenderStates.State currentRenderState = null;
    GLBackground background;
    GLMorphtargets morphtargets;
    GLBufferRenderer bufferRenderer;
    GLIndexedBufferRenderer indexedBufferRenderer;
    Param param;
    // shadow map

    public GLRenderer(Param parameters, int width, int height) {
        param = parameters;
        _viewport = new Vector4();
        _width = width;
        _height = height;
        _scissor = new Vector4( 0, 0, _width, _height );
        initGLContext();
        setViewport( 0, 0, width, height );
    }

    public void onContextLost() {
        contextLoss = true;
    }

    private void initGLContext() {
        capabilities = new GLCapabilities(param);
        state = new GLState();
        state.scissor( _currentScissor.copy( _scissor ).multiplyScalar( _pixelRatio ).floor() );
        state.viewport( _currentViewport.copy( _viewport ).multiplyScalar( _pixelRatio ).floor() );

        info = new GLInfo();
        properties = new GLProperties();
        textures = new GLTextures(state, properties, capabilities, info);
        attributes = new GLAttributes();
        geometries = new GLGeometries(attributes, info);
        objects = new GLObjects(geometries, info);
        morphtargets = new GLMorphtargets();
        programCache = new GLPrograms(this, capabilities);
        background = new GLBackground(this, state, objects, param.premultipliedAlpha);
        bufferRenderer = new GLBufferRenderer(info);
        indexedBufferRenderer = new GLIndexedBufferRenderer(info);
        info.programs = programCache.programs;

        shadowMap = new GLShadowMap(this, objects, capabilities.maxTextureSize);
    }

    public Vector2 getDrawingBufferSize(Vector2 target) {
        return target.set(_width *_pixelRatio, _height *_pixelRatio).floor();
    }

    public void setDrawingBufferSize(int width, int height, float pixelRatio) {
        _width = width;
        _height = height;

        _pixelRatio = pixelRatio;

        this.setViewport( 0, 0, width, height );
    }

    public boolean getScissorTest() {
        return _scissorTest;
    }

    public void setScissorTest(boolean value) {
        _scissorTest = value;
        state.setScissorTest(_scissorTest);
    }

    public Vector4 getScissor(Vector4 target) {
        return target.copy(_scissor);
    }

    public void setScissor(Vector4 x) {
        _scissor.copy(x);
        state.scissor( _currentScissor.copy( _scissor ).multiplyScalar( _pixelRatio ).floor() );
    }

    public void setScissor(float x, float y, float width, float height) {
        _scissor.set( x, _height - y - height, width, height );
        state.scissor( _currentScissor.copy( _scissor ).multiplyScalar( _pixelRatio ).floor() );
    }

    public Vector4 getCurrentViewport(Vector4 target) {
        return target.copy(_currentViewport);
    }

    public Vector4 getViewport(Vector4 target) {
        return target.copy(_viewport);
    }

    public void setViewport(Vector4 x) {
        _viewport.set( x.x, x.y, x.z, x.w );
        state.viewport(_currentViewport.copy(_viewport).multiplyScalar(_pixelRatio).floor() );
    }

    public void setViewport(float x, float y, float width, float height) {
        _viewport.set(x, _height - y - height, width, height);
        state.viewport(_currentViewport.copy(_viewport).multiplyScalar(_pixelRatio).floor() );
    }

    public Color getClearColor() {
        return background.getClearColor();
    }

    public void setClearColor(int color, float alpha) {
        background.setClearColor(new Color(color), alpha);
    }

    public float getClearAlpha() {
        return background.getClearAlpha();
    }

    public void setClearAlpha(float alpha) {
        background.setClearAlpha(alpha);
    }

    public float getPixelRatio() {
        return _pixelRatio;
    }

    public void setPixelRatio(float value) {
        _pixelRatio = value;
        setSize(_width, _height);
    }

    public Vector2 getSize(Vector2 target) {
        return target.set(_width, _height);
    }

    public void setSize(float width, float height) {
        _width = (int) width;
        _height = (int) height;

        this.setViewport( 0, 0, width, height );
    }

    public void clear() {
        clear(true, true, true);
    }
    public void clear(boolean color, boolean depth, boolean stencil) {
        int bits = 0;
        if (color) bits |= GLES30.GL_COLOR_BUFFER_BIT;
        if (depth) bits |= GLES30.GL_DEPTH_BUFFER_BIT;
        if (stencil) bits |= GLES30.GL_STENCIL_BUFFER_BIT;
        GLES30.glClear(bits);
    }

    public void clearColor() {
        clear(true, false, false);
    }

    public void clearDepth() {
        clear(false, true, false);
    }

    public void clearStencil() {
        clear(false, false, true);
    }

    public void dispose() {
        renderLists.dispose();
        renderStates.dispose();
        properties.dispose();
        objects.dispose();
    }

    IListener onMaterialDispose = new IListener() {
        @Override
        public void onEvent(Event event) {
            Material material = (Material) event.target;
            material.removeEventListener("dispose", onMaterialDispose);
            deallocateMaterial(material);
        }
    };

    // Buffer deallocation
    public void deallocateMaterial(Material material) {
        releaseMaterialProgramReference(material);
        properties.remove(material);
    }

    public void releaseMaterialProgramReference(Material material) {
        GLProgram programInfo = properties.get(material).program;
        material.program = null;
        if (programInfo != null) {
            programCache.releaseProgram(programInfo);
        }
    }

    public void setRenderTarget(GLRenderTarget renderTarget) {
        setRenderTarget(renderTarget, 0, 0);
    }

    public void setRenderTarget(GLRenderTarget renderTarget, int activeCubeFace, int activeMipMapLevel) {
        _currentRenderTarget = renderTarget;
        _currentActiveCubeFace = activeCubeFace;
        _currentActiveMipmapLevel = activeMipMapLevel;

        if ( renderTarget != null && properties.get( renderTarget ).glFramebuffer == null ) {
            textures.setupRenderTarget( renderTarget );
        }
        int framebuffer = _framebuffer;
        boolean isCube = false;

        if ( renderTarget != null ) {

            int[] glFramebuffer = properties.get( renderTarget ).glFramebuffer;

            if ( renderTarget instanceof GLRenderTargetCube ) {
                framebuffer = glFramebuffer[ activeCubeFace > 0 ? activeCubeFace : 0 ];
                isCube = true;
            } else if ( renderTarget instanceof GLMultisampleRenderTarget ) {
                framebuffer = properties.get( renderTarget ).glMultisampledFramebuffer[0];
            } else {
                framebuffer = glFramebuffer[0];
            }

            _currentViewport.copy( renderTarget.viewport );
            _currentScissor.copy( renderTarget.scissor );
            _currentScissorTest = renderTarget.scissorTest;

        } else {
            _currentViewport.copy( _viewport ).multiplyScalar( _pixelRatio ).floor();
            _currentScissor.copy( _scissor ).multiplyScalar( _pixelRatio ).floor();
            _currentScissorTest = _scissorTest;
        }

        if ( _currentFramebuffer != framebuffer ) {
            GLES30.glBindFramebuffer( GLES30.GL_FRAMEBUFFER, framebuffer );
            _currentFramebuffer = framebuffer;
        }

        state.viewport( _currentViewport );
        state.scissor( _currentScissor );
        state.setScissorTest( _currentScissorTest );

        if ( isCube ) {
            GLProperties.Fields textureProperties = properties.get( renderTarget.texture );
            GLES30.glFramebufferTexture2D( GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0, GLES30.GL_TEXTURE_CUBE_MAP_POSITIVE_X +
                    ( activeCubeFace > 0 ? activeCubeFace : 0 ), textureProperties.glTexture,
                    activeMipMapLevel > 0 ? activeMipMapLevel : 0 );

        }
    }

    public void readRenderTargetPixels(GLRenderTarget renderTarget, int x, int y, int width, int height,
                                       Buffer buffer, Integer activeCubeFaceIndex) {
        int[] framebufferArr = properties.get( renderTarget ).glFramebuffer;
        int framebuffer = framebufferArr[0];
        if ( renderTarget instanceof GLRenderTargetCube && activeCubeFaceIndex != null ) {
            framebuffer = framebufferArr[ activeCubeFaceIndex ];
        }

        if ( framebuffer > 0 ) {

            boolean restore = false;

            if ( framebuffer != _currentFramebuffer ) {
                GLES30.glBindFramebuffer( GLES30.GL_FRAMEBUFFER, framebuffer );
                restore = true;
            }

            try {

                Texture texture = renderTarget.texture;
                int textureFormat = texture.format;
                int textureType = texture.type;
                int[] temp = new int[1];
                GLES30.glGetIntegerv( GLES30.GL_IMPLEMENTATION_COLOR_READ_FORMAT, temp, 0);
                if ( textureFormat != RGBAFormat && GLUtils.convert( textureFormat ) != temp[0] ) {
                    Log.e(TAG, "THREE.WebGLRenderer.readRenderTargetPixels: renderTarget is not in RGBA or implementation defined format.");
                    return;

                }
                GLES30.glGetIntegerv( GLES30.GL_IMPLEMENTATION_COLOR_READ_TYPE, temp, 0);
                if ( textureType != UnsignedByteType && GLUtils.convert( textureType ) != temp[0] &&
                        ! (textureType == FloatType)  && // Chrome Mac >= 52 and Firefox
                        ! ( textureType == HalfFloatType) ) {
                    Log.e(TAG, "GLRenderer.readRenderTargetPixels: renderTarget is not in UnsignedByteType or implementation defined type.");
                    return;
                }

                if ( GLES30.glCheckFramebufferStatus( GLES30.GL_FRAMEBUFFER ) == GLES30.GL_FRAMEBUFFER_COMPLETE ) {

                    // the following if statement ensures valid read requests (no out-of-bounds pixels, see #8604)

                    if ( ( x >= 0 && x <= ( renderTarget.width - width ) ) && ( y >= 0 && y <= ( renderTarget.height - height ) ) ) {

                        GLES30.glReadPixels( x, y, width, height, GLUtils.convert( textureFormat ), GLUtils.convert( textureType ), buffer );

                    }

                } else {
                    Log.e(TAG, "GLRenderer.readRenderTargetPixels: readPixels from renderTarget failed. Framebuffer not complete.");
                }

            } finally {
                if ( restore ) {
                    GLES30.glBindFramebuffer( GLES30.GL_FRAMEBUFFER, _currentFramebuffer );
                }

            }

        }
    }

    public GLRenderTarget getRenderTarget() {
        return _currentRenderTarget;
    }

    public int getActiveCubeFace() {
        return _currentActiveCubeFace;
    }

    public int getActiveMipMapLevel() {
        return _currentActiveMipmapLevel;
    }

    public float getTargetPixelRatio() {
        return _currentRenderTarget == null ? _pixelRatio : 1;
    }

    // Buffer rendering
    private void renderObjectImmediate(ImmediateRenderObject object, final GLProgram program) {
        object.render(new ImmediateRenderObject.RenderCallback() {
            @Override
            public void renderCallback(ImmediateRenderObject object) {
                renderBufferImmediate(object, program);
            }
        });
    }

    private void renderBufferImmediate(ImmediateRenderObject object, GLProgram program) {
        state.initAttributes();
        GLProperties.Fields buffers = properties.get(object);
        if (object.hasPositions) {
            GLES30.glGenBuffers(1, buffers.position, 0);
        }
        if (object.hasNormals) {
            GLES30.glGenBuffers(1, buffers.normal, 0);
        }
        if (object.hasUvs) {
            GLES30.glGenBuffers(1, buffers.uv, 0);
        }
        if (object.hasColors) {
            GLES30.glGenBuffers(1, buffers.color, 0);
        }
        HashMap<String, Integer> programAttributes = program.getAttributes();
        if ( object.hasPositions ) {

            GLES30.glBindBuffer( GLES30.GL_ARRAY_BUFFER, buffers.position[0] );
            GLES30.glBufferData( GLES30.GL_ARRAY_BUFFER, object.positionArraySize, object.positionArray, GLES30.GL_DYNAMIC_DRAW );

            state.enableAttribute( programAttributes.get("position") );
            GLES30.glVertexAttribPointer( programAttributes.get("position"), 3, GLES30.GL_FLOAT, false, 0, 0 );

        }

        if ( object.hasNormals ) {

            GLES30.glBindBuffer( GLES30.GL_ARRAY_BUFFER, buffers.normal[0] );
            GLES30.glBufferData( GLES30.GL_ARRAY_BUFFER, object.normalArraySize, object.normalArray, GLES30.GL_DYNAMIC_DRAW );

            state.enableAttribute( programAttributes.get("normal") );
            GLES30.glVertexAttribPointer( programAttributes.get("normal"), 3, GLES30.GL_FLOAT, false, 0, 0 );

        }

        if ( object.hasUvs ) {

            GLES30.glBindBuffer( GLES30.GL_ARRAY_BUFFER, buffers.uv[0] );
            GLES30.glBufferData( GLES30.GL_ARRAY_BUFFER, object.uvArraySize, object.uvArray, GLES30.GL_DYNAMIC_DRAW );

            state.enableAttribute( programAttributes.get("uv") );
            GLES30.glVertexAttribPointer( programAttributes.get("uv"), 2, GLES30.GL_FLOAT, false, 0, 0 );

        }

        if ( object.hasColors ) {

            GLES30.glBindBuffer( GLES30.GL_ARRAY_BUFFER, buffers.color[0] );
            GLES30.glBufferData( GLES30.GL_ARRAY_BUFFER, object.colorArraySize, object.colorArray, GLES30.GL_DYNAMIC_DRAW );

            state.enableAttribute( programAttributes.get("color") );
            GLES30.glVertexAttribPointer( programAttributes.get("color"), 3, GLES30.GL_FLOAT, false, 0, 0 );

        }

        state.disableUnusedAttributes();

        GLES30.glDrawArrays( GLES30.GL_TRIANGLES, 0, object.count );

        object.count = 0;
    }

    public void renderBufferDirect(Camera camera, Fog fog, BufferGeometry geometry, Material material,
                                   Object3D object, GroupItem group) {
        boolean frontFaceCW = (object instanceof Mesh) && object.getWorldMatrix().determinant() < 0;
        state.setMaterial(material, frontFaceCW);
        GLProgram program = setProgram(camera, fog, material, object);
        boolean updateBuffers = false;

        if ( _currentGeometryProgram.geometry != geometry.id ||
                _currentGeometryProgram.program != program.id ||
                _currentGeometryProgram.wireframe != material.wireframe ) {

            _currentGeometryProgram.geometry = geometry.id;
            _currentGeometryProgram.program = program.id;
            _currentGeometryProgram.wireframe = material.wireframe;
            updateBuffers = true;
        }

        if (object.morphTargetInfluences.length > 0) {
            morphtargets.update(object, geometry, material, program);
            updateBuffers = true;
        }

        BufferAttribute index = geometry.getIndex();
        BufferAttribute position = geometry.position;
        int rangeFactor = 1;
        if (material.wireframe) {
            index = geometries.getWireframeAttribute(geometry);
            rangeFactor = 2;
        }

        GLAttributes.BufferItem attribute = null;
        GLBufferRenderer renderer = bufferRenderer;
        if (index != null) {
            attribute = attributes.get(index);
            renderer = indexedBufferRenderer;
            ((GLIndexedBufferRenderer) renderer).setIndex(attribute);
        }

        if (updateBuffers) {
            setupVertexAttributes(material, program, geometry);
            if (index != null) {
                GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, attribute.bufferLoc[0]);
            }
        }

        float dataCount = Float.POSITIVE_INFINITY;
        if (index != null) {
            dataCount = index.getCount();
        } else if (position != null) {
            dataCount = position.getCount();
        }
        float rangeStart = geometry.drawRangeStart * rangeFactor;
        float rangeCount = (float)geometry.drawRangeCount * rangeFactor;
        float groupStart = 0;
        float groupCount = Float.POSITIVE_INFINITY;
        if (group != null) {
            groupStart = group.start;
            groupCount = group.count;
        }
        float drawStart = Math.max(rangeStart, groupStart);
        float drawEnd = Math.min(dataCount, rangeStart + rangeCount);
        drawEnd = Math.min(drawEnd, groupStart + groupCount) - 1;

        float drawCount = Math.max( 0, drawEnd - drawStart + 1 );
        if ( drawCount == 0 ) return;

        if (object instanceof Mesh) {
            if (material.wireframe) {
                state.setLineWidth(material.wireframeLinewidth * getTargetPixelRatio() );
                renderer.setMode(GLES30.GL_LINES);
            } else {
                switch (((Mesh) object).getDrawMode()) {
                    case TrianglesDrawMode:
                        renderer.setMode( GLES30.GL_TRIANGLES );
                        break;

                    case TriangleStripDrawMode:
                        renderer.setMode( GLES30.GL_TRIANGLE_STRIP );
                        break;

                    case TriangleFanDrawMode:
                        renderer.setMode( GLES30.GL_TRIANGLE_FAN );
                        break;
                }
            }
        } else if (object instanceof Line) {
            int lineWidth = material.linewidth;
            if (lineWidth < 0) lineWidth = 1;
            state.setLineWidth(lineWidth * getTargetPixelRatio());
            if (object instanceof LineSegments) {
                renderer.setMode(GLES30.GL_LINES);
            } else if (object instanceof LineLoop) {
                renderer.setMode( GLES30.GL_LINE_LOOP );
            } else {
                renderer.setMode( GLES30.GL_LINE_STRIP );
            }
        } else if (object instanceof Points) {
            renderer.setMode( GLES30.GL_POINTS );
        } else if (object instanceof Sprite) {
            renderer.setMode( GLES30.GL_TRIANGLES );
        }

        if (geometry != null && geometry instanceof InstancedBufferGeometry) {
            if (((InstancedBufferGeometry) geometry).maxInstancedCount > 0) {
                renderer.renderInstances(geometry, (int)drawStart, (int)drawCount);
            }
        } else {
            renderer.render( (int)drawStart, (int)drawCount );
        }
    }

    public void setupVertexAttributes(Material material, GLProgram program, BufferGeometry geometry) {
        state.initAttributes();
        HashMap<String, BufferAttribute> geometryAttributes = geometry.getAttributesTotal();
        HashMap<String, Integer> programAttributes = program.getAttributes();
        HashMap<String, float[]> materialDefaultAttributeValues = material.defaultAttributeValues();

        Iterator iter = programAttributes.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, Integer> entry = (Map.Entry<String, Integer>) iter.next();
            String name = entry.getKey();
            int programAttribute = entry.getValue();
            if (programAttribute >= 0) {
                BufferAttribute geometryAttribute = geometryAttributes.get(name);
                if (geometryAttribute != null) {
                    boolean normalized = geometryAttribute.normalized;
                    int size = geometryAttribute.getItemSize();
                    GLAttributes.BufferItem attribute = attributes.get(geometryAttribute);
                    if (attribute == null) continue;
                    int type = attribute.type;
                    int bytesPerElement = attribute.bytesPerElement;

                    if (geometryAttribute instanceof InterleavedBufferAttribute) {
                        InterleavedBufferAttribute attribute1 = (InterleavedBufferAttribute) geometryAttribute;
                        InterleavedBuffer data = attribute1.data;
                        int stride = data.stride;
                        int offset = attribute1.offset;

                        if (data != null && data instanceof InstancedInterleavedBuffer) {
                            int meshPerAttribute = ((InstancedInterleavedBuffer) data).meshPerAttribute;
                            state.enableAttribute(programAttribute, meshPerAttribute);
                            if (geometry.maxInstancedCount() < 0) {
                                geometry.maxInstancedCount(meshPerAttribute * data.count);
                            }
                        } else {
                            state.enableAttribute(programAttribute);
                        }
                        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, attribute.bufferLoc[0]);
                        GLES30.glVertexAttribPointer( programAttribute, size, type, normalized, stride * bytesPerElement,
                                offset * bytesPerElement );
                    } else {
                        if (geometryAttribute instanceof InstancedBufferAttribute) {
                            int meshPerAttribute = ((InstancedBufferAttribute) geometryAttribute).meshPerAttribute;
                            state.enableAttribute(programAttribute, meshPerAttribute);
                            if (geometry.maxInstancedCount() < 0) {
                                geometry.maxInstancedCount(meshPerAttribute * geometryAttribute.getCount());
                            }
                        } else {
                            state.enableAttribute(programAttribute);
                        }
                        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, attribute.bufferLoc[0]);
                        GLES30.glVertexAttribPointer(programAttribute, size, type, normalized, 0, 0 );
                    }
                } else if (materialDefaultAttributeValues != null) {
                    float[] value = materialDefaultAttributeValues.get(name);
                    if (value != null) {
                        switch (value.length) {
                            case 2:
                                GLES30.glVertexAttrib2fv( programAttribute, value, 0 );
                                break;

                            case 3:
                                GLES30.glVertexAttrib3fv( programAttribute, value, 0 );
                                break;

                            case 4:
                                GLES30.glVertexAttrib4fv( programAttribute, value, 0 );
                                break;

                            default:
                                GLES30.glVertexAttrib1fv( programAttribute, value, 0 );
                        }
                    }
                }
            }
        }

        state.disableUnusedAttributes();
    }

    public void render(Scene scene, Camera camera) {
        render(scene, camera, null, false);
    }

    // Rendering
    public void render(Scene scene, Camera camera, GLRenderTarget renderTarget, boolean forceClear) {
        if (contextLoss) {
            programCache.releasePrograms();
            return;
        }
        // reset caching for this frame
        _currentGeometryProgram.geometry = -1;
        _currentGeometryProgram.program = -1;
        _currentGeometryProgram.wireframe = false;
        _currentMaterialId = - 1;
        _currentCamera = null;
        // update scene graph
        if ( scene.autoUpdate) scene.updateMatrixWorld(false);

        // update camera matrices and frustum

        if ( camera.getParent() == null ) camera.updateMatrixWorld(false);

        currentRenderState = renderStates.get(scene, camera);
        currentRenderState.init();

        scene.onBeforeRender.call(this, scene, camera);

        _projScreenMatrix.multiplyMatrices( camera.projectionMatrix, camera.matrixWorldInverse );
        _frustum.setFromMatrix( _projScreenMatrix );

        _localClippingEnabled = this.localClippingEnabled;
        _clippingEnabled = _clipping.init( this.clippingPlanes, _localClippingEnabled, camera );

        currentRenderList = renderLists.get( scene, camera );
        currentRenderList.init();

        projectObject( scene, camera, 0, sortObjects );

        if ( sortObjects) {
            currentRenderList.sort();
        }
        if ( _clippingEnabled ) _clipping.beginShadows();

        ArrayList<Light> shadowsArray = currentRenderState.shadowsArray;

        shadowMap.render( shadowsArray, scene, camera );

        currentRenderState.setupLights( camera );

        if ( _clippingEnabled ) _clipping.endShadows();

        //

        if ( info.autoReset ) this.info.reset();

        if ( renderTarget != null ) {
            this.setRenderTarget( renderTarget );
        } else {
            setRenderTarget(null);
        }

        background.render( currentRenderList, scene, camera, forceClear );

        // render scene

        ArrayList<GLRenderLists.List.RenderItem> opaqueObjects = currentRenderList.opaque;
        ArrayList<GLRenderLists.List.RenderItem> transparentObjects = currentRenderList.transparent;

        if ( scene.overrideMaterial != null ) {

            Material overrideMaterial = scene.overrideMaterial;

            if ( opaqueObjects.size() > 0 ) renderObjects( opaqueObjects, scene, camera, overrideMaterial );
            if ( transparentObjects.size() > 0 ) renderObjects( transparentObjects, scene, camera, overrideMaterial );

        } else {
            // opaque pass (front-to-back order)
            if ( opaqueObjects.size() > 0 ) {
                renderObjects( opaqueObjects, scene, camera, null );
            }
            // transparent pass (back-to-front order)

            if ( transparentObjects.size() > 0 ) {
                renderObjects( transparentObjects, scene, camera, null );
            }
        }
        scene.onAfterRender.call(this, scene, camera);
        if (_currentRenderTarget != null) {
            // Generate mipmap if we're using any kind of mipmap filtering
            textures.updateRenderTargetMipmap( _currentRenderTarget );

            // resolve multisample renderbuffers to a single-sample texture if necessary
            textures.updateMultisampleRenderTarget( _currentRenderTarget );
        }

        // Ensure depth buffer writing is enabled so it can be cleared on next render
        state.depthBuffer.setTest(true);
        state.depthBuffer.setMask(true);
        state.colorBuffer.setMask(true);
        state.setPolygonOffset(false, null, null);

        currentRenderList = null;
        currentRenderState = null;
    }

    public void projectObject(Object3D object, Camera camera, int groupOrder, boolean sortObjects) {
        if (!object.visible) {
            return;
        }
        boolean visible = object.layers.test(camera.layers);
        if (visible) {
            if (object instanceof Group) {
                groupOrder = object.renderOrder;
            } else if (object instanceof LOD) {
                if (((LOD) object).autoUpdate) {
                    ((LOD) object).update(camera);
                }
            } else if (object instanceof Light) {
                currentRenderState.pushLight((Light) object);
                if (object.castShadow) {
                    currentRenderState.pushShadow((Light) object);
                }
            } else if (object instanceof Sprite) {
                if (!object.frustumCulled || _frustum.intersectsSprite((Sprite) object)) {
                    if (sortObjects) {
                        _vector3.setFromMatrixPosition(object.getWorldMatrix()).applyMatrix4(_projScreenMatrix);
                    }
                    BufferGeometry geometry = objects.update(object);
                    Material material = ((Sprite) object).material.get(0);

                    if (material.visible) {
                        currentRenderList.push(object, geometry, material, groupOrder, _vector3.z, null);
                    }
                }
            } else if (object instanceof ImmediateRenderObject) {
                if (sortObjects) {
                    _vector3.setFromMatrixPosition(object.getWorldMatrix()).applyMatrix4(_projScreenMatrix);
                }
                currentRenderList.push(object, null, ((ImmediateRenderObject) object).material, groupOrder,
                        _vector3.z, null);
            } else if (object instanceof Mesh || object instanceof Line || object instanceof Points) {
                if (object instanceof SkinnedMesh) {
                    ((SkinnedMesh) object).skeleton.update();
                }
                if (! object.frustumCulled || _frustum.intersectsObject(object)) {
                    if (sortObjects) {
                        _vector3.setFromMatrixPosition(object.getWorldMatrix()).applyMatrix4(_projScreenMatrix);
                    }
                    BufferGeometry geometry = objects.update(object);
                    ArrayList<Material> materials = object.material;
                    if (materials.size() > 1) {
                        ArrayList<GroupItem> groups = geometry.getGroups();
                        for (int i = 0; i < groups.size(); i++) {
                            GroupItem group = groups.get(i);
                            Material groupMaterial = materials.get(group.materialIndex);

                            if (groupMaterial != null && groupMaterial.visible) {
                                currentRenderList.push(object, geometry, groupMaterial, groupOrder, _vector3.z, group );
                            }
                        }
                    } else if (materials.get(0).visible) {
                        currentRenderList.push(object, geometry, materials.get(0), groupOrder, _vector3.z, null);
                    }
                }
            }
        }
        for (Object3D child : object.children) {
            projectObject( child, camera, groupOrder, sortObjects );
        }
    }

    public void renderObjects(ArrayList<GLRenderLists.List.RenderItem> renderList, Scene scene,
                              Camera camera, Material overrideMaterial) {
        for (GLRenderLists.List.RenderItem renderItem : renderList) {
            Object3D object = renderItem.object;
            BufferGeometry geometry = renderItem.geometry;
            Material material = overrideMaterial != null ? overrideMaterial : renderItem.material;
            GroupItem group = renderItem.group;

            if (camera instanceof ArrayCamera) {
                _currentArrayCamera = (ArrayCamera) camera;

                ArrayList<PerspectiveCamera> cameras = _currentArrayCamera.cameras;

                for ( int j = 0; j < cameras.size(); j ++ ) {

                    PerspectiveCamera camera2 = cameras.get(j);

                    if ( object.layers.test( camera2.layers ) ) {

                        state.viewport( _currentViewport.copy( camera2.viewport ) );

                        currentRenderState.setupLights( camera2 );

                        renderObject( object, scene, camera2, geometry, material, group );
                    }
                }

            } else {
                _currentArrayCamera = null;
                renderObject( object, scene, camera, geometry, material, group );
            }
        }
    }

    public void renderObject(Object3D object, Scene scene, Camera camera, BufferGeometry geometry, Material material, GroupItem group) {
        object.onBeforeRender.call(this, scene, camera);
        currentRenderState = renderStates.get( scene, _currentArrayCamera == null ? camera : _currentArrayCamera );

        object.modelViewMatrix.multiplyMatrices(camera.matrixWorldInverse, object.getWorldMatrix());
        object.getNormalMatrix().getNormalMatrix(object.modelViewMatrix);

        if (object instanceof ImmediateRenderObject) {
            state.setMaterial(material);
            GLProgram program = setProgram( camera, scene.fog, material, object );
            _currentGeometryProgram.geometry = -1;
            _currentGeometryProgram.program = -1;
            _currentGeometryProgram.wireframe = false;

            renderObjectImmediate((ImmediateRenderObject) object, program );
        } else {
            renderBufferDirect(camera, scene.fog, geometry, material, object, group);
        }
        object.onAfterRender.call(this, scene, camera);
        currentRenderState = renderStates.get( scene, _currentArrayCamera == null ? camera : _currentArrayCamera );
    }

    public void initMaterial(Material material, Fog fog, Object3D object) {
        GLProperties.Fields materialProperties = properties.get( material );

        GLLights lights = currentRenderState.lights;
        ArrayList<Light> shadowsArray = currentRenderState.shadowsArray;

        int lightsStateVersion = lights.state.version;

        GLPrograms.Param parameters = programCache.getParameters(
                material, lights.state, shadowsArray, fog, _clipping.numPlanes, _clipping.numIntersection, object );

        String code = programCache.getProgramCode( material, parameters );

        GLProgram program = materialProperties.program;
        boolean programChange = true;
        if (program == null) {
            // new material
            material.addEventListener( "dispose", onMaterialDispose );
        } else if (!program.code.equals(code)) {
            // changed glsl or parameters
            releaseMaterialProgramReference( material );
        } else if ( materialProperties.lightsStateVersion != lightsStateVersion ) {

            materialProperties.lightsStateVersion = lightsStateVersion;

            programChange = false;

        } else if ( parameters.shaderID != null ) {

            // same glsl and uniform list
            return;

        } else {

            // only rebuild uniform list
            programChange = false;
        }

        if ( programChange ) {

            materialProperties.shaderName = material.getClass().getSimpleName();
            if ( parameters.shaderID != null) {
                ShaderLib shader = ShaderLib.lookup(parameters.shaderID);
                materialProperties.shaderLib = shader;

            } else {
                if (material instanceof ShaderMaterial) {
                    ShaderMaterial shaderMaterial = (ShaderMaterial) material;
                    materialProperties.shaderLib = new ShaderLib(shaderMaterial.uniforms,
                            shaderMaterial.vertexShader, shaderMaterial.fragmentShader );
                } else {
                    Log.e(TAG, "invalid material: " + materialProperties.shaderName);
                }
            }

            material.callback.onBeforeCompile( materialProperties.shaderLib, this );

            // Computing code again as onBeforeCompile may have changed the shaders
            code = programCache.getProgramCode( material, parameters );

            program = programCache.acquireProgram( material, materialProperties.shaderLib, parameters, code );

            materialProperties.program = program;
            material.program = program;
        }

        HashMap<String, Integer> programAttributes = program.getAttributes();

        if ( material.morphTargets ) {

            material.numSupportedMorphTargets = 0;

            for ( int i = 0; i < maxMorphTargets; i ++ ) {

                if (programAttributes.get("morphTarget" + i) >= 0) {
                    material.numSupportedMorphTargets ++;
                }

            }
        }

        if ( material.morphNormals ) {

            material.numSupportedMorphNormals = 0;

            for ( int i = 0; i < maxMorphNormals; i ++ ) {

                if ( programAttributes.get("morphTarget" + i) >= 0 ) {
                    material.numSupportedMorphNormals ++;
                }
            }
        }

        UniformsLib uniforms = materialProperties.shaderLib.uniforms;

        if ( ! (material instanceof ShaderMaterial) &&
                !  (material instanceof RawShaderMaterial) ||
                material.clipping) {

            materialProperties.numClippingPlanes = _clipping.numPlanes;
            materialProperties.numIntersection = _clipping.numIntersection;
            uniforms.put("clippingPlanesValue", _clipping.uniformValue);
            uniforms.put("clippingPlanesNeedUpdate", _clipping.uniformNeedsUpdate);
        }

        materialProperties.fog = fog;

        // store the light setup it was created for

        materialProperties.lightsStateVersion = lightsStateVersion;

        if ( material.lights ) {

            // wire up the material to this renderer's lighting state

            uniforms.put("ambientLightColor", lights.state.ambient);
            uniforms.put("lightProbe", new ArrayList(Arrays.asList(lights.state.probe)) );

            uniforms.put("directionalLights", lights.state.directional );
            uniforms.put("spotLights", lights.state.spot );
            uniforms.put("rectAreaLights", lights.state.rectArea );
            uniforms.put("pointLights", lights.state.point );
            uniforms.put("hemisphereLights", lights.state.hemi );

            uniforms.put("directionalShadowMap", lights.state.directionalShadowMap );
            uniforms.put("directionalShadowMatrix", lights.state.directionalShadowMatrix );
            uniforms.put("spotShadowMap", lights.state.spotShadowMap );
            uniforms.put("spotShadowMatrix", lights.state.spotShadowMatrix );
            uniforms.put("pointShadowMap", lights.state.pointShadowMap );
            uniforms.put("pointShadowMatrix", lights.state.pointShadowMatrix );
            // TODO (abelnation): add area lights shadow info to uniforms
        }

        GLUniforms progUniforms = materialProperties.program.getUniforms();
        ArrayList<Uniform> uniformsList = GLUniforms.seqWithValue( progUniforms.seq, uniforms );

        materialProperties.uniformsList = uniformsList;
    }

    private GLProgram setProgram(Camera camera, Fog fog, Material material, Object3D object) {
        textures.resetTextureUnits();
        GLProperties.Fields materialProperties = properties.get(material);
        GLLights lights = currentRenderState.lights;
        if (_clippingEnabled) {
            if ( _localClippingEnabled || camera != _currentCamera ) {

                boolean useCache = camera == _currentCamera &&
                                material.id == _currentMaterialId;

                // we might want to call this function with some ClippingGroup
                // object instead of the material, once it becomes feasible
                // (#8465, #8379)
                _clipping.setState(
                        material.clippingPlanes, material.clipIntersection, material.clipShadows,
                        camera, materialProperties, useCache );

            }
        }
        if (!material.needsUpdate) {
            if ( materialProperties.program == null ) {
                material.needsUpdate = true;
            } else if ( material.fog && materialProperties.fog != fog ) {
                material.needsUpdate = true;
            } else if ( material.lights && materialProperties.lightsStateVersion != lights.state.version ) {
                material.needsUpdate = true;
            } else if ( materialProperties.numClippingPlanes >= 0 &&
                    ( materialProperties.numClippingPlanes != _clipping.numPlanes ||
                            materialProperties.numIntersection != _clipping.numIntersection ) ) {
                material.needsUpdate = true;
            }
        } else {
            initMaterial(material, fog, object);
            material.needsUpdate = false;
        }

        boolean refreshProgram = false;
        boolean refreshMaterial = false;
        boolean refreshLights = false;
        GLProgram program = materialProperties.program;
        GLUniforms p_uniforms = program.getUniforms();
        UniformsLib m_uniforms = materialProperties.shaderLib.uniforms;

        if ( state.useProgram( program.program ) ) {
            refreshProgram = true;
            refreshMaterial = true;
            refreshLights = true;
        }

        if ( material.id != _currentMaterialId ) {
            _currentMaterialId = material.id;
            refreshMaterial = true;
        }

        if ( refreshProgram || _currentCamera != camera ) {

            p_uniforms.setValue( "projectionMatrix", camera.projectionMatrix );

            if ( capabilities.logarithmicDepthBuffer ) {

                Float logDepthVal = 2.0f / ( (float) Math.log( camera.far + 1.0 ) / MathTool.LN2);
                p_uniforms.setValue( "logDepthBufFC", logDepthVal);

            }

            if ( _currentCamera != camera ) {

                _currentCamera = camera;

                // lighting uniforms depend on the camera so enforce an update
                // now, in case this material supports lights - or later, when
                // the next material that does gets activated:

                refreshMaterial = true;		// set to true on material change
                refreshLights = true;		// remains set until update done
            }

            // load material specific uniforms
            // (shader material also gets them for the sake of genericity)

            if ( material instanceof ShaderMaterial ||
                    material instanceof MeshPhongMaterial ||
                    material instanceof MeshStandardMaterial ||
                    material.envMap != null) {

                Uniform uCamPos = p_uniforms.map.get("cameraPosition");

                if ( uCamPos != null ) {
                    uCamPos.setValue(
                            _vector3.setFromMatrixPosition( camera.getWorldMatrix() ), null );

                }

            }

            if ( material instanceof MeshPhongMaterial ||
                    material instanceof MeshLambertMaterial ||
                    material instanceof MeshBasicMaterial ||
                    material instanceof MeshStandardMaterial ||
                    material instanceof ShaderMaterial ||
                    material.skinning ) {

                p_uniforms.setValue( "viewMatrix", camera.matrixWorldInverse );

            }
        }

        // skinning uniforms must be set even if material didn't change
        // auto-setting of texture unit for bone texture must go before other textures
        // not sure why, but otherwise weird things happen

        if ( material.skinning ) {
            p_uniforms.setOptional( object, "bindMatrix" );
            p_uniforms.setOptional( object, "bindMatrixInverse" );

            Skeleton skeleton = null;
            if (object instanceof SkinnedMesh) {
                skeleton = ((SkinnedMesh) object).skeleton;
            }

            if ( skeleton != null ) {

                ArrayList<Bone> bones = skeleton.bones;

                if ( capabilities.floatVertexTextures ) {

                    if ( skeleton.boneTexture == null ) {

                        // layout (1 matrix = 4 pixels)
                        //      RGBA RGBA RGBA RGBA (=> column1, column2, column3, column4)
                        //  with  8x8  pixel texture max   16 bones * 4 pixels =  (8 * 8)
                        //       16x16 pixel texture max   64 bones * 4 pixels = (16 * 16)
                        //       32x32 pixel texture max  256 bones * 4 pixels = (32 * 32)
                        //       64x64 pixel texture max 1024 bones * 4 pixels = (64 * 64)


                        float size = (float) Math.sqrt( bones.size() * 4 ); // 4 pixels needed for 1 matrix
                        int sizeInt = (int) MathTool.ceilPowerOfTwo( size );
                        sizeInt = Math.max( sizeInt, 4 );

//                        float[] boneMatrices = new float[sizeInt * sizeInt * 4];// 4 floats per RGBA pixel
                        // copy current values
                        float[] boneMatrices = Arrays.copyOf(skeleton.boneMatrices, skeleton.boneMatrices.length);

                        DataTexture boneTexture = new DataTexture( boneMatrices, sizeInt, sizeInt, RGBAFormat, FloatType );
                        boneTexture.needsUpdate = true;

                        skeleton.boneMatrices = boneMatrices;
                        skeleton.boneTexture = boneTexture;
                        skeleton.boneTextureSize = sizeInt;

                    }

                    p_uniforms.setValue( "boneTexture", skeleton.boneTexture, textures );
                    p_uniforms.setValue( "boneTextureSize", skeleton.boneTextureSize );

                } else {

                    p_uniforms.setOptional( skeleton, "boneMatrices" );

                }

            }

        }

        if ( refreshMaterial ) {

            p_uniforms.setValue( "toneMappingExposure", toneMappingExposure );
            p_uniforms.setValue( "toneMappingWhitePoint", toneMappingWhitePoint );

            if ( material.lights ) {
                // the current material requires lighting info

                // note: all lighting uniforms are always set correctly
                // they simply reference the renderer's state for their
                // values
                //
                // use the current material's .needsUpdate flags to set
                // the GL state when required

                markUniformsLightsNeedsUpdate( m_uniforms, refreshLights );
            }

            // refresh uniforms common to several materials

            if ( fog != null && material.fog ) {
                refreshUniformsFog( m_uniforms, fog );
            }

            if ( material instanceof MeshBasicMaterial ) {
                refreshUniformsCommon( m_uniforms, material );
            } else if ( material instanceof MeshLambertMaterial ) {
                refreshUniformsCommon( m_uniforms, material );
                refreshUniformsLambert( m_uniforms, (MeshLambertMaterial) material );
            } else if ( material instanceof MeshPhongMaterial ) {
                refreshUniformsCommon( m_uniforms, material );
                if ( material instanceof MeshToonMaterial ) {
                    refreshUniformsToon( m_uniforms, (MeshToonMaterial) material );
                } else {

                    refreshUniformsPhong( m_uniforms, (MeshPhongMaterial) material );

                }

            } else if ( material instanceof MeshStandardMaterial) {
                refreshUniformsCommon( m_uniforms, material );
                if ( material instanceof MeshPhysicalMaterial) {
                    refreshUniformsPhysical( m_uniforms, (MeshPhysicalMaterial) material );
                } else {
                    refreshUniformsStandard( m_uniforms, (MeshStandardMaterial) material );
                }

            } else if ( material instanceof MeshMatcapMaterial ) {
                refreshUniformsCommon( m_uniforms, material );

                refreshUniformsMatcap( m_uniforms, (MeshMatcapMaterial) material );
            } else if ( material instanceof MeshDepthMaterial ) {

                refreshUniformsCommon( m_uniforms, material );
                refreshUniformsDepth( m_uniforms, (MeshDepthMaterial) material );

            } else if ( material instanceof MeshDistanceMaterial ) {

                refreshUniformsCommon( m_uniforms, material );
                refreshUniformsDistance( m_uniforms, (MeshDistanceMaterial) material );

            } else if ( material instanceof MeshNormalMaterial ) {

                refreshUniformsCommon( m_uniforms, material );
                refreshUniformsNormal( m_uniforms, (MeshNormalMaterial) material );

            } else if ( material instanceof LineBasicMaterial) {

                refreshUniformsLine( m_uniforms, (LineBasicMaterial) material );

                if ( material instanceof LineDashedMaterial) {

                    refreshUniformsDash( m_uniforms, (LineDashedMaterial) material );

                }

            } else if ( material instanceof PointsMaterial) {

                refreshUniformsPoints( m_uniforms, (PointsMaterial) material );

            } else if ( material instanceof SpriteMaterial) {

                refreshUniformsSprites( m_uniforms, (SpriteMaterial) material );

            } else if ( material instanceof ShadowMaterial) {
                m_uniforms.put("color", ((ShadowMaterial) material).color);
                m_uniforms.put("opacity", material.opacity);
            }

            // RectAreaLight Texture
            // TODO (mrdoob): Find a nicer implementation

//            if ( m_uniforms.ltc_1 !== undefined ) m_uniforms.ltc_1.value = UniformsLib.LTC_1;
//            if ( m_uniforms.ltc_2 !== undefined ) m_uniforms.ltc_2.value = UniformsLib.LTC_2;

            GLUniforms.upload( materialProperties.uniformsList, m_uniforms, textures );
        }

        if ( material instanceof ShaderMaterial && ((ShaderMaterial)material).uniformsNeedUpdate ) {
            GLUniforms.upload(materialProperties.uniformsList, m_uniforms, textures );
            ((ShaderMaterial)material).uniformsNeedUpdate = false;
        }

        if ( material instanceof SpriteMaterial ) {
            p_uniforms.setValue("center", ((Sprite) object).center );
        }

        // common matrices

        p_uniforms.setValue( "modelViewMatrix", object.modelViewMatrix );
        p_uniforms.setValue( "normalMatrix", object.getNormalMatrix() );
        p_uniforms.setValue( "modelMatrix", object.getWorldMatrix() );

        return program;
    }

    // If uniforms are marked as clean, they don't need to be loaded to the GPU.
    private void markUniformsLightsNeedsUpdate(UniformsLib uniforms, boolean value) {
        uniforms.needUpdate.ambientLightColor = value;
        uniforms.needUpdate.lightProbe = value;

        uniforms.needUpdate.directionalLights = value;
        uniforms.needUpdate.pointLights = value;
        uniforms.needUpdate.spotLights = value;
        uniforms.needUpdate.rectAreaLights = value;
        uniforms.needUpdate.hemisphereLights = value;
    }

    private void refreshUniformsFog(UniformsLib uniforms, Fog fog) {
        uniforms.put("fogColor", fog.color);
        if (fog instanceof FogExp2) {
            uniforms.put("fogDensity", ((FogExp2) fog).density);
        } else {
            uniforms.put("fogNear", fog.near);
            uniforms.put("fogFar", fog.far);
        }
    }

    // Uniforms (refresh uniforms objects)
    private void refreshUniformsCommon(UniformsLib uniforms, Material material) {
        uniforms.put("opacity", material.opacity);
        if ( material.color != null ) {
            uniforms.put("diffuse", material.color.clone());
        }

        if ( material.emissive != null) {
            uniforms.put("emissive", material.emissive.clone().multiplyScalar(material.emissiveIntensity) );
        }

        if ( material.map != null) {
            uniforms.put("map", material.map);
        }

        if ( material.alphaMap != null) {
            uniforms.put("alphaMap", material.alphaMap);
        }

        if ( material.specularMap != null) {
            uniforms.put("specularMap", material.specularMap);
        }

        if ( material.envMap != null) {
            uniforms.put("envMap", material.envMap);

            // don't flip CubeTexture envMaps, flip everything else:
            //  WebGLRenderTargetCube will be flipped for backwards compatibility
            //  WebGLRenderTargetCube.texture will be flipped because it's a Texture and NOT a CubeTexture
            // this check must be handled differently, or removed entirely, if WebGLRenderTargetCube uses a CubeTexture in the future
            uniforms.put("flipEnvMap", material.envMap instanceof CubeTexture ? - 1 : 1);
            uniforms.put("reflectivity", material.reflectivity);
            uniforms.put("refractionRatio", material.refractionRatio);
            uniforms.put("maxMipLevel", properties.get( material.envMap ).maxMipLevel);
        }

        if ( material.lightMap != null) {
            uniforms.put("lightMap", material.lightMap);
            uniforms.put("lightMapIntensity", material.lightMapIntensity);
        }

        if ( material.aoMap != null) {
            uniforms.put("aoMap", material.aoMap);
            uniforms.put("aoMapIntensity", material.aoMapIntensity);
        }

        // uv repeat and offset setting priorities
        // 1. color map
        // 2. specular map
        // 3. normal map
        // 4. bump map
        // 5. alpha map
        // 6. emissive map

        Texture uvScaleMap = null;
        if ( material.map != null) {
            uvScaleMap = material.map;
        } else if ( material.specularMap != null) {

            uvScaleMap = material.specularMap;

        } else if ( material.displacementMap != null) {

            uvScaleMap = material.displacementMap;

        } else if ( material.normalMap != null) {

            uvScaleMap = material.normalMap;

        } else if ( material.bumpMap != null) {

            uvScaleMap = material.bumpMap;

        } else if ( material instanceof MeshStandardMaterial &&
                ((MeshStandardMaterial) material).roughnessMap != null) {

            uvScaleMap = ((MeshStandardMaterial) material).roughnessMap;

        } else if (  material instanceof MeshStandardMaterial &&
                ((MeshStandardMaterial) material).metalnessMap != null) {

            uvScaleMap = ((MeshStandardMaterial) material).metalnessMap;

        } else if ( material.alphaMap != null) {

            uvScaleMap = material.alphaMap;

        } else if ( material.emissiveMap != null) {

            uvScaleMap = material.emissiveMap;

        }

        if ( uvScaleMap != null ) {
            // backwards compatibility
//            if ( uvScaleMap.isWebGLRenderTarget ) {
//                uvScaleMap = uvScaleMap.texture;
//            }

            if ( uvScaleMap.matrixAutoUpdate) {
                uvScaleMap.updateMatrix();
            }
            uniforms.put("uvTransform", uvScaleMap.getMatrix().clone());
        }
    }

    private void refreshUniformsLine(UniformsLib uniforms, Material material) {
        uniforms.put("diffuse", material.color.clone());
        uniforms.put("opacity", material.opacity);
    }

    private void refreshUniformsDash(UniformsLib uniforms, LineDashedMaterial material) {
        uniforms.put("dashSize", material.dashSize);
        uniforms.put("totalSize", material.dashSize + material.gapSize);
        uniforms.put("scale", material.scale);
    }

    private void refreshUniformsPoints(UniformsLib uniforms, PointsMaterial material) {
        uniforms.put("diffuse", material.color.clone());
        uniforms.put("opacity", material.opacity);
        uniforms.put("size", material.size * _pixelRatio);
        uniforms.put("scale", _height * 0.5f);

        uniforms.put("map", material.map);

        if ( material.map != null ) {

            if ( material.map.matrixAutoUpdate) {
                material.map.updateMatrix();
            }
            uniforms.put("uvTransform", material.map.getMatrix().clone());
        }
    }

    private void refreshUniformsSprites(UniformsLib uniforms, SpriteMaterial material) {
        uniforms.put("diffuse", material.color.clone());
        uniforms.put("opacity", material.opacity);
        uniforms.put("rotation", material.rotation);
        uniforms.put("map", material.map);

        if ( material.map != null ) {

            if ( material.map.matrixAutoUpdate) {
                material.map.updateMatrix();
            }
            uniforms.put("uvTransform", material.map.getMatrix().clone());
        }
    }

    private void refreshUniformsLambert(UniformsLib uniforms, MeshLambertMaterial material) {
        if (material.emissiveMap != null) {
            uniforms.put("emissiveMap", material.emissiveMap);
        }
    }

    private void refreshUniformsPhong(UniformsLib uniforms, MeshPhongMaterial material) {
        uniforms.put("specular", material.specular.clone());
        uniforms.put("shininess", Math.max(material.shininess, 1e-4f));// to prevent pow( 0.0, 0.0 )
        if (material.emissiveMap != null) {
            uniforms.put("emissiveMap", material.emissiveMap);
        }
        if (material.bumpMap != null) {
            uniforms.put("bumpMap", material.bumpMap);
            uniforms.put("bumpScale", material.bumpScale);
            if (material.side == BackSide) {
                uniforms.put("bumpScale", material.bumpScale * -1);
            }
        }
        if ( material.normalMap != null ) {
            uniforms.put("normalMap", material.normalMap);
            uniforms.put("normalScale", material.normalScale);
            if ( material.side == BackSide ) uniforms.put("normalScale", material.normalScale.negate());
        }

        if ( material.displacementMap != null ) {
            uniforms.put("displacementMap", material.displacementMap);
            uniforms.put("displacementScale", material.displacementScale);
            uniforms.put("displacementBias", material.displacementBias);
        }
    }

    private void refreshUniformsToon(UniformsLib uniforms, MeshToonMaterial material) {
        refreshUniformsPhong( uniforms, material );
        if (material.gradientMap != null) {
            uniforms.put("gradientMap", material.gradientMap);
        }
    }

    private void refreshUniformsStandard(UniformsLib uniforms, MeshStandardMaterial material) {
        uniforms.put("roughness", material.roughness);
        uniforms.put("metalness", material.metalness);

        if ( material.roughnessMap != null ) {
            uniforms.put("roughnessMap", material.roughnessMap);
        }

        if ( material.metalnessMap != null ) {
            uniforms.put("metalnessMap", material.metalnessMap);
        }

        if ( material.emissiveMap != null ) {
            uniforms.put("emissiveMap", material.emissiveMap);
        }

        if ( material.bumpMap != null ) {
            uniforms.put("bumpMap", material.bumpMap);
            uniforms.put("bumpScale", material.bumpScale);
            if (material.side == BackSide) {
                uniforms.put("bumpScale", material.bumpScale * -1);
            }
        }
        if ( material.normalMap != null ) {
            uniforms.put("normalMap", material.normalMap);
            uniforms.put("normalScale", material.normalScale);
            if ( material.side == BackSide ) uniforms.put("normalScale", material.normalScale.negate());
        }

        if ( material.displacementMap != null ) {
            uniforms.put("displacementMap", material.displacementMap);
            uniforms.put("displacementScale", material.displacementScale);
            uniforms.put("displacementBias", material.displacementBias);
        }

        if ( material.envMap != null ) {
            uniforms.put("envMapIntensity", material.emissiveIntensity);
            //uniforms.envMap = material.envMap; // part of uniforms common

        }
    }

    private void refreshUniformsPhysical(UniformsLib uniforms, MeshPhysicalMaterial material) {
        refreshUniformsStandard( uniforms, material );
        uniforms.put("reflectivity", material.reflectivity);// also part of uniforms common

        uniforms.put("clearCoat", material.clearCoat);
        uniforms.put("clearCoatRoughness", material.clearCoatRoughness);
    }

    private void refreshUniformsMatcap(UniformsLib uniforms, MeshMatcapMaterial material) {
        if ( material.matcap != null ) {
            uniforms.put("matcap", material.matcap);

        }
        if ( material.bumpMap != null ) {
            uniforms.put("bumpMap", material.bumpMap);
            uniforms.put("bumpScale", material.bumpScale);
            if (material.side == BackSide) {
                uniforms.put("bumpScale", material.bumpScale * -1);
            }
        }
        if ( material.normalMap != null ) {
            uniforms.put("normalMap", material.normalMap);
            uniforms.put("normalScale", material.normalScale);
            if ( material.side == BackSide ) uniforms.put("normalScale", material.normalScale.negate());
        }

        if ( material.displacementMap != null ) {
            uniforms.put("displacementMap", material.displacementMap);
            uniforms.put("displacementScale", material.displacementScale);
            uniforms.put("displacementBias", material.displacementBias);
        }
    }

    private void refreshUniformsDepth(UniformsLib uniforms, MeshDepthMaterial material) {
        if ( material.displacementMap != null ) {
            uniforms.put("displacementMap", material.displacementMap);
            uniforms.put("displacementScale", material.displacementScale);
            uniforms.put("displacementBias", material.displacementBias);
        }
    }

    private void refreshUniformsDistance(UniformsLib uniforms, MeshDistanceMaterial material) {
        if ( material.displacementMap != null ) {
            uniforms.put("displacementMap", material.displacementMap);
            uniforms.put("displacementScale", material.displacementScale);
            uniforms.put("displacementBias", material.displacementBias);
        }
        uniforms.put("referencePosition", material.referencePosition.clone());
        uniforms.put("nearDistance", material.nearDistance);
        uniforms.put("farDistance", material.farDistance);
    }

    private void refreshUniformsNormal(UniformsLib uniforms, MeshNormalMaterial material) {
        if ( material.bumpMap != null ) {
            uniforms.put("bumpMap", material.bumpMap);
            uniforms.put("bumpScale", material.bumpScale);
            if (material.side == BackSide) {
                uniforms.put("bumpScale", material.bumpScale * -1);
            }
        }
        if ( material.normalMap != null ) {
            uniforms.put("normalMap", material.normalMap);
            uniforms.put("normalScale", material.normalScale);
            if ( material.side == BackSide ) uniforms.put("normalScale", material.normalScale.negate());
        }

        if ( material.displacementMap != null ) {
            uniforms.put("displacementMap", material.displacementMap);
            uniforms.put("displacementScale", material.displacementScale);
            uniforms.put("displacementBias", material.displacementBias);
        }
    }

    public void copyFramebufferToTexture(Vector2 position, Texture texture, int level) {
        int width = texture.imgWidth;
        int height = texture.imgHeight;
        int glFormat = GLUtils.convert( texture.format );

        textures.setTexture2D( texture, 0 );

        GLES30.glCopyTexImage2D( GLES30.GL_TEXTURE_2D, level > 0 ? level : 0, glFormat, (int)position.x, (int)position.y,
                width, height, 0 );
    }

    public static class Param {
        public String precision;
        public boolean logarithmicDepthBuffer;
        public boolean alpha = false;
        public boolean depth = true;
        public boolean stencil = true;
        public boolean antialias = false;
        public boolean premultipliedAlpha = true;
        public boolean preserveDrawingBuffer = false;
        public boolean failIfMajorPerformanceCaveat = false;
    }

    private static class GeometryProgram {
        public long geometry = 0;
        public int program = -1;
        public boolean wireframe = false;
    }
}
