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
import soliloquy.specs.ruleset.entities.actonroundendandcharacterturn.EffectsCharacterOnRoundOrTurnChange.EffectsOnCharacter;
import soliloquy.specs.ruleset.entities.actonroundendandcharacterturn.StatisticChangeMagnitude;
import soliloquy.specs.ruleset.entities.character.CharacterAIType;
import soliloquy.specs.ruleset.entities.character.CharacterVariableStatisticType;
import soliloquy.specs.ruleset.entities.character.StatusEffectType;
import soliloquy.specs.ruleset.gameconcepts.StatisticMagnitudeEffectCalculation;
import soliloquy.specs.ruleset.gameconcepts.TurnHandling;

import java.util.List;
import java.util.function.Consumer;

import static inaugural.soliloquy.tools.collections.Collections.*;
import static inaugural.soliloquy.tools.random.Random.randomInt;
import static inaugural.soliloquy.tools.random.Random.randomIntWithInclusiveCeiling;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.*;

class TurnHandlingImplTests {
    private final int STATUS_EFFECT_TURN_START_PRIORITY = randomInt();
    private final int VARIABLE_STAT_TURN_START_PRIORITY =
            randomIntWithInclusiveCeiling(STATUS_EFFECT_TURN_START_PRIORITY - 1);
    private final int STATUS_EFFECT_TURN_END_PRIORITY = randomInt();

    private final int STATUS_EFFECT_LEVEL = randomInt();
    private final int VARIABLE_STAT_CURRENT_VALUE = randomInt();

    private final int STATUS_EFFECT_START_CALCULATED_EFFECT = randomInt();
    private final int VARIABLE_STAT_START_CALCULATED_EFFECT = randomInt();
    private final int STATUS_EFFECT_END_CALCULATED_EFFECT = randomInt();

    @Mock private StatisticMagnitudeEffectCalculation mockEffectCalculation;
    @Mock private Consumer<Pair<Character, VariableCache>> mockPassControlToPlayer;

    @Mock private CharacterVariableStatisticType mockTargetVariableStatType;
    @SuppressWarnings("rawtypes")
    @Mock private StatisticChangeMagnitude mockStatusEffectTypeTurnStartMagnitude;
    @SuppressWarnings("rawtypes")
    @Mock private StatisticChangeMagnitude mockStatusEffectTypeTurnEndMagnitude;
    @SuppressWarnings("rawtypes")
    @Mock private StatisticChangeMagnitude mockVariableStatTypeTurnStartMagnitude;

    @Mock private EffectsOnCharacter mockStatusEffectTypeTurnStart;
    @Mock private EffectsOnCharacter mockStatusEffectTypeTurnEnd;
    @Mock private EffectsOnCharacter mockVariableStatTypeTurnStart;

    @Mock private StatusEffectType mockStatusEffectType;
    @Mock private CharacterVariableStatisticType mockVariableStatType;
    @Mock private List<CharacterVariableStatisticType> mockVariableStatTypes;

    @Mock private CharacterStatusEffects mockStatusEffects;
    @Mock private CharacterAIType mockAIType;
    @Mock private Character mockCharacter;
    @Mock private VariableCache mockTurnData;

    private TurnHandling turnHandling;

