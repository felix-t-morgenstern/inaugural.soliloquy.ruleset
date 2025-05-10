package inaugural.soliloquy.ruleset.entities.factories;

import inaugural.soliloquy.ruleset.definitions.GroundTypeDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import soliloquy.specs.common.factories.Factory;
import soliloquy.specs.common.persistence.TypeHandler;
import soliloquy.specs.common.shared.Direction;
import soliloquy.specs.gamestate.entities.Character;
import soliloquy.specs.gamestate.entities.Tile;
import soliloquy.specs.graphics.assets.GlobalLoopingAnimation;
import soliloquy.specs.graphics.assets.Sprite;
import soliloquy.specs.graphics.renderables.colorshifting.ColorShift;
import soliloquy.specs.ruleset.entities.GroundType;

import java.util.Map;
import java.util.function.Function;

import static inaugural.soliloquy.tools.collections.Collections.listOf;
import static inaugural.soliloquy.tools.collections.Collections.mapOf;
import static inaugural.soliloquy.tools.random.Random.*;
import static inaugural.soliloquy.tools.testing.Mock.HandlerAndEntity;
import static inaugural.soliloquy.tools.testing.Mock.generateMockEntityAndHandler;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static soliloquy.specs.common.shared.Direction.SOUTHWEST;

class GroundTypeFactoryTests {
    private final String ID = randomString();
    private final String NAME = randomString();
    private final int ADDITIONAL_MOVEMENT_COST = randomInt();
    private final Direction ESCALATION_DIRECTION = randomDirection();
    private final int ESCALATION = randomInt();
    private final GroundTypeDefinition.Escalation[] ESCALATIONS =
            {new GroundTypeDefinition.Escalation(ESCALATION_DIRECTION, ESCALATION)};
    private final int HEIGHT_MOVEMENT_PENALTY_MITIGATION = randomInt();

    private final String SPRITE_ID = randomString();
    private final String GLOBAL_LOOPING_ANIMATION_ID = randomString();
    private final Map<String, Sprite> SPRITES = mapOf();
    private final Map<String, GlobalLoopingAnimation> GLOBAL_LOOPING_ANIMATIONS = mapOf();
    private final String ON_STEP_FUNCTION_ID = randomString();
    private final String CAN_STEP_FUNCTION_ID = randomString();
    private final String HEIGHT_MOVEMENT_PENALTY_MITIGATION_FUNCTION_ID = randomString();
    private final boolean BLOCKS_SIGHT = randomBoolean();
    private final Direction DIRECTION = SOUTHWEST;
    @SuppressWarnings("rawtypes")
    private final Map<String, Function> FUNCTIONS = mapOf();

    @Mock private Sprite mockSprite;
    @Mock private GlobalLoopingAnimation mockLoopingAnimation;
    @Mock private Function<Character, Boolean> mockOnStepFunction;
    @Mock private Function<Character, Boolean> mockCanStepFunction;
    @Mock private Function<Object[], Integer> mockHeightMovementPenaltyMitigationFunction;
    @Mock Character mockCharacter1;
    @Mock Character mockCharacter2;
    @Mock Tile mockTile;

    private final String COLOR_SHIFT_WRITTEN_VALUE = randomString();

    private final HandlerAndEntity<ColorShift>
            COLOR_SHIFT_ENTITY_AND_HANDLER =
            generateMockEntityAndHandler(ColorShift.class, COLOR_SHIFT_WRITTEN_VALUE);
    private final ColorShift COLOR_SHIFT = COLOR_SHIFT_ENTITY_AND_HANDLER.entity;
    private final TypeHandler<ColorShift> COLOR_SHIFT_HANDLER =
            COLOR_SHIFT_ENTITY_AND_HANDLER.handler;

