package inaugural.soliloquy.ruleset.gameconcepts;

import inaugural.soliloquy.tools.Check;
import org.javatuples.Triplet;
import soliloquy.specs.common.valueobjects.Coordinate2d;
import soliloquy.specs.common.valueobjects.Coordinate3d;
import soliloquy.specs.common.valueobjects.Pair;
import soliloquy.specs.gamestate.entities.GameZone;
import soliloquy.specs.gamestate.entities.Tile;
import soliloquy.specs.gamestate.entities.WallSegment;
import soliloquy.specs.gamestate.entities.WallSegmentOrientation;
import soliloquy.specs.ruleset.gameconcepts.TileVisibilityCalculation;
import soliloquy.specs.ruleset.gameconcepts.TileVisibilityRayCalculation;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static inaugural.soliloquy.tools.collections.Collections.mapOf;
import static inaugural.soliloquy.tools.valueobjects.Coordinate2d.addOffsets2d;
import static inaugural.soliloquy.tools.valueobjects.Pair.pairOf;
import static soliloquy.specs.gamestate.entities.WallSegmentOrientation.*;

public class TileVisibilityRayCalculationImpl implements TileVisibilityRayCalculation {
    private final Supplier<GameZone> GET_GAME_ZONE;
    private final float TARGET_ADDEND_ABOVE;
    private final float TARGET_ADDEND_BELOW;

