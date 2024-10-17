package up.mi.jgm.td3;

public class ColInfo {
    private String columnName;
    private String columnType;

    public ColInfo(String columnName, String columnType) {
        this.columnName = columnName;
        this.columnType = columnType;
    }

    public String getColumnName() {
        return columnName;
    }

    public String getColumnType() {
        return columnType;
    }
}
