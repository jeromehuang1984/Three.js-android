package edu.three.renderers.gl;

import android.opengl.GLES30;

import static edu.three.constant.Constants.MaxEquation;
import static edu.three.constant.Constants.MinEquation;
import static edu.three.constant.Constants.RGBA_ASTC_4x4_Format;
import static edu.three.constant.Constants.RGBA_ASTC_5x4_Format;
import static edu.three.constant.Constants.RGBA_ASTC_5x5_Format;
import static edu.three.constant.Constants.RGBA_ASTC_6x5_Format;
import static edu.three.constant.Constants.RGBA_ASTC_6x6_Format;
import static edu.three.constant.Constants.RGBA_ASTC_8x5_Format;
import static edu.three.constant.Constants.RGBA_ASTC_8x6_Format;
import static edu.three.constant.Constants.RGBA_ASTC_8x8_Format;
import static edu.three.constant.Constants.RGBA_ASTC_10x5_Format;
import static edu.three.constant.Constants.RGBA_ASTC_10x6_Format;
import static edu.three.constant.Constants.RGBA_ASTC_10x8_Format;
import static edu.three.constant.Constants.RGBA_ASTC_10x10_Format;
import static edu.three.constant.Constants.RGBA_ASTC_12x10_Format;
import static edu.three.constant.Constants.RGBA_ASTC_12x12_Format;
import static edu.three.constant.Constants.RGB_ETC1_Format;
import static edu.three.constant.Constants.RGBA_PVRTC_2BPPV1_Format;
import static edu.three.constant.Constants.RGBA_PVRTC_4BPPV1_Format;
import static edu.three.constant.Constants.RGB_PVRTC_2BPPV1_Format;
import static edu.three.constant.Constants.RGB_PVRTC_4BPPV1_Format;
import static edu.three.constant.Constants.RGBA_S3TC_DXT5_Format;
import static edu.three.constant.Constants.RGBA_S3TC_DXT3_Format;
import static edu.three.constant.Constants.RGBA_S3TC_DXT1_Format;
import static edu.three.constant.Constants.RGB_S3TC_DXT1_Format;
import static edu.three.constant.Constants.SrcAlphaSaturateFactor;
import static edu.three.constant.Constants.OneMinusDstColorFactor;
import static edu.three.constant.Constants.DstColorFactor;
import static edu.three.constant.Constants.OneMinusDstAlphaFactor;
import static edu.three.constant.Constants.DstAlphaFactor;
import static edu.three.constant.Constants.OneMinusSrcAlphaFactor;
import static edu.three.constant.Constants.SrcAlphaFactor;
import static edu.three.constant.Constants.OneMinusSrcColorFactor;
import static edu.three.constant.Constants.SrcColorFactor;
import static edu.three.constant.Constants.OneFactor;
import static edu.three.constant.Constants.ZeroFactor;
import static edu.three.constant.Constants.ReverseSubtractEquation;
import static edu.three.constant.Constants.SubtractEquation;
import static edu.three.constant.Constants.AddEquation;
import static edu.three.constant.Constants.DepthFormat;
import static edu.three.constant.Constants.DepthStencilFormat;
import static edu.three.constant.Constants.LuminanceAlphaFormat;
import static edu.three.constant.Constants.LuminanceFormat;
import static edu.three.constant.Constants.RedFormat;
import static edu.three.constant.Constants.RGBAFormat;
import static edu.three.constant.Constants.RGBFormat;
import static edu.three.constant.Constants.AlphaFormat;
import static edu.three.constant.Constants.HalfFloatType;
import static edu.three.constant.Constants.FloatType;
import static edu.three.constant.Constants.UnsignedIntType;
import static edu.three.constant.Constants.IntType;
import static edu.three.constant.Constants.UnsignedShortType;
import static edu.three.constant.Constants.ShortType;
import static edu.three.constant.Constants.ByteType;
import static edu.three.constant.Constants.UnsignedInt248Type;
import static edu.three.constant.Constants.UnsignedShort565Type;
import static edu.three.constant.Constants.UnsignedShort5551Type;
import static edu.three.constant.Constants.UnsignedShort4444Type;
import static edu.three.constant.Constants.UnsignedByteType;
import static edu.three.constant.Constants.LinearMipMapLinearFilter;
import static edu.three.constant.Constants.LinearMipMapNearestFilter;
import static edu.three.constant.Constants.LinearFilter;
import static edu.three.constant.Constants.NearestMipMapLinearFilter;
import static edu.three.constant.Constants.NearestMipMapNearestFilter;
import static edu.three.constant.Constants.NearestFilter;
import static edu.three.constant.Constants.MirroredRepeatWrapping;
import static edu.three.constant.Constants.ClampToEdgeWrapping;
import static edu.three.constant.Constants.RepeatWrapping;

