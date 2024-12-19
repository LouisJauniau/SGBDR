package up.mi.jgm.bdda;

/*
gere la conversion appropriée des colonnes car le comportement diffère
en fonction du type. les operateurs "<", ">", "==" n'ont pas le meme comportement
en fonction de si c'est des int ou str par exemple.
 */
public class Condition {
    private int indexColonne; // Indice de la colonne concernée
    private String operator;  // Opérateur de comparaison (exemple : =, >, <, et autre)
    private Object valeurConstante;     // Valeur constante pour comparaison
    private Integer indexAutreColonne; // Indice d'une autre colonne si nécessaire

    //Constructeur adapté si des operations sur colonne et valeur
    public Condition(int columnIndex, String operator, Object value) {
        this.indexColonne = columnIndex;
        this.operator = operator;
        this.valeurConstante = value;
        this.indexAutreColonne = null; // Pas forcément utilisée la seconde colonne.
    }

    //Constructeur adapté si des operations sur deux colonnes
    public Condition(int columnIndex, String operator, int otherColumnIndex) {
        this.indexColonne = columnIndex;
        this.operator = operator;
        this.valeurConstante = null; // Pas de constante
        this.indexAutreColonne = otherColumnIndex; // Comparaison avec une autre colonne
    }

    public Condition(Integer columnIndex, String operator, Integer otherColumnIndex) {
        this.indexColonne = columnIndex; // Conversion automatique d'Integer vers int
        this.operator = operator;
        this.valeurConstante = null; // Pas de constante
        this.indexAutreColonne = otherColumnIndex; // Comparaison avec une autre colonne
    }



    //On aurait pû utiliser wildcards avec Class<?>[] mais défaut d'utilisation.
    //On utilise donc simplement la généricité pour pouvoir representer les differentes valeurs possibles.
    public <T> boolean evaluate(Record record, Class<T>[] columnTypes) {
        // Récupérer la valeur de la colonne
    	T valeurColonne = (T) record.getValue(indexColonne).getData(); // Cast explicite vers T
        Class<T> typeColonne = columnTypes[indexColonne];

        // Comparaison avec une autre colonne
        if (indexAutreColonne != null) {
        	T valeurAutreColonne = (T) record.getValue(indexAutreColonne).getData(); // Cast explicite vers T
            Class<T> typeAutreColonne = columnTypes[indexAutreColonne];

            // Vérifier l'égalité des types
            if (!typeColonne.equals(typeAutreColonne)) {
                throw new IllegalArgumentException("Types incompatibles pour la comparaison");
            }

            return compare(valeurColonne, valeurAutreColonne, typeColonne);
        }

        // Comparaison colonne -> constante
        T constante = (T) valeurConstante; // Cast explicite vers T
        return compare(valeurColonne, constante, typeColonne);
    }


    private <T> boolean compare(T valeur1, T valeur2, Class<T> typeValeur) {
        // Effectuer la conversion et la comparaison en fonction de l'opérateur
        if (typeValeur == Integer.class) {
            //utilise cast explicite pour utiliser compareIntegers()
            return compareIntegers((Integer) valeur1, (Integer) valeur2);
        } else if (typeValeur == String.class) {
            //utilise cast explicite pour utiliser compareStrings()
            return compareStrings((String) valeur1, (String) valeur2);
        } else {
            throw new UnsupportedOperationException("Type non supporté: " + typeValeur);
        }
    }

    private boolean compareIntegers(Integer valeur1, Integer valeur2) {
        switch (operator) {
            case "=": return valeur1.equals(valeur2);
            case ">": return valeur1 > valeur2;
            case "<": return valeur1 < valeur2;
            case ">=": return valeur1 >= valeur2;
            case "<=": return valeur1 <= valeur2;
            case "<>": return !valeur1.equals(valeur2);
            default: throw new UnsupportedOperationException("Opérateur non supporté: " + operator);
        }
    }

    private boolean compareStrings(String valeur1, String valeur2) {
        switch (operator) {
            case "=": return valeur1.equals(valeur2);
            case ">": return valeur1.compareTo(valeur2) > 0;
            case "<": return valeur1.compareTo(valeur2) < 0;
            case ">=": return valeur1.compareTo(valeur2) >= 0;
            case "<=": return valeur1.compareTo(valeur2) <= 0;
            case "<>": return !valeur1.equals(valeur2);
            default: throw new UnsupportedOperationException("Opérateur non supporté: " + operator);
        }
    }
}
