package com.github.maxopoly.angelia_render;

import com.github.maxopoly.angelia_render.parse.RenderableBox;
import com.github.maxopoly.angeliacore.block.Chunk;
import com.github.maxopoly.angeliacore.block.states.BlockState;
import com.github.maxopoly.angeliacore.model.location.BlockFace;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;

public class VBOHandler {

	private List<Integer> vertexHandler;
	private List<Integer> texHandler;
	private List<Integer> vertexElementLength;
	private Queue<Chunk> chunksToProcess;

	private int textureID;

	public VBOHandler() {
		this.vertexHandler = new LinkedList<>();
		this.texHandler = new LinkedList<>();
		this.vertexElementLength = new LinkedList<>();
		this.chunksToProcess = new LinkedList<>();
	}

	public void render() {
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);
		for (int i = 0; i < vertexHandler.size(); i++) {
			int vertexID = vertexHandler.get(i);
			int texID = texHandler.get(i);
			int length = vertexElementLength.get(i);
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexID);
			GL11.glVertexPointer(3, GL11.GL_FLOAT, 0, 0l);

			/*
			 * GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, colorID); GL11.glColorPointer(3,
			 * GL11.GL_FLOAT, 0, 0l);
			 */

			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, texID);
			GL11.glTexCoordPointer(2, GL11.GL_FLOAT, 0, 0l);

			GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
			// GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
			GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);

			GL11.glDrawArrays(GL11.GL_QUADS, 0, length);

			GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
			// GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);
			GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
		}
	}

	public void addChunk(Chunk c) {
		synchronized (chunksToProcess) {
			chunksToProcess.add(c);
		}
	}

	public void popQueue() {
		while (!chunksToProcess.isEmpty()) {
			Chunk c;
			synchronized (chunksToProcess) {
				c = chunksToProcess.poll();
			}
			FloatBuffer[] buffer = genVBOData(c);
			FloatBuffer vertex_data = buffer [0];
			vertex_data.flip();

			int vbo_vertex_handle = GL15.glGenBuffers();
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo_vertex_handle);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertex_data, GL15.GL_STATIC_DRAW);
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

			FloatBuffer textureData = buffer [1];
			textureData.flip();

			int vbo_tex_handle = GL15.glGenBuffers();
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo_tex_handle);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, textureData, GL15.GL_STATIC_DRAW);
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

			vertexHandler.add(vbo_vertex_handle);
			texHandler.add(vbo_tex_handle);
			// 3 coords per vertix and 4 vertices per quad
			vertexElementLength.add(vertex_data.limit() / 3 / 4);
		}
	}

	private FloatBuffer [] genVBOData(Chunk c) {
		final List<float[]> worldCoords = new ArrayList<>();
		final List<float[]> textureCoords = new ArrayList<>();
		final int absoluteChunkX = c.getX() * Chunk.CHUNK_WIDTH;
		final int absoluteChunkZ = c.getZ() * Chunk.CHUNK_WIDTH;
		final BlockState[][] blocks = c.dump();
		final int chunkLimitSidewards = Chunk.CHUNK_WIDTH - 1;
		final List<int[]> blockOffSets = new ArrayList<>();
		int [] totalSize = new int [] {0};
		for (int section = 0; section < Chunk.SECTIONS_PER_CHUNK; section++) {
			BlockState[] currSection = blocks[section];
			BlockState[] lowerSection = section > 0 ? blocks[section - 1] : null;
			BlockState[] upperSection = section < Chunk.SECTIONS_PER_CHUNK - 1 ? blocks[section + 1] : null;
			if (currSection == null) {
				continue;
			}
			int sectionYOffset = section * Chunk.SECTION_HEIGHT;
			for (int y = 0; y < Chunk.SECTION_HEIGHT; y++) {
				for (int z = 0; z < Chunk.CHUNK_WIDTH; z++) {
					for (int x = 0; x < Chunk.CHUNK_WIDTH; x++) {
						int index = (((y * Chunk.CHUNK_WIDTH) + z) * Chunk.CHUNK_WIDTH) + x;
						BlockState block = currSection[index];
						if (block == null || block.getID() == 0 || block.getRenderModule() == null) {
							// air or not renderable
							continue;
						}
						BlockState[] neighbors = new BlockState[RenderableBox.renderableSides.length/* 6 */];
						if (x != 0) {
							neighbors[BlockFace.WEST.ordinal()] = currSection[index - 1];
						}
						if (x != chunkLimitSidewards) {
							neighbors[BlockFace.EAST.ordinal()] = currSection[index + 1];
						}
						if (z != 0) {
							neighbors[BlockFace.NORTH.ordinal()] = currSection[index - Chunk.CHUNK_WIDTH];
						}
						if (z != chunkLimitSidewards) {
							neighbors[BlockFace.SOUTH.ordinal()] = currSection[index + Chunk.CHUNK_WIDTH];
						}
						// top side
						// need to account for overlap into other sections
						int topIndex = index + Chunk.BLOCKS_PER_LAYER;
						boolean useNextTop = false;
						if (topIndex >= Chunk.BLOCKS_PER_SECTION) {
							topIndex -= Chunk.BLOCKS_PER_SECTION;
							useNextTop = true;
						}
						if (useNextTop) {
							if (upperSection != null) {
								neighbors[BlockFace.TOP.ordinal()] = upperSection[topIndex];
							}
						} else {
							neighbors[BlockFace.TOP.ordinal()] = currSection[topIndex];
						}
						// bottom side
						int botIndex = index - Chunk.BLOCKS_PER_LAYER;
						boolean useNextBottom = false;
						if (botIndex < 0) {
							botIndex += Chunk.BLOCKS_PER_SECTION;
							useNextBottom = true;
						}
						if (useNextBottom) {
							if (lowerSection != null) {
								neighbors[BlockFace.BOTTOM.ordinal()] = lowerSection[botIndex];
							}
						} else {
							neighbors[BlockFace.BOTTOM.ordinal()] = currSection[botIndex];
						}
						boolean[] sideBlocks = hasAdjacentBlock(neighbors);
						//TODO TODO TODO TODO TODO TODO
						//((RenderModuleImpl) block.getRenderModule()).render(worldCoords, textureCoords, sideBlocks, totalSize);
						blockOffSets.add(new int[] { worldCoords.size(), x + absoluteChunkX, y + sectionYOffset, z + absoluteChunkZ });
					}
				}
			}
		}
		FloatBuffer coordData = BufferUtils.createFloatBuffer(totalSize [0]);
		FloatBuffer textureData = BufferUtils.createFloatBuffer(totalSize [0] / 3 * 2);
		int startingIndex = 0;
		for (int[] currArray : blockOffSets) {
			final int upperBound = currArray[0];
			final int x = currArray[1];
			final int y = currArray[2];
			final int z = currArray[3];
			for(int i = startingIndex; i < upperBound; i++) {
				final float [] coordArray = worldCoords.get(i);
				for(int k = 0; k < coordArray.length; k+= 3) {
					coordData.put(coordArray [k] + x);
					coordData.put(coordArray [k + 1] + y);
					coordData.put(coordArray [k + 2] + z);
				}
				textureData.put(textureCoords.get(i));
			}
			startingIndex = upperBound;
		}
		return new FloatBuffer [] {coordData, textureData};
	}

	private boolean[] hasAdjacentBlock(BlockState[] states) {
		boolean[] result = new boolean[states.length];
		for (int i = 0; i < states.length; i++) {
			result[i] = states[i] == null || states[i].isOpaque();
		}
		return result;
	}

	public void loadTexture() {
		textureID = new TextureHandler().test();
	}

}
