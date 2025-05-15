package inaugural.soliloquy.ruleset.gameconcepts;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import soliloquy.specs.common.shared.Direction;
import soliloquy.specs.common.valueobjects.Coordinate3d;
import soliloquy.specs.gamestate.entities.*;
import soliloquy.specs.ruleset.entities.FixtureType;
import soliloquy.specs.ruleset.entities.GroundType;
import soliloquy.specs.ruleset.entities.WallSegmentType;
import soliloquy.specs.ruleset.gameconcepts.TileNavigability;

import java.util.function.Supplier;

import static inaugural.soliloquy.tools.collections.Collections.mapOf;
import static inaugural.soliloquy.tools.collections.Collections.setOf;
import static inaugural.soliloquy.tools.random.Random.*;
import static inaugural.soliloquy.tools.valueobjects.Coordinate2d.addOffsets2d;
import static inaugural.soliloquy.tools.valueobjects.Coordinate3d.addOffsets3d;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static soliloquy.specs.common.shared.Direction.*;
import static soliloquy.specs.common.valueobjects.Pair.pairOf;
import static soliloquy.specs.gamestate.entities.WallSegmentOrientation.*;

@ExtendWith(MockitoExtension.class)
public class TileNavigabilityImplTests {
    // Any values are valid; they're arbitrarily constrained here to avoid overflow errors in test
    private final int DEFAULT_MOVE_COST = randomIntInRange(-1000, 1000);
    // Similarly, any non-negative value is valid here
    private final int FREE_ESCALATION = randomIntInRange(0, 1000);
    private final Coordinate3d ORIGIN = randomCoordinate3d();
    private final Coordinate3d MOCK_TILE_LOC = randomCoordinate2d().to3d(ORIGIN.Z);
    // Any positive value is valid, the limitation here is for speed
    private final int CHAR_HEIGHT = randomIntInRange(2, 100);
    private final int WITHIN_CHAR_HEIGHT_OFFSET = randomIntInRange(0, CHAR_HEIGHT - 1);

    @Mock private FixtureType mockFixtureType1;
    @Mock private FixtureType mockFixtureType2;
    @Mock private TileFixture mockFixture1;
    @Mock private TileFixture mockFixture2;
    @Mock private TileEntities<TileFixture> mockTileFixtures;
    @Mock private GroundType mockGroundType;
    @Mock private Tile mockTile;
    @Mock private WallSegmentType mockSegmentType;
    @Mock private WallSegment mockSegment;
    @Mock private GameZone mockGameZone;
    @Mock private Supplier<GameZone> mockGetGameZone;

    private TileNavigability tileNavigability;

    @BeforeEach
    public void setUp() {
        lenient().when(mockTile.location()).thenReturn(MOCK_TILE_LOC);
        lenient().when(mockSegment.getType()).thenReturn(mockSegmentType);
        lenient().when(mockGroundType.additionalMovementCost()).thenReturn(0);
        lenient().when(mockTile.getGroundType()).thenReturn(mockGroundType);
        lenient().when(mockFixtureType1.additionalMovementCost()).thenReturn(0);
        lenient().when(mockFixture1.type()).thenReturn(mockFixtureType1);
        lenient().when(mockFixtureType2.additionalMovementCost()).thenReturn(0);
        lenient().when(mockFixture2.type()).thenReturn(mockFixtureType2);
        lenient().when(mockTileFixtures.representation()).thenReturn(mapOf(
                pairOf(mockFixture1, randomInt()), pairOf(mockFixture2, randomInt())
        ));
        lenient().when(mockTile.fixtures()).thenReturn(mockTileFixtures);
        lenient().when(mockGameZone.tiles(any())).thenReturn(setOf(mockTile));
        lenient().when(mockGameZone.segments(any())).thenReturn(mapOf());
        lenient().when(mockGetGameZone.get()).thenReturn(mockGameZone);

        tileNavigability =
                new TileNavigabilityImpl(mockGetGameZone, DEFAULT_MOVE_COST, FREE_ESCALATION);
    }