public class GLUtils {
    static public int convert(int p) {
        if ( p == RepeatWrapping ) return GLES30.GL_REPEAT;
        if ( p == ClampToEdgeWrapping ) return GLES30.GL_CLAMP_TO_EDGE;
        if ( p == MirroredRepeatWrapping ) return GLES30.GL_MIRRORED_REPEAT;

        if ( p == NearestFilter ) return GLES30.GL_NEAREST;
        if ( p == NearestMipMapNearestFilter ) return GLES30.GL_NEAREST_MIPMAP_NEAREST;
        if ( p == NearestMipMapLinearFilter ) return GLES30.GL_NEAREST_MIPMAP_LINEAR;

        if ( p == LinearFilter ) return GLES30.GL_LINEAR;
        if ( p == LinearMipMapNearestFilter ) return GLES30.GL_LINEAR_MIPMAP_NEAREST;
        if ( p == LinearMipMapLinearFilter ) return GLES30.GL_LINEAR_MIPMAP_LINEAR;

        if ( p == UnsignedByteType ) return GLES30.GL_UNSIGNED_BYTE;
        if ( p == UnsignedShort4444Type ) return GLES30.GL_UNSIGNED_SHORT_4_4_4_4;
        if ( p == UnsignedShort5551Type ) return GLES30.GL_UNSIGNED_SHORT_5_5_5_1;
        if ( p == UnsignedShort565Type ) return GLES30.GL_UNSIGNED_SHORT_5_6_5;

        if ( p == ByteType ) return GLES30.GL_BYTE;
        if ( p == ShortType ) return GLES30.GL_SHORT;
        if ( p == UnsignedShortType ) return GLES30.GL_UNSIGNED_SHORT;
        if ( p == IntType ) return GLES30.GL_INT;
        if ( p == UnsignedIntType ) return GLES30.GL_UNSIGNED_INT;
        if ( p == FloatType ) return GLES30.GL_FLOAT;

        if ( p == HalfFloatType ) {
            return GLES30.GL_HALF_FLOAT;
        }

        if ( p == AlphaFormat ) return GLES30.GL_ALPHA;
        if ( p == RGBFormat ) return GLES30.GL_RGB;
        if ( p == RGBAFormat ) return GLES30.GL_RGBA;
        if ( p == LuminanceFormat ) return GLES30.GL_LUMINANCE;
        if ( p == LuminanceAlphaFormat ) return GLES30.GL_LUMINANCE_ALPHA;
        if ( p == DepthFormat ) return GLES30.GL_DEPTH_COMPONENT;
        if ( p == DepthStencilFormat ) return GLES30.GL_DEPTH_STENCIL;
        if ( p == RedFormat ) return GLES30.GL_RED;

        if ( p == AddEquation ) return GLES30.GL_FUNC_ADD;
        if ( p == SubtractEquation ) return GLES30.GL_FUNC_SUBTRACT;
        if ( p == ReverseSubtractEquation ) return GLES30.GL_FUNC_REVERSE_SUBTRACT;

        if ( p == ZeroFactor ) return GLES30.GL_ZERO;
        if ( p == OneFactor ) return GLES30.GL_ONE;
        if ( p == SrcColorFactor ) return GLES30.GL_SRC_COLOR;
        if ( p == OneMinusSrcColorFactor ) return GLES30.GL_ONE_MINUS_SRC_COLOR;
        if ( p == SrcAlphaFactor ) return GLES30.GL_SRC_ALPHA;
        if ( p == OneMinusSrcAlphaFactor ) return GLES30.GL_ONE_MINUS_SRC_ALPHA;
        if ( p == DstAlphaFactor ) return GLES30.GL_DST_ALPHA;
        if ( p == OneMinusDstAlphaFactor ) return GLES30.GL_ONE_MINUS_DST_ALPHA;

        if ( p == DstColorFactor ) return GLES30.GL_DST_COLOR;
        if ( p == OneMinusDstColorFactor ) return GLES30.GL_ONE_MINUS_DST_COLOR;
        if ( p == SrcAlphaSaturateFactor ) return GLES30.GL_SRC_ALPHA_SATURATE;

        if ( p == RGB_S3TC_DXT1_Format || p == RGBA_S3TC_DXT1_Format ||
                p == RGBA_S3TC_DXT3_Format || p == RGBA_S3TC_DXT5_Format ) {

            return p;

        }

        if ( p == RGB_PVRTC_4BPPV1_Format || p == RGB_PVRTC_2BPPV1_Format ||
                p == RGBA_PVRTC_4BPPV1_Format || p == RGBA_PVRTC_2BPPV1_Format ) {

           return p;

        }

        if ( p == RGB_ETC1_Format ) {

            return p;

        }

        if ( p == RGBA_ASTC_4x4_Format || p == RGBA_ASTC_5x4_Format || p == RGBA_ASTC_5x5_Format ||
                p == RGBA_ASTC_6x5_Format || p == RGBA_ASTC_6x6_Format || p == RGBA_ASTC_8x5_Format ||
                p == RGBA_ASTC_8x6_Format || p == RGBA_ASTC_8x8_Format || p == RGBA_ASTC_10x5_Format ||
                p == RGBA_ASTC_10x6_Format || p == RGBA_ASTC_10x8_Format || p == RGBA_ASTC_10x10_Format ||
                p == RGBA_ASTC_12x10_Format || p == RGBA_ASTC_12x12_Format ) {

            return p;

        }

        if ( p == MinEquation ) return GLES30.GL_MIN;
        if ( p == MaxEquation ) return GLES30.GL_MAX;

        if ( p == UnsignedInt248Type ) {
            return GLES30.GL_UNSIGNED_INT_24_8;
        }

        return 0;
        
    }
}
