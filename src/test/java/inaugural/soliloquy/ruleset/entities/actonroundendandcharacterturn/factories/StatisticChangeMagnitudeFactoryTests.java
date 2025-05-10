package inaugural.soliloquy.ruleset.entities.actonroundendandcharacterturn.factories;

import inaugural.soliloquy.ruleset.definitions.StatisticChangeMagnitudeDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import soliloquy.specs.common.factories.Factory;
import soliloquy.specs.ruleset.entities.Element;
import soliloquy.specs.ruleset.entities.actonroundendandcharacterturn.StatisticChangeMagnitude;
import soliloquy.specs.ruleset.entities.character.VariableStatisticType;

import java.util.function.Function;

import static inaugural.soliloquy.tools.random.Random.*;
import static inaugural.soliloquy.tools.valueobjects.Pair.pairOf;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static soliloquy.specs.ruleset.entities.actonroundendandcharacterturn.StatisticChangeMagnitude.AmountType.PERCENT_OF_MAXIMUM;
import static soliloquy.specs.ruleset.entities.actonroundendandcharacterturn.StatisticChangeMagnitude.AmountType.VALUE;
import static soliloquy.specs.ruleset.entities.actonroundendandcharacterturn.StatisticChangeMagnitude.EffectType.ALTERATION;
import static soliloquy.specs.ruleset.entities.actonroundendandcharacterturn.StatisticChangeMagnitude.EffectType.DAMAGE;

class StatisticChangeMagnitudeFactoryTests {
    private final String VARIABLE_STAT_TYPE_ID = randomString();
    private final String ELEMENT_ID = randomString();
    private final int PER_LEVEL_VALUE_MINIMUM = randomInt();
    private final int PER_LEVEL_VALUE_MAXIMUM = randomIntWithInclusiveFloor(PER_LEVEL_VALUE_MINIMUM + 1);
    private final float PER_LEVEL_PERCENT_MINIMUM = randomFloat();
    private final float PER_LEVEL_PERCENT_MAXIMUM = randomFloatWithInclusiveFloor(PER_LEVEL_PERCENT_MINIMUM + 1f);
    private final int ABSOLUTE_VALUE_MINIMUM = randomInt();
    private final int ABSOLUTE_VALUE_MAXIMUM = randomIntWithInclusiveFloor(ABSOLUTE_VALUE_MINIMUM + 1);
    private final float ABSOLUTE_PERCENT_MINIMUM = randomFloat();
    private final float ABSOLUTE_PERCENT_MAXIMUM = randomFloatWithInclusiveFloor(ABSOLUTE_PERCENT_MINIMUM + 1f);

    @Mock private VariableStatisticType mockVariableStatType;
    @Mock private Function<String, VariableStatisticType> mockGetVariableStatType;
    @Mock private Element mockElement;
    @Mock private Function<String, Element> mockGetElement;

    @SuppressWarnings("rawtypes")
    private Factory<StatisticChangeMagnitudeDefinition, StatisticChangeMagnitude> factory;

    @BeforeEach
    void setUp() {
        mockVariableStatType = mock(VariableStatisticType.class);

        //noinspection unchecked
        mockGetVariableStatType =
                (Function<String, VariableStatisticType>) mock(Function.class);
        when(mockGetVariableStatType.apply(anyString())).thenReturn(mockVariableStatType);

        mockElement = mock(Element.class);

        //noinspection unchecked
        mockGetElement = (Function<String, Element>) mock(Function.class);
        when(mockGetElement.apply(anyString())).thenReturn(mockElement);

        factory = new StatisticChangeMagnitudeFactory(mockGetVariableStatType, mockGetElement);
    }

    @Test
    void testConstructorWithInvalidArgs() {
        assertThrows(IllegalArgumentException.class,
                () -> new StatisticChangeMagnitudeFactory(null, mockGetElement));
        assertThrows(IllegalArgumentException.class,
                () -> new StatisticChangeMagnitudeFactory(mockGetVariableStatType, null));
    }

    @Test
    void testMakeWithValue() {
        var definition = new StatisticChangeMagnitudeDefinition(VARIABLE_STAT_TYPE_ID, ELEMENT_ID,
                DAMAGE.name(), VALUE.name(),
                new Integer[]{PER_LEVEL_VALUE_MINIMUM, PER_LEVEL_VALUE_MAXIMUM}, null,
                new Integer[]{ABSOLUTE_VALUE_MINIMUM, ABSOLUTE_VALUE_MAXIMUM}, null);

        var output = factory.make(definition);

        assertNotNull(output);
        assertSame(mockVariableStatType, output.effectedStatisticType());
        verify(mockGetVariableStatType).apply(VARIABLE_STAT_TYPE_ID);
        assertSame(DAMAGE, output.effectType());
        assertSame(VALUE, output.amountType());
        assertEquals(pairOf(PER_LEVEL_VALUE_MINIMUM, PER_LEVEL_VALUE_MAXIMUM),
                output.perLevelRange());
        assertEquals(pairOf(ABSOLUTE_VALUE_MINIMUM, ABSOLUTE_VALUE_MAXIMUM),
                output.absoluteRange());
        assertEquals(StatisticChangeMagnitude.class.getCanonicalName() + "<" +
                Integer.class.getCanonicalName() + ">", output.getInterfaceName());
    }

