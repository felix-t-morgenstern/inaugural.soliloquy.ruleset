package inaugural.soliloquy.ruleset.entities.factories;

import inaugural.soliloquy.tools.Check;
import soliloquy.specs.common.factories.Factory;
import soliloquy.specs.gamestate.entities.WallSegmentDirection;
import soliloquy.specs.gamestate.entities.exceptions.EntityDeletedException;
import soliloquy.specs.graphics.assets.GlobalLoopingAnimation;
import soliloquy.specs.graphics.assets.ImageAsset;
import soliloquy.specs.graphics.assets.Sprite;
import inaugural.soliloquy.ruleset.definitions.WallSegmentTypeDefinition;
import soliloquy.specs.ruleset.entities.WallSegmentType;

import java.util.function.Function;

public class WallSegmentTypeFactory implements
        Factory<WallSegmentTypeDefinition, WallSegmentType> {
    private final ImageAssetRetrieval IMAGE_ASSET_SET_RETRIEVAL;

    public WallSegmentTypeFactory(Function<String, Sprite> getSprite,
                                  Function<String, GlobalLoopingAnimation> getGlobalLoopingAnimation) {
        IMAGE_ASSET_SET_RETRIEVAL = new ImageAssetRetrieval(getSprite, getGlobalLoopingAnimation);
    }

    @Override
    public WallSegmentType make(WallSegmentTypeDefinition definition)
            throws IllegalArgumentException {
        Check.ifNull(definition, "definition");
        Check.ifNullOrEmpty(definition.id, "definition.id");
        Check.ifNullOrEmpty(definition.name, "definition.name");
        Check.ifNull(definition.imageAssetType, "definition.imageAssetType");
        Check.ifNullOrEmpty(definition.imageAssetId, "definition.imageAssetId");

        var imageAsset = IMAGE_ASSET_SET_RETRIEVAL.getImageAsset(definition.imageAssetId,
                definition.imageAssetType, "WallSegmentTypeFactory");

        var direction = WallSegmentDirection.fromValue(definition.direction);

        return new WallSegmentType() {
            private String name = definition.name;

            @Override
            public WallSegmentDirection direction() throws EntityDeletedException {
                return direction;
            }

            @Override
            public ImageAsset imageAsset() {
                return imageAsset;
            }

            @Override
            public boolean blocksMovement() {
                return definition.blocksMovement;
            }

            @Override
            public boolean blocksSight() {
                return definition.blocksSight;
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
                return WallSegmentType.class.getCanonicalName();
            }
        };
    }

    @Override
    public String getInterfaceName() {
        return Factory.class.getCanonicalName() + "<" +
                WallSegmentTypeDefinition.class.getCanonicalName() + "," +
                WallSegmentType.class.getCanonicalName() + ">";
    }
}
