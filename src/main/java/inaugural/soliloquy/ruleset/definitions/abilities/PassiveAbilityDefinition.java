package inaugural.soliloquy.ruleset.definitions.abilities;

public class PassiveAbilityDefinition {
    public String characterSourceDescriptionFunctionId;
    public String itemSourceDescriptionFunctionId;
    public String data;

    public PassiveAbilityDefinition(String characterSourceDescriptionFunctionId,
                                    String itemSourceDescriptionFunctionId,
                                    String data) {
        this.characterSourceDescriptionFunctionId = characterSourceDescriptionFunctionId;
        this.itemSourceDescriptionFunctionId = itemSourceDescriptionFunctionId;
        this.data = data;
    }
}
