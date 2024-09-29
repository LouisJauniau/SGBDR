package sgbd;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class Tests {
    public static void main(String[] args) {
        // Test du constructeur
        DBConfig config = new DBConfig("path/to/db", 4096, 1048576);
        DiskManager diskManager = new DiskManager(config);
        System.out.println("Constructeur DiskManager : " + config.getDbpath() + ", " + config.getPagesize() + ", " + config.getDm_maxfilesize());

        // Test de l'allocation de page
        PageId pageId1 = diskManager.AllocPage();
        System.out.println("AllocPage : " + pageId1.getFileIdx() + ", " + pageId1.getPageIdx());

        // Test de la désallocation de page
        diskManager.DeallocPage(pageId1);
        System.out.println("DeallocPage : " + pageId1.getFileIdx() + ", " + pageId1.getPageIdx());

        // Test de la réallocation de page
        PageId pageId2 = diskManager.AllocPage();
        System.out.println("Re-AllocPage : " + pageId2.getFileIdx() + ", " + pageId2.getPageIdx());

        // Test de l'écriture et de la lecture de page
        byte[] writeBuff = new byte[config.getPagesize()];
        byte[] readBuff = new byte[config.getPagesize()];
        Arrays.fill(writeBuff, (byte) 1);

        try {
            diskManager.WritePage(pageId2, writeBuff);
            diskManager.ReadPage(pageId2, readBuff);
            System.out.println("WritePage et ReadPage : " + Arrays.equals(writeBuff, readBuff));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Test de la sauvegarde et du chargement de l'état
        diskManager.SaveState();
        DiskManager newDiskManager = new DiskManager(config);
        newDiskManager.LoadState();
        System.out.println("SaveState et LoadState : " + newDiskManager.AllocPage().getFileIdx() + ", " + newDiskManager.AllocPage().getPageIdx());

        // Test de la méthode LoadDBConfig
        try {
            // Créer un fichier de configuration temporaire pour le test
            String content = "{\"dbpath\":\"/test/db\",\"pagesize\":4096,\"dm_maxfilesize\":1048576}";
            Files.write(Paths.get("test_config.json"), content.getBytes());

            // Charger la configuration à partir du fichier
            DBConfig loadedConfig = DBConfig.LoadDBConfig("test_config.json");
            if (loadedConfig != null) {
                System.out.println("LoadDBConfig : " + loadedConfig.getDbpath() + ", " + loadedConfig.getPagesize() + ", " + loadedConfig.getDm_maxfilesize());
            } else {
                System.out.println("LoadDBConfig a échoué.");
            }

            // Supprimer le fichier de configuration temporaire
            Files.delete(Paths.get("test_config.json"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
