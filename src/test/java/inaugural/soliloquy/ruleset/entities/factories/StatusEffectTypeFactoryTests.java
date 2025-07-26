package inaugural.soliloquy.ruleset.entities.factories;

import inaugural.soliloquy.ruleset.definitions.EffectsOnCharacterDefinition;
import inaugural.soliloquy.ruleset.definitions.RoundEndEffectsOnCharacterDefinition;
import inaugural.soliloquy.ruleset.definitions.StatusEffectTypeDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import soliloquy.specs.common.entities.Action;
import soliloquy.specs.common.valueobjects.Pair;
import soliloquy.specs.gamestate.entities.Character;
import soliloquy.specs.io.graphics.assets.ImageAsset;
import soliloquy.specs.ruleset.entities.actonroundendandcharacterturn.EffectsCharacterOnRoundOrTurnChange.EffectsOnCharacter;
import soliloquy.specs.ruleset.entities.actonroundendandcharacterturn.EffectsCharacterOnRoundOrTurnChange.RoundEndEffectsOnCharacter;
import soliloquy.specs.ruleset.entities.character.StaticStatisticType;
import soliloquy.specs.ruleset.entities.character.StatusEffectType;

import java.util.function.Function;

import static inaugural.soliloquy.tools.collections.Collections.arrayOf;
import static inaugural.soliloquy.tools.random.Random.*;
import static inaugural.soliloquy.tools.testing.Assertions.once;
import static inaugural.soliloquy.tools.testing.Mock.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static soliloquy.specs.common.valueobjects.Pair.pairOf;

@ExtendWith(MockitoExtension.class)
public class StatusEffectTypeFactoryTests {
    private final String ID = randomString();
    private final String NAME = randomString();
    private final boolean STOPS_AT_ZERO = randomBoolean();
    private final String NAME_AT_VALUE_FUNCTION_ID = randomString();
    private final String ICON_TYPE_1 = randomString();
    private final String ICON_TYPE_2 = randomString();
    private final String ICON_TYPE_FUNCTION_1_ID = randomString();
    private final String ICON_TYPE_FUNCTION_2_ID = randomString();
    private final String RESISTANCE_STAT_TYPE_ID = randomString();

    private final String ALTER_ACTION_ID = randomString();
    @SuppressWarnings("rawtypes") private final LookupAndEntitiesWithId<Action> MOCK_ACTION_AND_LOOKUP = generateMockLookupFunctionWithId(Action.class, ALTER_ACTION_ID);
    @SuppressWarnings("rawtypes") private final Action MOCK_ALTER_ACTION = MOCK_ACTION_AND_LOOKUP.entities.getFirst();
    @SuppressWarnings("rawtypes")
    private final Function<String, Action> MOCK_GET_ACTION = MOCK_ACTION_AND_LOOKUP.lookup;

    @Mock private Character mockCharacter;
    @Mock private StaticStatisticType mockResistanceStatType;
    @Mock private Function<Integer, String> mockNameAtValueFunction;
    @Mock private Function<Character, Pair<ImageAsset, Integer>> mockIconTypeFunction1;
    @Mock private Function<Character, Pair<ImageAsset, Integer>> mockIconTypeFunction2;
    private Function<String, StaticStatisticType> mockGetStaticStatType;
    @SuppressWarnings("rawtypes")
    private Function<String, Function> mockGetFunction;


    @Mock private RoundEndEffectsOnCharacterDefinition mockRoundEndEffectDefinition;
    @Mock private EffectsOnCharacterDefinition mockTurnStartEffectDefinition;
    @Mock private EffectsOnCharacterDefinition mockTurnEndEffectDefinition;
    @Mock private RoundEndEffectsOnCharacter mockRoundEndEffect;
    @Mock private EffectsOnCharacter mockTurnStartEffect;
    @Mock private EffectsOnCharacter mockTurnEndEffect;
    @Mock private Function<EffectsOnCharacterDefinition, EffectsOnCharacter>
            mockEffectsOnCharacterFactory;
    @Mock private Function<RoundEndEffectsOnCharacterDefinition, RoundEndEffectsOnCharacter>
            mockRoundEndEffectsOnCharacterFactory;

