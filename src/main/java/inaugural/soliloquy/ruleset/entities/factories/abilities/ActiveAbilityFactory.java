package inaugural.soliloquy.ruleset.entities.factories.abilities;

import inaugural.soliloquy.ruleset.definitions.abilities.ActiveAbilityDefinition;
import inaugural.soliloquy.tools.Check;
import soliloquy.specs.common.factories.Factory;
import soliloquy.specs.common.infrastructure.VariableCache;
import soliloquy.specs.common.persistence.TypeHandler;
import soliloquy.specs.gamestate.entities.Character;
import soliloquy.specs.gamestate.entities.Item;
import soliloquy.specs.ruleset.entities.abilities.AbilitySource;
import soliloquy.specs.ruleset.entities.abilities.ActiveAbility;
import soliloquy.specs.ruleset.gameconcepts.CharacterEventFiring;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Function;

public class ActiveAbilityFactory implements Factory<ActiveAbilityDefinition, ActiveAbility> {
    @SuppressWarnings("rawtypes") private final Function<String, Function> GET_FUNCTION;
    @SuppressWarnings("rawtypes") private final Function<String, Consumer> GET_CONSUMER;
    private final TypeHandler<VariableCache> DATA_HANDLER;
    private final CharacterEventFiring CHARACTER_EVENT_FIRING;

    public ActiveAbilityFactory(
            @SuppressWarnings("rawtypes") Function<String, Function> getFunction,
            @SuppressWarnings("rawtypes") Function<String, Consumer> getConsumer,
            TypeHandler<VariableCache> dataHandler,
            CharacterEventFiring characterEventFiring) {
        GET_FUNCTION = Check.ifNull(getFunction, "getFunction");
        GET_CONSUMER = Check.ifNull(getConsumer, "getConsumer");
        DATA_HANDLER = Check.ifNull(dataHandler, "dataHandler");
        CHARACTER_EVENT_FIRING = Check.ifNull(characterEventFiring, "characterEventFiring");
    }

    @Override
    public ActiveAbility make(ActiveAbilityDefinition definition)
            throws IllegalArgumentException {
        Check.ifNull(definition, "definition");

        Check.ifNullOrEmpty(definition.id, "definition.id");

        Check.ifNullOrEmpty(definition.name, "definition.name");

        Check.ifNullOrEmpty(definition.characterSourceDescriptionFunctionId, "definition.characterSourceDescriptionFunctionId");
        //noinspection unchecked
        Function<Character, String> characterSourceDescriptionFunction = GET_FUNCTION.apply(definition.characterSourceDescriptionFunctionId);
        if (characterSourceDescriptionFunction == null) {
            throw new IllegalArgumentException("ActiveAbilityFactory.make: definition.characterSourceDescriptionFunctionId (" + definition.characterSourceDescriptionFunctionId + ") does not correspond to a valid function");
        }

        Check.ifNullOrEmpty(definition.itemSourceDescriptionFunctionId, "definition.itemSourceDescriptionFunctionId");
        //noinspection unchecked
        Function<Item, String> itemSourceDescriptionFunction = GET_FUNCTION.apply(definition.itemSourceDescriptionFunctionId);
        if (itemSourceDescriptionFunction == null) {
            throw new IllegalArgumentException("ActiveAbilityFactory.make: definition.itemSourceDescriptionFunctionId (" + definition.itemSourceDescriptionFunctionId + ") does not correspond to a valid function");
        }

        Check.ifNullOrEmpty(definition.useFunctionId, "definition.useFunctionId");
        var useFunction = GET_CONSUMER.apply(definition.useFunctionId);
        if (useFunction == null) {
            throw new IllegalArgumentException("ActiveAbilityFactory.make: definition.useFunctionId (" + definition.useFunctionId + ") does not correspond to a valid function");
        }

        Check.ifNull(definition.targetTypes, "definition.targetTypes");
        for (var targetType : definition.targetTypes) {
            if (targetType == null) {
                throw new IllegalArgumentException("ActiveAbilityFactory.make: definition.targetTypes cannot contain any null entries");
            }
        }
        var copiedTargetTypes = Arrays.copyOf(definition.targetTypes, definition.targetTypes.length);

        var data = DATA_HANDLER.read(Check.ifNullOrEmpty(definition.data, "definition.data"));

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
                return characterSourceDescriptionFunction.apply(Check.ifNull(character, "character"));
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
                return ActiveAbility.class.getCanonicalName();
            }
        };
    }

    @Override
    public String getInterfaceName() {
        return Factory.class.getCanonicalName() + "<" + ActiveAbilityDefinition.class.getCanonicalName() + "," + ActiveAbility.class.getCanonicalName() + ">";
    }
}
