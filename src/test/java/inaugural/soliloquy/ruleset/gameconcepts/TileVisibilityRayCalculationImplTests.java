package inaugural.soliloquy.ruleset.gameconcepts;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import soliloquy.specs.common.valueobjects.Coordinate2d;
import soliloquy.specs.common.valueobjects.Coordinate3d;
import soliloquy.specs.common.valueobjects.Pair;
import soliloquy.specs.gamestate.entities.GameZone;
import soliloquy.specs.gamestate.entities.WallSegment;
import soliloquy.specs.gamestate.entities.WallSegmentOrientation;
import soliloquy.specs.ruleset.gameconcepts.TileVisibilityRayCalculation;

import java.util.List;

import static inaugural.soliloquy.tools.collections.Collections.listOf;
import static inaugural.soliloquy.tools.collections.Collections.mapOf;
import static inaugural.soliloquy.tools.random.Random.randomInt;
import static inaugural.soliloquy.tools.random.Random.randomIntInRange;
import static inaugural.soliloquy.tools.valueobjects.Coordinate2d.addOffsets2d;
import static inaugural.soliloquy.tools.valueobjects.Pair.pairOf;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static soliloquy.specs.ruleset.gameconcepts.TileVisibilityCalculation.Result;

@RunWith(MockitoJUnitRunner.class)
public class TileVisibilityRayCalculationImplTests {
    @Mock private GameZone mockGameZone;

    private List<Pair<WallSegmentOrientation, Pair<Coordinate3d, WallSegment>>> segmentsReturned;

    private TileVisibilityRayCalculation rayCalculation;

    @Before
    public void setUp() {
        segmentsReturned = listOf();
        when(mockGameZone.getSegments(any())).thenAnswer(invocation -> {
            var segment = mock(WallSegment.class);
            var z = randomInt();
            var direction = WallSegmentOrientation.fromValue(randomIntInRange(1, 3));
            Coordinate2d location = invocation.getArgument(0);
            segmentsReturned.add(pairOf(direction, pairOf(location.to3d(z), segment)));
            return mapOf(pairOf(direction, mapOf(pairOf(location.to3d(z), segment))));
        });

        rayCalculation = new TileVisibilityRayCalculationImpl(mockGameZone);
    }

    @Test
    public void testConstructorWithInvalidParams() {
        assertThrows(IllegalArgumentException.class,
                () -> new TileVisibilityRayCalculationImpl(null));
    }

    @Test
    public void testGetVisibilityStraightHorizontalLine() {
        var origin = randomCoordinate3dInNormalRange();
        var rayLength = randomIntInRange(2, 10);
        var expectedNumberOfTiles = rayLength + 1;
        var destination = Coordinate2d.of(origin.X + rayLength, origin.Y);

        var result = rayCalculation.castRay(origin, destination);

        assertNotNull(result);
        assertEquals(expectedNumberOfTiles, result.tiles().size());
        assertEquals(expectedNumberOfTiles, segmentsReturned.size());
        for (var i = 0; i <= rayLength; i++) {
            testTileHit(Coordinate2d.of(origin.X + i, origin.Y), i, result);
        }
    }

    @Test
    public void testGetVisibilityStraightVerticalLine() {
        var origin = randomCoordinate3dInNormalRange();
        var rayLength = randomIntInRange(2, 10);
        var expectedNumberOfTiles = rayLength + 1;
        var destination = Coordinate2d.of(origin.X, origin.Y + rayLength);

        var result = rayCalculation.castRay(origin, destination);

        assertNotNull(result);
        assertEquals(expectedNumberOfTiles, result.tiles().size());
        assertEquals(expectedNumberOfTiles, segmentsReturned.size());
        for (var i = 0; i <= rayLength; i++) {
            testTileHit(Coordinate2d.of(origin.X, origin.Y + i), i, result);
        }
    }

    @Test
    public void testGetVisibilityAtAngleSlopeBelowOne_1() {
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
    public void testGetVisibilityAtAngleSlopeBelowOne_2() {
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

    // NB: With excessively high or low values, the floating point calculations used to calculate
    // the tiles covered by a visibility ray no longer work properly, due to floating point
    // rounding issues. This isn't an issue in practice, since no GameZone should need more than
    // 100,000 tiles, so this range should be adequate for testing.
    private Coordinate3d randomCoordinate3dInNormalRange() {
        return Coordinate3d.of(
                randomIntInRange(-10000, 10000),
                randomIntInRange(-10000, 10000),
                randomInt()
        );
    }

    private void testTileHit(Coordinate2d expectedTarget, int index, Result result) {
        verify(mockGameZone).getSegments(expectedTarget);
        assertTrue(result.tiles().contains(expectedTarget));
        assertTrue(result.segments().get(segmentsReturned.get(index).item1())
                .contains(segmentsReturned.get(index).item2().item1()));
        assertEquals(expectedTarget, segmentsReturned.get(index).item2().item1().to2d());
    }

    @Test
    public void testGetInterfaceName() {
        assertEquals(TileVisibilityRayCalculation.class.getCanonicalName(),
                rayCalculation.getInterfaceName());
    }
}
