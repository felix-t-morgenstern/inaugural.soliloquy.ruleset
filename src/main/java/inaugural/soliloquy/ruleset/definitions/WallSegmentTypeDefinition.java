package inaugural.soliloquy.ruleset.definitions;

import soliloquy.specs.graphics.assets.ImageAsset;

public class WallSegmentTypeDefinition {
    public String id;
    public String name;
    public ImageAsset.ImageAssetType imageAssetType;
    public String imageAssetId;
    public boolean blocksWest;
    public boolean blocksNorthwest;
    public boolean blocksNorth;

    public WallSegmentTypeDefinition(String id, String name,
                                     ImageAsset.ImageAssetType imageAssetType,
                                     String imageAssetId, boolean blocksWest,
                                     boolean blocksNorthwest, boolean blocksNorth) {
        this.id = id;
        this.name = name;
        this.imageAssetType = imageAssetType;
        this.imageAssetId = imageAssetId;
        this.blocksWest = blocksWest;
        this.blocksNorthwest = blocksNorthwest;
        this.blocksNorth = blocksNorth;
    }
}
