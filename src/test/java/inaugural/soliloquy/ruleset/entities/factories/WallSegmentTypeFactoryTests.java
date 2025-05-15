package inaugural.soliloquy.ruleset.entities.factories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import soliloquy.specs.gamestate.entities.WallSegmentOrientation;
import soliloquy.specs.graphics.assets.GlobalLoopingAnimation;
import soliloquy.specs.graphics.assets.ImageAsset;
import soliloquy.specs.graphics.assets.Sprite;
import inaugural.soliloquy.ruleset.definitions.WallSegmentTypeDefinition;
import soliloquy.specs.ruleset.entities.WallSegmentType;

import java.util.Map;
import java.util.function.Function;

import static inaugural.soliloquy.tools.collections.Collections.mapOf;
import static inaugural.soliloquy.tools.random.Random.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class WallSegmentTypeFactoryTests {
    private final String ID = randomString();
    private final String NAME = randomString();
    private final int DIRECTION = randomIntInRange(1, 3);
    private final String SPRITE_ID = randomString();
    private final String GLOBAL_LOOPING_ANIMATION_ID = randomString();
    private final boolean BLOCKS_MOVEMENT = randomBoolean();
    private final boolean BLOCKS_SIGHT = randomBoolean();
    private final Map<String, Sprite> SPRITES = mapOf();
    private final Map<String, GlobalLoopingAnimation> GLOBAL_LOOPING_ANIMATIONS = mapOf();

    @Mock
    private Sprite mockSprite;
    @Mock
    private GlobalLoopingAnimation mockGlobalLoopingAnimation;

    private Function<WallSegmentTypeDefinition, WallSegmentType> wallSegmentTypeFactory;

    @BeforeEach
    void setUp() {
        mockSprite = mock(Sprite.class);
        SPRITES.put(SPRITE_ID, mockSprite);
        mockGlobalLoopingAnimation = mock(GlobalLoopingAnimation.class);
        GLOBAL_LOOPING_ANIMATIONS.put(GLOBAL_LOOPING_ANIMATION_ID, mockGlobalLoopingAnimation);

        wallSegmentTypeFactory =
                new WallSegmentTypeFactory(SPRITES::get, GLOBAL_LOOPING_ANIMATIONS::get);
    }

    @Test
    void testConstructorWithInvalidArgs() {
        assertThrows(IllegalArgumentException.class,
                () -> new WallSegmentTypeFactory(null, GLOBAL_LOOPING_ANIMATIONS::get));
        assertThrows(IllegalArgumentException.class,
                () -> new WallSegmentTypeFactory(SPRITES::get, null));
    }

    @Test
    void testMakeWithSprite() {
        var definition =
                new WallSegmentTypeDefinition(ID, NAME, DIRECTION, ImageAsset.ImageAssetType.SPRITE,
                        SPRITE_ID, BLOCKS_MOVEMENT, BLOCKS_SIGHT);

        var output = wallSegmentTypeFactory.apply(definition);

        assertNotNull(output);
        assertEquals(ID, output.id());
        assertEquals(NAME, output.getName());
        assertSame(WallSegmentOrientation.fromValue(DIRECTION), output.orientation());
        assertSame(mockSprite, output.imageAsset());
        assertEquals(BLOCKS_MOVEMENT, output.blocksMovement());
        assertEquals(BLOCKS_SIGHT, output.blocksSight());
    }

    @Test
    void testMakeWithGlobalLoopingAnimation() {
        var definition = new WallSegmentTypeDefinition(ID, NAME, DIRECTION,
                ImageAsset.ImageAssetType.GLOBAL_LOOPING_ANIMATION, GLOBAL_LOOPING_ANIMATION_ID,
                BLOCKS_MOVEMENT, BLOCKS_SIGHT);

        var output = wallSegmentTypeFactory.apply(definition);

        assertNotNull(output);
        assertEquals(ID, output.id());
        assertEquals(NAME, output.getName());
        assertSame(WallSegmentOrientation.fromValue(DIRECTION), output.orientation());
        assertSame(mockGlobalLoopingAnimation, output.imageAsset());
        assertEquals(BLOCKS_MOVEMENT, output.blocksMovement());
        assertEquals(BLOCKS_SIGHT, output.blocksSight());
    }

    @Test
    void testMakeWithInvalidArgs() {
        assertThrows(IllegalArgumentException.class, () -> wallSegmentTypeFactory.apply(null));
        assertThrows(IllegalArgumentException.class, () -> wallSegmentTypeFactory.apply(
                new WallSegmentTypeDefinition(null, NAME, DIRECTION,
                        ImageAsset.ImageAssetType.SPRITE, SPRITE_ID, BLOCKS_MOVEMENT,
                        BLOCKS_SIGHT)));
        assertThrows(IllegalArgumentException.class, () -> wallSegmentTypeFactory.apply(
                new WallSegmentTypeDefinition("", NAME, DIRECTION, ImageAsset.ImageAssetType.SPRITE,
                        SPRITE_ID, BLOCKS_MOVEMENT, BLOCKS_SIGHT)));
        assertThrows(IllegalArgumentException.class, () -> wallSegmentTypeFactory.apply(
                new WallSegmentTypeDefinition(ID, null, DIRECTION, ImageAsset.ImageAssetType.SPRITE,
                        SPRITE_ID, BLOCKS_MOVEMENT, BLOCKS_SIGHT)));
        assertThrows(IllegalArgumentException.class, () -> wallSegmentTypeFactory.apply(
                new WallSegmentTypeDefinition(ID, "", DIRECTION, ImageAsset.ImageAssetType.SPRITE,
                        SPRITE_ID, BLOCKS_MOVEMENT, BLOCKS_SIGHT)));
        assertThrows(IllegalArgumentException.class, () -> wallSegmentTypeFactory.apply(
                new WallSegmentTypeDefinition(ID, NAME, 0, ImageAsset.ImageAssetType.SPRITE,
                        SPRITE_ID, BLOCKS_MOVEMENT, BLOCKS_SIGHT)));
        assertThrows(IllegalArgumentException.class, () -> wallSegmentTypeFactory.apply(
                new WallSegmentTypeDefinition(ID, NAME, 4, ImageAsset.ImageAssetType.SPRITE,
                        SPRITE_ID, BLOCKS_MOVEMENT, BLOCKS_SIGHT)));
        assertThrows(IllegalArgumentException.class, () -> wallSegmentTypeFactory.apply(
                new WallSegmentTypeDefinition(ID, NAME, DIRECTION, null, SPRITE_ID, BLOCKS_MOVEMENT,
                        BLOCKS_SIGHT)));
        assertThrows(IllegalArgumentException.class, () -> wallSegmentTypeFactory.apply(
                new WallSegmentTypeDefinition(ID, NAME, DIRECTION,
                        ImageAsset.ImageAssetType.ANIMATION, SPRITE_ID, BLOCKS_MOVEMENT,
                        BLOCKS_SIGHT)));
        assertThrows(IllegalArgumentException.class, () -> wallSegmentTypeFactory.apply(
                new WallSegmentTypeDefinition(ID, NAME, DIRECTION, ImageAsset.ImageAssetType.SPRITE,
                        null, BLOCKS_MOVEMENT, BLOCKS_SIGHT)));
        assertThrows(IllegalArgumentException.class, () -> wallSegmentTypeFactory.apply(
                new WallSegmentTypeDefinition(ID, NAME, DIRECTION, ImageAsset.ImageAssetType.SPRITE,
                        "", BLOCKS_MOVEMENT, BLOCKS_SIGHT)));
        assertThrows(IllegalArgumentException.class, () -> wallSegmentTypeFactory.apply(
                new WallSegmentTypeDefinition(ID, NAME, DIRECTION, ImageAsset.ImageAssetType.SPRITE,
                        GLOBAL_LOOPING_ANIMATION_ID, BLOCKS_MOVEMENT, BLOCKS_SIGHT)));
        assertThrows(IllegalArgumentException.class, () -> wallSegmentTypeFactory.apply(
                new WallSegmentTypeDefinition(ID, NAME, DIRECTION,
                        ImageAsset.ImageAssetType.GLOBAL_LOOPING_ANIMATION, SPRITE_ID,
                        BLOCKS_MOVEMENT, BLOCKS_SIGHT)));
    }

    @Test
    void testSetNameOnCreatedElement() {
        var definition =
                new WallSegmentTypeDefinition(ID, NAME, DIRECTION, ImageAsset.ImageAssetType.SPRITE,
                        SPRITE_ID, BLOCKS_MOVEMENT, BLOCKS_SIGHT);
        var output = wallSegmentTypeFactory.apply(definition);
        var newName = randomString();

        output.setName(newName);

        assertEquals(newName, output.getName());
    }

    @Test
    void testSetNameOnCreatedElementWithInvalidArgs() {
        var definition =
                new WallSegmentTypeDefinition(ID, NAME, DIRECTION, ImageAsset.ImageAssetType.SPRITE,
                        SPRITE_ID, BLOCKS_MOVEMENT, BLOCKS_SIGHT);
        var wallSegmentType = wallSegmentTypeFactory.apply(definition);

        assertThrows(IllegalArgumentException.class, () -> wallSegmentType.setName(null));
        assertThrows(IllegalArgumentException.class, () -> wallSegmentType.setName(""));
    }
}
