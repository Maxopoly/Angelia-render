package com.github.maxopoly.angelia_render.random;

import java.util.List;

import com.github.maxopoly.angeliacore.block.states.BlockState;

public interface RandomDistribution {

	public void render(int x, int y, int z, BlockState state, List<float[]> coordsToAdd, List<float[]> texCoordsToAdd,
			BlockState[] cullFaces, int[] floatCounter);

}
