package edu.three.renderers.gl;

import android.opengl.GLES30;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.HashMap;

import javax.microedition.khronos.opengles.GL;

import edu.three.constant.Constants;
import edu.three.materials.Material;
import edu.three.math.Vector4;
import edu.three.textures.Texture;

public class GLState {
    HashMap<Integer, Boolean> enabledCapabilities = new HashMap<>();
    public ColorBuffer colorBuffer = new ColorBuffer();
    public DepthBuffer depthBuffer = new DepthBuffer(this);
    public StencilBuffer stencilBuffer = new StencilBuffer(this);
    int compressedTextureFormats = -1;
    int currentProgram = -1;

    boolean currentBlendingEnabled;
    int currentBlending = -1;
    int currentBlendEquation = -1;
    int currentBlendSrc = -1;
    int currentBlendDst = -1;
    int currentBlendEquationAlpha = -1;
    int currentBlendSrcAlpha = -1;
    int currentBlendDstAlpha = -1;
    boolean currentPremultipledAlpha;

    boolean currentFlipSided;
    int currentCullFace = -1;

    float currentLineWidth = -1f;

    float currentPolygonOffsetFactor = -1f;
    float currentPolygonOffsetUnits = -1f;

    int maxTextures;
    int version = 0;
    String glVersion;

    boolean lineWidthAvailable = true;
    int currentTextureSlot = -1;
    HashMap<Integer, BoundTexture> currentBoundTextures = new HashMap<>();

    Vector4 currentScissor = new Vector4();
    Vector4 currentViewport = new Vector4();

    short[] newAttributes;
    short[] enabledAttributes;
    int[] attributeDivisors;

    HashMap<Integer, Integer> emptyTextures = new HashMap<>();

    public GLState() {
        glVersion = GLES30.glGetString(GLES30.GL_VERSION);
        int[] maxVertexAttri = new int[1];
        GLES30.glGetIntegerv(GLES30.GL_MAX_VERTEX_ATTRIBS, maxVertexAttri, 0);
        int maxVertexAttributes = maxVertexAttri[0];
        newAttributes = new short[maxVertexAttributes];
        enabledAttributes = new short[maxVertexAttributes];
        attributeDivisors = new int[maxVertexAttributes];

        int[] temp = new int[1];
        GLES30.glGetIntegerv(GLES30.GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS, temp, 0);
        maxTextures = temp[0];

        emptyTextures.put(GLES30.GL_TEXTURE_2D, createTexture(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_2D, 1));
        emptyTextures.put(GLES30.GL_TEXTURE_CUBE_MAP, createTexture(GLES30.GL_TEXTURE_CUBE_MAP, GLES30.GL_TEXTURE_CUBE_MAP_POSITIVE_X, 6));

        //init
        colorBuffer.setClear(0, 0, 0, 1, false);
        depthBuffer.setClear(1);
        stencilBuffer.setClear(0);

        enable(GLES30.GL_DEPTH_TEST);
        depthBuffer.setFunc(Constants.LessEqualDepth);

        setFlipSided( false );
        setCullFace( Constants.CullFaceBack );
        enable( GLES30.GL_CULL_FACE );

        setBlending( Constants.NoBlending );
    }

    public void initAttributes() {
        for ( int i = 0, l = newAttributes.length; i < l; i ++ ) {
            newAttributes[ i ] = 0;
        }
    }

    public int createTexture(int type, int target, int count) {
        IntBuffer data = ByteBuffer
                .allocateDirect(4 * 4)
                .order(ByteOrder.nativeOrder())
                .asIntBuffer();   // 4 is required to match default unpack alignment of 4.
        int[] temp = new int[1];
        GLES30.glGenTextures(1, temp, 0);
        GLES30.glBindTexture(type, temp[0]);
        GLES30.glTexParameteri(type, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_NEAREST);
        GLES30.glTexParameteri(type, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_NEAREST);

        for (int i = 0; i < count; i++) {
            GLES30.glTexImage2D(target + i, 0, GLES30.GL_RGBA, 1, 1, 0, GLES30.GL_RGBA,
                    GLES30.GL_UNSIGNED_BYTE, data);
        }
        return temp[0];
    }

