package jormCore.criteria;

public class WhereClause {

    private String propertyName;
    private Object value;
    private ComparisonOperator comparisonOperator;
    private LogicOperator logicOperator;
    private WhereClause leftClause;
    private WhereClause rightClause;

    // ggf ueber FieldWrapper
    public WhereClause(String propertyName, Object value, ComparisonOperator operator) {
        this.propertyName = propertyName;
        this.value = value;
        this.comparisonOperator = operator;
    }

    public WhereClause(WhereClause leftClause, WhereClause rightClause, LogicOperator operator) {
        if (leftClause != null && rightClause != null) {
            this.leftClause = leftClause;
            this.rightClause = rightClause;
            this.logicOperator = operator;
        } 
        else 
        {
            // default implementation
            loadDefault();
        }
    }

    private void loadDefault() {
        this.comparisonOperator = ComparisonOperator.Equal;
        this.propertyName = "1";
        this.value = 1;
    }

    public WhereClause And(WhereClause clause) {
        if (clause == null)
            return this;

        return new WhereClause(this, clause, LogicOperator.And);
    }

    public WhereClause Or(WhereClause clause) {
        if (clause == null)
            return this;

        return new WhereClause(this, clause, LogicOperator.Or);
    }

    public String getPropertyName() {
        return propertyName;
    }

    public Object getValue() {
        return value;
    }

    public ComparisonOperator getComparisonOperator() {
        return comparisonOperator;
    }

    public LogicOperator getLogicOperator() {
        return logicOperator;
    }

    public WhereClause getLeftClause() {
        return leftClause;
    }

    public WhereClause getRightClause() {
        return rightClause;
    }
}
