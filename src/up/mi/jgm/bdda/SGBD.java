package up.mi.jgm.bdda;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

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
                        processDropTableCommand(command);
                    else if (parts.length > 1 && parts[1].equalsIgnoreCase("TABLES")) {
                        processDropTablesCommand();
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

                case "INSERT":
                    if (parts.length > 1 && parts[1].equalsIgnoreCase("INTO")) {
                        processInsertCommand(command);
                    } else {
                        System.out.println("Commande INSERT invalide.");
                    }
                    break;

                case "BULKINSERT":
                    if (parts.length > 1 && parts[1].equalsIgnoreCase("INTO")) {
                        processBulkInsertCommand(command);
                    } else {
                        System.out.println("Commande BULKINSERT invalide.");
                    }
                    break;

                case "SELECT":
                    processSelectCommand(command);
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
        String[] parts = command.split("\\s+");
        if (parts.length == 3) {
            String dbName = parts[2];
            dbManager.createDatabase(dbName);
        } else {
            System.out.println("Commande CREATE DATABASE invalide.");
        }
    }

    private void processSetDatabaseCommand(String command) {
        String[] parts = command.split("\\s+");
        if (parts.length == 3) {
            String dbName = parts[2];
            dbManager.setCurrentDatabase(dbName);
        } else {
            System.out.println("Commande SET DATABASE invalide.");
        }
    }

    private void processCreateTableCommand(String command) {
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
                        String colName = colParts[0].trim();
                        String colTypeStr = colParts[1].trim();
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
                        } else if (colTypeStr.startsWith("VARSTRING(")) {
                            colType = ColInfo.Type.VARSTRING;
                            size = Integer.parseInt(colTypeStr.substring(10, colTypeStr.length() - 1));
                        } else if (colTypeStr.startsWith("STRING(")) {
                            colType = ColInfo.Type.STRING;
                            size = Integer.parseInt(colTypeStr.substring(7, colTypeStr.length() - 1));
                        } else if (colTypeStr.startsWith("FLOAT")) { // Ajouté
                            colType = ColInfo.Type.FLOAT;
                            size = 0;
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
                    Relation relation = new Relation(tableName, columns, diskManager, bufferManager);
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

    private void processInsertCommand(String command) {
        // Format : INSERT INTO nomRelation VALUES (val1,val2,...)
        // Parser nomRelation, extraire les valeurs
        String[] parts = command.split("\\s+", 4);
        if (parts.length < 4 || !parts[1].equalsIgnoreCase("INTO")) {
            System.out.println("Commande INSERT INTO invalide.");
            return;
        }

        String relationName = parts[2];
        String valuesPart = parts[3];

        if (!valuesPart.startsWith("VALUES")) {
            System.out.println("Commande INSERT INTO invalide, mot-clé VALUES manquant.");
            return;
        }

        int firstParen = valuesPart.indexOf('(');
        int lastParen = valuesPart.lastIndexOf(')');
        if (firstParen == -1 || lastParen == -1 || lastParen <= firstParen) {
            System.out.println("Format des valeurs invalide. Parenthèses manquantes ?");
            return;
        }

        String insideParens = valuesPart.substring(firstParen + 1, lastParen).trim();
        String[] rawValues = insideParens.split(",");

        Relation table = dbManager.getTableFromCurrentDatabase(relationName);
        if (table == null) {
            return;
        }

        List<ColInfo> cols = table.getColumns();
        if (rawValues.length != cols.size()) {
            System.out.println("Le nombre de valeurs ne correspond pas au nombre de colonnes de la table.");
            return;
        }

        List<Value> values = parseValues(cols, rawValues);
        if (values == null) return;

        Record record = new Record(values);
        try {
            table.InsertRecord(record);
            System.out.println("Tuple inséré avec succès dans la table '" + relationName + "'.");
        } catch (IOException e) {
            System.out.println("Erreur lors de l'insertion : " + e.getMessage());
        }
    }

    private void processBulkInsertCommand(String command) {
        // Format: BULKINSERT INTO nomRelation nomFichier.csv
        String[] parts = command.split("\\s+");
        if (parts.length != 4 || !parts[1].equalsIgnoreCase("INTO")) {
            System.out.println("Commande BULKINSERT invalide.");
            return;
        }

        String relationName = parts[2];
        String fileName = parts[3];

        Relation table = dbManager.getTableFromCurrentDatabase(relationName);
        if (table == null) {
            return;
        }

        List<ColInfo> cols = table.getColumns();

        try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(fileName))) {
            String line;
            int count = 0;
            while ((line = reader.readLine()) != null) {
                String[] rawValues = line.split(",");
                if (rawValues.length != cols.size()) {
                    System.out.println("Ligne CSV invalide: nombre de valeurs != nombre de colonnes");
                    continue;
                }

                List<Value> values = parseValues(cols, rawValues);
                if (values == null) continue; // Valeur incompatible

                Record record = new Record(values);
                table.InsertRecord(record);
                count++;
            }
            System.out.println(count + " tuples insérés avec succès dans la table '" + relationName + "'.");
        } catch (IOException e) {
            System.out.println("Erreur lors de la lecture du fichier CSV : " + e.getMessage());
        }
    }

    private List<Value> parseValues(List<ColInfo> cols, String[] rawValues) {
        List<Value> values = new ArrayList<>();
        for (int i = 0; i < cols.size(); i++) {
            ColInfo col = cols.get(i);
            String valStr = rawValues[i].trim();
            Value val = convertStringToValue(valStr, col);
            if (val == null) {
                System.out.println("Valeur incompatible avec le type de la colonne " + col.getName() + " : " + valStr);
                return null;
            }
            values.add(val);
        }
        return values;
    }

    private Value convertStringToValue(String valStr, ColInfo col) {
        switch (col.getType()) {
            case INT:
                try {
                    int intValue = Integer.parseInt(valStr);
                    return new Value(Value.Type.INT, intValue);
                } catch (NumberFormatException e) {
                    return null;
                }
            case REAL:
                try {
                    float floatValue = Float.parseFloat(valStr);
                    return new Value(Value.Type.REAL, floatValue);
                } catch (NumberFormatException e) {
                    return null;
                }
            case CHAR:
            case VARCHAR:
                // Valeur chaîne entre guillemets
                if (valStr.startsWith("\"") && valStr.endsWith("\"")) {
                    String strValue = valStr.substring(1, valStr.length() - 1);
                    return new Value(Value.Type.VARCHAR, strValue);
                } else {
                    return null;
                }
            default:
                return null;
        }
    }

    private void processSelectCommand(String command) {
        // Ici, on doit parser la commande SELECT et construire l'itérateur
        // Ce code est long, c'est un exemple simplifié, reportez-vous aux explications.
        // Supposons que vous avez implémenté le parsing comme dans l'explication détaillée.

        try {
            SelectCommandParser parser = new SelectCommandParser(command, dbManager);
            IRecordIterator it = parser.buildIterator();
            RecordPrinter.printTousLesRecords(it);
        } catch (Exception e) {
            System.out.println("Erreur dans SELECT : " + e.getMessage());
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
