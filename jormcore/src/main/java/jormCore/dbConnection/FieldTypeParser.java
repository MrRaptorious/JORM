package jormCore.DBConnection;

public interface FieldTypeParser {
	public abstract String parseFieldType(Class<?> type);
	public abstract Object castValue(Class<?> type, Object value);
	public abstract String normalizeValueForInsertStatement(Object value);
}
