package inaugural.soliloquy.ruleset.gameconcepts;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import soliloquy.specs.common.valueobjects.Coordinate2d;
import soliloquy.specs.common.valueobjects.Coordinate3d;
import soliloquy.specs.gamestate.entities.Tile;
import soliloquy.specs.gamestate.entities.WallSegmentOrientation;
import soliloquy.specs.ruleset.gameconcepts.TileVisibilityCalculation;
import soliloquy.specs.ruleset.gameconcepts.TileVisibilityCalculation.Result;
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
    private Map<WallSegmentOrientation, Set<Coordinate3d>> segmentsVisible;

    // Constraining values to avoid extremely rare calculation inconsistencies near min and max vals
    private final Coordinate3d LOCATION =
            Coordinate3d.of(randomIntInRange(-999, 999), randomIntInRange(-999, 999), randomInt());
    private final Coordinate3d ORIGIN = Coordinate3d.of(LOCATION.X, LOCATION.Y, LOCATION.Z);

    private TileVisibilityCalculation tileVisibilityCalculation;

    @Before
    public void setUp() {
        when(mockTile.location()).thenReturn(LOCATION);

        tileVisibilityCalculationOrigins = setOf();
        locationsVisible = setOf();
        segmentsVisible = mapOf(
                pairOf(WallSegmentOrientation.VERTICAL, setOf()),
                pairOf(WallSegmentOrientation.CORNER, setOf()),
                pairOf(WallSegmentOrientation.HORIZONTAL, setOf()));

        when(mockTileVisibilityRayCalculation.castRay(any(), any())).thenAnswer(invocation -> {
            var locationVisible = randomCoordinate2d();
            var northSegmentLocation = randomCoordinate3d();
            var northwestSegmentLocation = randomCoordinate3d();
            var westSegmentLocation = randomCoordinate3d();

            locationsVisible.add(locationVisible);
            segmentsVisible.get(WallSegmentOrientation.VERTICAL).add(northSegmentLocation);
            segmentsVisible.get(WallSegmentOrientation.CORNER).add(northwestSegmentLocation);
            segmentsVisible.get(WallSegmentOrientation.HORIZONTAL).add(westSegmentLocation);
            tileVisibilityCalculationOrigins.add(invocation.getArgument(0));

            return new TileVisibilityCalculation.Result() {
                private final Set<Coordinate2d> LOCATIONS_VISIBLE = setOf(locationVisible);
                private final Map<WallSegmentOrientation, Set<Coordinate3d>> SEGMENTS = mapOf(
                        pairOf(WallSegmentOrientation.VERTICAL, setOf(northSegmentLocation)),
                        pairOf(WallSegmentOrientation.CORNER, setOf(northwestSegmentLocation)),
                        pairOf(WallSegmentOrientation.HORIZONTAL, setOf(westSegmentLocation)));

                @Override
                public Set<Coordinate2d> tiles() {
                    return LOCATIONS_VISIBLE;
                }

                @Override
                public Map<WallSegmentOrientation, Set<Coordinate3d>> segments() {
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
        assertThrows(IllegalArgumentException.class,
                () -> tileVisibilityCalculation.atPoint(null, 0));
        assertThrows(IllegalArgumentException.class,
                () -> tileVisibilityCalculation.atPoint(mock(Tile.class), -1));
    }

    // NB: IF YOU CHANGE HOW RADII ARE CALCULATED, MANUALLY TEST YOUR RESULTS, TO ENSURE THAT THE
    // EXPECTED RAY TARGETS ARE WHAT YOU EXPECT.
    // Also, these are ideal cases for parameterized tests, but JUnit5 only supports primitive
    // parameters or CSV literals
    @Test
    public void testWithVisibilityRadius_0() {
        var result = tileVisibilityCalculation.atPoint(mockTile, 0);

        assertForRadius(result, 1, setOf(
                Coordinate2d.of(0, 0)
        ));
    }

    @Test
    public void testWithVisibilityRadius_1() {
        var result = tileVisibilityCalculation.atPoint(mockTile, 1);

        assertForRadius(result, 8, setOf(
                Coordinate2d.of(1, 1),
                Coordinate2d.of(1, 0),
                Coordinate2d.of(1, -1),
                Coordinate2d.of(0, 1),
                Coordinate2d.of(0, -1),
                Coordinate2d.of(-1, 1),
                Coordinate2d.of(-1, 0),
                Coordinate2d.of(-1, -1)
        ));
    }

    @Test
    public void testWithVisibilityRadius_2() {
        var result = tileVisibilityCalculation.atPoint(mockTile, 2);

        assertForRadius(result, 16, setOf(
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
        ));
    }

    @Test
    public void testWithVisibilityRadius_3() {
        var result = tileVisibilityCalculation.atPoint(mockTile, 3);

        assertForRadius(result, 24, setOf(
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
        ));
    }

    @Test
    public void testWithVisibilityRadius_20() {
        var result = tileVisibilityCalculation.atPoint(mockTile, 20);

        assertForRadius(result, 160, setOf(
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
        ));
    }

    private void assertForRadius(Result result, int expectedRays,
                                 Set<Coordinate2d> expectedRayOffsets) {
        assertNotNull(result);
        verify(mockTileVisibilityRayCalculation, times(expectedRays))
                .castRay(eq(ORIGIN), any());
        assertEquals(locationsVisible, result.tiles());
        assertEquals(segmentsVisible, result.segments());
        assertEquals(setOf(ORIGIN), tileVisibilityCalculationOrigins);
        expectedRayOffsets.forEach(offset ->
                verify(mockTileVisibilityRayCalculation).castRay(eq(ORIGIN),
                        eq(Coordinate2d.of(LOCATION.X + offset.X, LOCATION.Y + offset.Y))));
    }
}
