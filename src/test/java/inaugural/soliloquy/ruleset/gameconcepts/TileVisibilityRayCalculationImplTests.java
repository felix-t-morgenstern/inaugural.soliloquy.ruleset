package inaugural.soliloquy.ruleset.gameconcepts;

import org.javatuples.Triplet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import soliloquy.specs.common.valueobjects.Coordinate2d;
import soliloquy.specs.common.valueobjects.Coordinate3d;
import soliloquy.specs.common.valueobjects.Pair;
import soliloquy.specs.gamestate.entities.GameZone;
import soliloquy.specs.gamestate.entities.Tile;
import soliloquy.specs.gamestate.entities.WallSegment;
import soliloquy.specs.gamestate.entities.WallSegmentOrientation;
import soliloquy.specs.ruleset.entities.WallSegmentType;
import soliloquy.specs.ruleset.gameconcepts.TileVisibilityRayCalculation;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static inaugural.soliloquy.tools.collections.Collections.*;
import static inaugural.soliloquy.tools.random.Random.randomIntInRange;
import static inaugural.soliloquy.tools.valueobjects.Coordinate2d.addOffsets2d;
import static inaugural.soliloquy.tools.valueobjects.Coordinate3d.addOffsets3d;
import static inaugural.soliloquy.tools.valueobjects.Pair.pairOf;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static soliloquy.specs.gamestate.entities.WallSegmentOrientation.*;
import static soliloquy.specs.ruleset.gameconcepts.TileVisibilityCalculation.Result;

// Coordinates used for testing tiles to run over were generated by creating a line in a graphing
// calculator which intersects the offset of the target from the origin. Each tile's location can
// be treated as the intersections between whole-number values on the axes, with the segments at
// the +/-0.5 boundaries. When the line intersecting the target offset tile's location crosses a
// 0.5 boundary, again representing crossing a segment plane from one 2d coordinate to another,
// that is treated as a testing requirement for the visibility ray's cursor to move to that 2d
// location from its previous location.
@RunWith(MockitoJUnitRunner.class)
public class TileVisibilityRayCalculationImplTests {
    // This is being set statically since the test cases for this suite need to be manually
    // verified, prohibiting randomness
    private final float TARGET_ADDEND_ABOVE = 0.5f;
    private final float TARGET_ADDEND_BELOW = 8f;

    // Z values must be within a reasonable value, to avoid bizarre rounding errors
    private final int Z = randomIntInRange(-10000, 10000);

    @Mock private Supplier<GameZone> mockGetGameZone;
    @Mock private GameZone mockGameZone;

    private List<Pair<Coordinate3d, Tile>> tilesReturned;
    private List<Triplet<WallSegmentOrientation, Coordinate3d, WallSegment>> segmentsReturned;
    private Map<Integer, WallSegment> segmentReturnOverrides;
    private int segmentGenCounter;
    // NB: These are only relevant in line-of-sight blocking tests, since segments will need to
    // be at specific locations to not potentially overlap
    private boolean movingEast;
    private boolean movingSouth;

    private TileVisibilityRayCalculation rayCalculation;

    @Before
    public void setUp() {
        tilesReturned = listOf();
        segmentsReturned = listOf();
        segmentReturnOverrides = mapOf();
        segmentGenCounter = 0;
        movingEast = movingSouth = false;

        when(mockGameZone.getSegments(any())).thenAnswer(invocation -> {
            var index = segmentGenCounter++;
            WallSegment mockSegment;
            if (segmentReturnOverrides.containsKey(index)) {
                mockSegment = segmentReturnOverrides.get(index);
            }
            else {
                var orientation = WallSegmentOrientation.fromValue(randomIntInRange(1, 3));
                Coordinate2d tileLoc = invocation.getArgument(0);
                var segLocX = tileLoc.X + (movingEast ? 1 : 0);
                var segLocY = tileLoc.Y + (movingSouth ? 1 : 0);
                mockSegment =
                        makeMockSegment(orientation, Coordinate3d.of(segLocX, segLocY, Z), false);
            }
            var orientation = mockSegment.getType().orientation();
            var loc = mockSegment.location();
            segmentsReturned.add(Triplet.with(orientation, loc, mockSegment));
            Map<WallSegmentOrientation, Map<Coordinate3d, WallSegment>> segmentsMap = mapOf();
            // GameZone::getSegments is assumed to return a non-null map for each orientation
            segmentsMap.put(HORIZONTAL, mapOf());
            segmentsMap.put(CORNER, mapOf());
            segmentsMap.put(VERTICAL, mapOf());
            segmentsMap.get(orientation).put(loc, mockSegment);
            return segmentsMap;
        });
        when(mockGameZone.tiles(any())).thenAnswer(invocation -> {
            var mockTile = mock(Tile.class);
            var mockTileLoc3d = ((Coordinate2d) invocation.getArgument(0)).to3d(Z);
            when(mockTile.location()).thenReturn(mockTileLoc3d);
            tilesReturned.add(pairOf(mockTileLoc3d, mockTile));
            return setOf(mockTile);
        });
        when(mockGetGameZone.get()).thenReturn(mockGameZone);

        rayCalculation =
                new TileVisibilityRayCalculationImpl(mockGetGameZone, TARGET_ADDEND_ABOVE,
                        TARGET_ADDEND_BELOW);
    }

