package inaugural.soliloquy.ruleset.gameconcepts;

import inaugural.soliloquy.tools.Check;
import soliloquy.specs.common.infrastructure.VariableCache;
import soliloquy.specs.common.valueobjects.Pair;
import soliloquy.specs.gamestate.entities.Character;
import soliloquy.specs.ruleset.entities.actonroundendandcharacterturn.EffectsCharacterOnRoundOrTurnChange;
import soliloquy.specs.ruleset.entities.actonroundendandcharacterturn.EffectsCharacterOnRoundOrTurnChange.EffectsOnCharacter;
import soliloquy.specs.ruleset.entities.character.StaticStatisticType;
import soliloquy.specs.ruleset.entities.character.VariableStatisticType;
import soliloquy.specs.ruleset.entities.character.StatusEffectType;
import soliloquy.specs.ruleset.gameconcepts.StatisticMagnitudeEffectCalculation;
import soliloquy.specs.ruleset.gameconcepts.TurnHandling;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import static inaugural.soliloquy.tools.Tools.orderByPriority;
import static inaugural.soliloquy.tools.collections.Collections.mapOf;

public class TurnHandlingImpl implements TurnHandling {
    private final StatisticMagnitudeEffectCalculation EFFECT_CALCULATION;
    private final Consumer<Pair<Character, VariableCache>> PASS_CONTROL_TO_PLAYER;
    private final List<VariableStatisticType> VARIABLE_STAT_TYPES;
    private final List<StaticStatisticType> STATIC_STAT_TYPES;

    public TurnHandlingImpl(StatisticMagnitudeEffectCalculation effectCalculation,
                            Consumer<Pair<Character, VariableCache>> passControlToPlayer,
                            List<VariableStatisticType> variableStatTypes,
                            List<StaticStatisticType> staticStatTypes) {
        EFFECT_CALCULATION = Check.ifNull(effectCalculation, "effectCalculation");
        PASS_CONTROL_TO_PLAYER = Check.ifNull(passControlToPlayer, "passControlToPlayer");
        VARIABLE_STAT_TYPES = Check.ifNull(variableStatTypes, "variableStatTypes");
        STATIC_STAT_TYPES = Check.ifNull(staticStatTypes, "staticStatTypes");
    }

    @Override
    public void runTurn(Character character, VariableCache turnData, boolean advancingRounds)
            throws IllegalArgumentException {
        runTurnPhase(character, EffectsCharacterOnRoundOrTurnChange::onTurnStart, advancingRounds);

        if (!advancingRounds) {
            if (character.getPlayerControlled()) {
                PASS_CONTROL_TO_PLAYER.accept(Pair.of(character, turnData));
            }
            else {
                character.getAIType().act(character, turnData);
            }
        }

        runTurnPhase(character, EffectsCharacterOnRoundOrTurnChange::onTurnEnd, advancingRounds);
    }

    private void runTurnPhase(Character character,
                              Function<EffectsCharacterOnRoundOrTurnChange, EffectsOnCharacter>
                                      getPhaseEffect,
                              boolean advancingRounds) {
        Map<EffectsOnCharacter, EffectsCharacterOnRoundOrTurnChange> phaseEffects = mapOf();
        character.statusEffects().representation().keySet().forEach((statusEffectType -> {
            var phaseEffect = getPhaseEffect.apply(statusEffectType);
            if (phaseEffect != null) {
                phaseEffects.put(phaseEffect, statusEffectType);
            }
        }));
        VARIABLE_STAT_TYPES.forEach(variableStatType -> {
            var phaseEffect = getPhaseEffect.apply(variableStatType);
            if (phaseEffect != null) {
                phaseEffects.put(phaseEffect, variableStatType);
            }
        });
        STATIC_STAT_TYPES.forEach(staticStatType -> {
            var phaseEffect = getPhaseEffect.apply(staticStatType);
            if (phaseEffect != null) {
                phaseEffects.put(phaseEffect, staticStatType);
            }
        });
        var orderedEffects = orderByPriority(phaseEffects.keySet());

        orderedEffects.forEach(effects -> runEffect(effects, phaseEffects.get(effects), character,
                advancingRounds));
    }

    private void runEffect(EffectsOnCharacter effect,
                           EffectsCharacterOnRoundOrTurnChange effectingType,
                           Character character,
                           boolean advancingRounds) {
        var numberOfMagnitudes = effect.magnitudes().size();
        var effectValues = new int[numberOfMagnitudes];
        var targetVariableStats = new VariableStatisticType[numberOfMagnitudes];
        for (var i = 0; i < numberOfMagnitudes; i++) {
            var magnitude = effect.magnitudes().get(i);
            targetVariableStats[i] = magnitude.effectedStatisticType();
            if (effectingType instanceof VariableStatisticType) {
                //noinspection unchecked
                effectValues[i] =
                        EFFECT_CALCULATION.getEffect((VariableStatisticType) effectingType,
                                magnitude, character);
            }
            else if (effectingType instanceof StaticStatisticType) {
                //noinspection unchecked
                effectValues[i] =
                        EFFECT_CALCULATION.getEffect((StaticStatisticType) effectingType,
                                magnitude, character);
            }
            else {
                //noinspection unchecked
                effectValues[i] =
                        EFFECT_CALCULATION.getEffect((StatusEffectType) effectingType, magnitude,
                                character);
            }
        }

        effect.accompanyEffect(effectValues, character, advancingRounds);

        for (var i = 0; i < numberOfMagnitudes; i++) {
            targetVariableStats[i].alter(character, effectValues[i]);
        }

        effect.otherEffects(effectValues, character, advancingRounds);
    }
}
