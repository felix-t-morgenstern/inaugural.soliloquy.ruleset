package inaugural.soliloquy.ruleset.gameconcepts;

import inaugural.soliloquy.ruleset.definitions.concepts.CalculationByComponentsDefinition;
import inaugural.soliloquy.ruleset.definitions.concepts.CalculationByComponentsDefinition.TypeComponentDefinition;
import inaugural.soliloquy.ruleset.definitions.concepts.CalculationByComponentsDefinition.TypeDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import soliloquy.specs.gamestate.entities.Character;
import soliloquy.specs.gamestate.entities.CharacterEquipmentSlots;
import soliloquy.specs.gamestate.entities.CharacterStatusEffects;
import soliloquy.specs.gamestate.entities.Item;
import soliloquy.specs.ruleset.entities.abilities.PassiveAbility;
import soliloquy.specs.ruleset.entities.character.StaticStatisticType;
import soliloquy.specs.ruleset.entities.character.StatusEffectType;
import soliloquy.specs.ruleset.entities.character.VariableStatisticType;
import soliloquy.specs.ruleset.gameconcepts.StatisticCalculation;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static inaugural.soliloquy.tools.collections.Collections.arrayOf;
import static inaugural.soliloquy.tools.collections.Collections.mapOf;
import static inaugural.soliloquy.tools.random.Random.*;
import static inaugural.soliloquy.tools.testing.Mock.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static soliloquy.specs.common.valueobjects.Pair.pairOf;

@ExtendWith(MockitoExtension.class)
public class StatisticCalculationImplTests {
    private final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
    private final int DECIMAL_PLACES_FOR_MODIFIERS = 2;
    private final MathContext MATH_CONTEXT = new MathContext(DECIMAL_PLACES_FOR_MODIFIERS);

    private final String SOURCE_PASSIVE_ABILITY_ID = randomString();
    private final String STATUS_EFFECT_TYPE_ID = randomString();
    private final String ITEM_DATA_PARAM = randomString();
    private final int ITEM_DATA_PARAM_VALUE = randomInt();
    private final int STATUS_EFFECT_LEVEL = randomInt();
    private final String CALCULATED_STATIC_STAT_TYPE_ID = randomString();
    private final String CALCULATED_VARIABLE_STAT_TYPE_ID = randomString();
    private final int CALCULATED_STATIC_STAT_TYPE_BASE_AMOUNT = randomInt();
    private final int CALCULATED_VARIABLE_STAT_TYPE_BASE_AMOUNT = randomInt();
    private final float SOURCE_STATIC_STAT_MULTIPLICATION_FACTOR = randomFloat();
    private final float SOURCE_PASSIVE_ABILITY_MULTIPLICATION_FACTOR = randomFloat();
    private final float SOURCE_ITEM_DATA_MULTIPLICATION_FACTOR = randomFloat();
    private final float SOURCE_STATUS_EFFECT_MULTIPLICATION_FACTOR = randomFloat();

    private final BigDecimal EXPECTED_PASSIVE_ABILITY_MODIFIER =
            new BigDecimal(SOURCE_PASSIVE_ABILITY_MULTIPLICATION_FACTOR).round(MATH_CONTEXT);
    private final BigDecimal EXPECTED_ITEM_DATA_MODIFIER = new BigDecimal(
            SOURCE_ITEM_DATA_MULTIPLICATION_FACTOR * ITEM_DATA_PARAM_VALUE).round(MATH_CONTEXT);
    private final BigDecimal EXPECTED_STATUS_EFFECT_MODIFIER =
            new BigDecimal(SOURCE_STATUS_EFFECT_MULTIPLICATION_FACTOR * STATUS_EFFECT_LEVEL)
                    .round(MATH_CONTEXT);
    private final int EXPECTED_STATIC_STAT_VALUE = CALCULATED_STATIC_STAT_TYPE_BASE_AMOUNT +
            (EXPECTED_PASSIVE_ABILITY_MODIFIER
                    .add(EXPECTED_ITEM_DATA_MODIFIER)
                    .add(EXPECTED_STATUS_EFFECT_MODIFIER))
                    .round(MATH_CONTEXT)
                    .intValue();
    private final BigDecimal EXPECTED_STATIC_STAT_MODIFIER = new BigDecimal(
            SOURCE_STATIC_STAT_MULTIPLICATION_FACTOR * EXPECTED_STATIC_STAT_VALUE)
            .round(MATH_CONTEXT);
    private final int EXPECTED_VARIABLE_STAT_VALUE = CALCULATED_VARIABLE_STAT_TYPE_BASE_AMOUNT +
            (EXPECTED_STATIC_STAT_MODIFIER
                    .add(EXPECTED_PASSIVE_ABILITY_MODIFIER)
                    .add(EXPECTED_ITEM_DATA_MODIFIER)
                    .add(EXPECTED_STATUS_EFFECT_MODIFIER))
                    .round(MATH_CONTEXT)
                    .intValue();

