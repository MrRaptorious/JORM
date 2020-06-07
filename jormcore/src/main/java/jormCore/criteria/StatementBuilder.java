package jormCore.Criteria;

import jormCore.Wrapping.ClassWrapper;

public abstract class StatementBuilder {
    protected final String calculateLogicOperator(LogicOperator operator) {
        switch (operator) {
        case Not:
            return NOT();
        case And:
            return AND();
        case Or:
            return OR();
        }

        return null;
    }

    protected final String calculateComparisonOperator(ComparisonOperator operator) {
        switch (operator) {
        case Equal:
            return EQUAL();
        case NotEqual:
            return NOTEQUAL();
        case Less:
            return LESS();
        case LessOrEqual:
            return LESSOREQUAL();
        case Greater:
            return GREATER();
        case GreaterOrEqual:
            return GREATEROREQUAL();
        default:
            return null;
        }
    }

    public abstract String createSelect(ClassWrapper type, WhereClause whereClause);

    protected abstract String calculateWhereClause(WhereClause clause);

    public final WhereClause concatenateWhereClauses(WhereClause clause1, WhereClause clause2, LogicOperator operator) {
        return new WhereClause(clause1, clause2, operator);
    }

    // comparison
    protected abstract String EQUAL();

    protected abstract String NOTEQUAL();

    protected abstract String LESS();

    protected abstract String LESSOREQUAL();

    protected abstract String GREATER();

    protected abstract String GREATEROREQUAL();

    // logic
    protected abstract String AND();

    protected abstract String OR();

    protected abstract String NOT();
}