    public TileVisibilityRayCalculationImpl(Supplier<GameZone> getGameZone,
                                            float targetAddendAbove, float targetAddendBelow) {
        GET_GAME_ZONE = Check.ifNull(getGameZone, "getGameZone");
        TARGET_ADDEND_ABOVE = Check.throwOnLtValue(targetAddendAbove, 0f, "targetAddendAbove");
        TARGET_ADDEND_BELOW =
                Check.throwOnLtValue(targetAddendBelow, 0f, "targetAddendBelow");
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
        segmentsInRay.put(VERTICAL, mapOf());
        var gameZone = GET_GAME_ZONE.get();

        var rise = (float) target.Y - origin.Y;
        var run = (float) target.X - origin.X;
        var slope = rise / run;
        var incX = run > 0 ? 1 : -1;
        var halfIncX = incX / 2f;
        var incY = rise > 0 ? 1 : -1;
        var halfIncY = incY / 2f;
        System.out.println("Slope = " + slope);

        var blockingSlopesInXYZSpace = new BlockingSlopesInXYZSpace();

        var origin2d = origin.to2d();
        var cursor = origin2d;
        var cursorHitTarget = false;
        do {
            if (cursor.equals(target)) {
                cursorHitTarget = true;
            }
            System.out.printf("Cursor loc = [%d, %d]%n", cursor.X, cursor.Y);
            var tilesAtCursor = gameZone.tiles(cursor);
            tilesAtCursor.stream().filter(t -> blockingSlopesInXYZSpace.tileIsVisible(origin, t))
                    .forEach(t -> tilesInRay.put(t.location(), t));

            var segmentsAtCursor = gameZone.getSegments(cursor);
            segmentsAtCursor.forEach((o, segs) -> segs.entrySet().stream()
                    .filter(s -> blockingSlopesInXYZSpace.segmentIsVisible(origin, o, s.getKey()))
                    .forEach(s -> segmentsInRay.get(o).put(s.getKey(), s.getValue())));

            var nextCursorInfo =
                    nextCursor(origin2d, cursor, slope, incX, incY, halfIncX, halfIncY);
            cursor = nextCursorInfo.getValue0();
            if (nextCursorInfo.getValue1() != null) {
                var blockingSegments = segmentsAtCursor.entrySet().stream()
                        .filter(e -> e.getKey() == nextCursorInfo.getValue1())
                        .map(Map.Entry::getValue)
                        .toList().get(0).entrySet().stream()
                        .filter(e -> e.getKey().to2d().equals(nextCursorInfo.getValue2()) &&
                                e.getValue().getType().blocksSight()).map(Map.Entry::getValue)
                        .collect(Collectors.toSet());
                blockingSlopesInXYZSpace.addBlockingSegments(origin, blockingSegments,
                        nextCursorInfo.getValue1(), nextCursorInfo.getValue2());
            }
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

    private Triplet<Coordinate2d, WallSegmentOrientation, Coordinate2d> nextCursor(
            Coordinate2d origin, Coordinate2d cursor, float slope, int incX, int incY,
            float halfIncX, float halfIncY) {
        if (slopeIsHorizontal(slope)) {
            var newCursor = addOffsets2d(cursor, incX, 0);
            var segmentX = cursor.X + (incX > 0 ? 1 : 0);
            return Triplet.with(newCursor, VERTICAL,
                    Coordinate2d.of(segmentX, cursor.Y));
        }
        if (slopeIsStraightDown(slope)) {
            return Triplet.with(addOffsets2d(cursor, 0, incY), HORIZONTAL,
                    addOffsets2d(cursor, 0, incY));
        }
        if (slopeIsStraightUp(slope)) {
            return Triplet.with(addOffsets2d(cursor, 0, incY), HORIZONTAL, cursor);
        }
        if (slopeIsDiagonal(slope)) {
            var segX = cursor.X + (incX > 0 ? 1 : 0);
            var segY = cursor.Y + (incY < 0 ? 1 : 0);
            return Triplet.with(addOffsets2d(cursor, incX, incY), CORNER,
                    Coordinate2d.of(segX, segY));
        }

        var nextVertInterceptX = cursor.X + halfIncX;
        System.out.println("nextVertInterceptX = " + nextVertInterceptX);
        var nextVertInterceptRun = nextVertInterceptX - origin.X;
        System.out.println("nextVertInterceptRun = " + nextVertInterceptRun);
        var nextVertInterceptY = (slope * nextVertInterceptRun) + origin.Y;
        System.out.println("nextVertInterceptY = " + nextVertInterceptY);

        // (If incY == 0, then slope is also 0)
        if ((incY > 0 && nextVertInterceptY >= cursor.Y + halfIncY) ||
                (incY < 0 && nextVertInterceptY <= cursor.Y + halfIncY)) {
            System.out.println("incY");
            return Triplet.with(addOffsets2d(cursor, 0, incY), null, null);
        }
        else {
            System.out.println("incX");
            return Triplet.with(addOffsets2d(cursor, incX, 0), null, null);
        }
    }

    private boolean slopeIsHorizontal(float slope) {
        return slope == 0;
    }

    private boolean slopeIsStraightDown(float slope) {
        return slope == Float.NEGATIVE_INFINITY;
    }

    private boolean slopeIsStraightUp(float slope) {
        return slope == Float.POSITIVE_INFINITY;
    }

    private boolean slopeIsDiagonal(float slope) {
        return slope == 1 || slope == -1;
    }

    private float slope3d(Coordinate3d c1, float x2, float y2, float z2) {
        return slope3d(z2, c1.Z, runInXYZSpace(c1.X, x2, c1.Y, y2));
    }

    private float runInXYZSpace(float x1, float x2, float y1, float y2) {
        var xDist = x2 - x1;
        var yDist = y2 - y1;
        return (float) Math.sqrt(Math.pow(xDist, 2) + Math.pow(yDist, 2));
    }

    private float slope3d(float z1, float z2, float runInXYZSpace) {
        var riseInXYZSpace = z2 - z1;
        return riseInXYZSpace / runInXYZSpace;
    }

    @Override
    public String getInterfaceName() {
        return TileVisibilityRayCalculation.class.getCanonicalName();
    }

    protected class BlockingSlopesInXYZSpace {
        // Each of the RANGES are top-down; i.e., item1 > item2
        private final ArrayList<Pair<Float, Float>> RANGES = new ArrayList<>();

        protected void addBlockingSegments(Coordinate3d origin, Set<WallSegment> segments,
                                           WallSegmentOrientation orientation,
                                           Coordinate2d segmentsLoc) {
            if (segments.isEmpty()) {
                return;
            }
            // this is an adjustment, since segments are half a tile's distance away its center
            var segmentLocOnTileGrid = segmentLocOnTileGrid(orientation, segmentsLoc);
            var segmentX = segmentLocOnTileGrid.item1();
            var segmentY = segmentLocOnTileGrid.item2();
            var runInXYZSpace = runInXYZSpace(origin.X, segmentX, origin.Y, segmentY);

            var blockingZs = segments.stream().map(s -> s.location().Z);
            var inOrder = new TreeSet<>(blockingZs.toList()).descendingSet();

            Integer rangeStart = null;
            Integer rangeCursor = null;
            for (var z : inOrder) {
                if (rangeStart == null) {
                    rangeStart = rangeCursor = z;
                    continue;
                }
                if (z == rangeCursor - 1) {
                    rangeCursor--;
                    continue;
                }
                addZRange(origin, rangeStart, rangeCursor, runInXYZSpace);
                rangeStart = rangeCursor = z;
            }
            // The loop will *never* wrap up the last range, and if there were no ranges, we'd
            // have exited the method at the start
            //noinspection DataFlowIssue
            addZRange(origin, rangeStart, rangeCursor, runInXYZSpace);
        }

        // Merging overlapping ranges could either save or spend more CPU time, this is untested
        private void addZRange(Coordinate3d origin, int rangeStartZ, int rangeEndZ,
                               float runInXYZSpace) {
            // A z coordinate is at the 'center' of its height;
            var rangeStartWithHeight = rangeStartZ + 0.5f;
            var rangeEndWithHeight = rangeEndZ - 0.5f;
            RANGES.add(pairOf(slope3d(origin.Z, rangeStartWithHeight, runInXYZSpace),
                    slope3d(origin.Z, rangeEndWithHeight, runInXYZSpace)));
        }

        private boolean tileIsVisible(Coordinate3d origin, Tile tile) {
            var loc = tile.location();
            var adjZ = loc.Z;
            var riseInXYZSpace = loc.Z - origin.Z;
            if (riseInXYZSpace > 0) {
                adjZ += TARGET_ADDEND_ABOVE;
            }
            if (riseInXYZSpace < 0) {
                adjZ += TARGET_ADDEND_BELOW;
            }
            return !slopeIsBlocked(slope3d(origin, loc.X, loc.Y, adjZ));
        }

        private boolean segmentIsVisible(Coordinate3d origin, WallSegmentOrientation orientation,
                                         Coordinate3d segmentLoc) {
            var segmentLocOnTileGrid = segmentLocOnTileGrid(orientation, segmentLoc.to2d());
            var slope = slope3d(origin, segmentLocOnTileGrid.item1(), segmentLocOnTileGrid.item2(),
                    segmentLoc.Z);
            return !slopeIsBlocked(slope);
        }

        private Pair<Float, Float> segmentLocOnTileGrid(WallSegmentOrientation orientation,
                                                        Coordinate2d segmentsLoc) {
            return pairOf(segmentsLoc.X - (orientation != HORIZONTAL ? 0.5f : 0),
                    segmentsLoc.Y - (orientation != VERTICAL ? 0.5f : 0));
        }

        @SuppressWarnings("BooleanMethodIsAlwaysInverted")
        private boolean slopeIsBlocked(float slope) {
            return RANGES.stream().anyMatch(r -> r.item1() > slope &&
                    r.item2() < slope);
        }
    }
}
