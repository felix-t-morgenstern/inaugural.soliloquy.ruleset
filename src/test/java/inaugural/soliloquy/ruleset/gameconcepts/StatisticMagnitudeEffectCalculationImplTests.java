package inaugural.soliloquy.ruleset.gameconcepts;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import soliloquy.specs.common.valueobjects.Pair;
import soliloquy.specs.gamestate.entities.Character;
import soliloquy.specs.gamestate.entities.CharacterStatusEffects;
import soliloquy.specs.ruleset.entities.Element;
import soliloquy.specs.ruleset.entities.actonroundendandcharacterturn.StatisticChangeMagnitude;
import soliloquy.specs.ruleset.entities.actonroundendandcharacterturn.StatisticChangeMagnitude.EffectType;
import soliloquy.specs.ruleset.entities.actonroundendandcharacterturn.StatisticChangeMagnitude.AmountType;
import soliloquy.specs.ruleset.entities.character.StaticStatisticType;
import soliloquy.specs.ruleset.entities.character.StatusEffectType;
import soliloquy.specs.ruleset.entities.character.VariableStatisticType;
import soliloquy.specs.ruleset.gameconcepts.DamageResistanceCalculation;
import soliloquy.specs.ruleset.gameconcepts.StatisticCalculation;
import soliloquy.specs.ruleset.gameconcepts.StatisticMagnitudeEffectCalculation;

import java.math.RoundingMode;
import java.util.function.Supplier;

