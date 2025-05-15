package inaugural.soliloquy.ruleset.entities.factories;

import inaugural.soliloquy.ruleset.definitions.ElementDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import soliloquy.specs.graphics.assets.ImageAssetSet;
import soliloquy.specs.ruleset.entities.Element;
import soliloquy.specs.ruleset.entities.character.StaticStatisticType;

import java.util.function.Function;

import static inaugural.soliloquy.tools.random.Random.randomString;
import static inaugural.soliloquy.tools.testing.Mock.generateMockLookupFunction;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static soliloquy.specs.common.valueobjects.Pair.pairOf;

@ExtendWith(MockitoExtension.class)
public class ElementFactoryTests {
    private final String ID = randomString();
    private final String NAME = randomString();
    private final String DESCRIPTION = randomString();
    private final String IMAGE_ASSET_SET_ID = randomString();
    private final String RESISTANCE_STAT_TYPE_ID = randomString();
    private final ElementDefinition DEFINITION =
            new ElementDefinition(ID, NAME, DESCRIPTION, IMAGE_ASSET_SET_ID,
                    RESISTANCE_STAT_TYPE_ID);

    @Mock private ImageAssetSet mockImageAssetSet;
    private Function<String, ImageAssetSet> mockGetImageAssetSet;
    @Mock private StaticStatisticType mockResistanceStatType;
    private Function<String, StaticStatisticType> mockGetStaticStatType;

    private Function<ElementDefinition, Element> elementFactory;

    @BeforeEach
    public void setUp() {
        mockGetImageAssetSet =
                generateMockLookupFunction(pairOf(IMAGE_ASSET_SET_ID, mockImageAssetSet));

        mockGetStaticStatType = generateMockLookupFunction(
                pairOf(RESISTANCE_STAT_TYPE_ID, mockResistanceStatType));

        elementFactory = new ElementFactory(mockGetImageAssetSet, mockGetStaticStatType);
    }

    @Test
    public void testConstructorWithInvalidArgs() {
        assertThrows(IllegalArgumentException.class,
                () -> new ElementFactory(null, mockGetStaticStatType));
        assertThrows(IllegalArgumentException.class,
                () -> new ElementFactory(mockGetImageAssetSet, null));
    }

    @Test
    public void testMake() {
        var element = elementFactory.apply(
                new ElementDefinition(ID, NAME, DESCRIPTION, IMAGE_ASSET_SET_ID,
                        RESISTANCE_STAT_TYPE_ID));

        assertNotNull(element);
        assertEquals(ID, element.id());
        assertEquals(NAME, element.getName());
        assertEquals(DESCRIPTION, element.getDescription());
        assertSame(mockImageAssetSet, element.imageAssetSet());
        assertSame(mockResistanceStatType, element.resistanceStatisticType());
        verify(mockGetImageAssetSet).apply(IMAGE_ASSET_SET_ID);
        verify(mockGetStaticStatType).apply(RESISTANCE_STAT_TYPE_ID);
    }

    @Test
    public void testMakeWithInvalidArgs() {
        var invalidId = randomString();
        assertThrows(IllegalArgumentException.class, () -> elementFactory.apply(null));
        assertThrows(IllegalArgumentException.class, () -> elementFactory.apply(
                new ElementDefinition(null, NAME, DESCRIPTION, IMAGE_ASSET_SET_ID,
                        RESISTANCE_STAT_TYPE_ID)));
        assertThrows(IllegalArgumentException.class, () -> elementFactory.apply(
                new ElementDefinition("", NAME, DESCRIPTION, IMAGE_ASSET_SET_ID,
                        RESISTANCE_STAT_TYPE_ID)));
        assertThrows(IllegalArgumentException.class, () -> elementFactory.apply(
                new ElementDefinition(ID, null, DESCRIPTION, IMAGE_ASSET_SET_ID,
                        RESISTANCE_STAT_TYPE_ID)));
        assertThrows(IllegalArgumentException.class, () -> elementFactory.apply(
                new ElementDefinition(ID, "", DESCRIPTION, IMAGE_ASSET_SET_ID,
                        RESISTANCE_STAT_TYPE_ID)));
        assertThrows(IllegalArgumentException.class, () -> elementFactory.apply(
                new ElementDefinition(ID, NAME, null, IMAGE_ASSET_SET_ID,
                        RESISTANCE_STAT_TYPE_ID)));
        assertThrows(IllegalArgumentException.class, () -> elementFactory.apply(
                new ElementDefinition(ID, NAME, "", IMAGE_ASSET_SET_ID, RESISTANCE_STAT_TYPE_ID)));
        assertThrows(IllegalArgumentException.class, () -> elementFactory.apply(
                new ElementDefinition(ID, NAME, DESCRIPTION, null, RESISTANCE_STAT_TYPE_ID)));
        assertThrows(IllegalArgumentException.class, () -> elementFactory.apply(
                new ElementDefinition(ID, NAME, DESCRIPTION, "", RESISTANCE_STAT_TYPE_ID)));
        assertThrows(IllegalArgumentException.class, () -> elementFactory.apply(
                new ElementDefinition(ID, NAME, DESCRIPTION, invalidId, RESISTANCE_STAT_TYPE_ID)));
        assertThrows(IllegalArgumentException.class, () -> elementFactory.apply(
                new ElementDefinition(ID, NAME, DESCRIPTION, IMAGE_ASSET_SET_ID, null)));
        assertThrows(IllegalArgumentException.class, () -> elementFactory.apply(
                new ElementDefinition(ID, NAME, DESCRIPTION, IMAGE_ASSET_SET_ID, "")));
        assertThrows(IllegalArgumentException.class, () -> elementFactory.apply(
                new ElementDefinition(ID, NAME, DESCRIPTION, IMAGE_ASSET_SET_ID, invalidId)));
    }

    @Test
    public void testSetNameOnCreatedElement() {
        var element = elementFactory.apply(DEFINITION);
        var newName = randomString();

        element.setName(newName);

        assertEquals(newName, element.getName());
    }

    @Test
    public void testSetNameOnCreatedElementWithInvalidArgs() {
        var element = elementFactory.apply(DEFINITION);

        assertThrows(IllegalArgumentException.class, () -> element.setName(null));
        assertThrows(IllegalArgumentException.class, () -> element.setName(""));
    }

    @Test
    public void testSetDescriptionOnCreatedElement() {
        var element = elementFactory.apply(DEFINITION);
        var newDescription = randomString();

        element.setDescription(newDescription);

        assertEquals(newDescription, element.getDescription());
    }

    @Test
    public void testSetDescriptionOnCreatedElementWithInvalidArgs() {
        var element = elementFactory.apply(DEFINITION);

        assertThrows(IllegalArgumentException.class, () -> element.setDescription(null));
        assertThrows(IllegalArgumentException.class, () -> element.setDescription(""));
    }
}
