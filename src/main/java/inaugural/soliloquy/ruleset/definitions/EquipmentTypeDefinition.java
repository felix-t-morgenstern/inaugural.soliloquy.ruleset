package inaugural.soliloquy.ruleset.definitions;

public class EquipmentTypeDefinition {
    public String id;
    public String name;
    public String pluralName;
    public String[] equipableSlotTypes;

    public EquipmentTypeDefinition(String id, String name, String pluralName,
                                   String[] equipableSlotTypes) {
        this.id = id;
        this.name = name;
        this.pluralName = pluralName;
        this.equipableSlotTypes = equipableSlotTypes;
    }
}
