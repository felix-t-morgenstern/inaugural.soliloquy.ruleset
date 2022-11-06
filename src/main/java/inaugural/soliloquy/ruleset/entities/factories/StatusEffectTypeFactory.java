package inaugural.soliloquy.ruleset.entities.factories;

import inaugural.soliloquy.tools.Check;
import soliloquy.specs.common.factories.Factory;
import soliloquy.specs.common.valueobjects.Pair;
import soliloquy.specs.gamestate.entities.Character;
import soliloquy.specs.graphics.assets.ImageAsset;
import soliloquy.specs.ruleset.definitions.StatusEffectTypeDefinition;
import soliloquy.specs.ruleset.entities.StatusEffectType;

import java.util.HashMap;
import java.util.function.Function;

public class StatusEffectTypeFactory implements
        Factory<StatusEffectTypeDefinition, StatusEffectType> {
    @SuppressWarnings("rawtypes") private final Function<String, Function> GET_FUNCTION;

    @SuppressWarnings("rawtypes")
    public StatusEffectTypeFactory(Function<String, Function> getFunction) {
        GET_FUNCTION = Check.ifNull(getFunction, "getFunction");
    }

    @Override
    public StatusEffectType make(StatusEffectTypeDefinition definition)
            throws IllegalArgumentException {
        Check.ifNull(definition, "definition");
        Check.ifNullOrEmpty(definition.id, "definition.id");
        Check.ifNullOrEmpty(definition.name, "definition.name");
        Check.ifNullOrEmpty(definition.nameAtValueFunctionId, "definition.nameAtValueFunctionId");
        //noinspection unchecked
        Function<Integer, String> nameAtValueFunction =
                (Function<Integer, String>) GET_FUNCTION.apply(definition.nameAtValueFunctionId);
        if (nameAtValueFunction == null) {
            throw new IllegalArgumentException(
                    "StatusEffectTypeFactory.make: definition.nameAtValueFunctionId does not " +
                            "correspond to valid function Id");
        }
        Check.ifNull(definition.iconForCharacterFunctions, "definition.iconForCharacterFunctions");
        HashMap<String, Function<Character, Pair<ImageAsset, Integer>>> iconTypeFunctions =
                new HashMap<>();

        for (StatusEffectTypeDefinition.IconForCharacterFunction iconForCharacterFunction :
                definition.iconForCharacterFunctions) {
            Check.ifNull(iconForCharacterFunction,
                    "iconForCharacterFunction within definition.iconForCharacterFunctions");
            Check.ifNullOrEmpty(iconForCharacterFunction.iconType,
                    "iconType within iconForCharacterFunction within definition");
            Check.ifNullOrEmpty(iconForCharacterFunction.functionId,
                    "functionId within iconForCharacterFunction within definition");

            //noinspection unchecked
            Function<Character, Pair<ImageAsset, Integer>> iconTypeFunction =
                    (Function<Character, Pair<ImageAsset, Integer>>) GET_FUNCTION.apply(
                            iconForCharacterFunction.functionId);

            if (iconTypeFunction == null) {
                throw new IllegalArgumentException(
                        "StatusEffectTypeFactory.make: functionId within iconForCharacterFunction" +
                                " with iconType = " +
                                iconForCharacterFunction.iconType + " (functionId = " +
                                iconForCharacterFunction.functionId +
                                ") does not correspond to valid function Id");
            }

            iconTypeFunctions.put(iconForCharacterFunction.iconType, iconTypeFunction);
        }

        return new StatusEffectType() {
            private final String ID = definition.id;
            private final boolean STOPS_AT_ZERO = definition.stopsAtZero;
            private final Function<Integer, String> NAME_AT_VALUE_FUNCTION = nameAtValueFunction;
            private final HashMap<String, Function<Character, Pair<ImageAsset, Integer>>>
                    ICON_TYPE_FUNCTIONS = iconTypeFunctions;

            private String name = definition.name;

            @Override
            public boolean stopsAtZero() {
                return STOPS_AT_ZERO;
            }

            @Override
            public String nameAtValue(int value) throws UnsupportedOperationException {
                return NAME_AT_VALUE_FUNCTION.apply(value);
            }

            @Override
            public String id() throws IllegalStateException {
                return ID;
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
            public String getInterfaceName() {
                return StatusEffectType.class.getCanonicalName();
            }

            @Override
            public Pair<ImageAsset, Integer> getIcon(String iconType, Character character)
                    throws IllegalArgumentException {
                Check.ifNullOrEmpty(iconType, "iconType");
                Check.ifNull(character, "character");
                if (!ICON_TYPE_FUNCTIONS.containsKey(iconType)) {
                    throw new IllegalArgumentException(
                            "StatusEffectType.getIcon: iconType (" + iconType +
                                    ") is not a valid icon type");
                }

                return ICON_TYPE_FUNCTIONS.get(iconType).apply(character);
            }
        };
    }

    @Override
    public String getInterfaceName() {
        return Factory.class.getCanonicalName() + "<" +
                StatusEffectTypeDefinition.class.getCanonicalName() + "," +
                StatusEffectType.class.getCanonicalName() + ">";
    }
}