    @BeforeEach
    void setUp() {
        //noinspection unchecked
        mockPassControlToPlayer = (Consumer<Pair<Character, VariableCache>>) mock(Consumer.class);

        mockTargetVariableStatType = mock(CharacterVariableStatisticType.class);

        mockStatusEffectTypeTurnStartMagnitude = mock(StatisticChangeMagnitude.class);
        when(mockStatusEffectTypeTurnStartMagnitude.effectedStatisticType()).thenReturn(
                mockTargetVariableStatType);
        mockStatusEffectTypeTurnEndMagnitude = mock(StatisticChangeMagnitude.class);
        when(mockStatusEffectTypeTurnEndMagnitude.effectedStatisticType()).thenReturn(
                mockTargetVariableStatType);
        mockVariableStatTypeTurnStartMagnitude = mock(StatisticChangeMagnitude.class);
        when(mockVariableStatTypeTurnStartMagnitude.effectedStatisticType()).thenReturn(
                mockTargetVariableStatType);

        mockStatusEffectTypeTurnStart = mock(EffectsOnCharacter.class);
        when(mockStatusEffectTypeTurnStart.priority()).thenReturn(
                STATUS_EFFECT_TURN_START_PRIORITY);
        when(mockStatusEffectTypeTurnStart.magnitudes())
                .thenReturn(listOf(mockStatusEffectTypeTurnStartMagnitude));
        mockStatusEffectTypeTurnEnd = mock(EffectsOnCharacter.class);
        when(mockStatusEffectTypeTurnEnd.priority()).thenReturn(STATUS_EFFECT_TURN_END_PRIORITY);
        when(mockStatusEffectTypeTurnEnd.magnitudes())
                .thenReturn(listOf(mockStatusEffectTypeTurnEndMagnitude));
        mockVariableStatTypeTurnStart = mock(EffectsOnCharacter.class);
        when(mockVariableStatTypeTurnStart.priority()).thenReturn(
                VARIABLE_STAT_TURN_START_PRIORITY);
        when(mockVariableStatTypeTurnStart.magnitudes())
                .thenReturn(listOf(mockVariableStatTypeTurnStartMagnitude));

        mockStatusEffectType = mock(StatusEffectType.class);
        when(mockStatusEffectType.onTurnStart()).thenReturn(mockStatusEffectTypeTurnStart);
        when(mockStatusEffectType.onTurnEnd()).thenReturn(mockStatusEffectTypeTurnEnd);

        mockStatusEffects = mock(CharacterStatusEffects.class);
        when(mockStatusEffects.representation())
                .thenReturn(mapOf(Pair.of(mockStatusEffectType, STATUS_EFFECT_LEVEL)));

        mockVariableStatType = mock(CharacterVariableStatisticType.class);
        when(mockVariableStatType.onTurnStart()).thenReturn(mockVariableStatTypeTurnStart);
        when(mockVariableStatType.onTurnEnd()).thenReturn(null);

        var variableStatTypes = listOf(mockVariableStatType);
        var variableStatTypes2 = listOf(mockVariableStatType);
        //noinspection unchecked
        mockVariableStatTypes = (List<CharacterVariableStatisticType>) mock(List.class);
        when(mockVariableStatTypes.iterator())
                .thenReturn(variableStatTypes.iterator())
                .thenReturn(variableStatTypes2.iterator());
        doCallRealMethod().when(mockVariableStatTypes).forEach(any());


        mockEffectCalculation = mock(StatisticMagnitudeEffectCalculation.class);
        //noinspection unchecked
        when(mockEffectCalculation.getEffect(same(mockStatusEffectType),
                same(mockStatusEffectTypeTurnStartMagnitude), any()))
                .thenReturn(STATUS_EFFECT_START_CALCULATED_EFFECT);
        //noinspection unchecked
        when(mockEffectCalculation.getEffect(same(mockStatusEffectType),
                same(mockStatusEffectTypeTurnEndMagnitude), any()))
                .thenReturn(STATUS_EFFECT_END_CALCULATED_EFFECT);
        //noinspection unchecked
        when(mockEffectCalculation.getEffect(same(mockVariableStatType),
                same(mockVariableStatTypeTurnStartMagnitude), any()))
                .thenReturn(VARIABLE_STAT_START_CALCULATED_EFFECT);

        mockAIType = mock(CharacterAIType.class);

        mockCharacter = mock(Character.class);
        when(mockCharacter.statusEffects()).thenReturn(mockStatusEffects);
        when(mockCharacter.getAIType()).thenReturn(mockAIType);
        when(mockCharacter.getVariableStatisticCurrentValue(any())).thenReturn(
                VARIABLE_STAT_CURRENT_VALUE);

        mockTurnData = mock(VariableCache.class);

        turnHandling = new TurnHandlingImpl(mockEffectCalculation, mockPassControlToPlayer,
                mockVariableStatTypes);
    }

