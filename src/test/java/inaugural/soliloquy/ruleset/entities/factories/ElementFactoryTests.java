package inaugural.soliloquy.ruleset.entities.factories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import soliloquy.specs.common.factories.Factory;
import soliloquy.specs.graphics.assets.ImageAssetSet;
import inaugural.soliloquy.ruleset.definitions.ElementDefinition;
import soliloquy.specs.ruleset.entities.Element;

import java.util.Map;

import static inaugural.soliloquy.tools.collections.Collections.mapOf;
import static inaugural.soliloquy.tools.random.Random.randomString;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

class ElementFactoryTests {
    private final String ID = randomString();
    private final String NAME = randomString();
    private final String IMAGE_ASSET_SET_ID = randomString();
    private final Map<String, ImageAssetSet> IMAGE_ASSET_SETS = mapOf();

    @Mock
    private ImageAssetSet mockImageAssetSet;

    private Factory<ElementDefinition, Element> elementFactory;

    @BeforeEach
    void setUp() {
        mockImageAssetSet = mock(ImageAssetSet.class);
        IMAGE_ASSET_SETS.put(IMAGE_ASSET_SET_ID, mockImageAssetSet);

        elementFactory = new ElementFactory(IMAGE_ASSET_SETS::get);
    }

    @Test
    void testConstructorWithInvalidParams() {
        assertThrows(IllegalArgumentException.class, () -> new ElementFactory(null));
    }

    @Test
    void testMake() {
        var element = elementFactory.make(new ElementDefinition(ID, NAME, IMAGE_ASSET_SET_ID));

        assertNotNull(element);
        assertEquals(ID, element.id());
        assertEquals(NAME, element.getName());
        assertSame(mockImageAssetSet, element.imageAssetSet());
        assertEquals(Element.class.getCanonicalName(), element.getInterfaceName());
    }

    @Test
    void testMakeWithInvalidParams() {
        assertThrows(IllegalArgumentException.class, () -> elementFactory.make(null));
        assertThrows(IllegalArgumentException.class,
                () -> elementFactory.make(new ElementDefinition(null, NAME, IMAGE_ASSET_SET_ID)));
        assertThrows(IllegalArgumentException.class,
                () -> elementFactory.make(new ElementDefinition("", NAME, IMAGE_ASSET_SET_ID)));
        assertThrows(IllegalArgumentException.class,
                () -> elementFactory.make(new ElementDefinition(ID, null, IMAGE_ASSET_SET_ID)));
        assertThrows(IllegalArgumentException.class,
                () -> elementFactory.make(new ElementDefinition(ID, "", IMAGE_ASSET_SET_ID)));
        assertThrows(IllegalArgumentException.class,
                () -> elementFactory.make(new ElementDefinition(ID, NAME, null)));
        assertThrows(IllegalArgumentException.class,
                () -> elementFactory.make(new ElementDefinition(ID, NAME, "")));
        assertThrows(IllegalArgumentException.class,
                () -> elementFactory.make(new ElementDefinition(ID, NAME, "not a valid " +
                        "imageAssetSetId")));
    }

    @Test
    void testSetNameOnCreatedElement() {
        var element = elementFactory.make(new ElementDefinition(ID, NAME, IMAGE_ASSET_SET_ID));
        var newName = randomString();

        element.setName(newName);

        assertEquals(newName, element.getName());
    }

    @Test
    void testSetNameOnCreatedElementWithInvalidParams() {
        var element = elementFactory.make(new ElementDefinition(ID, NAME, IMAGE_ASSET_SET_ID));

        assertThrows(IllegalArgumentException.class, () -> element.setName(null));
        assertThrows(IllegalArgumentException.class, () -> element.setName(""));
    }

    @Test
    void testGetInterfaceName() {
        assertEquals(Factory.class.getCanonicalName() + "<" +
                ElementDefinition.class.getCanonicalName() + "," +
                Element.class.getCanonicalName() + ">", elementFactory.getInterfaceName());
    }
}
