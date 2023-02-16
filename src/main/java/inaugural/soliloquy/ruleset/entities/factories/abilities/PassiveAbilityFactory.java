package inaugural.soliloquy.ruleset.entities.factories.abilities;

import inaugural.soliloquy.ruleset.definitions.abilities.PassiveAbilityDefinition;
import inaugural.soliloquy.tools.Check;
import soliloquy.specs.common.factories.Factory;
import soliloquy.specs.common.infrastructure.VariableCache;
import soliloquy.specs.common.persistence.TypeHandler;
import soliloquy.specs.gamestate.entities.Character;
import soliloquy.specs.gamestate.entities.Item;
import soliloquy.specs.ruleset.entities.abilities.PassiveAbility;

import java.util.function.Function;

import static inaugural.soliloquy.ruleset.GetFunctions.getNonNullableFunction;

public class PassiveAbilityFactory implements Factory<PassiveAbilityDefinition, PassiveAbility> {
    @SuppressWarnings("rawtypes") private final Function<String, Function> GET_FUNCTION;
    private final TypeHandler<VariableCache> DATA_HANDLER;

    public PassiveAbilityFactory(
            @SuppressWarnings("rawtypes") Function<String, Function> getFunction,
            TypeHandler<VariableCache> dataHandler) {
        GET_FUNCTION = Check.ifNull(getFunction, "getFunction");
        DATA_HANDLER = Check.ifNull(dataHandler, "dataHandler");
    }

    @Override
    public PassiveAbility make(PassiveAbilityDefinition definition)
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

        var data = DATA_HANDLER.read(Check.ifNullOrEmpty(definition.data, "definition.data"));

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
            public VariableCache data() throws IllegalStateException {
                return data;
            }

            @Override
            public String getInterfaceName() {
                return PassiveAbility.class.getCanonicalName();
            }
        };
    }

    @Override
    public String getInterfaceName() {
        return Factory.class.getCanonicalName() + "<" +
                PassiveAbilityDefinition.class.getCanonicalName() + "," +
                PassiveAbility.class.getCanonicalName();
    }
}
