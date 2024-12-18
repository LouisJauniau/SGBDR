package up.mi.jgm.bdda;

import java.util.List;

public class ProjectOperator implements IRecordIterator {

    private IRecordIterator it;
    private List<Integer> colonnes;

    public ProjectOperator(IRecordIterator it, List<Integer> colonnes) {
        this.it = it;
        this.colonnes = colonnes;
    }

    @Override
    public Record getNextRecord() {
        Record origin = it.getNextRecord();
        if (origin == null) return null;
        Record projected = new Record();
        for (Integer c : colonnes) {
            projected.addValue(origin.getValue(c));
        }
        return projected;
    }

    @Override
    public void close() {
        it.close();
    }

    @Override
    public void reset() {
        it.reset();
    }
}