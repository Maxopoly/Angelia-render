package com.github.maxopoly.angelia_render;

public class Matrix4x4 {

    // row by row,so:

    private float[] matrix;

    private Matrix4x4(float[] matrix) {
        this.matrix = matrix;
    }

    /**
     * @return Identity Matrix
     */
    public static Matrix4x4 getIdentity() {
        return new Matrix4x4(new float[] { 1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f });
    }

    /**
     * Adds the given matrix to this one component by component
     *
     * @param other
     *            Matrix to add
     */
    public void add(Matrix4x4 other) {
        for (int i = 0; i < matrix.length; i++) {
            this.matrix[i] += other.matrix[i];
        }
    }

    /**
     * Subtracts the given matrix from this one
     *
     * @param other
     *            Matrix to subtract
     */
    public void subtract(Matrix4x4 other) {
        for (int i = 0; i < matrix.length; i++) {
            this.matrix[i] -= other.matrix[i];
        }
    }

    /**
     * Multiplies matrix with a scalar
     *
     * @param scalar
     *            Scalar to multiply by
     */
    public void multiply(float scalar) {
        for (int i = 0; i < matrix.length; i++) {
            this.matrix[i] *= scalar;
        }
    }

    public Matrix4x4 getFrustum(float l, float r, float b, float t, float n, float f) {
        return new Matrix4x4(new float[] { 2 * n / (r - l), 0f, (r + l) / (r - l), 0f, 0f, 2 * n / (t - b),
                (t + b) / (t - b), 0f, 0f, 0f, -(f + n) / (f - n), -2 * f * n / (f - n), 0f, 0f, -1f, 1f });
    }

}
