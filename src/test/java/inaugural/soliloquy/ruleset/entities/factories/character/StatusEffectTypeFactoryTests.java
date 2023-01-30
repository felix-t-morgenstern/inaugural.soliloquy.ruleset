package inaugural.soliloquy.ruleset.entities.factories.character;

import inaugural.soliloquy.ruleset.definitions.EffectsOnCharacterDefinition;
import inaugural.soliloquy.ruleset.definitions.StatusEffectTypeDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import soliloquy.specs.common.entities.Action;
import soliloquy.specs.common.factories.Factory;
import soliloquy.specs.common.valueobjects.Pair;
import soliloquy.specs.gamestate.entities.Character;
import soliloquy.specs.graphics.assets.ImageAsset;
import soliloquy.specs.ruleset.entities.actonroundendandcharacterturn.EffectsCharacterOnRoundOrTurnChange.EffectsOnCharacter;
import soliloquy.specs.ruleset.entities.character.StatusEffectType;

import java.util.function.Function;

import static inaugural.soliloquy.tools.collections.Collections.arrayOf;
import static inaugural.soliloquy.tools.random.Random.*;
import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

class StatusEffectTypeFactoryTests {
    private final String ID = randomString();
    private final String NAME = randomString();
    private final boolean STOPS_AT_ZERO = randomBoolean();
    private final String NAME_AT_VALUE_FUNCTION_ID = randomString();
    private final String ICON_TYPE_1 = randomString();
    private final String ICON_TYPE_2 = randomString();
    private final String ICON_TYPE_FUNCTION_1_ID = randomString();
    private final String ICON_TYPE_FUNCTION_2_ID = randomString();
    private final String ALTER_ACTION_ID = randomString();

    @Mock private Character mockCharacter;
    @Mock private Function<Integer, String> mockNameAtValueFunction;
    @Mock private Function<Character, Pair<ImageAsset, Integer>> mockIconTypeFunction1;
    @Mock private Function<Character, Pair<ImageAsset, Integer>> mockIconTypeFunction2;
    @SuppressWarnings("rawtypes")
    @Mock private Function<String, Function> mockGetFunction;

    @Mock private Action<Object[]> mockAlterAction;
    @SuppressWarnings("rawtypes")
    @Mock private Function<String, Action> mockGetAction;

    @Mock private EffectsOnCharacterDefinition mockRoundEndEffectDefinition;
    @Mock private EffectsOnCharacterDefinition mockTurnStartEffectDefinition;
    @Mock private EffectsOnCharacterDefinition mockTurnEndEffectDefinition;
    @Mock private EffectsOnCharacter mockRoundEndEffect;
    @Mock private EffectsOnCharacter mockTurnStartEffect;
    @Mock private EffectsOnCharacter mockTurnEndEffect;
    @Mock private Factory<EffectsOnCharacterDefinition, EffectsOnCharacter>
            mockEffectsOnCharacterFactory;

    private StatusEffectTypeDefinition definition;

    private Factory<StatusEffectTypeDefinition, StatusEffectType> statusEffectTypeFactory;

