package up.mi.jgm.td3;

public class Value {
    public enum Type {
        INT,
        REAL,
        CHAR,
        VARCHAR
    }

    private Type type;
    private Object data;

    public Value(Type type, Object data) {
        this.type = type;
        this.data = data;
    }

    public Type getType() {
        return type;
    }

    public Object getData() {
        return data;
    }
}
