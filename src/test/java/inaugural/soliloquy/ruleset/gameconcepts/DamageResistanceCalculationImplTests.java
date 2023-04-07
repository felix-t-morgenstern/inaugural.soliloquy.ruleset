package inaugural.soliloquy.ruleset.gameconcepts;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import soliloquy.specs.gamestate.entities.Character;
import soliloquy.specs.ruleset.entities.Element;
import soliloquy.specs.ruleset.entities.character.StaticStatisticType;
import soliloquy.specs.ruleset.gameconcepts.DamageResistanceCalculation;
import soliloquy.specs.ruleset.gameconcepts.StatisticCalculation;

import static inaugural.soliloquy.tools.random.Random.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DamageResistanceCalculationImplTests {
    // NB: Stats are calculated as percentages, so having resistance between -99 - 99% results in a
    //     more meaningful test
    private final int CALCULATED_ELEMENT_RESIST_STAT = randomIntInRange(-99, 99);

    @Mock private StaticStatisticType mockElementResistStat;
    @Mock private StatisticCalculation mockStatisticCalculation;
    @Mock private Character mockCharacter;
    @Mock private Element mockElement;

    private DamageResistanceCalculation damageResistanceCalculation;

    @Before
    public void setUp() {
        when(mockElement.resistanceStatisticType()).thenReturn(mockElementResistStat);

        when(mockStatisticCalculation.calculate(any(), any()))
                .thenReturn(CALCULATED_ELEMENT_RESIST_STAT);

        damageResistanceCalculation = new DamageResistanceCalculationImpl(mockStatisticCalculation);
    }

    @Test
    public void testConstructorWithInvalidParams() {
        assertThrows(IllegalArgumentException.class,
                () -> new DamageResistanceCalculationImpl(null));
    }

    @Test
    public void testCalculateEffectiveChangeWithNegativeAmount() {
        var baseAmount = randomIntWithInclusiveCeiling(-1);
        var expectedEffectiveChange =
                (int) (baseAmount * ((100f - CALCULATED_ELEMENT_RESIST_STAT) / 100f));

        var effectiveChange =
                damageResistanceCalculation.calculateEffectiveChange(mockCharacter, baseAmount,
                        mockElement);

        assertEquals(expectedEffectiveChange, effectiveChange);
        verify(mockElement).resistanceStatisticType();
        verify(mockStatisticCalculation).calculate(mockCharacter, mockElementResistStat);
    }

    @Test
    public void testCalculateEffectiveChangeWithPositiveAmount() {
        var baseAmount = randomIntWithInclusiveFloor(1);

        var effectiveChange =
                damageResistanceCalculation.calculateEffectiveChange(mockCharacter, baseAmount,
                        mockElement);

        assertEquals(baseAmount, effectiveChange);
        verify(mockElement, never()).resistanceStatisticType();
        verify(mockStatisticCalculation, never()).calculate(mockCharacter, mockElementResistStat);
    }

    @Test
    public void testCalculateEffectiveChangeWithInvalidParams() {
        assertThrows(IllegalArgumentException.class,
                () -> damageResistanceCalculation.calculateEffectiveChange(null, 0,
                        mockElement));
        assertThrows(IllegalArgumentException.class,
                () -> damageResistanceCalculation.calculateEffectiveChange(
                        mockCharacter, 0, null));
        when(mockCharacter.isDeleted()).thenReturn(true);
        assertThrows(IllegalArgumentException.class,
                () -> damageResistanceCalculation.calculateEffectiveChange(
                        mockCharacter, 0, mockElement));
    }

    @Test
    public void testGetInterfaceName() {
        assertEquals(DamageResistanceCalculation.class.getCanonicalName(),
                damageResistanceCalculation.getInterfaceName());
    }
}
