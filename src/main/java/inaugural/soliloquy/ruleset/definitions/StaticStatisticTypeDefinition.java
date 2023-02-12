package inaugural.soliloquy.ruleset.definitions;

public class StaticStatisticTypeDefinition {
    public String id;
    public String name;
    public String description;
    public String imageAssetSetId;
    public String[] defaultColorShifts;
    public RoundEndEffectsOnCharacterDefinition effectsOnRoundEnd;
    public EffectsOnCharacterDefinition effectsOnTurnStart;
    public EffectsOnCharacterDefinition effectsOnTurnEnd;

    public StaticStatisticTypeDefinition(String id,
                                         String name,
                                         String description,
                                         String imageAssetSetId,
                                         String[] defaultColorShifts,
                                         RoundEndEffectsOnCharacterDefinition effectsOnRoundEnd,
                                         EffectsOnCharacterDefinition effectsOnTurnStart,
                                         EffectsOnCharacterDefinition effectsOnTurnEnd) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.imageAssetSetId = imageAssetSetId;
        this.defaultColorShifts = defaultColorShifts;
        this.effectsOnRoundEnd = effectsOnRoundEnd;
        this.effectsOnTurnStart = effectsOnTurnStart;
        this.effectsOnTurnEnd = effectsOnTurnEnd;
    }
}
