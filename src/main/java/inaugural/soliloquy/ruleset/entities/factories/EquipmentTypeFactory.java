package inaugural.soliloquy.ruleset.entities.factories;

import inaugural.soliloquy.tools.Check;
import soliloquy.specs.common.factories.Factory;
import soliloquy.specs.ruleset.definitions.EquipmentTypeDefinition;
import soliloquy.specs.ruleset.entities.EquipmentType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class EquipmentTypeFactory implements Factory<EquipmentTypeDefinition, EquipmentType> {
    @Override
    public EquipmentType make(EquipmentTypeDefinition equipmentTypeDefinition)
            throws IllegalArgumentException {
        Check.ifNull(equipmentTypeDefinition, "equipmentTypeDefinition");

        return new EquipmentType() {
            private final String ID = Check.ifNullOrEmpty(equipmentTypeDefinition.id, "equipmentTypeDefinition.id");
            private final HashSet<String> EQUIPABLE_SLOT_TYPES = new HashSet<>(Arrays.asList(Check.ifNull(equipmentTypeDefinition.equipableSlotTypes, "equipmentTypeDefinition.equipableSlotTypes")));

            private String name = Check.ifNullOrEmpty(equipmentTypeDefinition.name, "equipmentTypeDefinition.name");
            private String pluralName = Check.ifNullOrEmpty(equipmentTypeDefinition.pluralName, "equipmentTypeDefinition.pluralName");

            @Override
            public boolean canEquipToSlotType(String equipmentSlotType)
                    throws IllegalArgumentException {
                return EQUIPABLE_SLOT_TYPES.contains(
                        Check.ifNullOrEmpty(equipmentSlotType, "equipmentSlotType"));
            }

            @Override
            public String id() throws IllegalStateException {
                return ID;
            }

            @Override
            public String getPluralName() {
                return pluralName;
            }

            @Override
            public void setPluralName(String pluralName) throws IllegalArgumentException {
                this.pluralName = Check.ifNullOrEmpty(pluralName, "pluralName");
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public void setName(String name) {
                this.name = Check.ifNullOrEmpty(name, "name");
            }

            @Override
            public String getInterfaceName() {
                return EquipmentType.class.getCanonicalName();
            }
        };
    }

    @Override
    public String getInterfaceName() {
        return Factory.class.getCanonicalName() + "<" +
                EquipmentTypeDefinition.class.getCanonicalName() + "," +
                EquipmentType.class.getCanonicalName() + ">";
    }
}
