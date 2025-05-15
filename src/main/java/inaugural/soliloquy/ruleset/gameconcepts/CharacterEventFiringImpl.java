package inaugural.soliloquy.ruleset.gameconcepts;

import inaugural.soliloquy.tools.Check;
import soliloquy.specs.common.infrastructure.ImmutableMap;
import soliloquy.specs.gamestate.entities.Character;
import soliloquy.specs.gamestate.entities.abilities.AbilitySource;
import soliloquy.specs.gamestate.entities.exceptions.EntityDeletedException;
import soliloquy.specs.ruleset.gameconcepts.CharacterEventFiring;

import java.util.Map;

public class CharacterEventFiringImpl implements CharacterEventFiring {
    public CharacterEventFiringImpl() {
    }

    @Override
    public void fireEvent(Character target, String event, ImmutableMap<String, Object> data)
            throws IllegalArgumentException, EntityDeletedException {
        Check.ifNull(target, "target");
        Check.ifNullOrEmpty(event, "event");
        Check.ifNull(data, "data");
        for (var characterEvent : target.events().eventsForTrigger(event)) {
            characterEvent.reactToEvent(target, event, data);
        }
    }

    @Override
    public void fireAbility(Character target, AbilitySource source)
            throws IllegalArgumentException, EntityDeletedException {
        Check.ifNull(target, "target");
        Check.ifNull(source, "source");
        var abilityId = source.Ability.id();
        for (var characterEvent : target.events().eventsForTrigger(abilityId)) {
            characterEvent.reactToAbility(target, source);
        }
    }
}
