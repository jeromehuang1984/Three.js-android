package edu.three.renderers.gl;

import android.graphics.Bitmap;
import android.opengl.GLES30;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

import edu.three.core.Event;
import edu.three.core.IListener;
import edu.three.math.MathTool;
import edu.three.renderers.GLMultisampleRenderTarget;
import edu.three.renderers.GLRenderTarget;
import edu.three.renderers.GLRenderTargetCube;
import edu.three.textures.CompressedTexture;
import edu.three.textures.CubeTexture;
import edu.three.textures.DataTexture;
import edu.three.textures.DataTexture2DArray;
import edu.three.textures.DataTexture3D;
import edu.three.textures.DepthTexture;
import edu.three.textures.Texture;
import edu.three.textures.VideoTexture;

import static edu.three.constant.Constants.DepthFormat;
import static edu.three.constant.Constants.DepthStencilFormat;
import static edu.three.constant.Constants.FloatType;
import static edu.three.constant.Constants.LinearFilter;
import static edu.three.constant.Constants.NearestFilter;
import static edu.three.constant.Constants.NearestMipMapLinearFilter;
import static edu.three.constant.Constants.NearestMipMapNearestFilter;
import static edu.three.constant.Constants.UnsignedInt248Type;
import static edu.three.constant.Constants.UnsignedShortType;
import static edu.three.math.MathTool.LOG2E;

public class GLTextures {
    String TAG = getClass().getSimpleName();
    GLState state;
    GLProperties properties;
    GLCapabilities capabilities;
    GLInfo info;

    HashMap<Long, Integer> _videoTextures = new HashMap<>();
    int textureUnits = 0;
    private int ClampToEdgeWrapping;
    private int UnsignedIntType;

    public GLTextures(GLState state, GLProperties properties, GLCapabilities capabilities, GLInfo info) {
        this.state = state;
        this.properties = properties;
        this.capabilities = capabilities;
        this.info = info;
    }

    private Bitmap resizeImage(Bitmap image, boolean needsPowerOfTwo, int maxSize) {
        float scale = 1;
        int imgWidth = image.getWidth();
        int imgHeight = image.getHeight();
        // handle case if texture exceeds max size
        if (imgWidth > maxSize ||imgHeight > maxSize) {
            scale = maxSize / Math.max(imgWidth, imgHeight);
        }
        // only perform resize if necessary
        if (scale < 1 || needsPowerOfTwo) {
            int width = (int) (imgWidth * scale), height = (int) (imgHeight * scale);
            if (needsPowerOfTwo) {
                width = (int)MathTool.floorPowerOfTwo(scale * imgWidth);
                height = (int)MathTool.floorPowerOfTwo(scale * imgHeight);
            }
            Bitmap ret = Bitmap.createScaledBitmap(image, width, height, false);
            image.recycle();
            return ret;
        }
        Log.i(TAG, "no resize for bitmap: " + imgWidth + " x " + imgHeight);
        return image;
    }

    private boolean isPowerOfTwo(Bitmap image) {
        return MathTool.isPowerOfTwo(image.getWidth()) && MathTool.isPowerOfTwo(image.getHeight());
    }

    private boolean isPowerOfTwo(int width, int height) {
        return MathTool.isPowerOfTwo(width) && MathTool.isPowerOfTwo(height);
    }

    private boolean textureNeedsPowerOfTwo(Texture texture) {
        return false;
    }

    private boolean textureNeedsGenerateMipmaps(Texture texture, boolean supportsMips) {
        return texture.generateMipmaps && supportsMips &&
                texture.minFilter != NearestFilter && texture.minFilter != LinearFilter;
    }

    private void generateMipmap(int target, Texture texture, int width, int height) {
        GLES30.glGenerateMipmap(target);
        GLProperties.Fields textureProperties = properties.get(texture);
        textureProperties.maxMipLevel = (int) (Math.log(Math.max(width, height) ) * LOG2E);
    }