    private GroundTypeDefinition definition;

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
        //noinspection unchecked
        mockHeightMovementPenaltyMitigationFunction =
                (Function<Object[], Integer>) mock(Function.class);
        when(mockHeightMovementPenaltyMitigationFunction.apply(any()))
                .thenReturn(HEIGHT_MOVEMENT_PENALTY_MITIGATION);
        FUNCTIONS.put(ON_STEP_FUNCTION_ID, mockOnStepFunction);
        FUNCTIONS.put(CAN_STEP_FUNCTION_ID, mockCanStepFunction);
        FUNCTIONS.put(HEIGHT_MOVEMENT_PENALTY_MITIGATION_FUNCTION_ID,
                mockHeightMovementPenaltyMitigationFunction);

        mockCharacter1 = mock(Character.class);
        mockCharacter2 = mock(Character.class);

        mockTile = mock(Tile.class);

        definition =
                new GroundTypeDefinition(ID, NAME, 1, SPRITE_ID, ON_STEP_FUNCTION_ID,
                        CAN_STEP_FUNCTION_ID, ADDITIONAL_MOVEMENT_COST, ESCALATIONS,
                        HEIGHT_MOVEMENT_PENALTY_MITIGATION_FUNCTION_ID, BLOCKS_SIGHT,
                        new String[]{COLOR_SHIFT_WRITTEN_VALUE});

