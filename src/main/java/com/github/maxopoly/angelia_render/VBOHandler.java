package com.github.maxopoly.angelia_render;

import com.github.maxopoly.angeliacore.block.Chunk;
import com.github.maxopoly.angeliacore.block.states.BlockState;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;

public class VBOHandler {

	// each of these contains 4 vertices, which make up one side
	private float[] leftSideTriangles = new float[] { 0f, 1f, 1f, 0f, 0f, 1f, 0f, 1.f, 0f, 0f, 0f, 0f };
	private float[] rightSideTriangles = new float[] { 1f, 1f, 1f, 1f, 0f, 1f, 1f, 1.f, 0f, 1f, 0f, 0f };
	private float[] topSideTriangles = new float[] { 1f, 1f, 1f, 0f, 1f, 1f, 1f, 1f, 0f, 0f, 1f, 0f };
	private float[] bottomSideTriangles = new float[] { 1f, 0f, 1f, 0f, 0f, 1f, 1f, 0f, 0f, 0f, 0f, 0f };
	private float[] backSideTriangles = new float[] { 1f, 1f, 0f, 0f, 1f, 0f, 1f, 0f, 0f, 0f, 0f, 0f };
	private float[] frontSideTriangles = new float[] { 1f, 1f, 1f, 0f, 1f, 1f, 1f, 0f, 1f, 0f, 0f, 1f };

	private float[] texCoords = new float[] { 0f, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 1f, 0f, 1f, 1f };

	private List<Integer> vertexHandler;
	private List<Integer> colorHandler;
	private List<Integer> texHandler;
	private List<Integer> vertexElementLength;
	private Queue<Chunk> chunksToProcess;
	
	private int textureID;

	public VBOHandler() {
		this.vertexHandler = new LinkedList<>();
		this.colorHandler = new LinkedList<>();
		this.texHandler = new LinkedList<>();
		this.vertexElementLength = new LinkedList<>();
		this.chunksToProcess = new LinkedList<>();
	}

	public void render() {
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);
		for (int i = 0; i < vertexHandler.size(); i++) {
			int vertexID = vertexHandler.get(i);
			int colorID = colorHandler.get(i);
			int texID = texHandler.get(i);
			int length = vertexElementLength.get(i);
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexID);
			GL11.glVertexPointer(3, GL11.GL_FLOAT, 0, 0l);

