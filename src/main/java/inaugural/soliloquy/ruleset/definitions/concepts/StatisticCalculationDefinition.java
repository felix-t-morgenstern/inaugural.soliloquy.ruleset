package inaugural.soliloquy.ruleset.definitions.concepts;

import java.math.RoundingMode;

public class StatisticCalculationDefinition {
    public RoundingMode roundingMode;
    public int decimalPlacesToDisplayForModifiers;

    public StatisticCalculationStatisticDefinition[] statisticCalculationDefinitions;

    public StatisticCalculationDefinition(
            StatisticCalculationStatisticDefinition[] statisticCalculationDefinitions,
            RoundingMode roundingMode,
            int decimalPlacesToDisplayForModifiers) {
        this.statisticCalculationDefinitions = statisticCalculationDefinitions;
        this.roundingMode = roundingMode;
        this.decimalPlacesToDisplayForModifiers = decimalPlacesToDisplayForModifiers;
    }

    public static class StatisticCalculationStatisticDefinition {
        public String statisticTypeId;
        public String baseAmount;
        public StatisticCalculationStatisticComponentDefinition[] staticStatisticComponents;
        public StatisticCalculationStatisticComponentDefinition[] passiveAbilityComponents;
        public StatisticCalculationStatisticComponentDefinition[] itemDataComponents;
        public StatisticCalculationStatisticComponentDefinition[] statusEffectComponents;

        public StatisticCalculationStatisticDefinition(
                String statisticTypeId,
                String baseAmount,
                StatisticCalculationStatisticComponentDefinition[] statisticComponents,
                StatisticCalculationStatisticComponentDefinition[] passiveAbilityComponents,
                StatisticCalculationStatisticComponentDefinition[] itemDataComponents,
                StatisticCalculationStatisticComponentDefinition[] statusEffectComponents) {
            this.statisticTypeId = statisticTypeId;
            this.baseAmount = baseAmount;
            this.staticStatisticComponents = statisticComponents;
            this.passiveAbilityComponents = passiveAbilityComponents;
            this.itemDataComponents = itemDataComponents;
            this.statusEffectComponents = statusEffectComponents;
        }
    }

    public static class StatisticCalculationStatisticComponentDefinition {
        public String typeId;
        public float multiplicationFactor;

        public StatisticCalculationStatisticComponentDefinition(String typeId,
                                                                float multiplicationFactor) {
            this.typeId = typeId;
            this.multiplicationFactor = multiplicationFactor;
        }
    }
}
