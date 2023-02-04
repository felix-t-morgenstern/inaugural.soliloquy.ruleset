package inaugural.soliloquy.ruleset.definitions.abilities;

public class ReactiveAbilityDefinition {
    public String characterSourceDescriptionFunctionId;
    public String itemSourceDescriptionFunctionId;
    public String firesAgainstEventFunctionId;
    public String firesAgainstAbilityFunctionId;
    public String reactToEventFunctionId;
    public String reactToAbilityFunctionId;
    public String data;

    public ReactiveAbilityDefinition(String characterSourceDescriptionFunctionId,
                                     String itemSourceDescriptionFunctionId,
                                     String firesAgainstEventFunctionId,
                                     String firesAgainstAbilityFunctionId,
                                     String reactToEventFunctionId,
                                     String reactToAbilityFunctionId,
                                     String data) {
        this.characterSourceDescriptionFunctionId = characterSourceDescriptionFunctionId;
        this.itemSourceDescriptionFunctionId = itemSourceDescriptionFunctionId;
        this.firesAgainstEventFunctionId = firesAgainstEventFunctionId;
        this.firesAgainstAbilityFunctionId = firesAgainstAbilityFunctionId;
        this.reactToEventFunctionId = reactToEventFunctionId;
        this.reactToAbilityFunctionId = reactToAbilityFunctionId;
        this.data = data;
    }
}
