package inaugural.soliloquy.ruleset.definitions.abilities;

public class PassiveAbilityDefinition {
    public String id;
    public String name;
    public String data;
    public String characterSourceDescriptionFunctionId;
    public String itemSourceDescriptionFunctionId;

    public PassiveAbilityDefinition(String id,
                                    String name,
                                    String data,
                                    String characterSourceDescriptionFunctionId,
                                    String itemSourceDescriptionFunctionId) {
        this.id = id;
        this.name = name;
        this.data = data;
        this.characterSourceDescriptionFunctionId = characterSourceDescriptionFunctionId;
        this.itemSourceDescriptionFunctionId = itemSourceDescriptionFunctionId;
    }
}
