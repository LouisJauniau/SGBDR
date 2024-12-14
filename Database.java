package up.mi.jgm.bdda;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Classe représentant une base de données.
 */
public class Database implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private Map<String, Relation> tables;

    public Database(String name) {
        this.name = name;
        this.tables = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public Map<String, Relation> getTables() {
        return tables;
    }

    public void addTable(Relation table) {
        tables.put(table.getName(), table);
    }

    public Relation getTable(String tableName) {
        return tables.get(tableName);
    }

    public void removeTable(String tableName) {
        tables.remove(tableName);
    }

    public void removeAllTables() {
        tables.clear();
    }
}