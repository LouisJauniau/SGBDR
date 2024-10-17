package sgbd;

import java.util.ArrayList;

public class Record {
    private ArrayList<Object> values;

    public Record() {
        this.values = new ArrayList<>();
    }

    public Record(ArrayList<Object> values) {
        this.values = values;
    }

    public void addValue(Object value) {
        values.add(value);
    }

    public Object getValue(int index) {
        if (index >= 0 && index < values.size()) {
            return values.get(index);
        } else {
            throw new IndexOutOfBoundsException("Index is out of bounds");
        }
    }

    public int getSize() {
        return values.size();
    }

    public ArrayList<Object> getValues() {
        return new ArrayList<>(values);
    }

    public void setValue(int index, Object value) {
        if (index >= 0 && index < values.size()) {
            values.set(index, value);
        } else {
            throw new IndexOutOfBoundsException("Index is out of bounds");
        }
    }

    @Override
    public String toString() {
        return "Record{" +
                "values=" + values +
                '}';
    }
}