    @Test
    void testConstructorWithInvalidParams() {
        assertThrows(IllegalArgumentException.class,
                () -> new TurnHandlingImpl(null, mockPassControlToPlayer, mockVariableStatTypes));
        assertThrows(IllegalArgumentException.class,
                () -> new TurnHandlingImpl(mockEffectCalculation, null, mockVariableStatTypes));
        assertThrows(IllegalArgumentException.class,
                () -> new TurnHandlingImpl(mockEffectCalculation, mockPassControlToPlayer, null));
    }

    @Test
    void testRunTurnWithNoEffectsForAiCharacter() {
        when(mockStatusEffectType.onTurnStart()).thenReturn(null);
        when(mockStatusEffectType.onTurnEnd()).thenReturn(null);
        when(mockVariableStatType.onTurnStart()).thenReturn(null);

        turnHandling.runTurn(mockCharacter, mockTurnData, false);

        verify(mockStatusEffectTypeTurnStart, never()).magnitudes();
        verify(mockStatusEffectTypeTurnStart, never()).accompanyEffect(any(), any(), anyBoolean());
        verify(mockStatusEffectTypeTurnStart, never()).otherEffects(any(), any(), anyBoolean());
        verify(mockStatusEffectTypeTurnEnd, never()).magnitudes();
        verify(mockStatusEffectTypeTurnEnd, never()).accompanyEffect(any(), any(), anyBoolean());
        verify(mockStatusEffectTypeTurnEnd, never()).otherEffects(any(), any(), anyBoolean());
        verify(mockVariableStatTypeTurnStart, never()).magnitudes();
        verify(mockVariableStatTypeTurnStart, never()).accompanyEffect(any(), any(), anyBoolean());
        verify(mockVariableStatTypeTurnStart, never()).otherEffects(any(), any(), anyBoolean());
        verify(mockEffectCalculation, never())
                .getEffect(any(CharacterVariableStatisticType.class), any(), any());
        verify(mockEffectCalculation, never()).getEffect(any(StatusEffectType.class), any(), any());
        verify(mockTargetVariableStatType, never()).alter(any(), anyInt());
        var inOrder = Mockito.inOrder(mockCharacter, mockStatusEffects, mockStatusEffectType,
                mockVariableStatType, mockEffectCalculation, mockVariableStatTypes,
                mockStatusEffectTypeTurnStart, mockVariableStatTypeTurnStart,
                mockStatusEffectTypeTurnEnd, mockAIType);
        inOrder.verify(mockCharacter).statusEffects();
        inOrder.verify(mockStatusEffectType).onTurnStart();
        inOrder.verify(mockVariableStatTypes).iterator();
        inOrder.verify(mockVariableStatType).onTurnStart();
        inOrder.verify(mockCharacter).getPlayerControlled();
        inOrder.verify(mockCharacter).getAIType();
        inOrder.verify(mockAIType).act(mockCharacter, mockTurnData);
        inOrder.verify(mockCharacter).statusEffects();
        inOrder.verify(mockStatusEffectType).onTurnEnd();
        inOrder.verify(mockVariableStatTypes).iterator();
        inOrder.verify(mockVariableStatType).onTurnEnd();
    }

