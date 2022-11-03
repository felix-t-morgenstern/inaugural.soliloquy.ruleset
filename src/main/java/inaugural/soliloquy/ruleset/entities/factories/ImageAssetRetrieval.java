package inaugural.soliloquy.ruleset.entities.factories;

import inaugural.soliloquy.tools.Check;
import soliloquy.specs.graphics.assets.GlobalLoopingAnimation;
import soliloquy.specs.graphics.assets.ImageAsset;
import soliloquy.specs.graphics.assets.Sprite;

import java.util.function.Function;

class ImageAssetRetrieval {
    private final Function<String, Sprite> GET_SPRITE;
    private final Function<String, GlobalLoopingAnimation> GET_GLOBAL_LOOPING_ANIMATION;

    ImageAssetRetrieval(Function<String, Sprite> getSprite,
                        Function<String, GlobalLoopingAnimation> getGlobalLoopingAnimation) {
        GET_SPRITE = Check.ifNull(getSprite, "getSprite");
        GET_GLOBAL_LOOPING_ANIMATION =
                Check.ifNull(getGlobalLoopingAnimation, "getGlobalLoopingAnimation");
    }

    ImageAsset getImageAsset(String imageAssetId, ImageAsset.ImageAssetType imageAssetType,
                             String className) {
        ImageAsset imageAsset;
        switch (imageAssetType) {
            case SPRITE -> {
                imageAsset = GET_SPRITE.apply(imageAssetId);
                if (imageAsset == null) {
                    throw new IllegalArgumentException(className + ".make: " +
                            "imageAssetId (" + imageAssetId + ") does not correspond " +
                            "to valid Sprite");
                }
            }
            case GLOBAL_LOOPING_ANIMATION -> {
                imageAsset = GET_GLOBAL_LOOPING_ANIMATION.apply(imageAssetId);
                if (imageAsset == null) {
                    throw new IllegalArgumentException(className + ".make: " +
                            "imageAssetId (" + imageAssetId + ") does not correspond " +
                            "to valid GlobalLoopingAnimation");
                }
            }
            case ANIMATION -> throw new IllegalArgumentException(className + ".make: " +
                    "WallSegmentTypes cannot have an ImageAssetType of ANIMATION");
            case UNKNOWN -> throw new IllegalArgumentException(className + ".make: " +
                    "WallSegmentTypes cannot have an ImageAssetType of UNKNOWN");
            default -> throw new IllegalArgumentException("WallSegmentTypeFactory.make: " +
                    "unexpected ImageAssetType");
        }

        return imageAsset;
    }
}
