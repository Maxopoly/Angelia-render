package com.github.maxopoly.angelia_render.parse;

public class ModelRotation {

	private float[] multiplicationMatrix;

	public ModelRotation(int x, int y, int z) {
		genMatrix(x, y, z);
	}

	public void apply(float[] coords) {
		for (int i = 0; i < coords.length; i += 3) {
			coords[i] = coords[i] * multiplicationMatrix[0] + coords[i + 1] * multiplicationMatrix[1]
					+ coords[i + 2] * multiplicationMatrix[2];
			coords[i + 1] = coords[i] * multiplicationMatrix[3] + coords[i + 1] * multiplicationMatrix[4]
					+ coords[i + 2] * multiplicationMatrix[5];
			coords[i + 2] = coords[i] * multiplicationMatrix[6] + coords[i + 1] * multiplicationMatrix[7]
					+ coords[i + 2] * multiplicationMatrix[8];
		}

	}

	private void genMatrix(int rotationX, int rotationY, int rotationZ) {
		// fourth component not relevant for us, so only 3x3
		float[] multX = getRotationMatrix(1f, 0f, 0f, simpleSin(rotationX), simpleCos(rotationX));
		float[] multY = getRotationMatrix(0f, 1f, 0f, simpleSin(rotationY), simpleCos(rotationY));
		float[] multZ = getRotationMatrix(0f, 0f, 1f, simpleSin(rotationZ), simpleCos(rotationZ));
		this.multiplicationMatrix = multMatrix(multMatrix(multZ, multY), multX);
	}

	private float[] multMatrix(float[] lhs, float[] rhs) {
		float[] result = new float[9];
		for (int row = 0; row < 3; row++) {
			for (int column = 0; column < 3; column++) {
				result[row * 3 + column] = 0f;
				for (int i = 0; i < 3; i++) {
					result[row * 3 + column] += lhs[3 * row + i] * rhs[column + i * 3];
				}
			}
		}
		return result;
	}

	private float simpleSin(int degree) {
		switch (degree) {
		case 90:
			return 1f;
		case 180:
		case 0:
			return 0f;
		case 270:
			return -1f;
		}
		throw new IllegalArgumentException(degree + " can not be applied as a rotation");
	}

	private float simpleCos(int degree) {
		switch (degree) {
		case 90:
		case 270:
			return 0f;
		case 180:
			return -1f;
		case 0:
			return 1f;
		}
		throw new IllegalArgumentException(degree + " can not be applied as a rotation");
	}

	private float[] getRotationMatrix(float x, float y, float z, float sinAngle, float cosAngle) {
		float cM1 = 1 - cosAngle;
		return new float[] { x * x * cM1 + cosAngle, x * y * cM1 - z * sinAngle,
				x * z * cM1 + y * sinAngle /* end first row */, y * x * cM1 + z * sinAngle, y * y * cM1 + cosAngle,
				y * z * cM1 - x * sinAngle /* end second row */, x * z * cM1 - y * sinAngle, y * z * cM1 + x * sinAngle,
				z * z * cM1 + cosAngle };
	}
}
