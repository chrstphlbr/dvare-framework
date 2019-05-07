/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2016-2019 DVARE (Data Validation and Aggregation Rule Engine)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Sogiftware.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.dvare.expression.operation;

import org.dvare.binding.data.InstancesBinding;
import org.dvare.exceptions.interpreter.InterpretException;
import org.dvare.expression.Expression;
import org.dvare.expression.literal.LiteralExpression;

import java.util.List;

/**
 * @author Muhammad Hammad
 * @since 2016-06-30
 */
public class CompositeOperationExpression extends Expression {

    private List<Expression> expressions;

    public CompositeOperationExpression(List<Expression> expressions) {
        this.expressions = expressions;
    }

    @Override
    public LiteralExpression interpret(InstancesBinding instancesBinding) throws InterpretException {

        LiteralExpression result = null;
        for (Expression expression : expressions) {
            result = expression.interpret(instancesBinding);
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder toStringBuilder = new StringBuilder();
        if (expressions != null) {
            for (Expression expression : expressions) {
                toStringBuilder.append(expression);
                toStringBuilder.append(" ");
            }
        }

        return toStringBuilder.toString();
    }
}
