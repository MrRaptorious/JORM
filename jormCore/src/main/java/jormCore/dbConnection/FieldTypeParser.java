package jormCore.dbConnection;

public abstract class FieldTypeParser {
    public abstract String parseFieldType(Class<?> type, int size);

    public String parseFieldType(Class<?> type) {
        return parseFieldType(type, -1);
    }

    public abstract Object castValue(Class<?> type, Object value);

    public abstract String normalizeValueForInsertStatement(Object value);
}
