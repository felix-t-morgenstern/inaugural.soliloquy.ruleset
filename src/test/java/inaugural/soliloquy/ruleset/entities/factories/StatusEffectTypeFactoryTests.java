package inaugural.soliloquy.ruleset.entities.factories;

import inaugural.soliloquy.ruleset.definitions.EffectsOnCharacterDefinition;
import inaugural.soliloquy.ruleset.definitions.RoundEndEffectsOnCharacterDefinition;
import inaugural.soliloquy.ruleset.definitions.StatusEffectTypeDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import soliloquy.specs.common.entities.Action;
import soliloquy.specs.common.factories.Factory;
import soliloquy.specs.common.valueobjects.Pair;
import soliloquy.specs.gamestate.entities.Character;
import soliloquy.specs.graphics.assets.ImageAsset;
import soliloquy.specs.ruleset.entities.actonroundendandcharacterturn.EffectsCharacterOnRoundOrTurnChange.EffectsOnCharacter;
import soliloquy.specs.ruleset.entities.actonroundendandcharacterturn.EffectsCharacterOnRoundOrTurnChange.RoundEndEffectsOnCharacter;
import soliloquy.specs.ruleset.entities.character.StaticStatisticType;
import soliloquy.specs.ruleset.entities.character.StatusEffectType;

import java.util.function.Function;

