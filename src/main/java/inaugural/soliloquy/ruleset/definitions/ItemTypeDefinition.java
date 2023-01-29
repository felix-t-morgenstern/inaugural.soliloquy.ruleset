package inaugural.soliloquy.ruleset.definitions;

public class ItemTypeDefinition {
    public String id;
    public String name;
    public String pluralName;
    public String equipmentTypeId;
    public String imageAssetSetId;
    public float defaultXTileWidthOffset;
    public float defaultYTileHeightOffset;
    public String descriptionFunctionId;
    public String traits;
    public boolean isStackable;
    public Integer defaultNumberInStack;
    public boolean hasCharges;
    public Integer defaultCharges;
    public String[] activeAbilityIds;
    public String[] reactiveAbilityIds;
    public String[] passiveAbilityIds;

    public ItemTypeDefinition(String id,
                              String name,
                              String pluralName,
                              String equipmentTypeId,
                              String imageAssetSetId,
                              float defaultXTileWidthOffset,
                              float defaultYTileHeightOffset,
                              String descriptionFunctionId,
                              String traits,
                              boolean isStackable,
                              Integer defaultNumberInStack,
                              boolean hasCharges,
                              Integer defaultCharges,
                              String[] activeAbilityIds,
                              String[] reactiveAbilityIds,
                              String[] passiveAbilityIds) {
        this.id = id;
        this.name = name;
        this.pluralName = pluralName;
        this.equipmentTypeId = equipmentTypeId;
        this.imageAssetSetId = imageAssetSetId;
        this.defaultXTileWidthOffset = defaultXTileWidthOffset;
        this.defaultYTileHeightOffset = defaultYTileHeightOffset;
        this.descriptionFunctionId = descriptionFunctionId;
        this.traits = traits;
        this.isStackable = isStackable;
        this.defaultNumberInStack = defaultNumberInStack;
        this.hasCharges = hasCharges;
        this.defaultCharges = defaultCharges;
        this.activeAbilityIds = activeAbilityIds;
        this.reactiveAbilityIds = reactiveAbilityIds;
        this.passiveAbilityIds = passiveAbilityIds;
    }
}
