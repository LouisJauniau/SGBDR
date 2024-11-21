package up.mi.jgm.td3;

import java.util.ArrayList;
import java.util.List;

public class Record {
    private List<Value> values;

    public Record() {
        this.values = new ArrayList<>();
    }

    public Record(List<Value> values) {
        this.values = values;
    }

    public void addValue(Value value) {
        values.add(value);
    }

    public List<Value> getValues() {
        return values;
    }

    public Value getValue(int index) {
        return values.get(index);
    }

    public int getSize() {
        return values.size();
    }
}