        groundTypeFactory = new GroundTypeFactory(COLOR_SHIFT_HANDLER, FUNCTIONS::get, SPRITES::get,
                GLOBAL_LOOPING_ANIMATIONS::get);
    }

    @Test
    void testConstructorWithInvalidArgs() {
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
        var definitionWithSprite =
                new GroundTypeDefinition(ID, NAME, 1, SPRITE_ID, ON_STEP_FUNCTION_ID,
                        CAN_STEP_FUNCTION_ID, ADDITIONAL_MOVEMENT_COST, ESCALATIONS,
                        HEIGHT_MOVEMENT_PENALTY_MITIGATION_FUNCTION_ID, BLOCKS_SIGHT,
                        new String[]{COLOR_SHIFT_WRITTEN_VALUE});

        var output = groundTypeFactory.make(definitionWithSprite);
        output.onStep(mockCharacter1);
        output.canStep(mockCharacter2);
        var additionalMovementCost = output.additionalMovementCost();
        var heightMovementPenaltyMitigation =
                output.heightMovementPenaltyMitigation(mockTile, mockCharacter1, DIRECTION);

        assertNotNull(definitionWithSprite);
        assertEquals(GroundType.class.getCanonicalName(), output.getInterfaceName());
        assertEquals(ID, output.id());
        assertEquals(NAME, output.getName());
        assertSame(mockSprite, output.imageAsset());
        assertEquals(listOf(COLOR_SHIFT), output.defaultColorShifts());
        assertEquals(ADDITIONAL_MOVEMENT_COST, additionalMovementCost);
        for (var direction : Direction.values()) {
            if (direction == ESCALATION_DIRECTION) {
                assertEquals(ESCALATION, output.escalation(direction));
            }
            else {
                assertEquals(0, output.escalation(direction));
            }
        }
        assertEquals(HEIGHT_MOVEMENT_PENALTY_MITIGATION, heightMovementPenaltyMitigation);
        assertEquals(BLOCKS_SIGHT, output.blocksSight());
        verify(COLOR_SHIFT_HANDLER).read(COLOR_SHIFT_WRITTEN_VALUE);
        verify(mockOnStepFunction).apply(mockCharacter1);
        verify(mockCanStepFunction).apply(mockCharacter2);
        verify(mockHeightMovementPenaltyMitigationFunction).apply(
                eq(new Object[]{mockTile, mockCharacter1, SOUTHWEST}));
    }

    @Test
    void testMakeWithLoopingAnimation() {
        var definitionWithLoopingAnimation =
                new GroundTypeDefinition(ID, NAME, 3, GLOBAL_LOOPING_ANIMATION_ID,
                        ON_STEP_FUNCTION_ID, CAN_STEP_FUNCTION_ID, ADDITIONAL_MOVEMENT_COST,
                        ESCALATIONS, HEIGHT_MOVEMENT_PENALTY_MITIGATION_FUNCTION_ID, BLOCKS_SIGHT,
                        new String[]{COLOR_SHIFT_WRITTEN_VALUE});

        var output = groundTypeFactory.make(definitionWithLoopingAnimation);
        output.onStep(mockCharacter1);
        output.canStep(mockCharacter2);
        var additionalMovementCost = output.additionalMovementCost();
        var heightMovementPenaltyMitigation =
                output.heightMovementPenaltyMitigation(mockTile, mockCharacter1, DIRECTION);

        assertNotNull(definitionWithLoopingAnimation);
        assertEquals(GroundType.class.getCanonicalName(), output.getInterfaceName());
        assertEquals(ID, output.id());
        assertEquals(NAME, output.getName());
        assertSame(mockLoopingAnimation, output.imageAsset());
        assertEquals(listOf(COLOR_SHIFT), output.defaultColorShifts());
        assertEquals(ADDITIONAL_MOVEMENT_COST, additionalMovementCost);
        for (var direction : Direction.values()) {
            if (direction == ESCALATION_DIRECTION) {
                assertEquals(ESCALATION, output.escalation(direction));
            }
            else {
                assertEquals(0, output.escalation(direction));
            }
        }
        assertEquals(HEIGHT_MOVEMENT_PENALTY_MITIGATION, heightMovementPenaltyMitigation);
        assertEquals(BLOCKS_SIGHT, output.blocksSight());
        verify(COLOR_SHIFT_HANDLER).read(COLOR_SHIFT_WRITTEN_VALUE);
        verify(mockOnStepFunction).apply(mockCharacter1);
        verify(mockCanStepFunction).apply(mockCharacter2);
        verify(mockHeightMovementPenaltyMitigationFunction).apply(
                eq(new Object[]{mockTile, mockCharacter1, SOUTHWEST}));
    }

    @Test
    void testMakeWithInvalidArgs() {
        var invalidFunctionId = randomString();

        assertThrows(IllegalArgumentException.class, () -> groundTypeFactory.make(null));
        assertThrows(IllegalArgumentException.class, () -> groundTypeFactory.make(
                new GroundTypeDefinition(null, NAME, 1, SPRITE_ID, ON_STEP_FUNCTION_ID,
                        CAN_STEP_FUNCTION_ID, ADDITIONAL_MOVEMENT_COST, ESCALATIONS,
                        HEIGHT_MOVEMENT_PENALTY_MITIGATION_FUNCTION_ID, BLOCKS_SIGHT, null)));
        assertThrows(IllegalArgumentException.class, () -> groundTypeFactory.make(
                new GroundTypeDefinition("", NAME, 1, SPRITE_ID, ON_STEP_FUNCTION_ID,
                        CAN_STEP_FUNCTION_ID, ADDITIONAL_MOVEMENT_COST, ESCALATIONS,
                        HEIGHT_MOVEMENT_PENALTY_MITIGATION_FUNCTION_ID, BLOCKS_SIGHT, null)));
        assertThrows(IllegalArgumentException.class, () -> groundTypeFactory.make(
                new GroundTypeDefinition(ID, null, 1, SPRITE_ID, ON_STEP_FUNCTION_ID,
                        CAN_STEP_FUNCTION_ID, ADDITIONAL_MOVEMENT_COST, ESCALATIONS,
                        HEIGHT_MOVEMENT_PENALTY_MITIGATION_FUNCTION_ID, BLOCKS_SIGHT, null)));
        assertThrows(IllegalArgumentException.class, () -> groundTypeFactory.make(
                new GroundTypeDefinition(ID, "", 1, SPRITE_ID, ON_STEP_FUNCTION_ID,
                        CAN_STEP_FUNCTION_ID, ADDITIONAL_MOVEMENT_COST, ESCALATIONS,
                        HEIGHT_MOVEMENT_PENALTY_MITIGATION_FUNCTION_ID, BLOCKS_SIGHT, null)));
        assertThrows(IllegalArgumentException.class, () -> groundTypeFactory.make(
                new GroundTypeDefinition(ID, NAME, 0, SPRITE_ID, ON_STEP_FUNCTION_ID,
                        CAN_STEP_FUNCTION_ID, ADDITIONAL_MOVEMENT_COST, ESCALATIONS,
                        HEIGHT_MOVEMENT_PENALTY_MITIGATION_FUNCTION_ID, BLOCKS_SIGHT, null)));
        assertThrows(IllegalArgumentException.class, () -> groundTypeFactory.make(
                new GroundTypeDefinition(ID, NAME, 2, SPRITE_ID, ON_STEP_FUNCTION_ID,
                        CAN_STEP_FUNCTION_ID, ADDITIONAL_MOVEMENT_COST, ESCALATIONS,
                        HEIGHT_MOVEMENT_PENALTY_MITIGATION_FUNCTION_ID, BLOCKS_SIGHT, null)));
        assertThrows(IllegalArgumentException.class, () -> groundTypeFactory.make(
                new GroundTypeDefinition(ID, NAME, 4, SPRITE_ID, ON_STEP_FUNCTION_ID,
                        CAN_STEP_FUNCTION_ID, ADDITIONAL_MOVEMENT_COST, ESCALATIONS,
                        HEIGHT_MOVEMENT_PENALTY_MITIGATION_FUNCTION_ID, BLOCKS_SIGHT, null)));
        assertThrows(IllegalArgumentException.class, () -> groundTypeFactory.make(
                new GroundTypeDefinition(ID, NAME, 1, null, ON_STEP_FUNCTION_ID,
                        CAN_STEP_FUNCTION_ID, ADDITIONAL_MOVEMENT_COST, ESCALATIONS,
                        HEIGHT_MOVEMENT_PENALTY_MITIGATION_FUNCTION_ID, BLOCKS_SIGHT, null)));
        assertThrows(IllegalArgumentException.class, () -> groundTypeFactory.make(
                new GroundTypeDefinition(ID, NAME, 1, "", ON_STEP_FUNCTION_ID, CAN_STEP_FUNCTION_ID,
                        ADDITIONAL_MOVEMENT_COST, ESCALATIONS,
                        HEIGHT_MOVEMENT_PENALTY_MITIGATION_FUNCTION_ID, BLOCKS_SIGHT, null)));
        assertThrows(IllegalArgumentException.class, () -> groundTypeFactory.make(
                new GroundTypeDefinition(ID, NAME, 1, invalidFunctionId, ON_STEP_FUNCTION_ID,
                        CAN_STEP_FUNCTION_ID, ADDITIONAL_MOVEMENT_COST, ESCALATIONS,
                        HEIGHT_MOVEMENT_PENALTY_MITIGATION_FUNCTION_ID, BLOCKS_SIGHT, null)));
        assertThrows(IllegalArgumentException.class, () -> groundTypeFactory.make(
                new GroundTypeDefinition(ID, NAME, 3, invalidFunctionId, ON_STEP_FUNCTION_ID,
                        CAN_STEP_FUNCTION_ID, ADDITIONAL_MOVEMENT_COST, ESCALATIONS,
                        HEIGHT_MOVEMENT_PENALTY_MITIGATION_FUNCTION_ID, BLOCKS_SIGHT, null)));
        assertThrows(IllegalArgumentException.class, () -> groundTypeFactory.make(
                new GroundTypeDefinition(ID, NAME, 1, SPRITE_ID, null, CAN_STEP_FUNCTION_ID,
                        ADDITIONAL_MOVEMENT_COST, ESCALATIONS,
                        HEIGHT_MOVEMENT_PENALTY_MITIGATION_FUNCTION_ID, BLOCKS_SIGHT, null)));
        assertThrows(IllegalArgumentException.class, () -> groundTypeFactory.make(
                new GroundTypeDefinition(ID, NAME, 1, SPRITE_ID, "", CAN_STEP_FUNCTION_ID,
                        ADDITIONAL_MOVEMENT_COST, ESCALATIONS,
                        HEIGHT_MOVEMENT_PENALTY_MITIGATION_FUNCTION_ID, BLOCKS_SIGHT, null)));
        assertThrows(IllegalArgumentException.class, () -> groundTypeFactory.make(
                new GroundTypeDefinition(ID, NAME, 1, SPRITE_ID, invalidFunctionId,
                        CAN_STEP_FUNCTION_ID, ADDITIONAL_MOVEMENT_COST, ESCALATIONS,
                        HEIGHT_MOVEMENT_PENALTY_MITIGATION_FUNCTION_ID, BLOCKS_SIGHT, null)));
        assertThrows(IllegalArgumentException.class, () -> groundTypeFactory.make(
                new GroundTypeDefinition(ID, NAME, 1, SPRITE_ID, ON_STEP_FUNCTION_ID, null,
                        ADDITIONAL_MOVEMENT_COST, ESCALATIONS,
                        HEIGHT_MOVEMENT_PENALTY_MITIGATION_FUNCTION_ID, BLOCKS_SIGHT, null)));
        assertThrows(IllegalArgumentException.class, () -> groundTypeFactory.make(
                new GroundTypeDefinition(ID, NAME, 1, SPRITE_ID, ON_STEP_FUNCTION_ID, "",
                        ADDITIONAL_MOVEMENT_COST, ESCALATIONS,
                        HEIGHT_MOVEMENT_PENALTY_MITIGATION_FUNCTION_ID, BLOCKS_SIGHT, null)));
        assertThrows(IllegalArgumentException.class, () -> groundTypeFactory.make(
                new GroundTypeDefinition(ID, NAME, 1, SPRITE_ID, ON_STEP_FUNCTION_ID,
                        invalidFunctionId, ADDITIONAL_MOVEMENT_COST, ESCALATIONS,
                        HEIGHT_MOVEMENT_PENALTY_MITIGATION_FUNCTION_ID, BLOCKS_SIGHT, null)));
        assertThrows(IllegalArgumentException.class, () -> groundTypeFactory.make(
                new GroundTypeDefinition(ID, NAME, 1, SPRITE_ID, ON_STEP_FUNCTION_ID,
                        CAN_STEP_FUNCTION_ID, ADDITIONAL_MOVEMENT_COST, ESCALATIONS, null,
                        BLOCKS_SIGHT, null)));
        assertThrows(IllegalArgumentException.class, () -> groundTypeFactory.make(
                new GroundTypeDefinition(ID, NAME, 1, SPRITE_ID, ON_STEP_FUNCTION_ID,
                        CAN_STEP_FUNCTION_ID, ADDITIONAL_MOVEMENT_COST, ESCALATIONS, "",
                        BLOCKS_SIGHT, null)));
        assertThrows(IllegalArgumentException.class, () -> groundTypeFactory.make(
                new GroundTypeDefinition(ID, NAME, 1, SPRITE_ID, ON_STEP_FUNCTION_ID,
                        CAN_STEP_FUNCTION_ID, ADDITIONAL_MOVEMENT_COST, ESCALATIONS,
                        invalidFunctionId, BLOCKS_SIGHT, null)));
    }

    @Test
    void testSetName() {
        var newName = randomString();
        var output = groundTypeFactory.make(definition);

        output.setName(newName);

        assertEquals(newName, output.getName());

    }

    @Test
    void testSetNameWithInvalidArgs() {
        var output = groundTypeFactory.make(definition);

        assertThrows(IllegalArgumentException.class, () -> output.setName(null));
        assertThrows(IllegalArgumentException.class, () -> output.setName(""));
    }

    @Test
    void testStepFunctionsWithInvalidArgs() {
        var output = groundTypeFactory.make(definition);

        assertThrows(IllegalArgumentException.class, () -> output.onStep(null));
        assertThrows(IllegalArgumentException.class, () -> output.canStep(null));
    }

    @Test
    void testEscalationWithInvalidArgs() {
        var output = groundTypeFactory.make(definition);

        assertThrows(IllegalArgumentException.class, () -> output.escalation(null));
    }

    @Test
    void testGetInterfaceName() {
        assertEquals(Factory.class.getCanonicalName() + "<" +
                GroundTypeDefinition.class.getCanonicalName() + "," +
                GroundType.class.getCanonicalName() + ">", groundTypeFactory.getInterfaceName());
    }
}