    @Test
    void testMakeWithValueAndOnlyPerLevel() {
        var definition = new StatisticChangeMagnitudeDefinition(VARIABLE_STAT_TYPE_ID, ELEMENT_ID,
                DAMAGE.name(), VALUE.name(),
                new Integer[]{PER_LEVEL_VALUE_MINIMUM, PER_LEVEL_VALUE_MAXIMUM}, null, null, null);

        var output = factory.make(definition);

        assertNotNull(output);
        assertSame(mockVariableStatType, output.effectedStatisticType());
        verify(mockGetVariableStatType).apply(VARIABLE_STAT_TYPE_ID);
        assertSame(DAMAGE, output.effectType());
        assertSame(VALUE, output.amountType());
        assertEquals(pairOf(PER_LEVEL_VALUE_MINIMUM, PER_LEVEL_VALUE_MAXIMUM),
                output.perLevelRange());
        assertNull(output.absoluteRange());
        assertEquals(StatisticChangeMagnitude.class.getCanonicalName() + "<" +
                Integer.class.getCanonicalName() + ">", output.getInterfaceName());
    }

    @Test
    void testMakeWithValueAndOnlyAbsolute() {
        var definition = new StatisticChangeMagnitudeDefinition(VARIABLE_STAT_TYPE_ID, ELEMENT_ID,
                DAMAGE.name(), VALUE.name(), null, null,
                new Integer[]{ABSOLUTE_VALUE_MINIMUM, ABSOLUTE_VALUE_MAXIMUM}, null);

        var output = factory.make(definition);

        assertNotNull(output);
        assertSame(mockVariableStatType, output.effectedStatisticType());
        verify(mockGetVariableStatType).apply(VARIABLE_STAT_TYPE_ID);
        assertSame(DAMAGE, output.effectType());
        assertSame(VALUE, output.amountType());
        assertNull(output.perLevelRange());
        assertEquals(pairOf(ABSOLUTE_VALUE_MINIMUM, ABSOLUTE_VALUE_MAXIMUM),
                output.absoluteRange());
        assertEquals(StatisticChangeMagnitude.class.getCanonicalName() + "<" +
                Integer.class.getCanonicalName() + ">", output.getInterfaceName());
    }

    @Test
    void testMakeWithPercent() {
        var definition = new StatisticChangeMagnitudeDefinition(VARIABLE_STAT_TYPE_ID, ELEMENT_ID,
                DAMAGE.name(), PERCENT_OF_MAXIMUM.name(), null,
                new Float[]{PER_LEVEL_PERCENT_MINIMUM, PER_LEVEL_PERCENT_MAXIMUM}, null,
                new Float[]{ABSOLUTE_PERCENT_MINIMUM, ABSOLUTE_PERCENT_MAXIMUM});

        var output = factory.make(definition);

        assertNotNull(output);
        assertSame(mockVariableStatType, output.effectedStatisticType());
        verify(mockGetVariableStatType).apply(VARIABLE_STAT_TYPE_ID);
        assertSame(mockElement, output.element());
        verify(mockGetElement).apply(ELEMENT_ID);
        assertSame(DAMAGE, output.effectType());
        assertSame(StatisticChangeMagnitude.AmountType.PERCENT_OF_MAXIMUM, output.amountType());
        assertEquals(pairOf(PER_LEVEL_PERCENT_MINIMUM, PER_LEVEL_PERCENT_MAXIMUM),
                output.perLevelRange());
        assertEquals(pairOf(ABSOLUTE_PERCENT_MINIMUM, ABSOLUTE_PERCENT_MAXIMUM),
                output.absoluteRange());
        assertEquals(StatisticChangeMagnitude.class.getCanonicalName() + "<" +
                Float.class.getCanonicalName() + ">", output.getInterfaceName());
    }