    public void enableAttribute(int attribute) {
        enableAttribute(attribute, -1);
    }
    public void enableAttribute(int attribute, int meshPerAttribute) {
        newAttributes[attribute] = 1;
        if (enabledAttributes[attribute] == 0) {
            GLES30.glEnableVertexAttribArray(attribute);
            enabledAttributes[attribute] = 1;
        }
        if (meshPerAttribute >= 0) {
            if ( attributeDivisors[ attribute ] != meshPerAttribute ) {
                GLES30.glVertexAttribDivisor(attribute, meshPerAttribute);
                attributeDivisors[ attribute ] = meshPerAttribute;
            }
        }
    }

    public void disableUnusedAttributes() {
        for (int i = 0; i < enabledAttributes.length; i++) {
            if (enabledAttributes[i] != newAttributes[i]) {
                GLES30.glDisableVertexAttribArray(i);
                enabledAttributes[i] = 0;
            }
        }
    }

    public void enable(int id) {
        Boolean val = enabledCapabilities.get(id);
        if (val == null || !val ) {
            GLES30.glEnable(id);
            enabledCapabilities.put(id, true);
        }
    }

    public void disable(int id) {
        Boolean val = enabledCapabilities.get(id);
        if (val == null || val) {
            GLES30.glDisable(id);
            enabledCapabilities.put(id, false);
        }
    }

    public void setMaterial(Material material) {
        setMaterial(material, false);
    }
    public void setMaterial(Material material, boolean frontFaceCW) {
        if (material.side == Constants.DoubleSide) {
            disable( GLES30.GL_CULL_FACE );
        } else {
            enable( GLES30.GL_CULL_FACE );
        }
        boolean flipSided = material.side == Constants.BackSide;
        if (frontFaceCW) {
            flipSided = !flipSided;
        }
        setFlipSided(flipSided);

        if ( material.blending == Constants.NormalBlending && !material.transparent ) {
            setBlending( Constants.NoBlending );
        } else {
            setBlending( material.blending, material.blendEquation, material.blendSrc, material.blendDst,
                    material.blendEquationAlpha, material.blendSrcAlpha, material.blendDstAlpha, material.premultipliedAlpha );
        }

        depthBuffer.setFunc( material.depthFunc );
        depthBuffer.setTest( material.depthTest );
        depthBuffer.setMask( material.depthWrite );
        colorBuffer.setMask( material.colorWrite );

        setPolygonOffset( material.polygonOffset, material.polygonOffsetFactor, material.polygonOffsetUnits );
    }

    public void setFlipSided(Boolean flipSided) {
        if (currentFlipSided != flipSided) {
            if (flipSided) {
                GLES30.glFrontFace(GLES30.GL_CW);
            } else {
                GLES30.glFrontFace(GLES30.GL_CCW);
            }
            currentFlipSided = flipSided;
        }
    }

    public void setCullFace(Integer cullFace) {
        if (cullFace != Constants.CullFaceNone) {
            enable(GLES30.GL_CULL_FACE);
            if (cullFace != currentCullFace) {
                if (cullFace == Constants.CullFaceBack) {
                    GLES30.glCullFace(GLES30.GL_BACK);
                } else if (cullFace == Constants.CullFaceFront) {
                    GLES30.glCullFace(GLES30.GL_FRONT);
                } else {
                    GLES30.glCullFace(GLES30.GL_FRONT_AND_BACK);
                }
            }
        } else {
            disable(GLES30.GL_CULL_FACE);
        }
        currentCullFace = cullFace;
    }

    public void setLineWidth(Float width) {
        if (width != currentLineWidth) {
            if (lineWidthAvailable) {
                GLES30.glLineWidth(width);
            }
            currentLineWidth = width;
        }
    }

    public void setPolygonOffset(boolean polygonOffset, Float factor, Float units) {
        if (polygonOffset) {
            enable(GLES30.GL_POLYGON_OFFSET_FILL);
            if (currentPolygonOffsetFactor != factor || currentPolygonOffsetUnits != units) {
                GLES30.glPolygonOffset(factor, units);
                currentPolygonOffsetFactor = factor;
                currentPolygonOffsetUnits = units;
            }
        } else {
            disable(GLES30.GL_POLYGON_OFFSET_FILL);
        }
    }

    public void setScissorTest(boolean scissorTest) {
        if (scissorTest) {
            enable(GLES30.GL_SCISSOR_TEST);
        } else {
            disable(GLES30.GL_SCISSOR_TEST);
        }
    }