    private int getInternalFormat(int glFormat, int glType) {
        int internalFormat = glFormat;
//        if (glFormat == GLES30.GL_RED) {
//            if ( glType == GLES30.GL_FLOAT ) internalFormat = GLES30.GL_R32F;
//            if ( glType == GLES30.GL_HALF_FLOAT ) internalFormat = GLES30.GL_R16F;
//            if ( glType == GLES30.GL_UNSIGNED_BYTE ) internalFormat = GLES30.GL_R8;
//        }
//        if ( glFormat == GLES30.GL_RGB ) {
//            if ( glType == GLES30.GL_FLOAT ) internalFormat = GLES30.GL_RGB32F;
//            if ( glType == GLES30.GL_HALF_FLOAT ) internalFormat = GLES30.GL_RGB16F;
//            if ( glType == GLES30.GL_UNSIGNED_BYTE ) internalFormat = GLES30.GL_RGB8;
//        }
//        if ( glFormat == GLES30.GL_RGBA ) {
//            if ( glType == GLES30.GL_FLOAT ) internalFormat = GLES30.GL_RGBA32F;
//            if ( glType == GLES30.GL_HALF_FLOAT ) internalFormat = GLES30.GL_RGBA16F;
//            if ( glType == GLES30.GL_UNSIGNED_BYTE ) internalFormat = GLES30.GL_RGBA8;
//        }
        return internalFormat;
    }

    // Fallback filters for non-power-of-2 textures
    private int filterFallback(int f) {
        if (f == NearestFilter || f == NearestMipMapNearestFilter || f == NearestMipMapLinearFilter ) {
            return GLES30.GL_NEAREST;
        }

        return GLES30.GL_LINEAR;
    }

    private IListener onTextureDispose = new IListener() {
        @Override
        public void onEvent(Event event) {
            Texture texture = (Texture) event.target;
            texture.removeEventListener("dispose", onTextureDispose);
            deallocateTexture(texture);

            if (texture instanceof VideoTexture) {
                _videoTextures.remove(texture.id);
            }

            info.textures--;
        }
    };

    private IListener onRenderTargetDispose = new IListener() {
        @Override
        public void onEvent(Event event) {
            GLRenderTarget renderTarget = (GLRenderTarget) event.target;
            renderTarget.removeEventListener("dispose", onTextureDispose);
            deallocateRenderTarget( renderTarget );

            info.textures--;
        }
    };

    private void deallocateTexture(Texture texture) {
        GLProperties.Fields textureProperties = properties.get(texture);
        if (!textureProperties.glInit) {
            return;
        }
        int[] temp = new int[] {textureProperties.glTexture};
        GLES30.glDeleteTextures(1, temp, 0);
        properties.remove(texture);
    }

    private void deallocateRenderTarget(GLRenderTarget renderTarget) {
        if (renderTarget == null) {
            return;
        }
        GLProperties.Fields renderTargetProperties = properties.get(renderTarget);
        GLProperties.Fields textureProperties = properties.get( renderTarget.texture );
        if (textureProperties.glTexture != null) {
            int[] temp = new int[] {textureProperties.glTexture};
            GLES30.glDeleteTextures(1, temp, 0);
        }
        if (renderTarget.depthTexture != null) {
            renderTarget.depthTexture.dispose();
        }
        if (renderTarget instanceof GLRenderTargetCube) {
            for (int i = 0; i < 6; i++) {
                GLES30.glDeleteFramebuffers(1, renderTargetProperties.glFramebuffers[i], 0);
                if (renderTargetProperties.glDepthbuffer != null) {
                    GLES30.glDeleteRenderbuffers(1, renderTargetProperties.glDepthbuffer, 0);
                }
            }
        } else {
            GLES30.glDeleteFramebuffers(1, renderTargetProperties.glFramebuffer, 0);
            if (renderTargetProperties.glDepthbuffer != null) {
                GLES30.glDeleteRenderbuffers(1, renderTargetProperties.glDepthbuffer, 0);
            }
        }
        properties.remove(renderTarget.texture);
        properties.remove(renderTarget);
    }

    public void resetTextureUnits() {
        textureUnits = 0;
    }

    public int allocateTextureUnit() {
        int textureUnit = textureUnits;
        if (textureUnit >= capabilities.maxTextures) {
            Log.w(getClass().getSimpleName(), "Trying to use " + textureUnit +
                    "texture units while this GPU supports only " + capabilities.maxTextures);
        }
        textureUnits += 1;
        return textureUnit;
    }

    public void initTexture(GLProperties.Fields textureProperties, Texture texture) {
        if (!textureProperties.glInit) {
            textureProperties.glInit = true;
            texture.addEventListener( "dispose", onTextureDispose );
            int[] temp = new int[1];
            GLES30.glGenTextures(1, temp, 0);
            textureProperties.glTexture = temp[0];

            info.textures ++;
        }
    }

