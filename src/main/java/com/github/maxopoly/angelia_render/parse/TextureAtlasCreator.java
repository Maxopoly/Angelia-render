package com.github.maxopoly.angelia_render.parse;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.util.LinkedList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.github.maxopoly.angelia_render.parse.Box2D.Corner;

public class TextureAtlasCreator {

	private int size;
	private List<BufferedImage> atlas;
	private List<List<Box2D>> atlasTracker;
	int counter = 0;

	public TextureAtlasCreator() {
		atlas = new LinkedList<>();
		atlasTracker = new LinkedList<>();
		size = GL11.glGetInteger(GL11.GL_MAX_TEXTURE_SIZE);
		//TODO progressively make smaller
	}

	public List<BufferedImage> getCreatedAtlas() {
		return atlas;
	}

	private void addAtlas() {
		BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
		atlas.add(img);
		atlasTracker.add(new LinkedList<>());
	}

	public TextureLocation add(BufferedImage imageToAdd) {
		if (imageToAdd.getWidth() > size || imageToAdd.getHeight() > size) {
			throw new IllegalArgumentException(
					"Can't fit image of size " + imageToAdd.getWidth() + "x" + imageToAdd.getHeight() + ". Max size is: " + size);
		}
		if (atlas.size() == 0) {
			addAtlas();
		}
		for (int i = 0; i < atlas.size(); i++) {
			BufferedImage atlasImage = atlas.get(i);
			List<Box2D> boxes = atlasTracker.get(i);
			Coord2D fit = fit(atlasImage, boxes, imageToAdd);
			if (fit != null) {
				return new TextureLocation(i, convertIntCoordsToFloat(fit.getX(), atlasImage.getWidth()),
						convertIntCoordsToFloat(fit.getY(), atlasImage.getHeight()),
						convertIntCoordsToFloat(fit.getX() + imageToAdd.getWidth() - 1, atlasImage.getWidth()),
						convertIntCoordsToFloat(fit.getY() + imageToAdd.getHeight() - 1, atlasImage.getHeight()));
			}
		}
		addAtlas();
		int index = atlas.size() - 1;
		BufferedImage atlasImage = atlas.get(index);
		Coord2D fit = fit(atlasImage, atlasTracker.get(index), imageToAdd);
		if (fit == null) {
			throw new IllegalStateException("Could not fit texture into empty atlas");
		}
		return new TextureLocation(index, convertIntCoordsToFloat(fit.getX(), atlasImage.getWidth()),
				convertIntCoordsToFloat(fit.getY(), atlasImage.getHeight()),
				convertIntCoordsToFloat(fit.getX() + imageToAdd.getWidth() - 1, atlasImage.getWidth()),
				convertIntCoordsToFloat(fit.getY() + imageToAdd.getHeight() - 1, atlasImage.getHeight()));
	}

	private float convertIntCoordsToFloat(int coord, int size) {
		return (float) coord / (float) size;
	}

	private Coord2D fit(BufferedImage atlas, List<Box2D> existingImages, BufferedImage toFit) {
		// This could probably be improved through smart stuff like quadtrees, but works
		// fine for now
		if (existingImages.isEmpty()) {
			// fit top left
			Box2D box = new Box2D(0, 0, Corner.TOP_LEFT, toFit.getWidth(), toFit.getHeight());
			box.makeCornerUnviable(Corner.TOP_LEFT);
			existingImages.add(box);
			addImage(atlas, toFit, 0, 0);
			return new Coord2D(0, 0);
		}
		Box2D atlasBox = new Box2D(0, 0, Corner.TOP_LEFT, atlas.getWidth(), atlas.getHeight());
		for (Box2D box : existingImages) {
			boxLoop: for (Corner corner : new Corner[] { Corner.TOP_RIGHT, Corner.BOTTOM_LEFT, Corner.BOTTOM_RIGHT }) {

				if (box.isCornerViable(corner)) {
					// check if we fit
					Coord2D offset = corner.getCornerOffset();
					Coord2D cornerCoord = box.getCorner(corner);
					Box2D newBox = new Box2D(offset.getX() + cornerCoord.getX(), offset.getY() + cornerCoord.getY(),
							corner.getCornerToFit(), toFit.getWidth(), toFit.getHeight());
					if (!newBox.isInside(atlasBox)) {
						continue;
					}
					// big O
					for (Box2D toCompare : existingImages) {
						if (toCompare.intersects(newBox)) {
							continue boxLoop;
						}
					}
					// fits, so insert
					box.makeCornerUnviable(corner);
					existingImages.add(newBox);
					newBox.makeCornerUnviable(corner.getCornerToFit());
					addImage(atlas, toFit, newBox.getCorner(Corner.TOP_LEFT).getX(),
							newBox.getCorner(Corner.TOP_LEFT).getY());
					return newBox.getCorner(Corner.TOP_LEFT);
				}
			}
		}
		return null;
	}

	private static void addImage(BufferedImage canvas, BufferedImage toAdd, int x, int y) {
		Graphics2D g2d = canvas.createGraphics();
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
		g2d.drawImage(toAdd, x, y, null);
		g2d.dispose();
	}

	private static BufferedImage deepCopyBufferedImage(BufferedImage bi) {
		ColorModel cm = bi.getColorModel();
		boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
		WritableRaster raster = bi.copyData(null);
		return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
	}

}
