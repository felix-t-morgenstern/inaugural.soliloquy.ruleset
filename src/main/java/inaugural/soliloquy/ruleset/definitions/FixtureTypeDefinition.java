package inaugural.soliloquy.ruleset.definitions;

import soliloquy.specs.common.shared.Direction;
import soliloquy.specs.io.graphics.assets.ImageAsset;

import static soliloquy.specs.io.graphics.assets.ImageAsset.ImageAssetType;

public class FixtureTypeDefinition {
    public String id;
    public String name;
    public String imageAssetSetId;
    public ImageAssetType imageAssetType;
    public boolean isContainer;
    public String onStepFunctionId;
    public String canStepFunctionId;
    public int additionalMovementCost;
    public Escalation[] escalations;
    public String heightMovementPenaltyMitigationFunctionId;
    public String[] defaultColorShifts;
    public float defaultXTileWidthOffset;
    public float defaultYTileHeightOffset;

    public FixtureTypeDefinition(String id, String name, String imageAssetSetId,
                                 ImageAsset.ImageAssetType imageAssetType, boolean isContainer,
                                 String onStepFunctionId, String canStepFunctionId,
                                 int additionalMovementCost, Escalation[] escalations,
                                 String heightMovementPenaltyMitigationFunctionId,
                                 String[] defaultColorShifts, float defaultXTileWidthOffset,
                                 float defaultYTileHeightOffset) {
        this.id = id;
        this.name = name;
        this.imageAssetSetId = imageAssetSetId;
        this.imageAssetType = imageAssetType;
        this.isContainer = isContainer;
        this.onStepFunctionId = onStepFunctionId;
        this.canStepFunctionId = canStepFunctionId;
        this.additionalMovementCost = additionalMovementCost;
        this.escalations = escalations;
        this.heightMovementPenaltyMitigationFunctionId = heightMovementPenaltyMitigationFunctionId;
        this.defaultColorShifts = defaultColorShifts;
        this.defaultXTileWidthOffset = defaultXTileWidthOffset;
        this.defaultYTileHeightOffset = defaultYTileHeightOffset;
    }

    public static class Escalation {
        public Direction direction;
        public int escalation;

        public Escalation(Direction direction, int escalation) {
            this.direction = direction;
            this.escalation = escalation;
        }
    }
}