    @Mock private StaticStatisticType mockSourceStaticStatType;
    @Mock private PassiveAbility mockSourcePassiveAbility;
    private List<PassiveAbility> mockPassiveAbilities;
    private Function<String, PassiveAbility> mockGetPassiveAbility;
    @Mock private StatusEffectType mockStatusEffectType;
    private Function<String, StatusEffectType> mockGetStatusEffectType;
    @Mock private Map<String, Object> mockItemData;
    @Mock private Item mockItem;
    @Mock private CharacterEquipmentSlots mockEquipmentSlots;
    @Mock private CharacterStatusEffects mockStatusEffects;
    @Mock private Character mockCharacter;


    private Map<String, StaticStatisticType> mockStaticStatTypesToCalculate;
    @Mock private VariableStatisticType mockVariableStatTypeToCalculate;
    private Map<String, VariableStatisticType> mockVariableStatTypesToCalculate;

    private StatisticCalculation statisticCalculation;

    @BeforeEach
    public void setUp() {
        mockGetPassiveAbility = generateMockLookupFunction(
                pairOf(SOURCE_PASSIVE_ABILITY_ID, mockSourcePassiveAbility));

        mockGetStatusEffectType =
                generateMockLookupFunction(pairOf(STATUS_EFFECT_TYPE_ID, mockStatusEffectType));

        lenient().when(mockItemData.get(ITEM_DATA_PARAM)).thenReturn(ITEM_DATA_PARAM_VALUE);

        lenient().when(mockItem.data()).thenReturn(mockItemData);

        mockEquipmentSlots = mock(CharacterEquipmentSlots.class);
        lenient().when(mockEquipmentSlots.representation())
                .thenReturn(mapOf(pairOf(randomString(), mockItem)));

        lenient().when(mockStatusEffects.getStatusEffectLevel(any()))
                .thenReturn(STATUS_EFFECT_LEVEL);

        mockPassiveAbilities = generateMockList(mockSourcePassiveAbility);

        lenient().when(mockCharacter.equipmentSlots()).thenReturn(mockEquipmentSlots);
        lenient().when(mockCharacter.statusEffects()).thenReturn(mockStatusEffects);
        lenient().when(mockCharacter.passiveAbilities()).thenReturn(mockPassiveAbilities);

        mockStaticStatTypesToCalculate = generateMockMap(
                pairOf(CALCULATED_STATIC_STAT_TYPE_ID, mockSourceStaticStatType));

        mockVariableStatTypesToCalculate = generateMockMap(
                pairOf(CALCULATED_VARIABLE_STAT_TYPE_ID, mockVariableStatTypeToCalculate));

        var statisticComponent = new TypeComponentDefinition(CALCULATED_STATIC_STAT_TYPE_ID,
                SOURCE_STATIC_STAT_MULTIPLICATION_FACTOR);
        var passiveAbilityComponent = new TypeComponentDefinition(SOURCE_PASSIVE_ABILITY_ID,
                SOURCE_PASSIVE_ABILITY_MULTIPLICATION_FACTOR);
        var itemDataComponent = new TypeComponentDefinition(ITEM_DATA_PARAM,
                SOURCE_ITEM_DATA_MULTIPLICATION_FACTOR);
        var statusEffectComponent = new TypeComponentDefinition(STATUS_EFFECT_TYPE_ID,
                SOURCE_STATUS_EFFECT_MULTIPLICATION_FACTOR);

        var staticStatTypeCalculationDefinition =
                new TypeDefinition(CALCULATED_STATIC_STAT_TYPE_ID, false,
                        CALCULATED_STATIC_STAT_TYPE_BASE_AMOUNT, arrayOf(),
                        arrayOf(passiveAbilityComponent), arrayOf(itemDataComponent),
                        arrayOf(statusEffectComponent));

        var variableStatTypeCalculationDefinition =
                new TypeDefinition(CALCULATED_VARIABLE_STAT_TYPE_ID, true,
                        CALCULATED_VARIABLE_STAT_TYPE_BASE_AMOUNT, arrayOf(statisticComponent),
                        arrayOf(passiveAbilityComponent), arrayOf(itemDataComponent),
                        arrayOf(statusEffectComponent));

        var definition = new CalculationByComponentsDefinition(
                arrayOf(staticStatTypeCalculationDefinition, variableStatTypeCalculationDefinition),
                ROUNDING_MODE, DECIMAL_PLACES_FOR_MODIFIERS);

        statisticCalculation =
                new StatisticCalculationImpl(definition, mockStaticStatTypesToCalculate,
                        mockVariableStatTypesToCalculate,
                        mockGetPassiveAbility, mockGetStatusEffectType);
    }

