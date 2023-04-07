package inaugural.soliloquy.ruleset.definitions;

public class StatusEffectTypeDefinition {
    public String id;
    public String name;
    public boolean stopsAtZero;
    public String nameAtValueFunctionId;
    public IconForCharacterFunction[] iconForCharacterFunctions;
    public String alterValueActionId;
    public RoundEndEffectsOnCharacterDefinition effectsOnRoundEnd;
    public EffectsOnCharacterDefinition effectsOnTurnStart;
    public EffectsOnCharacterDefinition effectsOnTurnEnd;
    public String resistanceStatisticTypeId;

    public StatusEffectTypeDefinition(String id, String name, boolean stopsAtZero,
                                      String nameAtValueFunctionId,
                                      IconForCharacterFunction[] iconForCharacterFunctions,
                                      String alterValueActionId,
                                      RoundEndEffectsOnCharacterDefinition effectsOnRoundEnd,
                                      EffectsOnCharacterDefinition effectsOnTurnStart,
                                      EffectsOnCharacterDefinition effectsOnTurnEnd,
                                      String resistanceStatisticTypeId) {
        this.id = id;
        this.name = name;
        this.stopsAtZero = stopsAtZero;
        this.nameAtValueFunctionId = nameAtValueFunctionId;
        this.iconForCharacterFunctions = iconForCharacterFunctions;
        this.alterValueActionId = alterValueActionId;
        this.effectsOnRoundEnd = effectsOnRoundEnd;
        this.effectsOnTurnStart = effectsOnTurnStart;
        this.effectsOnTurnEnd = effectsOnTurnEnd;
        this.resistanceStatisticTypeId = resistanceStatisticTypeId;
    }

    public static class IconForCharacterFunction {
        public String iconType;
        public String functionId;

        public IconForCharacterFunction(String iconType, String functionId) {
            this.iconType = iconType;
            this.functionId = functionId;
        }
    }
}
