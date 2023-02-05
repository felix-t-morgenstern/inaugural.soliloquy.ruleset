package inaugural.soliloquy.ruleset.definitions;

public class RoundEndEffectsOnCharacterDefinition extends EffectsOnCharacterDefinition{
    public String accompanyAllEffectsFunctionId;

    public RoundEndEffectsOnCharacterDefinition(int priority,
                                                MagnitudeForStatisticDefinition[] magnitudeForStatisticDefinitions,
                                                String accompanyEffectFunctionId,
                                                String otherEffectsFunctionId,
                                                String accompanyAllEffectsFunctionId) {
        super(priority, magnitudeForStatisticDefinitions, accompanyEffectFunctionId,
                otherEffectsFunctionId);
        this.accompanyAllEffectsFunctionId = accompanyAllEffectsFunctionId;
    }
}
