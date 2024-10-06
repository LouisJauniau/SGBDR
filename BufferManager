package sgbd;
import java.util.*;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.io.IOException;

//stocker buffer bite buffer 


public class BufferManager {
    private DBConfig config;
    private DiskManager diskManager;
    private Buffer[] buffers;  // Tableau de buffers
    private int currentIndex;  // Indice du buffer actuel pour la politique FIFO/Round-Robin
    private String replacementPolicy;  // Politique de remplacement (LRU, FIFO, etc.)

    // Constructeur
    public BufferManager(DBConfig config, DiskManager diskManager) {
        this.config = config;
        this.diskManager = diskManager;
        this.buffers = new Buffer[config.getBm_buffercount()];
        this.currentIndex = 0;
        this.replacementPolicy = config.getBm_policy();
    }
    
    public ByteBuffer loadPageFromDisk(PageId pageId) {
        ByteBuffer buffer = ByteBuffer.allocate(config.getPagesize());
        try {
            diskManager.ReadPage(pageId, buffer.array());  // Lire la page dans le ByteBuffer
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer;
    }
    
    public Buffer getPage(PageId pageId) {
    // Vérifier si la page est déjà dans un des buffers
        for (int i = 0; i < buffers.length; i++) {
            if (buffers[i] != null && buffers[i] instanceof ByteBuffer) {
                ByteBuffer buffer = (ByteBuffer) buffers[i];
                if (buffer.getInt(0) == pageId.getFileIdx() && buffer.getInt(4) == pageId.getPageIdx()) {
                    // La page est trouvée dans les buffers, on la retourne
                    return buffer;
                }
            }
        }

        // Si la page n'est pas trouvée, charger une nouvelle page
        ByteBuffer newPageBuffer = loadPageFromDisk(pageId);

        // Remplacer un buffer selon la politique FIFO
        buffers[currentIndex] = newPageBuffer;  // Remplacer le buffer à l'indice actuel
        currentIndex = (currentIndex + 1) % buffers.length;  // Passer au prochain buffer

        return newPageBuffer;
    }
    
    
    /*
    public void freePage(PageId pageId, boolean valdirty) {
        // Décrémenter le pin_count
        pages[pageId].pinCount--;

        // Mettre à jour le flag dirty
        pages[pageId].dirty = valdirty;
    }
    */

}
