package inaugural.soliloquy.ruleset.gameconcepts;

import inaugural.soliloquy.ruleset.definitions.concepts.StatisticCalculationDefinition;
import inaugural.soliloquy.ruleset.definitions.concepts.StatisticCalculationDefinition.StatisticCalculationStatisticDefinition;
import soliloquy.specs.common.valueobjects.Pair;
import soliloquy.specs.gamestate.entities.Character;
import soliloquy.specs.ruleset.entities.abilities.PassiveAbility;
import soliloquy.specs.ruleset.entities.character.StaticStatisticType;
import soliloquy.specs.ruleset.entities.character.StatisticType;
import soliloquy.specs.ruleset.entities.character.StatusEffectType;
import soliloquy.specs.ruleset.entities.character.VariableStatisticType;
import soliloquy.specs.ruleset.gameconcepts.StatisticCalculation;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static inaugural.soliloquy.tools.collections.Collections.mapOf;

public class StatisticCalculationImpl implements StatisticCalculation {
    private final Map<StatisticType, StatisticCalculationStatisticDefinition> DEFINITIONS;
    private final Function<String, StaticStatisticType> GET_STATIC_STAT_TYPE;
    private final Function<String, PassiveAbility> GET_PASSIVE_ABILITY;
    private final Function<String, StatusEffectType> GET_STATUS_EFFECT_TYPE;

    public StatisticCalculationImpl(StatisticCalculationDefinition definition,
                                    Function<String, StaticStatisticType> getStaticStatType,
                                    List<StaticStatisticType> staticStatTypes,
                                    List<VariableStatisticType> variableStatTypes,
                                    Function<String, PassiveAbility> getPassiveAbility,
                                    Function<String, StatusEffectType> getStatusEffectType) {
        checkDefinitionValidity(definition);
        DEFINITIONS = getDefinitions(definition, staticStatTypes, variableStatTypes);
        GET_STATIC_STAT_TYPE = getStaticStatType;
        GET_PASSIVE_ABILITY = getPassiveAbility;
        GET_STATUS_EFFECT_TYPE = getStatusEffectType;
    }

    private void checkDefinitionValidity(StatisticCalculationDefinition definition) {

    }

    private Map<StatisticType, StatisticCalculationStatisticDefinition> getDefinitions(
            StatisticCalculationDefinition definition,
            List<StaticStatisticType> staticStatTypes,
            List<VariableStatisticType> variableStatTypes) {
        return mapOf();
    }

    @Override
    public String getInterfaceName() {
        return null;
    }

    @Override
    public int calculate(Character character, StatisticType statisticType)
            throws IllegalArgumentException, IllegalStateException {
        return calculateWithDescriptors(character, statisticType).getItem1();
    }

    @Override
    public Pair<Integer, Map<String, Float>> calculateWithDescriptors(Character character,
                                                                      StatisticType statisticType)
            throws IllegalArgumentException, IllegalStateException {
        return null;
    }
}
