package inaugural.soliloquy.ruleset.entities.actonturnandroundend.factories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import soliloquy.specs.common.factories.Factory;
import soliloquy.specs.common.valueobjects.Pair;
import soliloquy.specs.ruleset.definitions.StatisticChangeMagnitudeDefinition;
import soliloquy.specs.ruleset.entities.actonturnandroundend.StatisticChangeMagnitude;

import static inaugural.soliloquy.tools.random.Random.randomFloat;
import static inaugural.soliloquy.tools.random.Random.randomInt;
import static org.junit.Assert.*;

class StatisticChangeMagnitudeFactoryTests {
    private final int PER_LEVEL_VALUE_MINIMUM = randomInt();
    private final int PER_LEVEL_VALUE_MAXIMUM = randomInt();
    private final float PER_LEVEL_PERCENT_MINIMUM = randomFloat();
    private final float PER_LEVEL_PERCENT_MAXIMUM = randomFloat();
    private final int ABSOLUTE_VALUE_MINIMUM = randomInt();
    private final int ABSOLUTE_VALUE_MAXIMUM = randomInt();
    private final float ABSOLUTE_PERCENT_MINIMUM = randomFloat();
    private final float ABSOLUTE_PERCENT_MAXIMUM = randomFloat();

    @SuppressWarnings("rawtypes")
    private Factory<StatisticChangeMagnitudeDefinition, StatisticChangeMagnitude> factory;

    @BeforeEach
    void setUp() {
        factory = new StatisticChangeMagnitudeFactory();
    }

    @Test
    void testMakeWithValue() {
        StatisticChangeMagnitudeDefinition definition = new StatisticChangeMagnitudeDefinition(2, 1,
                new Integer[]{PER_LEVEL_VALUE_MINIMUM, PER_LEVEL_VALUE_MAXIMUM}, null,
                new Integer[]{ABSOLUTE_VALUE_MINIMUM, ABSOLUTE_VALUE_MAXIMUM}, null);

        //noinspection unchecked
        StatisticChangeMagnitude<Integer> output = factory.make(definition);

        assertNotNull(output);
        assertSame(StatisticChangeMagnitude.EffectType.DAMAGE, output.effectType());
        assertSame(StatisticChangeMagnitude.AmountType.VALUE, output.amountType());
        assertEquals(Pair.of(PER_LEVEL_VALUE_MINIMUM, PER_LEVEL_VALUE_MAXIMUM),
                output.perLevelRange());
        assertEquals(Pair.of(ABSOLUTE_VALUE_MINIMUM, ABSOLUTE_VALUE_MAXIMUM),
                output.absoluteRange());
        assertEquals(StatisticChangeMagnitude.class.getCanonicalName() + "<" +
                Integer.class.getCanonicalName() + ">", output.getInterfaceName());
    }

    @Test
    void testMakeWithValueAndOnlyPerLevel() {
        StatisticChangeMagnitudeDefinition definition = new StatisticChangeMagnitudeDefinition(2, 1,
                new Integer[]{PER_LEVEL_VALUE_MINIMUM, PER_LEVEL_VALUE_MAXIMUM}, null, null, null);

        //noinspection unchecked
        StatisticChangeMagnitude<Integer> output = factory.make(definition);

        assertNotNull(output);
        assertSame(StatisticChangeMagnitude.EffectType.DAMAGE, output.effectType());
        assertSame(StatisticChangeMagnitude.AmountType.VALUE, output.amountType());
        assertEquals(Pair.of(PER_LEVEL_VALUE_MINIMUM, PER_LEVEL_VALUE_MAXIMUM),
                output.perLevelRange());
        assertNull(output.absoluteRange());
        assertEquals(StatisticChangeMagnitude.class.getCanonicalName() + "<" +
                Integer.class.getCanonicalName() + ">", output.getInterfaceName());
    }

    @Test
    void testMakeWithValueAndOnlyAbsolute() {
        StatisticChangeMagnitudeDefinition definition =
                new StatisticChangeMagnitudeDefinition(2, 1, null, null,
                        new Integer[]{ABSOLUTE_VALUE_MINIMUM, ABSOLUTE_VALUE_MAXIMUM}, null);

        //noinspection unchecked
        StatisticChangeMagnitude<Integer> output = factory.make(definition);

        assertNotNull(output);
        assertSame(StatisticChangeMagnitude.EffectType.DAMAGE, output.effectType());
        assertSame(StatisticChangeMagnitude.AmountType.VALUE, output.amountType());
        assertNull(output.perLevelRange());
        assertEquals(Pair.of(ABSOLUTE_VALUE_MINIMUM, ABSOLUTE_VALUE_MAXIMUM),
                output.absoluteRange());
        assertEquals(StatisticChangeMagnitude.class.getCanonicalName() + "<" +
                Integer.class.getCanonicalName() + ">", output.getInterfaceName());
    }