    public void setTexture2D(Texture texture, int slot) {
        setTexture(texture, slot, GLES30.GL_TEXTURE_2D);
    }
    public void setTexture3D(Texture texture, int slot) {
        setTexture(texture, slot, GLES30.GL_TEXTURE_3D);
    }
    public void setTexture2DArray(Texture texture, int slot) {
        setTexture(texture, slot, GLES30.GL_TEXTURE_2D_ARRAY);
    }
    public void setTexture(Texture texture, int slot, int textureType) {
        GLProperties.Fields textureProperties = properties.get(texture);
        if (texture instanceof VideoTexture) {
            updateVideoTexture((VideoTexture) texture);
        }
        if (texture.version > 0 && textureProperties.version != texture.version) {
            uploadTexture(textureProperties, texture, slot);
            return;
        }
        state.activeTexture(GLES30.GL_TEXTURE0 + slot);
        state.bindTexture(textureType, textureProperties.glTexture);
    }

    public void setTextureCube(CubeTexture texture, int slot) {
        GLProperties.Fields textureProperties = properties.get(texture);
        if (texture.imageLength == 6) {
            if (texture.version > 0 && textureProperties.version != texture.version) {
                initTexture(textureProperties, texture);
                state.activeTexture(GLES30.GL_TEXTURE0 + slot);
                state.bindTexture(GLES30.GL_TEXTURE_CUBE_MAP, textureProperties.glTexture);
//                GLES30.pixelStorei( GLES30.GL_UNPACK_FLIP_Y_WEBGL, texture.flipY );
                boolean isCompressed = false;
                boolean isDataTexture = false;
                if (texture.textures.length > 0) {
                    isDataTexture = texture.textures[0] instanceof DataTexture;
                }
                Bitmap[] cubeImage = new Bitmap[6];
                for (int i = 0; i < 6; i++) {
                    if (!isCompressed && !isDataTexture) {
                        cubeImage[i] = resizeImage(texture.bitmaps[i], false, capabilities.maxCubemapSize);
                    } else {
                        cubeImage[i] = isDataTexture ? texture.textures[i].image : texture.bitmaps[i];
                    }
                }

                Bitmap image = cubeImage[0];
                boolean supportsMips = isPowerOfTwo( image );
                int glFormat = GLUtils.convert(texture.format);
                int glType = GLUtils.convert(texture.type);
                int glInternalFormat = getInternalFormat(glFormat, glType);

                setTextureParameters( GLES30.GL_TEXTURE_CUBE_MAP, texture, supportsMips );
                for (int i = 0; i < 6; i++) {
                    Bitmap img = cubeImage[i];
                    if (!isCompressed) {
                        android.opengl.GLUtils.texImage2D(GLES30.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, glInternalFormat, image, 0);
                    } else {
                        //for compressed texture, not support
                    }
                }
                if (!isCompressed) {
                    textureProperties.maxMipLevel = 0;
                } else {
                    textureProperties.maxMipLevel = cubeImage.length - 1;
                }

                if (textureNeedsGenerateMipmaps(texture, supportsMips)) {
                    // We assume images for cube map have the same size.
                    generateMipmap( GLES30.GL_TEXTURE_CUBE_MAP, texture, image.getWidth(), image.getHeight() );
                }
                textureProperties.version = texture.version;
            } else {
                state.activeTexture(GLES30.GL_TEXTURE0 + slot);
                state.bindTexture(GLES30.GL_TEXTURE_CUBE_MAP, textureProperties.glTexture);
            }
        }
    }

