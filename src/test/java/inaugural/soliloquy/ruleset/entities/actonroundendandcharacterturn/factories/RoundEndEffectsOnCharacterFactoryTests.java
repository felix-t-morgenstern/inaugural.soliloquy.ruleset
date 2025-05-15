package inaugural.soliloquy.ruleset.entities.actonroundendandcharacterturn.factories;

import inaugural.soliloquy.ruleset.definitions.RoundEndEffectsOnCharacterDefinition;
import inaugural.soliloquy.ruleset.definitions.StatisticChangeMagnitudeDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import soliloquy.specs.common.entities.Action;
import soliloquy.specs.common.valueobjects.Pair;
import soliloquy.specs.gamestate.entities.Character;
import soliloquy.specs.ruleset.entities.actonroundendandcharacterturn.EffectsCharacterOnRoundOrTurnChange.RoundEndEffectsOnCharacter;
import soliloquy.specs.ruleset.entities.actonroundendandcharacterturn.StatisticChangeMagnitude;

import java.util.function.Function;

import static inaugural.soliloquy.tools.collections.Collections.arrayOf;
import static inaugural.soliloquy.tools.collections.Collections.listOf;
import static inaugural.soliloquy.tools.random.Random.randomInt;
import static inaugural.soliloquy.tools.random.Random.randomString;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static soliloquy.specs.common.valueobjects.Pair.pairOf;

@ExtendWith(MockitoExtension.class)
public class RoundEndEffectsOnCharacterFactoryTests {
    private final int PRIORITY = randomInt();
    private final String VARIABLE_STAT_TYPE_ID = randomString();
    private final String ACCOMPANY_EFFECT_ACTION_ID = randomString();
    private final String OTHER_EFFECTS_ACTION_ID = randomString();
    private final String ACCOMPANY_ALL_EFFECTS_ACTION_ID = randomString();

    @Mock private StatisticChangeMagnitudeDefinition mockMagnitudeDefinition;
    @SuppressWarnings("rawtypes")
    @Mock private StatisticChangeMagnitude mockMagnitude;
    @SuppressWarnings("rawtypes")
    @Mock private Function<StatisticChangeMagnitudeDefinition, StatisticChangeMagnitude>
            mockMagnitudeFactory;

    @Mock private Action<Object[]> mockAccompanyEffectAction;
    @Mock private Action<Object[]> mockOtherEffectsAction;
    @Mock private Action<Object[]> mockAccompanyAllEffectsAction;
    @SuppressWarnings("rawtypes")
    @Mock private Function<String, Action> mockGetAction;

    @Mock private Character mockCharacter;

    private RoundEndEffectsOnCharacterDefinition definition;

    private Function<RoundEndEffectsOnCharacterDefinition, RoundEndEffectsOnCharacter> factory;

