package inaugural.soliloquy.ruleset.definitions;

public class CharacterVariableStatisticTypeDefinition {
    public String id;
    public String name;
    public String pluralName;
    public String description;
    public String imageAssetSetId;
    public String[] defaultColorShifts;
    public String iconForCharacterFunctionId;
    public String alterActionId;
    public RoundEndEffectsOnCharacterDefinition effectsOnRoundEnd;
    public EffectsOnCharacterDefinition effectsOnTurnStart;
    public EffectsOnCharacterDefinition effectsOnTurnEnd;

    public CharacterVariableStatisticTypeDefinition(String id,
                                                    String name,
                                                    String pluralName,
                                                    String description,
                                                    String imageAssetSetId,
                                                    String[] defaultColorShifts,
                                                    String iconForCharacterFunctionId,
                                                    String alterActionId,
                                                    RoundEndEffectsOnCharacterDefinition effectsOnRoundEnd,
                                                    EffectsOnCharacterDefinition effectsOnTurnStart,
                                                    EffectsOnCharacterDefinition effectsOnTurnEnd) {
        this.id = id;
        this.name = name;
        this.pluralName = pluralName;
        this.description = description;
        this.imageAssetSetId = imageAssetSetId;
        this.defaultColorShifts = defaultColorShifts;
        this.iconForCharacterFunctionId = iconForCharacterFunctionId;
        this.alterActionId = alterActionId;
        this.effectsOnRoundEnd = effectsOnRoundEnd;
        this.effectsOnTurnStart = effectsOnTurnStart;
        this.effectsOnTurnEnd = effectsOnTurnEnd;
    }
}
