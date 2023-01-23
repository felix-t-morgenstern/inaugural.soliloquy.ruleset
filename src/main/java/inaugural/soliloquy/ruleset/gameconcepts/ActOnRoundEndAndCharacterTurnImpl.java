package inaugural.soliloquy.ruleset.gameconcepts;

import inaugural.soliloquy.tools.Check;
import soliloquy.specs.gamestate.entities.Character;
import soliloquy.specs.gamestate.entities.exceptions.EntityDeletedException;
import soliloquy.specs.ruleset.entities.actonroundendandcharacterturn.EffectsCharacterOnRoundOrTurnChange.EffectsOnCharacter;
import soliloquy.specs.ruleset.gameconcepts.ActOnRoundEndAndCharacterTurn;
import soliloquy.specs.ruleset.gameconcepts.CharacterStatisticCalculation;
import soliloquy.specs.ruleset.gameconcepts.StatisticMagnitudeEffectCalculation;

import java.util.ArrayList;

public class ActOnRoundEndAndCharacterTurnImpl implements ActOnRoundEndAndCharacterTurn {
    private final CharacterStatisticCalculation STATISTIC_CALCULATION;
    private final StatisticMagnitudeEffectCalculation EFFECT_CALCULATION;

    public ActOnRoundEndAndCharacterTurnImpl(
            CharacterStatisticCalculation statisticCalculation,
            StatisticMagnitudeEffectCalculation effectCalculation) {
        STATISTIC_CALCULATION = Check.ifNull(statisticCalculation, "statisticCalculation");
        EFFECT_CALCULATION = Check.ifNull(effectCalculation, "effectCalculation");
    }

    @Override
    public void roundEnd(Character character)
            throws IllegalArgumentException, EntityDeletedException {
        Check.ifNull(character, "character");
        Check.ifDeleted(character, "character");

        var effectsToFire = new ArrayList<EffectsOnCharacter>();

        var variableStats = character.variableStatistics();

        variableStats.forEach(v -> {

        });
    }

    @Override
    public void turnStart(Character character)
            throws IllegalArgumentException, EntityDeletedException {

    }

    @Override
    public void turnEnd(Character character)
            throws IllegalArgumentException, EntityDeletedException {

    }

    @Override
    public void affectOverMultipleTurns(Character character, int turns, boolean turnHasStarted,
                                        boolean turnHasEnded)
            throws IllegalArgumentException, EntityDeletedException {

    }
}
