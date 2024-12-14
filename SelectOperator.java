package up.mi.jgm.bdda;

import java.util.List;

public class SelectOperator<T> implements IRecordIterator {

    private IRecordIterator it;
    private List<Condition> conditions;
    private Class<T>[] typeColonnes;

    public SelectOperator(IRecordIterator it, List<Condition> conditions, Class<T>[] typeColonnes) {
        this.it = it;
        this.conditions = conditions;
        this.typeColonnes = typeColonnes;
    }

    @Override
    public Record getNextRecord() {
        Record rec;
        while ((rec = it.getNextRecord()) != null) {
            boolean ok = true;
            for (Condition cond : conditions) {
                if (!cond.evaluate(rec, typeColonnes)) {
                    ok = false;
                    break;
                }
            }
            if (ok) return rec;
        }
        return null;
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