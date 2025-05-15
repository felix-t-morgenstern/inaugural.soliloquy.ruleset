package inaugural.soliloquy.ruleset.gameconcepts;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import soliloquy.specs.common.valueobjects.Pair;
import soliloquy.specs.gamestate.entities.Character;
import soliloquy.specs.gamestate.entities.CharacterStatusEffects;
import soliloquy.specs.gamestate.entities.GameZone;
import soliloquy.specs.ruleset.entities.actonroundendandcharacterturn.EffectsCharacterOnRoundOrTurnChange.RoundEndEffectsOnCharacter;
import soliloquy.specs.ruleset.entities.actonroundendandcharacterturn.StatisticChangeMagnitude;
import soliloquy.specs.ruleset.entities.character.StaticStatisticType;
import soliloquy.specs.ruleset.entities.character.VariableStatisticType;
import soliloquy.specs.ruleset.entities.character.StatusEffectType;
import soliloquy.specs.ruleset.gameconcepts.ActiveCharactersProvider;
import soliloquy.specs.ruleset.gameconcepts.RoundEndHandling;
import soliloquy.specs.ruleset.gameconcepts.StatisticMagnitudeEffectCalculation;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static inaugural.soliloquy.tools.collections.Collections.listOf;
import static inaugural.soliloquy.tools.random.Random.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static soliloquy.specs.common.valueobjects.Pair.pairOf;

@ExtendWith(MockitoExtension.class)
public class RoundEndHandlingTests {
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
    @Mock private Map<String, Object> mockRoundData;
    @Mock private ActiveCharactersProvider mockActiveCharactersProvider;

    @Mock private VariableStatisticType mockTargetVariableStatType;
    @Mock private StatisticChangeMagnitude<Integer> mockStatusEffectRoundEndEffectMagnitude;
    @Mock private StatisticChangeMagnitude<Integer> mockVariableStat1RoundEndEffectMagnitude;
    @Mock private StatisticChangeMagnitude<Integer> mockStaticStatRoundEndEffectMagnitude;

    @Mock private RoundEndEffectsOnCharacter mockStatusEffectRoundEndEffects;
    @Mock private RoundEndEffectsOnCharacter mockVariableStat1RoundEndEffects;
    @Mock private RoundEndEffectsOnCharacter mockStaticStatRoundEndEffects;

    @Mock private StatusEffectType mockStatusEffectType;
    @Mock private VariableStatisticType mockVariableStatType1;
    @Mock private VariableStatisticType mockVariableStatType2;
    @Mock private StaticStatisticType mockStaticStatType;
    private List<StatusEffectType> statusEffectTypes;
    private List<VariableStatisticType> variableStatTypes;
    private List<StaticStatisticType> staticStatTypes;

    @Mock private StatisticMagnitudeEffectCalculation mockMagnitudeCalculation;

    private RoundEndHandling roundEndHandling;

