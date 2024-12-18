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
    private static final long serialVersionUID = 1L;

    private DBConfig dbConfig;
    private Map<String, Database> databases;
    private Database currentDatabase;
    private transient DiskManager diskManager;
    private transient BufferManager bufferManager;

    // Constructeur
    public DBManager(DBConfig dbConfig, DiskManager diskManager, BufferManager bufferManager) {
        this.dbConfig = dbConfig;
        this.diskManager = diskManager;
        this.bufferManager = bufferManager;
        this.databases = new HashMap<>();
        this.currentDatabase = null;
    }

    // Méthode pour créer une base de données
    public void createDatabase(String dbName) {
        if (!databases.containsKey(dbName)) {
            Database db = new Database(dbName);
            databases.put(dbName, db);
            System.out.println("Base de données '" + dbName + "' créée avec succès.");
        } else {
            System.out.println("La base de données '" + dbName + "' existe déjà.");
        }
    }

    // Méthode pour définir la base de données courante
    public void setCurrentDatabase(String dbName) throws IllegalArgumentException {
        if (databases.containsKey(dbName)) {
            currentDatabase = databases.get(dbName);
            System.out.println("Base de données actuelle définie sur '" + dbName + "'.");
        } else {
            throw new IllegalArgumentException("La base de données '" + dbName + "' n'existe pas.");
        }
    }

    // Méthode pour ajouter une table à la base de données courante
    public void addTableToCurrentDatabase(Relation table) {
        if (currentDatabase != null) {
            currentDatabase.addTable(table);
            System.out.println("Table '" + table.getName() + "' ajoutée à la base de données '" + currentDatabase.getName() + "'.");
        } else {
            System.out.println("Aucune base de données courante n'est définie.");
        }
    }

    // Méthode pour obtenir une table de la base de données courante
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

    // Méthode pour supprimer une table de la base de données courante
    public void removeTableFromCurrentDatabase(String tableName) throws IOException {
        if (currentDatabase != null) {
            Relation table = currentDatabase.getTable(tableName);
            if (table != null) {
                // Désallouer les pages de la table
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

    // Méthode pour supprimer toutes les tables de la base de données courante
    public void removeTablesFromCurrentDatabase() throws IOException {
        if (currentDatabase != null) {
            for (String tableName : currentDatabase.getTables().keySet()) {
                Relation table = currentDatabase.getTable(tableName);
                // Désallouer les pages de la table
                removeTablePages(table);
            }
            currentDatabase.removeAllTables();
            System.out.println("Toutes les tables ont été supprimées de la base de données '" + currentDatabase.getName() + "'.");
        } else {
            System.out.println("Aucune base de données courante n'est définie.");
        }
    }

    // Méthode pour supprimer une base de données
    public void removeDatabase(String dbName) throws IOException {
        if (databases.containsKey(dbName)) {
            Database db = databases.get(dbName);
            // Supprimer toutes les tables et désallouer les pages
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

    // Méthode pour supprimer toutes les bases de données
    public void removeDatabases() throws IOException {
        for (String dbName : databases.keySet()) {
            Database db = databases.get(dbName);
            // Supprimer toutes les tables et désallouer les pages
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

    // Méthode pour lister les tables de la base de données courante
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

    // Méthode pour sauvegarder l'état
    public void saveState() throws IOException {
        String dbPath = dbConfig.getDbpath();
        String saveFilePath = Paths.get(dbPath, "databases.save").toString();

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(saveFilePath))) {
            oos.writeObject(databases);
            oos.writeObject(currentDatabase != null ? currentDatabase.getName() : null);
        }
        System.out.println("État des bases de données sauvegardé.");
    }

    // Méthode pour charger l'état
    public void loadState() throws IOException, ClassNotFoundException {
        String dbPath = dbConfig.getDbpath();
        String saveFilePath = Paths.get(dbPath, "databases.save").toString();

        File saveFile = new File(saveFilePath);
        if (saveFile.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(saveFilePath))) {
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
                    table.setDiskManager(diskManager);
                    table.setBufferManager(bufferManager);
                }
            }
            System.out.println("État des bases de données chargé.");
        } else {
            databases = new HashMap<>();
            currentDatabase = null;
            System.out.println("Aucune sauvegarde précédente trouvée.");
        }
    }

    // Méthode pour désallouer les pages d'une table
    private void removeTablePages(Relation table) throws IOException {
        // Désallouer la header page
        diskManager.DeallocPage(table.getHeaderPageId());
        // Désallouer les pages de données
        List<PageId> dataPages = table.getDataPages();
        for (PageId pageId : dataPages) {
            diskManager.DeallocPage(pageId);
        }
    }
}
