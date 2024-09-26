import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class DBConfig {
    private String dbpath;
    private int pagesize;
    private int dm_maxfilesize;

    public DBConfig(String dbpath, int pagesize, int dm_maxfilesize) {
        this.dbpath = dbpath;
        this.pagesize = pagesize;
        this.dm_maxfilesize = dm_maxfilesize;
    }

    public static DBConfig loadDBConfig(String fichierConfig) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(fichierConfig));
        String line;
        String dbpath = null;
        int pagesize = 0;
        int dm_maxfilesize = 0;

        while ((line = reader.readLine()) != null) {
            if (line.startsWith("dbpath")) {
                dbpath = line.split("=")[1].trim();
        	} else if (line.startsWith("pagesize")) {
        		pagesize = Integer.parseInt(line.split("=")[1].trim());
        	} else if (line.startsWith("dm_maxfilesize")) {
        		dm_maxfilesize = Integer.parseInt(line.split("=")[1].trim());
        	}
        }
        reader.close();

        return new DBConfig(dbpath, pagesize, dm_maxfilesize);
    }

    public String getDbpath() {
        return dbpath;
    }
    
    /*public static void main(String[] args) {
        testCreationEnMemoire();

        testCreationViaFichierTexte();

        testCasErreur();
    }

    public static void testCreationEnMemoire() {
        DBConfig config = new DBConfig("../DB");
        assert "../DB".equals(config.getDbpath()) : "Test échoué : dbpath incorrect";
        System.out.println("Test 1 réussi : Création d'une instance en mémoire");
    }

   /* public static void testCreationViaFichierTexte() {
        try {
            String fichierConfig = "config.txt";
            java.nio.file.Files.write(java.nio.file.Paths.get(fichierConfig), "dbpath = '../DB'".getBytes());

            DBConfig config = DBConfig.loadDBConfig(fichierConfig);
            assert "../DB".equals(config.getDbpath()) : "Test échoué : dbpath incorrect";
            System.out.println("Test 2 réussi : Création d'une instance via un fichier texte");

            java.nio.file.Files.delete(java.nio.file.Paths.get(fichierConfig));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void testCasErreur() {
        try {
            DBConfig config = DBConfig.loadDBConfig("fichier_inexistant.txt");
            System.out.println("Test échoué : Exception attendue mais non levée");
        } catch (IOException e) {
            System.out.println("Test 3 réussi : Cas d'erreur géré correctement");
        }
    }*/
}
