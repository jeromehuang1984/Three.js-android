package edu.three.extras.lib.lines;

import edu.three.core.BufferAttribute;
import edu.three.core.BufferGeometry;
import edu.three.core.InstancedInterleavedBuffer;
import edu.three.core.InterleavedBufferAttribute;
import edu.three.materials.Material;
import edu.three.math.Vector3;
import edu.three.objects.Mesh;

public class LineSegments2 extends Mesh {

  Vector3 start = new Vector3();
  Vector3 end = new Vector3();
  public LineSegments2(BufferGeometry geometry, Material material) {
    super(geometry, material);
  }

  public LineSegments2 computeLineDistances() {
    BufferAttribute instanceStart = geometry.getAttribute("instanceStart");
    BufferAttribute instanceEnd = geometry.getAttribute("instanceEnd");
    float[] lineDistances = new float[2 * instanceStart.getCount()];
    for (int i = 0, j = 0, l = instanceStart.getCount(); i < l; i++, j+=2) {
      start.fromBufferAttribute(instanceStart, i);
      end.fromBufferAttribute(instanceEnd, i);
      lineDistances[j] = j == 0 ? 0 : lineDistances[j - 1];
      lineDistances[j + 1] = lineDistances[j] + (float)start.distanceTo(end);
    }
    InstancedInterleavedBuffer instanceDistanceBuffer = new InstancedInterleavedBuffer(lineDistances, 2, 1);
    geometry.addAttribute("instanceDistanceStart", new InterleavedBufferAttribute(instanceDistanceBuffer, 1, 0));
    geometry.addAttribute("instanceDistanceEnd", new InterleavedBufferAttribute(instanceDistanceBuffer, 1, 1));
    return this;
  }
}
