package up.mi.jgm.td3;

import java.io.IOException;
import java.util.List;

public class RelationScanner implements IRecordIterator {
    private Relation relation;
    private List<Record> records;
    private int indexCourant;

    public RelationScanner(Relation relation) throws IOException {
        this.relation = relation;

        this.records = relation.GetAllRecords();
        this.indexCourant = 0;
    }

    @Override
    public Record getNextRecord() {
        if (indexCourant < records.size()) {
            return records.get(indexCourant++);
        } else {
            return null;
        }
    }

    @Override
    public void close() {

        records = null;
    }

    @Override
    public void reset() {
        indexCourant = 0;
    }
}
