package jormMySQL.dbConnection;

import jormCore.PersistentObject;
import jormCore.annotaions.Size;
import jormCore.dbConnection.FieldTypeParser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class FieldTypeParserMySQL extends FieldTypeParser {

    private String normalizeValueForInsertStatement(Class<?> type, Object value) {
        if (type == String.class)
            return "'" + (String) value + "'";

        if (PersistentObject.class.isAssignableFrom(type))
            return "'" + ((PersistentObject) value).getID() + "'";

        if (type == Date.class)
            return "'" + new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format((Date)value) + "'";


        if (type == UUID.class)
            return "'" + ((UUID) value).toString() + "'";

        return value.toString();
    }

    @Override
    public String parseFieldType(Class<?> type, int size) {
        if (type == String.class || type == char.class
                || PersistentObject.class.isAssignableFrom(type))
            return size != -1 ? String.format("VARCHAR(%s)", size) : String.format("VARCHAR(%s)", Size.DefaultSize);

        // newer MySQL versions actually have a UUID type
        // a UUID is always 36 byte long
        if (type == UUID.class)
            return size == -1 ? "VARCHAR(36)" : String.format("VARCHAR(%s)", size);

        if (type == Date.class)
            return "DateTime";

        if (type == int.class)
            return "INT";

        // newer versions should provide a boolean type
        if (type == boolean.class)
            return "TINYINT(1)";

        return String.format("VARCHAR(%s)", Size.DefaultSize);
    }

    @Override
    public Object castValue(Class<?> type, Object value) {

        if (value == null)
            return null;

        if (type == String.class)
            return value.toString();

        if (type == int.class)
            return Integer.parseInt(value.toString());

        if (type == Date.class) {
            try {
                return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(value.toString());
            } catch (ParseException e) {
                e.printStackTrace(); // TODO
            }
        }

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
