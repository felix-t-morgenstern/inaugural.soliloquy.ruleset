package inaugural.soliloquy.ruleset.entities.factories;

import inaugural.soliloquy.tools.Check;
import soliloquy.specs.common.factories.Factory;
import soliloquy.specs.common.infrastructure.VariableCache;
import soliloquy.specs.common.persistence.TypeHandler;
import soliloquy.specs.common.valueobjects.Vertex;
import soliloquy.specs.gamestate.entities.Character;
import soliloquy.specs.graphics.assets.ImageAssetSet;
import inaugural.soliloquy.ruleset.definitions.ItemTypeDefinition;
import soliloquy.specs.ruleset.entities.EquipmentType;
import soliloquy.specs.ruleset.entities.ItemType;
import soliloquy.specs.ruleset.entities.abilities.ActiveAbility;
import soliloquy.specs.ruleset.entities.abilities.PassiveAbility;
import soliloquy.specs.ruleset.entities.abilities.ReactiveAbility;

import java.util.List;
import java.util.function.Function;

import static inaugural.soliloquy.tools.collections.Collections.listOf;

public class ItemTypeFactory implements Factory<ItemTypeDefinition, ItemType> {
    private final TypeHandler<VariableCache> VARIABLE_CACHE_HANDLER;
    private final Function<String, EquipmentType> GET_EQUIPMENT_TYPE;
    private final Function<String, ImageAssetSet> GET_IMAGE_ASSET_SET;
    private final Function<String, Function<Character, String>> GET_DESCRIPTION_FUNCTION;
    private final Function<String, ActiveAbility> GET_ACTIVE_ABILITY;
    private final Function<String, ReactiveAbility> GET_REACTIVE_ABILITY;
    private final Function<String, PassiveAbility> GET_PASSIVE_ABILITY;

    public ItemTypeFactory(
            TypeHandler<VariableCache> variableCacheHandler,
            Function<String, EquipmentType> getEquipmentType,
            Function<String, ImageAssetSet> getImageAssetSet,
            Function<String, Function<Character, String>> getDescriptionFunction,
            Function<String, ActiveAbility> getActiveAbility,
            Function<String, ReactiveAbility> getReactiveAbility,
            Function<String, PassiveAbility> getPassiveAbility) {
        VARIABLE_CACHE_HANDLER = Check.ifNull(variableCacheHandler, "variableCacheHandler");
        GET_EQUIPMENT_TYPE = Check.ifNull(getEquipmentType, "getEquipmentType");
        GET_IMAGE_ASSET_SET = Check.ifNull(getImageAssetSet, "getImageAssetSet");
        GET_DESCRIPTION_FUNCTION = Check.ifNull(getDescriptionFunction, "getDescriptionFunction");
        GET_ACTIVE_ABILITY = Check.ifNull(getActiveAbility, "getActiveAbility");
        GET_REACTIVE_ABILITY = Check.ifNull(getReactiveAbility, "getReactiveAbility");
        GET_PASSIVE_ABILITY = Check.ifNull(getPassiveAbility, "getPassiveAbility");
    }

