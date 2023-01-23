package inaugural.soliloquy.ruleset.gameconcepts;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import soliloquy.specs.common.valueobjects.Pair;
import soliloquy.specs.gamestate.entities.Character;
import soliloquy.specs.gamestate.entities.*;
import soliloquy.specs.gamestate.entities.exceptions.EntityDeletedException;
import soliloquy.specs.ruleset.entities.actonroundendandcharacterturn.EffectsCharacterOnRoundOrTurnChange.EffectsOnCharacter;
import soliloquy.specs.ruleset.entities.actonroundendandcharacterturn.StatisticChangeMagnitude;
import soliloquy.specs.ruleset.entities.character.CharacterVariableStatisticType;
import soliloquy.specs.ruleset.entities.character.StatusEffectType;
import soliloquy.specs.ruleset.gameconcepts.ActOnRoundEndAndCharacterTurn;
import soliloquy.specs.ruleset.gameconcepts.CharacterStatisticCalculation;
import soliloquy.specs.ruleset.gameconcepts.StatisticMagnitudeEffectCalculation;

import java.util.Map;

import static inaugural.soliloquy.tools.collections.Collections.listOf;
import static inaugural.soliloquy.tools.collections.Collections.mapOf;
import static inaugural.soliloquy.tools.random.Random.*;
import static inaugural.soliloquy.tools.testing.Mock.generateMockList;
import static inaugural.soliloquy.tools.testing.Mock.generateMockMap;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.*;

public class ActOnRoundEndAndCharacterTurnImplTests {
    private final int STATUS_EFFECT_LEVEL = randomInt();
    private final int EFFECTED_VARIABLE_STAT_MAX_VALUE = randomInt();
    private final int EFFECTED_VARIABLE_STAT_CURRENT_VALUE = randomInt();
    private final int VARIABLE_STATISTIC_PRIORITY = randomInt();
    private final int STATUS_EFFECT_PRIORITY =
            randomIntWithInclusiveCeiling(VARIABLE_STATISTIC_PRIORITY - 1);
    private final int SOURCE_VARIABLE_STAT_CURRENT_VALUE = randomInt();
    private final int MAGNITUDE_1_EFFECT = randomIntWithInclusiveFloor(1);
    private final int MAGNITUDE_2_EFFECT = randomIntWithInclusiveFloor(1);
    private final int MAGNITUDE_3_EFFECT = randomIntWithInclusiveFloor(1);

    @Mock private CharacterStatisticCalculation mockStatisticCalculation;
    @Mock private StatisticMagnitudeEffectCalculation mockEffectCalculation;

    @Mock private CharacterVariableStatisticType mockEffectedVariableStatType;
    @Mock private CharacterVariableStatistic mockEffectedVariableStat;

    @SuppressWarnings("rawtypes")
    @Mock private StatisticChangeMagnitude mockVariableStatStatisticChangeMagnitude1;
    @SuppressWarnings("rawtypes")
    @Mock private StatisticChangeMagnitude mockVariableStatStatisticChangeMagnitude2;

    @Mock private EffectsOnCharacter mockSourceVariableStatOnRoundEnd;
    @Mock private EffectsOnCharacter mockSourceVariableStatOnTurnStart;
    @Mock private EffectsOnCharacter mockSourceVariableStatOnTurnEnd;
    @Mock private CharacterVariableStatistic mockSourceVariableStat;
    @Mock private CharacterVariableStatisticType mockSourceVariableStatType;
    @Mock private CharacterVariableStatistics mockCharacterVariableStats;

    @Mock private EffectsOnCharacter mockStatusEffectOnRoundEnd;
    @Mock private EffectsOnCharacter mockStatusEffectOnTurnStart;
    @Mock private EffectsOnCharacter mockStatusEffectOnTurnEnd;
    @SuppressWarnings("rawtypes")
    @Mock private StatisticChangeMagnitude mockStatusEffectChangeMagnitude;
    @Mock private StatusEffectType mockStatusEffectType;
    @Mock private Map<StatusEffectType, Integer> mockCharacterStatusEffectTypes;
    @Mock private CharacterStatusEffects mockCharacterStatusEffects;

    @Mock private Character mockCharacter;

    private ActOnRoundEndAndCharacterTurn actOnRoundEndAndCharacterTurn;