    @Test
    void testRunTurnWithNoEffectsForPlayerCharacter() {
        when(mockCharacter.getPlayerControlled()).thenReturn(true);
        when(mockStatusEffectType.onTurnStart()).thenReturn(null);
        when(mockStatusEffectType.onTurnEnd()).thenReturn(null);
        when(mockVariableStatType.onTurnStart()).thenReturn(null);

        turnHandling.runTurn(mockCharacter, mockTurnData, false);

        verify(mockStatusEffectTypeTurnStart, never()).magnitudes();
        verify(mockStatusEffectTypeTurnStart, never()).accompanyEffect(any(), any(), anyBoolean());
        verify(mockStatusEffectTypeTurnStart, never()).otherEffects(any(), any(), anyBoolean());
        verify(mockStatusEffectTypeTurnEnd, never()).magnitudes();
        verify(mockStatusEffectTypeTurnEnd, never()).accompanyEffect(any(), any(), anyBoolean());
        verify(mockStatusEffectTypeTurnEnd, never()).otherEffects(any(), any(), anyBoolean());
        verify(mockVariableStatTypeTurnStart, never()).magnitudes();
        verify(mockVariableStatTypeTurnStart, never()).accompanyEffect(any(), any(), anyBoolean());
        verify(mockVariableStatTypeTurnStart, never()).otherEffects(any(), any(), anyBoolean());
        verify(mockEffectCalculation, never())
                .getEffect(any(CharacterVariableStatisticType.class), any(), any());
        verify(mockEffectCalculation, never()).getEffect(any(StatusEffectType.class), any(), any());
        verify(mockTargetVariableStatType, never()).alter(any(), anyInt());
        var inOrder = Mockito.inOrder(mockCharacter, mockStatusEffects, mockStatusEffectType,
                mockVariableStatType, mockEffectCalculation, mockVariableStatTypes,
                mockStatusEffectTypeTurnStart, mockVariableStatTypeTurnStart,
                mockStatusEffectTypeTurnEnd, mockPassControlToPlayer);
        inOrder.verify(mockCharacter).statusEffects();
        inOrder.verify(mockStatusEffectType).onTurnStart();
        inOrder.verify(mockVariableStatTypes).iterator();
        inOrder.verify(mockVariableStatType).onTurnStart();
        inOrder.verify(mockCharacter).getPlayerControlled();
        var playerControlCaptor = ArgumentCaptor.forClass(Pair.class);
        //noinspection unchecked
        inOrder.verify(mockPassControlToPlayer).accept(playerControlCaptor.capture());
        var playerControlParam = playerControlCaptor.getValue();
        assertSame(mockCharacter, playerControlParam.getItem1());
        assertSame(mockTurnData, playerControlParam.getItem2());
        inOrder.verify(mockCharacter).statusEffects();
        inOrder.verify(mockStatusEffectType).onTurnEnd();
        inOrder.verify(mockVariableStatTypes).iterator();
        inOrder.verify(mockVariableStatType).onTurnEnd();
    }

    @Test
    void testRunTurnWithEffectsForAiCharacter() {
        turnHandling.runTurn(mockCharacter, mockTurnData, false);

        var inOrder = Mockito.inOrder(mockCharacter, mockStatusEffects, mockStatusEffectType,
                mockVariableStatType, mockEffectCalculation, mockVariableStatTypes,
                mockStatusEffectTypeTurnStart, mockVariableStatTypeTurnStart,
                mockStatusEffectTypeTurnEnd, mockAIType, mockTargetVariableStatType);
        inOrder.verify(mockCharacter).statusEffects();
        inOrder.verify(mockStatusEffectType).onTurnStart();
        inOrder.verify(mockVariableStatTypes).iterator();
        inOrder.verify(mockVariableStatType).onTurnStart();
        //noinspection unchecked
        inOrder.verify(mockEffectCalculation)
                .getEffect(mockStatusEffectType, mockStatusEffectTypeTurnStartMagnitude,
                        mockCharacter);
        inOrder.verify(mockStatusEffectTypeTurnStart)
                .accompanyEffect(new int[]{STATUS_EFFECT_START_CALCULATED_EFFECT}, mockCharacter,
                        false);
        inOrder.verify(mockTargetVariableStatType)
                .alter(mockCharacter, STATUS_EFFECT_START_CALCULATED_EFFECT);
        inOrder.verify(mockStatusEffectTypeTurnStart)
                .otherEffects(new int[]{STATUS_EFFECT_START_CALCULATED_EFFECT}, mockCharacter,
                        false);
        //noinspection unchecked
        inOrder.verify(mockEffectCalculation)
                .getEffect(mockVariableStatType, mockVariableStatTypeTurnStartMagnitude,
                        mockCharacter);
        inOrder.verify(mockVariableStatTypeTurnStart)
                .accompanyEffect(new int[]{VARIABLE_STAT_START_CALCULATED_EFFECT}, mockCharacter,
                        false);
        inOrder.verify(mockTargetVariableStatType)
                .alter(mockCharacter, VARIABLE_STAT_START_CALCULATED_EFFECT);
        inOrder.verify(mockVariableStatTypeTurnStart)
                .otherEffects(new int[]{VARIABLE_STAT_START_CALCULATED_EFFECT}, mockCharacter,
                        false);
        inOrder.verify(mockCharacter).getPlayerControlled();
        inOrder.verify(mockCharacter).getAIType();
        inOrder.verify(mockAIType).act(mockCharacter, mockTurnData);
        inOrder.verify(mockCharacter).statusEffects();
        inOrder.verify(mockStatusEffectType).onTurnEnd();
        inOrder.verify(mockVariableStatTypes).iterator();
        inOrder.verify(mockVariableStatType).onTurnEnd();
        //noinspection unchecked
        inOrder.verify(mockEffectCalculation)
                .getEffect(mockStatusEffectType, mockStatusEffectTypeTurnEndMagnitude,
                        mockCharacter);
        inOrder.verify(mockStatusEffectTypeTurnEnd)
                .accompanyEffect(new int[]{STATUS_EFFECT_END_CALCULATED_EFFECT}, mockCharacter,
                        false);
        inOrder.verify(mockTargetVariableStatType)
                .alter(mockCharacter, STATUS_EFFECT_END_CALCULATED_EFFECT);
        inOrder.verify(mockStatusEffectTypeTurnEnd)
                .otherEffects(new int[]{STATUS_EFFECT_END_CALCULATED_EFFECT}, mockCharacter, false);
    }

