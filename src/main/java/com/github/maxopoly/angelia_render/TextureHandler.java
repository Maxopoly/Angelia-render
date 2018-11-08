package com.github.maxopoly.angelia_render;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Hashtable;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

public class TextureHandler {

	public static int texID;

	private BufferedImage testImage = loadTestImage();

	public BufferedImage loadTestImage() {
		/*
		 * BufferedImage img = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
		 * for(int x = 0; x < 64; x++) { for(int y = 0; y < 64; y++) { img.setRGB(x, y,
		 * 0x22332323); }} return img;
		 */

		try {
			return ImageIO.read(new File("/home/max/.minecraft/resourcepacks/bedrock.png"));
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

	}

	public int test() {
		System.out.println(testImage.getHeight());
		System.out.println(testImage.getWidth());
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		int id = GL11.glGenTextures();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);

		ByteBuffer buf = convertImageData(loadTestImage());
		GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
		// setup parameters
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, testImage.getWidth(), testImage.getHeight(), 0,
				GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buf);
		texID = id;
		return id;

	}

	private ByteBuffer convertImageData(BufferedImage bufferedImage) {
		int width = bufferedImage.getWidth();
		int height = bufferedImage.getHeight();

		int[] pixels = new int[width * height];
		bufferedImage.getRGB(0, 0, width, height, pixels, 0, width);

		ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4); // 4 because RGBA

		for (int y = 0; y < height; ++y) {
			for (int x = 0; x < width; ++x) {
				int pixel = pixels[x + y * width];
				buffer.put((byte) ((pixel >> 16) & 0xFF));
				buffer.put((byte) ((pixel >> 8) & 0xFF));
				buffer.put((byte) (pixel & 0xFF));
				buffer.put((byte) ((pixel >> 24) & 0xFF));
			}
		}
		buffer.flip();
		return buffer;
	}

}
