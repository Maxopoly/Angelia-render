package com.github.maxopoly.angelia_render.parse;

public class Coord2D {
	
	private final int x;
	private final int y;
	
	public Coord2D(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public String toString() {
		return String.format("(%d,%d)",x,y);
	}

}
