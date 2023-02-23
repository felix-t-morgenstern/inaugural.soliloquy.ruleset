package inaugural.soliloquy.ruleset.definitions;

public class ElementDefinition {
    public String id;
    public String name;
    public String description;
    public String imageAssetSetId;
    public String resistanceStatisticTypeId;

    public ElementDefinition(String id, String name, String description, String imageAssetSetId,
                             String resistanceStatisticTypeId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.imageAssetSetId = imageAssetSetId;
        this.resistanceStatisticTypeId = resistanceStatisticTypeId;
    }
}
