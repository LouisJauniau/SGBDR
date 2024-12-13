package up.mi.jgm.td3;


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

    private void processInsertCommand(String command) {
        // Analyse de la commande
        // Format: INSERT INTO nomRelation VALUES (val1,val2,...)
        // On suppose que le parsing est strict et qu'il n'y a pas de retours à la ligne.
        String[] parts = command.split("\\s+", 4);
        // parts[0] = INSERT, parts[1] = INTO, parts[2] = nomRelation, parts[3] = VALUES (....)
        if (parts.length < 4 || !parts[1].equalsIgnoreCase("INTO")) {
            System.out.println("Commande INSERT INTO invalide.");
            return;
        }

        String tableName = parts[2];
        String valuesPart = parts[3];

        if (!valuesPart.startsWith("VALUES")) {
            System.out.println("Commande INSERT INTO invalide, mot-clé VALUES manquant.");
            return;
        }

        // Extraire la partie entre parenthèses
        int firstParen = valuesPart.indexOf('(');
        int lastParen = valuesPart.lastIndexOf(')');
        if (firstParen == -1 || lastParen == -1 || lastParen <= firstParen) {
            System.out.println("Format des valeurs invalide. Parenthèses manquantes ?");
            return;
        }

        String insideParens = valuesPart.substring(firstParen + 1, lastParen).trim();
        // insideParens = val1,val2,val3 ...
        String[] rawValues = insideParens.split(",");

        Relation table = dbManager.getTableFromCurrentDatabase(tableName);
        if (table == null) {
            // Message déjà affiché par getTableFromCurrentDatabase
            return;
        }

        List<ColInfo> cols = table.getColumns();
        if (rawValues.length != cols.size()) {
            System.out.println("Le nombre de valeurs ne correspond pas au nombre de colonnes de la table.");
            return;
        }

        // Conversion des valeurs en Value en respectant le type
        List<Value> values = new ArrayList<>();
        for (int i = 0; i < cols.size(); i++) {
            ColInfo col = cols.get(i);
            String valStr = rawValues[i].trim();
            Value val = convertStringToValue(valStr, col);
            if (val == null) {
                System.out.println("Valeur incompatible avec le type de la colonne " + col.getName());
                return;
            }
            values.add(val);
        }

        // Insertion du record
        Record record = new Record(values);
        try {
            table.InsertRecord(record);
            System.out.println("Tuple inséré avec succès dans la table '" + tableName + "'.");
        } catch (IOException e) {
            System.out.println("Erreur lors de l'insertion : " + e.getMessage());
        }
    }

    // Méthode utilitaire pour convertir une chaîne en Value
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
                // Valeur chaîne : doit être entourée de guillemets "
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

    private void processBulkInsertCommand(String command) {
        // Format: BULKINSERT INTO nomRelation nomFichier.csv
        String[] parts = command.split("\\s+");
        if (parts.length != 4 || !parts[1].equalsIgnoreCase("INTO")) {
            System.out.println("Commande BULKINSERT invalide.");
            return;
        }

        String tableName = parts[2];
        String fileName = parts[3]; // nomFichier.csv

        Relation table = dbManager.getTableFromCurrentDatabase(tableName);
        if (table == null) {
            // Message déjà affiché si la table n'existe pas
            return;
        }

        List<ColInfo> cols = table.getColumns();

        // Lecture du fichier CSV
        try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(fileName))) {
            String line;
            int count = 0;
            while ((line = reader.readLine()) != null) {
                // Chaque ligne représente un record
                // Format: "val1","val2",val3 ... etc.
                String[] rawValues = line.split(",");

                if (rawValues.length != cols.size()) {
                    System.out.println("Ligne avec nombre de valeurs incorrect dans le fichier CSV.");
                    continue; // On ignore cette ligne ou on peut interrompre, selon la stratégie
                }

                List<Value> values = new ArrayList<>();
                boolean validLine = true;
                for (int i = 0; i < cols.size(); i++) {
                    ColInfo col = cols.get(i);
                    String valStr = rawValues[i].trim();
                    Value val = convertStringToValue(valStr, col);
                    if (val == null) {
                        System.out.println("Valeur incompatible avec le type de la colonne " + col.getName() + " dans le fichier CSV.");
                        validLine = false;
                        break;
                    }
                    values.add(val);
                }

                if (!validLine) {
                    continue; // On passe à la ligne suivante
                }

                // Insertion du record
                Record record = new Record(values);
                table.InsertRecord(record);
                count++;
            }
            System.out.println(count + " tuples insérés avec succès dans la table '" + tableName + "'.");
        } catch (IOException e) {
            System.out.println("Erreur lors de la lecture du fichier CSV : " + e.getMessage());
        }
    }


    /*
    CODE PROBLEMATIQUE
    NON FONCTIONNEL

    pROBLEMES DANS CETTE METHODE QUI FONT DISFONCTIONNER LES SCENARIES TP07
     */


    private void processSelectCommand(String command) {
        /*
        On crée un IRecordIterator qui scanne la relation.
        On applique ensuite un SelectOperator si conditions.
        On applique un ProjectOperator si ce n’est pas *.
        Enfin, on utilise RecordPrinter pour afficher.
         */



        int indicePositionFROM = command.toUpperCase().indexOf("FROM");
        if (indicePositionFROM == -1) {
            System.out.println("Commande SELECT invalide : mot-clé FROM manquant.");
            return;
        }

        String selectPart = command.substring("SELECT".length(), indicePositionFROM).trim();
        // selectPart contient * ou alias.col1,alias.col2,...

        String afterFrom = command.substring(indicePositionFROM + 4).trim();
        // afterFrom = nomRelation alias [WHERE ...]

        // Extraire nomRelation et alias
        String[] fromParts = afterFrom.split("\\s+", 3);
        if (fromParts.length < 2) {
            System.out.println("Commande SELECT invalide : alias manquant.");
            return;
        }
        String relationName = fromParts[0];
        String alias = fromParts[1];

        Relation table = dbManager.getTableFromCurrentDatabase(relationName);
        if (table == null) {
            // Message déjà affiché par getTableFromCurrentDatabase
            return;
        }

        List<ColInfo> cols = table.getColumns();

        // Déterminer s'il y a un WHERE
        String wherePart = null;
        if (fromParts.length == 3) {
            // Il reste quelque chose après l'alias
            String remaining = fromParts[2].trim();
            int whereIndex = remaining.toUpperCase().indexOf("WHERE");
            if (whereIndex != -1) {
                wherePart = remaining.substring(whereIndex + 5).trim(); // tout après WHERE
            }
        }

        // Parse des colonnes à projeter
        List<Integer> columnsToProject = new ArrayList<>();
        boolean selectAll = selectPart.equals("*");

        /*
        MODIFS POUR FAIRE FONCTIONNER LE SELECT *
         */
        if (selectAll) {
            // Ajouter toutes les colonnes
            for (int i = 0; i < cols.size(); i++) {
                columnsToProject.add(i);
            }
        } else {
            // Gestion des colonnes spécifiques avec alias
            String[] colRefs = selectPart.split(",");
            for (String c : colRefs) {
                c = c.trim();
                String colName;
                if (c.contains(".")) {
                    colName = c.substring(c.indexOf('.') + 1); // Enlever l'alias si présent
                } else {
                    colName = c; // Pas d'alias
                }

                int colIndex = -1;
                for (int i = 0; i < cols.size(); i++) {
                    if (cols.get(i).getName().equalsIgnoreCase(colName)) {
                        colIndex = i;
                        break;
                    }
                }

                if (colIndex == -1) {
                    System.out.println("Colonne " + colName + " introuvable dans la relation.");
                    return;
                }
                columnsToProject.add(colIndex);
            }
        }

        /*
        FIN DES MODIFS POUR FAIRE FONCTIONNER LE SELECT *
         */


        // Parse des conditions (si WHERE présent)
        List<Condition> conditions = new ArrayList<>();
        if (wherePart != null && !wherePart.isEmpty()) {
            // Conditions séparées par " AND "
            String[] condParts = wherePart.split("AND");
            // Chaque condParts[i] : Terme1OPTerme2 (sans espace autour de OP)
            for (String condStr : condParts) {
                condStr = condStr.trim();
                // On doit identifier Terme1, OP, Terme2
                // Format des opérateurs : =,<,>,<=,>=,<>
                String op = findOperator(condStr);
                if (op == null) {
                    System.out.println("Opérateur non reconnu dans la condition : " + condStr);
                    return;
                }
                String[] terms = condStr.split(op);
                if (terms.length != 2) {
                    System.out.println("Condition mal formée : " + condStr);
                    return;
                }
                String leftTerm = terms[0].trim();
                String rightTerm = terms[1].trim();

                // Déterminer si leftTerm/rightTerm sont colonnes ou valeurs constantes
                Integer leftColIndex = null;
                Object constantValue = null;
                Integer rightColIndex = null;

                // Méthode pour parser un terme : s’il commence par alias., c’est une colonne, sinon une constante
                // Les constantes chaînes sont entre guillemets, int/float non entre guillemets.

                // Pour le leftTerm
                if (leftTerm.startsWith(alias + ".")) {
                    String colName = leftTerm.substring(alias.length() + 1);
                    leftColIndex = findColumnIndex(cols, colName);
                    if (leftColIndex == -1) {
                        System.out.println("Colonne " + colName + " introuvable dans la relation.");
                        return;
                    }
                } else {
                    // C’est une constante
                    constantValue = parseConstant(leftTerm, cols);
                    if (constantValue == null) {
                        System.out.println("Constante invalide : " + leftTerm);
                        return;
                    }
                }

                // Pour le rightTerm
                if (rightTerm.startsWith(alias + ".")) {
                    String colName = rightTerm.substring(alias.length() + 1);
                    rightColIndex = findColumnIndex(cols, colName);
                    if (rightColIndex == -1) {
                        System.out.println("Colonne " + colName + " introuvable dans la relation.");
                        return;
                    }
                } else {
                    // Si on n’a pas déjà mis la constante dans leftTerm
                    if (constantValue != null) {
                        // Oups, on a déjà une constante, l’autre terme doit être une colonne...
                        // Mais si on tombe ici c’est que l’autre terme est aussi constant, ce qui est autorisé.
                        // Dans l’énoncé, il est dit "au maximum un terme est une constante"
                        // On va considérer qu’on ne doit pas avoir 2 constantes
                        System.out.println("Condition invalide : les deux termes semblent être des constantes.");
                        return;
                    } else {
                        constantValue = parseConstant(rightTerm, cols);
                        if (constantValue == null) {
                            System.out.println("Constante invalide : " + rightTerm);
                            return;
                        }
                    }
                }

                // Construire la condition
                if (leftColIndex != null && rightColIndex != null) {
                    // Colonne vs Colonne

                    conditions.add(new Condition(leftColIndex, op, rightColIndex));
                } else if (leftColIndex != null && constantValue != null) {
                    // Colonne vs constante
                    conditions.add(new Condition(leftColIndex, op, constantValue));
                } else if (rightColIndex != null && constantValue != null) {
                    // constante vs colonne : invert l’opérateur en conséquence (par ex: 10<=col devient col>=10)
                    // Pour simplifier, on va supposer que dans l’énoncé on n’a pas ce cas à gérer.
                    // Si besoin, il faudrait inverser l’opérateur.
                    System.out.println("Condition invalide : constante à gauche et colonne à droite non géré.");
                    return;
                } else {
                    System.out.println("Condition invalide : aucun terme colonne détecté.");
                    return;
                }
            }
        }

        // Construire l'itérateur final
        try {
            IRecordIterator scanner = new RelationScanner(table);

            // Si conditions présentes, on enveloppe le scanner dans un SelectOperator
            if (!conditions.isEmpty()) {
                Class<?>[] columnTypes = buildColumnTypesArray(cols);
                @SuppressWarnings("unchecked")
                Class<Object>[] colTypesCasted = (Class<Object>[]) columnTypes;
                scanner = new SelectOperator<>(scanner, conditions, colTypesCasted);
            }

            // Projection
            if (!selectAll) {
                scanner = new ProjectOperator(scanner, columnsToProject);
            }

            // Affichage
            RecordPrinter.printTousLesRecords(scanner);

        } catch (IOException e) {
            System.out.println("Erreur lors de l'accès à la relation : " + e.getMessage());
        }
    }