    // It is likely a _poor_ idea to have a non-positive default movement cost, but not strictly
    // invalid
    @Test
    public void testConstructorWithInvalidArgs() {
        assertThrows(IllegalArgumentException.class,
                () -> new TileNavigabilityImpl(null, DEFAULT_MOVE_COST, FREE_ESCALATION));
        assertThrows(IllegalArgumentException.class,
                () -> new TileNavigabilityImpl(mockGetGameZone, DEFAULT_MOVE_COST, -1));
    }

    @Test
    public void testNoTileToMoveTo() {
        when(mockGameZone.tiles(any())).thenReturn(setOf());

        var navigability = tileNavigability.calculate(ORIGIN, NORTH, CHAR_HEIGHT);

        assertNull(navigability);
    }

    @Test
    public void testAdjacentNavigabilityNorth() {
        testAdjacentNavigability(NORTH, 0, -1);
    }

    @Test
    public void testAdjacentNavigabilityNortheast() {
        testAdjacentNavigability(NORTHEAST, 1, -1);
    }

    @Test
    public void testAdjacentNavigabilityEast() {
        testAdjacentNavigability(EAST, 1, 0);
    }

    @Test
    public void testAdjacentNavigabilitySoutheast() {
        testAdjacentNavigability(SOUTHEAST, 1, 1);
    }

    @Test
    public void testAdjacentNavigabilitySouth() {
        testAdjacentNavigability(SOUTH, 0, 1);
    }

    @Test
    public void testAdjacentNavigabilitySouthwest() {
        testAdjacentNavigability(SOUTHWEST, -1, 1);
    }

    @Test
    public void testAdjacentNavigabilityWest() {
        testAdjacentNavigability(WEST, -1, 0);
    }

    @Test
    public void testAdjacentNavigabilityNorthwest() {
        testAdjacentNavigability(NORTHWEST, -1, -1);
    }

    private void testAdjacentNavigability(Direction direction, int offsetX, int offsetY) {
        var navigability = tileNavigability.calculate(ORIGIN, direction, CHAR_HEIGHT);

        assertNotNull(navigability);
        var expectedDestination = addOffsets3d(ORIGIN, offsetX, offsetY, 0);
        assertEquals(expectedDestination, navigability.destination());
        assertEquals(DEFAULT_MOVE_COST, navigability.cost());
    }

    @Test
    public void testMovesToMaximumTileWithinFreeEscalation() {
        var mockUnreachableTile = mock(Tile.class);
        when(mockUnreachableTile.location()).thenReturn(
                addOffsets3d(ORIGIN, 1, 0, FREE_ESCALATION + 1));
        var mockReachableTileBeneathMax = mock(Tile.class);
        when(mockReachableTileBeneathMax.location()).thenReturn(
                addOffsets3d(ORIGIN, 1, 0, FREE_ESCALATION + 1));
        var mockTileLocation = addOffsets3d(ORIGIN, 1, 0, FREE_ESCALATION);
        when(mockTile.location()).thenReturn(mockTileLocation);
        reset(mockGameZone);
        when(mockGameZone.tiles(any())).thenReturn(
                setOf(mockTile, mockUnreachableTile, mockReachableTileBeneathMax));

        var navigability = tileNavigability.calculate(ORIGIN, EAST, CHAR_HEIGHT);

        assertEquals(mockTileLocation, navigability.destination());
        verify(mockGetGameZone).get();
        verify(mockGameZone).tiles(eq(mockTileLocation.to2d()));
    }

    @Test
    public void testExcludesTilesBelowFreeEscalation() {
        var mockUnreachableTileLocation = addOffsets3d(ORIGIN, 1, 0, -FREE_ESCALATION - 1);
        when(mockTile.location()).thenReturn(mockUnreachableTileLocation);

        var navigability = tileNavigability.calculate(ORIGIN, EAST, CHAR_HEIGHT);

        assertNull(navigability);
        verify(mockGetGameZone).get();
        verify(mockGameZone).tiles(eq(mockUnreachableTileLocation.to2d()));
    }

    @Test
    public void testSegmentsCanBlockMovementNorth() {
        testSegmentCanBlockMovement(0, 0, NORTH, HORIZONTAL);
    }

