package jormMySQL;

import jormCore.DependencyConfiguration;
import jormCore.criteria.StatementBuilder;
import jormCore.dbConnection.DatabaseConnection;
import jormCore.dbConnection.FieldTypeParser;
import jormCore.wrapping.WrappingHandler;
import jormMySQL.dbConnection.FieldTypeParserMySQL;
import jormMySQL.dbConnection.MySQLConnection;
import jormMySQL.criteria.SQLiteStatementBuilder;

public class DependencyConfigurationMySQL extends DependencyConfiguration {
    @Override
    protected void configureTypes() {
        addMapping(DatabaseConnection.class, MySQLConnection.class);
        addMapping(FieldTypeParser.class, FieldTypeParserMySQL.class);
        addMapping(StatementBuilder.class, SQLiteStatementBuilder.class);
        addMapping(WrappingHandler.class, WrappingHandler.class,true);
    }
}