    @Test
    public void testConstructorWithInvalidParams() {
        assertThrows(IllegalArgumentException.class,
                () -> new TileVisibilityRayCalculationImpl(null, TARGET_ADDEND_ABOVE,
                        TARGET_ADDEND_BELOW));
        assertThrows(IllegalArgumentException.class,
                () -> new TileVisibilityRayCalculationImpl(mockGetGameZone, -0.000001f,
                        TARGET_ADDEND_BELOW));
        assertThrows(IllegalArgumentException.class,
                () -> new TileVisibilityRayCalculationImpl(mockGetGameZone,
                        TARGET_ADDEND_ABOVE, -0.000001f));

    }

    @Test
    public void testGetVisibilityAtOrigin() {
        var origin = randomCoordinate3dInNormalRange();
        var expectedCursorHits = 1;

        var result = rayCalculation.castRay(origin, origin.to2d());

        assertNotNull(result);
        assertEquals(expectedCursorHits, result.tiles().size());
        assertEquals(expectedCursorHits, segmentsReturned.size());
        testTileHit(origin.to2d(), 0, result);
    }

    @Test
    public void testGetVisibilityStraightHorizontalLineEast() {
        var origin = randomCoordinate3dInNormalRange();
        var rayLength = randomIntInRange(2, 10);
        var expectedCursorHits = rayLength + 1;
        var destination = Coordinate2d.of(origin.X + rayLength, origin.Y);

        var result = rayCalculation.castRay(origin, destination);

        assertNotNull(result);
        assertEquals(expectedCursorHits, result.tiles().size());
        assertEquals(expectedCursorHits, segmentsReturned.size());
        for (var i = 0; i <= rayLength; i++) {
            testTileHit(Coordinate2d.of(origin.X + i, origin.Y), i, result);
        }
    }

    @Test
    public void testGetVisibilityStraightHorizontalLineWest() {
        var origin = randomCoordinate3dInNormalRange();
        var rayLength = randomIntInRange(2, 10);
        var expectedCursorHits = rayLength + 1;
        var destination = Coordinate2d.of(origin.X - rayLength, origin.Y);

        var result = rayCalculation.castRay(origin, destination);

        assertNotNull(result);
        assertEquals(expectedCursorHits, result.tiles().size());
        assertEquals(expectedCursorHits, segmentsReturned.size());
        for (var i = 0; i <= rayLength; i++) {
            testTileHit(Coordinate2d.of(origin.X - i, origin.Y), i, result);
        }
    }

    @Test
    public void testGetVisibilityStraightVerticalLineSouth() {
        var origin = randomCoordinate3dInNormalRange();
        var rayLength = randomIntInRange(2, 10);
        var expectedCursorHits = rayLength + 1;
        var destination = Coordinate2d.of(origin.X, origin.Y + rayLength);

        var result = rayCalculation.castRay(origin, destination);

        assertNotNull(result);
        assertEquals(expectedCursorHits, result.tiles().size());
        assertEquals(expectedCursorHits, segmentsReturned.size());
        for (var i = 0; i <= rayLength; i++) {
            testTileHit(Coordinate2d.of(origin.X, origin.Y + i), i, result);
        }
    }

    @Test
    public void testGetVisibilityStraightVerticalLineNorth() {
        var origin = randomCoordinate3dInNormalRange();
        var rayLength = randomIntInRange(2, 10);
        var expectedCursorHits = rayLength + 1;
        var destination = Coordinate2d.of(origin.X, origin.Y - rayLength);

        var result = rayCalculation.castRay(origin, destination);

        assertNotNull(result);
        assertEquals(expectedCursorHits, result.tiles().size());
        assertEquals(expectedCursorHits, segmentsReturned.size());
        for (var i = 0; i <= rayLength; i++) {
            testTileHit(Coordinate2d.of(origin.X, origin.Y - i), i, result);
        }
    }

