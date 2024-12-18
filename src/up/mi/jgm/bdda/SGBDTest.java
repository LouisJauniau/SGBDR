package up.mi.jgm.bdda;

public class SGBDTest {
    public static void main(String[] args) {
        try {
            testProcessCommand();
            System.out.println("Tous les tests SGBD ont été exécutés.");
        } catch (AssertionError e) {
            System.err.println("Echec d'un test SGBD : " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void testProcessCommand() throws Exception {
        System.out.println("=== Test SGBD - ProcessCommand ===");
        DBConfig dbConfig = DBConfig.loadDBConfig("config.json");
        SGBD sgbd = new SGBD(dbConfig);

        System.out.println("Test ProcessCommand à adapter en fonction de l'implémentation de SGBD.");
    }
}