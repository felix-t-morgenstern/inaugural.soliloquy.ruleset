package inaugural.soliloquy.ruleset.entities.actonroundendandcharacterturn.factories;

import inaugural.soliloquy.tools.Check;
import soliloquy.specs.common.entities.Action;
import soliloquy.specs.common.factories.Factory;
import soliloquy.specs.common.valueobjects.Pair;
import soliloquy.specs.gamestate.entities.Character;
import soliloquy.specs.ruleset.definitions.EffectsOnCharacterDefinition;
import soliloquy.specs.ruleset.definitions.EffectsOnCharacterDefinition.MagnitudeForStatisticDefinition;
import soliloquy.specs.ruleset.definitions.StatisticChangeMagnitudeDefinition;
import soliloquy.specs.ruleset.entities.actonroundendandcharacterturn.EffectsCharacterOnRoundOrTurnChange.EffectsOnCharacter;
import soliloquy.specs.ruleset.entities.actonroundendandcharacterturn.StatisticChangeMagnitude;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@SuppressWarnings("rawtypes")
public class EffectsOnCharacterFactory
        implements Factory<EffectsOnCharacterDefinition, EffectsOnCharacter> {
    @SuppressWarnings("rawtypes") private final Function<String, Action> GET_ACTION;
    private final Factory<StatisticChangeMagnitudeDefinition, StatisticChangeMagnitude>
            MAGNITUDE_FACTORY;

    public EffectsOnCharacterFactory(
            @SuppressWarnings("rawtypes") Function<String, Action> getAction,
            Factory<StatisticChangeMagnitudeDefinition, StatisticChangeMagnitude> magnitudeFactory) {
        GET_ACTION = Check.ifNull(getAction, "getAction");
        MAGNITUDE_FACTORY = Check.ifNull(magnitudeFactory, "magnitudeFactory");
    }

    @Override
    public EffectsOnCharacter make(EffectsOnCharacterDefinition definition)
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
        Check.ifNullOrEmpty(definition.accompanyEffectFunctionId,
                "definition.accompanyEffectFunctionId");
        Check.ifNullOrEmpty(definition.otherEffectsFunctionId, "definition.otherEffectsFunctionId");

        //noinspection unchecked
        var accompanyEffectAction = (Action<Pair<int[], Character>>) GET_ACTION.apply(
                definition.accompanyEffectFunctionId);
        if (accompanyEffectAction == null) {
            throw new IllegalArgumentException(
                    "definition.accompanyEffectAction (" + definition.accompanyEffectFunctionId +
                            ") does not correspond to valid Action");
        }

        //noinspection unchecked
        var otherEffectsAction = (Action<Pair<int[], Character>>) GET_ACTION.apply(
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
            private List<StatisticChangeMagnitude> magnitudes = null;

            @Override
            public List<StatisticChangeMagnitude> magnitudes() {
                if (magnitudes == null) {
                    magnitudes = new ArrayList<>();
                    for (MagnitudeForStatisticDefinition definition : MAGNITUDE_DEFINITIONS) {
                        magnitudes.add(MAGNITUDE_FACTORY.make(definition.magnitudeDefinition));
                    }
                }
                return new ArrayList<>(magnitudes);
            }

            @Override
            public void accompanyEffect(int[] effects, Character character)
                    throws IllegalArgumentException {
                Check.ifNull(character, "character");
                accompanyEffectAction.run(Pair.of(effects, character));
            }

            @Override
            public void otherEffects(int[] effects, Character character)
                    throws IllegalArgumentException {
                Check.ifNull(character, "character");
                otherEffectsAction.run(Pair.of(effects, character));
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
