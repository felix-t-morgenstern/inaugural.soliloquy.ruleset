package inaugural.soliloquy.ruleset.entities.actonturnendandcharacterround.factories;

import inaugural.soliloquy.tools.Check;
import soliloquy.specs.common.entities.Action;
import soliloquy.specs.common.factories.Factory;
import soliloquy.specs.common.valueobjects.Pair;
import soliloquy.specs.gamestate.entities.Character;
import soliloquy.specs.ruleset.definitions.EffectsOnCharacterDefinition;
import soliloquy.specs.ruleset.definitions.EffectsOnCharacterDefinition.MagnitudeForStatisticDefinition;
import soliloquy.specs.ruleset.definitions.StatisticChangeMagnitudeDefinition;
import soliloquy.specs.ruleset.entities.CharacterVariableStatisticType;
import soliloquy.specs.ruleset.entities.actonturnendandcharacterround.EffectsCharacterOnRoundOrTurnChange.EffectsOnCharacter;
import soliloquy.specs.ruleset.entities.actonturnendandcharacterround.StatisticChangeMagnitude;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@SuppressWarnings("rawtypes")
public class EffectsOnCharacterFactory
        implements Factory<EffectsOnCharacterDefinition, EffectsOnCharacter> {
    private final Function<String, CharacterVariableStatisticType> GET_VARIABLE_STAT_TYPE;
    @SuppressWarnings("rawtypes") private final Function<String, Action> GET_ACTION;
    private final Factory<StatisticChangeMagnitudeDefinition, StatisticChangeMagnitude>
            MAGNITUDE_FACTORY;

    public EffectsOnCharacterFactory(
            Function<String, CharacterVariableStatisticType> getVariableStatType,
            @SuppressWarnings("rawtypes") Function<String, Action> getAction,
            Factory<StatisticChangeMagnitudeDefinition, StatisticChangeMagnitude> magnitudeFactory) {
        GET_VARIABLE_STAT_TYPE = Check.ifNull(getVariableStatType, "getVariableStatType");
        GET_ACTION = Check.ifNull(getAction, "getAction");
        MAGNITUDE_FACTORY = Check.ifNull(magnitudeFactory, "magnitudeFactory");
    }

    @Override
    public EffectsOnCharacter make(EffectsOnCharacterDefinition definition)
            throws IllegalArgumentException {
        Check.ifNull(definition, "definition");
        Check.ifNull(definition.magnitudeForStatisticDefinitions,
                "definition.magnitudeForStatisticDefinitions");
        for (MagnitudeForStatisticDefinition magnitudeDefinition :
                definition.magnitudeForStatisticDefinitions) {
            Check.ifNullOrEmpty(magnitudeDefinition.characterVariableStatisticTypeId,
                    "characterVariableStatisticTypeId within definition" +
                            ".magnitudeForStatisticDefinitions");
            Check.ifNull(magnitudeDefinition.magnitudeDefinition,
                    "magnitudeDefinition within definition.magnitudeForStatisticDefinitions");
        }
        Check.ifNullOrEmpty(definition.accompanyEffectFunctionId,
                "definition.accompanyEffectFunctionId");
        Check.ifNullOrEmpty(definition.otherEffectsFunctionId, "definition.otherEffectsFunctionId");

        //noinspection unchecked
        Action<Pair<Integer, Character>> accompanyEffectAction =
                (Action<Pair<Integer, Character>>) GET_ACTION.apply(
                        definition.accompanyEffectFunctionId);
        if (accompanyEffectAction == null) {
            throw new IllegalArgumentException(
                    "definition.accompanyEffectAction (" + definition.accompanyEffectFunctionId +
                            ") does not correspond to valid Action");
        }

        //noinspection unchecked
        Action<Pair<Integer, Character>> otherEffectsAction =
                (Action<Pair<Integer, Character>>) GET_ACTION.apply(
                        definition.otherEffectsFunctionId);
        if (otherEffectsAction == null) {
            throw new IllegalArgumentException(
                    "definition.otherEffectsAction (" + definition.otherEffectsFunctionId +
                            ") does not correspond to valid Action");
        }

        //noinspection rawtypes
        return new EffectsOnCharacter() {
            private final MagnitudeForStatisticDefinition[] MAGNITUDE_DEFINITIONS =
                    definition.magnitudeForStatisticDefinitions;
            private Map<CharacterVariableStatisticType, StatisticChangeMagnitude> magnitudes = null;

            @Override
            public Map<CharacterVariableStatisticType, StatisticChangeMagnitude> magnitudes() {
                if (magnitudes == null) {
                    magnitudes = new HashMap<>();
                    for (MagnitudeForStatisticDefinition definition : MAGNITUDE_DEFINITIONS) {
                        magnitudes.put(GET_VARIABLE_STAT_TYPE.apply(
                                        definition.characterVariableStatisticTypeId),
                                MAGNITUDE_FACTORY.make(definition.magnitudeDefinition));
                    }
                }
                return new HashMap<>(magnitudes);
            }

            @Override
            public void accompanyEffect(int magnitude, Character character)
                    throws IllegalArgumentException {
                Check.ifNull(character, "character");
                accompanyEffectAction.run(Pair.of(magnitude, character));
            }

            @Override
            public void otherEffects(int magnitude, Character character)
                    throws IllegalArgumentException {
                Check.ifNull(character, "character");
                otherEffectsAction.run(Pair.of(magnitude, character));
            }
        };
    }

    @Override
    public String getInterfaceName() {
        return Factory.class.getCanonicalName() + "<" +
                EffectsOnCharacterDefinition.class.getCanonicalName() + "," +
                EffectsOnCharacter.class.getCanonicalName() + ">";
    }
}
