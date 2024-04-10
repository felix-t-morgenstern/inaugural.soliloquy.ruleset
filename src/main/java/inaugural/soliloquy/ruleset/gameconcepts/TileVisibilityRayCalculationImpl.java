package inaugural.soliloquy.ruleset.gameconcepts;

import inaugural.soliloquy.tools.Check;
import soliloquy.specs.common.valueobjects.Coordinate2d;
import soliloquy.specs.common.valueobjects.Coordinate3d;
import soliloquy.specs.gamestate.entities.GameZone;
import soliloquy.specs.gamestate.entities.WallSegmentDirection;
import soliloquy.specs.ruleset.gameconcepts.TileVisibilityCalculation;
import soliloquy.specs.ruleset.gameconcepts.TileVisibilityRayCalculation;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static inaugural.soliloquy.tools.collections.Collections.*;
import static inaugural.soliloquy.tools.valueobjects.Coordinate2d.addOffsets2d;
import static soliloquy.specs.gamestate.entities.WallSegmentDirection.*;

public class TileVisibilityRayCalculationImpl implements TileVisibilityRayCalculation {
    private final GameZone GAME_ZONE;

    public TileVisibilityRayCalculationImpl(GameZone gameZone) {
        GAME_ZONE = Check.ifNull(gameZone, "gameZone");
    }

    @Override
    public TileVisibilityCalculation.Result castRay(Coordinate3d origin, Coordinate2d target)
            throws IllegalArgumentException {
        Set<Coordinate2d> resultTiles = setOf();
        Map<WallSegmentDirection, Set<Coordinate3d>> resultSegments = mapOf();
        resultSegments.put(NORTH, setOf());
        resultSegments.put(NORTHWEST, setOf());
        resultSegments.put(WEST, setOf());

        var cursor = origin.to2d();
        var hitTarget = false;
        var rise = (float) (target.Y - origin.Y);
        var run = (float) (target.X - origin.X);
        var slope = rise / run;
        List<Coordinate2d> allOffsets = listOf();

        while (!hitTarget) {
            if (cursor.equals(target)) {
                hitTarget = true;
            }

            var offsets = Coordinate2d.of(cursor.X - origin.X, cursor.Y - origin.Y);
            allOffsets.add(offsets);

            resultTiles.add(cursor);

            GAME_ZONE.getSegments(cursor).forEach((k1, v1) ->
                    v1.forEach((k2, v2) -> resultSegments.get(k1).add(k2)));

            // NB: The next rise is 0.5f up instead of 1.0f up, since whole numbers correspond to
            // centers of tiles, and therefore the borders between them are at 0.5, 1.5, etc.
            var nextX = (float)cursor.X + 1;
            var newRun = nextX - 0.5f - (float)origin.X;
            var nextY = cursor.Y + 1;
            var nextRise = nextY - 0.5f - origin.Y;
            var newRiseAtNextX = slope * newRun;

            if (newRiseAtNextX < nextRise) {
                cursor = addOffsets2d(cursor, 1, 0);
            }
            else if (newRiseAtNextX == nextRise) {
                if (slope < 1f) {
                    if (cursor.Y < nextY) {
                        cursor = addOffsets2d(cursor, 0, 1);
                    }
                    else {
                        cursor = addOffsets2d(cursor, 1, 0);
                    }
                }
                else if (slope == 1f) {
                    cursor = addOffsets2d(cursor, 1, 1);
                }
            }
            else {
                cursor = addOffsets2d(cursor, 0, 1);
            }
        }

        return new TileVisibilityCalculation.Result() {
            @Override
            public Set<Coordinate2d> tiles() {
                return resultTiles;
            }

            @Override
            public Map<WallSegmentDirection, Set<Coordinate3d>> segments() {
                return resultSegments;
            }
        };
    }

    @Override
    public String getInterfaceName() {
        return TileVisibilityRayCalculation.class.getCanonicalName();
    }
}
