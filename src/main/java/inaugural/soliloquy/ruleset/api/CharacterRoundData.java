package inaugural.soliloquy.ruleset.api;

public class CharacterRoundData {
    /**
     * The effective Impulse calculated by
     * {@link inaugural.soliloquy.ruleset.gameconcepts.ActiveCharactersProviderImpl} for the
     * Character in question at the start of the round
     * <p>
     * Type: int
     */
    public final static String ROUND_DATA_IMPULSE = "roundDataImpulse";

    /**
     * The AP a {@link soliloquy.specs.gamestate.entities.Character} has in the current round
     * <p>
     * Type: int
     */
    public final static String ROUND_DATA_AP = "roundDataAp";
}