    @BeforeEach
    void setUp() {
        mockCharacter = mock(Character.class);
        //noinspection unchecked
        mockNameAtValueFunction = mock(Function.class);
        //noinspection unchecked
        mockIconTypeFunction1 = mock(Function.class);
        //noinspection unchecked
        mockIconTypeFunction2 = mock(Function.class);

        //noinspection unchecked,rawtypes
        mockGetFunction = (Function<String, Function>) mock(Function.class);
        when(mockGetFunction.apply(NAME_AT_VALUE_FUNCTION_ID)).thenReturn(mockNameAtValueFunction);
        when(mockGetFunction.apply(ICON_TYPE_FUNCTION_1_ID)).thenReturn(mockIconTypeFunction1);
        when(mockGetFunction.apply(ICON_TYPE_FUNCTION_2_ID)).thenReturn(mockIconTypeFunction2);

        //noinspection unchecked
        mockAlterAction = (Action<Object[]>) mock(Action.class);

        //noinspection unchecked,rawtypes
        mockGetAction = (Function<String, Action>) mock(Function.class);
        when(mockGetAction.apply(anyString())).thenReturn(mockAlterAction);

        mockRoundEndEffectDefinition = mock(EffectsOnCharacterDefinition.class);
        mockTurnStartEffectDefinition = mock(EffectsOnCharacterDefinition.class);
        mockTurnEndEffectDefinition = mock(EffectsOnCharacterDefinition.class);

        mockRoundEndEffect = mock(EffectsOnCharacter.class);
        mockTurnStartEffect = mock(EffectsOnCharacter.class);
        mockTurnEndEffect = mock(EffectsOnCharacter.class);

        //noinspection unchecked
        mockEffectsOnCharacterFactory =
                (Factory<EffectsOnCharacterDefinition, EffectsOnCharacter>) mock(Factory.class);
        when(mockEffectsOnCharacterFactory.make(mockRoundEndEffectDefinition))
                .thenReturn(mockRoundEndEffect);
        when(mockEffectsOnCharacterFactory.make(mockTurnStartEffectDefinition))
                .thenReturn(mockTurnStartEffect);
        when(mockEffectsOnCharacterFactory.make(mockTurnEndEffectDefinition))
                .thenReturn(mockTurnEndEffect);

        definition =
                new StatusEffectTypeDefinition(ID, NAME, STOPS_AT_ZERO, NAME_AT_VALUE_FUNCTION_ID,
                        arrayOf(new StatusEffectTypeDefinition.IconForCharacterFunction(ICON_TYPE_1,
                                        ICON_TYPE_FUNCTION_1_ID),
                                new StatusEffectTypeDefinition.IconForCharacterFunction(ICON_TYPE_2,
                                        ICON_TYPE_FUNCTION_2_ID)), ALTER_ACTION_ID,
                        mockRoundEndEffectDefinition, mockTurnStartEffectDefinition,
                        mockTurnEndEffectDefinition);

        statusEffectTypeFactory =
                new StatusEffectTypeFactory(mockGetFunction, mockGetAction,
                        mockEffectsOnCharacterFactory);
    }

    @Test
    void testConstructorWithInvalidParams() {
        assertThrows(IllegalArgumentException.class,
                () -> new StatusEffectTypeFactory(null, mockGetAction,
                        mockEffectsOnCharacterFactory));
        assertThrows(IllegalArgumentException.class,
                () -> new StatusEffectTypeFactory(mockGetFunction, null,
                        mockEffectsOnCharacterFactory));
        assertThrows(IllegalArgumentException.class,
                () -> new StatusEffectTypeFactory(mockGetFunction, mockGetAction, null));
    }

    @Test
    void testMake() {
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
        verify(mockNameAtValueFunction, times(1)).apply(value);
        verify(mockIconTypeFunction1, times(1)).apply(mockCharacter);
        verify(mockIconTypeFunction2, times(1)).apply(mockCharacter);
        verify(mockAlterAction, times(1)).run(eq(arrayOf(mockCharacter, value)));
        assertEquals(StatusEffectType.class.getCanonicalName(), output.getInterfaceName());
        assertSame(mockRoundEndEffect, output.onRoundEnd());
        verify(mockEffectsOnCharacterFactory, times(1))
                .make(mockRoundEndEffectDefinition);
        assertSame(mockTurnStartEffect, output.onTurnStart());
        verify(mockEffectsOnCharacterFactory, times(1))
                .make(mockTurnStartEffectDefinition);
        assertSame(mockTurnEndEffect, output.onTurnEnd());
        verify(mockEffectsOnCharacterFactory, times(1))
                .make(mockTurnEndEffectDefinition);
    }

