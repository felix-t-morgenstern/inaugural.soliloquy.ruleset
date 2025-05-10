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
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static inaugural.soliloquy.tools.collections.Collections.mapOf;
import static inaugural.soliloquy.tools.valueobjects.Coordinate2d.addOffsets2d;
import static inaugural.soliloquy.tools.valueobjects.Pair.pairOf;
import static java.util.Comparator.comparing;
import static soliloquy.specs.gamestate.entities.WallSegmentOrientation.*;

// This class contains a great deal of central logic to the ruleset. I can't think of a cleavage
// which wouldn't violate the SRP, so I've tried to declare internal classes where necessary to
// make it more legible, but feel free to whack away at refactoring this.
public class TileVisibilityRayCalculationImpl implements TileVisibilityRayCalculation {
    private final Supplier<GameZone> GET_GAME_ZONE;
    private final Function<Coordinate3d, Integer> GET_VIEW_CEILING;
    private final Function<Coordinate3d, Integer> GET_VIEW_FLOOR;
    private final float Z_ADDEND_BELOW;

    private final static float HALF_INC = 0.5f;

    public TileVisibilityRayCalculationImpl(Supplier<GameZone> getGameZone,
                                            Function<Coordinate3d, Integer> getViewCeiling,
                                            Function<Coordinate3d, Integer> getViewFloor,
                                            float zAddendBelow) {
        GET_GAME_ZONE = Check.ifNull(getGameZone, "getGameZone");
        GET_VIEW_CEILING = Check.ifNull(getViewCeiling, "getViewCeiling");
        GET_VIEW_FLOOR = Check.ifNull(getViewFloor, "getViewFloor");
        Z_ADDEND_BELOW = Check.throwOnLtValue(zAddendBelow, 0, "zAddendBelow");
    }

