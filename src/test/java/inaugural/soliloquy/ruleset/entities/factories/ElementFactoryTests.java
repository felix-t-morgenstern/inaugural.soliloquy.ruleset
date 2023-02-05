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
    private final String DESCRIPTION = randomString();
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
        var element = elementFactory.make(new ElementDefinition(ID, NAME, DESCRIPTION, IMAGE_ASSET_SET_ID));

        assertNotNull(element);
        assertEquals(ID, element.id());
        assertEquals(NAME, element.getName());
        assertEquals(DESCRIPTION, element.getDescription());
        assertSame(mockImageAssetSet, element.imageAssetSet());
        assertEquals(Element.class.getCanonicalName(), element.getInterfaceName());
    }

    @Test
    void testMakeWithInvalidParams() {
        assertThrows(IllegalArgumentException.class, () -> elementFactory.make(null));
        assertThrows(IllegalArgumentException.class,
                () -> elementFactory.make(new ElementDefinition(null, NAME, DESCRIPTION, IMAGE_ASSET_SET_ID)));
        assertThrows(IllegalArgumentException.class,
                () -> elementFactory.make(new ElementDefinition("", NAME, DESCRIPTION, IMAGE_ASSET_SET_ID)));
        assertThrows(IllegalArgumentException.class,
                () -> elementFactory.make(new ElementDefinition(ID, null, DESCRIPTION, IMAGE_ASSET_SET_ID)));
        assertThrows(IllegalArgumentException.class,
                () -> elementFactory.make(new ElementDefinition(ID, "", DESCRIPTION, IMAGE_ASSET_SET_ID)));
        assertThrows(IllegalArgumentException.class,
                () -> elementFactory.make(new ElementDefinition(ID, NAME, null, IMAGE_ASSET_SET_ID)));
        assertThrows(IllegalArgumentException.class,
                () -> elementFactory.make(new ElementDefinition(ID, NAME, "", IMAGE_ASSET_SET_ID)));
        assertThrows(IllegalArgumentException.class,
                () -> elementFactory.make(new ElementDefinition(ID, NAME, DESCRIPTION, null)));
        assertThrows(IllegalArgumentException.class,
                () -> elementFactory.make(new ElementDefinition(ID, NAME, DESCRIPTION, "")));
        assertThrows(IllegalArgumentException.class,
                () -> elementFactory.make(new ElementDefinition(ID, NAME, DESCRIPTION,
                        "not a valid imageAssetSetId")));
    }

    @Test
    void testSetNameOnCreatedElement() {
        var element = elementFactory.make(new ElementDefinition(ID, NAME, DESCRIPTION, IMAGE_ASSET_SET_ID));
        var newName = randomString();

        element.setName(newName);

        assertEquals(newName, element.getName());
    }

    @Test
    void testSetNameOnCreatedElementWithInvalidParams() {
        var element = elementFactory.make(new ElementDefinition(ID, NAME, DESCRIPTION, IMAGE_ASSET_SET_ID));

        assertThrows(IllegalArgumentException.class, () -> element.setName(null));
        assertThrows(IllegalArgumentException.class, () -> element.setName(""));
    }

    @Test
    void testSetDescriptionOnCreatedElement() {
        var element = elementFactory.make(new ElementDefinition(ID, NAME, DESCRIPTION, IMAGE_ASSET_SET_ID));
        var newDescription = randomString();

        element.setDescription(newDescription);

        assertEquals(newDescription, element.getDescription());
    }

    @Test
    void testSetDescriptionOnCreatedElementWithInvalidParams() {
        var element = elementFactory.make(new ElementDefinition(ID, NAME, DESCRIPTION, IMAGE_ASSET_SET_ID));

        assertThrows(IllegalArgumentException.class, () -> element.setDescription(null));
        assertThrows(IllegalArgumentException.class, () -> element.setDescription(""));
    }

    @Test
    void testGetInterfaceName() {
        assertEquals(Factory.class.getCanonicalName() + "<" +
                ElementDefinition.class.getCanonicalName() + "," +
                Element.class.getCanonicalName() + ">", elementFactory.getInterfaceName());
    }
}
