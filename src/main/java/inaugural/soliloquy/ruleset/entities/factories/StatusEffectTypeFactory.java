package inaugural.soliloquy.ruleset.entities.factories;

import inaugural.soliloquy.ruleset.definitions.EffectsOnCharacterDefinition;
import inaugural.soliloquy.ruleset.definitions.RoundEndEffectsOnCharacterDefinition;
import inaugural.soliloquy.ruleset.definitions.StatusEffectTypeDefinition;
import inaugural.soliloquy.tools.Check;
import soliloquy.specs.common.entities.Action;
import soliloquy.specs.common.valueobjects.Pair;
import soliloquy.specs.gamestate.entities.Character;
import soliloquy.specs.gamestate.entities.exceptions.EntityDeletedException;
import soliloquy.specs.graphics.assets.ImageAsset;
import soliloquy.specs.ruleset.entities.actonroundendandcharacterturn.EffectsCharacterOnRoundOrTurnChange.EffectsOnCharacter;
import soliloquy.specs.ruleset.entities.actonroundendandcharacterturn.EffectsCharacterOnRoundOrTurnChange.RoundEndEffectsOnCharacter;
import soliloquy.specs.ruleset.entities.character.StaticStatisticType;
import soliloquy.specs.ruleset.entities.character.StatusEffectType;

import java.util.Map;
import java.util.function.Function;

import static inaugural.soliloquy.ruleset.GetFunctions.getNonNullableAction;
import static inaugural.soliloquy.ruleset.GetFunctions.getNonNullableFunction;
import static inaugural.soliloquy.tools.collections.Collections.arrayOf;
import static inaugural.soliloquy.tools.collections.Collections.mapOf;

