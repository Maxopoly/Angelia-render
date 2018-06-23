package com.github.maxopoly.angelia_render;

import com.github.maxopoly.angeliacore.event.AngeliaEventHandler;
import com.github.maxopoly.angeliacore.event.AngeliaListener;
import com.github.maxopoly.angeliacore.event.events.ChunkLoadEvent;

public class ChunkListener implements AngeliaListener {

    private VBOHandler handler;

    public ChunkListener(VBOHandler handler) {
        this.handler = handler;
    }

    @AngeliaEventHandler
    public void onChunkLoad(ChunkLoadEvent e) {
        handler.addChunk(e.getChunk());
    }

}
