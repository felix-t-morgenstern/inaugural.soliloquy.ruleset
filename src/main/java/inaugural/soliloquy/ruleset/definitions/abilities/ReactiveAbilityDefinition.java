package inaugural.soliloquy.ruleset.definitions.abilities;

public class ReactiveAbilityDefinition {
    public String characterSourceDescriptionFunctionId;
    public String itemSourceDescriptionFunctionId;
    public Integer priority;
    public String[] fireEvents;
    public String firesAgainstAbilityFunctionId;
    public String reactToAbilityFunctionId;
    public String reactToEventFunctionId;

    public ReactiveAbilityDefinition(String characterSourceDescriptionFunctionId,
                                     String itemSourceDescriptionFunctionId,
                                     Integer priority,
                                     String[] fireEvents,
                                     String firesAgainstAbilityFunctionId,
                                     String reactToAbilityFunctionId,
                                     String reactToEventFunctionId) {
        this.characterSourceDescriptionFunctionId = characterSourceDescriptionFunctionId;
        this.itemSourceDescriptionFunctionId = itemSourceDescriptionFunctionId;
        this.priority = priority;
        this.fireEvents = fireEvents;
        this.firesAgainstAbilityFunctionId = firesAgainstAbilityFunctionId;
        this.reactToAbilityFunctionId = reactToAbilityFunctionId;
        this.reactToEventFunctionId = reactToEventFunctionId;
    }
}
