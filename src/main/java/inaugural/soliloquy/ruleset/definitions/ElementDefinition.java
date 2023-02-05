package inaugural.soliloquy.ruleset.definitions;

public class ElementDefinition {
    public String id;
    public String name;
    public String description;
    public String imageAssetSetId;

    public ElementDefinition(String id, String name, String description, String imageAssetSetId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.imageAssetSetId = imageAssetSetId;
    }
}