    @Test
    public void testGetVisibilityAtAnglePosSlopeBelowOneSoutheast_1() {
        var origin = randomCoordinate3dInNormalRange();
        var destination = addOffsets2d(origin.to2d(), 5, 4);

        var result = rayCalculation.castRay(origin, destination);

        var expectedTiles = listOf(
                addOffsets2d(origin.to2d(), 0, 0),
                addOffsets2d(origin.to2d(), 1, 0),
                addOffsets2d(origin.to2d(), 1, 1),
                addOffsets2d(origin.to2d(), 2, 1),
                addOffsets2d(origin.to2d(), 2, 2),
                addOffsets2d(origin.to2d(), 3, 2),
                addOffsets2d(origin.to2d(), 3, 3),
                addOffsets2d(origin.to2d(), 4, 3),
                addOffsets2d(origin.to2d(), 4, 4),
                addOffsets2d(origin.to2d(), 5, 4)
        );
        assertNotNull(result);
        assertEquals(expectedTiles.size(), result.tiles().size());
        assertEquals(expectedTiles.size(), segmentsReturned.size());
        for (var i = 0; i < expectedTiles.size(); i++) {
            testTileHit(expectedTiles.get(i), i, result);
        }
    }

    @Test
    public void testGetVisibilityAtAnglePosSlopeBelowOneSoutheast_2() {
        var origin = randomCoordinate3dInNormalRange();
        var destination = addOffsets2d(origin.to2d(), 8, 2);

        var result = rayCalculation.castRay(origin, destination);

        var expectedTiles = listOf(
                addOffsets2d(origin.to2d(), 0, 0),
                addOffsets2d(origin.to2d(), 1, 0),
                addOffsets2d(origin.to2d(), 2, 0),
                addOffsets2d(origin.to2d(), 2, 1),
                addOffsets2d(origin.to2d(), 3, 1),
                addOffsets2d(origin.to2d(), 4, 1),
                addOffsets2d(origin.to2d(), 5, 1),
                addOffsets2d(origin.to2d(), 6, 1),
                addOffsets2d(origin.to2d(), 6, 2),
                addOffsets2d(origin.to2d(), 7, 2),
                addOffsets2d(origin.to2d(), 8, 2)
        );
        assertNotNull(result);
        assertEquals(expectedTiles.size(), result.tiles().size());
        assertEquals(expectedTiles.size(), segmentsReturned.size());
        for (var i = 0; i < expectedTiles.size(); i++) {
            testTileHit(expectedTiles.get(i), i, result);
        }
    }

    @Test
    public void testGetVisibilityAtAnglePosSlope1Southeast() {
        var origin = randomCoordinate3dInNormalRange();
        var rayLength = randomIntInRange(2, 10);
        var expectedCursorHits = rayLength + 1;
        var destination = Coordinate2d.of(origin.X + rayLength, origin.Y + rayLength);

        var result = rayCalculation.castRay(origin, destination);

        assertNotNull(result);
        assertEquals(expectedCursorHits, result.tiles().size());
        assertEquals(expectedCursorHits, segmentsReturned.size());
        for (var i = 0; i <= rayLength; i++) {
            testTileHit(Coordinate2d.of(origin.X + i, origin.Y + i), i, result);
        }
    }

    @Test
    public void testGetVisibilityAtAnglePosSlopeAboveOneSoutheast_1() {
        var origin = randomCoordinate3dInNormalRange();
        var destination = addOffsets2d(origin.to2d(), 4, 6);

        var result = rayCalculation.castRay(origin, destination);

        var expectedTiles = listOf(
                addOffsets2d(origin.to2d(), 0, 0),
                addOffsets2d(origin.to2d(), 0, 1),
                addOffsets2d(origin.to2d(), 1, 1),
                addOffsets2d(origin.to2d(), 1, 2),
                addOffsets2d(origin.to2d(), 2, 2),
                addOffsets2d(origin.to2d(), 2, 3),
                addOffsets2d(origin.to2d(), 2, 4),
                addOffsets2d(origin.to2d(), 3, 4),
                addOffsets2d(origin.to2d(), 3, 5),
                addOffsets2d(origin.to2d(), 4, 5),
                addOffsets2d(origin.to2d(), 4, 6)
        );
        assertNotNull(result);
        assertEquals(expectedTiles.size(), result.tiles().size());
        assertEquals(expectedTiles.size(), segmentsReturned.size());
        for (var i = 0; i < expectedTiles.size(); i++) {
            testTileHit(expectedTiles.get(i), i, result);
        }
    }

