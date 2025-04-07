package inaugural.soliloquy.ruleset.gameconcepts;

import inaugural.soliloquy.tools.Check;
import soliloquy.specs.common.valueobjects.Coordinate2d;
import soliloquy.specs.common.valueobjects.Coordinate3d;
import soliloquy.specs.gamestate.entities.GameZone;
import soliloquy.specs.gamestate.entities.Tile;
import soliloquy.specs.gamestate.entities.WallSegment;
import soliloquy.specs.gamestate.entities.WallSegmentOrientation;
import soliloquy.specs.ruleset.gameconcepts.TileVisibilityCalculation;
import soliloquy.specs.ruleset.gameconcepts.TileVisibilityRayCalculation;

import java.util.Map;
import java.util.function.Supplier;

import static inaugural.soliloquy.tools.collections.Collections.mapOf;
import static inaugural.soliloquy.tools.valueobjects.Coordinate2d.addOffsets2d;

public class TileVisibilityRayCalculationImpl implements TileVisibilityRayCalculation {
    private final Supplier<GameZone> GET_GAME_ZONE;

    public TileVisibilityRayCalculationImpl(Supplier<GameZone> getGameZone) {
        GET_GAME_ZONE = Check.ifNull(getGameZone, "getGameZone");
    }

    @Override
    public TileVisibilityCalculation.Result castRay(Coordinate3d origin, Coordinate2d target)
            throws IllegalArgumentException {
        System.out.printf("origin.to2d() = [%d, %d]%n", origin.X, origin.Y);
        System.out.printf("target = [%d, %d]%n", target.X, target.Y);
        Map<Coordinate3d, Tile> tilesInRay = mapOf();
        Map<WallSegmentOrientation, Map<Coordinate3d, WallSegment>> segmentsInRay = mapOf();
        segmentsInRay.put(WallSegmentOrientation.HORIZONTAL, mapOf());
        segmentsInRay.put(WallSegmentOrientation.CORNER, mapOf());
        segmentsInRay.put(WallSegmentOrientation.VERTICAL, mapOf());
        var gameZone = GET_GAME_ZONE.get();

        var rise = (float) target.Y - origin.Y;
        var run = (float) target.X - origin.X;
        var slope = rise / run;
        var absSlope = Math.abs(slope);
        var incX = run > 0 ? 1 : -1;
        var halfIncX = incX / 2f;
        var incY = rise > 0 ? 1 : -1;
        var halfIncY = incY / 2f;

        var origin2d = origin.to2d();
        var cursor = origin2d;
        var cursorHitTarget = false;
        do {
            if (cursor.equals(target)) {
                cursorHitTarget = true;
            }
            System.out.printf("Cursor loc = [%d, %d]%n", cursor.X, cursor.Y);
            var tilesAtCursor = gameZone.tiles(cursor);
            tilesAtCursor.forEach(t -> tilesInRay.put(t.location(), t));

            var segmentsAtLocation = gameZone.getSegments(cursor);
            segmentsAtLocation.forEach(
                    (o, segs) -> segs.forEach((l, s) -> segmentsInRay.get(o).put(l, s)));

            cursor = nextCursor(origin2d, cursor, slope, incX, incY, halfIncX, halfIncY);
        } while (!cursorHitTarget);

        return new TileVisibilityCalculation.Result() {
            @Override
            public Map<Coordinate3d, Tile> tiles() {
                return tilesInRay;
            }

            @Override
            public Map<WallSegmentOrientation, Map<Coordinate3d, WallSegment>> segments() {
                return segmentsInRay;
            }
        };
    }

    private Coordinate2d nextCursor(Coordinate2d origin, Coordinate2d cursor,
                                    float slope, int incX, int incY,
                                    float halfIncX, float halfIncY) {
        if (slope == 0) {
            return addOffsets2d(cursor, incX, 0);
        }
        if (slope == Float.NEGATIVE_INFINITY || slope == Float.POSITIVE_INFINITY) {
            return addOffsets2d(cursor, 0, incY);
        }
        if (slope < 1) {
            var nextVertInterceptX = cursor.X + halfIncX;
            var nextVertInterceptRun = nextVertInterceptX - origin.X;
            var nextVertInterceptY = (slope * nextVertInterceptRun) + origin.Y;

            if (Math.abs(nextVertInterceptY) >= Math.abs(cursor.Y + halfIncY)) {
                return addOffsets2d(cursor, 0, incY);
            }
            else {
                return addOffsets2d(cursor, incX, 0);
            }
        }
        return null;
    }

    @Override
    public String getInterfaceName() {
        return TileVisibilityRayCalculation.class.getCanonicalName();
    }
}