    @BeforeEach
    void setUp() {
        mockEffectedVariableStatType = mock(CharacterVariableStatisticType.class);

        mockEffectedVariableStat = mock(CharacterVariableStatistic.class);
        when(mockEffectedVariableStat.getCurrentValue()).thenReturn(
                EFFECTED_VARIABLE_STAT_CURRENT_VALUE);

        mockVariableStatStatisticChangeMagnitude1 = mock(StatisticChangeMagnitude.class);

        mockVariableStatStatisticChangeMagnitude2 = mock(StatisticChangeMagnitude.class);

        mockStatisticCalculation = mock(CharacterStatisticCalculation.class);
        when(mockStatisticCalculation.calculate(any(), mockEffectedVariableStatType))
                .thenReturn(Pair.of(EFFECTED_VARIABLE_STAT_MAX_VALUE, mapOf()));

        mockEffectCalculation = mock(StatisticMagnitudeEffectCalculation.class);
        //noinspection unchecked
        when(mockEffectCalculation.getEffect(mockSourceVariableStatType,
                same(mockVariableStatStatisticChangeMagnitude1), anyInt(), anyInt(), anyInt(),
                any()))
                .thenReturn(MAGNITUDE_1_EFFECT);
        //noinspection unchecked
        when(mockEffectCalculation.getEffect(mockStatusEffectType,
                same(mockVariableStatStatisticChangeMagnitude2), anyInt(), anyInt(), anyInt(),
                any()))
                .thenReturn(MAGNITUDE_2_EFFECT);

        mockEffectCalculation = mock(StatisticMagnitudeEffectCalculation.class);
        //noinspection unchecked
        when(mockEffectCalculation.getEffect(mockSourceVariableStatType,
                same(mockStatusEffectChangeMagnitude), anyInt(), anyInt(), anyInt(),
                any()))
                .thenReturn(MAGNITUDE_3_EFFECT);

        var variableStatMagnitudes =
                generateMockList(mockVariableStatStatisticChangeMagnitude1,
                        mockVariableStatStatisticChangeMagnitude2);

        mockSourceVariableStatOnRoundEnd = mock(EffectsOnCharacter.class);
        when(mockSourceVariableStatOnRoundEnd.magnitudes()).thenReturn(variableStatMagnitudes);
        when(mockSourceVariableStatOnRoundEnd.priority()).thenReturn(VARIABLE_STATISTIC_PRIORITY);

        mockSourceVariableStatOnTurnStart = mock(EffectsOnCharacter.class);
        when(mockSourceVariableStatOnTurnStart.magnitudes()).thenReturn(variableStatMagnitudes);
        when(mockSourceVariableStatOnTurnStart.priority()).thenReturn(VARIABLE_STATISTIC_PRIORITY);

        mockSourceVariableStatOnTurnEnd = mock(EffectsOnCharacter.class);
        when(mockSourceVariableStatOnTurnEnd.magnitudes()).thenReturn(variableStatMagnitudes);
        when(mockSourceVariableStatOnTurnEnd.priority()).thenReturn(VARIABLE_STATISTIC_PRIORITY);

        mockSourceVariableStatType = mock(CharacterVariableStatisticType.class);
        when(mockSourceVariableStatType.onRoundEnd()).thenReturn(
                mockSourceVariableStatOnRoundEnd);
        when(mockSourceVariableStatType.onTurnStart()).thenReturn(
                mockSourceVariableStatOnTurnStart);
        when(mockSourceVariableStatType.onTurnEnd()).thenReturn(
                mockSourceVariableStatOnTurnEnd);

        mockSourceVariableStat = mock(CharacterVariableStatistic.class);
        when(mockSourceVariableStat.type()).thenReturn(mockSourceVariableStatType);
        when(mockSourceVariableStat.getCurrentValue()).thenReturn(
                SOURCE_VARIABLE_STAT_CURRENT_VALUE);

        var mockVariableStats = listOf(mockSourceVariableStat);

        mockCharacterVariableStats = mock(CharacterVariableStatistics.class);
        when(mockCharacterVariableStats.iterator()).thenReturn(mockVariableStats.iterator());
        when(mockCharacterVariableStats.get(mockEffectedVariableStatType))
                .thenReturn(mockEffectedVariableStat);

        mockStatusEffectChangeMagnitude = mock(StatisticChangeMagnitude.class);

        var statusEffectMagnitudes = generateMockList(mockStatusEffectChangeMagnitude);

        mockStatusEffectOnRoundEnd = mock(EffectsOnCharacter.class);
        when(mockStatusEffectOnRoundEnd.magnitudes()).thenReturn(statusEffectMagnitudes);
        when(mockStatusEffectOnRoundEnd.priority()).thenReturn(STATUS_EFFECT_PRIORITY);

        mockStatusEffectOnTurnStart = mock(EffectsOnCharacter.class);
        when(mockStatusEffectOnTurnStart.magnitudes()).thenReturn(statusEffectMagnitudes);
        when(mockStatusEffectOnTurnStart.priority()).thenReturn(STATUS_EFFECT_PRIORITY);

        mockStatusEffectOnTurnEnd = mock(EffectsOnCharacter.class);
        when(mockStatusEffectOnTurnEnd.magnitudes()).thenReturn(statusEffectMagnitudes);
        when(mockStatusEffectOnTurnEnd.priority()).thenReturn(STATUS_EFFECT_PRIORITY);

        mockStatusEffectType = mock(StatusEffectType.class);
        when(mockStatusEffectType.onRoundEnd()).thenReturn(mockStatusEffectOnRoundEnd);
        when(mockStatusEffectType.onTurnStart()).thenReturn(mockStatusEffectOnTurnStart);
        when(mockStatusEffectType.onTurnEnd()).thenReturn(mockStatusEffectOnTurnEnd);

        mockCharacterStatusEffectTypes =
                generateMockMap(Pair.of(mockStatusEffectType, STATUS_EFFECT_LEVEL));

        mockCharacterStatusEffects = mock(CharacterStatusEffects.class);
        when(mockCharacterStatusEffects.representation()).thenReturn(
                mockCharacterStatusEffectTypes);

        mockCharacter = mock(Character.class);
        when(mockCharacter.isDeleted()).thenReturn(false);
        when(mockCharacter.variableStatistics()).thenReturn(mockCharacterVariableStats);
        when(mockCharacter.statusEffects()).thenReturn(mockCharacterStatusEffects);

        actOnRoundEndAndCharacterTurn =
                new ActOnRoundEndAndCharacterTurnImpl(mockStatisticCalculation,
                        mockEffectCalculation);
    }

