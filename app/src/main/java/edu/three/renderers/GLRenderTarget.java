package edu.three.renderers;

import android.graphics.Bitmap;

import edu.three.core.Event;
import edu.three.core.EventDispatcher;
import edu.three.math.Vector4;
import edu.three.textures.Texture;

/*
 In options, we can specify:
 * Texture parameters for an auto-generated target texture
 * depthBuffer/stencilBuffer: Booleans to indicate if we should generate these buffers
*/
public class GLRenderTarget extends EventDispatcher {
    public Texture texture;
    public int width, height;
    Vector4 viewport;
    Vector4 scissor;
    boolean scissorTest = false;
    public boolean depthBuffer = false;
    public boolean stencilBuffer = false;
    public Texture depthTexture;

    public GLRenderTarget() {}
    public GLRenderTarget(int width, int height, Texture.Option options) {
        this.width = width;
        this.height = height;

        scissor = new Vector4( 0, 0, width, height );
        scissorTest = false;

        viewport = new Vector4( 0, 0, width, height );

        texture = new Texture(null, null, options);
        texture.generateMipmaps = options.generateMipmaps == null ? false : options.generateMipmaps;
        texture.imgWidth = width;
        texture.imgHeight = height;

        depthBuffer = options.depthBuffer != null ? options.depthBuffer : true;
        stencilBuffer = options.stencilBuffer != null ? options.stencilBuffer : true;
        depthTexture = options.depthTexture != null ? options.depthTexture : null;
    }

    public void setSize(int width, int height) {
        if ( this.width != width || this.height != height ) {

            this.width = width;
            this.height = height;

            this.texture.imgWidth = width;
            this.texture.imgHeight = height;

            this.dispose();

        }

        this.viewport.set( 0, 0, width, height );
        this.scissor.set( 0, 0, width, height );
    }

    public GLRenderTarget clone() {
        return new GLRenderTarget().copy(this);
    }

    public GLRenderTarget copy(GLRenderTarget source) {
        width = source.width;
        height = source.height;

        viewport.copy( source.viewport );

        texture = source.texture.clone();

        depthBuffer = source.depthBuffer;
        stencilBuffer = source.stencilBuffer;
        depthTexture = source.depthTexture;

        return this;
    }

    public void dispose() {
        dispatchEvent(new Event("dispose"));
    }

    public int samples() {
        return 0;
    }
}
