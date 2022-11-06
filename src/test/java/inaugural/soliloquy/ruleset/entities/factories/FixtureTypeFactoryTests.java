package inaugural.soliloquy.ruleset.entities.factories;

import inaugural.soliloquy.tools.testing.Mock.TypeAndHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import soliloquy.specs.common.entities.Function;
import soliloquy.specs.common.factories.Factory;
import soliloquy.specs.common.persistence.TypeHandler;
import soliloquy.specs.gamestate.entities.Character;
import soliloquy.specs.graphics.assets.GlobalLoopingAnimation;
import soliloquy.specs.graphics.assets.ImageAsset;
import soliloquy.specs.graphics.assets.Sprite;
import soliloquy.specs.graphics.renderables.colorshifting.ColorShift;
import soliloquy.specs.ruleset.definitions.FixtureTypeDefinition;
import soliloquy.specs.ruleset.entities.FixtureType;

import java.util.ArrayList;
import java.util.HashMap;

import static inaugural.soliloquy.tools.random.Random.*;
import static inaugural.soliloquy.tools.testing.Mock.generateMockEntityAndHandler;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FixtureTypeFactoryTests {
    private final String ID = randomString();
    private final String NAME = randomString();
    private final boolean IS_CONTAINER = randomBoolean();
    private final float DEFAULT_X_TILE_WIDTH_OFFSET = randomFloat();
    private final float DEFAULT_Y_TILE_HEIGHT_OFFSET = randomFloat();

    private final String SPRITE_ID = randomString();
    private final String GLOBAL_LOOPING_ANIMATION_ID = randomString();
    private final HashMap<String, Sprite> SPRITES = new HashMap<>();
    private final HashMap<String, GlobalLoopingAnimation> GLOBAL_LOOPING_ANIMATIONS =
            new HashMap<>();
    private final String ON_STEP_FUNCTION_ID = randomString();
    private final String CAN_STEP_FUNCTION_ID = randomString();
    @SuppressWarnings("rawtypes")
    private final HashMap<String, Function> FUNCTIONS = new HashMap<>();

    @Mock private Sprite mockSprite;
    @Mock private GlobalLoopingAnimation mockLoopingAnimation;
    @Mock private Function<Character, Boolean> mockOnStepFunction;
    @Mock private Function<Character, Boolean> mockCanStepFunction;
    @Mock Character mockCharacter1;
    @Mock Character mockCharacter2;

    private final String COLOR_SHIFT_WRITTEN_VALUE = "colorShift";

    private final TypeAndHandler<ColorShift> COLOR_SHIFT_ENTITY_AND_HANDLER =
            generateMockEntityAndHandler(ColorShift.class, COLOR_SHIFT_WRITTEN_VALUE);
    private final ColorShift COLOR_SHIFT = COLOR_SHIFT_ENTITY_AND_HANDLER.entity;
    private final TypeHandler<ColorShift> COLOR_SHIFT_HANDLER =
            COLOR_SHIFT_ENTITY_AND_HANDLER.handler;

    private Factory<FixtureTypeDefinition, FixtureType> fixtureTypeFactory;

    @BeforeEach
    void setUp() {
        mockSprite = mock(Sprite.class);
        SPRITES.put(SPRITE_ID, mockSprite);
        mockLoopingAnimation = mock(GlobalLoopingAnimation.class);
        GLOBAL_LOOPING_ANIMATIONS.put(GLOBAL_LOOPING_ANIMATION_ID, mockLoopingAnimation);
        //noinspection unchecked
        mockOnStepFunction = mock(Function.class);
        when(mockOnStepFunction.apply(any())).thenReturn(true);
        //noinspection unchecked
        mockCanStepFunction = mock(Function.class);
        when(mockCanStepFunction.apply(any())).thenReturn(true);
        FUNCTIONS.put(ON_STEP_FUNCTION_ID, mockOnStepFunction);
        FUNCTIONS.put(CAN_STEP_FUNCTION_ID, mockCanStepFunction);

        mockCharacter1 = mock(Character.class);
        mockCharacter2 = mock(Character.class);

        fixtureTypeFactory =
                new FixtureTypeFactory(COLOR_SHIFT_HANDLER, FUNCTIONS::get, SPRITES::get,
                        GLOBAL_LOOPING_ANIMATIONS::get);
    }

    @Test
    void testConstructorWithInvalidParams() {
        assertThrows(IllegalArgumentException.class,
                () -> new FixtureTypeFactory(null, FUNCTIONS::get, SPRITES::get,
                        GLOBAL_LOOPING_ANIMATIONS::get));
        assertThrows(IllegalArgumentException.class,
                () -> new FixtureTypeFactory(COLOR_SHIFT_HANDLER, null, SPRITES::get,
                        GLOBAL_LOOPING_ANIMATIONS::get));
        assertThrows(IllegalArgumentException.class,
                () -> new FixtureTypeFactory(COLOR_SHIFT_HANDLER, FUNCTIONS::get, null,
                        GLOBAL_LOOPING_ANIMATIONS::get));
        assertThrows(IllegalArgumentException.class,
                () -> new FixtureTypeFactory(COLOR_SHIFT_HANDLER, FUNCTIONS::get, SPRITES::get,
                        null));
    }

    @Test
    void testMakeWithSprite() {
        FixtureTypeDefinition definition =
                new FixtureTypeDefinition(ID, NAME, SPRITE_ID, ImageAsset.ImageAssetType.SPRITE,
                        IS_CONTAINER, ON_STEP_FUNCTION_ID, CAN_STEP_FUNCTION_ID,
                        new String[]{COLOR_SHIFT_WRITTEN_VALUE}, DEFAULT_X_TILE_WIDTH_OFFSET,
                        DEFAULT_Y_TILE_HEIGHT_OFFSET);

        FixtureType fixtureType = fixtureTypeFactory.make(definition);
        fixtureType.onStep(mockCharacter1);
        fixtureType.canStep(mockCharacter2);

        assertNotNull(definition);
        assertEquals(FixtureType.class.getCanonicalName(), fixtureType.getInterfaceName());
        assertEquals(ID, fixtureType.id());
        assertEquals(NAME, fixtureType.getName());
        assertSame(mockSprite, fixtureType.imageAsset());
        assertEquals(IS_CONTAINER, fixtureType.isContainer());
        assertEquals(new ArrayList<ColorShift>() {{
            add(COLOR_SHIFT);
        }}, fixtureType.defaultColorShifts());
        assertEquals(DEFAULT_X_TILE_WIDTH_OFFSET, fixtureType.defaultXTileWidthOffset(), 0.001f);
        assertEquals(DEFAULT_Y_TILE_HEIGHT_OFFSET, fixtureType.defaultYTileHeightOffset(), 0.001f);
        verify(COLOR_SHIFT_HANDLER, times(1)).read(COLOR_SHIFT_WRITTEN_VALUE);
        verify(mockOnStepFunction, times(1)).apply(mockCharacter1);
        verify(mockCanStepFunction, times(1)).apply(mockCharacter2);
    }

    @Test
    void testMakeWithLoopingAnimation() {
        FixtureTypeDefinition definition =
                new FixtureTypeDefinition(ID, NAME, GLOBAL_LOOPING_ANIMATION_ID,
                        ImageAsset.ImageAssetType.GLOBAL_LOOPING_ANIMATION, IS_CONTAINER,
                        ON_STEP_FUNCTION_ID, CAN_STEP_FUNCTION_ID,
                        new String[]{COLOR_SHIFT_WRITTEN_VALUE}, DEFAULT_X_TILE_WIDTH_OFFSET,
                        DEFAULT_Y_TILE_HEIGHT_OFFSET);

        FixtureType fixtureType = fixtureTypeFactory.make(definition);
        fixtureType.onStep(mockCharacter1);
        fixtureType.canStep(mockCharacter2);

        assertNotNull(definition);
        assertEquals(FixtureType.class.getCanonicalName(), fixtureType.getInterfaceName());
        assertEquals(ID, fixtureType.id());
        assertEquals(NAME, fixtureType.getName());
        assertSame(mockLoopingAnimation, fixtureType.imageAsset());
        assertEquals(IS_CONTAINER, fixtureType.isContainer());
        assertEquals(new ArrayList<ColorShift>() {{
            add(COLOR_SHIFT);
        }}, fixtureType.defaultColorShifts());
        assertEquals(DEFAULT_X_TILE_WIDTH_OFFSET, fixtureType.defaultXTileWidthOffset(), 0.001f);
        assertEquals(DEFAULT_Y_TILE_HEIGHT_OFFSET, fixtureType.defaultYTileHeightOffset(), 0.001f);
        verify(COLOR_SHIFT_HANDLER, times(1)).read(COLOR_SHIFT_WRITTEN_VALUE);
        verify(mockOnStepFunction, times(1)).apply(mockCharacter1);
        verify(mockCanStepFunction, times(1)).apply(mockCharacter2);
    }

    @Test
    void testSetName() {
        FixtureTypeDefinition definition =
                new FixtureTypeDefinition(ID, NAME, SPRITE_ID, ImageAsset.ImageAssetType.SPRITE,
                        IS_CONTAINER, ON_STEP_FUNCTION_ID, CAN_STEP_FUNCTION_ID,
                        new String[]{COLOR_SHIFT_WRITTEN_VALUE}, DEFAULT_X_TILE_WIDTH_OFFSET,
                        DEFAULT_Y_TILE_HEIGHT_OFFSET);
        String newName = randomString();
        FixtureType fixtureType = fixtureTypeFactory.make(definition);

        fixtureType.setName(newName);

        assertEquals(newName, fixtureType.getName());
    }

    @Test
    void testSetNameWithInvalidParams() {
        FixtureTypeDefinition definition =
                new FixtureTypeDefinition(ID, NAME, SPRITE_ID, ImageAsset.ImageAssetType.SPRITE,
                        IS_CONTAINER, ON_STEP_FUNCTION_ID, CAN_STEP_FUNCTION_ID,
                        new String[]{COLOR_SHIFT_WRITTEN_VALUE}, DEFAULT_X_TILE_WIDTH_OFFSET,
                        DEFAULT_Y_TILE_HEIGHT_OFFSET);
        FixtureType fixtureType = fixtureTypeFactory.make(definition);

        assertThrows(IllegalArgumentException.class, () -> fixtureType.setName(null));
        assertThrows(IllegalArgumentException.class, () -> fixtureType.setName(""));
    }

    @Test
    void testStepFunctionsWithInvalidParams() {
        FixtureTypeDefinition definition =
                new FixtureTypeDefinition(ID, NAME, SPRITE_ID, ImageAsset.ImageAssetType.SPRITE,
                        IS_CONTAINER, ON_STEP_FUNCTION_ID, CAN_STEP_FUNCTION_ID,
                        new String[]{COLOR_SHIFT_WRITTEN_VALUE}, DEFAULT_X_TILE_WIDTH_OFFSET,
                        DEFAULT_Y_TILE_HEIGHT_OFFSET);
        FixtureType fixtureType = fixtureTypeFactory.make(definition);

        assertThrows(IllegalArgumentException.class, () -> fixtureType.onStep(null));
        assertThrows(IllegalArgumentException.class, () -> fixtureType.canStep(null));
    }

    @Test
    void testGetInterfaceName() {
        assertEquals(Factory.class.getCanonicalName() + "<" +
                FixtureTypeDefinition.class.getCanonicalName() + "," +
                FixtureType.class.getCanonicalName() + ">", fixtureTypeFactory.getInterfaceName());
    }
}