    @BeforeEach
    public void setUp() {
        lenient().when(mockMagnitudeFactory.apply(any())).thenReturn(mockMagnitude);

        lenient().when(mockGetAction.apply(ACCOMPANY_EFFECT_ACTION_ID))
                .thenReturn(mockAccompanyEffectAction);
        lenient().when(mockGetAction.apply(OTHER_EFFECTS_ACTION_ID))
                .thenReturn(mockOtherEffectsAction);
        lenient().when(mockGetAction.apply(ACCOMPANY_ALL_EFFECTS_ACTION_ID)).thenReturn(
                mockAccompanyAllEffectsAction);

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
    public void testConstructorWithInvalidArgs() {
        assertThrows(IllegalArgumentException.class,
                () -> new RoundEndEffectsOnCharacterFactory(null, mockMagnitudeFactory));
        assertThrows(IllegalArgumentException.class,
                () -> new RoundEndEffectsOnCharacterFactory(mockGetAction, null));
    }

    @Test
    public void testMake() {
        var inputMagnitudes = new int[]{randomInt()};

        var output = factory.apply(definition);
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
    public void testMakeWithNoAccompanyOrOtherEffects() {
        var definitionWithNoAccompanyOrOtherEffects =
                new RoundEndEffectsOnCharacterDefinition(PRIORITY,
                        new RoundEndEffectsOnCharacterDefinition.MagnitudeForStatisticDefinition[]{
                                new RoundEndEffectsOnCharacterDefinition.MagnitudeForStatisticDefinition(
                                        VARIABLE_STAT_TYPE_ID,
                                        mockMagnitudeDefinition)
                        }, "", "", "");
        var inputMagnitudes = new int[]{randomInt()};

        var output = factory.apply(definitionWithNoAccompanyOrOtherEffects);
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
    public void testAccompanyAllEffectsWithInvalidArgs() {
        var inputMagnitudes = new int[]{randomInt()};
        var output = factory.apply(definition);

        assertThrows(IllegalArgumentException.class, () -> output.accompanyAllEffects(null, true));
        assertThrows(IllegalArgumentException.class,
                () -> output.accompanyAllEffects(listOf((Pair<int[], Character>) null), true));
        assertThrows(IllegalArgumentException.class, () -> output.accompanyAllEffects(
                listOf(pairOf(null, mockCharacter)), true));
        assertThrows(IllegalArgumentException.class, () -> output.accompanyAllEffects(
                listOf(pairOf(inputMagnitudes, null)), true));
    }

    @Test
    public void testMakeWithInvalidArgs() {
        var invalidActionId = randomString();
        when(mockGetAction.apply(invalidActionId)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> factory.apply(null));
        assertThrows(IllegalArgumentException.class, () -> factory.apply(
                new RoundEndEffectsOnCharacterDefinition(PRIORITY, null, ACCOMPANY_EFFECT_ACTION_ID,
                        OTHER_EFFECTS_ACTION_ID, ACCOMPANY_ALL_EFFECTS_ACTION_ID)));
        assertThrows(IllegalArgumentException.class, () -> factory.apply(
                new RoundEndEffectsOnCharacterDefinition(PRIORITY,
                        new RoundEndEffectsOnCharacterDefinition.MagnitudeForStatisticDefinition[]{
                                new RoundEndEffectsOnCharacterDefinition.MagnitudeForStatisticDefinition(
                                        null, mockMagnitudeDefinition)},
                        ACCOMPANY_EFFECT_ACTION_ID, OTHER_EFFECTS_ACTION_ID,
                        ACCOMPANY_ALL_EFFECTS_ACTION_ID)));
        assertThrows(IllegalArgumentException.class, () -> factory.apply(
                new RoundEndEffectsOnCharacterDefinition(PRIORITY,
                        new RoundEndEffectsOnCharacterDefinition.MagnitudeForStatisticDefinition[]{
                                new RoundEndEffectsOnCharacterDefinition.MagnitudeForStatisticDefinition(
                                        "", mockMagnitudeDefinition)},
                        ACCOMPANY_EFFECT_ACTION_ID, OTHER_EFFECTS_ACTION_ID,
                        ACCOMPANY_ALL_EFFECTS_ACTION_ID)));
        assertThrows(IllegalArgumentException.class, () -> factory.apply(
                new RoundEndEffectsOnCharacterDefinition(PRIORITY,
                        new RoundEndEffectsOnCharacterDefinition.MagnitudeForStatisticDefinition[]{
                                new RoundEndEffectsOnCharacterDefinition.MagnitudeForStatisticDefinition(
                                        VARIABLE_STAT_TYPE_ID, null)},
                        ACCOMPANY_EFFECT_ACTION_ID, OTHER_EFFECTS_ACTION_ID,
                        ACCOMPANY_ALL_EFFECTS_ACTION_ID)));
        assertThrows(IllegalArgumentException.class, () -> factory.apply(
                new RoundEndEffectsOnCharacterDefinition(PRIORITY,
                        new RoundEndEffectsOnCharacterDefinition.MagnitudeForStatisticDefinition[]{
                                new RoundEndEffectsOnCharacterDefinition.MagnitudeForStatisticDefinition(
                                        VARIABLE_STAT_TYPE_ID,
                                        mockMagnitudeDefinition)}, invalidActionId,
                        OTHER_EFFECTS_ACTION_ID, ACCOMPANY_ALL_EFFECTS_ACTION_ID)));
        assertThrows(IllegalArgumentException.class, () -> factory.apply(
                new RoundEndEffectsOnCharacterDefinition(PRIORITY,
                        new RoundEndEffectsOnCharacterDefinition.MagnitudeForStatisticDefinition[]{
                                new RoundEndEffectsOnCharacterDefinition.MagnitudeForStatisticDefinition(
                                        VARIABLE_STAT_TYPE_ID,
                                        mockMagnitudeDefinition)}, ACCOMPANY_EFFECT_ACTION_ID,
                        invalidActionId, ACCOMPANY_ALL_EFFECTS_ACTION_ID)));
        assertThrows(IllegalArgumentException.class, () -> factory.apply(
                new RoundEndEffectsOnCharacterDefinition(PRIORITY,
                        new RoundEndEffectsOnCharacterDefinition.MagnitudeForStatisticDefinition[]{
                                new RoundEndEffectsOnCharacterDefinition.MagnitudeForStatisticDefinition(
                                        VARIABLE_STAT_TYPE_ID, mockMagnitudeDefinition)},
                        ACCOMPANY_EFFECT_ACTION_ID, OTHER_EFFECTS_ACTION_ID, invalidActionId)));
    }
}
