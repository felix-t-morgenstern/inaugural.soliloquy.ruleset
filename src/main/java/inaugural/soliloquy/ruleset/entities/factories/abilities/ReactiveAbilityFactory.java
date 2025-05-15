package inaugural.soliloquy.ruleset.entities.factories.abilities;

import inaugural.soliloquy.ruleset.definitions.abilities.ReactiveAbilityDefinition;
import inaugural.soliloquy.tools.Check;
import soliloquy.specs.common.infrastructure.ImmutableMap;
import soliloquy.specs.common.persistence.TypeHandler;
import soliloquy.specs.gamestate.entities.Character;
import soliloquy.specs.gamestate.entities.Item;
import soliloquy.specs.gamestate.entities.abilities.AbilitySource;
import soliloquy.specs.gamestate.entities.exceptions.EntityDeletedException;
import soliloquy.specs.ruleset.entities.abilities.ReactiveAbility;
import soliloquy.specs.ruleset.gameconcepts.CharacterEventFiring.FiringResponse;

import java.util.Map;
import java.util.function.Function;

import static inaugural.soliloquy.ruleset.GetFunctions.getNonNullableFunction;
import static inaugural.soliloquy.tools.collections.Collections.arrayOf;

public class ReactiveAbilityFactory implements Function<ReactiveAbilityDefinition, ReactiveAbility> {
    @SuppressWarnings("rawtypes") private final Function<String, Function> GET_FUNCTION;
    /** @noinspection rawtypes*/
    private final TypeHandler<Map> MAP_HANDLER;

    /** @noinspection rawtypes*/
    public ReactiveAbilityFactory(
            @SuppressWarnings("rawtypes") Function<String, Function> getFunction,
            TypeHandler<Map> dataHandler) {
        GET_FUNCTION = Check.ifNull(getFunction, "getFunction");
        MAP_HANDLER = Check.ifNull(dataHandler, "dataHandler");
    }

    @Override
    public ReactiveAbility apply(ReactiveAbilityDefinition definition)
            throws IllegalArgumentException {
        Check.ifNull(definition, "definition");
        Check.ifNullOrEmpty(definition.id, "definition.id");
        Check.ifNullOrEmpty(definition.name, "definition.name");
        Check.ifNullOrEmpty(definition.data, "definition.data");

        Function<Character, String> characterSourceDescriptionFunction =
                getNonNullableFunction(GET_FUNCTION,
                        definition.characterSourceDescriptionFunctionId,
                        "definition.characterSourceDescriptionFunctionId");
        Function<Item, String> itemSourceDescriptionFunction =
                getNonNullableFunction(GET_FUNCTION, definition.itemSourceDescriptionFunctionId,
                        "definition.itemSourceDescriptionFunctionId");
        Function<Object[], Boolean> firesAgainstEventFunction =
                getNonNullableFunction(GET_FUNCTION, definition.firesAgainstEventFunctionId,
                        "definition.firesAgainstEventFunctionId");
        Function<AbilitySource, Boolean> firesAgainstAbilityFunction =
                getNonNullableFunction(GET_FUNCTION, definition.firesAgainstAbilityFunctionId,
                        "definition.firesAgainstAbilityFunctionId");
        Function<Object[], FiringResponse> reactToEventFunction =
                getNonNullableFunction(GET_FUNCTION, definition.reactToEventFunctionId,
                        "definition.reactToEventFunctionId");
        Function<Object[], FiringResponse> reactToAbilityFunction =
                getNonNullableFunction(GET_FUNCTION, definition.reactToAbilityFunctionId,
                        "definition.reactToAbilityFunctionId");
        var data = MAP_HANDLER.<Map<String, Object>>read(definition.data);

        return new ReactiveAbility() {
            private String name = definition.name;

            @Override
            public String id() throws IllegalStateException {
                return definition.id;
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public void setName(String name) {
                this.name = Check.ifNullOrEmpty(name, "name");
            }

            @Override
            public Map<String, Object> data() throws IllegalStateException {
                return data;
            }

            @Override
            public String description(Character character) throws IllegalArgumentException {
                return characterSourceDescriptionFunction.apply(
                        Check.ifNull(character, "character"));
            }

            @Override
            public String description(Item item) throws IllegalArgumentException {
                return itemSourceDescriptionFunction.apply(Check.ifNull(item, "item"));
            }

            @Override
            public boolean firesAgainstEvent(String event, ImmutableMap<String, Object> params)
                    throws IllegalArgumentException {
                return firesAgainstEventFunction.apply(arrayOf(
                        Check.ifNullOrEmpty(event, "event"),
                        Check.ifNull(params, "params")));
            }

            @Override
            public boolean firesAgainstAbility(AbilitySource abilitySource)
                    throws IllegalArgumentException {
                return firesAgainstAbilityFunction.apply(
                        Check.ifNull(abilitySource, "abilitySource"));
            }

            @Override
            public FiringResponse reactToAbility(Character target, AbilitySource abilitySource)
                    throws IllegalArgumentException, EntityDeletedException {
                return reactToAbilityFunction.apply(arrayOf(
                        Check.ifNull(target, "target"),
                        Check.ifNull(abilitySource, "abilitySource")));
            }

            @Override
            public FiringResponse reactToEvent(Character target, String event,
                                               ImmutableMap<String, Object> params)
                    throws IllegalArgumentException, EntityDeletedException {
                return reactToEventFunction.apply(arrayOf(
                        Check.ifNull(target, "target"),
                        Check.ifNullOrEmpty(event, "event"),
                        Check.ifNull(params, "params")));
            }
        };
    }
}
