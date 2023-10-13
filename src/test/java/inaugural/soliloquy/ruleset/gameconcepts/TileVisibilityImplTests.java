package inaugural.soliloquy.ruleset.gameconcepts;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import soliloquy.specs.common.valueobjects.Coordinate2d;
import soliloquy.specs.common.valueobjects.Coordinate3d;
import soliloquy.specs.gamestate.entities.GameZone;
import soliloquy.specs.gamestate.entities.Tile;
import soliloquy.specs.gamestate.entities.WallSegment;
import soliloquy.specs.gamestate.entities.WallSegmentDirection;
import soliloquy.specs.ruleset.gameconcepts.TileVisibility;
import soliloquy.specs.ruleset.gameconcepts.TileVisibilityCalculation;

import java.util.Set;

import static inaugural.soliloquy.tools.collections.Collections.mapOf;
import static inaugural.soliloquy.tools.collections.Collections.setOf;
import static inaugural.soliloquy.tools.random.Random.randomCoordinate2d;
import static inaugural.soliloquy.tools.random.Random.randomInt;
import static inaugural.soliloquy.tools.valueobjects.Pair.pairOf;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TileVisibilityImplTests {
    @Mock private Tile mockTile;
    @Mock private TileVisibilityCalculation mockTileVisibilityCalculation;
    private Set<Coordinate2d> tileVisibilityCalculationTargets;

    private TileVisibility tileVisibility;

    @Before
    public void setUp() {
        tileVisibilityCalculationTargets = setOf();

        tileVisibility = new TileVisibilityImpl(mockTileVisibilityCalculation);
    }

    @Test
    public void testConstructorWithInvalidParams() {
        assertThrows(IllegalArgumentException.class, () -> new TileVisibilityImpl(null));
    }

    @Test
    public void testAtPointWithInvalidParams() {
        assertThrows(IllegalArgumentException.class, () -> tileVisibility.atPoint(null, 0));
        assertThrows(IllegalArgumentException.class,
                () -> tileVisibility.atPoint(mock(Tile.class), -1));
    }

    @Test
    public void testAtPointWithNoRadius() {
        var mockSegmentNw = mock(WallSegment.class);
        var mockSegmentN = mock(WallSegment.class);
        var mockSegmentNe = mock(WallSegment.class);
        var mockSegmentW = mock(WallSegment.class);
        var mockSegmentE = mock(WallSegment.class);
        var mockSegmentSw = mock(WallSegment.class);
        var mockSegmentS = mock(WallSegment.class);
        var mockSegmentSe = mock(WallSegment.class);
        var mockSegmentNwHeight = randomInt();
        var mockSegmentNHeight = randomInt();
        var mockSegmentNeHeight = randomInt();
        var mockSegmentWHeight = randomInt();
        var mockSegmentEHeight = randomInt();
        var mockSegmentSwHeight = randomInt();
        var mockSegmentSHeight = randomInt();
        var mockSegmentSeHeight = randomInt();
        var mockGameZone = mock(GameZone.class);
        var location = randomCoordinate2d();
        var southOfLocation = Coordinate2d.of(location.X, location.Y + 1);
        var westOfLocation = Coordinate2d.of(location.X + 1, location.Y);
        var southwestOfLocation = Coordinate2d.of(location.X + 1, location.Y + 1);
        when(mockGameZone.getSegments(location, WallSegmentDirection.NORTH)).thenReturn(
                mapOf(pairOf(mockSegmentNHeight, mockSegmentN)));
        when(mockGameZone.getSegments(location, WallSegmentDirection.NORTHWEST)).thenReturn(
                mapOf(pairOf(mockSegmentNwHeight, mockSegmentNw)));
        when(mockGameZone.getSegments(location, WallSegmentDirection.WEST)).thenReturn(
                mapOf(pairOf(mockSegmentWHeight, mockSegmentW)));
        when(mockGameZone.getSegments(southOfLocation, WallSegmentDirection.NORTHWEST)).thenReturn(
                mapOf(pairOf(mockSegmentSwHeight, mockSegmentSw)));
        when(mockGameZone.getSegments(southOfLocation, WallSegmentDirection.NORTH)).thenReturn(
                mapOf(pairOf(mockSegmentSHeight, mockSegmentS)));
        when(mockGameZone.getSegments(southwestOfLocation,
                WallSegmentDirection.NORTHWEST)).thenReturn(
                mapOf(pairOf(mockSegmentSeHeight, mockSegmentSe)));
        when(mockGameZone.getSegments(westOfLocation, WallSegmentDirection.WEST)).thenReturn(
                mapOf(pairOf(mockSegmentEHeight, mockSegmentE)));
        when(mockGameZone.getSegments(westOfLocation, WallSegmentDirection.NORTHWEST)).thenReturn(
                mapOf(pairOf(mockSegmentNeHeight, mockSegmentNe)));
        when(mockTile.location()).thenReturn(location);
        when(mockTile.gameZone()).thenReturn(mockGameZone);
        var expectedSegmentsN = setOf(
                Coordinate3d.of(location, mockSegmentNHeight),
                Coordinate3d.of(southOfLocation, mockSegmentSHeight));
        var expectedSegmentsNw = setOf(
                Coordinate3d.of(location, mockSegmentNwHeight),
                Coordinate3d.of(southOfLocation, mockSegmentSwHeight),
                Coordinate3d.of(southwestOfLocation, mockSegmentSeHeight),
                Coordinate3d.of(westOfLocation, mockSegmentNeHeight));
        var expectedSegmentsW = setOf(
                Coordinate3d.of(location, mockSegmentWHeight),
                Coordinate3d.of(westOfLocation, mockSegmentEHeight));
        var expectedSegments = mapOf(
                pairOf(WallSegmentDirection.NORTH, expectedSegmentsN),
                pairOf(WallSegmentDirection.NORTHWEST, expectedSegmentsNw),
                pairOf(WallSegmentDirection.WEST, expectedSegmentsW));

        var result = tileVisibility.atPoint(mockTile, 0);

        assertNotNull(result);
        assertEquals(setOf(location), result.tiles());
        assertEquals(expectedSegments, result.segments());
        verify(mockTile).location();
        verify(mockTile).gameZone();
        verify(mockGameZone, times(8)).getSegments(any(), any());
        verify(mockGameZone).getSegments(location, WallSegmentDirection.NORTH);
        verify(mockGameZone).getSegments(location, WallSegmentDirection.NORTHWEST);
        verify(mockGameZone).getSegments(location, WallSegmentDirection.WEST);
        verify(mockGameZone).getSegments(southOfLocation, WallSegmentDirection.NORTHWEST);
        verify(mockGameZone).getSegments(southOfLocation, WallSegmentDirection.NORTH);
        verify(mockGameZone).getSegments(southwestOfLocation, WallSegmentDirection.NORTHWEST);
        verify(mockGameZone).getSegments(westOfLocation, WallSegmentDirection.NORTHWEST);
        verify(mockGameZone).getSegments(westOfLocation, WallSegmentDirection.WEST);
    }


}
