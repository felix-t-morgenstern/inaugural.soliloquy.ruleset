package inaugural.soliloquy.ruleset.entities.factories.abilities;

import inaugural.soliloquy.ruleset.definitions.abilities.PassiveAbilityDefinition;
import inaugural.soliloquy.tools.Check;
import soliloquy.specs.common.persistence.TypeHandler;
import soliloquy.specs.gamestate.entities.Character;
import soliloquy.specs.gamestate.entities.Item;
import soliloquy.specs.ruleset.entities.abilities.PassiveAbility;

import java.util.Map;
import java.util.function.Function;

import static inaugural.soliloquy.ruleset.GetFunctions.getNonNullableFunction;

public class PassiveAbilityFactory implements Function<PassiveAbilityDefinition, PassiveAbility> {
    @SuppressWarnings("rawtypes") private final Function<String, Function> GET_FUNCTION;
    /** @noinspection rawtypes */
    private final TypeHandler<Map> MAP_HANDLER;

    /** @noinspection rawtypes */
    public PassiveAbilityFactory(
            @SuppressWarnings("rawtypes") Function<String, Function> getFunction,
            TypeHandler<Map> dataHandler) {
        GET_FUNCTION = Check.ifNull(getFunction, "getFunction");
        MAP_HANDLER = Check.ifNull(dataHandler, "dataHandler");
    }

    @Override
    public PassiveAbility apply(PassiveAbilityDefinition definition)
            throws IllegalArgumentException {
        Check.ifNull(definition, "definition");

        Check.ifNullOrEmpty(definition.id, "definition.id");

        Check.ifNullOrEmpty(definition.name, "definition.name");

        Function<Character, String> characterSourceDescriptionFunction =
                getNonNullableFunction(GET_FUNCTION,
                        definition.characterSourceDescriptionFunctionId,
                        "definition.characterSourceDescriptionFunctionId");
        Function<Item, String> itemSourceDescriptionFunction =
                getNonNullableFunction(GET_FUNCTION, definition.itemSourceDescriptionFunctionId,
                        "definition.itemSourceDescriptionFunctionId");

        var data = MAP_HANDLER.<Map<String, Object>>read(
                Check.ifNullOrEmpty(definition.data, "definition.data"));

        return new PassiveAbility() {
            private String name = definition.name;

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
        };
    }
}
