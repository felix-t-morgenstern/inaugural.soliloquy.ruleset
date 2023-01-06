package inaugural.soliloquy.ruleset.entities.factories;

import inaugural.soliloquy.ruleset.entities.actonturnendandcharacterround.factories.EffectsOnCharacterFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import soliloquy.specs.common.entities.Action;
import soliloquy.specs.common.entities.Function;
import soliloquy.specs.common.factories.Factory;
import soliloquy.specs.common.valueobjects.Pair;
import soliloquy.specs.gamestate.entities.Character;
import soliloquy.specs.ruleset.definitions.EffectsOnCharacterDefinition;
import soliloquy.specs.ruleset.definitions.EffectsOnCharacterDefinition.MagnitudeForStatisticDefinition;
import soliloquy.specs.ruleset.definitions.StatisticChangeMagnitudeDefinition;
import soliloquy.specs.ruleset.entities.CharacterVariableStatisticType;
import soliloquy.specs.ruleset.entities.actonturnendandcharacterround.EffectsCharacterOnRoundOrTurnChange.EffectsOnCharacter;
import soliloquy.specs.ruleset.entities.actonturnendandcharacterround.StatisticChangeMagnitude;

import java.util.Map;

import static inaugural.soliloquy.tools.random.Random.randomInt;
import static inaugural.soliloquy.tools.random.Random.randomString;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class EffectsOnCharacterFactoryTests {
    private final String VARIABLE_STAT_TYPE_ID = randomString();
    private final String ACCOMPANY_EFFECT_ACTION_ID = randomString();
    private final String OTHER_EFFECTS_ACTION_ID = randomString();

    @Mock private CharacterVariableStatisticType mockVariableStatType;
    @Mock private Function<String, CharacterVariableStatisticType> mockGetVariableStatType;
    @Mock private StatisticChangeMagnitudeDefinition mockMagnitudeDefinition;
    @SuppressWarnings("rawtypes")
    @Mock private StatisticChangeMagnitude mockMagnitude;
    @SuppressWarnings("rawtypes")
    @Mock private Factory<StatisticChangeMagnitudeDefinition, StatisticChangeMagnitude>
            mockMagnitudeFactory;

    @Mock private Action<Pair<Integer, Character>> mockAccompanyEffectAction;
    @Mock private Action<Pair<Integer, Character>> mockOtherEffectsAction;
    @SuppressWarnings("rawtypes")
    @Mock private Function<String, Action> mockGetAction;

    @Mock private Character mockCharacter;

    private EffectsOnCharacterDefinition definition;

    private Factory<EffectsOnCharacterDefinition, EffectsOnCharacter> factory;

    @BeforeEach
    void setUp() {
        mockVariableStatType = mock(CharacterVariableStatisticType.class);

        //noinspection unchecked
        mockGetVariableStatType =
                (Function<String, CharacterVariableStatisticType>) mock(Function.class);
        when(mockGetVariableStatType.apply(anyString())).thenReturn(mockVariableStatType);

        mockMagnitudeDefinition = mock(StatisticChangeMagnitudeDefinition.class);

        mockMagnitude = mock(StatisticChangeMagnitude.class);

        //noinspection unchecked,rawtypes
        mockMagnitudeFactory =
                (Factory<StatisticChangeMagnitudeDefinition, StatisticChangeMagnitude>) mock(
                        Factory.class);
        when(mockMagnitudeFactory.make(any())).thenReturn(mockMagnitude);

        //noinspection unchecked
        mockAccompanyEffectAction = (Action<Pair<Integer, Character>>) mock(Action.class);
        //noinspection unchecked
        mockOtherEffectsAction = (Action<Pair<Integer, Character>>) mock(Action.class);

        //noinspection unchecked,rawtypes
        mockGetAction = (Function<String, Action>) mock(Function.class);
        when(mockGetAction.apply(ACCOMPANY_EFFECT_ACTION_ID)).thenReturn(mockAccompanyEffectAction);
        when(mockGetAction.apply(OTHER_EFFECTS_ACTION_ID)).thenReturn(mockOtherEffectsAction);

        mockCharacter = mock(Character.class);

        definition = new EffectsOnCharacterDefinition(new MagnitudeForStatisticDefinition[]{
                new MagnitudeForStatisticDefinition(VARIABLE_STAT_TYPE_ID, mockMagnitudeDefinition)
        }, ACCOMPANY_EFFECT_ACTION_ID, OTHER_EFFECTS_ACTION_ID);

        factory = new EffectsOnCharacterFactory(mockGetVariableStatType, mockGetAction,
                mockMagnitudeFactory);
    }

    @Test
    void testConstructorWithInvalidParams() {
        assertThrows(IllegalArgumentException.class,
                () -> new EffectsOnCharacterFactory(null, mockGetAction, mockMagnitudeFactory));
        assertThrows(IllegalArgumentException.class,
                () -> new EffectsOnCharacterFactory(mockGetVariableStatType, null,
                        mockMagnitudeFactory));
        assertThrows(IllegalArgumentException.class,
                () -> new EffectsOnCharacterFactory(mockGetVariableStatType, mockGetAction, null));
    }

    @Test
    void testMake() {
        int magnitude = randomInt();

        EffectsOnCharacter output = factory.make(definition);
        output.accompanyEffect(magnitude, mockCharacter);
        output.otherEffects(magnitude, mockCharacter);

        assertNotNull(output);
        //noinspection rawtypes
        Map<CharacterVariableStatisticType, StatisticChangeMagnitude> magnitudes =
                output.magnitudes();
        assertNotNull(magnitudes);
        assertNotSame(output.magnitudes(), magnitudes);
        verify(mockGetVariableStatType, times(1)).apply(VARIABLE_STAT_TYPE_ID);
        verify(mockGetAction, times(1)).apply(ACCOMPANY_EFFECT_ACTION_ID);
        verify(mockGetAction, times(1)).apply(OTHER_EFFECTS_ACTION_ID);
        verify(mockAccompanyEffectAction, times(1)).run(eq(Pair.of(magnitude, mockCharacter)));
        verify(mockOtherEffectsAction, times(1)).run(eq(Pair.of(magnitude, mockCharacter)));
    }

    @Test
    void testAccompanyEffectWithInvalidParams() {
        EffectsOnCharacter output = factory.make(definition);

        assertThrows(IllegalArgumentException.class,
                () -> output.accompanyEffect(randomInt(), null));
    }

    @Test
    void testOtherEffectsWithInvalidParams() {
        EffectsOnCharacter output = factory.make(definition);

        assertThrows(IllegalArgumentException.class, () -> output.otherEffects(randomInt(), null));
    }

    @Test
    void testMakeWithInvalidParams() {
        String invalidActionId = randomString();
        when(mockGetAction.apply(invalidActionId)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> factory.make(null));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new EffectsOnCharacterDefinition(null, ACCOMPANY_EFFECT_ACTION_ID,
                        OTHER_EFFECTS_ACTION_ID)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new EffectsOnCharacterDefinition(new MagnitudeForStatisticDefinition[]{
                        new MagnitudeForStatisticDefinition(null, mockMagnitudeDefinition)},
                        ACCOMPANY_EFFECT_ACTION_ID, OTHER_EFFECTS_ACTION_ID)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new EffectsOnCharacterDefinition(new MagnitudeForStatisticDefinition[]{
                        new MagnitudeForStatisticDefinition("", mockMagnitudeDefinition)},
                        ACCOMPANY_EFFECT_ACTION_ID, OTHER_EFFECTS_ACTION_ID)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new EffectsOnCharacterDefinition(new MagnitudeForStatisticDefinition[]{
                        new MagnitudeForStatisticDefinition(VARIABLE_STAT_TYPE_ID, null)},
                        ACCOMPANY_EFFECT_ACTION_ID, OTHER_EFFECTS_ACTION_ID)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new EffectsOnCharacterDefinition(new MagnitudeForStatisticDefinition[]{
                        new MagnitudeForStatisticDefinition(VARIABLE_STAT_TYPE_ID,
                                mockMagnitudeDefinition)}, null, OTHER_EFFECTS_ACTION_ID)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new EffectsOnCharacterDefinition(new MagnitudeForStatisticDefinition[]{
                        new MagnitudeForStatisticDefinition(VARIABLE_STAT_TYPE_ID,
                                mockMagnitudeDefinition)}, "", OTHER_EFFECTS_ACTION_ID)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new EffectsOnCharacterDefinition(new MagnitudeForStatisticDefinition[]{
                        new MagnitudeForStatisticDefinition(VARIABLE_STAT_TYPE_ID,
                                mockMagnitudeDefinition)}, invalidActionId,
                        OTHER_EFFECTS_ACTION_ID)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new EffectsOnCharacterDefinition(new MagnitudeForStatisticDefinition[]{
                        new MagnitudeForStatisticDefinition(VARIABLE_STAT_TYPE_ID,
                                mockMagnitudeDefinition)}, ACCOMPANY_EFFECT_ACTION_ID, null)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new EffectsOnCharacterDefinition(new MagnitudeForStatisticDefinition[]{
                        new MagnitudeForStatisticDefinition(VARIABLE_STAT_TYPE_ID,
                                mockMagnitudeDefinition)}, ACCOMPANY_EFFECT_ACTION_ID, "")));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new EffectsOnCharacterDefinition(new MagnitudeForStatisticDefinition[]{
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
