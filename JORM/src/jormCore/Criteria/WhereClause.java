package jormCore.Criteria;

public class WhereClause {

    private String propertyName;
    private Object value;
    private ComparisonOperator comparisonOperator;
    private LogicOperator logicOperator;
    private WhereClause leftClause;
    private WhereClause rightClause;  

    // ggf ueber FieldWrapper
    public WhereClause(String propertyName, ComparisonOperator operator, Object value)
    {
        this.propertyName = propertyName;
        this.value = value;
        this.comparisonOperator = operator;
    }

    public WhereClause(WhereClause leftClause, WhereClause rightClause, LogicOperator operator)
    {
        this.leftClause = leftClause;
        this.rightClause = rightClause;
        this.logicOperator = operator;
    }

    // public WhereClause(String propertyName, List<Object> value)
    // {
    //     this.propertyName = propertyName;
    //     this.value = value;
    //     this.operator = ComparisonOperator.In;
    // }

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
