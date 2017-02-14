package org.dvare.expression.operation.validation;

import org.dvare.annotations.Operation;
import org.dvare.binding.data.InstancesBinding;
import org.dvare.binding.model.ContextsBinding;
import org.dvare.binding.model.TypeBinding;
import org.dvare.config.ConfigurationRegistry;
import org.dvare.exceptions.interpreter.InterpretException;
import org.dvare.exceptions.parser.ExpressionParseException;
import org.dvare.expression.Expression;
import org.dvare.expression.datatype.DataType;
import org.dvare.expression.literal.BooleanLiteral;
import org.dvare.expression.literal.ListLiteral;
import org.dvare.expression.literal.LiteralExpression;
import org.dvare.expression.literal.LiteralType;
import org.dvare.expression.operation.ListOperationExpression;
import org.dvare.expression.operation.OperationExpression;
import org.dvare.expression.operation.OperationType;
import org.dvare.expression.veriable.VariableExpression;
import org.dvare.expression.veriable.VariableType;
import org.dvare.util.TrimString;
import org.dvare.util.TypeFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Operation(type = OperationType.COMBINATION)
public class Combination extends OperationExpression {
    static Logger logger = LoggerFactory.getLogger(Combination.class);

    protected List<Expression> leftOperand;

    public Combination() {
        super(OperationType.COMBINATION);
    }


    @Override
    public Integer parse(String[] tokens, int pos, Stack<Expression> stack, ContextsBinding contextss) throws ExpressionParseException {

        int i = findNextExpression(tokens, pos + 1, stack, contextss);

        computeParam(this.leftOperand);
        stack.push(this);
        return i;
    }


    private void computeParam(List<Expression> expressions) throws ExpressionParseException {


        String error = null;


        if (expressions == null || expressions.isEmpty()) {
            error = "No Parameters Found Expression Found";

        } else if (expressions.size() != 2) {

            error = "two param need in combination function ";
        } else {

            if (!(expressions.get(0) instanceof VariableExpression)) {
                error = "First param of combination function must be variable";
            }

            if (!(expressions.get(1) instanceof ListLiteral) && !(expressions.get(1) instanceof ListOperationExpression)) {
                error = "Second param of combination function must be variable";
            }


        }


        if (error != null && !error.isEmpty()) {
            logger.error(error);
            throw new ExpressionParseException(error);
        }


        logger.debug("OperationExpression Call Expression : {}", getClass().getSimpleName());

    }


    @Override
    public Integer findNextExpression(String[] tokens, int pos, Stack<Expression> stack, ContextsBinding contexts) throws ExpressionParseException {
        ConfigurationRegistry configurationRegistry = ConfigurationRegistry.INSTANCE;

        Stack<Expression> localStack = new Stack<>();
        for (; pos < tokens.length; pos++) {
            String token = tokens[pos];

            OperationExpression op = configurationRegistry.getOperation(token);
            if (op != null) {
                if (op instanceof RightPriority) {
                    this.leftOperand = new ArrayList<>(localStack);
                    return pos;
                } else if (op instanceof LeftPriority) {

                } else {
                    pos = op.parse(tokens, pos, localStack, contexts);
                }
            } else {

                TokenType tokenType = findDataObject(token, contexts);
                if (tokenType.type != null && contexts.getContext(tokenType.type) != null && TypeFinder.findType(tokenType.token, contexts.getContext(tokenType.type)) != null) {
                    TypeBinding typeBinding = contexts.getContext(tokenType.type);
                    DataType variableType = TypeFinder.findType(tokenType.token, typeBinding);
                    VariableExpression variableExpression = VariableType.getVariableType(tokenType.token, variableType, tokenType.type);
                    localStack.add(variableExpression);

                } else {

                    LiteralExpression literalExpression = LiteralType.getLiteralExpression(token);
                    localStack.add(literalExpression);
                }


            }
        }
        return pos;
    }


