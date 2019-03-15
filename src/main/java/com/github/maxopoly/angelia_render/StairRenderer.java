package com.github.maxopoly.angelia_render;

import java.util.List;

import com.github.maxopoly.angelia_render.parse.RenderableBox;
import com.github.maxopoly.angelia_render.random.RandomDistributionImpl;
import com.github.maxopoly.angeliacore.block.states.BlockState;
import com.github.maxopoly.angeliacore.model.location.BlockFace;

public class StairRenderer {

	private RandomDistributionImpl[] boxes;

	private RenderableModel [] model;

	public void render(BlockState state, List<float[]> coordsToAdd, List<float[]> texCoordsToAdd, BlockState [] cullFaces,
			int[] floatCounter) {
		int index = state.getMetaData();
		if (cullFaces [BlovekFace.BOTTOM.ordinal()] instanceof FenceBlockState) {
			index += 16;
		}
		model [index].render(state, coordsToAdd, texCoordsToAdd, cullFaces, floatCounter);
	}



}
