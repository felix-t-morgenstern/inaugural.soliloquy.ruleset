package inaugural.soliloquy.ruleset.definitions.abilities;

public class PassiveAbilityDefinition {
    public String characterSourceDescriptionFunctionId;
    public String itemSourceDescriptionFunctionId;

    public PassiveAbilityDefinition(String characterSourceDescriptionFunctionId,
                                    String itemSourceDescriptionFunctionId) {
        this.characterSourceDescriptionFunctionId = characterSourceDescriptionFunctionId;
        this.itemSourceDescriptionFunctionId = itemSourceDescriptionFunctionId;
    }
}
