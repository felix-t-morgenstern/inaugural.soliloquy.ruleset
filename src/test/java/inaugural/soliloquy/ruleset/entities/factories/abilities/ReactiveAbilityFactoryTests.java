package inaugural.soliloquy.ruleset.entities.factories.abilities;

import inaugural.soliloquy.ruleset.definitions.abilities.ReactiveAbilityDefinition;
import inaugural.soliloquy.tools.testing.Mock.HandlerAndEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import soliloquy.specs.common.factories.Factory;
import soliloquy.specs.common.infrastructure.VariableCache;
import soliloquy.specs.common.persistence.TypeHandler;
import soliloquy.specs.gamestate.entities.Character;
import soliloquy.specs.gamestate.entities.Item;
import soliloquy.specs.gamestate.entities.abilities.AbilitySource;
import soliloquy.specs.ruleset.entities.abilities.ReactiveAbility;
import soliloquy.specs.ruleset.gameconcepts.CharacterEventFiring.FiringResponse;

import java.util.function.Function;

import static inaugural.soliloquy.tools.collections.Collections.arrayOf;
import static inaugural.soliloquy.tools.random.Random.randomBoolean;
import static inaugural.soliloquy.tools.random.Random.randomString;
import static inaugural.soliloquy.tools.testing.Mock.generateMockEntityAndHandler;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReactiveAbilityFactoryTests {
    private final String ID = randomString();
    private final String NAME = randomString();
    private final String CHARACTER_DESCRIPTION_FUNCTION_ID = randomString();
    private final String ITEM_DESCRIPTION_FUNCTION_ID = randomString();
    private final String FIRES_AGAINST_EVENT_FUNCTION_ID = randomString();
    private final String FIRES_AGAINST_ABILITY_FUNCTION_ID = randomString();
    private final String REACT_TO_EVENT_FUNCTION_ID = randomString();
    private final String REACT_TO_ABILITY_FUNCTION_ID = randomString();
    private final String DATA_WRITTEN = randomString();
    private final String DESCRIPTION = randomString();
    private final String EVENT = randomString();
    private final Boolean FIRES_AGAINST = randomBoolean();
    private final ReactiveAbilityDefinition DEFINITION =
            new ReactiveAbilityDefinition(ID, NAME, CHARACTER_DESCRIPTION_FUNCTION_ID,
                    ITEM_DESCRIPTION_FUNCTION_ID, FIRES_AGAINST_EVENT_FUNCTION_ID,
                    FIRES_AGAINST_ABILITY_FUNCTION_ID, REACT_TO_EVENT_FUNCTION_ID,
                    REACT_TO_ABILITY_FUNCTION_ID, DATA_WRITTEN);

    private final HandlerAndEntity<VariableCache> DATA_AND_HANDLER =
            generateMockEntityAndHandler(VariableCache.class, DATA_WRITTEN);
    private final TypeHandler<VariableCache> DATA_HANDLER = DATA_AND_HANDLER.handler;
    private final VariableCache DATA = DATA_AND_HANDLER.entity;

    private Function<Character, String> mockCharacterDescription;
    private Function<Item, String> mockItemDescription;
    private Function<Object[], Boolean> mockFiresAgainstEvent;
    private Function<AbilitySource, Boolean> mockFiresAgainstAbility;
    private Function<Object[], FiringResponse> mockReactToEvent;
    private Function<Object[], FiringResponse> mockReactToAbility;

    private Character mockCharacter;
    private AbilitySource mockAbilitySource;
    private FiringResponse mockFiringResponse;

    private ReactiveAbility output;

    @SuppressWarnings("rawtypes") private Function<String, Function> mockGetFunction;

    private Factory<ReactiveAbilityDefinition, ReactiveAbility> factory;

    @BeforeEach
    void setUp() {
        mockCharacter = mock(Character.class);
        mockAbilitySource = mock(AbilitySource.class);
        mockFiringResponse = mock(FiringResponse.class);

        //noinspection unchecked
        mockCharacterDescription = (Function<Character, String>) mock(Function.class);
        when(mockCharacterDescription.apply(any())).thenReturn(DESCRIPTION);
        //noinspection unchecked
        mockItemDescription = (Function<Item, String>) mock(Function.class);
        when(mockItemDescription.apply(any())).thenReturn(DESCRIPTION);
        //noinspection unchecked
        mockFiresAgainstEvent = (Function<Object[], Boolean>) mock(Function.class);
        when(mockFiresAgainstEvent.apply(any())).thenReturn(FIRES_AGAINST);
        //noinspection unchecked
        mockFiresAgainstAbility = (Function<AbilitySource, Boolean>) mock(Function.class);
        when(mockFiresAgainstAbility.apply(any())).thenReturn(FIRES_AGAINST);
        //noinspection unchecked
        mockReactToEvent = (Function<Object[], FiringResponse>) mock(Function.class);
        when(mockReactToEvent.apply(any())).thenReturn(mockFiringResponse);
        //noinspection unchecked
        mockReactToAbility = (Function<Object[], FiringResponse>) mock(Function.class);
        when(mockReactToEvent.apply(any())).thenReturn(mockFiringResponse);

        //noinspection unchecked,rawtypes
        mockGetFunction = (Function<String, Function>) mock(Function.class);
        when(mockGetFunction.apply(CHARACTER_DESCRIPTION_FUNCTION_ID)).thenReturn(
                mockCharacterDescription);
        when(mockGetFunction.apply(ITEM_DESCRIPTION_FUNCTION_ID)).thenReturn(mockItemDescription);
        when(mockGetFunction.apply(FIRES_AGAINST_EVENT_FUNCTION_ID)).thenReturn(
                mockFiresAgainstEvent);
        when(mockGetFunction.apply(FIRES_AGAINST_ABILITY_FUNCTION_ID)).thenReturn(
                mockFiresAgainstAbility);
        when(mockGetFunction.apply(REACT_TO_EVENT_FUNCTION_ID)).thenReturn(mockReactToEvent);
        when(mockGetFunction.apply(REACT_TO_ABILITY_FUNCTION_ID)).thenReturn(mockReactToAbility);

        factory = new ReactiveAbilityFactory(mockGetFunction, DATA_HANDLER);
    }

    @Test
    void testConstructorWithInvalidArgs() {
        assertThrows(IllegalArgumentException.class,
                () -> new ReactiveAbilityFactory(null, DATA_HANDLER));
        assertThrows(IllegalArgumentException.class,
                () -> new ReactiveAbilityFactory(mockGetFunction, null));
    }

    @Test
    void testMake() {
        var output = factory.make(DEFINITION);

        assertNotNull(output);
        assertEquals(ID, output.id());
        assertEquals(NAME, output.getName());
        assertSame(DATA, output.data());
        assertEquals(ReactiveAbility.class.getCanonicalName(), output.getInterfaceName());
        verify(mockGetFunction).apply(CHARACTER_DESCRIPTION_FUNCTION_ID);
        verify(mockGetFunction).apply(ITEM_DESCRIPTION_FUNCTION_ID);
        verify(mockGetFunction).apply(FIRES_AGAINST_ABILITY_FUNCTION_ID);
        verify(mockGetFunction).apply(FIRES_AGAINST_EVENT_FUNCTION_ID);
        verify(mockGetFunction).apply(REACT_TO_ABILITY_FUNCTION_ID);
        verify(mockGetFunction).apply(REACT_TO_EVENT_FUNCTION_ID);
        verify(DATA_HANDLER).read(DATA_WRITTEN);
    }

    @Test
    void testMakeWithInvalidArgs() {
        var invalidFunctionId = randomString();
        when(mockGetFunction.apply(invalidFunctionId)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> factory.make(null));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new ReactiveAbilityDefinition(null, NAME, CHARACTER_DESCRIPTION_FUNCTION_ID,
                        ITEM_DESCRIPTION_FUNCTION_ID, FIRES_AGAINST_EVENT_FUNCTION_ID,
                        FIRES_AGAINST_ABILITY_FUNCTION_ID, REACT_TO_EVENT_FUNCTION_ID,
                        REACT_TO_ABILITY_FUNCTION_ID, DATA_WRITTEN)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new ReactiveAbilityDefinition("", NAME, CHARACTER_DESCRIPTION_FUNCTION_ID,
                        ITEM_DESCRIPTION_FUNCTION_ID, FIRES_AGAINST_EVENT_FUNCTION_ID,
                        FIRES_AGAINST_ABILITY_FUNCTION_ID, REACT_TO_EVENT_FUNCTION_ID,
                        REACT_TO_ABILITY_FUNCTION_ID, DATA_WRITTEN)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new ReactiveAbilityDefinition(ID, null, CHARACTER_DESCRIPTION_FUNCTION_ID,
                        ITEM_DESCRIPTION_FUNCTION_ID, FIRES_AGAINST_EVENT_FUNCTION_ID,
                        FIRES_AGAINST_ABILITY_FUNCTION_ID, REACT_TO_EVENT_FUNCTION_ID,
                        REACT_TO_ABILITY_FUNCTION_ID, DATA_WRITTEN)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new ReactiveAbilityDefinition(ID, "", CHARACTER_DESCRIPTION_FUNCTION_ID,
                        ITEM_DESCRIPTION_FUNCTION_ID, FIRES_AGAINST_EVENT_FUNCTION_ID,
                        FIRES_AGAINST_ABILITY_FUNCTION_ID, REACT_TO_EVENT_FUNCTION_ID,
                        REACT_TO_ABILITY_FUNCTION_ID, DATA_WRITTEN)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new ReactiveAbilityDefinition(ID, NAME, null, ITEM_DESCRIPTION_FUNCTION_ID,
                        FIRES_AGAINST_EVENT_FUNCTION_ID, FIRES_AGAINST_ABILITY_FUNCTION_ID,
                        REACT_TO_EVENT_FUNCTION_ID, REACT_TO_ABILITY_FUNCTION_ID, DATA_WRITTEN)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new ReactiveAbilityDefinition(ID, NAME, "", ITEM_DESCRIPTION_FUNCTION_ID,
                        FIRES_AGAINST_EVENT_FUNCTION_ID, FIRES_AGAINST_ABILITY_FUNCTION_ID,
                        REACT_TO_EVENT_FUNCTION_ID, REACT_TO_ABILITY_FUNCTION_ID, DATA_WRITTEN)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new ReactiveAbilityDefinition(ID, NAME, invalidFunctionId,
                        ITEM_DESCRIPTION_FUNCTION_ID, FIRES_AGAINST_EVENT_FUNCTION_ID,
                        FIRES_AGAINST_ABILITY_FUNCTION_ID, REACT_TO_EVENT_FUNCTION_ID,
                        REACT_TO_ABILITY_FUNCTION_ID, DATA_WRITTEN)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new ReactiveAbilityDefinition(ID, NAME, CHARACTER_DESCRIPTION_FUNCTION_ID, null,
                        FIRES_AGAINST_EVENT_FUNCTION_ID, FIRES_AGAINST_ABILITY_FUNCTION_ID,
                        REACT_TO_EVENT_FUNCTION_ID, REACT_TO_ABILITY_FUNCTION_ID, DATA_WRITTEN)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new ReactiveAbilityDefinition(ID, NAME, CHARACTER_DESCRIPTION_FUNCTION_ID, "",
                        FIRES_AGAINST_EVENT_FUNCTION_ID, FIRES_AGAINST_ABILITY_FUNCTION_ID,
                        REACT_TO_EVENT_FUNCTION_ID, REACT_TO_ABILITY_FUNCTION_ID, DATA_WRITTEN)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new ReactiveAbilityDefinition(ID, NAME, CHARACTER_DESCRIPTION_FUNCTION_ID,
                        invalidFunctionId, FIRES_AGAINST_EVENT_FUNCTION_ID,
                        FIRES_AGAINST_ABILITY_FUNCTION_ID, REACT_TO_EVENT_FUNCTION_ID,
                        REACT_TO_ABILITY_FUNCTION_ID, DATA_WRITTEN)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new ReactiveAbilityDefinition(ID, NAME, CHARACTER_DESCRIPTION_FUNCTION_ID,
                        ITEM_DESCRIPTION_FUNCTION_ID, null, FIRES_AGAINST_ABILITY_FUNCTION_ID,
                        REACT_TO_EVENT_FUNCTION_ID, REACT_TO_ABILITY_FUNCTION_ID, DATA_WRITTEN)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new ReactiveAbilityDefinition(ID, NAME, CHARACTER_DESCRIPTION_FUNCTION_ID,
                        ITEM_DESCRIPTION_FUNCTION_ID, "", FIRES_AGAINST_ABILITY_FUNCTION_ID,
                        REACT_TO_EVENT_FUNCTION_ID, REACT_TO_ABILITY_FUNCTION_ID, DATA_WRITTEN)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new ReactiveAbilityDefinition(ID, NAME, CHARACTER_DESCRIPTION_FUNCTION_ID,
                        ITEM_DESCRIPTION_FUNCTION_ID, invalidFunctionId,
                        FIRES_AGAINST_ABILITY_FUNCTION_ID, REACT_TO_EVENT_FUNCTION_ID,
                        REACT_TO_ABILITY_FUNCTION_ID, DATA_WRITTEN)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new ReactiveAbilityDefinition(ID, NAME, CHARACTER_DESCRIPTION_FUNCTION_ID,
                        ITEM_DESCRIPTION_FUNCTION_ID, FIRES_AGAINST_EVENT_FUNCTION_ID, null,
                        REACT_TO_EVENT_FUNCTION_ID, REACT_TO_ABILITY_FUNCTION_ID, DATA_WRITTEN)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new ReactiveAbilityDefinition(ID, NAME, CHARACTER_DESCRIPTION_FUNCTION_ID,
                        ITEM_DESCRIPTION_FUNCTION_ID, FIRES_AGAINST_EVENT_FUNCTION_ID, "",
                        REACT_TO_EVENT_FUNCTION_ID, REACT_TO_ABILITY_FUNCTION_ID, DATA_WRITTEN)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new ReactiveAbilityDefinition(ID, NAME, CHARACTER_DESCRIPTION_FUNCTION_ID,
                        ITEM_DESCRIPTION_FUNCTION_ID, FIRES_AGAINST_EVENT_FUNCTION_ID,
                        invalidFunctionId, REACT_TO_EVENT_FUNCTION_ID, REACT_TO_ABILITY_FUNCTION_ID,
                        DATA_WRITTEN)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new ReactiveAbilityDefinition(ID, NAME, CHARACTER_DESCRIPTION_FUNCTION_ID,
                        ITEM_DESCRIPTION_FUNCTION_ID, FIRES_AGAINST_EVENT_FUNCTION_ID,
                        FIRES_AGAINST_ABILITY_FUNCTION_ID, null, REACT_TO_ABILITY_FUNCTION_ID,
                        DATA_WRITTEN)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new ReactiveAbilityDefinition(ID, NAME, CHARACTER_DESCRIPTION_FUNCTION_ID,
                        ITEM_DESCRIPTION_FUNCTION_ID, FIRES_AGAINST_EVENT_FUNCTION_ID,
                        FIRES_AGAINST_ABILITY_FUNCTION_ID, "", REACT_TO_ABILITY_FUNCTION_ID,
                        DATA_WRITTEN)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new ReactiveAbilityDefinition(ID, NAME, CHARACTER_DESCRIPTION_FUNCTION_ID,
                        ITEM_DESCRIPTION_FUNCTION_ID, FIRES_AGAINST_EVENT_FUNCTION_ID,
                        FIRES_AGAINST_ABILITY_FUNCTION_ID, invalidFunctionId,
                        REACT_TO_ABILITY_FUNCTION_ID, DATA_WRITTEN)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new ReactiveAbilityDefinition(ID, NAME, CHARACTER_DESCRIPTION_FUNCTION_ID,
                        ITEM_DESCRIPTION_FUNCTION_ID, FIRES_AGAINST_EVENT_FUNCTION_ID,
                        FIRES_AGAINST_ABILITY_FUNCTION_ID, REACT_TO_EVENT_FUNCTION_ID, null,
                        DATA_WRITTEN)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new ReactiveAbilityDefinition(ID, NAME, CHARACTER_DESCRIPTION_FUNCTION_ID,
                        ITEM_DESCRIPTION_FUNCTION_ID, FIRES_AGAINST_EVENT_FUNCTION_ID,
                        FIRES_AGAINST_ABILITY_FUNCTION_ID, REACT_TO_EVENT_FUNCTION_ID, "",
                        DATA_WRITTEN)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new ReactiveAbilityDefinition(ID, NAME, CHARACTER_DESCRIPTION_FUNCTION_ID,
                        ITEM_DESCRIPTION_FUNCTION_ID, FIRES_AGAINST_EVENT_FUNCTION_ID,
                        FIRES_AGAINST_ABILITY_FUNCTION_ID, REACT_TO_EVENT_FUNCTION_ID,
                        invalidFunctionId, DATA_WRITTEN)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new ReactiveAbilityDefinition(ID, NAME, CHARACTER_DESCRIPTION_FUNCTION_ID,
                        ITEM_DESCRIPTION_FUNCTION_ID, FIRES_AGAINST_EVENT_FUNCTION_ID,
                        FIRES_AGAINST_ABILITY_FUNCTION_ID, REACT_TO_EVENT_FUNCTION_ID,
                        REACT_TO_ABILITY_FUNCTION_ID, null)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new ReactiveAbilityDefinition(ID, NAME, CHARACTER_DESCRIPTION_FUNCTION_ID,
                        ITEM_DESCRIPTION_FUNCTION_ID, FIRES_AGAINST_EVENT_FUNCTION_ID,
                        FIRES_AGAINST_ABILITY_FUNCTION_ID, REACT_TO_EVENT_FUNCTION_ID,
                        REACT_TO_ABILITY_FUNCTION_ID, "")));
    }

    @Test
    void testSetName() {
        var output = factory.make(DEFINITION);
        var newName = randomString();

        output.setName(newName);

        assertEquals(newName, output.getName());
    }

    @Test
    void testSetNameWithInvalidArgs() {
        var output = factory.make(DEFINITION);

        assertThrows(IllegalArgumentException.class, () -> output.setName(null));
        assertThrows(IllegalArgumentException.class, () -> output.setName(""));
    }

    @Test
    void testDescriptionWithCharacterSource() {
        var output = factory.make(DEFINITION);

        var description = output.description(mockCharacter);

        assertEquals(DESCRIPTION, description);
        verify(mockCharacterDescription).apply(mockCharacter);
    }

    @Test
    void testDescriptionWithCharacterSourceWithInvalidArgs() {
        var output = factory.make(DEFINITION);

        assertThrows(IllegalArgumentException.class, () -> output.description((Character) null));
    }

    @Test
    void testDescriptionWithItemSource() {
        var output = factory.make(DEFINITION);
        var mockItem = mock(Item.class);

        var description = output.description(mockItem);

        assertEquals(DESCRIPTION, description);
        verify(mockItemDescription).apply(mockItem);
    }

    @Test
    void testDescriptionWithItemSourceWithInvalidArgs() {
        var output = factory.make(DEFINITION);

        assertThrows(IllegalArgumentException.class, () -> output.description((Item) null));
    }

    @Test
    void testFiresAgainstEvent() {
        var output = factory.make(DEFINITION);

        var firesAgainstEvent = output.firesAgainstEvent(EVENT, DATA);

        assertEquals(FIRES_AGAINST, firesAgainstEvent);
        verify(mockFiresAgainstEvent).apply(eq(arrayOf(EVENT, DATA)));
    }

    @Test
    void testFiresAgainstEventWithInvalidArgs() {
        var output = factory.make(DEFINITION);

        assertThrows(IllegalArgumentException.class, () -> output.firesAgainstEvent(null, DATA));
        assertThrows(IllegalArgumentException.class, () -> output.firesAgainstEvent("", DATA));
        assertThrows(IllegalArgumentException.class, () -> output.firesAgainstEvent(EVENT, null));
    }

    @Test
    void testFiresAgainstAbility() {
        var output = factory.make(DEFINITION);

        var firesAgainstAbility = output.firesAgainstAbility(mockAbilitySource);

        assertEquals(FIRES_AGAINST, firesAgainstAbility);
        verify(mockFiresAgainstAbility).apply(mockAbilitySource);
    }

    @Test
    void testFiresAgainstAbilityWithInvalidArgs() {
        var output = factory.make(DEFINITION);

        assertThrows(IllegalArgumentException.class, () -> output.firesAgainstAbility(null));
    }

    @Test
    void testReactToAbility() {
        var output = factory.make(DEFINITION);

        output.reactToAbility(mockCharacter, mockAbilitySource);

        verify(mockReactToAbility).apply(arrayOf(mockCharacter, mockAbilitySource));
    }

    @Test
    void testReactToAbilityWithInvalidArgs() {
        var output = factory.make(DEFINITION);

        assertThrows(IllegalArgumentException.class, () -> output.reactToAbility(null, mockAbilitySource));
        assertThrows(IllegalArgumentException.class, () -> output.reactToAbility(mockCharacter, null));
    }

    @Test
    void testReactToEvent() {
        var output = factory.make(DEFINITION);

        output.reactToEvent(mockCharacter, EVENT, DATA);

        verify(mockReactToEvent).apply(arrayOf(mockCharacter, EVENT, DATA));
    }

    @Test
    void testReactToEventWithInvalidArgs() {
        var output = factory.make(DEFINITION);

        assertThrows(IllegalArgumentException.class, () -> output.reactToEvent(null, EVENT, DATA));
        assertThrows(IllegalArgumentException.class, () -> output.reactToEvent(mockCharacter, null, DATA));
        assertThrows(IllegalArgumentException.class, () -> output.reactToEvent(mockCharacter, "", DATA));
        assertThrows(IllegalArgumentException.class, () -> output.reactToEvent(mockCharacter, EVENT, null));
    }

    @Test
    void testGetInterfaceName() {
        assertEquals(Factory.class.getCanonicalName() + "<" +
                ReactiveAbilityDefinition.class.getCanonicalName() + "," +
                ReactiveAbility.class.getCanonicalName() + ">", factory.getInterfaceName());
    }
}