    @Test
    public void testGetVisibilityAtAnglePosSlopeAboveOneSoutheast_2() {
        var origin = randomCoordinate3dInNormalRange();
        var destination = addOffsets2d(origin.to2d(), 2, 7);

        var result = rayCalculation.castRay(origin, destination);

        var expectedTiles = listOf(
                addOffsets2d(origin.to2d(), 0, 0),
                addOffsets2d(origin.to2d(), 0, 1),
                addOffsets2d(origin.to2d(), 0, 2),
                addOffsets2d(origin.to2d(), 1, 2),
                addOffsets2d(origin.to2d(), 1, 3),
                addOffsets2d(origin.to2d(), 1, 4),
                addOffsets2d(origin.to2d(), 1, 5),
                addOffsets2d(origin.to2d(), 2, 5),
                addOffsets2d(origin.to2d(), 2, 6),
                addOffsets2d(origin.to2d(), 2, 7)
        );
        assertNotNull(result);
        assertEquals(expectedTiles.size(), result.tiles().size());
        assertEquals(expectedTiles.size(), segmentsReturned.size());
        for (var i = 0; i < expectedTiles.size(); i++) {
            testTileHit(expectedTiles.get(i), i, result);
        }
    }

    @Test
    public void testGetVisibilityAtAnglePosSlopeBelowOneNorthwest_1() {
        var origin = randomCoordinate3dInNormalRange();
        var destination = addOffsets2d(origin.to2d(), -5, -4);

        var result = rayCalculation.castRay(origin, destination);

        var expectedTiles = listOf(
                addOffsets2d(origin.to2d(), 0, 0),
                addOffsets2d(origin.to2d(), -1, 0),
                addOffsets2d(origin.to2d(), -1, -1),
                addOffsets2d(origin.to2d(), -2, -1),
                addOffsets2d(origin.to2d(), -2, -2),
                addOffsets2d(origin.to2d(), -3, -2),
                addOffsets2d(origin.to2d(), -3, -3),
                addOffsets2d(origin.to2d(), -4, -3),
                addOffsets2d(origin.to2d(), -4, -4),
                addOffsets2d(origin.to2d(), -5, -4)
        );
        assertNotNull(result);
        assertEquals(expectedTiles.size(), result.tiles().size());
        assertEquals(expectedTiles.size(), segmentsReturned.size());
        for (var i = 0; i < expectedTiles.size(); i++) {
            testTileHit(expectedTiles.get(i), i, result);
        }
    }

    @Test
    public void testGetVisibilityAtAnglePosSlopeBelowOneNorthwest_2() {
        var origin = randomCoordinate3dInNormalRange();
        var destination = addOffsets2d(origin.to2d(), -8, -2);

        var result = rayCalculation.castRay(origin, destination);

        var expectedTiles = listOf(
                addOffsets2d(origin.to2d(), 0, 0),
                addOffsets2d(origin.to2d(), -1, 0),
                addOffsets2d(origin.to2d(), -2, 0),
                addOffsets2d(origin.to2d(), -2, -1),
                addOffsets2d(origin.to2d(), -3, -1),
                addOffsets2d(origin.to2d(), -4, -1),
                addOffsets2d(origin.to2d(), -5, -1),
                addOffsets2d(origin.to2d(), -6, -1),
                addOffsets2d(origin.to2d(), -6, -2),
                addOffsets2d(origin.to2d(), -7, -2),
                addOffsets2d(origin.to2d(), -8, -2)
        );
        assertNotNull(result);
        assertEquals(expectedTiles.size(), result.tiles().size());
        assertEquals(expectedTiles.size(), segmentsReturned.size());
        for (var i = 0; i < expectedTiles.size(); i++) {
            testTileHit(expectedTiles.get(i), i, result);
        }
    }

    @Test
    public void testGetVisibilityAtAnglePosSlope1Northwest() {
        var origin = randomCoordinate3dInNormalRange();
        var rayLength = randomIntInRange(2, 10);
        var expectedCursorHits = rayLength + 1;
        var destination = Coordinate2d.of(origin.X - rayLength, origin.Y - rayLength);

        var result = rayCalculation.castRay(origin, destination);

        assertNotNull(result);
        assertEquals(expectedCursorHits, result.tiles().size());
        assertEquals(expectedCursorHits, segmentsReturned.size());
        for (var i = 0; i <= rayLength; i++) {
            testTileHit(Coordinate2d.of(origin.X - i, origin.Y - i), i, result);
        }
    }

