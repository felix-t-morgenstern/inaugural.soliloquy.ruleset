package inaugural.soliloquy.ruleset.gameconcepts;

import inaugural.soliloquy.tools.Check;
import soliloquy.specs.common.valueobjects.Pair;
import soliloquy.specs.gamestate.entities.Character;
import soliloquy.specs.ruleset.entities.actonroundendandcharacterturn.EffectsCharacterOnRoundOrTurnChange;
import soliloquy.specs.ruleset.entities.actonroundendandcharacterturn.EffectsCharacterOnRoundOrTurnChange.EffectsOnCharacter;
import soliloquy.specs.ruleset.entities.character.CharacterVariableStatisticType;
import soliloquy.specs.ruleset.entities.character.StatusEffectType;
import soliloquy.specs.ruleset.gameconcepts.StatisticMagnitudeEffectCalculation;
import soliloquy.specs.ruleset.gameconcepts.TurnHandling;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import static inaugural.soliloquy.tools.Tools.orderByPriority;
import static inaugural.soliloquy.tools.collections.Collections.mapOf;

public class TurnHandlingImpl implements TurnHandling {
    private final StatisticMagnitudeEffectCalculation EFFECT_CALCULATION;
    private final Consumer<Character> PASS_CONTROL_TO_PLAYER;

    public TurnHandlingImpl(StatisticMagnitudeEffectCalculation effectCalculation,
                            Consumer<Character> passControlToPlayer) {
        EFFECT_CALCULATION = Check.ifNull(effectCalculation, "effectCalculation");
        PASS_CONTROL_TO_PLAYER = Check.ifNull(passControlToPlayer, "passControlToPlayer");
    }

    @Override
    public void runTurn(Character character) throws IllegalArgumentException {
        runTurnPhase(character, EffectsCharacterOnRoundOrTurnChange::onTurnStart);

        if (character.getPlayerControlled()) {
            PASS_CONTROL_TO_PLAYER.accept(character);
        }
        else {
            character.getAIType().act(character);
        }

        runTurnPhase(character, EffectsCharacterOnRoundOrTurnChange::onTurnEnd);
    }

    private void runTurnPhase(Character character,
                              Function<EffectsCharacterOnRoundOrTurnChange, EffectsOnCharacter>
                                      getPhaseEffect) {
        Map<EffectsOnCharacter, Pair<EffectsCharacterOnRoundOrTurnChange, Integer>>
                phaseEffects = mapOf();
        character.statusEffects().representation().forEach(((statusEffectType, level) -> {
            var phaseEffect = getPhaseEffect.apply(statusEffectType);
            if (phaseEffect != null) {
                phaseEffects.put(phaseEffect, Pair.of(statusEffectType, level));
            }
        }));
        character.variableStatistics().forEach(variableStat -> {
            var phaseEffect = getPhaseEffect.apply(variableStat.type());
            if (phaseEffect != null) {
                phaseEffects.put(phaseEffect,
                        Pair.of(variableStat.type(), variableStat.getCurrentValue()));
            }
        });
        var orderedEffects = orderByPriority(phaseEffects.keySet());

        orderedEffects.forEach(effects -> runEffect(effects, phaseEffects.get(effects).getItem1(),
                phaseEffects.get(effects).getItem2(), character));
    }

    private void runEffect(EffectsOnCharacter effect,
                           EffectsCharacterOnRoundOrTurnChange effectingType,
                           int effectingLevels,
                           Character character) {
        var effectValues = new int[effect.magnitudes().size()];
        for (var i = 0; i < effectValues.length; i++) {
            if (effectingType instanceof CharacterVariableStatisticType) {
                //noinspection unchecked
                effectValues[i] =
                        EFFECT_CALCULATION.getEffect((CharacterVariableStatisticType) effectingType,
                                effect.magnitudes().get(i), effectingLevels, character);
            }
            else {
                //noinspection unchecked
                effectValues[i] =
                        EFFECT_CALCULATION.getEffect((StatusEffectType) effectingType,
                                effect.magnitudes().get(i), effectingLevels, character);
            }
        }

        effect.accompanyEffect(effectValues, character);

        effect.otherEffects(effectValues, character);
    }
}
