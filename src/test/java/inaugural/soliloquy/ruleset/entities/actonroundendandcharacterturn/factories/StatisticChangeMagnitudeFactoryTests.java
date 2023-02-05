package inaugural.soliloquy.ruleset.entities.actonroundendandcharacterturn.factories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import soliloquy.specs.common.factories.Factory;
import soliloquy.specs.common.valueobjects.Pair;
import inaugural.soliloquy.ruleset.definitions.StatisticChangeMagnitudeDefinition;
import soliloquy.specs.ruleset.entities.Element;
import soliloquy.specs.ruleset.entities.actonroundendandcharacterturn.StatisticChangeMagnitude;
import soliloquy.specs.ruleset.entities.character.CharacterVariableStatisticType;

import java.util.function.Function;

import static inaugural.soliloquy.tools.random.Random.*;
import static org.junit.Assert.*;
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
    private final int PER_LEVEL_VALUE_MAXIMUM = randomInt();
    private final float PER_LEVEL_PERCENT_MINIMUM = randomFloat();
    private final float PER_LEVEL_PERCENT_MAXIMUM = randomFloat();
    private final int ABSOLUTE_VALUE_MINIMUM = randomInt();
    private final int ABSOLUTE_VALUE_MAXIMUM = randomInt();
    private final float ABSOLUTE_PERCENT_MINIMUM = randomFloat();
    private final float ABSOLUTE_PERCENT_MAXIMUM = randomFloat();

    @Mock private CharacterVariableStatisticType mockVariableStatType;
    @Mock private Function<String, CharacterVariableStatisticType> mockGetVariableStatType;
    @Mock private Element mockElement;
    @Mock private Function<String, Element> mockGetElement;

    @SuppressWarnings("rawtypes")
    private Factory<StatisticChangeMagnitudeDefinition, StatisticChangeMagnitude> factory;

    @BeforeEach
    void setUp() {
        mockVariableStatType = mock(CharacterVariableStatisticType.class);

        //noinspection unchecked
        mockGetVariableStatType =
                (Function<String, CharacterVariableStatisticType>) mock(Function.class);
        when(mockGetVariableStatType.apply(anyString())).thenReturn(mockVariableStatType);

        mockElement = mock(Element.class);

        //noinspection unchecked
        mockGetElement = (Function<String, Element>) mock(Function.class);
        when(mockGetElement.apply(anyString())).thenReturn(mockElement);

        factory = new StatisticChangeMagnitudeFactory(mockGetVariableStatType, mockGetElement);
    }

    @Test
    void testConstructorWithInvalidParams() {
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
        assertEquals(Pair.of(PER_LEVEL_VALUE_MINIMUM, PER_LEVEL_VALUE_MAXIMUM),
                output.perLevelRange());
        assertEquals(Pair.of(ABSOLUTE_VALUE_MINIMUM, ABSOLUTE_VALUE_MAXIMUM),
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
        assertEquals(Pair.of(PER_LEVEL_VALUE_MINIMUM, PER_LEVEL_VALUE_MAXIMUM),
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
        assertEquals(Pair.of(ABSOLUTE_VALUE_MINIMUM, ABSOLUTE_VALUE_MAXIMUM),
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
        assertEquals(Pair.of(PER_LEVEL_PERCENT_MINIMUM, PER_LEVEL_PERCENT_MAXIMUM),
                output.perLevelRange());
        assertEquals(Pair.of(ABSOLUTE_PERCENT_MINIMUM, ABSOLUTE_PERCENT_MAXIMUM),
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
        assertEquals(Pair.of(PER_LEVEL_PERCENT_MINIMUM, PER_LEVEL_PERCENT_MAXIMUM),
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
        assertEquals(Pair.of(ABSOLUTE_PERCENT_MINIMUM, ABSOLUTE_PERCENT_MAXIMUM),
                output.absoluteRange());
        assertEquals(StatisticChangeMagnitude.class.getCanonicalName() + "<" +
                Float.class.getCanonicalName() + ">", output.getInterfaceName());
    }

    @Test
    void testMakeWithInvalidParams() {
        var oneIntArray = new Integer[]{randomInt()};
        var threeIntArray = new Integer[]{randomInt(), randomInt(), randomInt()};
        var oneFloatArray = new Float[]{randomFloat()};
        var threeFloatArray = new Float[]{randomFloat(), randomFloat(), randomFloat()};
        var invalidEffectType = randomString();
        var invalidAmountType = randomString();
        var invalidElementId = randomString();
        when(mockGetElement.apply(invalidElementId)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> factory.make(null));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new StatisticChangeMagnitudeDefinition(VARIABLE_STAT_TYPE_ID, null, null,
                        VALUE.name(), null, null, null, null)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new StatisticChangeMagnitudeDefinition(VARIABLE_STAT_TYPE_ID, invalidElementId,
                        ALTERATION.name(), VALUE.name(), null, null, null,
                        null)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new StatisticChangeMagnitudeDefinition(VARIABLE_STAT_TYPE_ID, null, "",
                        VALUE.name(), null, null, null, null)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new StatisticChangeMagnitudeDefinition(VARIABLE_STAT_TYPE_ID, null,
                        invalidEffectType, VALUE.name(), null, null, null, null)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new StatisticChangeMagnitudeDefinition(VARIABLE_STAT_TYPE_ID, null,
                        ALTERATION.name(), null, null, null, null, null)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new StatisticChangeMagnitudeDefinition(VARIABLE_STAT_TYPE_ID, null,
                        ALTERATION.name(), "", null, null, null, null)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new StatisticChangeMagnitudeDefinition(VARIABLE_STAT_TYPE_ID, null,
                        ALTERATION.name(), invalidAmountType, null, null, null, null)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new StatisticChangeMagnitudeDefinition(VARIABLE_STAT_TYPE_ID, null,
                        ALTERATION.name(), VALUE.name(), oneIntArray, null, null, null)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new StatisticChangeMagnitudeDefinition(VARIABLE_STAT_TYPE_ID, null,
                        ALTERATION.name(), VALUE.name(), threeIntArray, null, null, null)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new StatisticChangeMagnitudeDefinition(VARIABLE_STAT_TYPE_ID, null,
                        ALTERATION.name(), VALUE.name(), null, oneFloatArray, null, null)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new StatisticChangeMagnitudeDefinition(VARIABLE_STAT_TYPE_ID, null,
                        ALTERATION.name(), VALUE.name(), null, threeFloatArray, null, null)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new StatisticChangeMagnitudeDefinition(VARIABLE_STAT_TYPE_ID, null,
                        ALTERATION.name(), VALUE.name(), null, null, oneIntArray, null)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new StatisticChangeMagnitudeDefinition(VARIABLE_STAT_TYPE_ID, null,
                        ALTERATION.name(), VALUE.name(), null, null, threeIntArray, null)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new StatisticChangeMagnitudeDefinition(VARIABLE_STAT_TYPE_ID, null,
                        ALTERATION.name(), VALUE.name(), null, null, null, oneFloatArray)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new StatisticChangeMagnitudeDefinition(VARIABLE_STAT_TYPE_ID, null,
                        ALTERATION.name(), VALUE.name(), null, null, null, threeFloatArray)));
    }

    @Test
    void testGetInterfaceName() {
        assertEquals(Factory.class.getCanonicalName() + "<" +
                        StatisticChangeMagnitudeDefinition.class.getCanonicalName() + "," +
                        StatisticChangeMagnitude.class.getCanonicalName() + ">",
                factory.getInterfaceName());
    }
}
