package up.mi.jgm.td3;

import java.io.IOException;

public class DBConfigTest {
    public static void main(String[] args) {
        // Test 1 : Création directe
        DBConfig config1 = new DBConfig("../DB", 10, 4096, 1000, 3, "LRU");
        System.out.println("Test 1 - dbpath : " + config1.getDbpath());
        System.out.println("Test 1 - maxConnections : " + config1.getMaxConnections());

        // Test 2 : Chargement depuis un fichier JSON valide
        try {
            DBConfig config2 = DBConfig.loadDBConfig("config.json");
            System.out.println("Test 2 - dbpath : " + config2.getDbpath());
            System.out.println("Test 2 - maxConnections : " + config2.getMaxConnections());
        } catch (IOException e) {
            System.err.println("Test 2 - Erreur lors du chargement : " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("Test 2 - Erreur de configuration : " + e.getMessage());
        }

        // Test 3 : Chargement depuis un fichier JSON invalide
        try {
            DBConfig config3 = DBConfig.loadDBConfig("config_invalide.json");
            System.out.println("Test 3 - dbpath : " + config3.getDbpath());
            System.out.println("Test 3 - maxConnections : " + config3.getMaxConnections());
        } catch (Exception e) {
            System.err.println("Test 3 - Erreur attendue : " + e.getMessage());
        }

        // Test 4 : Fichier inexistant
        try {
            DBConfig config4 = DBConfig.loadDBConfig("inexistant.json");
            System.out.println("Test 4 - dbpath : " + config4.getDbpath());
            System.out.println("Test 4 - maxConnections : " + config4.getMaxConnections());
        } catch (Exception e) {
            System.err.println("Test 4 - Erreur attendue : " + e.getMessage());
        }
    }
}
