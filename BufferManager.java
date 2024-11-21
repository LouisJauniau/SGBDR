package up.mi.jgm.td3;

import java.io.IOException;

public class BufferManager {
    private DBConfig config;
    private DiskManager diskManager;

    // Représentation d'un buffer (frame)
    private class Frame {
        PageId pageId;
        byte[] data;
        int pinCount;
        boolean dirty;
        long lastUsedTime; // Pour LRU/MRU

        Frame(int pageSize) {
            this.pageId = null;
            this.data = new byte[pageSize];
            this.pinCount = 0;
            this.dirty = false;
            this.lastUsedTime = 0;
        }
    }

    private Frame[] buffers;
    private int bufferCount;
    private String replacementPolicy;

    // Constructeur
    public BufferManager(DBConfig config, DiskManager diskManager) {
        this.config = config;
        this.diskManager = diskManager;
        this.bufferCount = config.getBufferCount();
        this.replacementPolicy = config.getReplacementPolicy();

        // Initialiser les buffers
        this.buffers = new Frame[bufferCount];
        for (int i = 0; i < bufferCount; i++) {
            buffers[i] = new Frame(config.getPageSize());
        }
    }

    // Méthode pour obtenir une page
    public byte[] GetPage(PageId pageId) throws IOException {
        // Vérifier si la page est déjà en mémoire
        for (Frame frame : buffers) {
            if (pageId.equals(frame.pageId)) {
                frame.pinCount++;
                frame.lastUsedTime = System.currentTimeMillis();
                return frame.data;
            }
        }

        // Chercher un buffer libre
        for (Frame frame : buffers) {
            if (frame.pinCount == 0 && frame.pageId == null) {
                // Charger la page depuis le disque
                diskManager.ReadPage(pageId, frame.data);
                frame.pageId = pageId;
                frame.pinCount = 1;
                frame.dirty = false;
                frame.lastUsedTime = System.currentTimeMillis();
                return frame.data;
            }
        }

        // Appliquer la politique de remplacement
        Frame victim = selectVictim();
        if (victim == null) {
            throw new IllegalStateException("Aucun buffer disponible pour le remplacement.");
        }

        // Écrire la page sur disque si elle est sale
        if (victim.dirty) {
            diskManager.WritePage(victim.pageId, victim.data);
        }

        // Charger la nouvelle page
        diskManager.ReadPage(pageId, victim.data);
        victim.pageId = pageId;
        victim.pinCount = 1;
        victim.dirty = false;
        victim.lastUsedTime = System.currentTimeMillis();

        return victim.data;
    }

    // Méthode pour libérer une page
    public void FreePage(PageId pageId, boolean isDirty) {
        for (Frame frame : buffers) {
            if (pageId.equals(frame.pageId)) {
                frame.pinCount--;
                if (isDirty) {
                    frame.dirty = true;
                }
                // Mise à jour de lastUsedTime pour MRU
                frame.lastUsedTime = System.currentTimeMillis();
                return;
            }
        }
        // Si la page n'est pas trouvée
        throw new IllegalArgumentException("La page " + pageId + " n'est pas chargée en mémoire.");
    }

    // Méthode pour changer la politique de remplacement
    public void SetCurrentReplacementPolicy(String policy) {
        this.replacementPolicy = policy;
    }

    // Méthode pour vider les buffers
    public void FlushBuffers() throws IOException {
        for (Frame frame : buffers) {
            if (frame.dirty && frame.pageId != null) {
                diskManager.WritePage(frame.pageId, frame.data);
            }
            // Réinitialiser le buffer
            frame.pageId = null;
            frame.pinCount = 0;
            frame.dirty = false;
            frame.lastUsedTime = 0;
        }
    }

    // Sélectionner une victime selon la politique de remplacement
    private Frame selectVictim() {
        Frame victim = null;
        long time = (replacementPolicy.equals("LRU")) ? Long.MAX_VALUE : Long.MIN_VALUE;

        for (Frame frame : buffers) {
            if (frame.pinCount == 0) {
                if (replacementPolicy.equals("LRU")) {
                    if (frame.lastUsedTime < time) {
                        time = frame.lastUsedTime;
                        victim = frame;
                    }
                } else if (replacementPolicy.equals("MRU")) {
                    if (frame.lastUsedTime > time) {
                        time = frame.lastUsedTime;
                        victim = frame;
                    }
                }
            }
        }
        return victim;
    }
}
