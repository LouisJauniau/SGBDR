package up.mi.jgm.td3;

import java.util.HashMap;
import java.util.Map;

public class DBManager {
    private DBConfig dbConfig;
    private Map<String, Map<String, Relation>> databases;
    private String currentDatabase;

    public DBManager(DBConfig dbConfig) {
        this.dbConfig = dbConfig;
        this.databases = new HashMap<>();
    }

    // Créer une base de données
    public void createDatabase(String nomBdd) {
        if (!databases.containsKey(nomBdd)) {
            databases.put(nomBdd, new HashMap<>());
            System.out.println("Base de données '" + nomBdd + "' créée avec succès.");
        } else {
            System.out.println("La base de données '" + nomBdd + "' existe déjà.");
        }
    }



    // Sélectionner la base de données active
    public void setCurrentDatabase(String nomBdd) {
        if (databases.containsKey(nomBdd)) {
            currentDatabase = nomBdd;
            System.out.println("Base de données actuelle définie sur '" + nomBdd + "'.");
        } else {
            throw new IllegalArgumentException("La base de données " + nomBdd + " n'existe pas");
        }
    }

    // Ajouter une table à la base de données active
    public void addTableToCurrentDatabase(Relation tab) {
        if (currentDatabase != null) {
            Map<String, Relation> tables = databases.get(currentDatabase);
            tables.put(tab.getName(), tab);
            System.out.println("Table '" + tab.getName() + "' ajoutée à la base de données '" + currentDatabase + "'.");
        } else {
            System.out.println("Aucune base de données actuelle définie.");
        }
    }
}