package inaugural.soliloquy.ruleset.gameconcepts;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import soliloquy.specs.common.infrastructure.ImmutableMap;
import soliloquy.specs.gamestate.entities.Character;
import soliloquy.specs.gamestate.entities.CharacterEvents;
import soliloquy.specs.gamestate.entities.CharacterEvents.CharacterEvent;
import soliloquy.specs.gamestate.entities.abilities.AbilitySource;
import soliloquy.specs.ruleset.entities.abilities.Ability;
import soliloquy.specs.ruleset.gameconcepts.CharacterEventFiring;

import static inaugural.soliloquy.tools.collections.Collections.arrayOf;
import static inaugural.soliloquy.tools.random.Random.randomString;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

@SuppressWarnings("ALL")
@ExtendWith(MockitoExtension.class)
public class CharacterEventFiringImplTests {
    private final static String EVENT = randomString();
    private final static String ABILITY_ID = randomString();

    @Mock private CharacterEvent mockCharacterEvent1;
    @Mock private CharacterEvent mockCharacterEvent2;
    @Mock private CharacterEvents mockCharacterEvents;
    @Mock private Character mockCharacter;
    @Mock private ImmutableMap<String, Object> mockData;
    @Mock private Ability mockAbility;
    private AbilitySource abilitySource;

    private CharacterEventFiring characterEventFiring;

    @BeforeEach
    public void setUp() {
        lenient().when(mockCharacterEvents.eventsForTrigger(anyString())).thenReturn(
                arrayOf(mockCharacterEvent1, mockCharacterEvent2));

        lenient().when(mockCharacter.events()).thenReturn(mockCharacterEvents);

        lenient().when(mockAbility.id()).thenReturn(ABILITY_ID);

        abilitySource = AbilitySource.of(mockCharacter, mockAbility, mockData);

        characterEventFiring = new CharacterEventFiringImpl();
    }

    @Test
    public void testFireEvent() {
        characterEventFiring.fireEvent(mockCharacter, EVENT, mockData);

        verify(mockCharacter).events();
        verify(mockCharacterEvents).eventsForTrigger(EVENT);
        verify(mockCharacterEvent1).reactToEvent(mockCharacter, EVENT, mockData);
        verify(mockCharacterEvent2).reactToEvent(mockCharacter, EVENT, mockData);
    }

    @Test
    public void testFireEventWithInvalidArgs() {
        assertThrows(IllegalArgumentException.class, () -> characterEventFiring.fireEvent(null, EVENT, mockData));
        assertThrows(IllegalArgumentException.class, () -> characterEventFiring.fireEvent(mockCharacter, null, mockData));
        assertThrows(IllegalArgumentException.class, () -> characterEventFiring.fireEvent(mockCharacter, "", mockData));
        assertThrows(IllegalArgumentException.class, () -> characterEventFiring.fireEvent(mockCharacter, EVENT, null));
    }

    @Test
    public void testFireAbility() {
        characterEventFiring.fireAbility(mockCharacter, abilitySource);

        verify(mockCharacter).events();
        verify(mockAbility).id();
        verify(mockCharacterEvents).eventsForTrigger(ABILITY_ID);
        verify(mockCharacterEvent1).reactToAbility(mockCharacter, abilitySource);
        verify(mockCharacterEvent2).reactToAbility(mockCharacter, abilitySource);
    }

    @Test
    public void testFireAbilityWithInvalidArgs() {
        assertThrows(IllegalArgumentException.class, () -> characterEventFiring.fireAbility(null, abilitySource));
        assertThrows(IllegalArgumentException.class, () -> characterEventFiring.fireAbility(mockCharacter, null));
    }
}
