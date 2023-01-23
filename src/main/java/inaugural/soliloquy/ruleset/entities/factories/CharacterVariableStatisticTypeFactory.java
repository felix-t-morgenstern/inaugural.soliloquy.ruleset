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
import soliloquy.specs.ruleset.definitions.EffectsOnCharacterDefinition;
import soliloquy.specs.ruleset.entities.character.CharacterVariableStatisticType;
import soliloquy.specs.ruleset.entities.actonroundendandcharacterturn.EffectsCharacterOnRoundOrTurnChange.EffectsOnCharacter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/** @noinspection rawtypes */
public class CharacterVariableStatisticTypeFactory implements
        Factory<CharacterVariableStatisticTypeDefinition, CharacterVariableStatisticType> {
    private final TypeHandler<ProviderAtTime<ColorShift>> COLOR_SHIFT_PROVIDER_HANDLER;
    private final java.util.function.Function<String, ImageAssetSet> GET_IMAGE_ASSET_SET;
    private final java.util.function.Function<String, Function> GET_FUNCTION;
    private final Factory<EffectsOnCharacterDefinition, EffectsOnCharacter>
            EFFECTS_ON_CHARACTER_FACTORY;

    public CharacterVariableStatisticTypeFactory(
            TypeHandler<ProviderAtTime<ColorShift>> colorShiftProviderHandler,
            java.util.function.Function<String, ImageAssetSet> getImageAssetSet,
            java.util.function.Function<String, Function> getFunction,
            Factory<EffectsOnCharacterDefinition, EffectsOnCharacter> effectsOnCharacterFactory) {
        COLOR_SHIFT_PROVIDER_HANDLER =
                Check.ifNull(colorShiftProviderHandler, "colorShiftProviderHandler");
        GET_IMAGE_ASSET_SET = Check.ifNull(getImageAssetSet, "getImageAssetSet");
        GET_FUNCTION = Check.ifNull(getFunction, "getFunction");
        EFFECTS_ON_CHARACTER_FACTORY =
                Check.ifNull(effectsOnCharacterFactory, "effectsOnCharacterFactory");
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
        Check.ifNull(definition.effectsOnRoundEnd, "definition.effectsOnRoundEnd");
        Check.ifNull(definition.effectsOnTurnStart, "definition.effectsOnTurnStart");
        Check.ifNull(definition.effectsOnTurnEnd, "definition.effectsOnTurnEnd");

        var imageAssetSet = GET_IMAGE_ASSET_SET.apply(definition.imageAssetSetId);
        if (imageAssetSet == null) {
            throw new IllegalArgumentException(
                    "CharacterVariableStatisticTypeFactory.make: definition.imageAssetSetId does " +
                            "not correspond to a valid ImageAssetSet");
        }

        //noinspection unchecked
        Function<Pair<String, Character>, Pair<ImageAsset, Integer>> iconForCharacterFunction =
                GET_FUNCTION.apply(definition.iconForCharacterFunctionId);
        if (iconForCharacterFunction == null) {
            throw new IllegalArgumentException(
                    "CharacterVariableStatisticTypeFactory.make: definition" +
                            ".iconForCharacterFunctionId does not correspond to a valid Function");
        }

        var colorShiftProviders = new ArrayList<ProviderAtTime<ColorShift>>();
        for (var colorShiftProvider : definition.defaultColorShifts) {
            colorShiftProviders.add(COLOR_SHIFT_PROVIDER_HANDLER.read(colorShiftProvider));
        }

        var onRoundEnd = EFFECTS_ON_CHARACTER_FACTORY.make(definition.effectsOnRoundEnd);
        var onTurnStart = EFFECTS_ON_CHARACTER_FACTORY.make(definition.effectsOnTurnStart);
        var onTurnEnd = EFFECTS_ON_CHARACTER_FACTORY.make(definition.effectsOnTurnEnd);

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

            @Override
            public EffectsOnCharacter onRoundEnd() {
                return onRoundEnd;
            }

            @Override
            public EffectsOnCharacter onTurnStart() {
                return onTurnStart;
            }

            @Override
            public EffectsOnCharacter onTurnEnd() {
                return onTurnEnd;
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
