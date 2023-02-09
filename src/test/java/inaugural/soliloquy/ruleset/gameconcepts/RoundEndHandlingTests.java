package inaugural.soliloquy.ruleset.gameconcepts;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import soliloquy.specs.common.infrastructure.VariableCache;
import soliloquy.specs.common.valueobjects.Pair;
import soliloquy.specs.gamestate.entities.Character;
import soliloquy.specs.gamestate.entities.CharacterStatusEffects;
import soliloquy.specs.gamestate.entities.GameZone;
import soliloquy.specs.ruleset.entities.actonroundendandcharacterturn.EffectsCharacterOnRoundOrTurnChange.RoundEndEffectsOnCharacter;
import soliloquy.specs.ruleset.entities.actonroundendandcharacterturn.StatisticChangeMagnitude;
import soliloquy.specs.ruleset.entities.character.CharacterStaticStatisticType;
import soliloquy.specs.ruleset.entities.character.CharacterVariableStatisticType;
import soliloquy.specs.ruleset.entities.character.StatusEffectType;
import soliloquy.specs.ruleset.gameconcepts.ActiveCharactersProvider;
import soliloquy.specs.ruleset.gameconcepts.RoundEndHandling;
import soliloquy.specs.ruleset.gameconcepts.StatisticMagnitudeEffectCalculation;

import java.util.List;
import java.util.function.Supplier;

