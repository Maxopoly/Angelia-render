package com.github.maxopoly.angelia_render.parse;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

public class PackLoader {
	
	public void load(Logger logger, File f) throws ResourcePackParseException {
		if (!f.isDirectory()) {
			throw new ResourcePackParseException("Given path was not an existing directory");
		}
		File assets = new File(f, "assets");
		if (!assets.isDirectory()) {
			throw new ResourcePackParseException("assets/ folder not found");
		}
		File minecraft = new File(assets, "minecraft");
		if (!minecraft.isDirectory()) {
			throw new ResourcePackParseException("assets/minecraft/ folder not found");
		}
		File blockStates = new File(minecraft, "blockstates");
		if (!blockStates.isDirectory()) {
			throw new ResourcePackParseException("assets/minecraft/blockstates/ folder not found");
		}
		File models = new File(minecraft, "models");
		if (!models.isDirectory()) {
			throw new ResourcePackParseException("assets/minecraft/models/ folder not found");
		}
		File textures = new File(minecraft, "textures");
		if (!textures.isDirectory()) {
			throw new ResourcePackParseException("assets/minecraft/textures/ folder not found");
		}
		Map<String, BufferedImage> textureMap = loadTextures("", textures, logger);
		Map<String, JSONObject> blockStateMap = loadJSONFolder("", blockStates, logger);
		Map<String, JSONObject> modelMap = loadJSONFolder("", models, logger);
		modelMap = resolveModelDependencies(modelMap, logger);
	}
	
	private Map<String, JSONObject> loadJSONFolder(String prefix, File folder, Logger logger) throws ResourcePackParseException {
		Map<String, JSONObject> map = new HashMap<>();
		for(File f : folder.listFiles()) {
			if (f.isDirectory()) {
				Map<String, JSONObject> subMap = loadJSONFolder(f.getName() + "/", f, logger);
				map.putAll(subMap);
				continue;
			}
			if (!f.getName().endsWith(".json")) {
				continue;
			}
			try {
				StringBuffer sb = new StringBuffer();
				for(String s : Files.readAllLines(f.toPath())) {
					sb.append(s);
				}
				JSONObject json = new JSONObject(sb.toString());
				map.put(prefix + reducePath(f.getName(), ".json"), json);
			} catch (IOException e) {
				logger.error("Failed to read json file", e);
				throw new ResourcePackParseException("Failed to read file " + f.getAbsolutePath());
			}
		}
		return map;
	}
	
	private Map<String, JSONObject> resolveModelDependencies(Map<String, JSONObject> map, Logger logger) {
		Map<String, JSONObject> result = new HashMap<>();
		for(Entry<String,JSONObject> entry : map.entrySet()) {
			JSONObject current = entry.getValue();
			while(current.has("parent")) {
				JSONObject parent = map.get(current.getString("parent"));
				if (parent == null) {
					logger.warn("Could not find parent  " + current.getString("parent") + ". Incomplete parent resolve for model: " + entry.getKey());
					break;
				}
				current.remove("parent");
				current = mergeJSON(parent, current);
			}
			result.put(entry.getKey(), current);
		}
		return result;
	}
	
	private static Map<String, BufferedImage> loadTextures(String prefix, File folder, Logger logger) throws ResourcePackParseException {
		Map<String, BufferedImage> result = new HashMap<>();
		for(File f : folder.listFiles()) {
			if (f.isDirectory()) {
				Map<String, BufferedImage> subMap = loadTextures(f.getName() + "/", f, logger);
				result.putAll(subMap);
				continue;
			}
			if (!f.getName().endsWith(".png")) {
				continue;
			}
			try {
				BufferedImage img = ImageIO.read(f);
				result.put(prefix + reducePath(f.getName(), ".png"), img);
			} catch (IOException e) {
				logger.error("Failed to read png file", e);
				throw new ResourcePackParseException("Failed to read file " + f.getAbsolutePath());
			}
		}
		return result;
	}
	
	private JSONObject mergeJSON(JSONObject parent, JSONObject child) {
		//dirty way to deep copy
		JSONObject result = new JSONObject(parent.toString());
		for(String key : child.keySet())  {
			if (!result.has(key)) {
				result.put(key, child.get(key));
				continue;
			}
			JSONObject optExistingValue = result.optJSONObject(key);
			if (optExistingValue == null) {
				//flat value which we will overwrite
				result.put(key, child.get(key));
				continue;
			}
			//jsons with same key exist, need to deep merge them
			result.put(key, mergeJSON(result.getJSONObject(key), child.getJSONObject(key)));
		}
		//System.out.println("Merging " + child + " into " + parent + ". Result: " + result);
		return result;
		
	}
	
	/**
	 * Removes .json from the end of a path
	 */
	private static String reducePath(String name, String extension) {
		if (!name.endsWith(extension)) {
			throw new IllegalArgumentException("Cannot remove " +extension + " from file name " + name);
		}
		return name.substring(0, name.length() - extension.length());
	}

}
