package up.mi.jgm.bdda;

import java.io.IOException;

public class DiskManagerTest {
    public static void main(String[] args) {
        try {
            // Charger la configuration
            DBConfig config = DBConfig.loadDBConfig("config.json");

            // Créer une instance de DiskManager
            DiskManager dm = new DiskManager(config);

            // Charger l'état précédent
            dm.LoadState();

            // Test d'allocation de pages
            System.out.println("=== Test d'allocation de pages ===");
            PageId page1 = dm.AllocPage();
            System.out.println("Page allouée : " + page1);

            PageId page2 = dm.AllocPage();
            System.out.println("Page allouée : " + page2);

            // Test d'écriture et de lecture
            System.out.println("\n=== Test d'écriture et de lecture ===");
            byte[] bufferWrite = new byte[config.getPageSize()];
            byte[] bufferRead = new byte[config.getPageSize()];

            // Remplir le buffer avec des données
            for (int i = 0; i < bufferWrite.length; i++) {
                bufferWrite[i] = (byte) (i % 256);
            }

            // Écrire dans la page
            dm.WritePage(page1, bufferWrite);
            System.out.println("Données écrites dans la page " + page1);

            // Lire depuis la page
            dm.ReadPage(page1, bufferRead);
            System.out.println("Données lues depuis la page " + page1);

            // Vérifier que les données sont identiques
            boolean dataMatch = true;
            for (int i = 0; i < bufferWrite.length; i++) {
                if (bufferWrite[i] != bufferRead[i]) {
                    dataMatch = false;
                    break;
                }
            }
            System.out.println("Les données lues sont identiques aux données écrites : " + dataMatch);

            // Test de désallocation
            System.out.println("\n=== Test de désallocation ===");
            dm.DeallocPage(page1);
            System.out.println("Page désallouée : " + page1);

            // Sauvegarder l'état
            dm.SaveState();
            System.out.println("État sauvegardé.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
