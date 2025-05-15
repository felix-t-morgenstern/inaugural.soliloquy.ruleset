package inaugural.soliloquy.ruleset.entities.actonroundendandcharacterturn.factories;

import inaugural.soliloquy.ruleset.definitions.EffectsOnCharacterDefinition;
import inaugural.soliloquy.ruleset.definitions.RoundEndEffectsOnCharacterDefinition;
import inaugural.soliloquy.ruleset.definitions.StatisticChangeMagnitudeDefinition;
import inaugural.soliloquy.tools.Check;
import soliloquy.specs.common.entities.Action;
import soliloquy.specs.common.valueobjects.Pair;
import soliloquy.specs.gamestate.entities.Character;
import soliloquy.specs.ruleset.entities.actonroundendandcharacterturn.EffectsCharacterOnRoundOrTurnChange.RoundEndEffectsOnCharacter;
import soliloquy.specs.ruleset.entities.actonroundendandcharacterturn.StatisticChangeMagnitude;

import java.util.List;
import java.util.function.Function;

import static inaugural.soliloquy.ruleset.GetFunctions.getNullableAction;
import static inaugural.soliloquy.tools.collections.Collections.arrayOf;
import static inaugural.soliloquy.tools.collections.Collections.listOf;

@SuppressWarnings("rawtypes")
public class RoundEndEffectsOnCharacterFactory
        implements Function<RoundEndEffectsOnCharacterDefinition, RoundEndEffectsOnCharacter> {
    @SuppressWarnings("rawtypes") private final Function<String, Action> GET_ACTION;
    private final Function<StatisticChangeMagnitudeDefinition, StatisticChangeMagnitude>
            MAGNITUDE_FACTORY;

    public RoundEndEffectsOnCharacterFactory(
            @SuppressWarnings("rawtypes") Function<String, Action> getAction,
            Function<StatisticChangeMagnitudeDefinition, StatisticChangeMagnitude> magnitudeFactory) {
        GET_ACTION = Check.ifNull(getAction, "getAction");
        MAGNITUDE_FACTORY = Check.ifNull(magnitudeFactory, "magnitudeFactory");
    }

    @Override
    public RoundEndEffectsOnCharacter apply(RoundEndEffectsOnCharacterDefinition definition)
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

        final Action<Object[]> accompanyAllEffectsAction =
                getNullableAction(GET_ACTION, definition.accompanyAllEffectsFunctionId,
                        "definition.accompanyAllEffectsFunctionId");

        //noinspection rawtypes
        return new RoundEndEffectsOnCharacter() {
            private final EffectsOnCharacterDefinition.MagnitudeForStatisticDefinition[]
                    MAGNITUDE_DEFINITIONS =
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
                    for (EffectsOnCharacterDefinition.MagnitudeForStatisticDefinition definition
                            : MAGNITUDE_DEFINITIONS) {
                        magnitudes.add(MAGNITUDE_FACTORY.apply(definition.magnitudeDefinition));
                    }
                }
                return listOf(magnitudes);
            }

            @Override
            public void accompanyEffect(int[] effects, Character character, boolean advancingRounds) throws IllegalArgumentException {
                Check.ifNull(effects, "effects");
                if (effects.length == 0) {
                    throw new IllegalArgumentException("RoundEndEffectsOnCharacter.accompanyEffect: effects cannot be empty");
                }
                Check.ifNull(character, "character");
                if (accompanyEffectAction == null) {
                    return;
                }
                accompanyEffectAction.run(arrayOf(effects, character, advancingRounds));
            }

            @Override
            public void otherEffects(int[] effects, Character character, boolean advancingRounds) throws IllegalArgumentException {
                Check.ifNull(effects, "effects");
                if (effects.length == 0) {
                    throw new IllegalArgumentException("RoundEndEffectsOnCharacter.otherEffects: effects cannot be empty");
                }
                Check.ifNull(character, "character");
                if (otherEffectsAction == null) {
                    return;
                }
                otherEffectsAction.run(arrayOf(effects, character, advancingRounds));
            }

            @Override
            public void accompanyAllEffects(List<Pair<int[], Character>> list, boolean advancingRounds) throws IllegalArgumentException {
                Check.ifNull(list, "list");
                for (var pair : list) {
                    Check.ifNull(pair, "pair within list");
                    Check.ifNull(pair.FIRST, "magnitudes within pair within list");
                    Check.ifNull(pair.SECOND, "character within pair within list");
                }
                if (accompanyAllEffectsAction == null) {
                    return;
                }
                accompanyAllEffectsAction.run(arrayOf(list, advancingRounds));
            }
        };
    }
}
