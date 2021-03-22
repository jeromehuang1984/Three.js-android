package edu.three.extras.lib.lines;

import edu.three.core.BufferAttribute;
import edu.three.core.InstancedBufferGeometry;
import edu.three.core.InstancedInterleavedBuffer;
import edu.three.core.InterleavedBufferAttribute;

public class LineSegmentsGeometry extends InstancedBufferGeometry {
  float[] positions = new float[] {- 1, 2, 0, 1, 2, 0, - 1, 1, 0, 1, 1, 0, - 1, 0, 0, 1, 0, 0, - 1, - 1, 0, 1, - 1, 0 };
  float[] uvs = new float[] {- 1, 2, 1, 2, - 1, 1, 1, 1, - 1, - 1, 1, - 1, - 1, - 2, 1, - 2};
  int[] index = new int[] {0, 2, 1, 2, 3, 1, 2, 4, 3, 4, 5, 3, 4, 6, 5, 6, 7, 5};

  public LineSegmentsGeometry() {
    setIndex(new BufferAttribute().setArray(index));
    position = new BufferAttribute().setArray(positions).setItemSize(3);
    uv = new BufferAttribute().setArray(uvs).setItemSize(2);
  }

  public LineSegmentsGeometry setPositions(float[] array) {
    InstancedInterleavedBuffer instanceBuffer = new InstancedInterleavedBuffer(array, 6, 1);
    addAttribute("instanceStart", new InterleavedBufferAttribute(instanceBuffer, 3, 0));
    addAttribute("instanceEnd", new InterleavedBufferAttribute(instanceBuffer, 3, 3));

    computeBoundingBox();
    computeBoundingSphere();
    return this;
  }

  public LineSegmentsGeometry setColors(float[] array) {
    InstancedInterleavedBuffer instanceColorBuffer = new InstancedInterleavedBuffer(array, 6, 1);
    addAttribute("instanceColorStart", new InterleavedBufferAttribute(instanceColorBuffer, 3, 0));
    addAttribute("instanceColorEnd", new InterleavedBufferAttribute(instanceColorBuffer, 3, 3));

    return this;
  }
}
