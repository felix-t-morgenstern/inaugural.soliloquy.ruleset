package inaugural.soliloquy.ruleset.entities.factories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import soliloquy.specs.graphics.assets.GlobalLoopingAnimation;
import soliloquy.specs.graphics.assets.ImageAsset;
import soliloquy.specs.graphics.assets.Sprite;
import soliloquy.specs.ruleset.definitions.WallSegmentTypeDefinition;
import soliloquy.specs.ruleset.entities.WallSegmentType;
import soliloquy.specs.ruleset.entities.factories.WallSegmentTypeFactory;

import java.util.HashMap;

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
    private final HashMap<String, Sprite> SPRITES = new HashMap<>();
    private final HashMap<String, GlobalLoopingAnimation> GLOBAL_LOOPING_ANIMATIONS =
            new HashMap<>();

    @Mock
    private Sprite mockSprite;
    @Mock
    private GlobalLoopingAnimation mockGlobalLoopingAnimation;

    private WallSegmentTypeFactory wallSegmentTypeFactory;

    @BeforeEach
    void setUp() {
        mockSprite = mock(Sprite.class);
        SPRITES.put(SPRITE_ID, mockSprite);
        mockGlobalLoopingAnimation = mock(GlobalLoopingAnimation.class);
        GLOBAL_LOOPING_ANIMATIONS.put(GLOBAL_LOOPING_ANIMATION_ID, mockGlobalLoopingAnimation);

        wallSegmentTypeFactory = new WallSegmentTypeFactoryImpl(SPRITES::get,
                GLOBAL_LOOPING_ANIMATIONS::get);
    }

    @Test
    void testConstructorWithInvalidParams() {
        assertThrows(IllegalArgumentException.class, () -> new WallSegmentTypeFactoryImpl(null,
                GLOBAL_LOOPING_ANIMATIONS::get));
        assertThrows(IllegalArgumentException.class,
                () -> new WallSegmentTypeFactoryImpl(SPRITES::get, null));
    }

    @Test
    void testMakeWithSprite() {
        WallSegmentTypeDefinition definition = new WallSegmentTypeDefinition(ID, NAME,
                ImageAsset.ImageAssetType.SPRITE, SPRITE_ID, BLOCKS_WEST, BLOCKS_NORTHWEST,
                BLOCKS_NORTH);

        WallSegmentType wallSegmentType = wallSegmentTypeFactory.make(definition);

        assertNotNull(wallSegmentType);
        assertEquals(ID, wallSegmentType.id());
        assertEquals(NAME, wallSegmentType.getName());
        assertSame(mockSprite, wallSegmentType.imageAsset());
        assertEquals(BLOCKS_WEST, wallSegmentType.blocksWest());
        assertEquals(BLOCKS_NORTHWEST, wallSegmentType.blocksNorthwest());
        assertEquals(BLOCKS_NORTH, wallSegmentType.blocksNorth());
        assertEquals(WallSegmentType.class.getCanonicalName(), wallSegmentType.getInterfaceName());
    }

    @Test
    void testMakeWithGlobalLoopingAnimation() {
        WallSegmentTypeDefinition definition = new WallSegmentTypeDefinition(ID, NAME,
                ImageAsset.ImageAssetType.GLOBAL_LOOPING_ANIMATION, GLOBAL_LOOPING_ANIMATION_ID,
                BLOCKS_WEST, BLOCKS_NORTHWEST, BLOCKS_NORTH);

        WallSegmentType wallSegmentType = wallSegmentTypeFactory.make(definition);

        assertNotNull(wallSegmentType);
        assertEquals(ID, wallSegmentType.id());
        assertEquals(NAME, wallSegmentType.getName());
        assertSame(mockGlobalLoopingAnimation, wallSegmentType.imageAsset());
        assertEquals(BLOCKS_WEST, wallSegmentType.blocksWest());
        assertEquals(BLOCKS_NORTHWEST, wallSegmentType.blocksNorthwest());
        assertEquals(BLOCKS_NORTH, wallSegmentType.blocksNorth());
        assertEquals(WallSegmentType.class.getCanonicalName(), wallSegmentType.getInterfaceName());
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
                        ImageAsset.ImageAssetType.UNKNOWN, SPRITE_ID, BLOCKS_WEST,
                        BLOCKS_NORTHWEST, BLOCKS_NORTH)));
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
        WallSegmentTypeDefinition definition = new WallSegmentTypeDefinition(ID, NAME,
                ImageAsset.ImageAssetType.SPRITE, SPRITE_ID, BLOCKS_WEST, BLOCKS_NORTHWEST,
                BLOCKS_NORTH);
        WallSegmentType wallSegmentType = wallSegmentTypeFactory.make(definition);
        String newName = randomString();

        wallSegmentType.setName(newName);

        assertEquals(newName, wallSegmentType.getName());
    }

    @Test
    void testSetNameOnCreatedElementWithInvalidParams() {
        WallSegmentTypeDefinition definition = new WallSegmentTypeDefinition(ID, NAME,
                ImageAsset.ImageAssetType.SPRITE, SPRITE_ID, BLOCKS_WEST, BLOCKS_NORTHWEST,
                BLOCKS_NORTH);
        WallSegmentType wallSegmentType = wallSegmentTypeFactory.make(definition);

        assertThrows(IllegalArgumentException.class, () -> wallSegmentType.setName(null));
        assertThrows(IllegalArgumentException.class, () -> wallSegmentType.setName(""));
    }

    @Test
    void testGetInterfaceName() {
        assertEquals(WallSegmentTypeFactory.class.getCanonicalName(),
                wallSegmentTypeFactory.getInterfaceName());
    }
}