    @Test
    public void testGetVisibilityAtAnglePosSlopeAboveOneNorthwest_1() {
        var origin = randomCoordinate3dInNormalRange();
        var destination = addOffsets2d(origin.to2d(), -4, -6);

        var result = rayCalculation.castRay(origin, destination);

        var expectedTiles = listOf(
                addOffsets2d(origin.to2d(), 0, 0),
                addOffsets2d(origin.to2d(), 0, -1),
                addOffsets2d(origin.to2d(), -1, -1),
                addOffsets2d(origin.to2d(), -1, -2),
                addOffsets2d(origin.to2d(), -2, -2),
                addOffsets2d(origin.to2d(), -2, -3),
                addOffsets2d(origin.to2d(), -2, -4),
                addOffsets2d(origin.to2d(), -3, -4),
                addOffsets2d(origin.to2d(), -3, -5),
                addOffsets2d(origin.to2d(), -4, -5),
                addOffsets2d(origin.to2d(), -4, -6)
        );
        assertNotNull(result);
        assertEquals(expectedTiles.size(), result.tiles().size());
        assertEquals(expectedTiles.size(), segmentsReturned.size());
        for (var i = 0; i < expectedTiles.size(); i++) {
            testTileHit(expectedTiles.get(i), i, result);
        }
    }

    @Test
    public void testGetVisibilityAtAnglePosSlopeAboveOneNorthwest_2() {
        var origin = randomCoordinate3dInNormalRange();
        var destination = addOffsets2d(origin.to2d(), -2, -7);

        var result = rayCalculation.castRay(origin, destination);

        var expectedTiles = listOf(
                addOffsets2d(origin.to2d(), 0, 0),
                addOffsets2d(origin.to2d(), 0, -1),
                addOffsets2d(origin.to2d(), 0, -2),
                addOffsets2d(origin.to2d(), -1, -2),
                addOffsets2d(origin.to2d(), -1, -3),
                addOffsets2d(origin.to2d(), -1, -4),
                addOffsets2d(origin.to2d(), -1, -5),
                addOffsets2d(origin.to2d(), -2, -5),
                addOffsets2d(origin.to2d(), -2, -6),
                addOffsets2d(origin.to2d(), -2, -7)
        );
        assertNotNull(result);
        assertEquals(expectedTiles.size(), result.tiles().size());
        assertEquals(expectedTiles.size(), segmentsReturned.size());
        for (var i = 0; i < expectedTiles.size(); i++) {
            testTileHit(expectedTiles.get(i), i, result);
        }
    }

    @Test
    public void testGetVisibilityAtAngleNegSlopeBelowOneNortheast_1() {
        var origin = randomCoordinate3dInNormalRange();
        var destination = addOffsets2d(origin.to2d(), 5, -4);

        var result = rayCalculation.castRay(origin, destination);

        var expectedTiles = listOf(
                addOffsets2d(origin.to2d(), 0, 0),
                addOffsets2d(origin.to2d(), 1, 0),
                addOffsets2d(origin.to2d(), 1, -1),
                addOffsets2d(origin.to2d(), 2, -1),
                addOffsets2d(origin.to2d(), 2, -2),
                addOffsets2d(origin.to2d(), 3, -2),
                addOffsets2d(origin.to2d(), 3, -3),
                addOffsets2d(origin.to2d(), 4, -3),
                addOffsets2d(origin.to2d(), 4, -4),
                addOffsets2d(origin.to2d(), 5, -4)
        );
        assertNotNull(result);
        assertEquals(expectedTiles.size(), result.tiles().size());
        assertEquals(expectedTiles.size(), segmentsReturned.size());
        for (var i = 0; i < expectedTiles.size(); i++) {
            testTileHit(expectedTiles.get(i), i, result);
        }
    }

    @Test
    public void testGetVisibilityAtAngleNegSlopeBelowOneNortheast_2() {
        var origin = randomCoordinate3dInNormalRange();
        var destination = addOffsets2d(origin.to2d(), 8, -2);

        var result = rayCalculation.castRay(origin, destination);

        var expectedTiles = listOf(
                addOffsets2d(origin.to2d(), 0, 0),
                addOffsets2d(origin.to2d(), 1, 0),
                addOffsets2d(origin.to2d(), 2, 0),
                addOffsets2d(origin.to2d(), 2, -1),
                addOffsets2d(origin.to2d(), 3, -1),
                addOffsets2d(origin.to2d(), 4, -1),
                addOffsets2d(origin.to2d(), 5, -1),
                addOffsets2d(origin.to2d(), 6, -1),
                addOffsets2d(origin.to2d(), 6, -2),
                addOffsets2d(origin.to2d(), 7, -2),
                addOffsets2d(origin.to2d(), 8, -2)
        );
        assertNotNull(result);
        assertEquals(expectedTiles.size(), result.tiles().size());
        assertEquals(expectedTiles.size(), segmentsReturned.size());
        for (var i = 0; i < expectedTiles.size(); i++) {
            testTileHit(expectedTiles.get(i), i, result);
        }
    }

