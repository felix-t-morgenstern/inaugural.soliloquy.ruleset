package inaugural.soliloquy.ruleset.entities.factories.abilities;

import inaugural.soliloquy.ruleset.definitions.abilities.ActiveAbilityDefinition;
import inaugural.soliloquy.tools.testing.Mock.HandlerAndEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import soliloquy.specs.common.persistence.TypeHandler;
import soliloquy.specs.gamestate.entities.Character;
import soliloquy.specs.gamestate.entities.Item;
import soliloquy.specs.gamestate.entities.abilities.AbilitySource;
import soliloquy.specs.ruleset.entities.abilities.ActiveAbility;
import soliloquy.specs.ruleset.entities.abilities.ActiveAbility.TargetType;
import soliloquy.specs.ruleset.gameconcepts.CharacterEventFiring;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import static inaugural.soliloquy.tools.collections.Collections.arrayOf;
import static inaugural.soliloquy.tools.random.Random.randomString;
import static inaugural.soliloquy.tools.testing.Mock.generateMockEntityAndHandler;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class ActiveAbilityFactoryTests {
    private final String ID = randomString();
    private final String NAME = randomString();
    private final String CHARACTER_SOURCE_DESCRIPTION_FUNCTION_ID = randomString();
    private final String ITEM_SOURCE_DESCRIPTION_FUNCTION_ID = randomString();
    private final String USE_FUNCTION_ID = randomString();
    private final TargetType[] TARGET_TYPES =
            arrayOf(TargetType.CHARACTER, TargetType.WALL_SEGMENT);
    private final String WRITTEN_DATA = randomString();
    private final String CHARACTER_SOURCE_DESCRIPTION_OUTPUT = randomString();
    private final String ITEM_SOURCE_DESCRIPTION_OUTPUT = randomString();

    @Mock private CharacterEventFiring mockCharacterEventFiring;

    @Mock private Function<Character, String> mockCharacterSourceDescriptionFunction;
    @Mock private Function<Item, String> mockItemSourceDescriptionFunction;
    @Mock private Consumer<Object[]> mockUseConsumer;
    @SuppressWarnings("rawtypes")
    @Mock private Function<String, Function> mockGetFunction;
    @SuppressWarnings("rawtypes")
    @Mock private Function<String, Consumer> mockGetConsumer;

    @Mock private AbilitySource mockAbilitySource;

    @SuppressWarnings("rawtypes") private final HandlerAndEntity<Map> MOCK_DATA_AND_HANDLER =
            generateMockEntityAndHandler(Map.class, WRITTEN_DATA);
    @SuppressWarnings("rawtypes") 
    private final TypeHandler<Map> MOCK_MAP_HANDLER = MOCK_DATA_AND_HANDLER.handler;
    @SuppressWarnings("unchecked") 
    private final Map<String, Object> MOCK_DATA = MOCK_DATA_AND_HANDLER.entity;

    private final ActiveAbilityDefinition DEFINITION = new ActiveAbilityDefinition(
            ID,
            NAME,
            CHARACTER_SOURCE_DESCRIPTION_FUNCTION_ID,
            ITEM_SOURCE_DESCRIPTION_FUNCTION_ID,
            USE_FUNCTION_ID,
            TARGET_TYPES,
            WRITTEN_DATA);

    private Function<ActiveAbilityDefinition, ActiveAbility> factory;

    @BeforeEach
    void setUp() {
        mockCharacterEventFiring = mock(CharacterEventFiring.class);

        //noinspection unchecked
        mockCharacterSourceDescriptionFunction = (Function<Character, String>) mock(Function.class);
        when(mockCharacterSourceDescriptionFunction.apply(any())).thenReturn(
                CHARACTER_SOURCE_DESCRIPTION_OUTPUT);

        //noinspection unchecked
        mockItemSourceDescriptionFunction = (Function<Item, String>) mock(Function.class);
        when(mockItemSourceDescriptionFunction.apply(any())).thenReturn(
                ITEM_SOURCE_DESCRIPTION_OUTPUT);

        //noinspection unchecked,rawtypes
        mockGetFunction = (Function<String, Function>) mock(Function.class);
        when(mockGetFunction.apply(CHARACTER_SOURCE_DESCRIPTION_FUNCTION_ID)).thenReturn(
                mockCharacterSourceDescriptionFunction);
        when(mockGetFunction.apply(ITEM_SOURCE_DESCRIPTION_FUNCTION_ID)).thenReturn(
                mockItemSourceDescriptionFunction);

        //noinspection unchecked
        mockUseConsumer = (Consumer<Object[]>) mock(Consumer.class);

        //noinspection unchecked,rawtypes
        mockGetConsumer = (Function<String, Consumer>) mock(Function.class);
        when(mockGetConsumer.apply(anyString())).thenReturn(mockUseConsumer);

        mockCharacterEventFiring = mock(CharacterEventFiring.class);

        mockAbilitySource = mock(AbilitySource.class);

        factory = new ActiveAbilityFactory(mockGetFunction, mockGetConsumer, MOCK_MAP_HANDLER,
                mockCharacterEventFiring);
    }

    @Test
    void testConstructorWithInvalidArgs() {
        assertThrows(IllegalArgumentException.class,
                () -> new ActiveAbilityFactory(null, mockGetConsumer, MOCK_MAP_HANDLER,
                        mockCharacterEventFiring));
        assertThrows(IllegalArgumentException.class,
                () -> new ActiveAbilityFactory(mockGetFunction, null, MOCK_MAP_HANDLER,
                        mockCharacterEventFiring));
        assertThrows(IllegalArgumentException.class,
                () -> new ActiveAbilityFactory(mockGetFunction, mockGetConsumer, null,
                        mockCharacterEventFiring));
        assertThrows(IllegalArgumentException.class,
                () -> new ActiveAbilityFactory(mockGetFunction, mockGetConsumer, MOCK_MAP_HANDLER,
                        null));
    }

    @Test
    void testMake() {
        var mockCharacter = mock(Character.class);
        var mockItem = mock(Item.class);
        Object[] targets = arrayOf(mockCharacter, mockItem);

        var output = factory.apply(DEFINITION);
        var data = output.data();
        var targetTypes = output.targetTypes();
        var characterSourceDescription = output.description(mockCharacter);
        var itemSourceDescription = output.description(mockItem);
        output.use(mockAbilitySource, targets);

        assertNotNull(output);
        verify(mockGetFunction).apply(CHARACTER_SOURCE_DESCRIPTION_FUNCTION_ID);
        verify(mockGetFunction).apply(ITEM_SOURCE_DESCRIPTION_FUNCTION_ID);
        verify(mockGetConsumer).apply(USE_FUNCTION_ID);
        verify(MOCK_MAP_HANDLER).read(WRITTEN_DATA);

        assertSame(MOCK_DATA, data);

        assertNotSame(TARGET_TYPES, targetTypes);
        assertArrayEquals(TARGET_TYPES, targetTypes);

        verify(mockCharacterSourceDescriptionFunction).apply(mockCharacter);
        assertEquals(CHARACTER_SOURCE_DESCRIPTION_OUTPUT, characterSourceDescription);

        verify(mockItemSourceDescriptionFunction).apply(mockItem);
        assertEquals(ITEM_SOURCE_DESCRIPTION_OUTPUT, itemSourceDescription);

        verify(mockUseConsumer).accept(
                eq(arrayOf(mockCharacterEventFiring, mockAbilitySource, mockCharacter, mockItem)));
    }

    @Test
    void testSetName() {
        var newName = randomString();
        var output = factory.apply(DEFINITION);

        output.setName(newName);

        assertEquals(newName, output.getName());
    }

    @Test
    void testSetNameWithInvalidArgs() {
        var output = factory.apply(DEFINITION);

        assertThrows(IllegalArgumentException.class, () -> output.setName(null));
        assertThrows(IllegalArgumentException.class, () -> output.setName(""));
    }

    @Test
    void testCharacterSourceDescriptionWithInvalidArgs() {
        var output = factory.apply(DEFINITION);

        assertThrows(IllegalArgumentException.class, () -> output.description((Character) null));
    }

    @Test
    void testItemSourceDescriptionWithInvalidArgs() {
        var output = factory.apply(DEFINITION);

        assertThrows(IllegalArgumentException.class, () -> output.description((Item) null));
    }

    @Test
    void testUseWithInvalidArgs() {
        var output = factory.apply(DEFINITION);

        assertThrows(IllegalArgumentException.class, () -> output.use(null, arrayOf()));
        assertThrows(IllegalArgumentException.class,
                () -> output.use(mockAbilitySource, (Object) null));
    }

    @Test
    void testMakeWithInvalidArgs() {
        var invalidFunctionId = randomString();
        when(mockGetFunction.apply(invalidFunctionId)).thenReturn(null);
        var invalidConsumerId = randomString();
        when(mockGetConsumer.apply(invalidConsumerId)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> factory.apply(null));
        assertThrows(IllegalArgumentException.class, () -> factory.apply(
                new ActiveAbilityDefinition(null, NAME, CHARACTER_SOURCE_DESCRIPTION_FUNCTION_ID,
                        ITEM_SOURCE_DESCRIPTION_FUNCTION_ID, USE_FUNCTION_ID, TARGET_TYPES,
                        WRITTEN_DATA)));
        assertThrows(IllegalArgumentException.class, () -> factory.apply(
                new ActiveAbilityDefinition("", NAME, CHARACTER_SOURCE_DESCRIPTION_FUNCTION_ID,
                        ITEM_SOURCE_DESCRIPTION_FUNCTION_ID, USE_FUNCTION_ID, TARGET_TYPES,
                        WRITTEN_DATA)));
        assertThrows(IllegalArgumentException.class, () -> factory.apply(
                new ActiveAbilityDefinition(ID, null, CHARACTER_SOURCE_DESCRIPTION_FUNCTION_ID,
                        ITEM_SOURCE_DESCRIPTION_FUNCTION_ID, USE_FUNCTION_ID, TARGET_TYPES,
                        WRITTEN_DATA)));
        assertThrows(IllegalArgumentException.class, () -> factory.apply(
                new ActiveAbilityDefinition(ID, "", CHARACTER_SOURCE_DESCRIPTION_FUNCTION_ID,
                        ITEM_SOURCE_DESCRIPTION_FUNCTION_ID, USE_FUNCTION_ID, TARGET_TYPES,
                        WRITTEN_DATA)));
        assertThrows(IllegalArgumentException.class, () -> factory.apply(
                new ActiveAbilityDefinition(ID, NAME, null, ITEM_SOURCE_DESCRIPTION_FUNCTION_ID,
                        USE_FUNCTION_ID, TARGET_TYPES, WRITTEN_DATA)));
        assertThrows(IllegalArgumentException.class, () -> factory.apply(
                new ActiveAbilityDefinition(ID, NAME, "", ITEM_SOURCE_DESCRIPTION_FUNCTION_ID,
                        USE_FUNCTION_ID, TARGET_TYPES, WRITTEN_DATA)));
        assertThrows(IllegalArgumentException.class, () -> factory.apply(
                new ActiveAbilityDefinition(ID, NAME, invalidFunctionId,
                        ITEM_SOURCE_DESCRIPTION_FUNCTION_ID, USE_FUNCTION_ID, TARGET_TYPES,
                        WRITTEN_DATA)));
        assertThrows(IllegalArgumentException.class, () -> factory.apply(
                new ActiveAbilityDefinition(ID, NAME, CHARACTER_SOURCE_DESCRIPTION_FUNCTION_ID,
                        null, USE_FUNCTION_ID, TARGET_TYPES, WRITTEN_DATA)));
        assertThrows(IllegalArgumentException.class, () -> factory.apply(
                new ActiveAbilityDefinition(ID, NAME, CHARACTER_SOURCE_DESCRIPTION_FUNCTION_ID, "",
                        USE_FUNCTION_ID, TARGET_TYPES, WRITTEN_DATA)));
        assertThrows(IllegalArgumentException.class, () -> factory.apply(
                new ActiveAbilityDefinition(ID, NAME, CHARACTER_SOURCE_DESCRIPTION_FUNCTION_ID,
                        invalidFunctionId, USE_FUNCTION_ID, TARGET_TYPES, WRITTEN_DATA)));
        assertThrows(IllegalArgumentException.class, () -> factory.apply(
                new ActiveAbilityDefinition(ID, NAME, CHARACTER_SOURCE_DESCRIPTION_FUNCTION_ID,
                        ITEM_SOURCE_DESCRIPTION_FUNCTION_ID, null, TARGET_TYPES, WRITTEN_DATA)));
        assertThrows(IllegalArgumentException.class, () -> factory.apply(
                new ActiveAbilityDefinition(ID, NAME, CHARACTER_SOURCE_DESCRIPTION_FUNCTION_ID,
                        ITEM_SOURCE_DESCRIPTION_FUNCTION_ID, "", TARGET_TYPES, WRITTEN_DATA)));
        assertThrows(IllegalArgumentException.class, () -> factory.apply(
                new ActiveAbilityDefinition(ID, NAME, CHARACTER_SOURCE_DESCRIPTION_FUNCTION_ID,
                        ITEM_SOURCE_DESCRIPTION_FUNCTION_ID, invalidConsumerId, TARGET_TYPES,
                        WRITTEN_DATA)));
        assertThrows(IllegalArgumentException.class, () -> factory.apply(
                new ActiveAbilityDefinition(ID, NAME, CHARACTER_SOURCE_DESCRIPTION_FUNCTION_ID,
                        ITEM_SOURCE_DESCRIPTION_FUNCTION_ID, USE_FUNCTION_ID, null, WRITTEN_DATA)));
        assertThrows(IllegalArgumentException.class, () -> factory.apply(
                new ActiveAbilityDefinition(ID, NAME, CHARACTER_SOURCE_DESCRIPTION_FUNCTION_ID,
                        ITEM_SOURCE_DESCRIPTION_FUNCTION_ID, USE_FUNCTION_ID,
                        arrayOf((TargetType) null), WRITTEN_DATA)));
        assertThrows(IllegalArgumentException.class, () -> factory.apply(
                new ActiveAbilityDefinition(ID, NAME, CHARACTER_SOURCE_DESCRIPTION_FUNCTION_ID,
                        ITEM_SOURCE_DESCRIPTION_FUNCTION_ID, USE_FUNCTION_ID, TARGET_TYPES, null)));
        assertThrows(IllegalArgumentException.class, () -> factory.apply(
                new ActiveAbilityDefinition(ID, NAME, CHARACTER_SOURCE_DESCRIPTION_FUNCTION_ID,
                        ITEM_SOURCE_DESCRIPTION_FUNCTION_ID, USE_FUNCTION_ID, TARGET_TYPES, "")));
    }
}