import static inaugural.soliloquy.tools.collections.Collections.listOf;
import static inaugural.soliloquy.tools.random.Random.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RoundEndHandlingTests {
    private final int CHARACTER_1_STATUS_EFFECT_MAGNITUDE = randomInt();
    private final int CHARACTER_1_VARIABLE_STAT_1_MAGNITUDE = randomInt();
    private final int CHARACTER_1_STATIC_STAT_MAGNITUDE = randomInt();
    private final int CHARACTER_2_STATUS_EFFECT_MAGNITUDE = randomInt();
    private final int CHARACTER_2_VARIABLE_STAT_1_MAGNITUDE = randomInt();
    private final int CHARACTER_2_STATIC_STAT_MAGNITUDE = randomInt();
    private final int CHARACTER_3_STATUS_EFFECT_MAGNITUDE = randomInt();
    private final int CHARACTER_3_VARIABLE_STAT_1_MAGNITUDE = randomInt();
    private final int CHARACTER_3_STATIC_STAT_MAGNITUDE = randomInt();

    private final int STATUS_EFFECT_ROUND_END_EFFECT_PRIORITY = randomInt();
    private final int VARIABLE_STAT_1_ROUND_END_EFFECT_PRIORITY =
            randomIntWithInclusiveFloor(STATUS_EFFECT_ROUND_END_EFFECT_PRIORITY + 1);
    private final int STATIC_STAT_ROUND_END_EFFECT_PRIORITY =
            randomIntWithInclusiveFloor(VARIABLE_STAT_1_ROUND_END_EFFECT_PRIORITY + 1);

    @Mock private GameZone mockGameZone;
    @Mock private Supplier<GameZone> mockGetCurrentGameZone;

    @Mock private CharacterStatusEffects mockCharacter1StatusEffects;
    @Mock private CharacterStatusEffects mockCharacter2StatusEffects;
    @Mock private CharacterStatusEffects mockCharacter3StatusEffects;

    @Mock private Character mockCharacter1;
    @Mock private Character mockCharacter2;
    @Mock private Character mockCharacter3;
    @Mock private VariableCache mockRoundData;
    @Mock private ActiveCharactersProvider mockActiveCharactersProvider;

    @Mock private CharacterVariableStatisticType mockTargetVariableStatType;
    @Mock private StatisticChangeMagnitude<Integer> mockStatusEffectRoundEndEffectMagnitude;
    @Mock private StatisticChangeMagnitude<Integer> mockVariableStat1RoundEndEffectMagnitude;
    @Mock private StatisticChangeMagnitude<Integer> mockStaticStatRoundEndEffectMagnitude;

    @Mock private RoundEndEffectsOnCharacter mockStatusEffectRoundEndEffects;
    @Mock private RoundEndEffectsOnCharacter mockVariableStat1RoundEndEffects;
    @Mock private RoundEndEffectsOnCharacter mockStaticStatRoundEndEffects;

    @Mock private StatusEffectType mockStatusEffectType;
    @Mock private CharacterVariableStatisticType mockVariableStatType1;
    @Mock private CharacterVariableStatisticType mockVariableStatType2;
    @Mock private CharacterStaticStatisticType mockStaticStatType;
    private List<StatusEffectType> statusEffectTypes;
    private List<CharacterVariableStatisticType> variableStatTypes;
    private List<CharacterStaticStatisticType> staticStatTypes;

    @Mock private StatisticMagnitudeEffectCalculation mockMagnitudeCalculation;

    private RoundEndHandling roundEndHandling;

    @BeforeEach
    void setUp() {
        mockGameZone = mock(GameZone.class);
        //noinspection unchecked
        mockGetCurrentGameZone = (Supplier<GameZone>) mock(Supplier.class);
        when(mockGetCurrentGameZone.get()).thenReturn(mockGameZone);

        mockCharacter1StatusEffects = mock(CharacterStatusEffects.class);
        mockCharacter2StatusEffects = mock(CharacterStatusEffects.class);
        mockCharacter3StatusEffects = mock(CharacterStatusEffects.class);

        mockCharacter1 = mock(Character.class);
        when(mockCharacter1.statusEffects()).thenReturn(mockCharacter1StatusEffects);
        mockCharacter2 = mock(Character.class);
        when(mockCharacter2.statusEffects()).thenReturn(mockCharacter2StatusEffects);
        mockCharacter3 = mock(Character.class);
        when(mockCharacter3.statusEffects()).thenReturn(mockCharacter3StatusEffects);

        mockRoundData = mock(VariableCache.class);

        mockActiveCharactersProvider = mock(ActiveCharactersProvider.class);
        when(mockActiveCharactersProvider.generateInTurnOrder(any())).thenReturn(
                listOf(Pair.of(mockCharacter1, mockRoundData),
                        Pair.of(mockCharacter2, mockRoundData),
                        Pair.of(mockCharacter3, mockRoundData)));

        mockTargetVariableStatType = mock(CharacterVariableStatisticType.class);

        //noinspection unchecked
        mockStatusEffectRoundEndEffectMagnitude =
                (StatisticChangeMagnitude<Integer>) mock(StatisticChangeMagnitude.class);
        when(mockStatusEffectRoundEndEffectMagnitude.effectedStatisticType())
                .thenReturn(mockTargetVariableStatType);
        //noinspection unchecked
        mockVariableStat1RoundEndEffectMagnitude =
                (StatisticChangeMagnitude<Integer>) mock(StatisticChangeMagnitude.class);
        when(mockVariableStat1RoundEndEffectMagnitude.effectedStatisticType())
                .thenReturn(mockTargetVariableStatType);
        //noinspection unchecked
        mockStaticStatRoundEndEffectMagnitude =
                (StatisticChangeMagnitude<Integer>) mock(StatisticChangeMagnitude.class);
        when(mockStaticStatRoundEndEffectMagnitude.effectedStatisticType())
                .thenReturn(mockTargetVariableStatType);

        mockStatusEffectRoundEndEffects = mock(RoundEndEffectsOnCharacter.class);
        when(mockStatusEffectRoundEndEffects.priority())
                .thenReturn(STATUS_EFFECT_ROUND_END_EFFECT_PRIORITY);
        when(mockStatusEffectRoundEndEffects.magnitudes()).thenReturn(
                listOf(mockStatusEffectRoundEndEffectMagnitude));

        mockVariableStat1RoundEndEffects = mock(RoundEndEffectsOnCharacter.class);
        when(mockVariableStat1RoundEndEffects.priority())
                .thenReturn(VARIABLE_STAT_1_ROUND_END_EFFECT_PRIORITY);
        when(mockVariableStat1RoundEndEffects.magnitudes()).thenReturn(
                listOf(mockVariableStat1RoundEndEffectMagnitude));

        mockStaticStatRoundEndEffects = mock(RoundEndEffectsOnCharacter.class);
        when(mockStaticStatRoundEndEffects.priority())
                .thenReturn(STATIC_STAT_ROUND_END_EFFECT_PRIORITY);
        when(mockStaticStatRoundEndEffects.magnitudes()).thenReturn(
                listOf(mockStaticStatRoundEndEffectMagnitude));

        mockStatusEffectType = mock(StatusEffectType.class);
        when(mockStatusEffectType.onRoundEnd()).thenReturn(mockStatusEffectRoundEndEffects);
        statusEffectTypes = listOf(mockStatusEffectType);

        mockVariableStatType1 = mock(CharacterVariableStatisticType.class);
        when(mockVariableStatType1.onRoundEnd()).thenReturn(mockVariableStat1RoundEndEffects);
        mockVariableStatType2 = mock(CharacterVariableStatisticType.class);
        when(mockVariableStatType2.onRoundEnd()).thenReturn(null);
        variableStatTypes = listOf(mockVariableStatType1, mockVariableStatType2);

        mockStaticStatType = mock(CharacterStaticStatisticType.class);
        when(mockStaticStatType.onRoundEnd()).thenReturn(mockStaticStatRoundEndEffects);
        staticStatTypes = listOf(mockStaticStatType);

        mockMagnitudeCalculation = mock(StatisticMagnitudeEffectCalculation.class);
        when(mockMagnitudeCalculation.getEffect(mockStatusEffectType,
                mockStatusEffectRoundEndEffectMagnitude, mockCharacter1))
                .thenReturn(CHARACTER_1_STATUS_EFFECT_MAGNITUDE);
        when(mockMagnitudeCalculation.getEffect(mockStatusEffectType,
                mockStatusEffectRoundEndEffectMagnitude, mockCharacter2))
                .thenReturn(CHARACTER_2_STATUS_EFFECT_MAGNITUDE);
        when(mockMagnitudeCalculation.getEffect(mockStatusEffectType,
                mockStatusEffectRoundEndEffectMagnitude, mockCharacter3))
                .thenReturn(CHARACTER_3_STATUS_EFFECT_MAGNITUDE);
        when(mockMagnitudeCalculation.getEffect(mockVariableStatType1,
                mockVariableStat1RoundEndEffectMagnitude, mockCharacter1))
                .thenReturn(CHARACTER_1_VARIABLE_STAT_1_MAGNITUDE);
        when(mockMagnitudeCalculation.getEffect(mockVariableStatType1,
                mockVariableStat1RoundEndEffectMagnitude, mockCharacter2))
                .thenReturn(CHARACTER_2_VARIABLE_STAT_1_MAGNITUDE);
        when(mockMagnitudeCalculation.getEffect(mockVariableStatType1,
                mockVariableStat1RoundEndEffectMagnitude, mockCharacter3))
                .thenReturn(CHARACTER_3_VARIABLE_STAT_1_MAGNITUDE);
        when(mockMagnitudeCalculation.getEffect(mockStaticStatType,
                mockStaticStatRoundEndEffectMagnitude, mockCharacter1))
                .thenReturn(CHARACTER_1_STATIC_STAT_MAGNITUDE);
        when(mockMagnitudeCalculation.getEffect(mockStaticStatType,
                mockStaticStatRoundEndEffectMagnitude, mockCharacter2))
                .thenReturn(CHARACTER_2_STATIC_STAT_MAGNITUDE);
        when(mockMagnitudeCalculation.getEffect(mockStaticStatType,
                mockStaticStatRoundEndEffectMagnitude, mockCharacter3))
                .thenReturn(CHARACTER_3_STATIC_STAT_MAGNITUDE);

        roundEndHandling =
                new RoundEndHandlingImpl(mockGetCurrentGameZone, mockActiveCharactersProvider,
                        statusEffectTypes, variableStatTypes, staticStatTypes,
                        mockMagnitudeCalculation);
    }

    @Test
    void testConstructorWithInvalidParams() {
        assertThrows(IllegalArgumentException.class,
                () -> new RoundEndHandlingImpl(null, mockActiveCharactersProvider,
                        statusEffectTypes, variableStatTypes, staticStatTypes,
                        mockMagnitudeCalculation));
        assertThrows(IllegalArgumentException.class,
                () -> new RoundEndHandlingImpl(mockGetCurrentGameZone, null, statusEffectTypes,
                        variableStatTypes, staticStatTypes, mockMagnitudeCalculation));
        assertThrows(IllegalArgumentException.class,
                () -> new RoundEndHandlingImpl(mockGetCurrentGameZone, mockActiveCharactersProvider,
                        null, variableStatTypes, staticStatTypes, mockMagnitudeCalculation));
        assertThrows(IllegalArgumentException.class,
                () -> new RoundEndHandlingImpl(mockGetCurrentGameZone, mockActiveCharactersProvider,
                        listOf((StatusEffectType) null), variableStatTypes, staticStatTypes,
                        mockMagnitudeCalculation));
        assertThrows(IllegalArgumentException.class,
                () -> new RoundEndHandlingImpl(mockGetCurrentGameZone, mockActiveCharactersProvider,
                        statusEffectTypes, null, staticStatTypes, mockMagnitudeCalculation));
        assertThrows(IllegalArgumentException.class,
                () -> new RoundEndHandlingImpl(mockGetCurrentGameZone, mockActiveCharactersProvider,
                        statusEffectTypes, listOf((CharacterVariableStatisticType) null),
                        staticStatTypes, mockMagnitudeCalculation));
        assertThrows(IllegalArgumentException.class,
                () -> new RoundEndHandlingImpl(mockGetCurrentGameZone, mockActiveCharactersProvider,
                        statusEffectTypes, null, staticStatTypes, mockMagnitudeCalculation));
        assertThrows(IllegalArgumentException.class,
                () -> new RoundEndHandlingImpl(mockGetCurrentGameZone, mockActiveCharactersProvider,
                        statusEffectTypes, variableStatTypes, null, mockMagnitudeCalculation));
        assertThrows(IllegalArgumentException.class,
                () -> new RoundEndHandlingImpl(mockGetCurrentGameZone, mockActiveCharactersProvider,
                        statusEffectTypes, variableStatTypes,
                        listOf((CharacterStaticStatisticType) null), mockMagnitudeCalculation));
        assertThrows(IllegalArgumentException.class,
                () -> new RoundEndHandlingImpl(mockGetCurrentGameZone, mockActiveCharactersProvider,
                        statusEffectTypes, variableStatTypes, staticStatTypes, null));
    }

    @Test
    void testRunRoundEnd() {
        var isAdvancingRounds = randomBoolean();

        roundEndHandling.runRoundEnd(isAdvancingRounds);

        var inOrder =
                Mockito.inOrder(mockStatusEffectType, mockStatusEffectType, mockVariableStatType1, mockStaticStatType,
                        mockGetCurrentGameZone, mockActiveCharactersProvider,
                        mockStatusEffectRoundEndEffects, mockVariableStat1RoundEndEffects, mockStaticStatRoundEndEffects,
                        mockMagnitudeCalculation, mockTargetVariableStatType);

        inOrder.verify(mockGetCurrentGameZone).get();
        inOrder.verify(mockActiveCharactersProvider).generateInTurnOrder(mockGameZone);

        inOrder.verify(mockMagnitudeCalculation)
                .getEffect(mockStaticStatType, mockStaticStatRoundEndEffectMagnitude,
                        mockCharacter1);
        inOrder.verify(mockStaticStatRoundEndEffects)
                .accompanyEffect(new int[]{CHARACTER_1_STATIC_STAT_MAGNITUDE}, mockCharacter1,
                        isAdvancingRounds);
        inOrder.verify(mockTargetVariableStatType)
                .alter(mockCharacter1, CHARACTER_1_STATIC_STAT_MAGNITUDE);
        inOrder.verify(mockStaticStatRoundEndEffects)
                .otherEffects(new int[]{CHARACTER_1_STATIC_STAT_MAGNITUDE}, mockCharacter1,
                        isAdvancingRounds);

        inOrder.verify(mockMagnitudeCalculation)
                .getEffect(mockStaticStatType, mockStaticStatRoundEndEffectMagnitude,
                        mockCharacter2);
        inOrder.verify(mockStaticStatRoundEndEffects)
                .accompanyEffect(new int[]{CHARACTER_2_STATIC_STAT_MAGNITUDE}, mockCharacter2,
                        isAdvancingRounds);
        inOrder.verify(mockTargetVariableStatType)
                .alter(mockCharacter2, CHARACTER_2_STATIC_STAT_MAGNITUDE);
        inOrder.verify(mockStaticStatRoundEndEffects)
                .otherEffects(new int[]{CHARACTER_2_STATIC_STAT_MAGNITUDE}, mockCharacter2,
                        isAdvancingRounds);

        inOrder.verify(mockMagnitudeCalculation)
                .getEffect(mockStaticStatType, mockStaticStatRoundEndEffectMagnitude,
                        mockCharacter3);
        inOrder.verify(mockStaticStatRoundEndEffects)
                .accompanyEffect(new int[]{CHARACTER_3_STATIC_STAT_MAGNITUDE}, mockCharacter3,
                        isAdvancingRounds);
        inOrder.verify(mockTargetVariableStatType)
                .alter(mockCharacter3, CHARACTER_3_STATIC_STAT_MAGNITUDE);
        inOrder.verify(mockStaticStatRoundEndEffects)
                .otherEffects(new int[]{CHARACTER_3_STATIC_STAT_MAGNITUDE}, mockCharacter3,
                        isAdvancingRounds);

        var staticStatRoundEndEffectsCaptor = ArgumentCaptor.forClass(List.class);
        //noinspection unchecked
        inOrder.verify(mockStaticStatRoundEndEffects)
                .accompanyAllEffects(staticStatRoundEndEffectsCaptor.capture(),
                        eq(isAdvancingRounds));

        //noinspection unchecked
        var staticStatAccompanyAllEffects =
                (List<Pair<int[], Character>>) staticStatRoundEndEffectsCaptor.getValue();
        assertEquals(3, staticStatAccompanyAllEffects.size());
        assertArrayEquals(new int[]{CHARACTER_1_STATIC_STAT_MAGNITUDE},
                staticStatAccompanyAllEffects.get(0).getItem1());
        assertSame(mockCharacter1, staticStatAccompanyAllEffects.get(0).getItem2());
        assertArrayEquals(new int[]{CHARACTER_2_STATIC_STAT_MAGNITUDE},
                staticStatAccompanyAllEffects.get(1).getItem1());
        assertSame(mockCharacter2, staticStatAccompanyAllEffects.get(1).getItem2());
        assertArrayEquals(new int[]{CHARACTER_3_STATIC_STAT_MAGNITUDE},
                staticStatAccompanyAllEffects.get(2).getItem1());
        assertSame(mockCharacter3, staticStatAccompanyAllEffects.get(2).getItem2());

        inOrder.verify(mockMagnitudeCalculation)
                .getEffect(mockVariableStatType1, mockVariableStat1RoundEndEffectMagnitude,
                        mockCharacter1);
        inOrder.verify(mockVariableStat1RoundEndEffects)
                .accompanyEffect(new int[]{CHARACTER_1_VARIABLE_STAT_1_MAGNITUDE}, mockCharacter1,
                        isAdvancingRounds);
        inOrder.verify(mockTargetVariableStatType)
                .alter(mockCharacter1, CHARACTER_1_VARIABLE_STAT_1_MAGNITUDE);
        inOrder.verify(mockVariableStat1RoundEndEffects)
                .otherEffects(new int[]{CHARACTER_1_VARIABLE_STAT_1_MAGNITUDE}, mockCharacter1,
                        isAdvancingRounds);

        inOrder.verify(mockMagnitudeCalculation)
                .getEffect(mockVariableStatType1, mockVariableStat1RoundEndEffectMagnitude,
                        mockCharacter2);
        inOrder.verify(mockVariableStat1RoundEndEffects)
                .accompanyEffect(new int[]{CHARACTER_2_VARIABLE_STAT_1_MAGNITUDE}, mockCharacter2,
                        isAdvancingRounds);
        inOrder.verify(mockTargetVariableStatType)
                .alter(mockCharacter2, CHARACTER_2_VARIABLE_STAT_1_MAGNITUDE);
        inOrder.verify(mockVariableStat1RoundEndEffects)
                .otherEffects(new int[]{CHARACTER_2_VARIABLE_STAT_1_MAGNITUDE}, mockCharacter2,
                        isAdvancingRounds);

        inOrder.verify(mockMagnitudeCalculation)
                .getEffect(mockVariableStatType1, mockVariableStat1RoundEndEffectMagnitude,
                        mockCharacter3);
        inOrder.verify(mockVariableStat1RoundEndEffects)
                .accompanyEffect(new int[]{CHARACTER_3_VARIABLE_STAT_1_MAGNITUDE}, mockCharacter3,
                        isAdvancingRounds);
        inOrder.verify(mockTargetVariableStatType)
                .alter(mockCharacter3, CHARACTER_3_VARIABLE_STAT_1_MAGNITUDE);
        inOrder.verify(mockVariableStat1RoundEndEffects)
                .otherEffects(new int[]{CHARACTER_3_VARIABLE_STAT_1_MAGNITUDE}, mockCharacter3,
                        isAdvancingRounds);

        var variableStat1RoundEndEffectsCaptor = ArgumentCaptor.forClass(List.class);
        //noinspection unchecked
        inOrder.verify(mockVariableStat1RoundEndEffects)
                .accompanyAllEffects(variableStat1RoundEndEffectsCaptor.capture(),
                        eq(isAdvancingRounds));

        //noinspection unchecked
        var variableStat1AccompanyAllEffects =
                (List<Pair<int[], Character>>) variableStat1RoundEndEffectsCaptor.getValue();
        assertEquals(3, variableStat1AccompanyAllEffects.size());
        assertArrayEquals(new int[]{CHARACTER_1_VARIABLE_STAT_1_MAGNITUDE},
                variableStat1AccompanyAllEffects.get(0).getItem1());
        assertSame(mockCharacter1, variableStat1AccompanyAllEffects.get(0).getItem2());
        assertArrayEquals(new int[]{CHARACTER_2_VARIABLE_STAT_1_MAGNITUDE},
                variableStat1AccompanyAllEffects.get(1).getItem1());
        assertSame(mockCharacter2, variableStat1AccompanyAllEffects.get(1).getItem2());
        assertArrayEquals(new int[]{CHARACTER_3_VARIABLE_STAT_1_MAGNITUDE},
                variableStat1AccompanyAllEffects.get(2).getItem1());
        assertSame(mockCharacter3, variableStat1AccompanyAllEffects.get(2).getItem2());

        inOrder.verify(mockMagnitudeCalculation)
                .getEffect(mockStatusEffectType, mockStatusEffectRoundEndEffectMagnitude,
                        mockCharacter1);
        inOrder.verify(mockStatusEffectRoundEndEffects)
                .accompanyEffect(new int[]{CHARACTER_1_STATUS_EFFECT_MAGNITUDE}, mockCharacter1,
                        isAdvancingRounds);
        inOrder.verify(mockTargetVariableStatType)
                .alter(mockCharacter1, CHARACTER_1_STATUS_EFFECT_MAGNITUDE);
        inOrder.verify(mockStatusEffectRoundEndEffects)
                .otherEffects(new int[]{CHARACTER_1_STATUS_EFFECT_MAGNITUDE}, mockCharacter1,
                        isAdvancingRounds);

        inOrder.verify(mockMagnitudeCalculation)
                .getEffect(mockStatusEffectType, mockStatusEffectRoundEndEffectMagnitude,
                        mockCharacter2);
        inOrder.verify(mockStatusEffectRoundEndEffects)
                .accompanyEffect(new int[]{CHARACTER_2_STATUS_EFFECT_MAGNITUDE}, mockCharacter2,
                        isAdvancingRounds);
        inOrder.verify(mockTargetVariableStatType)
                .alter(mockCharacter2, CHARACTER_2_STATUS_EFFECT_MAGNITUDE);
        inOrder.verify(mockStatusEffectRoundEndEffects)
                .otherEffects(new int[]{CHARACTER_2_STATUS_EFFECT_MAGNITUDE}, mockCharacter2,
                        isAdvancingRounds);

        inOrder.verify(mockMagnitudeCalculation)
                .getEffect(mockStatusEffectType, mockStatusEffectRoundEndEffectMagnitude,
                        mockCharacter3);
        inOrder.verify(mockStatusEffectRoundEndEffects)
                .accompanyEffect(new int[]{CHARACTER_3_STATUS_EFFECT_MAGNITUDE}, mockCharacter3,
                        isAdvancingRounds);
        inOrder.verify(mockTargetVariableStatType)
                .alter(mockCharacter3, CHARACTER_3_STATUS_EFFECT_MAGNITUDE);
        inOrder.verify(mockStatusEffectRoundEndEffects)
                .otherEffects(new int[]{CHARACTER_3_STATUS_EFFECT_MAGNITUDE}, mockCharacter3,
                        isAdvancingRounds);

        var statusEffectRoundEndEffectsCaptor = ArgumentCaptor.forClass(List.class);
        //noinspection unchecked
        inOrder.verify(mockStatusEffectRoundEndEffects)
                .accompanyAllEffects(statusEffectRoundEndEffectsCaptor.capture(),
                        eq(isAdvancingRounds));

        //noinspection unchecked
        var statusEffectAccompanyAllEffects =
                (List<Pair<int[], Character>>) statusEffectRoundEndEffectsCaptor.getValue();
        assertEquals(3, statusEffectAccompanyAllEffects.size());
        assertArrayEquals(new int[]{CHARACTER_1_STATUS_EFFECT_MAGNITUDE},
                statusEffectAccompanyAllEffects.get(0).getItem1());
        assertSame(mockCharacter1, statusEffectAccompanyAllEffects.get(0).getItem2());
        assertArrayEquals(new int[]{CHARACTER_2_STATUS_EFFECT_MAGNITUDE},
                statusEffectAccompanyAllEffects.get(1).getItem1());
        assertSame(mockCharacter2, statusEffectAccompanyAllEffects.get(1).getItem2());
        assertArrayEquals(new int[]{CHARACTER_3_STATUS_EFFECT_MAGNITUDE},
                statusEffectAccompanyAllEffects.get(2).getItem1());
        assertSame(mockCharacter3, statusEffectAccompanyAllEffects.get(2).getItem2());
    }
}