    @Test
    public void testGetVisibilityAtAngleNegSlope1Northeast() {
        var origin = randomCoordinate3dInNormalRange();
        var rayLength = randomIntInRange(2, 10);
        var expectedCursorHits = rayLength + 1;
        var destination = Coordinate2d.of(origin.X + rayLength, origin.Y - rayLength);

        var result = rayCalculation.castRay(origin, destination);

        assertNotNull(result);
        assertEquals(expectedCursorHits, result.tiles().size());
        assertEquals(expectedCursorHits, segmentsReturned.size());
        for (var i = 0; i <= rayLength; i++) {
            testTileHit(Coordinate2d.of(origin.X + i, origin.Y - i), i, result);
        }
    }

    @Test
    public void testGetVisibilityAtAngleNegSlopeAboveOneNortheast_1() {
        var origin = randomCoordinate3dInNormalRange();
        var destination = addOffsets2d(origin.to2d(), 4, -6);

        var result = rayCalculation.castRay(origin, destination);

        var expectedTiles = listOf(
                addOffsets2d(origin.to2d(), 0, 0),
                addOffsets2d(origin.to2d(), 0, -1),
                addOffsets2d(origin.to2d(), 1, -1),
                addOffsets2d(origin.to2d(), 1, -2),
                addOffsets2d(origin.to2d(), 2, -2),
                addOffsets2d(origin.to2d(), 2, -3),
                addOffsets2d(origin.to2d(), 2, -4),
                addOffsets2d(origin.to2d(), 3, -4),
                addOffsets2d(origin.to2d(), 3, -5),
                addOffsets2d(origin.to2d(), 4, -5),
                addOffsets2d(origin.to2d(), 4, -6)
        );
        assertNotNull(result);
        assertEquals(expectedTiles.size(), result.tiles().size());
        assertEquals(expectedTiles.size(), segmentsReturned.size());
        for (var i = 0; i < expectedTiles.size(); i++) {
            testTileHit(expectedTiles.get(i), i, result);
        }
    }

    @Test
    public void testGetVisibilityAtAngleNegSlopeAboveOneNortheast_2() {
        var origin = randomCoordinate3dInNormalRange();
        var destination = addOffsets2d(origin.to2d(), 2, -7);

        var result = rayCalculation.castRay(origin, destination);

        var expectedTiles = listOf(
                addOffsets2d(origin.to2d(), 0, 0),
                addOffsets2d(origin.to2d(), 0, -1),
                addOffsets2d(origin.to2d(), 0, -2),
                addOffsets2d(origin.to2d(), 1, -2),
                addOffsets2d(origin.to2d(), 1, -3),
                addOffsets2d(origin.to2d(), 1, -4),
                addOffsets2d(origin.to2d(), 1, -5),
                addOffsets2d(origin.to2d(), 2, -5),
                addOffsets2d(origin.to2d(), 2, -6),
                addOffsets2d(origin.to2d(), 2, -7)
        );
        assertNotNull(result);
        assertEquals(expectedTiles.size(), result.tiles().size());
        assertEquals(expectedTiles.size(), segmentsReturned.size());
        for (var i = 0; i < expectedTiles.size(); i++) {
            testTileHit(expectedTiles.get(i), i, result);
        }
    }

    @Test
    public void testGetVisibilityAtAngleNegSlopeBelowOneSouthwest_1() {
        var origin = randomCoordinate3dInNormalRange();
        var destination = addOffsets2d(origin.to2d(), -5, 4);

        var result = rayCalculation.castRay(origin, destination);

        var expectedTiles = listOf(
                addOffsets2d(origin.to2d(), 0, 0),
                addOffsets2d(origin.to2d(), -1, 0),
                addOffsets2d(origin.to2d(), -1, 1),
                addOffsets2d(origin.to2d(), -2, 1),
                addOffsets2d(origin.to2d(), -2, 2),
                addOffsets2d(origin.to2d(), -3, 2),
                addOffsets2d(origin.to2d(), -3, 3),
                addOffsets2d(origin.to2d(), -4, 3),
                addOffsets2d(origin.to2d(), -4, 4),
                addOffsets2d(origin.to2d(), -5, 4)
        );
        assertNotNull(result);
        assertEquals(expectedTiles.size(), result.tiles().size());
        assertEquals(expectedTiles.size(), segmentsReturned.size());
        for (var i = 0; i < expectedTiles.size(); i++) {
            testTileHit(expectedTiles.get(i), i, result);
        }
    }