    // texture
    public void activeTexture(Integer glSlot) {
        Integer slot = glSlot;
        if (glSlot == null) {
            slot = GLES30.GL_TEXTURE0 + maxTextures - 1;
        }
        if (currentTextureSlot != slot) {
            GLES30.glActiveTexture(slot);
            currentTextureSlot = slot;
        }
    }

    public void bindTexture(int glType, int glTexture) {
        if (currentTextureSlot == -1) {
            activeTexture(null);
        }
        BoundTexture boundTexture = currentBoundTextures.get(currentTextureSlot);
        if (boundTexture == null) {
            boundTexture = new BoundTexture();
            currentBoundTextures.put(currentTextureSlot, boundTexture);
        }
        if (boundTexture.type != glType || boundTexture.texture != glTexture) {
            GLES30.glBindTexture(glType, glTexture);
            boundTexture.type = glType;
            boundTexture.texture = glTexture;
        }
    }

    public void compressedTexImage2D() {

    }

    public void texImage2D() {

    }

    public void texImage3D() {

    }

    public void getCompressedTextureFormats() {

    }

    public void scissor(Vector4 scissor) {
        if (!currentScissor.equals(scissor)) {
            GLES30.glScissor((int)scissor.x, (int)scissor.y, (int)scissor.z, (int)scissor.w);
            currentScissor.copy(scissor);
        }
    }

    public void viewport(Vector4 viewport) {
        if (!currentViewport.equals(viewport)) {
            GLES30.glViewport((int)viewport.x, (int)viewport.y, (int)viewport.z, (int)viewport.w);
            currentViewport.copy(viewport);
        }
    }

    public void setBlending(int blending) {
        setBlending(blending, 0, 0, 0, 0, 0,
                0, null);
    }
    public void setBlending(int blending, int blendEquation, int blendSrc, int blendDst, int blendEquationAlpha,
                            int blendSrcAlpha, int blendDstAlpha, Boolean premultipliedAlpha) {
        if (blending == Constants.NoBlending) {
            if (currentBlendingEnabled) {
                disable(GLES30.GL_BLEND);
                currentBlendingEnabled = false;
            }
            return;
        }
        if (!currentBlendingEnabled) {
            enable(GLES30.GL_BLEND);
            currentBlendingEnabled = true;
        }
        if (blending != Constants.CustomBlending) {
            if ( blending != currentBlending || premultipliedAlpha != currentPremultipledAlpha ) {
                if (currentBlendEquation != Constants.AddEquation || currentBlendEquationAlpha != Constants.AddEquation) {
                    GLES30.glBlendEquation(GLES30.GL_FUNC_ADD);
                    currentBlendEquation = Constants.AddEquation;
                    currentBlendEquationAlpha = Constants.AddEquation;
                }
            }
            if (premultipliedAlpha) {
                switch (blending) {
                    case Constants.NormalBlending:
                        GLES30.glBlendFuncSeparate(GLES30.GL_ONE, GLES30.GL_ONE_MINUS_SRC_ALPHA, GLES30.GL_ONE,
                                GLES30.GL_ONE_MINUS_SRC_ALPHA);
                        break;

                    case Constants.AdditiveBlending:
                        GLES30.glBlendFunc( GLES30.GL_ONE, GLES30.GL_ONE );
                        break;

                    case Constants.SubtractiveBlending:
                        GLES30.glBlendFuncSeparate( GLES30.GL_ZERO, GLES30.GL_ZERO, GLES30.GL_ONE_MINUS_SRC_COLOR,
                                GLES30.GL_ONE_MINUS_SRC_ALPHA );
                        break;

                    case Constants.MultiplyBlending:
                        GLES30.glBlendFuncSeparate( GLES30.GL_ZERO, GLES30.GL_SRC_COLOR, GLES30.GL_ZERO, GLES30.GL_SRC_ALPHA );
                        break;

                    default:
                        Log.e(getClass().getSimpleName(), "THREE.WebGLState: Invalid blending: " + blending );
                        break;
                }
            } else {
                switch (blending) {
                    case Constants.NormalBlending:
                        GLES30.glBlendFuncSeparate(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA, GLES30.GL_ONE,
                                GLES30.GL_ONE_MINUS_SRC_ALPHA);
                        break;

                    case Constants.AdditiveBlending:
                        GLES30.glBlendFunc( GLES30.GL_SRC_ALPHA, GLES30.GL_ONE );
                        break;

                    case Constants.SubtractiveBlending:
                        GLES30.glBlendFunc( GLES30.GL_ZERO, GLES30.GL_ONE_MINUS_SRC_COLOR);
                        break;

                    case Constants.MultiplyBlending:
                        GLES30.glBlendFunc( GLES30.GL_ZERO, GLES30.GL_SRC_COLOR);
                        break;

                    default:
                        Log.e(getClass().getSimpleName(), "THREE.WebGLState: Invalid blending: " + blending );
                        break;
                }
            }

            currentBlendSrc = -1;
            currentBlendDst = -1;
            currentBlendSrcAlpha = -1;
            currentBlendDstAlpha = -1;

            currentBlending = blending;
            currentPremultipledAlpha = premultipliedAlpha;
            return;
        }
        // custom blending
        blendEquationAlpha = blendEquationAlpha > 0 ? blendEquationAlpha : blendEquation;
        blendSrcAlpha = blendSrcAlpha > 0 ? blendSrcAlpha:  blendSrc;
        blendDstAlpha = blendDstAlpha > 0 ? blendDstAlpha : blendDst;

        if ( blendEquation != currentBlendEquation || blendEquationAlpha != currentBlendEquationAlpha ) {
            GLES30.glBlendEquationSeparate( GLUtils.convert( blendEquation ), GLUtils.convert( blendEquationAlpha ) );

            currentBlendEquation = blendEquation;
            currentBlendEquationAlpha = blendEquationAlpha;
        }
        if ( blendSrc != currentBlendSrc || blendDst != currentBlendDst || blendSrcAlpha != currentBlendSrcAlpha || blendDstAlpha != currentBlendDstAlpha ) {

            GLES30.glBlendFuncSeparate( GLUtils.convert( blendSrc ), GLUtils.convert( blendDst ), GLUtils.convert( blendSrcAlpha ), GLUtils.convert( blendDstAlpha ) );

            currentBlendSrc = blendSrc;
            currentBlendDst = blendDst;
            currentBlendSrcAlpha = blendSrcAlpha;
            currentBlendDstAlpha = blendDstAlpha;

        }

        currentBlending = blending;
        currentPremultipledAlpha = false;
    }

