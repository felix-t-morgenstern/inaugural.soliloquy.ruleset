package inaugural.soliloquy.ruleset.entities.factories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import soliloquy.specs.common.factories.Factory;
import soliloquy.specs.graphics.assets.GlobalLoopingAnimation;
import soliloquy.specs.graphics.assets.ImageAsset;
import soliloquy.specs.graphics.assets.Sprite;
import inaugural.soliloquy.ruleset.definitions.WallSegmentTypeDefinition;
import soliloquy.specs.ruleset.entities.WallSegmentType;

import java.util.Map;

import static inaugural.soliloquy.tools.collections.Collections.mapOf;
import static inaugural.soliloquy.tools.random.Random.randomBoolean;
import static inaugural.soliloquy.tools.random.Random.randomString;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

class WallSegmentTypeFactoryImplTests {
    private final String ID = randomString();
    private final String NAME = randomString();
    private final String SPRITE_ID = randomString();
    private final String GLOBAL_LOOPING_ANIMATION_ID = randomString();
    private final boolean BLOCKS_WEST = randomBoolean();
    private final boolean BLOCKS_NORTHWEST = randomBoolean();
    private final boolean BLOCKS_NORTH = randomBoolean();
    private final Map<String, Sprite> SPRITES = mapOf();
    private final Map<String, GlobalLoopingAnimation> GLOBAL_LOOPING_ANIMATIONS = mapOf();

    @Mock
    private Sprite mockSprite;
    @Mock
    private GlobalLoopingAnimation mockGlobalLoopingAnimation;

    private Factory<WallSegmentTypeDefinition, WallSegmentType> wallSegmentTypeFactory;

    @BeforeEach
    void setUp() {
        mockSprite = mock(Sprite.class);
        SPRITES.put(SPRITE_ID, mockSprite);
        mockGlobalLoopingAnimation = mock(GlobalLoopingAnimation.class);
        GLOBAL_LOOPING_ANIMATIONS.put(GLOBAL_LOOPING_ANIMATION_ID, mockGlobalLoopingAnimation);

        wallSegmentTypeFactory = new WallSegmentTypeFactory(SPRITES::get,
                GLOBAL_LOOPING_ANIMATIONS::get);
    }

    @Test
    void testConstructorWithInvalidParams() {
        assertThrows(IllegalArgumentException.class, () -> new WallSegmentTypeFactory(null,
                GLOBAL_LOOPING_ANIMATIONS::get));
        assertThrows(IllegalArgumentException.class,
                () -> new WallSegmentTypeFactory(SPRITES::get, null));
    }

    @Test
    void testMakeWithSprite() {
        var definition =
                new WallSegmentTypeDefinition(ID, NAME, ImageAsset.ImageAssetType.SPRITE, SPRITE_ID,
                        BLOCKS_WEST, BLOCKS_NORTHWEST, BLOCKS_NORTH);

        var output = wallSegmentTypeFactory.make(definition);

        assertNotNull(output);
        assertEquals(ID, output.id());
        assertEquals(NAME, output.getName());
        assertSame(mockSprite, output.imageAsset());
        assertEquals(BLOCKS_WEST, output.blocksWest());
        assertEquals(BLOCKS_NORTHWEST, output.blocksNorthwest());
        assertEquals(BLOCKS_NORTH, output.blocksNorth());
        assertEquals(WallSegmentType.class.getCanonicalName(), output.getInterfaceName());
    }

    @Test
    void testMakeWithGlobalLoopingAnimation() {
        var definition = new WallSegmentTypeDefinition(ID, NAME,
                ImageAsset.ImageAssetType.GLOBAL_LOOPING_ANIMATION, GLOBAL_LOOPING_ANIMATION_ID,
                BLOCKS_WEST, BLOCKS_NORTHWEST, BLOCKS_NORTH);

        var output = wallSegmentTypeFactory.make(definition);

        assertNotNull(output);
        assertEquals(ID, output.id());
        assertEquals(NAME, output.getName());
        assertSame(mockGlobalLoopingAnimation, output.imageAsset());
        assertEquals(BLOCKS_WEST, output.blocksWest());
        assertEquals(BLOCKS_NORTHWEST, output.blocksNorthwest());
        assertEquals(BLOCKS_NORTH, output.blocksNorth());
        assertEquals(WallSegmentType.class.getCanonicalName(), output.getInterfaceName());
    }

