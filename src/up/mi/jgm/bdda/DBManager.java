package up.mi.jgm.bdda;

import java.io.*;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

/**
 * Classe permettant de gérer les bases de données et les tables.
 */
public class DBManager implements Serializable {
    private static final long serialVersionUID = 1L; //version de serialisation


    private DBConfig dbConfig;
    private Map<String, Database> databases; //dictionnaire des db presentes
    private Database currentDatabase; //db actuelle/courante
    private transient DiskManager diskManager;
    private transient BufferManager bufferManager;

    //constructeur
    public DBManager(DBConfig dbConfig, DiskManager diskManager, BufferManager bufferManager) {
        this.dbConfig = dbConfig;
        this.diskManager = diskManager;
        this.bufferManager = bufferManager;
        this.databases = new HashMap<>();
        this.currentDatabase = null; //instancie sans db courante
    }

    //methode pour creer une base de données
    public void createDatabase(String dbName) {
        //si databases ne contient pas la db alors on la cree et on l'ajoute a databases
        if (!databases.containsKey(dbName)) {
            Database db = new Database(dbName);
            databases.put(dbName, db);
            System.out.println("Base de données '" + dbName + "' créée avec succès.");
        } else {
            System.out.println("La base de données '" + dbName + "' existe déjà.");
        }
    }

    //methode pour définir la db courante
    public void setCurrentDatabase(String dbName) throws IllegalArgumentException {
        if (databases.containsKey(dbName)) {
            currentDatabase = databases.get(dbName);
            System.out.println("Base de données actuelle définie sur '" + dbName + "'.");
        } else {
            throw new IllegalArgumentException("La base de données '" + dbName + "' n'existe pas.");
        }
    }

    //methode pour ajouter une table à la base de données courante
    public void addTableToCurrentDatabase(Relation table) {
        //sil y a une db courante
        if (currentDatabase != null) {
            currentDatabase.addTable(table);
            System.out.println("Table '" + table.getName() + "' ajoutée à la base de données '" + currentDatabase.getName() + "'.");
        } else {
            System.out.println("Aucune base de données courante n'est définie.");
        }
    }

    //methode pour obtenir une table de la base de données courante
    public Relation getTableFromCurrentDatabase(String tableName) {
        if (currentDatabase != null) {
            Relation table = currentDatabase.getTable(tableName);
            if (table != null) {
                return table;
            } else {
                System.out.println("La table '" + tableName + "' n'existe pas dans la base de données '" + currentDatabase.getName() + "'.");
                return null;
            }
        } else {
            System.out.println("Aucune base de données courante n'est définie.");
            return null;
        }
    }

    //methode pour supprimer une table de la base de données courante
    public void removeTableFromCurrentDatabase(String tableName) throws IOException {
        if (currentDatabase != null) {
            Relation table = currentDatabase.getTable(tableName);
            if (table != null) {
                //desallouer les pages de la table
                removeTablePages(table);
                currentDatabase.removeTable(tableName);
                System.out.println("Table '" + tableName + "' supprimée de la base de données '" + currentDatabase.getName() + "'.");
            } else {
                System.out.println("La table '" + tableName + "' n'existe pas dans la base de données '" + currentDatabase.getName() + "'.");
            }
        } else {
            System.out.println("Aucune base de données courante n'est définie.");
        }
    }

    //methode pour supprimer toutes les tables de la base de données courante
    public void removeTablesFromCurrentDatabase() throws IOException {
        if (currentDatabase != null) {
            for (String tableName : currentDatabase.getTables().keySet()) {
                Relation table = currentDatabase.getTable(tableName);
                //desalloccation des pages de la table
                removeTablePages(table);
            }
            currentDatabase.removeAllTables();
            System.out.println("Toutes les tables ont été supprimées de la base de données '" + currentDatabase.getName() + "'.");
        } else {
            System.out.println("Aucune base de données courante n'est définie.");
        }
    }

    //methode pour supprimer une base de données
    public void removeDatabase(String dbName) throws IOException {
        if (databases.containsKey(dbName)) {
            Database db = databases.get(dbName);
            //suppr toutes les tables et désallouer les pages
            for (Relation table : db.getTables().values()) {
                removeTablePages(table);
            }
            databases.remove(dbName);
            if (currentDatabase != null && currentDatabase.getName().equals(dbName)) {
                currentDatabase = null;
            }
            System.out.println("Base de données '" + dbName + "' supprimée.");
        } else {
            System.out.println("La base de données '" + dbName + "' n'existe pas.");
        }
    }

