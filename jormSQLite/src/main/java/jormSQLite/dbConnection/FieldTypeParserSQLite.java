package jormSQLite.dbConnection;

import jormCore.PersistentObject;
import jormCore.dbConnection.FieldTypeParser;

import java.util.Date;
import java.util.UUID;

public class FieldTypeParserSQLite extends  FieldTypeParser{

    private String normalizeValueForInsertStatement(Class<?> type, Object value) {
        if (type == String.class)
            return "'" + value + "'";

        if (PersistentObject.class.isAssignableFrom(type))
            return "'" + ((PersistentObject) value).getID() + "'";

        if (type == Date.class)
            return "" + ((Date) value).getTime();

        if (type == UUID.class)
            return "'" + value.toString() + "'";

        return value.toString();
    }

    @Override
    public String parseFieldType(Class<?> type,int Size) {
        if (type == String.class || type == char.class || type == UUID.class
                || PersistentObject.class.isAssignableFrom(type))
            return "TEXT";

        if (type == int.class || type == Date.class || type == boolean.class)
            return "INTEGER";

        return "TEXT";
    }

    @Override
    public Object castValue(Class<?> type, Object value) {

        if (value == null)
            return null;

        if (type == String.class)
            return value.toString();

        if (type == int.class)
            return Integer.parseInt(value.toString());

        if (type == Date.class)
            return new Date(Long.parseLong(value.toString()));

        if (type == boolean.class)
            return !value.toString().equals("0");

        if (type == UUID.class)
            return UUID.fromString(value.toString());

        return null;

    }

    @Override
    public String normalizeValueForInsertStatement(Object value) {
        if (value == null)
            return "NULL";

        return normalizeValueForInsertStatement(value.getClass(), value);
    }
}
