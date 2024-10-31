package up.mi.jgm.bdda;

public class ColInfo {
    private String name;

    public enum Type {
        INT,
        REAL,
        CHAR,
        VARCHAR
    }

    private Type type;
    private int size; // Taille pour CHAR(T) et VARCHAR(T)

    public ColInfo(String name, Type type, int size) {
        this.name = name;
        this.type = type;
        this.size = size; // Pour INT et REAL, size sera ignoré
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public int getSize() {
        return size;
    }
}