    @Test
    public void testGetVisibilityAtAngleNegSlopeBelowOneSouthwest_2() {
        var origin = randomCoordinate3dInNormalRange();
        var destination = addOffsets2d(origin.to2d(), -8, 2);

        var result = rayCalculation.castRay(origin, destination);

        var expectedTiles = listOf(
                addOffsets2d(origin.to2d(), 0, 0),
                addOffsets2d(origin.to2d(), -1, 0),
                addOffsets2d(origin.to2d(), -2, 0),
                addOffsets2d(origin.to2d(), -2, 1),
                addOffsets2d(origin.to2d(), -3, 1),
                addOffsets2d(origin.to2d(), -4, 1),
                addOffsets2d(origin.to2d(), -5, 1),
                addOffsets2d(origin.to2d(), -6, 1),
                addOffsets2d(origin.to2d(), -6, 2),
                addOffsets2d(origin.to2d(), -7, 2),
                addOffsets2d(origin.to2d(), -8, 2)
        );
        assertNotNull(result);
        assertEquals(expectedTiles.size(), result.tiles().size());
        assertEquals(expectedTiles.size(), segmentsReturned.size());
        for (var i = 0; i < expectedTiles.size(); i++) {
            testTileHit(expectedTiles.get(i), i, result);
        }
    }

    @Test
    public void testGetVisibilityAtAngleNegSlope1Southwest() {
        var origin = randomCoordinate3dInNormalRange();
        var rayLength = randomIntInRange(2, 10);
        var expectedCursorHits = rayLength + 1;
        var destination = Coordinate2d.of(origin.X - rayLength, origin.Y + rayLength);

        var result = rayCalculation.castRay(origin, destination);

        assertNotNull(result);
        assertEquals(expectedCursorHits, result.tiles().size());
        assertEquals(expectedCursorHits, segmentsReturned.size());
        for (var i = 0; i <= rayLength; i++) {
            testTileHit(Coordinate2d.of(origin.X - i, origin.Y + i), i, result);
        }
    }

    @Test
    public void testGetVisibilityAtAngleNegSlopeAboveOneSouthwest_1() {
        var origin = randomCoordinate3dInNormalRange();
        var destination = addOffsets2d(origin.to2d(), -4, 6);

        var result = rayCalculation.castRay(origin, destination);

        var expectedTiles = listOf(
                addOffsets2d(origin.to2d(), 0, 0),
                addOffsets2d(origin.to2d(), 0, 1),
                addOffsets2d(origin.to2d(), -1, 1),
                addOffsets2d(origin.to2d(), -1, 2),
                addOffsets2d(origin.to2d(), -2, 2),
                addOffsets2d(origin.to2d(), -2, 3),
                addOffsets2d(origin.to2d(), -2, 4),
                addOffsets2d(origin.to2d(), -3, 4),
                addOffsets2d(origin.to2d(), -3, 5),
                addOffsets2d(origin.to2d(), -4, 5),
                addOffsets2d(origin.to2d(), -4, 6)
        );
        assertNotNull(result);
        assertEquals(expectedTiles.size(), result.tiles().size());
        assertEquals(expectedTiles.size(), segmentsReturned.size());
        for (var i = 0; i < expectedTiles.size(); i++) {
            testTileHit(expectedTiles.get(i), i, result);
        }
    }

    @Test
    public void testGetVisibilityAtAngleNegSlopeAboveOneSouthwest_2() {
        var origin = randomCoordinate3dInNormalRange();
        var destination = addOffsets2d(origin.to2d(), -2, -7);

        var result = rayCalculation.castRay(origin, destination);

        var expectedTiles = listOf(
                addOffsets2d(origin.to2d(), 0, 0),
                addOffsets2d(origin.to2d(), 0, -1),
                addOffsets2d(origin.to2d(), 0, -2),
                addOffsets2d(origin.to2d(), -1, -2),
                addOffsets2d(origin.to2d(), -1, -3),
                addOffsets2d(origin.to2d(), -1, -4),
                addOffsets2d(origin.to2d(), -1, -5),
                addOffsets2d(origin.to2d(), -2, -5),
                addOffsets2d(origin.to2d(), -2, -6),
                addOffsets2d(origin.to2d(), -2, -7)
        );
        assertNotNull(result);
        assertEquals(expectedTiles.size(), result.tiles().size());
        assertEquals(expectedTiles.size(), segmentsReturned.size());
        for (var i = 0; i < expectedTiles.size(); i++) {
            testTileHit(expectedTiles.get(i), i, result);
        }
    }

