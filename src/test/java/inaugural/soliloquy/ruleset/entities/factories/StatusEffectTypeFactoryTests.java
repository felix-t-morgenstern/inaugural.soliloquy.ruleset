package inaugural.soliloquy.ruleset.entities.factories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import soliloquy.specs.common.factories.Factory;
import soliloquy.specs.common.valueobjects.Pair;
import soliloquy.specs.gamestate.entities.Character;
import soliloquy.specs.graphics.assets.ImageAsset;
import soliloquy.specs.ruleset.definitions.StatusEffectTypeDefinition;
import soliloquy.specs.ruleset.entities.StatusEffectType;

import java.util.HashMap;
import java.util.function.Function;

import static inaugural.soliloquy.tools.random.Random.*;
import static org.junit.Assert.*;
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
    private final StatusEffectTypeDefinition DEFINITION =
            new StatusEffectTypeDefinition(ID, NAME, STOPS_AT_ZERO, NAME_AT_VALUE_FUNCTION_ID,
                    new StatusEffectTypeDefinition.IconForCharacterFunction[]{
                            new StatusEffectTypeDefinition.IconForCharacterFunction(ICON_TYPE_1,
                                    ICON_TYPE_FUNCTION_1_ID),
                            new StatusEffectTypeDefinition.IconForCharacterFunction(ICON_TYPE_2,
                                    ICON_TYPE_FUNCTION_2_ID)});

    @Mock private Character mockCharacter;
    @Mock private Function<Integer, String> mockNameAtValueFunction;
    @Mock private Function<Character, Pair<ImageAsset, Integer>> mockIconTypeFunction1;
    @Mock private Function<Character, Pair<ImageAsset, Integer>> mockIconTypeFunction2;

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

        statusEffectTypeFactory = new StatusEffectTypeFactory(FUNCTIONS::get);
    }

    @Test
    void testConstructorWithInvalidParams() {
        assertThrows(IllegalArgumentException.class, () -> new StatusEffectTypeFactory(null));
    }

    @Test
    void testMake() {
        StatusEffectTypeDefinition definition =
                new StatusEffectTypeDefinition(ID, NAME, STOPS_AT_ZERO, NAME_AT_VALUE_FUNCTION_ID,
                        new StatusEffectTypeDefinition.IconForCharacterFunction[]{
                                new StatusEffectTypeDefinition.IconForCharacterFunction(ICON_TYPE_1,
                                        ICON_TYPE_FUNCTION_1_ID),
                                new StatusEffectTypeDefinition.IconForCharacterFunction(ICON_TYPE_2,
                                        ICON_TYPE_FUNCTION_2_ID)});
        int value = randomInt();

        StatusEffectType output = statusEffectTypeFactory.make(definition);

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
    }

    @Test
    void testMakeWithInvalidParams() {
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.make(null));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.make(
                new StatusEffectTypeDefinition(null, NAME, STOPS_AT_ZERO, NAME_AT_VALUE_FUNCTION_ID,
                        new StatusEffectTypeDefinition.IconForCharacterFunction[]{
                                new StatusEffectTypeDefinition.IconForCharacterFunction(ICON_TYPE_1,
                                        ICON_TYPE_FUNCTION_1_ID)})));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.make(
                new StatusEffectTypeDefinition("", NAME, STOPS_AT_ZERO, NAME_AT_VALUE_FUNCTION_ID,
                        new StatusEffectTypeDefinition.IconForCharacterFunction[]{
                                new StatusEffectTypeDefinition.IconForCharacterFunction(ICON_TYPE_1,
                                        ICON_TYPE_FUNCTION_1_ID)})));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.make(
                new StatusEffectTypeDefinition(ID, null, STOPS_AT_ZERO, NAME_AT_VALUE_FUNCTION_ID,
                        new StatusEffectTypeDefinition.IconForCharacterFunction[]{
                                new StatusEffectTypeDefinition.IconForCharacterFunction(ICON_TYPE_1,
                                        ICON_TYPE_FUNCTION_1_ID)})));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.make(
                new StatusEffectTypeDefinition(ID, "", STOPS_AT_ZERO, NAME_AT_VALUE_FUNCTION_ID,
                        new StatusEffectTypeDefinition.IconForCharacterFunction[]{
                                new StatusEffectTypeDefinition.IconForCharacterFunction(ICON_TYPE_1,
                                        ICON_TYPE_FUNCTION_1_ID)})));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.make(
                new StatusEffectTypeDefinition(ID, NAME, STOPS_AT_ZERO, null,
                        new StatusEffectTypeDefinition.IconForCharacterFunction[]{
                                new StatusEffectTypeDefinition.IconForCharacterFunction(ICON_TYPE_1,
                                        ICON_TYPE_FUNCTION_1_ID)})));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.make(
                new StatusEffectTypeDefinition(ID, NAME, STOPS_AT_ZERO, "",
                        new StatusEffectTypeDefinition.IconForCharacterFunction[]{
                                new StatusEffectTypeDefinition.IconForCharacterFunction(ICON_TYPE_1,
                                        ICON_TYPE_FUNCTION_1_ID)})));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.make(
                new StatusEffectTypeDefinition(ID, NAME, STOPS_AT_ZERO, "NOT_A_VALID_FUNCTION_ID",
                        new StatusEffectTypeDefinition.IconForCharacterFunction[]{
                                new StatusEffectTypeDefinition.IconForCharacterFunction(ICON_TYPE_1,
                                        ICON_TYPE_FUNCTION_1_ID)})));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.make(
                new StatusEffectTypeDefinition(ID, NAME, STOPS_AT_ZERO, NAME_AT_VALUE_FUNCTION_ID,
                        null)));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.make(
                new StatusEffectTypeDefinition(ID, NAME, STOPS_AT_ZERO, NAME_AT_VALUE_FUNCTION_ID,
                        new StatusEffectTypeDefinition.IconForCharacterFunction[]{null})));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.make(
                new StatusEffectTypeDefinition(ID, NAME, STOPS_AT_ZERO, NAME_AT_VALUE_FUNCTION_ID,
                        new StatusEffectTypeDefinition.IconForCharacterFunction[]{
                                new StatusEffectTypeDefinition.IconForCharacterFunction(null,
                                        ICON_TYPE_FUNCTION_1_ID)})));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.make(
                new StatusEffectTypeDefinition(ID, NAME, STOPS_AT_ZERO, NAME_AT_VALUE_FUNCTION_ID,
                        new StatusEffectTypeDefinition.IconForCharacterFunction[]{
                                new StatusEffectTypeDefinition.IconForCharacterFunction("",
                                        ICON_TYPE_FUNCTION_1_ID)})));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.make(
                new StatusEffectTypeDefinition(ID, NAME, STOPS_AT_ZERO, NAME_AT_VALUE_FUNCTION_ID,
                        new StatusEffectTypeDefinition.IconForCharacterFunction[]{
                                new StatusEffectTypeDefinition.IconForCharacterFunction(ICON_TYPE_1,
                                        null)})));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.make(
                new StatusEffectTypeDefinition(ID, NAME, STOPS_AT_ZERO, NAME_AT_VALUE_FUNCTION_ID,
                        new StatusEffectTypeDefinition.IconForCharacterFunction[]{
                                new StatusEffectTypeDefinition.IconForCharacterFunction(ICON_TYPE_1,
                                        "")})));
        assertThrows(IllegalArgumentException.class, () -> statusEffectTypeFactory.make(
                new StatusEffectTypeDefinition(ID, NAME, STOPS_AT_ZERO, NAME_AT_VALUE_FUNCTION_ID,
                        new StatusEffectTypeDefinition.IconForCharacterFunction[]{
                                new StatusEffectTypeDefinition.IconForCharacterFunction(ICON_TYPE_1,
                                        "NOT_A_VALID_FUNCTION_ID")})));
    }

    @Test
    void testSetName() {
        StatusEffectType output = statusEffectTypeFactory.make(DEFINITION);
        String newName = randomString();

        output.setName(newName);

        assertEquals(newName, output.getName());
    }

    @Test
    void testSetNameWithInvalidParams() {
        StatusEffectType output = statusEffectTypeFactory.make(DEFINITION);

        assertThrows(IllegalArgumentException.class, () -> output.setName(null));
        assertThrows(IllegalArgumentException.class, () -> output.setName(""));
    }

    @Test
    void testGetIconWithInvalidParams() {
        StatusEffectType output = statusEffectTypeFactory.make(DEFINITION);

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
