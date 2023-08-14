package inaugural.soliloquy.ruleset.definitions;

import soliloquy.specs.graphics.assets.ImageAsset;

public class WallSegmentTypeDefinition {
    public String id;
    public String name;
    public int direction;
    public ImageAsset.ImageAssetType imageAssetType;
    public String imageAssetId;
    public boolean blocksMovement;
    public boolean blocksSight;

    public WallSegmentTypeDefinition(String id, String name, int direction,
                                     ImageAsset.ImageAssetType imageAssetType,
                                     String imageAssetId, boolean blocksMovement,
                                     boolean blocksSight) {
        this.id = id;
        this.name = name;
        this.direction = direction;
        this.imageAssetType = imageAssetType;
        this.imageAssetId = imageAssetId;
        this.blocksMovement = blocksMovement;
        this.blocksSight = blocksSight;
    }
}
