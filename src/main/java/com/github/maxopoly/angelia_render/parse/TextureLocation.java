package com.github.maxopoly.angelia_render.parse;

public class TextureLocation {

	private final float u;
	private final float length;
	private final float v;
	private final float height;
	private final int textureID;
	private float [] textureCoordArray;

	public TextureLocation(int textureID, float u, float v, float length, float height) {
		this.u = u;
		this.v = v;
		this.length = length;
		this.height = height;
		this.textureID = textureID;
		this.textureCoordArray = new float[] {
				// starting top left, going counter clock wise
				u, v, /* p0 */ u, v + height, /* p1 */ u + length, v + height, /* p2 */ u + height, v};
	}

	public float getULowerBound() {
		return u;
	}

	public float getUUpperBound() {
		return u + length;
	}

	public float getVLowerBound() {
		return v;
	}

	public float getVUpperBound() {
		return v + height;
	}

	public int getTextureID() {
		return textureID;
	}

	public float[] getUVCoordsArray() {
		return textureCoordArray;
	}

}
