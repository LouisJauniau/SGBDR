package up.mi.jgm.td1;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class DiskManager {
    private DBConfig config;
    private List<PageId> freePages;
    private int currentFileIdx;
    private int currentPageIdx;

    // Constructeur qui prend en argument une instance de DBConfig
    public DiskManager(DBConfig config) {
        this.config = config;
        this.freePages = new ArrayList<>();
        this.currentFileIdx = 0;
        this.currentPageIdx = 0;
        LoadState();
    }

    // Méthode pour allouer une page
    public PageId AllocPage() {
        if (!freePages.isEmpty()) {
            return freePages.remove(freePages.size() - 1);
        } else {
            if (currentPageIdx * config.getPagesize() >= config.getDm_maxfilesize()) {
                currentFileIdx++;
                currentPageIdx = 0;
            }
            PageId newPageId = new PageId(currentFileIdx, currentPageIdx);
            currentPageIdx++;
            return newPageId;
        }
    }

    // Méthode pour lire une page
    public void ReadPage(PageId pageId, byte[] buff) throws IOException {
        String filePath = config.getDbpath() + "/F" + pageId.getFileIdx();
        try (RandomAccessFile file = new RandomAccessFile(filePath, "r")) {
            file.seek(pageId.getPageIdx() * config.getPagesize());
            file.read(buff);
        }
    }

    // Méthode pour écrire une page
    public void WritePage(PageId pageId, byte[] buff) throws IOException {
        String filePath = config.getDbpath() + "/F" + pageId.getFileIdx();
        try (RandomAccessFile file = new RandomAccessFile(filePath, "rw")) {
            file.seek(pageId.getPageIdx() * config.getPagesize());
            file.write(buff);
        }
    }

    // Méthode pour désallouer une page
    public void DeallocPage(PageId pageId) {
        freePages.add(pageId);
    }

    // Méthode pour sauvegarder l'état
    public void SaveState() {
        String filePath = config.getDbpath() + "/dm.save";
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(freePages);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Méthode pour charger l'état
    public void LoadState() {
        String filePath = config.getDbpath() + "/dm.save";
        if (Files.exists(Paths.get(filePath))) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
                freePages = (List<PageId>) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
