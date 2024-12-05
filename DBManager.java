package up.mi.jgm.td3;

//nom du fichier apparait dans le script pas en argument
//main generique qui gere des fichiers de n'importe quel nom
//pas forcement bash
//classe relation lecture ecriture passe pas buffermanager

import java.util.*;
import java.util.HashMap;
import java.util.Map;

/*
Classe permettant d'instancier un gestionnaire de database et permet de
plusieurs databases qu'on peut creer et supprimer
 des commandes minimalistes en simulant des commandes SQL
Classe importante qui sera appelée par la classe principale
SGBD où seront appelées les methodes d'ici.
 */
public class DBManager {
    private DBConfig dbConfig; //créé un DBConfig qui sera associé à cette instance de DBManager
    /*
    créé une database qui est un
    Dictionnaire(chaine de caractères, Dictionnaire(chaine de caracteres, Relation));
    Se referer à la classe Relation.
     */
    private Map<String, Map<String, Relation>> databases;

    //Nom de la database courante.
    private String currentDatabase;

    //Constructeur
    public DBManager() {
        this.dbConfig = dbConfig;
        this.databases = new HashMap<>();
    }

    // Création database.
    public void createDatabase(String nomBdd) {
        //Si la database de nom nomBdd n'existe pas
        if (!databases.containsKey(nomBdd)) {
            databases.put(nomBdd, new HashMap<>());
            System.out.println("Base de données '" + nomBdd + "' créée avec succès.");
        }
        //Si la database de nom nomBdd existe
        else {
            System.out.println("La base de données '" + nomBdd + "' existe déjà.");
        }
    }



    // Sélectionner database courante
    public void setCurrentDatabase(String nomBdd) throws IllegalArgumentException {
        //Si la database nommée nomBdd existe
        if (databases.containsKey(nomBdd)) {
            currentDatabase = nomBdd;
            System.out.println("Base de données actuelle définie sur '" + nomBdd + "'.");

        //Sinon exception
        } else {
            throw new IllegalArgumentException("La base de données " + nomBdd + " n'existe pas");
        }
    }

    // Ajouter une table à la database courante
    public void addTableToCurrentDatabase(Relation tab) {
        if (currentDatabase != null) {
            Map<String, Relation> tables = databases.get(currentDatabase);
            tables.put(tab.getName(), tab);
            System.out.println("Table '" + tab.getName() + "' ajoutée à la base de données '" + currentDatabase + "'.");
        } else {
            System.out.println("Aucune database courante actuelle.");
        }
    }

    // Retourne la relation nommée nomTable dans la database courante.
    public Relation getTableFromCurrentDatabase(String nomTable) {
        //Si la db courante existe
        if (currentDatabase != null) {
            Map<String, Relation> tables = databases.get(currentDatabase);
            if (tables.containsKey(nomTable)) {
                return tables.get(nomTable);
            } else {
                System.out.println("La table '" + nomTable + "' n'existe pas dans la base de données courante '" + currentDatabase + "'.");
                return null;
            }
        } else {
            System.out.println("Aucune base de données courante n'est définie.");
            return null;
        }
    }

    // Supprime une table nommée nomTable dans la db courante.
    public void removeTableFromCurrentDatabase(String nomTable) {
        if (currentDatabase != null) {
            // Récupérer les tables de la base de données courante.
            Map<String, Relation> tables = databases.get(currentDatabase);

            // Vérifier si la table existe.
            if (tables.containsKey(nomTable)) {
                tables.remove(nomTable);
                System.out.println("Table '" + nomTable + "' supprimée de la base de données '" + currentDatabase + "'.");
            } else {
                System.out.println("La table '" + nomTable + "' n'existe pas dans la base de données '" + currentDatabase + "'.");
            }

        }
        //Cas où il n'y a pas de db courante
        else {
            System.out.println("Aucune base de données courante n'est définie.");
        }
    }

    public void removeDatabase(String nomBdd) {
        //supprimer les tables
        databases.remove(nomBdd);
    }

    public void removeTablesFromCurrentDatabase() {
        if (currentDatabase != null ) {
            // Obtenir toutes les clés de la base de données courante
            List<String> tables = new ArrayList<>(databases.get(currentDatabase).keySet());

            // Supprimer chaque table individuellement
            for (String table : tables) {
                removeTableFromCurrentDatabase(table);
            }
        }
        else
        {
            System.out.println("Aucune base de données courante n'est définie.");
        }
    }

    public void removeDatabases()
    {
        List<String> dbs = new ArrayList<>(databases.keySet());
        for (String db : dbs){
            currentDatabase = db;
            removeTablesFromCurrentDatabase();
            removeDatabase(db);
        }
    }
    
    public void listDatabases()
    {
        List<String> listeDbs = new ArrayList<>(databases.keySet());
        for (String db : listeDbs){
            System.out.println(db);
        }
    }

    public void listTablesInCurrentDatabase()
    {
        if (currentDatabase != null) {
            List<String> tables = new ArrayList<>(databases.get(currentDatabase).keySet());
            for ( String t : tables)
            {
                System.out.println(t + " / " + t.getClass().getName() + "\n");
                //affiche t et affiche le nom complet (avec package) de la classe de l'objet t
            }
        }
        else
        {
            System.out.println("Aucune base de données courante n'est définie.");
        }
    }


}