    private StatusEffectTypeDefinition definition;

    private Function<StatusEffectTypeDefinition, StatusEffectType> statusEffectTypeFactory;

    @BeforeEach
    public void setUp() {
        mockGetFunction = generateMockLookupFunction(
                pairOf(NAME_AT_VALUE_FUNCTION_ID, mockNameAtValueFunction),
                pairOf(ICON_TYPE_FUNCTION_1_ID, mockIconTypeFunction1),
                pairOf(ICON_TYPE_FUNCTION_2_ID, mockIconTypeFunction2)
        );

        mockGetStaticStatType = generateMockLookupFunction(
                pairOf(RESISTANCE_STAT_TYPE_ID, mockResistanceStatType));

        lenient().when(MOCK_GET_ACTION.apply(anyString())).thenReturn(MOCK_ALTER_ACTION);

        lenient().when(mockEffectsOnCharacterFactory.apply(mockTurnStartEffectDefinition))
                .thenReturn(mockTurnStartEffect);
        lenient().when(mockEffectsOnCharacterFactory.apply(mockTurnEndEffectDefinition))
                .thenReturn(mockTurnEndEffect);

        lenient().when(mockRoundEndEffectsOnCharacterFactory.apply(mockRoundEndEffectDefinition))
                .thenReturn(mockRoundEndEffect);

        definition =
                new StatusEffectTypeDefinition(ID, NAME, STOPS_AT_ZERO, NAME_AT_VALUE_FUNCTION_ID,
                        arrayOf(new StatusEffectTypeDefinition.IconForCharacterFunction(ICON_TYPE_1,
                                        ICON_TYPE_FUNCTION_1_ID),
                                new StatusEffectTypeDefinition.IconForCharacterFunction(ICON_TYPE_2,
                                        ICON_TYPE_FUNCTION_2_ID)), ALTER_ACTION_ID,
                        mockRoundEndEffectDefinition, mockTurnStartEffectDefinition,
                        mockTurnEndEffectDefinition, RESISTANCE_STAT_TYPE_ID);

        statusEffectTypeFactory =
                new StatusEffectTypeFactory(mockGetFunction, MOCK_GET_ACTION,
                        mockEffectsOnCharacterFactory, mockRoundEndEffectsOnCharacterFactory,
                        mockGetStaticStatType);
    }

    @Test
    public void testConstructorWithInvalidArgs() {
        assertThrows(IllegalArgumentException.class,
                () -> new StatusEffectTypeFactory(null, MOCK_GET_ACTION,
                        mockEffectsOnCharacterFactory, mockRoundEndEffectsOnCharacterFactory,
                        mockGetStaticStatType));
        assertThrows(IllegalArgumentException.class,
                () -> new StatusEffectTypeFactory(mockGetFunction, null,
                        mockEffectsOnCharacterFactory, mockRoundEndEffectsOnCharacterFactory,
                        mockGetStaticStatType));
        assertThrows(IllegalArgumentException.class,
                () -> new StatusEffectTypeFactory(mockGetFunction, MOCK_GET_ACTION, null,
                        mockRoundEndEffectsOnCharacterFactory, mockGetStaticStatType));
        assertThrows(IllegalArgumentException.class,
                () -> new StatusEffectTypeFactory(mockGetFunction, MOCK_GET_ACTION,
                        mockEffectsOnCharacterFactory, null, mockGetStaticStatType));
        assertThrows(IllegalArgumentException.class,
                () -> new StatusEffectTypeFactory(mockGetFunction, MOCK_GET_ACTION,
                        mockEffectsOnCharacterFactory, mockRoundEndEffectsOnCharacterFactory,
                        null));
    }

