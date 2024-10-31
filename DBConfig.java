package up.mi.jgm.bdda;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.json.JSONObject;
import org.json.JSONException;

public class DBConfig {
    private String dbpath;
    private int maxConnections;
    private int pageSize;
    private int maxPagesPerFile;
    private int bufferCount;
    private String replacementPolicy;

    // Constructeur
    public DBConfig(String dbpath, int maxConnections, int pageSize, int maxPagesPerFile, int bufferCount, String replacementPolicy) {
        this.dbpath = dbpath;
        this.maxConnections = maxConnections;
        this.pageSize = pageSize;
        this.maxPagesPerFile = maxPagesPerFile;
        this.bufferCount = bufferCount;
        this.replacementPolicy = replacementPolicy;
    }

    // Getters
    public String getDbpath() {
        return dbpath;
    }

    public int getMaxConnections() {
        return maxConnections;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getMaxPagesPerFile() {
        return maxPagesPerFile;
    }

    public int getBufferCount() {
        return bufferCount;
    }

    public String getReplacementPolicy() {
        return replacementPolicy;
    }

    // Méthode statique pour charger la configuration depuis un fichier JSON
    public static DBConfig loadDBConfig(String fichierConfig) throws IOException {
        try {
            // Lire tout le contenu du fichier
            String content = new String(Files.readAllBytes(Paths.get(fichierConfig)));

            // Créer un objet JSON à partir du contenu
            JSONObject json = new JSONObject(content);

            // Vérifier si 'dbpath' est présent dans le fichier JSON
            if (!json.has("dbpath")) {
                throw new IllegalArgumentException("Le fichier de configuration doit contenir 'dbpath'.");
            }

            // Extraire les valeurs des paramètres
            String dbpath = json.getString("dbpath");
            int maxConnections = json.optInt("maxConnections", 5); // Valeur par défaut de 5 si non spécifié
            int pageSize = json.optInt("pageSize", 4096); // Taille de page par défaut
            int maxPagesPerFile = json.optInt("maxPagesPerFile", 1000); // Valeur par défaut
            int bufferCount = json.optInt("bm_buffercount", 5); // Nombre de buffers par défaut
            String replacementPolicy = json.optString("bm_policy", "LRU"); // Politique par défaut

            return new DBConfig(dbpath, maxConnections, pageSize, maxPagesPerFile, bufferCount, replacementPolicy);

        } catch (JSONException e) {
            throw new IllegalArgumentException("Le fichier de configuration est mal formé : " + e.getMessage());
        }
    }
}
