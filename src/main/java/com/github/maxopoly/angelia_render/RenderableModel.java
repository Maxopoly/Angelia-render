package com.github.maxopoly.angelia_render;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.github.maxopoly.angelia_render.parse.RenderableBox;
import com.github.maxopoly.angelia_render.parse.ResourcePackParseException;
import com.github.maxopoly.angelia_render.parse.TextureLocation;
import com.github.maxopoly.angeliacore.block.states.BlockState;
import com.github.maxopoly.angeliacore.model.location.BlockFace;

public class RenderableModel {



	public RenderableModel(JSONObject json, Map<String, TextureLocation> textureMap, Logger logger)
			throws ResourcePackParseException {
		JSONObject elements = json.optJSONObject("elements");
		if (elements == null) {
			throw new ResourcePackParseException("No elements for json " + json);
		}
		JSONObject textures = json.optJSONObject("textures");
		if (textures == null) {
			throw new ResourcePackParseException("No textures for json " + json);
		}
		List<RenderableBox> boxList = new LinkedList<RenderableBox>();
		Map<String, String> textureRemapping = parseTextureRemapping(textures);
		for (String key : elements.keySet()) {
			JSONObject currentElement = elements.optJSONObject(key);
			if (currentElement == null) {
				// value that doesnt belong here, we'll ignore it
				continue;
			}
			JSONArray from = currentElement.getJSONArray("from");
			JSONArray to = currentElement.getJSONArray("to");
			float lowerX = convertTextureInt(from.getInt(0));
			float lowerY = convertTextureInt(from.getInt(1));
			float lowerZ = convertTextureInt(from.getInt(2));
			float upperX = convertTextureInt(to.getInt(0));
			float upperY = convertTextureInt(to.getInt(1));
			float upperZ = convertTextureInt(to.getInt(2));
			JSONObject faces = currentElement.optJSONObject("faces");
			TextureLocation textureLocs[] = new TextureLocation[RenderableBox.renderableSides.length];
			BlockFace cullSides[] = new BlockFace[RenderableBox.renderableSides.length];
			for (String faceKey : faces.keySet()) {
				JSONObject face = faces.getJSONObject(faceKey);
				String textureLocString = face.getString("texture");
				TextureLocation textureLoc = resolveTexture(textureLocString, textureMap, textureRemapping);
				BlockFace currSide = BlockFace.parse(key);
				String cullFaceString = face.optString("cullface");
				BlockFace cullSide;
				if (cullFaceString != null) {
					cullSide = BlockFace.parse(cullFaceString);
				} else {
					cullSide = null;
				}
				textureLocs[currSide.ordinal()] = textureLoc;
				cullSides[currSide.ordinal()] = cullSide;
			}
			RenderableBox rendBox = new RenderableBox(lowerX, lowerY, lowerZ, upperX, upperY, upperZ, cullSides,
					textureLocs);
			boxList.add(rendBox);
		}
		this.boxes = (RenderableBox[]) boxList.toArray();
	}

	public void render(BlockState state, List<float[]> coordsToAdd, List<float[]> texCoordsToAdd, boolean[] cullFaces,
			int[] floatCounter) {
		for (RenderableBox box : boxes) {
			box.render(coordsToAdd, texCoordsToAdd, cullFaces, floatCounter);
		}
	}

	private TextureLocation resolveTexture(String key, Map<String, TextureLocation> textureMapping,
			Map<String, String> textureRemapping) {
		while (key.startsWith("#")) {
			// cycles are gonna deadlock this, but fixing that is for later
			key = textureRemapping.get(key.substring(1, key.length()));
		}
		return textureMapping.get(key);
	}

	private float convertTextureInt(int step) {
		return ((float) step) / 16f;
	}

	private Map<String, String> parseTextureRemapping(JSONObject json) {
		Map<String, String> remapping = new HashMap<>();
		for (String key : json.keySet()) {
			String value = json.optString(key);
			if (value == null) {
				// ignore
				continue;
			}
			remapping.put(key, value);
		}
		return remapping;
	}

}
