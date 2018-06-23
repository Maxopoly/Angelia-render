package com.github.maxopoly.angelia_render;

import com.github.maxopoly.angeliacore.block.Chunk;
import com.github.maxopoly.angeliacore.connection.ServerConnection;
import com.github.maxopoly.angeliacore.event.AngeliaListener;
import com.github.maxopoly.angeliacore.plugin.AngeliaPlugin;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.commons.cli.Option;
import org.kohsuke.MetaInfServices;

@MetaInfServices(AngeliaPlugin.class)
public class RenderPlugin extends AngeliaPlugin implements AngeliaListener {

    private ServerConnection connection;

	public RenderPlugin() {
		super("renderer");
	}

	@Override
	public void start() {
		VBOHandler vboHandler = new VBOHandler();
		connection.getEventHandler().registerListener(new ChunkListener(vboHandler));
		RenderRunnable runnable = new RenderRunnable(connection.getLogger(), vboHandler);
		runnable.updateCameraLocation(connection.getPlayerStatus().getLocation());
		for(Chunk c : connection.getChunkHolder().getLoadedChunks()) {
            vboHandler.addChunk(c);
        }
		new Thread(runnable).start();
	}

	@Override
	public String getHelp() {
		return "";
	}

	@Override
	protected List<Option> createOptions() {
		// TODO Auto-generated method stub
		return new LinkedList<>();
	}

	@Override
	protected void parseOptions(ServerConnection connection, Map<String, List<String>> args) {
		this.connection = connection;

	}

	@Override
	public void tearDown() {
		// TODO Auto-generated method stub

	}

	@Override
	public AngeliaPlugin transistionToNewConnection(ServerConnection newConnection) {
		// TODO Auto-generated method stub
		return null;
	}



}