import static inaugural.soliloquy.tools.collections.Collections.arrayOf;
import static inaugural.soliloquy.tools.random.Random.*;
import static inaugural.soliloquy.tools.testing.Mock.generateMockLookupFunction;
import static inaugural.soliloquy.tools.valueobjects.Pair.pairOf;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
    private final String ALTER_ACTION_ID = randomString();
    private final String RESISTANCE_STAT_TYPE_ID = randomString();

    @Mock private Character mockCharacter;
    @Mock private StaticStatisticType mockResistanceStatType;
    @Mock private Function<Integer, String> mockNameAtValueFunction;
    @Mock private Function<Character, Pair<ImageAsset, Integer>> mockIconTypeFunction1;
    @Mock private Function<Character, Pair<ImageAsset, Integer>> mockIconTypeFunction2;
    private Function<String, StaticStatisticType> mockGetStaticStatType;
    @SuppressWarnings("rawtypes")
    @Mock private Function<String, Function> mockGetFunction;

    @Mock private Action<Object[]> mockAlterAction;
    @SuppressWarnings("rawtypes")
    @Mock private Function<String, Action> mockGetAction;

    @Mock private RoundEndEffectsOnCharacterDefinition mockRoundEndEffectDefinition;
    @Mock private EffectsOnCharacterDefinition mockTurnStartEffectDefinition;
    @Mock private EffectsOnCharacterDefinition mockTurnEndEffectDefinition;
    @Mock private RoundEndEffectsOnCharacter mockRoundEndEffect;
    @Mock private EffectsOnCharacter mockTurnStartEffect;
    @Mock private EffectsOnCharacter mockTurnEndEffect;
    @Mock private Factory<EffectsOnCharacterDefinition, EffectsOnCharacter>
            mockEffectsOnCharacterFactory;
    @Mock private Factory<RoundEndEffectsOnCharacterDefinition, RoundEndEffectsOnCharacter>
            mockRoundEndEffectsOnCharacterFactory;

    private StatusEffectTypeDefinition definition;

    private Factory<StatusEffectTypeDefinition, StatusEffectType> statusEffectTypeFactory;

    @BeforeEach
    public void setUp() {
        mockGetFunction = generateMockLookupFunction(
                pairOf(NAME_AT_VALUE_FUNCTION_ID, mockNameAtValueFunction),
                pairOf(ICON_TYPE_FUNCTION_1_ID, mockIconTypeFunction1),
                pairOf(ICON_TYPE_FUNCTION_2_ID, mockIconTypeFunction2)
        );

        mockGetStaticStatType = generateMockLookupFunction(
                pairOf(RESISTANCE_STAT_TYPE_ID, mockResistanceStatType));

        //noinspection unchecked
        mockAlterAction = (Action<Object[]>) mock(Action.class);

        //noinspection unchecked,rawtypes
        mockGetAction = (Function<String, Action>) mock(Function.class);
        lenient().when(mockGetAction.apply(anyString())).thenReturn(mockAlterAction);

        mockTurnStartEffectDefinition = mock(EffectsOnCharacterDefinition.class);
        mockTurnEndEffectDefinition = mock(EffectsOnCharacterDefinition.class);

        mockTurnStartEffect = mock(EffectsOnCharacter.class);
        mockTurnEndEffect = mock(EffectsOnCharacter.class);

        //noinspection unchecked
        mockEffectsOnCharacterFactory =
                (Factory<EffectsOnCharacterDefinition, EffectsOnCharacter>) mock(Factory.class);
        lenient().when(mockEffectsOnCharacterFactory.make(mockTurnStartEffectDefinition))
                .thenReturn(mockTurnStartEffect);
        lenient().when(mockEffectsOnCharacterFactory.make(mockTurnEndEffectDefinition))
                .thenReturn(mockTurnEndEffect);

        mockRoundEndEffectDefinition = mock(RoundEndEffectsOnCharacterDefinition.class);
        mockRoundEndEffect = mock(RoundEndEffectsOnCharacter.class);

        //noinspection unchecked
        mockRoundEndEffectsOnCharacterFactory =
                (Factory<RoundEndEffectsOnCharacterDefinition, RoundEndEffectsOnCharacter>) mock(
                        Factory.class);
        lenient().when(mockRoundEndEffectsOnCharacterFactory.make(mockRoundEndEffectDefinition))
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
                new StatusEffectTypeFactory(mockGetFunction, mockGetAction,
                        mockEffectsOnCharacterFactory, mockRoundEndEffectsOnCharacterFactory,
                        mockGetStaticStatType);
    }

    @Test
    public void testConstructorWithInvalidArgs() {
        assertThrows(IllegalArgumentException.class,
                () -> new StatusEffectTypeFactory(null, mockGetAction,
                        mockEffectsOnCharacterFactory, mockRoundEndEffectsOnCharacterFactory,
                        mockGetStaticStatType));
        assertThrows(IllegalArgumentException.class,
                () -> new StatusEffectTypeFactory(mockGetFunction, null,
                        mockEffectsOnCharacterFactory, mockRoundEndEffectsOnCharacterFactory,
                        mockGetStaticStatType));
        assertThrows(IllegalArgumentException.class,
                () -> new StatusEffectTypeFactory(mockGetFunction, mockGetAction, null,
                        mockRoundEndEffectsOnCharacterFactory, mockGetStaticStatType));
        assertThrows(IllegalArgumentException.class,
                () -> new StatusEffectTypeFactory(mockGetFunction, mockGetAction,
                        mockEffectsOnCharacterFactory, null, mockGetStaticStatType));
        assertThrows(IllegalArgumentException.class,
                () -> new StatusEffectTypeFactory(mockGetFunction, mockGetAction,
                        mockEffectsOnCharacterFactory, mockRoundEndEffectsOnCharacterFactory,
                        null));
    }

    @Test
    public void testMake() {
        var value = randomInt();

        var output = statusEffectTypeFactory.make(definition);
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
        verify(mockGetAction).apply(ALTER_ACTION_ID);
        verify(mockNameAtValueFunction).apply(value);
        verify(mockIconTypeFunction1).apply(mockCharacter);
        verify(mockIconTypeFunction2).apply(mockCharacter);
        verify(mockAlterAction).run(eq(arrayOf(mockCharacter, value)));
        assertEquals(StatusEffectType.class.getCanonicalName(), output.getInterfaceName());
        assertSame(mockRoundEndEffect, output.onRoundEnd());
        verify(mockRoundEndEffectsOnCharacterFactory).make(mockRoundEndEffectDefinition);
        assertSame(mockTurnStartEffect, output.onTurnStart());
        verify(mockEffectsOnCharacterFactory).make(mockTurnStartEffectDefinition);
        assertSame(mockTurnEndEffect, output.onTurnEnd());
        verify(mockEffectsOnCharacterFactory).make(mockTurnEndEffectDefinition);
        verify(mockGetStaticStatType).apply(RESISTANCE_STAT_TYPE_ID);
        assertSame(mockResistanceStatType, output.resistanceStatisticType());
    }

    @Test
    public void testMakeWithInvalidArgs() {
        var invalidFunctionId = randomString();
        when(mockGetFunction.apply(invalidFunctionId)).thenReturn(null);
        var invalidActionId = randomString();
        when(mockGetAction.apply(invalidActionId)).thenReturn(null);
        var invalidStatId = randomString();

        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.make(null));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.make(
                new StatusEffectTypeDefinition(null, NAME, STOPS_AT_ZERO, NAME_AT_VALUE_FUNCTION_ID,
                        arrayOf(new StatusEffectTypeDefinition.IconForCharacterFunction(ICON_TYPE_1,
                                ICON_TYPE_FUNCTION_1_ID)), ALTER_ACTION_ID,
                        mockRoundEndEffectDefinition, mockTurnStartEffectDefinition,
                        mockTurnEndEffectDefinition, RESISTANCE_STAT_TYPE_ID)));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.make(
                new StatusEffectTypeDefinition("", NAME, STOPS_AT_ZERO, NAME_AT_VALUE_FUNCTION_ID,
                        arrayOf(new StatusEffectTypeDefinition.IconForCharacterFunction(ICON_TYPE_1,
                                ICON_TYPE_FUNCTION_1_ID)), ALTER_ACTION_ID,
                        mockRoundEndEffectDefinition, mockTurnStartEffectDefinition,
                        mockTurnEndEffectDefinition, RESISTANCE_STAT_TYPE_ID)));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.make(
                new StatusEffectTypeDefinition(ID, null, STOPS_AT_ZERO, NAME_AT_VALUE_FUNCTION_ID,
                        arrayOf(new StatusEffectTypeDefinition.IconForCharacterFunction(ICON_TYPE_1,
                                ICON_TYPE_FUNCTION_1_ID)), ALTER_ACTION_ID,
                        mockRoundEndEffectDefinition, mockTurnStartEffectDefinition,
                        mockTurnEndEffectDefinition, RESISTANCE_STAT_TYPE_ID)));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.make(
                new StatusEffectTypeDefinition(ID, "", STOPS_AT_ZERO, NAME_AT_VALUE_FUNCTION_ID,
                        arrayOf(new StatusEffectTypeDefinition.IconForCharacterFunction(ICON_TYPE_1,
                                ICON_TYPE_FUNCTION_1_ID)), ALTER_ACTION_ID,
                        mockRoundEndEffectDefinition, mockTurnStartEffectDefinition,
                        mockTurnEndEffectDefinition, RESISTANCE_STAT_TYPE_ID)));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.make(
                new StatusEffectTypeDefinition(ID, NAME, STOPS_AT_ZERO, null,
                        arrayOf(new StatusEffectTypeDefinition.IconForCharacterFunction(ICON_TYPE_1,
                                ICON_TYPE_FUNCTION_1_ID)), ALTER_ACTION_ID,
                        mockRoundEndEffectDefinition, mockTurnStartEffectDefinition,
                        mockTurnEndEffectDefinition, RESISTANCE_STAT_TYPE_ID)));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.make(
                new StatusEffectTypeDefinition(ID, NAME, STOPS_AT_ZERO, "",
                        arrayOf(new StatusEffectTypeDefinition.IconForCharacterFunction(ICON_TYPE_1,
                                ICON_TYPE_FUNCTION_1_ID)), ALTER_ACTION_ID,
                        mockRoundEndEffectDefinition, mockTurnStartEffectDefinition,
                        mockTurnEndEffectDefinition, RESISTANCE_STAT_TYPE_ID)));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.make(
                new StatusEffectTypeDefinition(ID, NAME, STOPS_AT_ZERO, invalidFunctionId,
                        arrayOf(new StatusEffectTypeDefinition.IconForCharacterFunction(ICON_TYPE_1,
                                ICON_TYPE_FUNCTION_1_ID)), ALTER_ACTION_ID,
                        mockRoundEndEffectDefinition, mockTurnStartEffectDefinition,
                        mockTurnEndEffectDefinition, RESISTANCE_STAT_TYPE_ID)));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.make(
                new StatusEffectTypeDefinition(ID, NAME, STOPS_AT_ZERO, NAME_AT_VALUE_FUNCTION_ID,
                        null, ALTER_ACTION_ID, mockRoundEndEffectDefinition,
                        mockTurnStartEffectDefinition, mockTurnEndEffectDefinition,
                        RESISTANCE_STAT_TYPE_ID)));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.make(
                new StatusEffectTypeDefinition(ID, NAME, STOPS_AT_ZERO, NAME_AT_VALUE_FUNCTION_ID,
                        arrayOf((StatusEffectTypeDefinition.IconForCharacterFunction) null),
                        ALTER_ACTION_ID, mockRoundEndEffectDefinition,
                        mockTurnStartEffectDefinition, mockTurnEndEffectDefinition,
                        RESISTANCE_STAT_TYPE_ID)));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.make(
                new StatusEffectTypeDefinition(ID, NAME, STOPS_AT_ZERO, NAME_AT_VALUE_FUNCTION_ID,
                        arrayOf(new StatusEffectTypeDefinition.IconForCharacterFunction(null,
                                ICON_TYPE_FUNCTION_1_ID)), ALTER_ACTION_ID,
                        mockRoundEndEffectDefinition, mockTurnStartEffectDefinition,
                        mockTurnEndEffectDefinition, RESISTANCE_STAT_TYPE_ID)));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.make(
                new StatusEffectTypeDefinition(ID, NAME, STOPS_AT_ZERO, NAME_AT_VALUE_FUNCTION_ID,
                        arrayOf(new StatusEffectTypeDefinition.IconForCharacterFunction("",
                                ICON_TYPE_FUNCTION_1_ID)), ALTER_ACTION_ID,
                        mockRoundEndEffectDefinition, mockTurnStartEffectDefinition,
                        mockTurnEndEffectDefinition, RESISTANCE_STAT_TYPE_ID)));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.make(
                new StatusEffectTypeDefinition(ID, NAME, STOPS_AT_ZERO, NAME_AT_VALUE_FUNCTION_ID,
                        arrayOf(new StatusEffectTypeDefinition.IconForCharacterFunction(ICON_TYPE_1,
                                null)), ALTER_ACTION_ID, mockRoundEndEffectDefinition,
                        mockTurnStartEffectDefinition, mockTurnEndEffectDefinition,
                        RESISTANCE_STAT_TYPE_ID)));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.make(
                new StatusEffectTypeDefinition(ID, NAME, STOPS_AT_ZERO, NAME_AT_VALUE_FUNCTION_ID,
                        arrayOf(new StatusEffectTypeDefinition.IconForCharacterFunction(ICON_TYPE_1,
                                "")), ALTER_ACTION_ID, mockRoundEndEffectDefinition,
                        mockTurnStartEffectDefinition, mockTurnEndEffectDefinition,
                        RESISTANCE_STAT_TYPE_ID)));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.make(
                new StatusEffectTypeDefinition(ID, NAME, STOPS_AT_ZERO, NAME_AT_VALUE_FUNCTION_ID,
                        arrayOf(new StatusEffectTypeDefinition.IconForCharacterFunction(ICON_TYPE_1,
                                invalidFunctionId)), ALTER_ACTION_ID,
                        mockRoundEndEffectDefinition, mockTurnStartEffectDefinition,
                        mockTurnEndEffectDefinition, RESISTANCE_STAT_TYPE_ID)));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.make(
                new StatusEffectTypeDefinition(ID, NAME, STOPS_AT_ZERO, NAME_AT_VALUE_FUNCTION_ID,
                        arrayOf(new StatusEffectTypeDefinition.IconForCharacterFunction(ICON_TYPE_1,
                                ICON_TYPE_FUNCTION_1_ID)), null, mockRoundEndEffectDefinition,
                        mockTurnStartEffectDefinition, mockTurnEndEffectDefinition,
                        RESISTANCE_STAT_TYPE_ID)));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.make(
                new StatusEffectTypeDefinition(ID, NAME, STOPS_AT_ZERO, NAME_AT_VALUE_FUNCTION_ID,
                        arrayOf(new StatusEffectTypeDefinition.IconForCharacterFunction(ICON_TYPE_1,
                                ICON_TYPE_FUNCTION_1_ID)), "", mockRoundEndEffectDefinition,
                        mockTurnStartEffectDefinition, mockTurnEndEffectDefinition,
                        RESISTANCE_STAT_TYPE_ID)));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.make(
                new StatusEffectTypeDefinition(ID, NAME, STOPS_AT_ZERO, NAME_AT_VALUE_FUNCTION_ID,
                        arrayOf(new StatusEffectTypeDefinition.IconForCharacterFunction(ICON_TYPE_1,
                                ICON_TYPE_FUNCTION_1_ID)), invalidActionId,
                        mockRoundEndEffectDefinition, mockTurnStartEffectDefinition,
                        mockTurnEndEffectDefinition, RESISTANCE_STAT_TYPE_ID)));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.make(
                new StatusEffectTypeDefinition(ID, NAME, STOPS_AT_ZERO, NAME_AT_VALUE_FUNCTION_ID,
                        arrayOf(new StatusEffectTypeDefinition.IconForCharacterFunction(ICON_TYPE_1,
                                ICON_TYPE_FUNCTION_1_ID)), ALTER_ACTION_ID, null,
                        mockTurnStartEffectDefinition, mockTurnEndEffectDefinition,
                        RESISTANCE_STAT_TYPE_ID)));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.make(
                new StatusEffectTypeDefinition(ID, NAME, STOPS_AT_ZERO, NAME_AT_VALUE_FUNCTION_ID,
                        arrayOf(new StatusEffectTypeDefinition.IconForCharacterFunction(ICON_TYPE_1,
                                ICON_TYPE_FUNCTION_1_ID)), ALTER_ACTION_ID,
                        mockRoundEndEffectDefinition, null, mockTurnEndEffectDefinition,
                        RESISTANCE_STAT_TYPE_ID)));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.make(
                new StatusEffectTypeDefinition(ID, NAME, STOPS_AT_ZERO, NAME_AT_VALUE_FUNCTION_ID,
                        arrayOf(new StatusEffectTypeDefinition.IconForCharacterFunction(ICON_TYPE_1,
                                ICON_TYPE_FUNCTION_1_ID)), ALTER_ACTION_ID,
                        mockRoundEndEffectDefinition, mockTurnStartEffectDefinition, null,
                        RESISTANCE_STAT_TYPE_ID)));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.make(
                new StatusEffectTypeDefinition(ID, NAME, STOPS_AT_ZERO, NAME_AT_VALUE_FUNCTION_ID,
                        arrayOf(new StatusEffectTypeDefinition.IconForCharacterFunction(ICON_TYPE_1,
                                ICON_TYPE_FUNCTION_1_ID)), ALTER_ACTION_ID,
                        mockRoundEndEffectDefinition, mockTurnStartEffectDefinition,
                        mockTurnEndEffectDefinition, null)));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.make(
                new StatusEffectTypeDefinition(ID, NAME, STOPS_AT_ZERO, NAME_AT_VALUE_FUNCTION_ID,
                        arrayOf(new StatusEffectTypeDefinition.IconForCharacterFunction(ICON_TYPE_1,
                                ICON_TYPE_FUNCTION_1_ID)), ALTER_ACTION_ID,
                        mockRoundEndEffectDefinition, mockTurnStartEffectDefinition,
                        mockTurnEndEffectDefinition, "")));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.make(
                new StatusEffectTypeDefinition(ID, NAME, STOPS_AT_ZERO, NAME_AT_VALUE_FUNCTION_ID,
                        arrayOf(new StatusEffectTypeDefinition.IconForCharacterFunction(ICON_TYPE_1,
                                ICON_TYPE_FUNCTION_1_ID)), ALTER_ACTION_ID,
                        mockRoundEndEffectDefinition, mockTurnStartEffectDefinition,
                        mockTurnEndEffectDefinition, invalidStatId)));
    }

    @Test
    public void testAlterWithInvalidArgs() {
        var output = statusEffectTypeFactory.make(definition);

        assertThrows(IllegalArgumentException.class, () -> output.alterValue(null, randomInt()));
    }

    @Test
    public void testSetName() {
        var output = statusEffectTypeFactory.make(definition);
        var newName = randomString();

        output.setName(newName);

        assertEquals(newName, output.getName());
    }

    @Test
    public void testSetNameWithInvalidArgs() {
        var output = statusEffectTypeFactory.make(definition);

        assertThrows(IllegalArgumentException.class, () -> output.setName(null));
        assertThrows(IllegalArgumentException.class, () -> output.setName(""));
    }

    @Test
    public void testGetIconWithInvalidArgs() {
        var output = statusEffectTypeFactory.make(definition);

        assertThrows(IllegalArgumentException.class, () -> output.getIcon(null, mockCharacter));
        assertThrows(IllegalArgumentException.class, () -> output.getIcon("", mockCharacter));
        assertThrows(IllegalArgumentException.class,
                () -> output.getIcon("NOT_A_VALID_ICON_TYPE", mockCharacter));
        assertThrows(IllegalArgumentException.class, () -> output.getIcon(ICON_TYPE_1, null));
    }

    @Test
    public void testGetInterfaceName() {
        assertEquals(Factory.class.getCanonicalName() + "<" +
                        StatusEffectTypeDefinition.class.getCanonicalName() + "," +
                        StatusEffectType.class.getCanonicalName() + ">",
                statusEffectTypeFactory.getInterfaceName());
    }
}
