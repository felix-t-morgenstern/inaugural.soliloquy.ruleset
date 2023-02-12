package inaugural.soliloquy.ruleset.gameconcepts;

import inaugural.soliloquy.tools.Check;
import soliloquy.specs.common.factories.VariableCacheFactory;
import soliloquy.specs.common.infrastructure.VariableCache;
import soliloquy.specs.common.valueobjects.Pair;
import soliloquy.specs.gamestate.entities.Character;
import soliloquy.specs.gamestate.entities.GameZone;
import soliloquy.specs.ruleset.entities.character.StatisticType;
import soliloquy.specs.ruleset.gameconcepts.ActiveCharactersProvider;
import soliloquy.specs.ruleset.gameconcepts.StatisticCalculation;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static inaugural.soliloquy.ruleset.constants.Constants.BONUS_AP_TIER_1_RANGE_MAXIMUM;
import static inaugural.soliloquy.ruleset.constants.Constants.BONUS_AP_TIER_1_RANGE_MINIMUM;
import static inaugural.soliloquy.tools.collections.Collections.listOf;
import static inaugural.soliloquy.tools.collections.Collections.mapOf;

public class ActiveCharactersProviderImpl implements ActiveCharactersProvider {
    private final StatisticType ROUND_PRIORITY_STAT;
    private final StatisticType AP_BONUS_STAT;
    private final String CHARACTER_DATA_IS_INACTIVE;
    private final String CHARACTER_DATA_BASE_AP;
    private final String ROUND_DATA_COMBAT_PRIORITY;
    private final String ROUND_DATA_AP;
    private final StatisticCalculation STATISTIC_CALCULATION;
    private final Supplier<Float> GET_RANDOM_FLOAT;
    private final VariableCacheFactory CHARACTER_ROUND_DATA_FACTORY;

    public ActiveCharactersProviderImpl(Function<String, StatisticType> getStatType,
                                        StatisticCalculation statisticCalculation,
                                        Supplier<Float> getRandomFloat,
                                        VariableCacheFactory characterRoundDataFactory,
                                        String roundPriorityStat,
                                        String bonusApStat,
                                        String characterDataIsInactive,
                                        String characterDataBaseAp,
                                        String roundDataCombatPriority,
                                        String roundDataAp) {
        Check.ifNull(getStatType, "getStatType");
        ROUND_PRIORITY_STAT =
                getStatType.apply(Check.ifNullOrEmpty(roundPriorityStat, "roundPriorityStat"));
        AP_BONUS_STAT = getStatType.apply(Check.ifNullOrEmpty(bonusApStat, "bonusApStat"));
        CHARACTER_DATA_IS_INACTIVE =
                Check.ifNullOrEmpty(characterDataIsInactive, "characterDataIsInactive");
        CHARACTER_DATA_BASE_AP = Check.ifNullOrEmpty(characterDataBaseAp, "characterDataBaseAp");
        ROUND_DATA_COMBAT_PRIORITY =
                Check.ifNullOrEmpty(roundDataCombatPriority, "roundDataCombatPriority");
        ROUND_DATA_AP = Check.ifNullOrEmpty(roundDataAp, "roundDataAp");
        STATISTIC_CALCULATION = Check.ifNull(statisticCalculation, "statisticCalculation");
        GET_RANDOM_FLOAT = Check.ifNull(getRandomFloat, "getRandomFloat");
        CHARACTER_ROUND_DATA_FACTORY =
                Check.ifNull(characterRoundDataFactory, "characterRoundDataFactory");
    }

    @Override
    public List<Pair<Character, VariableCache>> generateInTurnOrder(GameZone gameZone)
            throws IllegalArgumentException {
        Check.ifNull(gameZone, "gameZone");

        Map<Integer, List<Pair<Character, VariableCache>>> activeCharactersByImpulse = mapOf();
        List<Integer> impulses = listOf();

        gameZone.charactersRepresentation().values().forEach(character -> {
            var isInactive = character.data().getVariable(CHARACTER_DATA_IS_INACTIVE);
            if (isInactive != null && (Boolean) isInactive) {
                return;
            }

            var roundPriorityStatValue =
                    STATISTIC_CALCULATION.calculate(character, ROUND_PRIORITY_STAT);
            var baseAp = (int) character.data().getVariable(CHARACTER_DATA_BASE_AP);
            var bonusApStatValue = STATISTIC_CALCULATION.calculate(character, AP_BONUS_STAT);
            var bonusApFromAlacrity = getBonusApFromAlacrity(bonusApStatValue);
            var roundAp = baseAp + bonusApFromAlacrity;

            var characterRoundData = CHARACTER_ROUND_DATA_FACTORY.make();
            characterRoundData.setVariable(ROUND_DATA_COMBAT_PRIORITY, roundPriorityStatValue);
            characterRoundData.setVariable(ROUND_DATA_AP, roundAp);

            var characterWithRoundData = Pair.of(character, characterRoundData);

            if (activeCharactersByImpulse.containsKey(roundPriorityStatValue)) {
                activeCharactersByImpulse.get(roundPriorityStatValue).add(characterWithRoundData);
            }
            else {
                var activeCharactersWithImpulse = listOf(characterWithRoundData);
                activeCharactersByImpulse.put(roundPriorityStatValue, activeCharactersWithImpulse);
                impulses.add(roundPriorityStatValue);
            }
        });

        List<Pair<Character, VariableCache>> activeCharacters = listOf();

        impulses.sort(Collections.reverseOrder());

        impulses.forEach(impulse -> {
            var characters = activeCharactersByImpulse.get(impulse);
            if (characters.size() == 1) {
                activeCharacters.add(characters.get(0));
            }
            else {
                resolveImpulseTies(activeCharacters, characters);
            }
        });

        return activeCharacters;
    }

    private void resolveImpulseTies(List<Pair<Character, VariableCache>> activeCharacters,
                                    List<Pair<Character, VariableCache>> tiedCharacters) {
        List<Float> tieBreakers = listOf();
        Map<Float, Pair<Character, VariableCache>> charactersByTieBreakers = mapOf();

        tiedCharacters.forEach(character -> {
            var tieBreaker = GET_RANDOM_FLOAT.get();
            tieBreakers.add(tieBreaker);
            charactersByTieBreakers.put(tieBreaker, character);
        });

        tieBreakers.sort(Collections.reverseOrder());

        tieBreakers.forEach(
                tieBreaker -> activeCharacters.add(charactersByTieBreakers.get(tieBreaker)));
    }

    private int getBonusApFromAlacrity(int alacrity) {
        if (alacrity == 0) {
            return 0;
        }

        var bonusAp = 0;
        var rangeMin = BONUS_AP_TIER_1_RANGE_MINIMUM;
        var rangeMax = BONUS_AP_TIER_1_RANGE_MAXIMUM;

        while (alacrity > rangeMin) {
            if (alacrity >= rangeMax) {
                bonusAp++;
            }
            else {
                var threshold = GET_RANDOM_FLOAT.get();
                var pointInRange = (alacrity - rangeMin) / (float) (rangeMax - rangeMin);
                if (pointInRange >= threshold) {
                    bonusAp++;
                }
            }

            rangeMin = rangeMax / 2;
            rangeMax *= 2;
        }

        return bonusAp;
    }
}
