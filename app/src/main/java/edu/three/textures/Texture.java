package edu.three.textures;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.nio.Buffer;
import java.util.ArrayList;
import java.util.UUID;

import edu.three.core.Event;
import edu.three.core.EventDispatcher;
import edu.three.math.Matrix3;
import edu.three.math.Vector2;

import static edu.three.constant.Constants.MirroredRepeatWrapping;
import static edu.three.constant.Constants.ClampToEdgeWrapping;
import static edu.three.constant.Constants.RepeatWrapping;
import static edu.three.constant.Constants.LinearEncoding;
import static edu.three.constant.Constants.UnsignedByteType;
import static edu.three.constant.Constants.RGBAFormat;
import static edu.three.constant.Constants.LinearMipMapLinearFilter;
import static edu.three.constant.Constants.LinearFilter;
import static edu.three.constant.Constants.UVMapping;

public class Texture extends EventDispatcher {
    static long textureId = 0;
    static int DEFAULT_MAPPING = UVMapping;
    public Bitmap image;
    public int imgWidth;
    public int imgHeight;
    public long id;
    public String uuid;
    public String name = "";
    public int mapping;
    public int wrapS;
    public int wrapT;
    public int wrapR;
    public int magFilter;
    public int minFilter;
    public int anisotropy;
    public int format;
    public int encoding;
    public int type = -1;
    public int version = 0;
    public boolean generateMipmaps = false;
    public boolean premultiplyAlpha = false;
    public boolean flipY = true;
    // valid values: 1, 2, 4, 8 (see http://www.khronos.org/opengles/sdk/docs/man/xhtml/glPixelStorei.xml)
    public int unpackAlignment = 4;
    public boolean matrixAutoUpdate = true;
    public Vector2 offset;
    public Vector2 repeat;
    public Vector2 center;
    float rotation = 0;
    public ArrayList<Mipmap> mipmaps = new ArrayList<>();

	protected Matrix3 matrix = new Matrix3();

	public Texture() {
        this(null, null, new Option());
    }
	public Texture(Bitmap image, Integer mapping, Option option) {
	    id = textureId++;
	    uuid = UUID.randomUUID().toString();
	    this.image = image;

        this.mapping = mapping != null ? mapping : DEFAULT_MAPPING;
        wrapS = option.wrapS != null ? option.wrapS : ClampToEdgeWrapping;
        wrapT = option.wrapT != null ? option.wrapT : ClampToEdgeWrapping;
        magFilter = option.magFilter != null ? option.magFilter : LinearFilter;
        minFilter = option.minFilter != null ? option.minFilter : LinearMipMapLinearFilter;
        generateMipmaps = option.generateMipmaps != null ? option.generateMipmaps : true;

        anisotropy = option.anisotropy != null ? option.anisotropy : 1;
        format = option.format != null ? option.format : RGBAFormat;
        type = option.type != null ? option.type : UnsignedByteType;

        offset = new Vector2(0, 0);
        repeat = new Vector2(1, 1);
        center = new Vector2(0, 0);

        // Values of encoding !== THREE.LinearEncoding only supported on map, envMap and emissiveMap.
        //
        // Also changing the encoding after already used by a Material will not automatically make the Material
        // update. You need to explicitly call Material.needsUpdate to trigger it to recompile.
        encoding = option.encoding != null ? option.encoding : LinearEncoding;
        version = 0;
    }

    public void setImage(Bitmap bitmap) {
	    image = bitmap;
	    imgWidth = bitmap.getWidth();
	    imgHeight = bitmap.getHeight();
	    setNeedsUpdate(true);
    }

    public void setNeedsUpdate(boolean value) {
	    if (value) {
	        version++;
        }
    }

    public int getVersion() {
	    return version;
    }

	public void updateMatrix() {
        matrix.setUvTransform( offset.x, offset.y, repeat.x, repeat.y, rotation, center.x, center.y );
    }

    public Matrix3 getMatrix() {
	    return matrix;
    }

    public void dispose() {
	    dispatchEvent(new Event("dispose"));
    }

    public Texture copy(Texture source) {
        name = source.name;

        image = source.image;
        for (Mipmap item : source.mipmaps) {
            mipmaps.add(item);
        }

        mapping = source.mapping;

        wrapS = source.wrapS;
        wrapT = source.wrapT;

        magFilter = source.magFilter;
        minFilter = source.minFilter;

        anisotropy = source.anisotropy;

        format = source.format;
        type = source.type;

        offset.copy( source.offset );
        repeat.copy( source.repeat );
        center.copy( source.center );
        rotation = source.rotation;

        matrixAutoUpdate = source.matrixAutoUpdate;
        matrix.copy( source.matrix );

        generateMipmaps = source.generateMipmaps;
        premultiplyAlpha = source.premultiplyAlpha;
        flipY = source.flipY;
        unpackAlignment = source.unpackAlignment;
        encoding = source.encoding;

        return this;
    }

    public Texture clone() {
	    return new Texture().copy(this);
    }

    public boolean equal(Texture texture) {
        return false;
    }

    public static class Option {
        public Integer wrapS;
        public Integer wrapT;
        public Integer magFilter;
        public Integer minFilter;
        public Integer format;
        public Integer type;
        public Integer anisotropy;
        public Integer encoding;
        public Boolean generateMipmaps;
	    public Boolean depthBuffer;
        public Boolean stencilBuffer;
        public Texture depthTexture;
    }

    public static class Mipmap {
	    public int width;
        public int height;
        public Buffer data;  //pixels
    }
}
