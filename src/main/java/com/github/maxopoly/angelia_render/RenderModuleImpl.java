package com.github.maxopoly.angelia_render;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.github.maxopoly.angelia_render.parse.RenderableBox;
import com.github.maxopoly.angelia_render.parse.ResourcePackParseException;
import com.github.maxopoly.angelia_render.parse.TextureLocation;
import com.github.maxopoly.angeliacore.block.BlockStateFactory;
import com.github.maxopoly.angeliacore.block.RenderModule;
import com.github.maxopoly.angeliacore.block.states.BlockHalfEnum;
import com.github.maxopoly.angeliacore.block.states.BlockState;
import com.github.maxopoly.angeliacore.block.states.StairShapeEnum;
import com.github.maxopoly.angeliacore.model.location.BlockFace;

public class RenderModuleImpl implements RenderModule {


	public RenderModuleImpl(String identifier, JSONObject json, Logger logger)
			throws ResourcePackParseException {
		parseBlockState(identifier, json);
	}

	public void render(BlockState state, List<float[]> coordsToAdd, List<float[]> texCoordsToAdd, boolean[] cullFaces,
			int[] floatCounter) {
	}

	private void parseBlockState(String identifier, JSONObject json) {
		JSONObject variants = json.optJSONObject("variants");
		if (variants != null) {
			for (String key : variants.keySet()) {
				List<RendBlockState> rendBlockStates = new LinkedList<>();
				JSONArray jsonArray = variants.optJSONArray(key);
				if (jsonArray != null) {
					for (int i = 0; i < jsonArray.length(); i++) {
						JSONObject arrayContent = jsonArray.getJSONObject(i);
						rendBlockStates.add(parseBlockStateVariant(arrayContent));
					}
				} else {
					rendBlockStates.add(parseBlockStateVariant(variants.getJSONObject(key)));
				}
				parseIndexOutOfStateEnums(identifier, key, BlockStateFactory.getStateByTextureIdentifier(identifier));
			}
		}
	}

	private int parseIndexOutOfStateEnums(String identifier, String key, BlockState state)  {
		List <Enum> enumList = new LinkedList<>();
		String [] parts = key.split(",");
		for(String part : parts) {
			if (part.toLowerCase().equals("normal")) {
				continue;
			}
			String [] keyValSplit = part.split("=");
			if (keyValSplit.length != 2) {
				//TODO error reporting
				continue;
			}
			enumList.add(parseEnum(identifier, keyValSplit[0], keyValSplit[1]));

		}
		if (state != null) {
		return state.getMetaData(enumList);
		}
		return 0;
	}

	private Enum parseEnum(String identifier, String key, String value) {
		switch (key.toLowerCase()) {
		case "half":
			return BlockHalfEnum.parse(value);
		case "facing":
			return BlockFace.parse(value);
		case "shape":
			return StairShapeEnum.parse(value);

		}

		System.out.println(identifier + ", Key: " + key + "   " + value);
		return null;
	}

	private RendBlockState parseBlockStateVariant(JSONObject json) {
		String model = json.getString("model");
		int x = json.optInt("x", 0);
		int y = json.optInt("y", 0);
		boolean uvLock = json.optBoolean("uvlock", false);
		int weight = json.optInt("weight", 1);
		return new RendBlockState(model, x, y, uvLock, weight);
	}

	private class RendBlockState {
		String model;
		int x;
		int y;
		boolean uvLock;
		int weight;

		RendBlockState(String model, int x, int y, boolean uvLock, int weight) {
			this.model = model;
			this.x = x;
			this.y = y;
			this.uvLock = uvLock;
			this.weight = weight;
		}
	}
}
