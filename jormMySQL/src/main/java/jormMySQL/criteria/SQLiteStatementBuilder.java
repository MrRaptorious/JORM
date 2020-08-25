package jormMySQL.criteria;

import jormCore.ChangedObject;
import jormCore.PersistentObject;
import jormCore.criteria.ComparisonOperator;
import jormCore.criteria.StatementBuilder;
import jormCore.criteria.WhereClause;
import jormCore.dbConnection.FieldTypeParser;
import jormCore.wrapping.ClassWrapper;
import jormCore.wrapping.FieldWrapper;
import jormCore.wrapping.WrappingHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SQLiteStatementBuilder extends StatementBuilder {

    public SQLiteStatementBuilder(FieldTypeParser parser, WrappingHandler handler) {
        super(parser,handler);
    }

    protected String calculateWhereClause(WhereClause clause) {

        if (clause == null)
            return "";

        if (clause.getLeftClause() == null && clause.getRightClause() == null) {
            return evaluateBasicWhereClause(clause);
        }

        if (clause.getLeftClause() == null && clause.getRightClause() != null) {
            return " ( " + evaluateBasicWhereClause(clause) + calculateLogicOperator(clause.getLogicOperator())
                    + calculateWhereClause(clause.getRightClause()) + " ) ";
        }

        if (clause.getLeftClause() != null && clause.getRightClause() == null) {
            return " ( " + calculateWhereClause(clause.getLeftClause())
                    + calculateLogicOperator(clause.getLogicOperator()) + evaluateBasicWhereClause(clause) + " ) ";
        }

        if (clause.getLeftClause() != null && clause.getRightClause() != null) {
            return " ( " + calculateWhereClause(clause.getLeftClause())
                    + calculateLogicOperator(clause.getLogicOperator()) + calculateWhereClause(clause.getRightClause())
                    + " ) ";
        }

        return "";
    }

    private String evaluateBasicWhereClause(WhereClause clause) {
        return " ( " + clause.getPropertyName() + calculateComparisonOperator(clause.getComparisonOperator())
                + this.fieldTypeParser.normalizeValueForInsertStatement(clause.getValue())
                + " ) ";
    }

    public String createSelect(ClassWrapper type, WhereClause whereClause, boolean loadDeleted) {
        // SELECT * FROM [TYPE] WHERE [WHERE]
        String result = "SELECT * FROM " + type.getName();
        WhereClause resultingClause = new WhereClause("DELETED", 0, ComparisonOperator.Equal);
        
        // normal case
        if(!loadDeleted)
        {
            resultingClause = resultingClause.And(whereClause);
        }
        else if (whereClause != null)
        {
            resultingClause = whereClause;
        }
        else
        {
            return result;
        }

        result += " WHERE " + calculateWhereClause(resultingClause);

//        System.out.println(result);
//        System.out.println();

        return result;
    }

    @Override
    public String createSelect(ClassWrapper type, WhereClause whereClause) {
        return createSelect(type, whereClause, false);
    }

    @Override
    public String createInsert(PersistentObject obj) {
        String result = "INSERT INTO ";
        String columnPart = "(";
        String valuePart = " VALUES(";

        Map<FieldWrapper, Object> objectValues = obj.getPersistentPropertiesWithValues();

        // add tableName
        result += wrappingHandler.getClassWrapper(obj.getClass()).getName();

        String delimiter = "";

        for (Map.Entry<FieldWrapper, Object> elem : objectValues.entrySet()) {

            if (elem.getValue() == null)
                continue;

            columnPart += delimiter + elem.getKey().getName();
            valuePart += delimiter
                    //+ fieldTypeParser.normalizeValueForInsertStatement(elem.getKey().getOriginalField().getType(), elem.getValue());
                    + fieldTypeParser.normalizeValueForInsertStatement(elem.getValue());

            if (delimiter == "")
                delimiter = " , ";
        }

        result += columnPart + ") " + valuePart + ")";

        return  result;
    }

    @Override
    public String createUpdate(ChangedObject obj) {
        ClassWrapper currentClassWrapper = wrappingHandler.getClassWrapper(obj.getRuntimeObject().getClass());

        String result = "UPDATE ";
        result += currentClassWrapper.getName();

        result += " SET ";

        String delimiter = "";

        for (Map.Entry<String, Object> elm : obj.getChangedFields().entrySet()) {

            FieldWrapper currentFieldWrapper = currentClassWrapper.getFieldWrapper(elm.getKey());

            result += delimiter + currentFieldWrapper.getName();
            result += " = ";
//            result += normalizeValueForInsertStatement(currentFieldWrapper.getOriginalField().getType(), elm.getValue());
            result += fieldTypeParser.normalizeValueForInsertStatement(elm.getValue());

            if (delimiter == "")
                delimiter = " , ";
        }

        result += " WHERE ";
        result += currentClassWrapper.getPrimaryKeyMember().getName() + " = ";
        result += "'" + obj.getRuntimeObject().getID() + "'";


        return  result;
    }

    @Override
    public String createEntity(ClassWrapper clsWrapper) {
        List<String> fKStatements = new ArrayList<>();

        String result = "CREATE TABLE IF NOT EXISTS " + clsWrapper.getName() + " (";

        for (int i = 0; i < clsWrapper.getWrappedFields().size(); i++) {

            FieldWrapper wr = clsWrapper.getWrappedFields().get(i);

            result += generateFieldDefinition(wr);

            if (wr.isForeignKey()) {
                fKStatements.add(generateForeignKeyDefinition(wr));
            }

            if (i < clsWrapper.getWrappedFields().size() - 1 || fKStatements.size() > 0)
                result += " ,";
        }

        // add FK definitions
        for (int i = 0; i < fKStatements.size(); i++) {

            result += fKStatements.get(i);

            if (i < fKStatements.size() - 1)
                result += " , ";

        }

        result += " ) ENGINE=InnoDB";

        return result;
    }

    public List<String> createAllEntity() {
        ArrayList<String> statements = new ArrayList<>();

        for (ClassWrapper classWrapper : wrappingHandler.getWrapperList()) {
            statements.add(createEntity(classWrapper));
        }

        return  statements;
    }

    public String generateFieldDefinition(FieldWrapper wr) {
        String result = "";

        result += wr.getName();
        result += " ";
        result += wr.getDBType();

        if (wr.isPrimaryKey())
            result += " PRIMARY KEY ";
        if (wr.isAutoincrement())
            result += " AUTOINCREMENT ";
        if (wr.isCanNotBeNull())
            result += " NOT NULL ";

        return result;
    }

    public String generateForeignKeyDefinition(FieldWrapper wr) {
        if (wr.isForeignKey()) {
            return " FOREIGN KEY(" + wr.getName() + ") REFERENCES " + wr.getForeignKey().getReferencingType().getName()
                    + "(" + wr.getForeignKey().getReferencingPrimaryKeyName() + ") ";
        }
        return "";
    }

    @Override
    public String createAddPropertyToEntity(FieldWrapper fieldWrapper) {
        return "ALTER TABLE " + fieldWrapper.getClassWrapper().getName() + " ADD " + generateFieldDefinition(fieldWrapper);
    }

    @Override
    protected String EQUAL() {
        return " = ";
    }

    @Override
    protected String NOTEQUAL() {
        return " <> ";
    }

    @Override
    protected String LESS() {
        return " < ";
    }

    @Override
    protected String LESSOREQUAL() {
        return " <= ";
    }

    @Override
    protected String GREATER() {
        return " > ";
    }

    @Override
    protected String GREATEROREQUAL() {
        return " >= ";
    }

    @Override
    protected String AND() {
        return " AND ";
    }

    @Override
    protected String OR() {
        return " OR ";
    }

    @Override
    protected String NOT() {
        return " NOT ";
    }
}
