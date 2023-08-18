package inaugural.soliloquy.ruleset.gameconcepts;

import inaugural.soliloquy.ruleset.definitions.concepts.CalculationByComponentsDefinition;
import inaugural.soliloquy.ruleset.definitions.concepts.CalculationByComponentsDefinition.TypeDefinition;
import inaugural.soliloquy.ruleset.definitions.concepts.CalculationByComponentsDefinition.TypeComponentDefinition;
import inaugural.soliloquy.tools.Check;
import soliloquy.specs.common.valueobjects.Pair;
import soliloquy.specs.gamestate.entities.Character;
import soliloquy.specs.ruleset.entities.abilities.PassiveAbility;
import soliloquy.specs.ruleset.entities.character.StaticStatisticType;
import soliloquy.specs.ruleset.entities.character.StatisticType;
import soliloquy.specs.ruleset.entities.character.StatusEffectType;
import soliloquy.specs.ruleset.entities.character.VariableStatisticType;
import soliloquy.specs.ruleset.gameconcepts.StatisticCalculation;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Map;
import java.util.function.Function;

import static inaugural.soliloquy.tools.collections.Collections.mapOf;
import static inaugural.soliloquy.tools.valueobjects.Pair.pairOf;

public class StatisticCalculationImpl implements StatisticCalculation {
    private final MathContext MATH_CONTEXT;
    private final Map<StatisticType, TypeDefinition> DEFINITIONS;
    private final Function<String, StaticStatisticType> GET_STATIC_STAT_TYPE;
    private final Function<String, PassiveAbility> GET_PASSIVE_ABILITY;
    private final Function<String, StatusEffectType> GET_STATUS_EFFECT_TYPE;

    public StatisticCalculationImpl(CalculationByComponentsDefinition definition,
                                    Map<String, StaticStatisticType> staticStatTypes,
                                    Map<String, VariableStatisticType> variableStatTypes,
                                    Function<String, PassiveAbility> getPassiveAbility,
                                    Function<String, StatusEffectType> getStatusEffectType) {
        Check.ifNull(variableStatTypes, "variableStatTypes");
        Check.ifNull(definition, "definition");
        Check.ifNull(definition.roundingMode, "definition.roundingMode");
        MATH_CONTEXT =
                new MathContext(definition.decimalPlacesForModifiers, definition.roundingMode);
        GET_STATIC_STAT_TYPE = Check.ifNull(staticStatTypes, "staticStatTypes")::get;
        GET_PASSIVE_ABILITY = Check.ifNull(getPassiveAbility, "getPassiveAbility");
        GET_STATUS_EFFECT_TYPE = Check.ifNull(getStatusEffectType, "getStatusEffectType");
        checkDefinitionValidity(definition);
        DEFINITIONS = getDefinitions(definition, staticStatTypes, variableStatTypes);
    }

    private void checkDefinitionValidity(CalculationByComponentsDefinition definition) {
        Check.ifNull(definition.typeDefinitions, "definition.typeDefinitions");
        for (var typeDefinition : definition.typeDefinitions) {
            checkTypeDefinitionValidity(typeDefinition);
        }
        Check.throwOnLtValue(definition.decimalPlacesForModifiers, 0,
                "definition.decimalPlacesForModifiers");
    }

    private void checkTypeDefinitionValidity(TypeDefinition typeDefinition) {
        Check.ifNull(typeDefinition, "typeDefinition");
        Check.ifNullOrEmpty(typeDefinition.typeId, "typeDefinition.typeId");
        Check.ifNull(typeDefinition.staticStatisticComponents,
                "typeDefinition.staticStatisticComponents");
        for (var typeComponentDefinition : typeDefinition.staticStatisticComponents) {
            checkTypeComponentDefinitionValidity(typeComponentDefinition, GET_STATIC_STAT_TYPE,
                    "StaticStatisticType");
        }
        Check.ifNull(typeDefinition.passiveAbilityComponents,
                "typeDefinition.passiveAbilityComponents");
        for (var typeComponentDefinition : typeDefinition.passiveAbilityComponents) {
            checkTypeComponentDefinitionValidity(typeComponentDefinition, GET_PASSIVE_ABILITY,
                    "PassiveAbility");
        }
        Check.ifNull(typeDefinition.itemDataComponents, "typeDefinition.itemDataComponents");
        for (var typeComponentDefinition : typeDefinition.itemDataComponents) {
            checkTypeComponentDefinitionValidity(typeComponentDefinition, s -> true, "");
        }
        Check.ifNull(typeDefinition.statusEffectComponents,
                "typeDefinition.statusEffectComponents");
        for (var typeComponentDefinition : typeDefinition.statusEffectComponents) {
            checkTypeComponentDefinitionValidity(typeComponentDefinition, GET_STATUS_EFFECT_TYPE,
                    "StatusEffectType");
        }
    }

