package inaugural.soliloquy.ruleset.gameconcepts;

import inaugural.soliloquy.tools.Check;
import soliloquy.specs.gamestate.entities.Character;
import soliloquy.specs.ruleset.entities.Element;
import soliloquy.specs.ruleset.entities.character.StatusEffectType;
import soliloquy.specs.ruleset.gameconcepts.StatisticCalculation;
import soliloquy.specs.ruleset.gameconcepts.StatusEffectResistanceCalculation;

public class StatusEffectResistanceCalculationImpl implements StatusEffectResistanceCalculation {
    private final StatisticCalculation STAT_CALCULATION;

    public StatusEffectResistanceCalculationImpl(StatisticCalculation statisticCalculation) {
        STAT_CALCULATION = Check.ifNull(statisticCalculation, "statisticCalculation");
    }

    @Override
    public int calculateEffectiveChange(Character character, StatusEffectType statusEffectType,
                                        int baseAmount, boolean stopAtZero, Element element)
            throws IllegalStateException, IllegalArgumentException {
        Check.ifNull(character, "character");
        Check.ifNull(statusEffectType, "statusEffectType");
        Check.ifNull(element, "element");

        var resistFromElement =
                STAT_CALCULATION.calculate(character, element.resistanceStatisticType());
        var resistFromStatusType =
                STAT_CALCULATION.calculate(character, statusEffectType.resistanceStatisticType());
        var resistance = Math.max(resistFromElement, resistFromStatusType);

        var effectiveChange = (int) (((100f - resistance) / 100f) * baseAmount);

        if (stopAtZero) {
            var statusEffectLevel =
                    character.statusEffects().getStatusEffectLevel(statusEffectType);
            if (statusEffectLevel < 0) {
                effectiveChange = Math.max(effectiveChange, -statusEffectLevel);
            }
            else if (statusEffectLevel > 0) {
                effectiveChange = Math.min(effectiveChange, -statusEffectLevel);
            }
        }

        return effectiveChange;
    }

    @Override
    public String getInterfaceName() {
        return StatusEffectResistanceCalculation.class.getCanonicalName();
    }
}