    @Test
    void testConstructorWithInvalidParams() {
        assertThrows(IllegalArgumentException.class,
                () -> new ActOnRoundEndAndCharacterTurnImpl(mockStatisticCalculation, null));
        assertThrows(IllegalArgumentException.class,
                () -> new ActOnRoundEndAndCharacterTurnImpl(null, mockEffectCalculation));
    }

    @Test
    void testRoundEnd() {
        actOnRoundEndAndCharacterTurn.roundEnd(mockCharacter);

        var inOrder = inOrder(mockCharacter, mockCharacterVariableStats, mockSourceVariableStat,
                mockSourceVariableStatType, mockEffectCalculation);
        inOrder.verify(mockCharacter, times(1)).variableStatistics();
        inOrder.verify(mockCharacterVariableStats, times(1)).iterator();
        inOrder.verify(mockSourceVariableStatType, times(1)).onRoundEnd();
        //noinspection unchecked
        inOrder.verify(mockEffectCalculation, times(1))
                .getEffect(mockSourceVariableStatType, mockVariableStatStatisticChangeMagnitude1,
                        SOURCE_VARIABLE_STAT_CURRENT_VALUE, EFFECTED_VARIABLE_STAT_MAX_VALUE,
                        EFFECTED_VARIABLE_STAT_CURRENT_VALUE, mockCharacter);
        inOrder.verify(mockSourceVariableStat, times(1)).alterCurrentValue(MAGNITUDE_1_EFFECT);
        //noinspection unchecked
        inOrder.verify(mockEffectCalculation, times(1))
                .getEffect(mockSourceVariableStatType, mockVariableStatStatisticChangeMagnitude2,
                        SOURCE_VARIABLE_STAT_CURRENT_VALUE, EFFECTED_VARIABLE_STAT_MAX_VALUE,
                        EFFECTED_VARIABLE_STAT_CURRENT_VALUE, mockCharacter);
        inOrder.verify(mockSourceVariableStat, times(1)).alterCurrentValue(MAGNITUDE_2_EFFECT);
        inOrder.verify(mockSourceVariableStatOnRoundEnd, times(1))
                .accompanyEffect(eq(new int[]{MAGNITUDE_1_EFFECT, MAGNITUDE_2_EFFECT}),
                        same(mockCharacter));
        inOrder.verify(mockSourceVariableStatOnRoundEnd, times(1))
                .otherEffects(eq(new int[]{MAGNITUDE_1_EFFECT, MAGNITUDE_2_EFFECT}),
                        same(mockCharacter));
        //noinspection unchecked
        inOrder.verify(mockEffectCalculation, times(1))
                .getEffect(mockStatusEffectType, mockVariableStatStatisticChangeMagnitude2,
                        STATUS_EFFECT_LEVEL, EFFECTED_VARIABLE_STAT_MAX_VALUE,
                        EFFECTED_VARIABLE_STAT_CURRENT_VALUE, mockCharacter);
        inOrder.verify(mockSourceVariableStat, times(1)).alterCurrentValue(MAGNITUDE_3_EFFECT);
        inOrder.verify(mockSourceVariableStatOnRoundEnd, times(1))
                .accompanyEffect(eq(new int[]{MAGNITUDE_3_EFFECT}),
                        same(mockCharacter));
        inOrder.verify(mockSourceVariableStatOnRoundEnd, times(1))
                .otherEffects(eq(new int[]{MAGNITUDE_3_EFFECT}),
                        same(mockCharacter));
    }

    @Test
    void testRoundEndWithInvalidParams() {
        when(mockCharacter.isDeleted()).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> actOnRoundEndAndCharacterTurn.roundEnd(null));
        assertThrows(EntityDeletedException.class,
                () -> actOnRoundEndAndCharacterTurn.roundEnd(mockCharacter));
    }
}
