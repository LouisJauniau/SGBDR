package up.mi.jgm.bdda;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/*
La classe joue un role sur la gestion des pages memoires sur le disque.
gere l'allocation, la desallocation, la lecture, l'ecriture de pages.
*/
public class DiskManager {
    private DBConfig config; //declaration d'un dbconfig
    private Set<PageId> freePages; //decla d'un ensemble de PageId
    private String dbPath; 
    private String binDataPath; //chemin vers les fichiers binaires de pages

    // Constructeur
    //verifie l'existence du dossier BinData et le crée si necessaire.
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
            // Trouver un fichier ou il reste de la place
            int fileIdx = 0;
            int pageIdx = 0;

            while (true) {
                String fileName = getFileName(fileIdx); //utilise la methode pour avoir le nom du fichier depuis son index
                RandomAccessFile file = new RandomAccessFile(fileName, "rw");
                //verifie si le fichier existe
                if (!new File(fileName).exists()) {
                    file.setLength(0); //initialise un fichier vide
                }

                long fileSize = file.length();
                int numPages = (int) (fileSize / config.getPageSize()); //calcul nombre de pages existantes

                if (numPages < config.getMaxPagesPerFile()) {
                    pageIdx = numPages; //premiere page libre dans ce fichier
                    //etendre le fichier pour inclure la nouvelle page
                    long newSize = (long) (pageIdx + 1) * config.getPageSize();
                    file.setLength(newSize);
                    file.close();
                    break;
                } else {
                    file.close();
                    fileIdx++; //passe au fcihier suivant
                }
            }

            //retourne la nouvelle PageId
            return new PageId(fileIdx, pageIdx);
        }
    }


    // Méthode de lecture de page
    public void ReadPage(PageId pageId, byte[] buff) throws IOException {
        String fileName = getFileName(pageId.getFileIdx());
        RandomAccessFile file = new RandomAccessFile(fileName, "r");

        //calcul d'offset: pos de depart de la page dans le fichier
        long offset = (long) pageId.getPageIdx() * config.getPageSize();
        file.seek(offset);
        int bytesRead = file.read(buff); //lecture des donnees dans le buffer
        //verif que toute la page a ete lue
        if (bytesRead != config.getPageSize()) {
            throw new IOException("Impossible de lire la page complète");
        }

        file.close();
    }

    // Méthode pour écrire une page
    public void WritePage(PageId pageId, byte[] buff) throws IOException {
        String fileName = getFileName(pageId.getFileIdx());
        RandomAccessFile file = new RandomAccessFile(fileName, "rw");
        //calcul de l'offset pour ecrire au bon emplacement
        long offset = (long) pageId.getPageIdx() * config.getPageSize();
        file.seek(offset);
        file.write(buff); //Ecriture des donnees

        file.close();
    }

    // Méthode pour désallouer une page
    public void DeallocPage(PageId pageId) {
        freePages.add(pageId); //ajout de la page dans l'ensemble des pages libres
    }

    //methode pour sauvegarder l'état dans un fichier dm.save
    public void SaveState() throws IOException {
        String saveFilePath = Paths.get(dbPath, "dm.save").toString();
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(saveFilePath));
        oos.writeObject(freePages); //sérialise l'ensemble des pages libres
        oos.close();
    }

    //methode pour charger l'état du fichier dm.save
    public void LoadState() throws IOException, ClassNotFoundException {
        String saveFilePath = Paths.get(dbPath, "dm.save").toString();
        File saveFile = new File(saveFilePath);
        if (saveFile.exists()) {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(saveFilePath));
            freePages = (Set<PageId>) ois.readObject(); //dé-sérialise les pages libres
            ois.close();
        } else {
            freePages = new HashSet<>(); //init ensemble vide
        }
    }

    // Méthode pour obtenir le nom du fichier correspondant à un indice
    private String getFileName(int fileIdx) {
        return Paths.get(binDataPath, "F" + fileIdx + ".rsdb").toString();
    }

    //eetourne la configuration associée a ce DiskManager
    public DBConfig getConfig() {
        return config;
    }
}
