package org.dvare.expression.operation.utility;

import org.dvare.annotations.Operation;
import org.dvare.binding.data.InstancesBinding;
import org.dvare.binding.expression.ExpressionBinding;
import org.dvare.binding.model.ContextsBinding;
import org.dvare.binding.model.TypeBinding;
import org.dvare.config.ConfigurationRegistry;
import org.dvare.exceptions.interpreter.InterpretException;
import org.dvare.exceptions.parser.ExpressionParseException;
import org.dvare.expression.Expression;
import org.dvare.expression.NamedExpression;
import org.dvare.expression.datatype.BooleanType;
import org.dvare.expression.datatype.DataType;
import org.dvare.expression.literal.LiteralType;
import org.dvare.expression.operation.OperationExpression;
import org.dvare.expression.operation.OperationType;
import org.dvare.expression.veriable.VariableExpression;
import org.dvare.expression.veriable.VariableType;
import org.dvare.util.TypeFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

@Operation(type = OperationType.FORALL)
public class ForAll extends OperationExpression {
    private static Logger logger = LoggerFactory.getLogger(ForAll.class);

    protected Expression referenceContext;
    protected Expression derivedContext;


    public ForAll() {
        super(OperationType.FORALL);
    }

    public ForAll(OperationType operationType) {
        super(operationType);
    }


    @Override
    public Integer parse(String[] tokens, int pos, Stack<Expression> stack, ExpressionBinding expressionBinding, ContextsBinding contextsBinding) throws ExpressionParseException {


        pos++;


        if (pos + 2 < tokens.length && tokens[pos + 2].equals("|")) {

            referenceContext = new NamedExpression(tokens[pos++]);
            derivedContext = new NamedExpression(tokens[pos++]);

        } else if (pos + 1 < tokens.length && tokens[pos + 1].equals("|")) {
            referenceContext = new NamedExpression("self");
            derivedContext = new NamedExpression(tokens[pos]);

        }
        pos++;


        TokenType tokenType = findDataObject(((NamedExpression) referenceContext).getName(), contextsBinding);

        if (tokenType.type != null && contextsBinding.getContext(tokenType.type) != null &&
                TypeFinder.findType(tokenType.token, contextsBinding.getContext(tokenType.type)) != null) {


            DataType variableType = TypeFinder.findType(tokenType.token, contextsBinding.getContext(tokenType.type));
            referenceContext = VariableType.getVariableExpression(tokenType.token, variableType, tokenType.type);


            String parts[] = ((NamedExpression) derivedContext).getName().split(":");
            if (parts.length == 2) {

                String name = parts[0].trim();
                String type = parts[1].trim();


                DataType dataType = DataType.valueOf(type);


                TypeBinding typeBinding = new TypeBinding();                             // new context
                typeBinding.addTypes(name, dataType);
                contextsBinding.addContext(((VariableExpression) referenceContext).getName(), typeBinding);


                derivedContext = VariableType.getVariableExpression(name, dataType, ((VariableExpression) referenceContext).getName());
            }


        } else {

            TypeBinding typeBinding = contextsBinding.getContext(((NamedExpression) referenceContext).getName());
            if (contextsBinding.getContext(((NamedExpression) referenceContext).getName()) != null) {
                contextsBinding.addContext(((NamedExpression) derivedContext).getName(), typeBinding);
                this.leftOperand = new NamedExpression(((NamedExpression) referenceContext).getName());
            }
        }


        pos = findNextExpression(tokens, pos, stack, expressionBinding, contextsBinding);

        this.leftOperand = stack.pop();

        if (derivedContext instanceof NamedExpression) {
            contextsBinding.removeContext(((NamedExpression) derivedContext).getName());
        } else if (referenceContext instanceof VariableExpression) {
            contextsBinding.removeContext(((VariableExpression) referenceContext).getName());
        }


        if (logger.isDebugEnabled()) {
            logger.debug("OperationExpression Call Expression : {}", getClass().getSimpleName());

        }
        stack.push(this);


        return pos;
    }


    @Override
    public Integer findNextExpression(String[] tokens, int pos, Stack<Expression> stack, ExpressionBinding
            expressionBinding, ContextsBinding contexts) throws ExpressionParseException {

        ConfigurationRegistry configurationRegistry = ConfigurationRegistry.INSTANCE;


        for (; pos < tokens.length; pos++) {
            String token = tokens[pos];

            OperationExpression op = configurationRegistry.getOperation(token);
            if (op != null) {
                if (op.getClass().equals(EndForAll.class) || op.getClass().equals(EndForEach.class)) {
                    return pos;
                } else if (!op.getClass().equals(LeftPriority.class)) {
                    pos = op.parse(tokens, pos, stack, expressionBinding, contexts);
                }
            } else {


                stack.add(buildExpression(token, contexts));

            }
        }

        throw new ExpressionParseException("Function Closing Bracket Not Found");
    }


    @Override
    public Object interpret(ExpressionBinding expressionBinding,
                            InstancesBinding instancesBinding) throws InterpretException {


        if (referenceContext instanceof NamedExpression && derivedContext instanceof NamedExpression) {
            Object object = instancesBinding.getInstance(((NamedExpression) referenceContext).getName());
            if (object instanceof List) {
                List instances = (List) object;

                List<Boolean> results = new ArrayList<>();

                for (Object instance : instances) {
                    instancesBinding.addInstance(((NamedExpression) derivedContext).getName(), instance);

                    Object result = leftOperand.interpret(expressionBinding, instancesBinding);
                    results.add(toBoolean(result));

                }

                instancesBinding.removeInstance(((NamedExpression) derivedContext).getName());
                Boolean result = results.stream().allMatch(Boolean::booleanValue);
                return LiteralType.getLiteralExpression(result, BooleanType.class);

            }

        } else if (referenceContext instanceof VariableExpression && derivedContext instanceof VariableExpression) {

        }
        return LiteralType.getLiteralExpression(false, BooleanType.class);
    }


    @Override
    public String toString() {

        StringBuilder toStringBuilder = new StringBuilder();

        toStringBuilder.append(this.getSymbols().get(0));


        toStringBuilder.append(" ");
        toStringBuilder.append(referenceContext.toString());

        toStringBuilder.append(" ");
        toStringBuilder.append(derivedContext.toString());


        toStringBuilder.append(" ");
        toStringBuilder.append("|");
        toStringBuilder.append(" ");

        if (leftOperand != null) {
            toStringBuilder.append(leftOperand.toString());
            toStringBuilder.append(" ");
        }

        return toStringBuilder.toString();


    }
}