    @Test
    public void testConstructorWithInvalidArgs() {
        // NB: There is a condition in which the number of static and variable stats combined
        // must match the number of type definitions. Having this object return a size of zero
        // sidesteps that for this test, except for the last case, see below.
        when(mockStaticStatTypesToCalculate.size()).thenReturn(0);
        var componentWithNullId = new TypeComponentDefinition(null, randomFloat());
        var componentWithEmptyId = new TypeComponentDefinition("", randomFloat());
        var componentWithInvalidId = new TypeComponentDefinition(randomString(), randomFloat());
        var invalidId = randomString();

        assertThrows(IllegalArgumentException.class,
                () -> new StatisticCalculationImpl(null, mockStaticStatTypesToCalculate,
                        mockVariableStatTypesToCalculate,
                        mockGetPassiveAbility, mockGetStatusEffectType));
        assertThrows(IllegalArgumentException.class, () -> new StatisticCalculationImpl(
                new CalculationByComponentsDefinition(null, ROUNDING_MODE, 0),
                mockStaticStatTypesToCalculate, mockVariableStatTypesToCalculate,
                mockGetPassiveAbility, mockGetStatusEffectType));
        assertThrows(IllegalArgumentException.class, () -> new StatisticCalculationImpl(
                new CalculationByComponentsDefinition(arrayOf(), ROUNDING_MODE, 0),
                mockStaticStatTypesToCalculate, mockVariableStatTypesToCalculate,
                mockGetPassiveAbility, mockGetStatusEffectType));
        assertThrows(IllegalArgumentException.class, () -> new StatisticCalculationImpl(
                new CalculationByComponentsDefinition(arrayOf(new TypeDefinition(null, false,
                        CALCULATED_STATIC_STAT_TYPE_BASE_AMOUNT, arrayOf(), arrayOf(), arrayOf(),
                        arrayOf())), ROUNDING_MODE, 0), mockStaticStatTypesToCalculate,
                mockVariableStatTypesToCalculate, mockGetPassiveAbility,
                mockGetStatusEffectType));
        assertThrows(IllegalArgumentException.class, () -> new StatisticCalculationImpl(
                new CalculationByComponentsDefinition(arrayOf(new TypeDefinition("", false,
                        CALCULATED_STATIC_STAT_TYPE_BASE_AMOUNT, arrayOf(), arrayOf(), arrayOf(),
                        arrayOf())), ROUNDING_MODE, 0), mockStaticStatTypesToCalculate,
                mockVariableStatTypesToCalculate, mockGetPassiveAbility,
                mockGetStatusEffectType));
        assertThrows(IllegalArgumentException.class, () -> new StatisticCalculationImpl(
                new CalculationByComponentsDefinition(arrayOf(new TypeDefinition(invalidId, false,
                        CALCULATED_STATIC_STAT_TYPE_BASE_AMOUNT, arrayOf(), arrayOf(), arrayOf(),
                        arrayOf())), ROUNDING_MODE, 0), mockStaticStatTypesToCalculate,
                mockVariableStatTypesToCalculate, mockGetPassiveAbility,
                mockGetStatusEffectType));
        assertThrows(IllegalArgumentException.class, () -> new StatisticCalculationImpl(
                new CalculationByComponentsDefinition(
                        arrayOf(new TypeDefinition(CALCULATED_STATIC_STAT_TYPE_ID, false,
                                CALCULATED_STATIC_STAT_TYPE_BASE_AMOUNT, null, arrayOf(), arrayOf(),
                                arrayOf())), ROUNDING_MODE, 0), mockStaticStatTypesToCalculate,
                mockVariableStatTypesToCalculate, mockGetPassiveAbility,
                mockGetStatusEffectType));
        assertThrows(IllegalArgumentException.class, () -> new StatisticCalculationImpl(
                new CalculationByComponentsDefinition(
                        arrayOf(new TypeDefinition(CALCULATED_STATIC_STAT_TYPE_ID, false,
                                CALCULATED_STATIC_STAT_TYPE_BASE_AMOUNT,
                                arrayOf((TypeComponentDefinition) null), arrayOf(), arrayOf(),
                                arrayOf())), ROUNDING_MODE, 0), mockStaticStatTypesToCalculate,
                mockVariableStatTypesToCalculate, mockGetPassiveAbility,
                mockGetStatusEffectType));
        assertThrows(IllegalArgumentException.class, () -> new StatisticCalculationImpl(
                new CalculationByComponentsDefinition(
                        arrayOf(new TypeDefinition(CALCULATED_STATIC_STAT_TYPE_ID, false,
                                CALCULATED_STATIC_STAT_TYPE_BASE_AMOUNT,
                                arrayOf(componentWithNullId), arrayOf(), arrayOf(), arrayOf())),
                        ROUNDING_MODE, 0), mockStaticStatTypesToCalculate,
                mockVariableStatTypesToCalculate, mockGetPassiveAbility,
                mockGetStatusEffectType));
        assertThrows(IllegalArgumentException.class, () -> new StatisticCalculationImpl(
                new CalculationByComponentsDefinition(
                        arrayOf(new TypeDefinition(CALCULATED_STATIC_STAT_TYPE_ID, false,
                                CALCULATED_STATIC_STAT_TYPE_BASE_AMOUNT,
                                arrayOf(componentWithEmptyId), arrayOf(), arrayOf(), arrayOf())),
                        ROUNDING_MODE, 0), mockStaticStatTypesToCalculate,
                mockVariableStatTypesToCalculate, mockGetPassiveAbility,
                mockGetStatusEffectType));
        assertThrows(IllegalArgumentException.class, () -> new StatisticCalculationImpl(
                new CalculationByComponentsDefinition(
                        arrayOf(new TypeDefinition(CALCULATED_STATIC_STAT_TYPE_ID, false,
                                CALCULATED_STATIC_STAT_TYPE_BASE_AMOUNT,
                                arrayOf(componentWithInvalidId), arrayOf(), arrayOf(), arrayOf())),
                        ROUNDING_MODE, 0), mockStaticStatTypesToCalculate,
                mockVariableStatTypesToCalculate, mockGetPassiveAbility,
                mockGetStatusEffectType));
        assertThrows(IllegalArgumentException.class, () -> new StatisticCalculationImpl(
                new CalculationByComponentsDefinition(
                        arrayOf(new TypeDefinition(CALCULATED_STATIC_STAT_TYPE_ID, false,
                                CALCULATED_STATIC_STAT_TYPE_BASE_AMOUNT, arrayOf(), null, arrayOf(),
                                arrayOf())), ROUNDING_MODE, 0), mockStaticStatTypesToCalculate,
                mockVariableStatTypesToCalculate, mockGetPassiveAbility,
                mockGetStatusEffectType));
        assertThrows(IllegalArgumentException.class, () -> new StatisticCalculationImpl(
                new CalculationByComponentsDefinition(
                        arrayOf(new TypeDefinition(CALCULATED_STATIC_STAT_TYPE_ID, false,
                                CALCULATED_STATIC_STAT_TYPE_BASE_AMOUNT, arrayOf(),
                                arrayOf((TypeComponentDefinition) null), arrayOf(), arrayOf())),
                        ROUNDING_MODE, 0), mockStaticStatTypesToCalculate,
                mockVariableStatTypesToCalculate, mockGetPassiveAbility,
                mockGetStatusEffectType));
        assertThrows(IllegalArgumentException.class, () -> new StatisticCalculationImpl(
                new CalculationByComponentsDefinition(
                        arrayOf(new TypeDefinition(CALCULATED_STATIC_STAT_TYPE_ID, false,
                                CALCULATED_STATIC_STAT_TYPE_BASE_AMOUNT, arrayOf(),
                                arrayOf(componentWithNullId), arrayOf(), arrayOf())), ROUNDING_MODE,
                        0), mockStaticStatTypesToCalculate, mockVariableStatTypesToCalculate,
                mockGetPassiveAbility, mockGetStatusEffectType));
        assertThrows(IllegalArgumentException.class, () -> new StatisticCalculationImpl(
                new CalculationByComponentsDefinition(
                        arrayOf(new TypeDefinition(CALCULATED_STATIC_STAT_TYPE_ID, false,
                                CALCULATED_STATIC_STAT_TYPE_BASE_AMOUNT, arrayOf(),
                                arrayOf(componentWithEmptyId), arrayOf(), arrayOf())),
                        ROUNDING_MODE, 0), mockStaticStatTypesToCalculate,
                mockVariableStatTypesToCalculate, mockGetPassiveAbility,
                mockGetStatusEffectType));
        assertThrows(IllegalArgumentException.class, () -> new StatisticCalculationImpl(
                new CalculationByComponentsDefinition(
                        arrayOf(new TypeDefinition(CALCULATED_STATIC_STAT_TYPE_ID, false,
                                CALCULATED_STATIC_STAT_TYPE_BASE_AMOUNT, arrayOf(),
                                arrayOf(componentWithInvalidId), arrayOf(), arrayOf())),
                        ROUNDING_MODE, 0), mockStaticStatTypesToCalculate,
                mockVariableStatTypesToCalculate, mockGetPassiveAbility,
                mockGetStatusEffectType));
        assertThrows(IllegalArgumentException.class, () -> new StatisticCalculationImpl(
                new CalculationByComponentsDefinition(
                        arrayOf(new TypeDefinition(CALCULATED_STATIC_STAT_TYPE_ID, false,
                                CALCULATED_STATIC_STAT_TYPE_BASE_AMOUNT, arrayOf(), arrayOf(), null,
                                arrayOf())), ROUNDING_MODE, 0), mockStaticStatTypesToCalculate,
                mockVariableStatTypesToCalculate, mockGetPassiveAbility,
                mockGetStatusEffectType));
        assertThrows(IllegalArgumentException.class, () -> new StatisticCalculationImpl(
                new CalculationByComponentsDefinition(
                        arrayOf(new TypeDefinition(CALCULATED_STATIC_STAT_TYPE_ID, false,
                                CALCULATED_STATIC_STAT_TYPE_BASE_AMOUNT, arrayOf(), arrayOf(),
                                arrayOf((TypeComponentDefinition) null), arrayOf())), ROUNDING_MODE,
                        0), mockStaticStatTypesToCalculate, mockVariableStatTypesToCalculate,
                mockGetPassiveAbility, mockGetStatusEffectType));
        assertThrows(IllegalArgumentException.class, () -> new StatisticCalculationImpl(
                new CalculationByComponentsDefinition(
                        arrayOf(new TypeDefinition(CALCULATED_STATIC_STAT_TYPE_ID, false,
                                CALCULATED_STATIC_STAT_TYPE_BASE_AMOUNT, arrayOf(), arrayOf(),
                                arrayOf(componentWithNullId), arrayOf())), ROUNDING_MODE, 0),
                mockStaticStatTypesToCalculate, mockVariableStatTypesToCalculate,
                mockGetPassiveAbility, mockGetStatusEffectType));
        assertThrows(IllegalArgumentException.class, () -> new StatisticCalculationImpl(
                new CalculationByComponentsDefinition(
                        arrayOf(new TypeDefinition(CALCULATED_STATIC_STAT_TYPE_ID, false,
                                CALCULATED_STATIC_STAT_TYPE_BASE_AMOUNT, arrayOf(), arrayOf(),
                                arrayOf(componentWithEmptyId), arrayOf())), ROUNDING_MODE, 0),
                mockStaticStatTypesToCalculate, mockVariableStatTypesToCalculate,
                mockGetPassiveAbility, mockGetStatusEffectType));
        // NB: There is no test for an invalid item data "type"
        assertThrows(IllegalArgumentException.class, () -> new StatisticCalculationImpl(
                new CalculationByComponentsDefinition(
                        arrayOf(new TypeDefinition(CALCULATED_STATIC_STAT_TYPE_ID, false,
                                CALCULATED_STATIC_STAT_TYPE_BASE_AMOUNT, arrayOf(), arrayOf(),
                                arrayOf(), null)), ROUNDING_MODE, 0),
                mockStaticStatTypesToCalculate, mockVariableStatTypesToCalculate,
                mockGetPassiveAbility, mockGetStatusEffectType));
        assertThrows(IllegalArgumentException.class, () -> new StatisticCalculationImpl(
                new CalculationByComponentsDefinition(
                        arrayOf(new TypeDefinition(CALCULATED_STATIC_STAT_TYPE_ID, false,
                                CALCULATED_STATIC_STAT_TYPE_BASE_AMOUNT, arrayOf(), arrayOf(),
                                arrayOf(), arrayOf((TypeComponentDefinition) null))), ROUNDING_MODE,
                        0), mockStaticStatTypesToCalculate, mockVariableStatTypesToCalculate,
                mockGetPassiveAbility, mockGetStatusEffectType));
        assertThrows(IllegalArgumentException.class, () -> new StatisticCalculationImpl(
                new CalculationByComponentsDefinition(
                        arrayOf(new TypeDefinition(CALCULATED_STATIC_STAT_TYPE_ID, false,
                                CALCULATED_STATIC_STAT_TYPE_BASE_AMOUNT, arrayOf(), arrayOf(),
                                arrayOf(), arrayOf(componentWithNullId))), ROUNDING_MODE, 0),
                mockStaticStatTypesToCalculate, mockVariableStatTypesToCalculate,
                mockGetPassiveAbility, mockGetStatusEffectType));
        assertThrows(IllegalArgumentException.class, () -> new StatisticCalculationImpl(
                new CalculationByComponentsDefinition(
                        arrayOf(new TypeDefinition(CALCULATED_STATIC_STAT_TYPE_ID, false,
                                CALCULATED_STATIC_STAT_TYPE_BASE_AMOUNT, arrayOf(), arrayOf(),
                                arrayOf(), arrayOf(componentWithEmptyId))), ROUNDING_MODE, 0),
                mockStaticStatTypesToCalculate, mockVariableStatTypesToCalculate,
                mockGetPassiveAbility, mockGetStatusEffectType));
        assertThrows(IllegalArgumentException.class, () -> new StatisticCalculationImpl(
                new CalculationByComponentsDefinition(
                        arrayOf(new TypeDefinition(CALCULATED_STATIC_STAT_TYPE_ID, false,
                                CALCULATED_STATIC_STAT_TYPE_BASE_AMOUNT, arrayOf(), arrayOf(),
                                arrayOf(), arrayOf(componentWithInvalidId))), ROUNDING_MODE, 0),
                mockStaticStatTypesToCalculate, mockVariableStatTypesToCalculate,
                mockGetPassiveAbility, mockGetStatusEffectType));
        assertThrows(IllegalArgumentException.class, () -> new StatisticCalculationImpl(
                new CalculationByComponentsDefinition(
                        arrayOf(new TypeDefinition(CALCULATED_STATIC_STAT_TYPE_ID, false,
                                CALCULATED_STATIC_STAT_TYPE_BASE_AMOUNT, arrayOf(), arrayOf(),
                                arrayOf(), arrayOf())), null, 0), mockStaticStatTypesToCalculate,
                mockVariableStatTypesToCalculate, mockGetPassiveAbility,
                mockGetStatusEffectType));
        assertThrows(IllegalArgumentException.class, () -> new StatisticCalculationImpl(
                new CalculationByComponentsDefinition(
                        arrayOf(new TypeDefinition(CALCULATED_STATIC_STAT_TYPE_ID, false,
                                CALCULATED_STATIC_STAT_TYPE_BASE_AMOUNT, arrayOf(), arrayOf(),
                                arrayOf(), arrayOf())), ROUNDING_MODE, -1),
                mockStaticStatTypesToCalculate, mockVariableStatTypesToCalculate,
                mockGetPassiveAbility, mockGetStatusEffectType));
        assertThrows(IllegalArgumentException.class, () -> new StatisticCalculationImpl(
                new CalculationByComponentsDefinition(
                        arrayOf(new TypeDefinition(CALCULATED_STATIC_STAT_TYPE_ID, false,
                                CALCULATED_STATIC_STAT_TYPE_BASE_AMOUNT, arrayOf(), arrayOf(),
                                arrayOf(), arrayOf())), ROUNDING_MODE, 0), null,
                mockVariableStatTypesToCalculate, mockGetPassiveAbility,
                mockGetStatusEffectType));
        assertThrows(IllegalArgumentException.class, () -> new StatisticCalculationImpl(
                new CalculationByComponentsDefinition(
                        arrayOf(new TypeDefinition(CALCULATED_STATIC_STAT_TYPE_ID, false,
                                CALCULATED_STATIC_STAT_TYPE_BASE_AMOUNT, arrayOf(), arrayOf(),
                                arrayOf(), arrayOf())), ROUNDING_MODE, 0),
                mockStaticStatTypesToCalculate, null, mockGetPassiveAbility,
                mockGetStatusEffectType));
        assertThrows(IllegalArgumentException.class, () -> new StatisticCalculationImpl(
                new CalculationByComponentsDefinition(
                        arrayOf(new TypeDefinition(CALCULATED_STATIC_STAT_TYPE_ID, false,
                                CALCULATED_STATIC_STAT_TYPE_BASE_AMOUNT, arrayOf(), arrayOf(),
                                arrayOf(), arrayOf())), ROUNDING_MODE, 0),
                mockStaticStatTypesToCalculate, mockVariableStatTypesToCalculate,
                null, mockGetStatusEffectType));
        assertThrows(IllegalArgumentException.class, () -> new StatisticCalculationImpl(
                new CalculationByComponentsDefinition(
                        arrayOf(new TypeDefinition(CALCULATED_STATIC_STAT_TYPE_ID, false,
                                CALCULATED_STATIC_STAT_TYPE_BASE_AMOUNT, arrayOf(), arrayOf(),
                                arrayOf(), arrayOf())), ROUNDING_MODE, 0),
                mockStaticStatTypesToCalculate, mockVariableStatTypesToCalculate,
                mockGetPassiveAbility, null));

        // NB: This tests the condition that the number of TypeDefinitions must equal the size of
        // staticStatTypes and variableStatTypes combined (which in this case is 2)
        when(mockStaticStatTypesToCalculate.size()).thenReturn(1);
        assertThrows(IllegalArgumentException.class, () -> new StatisticCalculationImpl(
                new CalculationByComponentsDefinition(
                        arrayOf(new TypeDefinition(CALCULATED_STATIC_STAT_TYPE_ID, false,
                                CALCULATED_STATIC_STAT_TYPE_BASE_AMOUNT, arrayOf(), arrayOf(),
                                arrayOf(), arrayOf())), ROUNDING_MODE, 0),
                mockStaticStatTypesToCalculate, mockVariableStatTypesToCalculate,
                mockGetPassiveAbility, mockGetStatusEffectType));
    }

