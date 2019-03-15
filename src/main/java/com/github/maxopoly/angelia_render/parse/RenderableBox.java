package com.github.maxopoly.angelia_render.parse;

import java.nio.FloatBuffer;
import java.util.LinkedList;
import java.util.List;

import com.github.maxopoly.angeliacore.model.location.BlockFace;

public class RenderableBox {

	public static final BlockFace[] renderableSides = new BlockFace[BlockFace.values().length - 1];

	static {
		// filter out SPECIAL, which is a model weirdness and has no place in rendering
		for (int i = 0; i < renderableSides.length; i++) {
			renderableSides[i] = BlockFace.values()[i];
		}
	}

	private float[][] coords;
	private float[][] textureCoords;
	private BlockFace[] cullSides;

	public RenderableBox(float lowerX, float lowerY, float lowerZ, float upperX, float upperY, float upperZ,
			BlockFace[] cullFaces, TextureLocation[] textures) {
		coords = new float[renderableSides.length][];
		textureCoords = new float[renderableSides.length][];
		// bottom
		if (textures[BlockFace.BOTTOM.ordinal()] != null) {
			coords[BlockFace.BOTTOM.ordinal()] = new float[] { upperX, lowerY, upperZ, /* (1,0,1) */ upperX, lowerY,
					lowerZ, /* (1,0,0) */lowerX, lowerY, lowerZ, /* (0,0,0) */ lowerX, lowerY, upperZ, /* (0,0,1) */ };
			textureCoords[BlockFace.BOTTOM.ordinal()] = textures[BlockFace.BOTTOM.ordinal()].getUVCoordsArray();
		}
		// top
		if (textures[BlockFace.TOP.ordinal()] != null) {
			coords[BlockFace.TOP.ordinal()] = new float[] { lowerX, lowerY, lowerZ, /* (0,0,0) */ lowerX, lowerY,
					upperZ, /* (0,0,1) */lowerX, lowerY, lowerZ, /* (0,0,0) */ upperX, lowerY, lowerZ, /* (1,0,0) */ };
			textureCoords[BlockFace.TOP.ordinal()] = textures[BlockFace.TOP.ordinal()].getUVCoordsArray();
		}
		// north
		if (textures[BlockFace.NORTH.ordinal()] != null) {
			coords[BlockFace.NORTH.ordinal()] = new float[] { upperX, upperY, lowerZ, /* (1,1,0) */ upperX, lowerY,
					lowerZ, /* (1,0,0) */lowerX, lowerY, lowerZ, /* (0,0,0) */ lowerX, upperY, lowerZ, /* (0,1,0) */ };
			textureCoords[BlockFace.NORTH.ordinal()] = textures[BlockFace.NORTH.ordinal()].getUVCoordsArray();
		}
		// south
		if (textures[BlockFace.SOUTH.ordinal()] != null) {
			coords[BlockFace.SOUTH.ordinal()] = new float[] { lowerX, upperY, upperZ, /* (0,1,1) */ lowerX, lowerY,
					upperZ, /* (0,0,1) */upperX, lowerY, upperZ, /* (1,0,1) */ upperX, upperY, upperZ, /* (1,1,1) */ };
			textureCoords[BlockFace.SOUTH.ordinal()] = textures[BlockFace.SOUTH.ordinal()].getUVCoordsArray();
		}
		// east
		if (textures[BlockFace.EAST.ordinal()] != null) {
			coords[BlockFace.EAST.ordinal()] = new float[] { upperX, upperY, upperZ, /* (1,1,1) */ upperX, lowerY,
					upperZ, /* (1,0,1) */upperX, lowerY, lowerZ, /* (1,0,0) */ upperX, upperY, lowerZ, /* (1,1,0) */ };
			textureCoords[BlockFace.EAST.ordinal()] = textures[BlockFace.EAST.ordinal()].getUVCoordsArray();
		}
		// west
		if (textures[BlockFace.WEST.ordinal()] != null) {
			coords[BlockFace.WEST.ordinal()] = new float[] { lowerX, upperY, lowerZ, /* (0,1,0) */ lowerX, lowerY,
					lowerZ, /* (0,0,0) */lowerX, lowerY, upperZ, /* (0,0,1) */ lowerX, upperY, upperZ, /* (0,1,1) */ };
			textureCoords[BlockFace.WEST.ordinal()] = textures[BlockFace.WEST.ordinal()].getUVCoordsArray();
		}
		compressArrays();
	}

	public void render(List <float []> coordsToAdd, List <float []> texCoordsToAdd, boolean[] cullFaces, int [] floatCounter) {
		for (int i = 0; i < this.coords.length; i++) {
			if ((cullSides[i] != null && cullFaces[cullSides[i].ordinal()]) || cullSides[i] == null) {
				coordsToAdd.add(this.coords[i]);
				texCoordsToAdd.add(this.textureCoords[i]);
				floatCounter [0] += this.coords [i].length;
			}
		}
	}

	private void compressArrays() {
		// TODO combine different sides with the same cull side? Is that ever used?
		List<float[]> coordsReplacement = new LinkedList<>();
		List<float[]> textureCoordsReplacement = new LinkedList<>();
		List<BlockFace> cullFaceReplacement = new LinkedList<>();
		int cullFaceIndIndex = -1;
		for (int i = 0; i < coords.length; i++) {
			if (coords[i] == null) {
				continue;
			}
			if (cullSides[i] == null) {
				if (cullFaceIndIndex == -1) {
					cullFaceIndIndex = i;
				} else {
					coordsReplacement.set(cullFaceIndIndex,
							combineArrays(coordsReplacement.get(cullFaceIndIndex), coords[cullFaceIndIndex]));
					textureCoordsReplacement.set(cullFaceIndIndex, combineArrays(
							textureCoordsReplacement.get(cullFaceIndIndex), textureCoords[cullFaceIndIndex]));
					continue;
				}
			}
			coordsReplacement.add(coords[i]);
			textureCoordsReplacement.add(textureCoords[i]);
			cullFaceReplacement.add(cullSides[i]);
		}
		this.coords = (float[][]) coordsReplacement.toArray();
		this.textureCoords = (float[][]) textureCoordsReplacement.toArray();
		this.cullSides = (BlockFace[]) cullFaceReplacement.toArray();
	}

	private float[] combineArrays(float[] a, float[] b) {
		final float[] result = new float[a.length + b.length];
		System.arraycopy(a, 0, result, 0, a.length);
		System.arraycopy(b, 0, result, a.length, b.length);
		return result;
	}

}
