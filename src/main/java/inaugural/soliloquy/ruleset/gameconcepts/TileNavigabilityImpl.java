package inaugural.soliloquy.ruleset.gameconcepts;

import inaugural.soliloquy.tools.Check;
import soliloquy.specs.common.shared.Direction;
import soliloquy.specs.common.valueobjects.Coordinate3d;
import soliloquy.specs.gamestate.entities.GameZone;
import soliloquy.specs.ruleset.gameconcepts.TileNavigability;

import java.util.Comparator;
import java.util.function.Supplier;

import static inaugural.soliloquy.tools.valueobjects.Coordinate2d.addOffsets2d;
import static soliloquy.specs.gamestate.entities.WallSegmentOrientation.*;

public class TileNavigabilityImpl implements TileNavigability {
    private final Supplier<GameZone> GET_GAME_ZONE;
    private final int DEFAULT_MOVE_COST;
    private final int FREE_ESCALATION;

    public TileNavigabilityImpl(Supplier<GameZone> getGameZone, int defaultMoveCost,
                                int freeEscalation) {
        GET_GAME_ZONE = Check.ifNull(getGameZone, "getGameZone");
        DEFAULT_MOVE_COST = defaultMoveCost;
        FREE_ESCALATION = Check.ifNonNegative(freeEscalation, "freeEscalation");
    }

    @Override
    public Navigability calculate(Coordinate3d origin, Direction direction, int charHeight)
            throws IllegalArgumentException {
        Check.ifNull(origin, "origin");
        Check.ifNull(direction, "direction");
        Check.throwOnLteZero(charHeight, "charHeight");

        var origin2d = origin.to2d();

        var destination2d = switch (direction) {
            case NORTH -> addOffsets2d(origin2d, 0, -1);
            case NORTHEAST -> addOffsets2d(origin2d, 1, -1);
            case EAST -> addOffsets2d(origin2d, 1, 0);
            case SOUTHEAST -> addOffsets2d(origin2d, 1, 1);
            case SOUTH -> addOffsets2d(origin2d, 0, 1);
            case SOUTHWEST -> addOffsets2d(origin2d, -1, 1);
            case WEST -> addOffsets2d(origin2d, -1, 0);
            case NORTHWEST -> addOffsets2d(origin2d, -1, -1);
        };
        var gameZone = GET_GAME_ZONE.get();

        var tilesAtDestination2d = gameZone.tiles(destination2d);
        if (tilesAtDestination2d.isEmpty()) {
            return null;
        }

        var reachableTiles = tilesAtDestination2d.stream()
                .filter(t -> t.location().Z <= origin.Z + FREE_ESCALATION &&
                        t.location().Z >= origin.Z - FREE_ESCALATION);

        var crossedSegments = switch (direction) {
            case NORTH -> gameZone.segments(addOffsets2d(origin2d, 0, 0), HORIZONTAL);
            case NORTHEAST -> gameZone.segments(addOffsets2d(origin2d, 1, 0), CORNER);
            case EAST -> gameZone.segments(addOffsets2d(origin2d, 1, 0), VERTICAL);
            case SOUTHEAST -> gameZone.segments(addOffsets2d(origin2d, 1, 1), CORNER);
            case SOUTH -> gameZone.segments(addOffsets2d(origin2d, 0, 1), HORIZONTAL);
            case SOUTHWEST -> gameZone.segments(addOffsets2d(origin2d, 0, 1), CORNER);
            case WEST -> gameZone.segments(addOffsets2d(origin2d, 0, 0), VERTICAL);
            case NORTHWEST -> gameZone.segments(addOffsets2d(origin2d, 0, 0), CORNER);
        };
        if (!crossedSegments.isEmpty()) {
            var blockingSegments = crossedSegments.stream()
                    .filter(s -> s.getType().blocksMovement())
                    .toList();
            reachableTiles =
                    reachableTiles.filter(tile -> blockingSegments.stream().noneMatch(seg ->
                            seg.location().Z >= tile.location().Z &&
                                    seg.location().Z <= tile.location().Z + charHeight));
        }
        var highestReachableTile = reachableTiles.max(Comparator.comparingInt(t -> t.location().Z));
        if (highestReachableTile.isEmpty()) {
            return null;
        }
        var destination = destination2d.to3d(highestReachableTile.get().location().Z);
        var groundAdditionalMoveCost =
                highestReachableTile.get().getGroundType().additionalMovementCost();
        var fixturesAdditionalMoveCost =
                highestReachableTile.get().fixtures().representation().keySet().stream()
                        .map(f -> f.type().additionalMovementCost())
                        .reduce(0, Integer::sum);
        var cost = DEFAULT_MOVE_COST + groundAdditionalMoveCost + fixturesAdditionalMoveCost;

        return new Navigability() {
            @Override
            public Coordinate3d destination() {
                return destination;
            }

            @Override
            public int cost() {
                return cost;
            }
        };
    }
}
