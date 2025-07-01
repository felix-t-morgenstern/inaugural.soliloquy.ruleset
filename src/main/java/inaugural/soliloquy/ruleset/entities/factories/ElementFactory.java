package inaugural.soliloquy.ruleset.entities.factories;

import inaugural.soliloquy.tools.Check;
import soliloquy.specs.io.graphics.assets.ImageAssetSet;
import inaugural.soliloquy.ruleset.definitions.ElementDefinition;
import soliloquy.specs.ruleset.entities.Element;
import soliloquy.specs.ruleset.entities.character.StaticStatisticType;

import java.util.function.Function;

public class ElementFactory implements Function<ElementDefinition, Element> {
    private final Function<String, ImageAssetSet> GET_IMAGE_ASSET_SET;
    private final Function<String, StaticStatisticType> GET_STATIC_STAT_TYPE;

    public ElementFactory(Function<String, ImageAssetSet> getImageAssetSet,
                          Function<String, StaticStatisticType> getStaticStatType) {
        GET_IMAGE_ASSET_SET = Check.ifNull(getImageAssetSet, "getImageAssetSet");
        GET_STATIC_STAT_TYPE = Check.ifNull(getStaticStatType, "getStaticStatType");
    }

    @Override
    public Element apply(ElementDefinition definition) throws IllegalArgumentException {
        Check.ifNull(definition, "definition");
        Check.ifNullOrEmpty(definition.id, "definition.id");
        Check.ifNullOrEmpty(definition.name, "definition.name");
        Check.ifNullOrEmpty(definition.description, "definition.description");
        Check.ifNullOrEmpty(definition.imageAssetSetId, "definition.imageAssetSetId");
        var imageAssetSet = GET_IMAGE_ASSET_SET.apply(definition.imageAssetSetId);
        if (imageAssetSet == null) {
            throw new IllegalArgumentException("Elementfactory.apply: definition.imageAssetSetId (" +
                    definition.imageAssetSetId + ") does not correspond to a valid ImageAssetSet");
        }
        Check.ifNullOrEmpty(definition.resistanceStatisticTypeId,
                "definition.resistanceStatisticTypeId");
        var staticStatType = GET_STATIC_STAT_TYPE.apply(definition.resistanceStatisticTypeId);
        if (staticStatType == null) {
            throw new IllegalArgumentException(
                    "Elementfactory.apply: definition.resistanceStatisticTypeId (" +
                            definition.resistanceStatisticTypeId +
                            ") does not correspond to a valid StaticStatisticType");
        }

        return new Element() {
            @Override
            public StaticStatisticType resistanceStatisticType() {
                return staticStatType;
            }

            private String name = definition.name;
            private String description = definition.description;

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
            public String getDescription() {
                return description;
            }

            @Override
            public void setDescription(String description) throws IllegalArgumentException {
                this.description = Check.ifNullOrEmpty(description, "description");
            }

            @Override
            public ImageAssetSet imageAssetSet() {
                return imageAssetSet;
            }
        };
    }
}