    @Test
    void testMakeWithPercentAndOnlyPerLevel() {
        var definition = new StatisticChangeMagnitudeDefinition(VARIABLE_STAT_TYPE_ID, ELEMENT_ID,
                DAMAGE.name(), PERCENT_OF_MAXIMUM.name(), null,
                new Float[]{PER_LEVEL_PERCENT_MINIMUM, PER_LEVEL_PERCENT_MAXIMUM}, null, null);

        var output = factory.make(definition);

        assertNotNull(output);
        assertSame(mockVariableStatType, output.effectedStatisticType());
        verify(mockGetVariableStatType).apply(VARIABLE_STAT_TYPE_ID);
        assertSame(mockElement, output.element());
        verify(mockGetElement).apply(ELEMENT_ID);
        assertSame(DAMAGE, output.effectType());
        assertSame(StatisticChangeMagnitude.AmountType.PERCENT_OF_MAXIMUM, output.amountType());
        assertEquals(pairOf(PER_LEVEL_PERCENT_MINIMUM, PER_LEVEL_PERCENT_MAXIMUM),
                output.perLevelRange());
        assertNull(output.absoluteRange());
        assertEquals(StatisticChangeMagnitude.class.getCanonicalName() + "<" +
                Float.class.getCanonicalName() + ">", output.getInterfaceName());
    }

    @Test
    void testMakeWithPercentAndOnlyAbsolute() {
        var definition = new StatisticChangeMagnitudeDefinition(VARIABLE_STAT_TYPE_ID, ELEMENT_ID,
                DAMAGE.name(), PERCENT_OF_MAXIMUM.name(), null, null, null,
                new Float[]{ABSOLUTE_PERCENT_MINIMUM, ABSOLUTE_PERCENT_MAXIMUM});

        var output = factory.make(definition);

        assertNotNull(output);
        assertSame(mockVariableStatType, output.effectedStatisticType());
        verify(mockGetVariableStatType).apply(VARIABLE_STAT_TYPE_ID);
        assertSame(mockElement, output.element());
        verify(mockGetElement).apply(ELEMENT_ID);
        assertSame(DAMAGE, output.effectType());
        assertSame(StatisticChangeMagnitude.AmountType.PERCENT_OF_MAXIMUM, output.amountType());
        assertNull(output.perLevelRange());
        assertEquals(pairOf(ABSOLUTE_PERCENT_MINIMUM, ABSOLUTE_PERCENT_MAXIMUM),
                output.absoluteRange());
        assertEquals(StatisticChangeMagnitude.class.getCanonicalName() + "<" +
                Float.class.getCanonicalName() + ">", output.getInterfaceName());
    }

