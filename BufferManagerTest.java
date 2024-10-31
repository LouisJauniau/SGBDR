package up.mi.jgm.bdda;

import java.io.IOException;

public class BufferManagerTest {
    public static void main(String[] args) {
        try {
            // Charger la configuration
            DBConfig config = DBConfig.loadDBConfig("config.json");

            // Créer une instance de DiskManager
            DiskManager dm = new DiskManager(config);

            // Charger l'état précédent du DiskManager
            dm.LoadState();

            // Créer une instance de BufferManager
            BufferManager bm = new BufferManager(config, dm);

            // Test de la politique de remplacement LRU
            System.out.println("=== Test du BufferManager avec politique LRU ===");

            // Allouer quelques pages
            PageId page1 = dm.AllocPage();
            PageId page2 = dm.AllocPage();
            PageId page3 = dm.AllocPage();
            PageId page4 = dm.AllocPage();

            // Écrire des données dans les pages
            writeDataToPage(bm, page1, (byte) 1);
            writeDataToPage(bm, page2, (byte) 2);
            writeDataToPage(bm, page3, (byte) 3);

            // Accéder aux pages pour influencer LRU
            bm.GetPage(page1);
            bm.FreePage(page1, false);

            bm.GetPage(page2);
            bm.FreePage(page2, false);

            // Cette opération devrait déclencher le remplacement selon LRU
            writeDataToPage(bm, page4, (byte) 4);

            // Vérifier le contenu des pages
            System.out.println("Contenu de la page 1 : " + readDataFromPage(bm, page1));
            System.out.println("Contenu de la page 2 : " + readDataFromPage(bm, page2));
            System.out.println("Contenu de la page 3 : " + readDataFromPage(bm, page3));
            System.out.println("Contenu de la page 4 : " + readDataFromPage(bm, page4));

            // Changer la politique de remplacement à MRU
            bm.SetCurrentReplacementPolicy("MRU");
            System.out.println("\n=== Politique de remplacement changée à MRU ===");

            // Écrire dans une nouvelle page pour déclencher le remplacement
            PageId page5 = dm.AllocPage();
            writeDataToPage(bm, page5, (byte) 5);

            // Vérifier le contenu des pages
            System.out.println("Contenu de la page 1 : " + readDataFromPage(bm, page1));
            System.out.println("Contenu de la page 2 : " + readDataFromPage(bm, page2));
            System.out.println("Contenu de la page 3 : " + readDataFromPage(bm, page3));
            System.out.println("Contenu de la page 4 : " + readDataFromPage(bm, page4));
            System.out.println("Contenu de la page 5 : " + readDataFromPage(bm, page5));

            // Vider les buffers
            bm.FlushBuffers();

            // Sauvegarder l'état du DiskManager
            dm.SaveState();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Méthode utilitaire pour écrire des données dans une page
    private static void writeDataToPage(BufferManager bm, PageId pageId, byte value) throws IOException {
        byte[] data = bm.GetPage(pageId);
        for (int i = 0; i < data.length; i++) {
            data[i] = value;
        }
        bm.FreePage(pageId, true);
    }

    // Méthode utilitaire pour lire des données depuis une page
    private static byte readDataFromPage(BufferManager bm, PageId pageId) throws IOException {
        byte[] data = bm.GetPage(pageId);
        byte value = data[0]; // Supposons que toute la page contient la même valeur
        bm.FreePage(pageId, false);
        return value;
    }
}