    private <V> void checkTypeComponentDefinitionValidity(
            TypeComponentDefinition typeComponentDefinition,
            Function<String, V> getEntity,
            String typeName) {
        Check.ifNull(typeComponentDefinition, "typeComponentDefinition");
        Check.ifNullOrEmpty(typeComponentDefinition.typeId, "typeComponentDefinition.typeId");
        var entity = getEntity.apply(typeComponentDefinition.typeId);
        if (entity == null) {
            throw new IllegalArgumentException(
                    "StatisticCalculationImpl.checkTypeComponentDefinitionValidity: " +
                            "typeComponentDefinition.typeId (" +
                            typeComponentDefinition.typeId + ") does not correspond to a valid " +
                            typeName);
        }
    }

    private Map<StatisticType, TypeDefinition> getDefinitions(
            CalculationByComponentsDefinition definition,
            Map<String, StaticStatisticType> staticStatTypes,
            Map<String, VariableStatisticType> variableStatTypes) {
        Map<StatisticType, TypeDefinition> definitions = mapOf();
        var expectedDefinitionsCount = staticStatTypes.size() + variableStatTypes.size();

        for (var typeDefinition : definition.typeDefinitions) {
            StatisticType statisticType;
            if (typeDefinition.variableStat) {
                statisticType = variableStatTypes.get(typeDefinition.typeId);
                if (statisticType == null) {
                    throw new IllegalArgumentException(
                            "StatisticCalculationImpl.getDefinitions: typeDefinition.typeId (" +
                                    typeDefinition.typeId +
                                    ") does not correspond to a valid variable stat type");
                }
            }
            else {
                statisticType = staticStatTypes.get(typeDefinition.typeId);
                if (statisticType == null) {
                    throw new IllegalArgumentException(
                            "StatisticCalculationImpl.getDefinitions: typeDefinition.typeId (" +
                                    typeDefinition.typeId +
                                    ") does not correspond to a valid static stat type");
                }
            }

            definitions.put(statisticType, typeDefinition);
        }

        if (definitions.size() != expectedDefinitionsCount) {
            throw new IllegalArgumentException(
                    "StatisticCalculationImpl.getDefinitions: number of definitions provided (" +
                            definitions.size() +
                            ") does not match number of definitions expected (" +
                            expectedDefinitionsCount + ")");
        }

        return definitions;
    }

    @Override
    public String getInterfaceName() {
        return StatisticCalculation.class.getCanonicalName();
    }

    @Override
    public int calculate(Character character, StatisticType statisticType)
            throws IllegalArgumentException, IllegalStateException {
        return calculateWithDescriptors(character, statisticType).item1();
    }

    @Override
    public Pair<Integer, Map<Object, BigDecimal>> calculateWithDescriptors(
            Character character,
            StatisticType statisticType)
            throws IllegalArgumentException, IllegalStateException {
        Check.ifNull(character, "character");
        Check.ifDeleted(character, "character");
        Check.ifNull(statisticType, "statisticType");

        var typeDefinition = DEFINITIONS.get(statisticType);

        Map<Object, BigDecimal> modifiers = mapOf();

        for (var staticStatComponent : typeDefinition.staticStatisticComponents) {
            var staticStatType = GET_STATIC_STAT_TYPE.apply(staticStatComponent.typeId);
            var staticStatValue = this.calculate(character, staticStatType);
            var staticStatModifier = new BigDecimal(
                    staticStatValue * staticStatComponent.multiplicationFactor)
                    .round(MATH_CONTEXT);
            modifiers.put(staticStatType, staticStatModifier);
        }
        for (var passiveAbilityComponent : typeDefinition.passiveAbilityComponents) {
            var passiveAbility = GET_PASSIVE_ABILITY.apply(passiveAbilityComponent.typeId);
            var hasPassiveAbility = character.passiveAbilities().contains(passiveAbility);
            if (hasPassiveAbility) {
                var passiveAbilityModifier =
                        new BigDecimal(passiveAbilityComponent.multiplicationFactor)
                                .round(MATH_CONTEXT);
                modifiers.put(passiveAbility, passiveAbilityModifier);
            }
        }
        for (var itemDataComponent : typeDefinition.itemDataComponents) {
            character.equipmentSlots().representation().values().forEach(item -> {
                Integer dataValue = item.data().getVariable(itemDataComponent.typeId);
                if (dataValue != null) {
                    var itemDataModifier =
                            new BigDecimal(dataValue * itemDataComponent.multiplicationFactor)
                                    .round(MATH_CONTEXT);
                    modifiers.put(item, itemDataModifier);
                }
            });
        }
        for (var statusEffectComponent : typeDefinition.statusEffectComponents) {
            var statusEffect = GET_STATUS_EFFECT_TYPE.apply(statusEffectComponent.typeId);
            var statusEffectLevel = character.statusEffects().getStatusEffectLevel(statusEffect);
            var statusEffectModifier =
                    new BigDecimal(statusEffectLevel * statusEffectComponent.multiplicationFactor)
                            .round(MATH_CONTEXT);
            modifiers.put(statusEffect, statusEffectModifier);
        }

        var modifiersTotal = modifiers.values().stream().reduce(new BigDecimal(0), BigDecimal::add)
                .round(MATH_CONTEXT)
                .intValue();

        return pairOf(typeDefinition.baseAmount + modifiersTotal, modifiers);
    }
}