// Méthodes utilitaires pour processSelectCommand

    private String findOperator(String condStr) {
        // Les opérateurs possibles : =,<,>,<=,>=,<>
        // On teste par ordre de complexité
        if (condStr.contains("<=")) return "<=";
        if (condStr.contains(">=")) return ">=";
        if (condStr.contains("<>")) return "<>";
        if (condStr.contains("=")) return "=";
        if (condStr.contains("<")) return "<";
        if (condStr.contains(">")) return ">";
        return null;
    }

    private int findColumnIndex(List<ColInfo> cols, String colName) {
        for (int i = 0; i < cols.size(); i++) {
            if (cols.get(i).getName().equalsIgnoreCase(colName)) {
                return i;
            }
        }
        return -1;
    }

    private Object parseConstant(String valStr, List<ColInfo> cols) {
        // Sans info précise de colonne ici, on ne sait pas quel type donner à la constante.
        // L’énoncé dit : "Au maximum un des deux termes est une constante".
        // On peut deviner le type à partir du format (si guillemets => String, sinon int/float)
        // Dans un cas réel, il faudrait plus d’infos. Ici, on tente une conversion heuristique.
        if (valStr.startsWith("\"") && valStr.endsWith("\"")) {
            return valStr.substring(1, valStr.length()-1);
        }

        // Essayer en int
        try {
            return Integer.parseInt(valStr);
        } catch (NumberFormatException e) {
            // Essayer en float
        }

        try {
            return Float.parseFloat(valStr);
        } catch (NumberFormatException e) {
            return null; // Aucun parse possible
        }
    }

    private Class<?>[] buildColumnTypesArray(List<ColInfo> cols) {
        Class<?>[] columnTypes = new Class<?>[cols.size()];
        for (int i = 0; i < cols.size(); i++) {
            switch (cols.get(i).getType()) {
                case INT:
                    columnTypes[i] = Integer.class;
                    break;
                case REAL:
                    columnTypes[i] = Float.class;
                    break;
                case CHAR:
                case VARCHAR:
                    columnTypes[i] = String.class;
                    break;
                default:
                    throw new UnsupportedOperationException("Type non supporté");
            }
        }
        return columnTypes;
    }


    /*
    Fin du code pas totalement fonctionnel
     */





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