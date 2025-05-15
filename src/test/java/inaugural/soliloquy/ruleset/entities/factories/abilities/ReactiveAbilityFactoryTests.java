package inaugural.soliloquy.ruleset.entities.factories.abilities;

import inaugural.soliloquy.ruleset.definitions.abilities.ReactiveAbilityDefinition;
import inaugural.soliloquy.tools.testing.Mock.HandlerAndEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import soliloquy.specs.common.persistence.TypeHandler;
import soliloquy.specs.gamestate.entities.Character;
import soliloquy.specs.gamestate.entities.Item;
import soliloquy.specs.gamestate.entities.abilities.AbilitySource;
import soliloquy.specs.ruleset.entities.abilities.ReactiveAbility;
import soliloquy.specs.ruleset.gameconcepts.CharacterEventFiring.FiringResponse;

import java.util.Map;
import java.util.function.Function;

import static inaugural.soliloquy.tools.collections.Collections.arrayOf;
import static inaugural.soliloquy.tools.collections.Collections.immutable;
import static inaugural.soliloquy.tools.random.Random.randomBoolean;
import static inaugural.soliloquy.tools.random.Random.randomString;
import static inaugural.soliloquy.tools.testing.Mock.generateMockEntityAndHandler;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ReactiveAbilityFactoryTests {
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

    @SuppressWarnings("rawtypes") private final HandlerAndEntity<Map> DATA_AND_HANDLER =
            generateMockEntityAndHandler(Map.class, DATA_WRITTEN);
    @SuppressWarnings("rawtypes")
    private final TypeHandler<Map> MAP_HANDLER = DATA_AND_HANDLER.handler;
    @SuppressWarnings("unchecked")
    private final Map<String, Object> DATA = DATA_AND_HANDLER.entity;

    private Function<Character, String> mockCharacterDescription;
    private Function<Item, String> mockItemDescription;
    private Function<Object[], Boolean> mockFiresAgainstEvent;
    private Function<AbilitySource, Boolean> mockFiresAgainstAbility;
    private Function<Object[], FiringResponse> mockReactToEvent;
    private Function<Object[], FiringResponse> mockReactToAbility;

    private Character mockCharacter;
    private AbilitySource mockAbilitySource;

    private ReactiveAbility output;

    @SuppressWarnings("rawtypes") private Function<String, Function> mockGetFunction;

    private Function<ReactiveAbilityDefinition, ReactiveAbility> factory;

    @BeforeEach
    void setUp() {
        mockCharacter = mock(Character.class);
        mockAbilitySource = mock(AbilitySource.class);
        var mockFiringResponse = mock(FiringResponse.class);

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

        factory = new ReactiveAbilityFactory(mockGetFunction, MAP_HANDLER);
    }

    @Test
    void testConstructorWithInvalidArgs() {
        assertThrows(IllegalArgumentException.class,
                () -> new ReactiveAbilityFactory(null, MAP_HANDLER));
        assertThrows(IllegalArgumentException.class,
                () -> new ReactiveAbilityFactory(mockGetFunction, null));
    }

    @Test
    void testMake() {
        var output = factory.apply(DEFINITION);

        assertNotNull(output);
        assertEquals(ID, output.id());
        assertEquals(NAME, output.getName());
        assertSame(DATA, output.data());
        verify(mockGetFunction).apply(CHARACTER_DESCRIPTION_FUNCTION_ID);
        verify(mockGetFunction).apply(ITEM_DESCRIPTION_FUNCTION_ID);
        verify(mockGetFunction).apply(FIRES_AGAINST_ABILITY_FUNCTION_ID);
        verify(mockGetFunction).apply(FIRES_AGAINST_EVENT_FUNCTION_ID);
        verify(mockGetFunction).apply(REACT_TO_ABILITY_FUNCTION_ID);
        verify(mockGetFunction).apply(REACT_TO_EVENT_FUNCTION_ID);
        verify(MAP_HANDLER).read(DATA_WRITTEN);
    }

    @Test
    void testMakeWithInvalidArgs() {
        var invalidFunctionId = randomString();
        when(mockGetFunction.apply(invalidFunctionId)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> factory.apply(null));
        assertThrows(IllegalArgumentException.class, () -> factory.apply(
                new ReactiveAbilityDefinition(null, NAME, CHARACTER_DESCRIPTION_FUNCTION_ID,
                        ITEM_DESCRIPTION_FUNCTION_ID, FIRES_AGAINST_EVENT_FUNCTION_ID,
                        FIRES_AGAINST_ABILITY_FUNCTION_ID, REACT_TO_EVENT_FUNCTION_ID,
                        REACT_TO_ABILITY_FUNCTION_ID, DATA_WRITTEN)));
        assertThrows(IllegalArgumentException.class, () -> factory.apply(
                new ReactiveAbilityDefinition("", NAME, CHARACTER_DESCRIPTION_FUNCTION_ID,
                        ITEM_DESCRIPTION_FUNCTION_ID, FIRES_AGAINST_EVENT_FUNCTION_ID,
                        FIRES_AGAINST_ABILITY_FUNCTION_ID, REACT_TO_EVENT_FUNCTION_ID,
                        REACT_TO_ABILITY_FUNCTION_ID, DATA_WRITTEN)));
        assertThrows(IllegalArgumentException.class, () -> factory.apply(
                new ReactiveAbilityDefinition(ID, null, CHARACTER_DESCRIPTION_FUNCTION_ID,
                        ITEM_DESCRIPTION_FUNCTION_ID, FIRES_AGAINST_EVENT_FUNCTION_ID,
                        FIRES_AGAINST_ABILITY_FUNCTION_ID, REACT_TO_EVENT_FUNCTION_ID,
                        REACT_TO_ABILITY_FUNCTION_ID, DATA_WRITTEN)));
        assertThrows(IllegalArgumentException.class, () -> factory.apply(
                new ReactiveAbilityDefinition(ID, "", CHARACTER_DESCRIPTION_FUNCTION_ID,
                        ITEM_DESCRIPTION_FUNCTION_ID, FIRES_AGAINST_EVENT_FUNCTION_ID,
                        FIRES_AGAINST_ABILITY_FUNCTION_ID, REACT_TO_EVENT_FUNCTION_ID,
                        REACT_TO_ABILITY_FUNCTION_ID, DATA_WRITTEN)));
        assertThrows(IllegalArgumentException.class, () -> factory.apply(
                new ReactiveAbilityDefinition(ID, NAME, null, ITEM_DESCRIPTION_FUNCTION_ID,
                        FIRES_AGAINST_EVENT_FUNCTION_ID, FIRES_AGAINST_ABILITY_FUNCTION_ID,
                        REACT_TO_EVENT_FUNCTION_ID, REACT_TO_ABILITY_FUNCTION_ID, DATA_WRITTEN)));
        assertThrows(IllegalArgumentException.class, () -> factory.apply(
                new ReactiveAbilityDefinition(ID, NAME, "", ITEM_DESCRIPTION_FUNCTION_ID,
                        FIRES_AGAINST_EVENT_FUNCTION_ID, FIRES_AGAINST_ABILITY_FUNCTION_ID,
                        REACT_TO_EVENT_FUNCTION_ID, REACT_TO_ABILITY_FUNCTION_ID, DATA_WRITTEN)));
        assertThrows(IllegalArgumentException.class, () -> factory.apply(
                new ReactiveAbilityDefinition(ID, NAME, invalidFunctionId,
                        ITEM_DESCRIPTION_FUNCTION_ID, FIRES_AGAINST_EVENT_FUNCTION_ID,
                        FIRES_AGAINST_ABILITY_FUNCTION_ID, REACT_TO_EVENT_FUNCTION_ID,
                        REACT_TO_ABILITY_FUNCTION_ID, DATA_WRITTEN)));
        assertThrows(IllegalArgumentException.class, () -> factory.apply(
                new ReactiveAbilityDefinition(ID, NAME, CHARACTER_DESCRIPTION_FUNCTION_ID, null,
                        FIRES_AGAINST_EVENT_FUNCTION_ID, FIRES_AGAINST_ABILITY_FUNCTION_ID,
                        REACT_TO_EVENT_FUNCTION_ID, REACT_TO_ABILITY_FUNCTION_ID, DATA_WRITTEN)));
        assertThrows(IllegalArgumentException.class, () -> factory.apply(
                new ReactiveAbilityDefinition(ID, NAME, CHARACTER_DESCRIPTION_FUNCTION_ID, "",
                        FIRES_AGAINST_EVENT_FUNCTION_ID, FIRES_AGAINST_ABILITY_FUNCTION_ID,
                        REACT_TO_EVENT_FUNCTION_ID, REACT_TO_ABILITY_FUNCTION_ID, DATA_WRITTEN)));
        assertThrows(IllegalArgumentException.class, () -> factory.apply(
                new ReactiveAbilityDefinition(ID, NAME, CHARACTER_DESCRIPTION_FUNCTION_ID,
                        invalidFunctionId, FIRES_AGAINST_EVENT_FUNCTION_ID,
                        FIRES_AGAINST_ABILITY_FUNCTION_ID, REACT_TO_EVENT_FUNCTION_ID,
                        REACT_TO_ABILITY_FUNCTION_ID, DATA_WRITTEN)));
        assertThrows(IllegalArgumentException.class, () -> factory.apply(
                new ReactiveAbilityDefinition(ID, NAME, CHARACTER_DESCRIPTION_FUNCTION_ID,
                        ITEM_DESCRIPTION_FUNCTION_ID, null, FIRES_AGAINST_ABILITY_FUNCTION_ID,
                        REACT_TO_EVENT_FUNCTION_ID, REACT_TO_ABILITY_FUNCTION_ID, DATA_WRITTEN)));
        assertThrows(IllegalArgumentException.class, () -> factory.apply(
                new ReactiveAbilityDefinition(ID, NAME, CHARACTER_DESCRIPTION_FUNCTION_ID,
                        ITEM_DESCRIPTION_FUNCTION_ID, "", FIRES_AGAINST_ABILITY_FUNCTION_ID,
                        REACT_TO_EVENT_FUNCTION_ID, REACT_TO_ABILITY_FUNCTION_ID, DATA_WRITTEN)));
        assertThrows(IllegalArgumentException.class, () -> factory.apply(
                new ReactiveAbilityDefinition(ID, NAME, CHARACTER_DESCRIPTION_FUNCTION_ID,
                        ITEM_DESCRIPTION_FUNCTION_ID, invalidFunctionId,
                        FIRES_AGAINST_ABILITY_FUNCTION_ID, REACT_TO_EVENT_FUNCTION_ID,
                        REACT_TO_ABILITY_FUNCTION_ID, DATA_WRITTEN)));
        assertThrows(IllegalArgumentException.class, () -> factory.apply(
                new ReactiveAbilityDefinition(ID, NAME, CHARACTER_DESCRIPTION_FUNCTION_ID,
                        ITEM_DESCRIPTION_FUNCTION_ID, FIRES_AGAINST_EVENT_FUNCTION_ID, null,
                        REACT_TO_EVENT_FUNCTION_ID, REACT_TO_ABILITY_FUNCTION_ID, DATA_WRITTEN)));
        assertThrows(IllegalArgumentException.class, () -> factory.apply(
                new ReactiveAbilityDefinition(ID, NAME, CHARACTER_DESCRIPTION_FUNCTION_ID,
                        ITEM_DESCRIPTION_FUNCTION_ID, FIRES_AGAINST_EVENT_FUNCTION_ID, "",
                        REACT_TO_EVENT_FUNCTION_ID, REACT_TO_ABILITY_FUNCTION_ID, DATA_WRITTEN)));
        assertThrows(IllegalArgumentException.class, () -> factory.apply(
                new ReactiveAbilityDefinition(ID, NAME, CHARACTER_DESCRIPTION_FUNCTION_ID,
                        ITEM_DESCRIPTION_FUNCTION_ID, FIRES_AGAINST_EVENT_FUNCTION_ID,
                        invalidFunctionId, REACT_TO_EVENT_FUNCTION_ID, REACT_TO_ABILITY_FUNCTION_ID,
                        DATA_WRITTEN)));
        assertThrows(IllegalArgumentException.class, () -> factory.apply(
                new ReactiveAbilityDefinition(ID, NAME, CHARACTER_DESCRIPTION_FUNCTION_ID,
                        ITEM_DESCRIPTION_FUNCTION_ID, FIRES_AGAINST_EVENT_FUNCTION_ID,
                        FIRES_AGAINST_ABILITY_FUNCTION_ID, null, REACT_TO_ABILITY_FUNCTION_ID,
                        DATA_WRITTEN)));
        assertThrows(IllegalArgumentException.class, () -> factory.apply(
                new ReactiveAbilityDefinition(ID, NAME, CHARACTER_DESCRIPTION_FUNCTION_ID,
                        ITEM_DESCRIPTION_FUNCTION_ID, FIRES_AGAINST_EVENT_FUNCTION_ID,
                        FIRES_AGAINST_ABILITY_FUNCTION_ID, "", REACT_TO_ABILITY_FUNCTION_ID,
                        DATA_WRITTEN)));
        assertThrows(IllegalArgumentException.class, () -> factory.apply(
                new ReactiveAbilityDefinition(ID, NAME, CHARACTER_DESCRIPTION_FUNCTION_ID,
                        ITEM_DESCRIPTION_FUNCTION_ID, FIRES_AGAINST_EVENT_FUNCTION_ID,
                        FIRES_AGAINST_ABILITY_FUNCTION_ID, invalidFunctionId,
                        REACT_TO_ABILITY_FUNCTION_ID, DATA_WRITTEN)));
        assertThrows(IllegalArgumentException.class, () -> factory.apply(
                new ReactiveAbilityDefinition(ID, NAME, CHARACTER_DESCRIPTION_FUNCTION_ID,
                        ITEM_DESCRIPTION_FUNCTION_ID, FIRES_AGAINST_EVENT_FUNCTION_ID,
                        FIRES_AGAINST_ABILITY_FUNCTION_ID, REACT_TO_EVENT_FUNCTION_ID, null,
                        DATA_WRITTEN)));
        assertThrows(IllegalArgumentException.class, () -> factory.apply(
                new ReactiveAbilityDefinition(ID, NAME, CHARACTER_DESCRIPTION_FUNCTION_ID,
                        ITEM_DESCRIPTION_FUNCTION_ID, FIRES_AGAINST_EVENT_FUNCTION_ID,
                        FIRES_AGAINST_ABILITY_FUNCTION_ID, REACT_TO_EVENT_FUNCTION_ID, "",
                        DATA_WRITTEN)));
        assertThrows(IllegalArgumentException.class, () -> factory.apply(
                new ReactiveAbilityDefinition(ID, NAME, CHARACTER_DESCRIPTION_FUNCTION_ID,
                        ITEM_DESCRIPTION_FUNCTION_ID, FIRES_AGAINST_EVENT_FUNCTION_ID,
                        FIRES_AGAINST_ABILITY_FUNCTION_ID, REACT_TO_EVENT_FUNCTION_ID,
                        invalidFunctionId, DATA_WRITTEN)));
        assertThrows(IllegalArgumentException.class, () -> factory.apply(
                new ReactiveAbilityDefinition(ID, NAME, CHARACTER_DESCRIPTION_FUNCTION_ID,
                        ITEM_DESCRIPTION_FUNCTION_ID, FIRES_AGAINST_EVENT_FUNCTION_ID,
                        FIRES_AGAINST_ABILITY_FUNCTION_ID, REACT_TO_EVENT_FUNCTION_ID,
                        REACT_TO_ABILITY_FUNCTION_ID, null)));
        assertThrows(IllegalArgumentException.class, () -> factory.apply(
                new ReactiveAbilityDefinition(ID, NAME, CHARACTER_DESCRIPTION_FUNCTION_ID,
                        ITEM_DESCRIPTION_FUNCTION_ID, FIRES_AGAINST_EVENT_FUNCTION_ID,
                        FIRES_AGAINST_ABILITY_FUNCTION_ID, REACT_TO_EVENT_FUNCTION_ID,
                        REACT_TO_ABILITY_FUNCTION_ID, "")));
    }

    @Test
    void testSetName() {
        var output = factory.apply(DEFINITION);
        var newName = randomString();

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
    void testDescriptionWithCharacterSource() {
        var output = factory.apply(DEFINITION);

        var description = output.description(mockCharacter);

        assertEquals(DESCRIPTION, description);
        verify(mockCharacterDescription).apply(mockCharacter);
    }

    @Test
    void testDescriptionWithCharacterSourceWithInvalidArgs() {
        var output = factory.apply(DEFINITION);

        assertThrows(IllegalArgumentException.class, () -> output.description((Character) null));
    }

    @Test
    void testDescriptionWithItemSource() {
        var output = factory.apply(DEFINITION);
        var mockItem = mock(Item.class);

        var description = output.description(mockItem);

        assertEquals(DESCRIPTION, description);
        verify(mockItemDescription).apply(mockItem);
    }

    @Test
    void testDescriptionWithItemSourceWithInvalidArgs() {
        var output = factory.apply(DEFINITION);

        assertThrows(IllegalArgumentException.class, () -> output.description((Item) null));
    }

    @Test
    void testFiresAgainstEvent() {
        var output = factory.apply(DEFINITION);

        var firesAgainstEvent = output.firesAgainstEvent(EVENT, immutable(DATA));

        assertEquals(FIRES_AGAINST, firesAgainstEvent);
        verify(mockFiresAgainstEvent).apply(eq(arrayOf(EVENT, immutable(DATA))));
    }

    @Test
    void testFiresAgainstEventWithInvalidArgs() {
        var output = factory.apply(DEFINITION);

        assertThrows(IllegalArgumentException.class, () -> output.firesAgainstEvent(null, immutable(DATA)));
        assertThrows(IllegalArgumentException.class, () -> output.firesAgainstEvent("", immutable(DATA)));
        assertThrows(IllegalArgumentException.class, () -> output.firesAgainstEvent(EVENT, null));
    }

    @Test
    void testFiresAgainstAbility() {
        var output = factory.apply(DEFINITION);

        var firesAgainstAbility = output.firesAgainstAbility(mockAbilitySource);

        assertEquals(FIRES_AGAINST, firesAgainstAbility);
        verify(mockFiresAgainstAbility).apply(mockAbilitySource);
    }

    @Test
    void testFiresAgainstAbilityWithInvalidArgs() {
        var output = factory.apply(DEFINITION);

        assertThrows(IllegalArgumentException.class, () -> output.firesAgainstAbility(null));
    }

    @Test
    void testReactToAbility() {
        var output = factory.apply(DEFINITION);

        output.reactToAbility(mockCharacter, mockAbilitySource);

        verify(mockReactToAbility).apply(arrayOf(mockCharacter, mockAbilitySource));
    }

    @Test
    void testReactToAbilityWithInvalidArgs() {
        var output = factory.apply(DEFINITION);

        assertThrows(IllegalArgumentException.class, () -> output.reactToAbility(null, mockAbilitySource));
        assertThrows(IllegalArgumentException.class, () -> output.reactToAbility(mockCharacter, null));
    }

    @Test
    void testReactToEvent() {
        var output = factory.apply(DEFINITION);

        output.reactToEvent(mockCharacter, EVENT, immutable(DATA));

        verify(mockReactToEvent).apply(arrayOf(mockCharacter, EVENT, immutable(DATA)));
    }

    @Test
    void testReactToEventWithInvalidArgs() {
        var output = factory.apply(DEFINITION);

        assertThrows(IllegalArgumentException.class, () -> output.reactToEvent(null, EVENT, immutable(DATA)));
        assertThrows(IllegalArgumentException.class, () -> output.reactToEvent(mockCharacter, null, immutable(DATA)));
        assertThrows(IllegalArgumentException.class, () -> output.reactToEvent(mockCharacter, "", immutable(DATA)));
        assertThrows(IllegalArgumentException.class, () -> output.reactToEvent(mockCharacter, EVENT, null));
    }
}
