package inaugural.soliloquy.ruleset.entities.factories;

import inaugural.soliloquy.ruleset.definitions.FixtureTypeDefinition;
import inaugural.soliloquy.tools.Check;
import inaugural.soliloquy.tools.collections.Collections;
import soliloquy.specs.common.factories.Factory;
import soliloquy.specs.common.persistence.TypeHandler;
import soliloquy.specs.common.shared.Direction;
import soliloquy.specs.common.valueobjects.Vertex;
import soliloquy.specs.gamestate.entities.Character;
import soliloquy.specs.gamestate.entities.Tile;
import soliloquy.specs.graphics.assets.GlobalLoopingAnimation;
import soliloquy.specs.graphics.assets.ImageAsset;
import soliloquy.specs.graphics.assets.Sprite;
import soliloquy.specs.graphics.renderables.colorshifting.ColorShift;
import soliloquy.specs.ruleset.entities.FixtureType;

import java.util.List;
import java.util.function.Function;

import static inaugural.soliloquy.ruleset.GetFunctions.getNonNullableFunction;
import static inaugural.soliloquy.tools.collections.Collections.listOf;

public class FixtureTypeFactory implements Factory<FixtureTypeDefinition, FixtureType> {
    private final TypeHandler<ColorShift> COLOR_SHIFT_HANDLER;
    @SuppressWarnings("rawtypes")
    private final Function<String, Function> GET_FUNCTION;
    private final ImageAssetRetrieval IMAGE_ASSET_SET_RETRIEVAL;

    public FixtureTypeFactory(TypeHandler<ColorShift> colorShiftHandler,
                              @SuppressWarnings("rawtypes") Function<String, Function> getFunction,
                              Function<String, Sprite> getSprite,
                              Function<String, GlobalLoopingAnimation> getGlobalLoopingAnimation) {
        COLOR_SHIFT_HANDLER = Check.ifNull(colorShiftHandler, "colorShiftHandler");
        GET_FUNCTION = Check.ifNull(getFunction, "getFunction");
        IMAGE_ASSET_SET_RETRIEVAL = new ImageAssetRetrieval(getSprite, getGlobalLoopingAnimation);
    }

    @Override
    public FixtureType make(FixtureTypeDefinition definition)
            throws IllegalArgumentException {
        Check.ifNull(definition, "definition");
        Check.ifNullOrEmpty(definition.id, "definition.id");
        Check.ifNullOrEmpty(definition.name, "definition.name");

        List<ColorShift> defaultColorShifts = listOf();
        if (definition.defaultColorShifts != null) {
            for (var colorShift : definition.defaultColorShifts) {
                defaultColorShifts.add(COLOR_SHIFT_HANDLER.read(colorShift));
            }
        }

        Check.ifNull(definition.imageAssetType, "definition.imageAssetType");
        var imageAsset = IMAGE_ASSET_SET_RETRIEVAL.getImageAsset(definition.imageAssetSetId,
                definition.imageAssetType, "FixtureTypeFactory");

        Function<Character, Boolean> onStepFunction =
                getNonNullableFunction(GET_FUNCTION, definition.onStepFunctionId,
                        "definition.onStepFunctionId");
        Function<Character, Boolean> canStepFunction =
                getNonNullableFunction(GET_FUNCTION, definition.canStepFunctionId,
                        "definition.canStepFunctionId");
        Function<Object[], Integer> heightMovementPenaltyMitigationFunction =
                getNonNullableFunction(GET_FUNCTION,
                        definition.heightMovementPenaltyMitigationFunctionId,
                        "definition.heightMovementPenaltyMitigationFunctionId");

        var escalations = Collections.<Direction, Integer>mapOf();
        if (definition.escalations != null) {
            for (var escalation : definition.escalations) {
                escalations.put(escalation.direction, escalation.escalation);
            }
        }

        return new FixtureType() {
            private String name = definition.name;

            @Override
            public boolean isContainer() {
                return definition.isContainer;
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
            public String getInterfaceName() {
                return FixtureType.class.getCanonicalName();
            }

            @Override
            public List<ColorShift> defaultColorShifts() {
                return defaultColorShifts;
            }

            @Override
            public Vertex defaultTileOffset() {
                return Vertex.of(definition.defaultXTileWidthOffset,
                        definition.defaultYTileHeightOffset);
            }

            @Override
            public ImageAsset imageAsset() {
                return imageAsset;
            }

            @Override
            public boolean onStep(Character character) {
                return onStepFunction.apply(Check.ifNull(character, "character"));
            }

            @Override
            public boolean canStep(Character character) {
                return canStepFunction.apply(Check.ifNull(character, "character"));
            }

            @Override
            public int additionalMovementCost() {
                return definition.additionalMovementCost;
            }

            @Override
            public int escalation(Direction direction) {
                return escalations.getOrDefault(Check.ifNull(direction, "direction"), 0);
            }

            @Override
            public int heightMovementPenaltyMitigation(Tile tile, Character character,
                                                       Direction direction) {
                return heightMovementPenaltyMitigationFunction.apply(
                        new Object[]{tile, character, direction});
            }
        };
    }

    @Override
    public String getInterfaceName() {
        return Factory.class.getCanonicalName() + "<" +
                FixtureTypeDefinition.class.getCanonicalName() + "," +
                FixtureType.class.getCanonicalName() + ">";
    }
}