import static inaugural.soliloquy.tools.random.Random.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class StatisticMagnitudeEffectCalculationImplTests {
    // The numbers used here are totally arbitrary, I'm just restraining the ranges to make test
    // cases more reasonable, and in some cases, not wholly ridiculous.
    private final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
    private final int EFFECT_ENTITY_LEVEL = 3;
    private final double RANDOM_DOUBLE_1 = randomDouble();
    private final double RANDOM_DOUBLE_2 = randomDouble();
    private final double RANDOM_DOUBLE_3 = randomDouble();
    private final double RANDOM_DOUBLE_4 = randomDouble();
    private final int VALUE_MAGNITUDE_PER_LEVEL_MIN = randomIntInRange(4, 10);
    private final int VALUE_MAGNITUDE_PER_LEVEL_MAX =
            randomIntInRange(VALUE_MAGNITUDE_PER_LEVEL_MIN, 20);
    private final int VALUE_MAGNITUDE_ABSOLUTE_MIN = randomIntInRange(8, 16);
    private final int VALUE_MAGNITUDE_ABSOLUTE_MAX =
            randomIntInRange(VALUE_MAGNITUDE_ABSOLUTE_MIN, 32);
    private final double VALUE_ABSOLUTE_COMPONENT =
            RANDOM_DOUBLE_1 * (VALUE_MAGNITUDE_ABSOLUTE_MAX - VALUE_MAGNITUDE_ABSOLUTE_MIN);
    private final double VALUE_PER_LEVEL_COMPONENT_1 =
            RANDOM_DOUBLE_2 * (VALUE_MAGNITUDE_PER_LEVEL_MAX - VALUE_MAGNITUDE_PER_LEVEL_MIN);
    private final double VALUE_PER_LEVEL_COMPONENT_2 =
            RANDOM_DOUBLE_3 * (VALUE_MAGNITUDE_PER_LEVEL_MAX - VALUE_MAGNITUDE_PER_LEVEL_MIN);
    private final double VALUE_PER_LEVEL_COMPONENT_3 =
            RANDOM_DOUBLE_4 * (VALUE_MAGNITUDE_PER_LEVEL_MAX - VALUE_MAGNITUDE_PER_LEVEL_MIN);
    private final double VALUE_BASE_EFFECT =
            VALUE_ABSOLUTE_COMPONENT + VALUE_PER_LEVEL_COMPONENT_1 + VALUE_PER_LEVEL_COMPONENT_2 +
                    VALUE_PER_LEVEL_COMPONENT_3;
    private final int ROUNDED_VALUE_BASE_EFFECT = (int) Math.round(VALUE_BASE_EFFECT);
    private final float PERCENT_MAGNITUDE_PER_LEVEL_MIN = randomFloatInRange(.01f, .04f);
    private final float PERCENT_MAGNITUDE_PER_LEVEL_MAX =
            randomFloatInRange(PERCENT_MAGNITUDE_PER_LEVEL_MIN, 0.20f);
    private final float PERCENT_MAGNITUDE_ABSOLUTE_MIN = randomFloatInRange(0.08f, 0.16f);
    private final float PERCENT_MAGNITUDE_ABSOLUTE_MAX =
            randomFloatInRange(PERCENT_MAGNITUDE_ABSOLUTE_MIN, 0.32f);
    private final double PERCENT_ABSOLUTE_COMPONENT =
            RANDOM_DOUBLE_1 * (PERCENT_MAGNITUDE_ABSOLUTE_MAX - PERCENT_MAGNITUDE_ABSOLUTE_MIN);
    private final double PERCENT_PER_LEVEL_COMPONENT_1 =
            RANDOM_DOUBLE_2 * (PERCENT_MAGNITUDE_PER_LEVEL_MAX - PERCENT_MAGNITUDE_PER_LEVEL_MIN);
    private final double PERCENT_PER_LEVEL_COMPONENT_2 =
            RANDOM_DOUBLE_3 * (PERCENT_MAGNITUDE_PER_LEVEL_MAX - PERCENT_MAGNITUDE_PER_LEVEL_MIN);
    private final double PERCENT_PER_LEVEL_COMPONENT_3 =
            RANDOM_DOUBLE_4 * (PERCENT_MAGNITUDE_PER_LEVEL_MAX - PERCENT_MAGNITUDE_PER_LEVEL_MIN);
    private final double PERCENT_BASE_EFFECT =
            PERCENT_ABSOLUTE_COMPONENT + PERCENT_PER_LEVEL_COMPONENT_1 +
                    PERCENT_PER_LEVEL_COMPONENT_2 + PERCENT_PER_LEVEL_COMPONENT_3;
    private final int EFFECTED_VARIABLE_STAT_MAX_VALUE = randomIntInRange(5000, 20000);
    private final int EFFECTED_VARIABLE_STAT_CURRENT_VALUE = randomIntInRange(4000, 12000);
    private final int ROUNDED_PERCENT_CURRENT_EFFECT =
            (int) Math.round(EFFECTED_VARIABLE_STAT_CURRENT_VALUE * PERCENT_BASE_EFFECT);
    private final int ROUNDED_PERCENT_MAXIMUM_EFFECT =
            (int) Math.round(EFFECTED_VARIABLE_STAT_MAX_VALUE * PERCENT_BASE_EFFECT);
    private final int DAMAGE_RESISTANCE_CALCULATION_OUTPUT = randomInt();

    @Mock DamageResistanceCalculation mockDamageResistanceCalculation;
    @Mock StatisticCalculation mockStatisticCalculation;
    @Mock Supplier<Double> mockRandomDoubleProvider;
    @Mock StaticStatisticType mockStaticStatType;
    @Mock VariableStatisticType mockVariableStatType;
    @Mock StatusEffectType mockStatusEffectType;
    @Mock VariableStatisticType mockEffectedVariableStatType;
    @Mock Element mockElement;
    @Mock Character mockCharacter;
    @Mock CharacterStatusEffects mockCharacterStatusEffects;
    @Mock StatisticChangeMagnitude<Integer> mockValueMagnitude;
    @Mock StatisticChangeMagnitude<Float> mockPercentMagnitude;

    private StatisticMagnitudeEffectCalculation statisticMagnitudeEffectCalculation;

    @Before
    public void setUp() {
        when(mockDamageResistanceCalculation.calculateEffectiveChange(any(), anyInt(), any()))
                .thenReturn(DAMAGE_RESISTANCE_CALCULATION_OUTPUT);

        when(mockRandomDoubleProvider.get())
                .thenReturn(RANDOM_DOUBLE_1)
                .thenReturn(RANDOM_DOUBLE_2)
                .thenReturn(RANDOM_DOUBLE_3)
                .thenReturn(RANDOM_DOUBLE_4);

        when(mockValueMagnitude.element()).thenReturn(mockElement);
        when(mockValueMagnitude.perLevelRange())
                .thenReturn(Pair.of(VALUE_MAGNITUDE_PER_LEVEL_MIN, VALUE_MAGNITUDE_PER_LEVEL_MAX));
        when(mockValueMagnitude.absoluteRange())
                .thenReturn(Pair.of(VALUE_MAGNITUDE_ABSOLUTE_MIN, VALUE_MAGNITUDE_ABSOLUTE_MAX));
        when(mockValueMagnitude.amountType()).thenReturn(AmountType.VALUE);

        when(mockPercentMagnitude.element()).thenReturn(mockElement);
        when(mockPercentMagnitude.absoluteRange()).thenReturn(
                Pair.of(PERCENT_MAGNITUDE_ABSOLUTE_MIN, PERCENT_MAGNITUDE_ABSOLUTE_MAX));
        when(mockPercentMagnitude.perLevelRange()).thenReturn(
                Pair.of(PERCENT_MAGNITUDE_PER_LEVEL_MIN, PERCENT_MAGNITUDE_PER_LEVEL_MAX));
        when(mockPercentMagnitude.effectedStatisticType()).thenReturn(mockEffectedVariableStatType);

        when(mockStatisticCalculation.calculate(any(), any())).thenReturn(EFFECT_ENTITY_LEVEL);
        when(mockStatisticCalculation.calculate(any(),
                same(mockEffectedVariableStatType))).thenReturn(EFFECTED_VARIABLE_STAT_MAX_VALUE);

        when(mockCharacterStatusEffects.getStatusEffectLevel(any()))
                .thenReturn(EFFECT_ENTITY_LEVEL);

        when(mockCharacter.statusEffects()).thenReturn(mockCharacterStatusEffects);
        when(mockCharacter.getVariableStatisticCurrentValue(any()))
                .thenReturn(EFFECTED_VARIABLE_STAT_CURRENT_VALUE);

        statisticMagnitudeEffectCalculation =
                new StatisticMagnitudeEffectCalculationImpl(mockDamageResistanceCalculation,
                        mockStatisticCalculation, mockRandomDoubleProvider);
    }

    @Test
    public void testConstructorWithInvalidParams() {
        assertThrows(IllegalArgumentException.class,
                () -> new StatisticMagnitudeEffectCalculationImpl(null, mockStatisticCalculation,
                        mockRandomDoubleProvider));
        assertThrows(IllegalArgumentException.class,
                () -> new StatisticMagnitudeEffectCalculationImpl(mockDamageResistanceCalculation,
                        null, mockRandomDoubleProvider));
        assertThrows(IllegalArgumentException.class,
                () -> new StatisticMagnitudeEffectCalculationImpl(mockDamageResistanceCalculation,
                        mockStatisticCalculation, null));
    }

    @Test
    public void testGetAlterationValueEffectForStatistic() {
        when(mockValueMagnitude.effectType()).thenReturn(EffectType.ALTERATION);

        var output = statisticMagnitudeEffectCalculation.getEffect(mockStaticStatType,
                mockValueMagnitude, mockCharacter);

        assertEquals(ROUNDED_VALUE_BASE_EFFECT, output);
        verify(mockStatisticCalculation).calculate(mockCharacter, mockStaticStatType);
        verify(mockRandomDoubleProvider, times(EFFECT_ENTITY_LEVEL + 1)).get();
    }

    @Test
    public void testGetDamageValueEffectForStatistic() {
        when(mockValueMagnitude.effectType()).thenReturn(EffectType.DAMAGE);

        var output = statisticMagnitudeEffectCalculation
                .getEffect(mockStaticStatType, mockValueMagnitude, mockCharacter);

        assertEquals(DAMAGE_RESISTANCE_CALCULATION_OUTPUT, output);
        verify(mockStatisticCalculation).calculate(mockCharacter, mockStaticStatType);
        verify(mockRandomDoubleProvider, times(EFFECT_ENTITY_LEVEL + 1)).get();
        verify(mockDamageResistanceCalculation).calculateEffectiveChange(mockCharacter,
                ROUNDED_VALUE_BASE_EFFECT, mockElement);
    }

    @Test
    public void testGetAlterationPercentOfCurrentEffectForStatistic() {
        when(mockPercentMagnitude.effectType()).thenReturn(EffectType.ALTERATION);
        when(mockPercentMagnitude.amountType()).thenReturn(AmountType.PERCENT_OF_CURRENT);

        var output = statisticMagnitudeEffectCalculation
                .getEffect(mockStaticStatType, mockPercentMagnitude, mockCharacter);

        assertEquals(ROUNDED_PERCENT_CURRENT_EFFECT, output);
        verify(mockStatisticCalculation).calculate(mockCharacter, mockStaticStatType);
        verify(mockRandomDoubleProvider, times(EFFECT_ENTITY_LEVEL + 1)).get();
        verify(mockCharacter).getVariableStatisticCurrentValue(mockEffectedVariableStatType);
    }

    @Test
    public void testGetDamagePercentOfCurrentEffectForStatistic() {
        when(mockPercentMagnitude.effectType()).thenReturn(EffectType.DAMAGE);
        when(mockPercentMagnitude.amountType()).thenReturn(AmountType.PERCENT_OF_CURRENT);

        var output = statisticMagnitudeEffectCalculation
                .getEffect(mockStaticStatType, mockPercentMagnitude, mockCharacter);

        assertEquals(DAMAGE_RESISTANCE_CALCULATION_OUTPUT, output);
        verify(mockStatisticCalculation).calculate(mockCharacter, mockStaticStatType);
        verify(mockRandomDoubleProvider, times(EFFECT_ENTITY_LEVEL + 1)).get();
        verify(mockCharacter).getVariableStatisticCurrentValue(mockEffectedVariableStatType);
        verify(mockDamageResistanceCalculation).calculateEffectiveChange(mockCharacter,
                ROUNDED_PERCENT_CURRENT_EFFECT, mockElement);
    }

    @Test
    public void testGetAlterationPercentOfMaximumEffectForStatistic() {
        when(mockPercentMagnitude.effectType()).thenReturn(EffectType.ALTERATION);
        when(mockPercentMagnitude.amountType()).thenReturn(AmountType.PERCENT_OF_MAXIMUM);

        var output = statisticMagnitudeEffectCalculation
                .getEffect(mockStaticStatType, mockPercentMagnitude, mockCharacter);

        assertEquals(ROUNDED_PERCENT_MAXIMUM_EFFECT, output);
        verify(mockStatisticCalculation).calculate(mockCharacter, mockStaticStatType);
        verify(mockRandomDoubleProvider, times(EFFECT_ENTITY_LEVEL + 1)).get();
        verify(mockStatisticCalculation).calculate(mockCharacter, mockEffectedVariableStatType);
    }

    @Test
    public void testGetDamagePercentOfMaximumEffectForStatistic() {
        when(mockPercentMagnitude.effectType()).thenReturn(EffectType.DAMAGE);
        when(mockPercentMagnitude.amountType()).thenReturn(AmountType.PERCENT_OF_MAXIMUM);

        var output = statisticMagnitudeEffectCalculation
                .getEffect(mockStaticStatType, mockPercentMagnitude, mockCharacter);

        assertEquals(DAMAGE_RESISTANCE_CALCULATION_OUTPUT, output);
        verify(mockStatisticCalculation).calculate(mockCharacter, mockStaticStatType);
        verify(mockRandomDoubleProvider, times(EFFECT_ENTITY_LEVEL + 1)).get();
        verify(mockStatisticCalculation).calculate(mockCharacter, mockEffectedVariableStatType);
        verify(mockDamageResistanceCalculation).calculateEffectiveChange(mockCharacter,
                ROUNDED_PERCENT_MAXIMUM_EFFECT, mockElement);
    }

    @Test
    public void testGetEffectForStatisticWithInvalidParams() {
        assertThrows(IllegalArgumentException.class,
                () -> statisticMagnitudeEffectCalculation.getEffect((StaticStatisticType) null,
                        mockValueMagnitude, mockCharacter));
        assertThrows(IllegalArgumentException.class,
                () -> statisticMagnitudeEffectCalculation.getEffect(mockStaticStatType, null,
                        mockCharacter));
        assertThrows(IllegalArgumentException.class,
                () -> statisticMagnitudeEffectCalculation.getEffect(mockStaticStatType,
                        mockValueMagnitude, null));
    }

    @Test
    public void testGetAlterationValueEffectForStatusEffectType() {
        when(mockValueMagnitude.effectType()).thenReturn(EffectType.ALTERATION);

        var output = statisticMagnitudeEffectCalculation.getEffect(mockStatusEffectType,
                mockValueMagnitude, mockCharacter);

        assertEquals(ROUNDED_VALUE_BASE_EFFECT, output);
        verify(mockStatisticCalculation).calculate(mockCharacter, mockStaticStatType);
        verify(mockRandomDoubleProvider, times(EFFECT_ENTITY_LEVEL + 1)).get();
    }

    @Test
    public void testGetDamageValueEffectForStatusEffectType() {
        when(mockValueMagnitude.effectType()).thenReturn(EffectType.DAMAGE);

        var output = statisticMagnitudeEffectCalculation
                .getEffect(mockStatusEffectType, mockValueMagnitude, mockCharacter);

        assertEquals(DAMAGE_RESISTANCE_CALCULATION_OUTPUT, output);
        verify(mockStatisticCalculation).calculate(mockCharacter, mockStaticStatType);
        verify(mockRandomDoubleProvider, times(EFFECT_ENTITY_LEVEL + 1)).get();
        verify(mockDamageResistanceCalculation).calculateEffectiveChange(mockCharacter,
                ROUNDED_VALUE_BASE_EFFECT, mockElement);
    }

    @Test
    public void testGetAlterationPercentOfCurrentEffectForStatusEffectType() {
        when(mockPercentMagnitude.effectType()).thenReturn(EffectType.ALTERATION);
        when(mockPercentMagnitude.amountType()).thenReturn(AmountType.PERCENT_OF_CURRENT);

        var output = statisticMagnitudeEffectCalculation
                .getEffect(mockStatusEffectType, mockPercentMagnitude, mockCharacter);

        assertEquals(ROUNDED_PERCENT_CURRENT_EFFECT, output);
        verify(mockStatisticCalculation).calculate(mockCharacter, mockStaticStatType);
        verify(mockRandomDoubleProvider, times(EFFECT_ENTITY_LEVEL + 1)).get();
        verify(mockCharacter).getVariableStatisticCurrentValue(mockEffectedVariableStatType);
    }

    @Test
    public void testGetDamagePercentOfCurrentEffectForStatusEffectType() {
        when(mockPercentMagnitude.effectType()).thenReturn(EffectType.DAMAGE);
        when(mockPercentMagnitude.amountType()).thenReturn(AmountType.PERCENT_OF_CURRENT);

        var output = statisticMagnitudeEffectCalculation
                .getEffect(mockStatusEffectType, mockPercentMagnitude, mockCharacter);

        assertEquals(DAMAGE_RESISTANCE_CALCULATION_OUTPUT, output);
        verify(mockStatisticCalculation).calculate(mockCharacter, mockStaticStatType);
        verify(mockRandomDoubleProvider, times(EFFECT_ENTITY_LEVEL + 1)).get();
        verify(mockCharacter).getVariableStatisticCurrentValue(mockEffectedVariableStatType);
        verify(mockDamageResistanceCalculation).calculateEffectiveChange(mockCharacter,
                ROUNDED_PERCENT_CURRENT_EFFECT, mockElement);
    }

    @Test
    public void testGetAlterationPercentOfMaximumEffectForStatusEffectType() {
        when(mockPercentMagnitude.effectType()).thenReturn(EffectType.ALTERATION);
        when(mockPercentMagnitude.amountType()).thenReturn(AmountType.PERCENT_OF_MAXIMUM);

        var output = statisticMagnitudeEffectCalculation
                .getEffect(mockStatusEffectType, mockPercentMagnitude, mockCharacter);

        assertEquals(ROUNDED_PERCENT_MAXIMUM_EFFECT, output);
        verify(mockStatisticCalculation).calculate(mockCharacter, mockStaticStatType);
        verify(mockRandomDoubleProvider, times(EFFECT_ENTITY_LEVEL + 1)).get();
        verify(mockStatisticCalculation).calculate(mockCharacter, mockEffectedVariableStatType);
    }

    @Test
    public void testGetDamagePercentOfMaximumEffectForStatusEffectType() {
        when(mockPercentMagnitude.effectType()).thenReturn(EffectType.DAMAGE);
        when(mockPercentMagnitude.amountType()).thenReturn(AmountType.PERCENT_OF_MAXIMUM);

        var output = statisticMagnitudeEffectCalculation
                .getEffect(mockStatusEffectType, mockPercentMagnitude, mockCharacter);

        assertEquals(DAMAGE_RESISTANCE_CALCULATION_OUTPUT, output);
        verify(mockStatisticCalculation).calculate(mockCharacter, mockStaticStatType);
        verify(mockRandomDoubleProvider, times(EFFECT_ENTITY_LEVEL + 1)).get();
        verify(mockStatisticCalculation).calculate(mockCharacter, mockEffectedVariableStatType);
        verify(mockDamageResistanceCalculation).calculateEffectiveChange(mockCharacter,
                ROUNDED_PERCENT_MAXIMUM_EFFECT, mockElement);
    }

    @Test
    public void testGetEffectForStatusEffectTypeWithInvalidParams() {
        assertThrows(IllegalArgumentException.class,
                () -> statisticMagnitudeEffectCalculation.getEffect((StaticStatisticType) null,
                        mockValueMagnitude, mockCharacter));
        assertThrows(IllegalArgumentException.class,
                () -> statisticMagnitudeEffectCalculation.getEffect(mockStaticStatType, null,
                        mockCharacter));
        assertThrows(IllegalArgumentException.class,
                () -> statisticMagnitudeEffectCalculation.getEffect(mockStaticStatType,
                        mockValueMagnitude, null));
    }
}
