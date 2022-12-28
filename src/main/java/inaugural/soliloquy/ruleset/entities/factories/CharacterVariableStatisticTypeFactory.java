package inaugural.soliloquy.ruleset.entities.factories;

import inaugural.soliloquy.tools.Check;
import soliloquy.specs.common.factories.Factory;
import soliloquy.specs.common.persistence.TypeHandler;
import soliloquy.specs.common.valueobjects.Pair;
import soliloquy.specs.gamestate.entities.Character;
import soliloquy.specs.graphics.assets.ImageAsset;
import soliloquy.specs.graphics.assets.ImageAssetSet;
import soliloquy.specs.graphics.renderables.colorshifting.ColorShift;
import soliloquy.specs.graphics.renderables.providers.ProviderAtTime;
import soliloquy.specs.ruleset.definitions.CharacterVariableStatisticTypeDefinition;
import soliloquy.specs.ruleset.entities.CharacterVariableStatisticType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/** @noinspection rawtypes */
public class CharacterVariableStatisticTypeFactory implements
        Factory<CharacterVariableStatisticTypeDefinition, CharacterVariableStatisticType> {
    private final TypeHandler<ProviderAtTime<ColorShift>> COLOR_SHIFT_PROVIDER_HANDLER;
    private final java.util.function.Function<String, ImageAssetSet> GET_IMAGE_ASSET_SET;
    private final java.util.function.Function<String, Function> GET_ICON_FOR_CHARACTER_FUNCTION;

    public CharacterVariableStatisticTypeFactory(
            TypeHandler<ProviderAtTime<ColorShift>> colorShiftProviderHandler,
            Function<String, ImageAssetSet> getImageAssetSet,
            Function<String, Function> getIconForCharacterFunction) {
        COLOR_SHIFT_PROVIDER_HANDLER =
                Check.ifNull(colorShiftProviderHandler, "colorShiftProviderHandler");
        GET_IMAGE_ASSET_SET = Check.ifNull(getImageAssetSet, "getImageAssetSet");
        GET_ICON_FOR_CHARACTER_FUNCTION =
                Check.ifNull(getIconForCharacterFunction, "getIconForCharacterFunction");
    }

    @Override
    public CharacterVariableStatisticType make(CharacterVariableStatisticTypeDefinition definition)
            throws IllegalArgumentException {
        Check.ifNull(definition, "definition");
        Check.ifNullOrEmpty(definition.id, "definition.id");
        Check.ifNullOrEmpty(definition.name, "definition.name");
        Check.ifNullOrEmpty(definition.pluralName, "definition.pluralName");
        Check.ifNullOrEmpty(definition.imageAssetSetId, "definition.imageAssetSetId");
        Check.ifNullOrEmpty(definition.iconForCharacterFunctionId,
                "definition.iconForCharacterFunctionId");

        ImageAssetSet imageAssetSet = GET_IMAGE_ASSET_SET.apply(definition.imageAssetSetId);
        if (imageAssetSet == null) {
            throw new IllegalArgumentException(
                    "CharacterVariableStatisticTypeFactory.make: definition.imageAssetSetId does " +
                            "not correspond to a valid ImageAssetSet");
        }

        //noinspection unchecked
        Function<Pair<String, Character>, Pair<ImageAsset, Integer>> iconForCharacterFunction =
                GET_ICON_FOR_CHARACTER_FUNCTION.apply(definition.iconForCharacterFunctionId);
        if (iconForCharacterFunction == null) {
            throw new IllegalArgumentException(
                    "CharacterVariableStatisticTypeFactory.make: definition" +
                            ".iconForCharacterFunctionId does not correspond to a valid Function");
        }

        ArrayList<ProviderAtTime<ColorShift>> colorShiftProviders = new ArrayList<>();
        for (String colorShiftProvider : definition.defaultColorShifts) {
            colorShiftProviders.add(COLOR_SHIFT_PROVIDER_HANDLER.read(colorShiftProvider));
        }

        return new CharacterVariableStatisticType() {
            private String name = definition.name;
            private String pluralName = definition.pluralName;
            private String description = definition.description;

            @Override
            public String getPluralName() {
                return pluralName;
            }

            @Override
            public void setPluralName(String pluralName) throws IllegalArgumentException {
                this.pluralName = Check.ifNullOrEmpty(pluralName, "pluralName");
            }

            @Override
            public String getDescription() {
                return description;
            }

            @Override
            public void setDescription(String description) {
                this.description = Check.ifNullOrEmpty(description, "description");
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
            public List<ProviderAtTime<ColorShift>> colorShiftProviders() {
                return colorShiftProviders;
            }

            @Override
            public ImageAssetSet imageAssetSet() {
                return imageAssetSet;
            }

            @Override
            public String getInterfaceName() {
                return CharacterVariableStatisticType.class.getCanonicalName();
            }

            @Override
            public Pair<ImageAsset, Integer> getIcon(String iconType, Character character)
                    throws IllegalArgumentException {
                return iconForCharacterFunction.apply(Pair.of(iconType, character));
            }
        };
    }

    @Override
    public String getInterfaceName() {
        return Factory.class.getCanonicalName() + "<" +
                CharacterVariableStatisticTypeDefinition.class.getCanonicalName() + "," +
                CharacterVariableStatisticType.class.getCanonicalName() + ">";
    }
}
