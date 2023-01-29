package inaugural.soliloquy.ruleset.definitions;

public class StatisticChangeMagnitudeDefinition {
    public String variableStatType;
    public String elementId;
    public String effectType;
    public String amountType;
    public Integer[] perLevelValueRange;
    public Float[] perLevelPercentRange;
    public Integer[] absoluteValueRange;
    public Float[] absolutePercentRange;

    public StatisticChangeMagnitudeDefinition(String variableStatType,
                                              String elementId,
                                              String effectType,
                                              String amountType,
                                              Integer[] perLevelValueRange,
                                              Float[] perLevelPercentRange,
                                              Integer[] absoluteValueRange,
                                              Float[] absolutePercentRange) {
        this.variableStatType = variableStatType;
        this.elementId = elementId;
        this.effectType = effectType;
        this.amountType = amountType;
        this.perLevelValueRange = perLevelValueRange;
        this.perLevelPercentRange = perLevelPercentRange;
        this.absoluteValueRange = absoluteValueRange;
        this.absolutePercentRange = absolutePercentRange;
    }
}
