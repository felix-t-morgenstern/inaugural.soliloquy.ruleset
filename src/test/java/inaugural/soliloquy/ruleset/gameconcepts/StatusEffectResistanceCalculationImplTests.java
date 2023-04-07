package inaugural.soliloquy.ruleset.gameconcepts;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import soliloquy.specs.gamestate.entities.Character;
import soliloquy.specs.gamestate.entities.CharacterStatusEffects;
import soliloquy.specs.ruleset.entities.Element;
import soliloquy.specs.ruleset.entities.character.StaticStatisticType;
import soliloquy.specs.ruleset.entities.character.StatusEffectType;
import soliloquy.specs.ruleset.gameconcepts.StatisticCalculation;
import soliloquy.specs.ruleset.gameconcepts.StatusEffectResistanceCalculation;

import static inaugural.soliloquy.tools.random.Random.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class StatusEffectResistanceCalculationImplTests {
    @Mock private StaticStatisticType mockStatusEffectTypeResistStat;
    @Mock private StaticStatisticType mockElementResistStat;
    @Mock private StatisticCalculation mockStatisticCalculation;
    @Mock private Character mockCharacter;
    @Mock private CharacterStatusEffects mockCharacterStatusEffects;
    @Mock private StatusEffectType mockStatusEffectType;
    @Mock private Element mockElement;

    private StatusEffectResistanceCalculation statusEffectResistanceCalculation;

    @Before
    public void setUp() {
        when(mockElement.resistanceStatisticType()).thenReturn(mockElementResistStat);
        when(mockStatusEffectType.resistanceStatisticType()).thenReturn(
                mockStatusEffectTypeResistStat);

        when(mockCharacter.statusEffects()).thenReturn(mockCharacterStatusEffects);

        statusEffectResistanceCalculation =
                new StatusEffectResistanceCalculationImpl(mockStatisticCalculation);
    }

    @Test
    public void testConstructorWithInvalidParams() {
        assertThrows(IllegalArgumentException.class,
                () -> new StatusEffectResistanceCalculationImpl(null));
    }

    @Test
    public void testCalculateEffectiveChangeWithGreaterResistanceFromElement() {
        var resistanceFromElement = randomIntInRange(-90, 99);
        var resistanceFromStatusEffectType = randomIntInRange(-99, resistanceFromElement);

        when(mockStatisticCalculation.calculate(any(), same(mockElementResistStat)))
                .thenReturn(resistanceFromElement);
        when(mockStatisticCalculation.calculate(any(), same(mockStatusEffectTypeResistStat)))
                .thenReturn(resistanceFromStatusEffectType);

        var baseAmount = randomInt();
        var expectedEffectiveChange = (int) (((100f - resistanceFromElement) / 100f) * baseAmount);

        var output = statusEffectResistanceCalculation.calculateEffectiveChange(mockCharacter,
                mockStatusEffectType, baseAmount, false, mockElement);

        assertEquals(expectedEffectiveChange, output);
        verify(mockElement).resistanceStatisticType();
        verify(mockStatisticCalculation).calculate(mockCharacter, mockElementResistStat);
        verify(mockStatusEffectType).resistanceStatisticType();
        verify(mockStatisticCalculation).calculate(mockCharacter, mockStatusEffectTypeResistStat);
    }

    @Test
    public void testCalculateEffectiveChangeWithGreaterResistanceFromStatusEffectType() {
        var resistanceFromStatusEffectType = randomIntInRange(-90, 99);
        var resistanceFromElement = randomIntInRange(-99, resistanceFromStatusEffectType);

        when(mockStatisticCalculation.calculate(any(), same(mockElementResistStat)))
                .thenReturn(resistanceFromElement);
        when(mockStatisticCalculation.calculate(any(), same(mockStatusEffectTypeResistStat)))
                .thenReturn(resistanceFromStatusEffectType);

        var baseAmount = randomInt();
        var expectedEffectiveChange = (int) (((100f - resistanceFromStatusEffectType) / 100f) * baseAmount);

        var output = statusEffectResistanceCalculation.calculateEffectiveChange(mockCharacter,
                mockStatusEffectType, baseAmount, false, mockElement);

        assertEquals(expectedEffectiveChange, output);
        verify(mockElement).resistanceStatisticType();
        verify(mockStatisticCalculation).calculate(mockCharacter, mockElementResistStat);
        verify(mockStatusEffectType).resistanceStatisticType();
        verify(mockStatisticCalculation).calculate(mockCharacter, mockStatusEffectTypeResistStat);
    }

    @Test
    public void testCalculateEffectiveChangeWhenStopsAtZeroFromNegative() {
        when(mockStatisticCalculation.calculate(any(), any())).thenReturn(0);

        var statusEffectLevel = randomIntWithInclusiveCeiling(-1);
        when(mockCharacterStatusEffects.getStatusEffectLevel(mockStatusEffectType))
                .thenReturn(statusEffectLevel);

        var baseAmount = (1 - statusEffectLevel);
        var expectedEffectiveChange = -statusEffectLevel;

        var output = statusEffectResistanceCalculation.calculateEffectiveChange(mockCharacter,
                mockStatusEffectType, baseAmount, true, mockElement);

        assertEquals(expectedEffectiveChange, output);
        verify(mockCharacter).statusEffects();
        verify(mockCharacterStatusEffects).getStatusEffectLevel(mockStatusEffectType);
    }

    @Test
    public void testCalculateEffectiveChangeWhenStopsAtZeroFromPositive() {
        when(mockStatisticCalculation.calculate(any(), any())).thenReturn(0);

        var statusEffectLevel = randomIntWithInclusiveFloor(1);
        when(mockCharacterStatusEffects.getStatusEffectLevel(mockStatusEffectType))
                .thenReturn(statusEffectLevel);

        var baseAmount = -(statusEffectLevel + 1);
        var expectedEffectiveChange = -statusEffectLevel;

        var output = statusEffectResistanceCalculation.calculateEffectiveChange(mockCharacter,
                mockStatusEffectType, baseAmount, true, mockElement);

        assertEquals(expectedEffectiveChange, output);
        verify(mockCharacter).statusEffects();
        verify(mockCharacterStatusEffects).getStatusEffectLevel(mockStatusEffectType);
    }

    @Test
    public void testCalculateEffectiveChangeWithInvalidParams() {
        assertThrows(IllegalArgumentException.class,
                () -> statusEffectResistanceCalculation.calculateEffectiveChange(null,
                        mockStatusEffectType, randomInt(), randomBoolean(), mockElement));
        assertThrows(IllegalArgumentException.class,
                () -> statusEffectResistanceCalculation.calculateEffectiveChange(mockCharacter,
                        null, randomInt(), randomBoolean(), mockElement));
        assertThrows(IllegalArgumentException.class,
                () -> statusEffectResistanceCalculation.calculateEffectiveChange(mockCharacter,
                        mockStatusEffectType, randomInt(), randomBoolean(), null));
    }

    @Test
    public void testGetInterfaceName() {
        assertEquals(StatusEffectResistanceCalculation.class.getCanonicalName(),
                statusEffectResistanceCalculation.getInterfaceName());
    }
}
