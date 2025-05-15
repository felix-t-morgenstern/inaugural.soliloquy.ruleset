package inaugural.soliloquy.ruleset.entities.factories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import inaugural.soliloquy.ruleset.definitions.EquipmentTypeDefinition;
import soliloquy.specs.ruleset.entities.EquipmentType;

import java.util.function.Function;

import static inaugural.soliloquy.tools.random.Random.randomString;
import static org.junit.jupiter.api.Assertions.*;

public class EquipmentTypeFactoryTests {
    private final String ID = randomString();
    private final String NAME = randomString();
    private final String PLURAL_NAME = randomString();
    private final String EQUIPABLE_SLOT_TYPE_1 = randomString();
    private final String EQUIPABLE_SLOT_TYPE_2 = randomString();
    private final String EQUIPABLE_SLOT_TYPE_3 = randomString();
    private final String[] EQUIPABLE_SLOT_TYPES =
            new String[]{EQUIPABLE_SLOT_TYPE_1, EQUIPABLE_SLOT_TYPE_2, EQUIPABLE_SLOT_TYPE_3};
    private final EquipmentTypeDefinition DEFINITION =
            new EquipmentTypeDefinition(ID, NAME, PLURAL_NAME, EQUIPABLE_SLOT_TYPES);

    private Function<EquipmentTypeDefinition, EquipmentType> equipmentTypeFactory;

    @BeforeEach
    public void setUp() {
        equipmentTypeFactory = new EquipmentTypeFactory();
    }

    @Test
    public void testMake() {
        var equipmentType = equipmentTypeFactory.apply(DEFINITION);

        assertNotNull(equipmentType);
        assertEquals(ID, equipmentType.id());
        assertEquals(NAME, equipmentType.getName());
        assertEquals(PLURAL_NAME, equipmentType.getPluralName());
        for (var equipableSlotType : EQUIPABLE_SLOT_TYPES) {
            assertTrue(equipmentType.canEquipToSlotType(equipableSlotType));
        }
    }

    @Test
    public void testMakeWithInvalidArgs() {
        assertThrows(IllegalArgumentException.class, () -> equipmentTypeFactory.apply(null));
        assertThrows(IllegalArgumentException.class, () -> equipmentTypeFactory.apply(
                new EquipmentTypeDefinition(null, NAME, PLURAL_NAME, EQUIPABLE_SLOT_TYPES)));
        assertThrows(IllegalArgumentException.class, () -> equipmentTypeFactory.apply(
                new EquipmentTypeDefinition("", NAME, PLURAL_NAME, EQUIPABLE_SLOT_TYPES)));
        assertThrows(IllegalArgumentException.class, () -> equipmentTypeFactory.apply(
                new EquipmentTypeDefinition(ID, null, PLURAL_NAME, EQUIPABLE_SLOT_TYPES)));
        assertThrows(IllegalArgumentException.class, () -> equipmentTypeFactory.apply(
                new EquipmentTypeDefinition(ID, "", PLURAL_NAME, EQUIPABLE_SLOT_TYPES)));
        assertThrows(IllegalArgumentException.class, () -> equipmentTypeFactory.apply(
                new EquipmentTypeDefinition(ID, NAME, null, EQUIPABLE_SLOT_TYPES)));
        assertThrows(IllegalArgumentException.class, () -> equipmentTypeFactory.apply(
                new EquipmentTypeDefinition(ID, NAME, "", EQUIPABLE_SLOT_TYPES)));
        assertThrows(IllegalArgumentException.class, () -> equipmentTypeFactory.apply(
                new EquipmentTypeDefinition(ID, NAME, PLURAL_NAME, null)));
    }

    @Test
    public void testMutatePropertiesOnCreatedEquipmentType() {
        var newName = randomString();
        var newPluralName = randomString();
        var equipmentType = equipmentTypeFactory.apply(DEFINITION);

        equipmentType.setName(newName);
        equipmentType.setPluralName(newPluralName);

        assertEquals(newName, equipmentType.getName());
        assertEquals(newPluralName, equipmentType.getPluralName());
    }

    @Test
    public void testMutatePropertiesOnCreatedEquipmentTypeWithInvalidArgs() {
        var equipmentType = equipmentTypeFactory.apply(DEFINITION);

        assertThrows(IllegalArgumentException.class, () -> equipmentType.setName(null));
        assertThrows(IllegalArgumentException.class, () -> equipmentType.setName(""));
        assertThrows(IllegalArgumentException.class, () -> equipmentType.setPluralName(null));
        assertThrows(IllegalArgumentException.class, () -> equipmentType.setPluralName(""));
    }

    @Test
    public void testCanEquipToSlotTypeWithInvalidArgs() {
        var equipmentType = equipmentTypeFactory.apply(DEFINITION);

        assertThrows(IllegalArgumentException.class, () -> equipmentType.canEquipToSlotType(null));
        assertThrows(IllegalArgumentException.class, () -> equipmentType.canEquipToSlotType(""));
    }
}