    @BeforeEach
    public void setUp() {
        lenient().when(mockGetCurrentGameZone.get()).thenReturn(mockGameZone);

        lenient().when(mockCharacter1.statusEffects()).thenReturn(mockCharacter1StatusEffects);
        lenient().when(mockCharacter2.statusEffects()).thenReturn(mockCharacter2StatusEffects);
        lenient().when(mockCharacter3.statusEffects()).thenReturn(mockCharacter3StatusEffects);

        lenient().when(mockActiveCharactersProvider.generateInTurnOrder(any())).thenReturn(
                listOf(pairOf(mockCharacter1, mockRoundData), pairOf(mockCharacter2, mockRoundData),
                        pairOf(mockCharacter3, mockRoundData)));

        lenient().when(mockStatusEffectRoundEndEffectMagnitude.effectedStatisticType())
                .thenReturn(mockTargetVariableStatType);
        lenient().when(mockVariableStat1RoundEndEffectMagnitude.effectedStatisticType())
                .thenReturn(mockTargetVariableStatType);
        lenient().when(mockStaticStatRoundEndEffectMagnitude.effectedStatisticType())
                .thenReturn(mockTargetVariableStatType);

        lenient().when(mockStatusEffectRoundEndEffects.priority())
                .thenReturn(STATUS_EFFECT_ROUND_END_EFFECT_PRIORITY);
        lenient().when(mockStatusEffectRoundEndEffects.magnitudes()).thenReturn(
                listOf(mockStatusEffectRoundEndEffectMagnitude));

        lenient().when(mockVariableStat1RoundEndEffects.priority())
                .thenReturn(VARIABLE_STAT_1_ROUND_END_EFFECT_PRIORITY);
        lenient().when(mockVariableStat1RoundEndEffects.magnitudes()).thenReturn(
                listOf(mockVariableStat1RoundEndEffectMagnitude));

        lenient().when(mockStaticStatRoundEndEffects.priority())
                .thenReturn(STATIC_STAT_ROUND_END_EFFECT_PRIORITY);
        lenient().when(mockStaticStatRoundEndEffects.magnitudes()).thenReturn(
                listOf(mockStaticStatRoundEndEffectMagnitude));

        lenient().when(mockStatusEffectType.onRoundEnd()).thenReturn(mockStatusEffectRoundEndEffects);
        statusEffectTypes = listOf(mockStatusEffectType);

        lenient().when(mockVariableStatType1.onRoundEnd()).thenReturn(mockVariableStat1RoundEndEffects);
        lenient().when(mockVariableStatType2.onRoundEnd()).thenReturn(null);
        variableStatTypes = listOf(mockVariableStatType1, mockVariableStatType2);

        lenient().when(mockStaticStatType.onRoundEnd()).thenReturn(mockStaticStatRoundEndEffects);
        staticStatTypes = listOf(mockStaticStatType);

        lenient().when(mockMagnitudeCalculation.getEffect(mockStatusEffectType,
                mockStatusEffectRoundEndEffectMagnitude, mockCharacter1))
                .thenReturn(CHARACTER_1_STATUS_EFFECT_MAGNITUDE);
        lenient().when(mockMagnitudeCalculation.getEffect(mockStatusEffectType,
                mockStatusEffectRoundEndEffectMagnitude, mockCharacter2))
                .thenReturn(CHARACTER_2_STATUS_EFFECT_MAGNITUDE);
        lenient().when(mockMagnitudeCalculation.getEffect(mockStatusEffectType,
                mockStatusEffectRoundEndEffectMagnitude, mockCharacter3))
                .thenReturn(CHARACTER_3_STATUS_EFFECT_MAGNITUDE);
        lenient().when(mockMagnitudeCalculation.getEffect(mockVariableStatType1,
                mockVariableStat1RoundEndEffectMagnitude, mockCharacter1))
                .thenReturn(CHARACTER_1_VARIABLE_STAT_1_MAGNITUDE);
        lenient().when(mockMagnitudeCalculation.getEffect(mockVariableStatType1,
                mockVariableStat1RoundEndEffectMagnitude, mockCharacter2))
                .thenReturn(CHARACTER_2_VARIABLE_STAT_1_MAGNITUDE);
        lenient().when(mockMagnitudeCalculation.getEffect(mockVariableStatType1,
                mockVariableStat1RoundEndEffectMagnitude, mockCharacter3))
                .thenReturn(CHARACTER_3_VARIABLE_STAT_1_MAGNITUDE);
        lenient().when(mockMagnitudeCalculation.getEffect(mockStaticStatType,
                mockStaticStatRoundEndEffectMagnitude, mockCharacter1))
                .thenReturn(CHARACTER_1_STATIC_STAT_MAGNITUDE);
        lenient().when(mockMagnitudeCalculation.getEffect(mockStaticStatType,
                mockStaticStatRoundEndEffectMagnitude, mockCharacter2))
                .thenReturn(CHARACTER_2_STATIC_STAT_MAGNITUDE);
        lenient().when(mockMagnitudeCalculation.getEffect(mockStaticStatType,
                mockStaticStatRoundEndEffectMagnitude, mockCharacter3))
                .thenReturn(CHARACTER_3_STATIC_STAT_MAGNITUDE);

        roundEndHandling =
                new RoundEndHandlingImpl(mockGetCurrentGameZone, mockActiveCharactersProvider,
                        statusEffectTypes, variableStatTypes, staticStatTypes,
                        mockMagnitudeCalculation);
    }

