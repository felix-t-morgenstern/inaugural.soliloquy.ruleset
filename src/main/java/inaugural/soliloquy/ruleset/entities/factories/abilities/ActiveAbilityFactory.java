package inaugural.soliloquy.ruleset.entities.factories.abilities;

import inaugural.soliloquy.ruleset.definitions.abilities.ActiveAbilityDefinition;
import inaugural.soliloquy.tools.Check;
import soliloquy.specs.common.persistence.TypeHandler;
import soliloquy.specs.gamestate.entities.Character;
import soliloquy.specs.gamestate.entities.Item;
import soliloquy.specs.gamestate.entities.abilities.AbilitySource;
import soliloquy.specs.ruleset.entities.abilities.ActiveAbility;
import soliloquy.specs.ruleset.gameconcepts.CharacterEventFiring;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import static inaugural.soliloquy.ruleset.GetFunctions.getNonNullableFunction;

public class ActiveAbilityFactory implements Function<ActiveAbilityDefinition, ActiveAbility> {
    @SuppressWarnings("rawtypes") private final Function<String, Function> GET_FUNCTION;
    @SuppressWarnings("rawtypes") private final Function<String, Consumer> GET_CONSUMER;
    /** @noinspection rawtypes */
    private final TypeHandler<Map> MAP_HANDLER;
    private final CharacterEventFiring CHARACTER_EVENT_FIRING;

    /** @noinspection rawtypes */
    public ActiveAbilityFactory(
            @SuppressWarnings("rawtypes") Function<String, Function> getFunction,
            @SuppressWarnings("rawtypes") Function<String, Consumer> getConsumer,
            TypeHandler<Map> dataHandler,
            CharacterEventFiring characterEventFiring) {
        GET_FUNCTION = Check.ifNull(getFunction, "getFunction");
        GET_CONSUMER = Check.ifNull(getConsumer, "getConsumer");
        MAP_HANDLER = Check.ifNull(dataHandler, "dataHandler");
        CHARACTER_EVENT_FIRING = Check.ifNull(characterEventFiring, "characterEventFiring");
    }

    @Override
    public ActiveAbility apply(ActiveAbilityDefinition definition)
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

        Check.ifNullOrEmpty(definition.useFunctionId, "definition.useFunctionId");
        var useFunction = GET_CONSUMER.apply(definition.useFunctionId);
        if (useFunction == null) {
            throw new IllegalArgumentException(
                    "ActiveAbilityfactory.apply: definition.useFunctionId (" +
                            definition.useFunctionId + ") does not correspond to a valid function");
        }

        Check.ifNull(definition.targetTypes, "definition.targetTypes");
        for (var targetType : definition.targetTypes) {
            if (targetType == null) {
                throw new IllegalArgumentException(
                        "ActiveAbilityfactory.apply: definition.targetTypes cannot contain any " +
                                "null entries");
            }
        }
        var copiedTargetTypes =
                Arrays.copyOf(definition.targetTypes, definition.targetTypes.length);

        var data = MAP_HANDLER.<Map<String, Object>>read(
                Check.ifNullOrEmpty(definition.data, "definition.data"));

        return new ActiveAbility() {
            private String name = definition.name;

            @Override
            public TargetType[] targetTypes() {
                return copiedTargetTypes;
            }

            @Override
            public void use(AbilitySource abilitySource, Object... targets)
                    throws IllegalArgumentException {
                Check.ifNull(abilitySource, "abilitySource");
                Check.ifNull(targets, "targets");
                Check.ifAnyNull(targets, "targets");

                var params = new Object[targets.length + 2];
                params[0] = CHARACTER_EVENT_FIRING;
                params[1] = abilitySource;
                System.arraycopy(targets, 0, params, 2, targets.length);

                //noinspection unchecked
                useFunction.accept(params);
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
