package inaugural.soliloquy.ruleset.gameconcepts;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import soliloquy.specs.common.factories.VariableCacheFactory;
import soliloquy.specs.common.infrastructure.VariableCache;
import soliloquy.specs.gamestate.entities.Character;
import soliloquy.specs.gamestate.entities.GameZone;
import soliloquy.specs.ruleset.entities.character.StaticStatisticType;
import soliloquy.specs.ruleset.entities.character.StatisticType;
import soliloquy.specs.ruleset.gameconcepts.ActiveCharactersProvider;
import soliloquy.specs.ruleset.gameconcepts.StatisticCalculation;

import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

import static inaugural.soliloquy.tools.collections.Collections.listOf;
import static inaugural.soliloquy.tools.random.Random.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ActiveCharactersProviderImplTests {
    private final String STATISTIC_COMBAT_ORDER = randomString();
    private final String STATISTIC_BONUS_AP = randomString();
    private final String CHARACTER_DATA_IS_INACTIVE = randomString();
    private final String CHARACTER_DATA_BASE_AP = randomString();
    private final String ROUND_DATA_COMBAT_PRIORITY = randomString();
    private final String ROUND_DATA_AP = randomString();

    private final int CHARACTER_1_IMPULSE = randomInt();
    private final int CHARACTER_2_IMPULSE = randomIntWithInclusiveCeiling(CHARACTER_1_IMPULSE - 1);
    private final int CHARACTER_3_IMPULSE = randomIntWithInclusiveCeiling(CHARACTER_2_IMPULSE - 1);
    private final int CHARACTER_4_IMPULSE = randomIntWithInclusiveCeiling(CHARACTER_3_IMPULSE - 1);

    private final int CHARACTER_1_BASE_AP = randomInt();
    private final int CHARACTER_2_BASE_AP = randomInt();
    private final int CHARACTER_3_BASE_AP = randomInt();
    private final int CHARACTER_4_BASE_AP = randomInt();

    @Mock private StaticStatisticType mockCombatOrder;
    @Mock private StaticStatisticType mockBonusAp;

    @Mock private Function<String, StatisticType> mockGetStatType;

    @Mock private VariableCache mockCharacter1Data;
    @Mock private VariableCache mockCharacter2Data;
    @Mock private VariableCache mockCharacter3Data;
    @Mock private VariableCache mockCharacter4Data;

    @Mock private Character mockCharacter1;
    @Mock private Character mockCharacter2;
    @Mock private Character mockCharacter3;
    @Mock private Character mockCharacter4;

    @Mock private GameZone mockGameZone;

    @Mock private StatisticCalculation mockStatisticCalculation;

    @Mock private Supplier<Float> mockGetRandomFloat;

    @Mock private VariableCache mockCharacter1RoundData;
    @Mock private VariableCache mockCharacter2RoundData;
    @Mock private VariableCache mockCharacter3RoundData;
    @Mock private VariableCache mockCharacter4RoundData;

    @Mock private VariableCacheFactory mockCharacterRoundDataFactory;

    private ActiveCharactersProvider activeCharactersProvider;

    @BeforeEach
    void setUp() {
        mockCombatOrder = mock(StaticStatisticType.class);
        mockBonusAp = mock(StaticStatisticType.class);

        //noinspection unchecked
        mockGetStatType = (Function<String, StatisticType>) mock(Function.class);
        when(mockGetStatType.apply(STATISTIC_COMBAT_ORDER)).thenReturn(mockCombatOrder);
        when(mockGetStatType.apply(STATISTIC_BONUS_AP)).thenReturn(mockBonusAp);

        mockCharacter1Data = mock(VariableCache.class);
        when(mockCharacter1Data.getVariable(CHARACTER_DATA_BASE_AP)).thenReturn(
                CHARACTER_1_BASE_AP);
        mockCharacter2Data = mock(VariableCache.class);
        when(mockCharacter2Data.getVariable(CHARACTER_DATA_BASE_AP)).thenReturn(
                CHARACTER_2_BASE_AP);
        mockCharacter3Data = mock(VariableCache.class);
        when(mockCharacter3Data.getVariable(CHARACTER_DATA_BASE_AP)).thenReturn(
                CHARACTER_3_BASE_AP);
        mockCharacter4Data = mock(VariableCache.class);
        when(mockCharacter4Data.getVariable(CHARACTER_DATA_BASE_AP)).thenReturn(
                CHARACTER_4_BASE_AP);

        mockGameZone = mock(GameZone.class);

        mockCharacter1RoundData = mock(VariableCache.class);
        mockCharacter2RoundData = mock(VariableCache.class);
        mockCharacter3RoundData = mock(VariableCache.class);
        mockCharacter4RoundData = mock(VariableCache.class);

        mockCharacterRoundDataFactory = mock(VariableCacheFactory.class);
        when(mockCharacterRoundDataFactory.make())
                .thenReturn(mockCharacter1RoundData)
                .thenReturn(mockCharacter2RoundData)
                .thenReturn(mockCharacter3RoundData)
                .thenReturn(mockCharacter4RoundData);

        mockCharacter1 = mock(Character.class);
        when(mockCharacter1.data()).thenReturn(mockCharacter1Data);
        mockCharacter2 = mock(Character.class);
        when(mockCharacter2.data()).thenReturn(mockCharacter2Data);
        mockCharacter3 = mock(Character.class);
        when(mockCharacter3.data()).thenReturn(mockCharacter3Data);
        mockCharacter4 = mock(Character.class);
        when(mockCharacter4.data()).thenReturn(mockCharacter4Data);

        mockStatisticCalculation = mock(StatisticCalculation.class);
        when(mockStatisticCalculation.calculate(mockCharacter1, mockCombatOrder))
                .thenReturn(CHARACTER_1_IMPULSE);
        when(mockStatisticCalculation.calculate(mockCharacter2, mockCombatOrder))
                .thenReturn(CHARACTER_2_IMPULSE);
        when(mockStatisticCalculation.calculate(mockCharacter3, mockCombatOrder))
                .thenReturn(CHARACTER_3_IMPULSE);
        when(mockStatisticCalculation.calculate(mockCharacter4, mockCombatOrder))
                .thenReturn(CHARACTER_4_IMPULSE);
        when(mockStatisticCalculation.calculate(any(), same(mockBonusAp)))
                .thenReturn(0);

        //noinspection unchecked
        mockGetRandomFloat = (Supplier<Float>) mock(Supplier.class);
        when(mockGetRandomFloat.get()).thenReturn(0f);

        activeCharactersProvider =
                new ActiveCharactersProviderImpl(mockGetStatType, mockStatisticCalculation,
                        mockGetRandomFloat, mockCharacterRoundDataFactory, STATISTIC_COMBAT_ORDER,
                        STATISTIC_BONUS_AP, CHARACTER_DATA_IS_INACTIVE, CHARACTER_DATA_BASE_AP,
                        ROUND_DATA_COMBAT_PRIORITY, ROUND_DATA_AP);
    }

    @Test
    void testConstructorWithInvalidArgs() {
        assertThrows(IllegalArgumentException.class,
                () -> new ActiveCharactersProviderImpl(null, mockStatisticCalculation,
                        mockGetRandomFloat, mockCharacterRoundDataFactory, STATISTIC_COMBAT_ORDER,
                        STATISTIC_BONUS_AP, CHARACTER_DATA_IS_INACTIVE, CHARACTER_DATA_BASE_AP,
                        ROUND_DATA_COMBAT_PRIORITY, ROUND_DATA_AP));
        assertThrows(IllegalArgumentException.class,
                () -> new ActiveCharactersProviderImpl(mockGetStatType, null, mockGetRandomFloat,
                        mockCharacterRoundDataFactory, STATISTIC_COMBAT_ORDER, STATISTIC_BONUS_AP,
                        CHARACTER_DATA_IS_INACTIVE, CHARACTER_DATA_BASE_AP,
                        ROUND_DATA_COMBAT_PRIORITY, ROUND_DATA_AP));
        assertThrows(IllegalArgumentException.class,
                () -> new ActiveCharactersProviderImpl(mockGetStatType,
                        mockStatisticCalculation, null, mockCharacterRoundDataFactory,
                        STATISTIC_COMBAT_ORDER, STATISTIC_BONUS_AP, CHARACTER_DATA_IS_INACTIVE,
                        CHARACTER_DATA_BASE_AP, ROUND_DATA_COMBAT_PRIORITY, ROUND_DATA_AP));
        assertThrows(IllegalArgumentException.class,
                () -> new ActiveCharactersProviderImpl(mockGetStatType,
                        mockStatisticCalculation, mockGetRandomFloat, null,
                        STATISTIC_COMBAT_ORDER, STATISTIC_BONUS_AP, CHARACTER_DATA_IS_INACTIVE,
                        CHARACTER_DATA_BASE_AP, ROUND_DATA_COMBAT_PRIORITY, ROUND_DATA_AP));
        assertThrows(IllegalArgumentException.class,
                () -> new ActiveCharactersProviderImpl(mockGetStatType,
                        mockStatisticCalculation, mockGetRandomFloat,
                        mockCharacterRoundDataFactory, null, STATISTIC_BONUS_AP,
                        CHARACTER_DATA_IS_INACTIVE, CHARACTER_DATA_BASE_AP,
                        ROUND_DATA_COMBAT_PRIORITY, ROUND_DATA_AP));
        assertThrows(IllegalArgumentException.class,
                () -> new ActiveCharactersProviderImpl(mockGetStatType,
                        mockStatisticCalculation, mockGetRandomFloat,
                        mockCharacterRoundDataFactory, "", STATISTIC_BONUS_AP,
                        CHARACTER_DATA_IS_INACTIVE, CHARACTER_DATA_BASE_AP,
                        ROUND_DATA_COMBAT_PRIORITY, ROUND_DATA_AP));
        assertThrows(IllegalArgumentException.class,
                () -> new ActiveCharactersProviderImpl(mockGetStatType,
                        mockStatisticCalculation, mockGetRandomFloat,
                        mockCharacterRoundDataFactory, STATISTIC_COMBAT_ORDER, null,
                        CHARACTER_DATA_IS_INACTIVE, CHARACTER_DATA_BASE_AP,
                        ROUND_DATA_COMBAT_PRIORITY, ROUND_DATA_AP));
        assertThrows(IllegalArgumentException.class,
                () -> new ActiveCharactersProviderImpl(mockGetStatType,
                        mockStatisticCalculation, mockGetRandomFloat,
                        mockCharacterRoundDataFactory, STATISTIC_COMBAT_ORDER, "",
                        CHARACTER_DATA_IS_INACTIVE, CHARACTER_DATA_BASE_AP,
                        ROUND_DATA_COMBAT_PRIORITY, ROUND_DATA_AP));
        assertThrows(IllegalArgumentException.class,
                () -> new ActiveCharactersProviderImpl(mockGetStatType,
                        mockStatisticCalculation, mockGetRandomFloat,
                        mockCharacterRoundDataFactory, STATISTIC_COMBAT_ORDER, STATISTIC_BONUS_AP,
                        null, CHARACTER_DATA_BASE_AP, ROUND_DATA_COMBAT_PRIORITY, ROUND_DATA_AP));
        assertThrows(IllegalArgumentException.class,
                () -> new ActiveCharactersProviderImpl(mockGetStatType,
                        mockStatisticCalculation, mockGetRandomFloat,
                        mockCharacterRoundDataFactory, STATISTIC_COMBAT_ORDER, STATISTIC_BONUS_AP,
                        "", CHARACTER_DATA_BASE_AP, ROUND_DATA_COMBAT_PRIORITY, ROUND_DATA_AP));
        assertThrows(IllegalArgumentException.class,
                () -> new ActiveCharactersProviderImpl(mockGetStatType,
                        mockStatisticCalculation, mockGetRandomFloat,
                        mockCharacterRoundDataFactory, STATISTIC_COMBAT_ORDER, STATISTIC_BONUS_AP,
                        CHARACTER_DATA_IS_INACTIVE, null, ROUND_DATA_COMBAT_PRIORITY,
                        ROUND_DATA_AP));
        assertThrows(IllegalArgumentException.class,
                () -> new ActiveCharactersProviderImpl(mockGetStatType,
                        mockStatisticCalculation, mockGetRandomFloat,
                        mockCharacterRoundDataFactory, STATISTIC_COMBAT_ORDER, STATISTIC_BONUS_AP,
                        CHARACTER_DATA_IS_INACTIVE, "", ROUND_DATA_COMBAT_PRIORITY, ROUND_DATA_AP));
        assertThrows(IllegalArgumentException.class,
                () -> new ActiveCharactersProviderImpl(mockGetStatType,
                        mockStatisticCalculation, mockGetRandomFloat,
                        mockCharacterRoundDataFactory, STATISTIC_COMBAT_ORDER, STATISTIC_BONUS_AP,
                        CHARACTER_DATA_IS_INACTIVE, CHARACTER_DATA_BASE_AP, null, ROUND_DATA_AP));
        assertThrows(IllegalArgumentException.class,
                () -> new ActiveCharactersProviderImpl(mockGetStatType,
                        mockStatisticCalculation, mockGetRandomFloat,
                        mockCharacterRoundDataFactory, STATISTIC_COMBAT_ORDER, STATISTIC_BONUS_AP,
                        CHARACTER_DATA_IS_INACTIVE, CHARACTER_DATA_BASE_AP, "", ROUND_DATA_AP));
        assertThrows(IllegalArgumentException.class,
                () -> new ActiveCharactersProviderImpl(mockGetStatType,
                        mockStatisticCalculation, mockGetRandomFloat,
                        mockCharacterRoundDataFactory, STATISTIC_COMBAT_ORDER, STATISTIC_BONUS_AP,
                        CHARACTER_DATA_IS_INACTIVE, CHARACTER_DATA_BASE_AP,
                        ROUND_DATA_COMBAT_PRIORITY, null));
        assertThrows(IllegalArgumentException.class,
                () -> new ActiveCharactersProviderImpl(mockGetStatType,
                        mockStatisticCalculation, mockGetRandomFloat,
                        mockCharacterRoundDataFactory, STATISTIC_COMBAT_ORDER, STATISTIC_BONUS_AP,
                        CHARACTER_DATA_IS_INACTIVE, CHARACTER_DATA_BASE_AP,
                        ROUND_DATA_COMBAT_PRIORITY, ""));
    }

    @Test
    void testGenerateInTurnOrder() {
        var mockRepresentation =
                mockRepresentation(mockCharacter1, mockCharacter2, mockCharacter3, mockCharacter4);
        when(mockGameZone.charactersRepresentation()).thenReturn(mockRepresentation);

        var activeCharacters = activeCharactersProvider.generateInTurnOrder(mockGameZone);

        assertNotNull(activeCharacters);
        assertEquals(4, activeCharacters.size());
        assertSame(mockCharacter1, activeCharacters.get(0).item1());
        assertSame(mockCharacter2, activeCharacters.get(1).item1());
        assertSame(mockCharacter3, activeCharacters.get(2).item1());
        assertSame(mockCharacter4, activeCharacters.get(3).item1());
        assertSame(mockCharacter1RoundData, activeCharacters.get(0).item2());
        assertSame(mockCharacter2RoundData, activeCharacters.get(1).item2());
        assertSame(mockCharacter3RoundData, activeCharacters.get(2).item2());
        assertSame(mockCharacter4RoundData, activeCharacters.get(3).item2());
        verify(mockGameZone).charactersRepresentation();
        verify(mockStatisticCalculation).calculate(mockCharacter1, mockCombatOrder);
        verify(mockStatisticCalculation).calculate(mockCharacter1, mockBonusAp);
        verify(mockStatisticCalculation).calculate(mockCharacter2, mockCombatOrder);
        verify(mockStatisticCalculation).calculate(mockCharacter2, mockBonusAp);
        verify(mockStatisticCalculation).calculate(mockCharacter3, mockCombatOrder);
        verify(mockStatisticCalculation).calculate(mockCharacter3, mockBonusAp);
        verify(mockStatisticCalculation).calculate(mockCharacter4, mockCombatOrder);
        verify(mockStatisticCalculation).calculate(mockCharacter4, mockBonusAp);
        verify(mockCharacterRoundDataFactory, times(4)).make();
        verify(mockCharacter1RoundData).setVariable(ROUND_DATA_COMBAT_PRIORITY, CHARACTER_1_IMPULSE);
        verify(mockCharacter2RoundData).setVariable(ROUND_DATA_COMBAT_PRIORITY, CHARACTER_2_IMPULSE);
        verify(mockCharacter3RoundData).setVariable(ROUND_DATA_COMBAT_PRIORITY, CHARACTER_3_IMPULSE);
        verify(mockCharacter4RoundData).setVariable(ROUND_DATA_COMBAT_PRIORITY, CHARACTER_4_IMPULSE);
        verify(mockCharacter1RoundData).setVariable(ROUND_DATA_AP, CHARACTER_1_BASE_AP);
        verify(mockCharacter2RoundData).setVariable(ROUND_DATA_AP, CHARACTER_2_BASE_AP);
        verify(mockCharacter3RoundData).setVariable(ROUND_DATA_AP, CHARACTER_3_BASE_AP);
        verify(mockCharacter4RoundData).setVariable(ROUND_DATA_AP, CHARACTER_4_BASE_AP);
    }

    @Test
    void testImpulseTiesResolvedRandomly() {
        var tiedImpulse = randomInt();
        when(mockStatisticCalculation.calculate(mockCharacter1, mockCombatOrder))
                .thenReturn(tiedImpulse);
        when(mockStatisticCalculation.calculate(mockCharacter2, mockCombatOrder))
                .thenReturn(tiedImpulse);
        when(mockStatisticCalculation.calculate(mockCharacter3, mockCombatOrder))
                .thenReturn(tiedImpulse);
        var character1TieBreaker = randomFloat();
        var character2TieBreaker = character1TieBreaker + 1f;
        var character3TieBreaker = character2TieBreaker + 1f;
        when(mockGetRandomFloat.get())
                .thenReturn(character1TieBreaker)
                .thenReturn(character2TieBreaker)
                .thenReturn(character3TieBreaker);
        var mockRepresentation = mockRepresentation(mockCharacter1, mockCharacter2, mockCharacter3);
        when(mockGameZone.charactersRepresentation()).thenReturn(mockRepresentation);

        var activeCharacters = activeCharactersProvider.generateInTurnOrder(mockGameZone);

        assertNotNull(activeCharacters);
        assertEquals(3, activeCharacters.size());
        assertSame(mockCharacter3, activeCharacters.get(0).item1());
        assertSame(mockCharacter2, activeCharacters.get(1).item1());
        assertSame(mockCharacter1, activeCharacters.get(2).item1());
        assertSame(mockCharacter3RoundData, activeCharacters.get(0).item2());
        assertSame(mockCharacter2RoundData, activeCharacters.get(1).item2());
        assertSame(mockCharacter1RoundData, activeCharacters.get(2).item2());
        verify(mockGetRandomFloat, times(3)).get();
    }

    @Test
    void testGetBonusApFromAlacrityOfTier1() {
        var character1Alacrity = 3;
        var character2Alacrity = 3;
        var belowCharacter1Alacrity = 0.25f;
        var aboveCharacter2Alacrity = 0.5f;
        when(mockStatisticCalculation.calculate(mockCharacter1, mockBonusAp))
                .thenReturn(character1Alacrity);
        when(mockStatisticCalculation.calculate(mockCharacter2, mockBonusAp))
                .thenReturn(character2Alacrity);
        when(mockGetRandomFloat.get())
                .thenReturn(belowCharacter1Alacrity)
                .thenReturn(aboveCharacter2Alacrity);
        var mockRepresentation = mockRepresentation(mockCharacter1, mockCharacter2);
        when(mockGameZone.charactersRepresentation()).thenReturn(mockRepresentation);

        activeCharactersProvider.generateInTurnOrder(mockGameZone);

        verify(mockCharacter1RoundData).setVariable(ROUND_DATA_AP, CHARACTER_1_BASE_AP + 1);
        verify(mockCharacter2RoundData).setVariable(ROUND_DATA_AP, CHARACTER_2_BASE_AP);
        verify(mockGetRandomFloat, times(2)).get();
    }

    @Test
    void testGetBonusApFromAlacrityOfTier2() {
        var character1Alacrity = 6;
        var character2Alacrity = 6;
        var belowCharacter1AlacrityTier1 = 0.5f;
        var belowCharacter1AlacrityTier2 = 0.125f;
        var aboveCharacter2AlacrityTier1 = 0.5f;
        var aboveCharacter2AlacrityTier2 = 0.25f;
        when(mockStatisticCalculation.calculate(mockCharacter1, mockBonusAp))
                .thenReturn(character1Alacrity);
        when(mockStatisticCalculation.calculate(mockCharacter2, mockBonusAp))
                .thenReturn(character2Alacrity);
        when(mockGetRandomFloat.get())
                .thenReturn(belowCharacter1AlacrityTier1)
                .thenReturn(belowCharacter1AlacrityTier2)
                .thenReturn(aboveCharacter2AlacrityTier1)
                .thenReturn(aboveCharacter2AlacrityTier2);
        var mockRepresentation = mockRepresentation(mockCharacter1, mockCharacter2);
        when(mockGameZone.charactersRepresentation()).thenReturn(mockRepresentation);

        activeCharactersProvider.generateInTurnOrder(mockGameZone);

        verify(mockCharacter1RoundData).setVariable(ROUND_DATA_AP, CHARACTER_1_BASE_AP + 2);
        verify(mockCharacter2RoundData).setVariable(ROUND_DATA_AP, CHARACTER_2_BASE_AP + 1);
        verify(mockGetRandomFloat, times(4)).get();
    }

    @Test
    void testGetBonusApFromAlacrityOfArbitrarilyHighTier() {
        var characters1And2Alacrity = 1312;
        var expectedRangeMinimum = 1024;
        var expectedRangeMaximum = expectedRangeMinimum * 4;
        var rangeWidth = expectedRangeMaximum - expectedRangeMinimum;
        var alacrityWithinRange = characters1And2Alacrity - expectedRangeMinimum;
        var threshold = alacrityWithinRange / (float) rangeWidth;
        when(mockStatisticCalculation.calculate(mockCharacter1, mockBonusAp))
                .thenReturn(characters1And2Alacrity);
        when(mockStatisticCalculation.calculate(mockCharacter2, mockBonusAp))
                .thenReturn(characters1And2Alacrity);
        // The initial thresholds of 0f are for +8 bonus AP, so the threshold can be tested at
        // level 9 for both characters
        when(mockGetRandomFloat.get())
                .thenReturn(0f)
                .thenReturn(threshold)
                .thenReturn(0f)
                .thenReturn(threshold + 0.001f);
        var mockRepresentation = mockRepresentation(mockCharacter1, mockCharacter2);
        when(mockGameZone.charactersRepresentation()).thenReturn(mockRepresentation);

        activeCharactersProvider.generateInTurnOrder(mockGameZone);

        verify(mockCharacter1RoundData).setVariable(ROUND_DATA_AP, CHARACTER_1_BASE_AP + 10);
        verify(mockCharacter2RoundData).setVariable(ROUND_DATA_AP, CHARACTER_2_BASE_AP + 9);
    }

    @Test
    void testExcludeInactiveCharacters() {
        when(mockCharacter1Data.getVariable(CHARACTER_DATA_IS_INACTIVE)).thenReturn(true);
        when(mockCharacter2Data.getVariable(CHARACTER_DATA_IS_INACTIVE)).thenReturn(null);
        when(mockCharacter3Data.getVariable(CHARACTER_DATA_IS_INACTIVE)).thenReturn(false);
        when(mockCharacterRoundDataFactory.make())
                .thenReturn(mockCharacter2RoundData)
                .thenReturn(mockCharacter3RoundData);
        var mockRepresentation = mockRepresentation(mockCharacter1, mockCharacter2, mockCharacter3);
        when(mockGameZone.charactersRepresentation()).thenReturn(mockRepresentation);

        var activeCharacters = activeCharactersProvider.generateInTurnOrder(mockGameZone);

        assertNotNull(activeCharacters);
        assertEquals(2, activeCharacters.size());
        assertSame(mockCharacter2, activeCharacters.get(0).item1());
        assertSame(mockCharacter3, activeCharacters.get(1).item1());
        assertSame(mockCharacter2RoundData, activeCharacters.get(0).item2());
        assertSame(mockCharacter3RoundData, activeCharacters.get(1).item2());
    }

    @Test
    void testGenerateInTurnOrderWithInvalidArgs() {
        assertThrows(IllegalArgumentException.class,
                () -> activeCharactersProvider.generateInTurnOrder(null));
    }

    private Map<UUID, Character> mockRepresentation(Character... characters) {
        //noinspection unchecked
        var mockRepresentation = (Map<UUID, Character>) mock(Map.class);
        var list = listOf(characters);
        when(mockRepresentation.values()).thenReturn(list);
        return mockRepresentation;
    }
}
