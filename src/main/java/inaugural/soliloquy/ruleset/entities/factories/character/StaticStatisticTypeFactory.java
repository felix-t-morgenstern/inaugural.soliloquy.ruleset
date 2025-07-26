package inaugural.soliloquy.ruleset.entities.factories.character;

import inaugural.soliloquy.ruleset.definitions.StaticStatisticTypeDefinition;
import inaugural.soliloquy.ruleset.definitions.EffectsOnCharacterDefinition;
import inaugural.soliloquy.ruleset.definitions.RoundEndEffectsOnCharacterDefinition;
import inaugural.soliloquy.tools.Check;
import soliloquy.specs.common.persistence.TypeHandler;
import soliloquy.specs.io.graphics.assets.ImageAssetSet;
import soliloquy.specs.io.graphics.renderables.colorshifting.ColorShift;
import soliloquy.specs.io.graphics.renderables.providers.ProviderAtTime;
import soliloquy.specs.ruleset.entities.actonroundendandcharacterturn.EffectsCharacterOnRoundOrTurnChange.EffectsOnCharacter;
import soliloquy.specs.ruleset.entities.actonroundendandcharacterturn.EffectsCharacterOnRoundOrTurnChange.RoundEndEffectsOnCharacter;
import soliloquy.specs.ruleset.entities.character.StaticStatisticType;

import java.util.List;
import java.util.function.Function;

import static inaugural.soliloquy.tools.collections.Collections.listOf;

public class StaticStatisticTypeFactory implements
        Function<StaticStatisticTypeDefinition, StaticStatisticType> {
    private final TypeHandler<ColorShift> SHIFT_HANDLER;
    private final Function<String, ImageAssetSet> GET_IMAGE_ASSET_SET;
    private final Function<EffectsOnCharacterDefinition, EffectsOnCharacter>
            EFFECTS_ON_CHARACTER_FACTORY;
    private final Function<RoundEndEffectsOnCharacterDefinition, RoundEndEffectsOnCharacter>
            ROUND_END_EFFECTS_ON_CHARACTER_FACTORY;

    public StaticStatisticTypeFactory(
            TypeHandler<ColorShift> shiftHandler,
            Function<String, ImageAssetSet> getImageAssetSet,
            Function<EffectsOnCharacterDefinition, EffectsOnCharacter> effectsOnCharacterFactory,
            Function<RoundEndEffectsOnCharacterDefinition, RoundEndEffectsOnCharacter> roundEndEffectsOnCharacterFactory) {
        SHIFT_HANDLER = Check.ifNull(shiftHandler, "shiftHandler");
        GET_IMAGE_ASSET_SET = Check.ifNull(getImageAssetSet, "getImageAssetSet");
        EFFECTS_ON_CHARACTER_FACTORY =
                Check.ifNull(effectsOnCharacterFactory, "effectsOnCharacterFactory");
        ROUND_END_EFFECTS_ON_CHARACTER_FACTORY = Check.ifNull(roundEndEffectsOnCharacterFactory,
                "roundEndEffectsOnCharacterFactory");
    }

    @Override
    public StaticStatisticType apply(StaticStatisticTypeDefinition definition)
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
                    "StaticStatisticTypeFactory.apply: definition.imageAssetSetId does " +
                            "not correspond to a valid ImageAssetSet");
        }

        List<ColorShift> colorShifts = listOf();
        for (var colorShift : definition.defaultColorShifts) {
            colorShifts.add(SHIFT_HANDLER.read(colorShift));
        }

        RoundEndEffectsOnCharacter onRoundEnd =
                ROUND_END_EFFECTS_ON_CHARACTER_FACTORY.apply(definition.effectsOnRoundEnd);
        EffectsOnCharacter onTurnStart =
                EFFECTS_ON_CHARACTER_FACTORY.apply(definition.effectsOnTurnStart);
        EffectsOnCharacter onTurnEnd =
                EFFECTS_ON_CHARACTER_FACTORY.apply(definition.effectsOnTurnEnd);

        return new StaticStatisticType() {
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
            public List<ColorShift> colorShifts() {
                return colorShifts;
            }

            @Override
            public ImageAssetSet imageAssetSet() {
                return imageAssetSet;
            }

            @Override
            public RoundEndEffectsOnCharacter onRoundEnd() {
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
}
