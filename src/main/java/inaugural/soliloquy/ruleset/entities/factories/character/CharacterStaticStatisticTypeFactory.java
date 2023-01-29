package inaugural.soliloquy.ruleset.entities.factories.character;

import inaugural.soliloquy.tools.Check;
import soliloquy.specs.common.factories.Factory;
import soliloquy.specs.common.persistence.TypeHandler;
import soliloquy.specs.graphics.assets.ImageAssetSet;
import soliloquy.specs.graphics.renderables.colorshifting.ColorShift;
import soliloquy.specs.graphics.renderables.providers.ProviderAtTime;
import inaugural.soliloquy.ruleset.definitions.CharacterStaticStatisticTypeDefinition;
import inaugural.soliloquy.ruleset.definitions.EffectsOnCharacterDefinition;
import soliloquy.specs.ruleset.entities.character.CharacterStaticStatisticType;
import soliloquy.specs.ruleset.entities.actonroundendandcharacterturn.EffectsCharacterOnRoundOrTurnChange.EffectsOnCharacter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class CharacterStaticStatisticTypeFactory implements
        Factory<CharacterStaticStatisticTypeDefinition, CharacterStaticStatisticType> {
    private final TypeHandler<ProviderAtTime<ColorShift>> COLOR_SHIFT_PROVIDER_HANDLER;
    private final Function<String, ImageAssetSet> GET_IMAGE_ASSET_SET;
    private final Factory<EffectsOnCharacterDefinition, EffectsOnCharacter>
            EFFECTS_ON_CHARACTER_FACTORY;

    public CharacterStaticStatisticTypeFactory(
            TypeHandler<ProviderAtTime<ColorShift>> colorShiftProviderHandler,
            Function<String, ImageAssetSet> getImageAssetSet,
            Factory<EffectsOnCharacterDefinition, EffectsOnCharacter> effectsOnCharacterFactory) {
        COLOR_SHIFT_PROVIDER_HANDLER =
                Check.ifNull(colorShiftProviderHandler, "colorShiftProviderHandler");
        GET_IMAGE_ASSET_SET = Check.ifNull(getImageAssetSet, "getImageAssetSet");
        EFFECTS_ON_CHARACTER_FACTORY =
                Check.ifNull(effectsOnCharacterFactory, "effectsOnCharacterFactory");
    }

    @Override
    public CharacterStaticStatisticType make(CharacterStaticStatisticTypeDefinition definition)
            throws IllegalArgumentException {
        Check.ifNull(definition, "definition");
        Check.ifNullOrEmpty(definition.id, "definition.id");
        Check.ifNullOrEmpty(definition.name, "definition.name");
        Check.ifNullOrEmpty(definition.imageAssetSetId, "definition.imageAssetSetId");
        Check.ifNull(definition.effectsOnRoundEnd, "definition.effectsOnRoundEnd");
        Check.ifNull(definition.effectsOnTurnStart, "definition.effectsOnTurnStart");
        Check.ifNull(definition.effectsOnTurnEnd, "definition.effectsOnTurnEnd");

        ImageAssetSet imageAssetSet = GET_IMAGE_ASSET_SET.apply(definition.imageAssetSetId);
        if (imageAssetSet == null) {
            throw new IllegalArgumentException(
                    "CharacterStaticStatisticTypeFactory.make: definition.imageAssetSetId does " +
                            "not correspond to a valid ImageAssetSet");
        }

        ArrayList<ProviderAtTime<ColorShift>> colorShiftProviders = new ArrayList<>();
        for (String colorShiftProvider : definition.defaultColorShifts) {
            colorShiftProviders.add(COLOR_SHIFT_PROVIDER_HANDLER.read(colorShiftProvider));
        }

        EffectsOnCharacter onRoundEnd =
                EFFECTS_ON_CHARACTER_FACTORY.make(definition.effectsOnRoundEnd);
        EffectsOnCharacter onTurnStart =
                EFFECTS_ON_CHARACTER_FACTORY.make(definition.effectsOnTurnStart);
        EffectsOnCharacter onTurnEnd =
                EFFECTS_ON_CHARACTER_FACTORY.make(definition.effectsOnTurnEnd);

        return new CharacterStaticStatisticType() {
            private String name = definition.name;
            private String description = definition.description;

            @Override
            public String getDescription() {
                return description;
            }

            @Override
            public void setDescription(String description) {
                this.description = description;
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

            @Override
            public String getInterfaceName() {
                return CharacterStaticStatisticType.class.getCanonicalName();
            }
        };
    }

    @Override
    public String getInterfaceName() {
        return Factory.class.getCanonicalName() + "<" +
                CharacterStaticStatisticTypeDefinition.class.getCanonicalName() + "," +
                CharacterStaticStatisticType.class.getCanonicalName() + ">";
    }
}
