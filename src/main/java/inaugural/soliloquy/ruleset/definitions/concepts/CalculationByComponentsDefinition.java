package inaugural.soliloquy.ruleset.definitions.concepts;

import java.math.RoundingMode;

public class CalculationByComponentsDefinition {
    public RoundingMode roundingMode;
    public int decimalPlacesForModifiers;

    public TypeDefinition[] typeDefinitions;

    public CalculationByComponentsDefinition(
            TypeDefinition[] typeDefinitions,
            RoundingMode roundingMode,
            int decimalPlacesForModifiers) {
        this.typeDefinitions = typeDefinitions;
        this.roundingMode = roundingMode;
        this.decimalPlacesForModifiers = decimalPlacesForModifiers;
    }

    public static class TypeDefinition {
        public String typeId;
        public boolean variableStat;
        public int baseAmount;
        public TypeComponentDefinition[] staticStatisticComponents;
        public TypeComponentDefinition[] passiveAbilityComponents;
        public TypeComponentDefinition[] itemDataComponents;
        public TypeComponentDefinition[] statusEffectComponents;

        public TypeDefinition(
                String typeId,
                boolean variableStat,
                int baseAmount,
                TypeComponentDefinition[] staticStatisticComponents,
                TypeComponentDefinition[] passiveAbilityComponents,
                TypeComponentDefinition[] itemDataComponents,
                TypeComponentDefinition[] statusEffectComponents) {
            this.typeId = typeId;
            this.variableStat = variableStat;
            this.baseAmount = baseAmount;
            this.staticStatisticComponents = staticStatisticComponents;
            this.passiveAbilityComponents = passiveAbilityComponents;
            this.itemDataComponents = itemDataComponents;
            this.statusEffectComponents = statusEffectComponents;
        }
    }

    public static class TypeComponentDefinition {
        public String typeId;
        public float multiplicationFactor;

        public TypeComponentDefinition(String typeId,
                                       float multiplicationFactor) {
            this.typeId = typeId;
            this.multiplicationFactor = multiplicationFactor;
        }
    }
}
