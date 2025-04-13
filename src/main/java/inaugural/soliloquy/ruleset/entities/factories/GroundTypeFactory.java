package inaugural.soliloquy.ruleset.entities.factories;

import inaugural.soliloquy.ruleset.definitions.GroundTypeDefinition;
import inaugural.soliloquy.tools.Check;
import soliloquy.specs.common.factories.Factory;
import soliloquy.specs.common.persistence.TypeHandler;
import soliloquy.specs.common.shared.Direction;
import soliloquy.specs.gamestate.entities.Character;
import soliloquy.specs.gamestate.entities.Tile;
import soliloquy.specs.graphics.assets.GlobalLoopingAnimation;
import soliloquy.specs.graphics.assets.ImageAsset;
import soliloquy.specs.graphics.assets.Sprite;
import soliloquy.specs.graphics.renderables.colorshifting.ColorShift;
import soliloquy.specs.ruleset.entities.GroundType;

import java.util.List;
import java.util.function.Function;

import static inaugural.soliloquy.ruleset.GetFunctions.getNonNullableFunction;
import static inaugural.soliloquy.tools.collections.Collections.listOf;

public class GroundTypeFactory implements Factory<GroundTypeDefinition, GroundType> {
    private final TypeHandler<ColorShift> COLOR_SHIFT_HANDLER;
    @SuppressWarnings("rawtypes")
    private final Function<String, Function> GET_FUNCTION;
    private final ImageAssetRetrieval IMAGE_ASSET_SET_RETRIEVAL;

    public GroundTypeFactory(TypeHandler<ColorShift> colorShiftHandler,
                             @SuppressWarnings("rawtypes")
                             Function<String, Function> getFunction,
                             Function<String, Sprite> getSprite,
                             Function<String, GlobalLoopingAnimation> getGlobalLoopingAnimation) {
        COLOR_SHIFT_HANDLER = Check.ifNull(colorShiftHandler, "colorShiftHandler");
        GET_FUNCTION = Check.ifNull(getFunction, "getFunction");
        IMAGE_ASSET_SET_RETRIEVAL = new ImageAssetRetrieval(getSprite, getGlobalLoopingAnimation);
    }

    @Override
    public GroundType make(GroundTypeDefinition definition)
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

        var imageAsset = IMAGE_ASSET_SET_RETRIEVAL.getImageAsset(definition.imageAssetId,
                ImageAsset.ImageAssetType.getFromValue(definition.imageAssetType),
                "GroundTypeFactory");

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

        return new GroundType() {
            private String name = definition.name;
            private boolean blocksSight = definition.blocksSight;

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
            public int heightMovementPenaltyMitigation(Tile tile, Character character,
                                                       Direction direction) {
                return heightMovementPenaltyMitigationFunction.apply(
                        new Object[]{tile, character, direction});
            }

            @Override
            public boolean blocksSight() {
                return false;
            }

            @Override
            public String getInterfaceName() {
                return GroundType.class.getCanonicalName();
            }

            @Override
            public List<ColorShift> defaultColorShifts() {
                return defaultColorShifts;
            }
        };
    }

    @Override
    public String getInterfaceName() {
        return Factory.class.getCanonicalName() + "<" +
                GroundTypeDefinition.class.getCanonicalName() + "," +
                GroundType.class.getCanonicalName() + ">";
    }
}