    @Test
    public void testConstructorWithInvalidArgs() {
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
                        statusEffectTypes, listOf((VariableStatisticType) null), staticStatTypes,
                        mockMagnitudeCalculation));
        assertThrows(IllegalArgumentException.class,
                () -> new RoundEndHandlingImpl(mockGetCurrentGameZone, mockActiveCharactersProvider,
                        statusEffectTypes, null, staticStatTypes, mockMagnitudeCalculation));
        assertThrows(IllegalArgumentException.class,
                () -> new RoundEndHandlingImpl(mockGetCurrentGameZone, mockActiveCharactersProvider,
                        statusEffectTypes, variableStatTypes, null, mockMagnitudeCalculation));
        assertThrows(IllegalArgumentException.class,
                () -> new RoundEndHandlingImpl(mockGetCurrentGameZone, mockActiveCharactersProvider,
                        statusEffectTypes, variableStatTypes, listOf((StaticStatisticType) null),
                        mockMagnitudeCalculation));
        assertThrows(IllegalArgumentException.class,
                () -> new RoundEndHandlingImpl(mockGetCurrentGameZone, mockActiveCharactersProvider,
                        statusEffectTypes, variableStatTypes, staticStatTypes, null));
    }

    @Test
    public void testRunRoundEnd() {
        var isAdvancingRounds = randomBoolean();

        roundEndHandling.runRoundEnd(isAdvancingRounds);

        var inOrder =
                Mockito.inOrder(mockStatusEffectType, mockStatusEffectType, mockVariableStatType1,
                        mockStaticStatType, mockGetCurrentGameZone, mockActiveCharactersProvider,
                        mockStatusEffectRoundEndEffects, mockVariableStat1RoundEndEffects,
                        mockStaticStatRoundEndEffects, mockMagnitudeCalculation,
                        mockTargetVariableStatType);

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
                staticStatAccompanyAllEffects.get(0).FIRST);
        assertSame(mockCharacter1, staticStatAccompanyAllEffects.get(0).SECOND);
        assertArrayEquals(new int[]{CHARACTER_2_STATIC_STAT_MAGNITUDE},
                staticStatAccompanyAllEffects.get(1).FIRST);
        assertSame(mockCharacter2, staticStatAccompanyAllEffects.get(1).SECOND);
        assertArrayEquals(new int[]{CHARACTER_3_STATIC_STAT_MAGNITUDE},
                staticStatAccompanyAllEffects.get(2).FIRST);
        assertSame(mockCharacter3, staticStatAccompanyAllEffects.get(2).SECOND);

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
                variableStat1AccompanyAllEffects.get(0).FIRST);
        assertSame(mockCharacter1, variableStat1AccompanyAllEffects.get(0).SECOND);
        assertArrayEquals(new int[]{CHARACTER_2_VARIABLE_STAT_1_MAGNITUDE},
                variableStat1AccompanyAllEffects.get(1).FIRST);
        assertSame(mockCharacter2, variableStat1AccompanyAllEffects.get(1).SECOND);
        assertArrayEquals(new int[]{CHARACTER_3_VARIABLE_STAT_1_MAGNITUDE},
                variableStat1AccompanyAllEffects.get(2).FIRST);
        assertSame(mockCharacter3, variableStat1AccompanyAllEffects.get(2).SECOND);

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
                statusEffectAccompanyAllEffects.get(0).FIRST);
        assertSame(mockCharacter1, statusEffectAccompanyAllEffects.get(0).SECOND);
        assertArrayEquals(new int[]{CHARACTER_2_STATUS_EFFECT_MAGNITUDE},
                statusEffectAccompanyAllEffects.get(1).FIRST);
        assertSame(mockCharacter2, statusEffectAccompanyAllEffects.get(1).SECOND);
        assertArrayEquals(new int[]{CHARACTER_3_STATUS_EFFECT_MAGNITUDE},
                statusEffectAccompanyAllEffects.get(2).FIRST);
        assertSame(mockCharacter3, statusEffectAccompanyAllEffects.get(2).SECOND);
    }
}