    @Test
    public void testMake() {
        var alterActionCaptor = ArgumentCaptor.forClass(Object[].class);
        //noinspection unchecked
        doNothing().when(MOCK_ALTER_ACTION).run(any(), alterActionCaptor.capture());

        var value = randomInt();

        var output = statusEffectTypeFactory.apply(definition);
        output.nameAtValue(value);
        output.getIcon(ICON_TYPE_1, mockCharacter);
        output.getIcon(ICON_TYPE_2, mockCharacter);
        output.alterValue(mockCharacter, value);

        assertNotNull(output);
        assertEquals(ID, output.id());
        assertEquals(NAME, output.getName());
        assertEquals(STOPS_AT_ZERO, output.stopsAtZero());
        verify(mockGetFunction).apply(NAME_AT_VALUE_FUNCTION_ID);
        verify(mockGetFunction).apply(ICON_TYPE_FUNCTION_1_ID);
        verify(mockGetFunction).apply(ICON_TYPE_FUNCTION_2_ID);
        verify(MOCK_GET_ACTION).apply(ALTER_ACTION_ID);
        verify(mockNameAtValueFunction).apply(value);
        verify(mockIconTypeFunction1).apply(mockCharacter);
        verify(mockIconTypeFunction2).apply(mockCharacter);
        //noinspection unchecked
        verify(MOCK_ALTER_ACTION, once()).run(alterActionCaptor.capture());
        assertArrayEquals(arrayOf(mockCharacter, value), alterActionCaptor.getValue());
        assertSame(mockRoundEndEffect, output.onRoundEnd());
        verify(mockRoundEndEffectsOnCharacterFactory).apply(mockRoundEndEffectDefinition);
        assertSame(mockTurnStartEffect, output.onTurnStart());
        verify(mockEffectsOnCharacterFactory).apply(mockTurnStartEffectDefinition);
        assertSame(mockTurnEndEffect, output.onTurnEnd());
        verify(mockEffectsOnCharacterFactory).apply(mockTurnEndEffectDefinition);
        verify(mockGetStaticStatType).apply(RESISTANCE_STAT_TYPE_ID);
        assertSame(mockResistanceStatType, output.resistanceStatisticType());
    }

