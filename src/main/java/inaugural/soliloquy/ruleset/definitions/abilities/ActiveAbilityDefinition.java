package inaugural.soliloquy.ruleset.definitions.abilities;

import soliloquy.specs.ruleset.entities.abilities.ActiveAbility;

public class ActiveAbilityDefinition {
    public String id;
    public String name;
    public String characterSourceDescriptionFunctionId;
    public String itemSourceDescriptionFunctionId;
    public String useFunctionId;
    public ActiveAbility.TargetType[] targetTypes;
    public String data;

    public ActiveAbilityDefinition(String id,
                                   String name,
                                   String characterSourceDescriptionFunctionId,
                                   String itemSourceDescriptionFunctionId,
                                   String useFunctionId,
                                   ActiveAbility.TargetType[] targetTypes,
                                   String data) {
        this.id = id;
        this.name = name;
        this.characterSourceDescriptionFunctionId = characterSourceDescriptionFunctionId;
        this.itemSourceDescriptionFunctionId = itemSourceDescriptionFunctionId;
        this.useFunctionId = useFunctionId;
        this.targetTypes = targetTypes;
        this.data = data;
    }
}
