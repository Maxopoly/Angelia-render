package com.github.maxopoly.angelia_render.parse;

/**
 * 0,0 is in top left corner, insertion priority is LEFT > TOP > RIGHT > BOTTOM
 *
 */
public class Box2D {

	public enum Corner {
		TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT;

		/**
		 * If we were to place a new box based on this corner, which corner of the new
		 * box would this corner be
		 * 
		 */
		public Corner getCornerToFit() {
			switch (this) {
			case BOTTOM_LEFT:
			case TOP_RIGHT:
				return TOP_LEFT;
			case BOTTOM_RIGHT:
			case TOP_LEFT:
				return TOP_RIGHT;
			default:
				throw new IllegalArgumentException("This should never happen");
			}
		}

		public Coord2D getCornerOffset() {
			switch (this) {
			case BOTTOM_LEFT:
			case BOTTOM_RIGHT:
				return new Coord2D(0, 1);
			case TOP_RIGHT:
				return new Coord2D(1, 0);
			case TOP_LEFT:
				return new Coord2D(-1, 0);
			default:
				throw new IllegalArgumentException("This should never happen");
			}
		}
	}

	private int x;
	private int y;
	private int length;
	private int height;
	private boolean[] viableCorners;

	public Box2D(int x, int y, Corner corner, int length, int height) {
		if (length == 0 || height == 0) {
			throw new IllegalArgumentException("Illegal dimensions were " + length + " " + height);
		}
		this.x = x;
		this.y = y;
		if (corner == Corner.BOTTOM_RIGHT || corner == Corner.BOTTOM_LEFT) {
			this.y -= height - 1;
		}
		if (corner == Corner.TOP_RIGHT || corner == Corner.BOTTOM_RIGHT) {
			x -= length - 1;
		}
		this.length = length;
		this.height = height;
		viableCorners = new boolean[Corner.values().length];
		for (int i = 0; i < viableCorners.length; i++) {
			viableCorners[i] = true;
		}
	}

	public Box2D(Coord2D coords, Corner corner, int length, int height) {
		this(coords.getX(), coords.getY(), corner, length, height);
	}

	public boolean isInside(int x, int y) {
		return x >= this.x && x <= this.x + length - 1 && y >= this.y && y <= this.y + height - 1;
	}

	public boolean isInside(Coord2D coords) {
		return isInside(coords.getX(), coords.getY());
	}

	public boolean isInside(Box2D outer) {
		for (Corner corner : Corner.values()) {
			if (!outer.isInside(getCorner(corner))) {
				return false;
			}
		}
		return true;
	}

	public Coord2D getCorner(Corner corner) {
		switch (corner) {
		case BOTTOM_LEFT:
			return new Coord2D(x, y + height - 1);
		case BOTTOM_RIGHT:
			return new Coord2D(x + length - 1, y + height - 1);
		case TOP_LEFT:
			return new Coord2D(x, y);
		case TOP_RIGHT:
			return new Coord2D(x + length - 1, y);
		default:
			throw new IllegalArgumentException("This should never happen");
		}
	}

	public boolean intersects(Box2D box) {
		for(Corner corner : Corner.values()) {
			if (box.isInside(getCorner(corner)) || isInside(box.getCorner(corner))) {
				return true;
			}
		}
		return false;
	}

	public boolean isCornerViable(Corner corner) {
		return viableCorners[corner.ordinal()];
	}

	public void makeCornerUnviable(Corner corner) {
		viableCorners[corner.ordinal()] = false;
	}

	public String toString() {
		return String.format("%d, %d (%d x %d) [%b %b %b %b]", x, y, length, height, viableCorners[0], viableCorners[1],
				viableCorners[2], viableCorners[3]);
	}

}
