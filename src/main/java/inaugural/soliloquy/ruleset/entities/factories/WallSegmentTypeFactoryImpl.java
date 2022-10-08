package inaugural.soliloquy.ruleset.entities.factories;

import inaugural.soliloquy.tools.Check;
import soliloquy.specs.graphics.assets.GlobalLoopingAnimation;
import soliloquy.specs.graphics.assets.ImageAsset;
import soliloquy.specs.graphics.assets.Sprite;
import soliloquy.specs.ruleset.definitions.WallSegmentTypeDefinition;
import soliloquy.specs.ruleset.entities.WallSegmentType;
import soliloquy.specs.ruleset.entities.factories.WallSegmentTypeFactory;

import java.util.function.Function;

public class WallSegmentTypeFactoryImpl implements WallSegmentTypeFactory {
    private final Function<String, Sprite> GET_SPRITE;
    private final Function<String, GlobalLoopingAnimation> GET_GLOBAL_LOOPING_ANIMATION;

    public WallSegmentTypeFactoryImpl(Function<String, Sprite> getSprite, Function<String,
            GlobalLoopingAnimation> getGlobalLoopingAnimation) {
        GET_SPRITE = Check.ifNull(getSprite, "getSprite");
        GET_GLOBAL_LOOPING_ANIMATION = Check.ifNull(getGlobalLoopingAnimation,
                "getGlobalLoopingAnimation");
    }

    @Override
    public WallSegmentType make(WallSegmentTypeDefinition definition)
            throws IllegalArgumentException {
        Check.ifNull(definition, "definition");
        Check.ifNullOrEmpty(definition.id, "definition.id");
        Check.ifNullOrEmpty(definition.name, "definition.name");
        Check.ifNull(definition.imageAssetType, "definition.imageAssetType");
        Check.ifNullOrEmpty(definition.imageAssetId, "definition.imageAssetId");

        ImageAsset imageAsset;

        switch (definition.imageAssetType) {
            case SPRITE -> {
                imageAsset = GET_SPRITE.apply(definition.imageAssetId);
                if (imageAsset == null) {
                    throw new IllegalArgumentException("WallSegmentTypeFactoryImpl.make: " +
                            "imageAssetId (" + definition.imageAssetId + ") does not correspond " +
                            "to valid Sprite");
                }
            }
            case GLOBAL_LOOPING_ANIMATION -> {
                imageAsset = GET_GLOBAL_LOOPING_ANIMATION.apply(definition.imageAssetId);
                if (imageAsset == null) {
                    throw new IllegalArgumentException("WallSegmentTypeFactoryImpl.make: " +
                            "imageAssetId (" + definition.imageAssetId + ") does not correspond " +
                            "to valid GlobalLoopingAnimation");
                }
            }
            case ANIMATION ->
                    throw new IllegalArgumentException("WallSegmentTypeFactoryImpl.make: " +
                            "WallSegmentTypes cannot have an ImageAssetType of ANIMATION");
            case UNKNOWN ->
                    throw new IllegalArgumentException("WallSegmentTypeFactoryImpl.make: " +
                            "WallSegmentTypes cannot have an ImageAssetType of UNKNOWN");
            default ->
                    throw new IllegalArgumentException("WallSegmentTypeFactoryImpl.make: " +
                            "unexpected ImageAssetType");
        }

        return new WallSegmentType() {
            private String name = definition.name;

            @Override
            public ImageAsset imageAsset() {
                return imageAsset;
            }

            @Override
            public boolean blocksWest() {
                return definition.blocksWest;
            }

            @Override
            public boolean blocksNorthwest() {
                return definition.blocksNorthwest;
            }

            @Override
            public boolean blocksNorth() {
                return definition.blocksNorth;
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
        return WallSegmentTypeFactory.class.getCanonicalName();
    }
}
