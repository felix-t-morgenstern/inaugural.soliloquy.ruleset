package inaugural.soliloquy.ruleset.gameconcepts;

import inaugural.soliloquy.tools.Check;
import soliloquy.specs.gamestate.entities.Character;
import soliloquy.specs.ruleset.entities.Element;
import soliloquy.specs.ruleset.gameconcepts.DamageResistanceCalculation;
import soliloquy.specs.ruleset.gameconcepts.StatisticCalculation;

public class DamageResistanceCalculationImpl implements DamageResistanceCalculation {
    private final StatisticCalculation STAT_CALCULATION;

    public DamageResistanceCalculationImpl(
            StatisticCalculation statisticCalculation) {
        STAT_CALCULATION = Check.ifNull(statisticCalculation, "statisticCalculation");
    }

    @Override
    public int calculateEffectiveChange(Character character, int baseAmount, Element element)
            throws IllegalStateException, IllegalArgumentException {
        Check.ifNull(character, "character");
        Check.ifDeleted(character, "character");
        Check.ifNull(element, "element");
        if (baseAmount >= 0) {
            return baseAmount;
        }
        var calculatedResistStat = STAT_CALCULATION.calculate(character, element.resistanceStatisticType());
        return (int)(baseAmount * (calculatedResistStat / 100f));
    }

    @Override
    public String getInterfaceName() {
        return DamageResistanceCalculation.class.getCanonicalName();
    }
}
