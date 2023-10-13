package inaugural.soliloquy.ruleset.gameconcepts;

import inaugural.soliloquy.tools.Check;
import soliloquy.specs.common.valueobjects.Coordinate2d;
import soliloquy.specs.common.valueobjects.Coordinate3d;
import soliloquy.specs.gamestate.entities.GameZone;
import soliloquy.specs.gamestate.entities.Tile;
import soliloquy.specs.gamestate.entities.WallSegmentDirection;
import soliloquy.specs.ruleset.gameconcepts.TileVisibility;
import soliloquy.specs.ruleset.gameconcepts.TileVisibilityCalculation;

import java.util.Map;
import java.util.Set;

import static inaugural.soliloquy.tools.collections.Collections.mapOf;
import static inaugural.soliloquy.tools.collections.Collections.setOf;
import static inaugural.soliloquy.tools.valueobjects.Pair.pairOf;

public class TileVisibilityImpl implements TileVisibility {
    private final TileVisibilityCalculation CALCULATION;
    private final Map<Integer, Set<Coordinate2d>> CACHED_OFFSETS;

    public TileVisibilityImpl(TileVisibilityCalculation calculation) {
        CALCULATION = Check.ifNull(calculation, "calculation");
        CACHED_OFFSETS = mapOf();
    }

    @Override
    public TileVisibilityCalculation.Result atPoint(Tile point, int visibilityRadius)
            throws IllegalArgumentException {
        Check.ifNull(point, "point");
        Check.throwOnLtValue(visibilityRadius, 0, "visibilityRadius");

        if (visibilityRadius == 0) {
            var gameZone = point.gameZone();
            var location = point.location();
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
            return new TileVisibilityCalculation.Result() {
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

        return null;
    }

    private static void aggregateSegments(Set<Coordinate3d> aggregate, GameZone gameZone,
                                          Coordinate2d location, WallSegmentDirection direction) {
        var segments = gameZone.getSegments(location, direction);
        segments.keySet().forEach(height -> aggregate.add(Coordinate3d.of(location, height)));
    }
}
