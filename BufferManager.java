package up.mi.jgm.td3;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class BufferManager {
    private DBConfig config;
    private DiskManager diskManager;
    private Map<PageId, byte[]> bufferPool;
    private String currentReplacementPolicy;

    // Constructor
    public BufferManager(DBConfig config, DiskManager diskManager) {
        this.config = config;
        this.diskManager = diskManager;
        this.bufferPool = new HashMap<>();
        this.currentReplacementPolicy = config.getBm_policy();
    }

    // GetPage method
    public byte[] GetPage(PageId pageId) throws IOException {
        byte[] buff = bufferPool.get(pageId);
        if (buff == null) {
            buff = new byte[config.getPagesize()];
            diskManager.ReadPage(pageId, buff);
            bufferPool.put(pageId, buff);
        }
        // Apply replacement policy if needed
        // Example: FIFO, LRU, etc.
        return buff;
    }

    // FreePage method
    public void FreePage(PageId pageId, boolean valdirty) {
        if (valdirty) {
            // Logic to mark the page as dirty
        }
        // Decrement pin_count and update other replacement policy-related info
        // No DiskManager calls here
    }

    // SetCurrentReplacementPolicy method
    public void SetCurrentReplacementPolicy(String policy) {
        this.currentReplacementPolicy = policy;
    }

    // FlushBuffers method
    public void FlushBuffers() throws IOException {
        for (Map.Entry<PageId, byte[]> entry : bufferPool.entrySet()) {
            PageId pageId = entry.getKey();
            byte[] buff = entry.getValue();
            // Assuming flag dirty check is handled, write to disk
            diskManager.WritePage(pageId, buff);
        }
        bufferPool.clear();
    }
}