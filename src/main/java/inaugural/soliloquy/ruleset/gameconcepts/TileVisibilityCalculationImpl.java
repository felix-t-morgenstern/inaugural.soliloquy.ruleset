package inaugural.soliloquy.ruleset.gameconcepts;

import inaugural.soliloquy.tools.Check;
import soliloquy.specs.common.valueobjects.Coordinate2d;
import soliloquy.specs.common.valueobjects.Coordinate3d;
import soliloquy.specs.gamestate.entities.GameZone;
import soliloquy.specs.gamestate.entities.Tile;
import soliloquy.specs.gamestate.entities.WallSegmentDirection;
import soliloquy.specs.ruleset.gameconcepts.TileVisibilityCalculation;
import soliloquy.specs.ruleset.gameconcepts.TileVisibilityRayCalculation;

import java.util.*;

import static inaugural.soliloquy.tools.collections.Collections.*;
import static inaugural.soliloquy.tools.valueobjects.Pair.pairOf;

public class TileVisibilityCalculationImpl implements TileVisibilityCalculation {
    private final TileVisibilityRayCalculation RAY_CALCULATION;
    private final Map<Integer, Set<Coordinate2d>> CACHED_OFFSETS;

    public TileVisibilityCalculationImpl(TileVisibilityRayCalculation rayCalculation) {
        RAY_CALCULATION = Check.ifNull(rayCalculation, "rayCalculation");
        CACHED_OFFSETS = mapOf();
    }

    @Override
    public Result atPoint(Tile point, int visibilityRadius)
            throws IllegalArgumentException {
        Check.ifNull(point, "point");
        Check.throwOnLtValue(visibilityRadius, 0, "visibilityRadius");
        var location = point.location();

        if (visibilityRadius == 0) {
            return zeroRadius(point, location);
        }

        var offsets = CACHED_OFFSETS.get(visibilityRadius);
        if (offsets == null) {
            var offsets45Degrees = setOf(Coordinate2d.of(0, visibilityRadius));

            var radius = (double) visibilityRadius;
            var radiusSquared = Math.pow(radius, 2d);
            var cursorX = 0;
            var cursorY = (int)radius;

            while (cursorX < cursorY) {
                var interceptedY = Math.sqrt(radiusSquared - Math.pow(cursorX + 0.5d, 2d));
                if ((int) Math.ceil(interceptedY) == cursorY) {
                    cursorX += 1;
                }
                else {
                    cursorY -= 1;
                }
                offsets45Degrees.add(Coordinate2d.of(cursorX, cursorY));
            }

            var offsets90Degrees = setOf(offsets45Degrees);
            for (var offset : offsets45Degrees) {
                //noinspection SuspiciousNameCombination
                offsets90Degrees.add(Coordinate2d.of(offset.Y, offset.X));
            }

            offsets = setOf(offsets90Degrees);
            for (var offset : offsets90Degrees) {
                offsets.add(Coordinate2d.of(-offset.X, offset.Y));
                offsets.add(Coordinate2d.of(offset.X, -offset.Y));
                offsets.add(Coordinate2d.of(-offset.X, -offset.Y));
            }

            CACHED_OFFSETS.put(visibilityRadius, offsets);
        }

        var origin = Coordinate3d.of(location.X, location.Y, point.getHeight());
        Result result = null;
        for (var offset : offsets) {
            var locationToCalculate = Coordinate2d.of(location.X + offset.X, location.Y + offset.Y);
            var aggregand = RAY_CALCULATION.castRay(origin, locationToCalculate);
            if (result == null) {
                result = aggregand;
            } else {
                aggregateResults(result, aggregand);
            }
        }

        return result;
    }

    private Result zeroRadius(Tile point, Coordinate2d location) {
        var gameZone = point.gameZone();
        var southOfLocation = Coordinate2d.of(location.X, location.Y + 1);
        var westOfLocation = Coordinate2d.of(location.X + 1, location.Y);
        var southwestOfLocation = Coordinate2d.of(location.X + 1, location.Y + 1);
        Set<Coordinate3d> segmentsN = setOf();
        Set<Coordinate3d> segmentsNw = setOf();
        Set<Coordinate3d> segmentsW = setOf();
        aggregateSegments(segmentsN, gameZone, location, WallSegmentDirection.NORTH);
        aggregateSegments(segmentsN, gameZone, southOfLocation, WallSegmentDirection.NORTH);
        aggregateSegments(segmentsW, gameZone, location, WallSegmentDirection.WEST);
        aggregateSegments(segmentsW, gameZone, westOfLocation, WallSegmentDirection.WEST);
        aggregateSegments(segmentsNw, gameZone, location, WallSegmentDirection.NORTHWEST);
        aggregateSegments(segmentsNw, gameZone, southOfLocation,
                WallSegmentDirection.NORTHWEST);
        aggregateSegments(segmentsNw, gameZone, southwestOfLocation,
                WallSegmentDirection.NORTHWEST);
        aggregateSegments(segmentsNw, gameZone, westOfLocation, WallSegmentDirection.NORTHWEST);
        return new Result() {
            @Override
            public Set<Coordinate2d> tiles() {
                return setOf(location);
            }

            @Override
            public Map<WallSegmentDirection, Set<Coordinate3d>> segments() {
                return mapOf(
                        pairOf(WallSegmentDirection.NORTH, segmentsN),
                        pairOf(WallSegmentDirection.NORTHWEST, segmentsNw),
                        pairOf(WallSegmentDirection.WEST, segmentsW));
            }
        };
    }

    private static void aggregateResults(Result aggregate, Result aggregand) {
        aggregate.tiles().addAll(aggregand.tiles());
        aggregate.segments().get(WallSegmentDirection.NORTH).addAll(
                aggregand.segments().get(WallSegmentDirection.NORTH));
        aggregate.segments().get(WallSegmentDirection.NORTHWEST).addAll(
                aggregand.segments().get(WallSegmentDirection.NORTHWEST));
        aggregate.segments().get(WallSegmentDirection.WEST).addAll(
                aggregand.segments().get(WallSegmentDirection.WEST));
    }

    private static void aggregateSegments(Set<Coordinate3d> aggregate, GameZone gameZone,
                                          Coordinate2d location, WallSegmentDirection direction) {
        var segments = gameZone.getSegments(location, direction);
        segments.keySet().forEach(height -> aggregate.add(Coordinate3d.of(location, height)));
    }
}
