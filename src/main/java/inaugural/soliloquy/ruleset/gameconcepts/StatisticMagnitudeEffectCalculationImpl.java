package inaugural.soliloquy.ruleset.gameconcepts;

import inaugural.soliloquy.tools.Check;
import soliloquy.specs.gamestate.entities.Character;
import soliloquy.specs.gamestate.entities.exceptions.EntityDeletedException;
import soliloquy.specs.ruleset.entities.actonroundendandcharacterturn.StatisticChangeMagnitude;
import soliloquy.specs.ruleset.entities.actonroundendandcharacterturn.StatisticChangeMagnitude.EffectType;
import soliloquy.specs.ruleset.entities.character.StatisticType;
import soliloquy.specs.ruleset.entities.character.StatusEffectType;
import soliloquy.specs.ruleset.entities.character.VariableStatisticType;
import soliloquy.specs.ruleset.gameconcepts.DamageResistanceCalculation;
import soliloquy.specs.ruleset.gameconcepts.StatisticCalculation;
import soliloquy.specs.ruleset.gameconcepts.StatisticMagnitudeEffectCalculation;

import java.util.function.Supplier;

public class StatisticMagnitudeEffectCalculationImpl
        implements StatisticMagnitudeEffectCalculation {
    private final DamageResistanceCalculation DAMAGE_RESISTANCE_CALCULATION;
    private final StatisticCalculation STATISTIC_CALCULATION;
    private final Supplier<Double> RANDOM_VALUE_SUPPLIER;

    public StatisticMagnitudeEffectCalculationImpl(
            DamageResistanceCalculation damageResistanceCalculation,
            StatisticCalculation statisticCalculation,
            Supplier<Double> randomDoubleSupplier) {
        DAMAGE_RESISTANCE_CALCULATION =
                Check.ifNull(damageResistanceCalculation, "damageResistanceCalculation");
        STATISTIC_CALCULATION = Check.ifNull(statisticCalculation, "statisticCalculation");
        RANDOM_VALUE_SUPPLIER = Check.ifNull(randomDoubleSupplier, "randomDoubleSupplier");
    }

    @Override
    public <T extends Number> int getEffect(StatisticType statisticType,
                                            StatisticChangeMagnitude<T> statisticChangeMagnitude,
                                            Character character)
            throws IllegalArgumentException, EntityDeletedException {
        Check.ifNull(statisticType, "statisticType");
        Check.ifNull(statisticChangeMagnitude, "statisticChangeMagnitude");
        Check.ifNull(character, "character");

        var staticStatLevel = STATISTIC_CALCULATION.calculate(character, statisticType);

        return getEffectWithAmountType(staticStatLevel, statisticChangeMagnitude, character);
    }

    @Override
    public <T extends Number> int getEffect(StatusEffectType statusEffectType,
                                            StatisticChangeMagnitude<T> statisticChangeMagnitude,
                                            Character character)
            throws IllegalArgumentException, EntityDeletedException {
        Check.ifNull(statusEffectType, "statusEffectType");
        Check.ifNull(statisticChangeMagnitude, "statisticChangeMagnitude");
        Check.ifNull(character, "character");

        var statusEffectLevel = character.statusEffects().getStatusEffectLevel(statusEffectType);

        return getEffectWithAmountType(statusEffectLevel, statisticChangeMagnitude, character);
    }

    private <T extends Number> int getEffectWithAmountType(int level,
                                                           StatisticChangeMagnitude<T> statisticChangeMagnitude,
                                                           Character character) {
        var baseEffect = getBaseEffect(level, statisticChangeMagnitude);

        var effect = switch (statisticChangeMagnitude.amountType()) {
            case VALUE -> (int) Math.round(baseEffect);
            case PERCENT_OF_CURRENT -> getPercentOfCurrent(baseEffect,
                    statisticChangeMagnitude.effectedStatisticType(), character);
            case PERCENT_OF_MAXIMUM -> getPercentOfMaximum(baseEffect,
                    statisticChangeMagnitude.effectedStatisticType(), character);
        };

        if (statisticChangeMagnitude.effectType() == EffectType.DAMAGE) {
            effect = DAMAGE_RESISTANCE_CALCULATION.calculateEffectiveChange(character, effect,
                    statisticChangeMagnitude.element());
        }

        return effect;
    }

    private <T extends Number> double getBaseEffect(int entityTypeLevel,
                                                    StatisticChangeMagnitude<T> magnitude) {
        var baseEffect = 0d;

        if (magnitude.absoluteRange() != null) {
            baseEffect += RANDOM_VALUE_SUPPLIER.get() *
                    (magnitude.absoluteRange().item2().doubleValue() -
                            magnitude.absoluteRange().item1().doubleValue());
        }
        if (magnitude.perLevelRange() != null) {
            for (var i = 0; i < entityTypeLevel; i++) {
                baseEffect += RANDOM_VALUE_SUPPLIER.get() *
                        (magnitude.perLevelRange().item2().doubleValue() -
                                magnitude.perLevelRange().item1().doubleValue());
            }
        }

        return baseEffect;
    }

    private int getPercentOfCurrent(double percent, VariableStatisticType effectedVariableStat,
                                    Character character) {
        var current = character.getVariableStatisticCurrentValue(effectedVariableStat);

        return (int) Math.round(current * percent);
    }

    private int getPercentOfMaximum(double percent, VariableStatisticType effectedVariableStat,
                                    Character character) {
        var maximum = STATISTIC_CALCULATION.calculate(character, effectedVariableStat);

        return (int) Math.round(maximum * percent);
    }
}