    @Test
    public void testMakeWithInvalidArgs() {
        var invalidFunctionId = randomString();
        when(mockGetFunction.apply(invalidFunctionId)).thenReturn(null);
        var invalidActionId = randomString();
        when(MOCK_GET_ACTION.apply(invalidActionId)).thenReturn(null);
        var invalidStatId = randomString();

        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.apply(null));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.apply(
                new StatusEffectTypeDefinition(null, NAME, STOPS_AT_ZERO, NAME_AT_VALUE_FUNCTION_ID,
                        arrayOf(new StatusEffectTypeDefinition.IconForCharacterFunction(ICON_TYPE_1,
                                ICON_TYPE_FUNCTION_1_ID)), ALTER_ACTION_ID,
                        mockRoundEndEffectDefinition, mockTurnStartEffectDefinition,
                        mockTurnEndEffectDefinition, RESISTANCE_STAT_TYPE_ID)));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.apply(
                new StatusEffectTypeDefinition("", NAME, STOPS_AT_ZERO, NAME_AT_VALUE_FUNCTION_ID,
                        arrayOf(new StatusEffectTypeDefinition.IconForCharacterFunction(ICON_TYPE_1,
                                ICON_TYPE_FUNCTION_1_ID)), ALTER_ACTION_ID,
                        mockRoundEndEffectDefinition, mockTurnStartEffectDefinition,
                        mockTurnEndEffectDefinition, RESISTANCE_STAT_TYPE_ID)));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.apply(
                new StatusEffectTypeDefinition(ID, null, STOPS_AT_ZERO, NAME_AT_VALUE_FUNCTION_ID,
                        arrayOf(new StatusEffectTypeDefinition.IconForCharacterFunction(ICON_TYPE_1,
                                ICON_TYPE_FUNCTION_1_ID)), ALTER_ACTION_ID,
                        mockRoundEndEffectDefinition, mockTurnStartEffectDefinition,
                        mockTurnEndEffectDefinition, RESISTANCE_STAT_TYPE_ID)));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.apply(
                new StatusEffectTypeDefinition(ID, "", STOPS_AT_ZERO, NAME_AT_VALUE_FUNCTION_ID,
                        arrayOf(new StatusEffectTypeDefinition.IconForCharacterFunction(ICON_TYPE_1,
                                ICON_TYPE_FUNCTION_1_ID)), ALTER_ACTION_ID,
                        mockRoundEndEffectDefinition, mockTurnStartEffectDefinition,
                        mockTurnEndEffectDefinition, RESISTANCE_STAT_TYPE_ID)));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.apply(
                new StatusEffectTypeDefinition(ID, NAME, STOPS_AT_ZERO, null,
                        arrayOf(new StatusEffectTypeDefinition.IconForCharacterFunction(ICON_TYPE_1,
                                ICON_TYPE_FUNCTION_1_ID)), ALTER_ACTION_ID,
                        mockRoundEndEffectDefinition, mockTurnStartEffectDefinition,
                        mockTurnEndEffectDefinition, RESISTANCE_STAT_TYPE_ID)));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.apply(
                new StatusEffectTypeDefinition(ID, NAME, STOPS_AT_ZERO, "",
                        arrayOf(new StatusEffectTypeDefinition.IconForCharacterFunction(ICON_TYPE_1,
                                ICON_TYPE_FUNCTION_1_ID)), ALTER_ACTION_ID,
                        mockRoundEndEffectDefinition, mockTurnStartEffectDefinition,
                        mockTurnEndEffectDefinition, RESISTANCE_STAT_TYPE_ID)));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.apply(
                new StatusEffectTypeDefinition(ID, NAME, STOPS_AT_ZERO, invalidFunctionId,
                        arrayOf(new StatusEffectTypeDefinition.IconForCharacterFunction(ICON_TYPE_1,
                                ICON_TYPE_FUNCTION_1_ID)), ALTER_ACTION_ID,
                        mockRoundEndEffectDefinition, mockTurnStartEffectDefinition,
                        mockTurnEndEffectDefinition, RESISTANCE_STAT_TYPE_ID)));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.apply(
                new StatusEffectTypeDefinition(ID, NAME, STOPS_AT_ZERO, NAME_AT_VALUE_FUNCTION_ID,
                        null, ALTER_ACTION_ID, mockRoundEndEffectDefinition,
                        mockTurnStartEffectDefinition, mockTurnEndEffectDefinition,
                        RESISTANCE_STAT_TYPE_ID)));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.apply(
                new StatusEffectTypeDefinition(ID, NAME, STOPS_AT_ZERO, NAME_AT_VALUE_FUNCTION_ID,
                        arrayOf((StatusEffectTypeDefinition.IconForCharacterFunction) null),
                        ALTER_ACTION_ID, mockRoundEndEffectDefinition,
                        mockTurnStartEffectDefinition, mockTurnEndEffectDefinition,
                        RESISTANCE_STAT_TYPE_ID)));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.apply(
                new StatusEffectTypeDefinition(ID, NAME, STOPS_AT_ZERO, NAME_AT_VALUE_FUNCTION_ID,
                        arrayOf(new StatusEffectTypeDefinition.IconForCharacterFunction(null,
                                ICON_TYPE_FUNCTION_1_ID)), ALTER_ACTION_ID,
                        mockRoundEndEffectDefinition, mockTurnStartEffectDefinition,
                        mockTurnEndEffectDefinition, RESISTANCE_STAT_TYPE_ID)));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.apply(
                new StatusEffectTypeDefinition(ID, NAME, STOPS_AT_ZERO, NAME_AT_VALUE_FUNCTION_ID,
                        arrayOf(new StatusEffectTypeDefinition.IconForCharacterFunction("",
                                ICON_TYPE_FUNCTION_1_ID)), ALTER_ACTION_ID,
                        mockRoundEndEffectDefinition, mockTurnStartEffectDefinition,
                        mockTurnEndEffectDefinition, RESISTANCE_STAT_TYPE_ID)));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.apply(
                new StatusEffectTypeDefinition(ID, NAME, STOPS_AT_ZERO, NAME_AT_VALUE_FUNCTION_ID,
                        arrayOf(new StatusEffectTypeDefinition.IconForCharacterFunction(ICON_TYPE_1,
                                null)), ALTER_ACTION_ID, mockRoundEndEffectDefinition,
                        mockTurnStartEffectDefinition, mockTurnEndEffectDefinition,
                        RESISTANCE_STAT_TYPE_ID)));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.apply(
                new StatusEffectTypeDefinition(ID, NAME, STOPS_AT_ZERO, NAME_AT_VALUE_FUNCTION_ID,
                        arrayOf(new StatusEffectTypeDefinition.IconForCharacterFunction(ICON_TYPE_1,
                                "")), ALTER_ACTION_ID, mockRoundEndEffectDefinition,
                        mockTurnStartEffectDefinition, mockTurnEndEffectDefinition,
                        RESISTANCE_STAT_TYPE_ID)));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.apply(
                new StatusEffectTypeDefinition(ID, NAME, STOPS_AT_ZERO, NAME_AT_VALUE_FUNCTION_ID,
                        arrayOf(new StatusEffectTypeDefinition.IconForCharacterFunction(ICON_TYPE_1,
                                invalidFunctionId)), ALTER_ACTION_ID,
                        mockRoundEndEffectDefinition, mockTurnStartEffectDefinition,
                        mockTurnEndEffectDefinition, RESISTANCE_STAT_TYPE_ID)));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.apply(
                new StatusEffectTypeDefinition(ID, NAME, STOPS_AT_ZERO, NAME_AT_VALUE_FUNCTION_ID,
                        arrayOf(new StatusEffectTypeDefinition.IconForCharacterFunction(ICON_TYPE_1,
                                ICON_TYPE_FUNCTION_1_ID)), null, mockRoundEndEffectDefinition,
                        mockTurnStartEffectDefinition, mockTurnEndEffectDefinition,
                        RESISTANCE_STAT_TYPE_ID)));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.apply(
                new StatusEffectTypeDefinition(ID, NAME, STOPS_AT_ZERO, NAME_AT_VALUE_FUNCTION_ID,
                        arrayOf(new StatusEffectTypeDefinition.IconForCharacterFunction(ICON_TYPE_1,
                                ICON_TYPE_FUNCTION_1_ID)), "", mockRoundEndEffectDefinition,
                        mockTurnStartEffectDefinition, mockTurnEndEffectDefinition,
                        RESISTANCE_STAT_TYPE_ID)));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.apply(
                new StatusEffectTypeDefinition(ID, NAME, STOPS_AT_ZERO, NAME_AT_VALUE_FUNCTION_ID,
                        arrayOf(new StatusEffectTypeDefinition.IconForCharacterFunction(ICON_TYPE_1,
                                ICON_TYPE_FUNCTION_1_ID)), invalidActionId,
                        mockRoundEndEffectDefinition, mockTurnStartEffectDefinition,
                        mockTurnEndEffectDefinition, RESISTANCE_STAT_TYPE_ID)));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.apply(
                new StatusEffectTypeDefinition(ID, NAME, STOPS_AT_ZERO, NAME_AT_VALUE_FUNCTION_ID,
                        arrayOf(new StatusEffectTypeDefinition.IconForCharacterFunction(ICON_TYPE_1,
                                ICON_TYPE_FUNCTION_1_ID)), ALTER_ACTION_ID, null,
                        mockTurnStartEffectDefinition, mockTurnEndEffectDefinition,
                        RESISTANCE_STAT_TYPE_ID)));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.apply(
                new StatusEffectTypeDefinition(ID, NAME, STOPS_AT_ZERO, NAME_AT_VALUE_FUNCTION_ID,
                        arrayOf(new StatusEffectTypeDefinition.IconForCharacterFunction(ICON_TYPE_1,
                                ICON_TYPE_FUNCTION_1_ID)), ALTER_ACTION_ID,
                        mockRoundEndEffectDefinition, null, mockTurnEndEffectDefinition,
                        RESISTANCE_STAT_TYPE_ID)));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.apply(
                new StatusEffectTypeDefinition(ID, NAME, STOPS_AT_ZERO, NAME_AT_VALUE_FUNCTION_ID,
                        arrayOf(new StatusEffectTypeDefinition.IconForCharacterFunction(ICON_TYPE_1,
                                ICON_TYPE_FUNCTION_1_ID)), ALTER_ACTION_ID,
                        mockRoundEndEffectDefinition, mockTurnStartEffectDefinition, null,
                        RESISTANCE_STAT_TYPE_ID)));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.apply(
                new StatusEffectTypeDefinition(ID, NAME, STOPS_AT_ZERO, NAME_AT_VALUE_FUNCTION_ID,
                        arrayOf(new StatusEffectTypeDefinition.IconForCharacterFunction(ICON_TYPE_1,
                                ICON_TYPE_FUNCTION_1_ID)), ALTER_ACTION_ID,
                        mockRoundEndEffectDefinition, mockTurnStartEffectDefinition,
                        mockTurnEndEffectDefinition, null)));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.apply(
                new StatusEffectTypeDefinition(ID, NAME, STOPS_AT_ZERO, NAME_AT_VALUE_FUNCTION_ID,
                        arrayOf(new StatusEffectTypeDefinition.IconForCharacterFunction(ICON_TYPE_1,
                                ICON_TYPE_FUNCTION_1_ID)), ALTER_ACTION_ID,
                        mockRoundEndEffectDefinition, mockTurnStartEffectDefinition,
                        mockTurnEndEffectDefinition, "")));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.apply(
                new StatusEffectTypeDefinition(ID, NAME, STOPS_AT_ZERO, NAME_AT_VALUE_FUNCTION_ID,
                        arrayOf(new StatusEffectTypeDefinition.IconForCharacterFunction(ICON_TYPE_1,
                                ICON_TYPE_FUNCTION_1_ID)), ALTER_ACTION_ID,
                        mockRoundEndEffectDefinition, mockTurnStartEffectDefinition,
                        mockTurnEndEffectDefinition, invalidStatId)));
    }

    @Test
    public void testAlterWithInvalidArgs() {
        var output = statusEffectTypeFactory.apply(definition);

        assertThrows(IllegalArgumentException.class, () -> output.alterValue(null, randomInt()));
    }

    @Test
    public void testSetName() {
        var output = statusEffectTypeFactory.apply(definition);
        var newName = randomString();

        output.setName(newName);

        assertEquals(newName, output.getName());
    }

    @Test
    public void testSetNameWithInvalidArgs() {
        var output = statusEffectTypeFactory.apply(definition);

        assertThrows(IllegalArgumentException.class, () -> output.setName(null));
        assertThrows(IllegalArgumentException.class, () -> output.setName(""));
    }

    @Test
    public void testGetIconWithInvalidArgs() {
        var output = statusEffectTypeFactory.apply(definition);

        assertThrows(IllegalArgumentException.class, () -> output.getIcon(null, mockCharacter));
        assertThrows(IllegalArgumentException.class, () -> output.getIcon("", mockCharacter));
        assertThrows(IllegalArgumentException.class,
                () -> output.getIcon("NOT_A_VALID_ICON_TYPE", mockCharacter));
        assertThrows(IllegalArgumentException.class, () -> output.getIcon(ICON_TYPE_1, null));
    }
}
