package inaugural.soliloquy.ruleset.definitions.abilities;

public class ActiveAbilityDefinition {
    public String characterSourceDescriptionFunctionId;
    public String itemSourceDescriptionFunctionId;
    public String useFunctionId;

    public ActiveAbilityDefinition(String characterSourceDescriptionFunctionId,
                                   String itemSourceDescriptionFunctionId,
                                   String useFunctionId) {
        this.characterSourceDescriptionFunctionId = characterSourceDescriptionFunctionId;
        this.itemSourceDescriptionFunctionId = itemSourceDescriptionFunctionId;
        this.useFunctionId = useFunctionId;
    }
}