    @Override
    public TileVisibilityCalculation.Result castRay(Coordinate3d origin, Coordinate2d target)
            throws IllegalArgumentException {
        Map<Coordinate3d, Tile> tilesInRay = mapOf();
        Map<WallSegmentOrientation, Map<Coordinate3d, WallSegment>> segmentsInRay = mapOf();
        segmentsInRay.put(WallSegmentOrientation.HORIZONTAL, mapOf());
        segmentsInRay.put(WallSegmentOrientation.CORNER, mapOf());
        segmentsInRay.put(VERTICAL, mapOf());
        var gameZone = GET_GAME_ZONE.get();

        var riseXY = (float) target.Y - origin.Y;
        var runXY = (float) target.X - origin.X;
        var slopeXY = riseXY / runXY;
        var incX = runXY > 0 ? 1 : -1;
        var halfIncX = incX / 2f;
        var incY = riseXY > 0 ? 1 : -1;
        var halfIncY = incY / 2f;

        var blockingSlopesInXYZSpace = new BlockingSlopesInXYZSpace();

        var origin2d = origin.to2d();
        var cursor = origin2d;
        var cursorHitTarget = false;
        do {
            if (cursor.equals(target)) {
                cursorHitTarget = true;
            }
            var tilesAtCursor = gameZone.tiles(cursor);
            var nextCursorInfo =
                    nextCursor(origin2d, cursor, slopeXY, incX, incY, halfIncX, halfIncY);

            var floorBlockingTile = tilesAtCursor.stream()
                    .filter(t -> t.getGroundType().blocksSight() && t.location().Z <= origin.Z)
                    .max(comparing(t -> t.location().Z));
            var ceilingBlockingTile = tilesAtCursor.stream()
                    .filter(t -> t.getGroundType().blocksSight() && t.location().Z > origin.Z)
                    .min(comparing(t -> t.location().Z));
            // (The ugly multiple nested assignments are to ensure the variables are effectively
            // final for lambdas below)
            Integer floor;
            Integer floorFromZone = GET_VIEW_FLOOR.apply(cursor.to3d(origin.Z));
            if (floorBlockingTile.isPresent()) {
                if (floorFromZone != null) {
                    floor = Math.max(floorFromZone, floorBlockingTile.get().location().Z);
                }
                else {
                    floor = floorBlockingTile.get().location().Z;
                }
            }
            else {
                floor = floorFromZone;
            }
            Integer ceiling;
            Integer ceilingFromZone = GET_VIEW_CEILING.apply(cursor.to3d(origin.Z));
            if (ceilingBlockingTile.isPresent()) {
                if (ceilingFromZone != null) {
                    ceiling = Math.min(ceilingFromZone, ceilingBlockingTile.get().location().Z);
                }
                else {
                    ceiling = ceilingBlockingTile.get().location().Z;
                }
            }
            else {
                ceiling = ceilingFromZone;
            }

            blockingSlopesInXYZSpace.addBlockingTiles(
                    nextCursorInfo.rayEnterX, nextCursorInfo.rayEnterY,
                    nextCursorInfo.rayExitX, nextCursorInfo.rayExitY,
                    origin, floor, ceiling);
            var visibleTilesAtCursor = tilesAtCursor.stream()
                    .filter(t -> blockingSlopesInXYZSpace.tileIsVisible(origin, t));
            if (floor != null) {
                visibleTilesAtCursor = visibleTilesAtCursor.filter(t -> t.location().Z >= floor);
            }
            if (ceiling != null) {
                visibleTilesAtCursor = visibleTilesAtCursor.filter(t -> t.location().Z < ceiling);
            }
            visibleTilesAtCursor.forEach(t -> tilesInRay.put(t.location(), t));

            var segmentsAtCursor = gameZone.segments(cursor);
            segmentsAtCursor.forEach((o, segs) -> segs.entrySet().stream()
                    .filter(s -> blockingSlopesInXYZSpace.segmentIsVisible(origin, o, s.getKey()))
                    .filter(s -> floor == null || s.getKey().Z >= floor)
                    .forEach(s -> segmentsInRay.get(o).put(s.getKey(), s.getValue())));

            cursor = nextCursorInfo.nextCursor;
            if (nextCursorInfo.crossingSegmentsOrientation != null) {
                var blockingSegments = segmentsAtCursor.entrySet().stream()
                        .filter(e -> e.getKey() == nextCursorInfo.crossingSegmentsOrientation)
                        .map(Map.Entry::getValue).toList().get(0).entrySet().stream().filter(e ->
                                e.getKey().to2d().equals(nextCursorInfo.crossingSegmentsLoc2d) &&
                                        e.getValue().getType().blocksSight())
                        .map(Map.Entry::getValue).collect(Collectors.toSet());
                blockingSlopesInXYZSpace.addBlockingSegments(origin, blockingSegments,
                        nextCursorInfo.crossingSegmentsOrientation,
                        nextCursorInfo.crossingSegmentsLoc2d);
            }

            if (blockingSlopesInXYZSpace.rayIsCompletelyBlocked()) {
                cursorHitTarget = true;
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

    private NextCursorInfo nextCursor(Coordinate2d origin, Coordinate2d cursor, float slope,
                                      int incX, int incY, float halfIncX, float halfIncY) {
        var isEast = incX > 0;
        var isSouth = incY > 0;
        if (slopeIsHorizontal(slope)) {
            var newCursor = addOffsets2d(cursor, incX, 0);
            var rayXAdj = (isEast ? HALF_INC : -HALF_INC);
            var rayEnterX = cursor.X - rayXAdj;
            rayEnterX = rayComponentStartingAtOrigin(origin.X, isEast, rayEnterX);
            var rayExitX = cursor.X + rayXAdj;
            var segX = cursor.X + (isEast ? 1 : 0);
            return new NextCursorInfo(newCursor, rayEnterX, cursor.Y, rayExitX, cursor.Y, VERTICAL,
                    Coordinate2d.of(segX, cursor.Y));
        }
        else if (slopeIsVertical(slope)) {
            var rayYAdj = (isSouth ? HALF_INC : -HALF_INC);
            var rayEnterY = cursor.Y - rayYAdj;
            rayEnterY = rayComponentStartingAtOrigin(origin.Y, isSouth, rayEnterY);
            var rayExitY = cursor.Y + rayYAdj;
            var segY = cursor.Y + (isSouth ? 1 : 0);
            return new NextCursorInfo(addOffsets2d(cursor, 0, incY), cursor.X, rayEnterY,
                    cursor.X, rayExitY, HORIZONTAL, Coordinate2d.of(cursor.X, segY));
        }
        else if (slopeIsDiagonal(slope)) {
            var segX = cursor.X + (isEast ? 1 : 0);
            var segY = cursor.Y + (isSouth ? 1 : 0);
            var rayEnterX = cursor.X + (isEast ? -HALF_INC : HALF_INC);
            rayEnterX = rayComponentStartingAtOrigin(origin.X, isEast, rayEnterX);
            var rayExitX = cursor.X + (isEast ? HALF_INC : -HALF_INC);
            var rayEnterY = cursor.Y + (isSouth ? -HALF_INC : HALF_INC);
            rayEnterY = rayComponentStartingAtOrigin(origin.Y, isSouth, rayEnterY);
            var rayExitY = cursor.Y + (isSouth ? HALF_INC : -HALF_INC);
            return new NextCursorInfo(addOffsets2d(cursor, incX, incY), rayEnterX, rayEnterY,
                    rayExitX, rayExitY, CORNER, Coordinate2d.of(segX, segY));
        }

        // (If incY == 0, then slope is also 0)
        return noncardinalCrossingInfo(incX, incY, slope, origin, cursor, halfIncX, halfIncY,
                isEast, isSouth);
    }

    private static float rayComponentStartingAtOrigin(int originComponent, boolean movingPositively,
                                                      float rayEnterComponent) {
        if (movingPositively) {
            rayEnterComponent = Math.max(originComponent, rayEnterComponent);
        }
        else {
            rayEnterComponent = Math.min(originComponent, rayEnterComponent);
        }
        return rayEnterComponent;
    }

    private static NextCursorInfo noncardinalCrossingInfo(int incX, int incY, float slope,
                                                          Coordinate2d origin, Coordinate2d cursor,
                                                          float halfIncX, float halfIncY,
                                                          boolean isEast, boolean isSouth) {
        Coordinate2d nextCursor;
        WallSegmentOrientation crossingOrientation;
        int crossingSegX;
        int crossingSegY;
        float cursorEnterX;
        float cursorEnterY;
        float cursorExitX;
        float cursorExitY;

        var cursorEnterBoundaryY = cursor.Y - halfIncY;
        var cursorExitBoundaryY = cursor.Y + halfIncY;
        var prevVertInterceptX = cursor.X - halfIncX;
        var nextVertInterceptX = cursor.X + halfIncX;
        var prevVertInterceptRun = prevVertInterceptX - origin.X;
        var nextVertInterceptRun = nextVertInterceptX - origin.X;
        var prevVertInterceptY = (slope * prevVertInterceptRun) + origin.Y;
        var nextVertInterceptY = (slope * nextVertInterceptRun) + origin.Y;

        if ((incY > 0 && prevVertInterceptY <= cursorEnterBoundaryY) ||
                (incY < 0 && prevVertInterceptY <= cursorEnterBoundaryY)) {
            cursorEnterY = cursorEnterBoundaryY;
            var cursorEnterYOffsetFromOrigin = cursorEnterY - origin.Y;
            var cursorEnterXOffsetFromOrigin = cursorEnterYOffsetFromOrigin / slope;
            cursorEnterX = origin.X + cursorEnterXOffsetFromOrigin;
        }
        else {
            cursorEnterX = prevVertInterceptX;
            var cursorEnterXOffsetFromOrigin = cursorEnterX - origin.X;
            var cursorEnterYOffsetFromOrigin = slope * cursorEnterXOffsetFromOrigin;
            cursorEnterY = origin.Y + cursorEnterYOffsetFromOrigin;
        }

        if ((incY > 0 && nextVertInterceptY > cursorExitBoundaryY) ||
                (incY < 0 && nextVertInterceptY < cursorExitBoundaryY)) {
            nextCursor = addOffsets2d(cursor, 0, incY);
            crossingOrientation = HORIZONTAL;
            crossingSegX = cursor.X;
            crossingSegY = cursor.Y + (incY > 0 ? 1 : 0);
            cursorExitY = cursorExitBoundaryY;
            var cursorExitYOffsetFromOrigin = cursorExitY - origin.Y;
            var cursorExitXOffsetFromOrigin = cursorExitYOffsetFromOrigin / slope;
            cursorExitX = origin.X + cursorExitXOffsetFromOrigin;
        }
        else if ((incY > 0 && nextVertInterceptY < cursorExitBoundaryY) ||
                (incY < 0 && nextVertInterceptY > cursorExitBoundaryY)) {
            nextCursor = addOffsets2d(cursor, incX, 0);
            crossingOrientation = VERTICAL;
            crossingSegY = cursor.Y;
            crossingSegX = cursor.X + (incX > 0 ? 1 : 0);
            cursorExitX = nextVertInterceptX;
            var cursorExitXOffsetFromOrigin = cursorExitX - origin.X;
            var cursorExitYOffsetFromOrigin = slope * cursorExitXOffsetFromOrigin;
            cursorExitY = origin.Y + cursorExitYOffsetFromOrigin;
        }
        else {
            nextCursor = addOffsets2d(cursor, incX, incY);
            crossingOrientation = CORNER;
            crossingSegX = cursor.X + (incX > 0 ? 1 : 0);
            crossingSegY = cursor.Y + (incY > 0 ? 1 : 0);
            cursorExitX = cursor.X + halfIncX;
            cursorExitY = cursor.Y + halfIncY;
        }

        cursorEnterX = rayComponentStartingAtOrigin(origin.X, isEast, cursorEnterX);
        cursorEnterY = rayComponentStartingAtOrigin(origin.Y, isSouth, cursorEnterY);

        return new NextCursorInfo(nextCursor, cursorEnterX, cursorEnterY, cursorExitX, cursorExitY,
                crossingOrientation, Coordinate2d.of(crossingSegX, crossingSegY));
    }

    private static class NextCursorInfo {
        Coordinate2d nextCursor;
        float rayEnterX;
        float rayEnterY;
        float rayExitX;
        float rayExitY;
        WallSegmentOrientation crossingSegmentsOrientation;
        Coordinate2d crossingSegmentsLoc2d;

        private NextCursorInfo(Coordinate2d nextCursor,
                               float rayEnterX, float rayEnterY, float rayExitX, float rayExitY,
                               WallSegmentOrientation crossingSegmentsOrientation,
                               Coordinate2d crossingSegmentsLoc2d) {
            this.nextCursor = nextCursor;
            this.rayEnterX = rayEnterX;
            this.rayEnterY = rayEnterY;
            this.rayExitX = rayExitX;
            this.rayExitY = rayExitY;
            this.crossingSegmentsOrientation = crossingSegmentsOrientation;
            this.crossingSegmentsLoc2d = crossingSegmentsLoc2d;
        }
    }

    private static boolean slopeIsHorizontal(float slope) {
        return slope == 0;
    }

    private static boolean slopeIsVertical(float slope) {
        return slope == Float.POSITIVE_INFINITY || slope == Float.NEGATIVE_INFINITY;
    }

    private static boolean slopeIsDiagonal(float slope) {
        return slope == 1 || slope == -1;
    }

    private static float slope3d(Coordinate3d c1, float x2, float y2, float z2) {
        return slope3d(c1.Z, z2, runInXYZSpace(c1.X, x2, c1.Y, y2));
    }

    private static float runInXYZSpace(float x1, float x2, float y1, float y2) {
        var xDist = x2 - x1;
        var yDist = y2 - y1;
        return (float) Math.sqrt(Math.pow(xDist, 2) + Math.pow(yDist, 2));
    }

    private static float slope3d(float z1, float z2, float runInXYZSpace) {
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

        private void addBlockingTiles(float rayEnterX, float rayEnterY, float rayExitX,
                                      float rayExitY, Coordinate3d origin,
                                      Integer floorZ, Integer ceilingZ) {
            if (floorZ != null || ceilingZ != null) {
                if (floorZ != null) {
                    var adjFloorZ = floorZ - HALF_INC;
                    addBlockingPlane(rayExitX, rayExitY, rayEnterX, rayEnterY, origin, adjFloorZ);
                }
                if (ceilingZ != null) {
                    var adjCeilingZ = ceilingZ - HALF_INC;
                    addBlockingPlane(rayEnterX, rayEnterY, rayExitX, rayExitY, origin, adjCeilingZ);
                }
            }
        }

        private void addBlockingPlane(float upperSlopeX, float upperSlopeY,
                                      float lowerSlopeX, float lowerSlopeY,
                                      Coordinate3d origin, float z) {
            var upperSlope = slope3d(origin, upperSlopeX, upperSlopeY, z);
            var lowerSlope = slope3d(origin, lowerSlopeX, lowerSlopeY, z);
            addRange(upperSlope, lowerSlope);
        }

        private void addBlockingSegments(Coordinate3d origin, Set<WallSegment> segments,
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
            var rangeMaxWithHeight = rangeStartZ + HALF_INC;
            var rangeMinWithHeight = rangeEndZ - HALF_INC;
            var rangeUpperSlope = slope3d(origin.Z, rangeMaxWithHeight, runInXYZSpace);
            var rangeLowerSlope = slope3d(origin.Z, rangeMinWithHeight, runInXYZSpace);
            addRange(rangeUpperSlope, rangeLowerSlope);
        }

        private void addRange(float upperBound, float lowerBound) {
            var upperBoundToPlace = upperBound;
            var lowerBoundToPlace = lowerBound;
            for (var i = 0; i < RANGES.size(); i++) {
                var rangeUpperBound = RANGES.get(i).item1();
                var rangeLowerBound = RANGES.get(i).item2();

                if (valueIsInRange(upperBound, rangeUpperBound, rangeLowerBound) ||
                        valueIsInRange(lowerBound, rangeUpperBound, rangeLowerBound)) {
                    upperBoundToPlace = Math.max(upperBoundToPlace, rangeUpperBound);
                    lowerBoundToPlace = Math.min(lowerBoundToPlace, rangeLowerBound);
                    RANGES.remove(i);
                    i--;
                }
            }
            RANGES.add(pairOf(upperBoundToPlace, lowerBoundToPlace));
        }

        private boolean valueIsInRange(float val, float rangeUpperBound, float rangeLowerBound) {
            return val >= rangeLowerBound && val <= rangeUpperBound;
        }

        private boolean tileIsVisible(Coordinate3d origin, Tile tile) {
            var adjLoc = viewBottomAdjustedLoc(origin.Z, tile.location());
            var slope = slope3d(origin, adjLoc.getValue0(), adjLoc.getValue1(), adjLoc.getValue2());
            return !slopeIsBlocked(slope);
        }

        private boolean segmentIsVisible(Coordinate3d origin, WallSegmentOrientation orientation,
                                         Coordinate3d segmentLoc) {
            var segmentLocOnTileGrid = segmentLocOnTileGrid(orientation, segmentLoc.to2d());
            var slope = slope3d(origin, segmentLocOnTileGrid.item1(), segmentLocOnTileGrid.item2(),
                    viewBottomAdjustedZ(origin.Z, segmentLoc.Z));
            return !slopeIsBlocked(slope);
        }

        private boolean rayIsCompletelyBlocked() {
            return RANGES.stream().anyMatch(r ->
                    r.item1() == Float.POSITIVE_INFINITY && r.item2() == Float.NEGATIVE_INFINITY);
        }

        private Triplet<Integer, Integer, Float> viewBottomAdjustedLoc(int originZ,
                                                                       Coordinate3d loc) {
            return Triplet.with(loc.X, loc.Y, viewBottomAdjustedZ(originZ, loc.Z));
        }

        private float viewBottomAdjustedZ(int originZ, float targetZ) {
            if (targetZ >= originZ) {
                return targetZ;
            }
            else {
                return Math.min(originZ, targetZ + Z_ADDEND_BELOW);
            }
        }

        private Pair<Float, Float> segmentLocOnTileGrid(WallSegmentOrientation orientation,
                                                        Coordinate2d segmentsLoc) {
            return pairOf(segmentsLoc.X - (orientation != HORIZONTAL ? HALF_INC : 0),
                    segmentsLoc.Y - (orientation != VERTICAL ? HALF_INC : 0));
        }

        @SuppressWarnings("BooleanMethodIsAlwaysInverted")
        private boolean slopeIsBlocked(Float slope) {
            if (Float.isNaN(slope)) {
                return false;
            }
            return RANGES.stream().anyMatch(r -> r.item1() > slope &&
                    r.item2() < slope);
        }
    }
}