    @Test
    public void testCalculateWithDescriptors() {
        var expectedResult = pairOf(EXPECTED_VARIABLE_STAT_VALUE, mapOf(
                pairOf(mockSourceStaticStatType, EXPECTED_STATIC_STAT_MODIFIER),
                pairOf(mockSourcePassiveAbility, EXPECTED_PASSIVE_ABILITY_MODIFIER),
                pairOf(mockItem, EXPECTED_ITEM_DATA_MODIFIER),
                pairOf(mockStatusEffectType, EXPECTED_STATUS_EFFECT_MODIFIER)
        ));

        var calculatedWithDescriptors = statisticCalculation.calculateWithDescriptors(mockCharacter,
                mockVariableStatTypeToCalculate);

        assertNotNull(calculatedWithDescriptors);
        assertEquals(expectedResult, calculatedWithDescriptors);
        assertCalculationStepsInOrder();
    }

    @Test
    public void testCalculateWithDescriptorsWithInvalidArgs() {
        assertThrows(IllegalArgumentException.class,
                () -> statisticCalculation.calculateWithDescriptors(null,
                        mockVariableStatTypeToCalculate));
        assertThrows(IllegalArgumentException.class,
                () -> statisticCalculation.calculateWithDescriptors(mockCharacter, null));
    }

