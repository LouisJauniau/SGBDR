package up.mi.jgm.td3;



import java.util.List;

/*
Classe permetant de réaliser une SELECTION.
La selection filtre les lignes d'une table tandis qu'une PROJECTION
filtre les colonnes.

On utilise la généricité car elle est utilisée dans la Classe COndition.

 */

public class SelectOperator<T> implements IRecordIterator {

    private IRecordIterator it;           // L'iteration défini par l'interface
    private List<Condition> conditions;      // Liste de conditions à réunir
    private Class<T>[] typeColonnes;          // Types des colonnes (nécessaire pour évaluation conditions)

    public SelectOperator(IRecordIterator it, List<Condition> conditions, Class<T>[] typeColonnes) {
        this.it = it;
        this.conditions = conditions;
        this.typeColonnes = typeColonnes;
    }

    @Override
    public Record getNextRecord() {
        /*
        retourne le record courant et avance le curseur de l’it.
        Ou retourne null si plus de record dans ensemble des tuples.
         */
        Record record;
        while ((record = it.getNextRecord()) != null) {
            boolean conditionsRemplies = true;
            for (Condition cond : conditions) {
                if (!cond.evaluate(record, typeColonnes)) {
                    conditionsRemplies = false;
                    break;
                }
            }
            if (conditionsRemplies) {
                return record; // On retourne le premier record qui satisfait toutes les conditions
            }
        }
        return null; // Plus de record remplissant conditions
    }


    // Ferme l'itérateur
    @Override
    public void close() {

        it.close();
    }

    // Réinitialise l'itérateur
    @Override
    public void reset() {

        it.reset();
    }
}
