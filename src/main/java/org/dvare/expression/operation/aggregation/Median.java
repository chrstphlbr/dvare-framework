/*The MIT License (MIT)

Copyright (c) 2016 Muhammad Hammad

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Sogiftware.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.*/


package org.dvare.expression.operation.aggregation;

import org.dvare.annotations.Operation;
import org.dvare.binding.data.InstancesBinding;
import org.dvare.exceptions.interpreter.InterpretException;
import org.dvare.expression.Expression;
import org.dvare.expression.datatype.DataType;
import org.dvare.expression.datatype.IntegerType;
import org.dvare.expression.literal.LiteralExpression;
import org.dvare.expression.literal.LiteralType;
import org.dvare.expression.literal.NullLiteral;
import org.dvare.expression.operation.AggregationOperationExpression;
import org.dvare.expression.operation.OperationType;
import org.dvare.expression.veriable.VariableExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@Operation(type = OperationType.MEDIAN, dataTypes = {DataType.FloatType, DataType.IntegerType})
public class Median extends AggregationOperationExpression {
    static Logger logger = LoggerFactory.getLogger(Median.class);


    public Median() {
        super(OperationType.MEDIAN);
    }


    @Override
    public Object interpret(InstancesBinding instancesBinding) throws InterpretException {

        Object aggregation = instancesBinding.getInstance("self");
        List<Object> dataSet = (List) instancesBinding.getInstance("data");

        LiteralExpression leftExpression = null;
        if (leftOperand instanceof LiteralExpression) {
            leftExpression = (LiteralExpression) leftOperand;
        } else {
            leftExpression = new NullLiteral();
        }


        int length = dataSet.size();
        int middle = length / 2;

        Expression right = this.rightOperand;
        if (right instanceof VariableExpression) {
            VariableExpression variableExpression = ((VariableExpression) right);
            String name = variableExpression.getName();
            DataType type = toDataType(variableExpression.getType());

            switch (type) {

                case FloatType: {

                    List<Float> values = new ArrayList<>();

                    for (Object bindings : dataSet) {
                        Object value = getValue(bindings, name);
                        if (value instanceof Float) {
                            values.add((Float) value);
                        }
                    }


                    Float result = 0f;
                    if (length % 2 == 1) {
                        result = values.get(middle);

                    } else {
                        Float value1 = values.get(middle - 1);
                        Float value2 = values.get(middle - 2);
                        result = (value1 + value2) / 2.f;
                    }


                    leftExpression = LiteralType.getLiteralExpression(result, IntegerType.class);

                    break;
                }
                case IntegerType: {
                    List<Integer> values = new ArrayList<>();

                    for (Object bindings : dataSet) {
                        Object value = getValue(bindings, name);
                        if (value instanceof Integer) {
                            values.add((Integer) value);
                        }
                    }


                    Integer result = 0;
                    if (length % 2 == 1) {
                        result = values.get(middle);

                    } else {
                        Integer value1 = values.get(middle);
                        Integer value2 = values.get(middle - 1);
                        result = (value1 + value2) / 2;
                    }


                    leftExpression = LiteralType.getLiteralExpression(result, IntegerType.class);
                    break;
                }
            }
        }


        return leftExpression;
    }

}