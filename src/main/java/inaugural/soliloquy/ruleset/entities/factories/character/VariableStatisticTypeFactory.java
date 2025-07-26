package inaugural.soliloquy.ruleset.entities.factories.character;

import inaugural.soliloquy.ruleset.definitions.EffectsOnCharacterDefinition;
import inaugural.soliloquy.ruleset.definitions.RoundEndEffectsOnCharacterDefinition;
import inaugural.soliloquy.ruleset.definitions.VariableStatisticTypeDefinition;
import inaugural.soliloquy.tools.Check;
import inaugural.soliloquy.tools.collections.Collections;
import soliloquy.specs.common.entities.Action;
import soliloquy.specs.common.persistence.TypeHandler;
import soliloquy.specs.common.valueobjects.Pair;
import soliloquy.specs.gamestate.entities.Character;
import soliloquy.specs.gamestate.entities.exceptions.EntityDeletedException;
import soliloquy.specs.io.graphics.assets.ImageAsset;
import soliloquy.specs.io.graphics.assets.ImageAssetSet;
import soliloquy.specs.io.graphics.renderables.colorshifting.ColorShift;
import soliloquy.specs.ruleset.entities.actonroundendandcharacterturn.EffectsCharacterOnRoundOrTurnChange.EffectsOnCharacter;
import soliloquy.specs.ruleset.entities.actonroundendandcharacterturn.EffectsCharacterOnRoundOrTurnChange.RoundEndEffectsOnCharacter;
import soliloquy.specs.ruleset.entities.character.VariableStatisticType;

import java.util.List;
import java.util.function.Function;

import static inaugural.soliloquy.ruleset.GetFunctions.getNonNullableAction;
import static inaugural.soliloquy.ruleset.GetFunctions.getNonNullableFunction;
import static inaugural.soliloquy.tools.collections.Collections.arrayOf;
import static inaugural.soliloquy.tools.collections.Collections.listOf;
import static soliloquy.specs.common.valueobjects.Pair.pairOf;

/** @noinspection rawtypes */
public class VariableStatisticTypeFactory implements
        Function<VariableStatisticTypeDefinition, VariableStatisticType> {
    private final TypeHandler<ColorShift> SHIFT_HANDLER;
    private final Function<String, ImageAssetSet> GET_IMAGE_ASSET_SET;
    private final Function<String, Function> GET_FUNCTION;
    private final Function<String, Action> GET_ACTION;
    private final Function<EffectsOnCharacterDefinition, EffectsOnCharacter>
            EFFECTS_ON_CHARACTER_FACTORY;
    private final Function<RoundEndEffectsOnCharacterDefinition, RoundEndEffectsOnCharacter>
            ROUND_END_EFFECTS_ON_CHARACTER_FACTORY;

    public VariableStatisticTypeFactory(
            TypeHandler<ColorShift> shiftHandler,
            Function<String, ImageAssetSet> getImageAssetSet,
            Function<String, Function> getFunction,
            Function<String, Action> getAction,
            Function<EffectsOnCharacterDefinition, EffectsOnCharacter> effectsOnCharacterFactory,
            Function<RoundEndEffectsOnCharacterDefinition, RoundEndEffectsOnCharacter> roundEndEffectsOnCharacterFactory) {
        SHIFT_HANDLER = Check.ifNull(shiftHandler, "shiftHandler");
        GET_IMAGE_ASSET_SET = Check.ifNull(getImageAssetSet, "getImageAssetSet");
        GET_FUNCTION = Check.ifNull(getFunction, "getFunction");
        GET_ACTION = Check.ifNull(getAction, "getAction");
        EFFECTS_ON_CHARACTER_FACTORY =
                Check.ifNull(effectsOnCharacterFactory, "effectsOnCharacterFactory");
        ROUND_END_EFFECTS_ON_CHARACTER_FACTORY = roundEndEffectsOnCharacterFactory;
    }

    @Override
    public VariableStatisticType apply(VariableStatisticTypeDefinition definition)
            throws IllegalArgumentException {
        Check.ifNull(definition, "definition");
        Check.ifNullOrEmpty(definition.id, "definition.id");
        Check.ifNullOrEmpty(definition.name, "definition.name");
        Check.ifNullOrEmpty(definition.pluralName, "definition.pluralName");
        Check.ifNullOrEmpty(definition.imageAssetSetId, "definition.imageAssetSetId");
        Check.ifNull(definition.effectsOnRoundEnd, "definition.effectsOnRoundEnd");
        Check.ifNull(definition.effectsOnTurnStart, "definition.effectsOnTurnStart");
        Check.ifNull(definition.effectsOnTurnEnd, "definition.effectsOnTurnEnd");

        var imageAssetSet = GET_IMAGE_ASSET_SET.apply(definition.imageAssetSetId);
        if (imageAssetSet == null) {
            throw new IllegalArgumentException(
                    "VariableStatisticTypeFactory.apply: definition.imageAssetSetId does " +
                            "not correspond to a valid ImageAssetSet");
        }

        Function<Pair<String, Character>, Pair<ImageAsset, Integer>> iconForCharacterFunction =
                getNonNullableFunction(GET_FUNCTION,
                        definition.iconForCharacterFunctionId,
                        "definition.iconForCharacterFunctionId");

        final var alterAction = getNonNullableAction(GET_ACTION, definition.alterActionId,
                "definition.alterActionId");

        var colorShifts = Collections.<ColorShift>listOf();
        for (var colorShiftProvider : definition.defaultColorShifts) {
            colorShifts.add(SHIFT_HANDLER.read(colorShiftProvider));
        }

        var onRoundEnd = ROUND_END_EFFECTS_ON_CHARACTER_FACTORY.apply(definition.effectsOnRoundEnd);
        var onTurnStart = EFFECTS_ON_CHARACTER_FACTORY.apply(definition.effectsOnTurnStart);
        var onTurnEnd = EFFECTS_ON_CHARACTER_FACTORY.apply(definition.effectsOnTurnEnd);

        return new VariableStatisticType() {
            @Override
            public void alter(Character character, int amount)
                    throws IllegalArgumentException, EntityDeletedException {
                Check.ifNull(character, "character");
                alterAction.run(character, amount, this);
            }

            private String name = definition.name;
            private String pluralName = definition.pluralName;
            private String description = definition.description;

            @Override
            public String getPluralName() {
                return pluralName;
            }

            @Override
            public void setPluralName(String pluralName) throws IllegalArgumentException {
                this.pluralName = Check.ifNullOrEmpty(pluralName, "pluralName");
            }

            @Override
            public String getDescription() {
                return description;
            }

            @Override
            public void setDescription(String description) {
                this.description = Check.ifNullOrEmpty(description, "description");
            }

            @Override
            public String id() throws IllegalStateException {
                return definition.id;
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
            public List<ColorShift> colorShifts() {
                return colorShifts;
            }

            @Override
            public ImageAssetSet imageAssetSet() {
                return imageAssetSet;
            }

            @Override
            public Pair<ImageAsset, Integer> getIcon(String iconType, Character character)
                    throws IllegalArgumentException {
                return iconForCharacterFunction.apply(pairOf(iconType, character));
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