    public void updateVideoTexture(VideoTexture texture) {
        long id = texture.id;
        int frame = info.frame;
        // Check the last frame we updated the VideoTexture
        if (_videoTextures.get(id) != frame) {
            _videoTextures.put(id, frame);
            texture.update();
        }
    }
    public void uploadTexture(GLProperties.Fields textureProperties, Texture texture, int slot) {
        int textureType = GLES30.GL_TEXTURE_2D;
        if (texture instanceof DataTexture2DArray) {
            textureType = GLES30.GL_TEXTURE_2D_ARRAY;
        } else if (texture instanceof DataTexture3D) {
            textureType = GLES30.GL_TEXTURE_3D;
        }
        initTexture(textureProperties, texture);

        state.activeTexture(GLES30.GL_TEXTURE0 + slot);
        state.bindTexture(textureType, textureProperties.glTexture);

//        GLES30.glPixelStorei( GLES30.UNPACK_FLIP_Y_WEBGL, texture.flipY );
//        GLES30.glPixelStorei( GLES30.GL_UNPACK_PREMULTIPLY_ALPHA_WEBGL, texture.premultiplyAlpha );
        GLES30.glPixelStorei( GLES30.GL_UNPACK_ALIGNMENT, texture.unpackAlignment );

        boolean needsPowerOfTwo = textureNeedsPowerOfTwo( texture ) && !isPowerOfTwo( texture.image );
        Bitmap image = resizeImage(texture.image, needsPowerOfTwo, capabilities.maxTextureSize);

        Boolean supportsMips = true;
        int glFormat = GLUtils.convert(texture.format);
        int glType = GLUtils.convert(texture.type);
        int glInternalFormat = getInternalFormat(glFormat, glType);

        setTextureParameters( GLES30.GL_TEXTURE_CUBE_MAP, texture, supportsMips );

        Texture.Mipmap mipmap;
        ArrayList<Texture.Mipmap> mipmaps = texture.mipmaps;
        if (texture instanceof DepthTexture) {
            // populate depth texture with dummy data
            glInternalFormat = GLES30.GL_DEPTH_COMPONENT;

            if ( texture.type == FloatType ) {
                glInternalFormat = GLES30.GL_DEPTH_COMPONENT32F;
            } else {
                // WebGL 2.0 requires signed internalformat for glTexImage2D
                glInternalFormat = GLES30.GL_DEPTH_COMPONENT16;
            }

            if ( texture.format == DepthFormat && glInternalFormat == GLES30.GL_DEPTH_COMPONENT ) {

                // The error INVALID_OPERATION is generated by texImage2D if format and internalformat are
                // DEPTH_COMPONENT and type is not UNSIGNED_SHORT or UNSIGNED_INT
                // (https://www.khronos.org/registry/webgl/extensions/WEBGL_depth_texture/)
                if ( texture.type != UnsignedShortType && texture.type != UnsignedIntType ) {

//                    console.warn( 'THREE.WebGLRenderer: Use UnsignedShortType or UnsignedIntType for DepthFormat DepthTexture.' );

                    texture.type = UnsignedShortType;
                    glType = GLUtils.convert( texture.type );

                }

            }

            // Depth stencil textures need the DEPTH_STENCIL internal format
            // (https://www.khronos.org/registry/webgl/extensions/WEBGL_depth_texture/)
            if ( texture.format == DepthStencilFormat ) {

                glInternalFormat = GLES30.GL_DEPTH_STENCIL;

                // The error INVALID_OPERATION is generated by texImage2D if format and internalformat are
                // DEPTH_STENCIL and type is not UNSIGNED_INT_24_8_WEBGL.
                // (https://www.khronos.org/registry/webgl/extensions/WEBGL_depth_texture/)
                if ( texture.type != UnsignedInt248Type ) {

//                    console.warn( 'THREE.WebGLRenderer: Use UnsignedInt248Type for DepthStencilFormat DepthTexture.' );

                    texture.type = UnsignedInt248Type;
                    glType = GLUtils.convert( texture.type );

                }

            }
            GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, glInternalFormat, image.getWidth(), image.getHeight(), 0, glFormat, glType, null);

//        } else if (texture instanceof DataTexture) {
//            if (mipmaps.size() > 0 &&supportsMips) {
//                for (int i = 0; i < mipmaps.size(); i++) {
//                    mipmap = mipmaps.get(i);
//                    GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, i, glInternalFormat, mipmap.width, mipmap.height, 0, glFormat, glType, mipmap.data);
//                }
//                texture.generateMipmaps = false;
//                textureProperties.maxMipLevel = mipmaps.size() - 1;
//            } else {
//                android.opengl.GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, glInternalFormat, image, 0);
//                textureProperties.maxMipLevel = 0;
//            }
        } else if (texture instanceof CompressedTexture) {
            for (int i = 0; i < mipmaps.size(); i++) {
                mipmap = mipmaps.get(i);
                //not support now
                GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, i, glInternalFormat, mipmap.width, mipmap.height,
                        0, glFormat, glType, mipmap.data);
            }
        } else if (texture instanceof DataTexture2DArray) {
            android.opengl.GLUtils.texImage2D(GLES30.GL_TEXTURE_2D_ARRAY, 0, glInternalFormat, image, 0);
            textureProperties.maxMipLevel = 0;
        } else if (texture instanceof DataTexture3D) {
            android.opengl.GLUtils.texImage2D(GLES30.GL_TEXTURE_3D, 0, glInternalFormat, image, 0);
            textureProperties.maxMipLevel = 0;
        } else {
            // regular Texture (image, video, canvas) or DataTexture
            if (mipmaps.size() > 0 && supportsMips) {
                for (int i = 0; i < mipmaps.size(); i++) {
                    mipmap = mipmaps.get(i);
                    GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, i, glInternalFormat, mipmap.width, mipmap.height, 0, glFormat, glType, mipmap.data);
                }
                texture.generateMipmaps = false;
                textureProperties.maxMipLevel = mipmaps.size() - 1;
            } else {
                android.opengl.GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, glInternalFormat, image, 0);
                textureProperties.maxMipLevel = 0;
            }
        }
        if ( textureNeedsGenerateMipmaps( texture, supportsMips ) ) {
            generateMipmap( GLES30.GL_TEXTURE_2D, texture, image.getWidth(), image.getHeight() );
        }

        textureProperties.version = texture.version;
    }

    // Setup storage for target texture and bind it to correct framebuffer
    public void setupFrameBufferTexture(int framebuffer, GLRenderTarget renderTarget, int attachment, int textureTarget) {
        int glFormat = GLUtils.convert(renderTarget.texture.format);
        int glType = GLUtils.convert(renderTarget.texture.type);
        int glInternalFormat = getInternalFormat(glFormat, glType);
        GLES30.glTexImage2D(textureTarget, 0, glInternalFormat, renderTarget.width, renderTarget.height, 0, glFormat, glType, null);
        GLES30.glBindFramebuffer( GLES30.GL_FRAMEBUFFER, framebuffer );
        GLES30.glFramebufferTexture2D( GLES30.GL_FRAMEBUFFER, attachment, textureTarget, properties.get( renderTarget.texture ).glTexture, 0 );
        GLES30.glBindFramebuffer( GLES30.GL_FRAMEBUFFER, 0 );
    }

    public int getRenderTargetSamples(GLRenderTarget renderTarget) {
        if (renderTarget instanceof GLMultisampleRenderTarget) {
            return Math.min(capabilities.maxSamples, renderTarget.samples());
        }
        return 0;
    }
    // Setup storage for internal depth/stencil buffers and bind to correct framebuffer
    public void setupRenderBufferStorage(int renderbuffer, GLRenderTarget renderTarget, boolean isMultisample) {
        GLES30.glBindRenderbuffer(GLES30.GL_RENDERBUFFER, renderbuffer);
        if (renderTarget.depthBuffer && !renderTarget.stencilBuffer) {
            if (isMultisample) {
                int samples = getRenderTargetSamples(renderTarget);
                GLES30.glRenderbufferStorageMultisample(GLES30.GL_RENDERBUFFER, samples, GLES30.GL_DEPTH_COMPONENT16, renderTarget.width, renderTarget.height);
            } else {
                GLES30.glRenderbufferStorage(GLES30.GL_RENDERBUFFER, GLES30.GL_DEPTH_COMPONENT16, renderTarget.width, renderTarget.height);
            }
            GLES30.glFramebufferRenderbuffer( GLES30.GL_FRAMEBUFFER, GLES30.GL_DEPTH_ATTACHMENT, GLES30.GL_RENDERBUFFER, renderbuffer );
        } else if (renderTarget.depthBuffer &&renderTarget.stencilBuffer) {
            if (isMultisample) {
                int samples = getRenderTargetSamples( renderTarget );

                GLES30.glRenderbufferStorageMultisample( GLES30.GL_RENDERBUFFER, samples, GLES30.GL_DEPTH24_STENCIL8, renderTarget.width, renderTarget.height );

            } else {

//                GLES30.glRenderbufferStorage( GLES30.GL_RENDERBUFFER, GLES30.GL_DEPTH_STENCIL, renderTarget.width, renderTarget.height );
                GLES30.glRenderbufferStorage( GLES30.GL_RENDERBUFFER, GLES30.GL_DEPTH_COMPONENT16, renderTarget.width, renderTarget.height );

            }


//            GLES30.glFramebufferRenderbuffer( GLES30.GL_FRAMEBUFFER, GLES30.GL_DEPTH_STENCIL_ATTACHMENT, GLES30.GL_RENDERBUFFER, renderbuffer );
            GLES30.glFramebufferRenderbuffer( GLES30.GL_FRAMEBUFFER, GLES30.GL_DEPTH_ATTACHMENT, GLES30.GL_RENDERBUFFER, renderbuffer );

        } else {

            int glFormat = GLUtils.convert( renderTarget.texture.format );
            int glType = GLUtils.convert( renderTarget.texture.type );
            int glInternalFormat = getInternalFormat( glFormat, glType );

            if ( isMultisample ) {

                int samples = getRenderTargetSamples( renderTarget );

                GLES30.glRenderbufferStorageMultisample( GLES30.GL_RENDERBUFFER, samples, glInternalFormat, renderTarget.width, renderTarget.height );

            } else {

                GLES30.glRenderbufferStorage( GLES30.GL_RENDERBUFFER, glInternalFormat, renderTarget.width, renderTarget.height );

            }

        }

        GLES30.glBindRenderbuffer( GLES30.GL_RENDERBUFFER, 0 );
    }

    // Setup resources for a Depth Texture for a FBO
    public void setupDepthTexture(int framebuffer, GLRenderTarget renderTarget) {
        boolean isCube = (renderTarget != null && (renderTarget instanceof GLRenderTargetCube));
        if (isCube) {
            Log.e(TAG, "Depth Texture with cube render targets is not supported");
            return;
        }
        GLES30.glBindFramebuffer( GLES30.GL_FRAMEBUFFER, framebuffer );
        if (!(renderTarget.depthTexture instanceof DepthTexture)) {
            Log.e(TAG, "renderTarget.depthTexture must be an instance of THREE.DepthTexture");
            return;
        }
        // upload an empty depth texture with framebuffer size
        if (properties.get(renderTarget.depthTexture).glTexture == null ||
                renderTarget.depthTexture.imgWidth != renderTarget.width ||
                renderTarget.depthTexture.imgHeight != renderTarget.height) {
            renderTarget.depthTexture.imgWidth = renderTarget.width;
            renderTarget.depthTexture.imgHeight = renderTarget.height;
            renderTarget.depthTexture.setNeedsUpdate(true);
        }
        setTexture2D( renderTarget.depthTexture, 0 );
        int glDepthTexture = properties.get(renderTarget.depthTexture).glTexture;
        if (renderTarget.depthTexture.format == DepthFormat) {
            GLES30.glFramebufferTexture2D( GLES30.GL_FRAMEBUFFER, GLES30.GL_DEPTH_ATTACHMENT, GLES30.GL_TEXTURE_2D, glDepthTexture, 0 );

        } else if ( renderTarget.depthTexture.format == DepthStencilFormat ) {

            GLES30.glFramebufferTexture2D( GLES30.GL_FRAMEBUFFER, GLES30.GL_DEPTH_STENCIL_ATTACHMENT, GLES30.GL_TEXTURE_2D, glDepthTexture, 0 );

        } else {
            Log.e(TAG, "Unknown depthTexture format");
        }
    }

    // Setup GL resources for a non-texture depth buffer
    public void setupDepthRenderbuffer(GLRenderTarget renderTarget) {
        GLProperties.Fields renderTargetProperties = properties.get(renderTarget);
        boolean isCube = renderTarget instanceof GLRenderTargetCube;
        if (renderTarget.depthTexture != null) {
            if (isCube) {
                Log.e(TAG, "target.depthTexture not supported in Cube render targets");
                return;
            } else {
                setupDepthTexture(renderTargetProperties.glFramebuffer[0], renderTarget);
            }
        } else {
            if (isCube) {
                renderTargetProperties.glDepthbuffer = new int[6];
                for (int i = 0; i < 6; i++) {
                    GLES30.glBindFramebuffer( GLES30.GL_FRAMEBUFFER, renderTargetProperties.glDepthbuffer[ i ] );
                    int[] temp = new int[1];
                    GLES30.glGenRenderbuffers(1, temp, 0);
                    renderTargetProperties.glDepthbuffer[ i ] = temp[0];
                    setupRenderBufferStorage( renderTargetProperties.glDepthbuffer[ i ], renderTarget, false );
                }
            } else {
                GLES30.glBindFramebuffer( GLES30.GL_FRAMEBUFFER, renderTargetProperties.glFramebuffer[0] );
                int[] temp = new int[1];
                GLES30.glGenRenderbuffers(1, temp, 0);
                renderTargetProperties.glDepthbuffer = temp;
                setupRenderBufferStorage( renderTargetProperties.glDepthbuffer[ 0 ], renderTarget, false );
            }
        }
        GLES30.glBindFramebuffer( GLES30.GL_FRAMEBUFFER, 0 );
    }

    // assumed: texture property of THREE.GLRenderTargetCube
    public void setTextureCubeDynamic(Texture texture, int slot) {
        state.activeTexture(GLES30.GL_TEXTURE0 + slot);
        state.bindTexture(GLES30.GL_TEXTURE_CUBE_MAP, properties.get(texture).glTexture);
    }

    private void setTextureParameters(int textureType, Texture texture, boolean supportsMips) {
        if (supportsMips) {
            GLES30.glTexParameteri(textureType, GLES30.GL_TEXTURE_WRAP_S, GLUtils.convert(texture.wrapS));
            GLES30.glTexParameteri(textureType, GLES30.GL_TEXTURE_WRAP_T, GLUtils.convert(texture.wrapT));

            if ( textureType == GLES30.GL_TEXTURE_3D || textureType == GLES30.GL_TEXTURE_2D_ARRAY ) {

                GLES30.glTexParameteri( textureType, GLES30.GL_TEXTURE_WRAP_R, GLUtils.convert( texture.wrapR ) );

            }

            GLES30.glTexParameteri( textureType, GLES30.GL_TEXTURE_MAG_FILTER, GLUtils.convert( texture.magFilter ) );
            GLES30.glTexParameteri( textureType, GLES30.GL_TEXTURE_MIN_FILTER, GLUtils.convert( texture.minFilter ) );
        } else {
            GLES30.glTexParameteri( textureType, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE );
            GLES30.glTexParameteri( textureType, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE );

            if ( textureType == GLES30.GL_TEXTURE_3D || textureType == GLES30.GL_TEXTURE_2D_ARRAY ) {

                GLES30.glTexParameteri( textureType, GLES30.GL_TEXTURE_WRAP_R, GLES30.GL_CLAMP_TO_EDGE );

            }

            if ( texture.wrapS != ClampToEdgeWrapping || texture.wrapT != ClampToEdgeWrapping ) {
                Log.w(getClass().getSimpleName(), "Texture is not power of two. Texture.wrapS and Texture.wrapT should be set to THREE.ClampToEdgeWrapping.");
            }

            GLES30.glTexParameteri( textureType, GLES30.GL_TEXTURE_MAG_FILTER, filterFallback( texture.magFilter ) );
            GLES30.glTexParameteri( textureType, GLES30.GL_TEXTURE_MIN_FILTER, filterFallback( texture.minFilter ) );
            if ( texture.minFilter != NearestFilter && texture.minFilter != LinearFilter ) {
                Log.w(getClass().getSimpleName(), "Texture is not power of two. Texture.minFilter should be set to THREE.NearestFilter or THREE.LinearFilter.");
            }
        }
    }

    // Set up GL resources for the render target
    public void setupRenderTarget(GLRenderTarget renderTarget) {
        GLProperties.Fields renderTargetProperties = properties.get(renderTarget);
        GLProperties.Fields textureProperties = properties.get(renderTarget.texture);
        renderTarget.addEventListener( "dispose", onRenderTargetDispose );

        int[] temp = new int[1];
        GLES30.glGenTextures(1, temp, 0);
        textureProperties.glTexture = temp[0];
        info.textures++;
        boolean isCube = renderTarget instanceof GLRenderTargetCube;
        boolean isMultisample = renderTarget instanceof GLMultisampleRenderTarget;
        boolean supportsMips = isPowerOfTwo( renderTarget.width, renderTarget.height );

        // Setup framebuffer
        if (isCube) {
            renderTargetProperties.glFramebuffer = new int[6];
            for (int i = 0; i < 6; i++) {
                temp = new int[1];
                GLES30.glGenFramebuffers(1, temp, 0);
                renderTargetProperties.glFramebuffer[i] = temp[0];
            }
        } else {
            renderTargetProperties.glFramebuffer = new int[1];
            GLES30.glGenFramebuffers(1, renderTargetProperties.glFramebuffer, 0);
            if (isMultisample) {
                renderTargetProperties.glMultisampledFramebuffer = new int[1];
                GLES30.glGenFramebuffers(1, renderTargetProperties.glMultisampledFramebuffer, 0);
                renderTargetProperties.glColorRenderbuffer = new int[1];
                GLES30.glGenFramebuffers(1, renderTargetProperties.glColorRenderbuffer, 0);

                GLES30.glBindRenderbuffer( GLES30.GL_RENDERBUFFER, renderTargetProperties.glColorRenderbuffer[0] );
                int glFormat = GLUtils.convert( renderTarget.texture.format );
                int glType = GLUtils.convert( renderTarget.texture.type );
                int glInternalFormat = getInternalFormat( glFormat, glType );
                int samples = getRenderTargetSamples( renderTarget );
                GLES30.glRenderbufferStorageMultisample( GLES30.GL_RENDERBUFFER, samples, glInternalFormat, renderTarget.width, renderTarget.height );

                GLES30.glBindFramebuffer( GLES30.GL_FRAMEBUFFER, renderTargetProperties.glMultisampledFramebuffer[0] );
                GLES30.glFramebufferRenderbuffer( GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0, GLES30.GL_RENDERBUFFER, renderTargetProperties.glColorRenderbuffer[0] );
                GLES30.glBindRenderbuffer( GLES30.GL_RENDERBUFFER, 0 );

                if ( renderTarget.depthBuffer ) {

                    renderTargetProperties.glDepthRenderbuffer = new int[1];
                    GLES30.glGenRenderbuffers(1, renderTargetProperties.glDepthRenderbuffer, 0);
                    setupRenderBufferStorage( renderTargetProperties.glDepthRenderbuffer[0], renderTarget, true );

                }

                GLES30.glBindFramebuffer( GLES30.GL_FRAMEBUFFER, 0 );
            }
            // Setup color buffer
            if (isCube) {
                state.bindTexture(GLES30.GL_TEXTURE_CUBE_MAP, textureProperties.glTexture);
                setTextureParameters(GLES30.GL_TEXTURE_CUBE_MAP, renderTarget.texture, supportsMips );
                for (int i = 0; i < 6; i++) {
                    setupFrameBufferTexture( renderTargetProperties.glFramebuffer[ i ], renderTarget, GLES30.GL_COLOR_ATTACHMENT0, GLES30.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i );
                }
                if ( textureNeedsGenerateMipmaps( renderTarget.texture, supportsMips ) ) {

                    generateMipmap( GLES30.GL_TEXTURE_CUBE_MAP, renderTarget.texture, renderTarget.width, renderTarget.height );

                }

                state.bindTexture( GLES30.GL_TEXTURE_CUBE_MAP, 0 );
            } else {
                state.bindTexture( GLES30.GL_TEXTURE_2D, textureProperties.glTexture );
                setTextureParameters( GLES30.GL_TEXTURE_2D, renderTarget.texture, supportsMips );
                setupFrameBufferTexture( renderTargetProperties.glFramebuffer[0], renderTarget, GLES30.GL_COLOR_ATTACHMENT0, GLES30.GL_TEXTURE_2D );

                if ( textureNeedsGenerateMipmaps( renderTarget.texture, supportsMips ) ) {

                    generateMipmap( GLES30.GL_TEXTURE_2D, renderTarget.texture, renderTarget.width, renderTarget.height );

                }

                state.bindTexture( GLES30.GL_TEXTURE_2D, 0 );
            }
            // Setup depth and stencil buffers
            if ( renderTarget.depthBuffer ) {
                setupDepthRenderbuffer( renderTarget );
            }
        }
    }

    public void updateRenderTargetMipmap(GLRenderTarget renderTarget) {
        Texture texture = renderTarget.texture;
        boolean supportsMips = isPowerOfTwo( renderTarget.width, renderTarget.height );
        if ( textureNeedsGenerateMipmaps( texture, supportsMips ) ) {

            int target = renderTarget instanceof GLRenderTargetCube ? GLES30.GL_TEXTURE_CUBE_MAP : GLES30.GL_TEXTURE_2D;
            Integer glTexture = properties.get( texture ).glTexture;

            state.bindTexture( target, glTexture );
            generateMipmap( target, texture, renderTarget.width, renderTarget.height );
            state.bindTexture( target, 0 );

        }
    }

    public void updateMultisampleRenderTarget(GLRenderTarget renderTarget) {
        GLProperties.Fields renderTargetProperties = properties.get( renderTarget );
        if (renderTargetProperties.glMultisampledFramebuffer != null) {
            GLES30.glBindFramebuffer( GLES30.GL_READ_FRAMEBUFFER, renderTargetProperties.glMultisampledFramebuffer[0] );
        }
        GLES30.glBindFramebuffer( GLES30.GL_DRAW_FRAMEBUFFER, renderTargetProperties.glFramebuffer[0] );

        int width = renderTarget.width;
        int height = renderTarget.height;
        int mask = GLES30.GL_COLOR_BUFFER_BIT;

        if ( renderTarget.depthBuffer ) mask |= GLES30.GL_DEPTH_BUFFER_BIT;
        if ( renderTarget.stencilBuffer ) mask |= GLES30.GL_STENCIL_BUFFER_BIT;

        GLES30.glBlitFramebuffer( 0, 0, width, height, 0, 0, width, height, mask, GLES30.GL_NEAREST );
    }
}
