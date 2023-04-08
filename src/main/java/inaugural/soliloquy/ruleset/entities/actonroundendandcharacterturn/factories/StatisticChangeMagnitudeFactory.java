package inaugural.soliloquy.ruleset.entities.actonroundendandcharacterturn.factories;

import inaugural.soliloquy.tools.Check;
import soliloquy.specs.common.factories.Factory;
import soliloquy.specs.common.valueobjects.Pair;
import inaugural.soliloquy.ruleset.definitions.StatisticChangeMagnitudeDefinition;
import soliloquy.specs.ruleset.entities.Element;
import soliloquy.specs.ruleset.entities.actonroundendandcharacterturn.StatisticChangeMagnitude;
import soliloquy.specs.ruleset.entities.actonroundendandcharacterturn.StatisticChangeMagnitude.AmountType;
import soliloquy.specs.ruleset.entities.actonroundendandcharacterturn.StatisticChangeMagnitude.EffectType;
import soliloquy.specs.ruleset.entities.character.VariableStatisticType;

import java.util.function.Function;

@SuppressWarnings("rawtypes")
public class StatisticChangeMagnitudeFactory
        implements Factory<StatisticChangeMagnitudeDefinition, StatisticChangeMagnitude> {
    private final Function<String, VariableStatisticType> GET_VARIABLE_STAT_TYPE;
    private final Function<String, Element> GET_ELEMENT;

    public StatisticChangeMagnitudeFactory(
            Function<String, VariableStatisticType> getVariableStatType,
            Function<String, Element> getElement) {
        GET_VARIABLE_STAT_TYPE = Check.ifNull(getVariableStatType, "getVariableStatType");
        GET_ELEMENT = Check.ifNull(getElement, "getElement");
    }

    @Override
    public StatisticChangeMagnitude make(StatisticChangeMagnitudeDefinition definition)
            throws IllegalArgumentException {
        Check.ifNull(definition, "definition");
        Check.ifNull(definition.amountType, "definition.amountType");
        Check.ifNull(definition.effectType, "definition.effectType");

        var amountType = AmountType.valueOf(definition.amountType);
        var effectType = EffectType.valueOf(definition.effectType);

        checkRangeArray(definition.perLevelValueRange, "definition.perLevelValueRange");
        checkRangeArray(definition.perLevelPercentRange, "definition.perLevelPercentRange");
        checkRangeArray(definition.absoluteValueRange, "definition.absoluteValueRange");
        checkRangeArray(definition.absolutePercentRange, "definition.absolutePercentRange");

        Element element = null;
        if (definition.elementId != null && !"".equals(definition.elementId)) {
            element = GET_ELEMENT.apply(definition.elementId);
        }
        if (element == null) {
            throw new IllegalArgumentException(
                    "StatisticChangeMagnitudeFactory.make: elementId (" + definition.elementId +
                            ") does not correspond to a valid Element");
        }

        var variableStatType = GET_VARIABLE_STAT_TYPE.apply(definition.variableStatType);

        if (amountType == AmountType.VALUE) {
            return makeValueMagnitude(variableStatType, element, amountType,
                    effectType, definition.perLevelValueRange, definition.absoluteValueRange);
        }
        else {
            return makePercentMagnitude(variableStatType, element, amountType,
                    effectType, definition.perLevelPercentRange, definition.absolutePercentRange);
        }
    }

    private static <T extends Number> void checkRangeArray(T[] array, String paramName) {
        if (array == null) {
            return;
        }
        if (array.length != 2) {
            throw new IllegalArgumentException(
                    "StatisticChangeMagnitudeFactory.make: " + paramName +
                            " must have length of 2, but was provided with length of " +
                            array.length);
        }
        Check.ifNull(array[0], "first item in " + paramName);
        Check.ifNull(array[1], "second item in " + paramName);
        if (array[0].floatValue() > array[1].floatValue()) {
            throw new IllegalArgumentException(
                    "StatisticChangeMagnitudeFactory.make: " + paramName +
                            " cannot have a first item (" + array[0] +
                            ") with a higher value than the second (" + array[1] + ")");
        }
    }

    private static StatisticChangeMagnitude makeValueMagnitude(
            VariableStatisticType variableStatType,
            Element element,
            AmountType amountType,
            EffectType effectType,
            Integer[] perLevelValueRange,
            Integer[] absoluteValueRange) {
        Pair<Integer, Integer> perLevelRange;
        if (perLevelValueRange != null) {
            perLevelRange = Pair.of(perLevelValueRange[0], perLevelValueRange[1]);
        }
        else {
            perLevelRange = null;
        }

        Pair<Integer, Integer> absoluteRange;
        if (absoluteValueRange != null) {
            absoluteRange = Pair.of(absoluteValueRange[0], absoluteValueRange[1]);
        }
        else {
            absoluteRange = null;
        }

        return new StatisticChangeMagnitude<Integer>() {
            @Override
            public String getInterfaceName() {
                return StatisticChangeMagnitude.class.getCanonicalName() + "<" +
                        Integer.class.getCanonicalName() + ">";
            }

            @Override
            public VariableStatisticType effectedStatisticType() {
                return variableStatType;
            }

            @Override
            public Element element() {
                return element;
            }

            @Override
            public EffectType effectType() {
                return effectType;
            }

            @Override
            public AmountType amountType() {
                return amountType;
            }

            @Override
            public Pair<Integer, Integer> perLevelRange() {
                return perLevelRange;
            }

            @Override
            public Pair<Integer, Integer> absoluteRange() {
                return absoluteRange;
            }
        };
    }

    private static StatisticChangeMagnitude makePercentMagnitude(
            VariableStatisticType variableStatType,
            Element element,
            AmountType amountType,
            EffectType effectType,
            Float[] perLevelPercentRange,
            Float[] absolutePercentRange) {
        Pair<Float, Float> perLevelRange;
        if (perLevelPercentRange != null) {
            perLevelRange = Pair.of(perLevelPercentRange[0], perLevelPercentRange[1]);
        }
        else {
            perLevelRange = null;
        }

        Pair<Float, Float> absoluteRange;
        if (absolutePercentRange != null) {
            absoluteRange = Pair.of(absolutePercentRange[0], absolutePercentRange[1]);
        }
        else {
            absoluteRange = null;
        }

        return new StatisticChangeMagnitude<Float>() {
            @Override
            public String getInterfaceName() {
                return StatisticChangeMagnitude.class.getCanonicalName() + "<" +
                        Float.class.getCanonicalName() + ">";
            }

            @Override
            public VariableStatisticType effectedStatisticType() {
                return variableStatType;
            }

            @Override
            public Element element() {
                return element;
            }

            @Override
            public EffectType effectType() {
                return effectType;
            }

            @Override
            public AmountType amountType() {
                return amountType;
            }

            @Override
            public Pair<Float, Float> perLevelRange() {
                return perLevelRange;
            }

            @Override
            public Pair<Float, Float> absoluteRange() {
                return absoluteRange;
            }
        };
    }

    @Override
    public String getInterfaceName() {
        return Factory.class.getCanonicalName() + "<" +
                StatisticChangeMagnitudeDefinition.class.getCanonicalName() + "," +
                StatisticChangeMagnitude.class.getCanonicalName() + ">";
    }
}
