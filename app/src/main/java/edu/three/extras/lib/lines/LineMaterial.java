package edu.three.extras.lib.lines;


import edu.three.materials.ShaderMaterial;
import edu.three.math.Color;
import edu.three.math.Vector2;
import edu.three.renderers.shaders.ShaderLib;

public class LineMaterial extends ShaderMaterial {
  ShaderLib lib = ShaderLib.line();
  public boolean dashed = false;

  public LineMaterial() {
    uniforms = lib.uniforms;
    vertexShader = lib.vertexShader;
    fragmentShader = lib.fragmentShader;
    clipping = true;
  }

  public float linewidth() {
    return (float) uniforms.get("linewidth");
  }

  public void setLinewidth(float val) {
    uniforms.put("linewidth", val);
  }

  public Color color() {
    return (Color) uniforms.get("diffuse");
  }

  public void setColor(int color) {
    uniforms.put("diffuse", new Color().setHex(color));
  }

  public float opacity() {
    return  (float) uniforms.get("opacity");
  }
  public void setOpacity(float val) {
    uniforms.put("opacity", val);
  }

  public Vector2 resolution() {
    return (Vector2) uniforms.get("resolution");
  }
  public void setResolution(Vector2 screen) {
    uniforms.put("resolution", screen);
  }

  public void init(LineStyleVO lineStyle) {
    setColor(lineStyle.color);
    setLinewidth(lineStyle.linewidth);
    setOpacity(lineStyle.opacity);
    setResolution(lineStyle.resolution);
  }
}
