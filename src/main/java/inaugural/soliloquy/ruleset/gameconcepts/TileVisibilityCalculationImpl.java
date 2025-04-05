package inaugural.soliloquy.ruleset.gameconcepts;

import inaugural.soliloquy.tools.Check;
import soliloquy.specs.common.valueobjects.Coordinate2d;
import soliloquy.specs.common.valueobjects.Coordinate3d;
import soliloquy.specs.gamestate.entities.Tile;
import soliloquy.specs.gamestate.entities.WallSegmentOrientation;
import soliloquy.specs.ruleset.gameconcepts.TileVisibilityCalculation;
import soliloquy.specs.ruleset.gameconcepts.TileVisibilityRayCalculation;

import java.util.Map;
import java.util.Set;

import static inaugural.soliloquy.tools.collections.Collections.mapOf;
import static inaugural.soliloquy.tools.collections.Collections.setOf;

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

        var origin = point.location();
        Result result = null;
        for (var offset : offsets) {
            var locationToCalculate = Coordinate2d.of(origin.X + offset.X, origin.Y + offset.Y);
            var aggregand = RAY_CALCULATION.castRay(origin, locationToCalculate);
            if (result == null) {
                result = aggregand;
            } else {
                aggregateResults(result, aggregand);
            }
        }

        return result;
    }

    private static void aggregateResults(Result aggregate, Result aggregand) {
        aggregate.tiles().addAll(aggregand.tiles());
        aggregate.segments().get(WallSegmentOrientation.VERTICAL).addAll(
                aggregand.segments().get(WallSegmentOrientation.VERTICAL));
        aggregate.segments().get(WallSegmentOrientation.CORNER).addAll(
                aggregand.segments().get(WallSegmentOrientation.CORNER));
        aggregate.segments().get(WallSegmentOrientation.HORIZONTAL).addAll(
                aggregand.segments().get(WallSegmentOrientation.HORIZONTAL));
    }
}
