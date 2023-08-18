package inaugural.soliloquy.ruleset.entities.actonroundendandcharacterturn.factories;

import inaugural.soliloquy.ruleset.definitions.RoundEndEffectsOnCharacterDefinition;
import inaugural.soliloquy.ruleset.definitions.StatisticChangeMagnitudeDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import soliloquy.specs.common.entities.Action;
import soliloquy.specs.common.factories.Factory;
import soliloquy.specs.common.valueobjects.Pair;
import soliloquy.specs.gamestate.entities.Character;
import soliloquy.specs.ruleset.entities.actonroundendandcharacterturn.EffectsCharacterOnRoundOrTurnChange.RoundEndEffectsOnCharacter;
import soliloquy.specs.ruleset.entities.actonroundendandcharacterturn.StatisticChangeMagnitude;

import java.util.function.Function;

import static inaugural.soliloquy.tools.collections.Collections.arrayOf;
import static inaugural.soliloquy.tools.collections.Collections.listOf;
import static inaugural.soliloquy.tools.random.Random.randomInt;
import static inaugural.soliloquy.tools.random.Random.randomString;
import static inaugural.soliloquy.tools.valueobjects.Pair.pairOf;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RoundEndEffectsOnCharacterFactoryTests {
    private final int PRIORITY = randomInt();
    private final String VARIABLE_STAT_TYPE_ID = randomString();
    private final String ACCOMPANY_EFFECT_ACTION_ID = randomString();
    private final String OTHER_EFFECTS_ACTION_ID = randomString();
    private final String ACCOMPANY_ALL_EFFECTS_ACTION_ID = randomString();

    @Mock private StatisticChangeMagnitudeDefinition mockMagnitudeDefinition;
    @SuppressWarnings("rawtypes")
    @Mock private StatisticChangeMagnitude mockMagnitude;
    @SuppressWarnings("rawtypes")
    @Mock private Factory<StatisticChangeMagnitudeDefinition, StatisticChangeMagnitude>
            mockMagnitudeFactory;

    @Mock private Action<Object[]> mockAccompanyEffectAction;
    @Mock private Action<Object[]> mockOtherEffectsAction;
    @Mock private Action<Object[]> mockAccompanyAllEffectsAction;
    @SuppressWarnings("rawtypes")
    @Mock private Function<String, Action> mockGetAction;

    @Mock private Character mockCharacter;

    private RoundEndEffectsOnCharacterDefinition definition;

    private Factory<RoundEndEffectsOnCharacterDefinition, RoundEndEffectsOnCharacter> factory;

    @BeforeEach
    void setUp() {
        mockMagnitudeDefinition = mock(StatisticChangeMagnitudeDefinition.class);

        mockMagnitude = mock(StatisticChangeMagnitude.class);

        //noinspection unchecked,rawtypes
        mockMagnitudeFactory =
                (Factory<StatisticChangeMagnitudeDefinition, StatisticChangeMagnitude>) mock(
                        Factory.class);
        when(mockMagnitudeFactory.make(any())).thenReturn(mockMagnitude);

        //noinspection unchecked
        mockAccompanyEffectAction = (Action<Object[]>) mock(Action.class);
        //noinspection unchecked
        mockOtherEffectsAction = (Action<Object[]>) mock(Action.class);
        //noinspection unchecked
        mockAccompanyAllEffectsAction = (Action<Object[]>) mock(Action.class);

        //noinspection unchecked,rawtypes
        mockGetAction = (Function<String, Action>) mock(Function.class);
        when(mockGetAction.apply(ACCOMPANY_EFFECT_ACTION_ID)).thenReturn(mockAccompanyEffectAction);
        when(mockGetAction.apply(OTHER_EFFECTS_ACTION_ID)).thenReturn(mockOtherEffectsAction);
        when(mockGetAction.apply(ACCOMPANY_ALL_EFFECTS_ACTION_ID)).thenReturn(
                mockAccompanyAllEffectsAction);

        mockCharacter = mock(Character.class);

        definition = new RoundEndEffectsOnCharacterDefinition(PRIORITY,
                new RoundEndEffectsOnCharacterDefinition.MagnitudeForStatisticDefinition[]{
                        new RoundEndEffectsOnCharacterDefinition.MagnitudeForStatisticDefinition(
                                VARIABLE_STAT_TYPE_ID,
                                mockMagnitudeDefinition)
                }, ACCOMPANY_EFFECT_ACTION_ID, OTHER_EFFECTS_ACTION_ID,
                ACCOMPANY_ALL_EFFECTS_ACTION_ID);

        factory = new RoundEndEffectsOnCharacterFactory(mockGetAction, mockMagnitudeFactory);
    }

    @Test
    void testConstructorWithInvalidParams() {
        assertThrows(IllegalArgumentException.class,
                () -> new RoundEndEffectsOnCharacterFactory(null, mockMagnitudeFactory));
        assertThrows(IllegalArgumentException.class,
                () -> new RoundEndEffectsOnCharacterFactory(mockGetAction, null));
    }

    @Test
    void testMake() {
        var inputMagnitudes = new int[]{randomInt()};

        var output = factory.make(definition);
        output.accompanyEffect(inputMagnitudes, mockCharacter, true);
        output.otherEffects(inputMagnitudes, mockCharacter, false);
        output.accompanyAllEffects(listOf(pairOf(inputMagnitudes, mockCharacter)), true);

        assertNotNull(output);
        var magnitudes = output.magnitudes();
        assertNotNull(magnitudes);
        assertNotSame(output.magnitudes(), magnitudes);
        verify(mockGetAction).apply(ACCOMPANY_EFFECT_ACTION_ID);
        verify(mockGetAction).apply(OTHER_EFFECTS_ACTION_ID);
        verify(mockAccompanyEffectAction).run(eq(arrayOf(inputMagnitudes, mockCharacter, true)));
        verify(mockOtherEffectsAction).run(eq(arrayOf(inputMagnitudes, mockCharacter, false)));
        verify(mockAccompanyAllEffectsAction).run(
                eq(arrayOf(listOf(pairOf(inputMagnitudes, mockCharacter)), true)));
    }

    @Test
    void testMakeWithNoAccompanyOrOtherEffects() {
        var definitionWithNoAccompanyOrOtherEffects =
                new RoundEndEffectsOnCharacterDefinition(PRIORITY,
                        new RoundEndEffectsOnCharacterDefinition.MagnitudeForStatisticDefinition[]{
                                new RoundEndEffectsOnCharacterDefinition.MagnitudeForStatisticDefinition(
                                        VARIABLE_STAT_TYPE_ID,
                                        mockMagnitudeDefinition)
                        }, "", "", "");
        var inputMagnitudes = new int[]{randomInt()};

        var output = factory.make(definitionWithNoAccompanyOrOtherEffects);
        output.accompanyEffect(inputMagnitudes, mockCharacter, true);
        output.otherEffects(inputMagnitudes, mockCharacter, false);
        output.accompanyAllEffects(listOf(pairOf(inputMagnitudes, mockCharacter)), true);

        assertNotNull(output);
        var magnitudes = output.magnitudes();
        assertNotNull(magnitudes);
        assertNotSame(output.magnitudes(), magnitudes);
        verify(mockGetAction, never()).apply(anyString());
        verify(mockGetAction, never()).apply(anyString());
        verify(mockAccompanyEffectAction, never()).run(any());
        verify(mockOtherEffectsAction, never()).run(any());
        verify(mockAccompanyAllEffectsAction, never()).run(any());
    }

    @Test
    void testAccompanyEffectWithInvalidParams() {
        var output = factory.make(definition);

        assertThrows(IllegalArgumentException.class,
                () -> output.accompanyEffect(null, mockCharacter, true));
        assertThrows(IllegalArgumentException.class,
                () -> output.accompanyEffect(new int[]{}, null, true));
    }

    @Test
    void testOtherEffectsWithInvalidParams() {
        var output = factory.make(definition);

        assertThrows(IllegalArgumentException.class,
                () -> output.otherEffects(null, mockCharacter, true));
        assertThrows(IllegalArgumentException.class,
                () -> output.otherEffects(new int[]{}, null, true));
    }

    @Test
    void testAccompanyAllEffectsWithInvalidParams() {
        var inputMagnitudes = new int[]{randomInt()};
        var output = factory.make(definition);

        assertThrows(IllegalArgumentException.class, () -> output.accompanyAllEffects(null, true));
        assertThrows(IllegalArgumentException.class,
                () -> output.accompanyAllEffects(listOf((Pair<int[], Character>) null), true));
        assertThrows(IllegalArgumentException.class, () -> output.accompanyAllEffects(
                listOf(pairOf(null, mockCharacter, new int[0], mockCharacter)), true));
        assertThrows(IllegalArgumentException.class, () -> output.accompanyAllEffects(
                listOf(pairOf(inputMagnitudes, null, new int[0], mockCharacter)), true));
    }

    @Test
    void testMakeWithInvalidParams() {
        var invalidActionId = randomString();
        when(mockGetAction.apply(invalidActionId)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> factory.make(null));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new RoundEndEffectsOnCharacterDefinition(PRIORITY, null, ACCOMPANY_EFFECT_ACTION_ID,
                        OTHER_EFFECTS_ACTION_ID, ACCOMPANY_ALL_EFFECTS_ACTION_ID)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new RoundEndEffectsOnCharacterDefinition(PRIORITY,
                        new RoundEndEffectsOnCharacterDefinition.MagnitudeForStatisticDefinition[]{
                                new RoundEndEffectsOnCharacterDefinition.MagnitudeForStatisticDefinition(
                                        null, mockMagnitudeDefinition)},
                        ACCOMPANY_EFFECT_ACTION_ID, OTHER_EFFECTS_ACTION_ID,
                        ACCOMPANY_ALL_EFFECTS_ACTION_ID)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new RoundEndEffectsOnCharacterDefinition(PRIORITY,
                        new RoundEndEffectsOnCharacterDefinition.MagnitudeForStatisticDefinition[]{
                                new RoundEndEffectsOnCharacterDefinition.MagnitudeForStatisticDefinition(
                                        "", mockMagnitudeDefinition)},
                        ACCOMPANY_EFFECT_ACTION_ID, OTHER_EFFECTS_ACTION_ID,
                        ACCOMPANY_ALL_EFFECTS_ACTION_ID)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new RoundEndEffectsOnCharacterDefinition(PRIORITY,
                        new RoundEndEffectsOnCharacterDefinition.MagnitudeForStatisticDefinition[]{
                                new RoundEndEffectsOnCharacterDefinition.MagnitudeForStatisticDefinition(
                                        VARIABLE_STAT_TYPE_ID, null)},
                        ACCOMPANY_EFFECT_ACTION_ID, OTHER_EFFECTS_ACTION_ID,
                        ACCOMPANY_ALL_EFFECTS_ACTION_ID)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new RoundEndEffectsOnCharacterDefinition(PRIORITY,
                        new RoundEndEffectsOnCharacterDefinition.MagnitudeForStatisticDefinition[]{
                                new RoundEndEffectsOnCharacterDefinition.MagnitudeForStatisticDefinition(
                                        VARIABLE_STAT_TYPE_ID,
                                        mockMagnitudeDefinition)}, invalidActionId,
                        OTHER_EFFECTS_ACTION_ID, ACCOMPANY_ALL_EFFECTS_ACTION_ID)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new RoundEndEffectsOnCharacterDefinition(PRIORITY,
                        new RoundEndEffectsOnCharacterDefinition.MagnitudeForStatisticDefinition[]{
                                new RoundEndEffectsOnCharacterDefinition.MagnitudeForStatisticDefinition(
                                        VARIABLE_STAT_TYPE_ID,
                                        mockMagnitudeDefinition)}, ACCOMPANY_EFFECT_ACTION_ID,
                        invalidActionId, ACCOMPANY_ALL_EFFECTS_ACTION_ID)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new RoundEndEffectsOnCharacterDefinition(PRIORITY,
                        new RoundEndEffectsOnCharacterDefinition.MagnitudeForStatisticDefinition[]{
                                new RoundEndEffectsOnCharacterDefinition.MagnitudeForStatisticDefinition(
                                        VARIABLE_STAT_TYPE_ID, mockMagnitudeDefinition)},
                        ACCOMPANY_EFFECT_ACTION_ID, OTHER_EFFECTS_ACTION_ID, invalidActionId)));
    }

    @Test
    void testGetInterfaceName() {
        assertEquals(Factory.class.getCanonicalName() + "<" +
                RoundEndEffectsOnCharacterDefinition.class.getCanonicalName() + "," +
                RoundEndEffectsOnCharacter.class.getCanonicalName() +
                ">", factory.getInterfaceName());
    }
}