    public boolean useProgram(Integer program) {
        if (currentProgram != program) {
            GLES30.glUseProgram(program);
            currentProgram = program;
            return true;
        }
        return false;
    }

    public void reset() {
        for ( int i = 0; i < enabledAttributes.length; i ++ ) {
            if ( enabledAttributes[ i ] == 1 ) {
                GLES30.glDisableVertexAttribArray( i );
                enabledAttributes[ i ] = 0;
            }
        }

        enabledCapabilities.clear();
        compressedTextureFormats = -1;

        currentTextureSlot = -1;
        currentBoundTextures.clear();
    }

    public static class BoundTexture {
        public int type;
        public int texture;
    }
    public static class ColorBuffer {
        boolean locked = false;
        Vector4 color = new Vector4();
        Boolean currentColorMask = null;
        Vector4 currentColorClear = new Vector4(0, 0, 0, 0);

        public void setMask(Boolean colorMask) {
            if (colorMask != currentColorMask && !locked) {
                GLES30.glColorMask(colorMask, colorMask, colorMask, colorMask);
                currentColorMask = colorMask;
            }
        }

        public void setLocked(boolean lock) {
            locked = lock;
        }

        public void setClear(float r, float g, float b, float a, boolean premultipliedAlpha) {
            if (premultipliedAlpha) {
                r *= a; g *= a; b *= a;
            }
            color.set(r, g, b, a);
            if (!color.equals(currentColorClear)) {
                GLES30.glClearColor(r, g, b, a);
                currentColorClear.copy(color);
            }
        }

        public void reset() {
            locked = false;
            currentColorMask = null;
            currentColorClear.set(-1, 0, 0, 0); // set to invalid state
        }
    }

    public static class DepthBuffer {
        GLState context;
        boolean locked = false;
        Boolean currentDepthMask = null;
        Integer currentDepthFunc = null;
        float currentDepthClear;

        public DepthBuffer(GLState parent) {
            context = parent;
        }

        public void setTest(boolean depthTest) {
            if (depthTest) {
                context.enable(GLES30.GL_DEPTH_TEST);
            } else {
                context.disable(GLES30.GL_DEPTH_TEST);
            }
        }

