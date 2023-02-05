package inaugural.soliloquy.ruleset.entities.factories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import soliloquy.specs.common.factories.Factory;
import soliloquy.specs.common.persistence.TypeHandler;
import soliloquy.specs.common.shared.Direction;
import soliloquy.specs.common.valueobjects.Vertex;
import soliloquy.specs.gamestate.entities.Character;
import soliloquy.specs.gamestate.entities.Tile;
import soliloquy.specs.graphics.assets.GlobalLoopingAnimation;
import soliloquy.specs.graphics.assets.ImageAsset;
import soliloquy.specs.graphics.assets.Sprite;
import soliloquy.specs.graphics.renderables.colorshifting.ColorShift;
import inaugural.soliloquy.ruleset.definitions.FixtureTypeDefinition;
import soliloquy.specs.ruleset.entities.FixtureType;

import java.util.Map;
import java.util.function.Function;

import static inaugural.soliloquy.tools.collections.Collections.listOf;
import static inaugural.soliloquy.tools.collections.Collections.mapOf;
import static inaugural.soliloquy.tools.random.Random.*;
import static inaugural.soliloquy.tools.testing.Mock.HandlerAndEntity;
import static inaugural.soliloquy.tools.testing.Mock.generateMockEntityAndHandler;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static soliloquy.specs.common.shared.Direction.SOUTHWEST;

class FixtureTypeFactoryTests {
    private final String ID = randomString();
    private final String NAME = randomString();
    private final boolean IS_CONTAINER = randomBoolean();
    private final float DEFAULT_X_TILE_WIDTH_OFFSET = randomFloat();
    private final float DEFAULT_Y_TILE_HEIGHT_OFFSET = randomFloat();
    private final int ADDITIONAL_MOVEMENT_COST = randomInt();
    private final int HEIGHT_MOVEMENT_PENALTY_MITIGATION = randomInt();

    private final String SPRITE_ID = randomString();
    private final String GLOBAL_LOOPING_ANIMATION_ID = randomString();
    private final Map<String, Sprite> SPRITES = mapOf();
    private final Map<String, GlobalLoopingAnimation> GLOBAL_LOOPING_ANIMATIONS = mapOf();
    private final String ON_STEP_FUNCTION_ID = randomString();
    private final String CAN_STEP_FUNCTION_ID = randomString();
    private final String HEIGHT_MOVEMENT_PENALTY_MITIGATION_FUNCTION_ID = randomString();
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

    private FixtureTypeDefinition definition;

    private Factory<FixtureTypeDefinition, FixtureType> fixtureTypeFactory;

    @BeforeEach
    void setUp() {
        mockSprite = mock(Sprite.class);
        SPRITES.put(SPRITE_ID, mockSprite);
        mockLoopingAnimation = mock(GlobalLoopingAnimation.class);
        GLOBAL_LOOPING_ANIMATIONS.put(GLOBAL_LOOPING_ANIMATION_ID, mockLoopingAnimation);
        //noinspection unchecked
        mockOnStepFunction = (Function<Character, Boolean>) mock(Function.class);
        when(mockOnStepFunction.apply(any())).thenReturn(true);
        //noinspection unchecked
        mockCanStepFunction = (Function<Character, Boolean>) mock(Function.class);
        when(mockCanStepFunction.apply(any())).thenReturn(true);
        //noinspection unchecked
        mockHeightMovementPenaltyMitigationFunction = (Function<Object[], Integer>) mock(Function.class);
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
                new FixtureTypeDefinition(ID, NAME, SPRITE_ID, ImageAsset.ImageAssetType.SPRITE,
                        IS_CONTAINER, ON_STEP_FUNCTION_ID, CAN_STEP_FUNCTION_ID,
                        ADDITIONAL_MOVEMENT_COST, HEIGHT_MOVEMENT_PENALTY_MITIGATION_FUNCTION_ID,
                        new String[]{COLOR_SHIFT_WRITTEN_VALUE}, DEFAULT_X_TILE_WIDTH_OFFSET,
                        DEFAULT_Y_TILE_HEIGHT_OFFSET);

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
        var definitionWithSprite =
                new FixtureTypeDefinition(ID, NAME, SPRITE_ID, ImageAsset.ImageAssetType.SPRITE,
                        IS_CONTAINER, ON_STEP_FUNCTION_ID, CAN_STEP_FUNCTION_ID,
                        ADDITIONAL_MOVEMENT_COST, HEIGHT_MOVEMENT_PENALTY_MITIGATION_FUNCTION_ID,
                        new String[]{COLOR_SHIFT_WRITTEN_VALUE}, DEFAULT_X_TILE_WIDTH_OFFSET,
                        DEFAULT_Y_TILE_HEIGHT_OFFSET);

        var output = fixtureTypeFactory.make(definitionWithSprite);
        output.onStep(mockCharacter1);
        output.canStep(mockCharacter2);
        var additionalMovementCost = output.additionalMovementCost();
        var heightMovementPenaltyMitigation =
                output.heightMovementPenaltyMitigation(mockTile, mockCharacter1, DIRECTION);

        assertNotNull(definitionWithSprite);
        assertEquals(FixtureType.class.getCanonicalName(), output.getInterfaceName());
        assertEquals(ID, output.id());
        assertEquals(NAME, output.getName());
        assertSame(mockSprite, output.imageAsset());
        assertEquals(IS_CONTAINER, output.isContainer());
        assertEquals(listOf(COLOR_SHIFT), output.defaultColorShifts());
        assertEquals(Vertex.of(DEFAULT_X_TILE_WIDTH_OFFSET, DEFAULT_Y_TILE_HEIGHT_OFFSET),
                output.defaultTileOffset());
        assertEquals(ADDITIONAL_MOVEMENT_COST, additionalMovementCost);
        assertEquals(HEIGHT_MOVEMENT_PENALTY_MITIGATION, heightMovementPenaltyMitigation);
        verify(COLOR_SHIFT_HANDLER).read(COLOR_SHIFT_WRITTEN_VALUE);
        verify(mockOnStepFunction).apply(mockCharacter1);
        verify(mockCanStepFunction).apply(mockCharacter2);
        verify(mockHeightMovementPenaltyMitigationFunction).apply(
                eq(new Object[]{mockTile, mockCharacter1, SOUTHWEST}));
    }

