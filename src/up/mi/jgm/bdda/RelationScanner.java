package up.mi.jgm.bdda;

import java.io.IOException;
import java.util.List;

public class RelationScanner implements IRecordIterator {
    private List<Record> records;
    private int index;

    public RelationScanner(Relation relation) throws IOException {
        this.records = relation.GetAllRecords();
        this.index = 0;
    }

    @Override
    public Record getNextRecord() {
        if (index < records.size()) {
            return records.get(index++);
        }
        return null;
    }

    @Override
    public void close() {
        records = null;
    }

    @Override
    public void reset() {
        index = 0;
    }
}