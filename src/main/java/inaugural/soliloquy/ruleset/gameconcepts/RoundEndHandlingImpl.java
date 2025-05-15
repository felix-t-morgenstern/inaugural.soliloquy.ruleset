package inaugural.soliloquy.ruleset.gameconcepts;

import inaugural.soliloquy.tools.Check;
import soliloquy.specs.common.valueobjects.Pair;
import soliloquy.specs.gamestate.entities.Character;
import soliloquy.specs.gamestate.entities.GameZone;
import soliloquy.specs.ruleset.entities.actonroundendandcharacterturn.EffectsCharacterOnRoundOrTurnChange;
import soliloquy.specs.ruleset.entities.actonroundendandcharacterturn.EffectsCharacterOnRoundOrTurnChange.RoundEndEffectsOnCharacter;
import soliloquy.specs.ruleset.entities.character.StaticStatisticType;
import soliloquy.specs.ruleset.entities.character.VariableStatisticType;
import soliloquy.specs.ruleset.entities.character.StatusEffectType;
import soliloquy.specs.ruleset.gameconcepts.ActiveCharactersProvider;
import soliloquy.specs.ruleset.gameconcepts.RoundEndHandling;
import soliloquy.specs.ruleset.gameconcepts.StatisticMagnitudeEffectCalculation;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import static inaugural.soliloquy.tools.Tools.orderByPriority;
import static inaugural.soliloquy.tools.collections.Collections.listOf;
import static inaugural.soliloquy.tools.collections.Collections.mapOf;
import static soliloquy.specs.common.valueobjects.Pair.pairOf;

public class RoundEndHandlingImpl implements RoundEndHandling {
    private final Supplier<GameZone> GET_CURRENT_GAME_ZONE;
    private final ActiveCharactersProvider ACTIVE_CHARACTERS_PROVIDER;
    private final List<RoundEndEffectsOnCharacter> ROUND_END_EFFECTS;
    private final Map<RoundEndEffectsOnCharacter, StatusEffectType> STATUS_EFFECT_TYPES;
    private final Map<RoundEndEffectsOnCharacter, VariableStatisticType>
            VARIABLE_STAT_TYPES;
    private final Map<RoundEndEffectsOnCharacter, StaticStatisticType>
            STATIC_STAT_TYPES;
    private final StatisticMagnitudeEffectCalculation MAGNITUDE_CALCULATION;

    public RoundEndHandlingImpl(Supplier<GameZone> getCurrentGameZone,
                                ActiveCharactersProvider activeCharactersProvider,
                                List<StatusEffectType> statusEffectTypes,
                                List<VariableStatisticType> variableStatTypes,
                                List<StaticStatisticType> staticStatTypes,
                                StatisticMagnitudeEffectCalculation magnitudeCalculation) {
        GET_CURRENT_GAME_ZONE = Check.ifNull(getCurrentGameZone, "getCurrentGameZone");
        ACTIVE_CHARACTERS_PROVIDER =
                Check.ifNull(activeCharactersProvider, "activeCharactersProvider");
        Check.ifNull(statusEffectTypes, "statusEffectTypes");
        Check.ifNull(variableStatTypes, "variableStatTypes");
        Check.ifNull(staticStatTypes, "staticStatTypes");
        statusEffectTypes.forEach(
                e -> Check.ifNull(e, "statusEffectType within statusEffectTypes"));
        variableStatTypes.forEach(
                e -> Check.ifNull(e, "variableStatType within variableStatTypes"));
        staticStatTypes.forEach(
                e -> Check.ifNull(e, "staticStatType within staticStatTypes"));
        List<EffectsCharacterOnRoundOrTurnChange> effectsCharacterOnRoundOrTurnChanges = listOf();
        effectsCharacterOnRoundOrTurnChanges.addAll(statusEffectTypes);
        effectsCharacterOnRoundOrTurnChanges.addAll(variableStatTypes);
        effectsCharacterOnRoundOrTurnChanges.addAll(staticStatTypes);
        STATUS_EFFECT_TYPES = mapOf();
        VARIABLE_STAT_TYPES = mapOf();
        STATIC_STAT_TYPES = mapOf();
        ROUND_END_EFFECTS = orderByPriority(effectsCharacterOnRoundOrTurnChanges
                .stream()
                .map(e -> {
                    var roundEnd = e.onRoundEnd();
                    if (e instanceof StatusEffectType) {
                        STATUS_EFFECT_TYPES.put(roundEnd, (StatusEffectType) e);
                    }
                    else if(e instanceof VariableStatisticType) {
                        VARIABLE_STAT_TYPES.put(roundEnd, (VariableStatisticType) e);
                    }
                    else {
                        STATIC_STAT_TYPES.put(roundEnd, (StaticStatisticType) e);
                    }
                    return roundEnd;
                })
                .filter(Objects::nonNull)
                .toList());
        MAGNITUDE_CALCULATION = Check.ifNull(magnitudeCalculation, "magnitudeCalculation");
    }

    @Override
    public void runRoundEnd(boolean advancingRounds) {
        var activeCharacters =
                ACTIVE_CHARACTERS_PROVIDER.generateInTurnOrder(GET_CURRENT_GAME_ZONE.get());

        // NB: The Big O value for this method will be ENORMOUS. I am facilitating what is
        //     probably WAY too large of a variety of behaviors.
        ROUND_END_EFFECTS.forEach(effect -> {
            List<Pair<int[], Character>> allEffects = listOf();
            activeCharacters.forEach(characterWithTurnData -> {
                var character = characterWithTurnData.FIRST;
                var calculatedEffects = new int[effect.magnitudes().size()];
                for (var i = 0; i < effect.magnitudes().size(); i++) {
                    int calculatedEffect;
                    if (STATUS_EFFECT_TYPES.containsKey(effect)) {
                        var statusEffectType = STATUS_EFFECT_TYPES.get(effect);
                        //noinspection unchecked
                        calculatedEffect = MAGNITUDE_CALCULATION.getEffect(statusEffectType,
                                effect.magnitudes().get(i), character);
                    }
                    else if (VARIABLE_STAT_TYPES.containsKey(effect)) {
                        var variableStatType = VARIABLE_STAT_TYPES.get(effect);
                        //noinspection unchecked
                        calculatedEffect = MAGNITUDE_CALCULATION.getEffect(variableStatType,
                                effect.magnitudes().get(i), character);
                    }
                    else {
                        var staticStatType = STATIC_STAT_TYPES.get(effect);
                        //noinspection unchecked
                        calculatedEffect = MAGNITUDE_CALCULATION.getEffect(staticStatType,
                                effect.magnitudes().get(i), character);
                    }
                    calculatedEffects[i] = calculatedEffect;
                }
                effect.accompanyEffect(calculatedEffects, character, advancingRounds);
                for (var i = 0; i < effect.magnitudes().size(); i++) {
                    effect.magnitudes().get(i).effectedStatisticType()
                            .alter(character, calculatedEffects[i]);
                }
                effect.otherEffects(calculatedEffects, character, advancingRounds);
                allEffects.add(pairOf(calculatedEffects, character));
            });
            effect.accompanyAllEffects(allEffects, advancingRounds);
        });
    }
}
