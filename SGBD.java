package up.mi.jgm.bdda;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Classe principale du SGBD.
 */
public class SGBD {

    private DBConfig dbConfig;
    private DiskManager diskManager;
    private BufferManager bufferManager;
    private DBManager dbManager;

    public SGBD(DBConfig dbConfig) {
        this.dbConfig = dbConfig;
        this.diskManager = new DiskManager(dbConfig);
        try {
            this.diskManager.LoadState();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        this.bufferManager = new BufferManager(dbConfig, diskManager);
        this.dbManager = new DBManager(dbConfig, diskManager, bufferManager);
        try {
            this.dbManager.loadState();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        Scanner scanner = new Scanner(System.in);
        String command;

        while (true) {
            System.out.print("Veuillez svp taper une commande: ");
            command = scanner.nextLine().trim();

            if (command.equalsIgnoreCase("QUIT")) {
                processQuitCommand();
                break;
            }

            parseAndProcessCommand(command);
        }

        scanner.close();
    }

    private void parseAndProcessCommand(String command) {
        // Analyse la commande pour déterminer de quel type elle est
        String[] parts = command.split("\\s+");
        String commandType = parts[0].toUpperCase();

        try {
            switch (commandType) {
                case "CREATE":
                    if (parts.length > 1 && parts[1].equalsIgnoreCase("TABLE")) {
                        processCreateTableCommand(command);
                    } else if (parts.length > 1 && parts[1].equalsIgnoreCase("DATABASE")) {
                        processCreateDatabaseCommand(command);
                    }
                    break;
                case "DROP":
                    if (parts.length > 1 && parts[1].equalsIgnoreCase("TABLE")) {
                        if (parts.length > 2 && parts[2].equalsIgnoreCase("S")) {
                            processDropTablesCommand();
                        } else {
                            processDropTableCommand(command);
                        }
                    } else if (parts.length > 1 && parts[1].equalsIgnoreCase("DATABASES")) {
                        processDropDatabasesCommand();
                    } else if (parts.length > 1 && parts[1].equalsIgnoreCase("DATABASE")) {
                        processDropDatabaseCommand(command);
                    }
                    break;
                case "SET":
                    processSetDatabaseCommand(command);
                    break;
                case "LIST":
                    if (parts.length > 1 && parts[1].equalsIgnoreCase("TABLES")) {
                        processListTablesCommand();
                    } else if (parts.length > 1 && parts[1].equalsIgnoreCase("DATABASES")) {
                        processListDatabasesCommand();
                    }
                    break;
                default:
                    System.out.println("Commande inconnue: " + command);
                    break;
            }
        } catch (Exception e) {
            System.out.println("Erreur lors du traitement de la commande: " + e.getMessage());
        }
    }

    private void processQuitCommand() {
        try {
            dbManager.saveState();
            diskManager.SaveState();
            bufferManager.FlushBuffers();
            System.out.println("État sauvegardé. Arrêt du SGBD.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processCreateDatabaseCommand(String command) {
        // Format attendu: CREATE DATABASE NomBDD
        String[] parts = command.split("\\s+");
        if (parts.length == 3) {
            String dbName = parts[2];
            dbManager.createDatabase(dbName);
        } else {
            System.out.println("Commande CREATE DATABASE invalide.");
        }
    }

    private void processSetDatabaseCommand(String command) {
        // Format attendu: SET DATABASE NomBDD
        String[] parts = command.split("\\s+");
        if (parts.length == 3) {
            String dbName = parts[2];
            dbManager.setCurrentDatabase(dbName);
        } else {
            System.out.println("Commande SET DATABASE invalide.");
        }
    }

    private void processCreateTableCommand(String command) {
        // Format attendu: CREATE TABLE NomTable (Col1:Type1,Col2:Type2,...)
        int firstParen = command.indexOf('(');
        int lastParen = command.lastIndexOf(')');
        if (firstParen != -1 && lastParen != -1 && lastParen > firstParen) {
            String beforeParen = command.substring(0, firstParen).trim();
            String insideParen = command.substring(firstParen + 1, lastParen).trim();

            String[] beforeParts = beforeParen.split("\\s+");
            if (beforeParts.length == 3) {
                String tableName = beforeParts[2];
                String[] columnDefs = insideParen.split(",");
                List<ColInfo> columns = new ArrayList<>();

                for (String colDef : columnDefs) {
                    String[] colParts = colDef.trim().split(":");
                    if (colParts.length == 2) {
                        String colName = colParts[0];
                        String colTypeStr = colParts[1];
                        ColInfo.Type colType = null;
                        int size = 0;

                        if (colTypeStr.startsWith("INT")) {
                            colType = ColInfo.Type.INT;
                        } else if (colTypeStr.startsWith("REAL")) {
                            colType = ColInfo.Type.REAL;
                        } else if (colTypeStr.startsWith("CHAR(")) {
                            colType = ColInfo.Type.CHAR;
                            size = Integer.parseInt(colTypeStr.substring(5, colTypeStr.length() - 1));
                        } else if (colTypeStr.startsWith("VARCHAR(")) {
                            colType = ColInfo.Type.VARCHAR;
                            size = Integer.parseInt(colTypeStr.substring(8, colTypeStr.length() - 1));
                        } else {
                            System.out.println("Type de colonne inconnu : " + colTypeStr);
                            return;
                        }
                        columns.add(new ColInfo(colName, colType, size));
                    } else {
                        System.out.println("Définition de colonne invalide : " + colDef);
                        return;
                    }
                }

                try {
                    // Créer la relation
                    Relation relation = new Relation(tableName, columns, diskManager, bufferManager);
                    // Ajouter la table à la base de données courante
                    dbManager.addTableToCurrentDatabase(relation);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else {
                System.out.println("Commande CREATE TABLE invalide.");
            }
        } else {
            System.out.println("Commande CREATE TABLE invalide.");
        }
    }

    private void processDropTableCommand(String command) {
        // Format attendu: DROP TABLE NomTable
        String[] parts = command.split("\\s+");
        if (parts.length == 3) {
            String tableName = parts[2];
            try {
                dbManager.removeTableFromCurrentDatabase(tableName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Commande DROP TABLE invalide.");
        }
    }

    private void processDropTablesCommand() {
        try {
            dbManager.removeTablesFromCurrentDatabase();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processListTablesCommand() {
        dbManager.listTablesInCurrentDatabase();
    }

    private void processDropDatabasesCommand() {
        try {
            dbManager.removeDatabases();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processListDatabasesCommand() {
        dbManager.listDatabases();
    }

    private void processDropDatabaseCommand(String command) {
        // Format attendu: DROP DATABASE NomBDD
        String[] parts = command.split("\\s+");
        if (parts.length == 3) {
            String dbName = parts[2];
            try {
                dbManager.removeDatabase(dbName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Commande DROP DATABASE invalide.");
        }
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java SGBD <cheminFichierDeConfiguration>");
            return;
        }

        String configFilePath = args[0];
        try {
            DBConfig dbConfig = DBConfig.loadDBConfig(configFilePath);
            SGBD sgbd = new SGBD(dbConfig);
            sgbd.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}