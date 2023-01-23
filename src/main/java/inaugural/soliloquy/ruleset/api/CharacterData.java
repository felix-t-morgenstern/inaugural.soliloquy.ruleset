package inaugural.soliloquy.ruleset.api;

public class CharacterData {
    /**
     * The number of AP inherent to a {@link soliloquy.specs.gamestate.entities.Character},
     * regardless of its stats or status effects. The intention is for its stats and status effects
     * to effect the AP they have on any given round (see {@link CharacterRoundData#ROUND_DATA_AP})
     * <p>
     * Type: int
     */
    public final static String CHARACTER_BASE_AP = "characterBaseAp";

    /**
     * Whether a {@link soliloquy.specs.gamestate.entities.Character} is active, to be included in
     * the next combat round by
     * {@link soliloquy.specs.ruleset.gameconcepts.ActiveCharactersProvider}
     * <p>
     * Type: boolean
     */
    public final static String CHARACTER_IS_INACTIVE = "characterIsInactive";
}
