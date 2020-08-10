package jormSQLite;

import jormCore.DependencyConfiguration;
import jormCore.criteria.StatementBuilder;
import jormCore.dbConnection.DatabaseConnection;
import jormCore.dbConnection.FieldTypeParser;
import jormCore.wrapping.WrappingHandler;
import jormSQLite.dbConnection.FieldTypeParserSQLite;
import jormSQLite.dbConnection.SQLiteConnection;
import jormSQLite.criteria.SQLiteStatementBuilder;

public class DependencyConfigurationSQLite extends DependencyConfiguration {
    @Override
    protected void configureTypes() {
        addMapping(DatabaseConnection.class, SQLiteConnection.class);
        addMapping(FieldTypeParser.class, FieldTypeParserSQLite.class);
        addMapping(StatementBuilder.class, SQLiteStatementBuilder.class);
        addMapping(WrappingHandler.class, WrappingHandler.class,true);
    }
}
