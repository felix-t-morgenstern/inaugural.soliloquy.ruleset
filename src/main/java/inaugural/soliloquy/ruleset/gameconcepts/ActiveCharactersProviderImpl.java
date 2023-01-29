package inaugural.soliloquy.ruleset.gameconcepts;

import inaugural.soliloquy.tools.Check;
import soliloquy.specs.common.factories.VariableCacheFactory;
import soliloquy.specs.common.infrastructure.VariableCache;
import soliloquy.specs.common.valueobjects.Pair;
import soliloquy.specs.gamestate.entities.Character;
import soliloquy.specs.gamestate.entities.GameZone;
import soliloquy.specs.ruleset.entities.character.CharacterStatisticType;
import soliloquy.specs.ruleset.gameconcepts.ActiveCharactersProvider;
import soliloquy.specs.ruleset.gameconcepts.CharacterStatisticCalculation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import static inaugural.soliloquy.ruleset.constants.Constants.BONUS_AP_TIER_1_RANGE_MAXIMUM;
import static inaugural.soliloquy.ruleset.constants.Constants.BONUS_AP_TIER_1_RANGE_MINIMUM;
import static inaugural.soliloquy.tools.collections.Collections.listOf;

public class ActiveCharactersProviderImpl implements ActiveCharactersProvider {
    private final CharacterStatisticType IMPULSE;
    private final CharacterStatisticType ALACRITY;
    private final String CHARACTER_DATA_IS_INACTIVE;
    private final String CHARACTER_DATA_BASE_AP;
    private final String ROUND_DATA_COMBAT_PRIORITY;
    private final String ROUND_DATA_AP;
    private final CharacterStatisticCalculation CHARACTER_STATISTIC_CALCULATION;
    private final Supplier<Float> GET_RANDOM_FLOAT;
    private final VariableCacheFactory CHARACTER_ROUND_DATA_FACTORY;

    public ActiveCharactersProviderImpl(Function<String, CharacterStatisticType> getStatType,
                                        CharacterStatisticCalculation characterStatisticCalculation,
                                        Supplier<Float> getRandomFloat,
                                        VariableCacheFactory characterRoundDataFactory,
                                        String statisticCombatOrder,
                                        String statisticBonusAp,
                                        String characterDataIsInactive,
                                        String characterDataBaseAp,
                                        String roundDataCombatPriority,
                                        String roundDataAp) {
        Check.ifNull(getStatType, "getStatType");
        IMPULSE = getStatType.apply(
                Check.ifNullOrEmpty(statisticCombatOrder, "statisticCombatOrder"));
        ALACRITY = getStatType.apply(Check.ifNullOrEmpty(statisticBonusAp, "statisticBonusAp"));
        CHARACTER_DATA_IS_INACTIVE =
                Check.ifNullOrEmpty(characterDataIsInactive, "characterDataIsInactive");
        CHARACTER_DATA_BASE_AP = Check.ifNullOrEmpty(characterDataBaseAp, "characterDataBaseAp");
        ROUND_DATA_COMBAT_PRIORITY =
                Check.ifNullOrEmpty(roundDataCombatPriority, "roundDataCombatPriority");
        ROUND_DATA_AP = Check.ifNullOrEmpty(roundDataAp, "roundDataAp");
        CHARACTER_STATISTIC_CALCULATION =
                Check.ifNull(characterStatisticCalculation, "characterStatisticCalculation");
        GET_RANDOM_FLOAT = Check.ifNull(getRandomFloat, "getRandomFloat");
        CHARACTER_ROUND_DATA_FACTORY =
                Check.ifNull(characterRoundDataFactory, "characterRoundDataFactory");
    }

    @Override
    public List<Pair<Character, VariableCache>> generateInTurnOrder(GameZone gameZone)
            throws IllegalArgumentException {
        Check.ifNull(gameZone, "gameZone");

        var activeCharactersByImpulse =
                new HashMap<Integer, List<Pair<Character, VariableCache>>>();
        var impulses = new ArrayList<Integer>();

        gameZone.charactersRepresentation().values().forEach(character -> {
            var isInactive = character.data().getVariable(CHARACTER_DATA_IS_INACTIVE);
            if (isInactive != null && (Boolean) isInactive) {
                return;
            }

            var impulse = CHARACTER_STATISTIC_CALCULATION.calculate(character, IMPULSE).getItem1();
            var baseAp = (int) character.data().getVariable(CHARACTER_DATA_BASE_AP);
            var alacrity =
                    CHARACTER_STATISTIC_CALCULATION.calculate(character, ALACRITY).getItem1();
            var bonusApFromAlacrity = getBonusApFromAlacrity(alacrity);
            var roundAp = baseAp + bonusApFromAlacrity;

            var characterRoundData = CHARACTER_ROUND_DATA_FACTORY.make();
            characterRoundData.setVariable(ROUND_DATA_COMBAT_PRIORITY, impulse);
            characterRoundData.setVariable(ROUND_DATA_AP, roundAp);

            var characterWithRoundData = Pair.of(character, characterRoundData);

            if (activeCharactersByImpulse.containsKey(impulse)) {
                activeCharactersByImpulse.get(impulse).add(characterWithRoundData);
            }
            else {
                var activeCharactersWithImpulse = listOf(characterWithRoundData);
                activeCharactersByImpulse.put(impulse, activeCharactersWithImpulse);
                impulses.add(impulse);
            }
        });

        var activeCharacters = new ArrayList<Pair<Character, VariableCache>>();

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
        var tieBreakers = new ArrayList<Float>();
        var charactersByTieBreakers = new HashMap<Float, Pair<Character, VariableCache>>();

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
