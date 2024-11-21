package up.mi.jgm.td3;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class DiskManager {
    private DBConfig config;
    private Set<PageId> freePages;
    private String dbPath;
    private String binDataPath;

    // Constructeur
    public DiskManager(DBConfig config) {
        this.config = config;
        this.dbPath = config.getDbpath();
        this.binDataPath = Paths.get(dbPath, "BinData").toString();
        this.freePages = new HashSet<>();

        // Créer le dossier BinData s'il n'existe pas
        File binDataDir = new File(binDataPath);
        if (!binDataDir.exists()) {
            binDataDir.mkdirs();
        }
    }

    // Méthode pour allouer une page
    public PageId AllocPage() throws IOException {
        if (!freePages.isEmpty()) {
            // Récupérer une page libre
            Iterator<PageId> iterator = freePages.iterator();
            PageId pageId = iterator.next();
            iterator.remove();
            return pageId;
        } else {
            // Trouver un fichier qui n'est pas plein
            int fileIdx = 0;
            int pageIdx = 0;

            while (true) {
                String fileName = getFileName(fileIdx);
                RandomAccessFile file = new RandomAccessFile(fileName, "rw");
                if (!new File(fileName).exists()) {
                    // Le fichier n'existe pas, le créer
                    file.setLength(0);
                }

                long fileSize = file.length();
                int numPages = (int) (fileSize / config.getPageSize());

                if (numPages < config.getMaxPagesPerFile()) {
                    pageIdx = numPages;
                    // Étendre le fichier pour inclure la nouvelle page
                    long newSize = (long) (pageIdx + 1) * config.getPageSize();
                    file.setLength(newSize);
                    file.close();
                    break;
                } else {
                    file.close();
                    fileIdx++;
                }
            }

            // Retourner le nouveau PageId
            return new PageId(fileIdx, pageIdx);
        }
    }


    // Méthode pour lire une page
    public void ReadPage(PageId pageId, byte[] buff) throws IOException {
        String fileName = getFileName(pageId.getFileIdx());
        RandomAccessFile file = new RandomAccessFile(fileName, "r");

        long offset = (long) pageId.getPageIdx() * config.getPageSize();
        file.seek(offset);
        int bytesRead = file.read(buff);
        if (bytesRead != config.getPageSize()) {
            throw new IOException("Impossible de lire la page complète");
        }

        file.close();
    }

    // Méthode pour écrire une page
    public void WritePage(PageId pageId, byte[] buff) throws IOException {
        String fileName = getFileName(pageId.getFileIdx());
        RandomAccessFile file = new RandomAccessFile(fileName, "rw");

        long offset = (long) pageId.getPageIdx() * config.getPageSize();
        file.seek(offset);
        file.write(buff);

        file.close();
    }

    // Méthode pour désallouer une page
    public void DeallocPage(PageId pageId) {
        freePages.add(pageId);
    }

    // Méthode pour sauvegarder l'état
    public void SaveState() throws IOException {
        String saveFilePath = Paths.get(dbPath, "dm.save").toString();
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(saveFilePath));
        oos.writeObject(freePages);
        oos.close();
    }

    // Méthode pour charger l'état
    public void LoadState() throws IOException, ClassNotFoundException {
        String saveFilePath = Paths.get(dbPath, "dm.save").toString();
        File saveFile = new File(saveFilePath);
        if (saveFile.exists()) {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(saveFilePath));
            freePages = (Set<PageId>) ois.readObject();
            ois.close();
        } else {
            freePages = new HashSet<>();
        }
    }

    // Méthode utilitaire pour obtenir le nom du fichier correspondant à un indice
    private String getFileName(int fileIdx) {
        return Paths.get(binDataPath, "F" + fileIdx + ".rsdb").toString();
    }
    
    public DBConfig getConfig() {
        return config;
    }
}
