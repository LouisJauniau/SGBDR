package up.mi.jgm.bdda;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe utilitaire pour parser les commandes SELECT.
 */
public class SelectCommandParser {
    private String command;
    private DBManager dbManager;

    private List<Integer> columnsToProject;
    private String relationName;
    private String alias;
    private List<Condition> conditions;

    public SelectCommandParser(String command, DBManager dbManager) {
        this.command = command;
        this.dbManager = dbManager;
        this.columnsToProject = new ArrayList<>();
        this.conditions = new ArrayList<>();
    }

    /**
     * Parse la commande SELECT et construit l'itérateur approprié.
     * @return Un IRecordIterator configuré selon la commande SELECT.
     * @throws IOException En cas d'erreur d'I/O.
     */
    public IRecordIterator buildIterator() throws IOException {
        parseCommand();
        Relation table = dbManager.getTableFromCurrentDatabase(relationName);
        if (table == null) {
            throw new IllegalArgumentException("La table " + relationName + " n'existe pas.");
        }
        IRecordIterator it = new RelationScanner(table);
        
        if (!conditions.isEmpty()) {
            Class<?>[] columnTypes = buildColumnTypesArray(table.getColumns());
            @SuppressWarnings("unchecked")
            Class<Object>[] colTypesCasted = (Class<Object>[]) columnTypes;
            it = new SelectOperator<>(it, conditions, colTypesCasted);
        }

        if (!columnsToProject.isEmpty()) {
            it = new ProjectOperator(it, columnsToProject);
        }

        return it;
    }

    /**
     * Parse la commande SELECT et extrait les colonnes à projeter, la relation, l'alias et les conditions.
     */
    private void parseCommand() {
        int indexFROM = command.toUpperCase().indexOf("FROM");
        if (indexFROM == -1) {
            throw new IllegalArgumentException("Commande SELECT invalide : mot-clé FROM manquant.");
        }

        String selectPart = command.substring("SELECT".length(), indexFROM).trim();
        String afterFrom = command.substring(indexFROM + 4).trim();

        // Extraire nomRelation et alias
        String[] fromParts = afterFrom.split("\\s+", 3);
        if (fromParts.length < 2) {
            throw new IllegalArgumentException("Commande SELECT invalide : alias manquant.");
        }
        relationName = fromParts[0];
        alias = fromParts[1];

        Relation table = dbManager.getTableFromCurrentDatabase(relationName);
        if (table == null) {
            throw new IllegalArgumentException("La table " + relationName + " n'existe pas.");
        }

        List<ColInfo> cols = table.getColumns();

        // Déterminer s'il y a un WHERE
        String wherePart = null;
        if (fromParts.length == 3) {
            String remaining = fromParts[2].trim();
            int whereIndex = remaining.toUpperCase().indexOf("WHERE");
            if (whereIndex != -1) {
                wherePart = remaining.substring(whereIndex + 5).trim(); // tout après WHERE
            }
        }

        // Parse des colonnes à projeter
        boolean selectAll = selectPart.equals("*");

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
                    throw new IllegalArgumentException("Colonne " + colName + " introuvable dans la relation.");
                }
                columnsToProject.add(colIndex);
            }
        }

        // Parse des conditions (si WHERE présent)
        if (wherePart != null && !wherePart.isEmpty()) {
            String[] condParts = wherePart.split("AND");
            for (String condStr : condParts) {
                condStr = condStr.trim();
                String op = findOperator(condStr);
                if (op == null) {
                    throw new IllegalArgumentException("Opérateur non reconnu dans la condition : " + condStr);
                }
                String[] terms = condStr.split(op);
                if (terms.length != 2) {
                    throw new IllegalArgumentException("Condition mal formée : " + condStr);
                }
                String leftTerm = terms[0].trim();
                String rightTerm = terms[1].trim();

                Integer leftColIndex = null;
                Object constantValue = null;
                Integer rightColIndex = null;

                if (leftTerm.startsWith(alias + ".")) {
                    String colName = leftTerm.substring(alias.length() + 1);
                    leftColIndex = findColumnIndex(cols, colName);
                    if (leftColIndex == -1) {
                        throw new IllegalArgumentException("Colonne " + colName + " introuvable dans la relation.");
                    }
                } else {
                    constantValue = parseConstant(leftTerm, cols);
                    if (constantValue == null) {
                        throw new IllegalArgumentException("Constante invalide : " + leftTerm);
                    }
                }

                if (rightTerm.startsWith(alias + ".")) {
                    String colName = rightTerm.substring(alias.length() + 1);
                    rightColIndex = findColumnIndex(cols, colName);
                    if (rightColIndex == -1) {
                        throw new IllegalArgumentException("Colonne " + colName + " introuvable dans la relation.");
                    }
                } else {
                    if (constantValue != null) {
                        // Deux constantes, invalides pour le TP7
                        throw new IllegalArgumentException("Condition invalide : les deux termes sont des constantes.");
                    } else {
                        constantValue = parseConstant(rightTerm, cols);
                        if (constantValue == null) {
                            throw new IllegalArgumentException("Constante invalide : " + rightTerm);
                        }
                    }
                }

                if (leftColIndex != null && rightColIndex != null) {
                    conditions.add(new Condition(leftColIndex, op, rightColIndex));
                } else if (leftColIndex != null && constantValue != null) {
                    conditions.add(new Condition(leftColIndex, op, constantValue));
                } else if (rightColIndex != null && constantValue != null) {
                    // Pour simplifier, on ne gère pas les constantes à gauche
                    throw new IllegalArgumentException("Condition invalide : constante à gauche et colonne à droite non géré.");
                } else {
                    throw new IllegalArgumentException("Condition invalide : aucun terme colonne détecté.");
                }
            }
        }
    }

    private String findOperator(String condStr) {
        if (condStr.contains("<=")) return "<=";
        if (condStr.contains(">=")) return ">=";
        if (condStr.contains("<>")) return "<>";
        if (condStr.contains("=")) return "=";
        if (condStr.contains("<")) return "<";
        if (condStr.contains(">")) return ">";
        if (condStr.contains("!=")) return "!=";
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
        if (valStr.startsWith("\"") && valStr.endsWith("\"")) {
            return valStr.substring(1, valStr.length() - 1);
        }

        try {
            return Integer.parseInt(valStr);
        } catch (NumberFormatException e) {
            try {
                return Float.parseFloat(valStr);
            } catch (NumberFormatException e2) {
                return null;
            }
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
}
