package up.mi.jgm.td1;

import org.json.JSONObject;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

public class DBConfig {
    private String dbpath;
    private int pagesize;
    private int dm_maxfilesize;

    // Constructeur qui prend en argument les paramètres dbpath, pagesize et dm_maxfilesize
    public DBConfig(String dbpath, int pagesize, int dm_maxfilesize) {
        this.dbpath = dbpath;
        this.pagesize = pagesize;
        this.dm_maxfilesize = dm_maxfilesize;
    }

    // Méthode statique pour charger la configuration à partir d'un fichier texte
    public static DBConfig LoadDBConfig(String fichier_config) {
        try {
            // Lire le contenu du fichier
            String content = new String(Files.readAllBytes(Paths.get(fichier_config)));
            // Convertir le contenu en objet JSON
            JSONObject json = new JSONObject(content);
            // Extraire les paramètres du JSON
            String dbpath = json.getString("dbpath");
            int pagesize = json.getInt("pagesize");
            int dm_maxfilesize = json.getInt("dm_maxfilesize");
            // Créer et retourner une instance de DBConfig
            return new DBConfig(dbpath, pagesize, dm_maxfilesize);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Getters et setters pour dbpath, pagesize et dm_maxfilesize
    public String getDbpath() {
        return dbpath;
    }

    public void setDbpath(String dbpath) {
        this.dbpath = dbpath;
    }

    public int getPagesize() {
        return pagesize;
    }

    public void setPagesize(int pagesize) {
        this.pagesize = pagesize;
    }

    public int getDm_maxfilesize() {
        return dm_maxfilesize;
    }

    public void setDm_maxfilesize(int dm_maxfilesize) {
        this.dm_maxfilesize = dm_maxfilesize;
    }
}