    @Override
    public Object interpret(InstancesBinding instancesBinding) throws InterpretException {


        List<Expression> expressions = this.leftOperand;
        DataType dataType = null;
        List values = new ArrayList<>();
        if (expressions.get(0) instanceof VariableExpression) {
            VariableExpression variableExpression = (VariableExpression) expressions.get(0);

            Object instance = instancesBinding.getInstance(variableExpression.getOperandType());
            List<Object> dataSet;
            if (instance instanceof Collection) {
                dataSet = (List) instance;
            } else {
                dataSet = new ArrayList<>();
                dataSet.add(instance);
            }


            dataType = toDataType(variableExpression.getType());
            for (Object dataRow : dataSet) {
                variableExpression = VariableType.setVariableValue(variableExpression, dataRow);
                if (variableExpression.getValue() != null) {
                    values.add(variableExpression.getValue());
                }
            }

        }


        List comb = null;
        if (expressions.get(1) instanceof ListLiteral) {
            ListLiteral listLiteral = (ListLiteral) expressions.get(1);

            if (listLiteral.getValue() != null) {
                comb = listLiteral.getValue();
            }
        } else if (expressions.get(1) instanceof OperationExpression) {
            OperationExpression operationExpression = (OperationExpression) expressions.get(1);

            LiteralExpression literalExpression = (LiteralExpression) operationExpression.interpret(instancesBinding);
            if (literalExpression instanceof ListLiteral) {
                ListLiteral listLiteral = (ListLiteral) literalExpression;
                if (listLiteral.getValue() != null) {
                    comb = listLiteral.getValue();
                }
            }
        }


        Boolean anyMatch = false;
        if (expressions.size() > 2) {

            if (expressions.get(2) instanceof BooleanLiteral) {
                anyMatch = ((BooleanLiteral) expressions.get(2)).getValue();
            }
        }


        if (comb != null && !comb.isEmpty() && !values.isEmpty()) {


            switch (dataType) {
                case StringType: {
                    List<String> valueStringList = (List<String>) values;
                    TreeSet<String> valuesSet = new TreeSet<>();
                    for (String value : valueStringList) {
                        valuesSet.add(TrimString.trim(value));
                    }


                    try {
                        List<String> combStringList = (List<String>) comb;


                        List<String> combSet = new ArrayList<>();
                        for (String value : combStringList) {
                            combSet.add(TrimString.trim(value));
                        }

                        if (anyMatch) {

                            for (String value : valuesSet) {
                                if (!combStringList.contains(value)) {
                                    return false;
                                }
                            }
                            return true;
                        } else {
                            return valuesSet.containsAll(combSet);
                        }


                    } catch (ClassCastException e) {
                        logger.error(e.getMessage(), e);
                    }

                }

                case IntegerType: {
                    List<Integer> valueStringList = (List<Integer>) values;
                    TreeSet<Integer> valuesSet = new TreeSet<>();
                    valuesSet.addAll(valueStringList);

                    try {
                        List<Integer> combList = (List<Integer>) comb;

                        if (anyMatch) {
                            for (Integer value : valuesSet) {
                                if (!combList.contains(value)) {
                                    return false;
                                }
                            }
                            return true;
                        } else {
                            return valueStringList.containsAll(combList);
                        }


                    } catch (ClassCastException e) {
                        List<Float> combList = (List<Float>) comb;

                        return valuesSet.containsAll(combList);
                    }

                }

                case FloatType: {
                    List<Float> valueStringList = (List<Float>) values;


                    TreeSet<Float> valuesSet = new TreeSet<>();
                    valuesSet.addAll(valueStringList);

                    try {
                        List<Float> combList = (List<Float>) comb;


                        if (anyMatch) {
                            for (Float value : valuesSet) {
                                if (!combList.contains(value)) {
                                    return false;
                                }
                            }
                            return true;
                        } else {
                            return valuesSet.containsAll(combList);
                        }


                    } catch (ClassCastException e) {
                        List<Integer> combList = (List<Integer>) comb;
                        return valuesSet.containsAll(combList);
                    }
                }

            }


        }
        return false;
    }
}