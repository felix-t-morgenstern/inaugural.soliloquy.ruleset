package inaugural.soliloquy.ruleset.definitions;

import soliloquy.specs.io.graphics.assets.ImageAsset;

import static soliloquy.specs.io.graphics.assets.ImageAsset.ImageAssetType;

public class WallSegmentTypeDefinition {
    public String id;
    public String name;
    public int orientation;
    public ImageAssetType imageAssetType;
    public String imageAssetId;
    public boolean blocksMovement;
    public boolean blocksSight;

    public WallSegmentTypeDefinition(String id, String name, int orientation,
                                     ImageAsset.ImageAssetType imageAssetType,
                                     String imageAssetId, boolean blocksMovement,
                                     boolean blocksSight) {
        this.id = id;
        this.name = name;
        this.orientation = orientation;
        this.imageAssetType = imageAssetType;
        this.imageAssetId = imageAssetId;
        this.blocksMovement = blocksMovement;
        this.blocksSight = blocksSight;
    }
}