    @Test
    void testRunTurnWithEffectsForPlayerCharacter() {
        when(mockCharacter.getPlayerControlled()).thenReturn(true);

        turnHandling.runTurn(mockCharacter, mockTurnData, false);

        var inOrder = Mockito.inOrder(mockCharacter, mockStatusEffects, mockStatusEffectType,
                mockVariableStatType, mockEffectCalculation, mockVariableStatTypes,
                mockStatusEffectTypeTurnStart, mockVariableStatTypeTurnStart,
                mockStatusEffectTypeTurnEnd, mockPassControlToPlayer, mockTargetVariableStatType);
        inOrder.verify(mockCharacter).statusEffects();
        inOrder.verify(mockStatusEffectType).onTurnStart();
        inOrder.verify(mockVariableStatTypes).iterator();
        inOrder.verify(mockVariableStatType).onTurnStart();
        //noinspection unchecked
        inOrder.verify(mockEffectCalculation)
                .getEffect(mockStatusEffectType, mockStatusEffectTypeTurnStartMagnitude,
                        mockCharacter);
        inOrder.verify(mockStatusEffectTypeTurnStart)
                .accompanyEffect(new int[]{STATUS_EFFECT_START_CALCULATED_EFFECT}, mockCharacter,
                        false);
        inOrder.verify(mockTargetVariableStatType)
                .alter(mockCharacter, STATUS_EFFECT_START_CALCULATED_EFFECT);
        inOrder.verify(mockStatusEffectTypeTurnStart)
                .otherEffects(new int[]{STATUS_EFFECT_START_CALCULATED_EFFECT}, mockCharacter,
                        false);
        //noinspection unchecked
        inOrder.verify(mockEffectCalculation)
                .getEffect(mockVariableStatType, mockVariableStatTypeTurnStartMagnitude,
                        mockCharacter);
        inOrder.verify(mockVariableStatTypeTurnStart)
                .accompanyEffect(new int[]{VARIABLE_STAT_START_CALCULATED_EFFECT}, mockCharacter,
                        false);
        inOrder.verify(mockTargetVariableStatType)
                .alter(mockCharacter, VARIABLE_STAT_START_CALCULATED_EFFECT);
        inOrder.verify(mockVariableStatTypeTurnStart)
                .otherEffects(new int[]{VARIABLE_STAT_START_CALCULATED_EFFECT}, mockCharacter,
                        false);
        inOrder.verify(mockCharacter).getPlayerControlled();
        var playerControlCaptor = ArgumentCaptor.forClass(Pair.class);
        //noinspection unchecked
        inOrder.verify(mockPassControlToPlayer).accept(playerControlCaptor.capture());
        var playerControlParam = playerControlCaptor.getValue();
        assertSame(mockCharacter, playerControlParam.getItem1());
        assertSame(mockTurnData, playerControlParam.getItem2());
        inOrder.verify(mockCharacter).statusEffects();
        inOrder.verify(mockStatusEffectType).onTurnEnd();
        inOrder.verify(mockVariableStatTypes).iterator();
        inOrder.verify(mockVariableStatType).onTurnEnd();
        //noinspection unchecked
        inOrder.verify(mockEffectCalculation)
                .getEffect(mockStatusEffectType, mockStatusEffectTypeTurnEndMagnitude,
                        mockCharacter);
        inOrder.verify(mockStatusEffectTypeTurnEnd)
                .accompanyEffect(new int[]{STATUS_EFFECT_END_CALCULATED_EFFECT}, mockCharacter,
                        false);
        inOrder.verify(mockTargetVariableStatType)
                .alter(mockCharacter, STATUS_EFFECT_END_CALCULATED_EFFECT);
        inOrder.verify(mockStatusEffectTypeTurnEnd)
                .otherEffects(new int[]{STATUS_EFFECT_END_CALCULATED_EFFECT}, mockCharacter, false);
    }

