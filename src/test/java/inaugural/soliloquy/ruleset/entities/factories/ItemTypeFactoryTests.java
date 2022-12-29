package inaugural.soliloquy.ruleset.entities.factories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import soliloquy.specs.common.entities.Function;
import soliloquy.specs.common.factories.Factory;
import soliloquy.specs.common.infrastructure.VariableCache;
import soliloquy.specs.common.persistence.TypeHandler;
import soliloquy.specs.common.valueobjects.Vertex;
import soliloquy.specs.gamestate.entities.Character;
import soliloquy.specs.graphics.assets.ImageAssetSet;
import soliloquy.specs.ruleset.definitions.ItemTypeDefinition;
import soliloquy.specs.ruleset.entities.EquipmentType;
import soliloquy.specs.ruleset.entities.ItemType;
import soliloquy.specs.ruleset.entities.abilities.ActiveAbility;
import soliloquy.specs.ruleset.entities.abilities.PassiveAbility;
import soliloquy.specs.ruleset.entities.abilities.ReactiveAbility;

import java.util.ArrayList;

import static inaugural.soliloquy.tools.random.Random.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ItemTypeFactoryTests {
    private final String ID = randomString();
    private final String NAME = randomString();
    private final String PLURAL_NAME = randomString();
    private final String EQUIPMENT_TYPE_ID = randomString();
    private final String IMAGE_ASSET_SET_ID = randomString();
    private final float DEFAULT_X_TILE_WIDTH_OFFSET = randomFloat();
    private final float DEFAULT_Y_TILE_HEIGHT_OFFSET = randomFloat();
    private final String DESCRIPTION_FUNCTION_ID = randomString();
    private final String TRAITS = randomString();
    private final int DEFAULT_NUMBER_IN_STACK = randomIntWithInclusiveFloor(1);
    private final int DEFAULT_CHARGES = randomIntWithInclusiveFloor(1);
    private final String ACTIVE_ABILITY_ID = randomString();
    private final String REACTIVE_ABILITY_ID = randomString();
    private final String PASSIVE_ABILITY_ID = randomString();

    private final ItemTypeDefinition DEFINITION =
            new ItemTypeDefinition(ID, NAME, PLURAL_NAME, EQUIPMENT_TYPE_ID, IMAGE_ASSET_SET_ID,
                    DEFAULT_X_TILE_WIDTH_OFFSET, DEFAULT_Y_TILE_HEIGHT_OFFSET,
                    DESCRIPTION_FUNCTION_ID, TRAITS, false, null, false, null,
                    new String[]{ACTIVE_ABILITY_ID}, new String[]{REACTIVE_ABILITY_ID},
                    new String[]{PASSIVE_ABILITY_ID});

    @Mock private VariableCache mockVariableCache;
    @Mock private TypeHandler<VariableCache> mockVariableCacheHandler;
    @Mock private EquipmentType mockEquipmentType;
    @Mock private java.util.function.Function<String, EquipmentType> mockGetEquipmentType;
    @Mock private ImageAssetSet mockImageAssetSet;
    @Mock private java.util.function.Function<String, ImageAssetSet> mockGetImageAssetSet;
    @Mock private Function<Character, String> mockDescriptionFunction;
    @Mock private java.util.function.Function<String, Function<Character, String>>
            mockGetDescriptionFunction;
    @Mock private ActiveAbility mockActiveAbility;
    @Mock private java.util.function.Function<String, ActiveAbility> mockGetActiveAbility;
    @Mock private ReactiveAbility mockReactiveAbility;
    @Mock private java.util.function.Function<String, ReactiveAbility> mockGetReactiveAbility;
    @Mock private PassiveAbility mockPassiveAbility;
    @Mock private java.util.function.Function<String, PassiveAbility> mockGetPassiveAbility;

    private Factory<ItemTypeDefinition, ItemType> factory;

    @BeforeEach
    void setUp() {
        mockVariableCache = mock(VariableCache.class);

        //noinspection unchecked
        mockVariableCacheHandler = (TypeHandler<VariableCache>) mock(TypeHandler.class);
        when(mockVariableCacheHandler.read(anyString())).thenReturn(mockVariableCache);

        mockEquipmentType = mock(EquipmentType.class);

        //noinspection unchecked
        mockGetEquipmentType = (java.util.function.Function<String, EquipmentType>) mock(
                java.util.function.Function.class);
        when(mockGetEquipmentType.apply(anyString())).thenReturn(mockEquipmentType);

        mockImageAssetSet = mock(ImageAssetSet.class);

        //noinspection unchecked
        mockGetImageAssetSet = (java.util.function.Function<String, ImageAssetSet>) mock(
                java.util.function.Function.class);
        when(mockGetImageAssetSet.apply(anyString())).thenReturn(mockImageAssetSet);

        //noinspection unchecked
        mockDescriptionFunction = (Function<Character, String>) mock(Function.class);

        //noinspection unchecked
        mockGetDescriptionFunction =
                (java.util.function.Function<String, Function<Character, String>>) mock(
                        java.util.function.Function.class);
        when(mockGetDescriptionFunction.apply(anyString())).thenReturn(mockDescriptionFunction);

        mockActiveAbility = mock(ActiveAbility.class);

        //noinspection unchecked
        mockGetActiveAbility = (java.util.function.Function<String, ActiveAbility>) mock(
                java.util.function.Function.class);
        when(mockGetActiveAbility.apply(anyString())).thenReturn(mockActiveAbility);

        mockReactiveAbility = mock(ReactiveAbility.class);

        //noinspection unchecked
        mockGetReactiveAbility = (java.util.function.Function<String, ReactiveAbility>) mock(
                java.util.function.Function.class);
        when(mockGetReactiveAbility.apply(anyString())).thenReturn(mockReactiveAbility);

        mockPassiveAbility = mock(PassiveAbility.class);

        //noinspection unchecked
        mockGetPassiveAbility = (java.util.function.Function<String, PassiveAbility>) mock(
                java.util.function.Function.class);
        when(mockGetPassiveAbility.apply(anyString())).thenReturn(mockPassiveAbility);

        factory = new ItemTypeFactory(mockVariableCacheHandler, mockGetEquipmentType,
                mockGetImageAssetSet, mockGetDescriptionFunction, mockGetActiveAbility,
                mockGetReactiveAbility, mockGetPassiveAbility);
    }

    @Test
    void testConstructorWithInvalidParams() {
        assertThrows(IllegalArgumentException.class,
                () -> new ItemTypeFactory(null, mockGetEquipmentType, mockGetImageAssetSet,
                        mockGetDescriptionFunction, mockGetActiveAbility, mockGetReactiveAbility,
                        mockGetPassiveAbility));
        assertThrows(IllegalArgumentException.class,
                () -> new ItemTypeFactory(mockVariableCacheHandler, null, mockGetImageAssetSet,
                        mockGetDescriptionFunction, mockGetActiveAbility, mockGetReactiveAbility,
                        mockGetPassiveAbility));
        assertThrows(IllegalArgumentException.class,
                () -> new ItemTypeFactory(mockVariableCacheHandler, mockGetEquipmentType, null,
                        mockGetDescriptionFunction, mockGetActiveAbility, mockGetReactiveAbility,
                        mockGetPassiveAbility));
        assertThrows(IllegalArgumentException.class,
                () -> new ItemTypeFactory(mockVariableCacheHandler, mockGetEquipmentType,
                        mockGetImageAssetSet, null, mockGetActiveAbility, mockGetReactiveAbility,
                        mockGetPassiveAbility));
        assertThrows(IllegalArgumentException.class,
                () -> new ItemTypeFactory(mockVariableCacheHandler, mockGetEquipmentType,
                        mockGetImageAssetSet, mockGetDescriptionFunction, null,
                        mockGetReactiveAbility, mockGetPassiveAbility));
        assertThrows(IllegalArgumentException.class,
                () -> new ItemTypeFactory(mockVariableCacheHandler, mockGetEquipmentType,
                        mockGetImageAssetSet, mockGetDescriptionFunction, mockGetActiveAbility,
                        null, mockGetPassiveAbility));
        assertThrows(IllegalArgumentException.class,
                () -> new ItemTypeFactory(mockVariableCacheHandler, mockGetEquipmentType,
                        mockGetImageAssetSet, mockGetDescriptionFunction, mockGetActiveAbility,
                        mockGetReactiveAbility, null));
    }

    @Test
    void testMake() {
        ItemType output = factory.make(DEFINITION);

        assertNotNull(output);
        assertEquals(ID, output.id());
        assertEquals(NAME, output.getName());
        assertEquals(PLURAL_NAME, output.getPluralName());
        assertSame(mockEquipmentType, output.equipmentType());
        assertSame(mockImageAssetSet, output.imageAssetSet());
        assertEquals(Vertex.of(DEFAULT_X_TILE_WIDTH_OFFSET, DEFAULT_Y_TILE_HEIGHT_OFFSET),
                output.defaultTileOffset());
        assertSame(mockDescriptionFunction, output.descriptionFunction());
        assertSame(mockVariableCache, output.traits());
        assertFalse(output.isStackable());
        assertThrows(UnsupportedOperationException.class, output::defaultNumberInStack);
        assertFalse(output.hasCharges());
        assertThrows(UnsupportedOperationException.class, output::defaultCharges);
        assertEquals(new ArrayList<>() {{
            add(mockActiveAbility);
        }}, output.defaultActiveAbilities());
        assertEquals(new ArrayList<>() {{
            add(mockReactiveAbility);
        }}, output.defaultReactiveAbilities());
        assertEquals(new ArrayList<>() {{
            add(mockPassiveAbility);
        }}, output.defaultPassiveAbilities());
        assertEquals(ItemType.class.getCanonicalName(), output.getInterfaceName());
        verify(mockGetEquipmentType, times(1)).apply(EQUIPMENT_TYPE_ID);
        verify(mockGetImageAssetSet, times(1)).apply(IMAGE_ASSET_SET_ID);
        verify(mockGetDescriptionFunction, times(1)).apply(DESCRIPTION_FUNCTION_ID);
        verify(mockVariableCacheHandler, times(1)).read(TRAITS);
        verify(mockGetActiveAbility, times(1)).apply(ACTIVE_ABILITY_ID);
        verify(mockGetReactiveAbility, times(1)).apply(REACTIVE_ABILITY_ID);
        verify(mockGetPassiveAbility, times(1)).apply(PASSIVE_ABILITY_ID);
    }

    @Test
    void testMakeStackable() {
        ItemTypeDefinition stackableDefinition =
                new ItemTypeDefinition(ID, NAME, PLURAL_NAME, EQUIPMENT_TYPE_ID, IMAGE_ASSET_SET_ID,
                        DEFAULT_X_TILE_WIDTH_OFFSET, DEFAULT_Y_TILE_HEIGHT_OFFSET,
                        DESCRIPTION_FUNCTION_ID, TRAITS, true, DEFAULT_NUMBER_IN_STACK, false, null,
                        new String[]{ACTIVE_ABILITY_ID}, new String[]{REACTIVE_ABILITY_ID},
                        new String[]{PASSIVE_ABILITY_ID});

        ItemType output = factory.make(stackableDefinition);

        assertTrue(output.isStackable());
        assertEquals(DEFAULT_NUMBER_IN_STACK, output.defaultNumberInStack());
        assertFalse(output.hasCharges());
        assertThrows(UnsupportedOperationException.class, output::defaultCharges);
    }

    @Test
    void testMakeWithCharges() {
        ItemTypeDefinition definitionWithCharges =
                new ItemTypeDefinition(ID, NAME, PLURAL_NAME, EQUIPMENT_TYPE_ID, IMAGE_ASSET_SET_ID,
                        DEFAULT_X_TILE_WIDTH_OFFSET, DEFAULT_Y_TILE_HEIGHT_OFFSET,
                        DESCRIPTION_FUNCTION_ID, TRAITS, false, null, true, DEFAULT_CHARGES,
                        new String[]{ACTIVE_ABILITY_ID}, new String[]{REACTIVE_ABILITY_ID},
                        new String[]{PASSIVE_ABILITY_ID});

        ItemType output = factory.make(definitionWithCharges);

        assertTrue(output.hasCharges());
        assertEquals(DEFAULT_CHARGES, output.defaultCharges());
        assertFalse(output.isStackable());
        assertThrows(UnsupportedOperationException.class, output::defaultNumberInStack);
    }

    @Test
    void testSetAndGetName() {
        String newName = randomString();
        ItemType output = factory.make(DEFINITION);

        output.setName(newName);

        assertEquals(newName, output.getName());
    }

    @Test
    void testSetNameWithInvalidParams() {
        ItemType output = factory.make(DEFINITION);

        assertThrows(IllegalArgumentException.class, () -> output.setName(null));
        assertThrows(IllegalArgumentException.class, () -> output.setName(""));
    }

    @Test
    void testSetAndGetPluralName() {
        String newPluralName = randomString();
        ItemType output = factory.make(DEFINITION);

        output.setPluralName(newPluralName);

        assertEquals(newPluralName, output.getPluralName());
    }

    @Test
    void testSetPluralNameWithInvalidParams() {
        ItemType output = factory.make(DEFINITION);

        assertThrows(IllegalArgumentException.class, () -> output.setPluralName(null));
        assertThrows(IllegalArgumentException.class, () -> output.setPluralName(""));
    }

    @Test
    void testMakeWithInvalidParams() {
        String invalidEquipmentTypeId = randomString();
        when(mockGetEquipmentType.apply(invalidEquipmentTypeId)).thenReturn(null);
        String invalidImageAssetSetId = randomString();
        when(mockGetImageAssetSet.apply(invalidImageAssetSetId)).thenReturn(null);
        String invalidDescriptionFunctionId = randomString();
        when(mockGetDescriptionFunction.apply(invalidDescriptionFunctionId)).thenReturn(null);
        String invalidActiveAbilityId = randomString();
        when(mockGetActiveAbility.apply(invalidActiveAbilityId)).thenReturn(null);
        String invalidReactiveAbilityId = randomString();
        when(mockGetReactiveAbility.apply(invalidReactiveAbilityId)).thenReturn(null);
        String invalidPassiveAbilityId = randomString();
        when(mockGetPassiveAbility.apply(invalidPassiveAbilityId)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> factory.make(null));
        assertThrows(IllegalArgumentException.class, () -> factory
                .make(new ItemTypeDefinition(null, NAME, PLURAL_NAME, EQUIPMENT_TYPE_ID,
                        IMAGE_ASSET_SET_ID, DEFAULT_X_TILE_WIDTH_OFFSET,
                        DEFAULT_Y_TILE_HEIGHT_OFFSET, DESCRIPTION_FUNCTION_ID, TRAITS, false, null,
                        false, null, new String[]{ACTIVE_ABILITY_ID},
                        new String[]{REACTIVE_ABILITY_ID}, new String[]{PASSIVE_ABILITY_ID})));
        assertThrows(IllegalArgumentException.class, () -> factory
                .make(new ItemTypeDefinition("", NAME, PLURAL_NAME, EQUIPMENT_TYPE_ID,
                        IMAGE_ASSET_SET_ID, DEFAULT_X_TILE_WIDTH_OFFSET,
                        DEFAULT_Y_TILE_HEIGHT_OFFSET, DESCRIPTION_FUNCTION_ID, TRAITS, false, null,
                        false, null, new String[]{ACTIVE_ABILITY_ID},
                        new String[]{REACTIVE_ABILITY_ID}, new String[]{PASSIVE_ABILITY_ID})));
        assertThrows(IllegalArgumentException.class, () -> factory
                .make(new ItemTypeDefinition(ID, null, PLURAL_NAME, EQUIPMENT_TYPE_ID,
                        IMAGE_ASSET_SET_ID, DEFAULT_X_TILE_WIDTH_OFFSET,
                        DEFAULT_Y_TILE_HEIGHT_OFFSET, DESCRIPTION_FUNCTION_ID, TRAITS, false, null,
                        false, null, new String[]{ACTIVE_ABILITY_ID},
                        new String[]{REACTIVE_ABILITY_ID}, new String[]{PASSIVE_ABILITY_ID})));
        assertThrows(IllegalArgumentException.class, () -> factory
                .make(new ItemTypeDefinition(ID, "", PLURAL_NAME, EQUIPMENT_TYPE_ID,
                        IMAGE_ASSET_SET_ID, DEFAULT_X_TILE_WIDTH_OFFSET,
                        DEFAULT_Y_TILE_HEIGHT_OFFSET, DESCRIPTION_FUNCTION_ID, TRAITS, false, null,
                        false, null, new String[]{ACTIVE_ABILITY_ID},
                        new String[]{REACTIVE_ABILITY_ID}, new String[]{PASSIVE_ABILITY_ID})));
        assertThrows(IllegalArgumentException.class, () -> factory
                .make(new ItemTypeDefinition(ID, NAME, null, EQUIPMENT_TYPE_ID, IMAGE_ASSET_SET_ID,
                        DEFAULT_X_TILE_WIDTH_OFFSET, DEFAULT_Y_TILE_HEIGHT_OFFSET,
                        DESCRIPTION_FUNCTION_ID, TRAITS, false, null, false, null,
                        new String[]{ACTIVE_ABILITY_ID}, new String[]{REACTIVE_ABILITY_ID},
                        new String[]{PASSIVE_ABILITY_ID})));
        assertThrows(IllegalArgumentException.class, () -> factory
                .make(new ItemTypeDefinition(ID, NAME, "", EQUIPMENT_TYPE_ID, IMAGE_ASSET_SET_ID,
                        DEFAULT_X_TILE_WIDTH_OFFSET, DEFAULT_Y_TILE_HEIGHT_OFFSET,
                        DESCRIPTION_FUNCTION_ID, TRAITS, false, null, false, null,
                        new String[]{ACTIVE_ABILITY_ID}, new String[]{REACTIVE_ABILITY_ID},
                        new String[]{PASSIVE_ABILITY_ID})));
        assertThrows(IllegalArgumentException.class, () -> factory
                .make(new ItemTypeDefinition(ID, NAME, PLURAL_NAME, null, IMAGE_ASSET_SET_ID,
                        DEFAULT_X_TILE_WIDTH_OFFSET, DEFAULT_Y_TILE_HEIGHT_OFFSET,
                        DESCRIPTION_FUNCTION_ID, TRAITS, false, null, false, null,
                        new String[]{ACTIVE_ABILITY_ID}, new String[]{REACTIVE_ABILITY_ID},
                        new String[]{PASSIVE_ABILITY_ID})));
        assertThrows(IllegalArgumentException.class, () -> factory
                .make(new ItemTypeDefinition(ID, NAME, PLURAL_NAME, "", IMAGE_ASSET_SET_ID,
                        DEFAULT_X_TILE_WIDTH_OFFSET, DEFAULT_Y_TILE_HEIGHT_OFFSET,
                        DESCRIPTION_FUNCTION_ID, TRAITS, false, null, false, null,
                        new String[]{ACTIVE_ABILITY_ID}, new String[]{REACTIVE_ABILITY_ID},
                        new String[]{PASSIVE_ABILITY_ID})));
        assertThrows(IllegalArgumentException.class, () -> factory
                .make(new ItemTypeDefinition(ID, NAME, PLURAL_NAME, invalidEquipmentTypeId,
                        IMAGE_ASSET_SET_ID, DEFAULT_X_TILE_WIDTH_OFFSET,
                        DEFAULT_Y_TILE_HEIGHT_OFFSET, DESCRIPTION_FUNCTION_ID, TRAITS, false, null,
                        false, null, new String[]{ACTIVE_ABILITY_ID},
                        new String[]{REACTIVE_ABILITY_ID}, new String[]{PASSIVE_ABILITY_ID})));
        assertThrows(IllegalArgumentException.class, () -> factory
                .make(new ItemTypeDefinition(ID, NAME, PLURAL_NAME, EQUIPMENT_TYPE_ID, null,
                        DEFAULT_X_TILE_WIDTH_OFFSET, DEFAULT_Y_TILE_HEIGHT_OFFSET,
                        DESCRIPTION_FUNCTION_ID, TRAITS, false, null, false, null,
                        new String[]{ACTIVE_ABILITY_ID}, new String[]{REACTIVE_ABILITY_ID},
                        new String[]{PASSIVE_ABILITY_ID})));
        assertThrows(IllegalArgumentException.class, () -> factory
                .make(new ItemTypeDefinition(ID, NAME, PLURAL_NAME, EQUIPMENT_TYPE_ID, "",
                        DEFAULT_X_TILE_WIDTH_OFFSET, DEFAULT_Y_TILE_HEIGHT_OFFSET,
                        DESCRIPTION_FUNCTION_ID, TRAITS, false, null, false, null,
                        new String[]{ACTIVE_ABILITY_ID}, new String[]{REACTIVE_ABILITY_ID},
                        new String[]{PASSIVE_ABILITY_ID})));
        assertThrows(IllegalArgumentException.class, () -> factory
                .make(new ItemTypeDefinition(ID, NAME, PLURAL_NAME, EQUIPMENT_TYPE_ID,
                        invalidImageAssetSetId, DEFAULT_X_TILE_WIDTH_OFFSET,
                        DEFAULT_Y_TILE_HEIGHT_OFFSET, DESCRIPTION_FUNCTION_ID, TRAITS, false, null,
                        false, null, new String[]{ACTIVE_ABILITY_ID},
                        new String[]{REACTIVE_ABILITY_ID}, new String[]{PASSIVE_ABILITY_ID})));
        assertThrows(IllegalArgumentException.class, () -> factory
                .make(new ItemTypeDefinition(ID, NAME, PLURAL_NAME, EQUIPMENT_TYPE_ID,
                        IMAGE_ASSET_SET_ID, DEFAULT_X_TILE_WIDTH_OFFSET,
                        DEFAULT_Y_TILE_HEIGHT_OFFSET, null, TRAITS, false, null, false, null,
                        new String[]{ACTIVE_ABILITY_ID}, new String[]{REACTIVE_ABILITY_ID},
                        new String[]{PASSIVE_ABILITY_ID})));
        assertThrows(IllegalArgumentException.class, () -> factory
                .make(new ItemTypeDefinition(ID, NAME, PLURAL_NAME, EQUIPMENT_TYPE_ID,
                        IMAGE_ASSET_SET_ID, DEFAULT_X_TILE_WIDTH_OFFSET,
                        DEFAULT_Y_TILE_HEIGHT_OFFSET, "", TRAITS, false, null, false, null,
                        new String[]{ACTIVE_ABILITY_ID}, new String[]{REACTIVE_ABILITY_ID},
                        new String[]{PASSIVE_ABILITY_ID})));
        assertThrows(IllegalArgumentException.class, () -> factory
                .make(new ItemTypeDefinition(ID, NAME, PLURAL_NAME, EQUIPMENT_TYPE_ID,
                        IMAGE_ASSET_SET_ID, DEFAULT_X_TILE_WIDTH_OFFSET,
                        DEFAULT_Y_TILE_HEIGHT_OFFSET, invalidDescriptionFunctionId, TRAITS, false,
                        null, false, null, new String[]{ACTIVE_ABILITY_ID},
                        new String[]{REACTIVE_ABILITY_ID}, new String[]{PASSIVE_ABILITY_ID})));
        assertThrows(IllegalArgumentException.class, () -> factory
                .make(new ItemTypeDefinition(ID, NAME, PLURAL_NAME, EQUIPMENT_TYPE_ID,
                        IMAGE_ASSET_SET_ID, DEFAULT_X_TILE_WIDTH_OFFSET,
                        DEFAULT_Y_TILE_HEIGHT_OFFSET, DESCRIPTION_FUNCTION_ID, null, false, null,
                        false, null, new String[]{ACTIVE_ABILITY_ID},
                        new String[]{REACTIVE_ABILITY_ID}, new String[]{PASSIVE_ABILITY_ID})));
        assertThrows(IllegalArgumentException.class, () -> factory
                .make(new ItemTypeDefinition(ID, NAME, PLURAL_NAME, EQUIPMENT_TYPE_ID,
                        IMAGE_ASSET_SET_ID, DEFAULT_X_TILE_WIDTH_OFFSET,
                        DEFAULT_Y_TILE_HEIGHT_OFFSET, DESCRIPTION_FUNCTION_ID, "", false, null,
                        false, null, new String[]{ACTIVE_ABILITY_ID},
                        new String[]{REACTIVE_ABILITY_ID}, new String[]{PASSIVE_ABILITY_ID})));
        assertThrows(IllegalArgumentException.class, () -> factory
                .make(new ItemTypeDefinition(ID, NAME, PLURAL_NAME, EQUIPMENT_TYPE_ID,
                        IMAGE_ASSET_SET_ID, DEFAULT_X_TILE_WIDTH_OFFSET,
                        DEFAULT_Y_TILE_HEIGHT_OFFSET, DESCRIPTION_FUNCTION_ID, TRAITS, true, null,
                        false, null, new String[]{ACTIVE_ABILITY_ID},
                        new String[]{REACTIVE_ABILITY_ID}, new String[]{PASSIVE_ABILITY_ID})));
        assertThrows(IllegalArgumentException.class, () -> factory
                .make(new ItemTypeDefinition(ID, NAME, PLURAL_NAME, EQUIPMENT_TYPE_ID,
                        IMAGE_ASSET_SET_ID, DEFAULT_X_TILE_WIDTH_OFFSET,
                        DEFAULT_Y_TILE_HEIGHT_OFFSET, DESCRIPTION_FUNCTION_ID, TRAITS, false,
                        DEFAULT_NUMBER_IN_STACK, false, null, new String[]{ACTIVE_ABILITY_ID},
                        new String[]{REACTIVE_ABILITY_ID}, new String[]{PASSIVE_ABILITY_ID})));
        assertThrows(IllegalArgumentException.class, () -> factory
                .make(new ItemTypeDefinition(ID, NAME, PLURAL_NAME, EQUIPMENT_TYPE_ID,
                        IMAGE_ASSET_SET_ID, DEFAULT_X_TILE_WIDTH_OFFSET,
                        DEFAULT_Y_TILE_HEIGHT_OFFSET, DESCRIPTION_FUNCTION_ID, TRAITS, true,
                        randomIntWithInclusiveCeiling(0), false, null,
                        new String[]{ACTIVE_ABILITY_ID}, new String[]{REACTIVE_ABILITY_ID},
                        new String[]{PASSIVE_ABILITY_ID})));
        assertThrows(IllegalArgumentException.class, () -> factory
                .make(new ItemTypeDefinition(ID, NAME, PLURAL_NAME, EQUIPMENT_TYPE_ID,
                        IMAGE_ASSET_SET_ID, DEFAULT_X_TILE_WIDTH_OFFSET,
                        DEFAULT_Y_TILE_HEIGHT_OFFSET, DESCRIPTION_FUNCTION_ID, TRAITS, false, null,
                        true, null, new String[]{ACTIVE_ABILITY_ID},
                        new String[]{REACTIVE_ABILITY_ID}, new String[]{PASSIVE_ABILITY_ID})));
        assertThrows(IllegalArgumentException.class, () -> factory
                .make(new ItemTypeDefinition(ID, NAME, PLURAL_NAME, EQUIPMENT_TYPE_ID,
                        IMAGE_ASSET_SET_ID, DEFAULT_X_TILE_WIDTH_OFFSET,
                        DEFAULT_Y_TILE_HEIGHT_OFFSET, DESCRIPTION_FUNCTION_ID, TRAITS, false, null,
                        false, DEFAULT_CHARGES, new String[]{ACTIVE_ABILITY_ID},
                        new String[]{REACTIVE_ABILITY_ID}, new String[]{PASSIVE_ABILITY_ID})));
        assertThrows(IllegalArgumentException.class, () -> factory
                .make(new ItemTypeDefinition(ID, NAME, PLURAL_NAME, EQUIPMENT_TYPE_ID,
                        IMAGE_ASSET_SET_ID, DEFAULT_X_TILE_WIDTH_OFFSET,
                        DEFAULT_Y_TILE_HEIGHT_OFFSET, DESCRIPTION_FUNCTION_ID, TRAITS, false, null,
                        true, randomIntWithInclusiveCeiling(0), new String[]{ACTIVE_ABILITY_ID},
                        new String[]{REACTIVE_ABILITY_ID}, new String[]{PASSIVE_ABILITY_ID})));
        assertThrows(IllegalArgumentException.class, () -> factory
                .make(new ItemTypeDefinition(ID, NAME, PLURAL_NAME, EQUIPMENT_TYPE_ID,
                        IMAGE_ASSET_SET_ID, DEFAULT_X_TILE_WIDTH_OFFSET,
                        DEFAULT_Y_TILE_HEIGHT_OFFSET, DESCRIPTION_FUNCTION_ID, TRAITS, true,
                        DEFAULT_NUMBER_IN_STACK, true, DEFAULT_CHARGES,
                        new String[]{ACTIVE_ABILITY_ID}, new String[]{REACTIVE_ABILITY_ID},
                        new String[]{PASSIVE_ABILITY_ID})));
        assertThrows(IllegalArgumentException.class, () -> factory
                .make(new ItemTypeDefinition(ID, NAME, PLURAL_NAME, EQUIPMENT_TYPE_ID,
                        IMAGE_ASSET_SET_ID, DEFAULT_X_TILE_WIDTH_OFFSET,
                        DEFAULT_Y_TILE_HEIGHT_OFFSET, DESCRIPTION_FUNCTION_ID, TRAITS, false, null,
                        false, null, new String[]{null}, new String[]{REACTIVE_ABILITY_ID},
                        new String[]{PASSIVE_ABILITY_ID})));
        assertThrows(IllegalArgumentException.class, () -> factory
                .make(new ItemTypeDefinition(ID, NAME, PLURAL_NAME, EQUIPMENT_TYPE_ID,
                        IMAGE_ASSET_SET_ID, DEFAULT_X_TILE_WIDTH_OFFSET,
                        DEFAULT_Y_TILE_HEIGHT_OFFSET, DESCRIPTION_FUNCTION_ID, TRAITS, false, null,
                        false, null, new String[]{""}, new String[]{REACTIVE_ABILITY_ID},
                        new String[]{PASSIVE_ABILITY_ID})));
        assertThrows(IllegalArgumentException.class, () -> factory
                .make(new ItemTypeDefinition(ID, NAME, PLURAL_NAME, EQUIPMENT_TYPE_ID,
                        IMAGE_ASSET_SET_ID, DEFAULT_X_TILE_WIDTH_OFFSET,
                        DEFAULT_Y_TILE_HEIGHT_OFFSET, DESCRIPTION_FUNCTION_ID, TRAITS, false, null,
                        false, null, new String[]{invalidActiveAbilityId},
                        new String[]{REACTIVE_ABILITY_ID}, new String[]{PASSIVE_ABILITY_ID})));
        assertThrows(IllegalArgumentException.class, () -> factory
                .make(new ItemTypeDefinition(ID, NAME, PLURAL_NAME, EQUIPMENT_TYPE_ID,
                        IMAGE_ASSET_SET_ID, DEFAULT_X_TILE_WIDTH_OFFSET,
                        DEFAULT_Y_TILE_HEIGHT_OFFSET, DESCRIPTION_FUNCTION_ID, TRAITS, false, null,
                        false, null, new String[]{ACTIVE_ABILITY_ID}, new String[]{null},
                        new String[]{PASSIVE_ABILITY_ID})));
        assertThrows(IllegalArgumentException.class, () -> factory
                .make(new ItemTypeDefinition(ID, NAME, PLURAL_NAME, EQUIPMENT_TYPE_ID,
                        IMAGE_ASSET_SET_ID, DEFAULT_X_TILE_WIDTH_OFFSET,
                        DEFAULT_Y_TILE_HEIGHT_OFFSET, DESCRIPTION_FUNCTION_ID, TRAITS, false, null,
                        false, null, new String[]{ACTIVE_ABILITY_ID}, new String[]{""},
                        new String[]{PASSIVE_ABILITY_ID})));
        assertThrows(IllegalArgumentException.class, () -> factory
                .make(new ItemTypeDefinition(ID, NAME, PLURAL_NAME, EQUIPMENT_TYPE_ID,
                        IMAGE_ASSET_SET_ID, DEFAULT_X_TILE_WIDTH_OFFSET,
                        DEFAULT_Y_TILE_HEIGHT_OFFSET, DESCRIPTION_FUNCTION_ID, TRAITS, false, null,
                        false, null, new String[]{ACTIVE_ABILITY_ID},
                        new String[]{invalidReactiveAbilityId}, new String[]{PASSIVE_ABILITY_ID})));
        assertThrows(IllegalArgumentException.class, () -> factory
                .make(new ItemTypeDefinition(ID, NAME, PLURAL_NAME, EQUIPMENT_TYPE_ID,
                        IMAGE_ASSET_SET_ID, DEFAULT_X_TILE_WIDTH_OFFSET,
                        DEFAULT_Y_TILE_HEIGHT_OFFSET, DESCRIPTION_FUNCTION_ID, TRAITS, false, null,
                        false, null, new String[]{ACTIVE_ABILITY_ID},
                        new String[]{REACTIVE_ABILITY_ID}, new String[]{null})));
        assertThrows(IllegalArgumentException.class, () -> factory
                .make(new ItemTypeDefinition(ID, NAME, PLURAL_NAME, EQUIPMENT_TYPE_ID,
                        IMAGE_ASSET_SET_ID, DEFAULT_X_TILE_WIDTH_OFFSET,
                        DEFAULT_Y_TILE_HEIGHT_OFFSET, DESCRIPTION_FUNCTION_ID, TRAITS, false, null,
                        false, null, new String[]{ACTIVE_ABILITY_ID},
                        new String[]{REACTIVE_ABILITY_ID}, new String[]{""})));
        assertThrows(IllegalArgumentException.class, () -> factory
                .make(new ItemTypeDefinition(ID, NAME, PLURAL_NAME, EQUIPMENT_TYPE_ID,
                        IMAGE_ASSET_SET_ID, DEFAULT_X_TILE_WIDTH_OFFSET,
                        DEFAULT_Y_TILE_HEIGHT_OFFSET, DESCRIPTION_FUNCTION_ID, TRAITS, false, null,
                        false, null, new String[]{ACTIVE_ABILITY_ID},
                        new String[]{REACTIVE_ABILITY_ID}, new String[]{invalidPassiveAbilityId})));
    }

    @Test
    void testGetInterfaceName() {
        assertEquals(Factory.class.getCanonicalName() + "<" +
                ItemTypeDefinition.class.getCanonicalName() + "," +
                ItemType.class.getCanonicalName() + ">", factory.getInterfaceName());
    }
}
