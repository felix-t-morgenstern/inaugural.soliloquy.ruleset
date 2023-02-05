package inaugural.soliloquy.ruleset.entities.factories;

import inaugural.soliloquy.tools.Check;
import soliloquy.specs.common.factories.Factory;
import soliloquy.specs.graphics.assets.ImageAssetSet;
import inaugural.soliloquy.ruleset.definitions.ElementDefinition;
import soliloquy.specs.ruleset.entities.Element;

import java.util.function.Function;

public class ElementFactory implements Factory<ElementDefinition, Element> {
    private final Function<String, ImageAssetSet> GET_IMAGE_ASSET_SET;

    public ElementFactory(Function<String, ImageAssetSet> getImageAssetSet) {
        GET_IMAGE_ASSET_SET = Check.ifNull(getImageAssetSet, "getImageAssetSet");
    }

    @Override
    public Element make(ElementDefinition definition) throws IllegalArgumentException {
        Check.ifNull(definition, "definition");
        Check.ifNullOrEmpty(definition.id, "definition.id");
        Check.ifNullOrEmpty(definition.name, "definition.name");
        Check.ifNullOrEmpty(definition.description, "definition.description");
        Check.ifNullOrEmpty(definition.imageAssetSetId, "definition.imageAssetSetId");
        ImageAssetSet imageAssetSet = GET_IMAGE_ASSET_SET.apply(definition.imageAssetSetId);
        if (imageAssetSet == null) {
            throw new IllegalArgumentException("ElementFactory.make: definition" +
                    ".imageAssetSetId does not correspond to a valid ImageAssetSet");
        }

        return new Element() {
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

            @Override
            public String getInterfaceName() {
                return Element.class.getCanonicalName();
            }
        };
    }

    @Override
    public String getInterfaceName() {
        return Factory.class.getCanonicalName() + "<" + ElementDefinition.class.getCanonicalName() +
                "," + Element.class.getCanonicalName() + ">";
    }
}
