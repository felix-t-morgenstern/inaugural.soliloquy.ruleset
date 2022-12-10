package inaugural.soliloquy.ruleset.entities.factories;

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
import soliloquy.specs.ruleset.definitions.GroundTypeDefinition;
import soliloquy.specs.ruleset.entities.FixtureType;
import soliloquy.specs.ruleset.entities.GroundType;

import java.util.ArrayList;
import java.util.HashMap;

import static inaugural.soliloquy.tools.random.Random.randomString;
import static inaugural.soliloquy.tools.testing.Mock.generateMockEntityAndHandler;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class GroundTypeFactoryTests {
    private final String ID = randomString();
    private final String NAME = randomString();

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

    private final inaugural.soliloquy.tools.testing.Mock.TypeAndHandler<ColorShift>
            COLOR_SHIFT_ENTITY_AND_HANDLER =
            generateMockEntityAndHandler(ColorShift.class, COLOR_SHIFT_WRITTEN_VALUE);
    private final ColorShift COLOR_SHIFT = COLOR_SHIFT_ENTITY_AND_HANDLER.entity;
    private final TypeHandler<ColorShift> COLOR_SHIFT_HANDLER =
            COLOR_SHIFT_ENTITY_AND_HANDLER.handler;

    private Factory<GroundTypeDefinition, GroundType> groundTypeFactory;

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

        groundTypeFactory = new GroundTypeFactory(COLOR_SHIFT_HANDLER, FUNCTIONS::get, SPRITES::get,
                GLOBAL_LOOPING_ANIMATIONS::get);
    }

    @Test
    void testConstructorWithInvalidParams() {
        assertThrows(IllegalArgumentException.class,
                () -> new GroundTypeFactory(null, FUNCTIONS::get, SPRITES::get,
                        GLOBAL_LOOPING_ANIMATIONS::get));
        assertThrows(IllegalArgumentException.class,
                () -> new GroundTypeFactory(COLOR_SHIFT_HANDLER, null, SPRITES::get,
                        GLOBAL_LOOPING_ANIMATIONS::get));
        assertThrows(IllegalArgumentException.class,
                () -> new GroundTypeFactory(COLOR_SHIFT_HANDLER, FUNCTIONS::get, null,
                        GLOBAL_LOOPING_ANIMATIONS::get));
        assertThrows(IllegalArgumentException.class,
                () -> new GroundTypeFactory(COLOR_SHIFT_HANDLER, FUNCTIONS::get, SPRITES::get,
                        null));
    }

    @Test
    void testMakeWithSprite() {
        GroundTypeDefinition definition =
                new GroundTypeDefinition(ID, NAME, 1, SPRITE_ID,
                        ON_STEP_FUNCTION_ID, CAN_STEP_FUNCTION_ID,
                        new String[]{COLOR_SHIFT_WRITTEN_VALUE});

        GroundType groundType = groundTypeFactory.make(definition);
        groundType.onStep(mockCharacter1);
        groundType.canStep(mockCharacter2);

        assertNotNull(definition);
        assertEquals(GroundType.class.getCanonicalName(), groundType.getInterfaceName());
        assertEquals(ID, groundType.id());
        assertEquals(NAME, groundType.getName());
        assertSame(mockSprite, groundType.imageAsset());
        assertEquals(new ArrayList<ColorShift>() {{
            add(COLOR_SHIFT);
        }}, groundType.defaultColorShifts());
        verify(COLOR_SHIFT_HANDLER, times(1)).read(COLOR_SHIFT_WRITTEN_VALUE);
        verify(mockOnStepFunction, times(1)).apply(mockCharacter1);
        verify(mockCanStepFunction, times(1)).apply(mockCharacter2);
    }

    @Test
    void testMakeWithLoopingAnimation() {
        GroundTypeDefinition definition =
                new GroundTypeDefinition(ID, NAME, 3, GLOBAL_LOOPING_ANIMATION_ID,
                        ON_STEP_FUNCTION_ID, CAN_STEP_FUNCTION_ID,
                        new String[]{COLOR_SHIFT_WRITTEN_VALUE});

        GroundType groundType = groundTypeFactory.make(definition);
        groundType.onStep(mockCharacter1);
        groundType.canStep(mockCharacter2);

        assertNotNull(definition);
        assertEquals(GroundType.class.getCanonicalName(), groundType.getInterfaceName());
        assertEquals(ID, groundType.id());
        assertEquals(NAME, groundType.getName());
        assertSame(mockLoopingAnimation, groundType.imageAsset());
        assertEquals(new ArrayList<ColorShift>() {{
            add(COLOR_SHIFT);
        }}, groundType.defaultColorShifts());
        verify(COLOR_SHIFT_HANDLER, times(1)).read(COLOR_SHIFT_WRITTEN_VALUE);
        verify(mockOnStepFunction, times(1)).apply(mockCharacter1);
        verify(mockCanStepFunction, times(1)).apply(mockCharacter2);
    }

    @Test
    void testMakeWithInvalidParams() {
        assertThrows(IllegalArgumentException.class, () -> groundTypeFactory.make(null));
        assertThrows(IllegalArgumentException.class, () -> groundTypeFactory.make(
                new GroundTypeDefinition(null, NAME, 1, SPRITE_ID, ON_STEP_FUNCTION_ID,
                        CAN_STEP_FUNCTION_ID, null)));
        assertThrows(IllegalArgumentException.class, () -> groundTypeFactory.make(
                new GroundTypeDefinition("", NAME, 1, SPRITE_ID, ON_STEP_FUNCTION_ID,
                        CAN_STEP_FUNCTION_ID, null)));
        assertThrows(IllegalArgumentException.class, () -> groundTypeFactory.make(
                new GroundTypeDefinition(ID, null, 1, SPRITE_ID, ON_STEP_FUNCTION_ID,
                        CAN_STEP_FUNCTION_ID, null)));
        assertThrows(IllegalArgumentException.class, () -> groundTypeFactory.make(
                new GroundTypeDefinition(ID, "", 1, SPRITE_ID, ON_STEP_FUNCTION_ID,
                        CAN_STEP_FUNCTION_ID, null)));
        assertThrows(IllegalArgumentException.class, () -> groundTypeFactory.make(
                new GroundTypeDefinition(ID, NAME, 0, SPRITE_ID, ON_STEP_FUNCTION_ID,
                        CAN_STEP_FUNCTION_ID, null)));
        assertThrows(IllegalArgumentException.class, () -> groundTypeFactory.make(
                new GroundTypeDefinition(ID, NAME, 2, SPRITE_ID, ON_STEP_FUNCTION_ID,
                        CAN_STEP_FUNCTION_ID, null)));
        assertThrows(IllegalArgumentException.class, () -> groundTypeFactory.make(
                new GroundTypeDefinition(ID, NAME, 4, SPRITE_ID, ON_STEP_FUNCTION_ID,
                        CAN_STEP_FUNCTION_ID, null)));
        assertThrows(IllegalArgumentException.class, () -> groundTypeFactory.make(
                new GroundTypeDefinition(ID, NAME, 1, null, ON_STEP_FUNCTION_ID,
                        CAN_STEP_FUNCTION_ID, null)));
        assertThrows(IllegalArgumentException.class, () -> groundTypeFactory.make(
                new GroundTypeDefinition(ID, NAME, 1, "", ON_STEP_FUNCTION_ID, CAN_STEP_FUNCTION_ID,
                        null)));
        assertThrows(IllegalArgumentException.class, () -> groundTypeFactory.make(
                new GroundTypeDefinition(ID, NAME, 1, "invalidId", ON_STEP_FUNCTION_ID,
                        CAN_STEP_FUNCTION_ID, null)));
        assertThrows(IllegalArgumentException.class, () -> groundTypeFactory.make(
                new GroundTypeDefinition(ID, NAME, 3, "invalidId", ON_STEP_FUNCTION_ID,
                        CAN_STEP_FUNCTION_ID, null)));
        assertThrows(IllegalArgumentException.class, () -> groundTypeFactory.make(
                new GroundTypeDefinition(ID, NAME, 1, SPRITE_ID, null, CAN_STEP_FUNCTION_ID,
                        null)));
        assertThrows(IllegalArgumentException.class, () -> groundTypeFactory.make(
                new GroundTypeDefinition(ID, NAME, 1, SPRITE_ID, "", CAN_STEP_FUNCTION_ID, null)));
        assertThrows(IllegalArgumentException.class, () -> groundTypeFactory.make(
                new GroundTypeDefinition(ID, NAME, 1, SPRITE_ID, "invalidId", CAN_STEP_FUNCTION_ID,
                        null)));
        assertThrows(IllegalArgumentException.class, () -> groundTypeFactory.make(
                new GroundTypeDefinition(ID, NAME, 1, SPRITE_ID, ON_STEP_FUNCTION_ID, null, null)));
        assertThrows(IllegalArgumentException.class, () -> groundTypeFactory.make(
                new GroundTypeDefinition(ID, NAME, 1, SPRITE_ID, ON_STEP_FUNCTION_ID, "", null)));
        assertThrows(IllegalArgumentException.class, () -> groundTypeFactory.make(
                new GroundTypeDefinition(ID, NAME, 1, SPRITE_ID, ON_STEP_FUNCTION_ID, "invalidId",
                        null)));
    }

    @Test
    void testSetName() {
        GroundTypeDefinition definition =
                new GroundTypeDefinition(ID, NAME, 1, SPRITE_ID,
                        ON_STEP_FUNCTION_ID, CAN_STEP_FUNCTION_ID,
                        new String[]{COLOR_SHIFT_WRITTEN_VALUE});
        String newName = randomString();
        GroundType groundType = groundTypeFactory.make(definition);

        groundType.setName(newName);

        assertEquals(newName, groundType.getName());

    }

    @Test
    void testSetNameWithInvalidParams() {
        GroundTypeDefinition definition =
                new GroundTypeDefinition(ID, NAME, 1, SPRITE_ID,
                        ON_STEP_FUNCTION_ID, CAN_STEP_FUNCTION_ID,
                        new String[]{COLOR_SHIFT_WRITTEN_VALUE});
        GroundType groundType = groundTypeFactory.make(definition);

        assertThrows(IllegalArgumentException.class, () -> groundType.setName(null));
        assertThrows(IllegalArgumentException.class, () -> groundType.setName(""));
    }

    @Test
    void testStepFunctionsWithInvalidParams() {
        GroundTypeDefinition definition =
                new GroundTypeDefinition(ID, NAME, 1, SPRITE_ID,
                        ON_STEP_FUNCTION_ID, CAN_STEP_FUNCTION_ID,
                        new String[]{COLOR_SHIFT_WRITTEN_VALUE});
        GroundType groundType = groundTypeFactory.make(definition);

        assertThrows(IllegalArgumentException.class, () -> groundType.onStep(null));
        assertThrows(IllegalArgumentException.class, () -> groundType.canStep(null));
    }

    @Test
    void testGetInterfaceName() {
        assertEquals(Factory.class.getCanonicalName() + "<" +
                GroundTypeDefinition.class.getCanonicalName() + "," +
                GroundType.class.getCanonicalName() + ">", groundTypeFactory.getInterfaceName());
    }
}
