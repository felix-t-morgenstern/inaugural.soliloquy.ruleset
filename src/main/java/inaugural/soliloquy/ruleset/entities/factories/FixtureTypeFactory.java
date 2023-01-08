package inaugural.soliloquy.ruleset.entities.factories;

import inaugural.soliloquy.tools.Check;
import soliloquy.specs.common.factories.Factory;
import soliloquy.specs.common.persistence.TypeHandler;
import soliloquy.specs.common.shared.Direction;
import soliloquy.specs.common.valueobjects.Vertex;
import soliloquy.specs.game.Game;
import soliloquy.specs.gamestate.entities.Character;
import soliloquy.specs.gamestate.entities.Tile;
import soliloquy.specs.graphics.assets.GlobalLoopingAnimation;
import soliloquy.specs.graphics.assets.ImageAsset;
import soliloquy.specs.graphics.assets.Sprite;
import soliloquy.specs.graphics.renderables.colorshifting.ColorShift;
import soliloquy.specs.logger.Logger;
import soliloquy.specs.ruleset.definitions.FixtureTypeDefinition;
import soliloquy.specs.ruleset.entities.FixtureType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class FixtureTypeFactory implements Factory<FixtureTypeDefinition, FixtureType> {
    private final TypeHandler<ColorShift> COLOR_SHIFT_HANDLER;
    @SuppressWarnings("rawtypes")
    private final Function<String, soliloquy.specs.common.entities.Function> GET_FUNCTION;
    private final ImageAssetRetrieval IMAGE_ASSET_SET_RETRIEVAL;

    public FixtureTypeFactory(TypeHandler<ColorShift> colorShiftHandler,
                              @SuppressWarnings("rawtypes")
                                      Function<String, soliloquy.specs.common.entities.Function> getFunction,
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

        var defaultColorShifts = new ArrayList<ColorShift>();
        if (definition.defaultColorShifts != null) {
            for (var colorShift : definition.defaultColorShifts) {
                defaultColorShifts.add(COLOR_SHIFT_HANDLER.read(colorShift));
            }
        }

        Check.ifNull(definition.imageAssetType, "definition.imageAssetType");
        var imageAsset = IMAGE_ASSET_SET_RETRIEVAL.getImageAsset(definition.imageAssetSetId,
                definition.imageAssetType, "FixtureTypeFactory");

        //noinspection unchecked
        var onStepFunction =
                (soliloquy.specs.common.entities.Function<Character, Boolean>) GET_FUNCTION.apply(
                        definition.onStepFunctionId);
        if (onStepFunction == null) {
            throw new IllegalArgumentException(
                    "GroundTypeFactory.make: onStepFunctionId (" + definition.onStepFunctionId +
                            ") does not correspond to a valid function");
        }
        //noinspection unchecked
        var canStepFunction =
                (soliloquy.specs.common.entities.Function<Character, Boolean>) GET_FUNCTION.apply(
                        definition.canStepFunctionId);
        if (canStepFunction == null) {
            throw new IllegalArgumentException(
                    "GroundTypeFactory.make: canStepFunctionId (" + definition.canStepFunctionId +
                            ") does not correspond to a valid function");
        }

        //noinspection unchecked
        var heightMovementPenaltyMitigationFunction =
                (soliloquy.specs.common.entities.Function<Object[], Integer>) GET_FUNCTION.apply(
                        definition.heightMovementPenaltyMitigationFunctionId);
        if (heightMovementPenaltyMitigationFunction == null) {
            throw new IllegalArgumentException(
                    "GroundTypeFactory.make: heightMovementPenaltyMitigationFunctionId (" +
                            definition.heightMovementPenaltyMitigationFunctionId +
                            ") does not correspond to a valid function");
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
