package up.mi.jgm.td3;

import java.io.IOException;
import java.util.List;

public class RelationScannerWithSelect<T> implements IRecordIterator {
    private Relation relation;
    private List<Condition> conditions;
    private List<Record> records;
    private int indexCourant;
    private Class<T>[] typeColonne;


    public RelationScannerWithSelect(Relation relation, List<Condition> conditions) throws IOException {
        this.relation = relation;
        this.conditions = conditions;
        this.records = relation.GetAllRecords();
        this.indexCourant = 0;

        // Déterminer le type des colonnes depuis la relation
        List<ColInfo> cols = relation.getColumns();
        typeColonne = (Class<T>[]) new Class[cols.size()]; // Crée un tableau typé de Class<T>
        for (int i = 0; i < cols.size(); i++) {
            ColInfo col = cols.get(i);
            switch (col.getType()) {
                case INT:
                    typeColonne[i] = (Class<T>) Integer.class;
                    break;
                case REAL:
                    typeColonne[i] = (Class<T>) Float.class;
                    break;
                case CHAR:
                case VARCHAR:
                    typeColonne[i] = (Class<T>) String.class;
                    break;
                default:
                    throw new UnsupportedOperationException("Type non supporté");
            }
        }
    }

    @Override
    public Record getNextRecord() {
        // Parcourt les records jusqu'à en trouver un satisfaisant toutes les conditions
        while (indexCourant < records.size()) {
            Record currentRecord = records.get(indexCourant++);
            if (satisfiesAllConditions(currentRecord)) {
                return currentRecord;
            }
        }
        return null; // Aucun record satisfaisant de plus
    }

    @Override
    public void close() {
        records = null;
    }

    @Override
    public void reset() {
        indexCourant = 0;
    }

    private boolean satisfiesAllConditions(Record record) {
        for (Condition condition : conditions) {
            if (!condition.evaluate(record, typeColonne)) {
                return false;
            }
        }
        return true;
    }
}