    @Override
    public ItemType make(ItemTypeDefinition definition) throws IllegalArgumentException {
        Check.ifNull(definition, "definition");
        Check.ifNullOrEmpty(definition.id, "definition.id");
        Check.ifNullOrEmpty(definition.name, "definition.name");
        Check.ifNullOrEmpty(definition.pluralName, "definition.pluralName");

        if (definition.isStackable && definition.defaultNumberInStack == null) {
            throw new IllegalArgumentException(
                    "ItemFactory.make: If isStackable is true, defaultNumberInStack must be " +
                            "defined");
        }
        if (!definition.isStackable && definition.defaultNumberInStack != null) {
            throw new IllegalArgumentException(
                    "ItemFactory.make: If isStackable is false, defaultNumberInStack cannot be " +
                            "defined");
        }
        if (definition.isStackable) {
            Check.ifNonNegative(definition.defaultNumberInStack, "definition.defaultNumberInStack");
        }

        if (definition.hasCharges && definition.defaultCharges == null) {
            throw new IllegalArgumentException(
                    "ItemFactory.make: If hasCharges is true, defaultCharges must be defined");
        }
        if (!definition.hasCharges && definition.defaultCharges != null) {
            throw new IllegalArgumentException(
                    "ItemFactory.make: If hasCharges is false, defaultCharges cannot be defined");
        }
        if (definition.hasCharges) {
            Check.ifNonNegative(definition.defaultCharges, "definition.defaultCharges");
        }

        if (definition.isStackable && definition.hasCharges) {
            throw new IllegalArgumentException(
                    "ItemFactory.make: Both isStackable and hasCharges cannot both be true");
        }

        EquipmentType equipmentType = GET_EQUIPMENT_TYPE.apply(Check
                .ifNullOrEmpty(definition.equipmentTypeId, "definition.equipmentTypeId"));
        if (equipmentType == null) {
            throw new IllegalArgumentException(
                    "ItemTypeFactory.make: definition.equipmentTypeId (" +
                            definition.equipmentTypeId +
                            ") does not correspond to a valid EquipmentType");
        }

        Function<Character, String> descriptionFunction =
                GET_DESCRIPTION_FUNCTION.apply(Check.ifNullOrEmpty(definition.descriptionFunctionId,
                        "definition.descriptionFunctionId"));
        if (descriptionFunction == null) {
            throw new IllegalArgumentException(
                    "ItemTypeFactory.make: definition.descriptionFunctionId (" +
                            definition.descriptionFunctionId +
                            ") does not correspond to a valid Function");
        }

        VariableCache traits = VARIABLE_CACHE_HANDLER
                .read(Check.ifNullOrEmpty(definition.traits, "definition.traits"));

        List<ActiveAbility> activeAbilities =
                populateEntityList(definition.activeAbilityIds, GET_ACTIVE_ABILITY,
                        "definition.activeAbilityIds");

        List<ReactiveAbility> reactiveAbilities =
                populateEntityList(definition.reactiveAbilityIds, GET_REACTIVE_ABILITY,
                        "definition.reactiveAbilityIds");

        List<PassiveAbility> passiveAbilities =
                populateEntityList(definition.passiveAbilityIds, GET_PASSIVE_ABILITY,
                        "definition.passiveAbilityIds");

        ImageAssetSet imageAssetSet = GET_IMAGE_ASSET_SET.apply(Check
                .ifNullOrEmpty(definition.imageAssetSetId, "definition.imageAssetSetId"));
        if (imageAssetSet == null) {
            throw new IllegalArgumentException(
                    "ItemTypeFactory.make: definition.imageAssetSetId (" +
                            definition.imageAssetSetId +
                            ") does not correspond to a valid ImageAssetSet");
        }

        return new ItemType() {
            private String name = definition.name;
            private String pluralName = definition.pluralName;

            @Override
            public EquipmentType equipmentType() {
                return equipmentType;
            }

            @Override
            public Function<Character, String> descriptionFunction() {
                return descriptionFunction;
            }

            @Override
            public VariableCache traits() {
                return traits;
            }

            @Override
            public boolean isStackable() {
                return definition.isStackable;
            }

            @Override
            public int defaultNumberInStack() throws UnsupportedOperationException {
                if (!isStackable()) {
                    throw new UnsupportedOperationException(
                            "ItemType.defaultNumberInStack: ItemType is not stackable");
                }
                return definition.defaultNumberInStack;
            }

            @Override
            public boolean hasCharges() {
                return definition.hasCharges;
            }

            @Override
            public int defaultCharges() throws UnsupportedOperationException {
                if (!hasCharges()) {
                    throw new UnsupportedOperationException(
                            "ItemType.defaultCharges: ItemType does not have charges");
                }
                return definition.defaultCharges;
            }

            @Override
            public List<ActiveAbility> defaultActiveAbilities() {
                return activeAbilities;
            }

            @Override
            public List<ReactiveAbility> defaultReactiveAbilities() {
                return reactiveAbilities;
            }

            @Override
            public List<PassiveAbility> defaultPassiveAbilities() {
                return passiveAbilities;
            }

            @Override
            public ImageAssetSet imageAssetSet() {
                return imageAssetSet;
            }

            @Override
            public String id() throws IllegalStateException {
                return definition.id;
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
                return ItemType.class.getCanonicalName();
            }

            @Override
            public Vertex defaultTileOffset() {
                return Vertex.of(definition.defaultXTileWidthOffset,
                        definition.defaultYTileHeightOffset);
            }
        };
    }

    private <T> List<T> populateEntityList(String[] entityIds,
                                           Function<String, T> getEntity,
                                           String entityIdListName) {
        List<T> entityList = listOf();

        for (var entityId : entityIds) {
            Check.ifNullOrEmpty(entityId, "Id within " + entityIdListName);
            T entity = getEntity.apply(entityId);
            if (entity == null) {
                throw new IllegalArgumentException(
                        "ItemTypeFactory.populateEntityList: Id within " + entityIdListName + " (" +
                                entityId + ") does not correspond to a valid entity");
            }
            entityList.add(entity);
        }

        return entityList;
    }

    @Override
    public String getInterfaceName() {
        return Factory.class.getCanonicalName() + "<" +
                ItemTypeDefinition.class.getCanonicalName() + "," +
                ItemType.class.getCanonicalName() + ">";
    }
}
