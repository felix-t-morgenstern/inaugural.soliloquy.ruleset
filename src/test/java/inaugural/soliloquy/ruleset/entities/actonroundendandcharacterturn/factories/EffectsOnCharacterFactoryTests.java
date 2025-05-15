package inaugural.soliloquy.ruleset.entities.actonroundendandcharacterturn.factories;

import inaugural.soliloquy.ruleset.definitions.EffectsOnCharacterDefinition;
import inaugural.soliloquy.ruleset.definitions.EffectsOnCharacterDefinition.MagnitudeForStatisticDefinition;
import inaugural.soliloquy.ruleset.definitions.StatisticChangeMagnitudeDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import soliloquy.specs.common.entities.Action;
import soliloquy.specs.gamestate.entities.Character;
import soliloquy.specs.ruleset.entities.actonroundendandcharacterturn.EffectsCharacterOnRoundOrTurnChange.EffectsOnCharacter;
import soliloquy.specs.ruleset.entities.actonroundendandcharacterturn.StatisticChangeMagnitude;

import java.util.function.Function;

import static inaugural.soliloquy.tools.collections.Collections.arrayOf;
import static inaugural.soliloquy.tools.random.Random.randomInt;
import static inaugural.soliloquy.tools.random.Random.randomString;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EffectsOnCharacterFactoryTests {
    private final int PRIORITY = randomInt();
    private final String VARIABLE_STAT_TYPE_ID = randomString();
    private final String ACCOMPANY_EFFECT_ACTION_ID = randomString();
    private final String OTHER_EFFECTS_ACTION_ID = randomString();

    @Mock private StatisticChangeMagnitudeDefinition mockMagnitudeDefinition;
    @SuppressWarnings("rawtypes")
    @Mock private StatisticChangeMagnitude mockMagnitude;
    @SuppressWarnings("rawtypes")
    @Mock private Function<StatisticChangeMagnitudeDefinition, StatisticChangeMagnitude>
            mockMagnitudeFactory;

    @Mock private Action<Object[]> mockAccompanyEffectAction;
    @Mock private Action<Object[]> mockOtherEffectsAction;
    @SuppressWarnings("rawtypes")
    @Mock private Function<String, Action> mockGetAction;

    @Mock private Character mockCharacter;

    private EffectsOnCharacterDefinition definition;

    private Function<EffectsOnCharacterDefinition, EffectsOnCharacter> factory;

    @BeforeEach
    public void setUp() {
        lenient().when(mockMagnitudeFactory.apply(any())).thenReturn(mockMagnitude);

        //noinspection unchecked
        mockAccompanyEffectAction = (Action<Object[]>) mock(Action.class);
        //noinspection unchecked
        mockOtherEffectsAction = (Action<Object[]>) mock(Action.class);

        //noinspection unchecked,rawtypes
        mockGetAction = (Function<String, Action>) mock(Function.class);
        lenient().when(mockGetAction.apply(ACCOMPANY_EFFECT_ACTION_ID))
                .thenReturn(mockAccompanyEffectAction);
        lenient().when(mockGetAction.apply(OTHER_EFFECTS_ACTION_ID))
                .thenReturn(mockOtherEffectsAction);

        mockCharacter = mock(Character.class);

        definition = new EffectsOnCharacterDefinition(PRIORITY,
                new MagnitudeForStatisticDefinition[]{
                        new MagnitudeForStatisticDefinition(VARIABLE_STAT_TYPE_ID,
                                mockMagnitudeDefinition)
                }, ACCOMPANY_EFFECT_ACTION_ID, OTHER_EFFECTS_ACTION_ID);

        factory = new EffectsOnCharacterFactory(mockGetAction, mockMagnitudeFactory);
    }

    @Test
    public void testConstructorWithInvalidArgs() {
        assertThrows(IllegalArgumentException.class,
                () -> new EffectsOnCharacterFactory(null, mockMagnitudeFactory));
        assertThrows(IllegalArgumentException.class,
                () -> new EffectsOnCharacterFactory(mockGetAction, null));
    }

    @Test
    public void testMake() {
        var magnitude = new int[]{randomInt()};

        var output = factory.apply(definition);
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
    public void testMakeWithNoAccompanyOrOtherEffects() {
        var definitionWithNoAccompanyOrOtherEffects = new EffectsOnCharacterDefinition(PRIORITY,
                new MagnitudeForStatisticDefinition[]{
                        new MagnitudeForStatisticDefinition(VARIABLE_STAT_TYPE_ID,
                                mockMagnitudeDefinition)
                }, "", "");
        var magnitude = new int[]{randomInt()};

        var output = factory.apply(definitionWithNoAccompanyOrOtherEffects);
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
    public void testAccompanyEffectWithInvalidArgs() {
        var output = factory.apply(definition);

        assertThrows(IllegalArgumentException.class,
                () -> output.accompanyEffect(null, mockCharacter, true));
        assertThrows(IllegalArgumentException.class,
                () -> output.accompanyEffect(new int[]{}, null, true));
    }

    @Test
    public void testOtherEffectsWithInvalidArgs() {
        var output = factory.apply(definition);

        assertThrows(IllegalArgumentException.class,
                () -> output.otherEffects(null, mockCharacter, true));
        assertThrows(IllegalArgumentException.class,
                () -> output.otherEffects(new int[]{}, null, true));
    }

    @Test
    public void testMakeWithInvalidArgs() {
        var invalidActionId = randomString();
        when(mockGetAction.apply(invalidActionId)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> factory.apply(null));
        assertThrows(IllegalArgumentException.class, () -> factory.apply(
                new EffectsOnCharacterDefinition(PRIORITY, null, ACCOMPANY_EFFECT_ACTION_ID,
                        OTHER_EFFECTS_ACTION_ID)));
        assertThrows(IllegalArgumentException.class, () -> factory.apply(
                new EffectsOnCharacterDefinition(PRIORITY, new MagnitudeForStatisticDefinition[]{
                        new MagnitudeForStatisticDefinition(null, mockMagnitudeDefinition)},
                        ACCOMPANY_EFFECT_ACTION_ID, OTHER_EFFECTS_ACTION_ID)));
        assertThrows(IllegalArgumentException.class, () -> factory.apply(
                new EffectsOnCharacterDefinition(PRIORITY, new MagnitudeForStatisticDefinition[]{
                        new MagnitudeForStatisticDefinition("", mockMagnitudeDefinition)},
                        ACCOMPANY_EFFECT_ACTION_ID, OTHER_EFFECTS_ACTION_ID)));
        assertThrows(IllegalArgumentException.class, () -> factory.apply(
                new EffectsOnCharacterDefinition(PRIORITY, new MagnitudeForStatisticDefinition[]{
                        new MagnitudeForStatisticDefinition(VARIABLE_STAT_TYPE_ID, null)},
                        ACCOMPANY_EFFECT_ACTION_ID, OTHER_EFFECTS_ACTION_ID)));
        assertThrows(IllegalArgumentException.class, () -> factory.apply(
                new EffectsOnCharacterDefinition(PRIORITY, new MagnitudeForStatisticDefinition[]{
                        new MagnitudeForStatisticDefinition(VARIABLE_STAT_TYPE_ID,
                                mockMagnitudeDefinition)}, invalidActionId,
                        OTHER_EFFECTS_ACTION_ID)));
        assertThrows(IllegalArgumentException.class, () -> factory.apply(
                new EffectsOnCharacterDefinition(PRIORITY, new MagnitudeForStatisticDefinition[]{
                        new MagnitudeForStatisticDefinition(VARIABLE_STAT_TYPE_ID,
                                mockMagnitudeDefinition)}, ACCOMPANY_EFFECT_ACTION_ID,
                        invalidActionId)));
    }
}