    @Test
    void testMakeWithLoopingAnimation() {
        FixtureTypeDefinition definitionWithLoopingAnimation =
                new FixtureTypeDefinition(ID, NAME, GLOBAL_LOOPING_ANIMATION_ID,
                        ImageAsset.ImageAssetType.GLOBAL_LOOPING_ANIMATION, IS_CONTAINER,
                        ON_STEP_FUNCTION_ID, CAN_STEP_FUNCTION_ID,
                        ADDITIONAL_MOVEMENT_COST, HEIGHT_MOVEMENT_PENALTY_MITIGATION_FUNCTION_ID,
                        new String[]{COLOR_SHIFT_WRITTEN_VALUE}, DEFAULT_X_TILE_WIDTH_OFFSET,
                        DEFAULT_Y_TILE_HEIGHT_OFFSET);

        var output = fixtureTypeFactory.make(definitionWithLoopingAnimation);
        output.onStep(mockCharacter1);
        output.canStep(mockCharacter2);
        var additionalMovementCost = output.additionalMovementCost();
        var heightMovementPenaltyMitigation =
                output.heightMovementPenaltyMitigation(mockTile, mockCharacter1, DIRECTION);

        assertNotNull(definitionWithLoopingAnimation);
        assertEquals(FixtureType.class.getCanonicalName(), output.getInterfaceName());
        assertEquals(ID, output.id());
        assertEquals(NAME, output.getName());
        assertSame(mockLoopingAnimation, output.imageAsset());
        assertEquals(IS_CONTAINER, output.isContainer());
        assertEquals(listOf(COLOR_SHIFT), output.defaultColorShifts());
        assertEquals(Vertex.of(DEFAULT_X_TILE_WIDTH_OFFSET, DEFAULT_Y_TILE_HEIGHT_OFFSET),
                output.defaultTileOffset());
        assertEquals(ADDITIONAL_MOVEMENT_COST, additionalMovementCost);
        assertEquals(HEIGHT_MOVEMENT_PENALTY_MITIGATION, heightMovementPenaltyMitigation);
        verify(COLOR_SHIFT_HANDLER).read(COLOR_SHIFT_WRITTEN_VALUE);
        verify(mockOnStepFunction).apply(mockCharacter1);
        verify(mockCanStepFunction).apply(mockCharacter2);
        verify(mockHeightMovementPenaltyMitigationFunction).apply(
                eq(new Object[]{mockTile, mockCharacter1, SOUTHWEST}));
    }