    @Test
    void testMakeWithInvalidParams() {
        var invalidFunctionId = randomString();
        when(mockGetFunction.apply(invalidFunctionId)).thenReturn(null);
        var invalidActionId = randomString();
        when(mockGetAction.apply(invalidActionId)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.make(null));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.make(
                new StatusEffectTypeDefinition(null, NAME, STOPS_AT_ZERO, NAME_AT_VALUE_FUNCTION_ID,
                        arrayOf(new StatusEffectTypeDefinition.IconForCharacterFunction(ICON_TYPE_1,
                                ICON_TYPE_FUNCTION_1_ID)), ALTER_ACTION_ID,
                        mockRoundEndEffectDefinition, mockTurnStartEffectDefinition,
                        mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.make(
                new StatusEffectTypeDefinition("", NAME, STOPS_AT_ZERO, NAME_AT_VALUE_FUNCTION_ID,
                        arrayOf(new StatusEffectTypeDefinition.IconForCharacterFunction(ICON_TYPE_1,
                                ICON_TYPE_FUNCTION_1_ID)), ALTER_ACTION_ID,
                        mockRoundEndEffectDefinition, mockTurnStartEffectDefinition,
                        mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.make(
                new StatusEffectTypeDefinition(ID, null, STOPS_AT_ZERO, NAME_AT_VALUE_FUNCTION_ID,
                        arrayOf(new StatusEffectTypeDefinition.IconForCharacterFunction(ICON_TYPE_1,
                                ICON_TYPE_FUNCTION_1_ID)), ALTER_ACTION_ID,
                        mockRoundEndEffectDefinition, mockTurnStartEffectDefinition,
                        mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.make(
                new StatusEffectTypeDefinition(ID, "", STOPS_AT_ZERO, NAME_AT_VALUE_FUNCTION_ID,
                        arrayOf(new StatusEffectTypeDefinition.IconForCharacterFunction(ICON_TYPE_1,
                                ICON_TYPE_FUNCTION_1_ID)), ALTER_ACTION_ID,
                        mockRoundEndEffectDefinition, mockTurnStartEffectDefinition,
                        mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.make(
                new StatusEffectTypeDefinition(ID, NAME, STOPS_AT_ZERO, null,
                        arrayOf(new StatusEffectTypeDefinition.IconForCharacterFunction(ICON_TYPE_1,
                                ICON_TYPE_FUNCTION_1_ID)), ALTER_ACTION_ID,
                        mockRoundEndEffectDefinition, mockTurnStartEffectDefinition,
                        mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.make(
                new StatusEffectTypeDefinition(ID, NAME, STOPS_AT_ZERO, "",
                        arrayOf(new StatusEffectTypeDefinition.IconForCharacterFunction(ICON_TYPE_1,
                                ICON_TYPE_FUNCTION_1_ID)), ALTER_ACTION_ID,
                        mockRoundEndEffectDefinition, mockTurnStartEffectDefinition,
                        mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.make(
                new StatusEffectTypeDefinition(ID, NAME, STOPS_AT_ZERO, invalidFunctionId,
                        arrayOf(new StatusEffectTypeDefinition.IconForCharacterFunction(ICON_TYPE_1,
                                ICON_TYPE_FUNCTION_1_ID)), ALTER_ACTION_ID,
                        mockRoundEndEffectDefinition, mockTurnStartEffectDefinition,
                        mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.make(
                new StatusEffectTypeDefinition(ID, NAME, STOPS_AT_ZERO, NAME_AT_VALUE_FUNCTION_ID,
                        null, ALTER_ACTION_ID, mockRoundEndEffectDefinition,
                        mockTurnStartEffectDefinition, mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.make(
                new StatusEffectTypeDefinition(ID, NAME, STOPS_AT_ZERO, NAME_AT_VALUE_FUNCTION_ID,
                        arrayOf((StatusEffectTypeDefinition.IconForCharacterFunction) null),
                        ALTER_ACTION_ID, mockRoundEndEffectDefinition,
                        mockTurnStartEffectDefinition, mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.make(
                new StatusEffectTypeDefinition(ID, NAME, STOPS_AT_ZERO, NAME_AT_VALUE_FUNCTION_ID,
                        arrayOf(new StatusEffectTypeDefinition.IconForCharacterFunction(null,
                                ICON_TYPE_FUNCTION_1_ID)), ALTER_ACTION_ID,
                        mockRoundEndEffectDefinition, mockTurnStartEffectDefinition,
                        mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.make(
                new StatusEffectTypeDefinition(ID, NAME, STOPS_AT_ZERO, NAME_AT_VALUE_FUNCTION_ID,
                        arrayOf(new StatusEffectTypeDefinition.IconForCharacterFunction("",
                                ICON_TYPE_FUNCTION_1_ID)), ALTER_ACTION_ID,
                        mockRoundEndEffectDefinition, mockTurnStartEffectDefinition,
                        mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.make(
                new StatusEffectTypeDefinition(ID, NAME, STOPS_AT_ZERO, NAME_AT_VALUE_FUNCTION_ID,
                        arrayOf(new StatusEffectTypeDefinition.IconForCharacterFunction(ICON_TYPE_1,
                                null)), ALTER_ACTION_ID, mockRoundEndEffectDefinition,
                        mockTurnStartEffectDefinition, mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.make(
                new StatusEffectTypeDefinition(ID, NAME, STOPS_AT_ZERO, NAME_AT_VALUE_FUNCTION_ID,
                        arrayOf(new StatusEffectTypeDefinition.IconForCharacterFunction(ICON_TYPE_1,
                                "")), ALTER_ACTION_ID, mockRoundEndEffectDefinition,
                        mockTurnStartEffectDefinition, mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.make(
                new StatusEffectTypeDefinition(ID, NAME, STOPS_AT_ZERO, NAME_AT_VALUE_FUNCTION_ID,
                        arrayOf(new StatusEffectTypeDefinition.IconForCharacterFunction(ICON_TYPE_1,
                                invalidFunctionId)), ALTER_ACTION_ID,
                        mockRoundEndEffectDefinition, mockTurnStartEffectDefinition,
                        mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.make(
                new StatusEffectTypeDefinition(ID, NAME, STOPS_AT_ZERO, NAME_AT_VALUE_FUNCTION_ID,
                        arrayOf(new StatusEffectTypeDefinition.IconForCharacterFunction(ICON_TYPE_1,
                                ICON_TYPE_FUNCTION_1_ID)), null, mockRoundEndEffectDefinition,
                        mockTurnStartEffectDefinition, mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.make(
                new StatusEffectTypeDefinition(ID, NAME, STOPS_AT_ZERO, NAME_AT_VALUE_FUNCTION_ID,
                        arrayOf(new StatusEffectTypeDefinition.IconForCharacterFunction(ICON_TYPE_1,
                                ICON_TYPE_FUNCTION_1_ID)), "", mockRoundEndEffectDefinition,
                        mockTurnStartEffectDefinition, mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.make(
                new StatusEffectTypeDefinition(ID, NAME, STOPS_AT_ZERO, NAME_AT_VALUE_FUNCTION_ID,
                        arrayOf(new StatusEffectTypeDefinition.IconForCharacterFunction(ICON_TYPE_1,
                                ICON_TYPE_FUNCTION_1_ID)), invalidActionId,
                        mockRoundEndEffectDefinition, mockTurnStartEffectDefinition,
                        mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.make(
                new StatusEffectTypeDefinition(ID, NAME, STOPS_AT_ZERO, NAME_AT_VALUE_FUNCTION_ID,
                        arrayOf(new StatusEffectTypeDefinition.IconForCharacterFunction(ICON_TYPE_1,
                                ICON_TYPE_FUNCTION_1_ID)), ALTER_ACTION_ID, null,
                        mockTurnStartEffectDefinition, mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.make(
                new StatusEffectTypeDefinition(ID, NAME, STOPS_AT_ZERO, NAME_AT_VALUE_FUNCTION_ID,
                        arrayOf(new StatusEffectTypeDefinition.IconForCharacterFunction(ICON_TYPE_1,
                                ICON_TYPE_FUNCTION_1_ID)), ALTER_ACTION_ID,
                        mockRoundEndEffectDefinition, null, mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.make(
                new StatusEffectTypeDefinition(ID, NAME, STOPS_AT_ZERO, NAME_AT_VALUE_FUNCTION_ID,
                        arrayOf(new StatusEffectTypeDefinition.IconForCharacterFunction(ICON_TYPE_1,
                                ICON_TYPE_FUNCTION_1_ID)), ALTER_ACTION_ID,
                        mockRoundEndEffectDefinition, mockTurnStartEffectDefinition, null)));
    }

    @Test
    void testAlterWithInvalidParams() {
        var output = statusEffectTypeFactory.make(definition);

        assertThrows(IllegalArgumentException.class, () -> output.alterValue(null, randomInt()));
    }

    @Test
    void testSetName() {
        var output = statusEffectTypeFactory.make(definition);
        var newName = randomString();

        output.setName(newName);

        assertEquals(newName, output.getName());
    }

    @Test
    void testSetNameWithInvalidParams() {
        var output = statusEffectTypeFactory.make(definition);

        assertThrows(IllegalArgumentException.class, () -> output.setName(null));
        assertThrows(IllegalArgumentException.class, () -> output.setName(""));
    }

    @Test
    void testGetIconWithInvalidParams() {
        var output = statusEffectTypeFactory.make(definition);

        assertThrows(IllegalArgumentException.class, () -> output.getIcon(null, mockCharacter));
        assertThrows(IllegalArgumentException.class, () -> output.getIcon("", mockCharacter));
        assertThrows(IllegalArgumentException.class,
                () -> output.getIcon("NOT_A_VALID_ICON_TYPE", mockCharacter));
        assertThrows(IllegalArgumentException.class, () -> output.getIcon(ICON_TYPE_1, null));
    }

    @Test
    void testGetInterfaceName() {
        assertEquals(Factory.class.getCanonicalName() + "<" +
                        StatusEffectTypeDefinition.class.getCanonicalName() + "," +
                        StatusEffectType.class.getCanonicalName() + ">",
                statusEffectTypeFactory.getInterfaceName());
    }
}