    @Test
    public void testSegmentsCanBlockMovementNortheast() {
        testSegmentCanBlockMovement(1, 0, NORTHEAST, CORNER);
    }

    @Test
    public void testSegmentCanBlockMovementEast() {
        testSegmentCanBlockMovement(1, 0, EAST, VERTICAL);
    }

    @Test
    public void testSegmentCanBlockMovementSoutheast() {
        testSegmentCanBlockMovement(1, 1, SOUTHEAST, CORNER);
    }

    @Test
    public void testSegmentCanBlockMovementSouth() {
        testSegmentCanBlockMovement(0, 1, SOUTH, HORIZONTAL);
    }

    @Test
    public void testSegmentCanBlockMovementSouthwest() {
        testSegmentCanBlockMovement(0, 1, SOUTHWEST, CORNER);
    }

    @Test
    public void testSegmentCanBlockMovementWest() {
        testSegmentCanBlockMovement(0, 0, WEST, VERTICAL);
    }

    @Test
    public void testSegmentCanBlockMovementNorthwest() {
        testSegmentCanBlockMovement(0, 0, NORTHWEST, CORNER);
    }

    @Test
    public void testGroundCanIncreaseMovementCost() {
        var groundAdditionalMoveCost = randomInt();
        when(mockGroundType.additionalMovementCost()).thenReturn(groundAdditionalMoveCost);

        var navigability = tileNavigability.calculate(ORIGIN, randomDirection(), CHAR_HEIGHT);

        assertEquals(DEFAULT_MOVE_COST + groundAdditionalMoveCost, navigability.cost());
        verify(mockTile).getGroundType();
        verify(mockGroundType).additionalMovementCost();
    }

    @Test
    public void testFixturesCanIncreaseMovementCost() {
        var fixture1AdditionalMoveCost = randomInt();
        var fixture2AdditionalMoveCost = randomInt();
        when(mockFixtureType1.additionalMovementCost()).thenReturn(fixture1AdditionalMoveCost);
        when(mockFixtureType2.additionalMovementCost()).thenReturn(fixture2AdditionalMoveCost);

        var navigability = tileNavigability.calculate(ORIGIN, randomDirection(), CHAR_HEIGHT);

        assertEquals(DEFAULT_MOVE_COST + fixture1AdditionalMoveCost + fixture2AdditionalMoveCost,
                navigability.cost());
        verify(mockTile).fixtures();
        verify(mockTileFixtures).representation();
        verify(mockFixture1).type();
        verify(mockFixtureType1).additionalMovementCost();
        verify(mockFixture2).type();
        verify(mockFixtureType2).additionalMovementCost();
    }

    private void testSegmentCanBlockMovement(int offsetX, int offsetY, Direction direction,
                                             WallSegmentOrientation expectedOrientation) {
        when(mockGameZone.segments(any(), any())).thenReturn(setOf(mockSegment));
        when(mockSegmentType.blocksMovement()).thenReturn(true);
        when(mockSegment.location())
                .thenReturn(addOffsets3d(ORIGIN, offsetX, offsetY, WITHIN_CHAR_HEIGHT_OFFSET));

        var navigability = tileNavigability.calculate(ORIGIN, direction, CHAR_HEIGHT);

        assertNull(navigability);
        verify(mockGetGameZone).get();
        verify(mockGameZone).segments(eq(addOffsets2d(ORIGIN.to2d(), offsetX, offsetY)),
                eq(expectedOrientation));
        verify(mockSegment).getType();
        verify(mockSegmentType).blocksMovement();
    }

    @Test
    public void testCalculateWithInvalidArgs() {
        assertThrows(IllegalArgumentException.class,
                () -> tileNavigability.calculate(null, SOUTH, CHAR_HEIGHT));
        assertThrows(IllegalArgumentException.class,
                () -> tileNavigability.calculate(ORIGIN, null, CHAR_HEIGHT));
        assertThrows(IllegalArgumentException.class,
                () -> tileNavigability.calculate(ORIGIN, SOUTH, 0));
    }
}
