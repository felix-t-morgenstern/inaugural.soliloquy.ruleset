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
import soliloquy.specs.ruleset.gameconcepts.TileVisibilityCalculation;
import soliloquy.specs.ruleset.gameconcepts.TileVisibilityRayCalculation;

import java.util.Map;
import java.util.Set;

import static inaugural.soliloquy.tools.collections.Collections.mapOf;
import static inaugural.soliloquy.tools.collections.Collections.setOf;
import static inaugural.soliloquy.tools.random.Random.*;
import static inaugural.soliloquy.tools.valueobjects.Pair.pairOf;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TileVisibilityCalculationImplTests {
    @Mock private Tile mockTile;
    @Mock private TileVisibilityRayCalculation mockTileVisibilityRayCalculation;
    private Set<Coordinate3d> tileVisibilityCalculationOrigins;
    private Set<Coordinate2d> locationsVisible;
    private Map<WallSegmentDirection, Set<Coordinate3d>> segmentsVisible;

    // Constraining values to avoid extremely rare inconsistencies near min and max vals
    private final Coordinate2d LOCATION =
            Coordinate2d.of(randomIntInRange(-999, 999), randomIntInRange(-999, 999));
    private final int Z = randomInt();
    private final Coordinate3d ORIGIN = Coordinate3d.of(LOCATION.X, LOCATION.Y, Z);

    private TileVisibilityCalculation tileVisibilityCalculation;

    @Before
    public void setUp() {
        when(mockTile.location()).thenReturn(LOCATION);
        when(mockTile.getHeight()).thenReturn(Z);

        tileVisibilityCalculationOrigins = setOf();
        locationsVisible = setOf();
        segmentsVisible = mapOf(
                pairOf(WallSegmentDirection.NORTH, setOf()),
                pairOf(WallSegmentDirection.NORTHWEST, setOf()),
                pairOf(WallSegmentDirection.WEST, setOf()));

        when(mockTileVisibilityRayCalculation.castRay(any(), any())).thenAnswer(invocation -> {
            var locationVisible = randomCoordinate2d();
            var northSegmentLocation = randomCoordinate3d();
            var northwestSegmentLocation = randomCoordinate3d();
            var westSegmentLocation = randomCoordinate3d();

            locationsVisible.add(locationVisible);
            segmentsVisible.get(WallSegmentDirection.NORTH).add(northSegmentLocation);
            segmentsVisible.get(WallSegmentDirection.NORTHWEST).add(northwestSegmentLocation);
            segmentsVisible.get(WallSegmentDirection.WEST).add(westSegmentLocation);
            tileVisibilityCalculationOrigins.add(invocation.getArgument(0));

            return new TileVisibilityCalculation.Result() {
                private final Set<Coordinate2d> LOCATIONS_VISIBLE = setOf(locationVisible);
                private final Map<WallSegmentDirection, Set<Coordinate3d>> SEGMENTS = mapOf(
                        pairOf(WallSegmentDirection.NORTH, setOf(northSegmentLocation)),
                        pairOf(WallSegmentDirection.NORTHWEST, setOf(northwestSegmentLocation)),
                        pairOf(WallSegmentDirection.WEST, setOf(westSegmentLocation)));

                @Override
                public Set<Coordinate2d> tiles() {
                    return LOCATIONS_VISIBLE;
                }

                @Override
                public Map<WallSegmentDirection, Set<Coordinate3d>> segments() {
                    return SEGMENTS;
                }
            };
        });

        tileVisibilityCalculation = new TileVisibilityCalculationImpl(
                mockTileVisibilityRayCalculation);
    }

    @Test
    public void testConstructorWithInvalidParams() {
        assertThrows(IllegalArgumentException.class, () -> new TileVisibilityCalculationImpl(null));
    }

    @Test
    public void testAtPointWithInvalidParams() {
        assertThrows(IllegalArgumentException.class, () -> tileVisibilityCalculation.atPoint(null, 0));
        assertThrows(IllegalArgumentException.class,
                () -> tileVisibilityCalculation.atPoint(mock(Tile.class), -1));
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

        var result = tileVisibilityCalculation.atPoint(mockTile, 0);

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

    // NB: These are ideal cases for parameterized tests, but JUnit5 only supports primitive
    // parameters or CSV literals
    @Test
    public void testWithVisibilityRadius_1() {
        var result = tileVisibilityCalculation.atPoint(mockTile, 1);

        assertNotNull(result);
        verify(mockTileVisibilityRayCalculation, times(8)).castRay(eq(ORIGIN), any());
        assertEquals(locationsVisible, result.tiles());
        assertEquals(segmentsVisible, result.segments());
        assertEquals(setOf(ORIGIN), tileVisibilityCalculationOrigins);
        setOf(
                Coordinate2d.of(1, 1),
                Coordinate2d.of(1, 0),
                Coordinate2d.of(1, -1),
                Coordinate2d.of(0, 1),
                Coordinate2d.of(0, -1),
                Coordinate2d.of(-1, 1),
                Coordinate2d.of(-1, 0),
                Coordinate2d.of(-1, -1)
        ).forEach(offset ->
                verify(mockTileVisibilityRayCalculation).castRay(eq(ORIGIN),
                        eq(Coordinate2d.of(LOCATION.X + offset.X, LOCATION.Y + offset.Y))));
    }

    @Test
    public void testWithVisibilityRadius_2() {
        var result = tileVisibilityCalculation.atPoint(mockTile, 2);

        assertNotNull(result);
        verify(mockTileVisibilityRayCalculation, times(16)).castRay(eq(ORIGIN), any());
        assertEquals(locationsVisible, result.tiles());
        assertEquals(segmentsVisible, result.segments());
        assertEquals(setOf(ORIGIN), tileVisibilityCalculationOrigins);
        setOf(
                Coordinate2d.of(2, 2),
                Coordinate2d.of(2, 1),
                Coordinate2d.of(2, 0),
                Coordinate2d.of(2, -1),
                Coordinate2d.of(2, -2),
                Coordinate2d.of(1, 2),
                Coordinate2d.of(1, -2),
                Coordinate2d.of(0, 2),
                Coordinate2d.of(0, -2),
                Coordinate2d.of(-1, 2),
                Coordinate2d.of(-1, 2),
                Coordinate2d.of(-2, 2),
                Coordinate2d.of(-2, 1),
                Coordinate2d.of(-2, 0),
                Coordinate2d.of(-2, -1),
                Coordinate2d.of(-2, -2)
        ).forEach(offset ->
                verify(mockTileVisibilityRayCalculation).castRay(eq(ORIGIN),
                        eq(Coordinate2d.of(LOCATION.X + offset.X, LOCATION.Y + offset.Y))));
    }

    @Test
    public void testWithVisibilityRadius_3() {
        var result = tileVisibilityCalculation.atPoint(mockTile, 3);

        assertNotNull(result);
        verify(mockTileVisibilityRayCalculation, times(24)).castRay(eq(ORIGIN), any());
        assertEquals(locationsVisible, result.tiles());
        assertEquals(segmentsVisible, result.segments());
        assertEquals(setOf(ORIGIN), tileVisibilityCalculationOrigins);
        setOf(
                Coordinate2d.of(3, 2),
                Coordinate2d.of(-2, -3),
                Coordinate2d.of(2, 2),
                Coordinate2d.of(-2, -2),
                Coordinate2d.of(2, 3),
                Coordinate2d.of(-3, -2),
                Coordinate2d.of(1, 3),
                Coordinate2d.of(-3, -1),
                Coordinate2d.of(0, 3),
                Coordinate2d.of(-3, 0),
                Coordinate2d.of(-2, 2),
                Coordinate2d.of(-1, 3),
                Coordinate2d.of(-3, 1),
                Coordinate2d.of(-3, 2),
                Coordinate2d.of(-2, 3),
                Coordinate2d.of(3, -2),
                Coordinate2d.of(2, -3),
                Coordinate2d.of(2, -2),
                Coordinate2d.of(1, -3),
                Coordinate2d.of(3, -1),
                Coordinate2d.of(3, 0),
                Coordinate2d.of(0, -3),
                Coordinate2d.of(3, 1),
                Coordinate2d.of(-1, -3)
        ).forEach(offset ->
                verify(mockTileVisibilityRayCalculation).castRay(eq(ORIGIN),
                        eq(Coordinate2d.of(LOCATION.X + offset.X, LOCATION.Y + offset.Y))));
    }

    @Test
    public void testWithVisibilityRadius_20() {
        var result = tileVisibilityCalculation.atPoint(mockTile, 20);

        assertNotNull(result);
        verify(mockTileVisibilityRayCalculation, times(160)).castRay(eq(ORIGIN), any());
        assertEquals(locationsVisible, result.tiles());
        assertEquals(segmentsVisible, result.segments());
        assertEquals(setOf(ORIGIN), tileVisibilityCalculationOrigins);
        setOf(
                Coordinate2d.of(-20, -6),
                Coordinate2d.of(-20, -5),
                Coordinate2d.of(-20, -4),
                Coordinate2d.of(-20, -3),
                Coordinate2d.of(-20, -2),
                Coordinate2d.of(-20, -1),
                Coordinate2d.of(-20, 0),
                Coordinate2d.of(-20, 1),
                Coordinate2d.of(-20, 2),
                Coordinate2d.of(-20, 3),
                Coordinate2d.of(-20, 4),
                Coordinate2d.of(-20, 5),
                Coordinate2d.of(-20, 6),
                Coordinate2d.of(-19, -9),
                Coordinate2d.of(-19, -8),
                Coordinate2d.of(-19, -7),
                Coordinate2d.of(-19, -6),
                Coordinate2d.of(-19, 6),
                Coordinate2d.of(-19, 7),
                Coordinate2d.of(-19, 8),
                Coordinate2d.of(-19, 9),
                Coordinate2d.of(-18, -11),
                Coordinate2d.of(-18, -10),
                Coordinate2d.of(-18, -9),
                Coordinate2d.of(-18, 9),
                Coordinate2d.of(-18, 10),
                Coordinate2d.of(-18, 11),
                Coordinate2d.of(-17, -12),
                Coordinate2d.of(-17, -11),
                Coordinate2d.of(-17, 11),
                Coordinate2d.of(-17, 12),
                Coordinate2d.of(-16, -13),
                Coordinate2d.of(-16, -12),
                Coordinate2d.of(-16, 12),
                Coordinate2d.of(-16, 13),
                Coordinate2d.of(-15, -14),
                Coordinate2d.of(-15, -13),
                Coordinate2d.of(-15, 13),
                Coordinate2d.of(-15, 14),
                Coordinate2d.of(-14, -15),
                Coordinate2d.of(-14, -14),
                Coordinate2d.of(-14, 14),
                Coordinate2d.of(-14, 15),
                Coordinate2d.of(-13, -16),
                Coordinate2d.of(-13, -15),
                Coordinate2d.of(-13, 15),
                Coordinate2d.of(-13, 16),
                Coordinate2d.of(-12, -17),
                Coordinate2d.of(-12, -16),
                Coordinate2d.of(-12, 16),
                Coordinate2d.of(-12, 17),
                Coordinate2d.of(-11, -18),
                Coordinate2d.of(-11, -17),
                Coordinate2d.of(-11, 17),
                Coordinate2d.of(-11, 18),
                Coordinate2d.of(-10, -18),
                Coordinate2d.of(-10, 18),
                Coordinate2d.of(-9, -19),
                Coordinate2d.of(-9, -18),
                Coordinate2d.of(-9, 18),
                Coordinate2d.of(-9, 19),
                Coordinate2d.of(-8, -19),
                Coordinate2d.of(-8, 19),
                Coordinate2d.of(-7, -19),
                Coordinate2d.of(-7, 19),
                Coordinate2d.of(-6, -20),
                Coordinate2d.of(-6, -19),
                Coordinate2d.of(-6, 19),
                Coordinate2d.of(-6, 20),
                Coordinate2d.of(-5, -20),
                Coordinate2d.of(-5, 20),
                Coordinate2d.of(-4, -20),
                Coordinate2d.of(-4, 20),
                Coordinate2d.of(-3, -20),
                Coordinate2d.of(-3, 20),
                Coordinate2d.of(-2, -20),
                Coordinate2d.of(-2, 20),
                Coordinate2d.of(-1, -20),
                Coordinate2d.of(-1, 20),
                Coordinate2d.of(0, -20),
                Coordinate2d.of(0, 20),
                Coordinate2d.of(1, -20),
                Coordinate2d.of(1, 20),
                Coordinate2d.of(2, -20),
                Coordinate2d.of(2, 20),
                Coordinate2d.of(3, -20),
                Coordinate2d.of(3, 20),
                Coordinate2d.of(4, -20),
                Coordinate2d.of(4, 20),
                Coordinate2d.of(5, -20),
                Coordinate2d.of(5, 20),
                Coordinate2d.of(6, -20),
                Coordinate2d.of(6, -19),
                Coordinate2d.of(6, 19),
                Coordinate2d.of(6, 20),
                Coordinate2d.of(7, -19),
                Coordinate2d.of(7, 19),
                Coordinate2d.of(8, -19),
                Coordinate2d.of(8, 19),
                Coordinate2d.of(9, -19),
                Coordinate2d.of(9, -18),
                Coordinate2d.of(9, 18),
                Coordinate2d.of(9, 19),
                Coordinate2d.of(10, -18),
                Coordinate2d.of(10, 18),
                Coordinate2d.of(11, -18),
                Coordinate2d.of(11, -17),
                Coordinate2d.of(11, 17),
                Coordinate2d.of(11, 18),
                Coordinate2d.of(12, -17),
                Coordinate2d.of(12, -16),
                Coordinate2d.of(12, 16),
                Coordinate2d.of(12, 17),
                Coordinate2d.of(13, -16),
                Coordinate2d.of(13, -15),
                Coordinate2d.of(13, 15),
                Coordinate2d.of(13, 16),
                Coordinate2d.of(14, -15),
                Coordinate2d.of(14, -14),
                Coordinate2d.of(14, 14),
                Coordinate2d.of(14, 15),
                Coordinate2d.of(15, -14),
                Coordinate2d.of(15, -13),
                Coordinate2d.of(15, 13),
                Coordinate2d.of(15, 14),
                Coordinate2d.of(16, -13),
                Coordinate2d.of(16, -12),
                Coordinate2d.of(16, 12),
                Coordinate2d.of(16, 13),
                Coordinate2d.of(17, -12),
                Coordinate2d.of(17, -11),
                Coordinate2d.of(17, 11),
                Coordinate2d.of(17, 12),
                Coordinate2d.of(18, -11),
                Coordinate2d.of(18, -10),
                Coordinate2d.of(18, -9),
                Coordinate2d.of(18, 9),
                Coordinate2d.of(18, 10),
                Coordinate2d.of(18, 11),
                Coordinate2d.of(19, -9),
                Coordinate2d.of(19, -8),
                Coordinate2d.of(19, -7),
                Coordinate2d.of(19, -6),
                Coordinate2d.of(19, 6),
                Coordinate2d.of(19, 7),
                Coordinate2d.of(19, 8),
                Coordinate2d.of(19, 9),
                Coordinate2d.of(20, -6),
                Coordinate2d.of(20, -5),
                Coordinate2d.of(20, -4),
                Coordinate2d.of(20, -3),
                Coordinate2d.of(20, -2),
                Coordinate2d.of(20, -1),
                Coordinate2d.of(20, 0),
                Coordinate2d.of(20, 1),
                Coordinate2d.of(20, 2),
                Coordinate2d.of(20, 3),
                Coordinate2d.of(20, 4),
                Coordinate2d.of(20, 5),
                Coordinate2d.of(20, 6)
        ).forEach(offset ->
                verify(mockTileVisibilityRayCalculation).castRay(eq(ORIGIN),
                        eq(Coordinate2d.of(LOCATION.X + offset.X, LOCATION.Y + offset.Y))));
    }
}