        public void setMask(Boolean depthMask) {
            if (currentDepthMask != depthMask && !locked) {
                GLES30.glDepthMask(depthMask);
                currentDepthMask = depthMask;
            }
        }

        public void setFunc(Integer depthFunc) {
            if (currentDepthFunc != depthFunc) {
                if (depthFunc != null) {
                    switch (depthFunc) {
                        case Constants.NeverDepth:
                            GLES30.glDepthFunc(GLES30.GL_NEVER);
                            break;
                        case Constants.AlwaysDepth:
                            GLES30.glDepthFunc(GLES30.GL_ALWAYS);
                            break;
                        case Constants.LessDepth:
                            GLES30.glDepthFunc(GLES30.GL_LESS);
                            break;
                        case Constants.LessEqualDepth:
                            GLES30.glDepthFunc(GLES30.GL_LEQUAL);
                            break;
                        case Constants.EqualDepth:
                            GLES30.glDepthFunc(GLES30.GL_EQUAL);
                            break;
                        case Constants.GreaterEqualDepth:
                            GLES30.glDepthFunc(GLES30.GL_GEQUAL);
                            break;
                        case Constants.GreaterDepth:
                            GLES30.glDepthFunc(GLES30.GL_GREATER);
                            break;
                        case Constants.NotEqualDepth:
                            GLES30.glDepthFunc(GLES30.GL_NOTEQUAL);
                            break;
                        default:
                            GLES30.glDepthFunc(GLES30.GL_LEQUAL);
                    }
                } else {
                    GLES30.glDepthFunc(GLES30.GL_LEQUAL);
                }
                currentDepthFunc = depthFunc;
            }
        }

        public void setLocked(boolean lock) {
            locked = lock;
        }

        public void setClear(float depth) {
            if (currentDepthClear != depth) {
                GLES30.glClearDepthf(depth);
                currentDepthClear = depth;
            }
        }

        public void reset() {
            locked = false;
            currentDepthMask = null;
            currentDepthFunc = null;
            currentDepthClear = -1;
        }
    }

    public static class StencilBuffer {
        GLState context;
        boolean locked = false;

        Integer currentStencilMask = null;
        Integer currentStencilFunc = null;
        Integer currentStencilRef = null;
        Integer currentStencilFuncMask = null;
        Integer currentStencilFail = null;
        Integer currentStencilZFail = null;
        Integer currentStencilZPass = null;
        Integer currentStencilClear = null;

        public StencilBuffer(GLState parent) {
            context = parent;
        }

        public void setTest(boolean stencilTest) {
            if (stencilTest) {
                context.enable(GLES30.GL_STENCIL_TEST);
            } else {
                context.disable(GLES30.GL_STENCIL_TEST);
            }
        }

        public void setMask(Integer stencilMask) {
            if (currentStencilMask != stencilMask && !locked) {
                GLES30.glStencilMask(stencilMask);
                currentStencilMask = stencilMask;
            }
        }

        public void setFunc(Integer stencilFunc, Integer stencilRef, Integer stencilMask) {
            if ( currentStencilFunc != stencilFunc ||
                    currentStencilRef 	!=stencilRef 	||
                    currentStencilFuncMask != stencilMask ) {

                GLES30.glStencilFunc(stencilFunc, stencilRef, stencilMask);

                currentStencilFunc = stencilFunc;
                currentStencilRef = stencilRef;
                currentStencilFuncMask = stencilMask;

            }
        }

        public void setOp(Integer stencilFail, Integer stencilZFail, Integer stencilZPass) {
            if ( currentStencilFail	 != stencilFail 	||
                    currentStencilZFail != stencilZFail ||
                    currentStencilZPass != stencilZPass ) {

                GLES30.glStencilOp( stencilFail, stencilZFail, stencilZPass );

                currentStencilFail = stencilFail;
                currentStencilZFail = stencilZFail;
                currentStencilZPass = stencilZPass;

            }
        }

        public void setLocked(boolean lock) {
            locked = lock;
        }

        public void setClear(Integer stencil) {
            if ( currentStencilClear != stencil ) {
                GLES30.glClearStencil( stencil );
                currentStencilClear = stencil;
            }
        }

        public void reset() {
            locked = false;

            currentStencilMask = null;
            currentStencilFunc = null;
            currentStencilRef = null;
            currentStencilFuncMask = null;
            currentStencilFail = null;
            currentStencilZFail = null;
            currentStencilZPass = null;
            currentStencilClear = null;
        }
    }
}
