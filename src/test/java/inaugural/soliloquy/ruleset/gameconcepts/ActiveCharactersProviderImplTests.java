package inaugural.soliloquy.ruleset.gameconcepts;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import soliloquy.specs.common.factories.VariableCacheFactory;
import soliloquy.specs.common.infrastructure.VariableCache;
import soliloquy.specs.common.valueobjects.Pair;
import soliloquy.specs.gamestate.entities.Character;
import soliloquy.specs.gamestate.entities.CharacterStatistic;
import soliloquy.specs.gamestate.entities.EntityMembersOfType;
import soliloquy.specs.gamestate.entities.GameZone;
import soliloquy.specs.ruleset.entities.CharacterStaticStatisticType;
import soliloquy.specs.ruleset.entities.CharacterStatisticType;
import soliloquy.specs.ruleset.gameconcepts.ActiveCharactersProvider;
import soliloquy.specs.ruleset.gameconcepts.CharacterStatisticCalculation;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

import static inaugural.soliloquy.ruleset.api.CharacterData.CHARACTER_BASE_AP;
import static inaugural.soliloquy.ruleset.api.CharacterData.CHARACTER_IS_INACTIVE;
import static inaugural.soliloquy.ruleset.api.CharacterRoundData.ROUND_DATA_AP;
import static inaugural.soliloquy.ruleset.api.CharacterRoundData.ROUND_DATA_IMPULSE;
import static inaugural.soliloquy.ruleset.api.CharacterStaticStatistics.ALACRITY;
import static inaugural.soliloquy.ruleset.api.CharacterStaticStatistics.IMPULSE;
import static inaugural.soliloquy.tools.collections.Collections.listOf;
import static inaugural.soliloquy.tools.random.Random.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ActiveCharactersProviderImplTests {
    private final int CHARACTER_1_IMPULSE = randomInt();
    private final int CHARACTER_2_IMPULSE = randomIntWithInclusiveCeiling(CHARACTER_1_IMPULSE - 1);
    private final int CHARACTER_3_IMPULSE = randomIntWithInclusiveCeiling(CHARACTER_2_IMPULSE - 1);
    private final int CHARACTER_4_IMPULSE = randomIntWithInclusiveCeiling(CHARACTER_3_IMPULSE - 1);

    private final int CHARACTER_1_BASE_AP = randomInt();
    private final int CHARACTER_2_BASE_AP = randomInt();
    private final int CHARACTER_3_BASE_AP = randomInt();
    private final int CHARACTER_4_BASE_AP = randomInt();

    @Mock private CharacterStaticStatisticType mockImpulse;
    @Mock private CharacterStaticStatisticType mockAlacrity;

    @Mock private Function<String, CharacterStatisticType> mockGetStatType;

    @Mock private CharacterStatistic<CharacterStaticStatisticType> mockCharacter1Impulse;
    @Mock private CharacterStatistic<CharacterStaticStatisticType> mockCharacter2Impulse;
    @Mock private CharacterStatistic<CharacterStaticStatisticType> mockCharacter3Impulse;
    @Mock private CharacterStatistic<CharacterStaticStatisticType> mockCharacter4Impulse;

    @Mock private EntityMembersOfType<CharacterStaticStatisticType,
            CharacterStatistic<CharacterStaticStatisticType>, Character> mockCharacter1StaticStats;
    @Mock private EntityMembersOfType<CharacterStaticStatisticType,
            CharacterStatistic<CharacterStaticStatisticType>, Character> mockCharacter2StaticStats;
    @Mock private EntityMembersOfType<CharacterStaticStatisticType,
            CharacterStatistic<CharacterStaticStatisticType>, Character> mockCharacter3StaticStats;
    @Mock private EntityMembersOfType<CharacterStaticStatisticType,
            CharacterStatistic<CharacterStaticStatisticType>, Character> mockCharacter4StaticStats;

    @Mock private VariableCache mockCharacter1Data;
    @Mock private VariableCache mockCharacter2Data;
    @Mock private VariableCache mockCharacter3Data;
    @Mock private VariableCache mockCharacter4Data;

    @Mock private Character mockCharacter1;
    @Mock private Character mockCharacter2;
    @Mock private Character mockCharacter3;
    @Mock private Character mockCharacter4;

    @Mock private GameZone mockGameZone;

    @Mock private CharacterStatisticCalculation mockCharacterStatisticCalculation;

    @Mock private Supplier<Float> mockGetRandomFloat;

    @Mock private VariableCache mockCharacter1RoundData;
    @Mock private VariableCache mockCharacter2RoundData;
    @Mock private VariableCache mockCharacter3RoundData;
    @Mock private VariableCache mockCharacter4RoundData;

    @Mock private VariableCacheFactory mockCharacterRoundDataFactory;

    private ActiveCharactersProvider activeCharactersProvider;

    @BeforeEach
    void setUp() {
        mockImpulse = mock(CharacterStaticStatisticType.class);
        mockAlacrity = mock(CharacterStaticStatisticType.class);

        //noinspection unchecked
        mockGetStatType = (Function<String, CharacterStatisticType>) mock(Function.class);
        when(mockGetStatType.apply(IMPULSE)).thenReturn(mockImpulse);
        when(mockGetStatType.apply(ALACRITY)).thenReturn(mockAlacrity);

        //noinspection unchecked
        mockCharacter1StaticStats =
                (EntityMembersOfType<CharacterStaticStatisticType,
                        CharacterStatistic<CharacterStaticStatisticType>, Character>) mock(
                        EntityMembersOfType.class);
        when(mockCharacter1StaticStats.get(any())).thenReturn(mockCharacter1Impulse);
        //noinspection unchecked
        mockCharacter2StaticStats =
                (EntityMembersOfType<CharacterStaticStatisticType,
                        CharacterStatistic<CharacterStaticStatisticType>, Character>) mock(
                        EntityMembersOfType.class);
        when(mockCharacter2StaticStats.get(any())).thenReturn(mockCharacter2Impulse);
        //noinspection unchecked
        mockCharacter3StaticStats =
                (EntityMembersOfType<CharacterStaticStatisticType,
                        CharacterStatistic<CharacterStaticStatisticType>, Character>) mock(
                        EntityMembersOfType.class);
        when(mockCharacter3StaticStats.get(any())).thenReturn(mockCharacter3Impulse);
        //noinspection unchecked
        mockCharacter4StaticStats =
                (EntityMembersOfType<CharacterStaticStatisticType,
                        CharacterStatistic<CharacterStaticStatisticType>, Character>) mock(
                        EntityMembersOfType.class);
        when(mockCharacter4StaticStats.get(any())).thenReturn(mockCharacter4Impulse);

        mockCharacter1Data = mock(VariableCache.class);
        when(mockCharacter1Data.getVariable(CHARACTER_BASE_AP)).thenReturn(CHARACTER_1_BASE_AP);
        mockCharacter2Data = mock(VariableCache.class);
        when(mockCharacter2Data.getVariable(CHARACTER_BASE_AP)).thenReturn(CHARACTER_2_BASE_AP);
        mockCharacter3Data = mock(VariableCache.class);
        when(mockCharacter3Data.getVariable(CHARACTER_BASE_AP)).thenReturn(CHARACTER_3_BASE_AP);
        mockCharacter4Data = mock(VariableCache.class);
        when(mockCharacter4Data.getVariable(CHARACTER_BASE_AP)).thenReturn(CHARACTER_4_BASE_AP);

        mockCharacter1 = mock(Character.class);
        when(mockCharacter1.staticStatistics()).thenReturn(mockCharacter1StaticStats);
        when(mockCharacter1.data()).thenReturn(mockCharacter1Data);
        mockCharacter2 = mock(Character.class);
        when(mockCharacter2.staticStatistics()).thenReturn(mockCharacter2StaticStats);
        when(mockCharacter2.data()).thenReturn(mockCharacter2Data);
        mockCharacter3 = mock(Character.class);
        when(mockCharacter3.staticStatistics()).thenReturn(mockCharacter3StaticStats);
        when(mockCharacter3.data()).thenReturn(mockCharacter3Data);
        mockCharacter4 = mock(Character.class);
        when(mockCharacter4.staticStatistics()).thenReturn(mockCharacter4StaticStats);
        when(mockCharacter4.data()).thenReturn(mockCharacter4Data);

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

        mockCharacterStatisticCalculation = mock(CharacterStatisticCalculation.class);
        when(mockCharacterStatisticCalculation.calculate(mockCharacter1, mockImpulse))
                .thenReturn(Pair.of(CHARACTER_1_IMPULSE, new HashMap<>()));
        when(mockCharacterStatisticCalculation.calculate(mockCharacter2, mockImpulse))
                .thenReturn(Pair.of(CHARACTER_2_IMPULSE, new HashMap<>()));
        when(mockCharacterStatisticCalculation.calculate(mockCharacter3, mockImpulse))
                .thenReturn(Pair.of(CHARACTER_3_IMPULSE, new HashMap<>()));
        when(mockCharacterStatisticCalculation.calculate(mockCharacter4, mockImpulse))
                .thenReturn(Pair.of(CHARACTER_4_IMPULSE, new HashMap<>()));
        when(mockCharacterStatisticCalculation.calculate(any(), same(mockAlacrity)))
                .thenReturn(Pair.of(0, new HashMap<>()));

        //noinspection unchecked
        mockGetRandomFloat = (Supplier<Float>) mock(Supplier.class);
        when(mockGetRandomFloat.get()).thenReturn(0f);

        activeCharactersProvider =
                new ActiveCharactersProviderImpl(mockGetStatType, mockCharacterStatisticCalculation,
                        mockGetRandomFloat, mockCharacterRoundDataFactory);
    }

    @Test
    void testConstructorWithInvalidParams() {
        assertThrows(IllegalArgumentException.class,
                () -> new ActiveCharactersProviderImpl(null, mockCharacterStatisticCalculation,
                        mockGetRandomFloat, mockCharacterRoundDataFactory));
        assertThrows(IllegalArgumentException.class,
                () -> new ActiveCharactersProviderImpl(mockGetStatType, null, mockGetRandomFloat,
                        mockCharacterRoundDataFactory));
        assertThrows(IllegalArgumentException.class,
                () -> new ActiveCharactersProviderImpl(mockGetStatType,
                        mockCharacterStatisticCalculation, null, mockCharacterRoundDataFactory));
        assertThrows(IllegalArgumentException.class,
                () -> new ActiveCharactersProviderImpl(mockGetStatType,
                        mockCharacterStatisticCalculation, mockGetRandomFloat, null));
    }

    @Test
    void testGenerateInTurnOrder() {
        //noinspection unchecked
        var mockRepresentation = (Map<UUID, Character>) mock(Map.class);
        when(mockRepresentation.values()).thenReturn(
                listOf(mockCharacter1, mockCharacter2, mockCharacter3, mockCharacter4));
        when(mockGameZone.charactersRepresentation()).thenReturn(mockRepresentation);

        var activeCharacters = activeCharactersProvider.generateInTurnOrder(mockGameZone);

        assertNotNull(activeCharacters);
        assertEquals(4, activeCharacters.size());
        assertSame(mockCharacter1, activeCharacters.get(0).getItem1());
        assertSame(mockCharacter2, activeCharacters.get(1).getItem1());
        assertSame(mockCharacter3, activeCharacters.get(2).getItem1());
        assertSame(mockCharacter4, activeCharacters.get(3).getItem1());
        assertSame(mockCharacter1RoundData, activeCharacters.get(0).getItem2());
        assertSame(mockCharacter2RoundData, activeCharacters.get(1).getItem2());
        assertSame(mockCharacter3RoundData, activeCharacters.get(2).getItem2());
        assertSame(mockCharacter4RoundData, activeCharacters.get(3).getItem2());
        verify(mockGameZone, times(1)).charactersRepresentation();
        verify(mockCharacterStatisticCalculation, times(1)).calculate(mockCharacter1, mockImpulse);
        verify(mockCharacterStatisticCalculation, times(1)).calculate(mockCharacter1, mockAlacrity);
        verify(mockCharacterStatisticCalculation, times(1)).calculate(mockCharacter2, mockImpulse);
        verify(mockCharacterStatisticCalculation, times(1)).calculate(mockCharacter2, mockAlacrity);
        verify(mockCharacterStatisticCalculation, times(1)).calculate(mockCharacter3, mockImpulse);
        verify(mockCharacterStatisticCalculation, times(1)).calculate(mockCharacter3, mockAlacrity);
        verify(mockCharacterStatisticCalculation, times(1)).calculate(mockCharacter4, mockImpulse);
        verify(mockCharacterStatisticCalculation, times(1)).calculate(mockCharacter4, mockAlacrity);
        verify(mockCharacterRoundDataFactory, times(4)).make();
        verify(mockCharacter1RoundData, times(1))
                .setVariable(ROUND_DATA_IMPULSE, CHARACTER_1_IMPULSE);
        verify(mockCharacter2RoundData, times(1))
                .setVariable(ROUND_DATA_IMPULSE, CHARACTER_2_IMPULSE);
        verify(mockCharacter3RoundData, times(1))
                .setVariable(ROUND_DATA_IMPULSE, CHARACTER_3_IMPULSE);
        verify(mockCharacter4RoundData, times(1))
                .setVariable(ROUND_DATA_IMPULSE, CHARACTER_4_IMPULSE);
        verify(mockCharacter1RoundData, times(1)).setVariable(ROUND_DATA_AP, CHARACTER_1_BASE_AP);
        verify(mockCharacter2RoundData, times(1)).setVariable(ROUND_DATA_AP, CHARACTER_2_BASE_AP);
        verify(mockCharacter3RoundData, times(1)).setVariable(ROUND_DATA_AP, CHARACTER_3_BASE_AP);
        verify(mockCharacter4RoundData, times(1)).setVariable(ROUND_DATA_AP, CHARACTER_4_BASE_AP);
    }

    @Test
    void testImpulseTiesResolvedRandomly() {
        var tiedImpulse = randomInt();
        when(mockCharacterStatisticCalculation.calculate(mockCharacter1, mockImpulse))
                .thenReturn(Pair.of(tiedImpulse, new HashMap<>()));
        when(mockCharacterStatisticCalculation.calculate(mockCharacter2, mockImpulse))
                .thenReturn(Pair.of(tiedImpulse, new HashMap<>()));
        when(mockCharacterStatisticCalculation.calculate(mockCharacter3, mockImpulse))
                .thenReturn(Pair.of(tiedImpulse, new HashMap<>()));
        var character1TieBreaker = randomFloat();
        var character2TieBreaker = character1TieBreaker + 1f;
        var character3TieBreaker = character2TieBreaker + 1f;
        when(mockGetRandomFloat.get())
                .thenReturn(character1TieBreaker)
                .thenReturn(character2TieBreaker)
                .thenReturn(character3TieBreaker);
        //noinspection unchecked
        var mockRepresentation = (Map<UUID, Character>) mock(Map.class);
        when(mockRepresentation.values()).thenReturn(
                listOf(mockCharacter1, mockCharacter2, mockCharacter3));
        when(mockGameZone.charactersRepresentation()).thenReturn(mockRepresentation);

        var activeCharacters = activeCharactersProvider.generateInTurnOrder(mockGameZone);

        assertNotNull(activeCharacters);
        assertEquals(3, activeCharacters.size());
        assertSame(mockCharacter3, activeCharacters.get(0).getItem1());
        assertSame(mockCharacter2, activeCharacters.get(1).getItem1());
        assertSame(mockCharacter1, activeCharacters.get(2).getItem1());
        assertSame(mockCharacter3RoundData, activeCharacters.get(0).getItem2());
        assertSame(mockCharacter2RoundData, activeCharacters.get(1).getItem2());
        assertSame(mockCharacter1RoundData, activeCharacters.get(2).getItem2());
        verify(mockGetRandomFloat, times(3)).get();
    }

    @Test
    void testGetBonusApFromAlacrityOfTier1() {
        var character1Alacrity = 3;
        var character2Alacrity = 3;
        var belowCharacter1Alacrity = 0.25f;
        var aboveCharacter2Alacrity = 0.5f;
        when(mockCharacterStatisticCalculation.calculate(mockCharacter1, mockAlacrity))
                .thenReturn(Pair.of(character1Alacrity, new HashMap<>()));
        when(mockCharacterStatisticCalculation.calculate(mockCharacter2, mockAlacrity))
                .thenReturn(Pair.of(character2Alacrity, new HashMap<>()));
        when(mockGetRandomFloat.get())
                .thenReturn(belowCharacter1Alacrity)
                .thenReturn(aboveCharacter2Alacrity);
        //noinspection unchecked
        var mockRepresentation = (Map<UUID, Character>) mock(Map.class);
        when(mockRepresentation.values()).thenReturn(listOf(mockCharacter1, mockCharacter2));
        when(mockGameZone.charactersRepresentation()).thenReturn(mockRepresentation);

        activeCharactersProvider.generateInTurnOrder(mockGameZone);

        verify(mockCharacter1RoundData, times(1)).setVariable(ROUND_DATA_AP,
                CHARACTER_1_BASE_AP + 1);
        verify(mockCharacter2RoundData, times(1)).setVariable(ROUND_DATA_AP, CHARACTER_2_BASE_AP);
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
        when(mockCharacterStatisticCalculation.calculate(mockCharacter1, mockAlacrity))
                .thenReturn(Pair.of(character1Alacrity, new HashMap<>()));
        when(mockCharacterStatisticCalculation.calculate(mockCharacter2, mockAlacrity))
                .thenReturn(Pair.of(character2Alacrity, new HashMap<>()));
        when(mockGetRandomFloat.get())
                .thenReturn(belowCharacter1AlacrityTier1)
                .thenReturn(belowCharacter1AlacrityTier2)
                .thenReturn(aboveCharacter2AlacrityTier1)
                .thenReturn(aboveCharacter2AlacrityTier2);
        //noinspection unchecked
        var mockRepresentation = (Map<UUID, Character>) mock(Map.class);
        when(mockRepresentation.values()).thenReturn(listOf(mockCharacter1, mockCharacter2));
        when(mockGameZone.charactersRepresentation()).thenReturn(mockRepresentation);

        activeCharactersProvider.generateInTurnOrder(mockGameZone);

        verify(mockCharacter1RoundData, times(1)).setVariable(ROUND_DATA_AP,
                CHARACTER_1_BASE_AP + 2);
        verify(mockCharacter2RoundData, times(1)).setVariable(ROUND_DATA_AP,
                CHARACTER_2_BASE_AP + 1);
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
        when(mockCharacterStatisticCalculation.calculate(mockCharacter1, mockAlacrity))
                .thenReturn(Pair.of(characters1And2Alacrity, new HashMap<>()));
        when(mockCharacterStatisticCalculation.calculate(mockCharacter2, mockAlacrity))
                .thenReturn(Pair.of(characters1And2Alacrity, new HashMap<>()));
        // The initial thresholds of 0f are for +8 bonus AP, so the threshold can be tested at
        // level 9 for both characters
        when(mockGetRandomFloat.get())
                .thenReturn(0f)
                .thenReturn(threshold)
                .thenReturn(0f)
                .thenReturn(threshold + 0.001f);
        //noinspection unchecked
        var mockRepresentation = (Map<UUID, Character>) mock(Map.class);
        when(mockRepresentation.values()).thenReturn(listOf(mockCharacter1, mockCharacter2));
        when(mockGameZone.charactersRepresentation()).thenReturn(mockRepresentation);

        activeCharactersProvider.generateInTurnOrder(mockGameZone);

        verify(mockCharacter1RoundData, times(1)).setVariable(ROUND_DATA_AP,
                CHARACTER_1_BASE_AP + 10);
        verify(mockCharacter2RoundData, times(1)).setVariable(ROUND_DATA_AP,
                CHARACTER_2_BASE_AP + 9);
    }

    @Test
    void testExcludeInactiveCharacters() {
        when(mockCharacter1Data.getVariable(CHARACTER_IS_INACTIVE)).thenReturn(true);
        when(mockCharacter2Data.getVariable(CHARACTER_IS_INACTIVE)).thenReturn(null);
        when(mockCharacter3Data.getVariable(CHARACTER_IS_INACTIVE)).thenReturn(false);
        when(mockCharacterRoundDataFactory.make())
                .thenReturn(mockCharacter2RoundData)
                .thenReturn(mockCharacter3RoundData);
        //noinspection unchecked
        var mockRepresentation = (Map<UUID, Character>) mock(Map.class);
        when(mockRepresentation.values()).thenReturn(
                listOf(mockCharacter1, mockCharacter2, mockCharacter3));
        when(mockGameZone.charactersRepresentation()).thenReturn(mockRepresentation);

        var activeCharacters = activeCharactersProvider.generateInTurnOrder(mockGameZone);

        assertNotNull(activeCharacters);
        assertEquals(2, activeCharacters.size());
        assertSame(mockCharacter2, activeCharacters.get(0).getItem1());
        assertSame(mockCharacter3, activeCharacters.get(1).getItem1());
        assertSame(mockCharacter2RoundData, activeCharacters.get(0).getItem2());
        assertSame(mockCharacter3RoundData, activeCharacters.get(1).getItem2());
    }

    @Test
    void testGenerateInTurnOrderWithInvalidParams() {
        assertThrows(IllegalArgumentException.class,
                () -> activeCharactersProvider.generateInTurnOrder(null));
    }
}