public class StatusEffectTypeFactory implements
        Function<StatusEffectTypeDefinition, StatusEffectType> {
    @SuppressWarnings("rawtypes") private final Function<String, Function> GET_FUNCTION;
    @SuppressWarnings("rawtypes") private final Function<String, Action> GET_ACTION;
    private final Function<String, StaticStatisticType> GET_STATIC_STAT_TYPE;
    private final Function<EffectsOnCharacterDefinition, EffectsOnCharacter>
            EFFECTS_ON_CHARACTER_FACTORY;
    private final Function<RoundEndEffectsOnCharacterDefinition, RoundEndEffectsOnCharacter>
            ROUND_END_EFFECTS_ON_CHARACTER_FACTORY;

    @SuppressWarnings("rawtypes")
    public StatusEffectTypeFactory(Function<String, Function> getFunction,
                                   Function<String, Action> getAction,
                                   Function<EffectsOnCharacterDefinition, EffectsOnCharacter> effectsOnCharacterFactory,
                                   Function<RoundEndEffectsOnCharacterDefinition,
                                           RoundEndEffectsOnCharacter>
                                           roundEndEffectsOnCharacterFactory,
                                   Function<String, StaticStatisticType> getStaticStatType) {
        GET_FUNCTION = Check.ifNull(getFunction, "getFunction");
        GET_ACTION = Check.ifNull(getAction, "getAction");
        EFFECTS_ON_CHARACTER_FACTORY =
                Check.ifNull(effectsOnCharacterFactory, "effectsOnCharacterFactory");
        ROUND_END_EFFECTS_ON_CHARACTER_FACTORY = Check.ifNull(roundEndEffectsOnCharacterFactory,
                "roundEndEffectsOnCharacterFactory");
        GET_STATIC_STAT_TYPE = Check.ifNull(getStaticStatType, "getStaticStatType");
    }

    @Override
    public StatusEffectType apply(StatusEffectTypeDefinition definition)
            throws IllegalArgumentException {
        Check.ifNull(definition, "definition");
        Check.ifNullOrEmpty(definition.id, "definition.id");
        Check.ifNullOrEmpty(definition.name, "definition.name");
        Check.ifNull(definition.effectsOnRoundEnd, "definition.effectsOnRoundEnd");
        Check.ifNull(definition.effectsOnTurnStart, "definition.effectsOnTurnStart");
        Check.ifNull(definition.effectsOnTurnEnd, "definition.effectsOnTurnEnd");

        Function<Integer, String> nameAtValueFunction = getNonNullableFunction(GET_FUNCTION,
                definition.nameAtValueFunctionId, "definition.nameAtValueFunctionId");

        Check.ifNull(definition.iconForCharacterFunctions, "definition.iconForCharacterFunctions");
        Map<String, Function<Character, Pair<ImageAsset, Integer>>> iconTypeFunctions = mapOf();

        for (var iconForCharacterFunction : definition.iconForCharacterFunctions) {
            Check.ifNull(iconForCharacterFunction,
                    "iconForCharacterFunction within definition.iconForCharacterFunctions");
            Check.ifNullOrEmpty(iconForCharacterFunction.iconType,
                    "iconType within iconForCharacterFunction within definition");
            Check.ifNullOrEmpty(iconForCharacterFunction.functionId,
                    "functionId within iconForCharacterFunction within definition");

            Function<Character, Pair<ImageAsset, Integer>> iconTypeFunction =
                    getNonNullableFunction(GET_FUNCTION, iconForCharacterFunction.functionId,
                            "iconForCharacterFunction within definition.iconForCharacterFunctions");

            iconTypeFunctions.put(iconForCharacterFunction.iconType, iconTypeFunction);
        }

        Action<Object[]> alterAction =
                getNonNullableAction(GET_ACTION, definition.alterValueActionId,
                        "definition.alterValueActionId");

        var onRoundEnd = ROUND_END_EFFECTS_ON_CHARACTER_FACTORY.apply(definition.effectsOnRoundEnd);
        var onTurnStart = EFFECTS_ON_CHARACTER_FACTORY.apply(definition.effectsOnTurnStart);
        var onTurnEnd = EFFECTS_ON_CHARACTER_FACTORY.apply(definition.effectsOnTurnEnd);

        var staticStatType = GET_STATIC_STAT_TYPE.apply(definition.resistanceStatisticTypeId);
        if (staticStatType == null) {
            throw new IllegalArgumentException(
                    "StatusEffectTypefactory.apply: definition.resistanceStatisticTypeId (" +
                            definition.resistanceStatisticTypeId +
                            ") does not correspond to a valid StaticStatisticType");
        }

        return new StatusEffectType() {
            private final String ID = definition.id;
            private final boolean STOPS_AT_ZERO = definition.stopsAtZero;
            private final Function<Integer, String> NAME_AT_VALUE_FUNCTION = nameAtValueFunction;
            private final Map<String, Function<Character, Pair<ImageAsset, Integer>>>
                    ICON_TYPE_FUNCTIONS = iconTypeFunctions;

            private String name = definition.name;

            @Override
            public boolean stopsAtZero() {
                return STOPS_AT_ZERO;
            }

            @Override
            public String nameAtValue(int value) throws UnsupportedOperationException {
                return NAME_AT_VALUE_FUNCTION.apply(value);
            }

            @Override
            public void alterValue(Character character, int amount)
                    throws IllegalArgumentException, EntityDeletedException {
                Check.ifNull(character, "character");
                alterAction.run(arrayOf(character, amount));
            }

            @Override
            public String id() throws IllegalStateException {
                return ID;
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public void setName(String name) {
                this.name = Check.ifNullOrEmpty(name, "name");
            }

            @Override
            public Pair<ImageAsset, Integer> getIcon(String iconType, Character character)
                    throws IllegalArgumentException {
                Check.ifNullOrEmpty(iconType, "iconType");
                Check.ifNull(character, "character");
                if (!ICON_TYPE_FUNCTIONS.containsKey(iconType)) {
                    throw new IllegalArgumentException(
                            "StatusEffectType.getIcon: iconType (" + iconType +
                                    ") is not a valid icon type");
                }

                return ICON_TYPE_FUNCTIONS.get(iconType).apply(character);
            }

            @Override
            public StaticStatisticType resistanceStatisticType() {
                return staticStatType;
            }

            @Override
            public RoundEndEffectsOnCharacter onRoundEnd() {
                return onRoundEnd;
            }

            @Override
            public EffectsOnCharacter onTurnStart() {
                return onTurnStart;
            }

            @Override
            public EffectsOnCharacter onTurnEnd() {
                return onTurnEnd;
            }
        };
    }
}
