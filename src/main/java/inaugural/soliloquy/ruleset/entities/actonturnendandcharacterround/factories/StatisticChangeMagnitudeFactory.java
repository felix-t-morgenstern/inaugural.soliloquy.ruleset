package inaugural.soliloquy.ruleset.entities.actonturnendandcharacterround.factories;

import inaugural.soliloquy.tools.Check;
import soliloquy.specs.common.factories.Factory;
import soliloquy.specs.common.valueobjects.Pair;
import soliloquy.specs.ruleset.definitions.StatisticChangeMagnitudeDefinition;
import soliloquy.specs.ruleset.entities.actonturnendandcharacterround.StatisticChangeMagnitude;
import soliloquy.specs.ruleset.entities.actonturnendandcharacterround.StatisticChangeMagnitude.AmountType;
import soliloquy.specs.ruleset.entities.actonturnendandcharacterround.StatisticChangeMagnitude.EffectType;

@SuppressWarnings("rawtypes")
public class StatisticChangeMagnitudeFactory
        implements Factory<StatisticChangeMagnitudeDefinition, StatisticChangeMagnitude> {
    @Override
    public StatisticChangeMagnitude make(StatisticChangeMagnitudeDefinition definition)
            throws IllegalArgumentException {
        Check.ifNull(definition, "definition");

        AmountType amountType = AmountType.fromValue(definition.amountType);
        EffectType effectType = EffectType.fromValue(definition.effectType);

        checkRangeArray(definition.perLevelValueRange, "definition.perLevelValueRange");
        checkRangeArray(definition.perLevelPercentRange, "definition.perLevelPercentRange");
        checkRangeArray(definition.absoluteValueRange, "definition.absoluteValueRange");
        checkRangeArray(definition.absolutePercentRange, "definition.absolutePercentRange");

        if (amountType == AmountType.VALUE) {
            return makeValueMagnitude(amountType, effectType, definition.perLevelValueRange,
                    definition.absoluteValueRange);
        }
        else {
            return makePercentMagnitude(amountType, effectType, definition.perLevelPercentRange,
                    definition.absolutePercentRange);
        }
    }

    private static void checkRangeArray(Object[] array, String paramName) {
        if (array != null && array.length != 2) {
            throw new IllegalArgumentException(
                    "StatisticChangeMagnitudeFactory.make: " + paramName +
                            " must have length of 2, but was provided with length of " +
                            array.length);
        }
    }

    private StatisticChangeMagnitude makeValueMagnitude(AmountType amountType,
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

    private StatisticChangeMagnitude makePercentMagnitude(AmountType amountType,
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