    @Test
    void testMakeWithInvalidParams() {
        assertThrows(IllegalArgumentException.class, () -> wallSegmentTypeFactory.make(null));
        assertThrows(IllegalArgumentException.class,
                () -> wallSegmentTypeFactory.make(new WallSegmentTypeDefinition(null, NAME,
                        ImageAsset.ImageAssetType.SPRITE, SPRITE_ID, BLOCKS_WEST,
                        BLOCKS_NORTHWEST, BLOCKS_NORTH)));
        assertThrows(IllegalArgumentException.class,
                () -> wallSegmentTypeFactory.make(new WallSegmentTypeDefinition("", NAME,
                        ImageAsset.ImageAssetType.SPRITE, SPRITE_ID, BLOCKS_WEST,
                        BLOCKS_NORTHWEST, BLOCKS_NORTH)));
        assertThrows(IllegalArgumentException.class,
                () -> wallSegmentTypeFactory.make(new WallSegmentTypeDefinition(ID, null,
                        ImageAsset.ImageAssetType.SPRITE, SPRITE_ID, BLOCKS_WEST,
                        BLOCKS_NORTHWEST, BLOCKS_NORTH)));
        assertThrows(IllegalArgumentException.class,
                () -> wallSegmentTypeFactory.make(new WallSegmentTypeDefinition(ID, "",
                        ImageAsset.ImageAssetType.SPRITE, SPRITE_ID, BLOCKS_WEST,
                        BLOCKS_NORTHWEST, BLOCKS_NORTH)));
        assertThrows(IllegalArgumentException.class,
                () -> wallSegmentTypeFactory.make(new WallSegmentTypeDefinition(ID, NAME, null,
                        SPRITE_ID, BLOCKS_WEST, BLOCKS_NORTHWEST, BLOCKS_NORTH)));
        assertThrows(IllegalArgumentException.class,
                () -> wallSegmentTypeFactory.make(new WallSegmentTypeDefinition(ID, NAME,
                        ImageAsset.ImageAssetType.ANIMATION, SPRITE_ID, BLOCKS_WEST,
                        BLOCKS_NORTHWEST, BLOCKS_NORTH)));
        assertThrows(IllegalArgumentException.class,
                () -> wallSegmentTypeFactory.make(new WallSegmentTypeDefinition(ID, NAME,
                        ImageAsset.ImageAssetType.SPRITE, null, BLOCKS_WEST, BLOCKS_NORTHWEST,
                        BLOCKS_NORTH)));
        assertThrows(IllegalArgumentException.class,
                () -> wallSegmentTypeFactory.make(new WallSegmentTypeDefinition(ID, NAME,
                        ImageAsset.ImageAssetType.SPRITE, "", BLOCKS_WEST, BLOCKS_NORTHWEST,
                        BLOCKS_NORTH)));
        assertThrows(IllegalArgumentException.class,
                () -> wallSegmentTypeFactory.make(new WallSegmentTypeDefinition(ID, NAME,
                        ImageAsset.ImageAssetType.SPRITE, GLOBAL_LOOPING_ANIMATION_ID,
                        BLOCKS_WEST, BLOCKS_NORTHWEST, BLOCKS_NORTH)));
        assertThrows(IllegalArgumentException.class,
                () -> wallSegmentTypeFactory.make(new WallSegmentTypeDefinition(ID, NAME,
                        ImageAsset.ImageAssetType.GLOBAL_LOOPING_ANIMATION, SPRITE_ID,
                        BLOCKS_WEST, BLOCKS_NORTHWEST, BLOCKS_NORTH)));
    }

    @Test
    void testSetNameOnCreatedElement() {
        var definition =
                new WallSegmentTypeDefinition(ID, NAME, ImageAsset.ImageAssetType.SPRITE, SPRITE_ID,
                        BLOCKS_WEST, BLOCKS_NORTHWEST, BLOCKS_NORTH);
        var output = wallSegmentTypeFactory.make(definition);
        var newName = randomString();

        output.setName(newName);

        assertEquals(newName, output.getName());
    }

    @Test
    void testSetNameOnCreatedElementWithInvalidParams() {
        var definition =
                new WallSegmentTypeDefinition(ID, NAME, ImageAsset.ImageAssetType.SPRITE, SPRITE_ID,
                        BLOCKS_WEST, BLOCKS_NORTHWEST, BLOCKS_NORTH);
        var wallSegmentType = wallSegmentTypeFactory.make(definition);

        assertThrows(IllegalArgumentException.class, () -> wallSegmentType.setName(null));
        assertThrows(IllegalArgumentException.class, () -> wallSegmentType.setName(""));
    }

    @Test
    void testGetInterfaceName() {
        assertEquals(Factory.class.getCanonicalName() + "<" +
                        WallSegmentTypeDefinition.class.getCanonicalName() + "," +
                        WallSegmentType.class.getCanonicalName() + ">",
                wallSegmentTypeFactory.getInterfaceName());
    }
}