    @Test
    void testMakeWithInvalidArgs() {
        var oneIntArray = new Integer[]{randomInt()};
        var threeIntArray = new Integer[]{randomInt(), randomInt(), randomInt()};
        var intArrayWith0Null = new Integer[]{null, randomInt()};
        var intArrayWith1Null = new Integer[]{randomInt(), null};
        var outOfOrderIntHigher = randomInt();
        var outOfOrderIntLower = randomIntWithInclusiveCeiling(outOfOrderIntHigher - 1);
        var outOfOrderIntArray = new Integer[]{outOfOrderIntHigher, outOfOrderIntLower};
        var oneFloatArray = new Float[]{randomFloat()};
        var threeFloatArray = new Float[]{randomFloat(), randomFloat(), randomFloat()};
        var floatArrayWith0Null = new Float[]{null, randomFloat()};
        var floatArrayWith1Null = new Float[]{randomFloat(), null};
        var outOfOrderFloatHigher = randomFloat();
        var outOfOrderFloatLower = randomFloatWithInclusiveCeiling(outOfOrderFloatHigher - 1);
        var outOfOrderFloatArray = new Float[]{outOfOrderFloatHigher, outOfOrderFloatLower};
        var invalidEffectType = randomString();
        var invalidAmountType = randomString();
        var invalidElementId = randomString();
        when(mockGetElement.apply(invalidElementId)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> factory.make(null));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new StatisticChangeMagnitudeDefinition(VARIABLE_STAT_TYPE_ID, randomString(), null,
                        VALUE.name(), null, null, null, null)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new StatisticChangeMagnitudeDefinition(VARIABLE_STAT_TYPE_ID, invalidElementId,
                        ALTERATION.name(), VALUE.name(), null, null, null,
                        null)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new StatisticChangeMagnitudeDefinition(VARIABLE_STAT_TYPE_ID, randomString(), "",
                        VALUE.name(), null, null, null, null)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new StatisticChangeMagnitudeDefinition(VARIABLE_STAT_TYPE_ID, randomString(),
                        invalidEffectType, VALUE.name(), null, null, null, null)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new StatisticChangeMagnitudeDefinition(VARIABLE_STAT_TYPE_ID, randomString(),
                        ALTERATION.name(), null, null, null, null, null)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new StatisticChangeMagnitudeDefinition(VARIABLE_STAT_TYPE_ID, randomString(),
                        ALTERATION.name(), "", null, null, null, null)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new StatisticChangeMagnitudeDefinition(VARIABLE_STAT_TYPE_ID, randomString(),
                        ALTERATION.name(), invalidAmountType, null, null, null, null)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new StatisticChangeMagnitudeDefinition(VARIABLE_STAT_TYPE_ID, randomString(),
                        ALTERATION.name(), VALUE.name(), oneIntArray, null, null, null)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new StatisticChangeMagnitudeDefinition(VARIABLE_STAT_TYPE_ID, randomString(),
                        ALTERATION.name(), VALUE.name(), threeIntArray, null, null, null)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new StatisticChangeMagnitudeDefinition(VARIABLE_STAT_TYPE_ID, randomString(),
                        ALTERATION.name(), VALUE.name(), intArrayWith0Null, null, null, null)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new StatisticChangeMagnitudeDefinition(VARIABLE_STAT_TYPE_ID, randomString(),
                        ALTERATION.name(), VALUE.name(), intArrayWith1Null, null, null, null)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new StatisticChangeMagnitudeDefinition(VARIABLE_STAT_TYPE_ID, randomString(),
                        ALTERATION.name(), VALUE.name(), outOfOrderIntArray, null, null, null)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new StatisticChangeMagnitudeDefinition(VARIABLE_STAT_TYPE_ID, randomString(),
                        ALTERATION.name(), VALUE.name(), null, oneFloatArray, null, null)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new StatisticChangeMagnitudeDefinition(VARIABLE_STAT_TYPE_ID, randomString(),
                        ALTERATION.name(), VALUE.name(), null, threeFloatArray, null, null)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new StatisticChangeMagnitudeDefinition(VARIABLE_STAT_TYPE_ID, randomString(),
                        ALTERATION.name(), VALUE.name(), null, floatArrayWith0Null, null, null)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new StatisticChangeMagnitudeDefinition(VARIABLE_STAT_TYPE_ID, randomString(),
                        ALTERATION.name(), VALUE.name(), null, floatArrayWith1Null, null, null)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new StatisticChangeMagnitudeDefinition(VARIABLE_STAT_TYPE_ID, randomString(),
                        ALTERATION.name(), VALUE.name(), null, outOfOrderFloatArray, null, null)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new StatisticChangeMagnitudeDefinition(VARIABLE_STAT_TYPE_ID, randomString(),
                        ALTERATION.name(), VALUE.name(), null, null, oneIntArray, null)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new StatisticChangeMagnitudeDefinition(VARIABLE_STAT_TYPE_ID, randomString(),
                        ALTERATION.name(), VALUE.name(), null, null, threeIntArray, null)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new StatisticChangeMagnitudeDefinition(VARIABLE_STAT_TYPE_ID, randomString(),
                        ALTERATION.name(), VALUE.name(), null, null, intArrayWith0Null, null)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new StatisticChangeMagnitudeDefinition(VARIABLE_STAT_TYPE_ID, randomString(),
                        ALTERATION.name(), VALUE.name(), null, null, intArrayWith1Null, null)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new StatisticChangeMagnitudeDefinition(VARIABLE_STAT_TYPE_ID, randomString(),
                        ALTERATION.name(), VALUE.name(), null, null, outOfOrderIntArray, null)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new StatisticChangeMagnitudeDefinition(VARIABLE_STAT_TYPE_ID, randomString(),
                        ALTERATION.name(), VALUE.name(), null, null, null, oneFloatArray)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new StatisticChangeMagnitudeDefinition(VARIABLE_STAT_TYPE_ID, randomString(),
                        ALTERATION.name(), VALUE.name(), null, null, null, threeFloatArray)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new StatisticChangeMagnitudeDefinition(VARIABLE_STAT_TYPE_ID, randomString(),
                        ALTERATION.name(), VALUE.name(), null, null, null, floatArrayWith0Null)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new StatisticChangeMagnitudeDefinition(VARIABLE_STAT_TYPE_ID, randomString(),
                        ALTERATION.name(), VALUE.name(), null, null, null, floatArrayWith1Null)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new StatisticChangeMagnitudeDefinition(VARIABLE_STAT_TYPE_ID, randomString(),
                        ALTERATION.name(), VALUE.name(), null, null, null, outOfOrderFloatArray)));
    }

    @Test
    void testGetInterfaceName() {
        assertEquals(Factory.class.getCanonicalName() + "<" +
                        StatisticChangeMagnitudeDefinition.class.getCanonicalName() + "," +
                        StatisticChangeMagnitude.class.getCanonicalName() + ">",
                factory.getInterfaceName());
    }
}
