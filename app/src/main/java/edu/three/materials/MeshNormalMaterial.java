package edu.three.materials;

import edu.three.constant.Constants;
import edu.three.math.Vector2;
import edu.three.textures.Texture;

public class MeshNormalMaterial extends Material {
	//bumpMap
	public float bumpScale = 1;

	//normalMap
	//normalMapType;
	public Vector2 normalScale = new Vector2( 1, 1 );

	//displacementMap
	public float displacementScale = 1;
	public float displacementBias = 0;

//	public boolean wireframe = false;
//	public int wireframeLinewidth = 1;

	//skinning
//	public boolean fog = false;
//	public boolean lights = false;

	public MeshNormalMaterial() {
		normalMapType = Constants.TangentSpaceNormalMap;
		fog = false;
		lights = false;
		wireframe = false;
		wireframeLinewidth = 1;
	}

}
