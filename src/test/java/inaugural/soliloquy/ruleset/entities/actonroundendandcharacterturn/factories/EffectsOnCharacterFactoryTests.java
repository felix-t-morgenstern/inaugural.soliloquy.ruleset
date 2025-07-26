package inaugural.soliloquy.ruleset.entities.actonroundendandcharacterturn.factories;

import inaugural.soliloquy.ruleset.definitions.EffectsOnCharacterDefinition;
import inaugural.soliloquy.ruleset.definitions.EffectsOnCharacterDefinition.MagnitudeForStatisticDefinition;
import inaugural.soliloquy.ruleset.definitions.StatisticChangeMagnitudeDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import soliloquy.specs.common.entities.Action;
import soliloquy.specs.gamestate.entities.Character;
import soliloquy.specs.ruleset.entities.actonroundendandcharacterturn.EffectsCharacterOnRoundOrTurnChange.EffectsOnCharacter;
import soliloquy.specs.ruleset.entities.actonroundendandcharacterturn.StatisticChangeMagnitude;

import java.util.function.Function;

import static inaugural.soliloquy.tools.collections.Collections.arrayOf;
import static inaugural.soliloquy.tools.testing.Assertions.once;
import static inaugural.soliloquy.tools.testing.Mock.LookupAndEntitiesWithId;
import static inaugural.soliloquy.tools.random.Random.randomInt;
import static inaugural.soliloquy.tools.random.Random.randomString;
import static inaugural.soliloquy.tools.testing.Mock.generateMockLookupFunctionWithId;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EffectsOnCharacterFactoryTests {
    private final int PRIORITY = randomInt();
    private final String VARIABLE_STAT_TYPE_ID = randomString();

    private final String ACCOMPANY_EFFECT_ACTION_ID = randomString();
    private final String OTHER_EFFECTS_ACTION_ID = randomString();
    @SuppressWarnings("rawtypes") private final LookupAndEntitiesWithId<Action>
            MOCK_ACTIONS_AND_LOOKUP =
            generateMockLookupFunctionWithId(Action.class, ACCOMPANY_EFFECT_ACTION_ID,
                    OTHER_EFFECTS_ACTION_ID);
    @SuppressWarnings("rawtypes") private final Action MOCK_ACCOMPANY_EFFECT_ACTION =
            MOCK_ACTIONS_AND_LOOKUP.entities.getFirst();
    @SuppressWarnings("rawtypes") private final Action MOCK_OTHER_EFFECTS_ACTION =
            MOCK_ACTIONS_AND_LOOKUP.entities.get(1);
    @SuppressWarnings("rawtypes") private final Function<String, Action> MOCK_GET_ACTION =
            MOCK_ACTIONS_AND_LOOKUP.lookup;

    @Mock private StatisticChangeMagnitudeDefinition mockMagnitudeDefinition;
    @SuppressWarnings("rawtypes")
    @Mock private StatisticChangeMagnitude mockMagnitude;
    @SuppressWarnings("rawtypes")
    @Mock private Function<StatisticChangeMagnitudeDefinition, StatisticChangeMagnitude>
            mockMagnitudeFactory;

    @Mock private Character mockCharacter;

    private EffectsOnCharacterDefinition definition;

    private Function<EffectsOnCharacterDefinition, EffectsOnCharacter> factory;

    @BeforeEach
    public void setUp() {
        lenient().when(mockMagnitudeFactory.apply(any())).thenReturn(mockMagnitude);

        definition = new EffectsOnCharacterDefinition(PRIORITY,
                new MagnitudeForStatisticDefinition[]{
                        new MagnitudeForStatisticDefinition(VARIABLE_STAT_TYPE_ID,
                                mockMagnitudeDefinition)
                }, ACCOMPANY_EFFECT_ACTION_ID, OTHER_EFFECTS_ACTION_ID);

        factory = new EffectsOnCharacterFactory(MOCK_GET_ACTION, mockMagnitudeFactory);
    }

    @Test
    public void testConstructorWithInvalidArgs() {
        assertThrows(IllegalArgumentException.class,
                () -> new EffectsOnCharacterFactory(null, mockMagnitudeFactory));
        assertThrows(IllegalArgumentException.class,
                () -> new EffectsOnCharacterFactory(MOCK_GET_ACTION, null));
    }

    @Test
    public void testMake() {
        var accompanyEffectActionCaptor = ArgumentCaptor.forClass(Object[].class);
        var otherEffectsActionCaptor = ArgumentCaptor.forClass(Object[].class);

        //noinspection unchecked
        doNothing().when(MOCK_ACCOMPANY_EFFECT_ACTION)
                .run(any(), accompanyEffectActionCaptor.capture());
        //noinspection unchecked
        doNothing().when(MOCK_OTHER_EFFECTS_ACTION).run(any());

        var magnitudesArr = new int[]{randomInt()};

        var output = factory.apply(definition);
        output.accompanyEffect(magnitudesArr, mockCharacter, true);
        output.otherEffects(magnitudesArr, mockCharacter, false);

        assertNotNull(output);
        var magnitudes = output.magnitudes();
        assertNotNull(magnitudes);
        assertNotSame(output.magnitudes(), magnitudes);
        verify(MOCK_GET_ACTION, once()).apply(ACCOMPANY_EFFECT_ACTION_ID);
        verify(MOCK_GET_ACTION, once()).apply(OTHER_EFFECTS_ACTION_ID);
        assertNotNull(accompanyEffectActionCaptor);
        //noinspection unchecked
        verify(MOCK_ACCOMPANY_EFFECT_ACTION, once()).run(accompanyEffectActionCaptor.capture());
        assertArrayEquals(arrayOf(magnitudesArr, mockCharacter, true),
                accompanyEffectActionCaptor.getValue());
        //noinspection unchecked
        verify(MOCK_OTHER_EFFECTS_ACTION, once()).run(otherEffectsActionCaptor.capture());
        assertArrayEquals(arrayOf(magnitudesArr, mockCharacter, false),
                otherEffectsActionCaptor.getValue());
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
        verify(MOCK_GET_ACTION, never()).apply(anyString());
        verify(MOCK_GET_ACTION, never()).apply(anyString());
        //noinspection unchecked
        verify(MOCK_ACCOMPANY_EFFECT_ACTION, never()).run(any());
        //noinspection unchecked
        verify(MOCK_OTHER_EFFECTS_ACTION, never()).run(any());
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
        when(MOCK_GET_ACTION.apply(invalidActionId)).thenReturn(null);

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
