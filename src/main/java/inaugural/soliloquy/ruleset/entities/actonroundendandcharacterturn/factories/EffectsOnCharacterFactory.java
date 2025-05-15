package inaugural.soliloquy.ruleset.entities.actonroundendandcharacterturn.factories;

import inaugural.soliloquy.tools.Check;
import soliloquy.specs.common.entities.Action;
import soliloquy.specs.gamestate.entities.Character;
import inaugural.soliloquy.ruleset.definitions.EffectsOnCharacterDefinition;
import inaugural.soliloquy.ruleset.definitions.EffectsOnCharacterDefinition.MagnitudeForStatisticDefinition;
import inaugural.soliloquy.ruleset.definitions.StatisticChangeMagnitudeDefinition;
import soliloquy.specs.ruleset.entities.actonroundendandcharacterturn.EffectsCharacterOnRoundOrTurnChange.EffectsOnCharacter;
import soliloquy.specs.ruleset.entities.actonroundendandcharacterturn.StatisticChangeMagnitude;

import java.util.List;
import java.util.function.Function;

import static inaugural.soliloquy.ruleset.GetFunctions.getNullableAction;
import static inaugural.soliloquy.tools.collections.Collections.arrayOf;
import static inaugural.soliloquy.tools.collections.Collections.listOf;

@SuppressWarnings("rawtypes")
public class EffectsOnCharacterFactory
        implements Function<EffectsOnCharacterDefinition, EffectsOnCharacter> {
    @SuppressWarnings("rawtypes") private final Function<String, Action> GET_ACTION;
    private final Function<StatisticChangeMagnitudeDefinition, StatisticChangeMagnitude>
            MAGNITUDE_FACTORY;

    public EffectsOnCharacterFactory(
            @SuppressWarnings("rawtypes") Function<String, Action> getAction,
            Function<StatisticChangeMagnitudeDefinition, StatisticChangeMagnitude> magnitudeFactory) {
        GET_ACTION = Check.ifNull(getAction, "getAction");
        MAGNITUDE_FACTORY = Check.ifNull(magnitudeFactory, "magnitudeFactory");
    }

    @Override
    public EffectsOnCharacter apply(EffectsOnCharacterDefinition definition)
            throws IllegalArgumentException {
        Check.ifNull(definition, "definition");
        Check.ifNull(definition.magnitudeForStatisticDefinitions,
                "definition.magnitudeForStatisticDefinitions");
        for (var magnitudeDefinition : definition.magnitudeForStatisticDefinitions) {
            Check.ifNullOrEmpty(magnitudeDefinition.characterVariableStatisticTypeId,
                    "characterVariableStatisticTypeId within definition" +
                            ".magnitudeForStatisticDefinitions");
            Check.ifNull(magnitudeDefinition.magnitudeDefinition,
                    "magnitudeDefinition within definition.magnitudeForStatisticDefinitions");
        }

        final Action<Object[]> accompanyEffectAction =
                getNullableAction(GET_ACTION, definition.accompanyEffectFunctionId,
                        "definition.accompanyEffectFunctionId");

        final Action<Object[]> otherEffectsAction =
                getNullableAction(GET_ACTION, definition.otherEffectsFunctionId,
                        "definition.otherEffectsFunctionId");

        //noinspection rawtypes
        return new EffectsOnCharacter() {
            private final MagnitudeForStatisticDefinition[] MAGNITUDE_DEFINITIONS =
                    definition.magnitudeForStatisticDefinitions;
            private List<StatisticChangeMagnitude> magnitudes = null;

            @Override
            public int priority() {
                return definition.priority;
            }

            @Override
            public List<StatisticChangeMagnitude> magnitudes() {
                if (magnitudes == null) {
                    magnitudes = listOf();
                    for (MagnitudeForStatisticDefinition definition : MAGNITUDE_DEFINITIONS) {
                        magnitudes.add(MAGNITUDE_FACTORY.apply(definition.magnitudeDefinition));
                    }
                }
                return listOf(magnitudes);
            }

            @Override
            public void accompanyEffect(int[] effects, Character character, boolean advancingRounds)
                    throws IllegalArgumentException {
                Check.ifNull(effects, "effects");
                if (effects.length == 0) {
                    throw new IllegalArgumentException(
                            "EffectsOnCharacter.accompanyEffect: effects cannot be empty");
                }
                Check.ifNull(character, "character");
                if (accompanyEffectAction == null) {
                    return;
                }
                accompanyEffectAction.run(arrayOf(effects, character, advancingRounds));
            }

            @Override
            public void otherEffects(int[] effects, Character character, boolean advancingRounds)
                    throws IllegalArgumentException {
                Check.ifNull(effects, "effects");
                if (effects.length == 0) {
                    throw new IllegalArgumentException(
                            "EffectsOnCharacter.otherEffects: effects cannot be empty");
                }
                Check.ifNull(character, "character");
                if (otherEffectsAction == null) {
                    return;
                }
                otherEffectsAction.run(arrayOf(effects, character, advancingRounds));
            }
        };
    }
}
