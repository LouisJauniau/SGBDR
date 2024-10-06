package sgbd;

import org.json.JSONObject;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

public class DBConfig {
    private String dbpath;
    private int pagesize;
    private int dm_maxfilesize;
    
    // Nouvelles variables membres
    private int bm_buffercount;
    private String bm_policy;

    // Constructeur mis à jour
    public DBConfig(String dbpath, int pagesize, int dm_maxfilesize, int bm_buffercount, String bm_policy) {
        this.dbpath = dbpath;
        this.pagesize = pagesize;
        this.dm_maxfilesize = dm_maxfilesize;
        this.bm_buffercount = bm_buffercount;
        this.bm_policy = bm_policy;
    }

    // Méthode statique mise à jour pour charger la configuration à partir d'un fichier texte
    public static DBConfig LoadDBConfig(String fichier_config) {
        try {
            String content = new String(Files.readAllBytes(Paths.get(fichier_config)));
            JSONObject json = new JSONObject(content);
            
            String dbpath = json.getString("dbpath");
            int pagesize = json.getInt("pagesize");
            int dm_maxfilesize = json.getInt("dm_maxfilesize");
            int bm_buffercount = json.getInt("bm_buffercount");
            String bm_policy = json.getString("bm_policy");

            return new DBConfig(dbpath, pagesize, dm_maxfilesize, bm_buffercount, bm_policy);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

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

    // Nouveaux getters et setters pour bm_buffercount et bm_policy
    public int getBm_buffercount() {
        return bm_buffercount;
    }

    public void setBm_buffercount(int bm_buffercount) {
        this.bm_buffercount = bm_buffercount;
    }

    public String getBm_policy() {
        return bm_policy;
    }

    public void setBm_policy(String bm_policy) {
        this.bm_policy = bm_policy;
    }
}
