package inaugural.soliloquy.ruleset.entities.factories.abilities;

import inaugural.soliloquy.ruleset.definitions.abilities.PassiveAbilityDefinition;
import inaugural.soliloquy.tools.testing.Mock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import soliloquy.specs.common.factories.Factory;
import soliloquy.specs.common.infrastructure.VariableCache;
import soliloquy.specs.common.persistence.TypeHandler;
import soliloquy.specs.gamestate.entities.Character;
import soliloquy.specs.gamestate.entities.Item;
import soliloquy.specs.ruleset.entities.abilities.PassiveAbility;

import java.util.function.Function;

import static inaugural.soliloquy.tools.random.Random.randomString;
import static inaugural.soliloquy.tools.testing.Mock.generateMockEntityAndHandler;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PassiveAbilityFactoryTests {
    private final String ID = randomString();
    private final String NAME = randomString();
    private final String DATA_WRITTEN = randomString();
    private final String CHARACTER_SOURCE_DESCRIPTION_FUNCTION_ID = randomString();
    private final String ITEM_SOURCE_DESCRIPTION_FUNCTION_ID = randomString();
    private final String DESCRIPTION = randomString();

    private final Mock.HandlerAndEntity<VariableCache> MOCK_DATA_AND_HANDLER =
            generateMockEntityAndHandler(VariableCache.class, DATA_WRITTEN);
    private final TypeHandler<VariableCache> MOCK_DATA_HANDLER = MOCK_DATA_AND_HANDLER.handler;
    private final VariableCache MOCK_DATA = MOCK_DATA_AND_HANDLER.entity;

    private Function<Character, String> mockCharacterSourceDescriptionFunction;
    private Function<Item, String> mockItemSourceDescriptionFunction;
    @SuppressWarnings("rawtypes") private Function<String, Function> mockGetFunction;

    private final PassiveAbilityDefinition DEFINITION =
            new PassiveAbilityDefinition(ID, NAME, DATA_WRITTEN,
                    CHARACTER_SOURCE_DESCRIPTION_FUNCTION_ID, ITEM_SOURCE_DESCRIPTION_FUNCTION_ID);

    private Factory<PassiveAbilityDefinition, PassiveAbility> factory;

    @BeforeEach
    void setUp() {
        //noinspection unchecked
        mockCharacterSourceDescriptionFunction = (Function<Character, String>) mock(Function.class);
        when(mockCharacterSourceDescriptionFunction.apply(any())).thenReturn(DESCRIPTION);

        //noinspection unchecked
        mockItemSourceDescriptionFunction = (Function<Item, String>) mock(Function.class);
        when(mockItemSourceDescriptionFunction.apply(any())).thenReturn(DESCRIPTION);

        //noinspection unchecked,rawtypes
        mockGetFunction = (Function<String, Function>) mock(Function.class);
        when(mockGetFunction.apply(CHARACTER_SOURCE_DESCRIPTION_FUNCTION_ID))
                .thenReturn(mockCharacterSourceDescriptionFunction);
        when(mockGetFunction.apply(ITEM_SOURCE_DESCRIPTION_FUNCTION_ID))
                .thenReturn(mockItemSourceDescriptionFunction);

        factory = new PassiveAbilityFactory(mockGetFunction, MOCK_DATA_HANDLER);
    }

    @Test
    void testConstructorWithInvalidParams() {
        assertThrows(IllegalArgumentException.class,
                () -> new PassiveAbilityFactory(null, MOCK_DATA_HANDLER));
        assertThrows(IllegalArgumentException.class,
                () -> new PassiveAbilityFactory(mockGetFunction, null));
    }

    @Test
    void testMake() {
        var output = factory.make(DEFINITION);

        assertNotNull(output);
        assertEquals(ID, output.id());
        assertEquals(NAME, output.getName());
        assertSame(MOCK_DATA, output.data());
        assertEquals(PassiveAbility.class.getCanonicalName(), output.getInterfaceName());
        verify(mockGetFunction).apply(CHARACTER_SOURCE_DESCRIPTION_FUNCTION_ID);
        verify(mockGetFunction).apply(ITEM_SOURCE_DESCRIPTION_FUNCTION_ID);
        verify(MOCK_DATA_HANDLER).read(DATA_WRITTEN);
    }

    @Test
    void testMakeWithInvalidParams() {
        var invalidFunctionId = randomString();
        when(mockGetFunction.apply(invalidFunctionId)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> factory.make(null));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new PassiveAbilityDefinition(null, NAME, DATA_WRITTEN,
                        CHARACTER_SOURCE_DESCRIPTION_FUNCTION_ID,
                        ITEM_SOURCE_DESCRIPTION_FUNCTION_ID)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new PassiveAbilityDefinition("", NAME, DATA_WRITTEN,
                        CHARACTER_SOURCE_DESCRIPTION_FUNCTION_ID,
                        ITEM_SOURCE_DESCRIPTION_FUNCTION_ID)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new PassiveAbilityDefinition(ID, null, DATA_WRITTEN,
                        CHARACTER_SOURCE_DESCRIPTION_FUNCTION_ID,
                        ITEM_SOURCE_DESCRIPTION_FUNCTION_ID)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new PassiveAbilityDefinition(ID, "", DATA_WRITTEN,
                        CHARACTER_SOURCE_DESCRIPTION_FUNCTION_ID,
                        ITEM_SOURCE_DESCRIPTION_FUNCTION_ID)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new PassiveAbilityDefinition(ID, NAME, null,
                        CHARACTER_SOURCE_DESCRIPTION_FUNCTION_ID,
                        ITEM_SOURCE_DESCRIPTION_FUNCTION_ID)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new PassiveAbilityDefinition(ID, NAME, "", CHARACTER_SOURCE_DESCRIPTION_FUNCTION_ID,
                        ITEM_SOURCE_DESCRIPTION_FUNCTION_ID)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new PassiveAbilityDefinition(ID, NAME, DATA_WRITTEN, null,
                        ITEM_SOURCE_DESCRIPTION_FUNCTION_ID)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new PassiveAbilityDefinition(ID, NAME, DATA_WRITTEN, "",
                        ITEM_SOURCE_DESCRIPTION_FUNCTION_ID)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new PassiveAbilityDefinition(ID, NAME, DATA_WRITTEN, invalidFunctionId,
                        ITEM_SOURCE_DESCRIPTION_FUNCTION_ID)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new PassiveAbilityDefinition(ID, NAME, DATA_WRITTEN,
                        CHARACTER_SOURCE_DESCRIPTION_FUNCTION_ID, null)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new PassiveAbilityDefinition(ID, NAME, DATA_WRITTEN,
                        CHARACTER_SOURCE_DESCRIPTION_FUNCTION_ID, "")));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new PassiveAbilityDefinition(ID, NAME, DATA_WRITTEN,
                        CHARACTER_SOURCE_DESCRIPTION_FUNCTION_ID, invalidFunctionId)));
    }

    @Test
    void testSetName() {
        var newName = randomString();
        var output = factory.make(DEFINITION);

        output.setName(newName);

        assertEquals(newName, output.getName());
    }

    @Test
    void testSetNameWithInvalidParams() {
        var output = factory.make(DEFINITION);

        assertThrows(IllegalArgumentException.class, () -> output.setName(null));
        assertThrows(IllegalArgumentException.class, () -> output.setName(""));
    }

    @Test
    void testCharacterDescription() {
        var mockCharacter = mock(Character.class);
        var output = factory.make(DEFINITION);

        var description = output.description(mockCharacter);

        assertEquals(DESCRIPTION, description);
        verify(mockCharacterSourceDescriptionFunction).apply(mockCharacter);
    }

    @Test
    void testCharacterDescriptionWithInvalidParams() {
        var output = factory.make(DEFINITION);

        assertThrows(IllegalArgumentException.class, () -> output.description((Character) null));
    }

    @Test
    void testItemDescription() {
        var mockItem = mock(Item.class);
        var output = factory.make(DEFINITION);

        var description = output.description(mockItem);

        assertEquals(DESCRIPTION, description);
        verify(mockItemSourceDescriptionFunction).apply(mockItem);
    }

    @Test
    void testItemDescriptionWithInvalidParams() {
        var output = factory.make(DEFINITION);

        assertThrows(IllegalArgumentException.class, () -> output.description((Item) null));
    }

    @Test
    void testGetInterfaceName() {
        assertEquals(Factory.class.getCanonicalName() + "<" +
                PassiveAbilityDefinition.class.getCanonicalName() + "," +
                PassiveAbility.class.getCanonicalName(), factory.getInterfaceName());
    }
}