    @Test
    void testHandleAiControlledCharacterTurnWithAdvancingMultipleRounds() {
        turnHandling.runTurn(mockCharacter, mockTurnData, true);

        verify(mockCharacter, never()).getPlayerControlled();
        verify(mockCharacter, never()).getAIType();
        verify(mockAIType, never()).act(mockCharacter, mockTurnData);
        var inOrder = Mockito.inOrder(mockCharacter, mockStatusEffects, mockStatusEffectType,
                mockVariableStatType, mockEffectCalculation, mockVariableStatTypes,
                mockStatusEffectTypeTurnStart, mockVariableStatTypeTurnStart,
                mockStatusEffectTypeTurnEnd, mockAIType, mockTargetVariableStatType);
        inOrder.verify(mockCharacter).statusEffects();
        inOrder.verify(mockStatusEffectType).onTurnStart();
        inOrder.verify(mockVariableStatTypes).iterator();
        inOrder.verify(mockVariableStatType).onTurnStart();
        //noinspection unchecked
        inOrder.verify(mockEffectCalculation)
                .getEffect(mockStatusEffectType, mockStatusEffectTypeTurnStartMagnitude,
                        mockCharacter);
        inOrder.verify(mockStatusEffectTypeTurnStart)
                .accompanyEffect(new int[]{STATUS_EFFECT_START_CALCULATED_EFFECT}, mockCharacter,
                        true);
        inOrder.verify(mockTargetVariableStatType)
                .alter(mockCharacter, STATUS_EFFECT_START_CALCULATED_EFFECT);
        inOrder.verify(mockStatusEffectTypeTurnStart)
                .otherEffects(new int[]{STATUS_EFFECT_START_CALCULATED_EFFECT}, mockCharacter,
                        true);
        //noinspection unchecked
        inOrder.verify(mockEffectCalculation)
                .getEffect(mockVariableStatType, mockVariableStatTypeTurnStartMagnitude,
                        mockCharacter);
        inOrder.verify(mockVariableStatTypeTurnStart)
                .accompanyEffect(new int[]{VARIABLE_STAT_START_CALCULATED_EFFECT}, mockCharacter,
                        true);
        inOrder.verify(mockTargetVariableStatType)
                .alter(mockCharacter, VARIABLE_STAT_START_CALCULATED_EFFECT);
        inOrder.verify(mockVariableStatTypeTurnStart)
                .otherEffects(new int[]{VARIABLE_STAT_START_CALCULATED_EFFECT}, mockCharacter,
                        true);
        inOrder.verify(mockCharacter).statusEffects();
        inOrder.verify(mockStatusEffectType).onTurnEnd();
        inOrder.verify(mockVariableStatTypes).iterator();
        inOrder.verify(mockVariableStatType).onTurnEnd();
        //noinspection unchecked
        inOrder.verify(mockEffectCalculation)
                .getEffect(mockStatusEffectType, mockStatusEffectTypeTurnEndMagnitude,
                        mockCharacter);
        inOrder.verify(mockStatusEffectTypeTurnEnd)
                .accompanyEffect(new int[]{STATUS_EFFECT_END_CALCULATED_EFFECT}, mockCharacter,
                        true);
        inOrder.verify(mockTargetVariableStatType)
                .alter(mockCharacter, STATUS_EFFECT_END_CALCULATED_EFFECT);
        inOrder.verify(mockStatusEffectTypeTurnEnd)
                .otherEffects(new int[]{STATUS_EFFECT_END_CALCULATED_EFFECT}, mockCharacter, true);
    }

