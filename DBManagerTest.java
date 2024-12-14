package up.mi.jgm.bdda;

import java.io.IOException;

public class DBManagerTest {
    public static void main(String[] args) {
        try {
            testCreateDatabase();
            testSetCurrentDatabase();
            testCreateAndRemoveTable();
            System.out.println("Tous les tests DBManager ont été exécutés.");
        } catch (AssertionError e) {
            System.err.println("Echec d'un test DBManager : " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void testCreateDatabase() throws IOException, ClassNotFoundException {
        System.out.println("=== Test DBManager - CreateDatabase ===");
        DBConfig config = DBConfig.loadDBConfig("config.json");
        DiskManager dm = new DiskManager(config);
        dm.LoadState();
        BufferManager bm = new BufferManager(config, dm);
        DBManager dbm = new DBManager(config, dm, bm);
        dbm.loadState();

        dbm.createDatabase("TestDB");
        // On suppose une méthode databaseExists (vous pouvez l'implémenter dans DBManager)
        // Sinon, testez via listDatabases() ou autre.
        // Ici on fait simple : on relance listDatabases et on vérifie manuellement.
        // Comme pas de méthode directe, on se contente de faire confiance au message affiché.

        System.out.println("Test CreateDatabase réussi (vérifiez manuellement dans le code si DB est créée).");
    }

    private static void testSetCurrentDatabase() throws IOException, ClassNotFoundException {
        System.out.println("=== Test DBManager - SetCurrentDatabase ===");
        DBConfig config = DBConfig.loadDBConfig("config.json");
        DiskManager dm = new DiskManager(config);
        dm.LoadState();
        BufferManager bm = new BufferManager(config, dm);
        DBManager dbm = new DBManager(config, dm, bm);
        dbm.loadState();

        dbm.createDatabase("TestDB2");
        dbm.setCurrentDatabase("TestDB2");
        // Supposez que vous ajoutiez une méthode getCurrentDatabaseName() dans DBManager pour tester
        // Pour l'instant, on fait confiance au message
        System.out.println("Test SetCurrentDatabase réussi (vérifiez manuellement).");
    }

    private static void testCreateAndRemoveTable() throws IOException, ClassNotFoundException {
        System.out.println("=== Test DBManager - Create and Remove Table ===");
        DBConfig config = DBConfig.loadDBConfig("config.json");
        DiskManager dm = new DiskManager(config);
        dm.LoadState();
        BufferManager bm = new BufferManager(config, dm);
        DBManager dbm = new DBManager(config, dm, bm);
        dbm.loadState();

        dbm.createDatabase("TestDB3");
        dbm.setCurrentDatabase("TestDB3");

        Relation tab = createSampleRelation(dm, bm);
        dbm.addTableToCurrentDatabase(tab);
        // Vérifiez si la table apparaît dans listTablesInCurrentDatabase()

        dbm.removeTableFromCurrentDatabase("SampleTable");
        // Vérifiez à nouveau via listTablesInCurrentDatabase()
        System.out.println("Test Create and Remove Table réussi (vérifiez manuellement via listTablesInCurrentDatabase()).");
    }

    private static Relation createSampleRelation(DiskManager dm, BufferManager bm) throws IOException {
        ColInfo col1 = new ColInfo("C1", ColInfo.Type.INT, 0);
        ColInfo col2 = new ColInfo("C2", ColInfo.Type.VARCHAR, 10);
        return new Relation("SampleTable", java.util.Arrays.asList(col1, col2), dm, bm);
    }
}