    @Test
    void testMakeWithPercent() {
        StatisticChangeMagnitudeDefinition definition = new StatisticChangeMagnitudeDefinition(2, 3,
                null, new Float[]{PER_LEVEL_PERCENT_MINIMUM, PER_LEVEL_PERCENT_MAXIMUM},
                null, new Float[]{ABSOLUTE_PERCENT_MINIMUM, ABSOLUTE_PERCENT_MAXIMUM});

        //noinspection unchecked
        StatisticChangeMagnitude<Float> output = factory.make(definition);

        assertNotNull(output);
        assertSame(StatisticChangeMagnitude.EffectType.DAMAGE, output.effectType());
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
        StatisticChangeMagnitudeDefinition definition = new StatisticChangeMagnitudeDefinition(2, 3,
                null, new Float[]{PER_LEVEL_PERCENT_MINIMUM, PER_LEVEL_PERCENT_MAXIMUM}, null,
                null);

        //noinspection unchecked
        StatisticChangeMagnitude<Float> output = factory.make(definition);

        assertNotNull(output);
        assertSame(StatisticChangeMagnitude.EffectType.DAMAGE, output.effectType());
        assertSame(StatisticChangeMagnitude.AmountType.PERCENT_OF_MAXIMUM, output.amountType());
        assertEquals(Pair.of(PER_LEVEL_PERCENT_MINIMUM, PER_LEVEL_PERCENT_MAXIMUM),
                output.perLevelRange());
        assertNull(output.absoluteRange());
        assertEquals(StatisticChangeMagnitude.class.getCanonicalName() + "<" +
                Float.class.getCanonicalName() + ">", output.getInterfaceName());
    }

    @Test
    void testMakeWithPercentAndOnlyAbsolute() {
        StatisticChangeMagnitudeDefinition definition =
                new StatisticChangeMagnitudeDefinition(2, 3, null, null, null,
                        new Float[]{ABSOLUTE_PERCENT_MINIMUM, ABSOLUTE_PERCENT_MAXIMUM});

        //noinspection unchecked
        StatisticChangeMagnitude<Float> output = factory.make(definition);

        assertNotNull(output);
        assertSame(StatisticChangeMagnitude.EffectType.DAMAGE, output.effectType());
        assertSame(StatisticChangeMagnitude.AmountType.PERCENT_OF_MAXIMUM, output.amountType());
        assertNull(output.perLevelRange());
        assertEquals(Pair.of(ABSOLUTE_PERCENT_MINIMUM, ABSOLUTE_PERCENT_MAXIMUM),
                output.absoluteRange());
        assertEquals(StatisticChangeMagnitude.class.getCanonicalName() + "<" +
                Float.class.getCanonicalName() + ">", output.getInterfaceName());
    }

    @Test
    void testMakeWithInvalidParams() {
        Integer[] oneIntArray = new Integer[]{randomInt()};
        Integer[] threeIntArray = new Integer[]{randomInt(), randomInt(), randomInt()};
        Float[] oneFloatArray = new Float[]{randomFloat()};
        Float[] threeFloatArray = new Float[]{randomFloat(), randomFloat(), randomFloat()};

        assertThrows(IllegalArgumentException.class, () -> factory.make(null));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new StatisticChangeMagnitudeDefinition(0, 1, null, null, null, null)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new StatisticChangeMagnitudeDefinition(3, 1, null, null, null, null)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new StatisticChangeMagnitudeDefinition(1, 0, null, null, null, null)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new StatisticChangeMagnitudeDefinition(1, 4, null, null, null, null)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new StatisticChangeMagnitudeDefinition(1, 1, oneIntArray, null, null, null)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new StatisticChangeMagnitudeDefinition(1, 1, threeIntArray, null, null, null)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new StatisticChangeMagnitudeDefinition(1, 1, null, oneFloatArray, null, null)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new StatisticChangeMagnitudeDefinition(1, 1, null, threeFloatArray, null, null)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new StatisticChangeMagnitudeDefinition(1, 1, null, null, oneIntArray, null)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new StatisticChangeMagnitudeDefinition(1, 1, null, null, threeIntArray, null)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new StatisticChangeMagnitudeDefinition(1, 1, null, null, null, oneFloatArray)));
        assertThrows(IllegalArgumentException.class, () -> factory.make(
                new StatisticChangeMagnitudeDefinition(1, 1, null, null, null, threeFloatArray)));
    }

    @Test
    void testGetInterfaceName() {
        assertEquals(Factory.class.getCanonicalName() + "<" +
                        StatisticChangeMagnitudeDefinition.class.getCanonicalName() + "," +
                        StatisticChangeMagnitude.class.getCanonicalName() + ">",
                factory.getInterfaceName());
    }
}