    // 'blockingSegmentDist' implies the number of tiles _away from_ the origin, e.g. a value of
    // 0 implies it's adjacent
    @Test
    public void testVisibilityBlockingSegmentStraightLineEast() {
        movingEast = true;
        movingSouth = false;
        var origin = Coordinate3d.of(0, 0, Z);//randomCoordinate3dInNormalRange();
        var rayLength = randomIntInRange(7, 10);
        var blockingSegmentDist = 1; //randomIntInRange(2, 4);
        var mockBlockingSegment = makeMockSegment(WallSegmentOrientation.VERTICAL,
                addOffsets3d(origin, blockingSegmentDist + 1, 0, 0), true);
        segmentReturnOverrides.put(blockingSegmentDist, mockBlockingSegment);
        var expectedCursorHits = blockingSegmentDist + 1;
        var destination = Coordinate2d.of(origin.X + rayLength, origin.Y);

        var result = rayCalculation.castRay(origin, destination);

        assertNotNull(result);
        assertEquals(expectedCursorHits, result.tiles().size());
        assertEquals(3, result.segments().size());
        assertEquals(expectedCursorHits,
                result.segments().get(HORIZONTAL).size() + result.segments().get(CORNER).size() +
                        result.segments().get(VERTICAL).size());
        for (var i = 0; i < expectedCursorHits; i++) {
            testTileHit(Coordinate2d.of(origin.X + i, origin.Y), i, result);
            System.out.println("I passed a hit!");
        }
    }

    @Test
    public void testCanSeeTileAboveSufficientlyLowLedge() {

    }

    @Test
    public void testCannotSeeTileAboveSufficientlyHighLedge() {

    }

    @Test
    public void testCanSeeTileBeneathSufficientlyShallowCliff() {

    }

    @Test
    public void testCannotSeeTileBeneathSufficientlyDeepCliff() {

    }

    @Test
    public void testTargetAddendBelowDoesNotAdjustTargetAboveOrigin() {

    }

    @Test
    public void testBlockingTileBlocksVisibilityBeneathItself() {

    }

    @Test
    public void testBlockingTileBlocksVisibilityAboveItself() {

    }

    private void testTileHit(Coordinate2d expectedTarget, int index, Result result) {
        verify(mockGameZone).tiles(expectedTarget);
        assertTrue(result.tiles().keySet().stream()
                .anyMatch(loc -> loc.to2d().equals(expectedTarget)));
        assertEquals(expectedTarget, tilesReturned.get(index).item1().to2d());

        verify(mockGameZone).getSegments(expectedTarget);
        var expectedOrientation = segmentsReturned.get(index).getValue0();
        var expectedSegReturned = segmentsReturned.get(index).getValue2();
        assertEquals(segmentsReturned.get(index).getValue1(), expectedSegReturned.location());
        assertTrue(result.segments().get(expectedOrientation)
                .containsValue(segmentsReturned.get(index).getValue2()));
        var expectedSegLoc =
                addOffsets2d(expectedTarget, (movingEast ? 1 : 0), (movingSouth ? 1 : 0));
        assertEquals(expectedSegLoc, expectedSegReturned.location().to2d());
    }

    private WallSegment makeMockSegment(WallSegmentOrientation orientation, Coordinate3d loc,
                                        boolean blocking) {
        var mockSegmentType = mock(WallSegmentType.class);
        when(mockSegmentType.orientation()).thenReturn(orientation);
        when(mockSegmentType.blocksSight()).thenReturn(blocking);
        var mockSegment = mock(WallSegment.class);
        when(mockSegment.getType()).thenReturn(mockSegmentType);
        when(mockSegment.location()).thenReturn(loc);
        return mockSegment;
    }

    private WallSegment makeMockSegment(WallSegmentOrientation orientation, Coordinate3d loc) {
        return makeMockSegment(orientation, loc, false);
    }

    // NB: With excessively high or low values, the floating point calculations used to calculate
    // the tiles covered by a visibility ray no longer work properly, due to floating point
    // rounding issues. This isn't an issue in practice, since no GameZone should need more than
    // 100,000,000 tiles, so this range should be adequate for testing.
    private Coordinate3d randomCoordinate3dInNormalRange() {
        return Coordinate3d.of(
                randomIntInRange(-10000, 10000),
                randomIntInRange(-10000, 10000),
                Z
        );
    }

    @Test
    public void testGetInterfaceName() {
        assertEquals(TileVisibilityRayCalculation.class.getCanonicalName(),
                rayCalculation.getInterfaceName());
    }
}