			/*
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, colorID);
			GL11.glColorPointer(3, GL11.GL_FLOAT, 0, 0l);  */

			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, texID);
			GL11.glTexCoordPointer(2, GL11.GL_FLOAT, 0, 0l); 

			GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
			//GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
			GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);

			GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, length);

			GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
			//GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);
			GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
		}
	}

	public void addChunk(Chunk c) {
		chunksToProcess.add(c);
	}

	public void popQueue() {
		while (!chunksToProcess.isEmpty()) {
			Chunk c = chunksToProcess.poll();
			List<Float> worldCoords = new ArrayList<>();
			List<Float> colorCoords = new ArrayList<>();
			List<Float> trianglePos = genTrianglePositions(worldCoords, colorCoords, c);
			FloatBuffer vertex_data = BufferUtils.createFloatBuffer(trianglePos.size());
			for (float f : trianglePos) {
				vertex_data.put(f);
			}
			vertex_data.flip();

			int vbo_vertex_handle = GL15.glGenBuffers();
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo_vertex_handle);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertex_data, GL15.GL_STATIC_DRAW);
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

			FloatBuffer color_data = BufferUtils.createFloatBuffer(colorCoords.size());
			for (float f : colorCoords) {
				color_data.put(f);
			}
			color_data.flip();

			int vbo_color_handle = GL15.glGenBuffers();
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo_color_handle);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, color_data, GL15.GL_STATIC_DRAW);
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

			FloatBuffer textureData = BufferUtils.createFloatBuffer(trianglePos.size() / 3 / 6 * 12);
			for (int i = 0; i < trianglePos.size() / 3 / 6; i++) {
				for (float f : texCoords) {
					textureData.put(f);
				}
			}
			
			textureData.flip();

			int vbo_tex_handle = GL15.glGenBuffers();
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo_tex_handle);
			GL15.glBufferData(GL15.GL_ARRAY_BUFFER, textureData, GL15.GL_STATIC_DRAW);
			GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

			vertexHandler.add(vbo_vertex_handle);
			colorHandler.add(vbo_color_handle);
			texHandler.add(vbo_tex_handle);
			vertexElementLength.add(trianglePos.size());
		}
	}

	private List<Float> genTrianglePositions(List<Float> worldCoords, List<Float> colorCoords, Chunk c) {
		int absoluteChunkX = c.getX() * Chunk.CHUNK_WIDTH;
		int absoluteChunkZ = c.getZ() * Chunk.CHUNK_WIDTH;
		BlockState[][] blocks = c.dump();
		int chunkLimitSidewards = Chunk.CHUNK_WIDTH - 1;
		int chunkLimitUpwards = Chunk.CHUNK_HEIGHT - 1;
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
						if (block == null || block.getID() == 0) {
							// air
							continue;
						}
						// left side
						if (x == 0 || !currSection[index - 1].isOpaque()) {
							drawSide(worldCoords, colorCoords, x + absoluteChunkX, y + sectionYOffset,
									z + absoluteChunkZ, leftSideTriangles);
						}
						// right side
						if (x == chunkLimitSidewards || !currSection[index + 1].isOpaque()) {
							drawSide(worldCoords, colorCoords, x + absoluteChunkX, y + sectionYOffset,
									z + absoluteChunkZ, rightSideTriangles);
						}
						// back side
						if (z == 0 || !currSection[index - Chunk.CHUNK_WIDTH].isOpaque()) {
							drawSide(worldCoords, colorCoords, x + absoluteChunkX, y + sectionYOffset,
									z + absoluteChunkZ, backSideTriangles);
						}
						// front side
						if (z == chunkLimitSidewards || !currSection[index + Chunk.CHUNK_WIDTH].isOpaque()) {
							drawSide(worldCoords, colorCoords, x + absoluteChunkX, y + sectionYOffset,
									z + absoluteChunkZ, frontSideTriangles);
						}
						// top side
						// need to account for overlap into other sections
						int topIndex = index + Chunk.BLOCKS_PER_LAYER;
						boolean useNextTop = false;
						if (topIndex >= Chunk.BLOCKS_PER_SECTION) {
							topIndex -= Chunk.BLOCKS_PER_SECTION;
							useNextTop = true;
						}
						if (y == chunkLimitUpwards
								|| (useNextTop && (upperSection == null || !upperSection[topIndex].isOpaque()))
								|| (!useNextTop && !currSection[topIndex].isOpaque())) {
							drawSide(worldCoords, colorCoords, x + absoluteChunkX, y + sectionYOffset,
									z + absoluteChunkZ, topSideTriangles);
						}
						// bottom side
						int botIndex = index - Chunk.BLOCKS_PER_LAYER;
						boolean useNextBottom = false;
						if (botIndex < 0) {
							botIndex += Chunk.BLOCKS_PER_SECTION;
							useNextBottom = true;
						}
						if (y == 0 || (useNextBottom && (lowerSection == null || !lowerSection[botIndex].isOpaque()))
								|| (!useNextBottom && !currSection[botIndex].isOpaque())) {
							drawSide(worldCoords, colorCoords, x + absoluteChunkX, y + sectionYOffset,
									z + absoluteChunkZ, bottomSideTriangles);
						}
					}
				}
			}
		}
		return worldCoords;
	}

	private void drawSide(List<Float> worldCoordList, List<Float> colorCoords, int x, int y, int z,
			float[] rawTriangleCoords) {
		float[] multipliedTriangleCoords = new float[rawTriangleCoords.length];
		// premulitply all vertices
		for (int i = 0; i < rawTriangleCoords.length; i += 3) {
			multipliedTriangleCoords[i] = rawTriangleCoords[i] + x;
			multipliedTriangleCoords[i + 1] = rawTriangleCoords[i + 1] + y;
			multipliedTriangleCoords[i + 2] = rawTriangleCoords[i + 2] + z;
		}

		// we are drawing 2 triangles, so we insert vertices in the order: 1, 2, 3 , 2,
		// 3, 4
		for (int i = 0; i < 3 * 3; i++) {
			colorCoords.add(rawTriangleCoords[i]);
			worldCoordList.add(multipliedTriangleCoords[i]);
		}
		for (int i = 3; i < 4 * 3; i++) {
			colorCoords.add(rawTriangleCoords[i]);
			worldCoordList.add(multipliedTriangleCoords[i]);
		}
	}
	
	public void loadTexture() {
		textureID = new TextureHandler().test();
	}

}
