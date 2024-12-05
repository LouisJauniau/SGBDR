package up.mi.jgm.td3;


/*
gere la conversion appropriée des colonnes car le comportement diffère
en fonction du type. les operateurs "<", ">", "==" n'ont pas le meme comportement
en fonction de si c'est des int ou str par exemple.
 */
public class Condition {
    private int columnIndex; // Indice de la colonne concernée
    private String operator;  // Opérateur de comparaison (exemple : =, >, <, et autre)
    private Object value;     // Valeur constante pour comparaison
    private Integer otherColumnIndex; // Indice d'une autre colonne si nécessaire

    //Constructeur adapté si des operations sur colonne et valeur
    public Condition(int columnIndex, String operator, Object value) {
        this.columnIndex = columnIndex;
        this.operator = operator;
        this.value = value;
        this.otherColumnIndex = null; // Pas forcément utilisée la seconde colonne.
    }

    //Constructeur adapté si des operations sur deux colonnes
    public Condition(int columnIndex, String operator, int otherColumnIndex) {
        this.columnIndex = columnIndex;
        this.operator = operator;
        this.value = null; // Pas de constante
        this.otherColumnIndex = otherColumnIndex; // Comparaison avec une autre colonne
    }

    public boolean evaluate(Record record, Class<?>[] columnTypes) {
        // Récupérer la valeur de la colonne
        Object columnValue = record.getValue(columnIndex);
        Class<?> columnType = columnTypes[columnIndex];

        // Comparaison avec une autre colonne
        if (otherColumnIndex != null) {
            Object otherValue = record.getValue(otherColumnIndex);
            Class<?> otherType = columnTypes[otherColumnIndex];

            // Vérifier l'égalité des types
            if (!columnType.equals(otherType)) {
                throw new IllegalArgumentException("Types incompatibles pour la comparaison");
            }

            return compare(columnValue, otherValue, columnType);
        }

        // Comparaison avec une constante
        return compare(columnValue, value, columnType);
    }

    private boolean compare(Object value1, Object value2, Class<?> valueType) {
        // Effectuer la conversion et la comparaison en fonction de l'opérateur
        if (valueType == Integer.class) {
            return compareIntegers((Integer) value1, (Integer) value2);
        } else if (valueType == String.class) {
            return compareStrings((String) value1, (String) value2);
        } else {
            throw new UnsupportedOperationException("Type non supporté: " + valueType);
        }
    }

    private boolean compareIntegers(Integer value1, Integer value2) {
        switch (operator) {
            case "=": return value1.equals(value2);
            case ">": return value1 > value2;
            case "<": return value1 < value2;
            case ">=": return value1 >= value2;
            case "<=": return value1 <= value2;
            case "!=": return !value1.equals(value2);
            default: throw new UnsupportedOperationException("Opérateur non supporté: " + operator);
        }
    }

    private boolean compareStrings(String value1, String value2) {
        switch (operator) {
            case "=": return value1.equals(value2);
            case ">": return value1.compareTo(value2) > 0;
            case "<": return value1.compareTo(value2) < 0;
            case ">=": return value1.compareTo(value2) >= 0;
            case "<=": return value1.compareTo(value2) <= 0;
            case "!=": return !value1.equals(value2);
            default: throw new UnsupportedOperationException("Opérateur non supporté: " + operator);
        }
    }
}