package inaugural.soliloquy.ruleset.definitions;

import soliloquy.specs.common.shared.Direction;

public class GroundTypeDefinition {
    public String id;
    public String name;
    public int imageAssetType;
    public String imageAssetId;
    public String onStepFunctionId;
    public String canStepFunctionId;
    public int additionalMovementCost;
    public GroundTypeDefinition.Escalation[] escalations;
    public String heightMovementPenaltyMitigationFunctionId;
    public boolean blocksSight;
    public String[] defaultColorShifts;

    public GroundTypeDefinition(String id, String name, int imageAssetType, String imageAssetId,
                                String onStepFunctionId, String canStepFunctionId,
                                int additionalMovementCost, Escalation[] escalations,
                                String heightMovementPenaltyMitigationFunctionId,
                                boolean blocksSight, String[] defaultColorShifts) {
        this.id = id;
        this.name = name;
        this.imageAssetType = imageAssetType;
        this.imageAssetId = imageAssetId;
        this.onStepFunctionId = onStepFunctionId;
        this.canStepFunctionId = canStepFunctionId;
        this.additionalMovementCost = additionalMovementCost;
        this.escalations = escalations;
        this.heightMovementPenaltyMitigationFunctionId = heightMovementPenaltyMitigationFunctionId;
        this.blocksSight = blocksSight;
        this.defaultColorShifts = defaultColorShifts;
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