    @Test
    void testHandlePlayerControlledCharacterTurnWithAdvancingMultipleRounds() {
        when(mockCharacter.getPlayerControlled()).thenReturn(true);

        turnHandling.runTurn(mockCharacter, mockTurnData, true);

        verify(mockCharacter, never()).getPlayerControlled();
        verify(mockPassControlToPlayer, never()).accept(any());
        var inOrder = Mockito.inOrder(mockCharacter, mockStatusEffects, mockStatusEffectType,
                mockVariableStatType, mockEffectCalculation, mockVariableStatTypes,
                mockStatusEffectTypeTurnStart, mockVariableStatTypeTurnStart,
                mockStatusEffectTypeTurnEnd, mockPassControlToPlayer, mockTargetVariableStatType);
        inOrder.verify(mockCharacter).statusEffects();
        inOrder.verify(mockStatusEffectType).onTurnStart();
        inOrder.verify(mockVariableStatTypes).iterator();
        inOrder.verify(mockVariableStatType).onTurnStart();
        //noinspection unchecked
        inOrder.verify(mockEffectCalculation)
                .getEffect(mockStatusEffectType, mockStatusEffectTypeTurnStartMagnitude,
                        mockCharacter);
        inOrder.verify(mockStatusEffectTypeTurnStart)
                .accompanyEffect(new int[]{STATUS_EFFECT_START_CALCULATED_EFFECT}, mockCharacter,
                        true);
        inOrder.verify(mockTargetVariableStatType)
                .alter(mockCharacter, STATUS_EFFECT_START_CALCULATED_EFFECT);
        inOrder.verify(mockStatusEffectTypeTurnStart)
                .otherEffects(new int[]{STATUS_EFFECT_START_CALCULATED_EFFECT}, mockCharacter,
                        true);
        //noinspection unchecked
        inOrder.verify(mockEffectCalculation)
                .getEffect(mockVariableStatType, mockVariableStatTypeTurnStartMagnitude,
                        mockCharacter);
        inOrder.verify(mockVariableStatTypeTurnStart)
                .accompanyEffect(new int[]{VARIABLE_STAT_START_CALCULATED_EFFECT}, mockCharacter,
                        true);
        inOrder.verify(mockTargetVariableStatType)
                .alter(mockCharacter, VARIABLE_STAT_START_CALCULATED_EFFECT);
        inOrder.verify(mockVariableStatTypeTurnStart)
                .otherEffects(new int[]{VARIABLE_STAT_START_CALCULATED_EFFECT}, mockCharacter,
                        true);
        inOrder.verify(mockCharacter).statusEffects();
        inOrder.verify(mockStatusEffectType).onTurnEnd();
        inOrder.verify(mockVariableStatTypes).iterator();
        inOrder.verify(mockVariableStatType).onTurnEnd();
        //noinspection unchecked
        inOrder.verify(mockEffectCalculation)
                .getEffect(mockStatusEffectType, mockStatusEffectTypeTurnEndMagnitude,
                        mockCharacter);
        inOrder.verify(mockStatusEffectTypeTurnEnd)
                .accompanyEffect(new int[]{STATUS_EFFECT_END_CALCULATED_EFFECT}, mockCharacter,
                        true);
        inOrder.verify(mockTargetVariableStatType)
                .alter(mockCharacter, STATUS_EFFECT_END_CALCULATED_EFFECT);
        inOrder.verify(mockStatusEffectTypeTurnEnd)
                .otherEffects(new int[]{STATUS_EFFECT_END_CALCULATED_EFFECT}, mockCharacter, true);
    }
}