    @Test
    public void testCalculate() {
        var calculated =
                statisticCalculation.calculate(mockCharacter, mockVariableStatTypeToCalculate);

        assertEquals(EXPECTED_VARIABLE_STAT_VALUE, calculated);
        assertCalculationStepsInOrder();
    }

    private void assertCalculationStepsInOrder() {
        var inOrder = Mockito.inOrder(mockStaticStatTypesToCalculate, mockGetPassiveAbility,
                mockCharacter, mockPassiveAbilities, mockEquipmentSlots, mockItem, mockItemData,
                mockGetStatusEffectType, mockStatusEffects);
        // Was already called twice in the constructor for verification
        inOrder.verify(mockStaticStatTypesToCalculate, times(3))
                .get(CALCULATED_STATIC_STAT_TYPE_ID);
        inOrder.verify(mockGetPassiveAbility).apply(SOURCE_PASSIVE_ABILITY_ID);
        inOrder.verify(mockCharacter).passiveAbilities();
        //noinspection ResultOfMethodCallIgnored
        inOrder.verify(mockPassiveAbilities).contains(mockSourcePassiveAbility);
        inOrder.verify(mockCharacter).equipmentSlots();
        inOrder.verify(mockEquipmentSlots).representation();
        inOrder.verify(mockItem).data();
        inOrder.verify(mockItemData).get(ITEM_DATA_PARAM);
        inOrder.verify(mockGetStatusEffectType).apply(STATUS_EFFECT_TYPE_ID);
        inOrder.verify(mockCharacter).statusEffects();
        inOrder.verify(mockStatusEffects).getStatusEffectLevel(mockStatusEffectType);
        //noinspection ResultOfMethodCallIgnored
        inOrder.verify(mockPassiveAbilities).contains(mockSourcePassiveAbility);
        inOrder.verify(mockCharacter).equipmentSlots();
        inOrder.verify(mockEquipmentSlots).representation();
        inOrder.verify(mockItem).data();
        inOrder.verify(mockItemData).get(ITEM_DATA_PARAM);
        inOrder.verify(mockGetStatusEffectType).apply(STATUS_EFFECT_TYPE_ID);
        inOrder.verify(mockCharacter).statusEffects();
        inOrder.verify(mockStatusEffects).getStatusEffectLevel(mockStatusEffectType);
    }

    @Test
    public void testCalculateWithInvalidArgs() {
        assertThrows(IllegalArgumentException.class,
                () -> statisticCalculation.calculate(null, mockVariableStatTypeToCalculate));
        assertThrows(IllegalArgumentException.class,
                () -> statisticCalculation.calculate(mockCharacter, null));
        when(mockCharacter.isDeleted()).thenReturn(true);
        assertThrows(IllegalArgumentException.class,
                () -> statisticCalculation.calculate(mockCharacter,
                        mockVariableStatTypeToCalculate));
    }
}
