package up.mi.jgm.td3;

import java.nio.ByteBuffer;
import java.util.List;

public class Relation {
    private String name;
    private int columnCount;
    private List<ColInfo> columns;

    public Relation(String name, int columnCount, List<ColInfo> columns) {
        this.name = name;
        this.columnCount = columnCount;
        this.columns = columns;
    }

    public int writeRecordToBuffer(Record record, ByteBuffer buffer, int pos) {
        int initialPos = pos;
        if (columns.stream().anyMatch(col -> col.getColumnType().startsWith("VARCHAR"))) {
            // Utilisation du format à taille variable
            int[] offsets = new int[record.getValues().size() + 1];
            for (int i = 0; i < record.getValues().size(); i++) {
                offsets[i] = pos;
                String value = record.getValues().get(i);
                if (columns.get(i).getColumnType().equals("INT")) {
                    buffer.putInt(pos, Integer.parseInt(value));
                    pos += Integer.BYTES;
                } else if (columns.get(i).getColumnType().equals("REAL")) {
                    buffer.putFloat(pos, Float.parseFloat(value));
                    pos += Float.BYTES;
                } else {
                    for (char c : value.toCharArray()) {
                        buffer.putChar(pos, c);
                        pos += Character.BYTES;
                    }
                }
            }
            offsets[record.getValues().size()] = pos;
            for (int offset : offsets) {
                buffer.putInt(initialPos, offset);
                initialPos += Integer.BYTES;
            }
        } else {
            // Utilisation du format à taille fixe
            for (int i = 0; i < record.getValues().size(); i++) {
                String value = record.getValues().get(i);
                if (columns.get(i).getColumnType().equals("INT")) {
                    buffer.putInt(pos, Integer.parseInt(value));
                    pos += Integer.BYTES;
                } else if (columns.get(i).getColumnType().equals("REAL")) {
                    buffer.putFloat(pos, Float.parseFloat(value));
                    pos += Float.BYTES;
                } else {
                    for (char c : value.toCharArray()) {
                        buffer.putChar(pos, c);
                        pos += Character.BYTES;
                    }
                }
            }
        }
        return pos - initialPos;
    }

    public int readFromBuffer(Record record, ByteBuffer buffer, int pos) {
        List<String> values = record.getValues();
        if (columns.stream().anyMatch(col -> col.getColumnType().startsWith("VARCHAR"))) {
            // Lecture en format à taille variable
            int[] offsets = new int[values.size() + 1];
            for (int i = 0; i < values.size() + 1; i++) {
                offsets[i] = buffer.getInt(pos);
                pos += Integer.BYTES;
            }
            for (int i = 0; i < values.size(); i++) {
                int start = offsets[i];
                int end = offsets[i + 1];
                byte[] byteValue = new byte[end - start];
                buffer.position(start);
                buffer.get(byteValue);
                String value = new String(byteValue);
                values.set(i, value);
            }
        } else {
            // Lecture en format à taille fixe
            for (int i = 0; i < columns.size(); i++) {
                String type = columns.get(i).getColumnType();
                if (type.equals("INT")) {
                    values.set(i, String.valueOf(buffer.getInt(pos)));
                    pos += Integer.BYTES;
                } else if (type.equals("REAL")) {
                    values.set(i, String.valueOf(buffer.getFloat(pos)));
                    pos += Float.BYTES;
                } else {
                    StringBuilder value = new StringBuilder();
                    for (int j = 0; j < type.length(); j++) {
                        value.append(buffer.getChar(pos));
                        pos += Character.BYTES;
                    }
                    values.set(i, value.toString());
                }
            }
        }
        record.setValues(values);
        return pos;
    }
}
