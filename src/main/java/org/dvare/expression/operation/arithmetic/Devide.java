package org.dvare.expression.operation.arithmetic;

import org.dvare.annotations.OperationType;
import org.dvare.expression.datatype.DataType;
import org.dvare.expression.operation.ArithmeticOperationExpression;

@org.dvare.annotations.Operation(type = OperationType.VALIDATION, symbols = {"Div", "div", "/"}, dataTypes = {DataType.FloatType, DataType.IntegerType})
public class Devide extends ArithmeticOperationExpression {
    public Devide() {
        super("Div", "div", "/");
    }

    public Devide copy() {
        return new Devide();
    }

}