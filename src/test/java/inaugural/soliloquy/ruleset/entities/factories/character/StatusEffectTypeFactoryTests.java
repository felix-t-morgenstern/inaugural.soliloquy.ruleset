package inaugural.soliloquy.ruleset.entities.factories.character;

import inaugural.soliloquy.ruleset.entities.factories.character.StatusEffectTypeFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import soliloquy.specs.common.factories.Factory;
import soliloquy.specs.common.valueobjects.Pair;
import soliloquy.specs.gamestate.entities.Character;
import soliloquy.specs.graphics.assets.ImageAsset;
import soliloquy.specs.ruleset.definitions.EffectsOnCharacterDefinition;
import soliloquy.specs.ruleset.definitions.StatusEffectTypeDefinition;
import soliloquy.specs.ruleset.entities.actonroundendandcharacterturn.EffectsCharacterOnRoundOrTurnChange.EffectsOnCharacter;
import soliloquy.specs.ruleset.entities.character.StatusEffectType;

import java.util.HashMap;
import java.util.function.Function;

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
    @SuppressWarnings("rawtypes")
    private final HashMap<String, Function> FUNCTIONS = new HashMap<>();

    @Mock private Character mockCharacter;
    @Mock private Function<Integer, String> mockNameAtValueFunction;
    @Mock private Function<Character, Pair<ImageAsset, Integer>> mockIconTypeFunction1;
    @Mock private Function<Character, Pair<ImageAsset, Integer>> mockIconTypeFunction2;

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

        FUNCTIONS.put(NAME_AT_VALUE_FUNCTION_ID, mockNameAtValueFunction);
        FUNCTIONS.put(ICON_TYPE_FUNCTION_1_ID, mockIconTypeFunction1);
        FUNCTIONS.put(ICON_TYPE_FUNCTION_2_ID, mockIconTypeFunction2);

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
                        new StatusEffectTypeDefinition.IconForCharacterFunction[]{
                                new StatusEffectTypeDefinition.IconForCharacterFunction(ICON_TYPE_1,
                                        ICON_TYPE_FUNCTION_1_ID),
                                new StatusEffectTypeDefinition.IconForCharacterFunction(ICON_TYPE_2,
                                        ICON_TYPE_FUNCTION_2_ID)}, mockRoundEndEffectDefinition,
                        mockTurnStartEffectDefinition, mockTurnEndEffectDefinition);

        statusEffectTypeFactory =
                new StatusEffectTypeFactory(FUNCTIONS::get, mockEffectsOnCharacterFactory);
    }

    @Test
    void testConstructorWithInvalidParams() {
        assertThrows(IllegalArgumentException.class,
                () -> new StatusEffectTypeFactory(null, mockEffectsOnCharacterFactory));
        assertThrows(IllegalArgumentException.class,
                () -> new StatusEffectTypeFactory(FUNCTIONS::get, null));
    }

    @Test
    void testMake() {
        var value = randomInt();

        var output = statusEffectTypeFactory.make(definition);

        assertNotNull(output);
        assertEquals(ID, output.id());
        assertEquals(NAME, output.getName());
        assertEquals(STOPS_AT_ZERO, output.stopsAtZero());
        output.nameAtValue(value);
        verify(mockNameAtValueFunction, times(1)).apply(value);
        output.getIcon(ICON_TYPE_1, mockCharacter);
        verify(mockIconTypeFunction1, times(1)).apply(mockCharacter);
        output.getIcon(ICON_TYPE_2, mockCharacter);
        verify(mockIconTypeFunction2, times(1)).apply(mockCharacter);
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
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.make(null));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.make(
                new StatusEffectTypeDefinition(null, NAME, STOPS_AT_ZERO,
                        NAME_AT_VALUE_FUNCTION_ID,
                        new StatusEffectTypeDefinition.IconForCharacterFunction[]{
                                new StatusEffectTypeDefinition.IconForCharacterFunction
                                        (ICON_TYPE_1,
                                                ICON_TYPE_FUNCTION_1_ID)},
                        mockRoundEndEffectDefinition,
                        mockTurnStartEffectDefinition, mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.make(
                new StatusEffectTypeDefinition("", NAME, STOPS_AT_ZERO, NAME_AT_VALUE_FUNCTION_ID,
                        new StatusEffectTypeDefinition.IconForCharacterFunction[]{
                                new StatusEffectTypeDefinition.IconForCharacterFunction
                                        (ICON_TYPE_1,
                                                ICON_TYPE_FUNCTION_1_ID)},
                        mockRoundEndEffectDefinition,
                        mockTurnStartEffectDefinition, mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.make(
                new StatusEffectTypeDefinition(ID, null, STOPS_AT_ZERO, NAME_AT_VALUE_FUNCTION_ID,
                        new StatusEffectTypeDefinition.IconForCharacterFunction[]{
                                new StatusEffectTypeDefinition.IconForCharacterFunction
                                        (ICON_TYPE_1,
                                                ICON_TYPE_FUNCTION_1_ID)},
                        mockRoundEndEffectDefinition,
                        mockTurnStartEffectDefinition, mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.make(
                new StatusEffectTypeDefinition(ID, "", STOPS_AT_ZERO, NAME_AT_VALUE_FUNCTION_ID,
                        new StatusEffectTypeDefinition.IconForCharacterFunction[]{
                                new StatusEffectTypeDefinition.IconForCharacterFunction
                                        (ICON_TYPE_1,
                                                ICON_TYPE_FUNCTION_1_ID)},
                        mockRoundEndEffectDefinition,
                        mockTurnStartEffectDefinition, mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.make(
                new StatusEffectTypeDefinition(ID, NAME, STOPS_AT_ZERO, null,
                        new StatusEffectTypeDefinition.IconForCharacterFunction[]{
                                new StatusEffectTypeDefinition.IconForCharacterFunction
                                        (ICON_TYPE_1,
                                                ICON_TYPE_FUNCTION_1_ID)},
                        mockRoundEndEffectDefinition,
                        mockTurnStartEffectDefinition, mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.make(
                new StatusEffectTypeDefinition(ID, NAME, STOPS_AT_ZERO, "",
                        new StatusEffectTypeDefinition.IconForCharacterFunction[]{
                                new StatusEffectTypeDefinition.IconForCharacterFunction
                                        (ICON_TYPE_1,
                                                ICON_TYPE_FUNCTION_1_ID)},
                        mockRoundEndEffectDefinition,
                        mockTurnStartEffectDefinition, mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.make(
                new StatusEffectTypeDefinition(ID, NAME, STOPS_AT_ZERO, "NOT_A_VALID_FUNCTION_ID",
                        new StatusEffectTypeDefinition.IconForCharacterFunction[]{
                                new StatusEffectTypeDefinition.IconForCharacterFunction
                                        (ICON_TYPE_1,
                                                ICON_TYPE_FUNCTION_1_ID)},
                        mockRoundEndEffectDefinition,
                        mockTurnStartEffectDefinition, mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.make(
                new StatusEffectTypeDefinition(ID, NAME, STOPS_AT_ZERO, NAME_AT_VALUE_FUNCTION_ID,
                        null, mockRoundEndEffectDefinition, mockTurnStartEffectDefinition,
                        mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.make(
                new StatusEffectTypeDefinition(ID, NAME, STOPS_AT_ZERO, NAME_AT_VALUE_FUNCTION_ID,
                        new StatusEffectTypeDefinition.IconForCharacterFunction[]{null},
                        mockRoundEndEffectDefinition, mockTurnStartEffectDefinition,
                        mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.make(
                new StatusEffectTypeDefinition(ID, NAME, STOPS_AT_ZERO, NAME_AT_VALUE_FUNCTION_ID,
                        new StatusEffectTypeDefinition.IconForCharacterFunction[]{
                                new StatusEffectTypeDefinition.IconForCharacterFunction(null,
                                        ICON_TYPE_FUNCTION_1_ID)}, mockRoundEndEffectDefinition,
                        mockTurnStartEffectDefinition, mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.make(
                new StatusEffectTypeDefinition(ID, NAME, STOPS_AT_ZERO, NAME_AT_VALUE_FUNCTION_ID,
                        new StatusEffectTypeDefinition.IconForCharacterFunction[]{
                                new StatusEffectTypeDefinition.IconForCharacterFunction("",
                                        ICON_TYPE_FUNCTION_1_ID)}, mockRoundEndEffectDefinition,
                        mockTurnStartEffectDefinition, mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.make(
                new StatusEffectTypeDefinition(ID, NAME, STOPS_AT_ZERO, NAME_AT_VALUE_FUNCTION_ID,
                        new StatusEffectTypeDefinition.IconForCharacterFunction[]{
                                new StatusEffectTypeDefinition.IconForCharacterFunction
                                        (ICON_TYPE_1,
                                                null)}, mockRoundEndEffectDefinition,
                        mockTurnStartEffectDefinition, mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.make(
                new StatusEffectTypeDefinition(ID, NAME, STOPS_AT_ZERO, NAME_AT_VALUE_FUNCTION_ID,
                        new StatusEffectTypeDefinition.IconForCharacterFunction[]{
                                new StatusEffectTypeDefinition.IconForCharacterFunction
                                        (ICON_TYPE_1,
                                                "")}, mockRoundEndEffectDefinition,
                        mockTurnStartEffectDefinition, mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.make(
                new StatusEffectTypeDefinition(ID, NAME, STOPS_AT_ZERO, NAME_AT_VALUE_FUNCTION_ID,
                        new StatusEffectTypeDefinition.IconForCharacterFunction[]{
                                new StatusEffectTypeDefinition.IconForCharacterFunction
                                        (ICON_TYPE_1,
                                                "NOT_A_VALID_FUNCTION_ID")},
                        mockRoundEndEffectDefinition, mockTurnStartEffectDefinition,
                        mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.make(
                new StatusEffectTypeDefinition(ID, NAME, STOPS_AT_ZERO,
                        NAME_AT_VALUE_FUNCTION_ID,
                        new StatusEffectTypeDefinition.IconForCharacterFunction[]{
                                new StatusEffectTypeDefinition.IconForCharacterFunction
                                        (ICON_TYPE_1,
                                                ICON_TYPE_FUNCTION_1_ID)},
                        null, mockTurnStartEffectDefinition,
                        mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.make(
                new StatusEffectTypeDefinition(ID, NAME, STOPS_AT_ZERO,
                        NAME_AT_VALUE_FUNCTION_ID,
                        new StatusEffectTypeDefinition.IconForCharacterFunction[]{
                                new StatusEffectTypeDefinition.IconForCharacterFunction
                                        (ICON_TYPE_1,
                                                ICON_TYPE_FUNCTION_1_ID)},
                        mockRoundEndEffectDefinition, null,
                        mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.make(
                new StatusEffectTypeDefinition(ID, NAME, STOPS_AT_ZERO,
                        NAME_AT_VALUE_FUNCTION_ID,
                        new StatusEffectTypeDefinition.IconForCharacterFunction[]{
                                new StatusEffectTypeDefinition.IconForCharacterFunction
                                        (ICON_TYPE_1,
                                                ICON_TYPE_FUNCTION_1_ID)},
                        mockRoundEndEffectDefinition, mockTurnStartEffectDefinition,
                        null)));
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
