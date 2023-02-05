package inaugural.soliloquy.ruleset;

import inaugural.soliloquy.tools.Check;
import soliloquy.specs.common.entities.Action;

import java.util.function.Function;

public class GetFunctions {
    @SuppressWarnings("rawtypes")
    public static <TInput> Action<TInput> getNullableAction(Function<String, Action> getAction,
                                                            String actionId,
                                                            String paramName) {
        if (actionId != null && !"".equals(actionId)) {
            //noinspection unchecked
            var action = (Action<TInput>) getAction.apply(actionId);
            if (action == null) {
                throw new IllegalArgumentException(
                        paramName + " (" + actionId + ") does not correspond to valid Action");
            }
            return action;
        }
        else {
            return null;
        }
    }

    @SuppressWarnings("rawtypes")
    public static <TInput> Action<TInput> getNonNullableAction(Function<String, Action> getAction,
                                                               String actionId,
                                                               String paramName) {
        Check.ifNullOrEmpty(actionId, paramName);
        //noinspection unchecked
        var action = (Action<TInput>) getAction.apply(actionId);
        if (action == null) {
            throw new IllegalArgumentException(
                    paramName + " (" + actionId + ") does not correspond to valid Action");
        }
        return action;
    }

    @SuppressWarnings("rawtypes")
    public static <TInput, TOutput> Function<TInput, TOutput> getNonNullableFunction(
            Function<String, Function> getFunction,
            String functionId,
            String paramName) {
        Check.ifNullOrEmpty(functionId, paramName);
        //noinspection unchecked
        var function = (Function<TInput, TOutput>) getFunction.apply(functionId);
        if (function == null) {
            throw new IllegalArgumentException(
                    paramName + " (" + functionId + ") does not correspond to valid Function");
        }
        return function;
    }
}
