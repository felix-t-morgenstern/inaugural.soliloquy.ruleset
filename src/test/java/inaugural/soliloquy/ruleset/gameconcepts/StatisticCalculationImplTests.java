package inaugural.soliloquy.ruleset.gameconcepts;

import inaugural.soliloquy.ruleset.definitions.concepts.StatisticCalculationDefinition;
import inaugural.soliloquy.ruleset.definitions.concepts.StatisticCalculationDefinition.StatisticCalculationStatisticDefinition;
import org.junit.jupiter.api.BeforeEach;
import soliloquy.specs.common.valueobjects.Pair;
import soliloquy.specs.gamestate.entities.Character;
import soliloquy.specs.gamestate.entities.CharacterEquipmentSlots;
import soliloquy.specs.gamestate.entities.Item;
import soliloquy.specs.ruleset.entities.abilities.PassiveAbility;
import soliloquy.specs.ruleset.entities.character.StaticStatisticType;
import soliloquy.specs.ruleset.entities.character.VariableStatisticType;
import soliloquy.specs.ruleset.gameconcepts.StatisticCalculation;

import static inaugural.soliloquy.tools.collections.Collections.listOf;
import static inaugural.soliloquy.tools.collections.Collections.mapOf;
import static inaugural.soliloquy.tools.random.Random.randomString;
import static inaugural.soliloquy.tools.testing.Mock.generateMockWithId;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StatisticCalculationImplTests {
    private final int DECIMAL_PLACES_TO_DISPLAY_FOR_MODIFIERS = 2;
    private final String PASSIVE_ABILITY_ID = randomString();
    private final String ITEM_DATA_PARAM = randomString();

    private PassiveAbility mockPassiveAbility;
    private Item mockItem;
    private CharacterEquipmentSlots mockEquipmentSlots;
    private Character mockCharacter;

    private StaticStatisticType mockStaticStatTypeToCalculate;
    private VariableStatisticType mockVariableStatTypeToCalculate;

    private StatisticCalculationStatisticDefinition staticStatTypeCalculationDefinition;
    private StatisticCalculationStatisticDefinition variableStatTypeCalculationDefinition;
    private StatisticCalculationDefinition definition;

    private StatisticCalculation statisticCalculation;

    @BeforeEach
    void setUp() {
        mockPassiveAbility = generateMockWithId(PassiveAbility.class, PASSIVE_ABILITY_ID);

        mockItem = mock(Item.class);

        mockEquipmentSlots = mock(CharacterEquipmentSlots.class);
        when(mockEquipmentSlots.representation())
                .thenReturn(mapOf(Pair.of(randomString(), mockItem)));

        mockCharacter = mock(Character.class);
        when(mockCharacter.equipmentSlots()).thenReturn(mockEquipmentSlots);
        when(mockCharacter.passiveAbilities()).thenReturn(listOf(mockPassiveAbility));
    }
}
