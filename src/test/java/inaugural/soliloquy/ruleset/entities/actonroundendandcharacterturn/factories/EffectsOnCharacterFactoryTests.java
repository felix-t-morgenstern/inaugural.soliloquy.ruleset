package inaugural.soliloquy.ruleset.entities.actonroundendandcharacterturn.factories;

import inaugural.soliloquy.ruleset.definitions.EffectsOnCharacterDefinition;
import inaugural.soliloquy.ruleset.definitions.EffectsOnCharacterDefinition.MagnitudeForStatisticDefinition;
import inaugural.soliloquy.ruleset.definitions.StatisticChangeMagnitudeDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import soliloquy.specs.common.entities.Action;
import soliloquy.specs.common.factories.Factory;
import soliloquy.specs.gamestate.entities.Character;
import soliloquy.specs.ruleset.entities.actonroundendandcharacterturn.EffectsCharacterOnRoundOrTurnChange.EffectsOnCharacter;
import soliloquy.specs.ruleset.entities.actonroundendandcharacterturn.StatisticChangeMagnitude;

import java.util.function.Function;

import static inaugural.soliloquy.tools.collections.Collections.arrayOf;
import static inaugural.soliloquy.tools.random.Random.randomInt;
import static inaugural.soliloquy.tools.random.Random.randomString;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EffectsOnCharacterFactoryTests {
    private final int PRIORITY = randomInt();
    private final String VARIABLE_STAT_TYPE_ID = randomString();
    private final String ACCOMPANY_EFFECT_ACTION_ID = randomString();
    private final String OTHER_EFFECTS_ACTION_ID = randomString();

    @Mock private StatisticChangeMagnitudeDefinition mockMagnitudeDefinition;
    @SuppressWarnings("rawtypes")
    @Mock private StatisticChangeMagnitude mockMagnitude;
    @SuppressWarnings("rawtypes")
    @Mock private Factory<StatisticChangeMagnitudeDefinition, StatisticChangeMagnitude>
            mockMagnitudeFactory;

    @Mock private Action<Object[]> mockAccompanyEffectAction;
    @Mock private Action<Object[]> mockOtherEffectsAction;
    @SuppressWarnings("rawtypes")
    @Mock private Function<String, Action> mockGetAction;

    @Mock private Character mockCharacter;

    private EffectsOnCharacterDefinition definition;

    private Factory<EffectsOnCharacterDefinition, EffectsOnCharacter> factory;

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

        //noinspection unchecked,rawtypes
        mockGetAction = (Function<String, Action>) mock(Function.class);
        when(mockGetAction.apply(ACCOMPANY_EFFECT_ACTION_ID)).thenReturn(mockAccompanyEffectAction);
        when(mockGetAction.apply(OTHER_EFFECTS_ACTION_ID)).thenReturn(mockOtherEffectsAction);

        mockCharacter = mock(Character.class);

        definition = new EffectsOnCharacterDefinition(PRIORITY,
                new MagnitudeForStatisticDefinition[]{
                        new MagnitudeForStatisticDefinition(VARIABLE_STAT_TYPE_ID,
                                mockMagnitudeDefinition)
                }, ACCOMPANY_EFFECT_ACTION_ID, OTHER_EFFECTS_ACTION_ID);

        factory = new EffectsOnCharacterFactory(mockGetAction, mockMagnitudeFactory);
    }

    @Test
    void testConstructorWithInvalidParams() {
        assertThrows(IllegalArgumentException.class,
                () -> new EffectsOnCharacterFactory(null, mockMagnitudeFactory));
        assertThrows(IllegalArgumentException.class,
                () -> new EffectsOnCharacterFactory(mockGetAction, null));
    }

    @Test
    void testMake() {
        var magnitude = new int[]{randomInt()};

        var output = factory.make(definition);
        output.accompanyEffect(magnitude, mockCharacter, true);
        output.otherEffects(magnitude, mockCharacter, false);

        assertNotNull(output);
        var magnitudes = output.magnitudes();
        assertNotNull(magnitudes);
        assertNotSame(output.magnitudes(), magnitudes);
        verify(mockGetAction).apply(ACCOMPANY_EFFECT_ACTION_ID);
        verify(mockGetAction).apply(OTHER_EFFECTS_ACTION_ID);
        verify(mockAccompanyEffectAction).run(eq(arrayOf(magnitude, mockCharacter, true)));
        verify(mockOtherEffectsAction).run(eq(arrayOf(magnitude, mockCharacter, false)));
    }

    @Test
    void testMakeWithNoAccompanyOrOtherEffects() {
        var definitionWithNoAccompanyOrOtherEffects = new EffectsOnCharacterDefinition(PRIORITY,
                new MagnitudeForStatisticDefinition[]{
                        new MagnitudeForStatisticDefinition(VARIABLE_STAT_TYPE_ID,
                                mockMagnitudeDefinition)
                }, "", "");
        var magnitude = new int[]{randomInt()};

        var output = factory.make(definitionWithNoAccompanyOrOtherEffects);
        output.accompanyEffect(magnitude, mockCharacter, true);
        output.otherEffects(magnitude, mockCharacter, false);

        assertNotNull(output);
        var magnitudes = output.magnitudes();
        assertNotNull(magnitudes);
        assertNotSame(output.magnitudes(), magnitudes);
        verify(mockGetAction, never()).apply(anyString());
        verify(mockGetAction, never()).apply(anyString());
        verify(mockAccompanyEffectAction, never()).run(any());
        verify(mockOtherEffectsAction, never()).run(any());
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

        assertThrows(IllegalArgumentException.class, () -> output.otherEffects(null, mockCharacter, true));
        assertThrows(IllegalArgumentException.class, () -> output.otherEffects(new int[]{}, null, true));
    }

    @Test
    void testMakeWithInvalidParams() {
        var invalidActionId = randomString();
        when(mockGetAction.apply(invalidActionId)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> factory.make(null));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new EffectsOnCharacterDefinition(PRIORITY, null, ACCOMPANY_EFFECT_ACTION_ID,
                        OTHER_EFFECTS_ACTION_ID)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new EffectsOnCharacterDefinition(PRIORITY, new MagnitudeForStatisticDefinition[]{
                        new MagnitudeForStatisticDefinition(null, mockMagnitudeDefinition)},
                        ACCOMPANY_EFFECT_ACTION_ID, OTHER_EFFECTS_ACTION_ID)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new EffectsOnCharacterDefinition(PRIORITY, new MagnitudeForStatisticDefinition[]{
                        new MagnitudeForStatisticDefinition("", mockMagnitudeDefinition)},
                        ACCOMPANY_EFFECT_ACTION_ID, OTHER_EFFECTS_ACTION_ID)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new EffectsOnCharacterDefinition(PRIORITY, new MagnitudeForStatisticDefinition[]{
                        new MagnitudeForStatisticDefinition(VARIABLE_STAT_TYPE_ID, null)},
                        ACCOMPANY_EFFECT_ACTION_ID, OTHER_EFFECTS_ACTION_ID)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new EffectsOnCharacterDefinition(PRIORITY, new MagnitudeForStatisticDefinition[]{
                        new MagnitudeForStatisticDefinition(VARIABLE_STAT_TYPE_ID,
                                mockMagnitudeDefinition)}, invalidActionId,
                        OTHER_EFFECTS_ACTION_ID)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new EffectsOnCharacterDefinition(PRIORITY, new MagnitudeForStatisticDefinition[]{
                        new MagnitudeForStatisticDefinition(VARIABLE_STAT_TYPE_ID,
                                mockMagnitudeDefinition)}, ACCOMPANY_EFFECT_ACTION_ID,
                        invalidActionId)));
    }

    @Test
    void testGetInterfaceName() {
        assertEquals(Factory.class.getCanonicalName() + "<" +
                EffectsOnCharacterDefinition.class.getCanonicalName() + "," +
                EffectsOnCharacter.class.getCanonicalName() + ">", factory.getInterfaceName());
    }
}
