package com.github.maxopoly.angelia_render.random;

import java.util.List;
import java.util.Random;

import com.github.maxopoly.angelia_render.RenderableModel;
import com.github.maxopoly.angelia_render.parse.RenderableBox;
import com.github.maxopoly.angeliacore.block.states.BlockState;

public class RandomDistributionImpl implements RandomDistribution {

	private RenderableModel[] model;
	private int [] chances;
	private int chanceBound;
	@Override
	public void render(int x, int y, int z, BlockState state, List<float[]> coordsToAdd, List<float[]> texCoordsToAdd,
			BlockState[] cullFaces, int[] floatCounter) {
		Random rng = new Random((((x * 31) + y) * 31) + z);
		int randonNum = rng.nextInt(chanceBound);
		for(int i = 0; i < chances.length; i++) {
			if (chances [i] >= randonNum) {
				model.render(...);
				break;
			}
		}

	}



}