    //methode pour supprimer toutes les bases de données
    public void removeDatabases() throws IOException {
        for (String dbName : databases.keySet()) {
            Database db = databases.get(dbName);
            //supprimer toutes les tables et désallouer les pages
            for (Relation table : db.getTables().values()) {
                removeTablePages(table);
            }
        }
        databases.clear();
        currentDatabase = null;
        System.out.println("Toutes les bases de données ont été supprimées.");
    }

    // Méthode pour lister les bases de données existantes
    public void listDatabases() {
        if (databases.isEmpty()) {
            System.out.println("Aucune base de données existante.");
        } else {
            for (String dbName : databases.keySet()) {
                System.out.println(dbName);
            }
        }
    }

    //methode pour lister les tables de la base de données courante
    public void listTablesInCurrentDatabase() {
        if (currentDatabase != null) {
            if (currentDatabase.getTables().isEmpty()) {
                System.out.println("Aucune table dans la base de données courante.");
            } else {
                for (Relation table : currentDatabase.getTables().values()) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("TABLE ").append(table.getName()).append(" (");
                    List<ColInfo> cols = table.getColumns();
                    for (int i = 0; i < cols.size(); i++) {
                        ColInfo col = cols.get(i);
                        //ajoute au string builder pour ne pas creer de nouvel objet et obtenir un string avec toutes les tables
                        sb.append(col.getName()).append(":").append(col.getType().name());
                        if (col.getType() == ColInfo.Type.CHAR || col.getType() == ColInfo.Type.VARCHAR) {
                            sb.append("(").append(col.getSize()).append(")");
                        }
                        if (i < cols.size() - 1) {
                            sb.append(",");
                        }
                    }
                    sb.append(")");
                    System.out.println(sb.toString());
                }
            }
        } else {
            System.out.println("Aucune base de données courante n'est définie.");
        }
    }

    //methode pour sauvegarder l'état
    public void saveState() throws IOException {
        //recupération du chemin de la base de données à partir de la configuration
        String dbPath = dbConfig.getDbpath();
        //creation du chemin complet vers le fichier de sauvegarde nommé "databases.save"
        String saveFilePath = Paths.get(dbPath, "databases.save").toString();
                //utilisation d'un ObjectOutputStream pour sérialiser les objets et les écrire dans un fichier
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(saveFilePath))) {
            oos.writeObject(databases);
            oos.writeObject(currentDatabase != null ? currentDatabase.getName() : null);
        }
        System.out.println("État des bases de données sauvegardé.");
    }

    //methode pour charger l'état
    public void loadState() throws IOException, ClassNotFoundException {
        String dbPath = dbConfig.getDbpath();
        String saveFilePath = Paths.get(dbPath, "databases.save").toString();
        //verif que le fichier de sauvegarde existe
        File saveFile = new File(saveFilePath);
        if (saveFile.exists()) {
            //ObjectInputStream pour désérialiser 
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(saveFilePath))) {
                 //chargement des db sérialisees
                databases = (Map<String, Database>) ois.readObject();
                String currentDbName = (String) ois.readObject();
                if (currentDbName != null) {
                    currentDatabase = databases.get(currentDbName);
                } else {
                    currentDatabase = null;
                }
            }
            // Réinitialiser les références transitoires
            for (Database db : databases.values()) {
                for (Relation table : db.getTables().values()) {
                    table.setDiskManager(diskManager); //reassocie le gestionnaire de disque
                    table.setBufferManager(bufferManager); //reassocie le gestionnaire de buffer
                }
            }
            System.out.println("État des bases de données chargé.");
        } else {
            //si aucun fichier sauvegarde existe, initialiser les structures de donnees
            databases = new HashMap<>();
            currentDatabase = null;
            System.out.println("Aucune sauvegarde précédente trouvée.");
        }
    }

    //methode pour désallouer les pages d'une table
    private void removeTablePages(Relation table) throws IOException {
        //desallouer le header page
        diskManager.DeallocPage(table.getHeaderPageId());
        //desallouer les pages de données
        List<PageId> dataPages = table.getDataPages();
        for (PageId pageId : dataPages) {
            diskManager.DeallocPage(pageId);
        }
    }
}
