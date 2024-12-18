package up.mi.jgm.bdda;

import java.io.IOException;
import java.nio.ByteBuffer;

public class BufferManagerTest {
    public static void main(String[] args) {
        try {
            testGetPage();
            testFreePageDirty();
            System.out.println("Tous les tests BufferManager ont été exécutés avec succès.");
        } catch (AssertionError e) {
            System.err.println("Echec d'un test BufferManager : " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void testGetPage() throws IOException, ClassNotFoundException {
        System.out.println("=== Test GetPage ===");
        DBConfig config = DBConfig.loadDBConfig("config.json");
        DiskManager dm = new DiskManager(config);
        dm.LoadState();
        BufferManager bm = new BufferManager(config, dm);

        PageId pid = dm.AllocPage();
        ByteBuffer pageBuffer = bm.GetPage(pid);
        assert pageBuffer != null : "GetPage a retourné null";
        System.out.println("Test GetPage réussi.");
    }

    private static void testFreePageDirty() throws IOException, ClassNotFoundException {
        System.out.println("=== Test FreePage avec dirty ===");
        DBConfig config = DBConfig.loadDBConfig("config.json");
        DiskManager dm = new DiskManager(config);
        dm.LoadState();
        BufferManager bm = new BufferManager(config, dm);

        PageId pid = dm.AllocPage();
        ByteBuffer pageBuffer = bm.GetPage(pid);
        pageBuffer.put(0, (byte) 55); // écrit la valeur 55 à l'offset 0

        bm.FreePage(pid, true); // Indiquer que la page est sale (modifiée)

        // Relire la page pour vérifier si la modif est persistée
        ByteBuffer newPageBuffer = bm.GetPage(pid);
        byte value = newPageBuffer.get(0);
        assert value == 55 : "La page n'a pas été mise à jour correctement sur disque, valeur lue: " + value;
        System.out.println("Test FreePage avec dirty réussi.");
    }
}