    @Test
    void testMakeWithInvalidParams() {
        var invalidFunctionId = randomString();

        assertThrows(IllegalArgumentException.class, () -> fixtureTypeFactory.make(null));
        assertThrows(IllegalArgumentException.class, () -> fixtureTypeFactory.make(
                new FixtureTypeDefinition(null, NAME, SPRITE_ID, ImageAsset.ImageAssetType.SPRITE,
                        IS_CONTAINER, ON_STEP_FUNCTION_ID, CAN_STEP_FUNCTION_ID,
                        ADDITIONAL_MOVEMENT_COST, HEIGHT_MOVEMENT_PENALTY_MITIGATION_FUNCTION_ID,
                        new String[]{COLOR_SHIFT_WRITTEN_VALUE}, DEFAULT_X_TILE_WIDTH_OFFSET,
                        DEFAULT_Y_TILE_HEIGHT_OFFSET)));
        assertThrows(IllegalArgumentException.class, () -> fixtureTypeFactory.make(
                new FixtureTypeDefinition("", NAME, SPRITE_ID, ImageAsset.ImageAssetType.SPRITE,
                        IS_CONTAINER, ON_STEP_FUNCTION_ID, CAN_STEP_FUNCTION_ID,
                        ADDITIONAL_MOVEMENT_COST, HEIGHT_MOVEMENT_PENALTY_MITIGATION_FUNCTION_ID,
                        new String[]{COLOR_SHIFT_WRITTEN_VALUE}, DEFAULT_X_TILE_WIDTH_OFFSET,
                        DEFAULT_Y_TILE_HEIGHT_OFFSET)));
        assertThrows(IllegalArgumentException.class, () -> fixtureTypeFactory.make(
                new FixtureTypeDefinition(ID, null, SPRITE_ID, ImageAsset.ImageAssetType.SPRITE,
                        IS_CONTAINER, ON_STEP_FUNCTION_ID, CAN_STEP_FUNCTION_ID,
                        ADDITIONAL_MOVEMENT_COST, HEIGHT_MOVEMENT_PENALTY_MITIGATION_FUNCTION_ID,
                        new String[]{COLOR_SHIFT_WRITTEN_VALUE}, DEFAULT_X_TILE_WIDTH_OFFSET,
                        DEFAULT_Y_TILE_HEIGHT_OFFSET)));
        assertThrows(IllegalArgumentException.class, () -> fixtureTypeFactory.make(
                new FixtureTypeDefinition(ID, "", SPRITE_ID, ImageAsset.ImageAssetType.SPRITE,
                        IS_CONTAINER, ON_STEP_FUNCTION_ID, CAN_STEP_FUNCTION_ID,
                        ADDITIONAL_MOVEMENT_COST, HEIGHT_MOVEMENT_PENALTY_MITIGATION_FUNCTION_ID,
                        new String[]{COLOR_SHIFT_WRITTEN_VALUE}, DEFAULT_X_TILE_WIDTH_OFFSET,
                        DEFAULT_Y_TILE_HEIGHT_OFFSET)));
        assertThrows(IllegalArgumentException.class, () -> fixtureTypeFactory.make(
                new FixtureTypeDefinition(ID, NAME, null, ImageAsset.ImageAssetType.SPRITE,
                        IS_CONTAINER, ON_STEP_FUNCTION_ID, CAN_STEP_FUNCTION_ID,
                        ADDITIONAL_MOVEMENT_COST, HEIGHT_MOVEMENT_PENALTY_MITIGATION_FUNCTION_ID,
                        new String[]{COLOR_SHIFT_WRITTEN_VALUE}, DEFAULT_X_TILE_WIDTH_OFFSET,
                        DEFAULT_Y_TILE_HEIGHT_OFFSET)));
        assertThrows(IllegalArgumentException.class, () -> fixtureTypeFactory.make(
                new FixtureTypeDefinition(ID, NAME, "", ImageAsset.ImageAssetType.SPRITE,
                        IS_CONTAINER, ON_STEP_FUNCTION_ID, CAN_STEP_FUNCTION_ID,
                        ADDITIONAL_MOVEMENT_COST, HEIGHT_MOVEMENT_PENALTY_MITIGATION_FUNCTION_ID,
                        new String[]{COLOR_SHIFT_WRITTEN_VALUE}, DEFAULT_X_TILE_WIDTH_OFFSET,
                        DEFAULT_Y_TILE_HEIGHT_OFFSET)));
        assertThrows(IllegalArgumentException.class, () -> fixtureTypeFactory.make(
                new FixtureTypeDefinition(ID, NAME, "invalidId", ImageAsset.ImageAssetType.SPRITE,
                        IS_CONTAINER, ON_STEP_FUNCTION_ID, CAN_STEP_FUNCTION_ID,
                        ADDITIONAL_MOVEMENT_COST, HEIGHT_MOVEMENT_PENALTY_MITIGATION_FUNCTION_ID,
                        new String[]{COLOR_SHIFT_WRITTEN_VALUE}, DEFAULT_X_TILE_WIDTH_OFFSET,
                        DEFAULT_Y_TILE_HEIGHT_OFFSET)));
        assertThrows(IllegalArgumentException.class, () -> fixtureTypeFactory.make(
                new FixtureTypeDefinition(ID, NAME, "invalidId",
                        ImageAsset.ImageAssetType.GLOBAL_LOOPING_ANIMATION,
                        IS_CONTAINER, ON_STEP_FUNCTION_ID, CAN_STEP_FUNCTION_ID,
                        ADDITIONAL_MOVEMENT_COST, HEIGHT_MOVEMENT_PENALTY_MITIGATION_FUNCTION_ID,
                        new String[]{COLOR_SHIFT_WRITTEN_VALUE}, DEFAULT_X_TILE_WIDTH_OFFSET,
                        DEFAULT_Y_TILE_HEIGHT_OFFSET)));
        assertThrows(IllegalArgumentException.class, () -> fixtureTypeFactory.make(
                new FixtureTypeDefinition(ID, NAME, SPRITE_ID, null,
                        IS_CONTAINER, ON_STEP_FUNCTION_ID, CAN_STEP_FUNCTION_ID,
                        ADDITIONAL_MOVEMENT_COST, HEIGHT_MOVEMENT_PENALTY_MITIGATION_FUNCTION_ID,
                        new String[]{COLOR_SHIFT_WRITTEN_VALUE}, DEFAULT_X_TILE_WIDTH_OFFSET,
                        DEFAULT_Y_TILE_HEIGHT_OFFSET)));
        assertThrows(IllegalArgumentException.class, () -> fixtureTypeFactory.make(
                new FixtureTypeDefinition(ID, NAME, SPRITE_ID, ImageAsset.ImageAssetType.ANIMATION,
                        IS_CONTAINER, ON_STEP_FUNCTION_ID, CAN_STEP_FUNCTION_ID,
                        ADDITIONAL_MOVEMENT_COST, HEIGHT_MOVEMENT_PENALTY_MITIGATION_FUNCTION_ID,
                        new String[]{COLOR_SHIFT_WRITTEN_VALUE}, DEFAULT_X_TILE_WIDTH_OFFSET,
                        DEFAULT_Y_TILE_HEIGHT_OFFSET)));
        assertThrows(IllegalArgumentException.class, () -> fixtureTypeFactory.make(
                new FixtureTypeDefinition(ID, NAME, SPRITE_ID, ImageAsset.ImageAssetType.SPRITE,
                        IS_CONTAINER, null, CAN_STEP_FUNCTION_ID,
                        ADDITIONAL_MOVEMENT_COST, HEIGHT_MOVEMENT_PENALTY_MITIGATION_FUNCTION_ID,
                        new String[]{COLOR_SHIFT_WRITTEN_VALUE}, DEFAULT_X_TILE_WIDTH_OFFSET,
                        DEFAULT_Y_TILE_HEIGHT_OFFSET)));
        assertThrows(IllegalArgumentException.class, () -> fixtureTypeFactory.make(
                new FixtureTypeDefinition(ID, NAME, SPRITE_ID, ImageAsset.ImageAssetType.SPRITE,
                        IS_CONTAINER, "", CAN_STEP_FUNCTION_ID,
                        ADDITIONAL_MOVEMENT_COST, HEIGHT_MOVEMENT_PENALTY_MITIGATION_FUNCTION_ID,
                        new String[]{COLOR_SHIFT_WRITTEN_VALUE}, DEFAULT_X_TILE_WIDTH_OFFSET,
                        DEFAULT_Y_TILE_HEIGHT_OFFSET)));
        assertThrows(IllegalArgumentException.class, () -> fixtureTypeFactory.make(
                new FixtureTypeDefinition(ID, NAME, SPRITE_ID, ImageAsset.ImageAssetType.SPRITE,
                        IS_CONTAINER, "invalidId", CAN_STEP_FUNCTION_ID,
                        ADDITIONAL_MOVEMENT_COST, HEIGHT_MOVEMENT_PENALTY_MITIGATION_FUNCTION_ID,
                        new String[]{COLOR_SHIFT_WRITTEN_VALUE}, DEFAULT_X_TILE_WIDTH_OFFSET,
                        DEFAULT_Y_TILE_HEIGHT_OFFSET)));
        assertThrows(IllegalArgumentException.class, () -> fixtureTypeFactory.make(
                new FixtureTypeDefinition(ID, NAME, SPRITE_ID, ImageAsset.ImageAssetType.SPRITE,
                        IS_CONTAINER, ON_STEP_FUNCTION_ID, null,
                        ADDITIONAL_MOVEMENT_COST, HEIGHT_MOVEMENT_PENALTY_MITIGATION_FUNCTION_ID,
                        new String[]{COLOR_SHIFT_WRITTEN_VALUE}, DEFAULT_X_TILE_WIDTH_OFFSET,
                        DEFAULT_Y_TILE_HEIGHT_OFFSET)));
        assertThrows(IllegalArgumentException.class, () -> fixtureTypeFactory.make(
                new FixtureTypeDefinition(ID, NAME, SPRITE_ID, ImageAsset.ImageAssetType.SPRITE,
                        IS_CONTAINER, ON_STEP_FUNCTION_ID, "",
                        ADDITIONAL_MOVEMENT_COST, HEIGHT_MOVEMENT_PENALTY_MITIGATION_FUNCTION_ID,
                        new String[]{COLOR_SHIFT_WRITTEN_VALUE}, DEFAULT_X_TILE_WIDTH_OFFSET,
                        DEFAULT_Y_TILE_HEIGHT_OFFSET)));
        assertThrows(IllegalArgumentException.class, () -> fixtureTypeFactory.make(
                new FixtureTypeDefinition(ID, NAME, SPRITE_ID, ImageAsset.ImageAssetType.SPRITE,
                        IS_CONTAINER, ON_STEP_FUNCTION_ID, invalidFunctionId,
                        ADDITIONAL_MOVEMENT_COST, HEIGHT_MOVEMENT_PENALTY_MITIGATION_FUNCTION_ID,
                        new String[]{COLOR_SHIFT_WRITTEN_VALUE}, DEFAULT_X_TILE_WIDTH_OFFSET,
                        DEFAULT_Y_TILE_HEIGHT_OFFSET)));
        assertThrows(IllegalArgumentException.class, () -> fixtureTypeFactory.make(
                new FixtureTypeDefinition(ID, NAME, SPRITE_ID, ImageAsset.ImageAssetType.SPRITE,
                        IS_CONTAINER, ON_STEP_FUNCTION_ID, CAN_STEP_FUNCTION_ID,
                        ADDITIONAL_MOVEMENT_COST, null,
                        new String[]{COLOR_SHIFT_WRITTEN_VALUE}, DEFAULT_X_TILE_WIDTH_OFFSET,
                        DEFAULT_Y_TILE_HEIGHT_OFFSET)));
        assertThrows(IllegalArgumentException.class, () -> fixtureTypeFactory.make(
                new FixtureTypeDefinition(ID, NAME, SPRITE_ID, ImageAsset.ImageAssetType.SPRITE,
                        IS_CONTAINER, ON_STEP_FUNCTION_ID, CAN_STEP_FUNCTION_ID,
                        ADDITIONAL_MOVEMENT_COST, "",
                        new String[]{COLOR_SHIFT_WRITTEN_VALUE}, DEFAULT_X_TILE_WIDTH_OFFSET,
                        DEFAULT_Y_TILE_HEIGHT_OFFSET)));
        assertThrows(IllegalArgumentException.class, () -> fixtureTypeFactory.make(
                new FixtureTypeDefinition(ID, NAME, SPRITE_ID, ImageAsset.ImageAssetType.SPRITE,
                        IS_CONTAINER, ON_STEP_FUNCTION_ID, CAN_STEP_FUNCTION_ID,
                        ADDITIONAL_MOVEMENT_COST, invalidFunctionId,
                        new String[]{COLOR_SHIFT_WRITTEN_VALUE}, DEFAULT_X_TILE_WIDTH_OFFSET,
                        DEFAULT_Y_TILE_HEIGHT_OFFSET)));
    }

    @Test
    void testSetName() {
        var newName = randomString();
        var output = fixtureTypeFactory.make(definition);

        output.setName(newName);

        assertEquals(newName, output.getName());
    }

    @Test
    void testSetNameWithInvalidParams() {
        var output = fixtureTypeFactory.make(definition);

        assertThrows(IllegalArgumentException.class, () -> output.setName(null));
        assertThrows(IllegalArgumentException.class, () -> output.setName(""));
    }

    @Test
    void testStepFunctionsWithInvalidParams() {
        var output = fixtureTypeFactory.make(definition);

        assertThrows(IllegalArgumentException.class, () -> output.onStep(null));
        assertThrows(IllegalArgumentException.class, () -> output.canStep(null));
    }

    @Test
    void testGetInterfaceName() {
        assertEquals(Factory.class.getCanonicalName() + "<" +
                FixtureTypeDefinition.class.getCanonicalName() + "," +
                FixtureType.class.getCanonicalName() + ">", fixtureTypeFactory.getInterfaceName());
    }
}
