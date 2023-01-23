package inaugural.soliloquy.ruleset.entities.factories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import soliloquy.specs.common.factories.Factory;
import soliloquy.specs.common.persistence.TypeHandler;
import soliloquy.specs.common.valueobjects.Pair;
import soliloquy.specs.gamestate.entities.Character;
import soliloquy.specs.graphics.assets.ImageAsset;
import soliloquy.specs.graphics.assets.ImageAssetSet;
import soliloquy.specs.graphics.renderables.colorshifting.ColorShift;
import soliloquy.specs.graphics.renderables.providers.ProviderAtTime;
import soliloquy.specs.ruleset.definitions.CharacterVariableStatisticTypeDefinition;
import soliloquy.specs.ruleset.definitions.EffectsOnCharacterDefinition;
import soliloquy.specs.ruleset.entities.actonroundendandcharacterturn.EffectsCharacterOnRoundOrTurnChange.EffectsOnCharacter;
import soliloquy.specs.ruleset.entities.character.CharacterVariableStatisticType;

import java.util.ArrayList;
import java.util.function.Function;

import static inaugural.soliloquy.tools.random.Random.randomString;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class CharacterVariableStatisticTypeFactoryTests {
    private final String ID = randomString();
    private final String NAME = randomString();
    private final String PLURAL_NAME = randomString();
    private final String DESCRIPTION = randomString();
    private final String IMAGE_ASSET_SET_ID = randomString();
    private final String WRITTEN_COLOR_SHIFT_PROVIDER = randomString();
    private final String ICON_FOR_CHARACTER_FUNCTION_ID = randomString();

    @Mock private ImageAssetSet mockImageAssetSet;
    @Mock private Function<String, ImageAssetSet> mockGetImageAssetSet;
    @Mock private ProviderAtTime<ColorShift> mockColorShiftProvider;
    @Mock private TypeHandler<ProviderAtTime<ColorShift>> mockColorShiftProviderHandler;
    @Mock private Pair<ImageAsset, Integer> mockIconForCharacter;
    @Mock private Function<Pair<String, Character>, Pair<ImageAsset, Integer>>
            mockIconForCharacterFunction;
    /** @noinspection rawtypes */
    @Mock private java.util.function.Function<String, Function> mockGetFunction;

    @Mock private EffectsOnCharacterDefinition mockRoundEndEffectDefinition;
    @Mock private EffectsOnCharacterDefinition mockTurnStartEffectDefinition;
    @Mock private EffectsOnCharacterDefinition mockTurnEndEffectDefinition;
    @Mock private EffectsOnCharacter mockRoundEndEffect;
    @Mock private EffectsOnCharacter mockTurnStartEffect;
    @Mock private EffectsOnCharacter mockTurnEndEffect;
    @Mock private Factory<EffectsOnCharacterDefinition, EffectsOnCharacter>
            mockEffectsOnCharacterFactory;

    private CharacterVariableStatisticTypeDefinition definition;

    private Factory<CharacterVariableStatisticTypeDefinition, CharacterVariableStatisticType>
            factory;

    @BeforeEach
    void setUp() {
        mockImageAssetSet = mock(ImageAssetSet.class);

        //noinspection unchecked
        mockGetImageAssetSet = (Function<String, ImageAssetSet>) mock(Function.class);
        when(mockGetImageAssetSet.apply(anyString())).thenReturn(mockImageAssetSet);

        //noinspection unchecked
        mockColorShiftProvider = (ProviderAtTime<ColorShift>) mock(ProviderAtTime.class);

        //noinspection unchecked
        mockColorShiftProviderHandler =
                (TypeHandler<ProviderAtTime<ColorShift>>) mock(TypeHandler.class);
        when(mockColorShiftProviderHandler.read(anyString())).thenReturn(mockColorShiftProvider);

        //noinspection unchecked
        mockIconForCharacter = (Pair<ImageAsset, Integer>) mock(Pair.class);

        //noinspection unchecked
        mockIconForCharacterFunction =
                (Function<Pair<String, Character>, Pair<ImageAsset, Integer>>) mock(Function.class);
        when(mockIconForCharacterFunction.apply(any())).thenReturn(mockIconForCharacter);

        //noinspection unchecked,rawtypes
        mockGetFunction = (java.util.function.Function<String, Function>) mock(
                java.util.function.Function.class);
        when(mockGetFunction.apply(anyString()))
                .thenReturn(mockIconForCharacterFunction);

        mockRoundEndEffectDefinition = mock(EffectsOnCharacterDefinition.class);
        mockTurnStartEffectDefinition = mock(EffectsOnCharacterDefinition.class);
        mockTurnEndEffectDefinition = mock(EffectsOnCharacterDefinition.class);

        mockRoundEndEffect = mock(EffectsOnCharacter.class);
        mockTurnStartEffect = mock(EffectsOnCharacter.class);
        mockTurnEndEffect = mock(EffectsOnCharacter.class);

        //noinspection unchecked
        mockEffectsOnCharacterFactory =
                (Factory<EffectsOnCharacterDefinition, EffectsOnCharacter>) mock(Factory.class);
        when(mockEffectsOnCharacterFactory.make(mockRoundEndEffectDefinition))
                .thenReturn(mockRoundEndEffect);
        when(mockEffectsOnCharacterFactory.make(mockTurnStartEffectDefinition))
                .thenReturn(mockTurnStartEffect);
        when(mockEffectsOnCharacterFactory.make(mockTurnEndEffectDefinition))
                .thenReturn(mockTurnEndEffect);

        definition =
                new CharacterVariableStatisticTypeDefinition(ID, NAME, PLURAL_NAME, DESCRIPTION,
                        IMAGE_ASSET_SET_ID, new String[]{WRITTEN_COLOR_SHIFT_PROVIDER},
                        ICON_FOR_CHARACTER_FUNCTION_ID, mockRoundEndEffectDefinition,
                        mockTurnStartEffectDefinition, mockTurnEndEffectDefinition);

        factory = new CharacterVariableStatisticTypeFactory(mockColorShiftProviderHandler,
                mockGetImageAssetSet, mockGetFunction, mockEffectsOnCharacterFactory);
    }

    @Test
    void testConstructorWithInvalidParams() {
        assertThrows(IllegalArgumentException.class,
                () -> new CharacterVariableStatisticTypeFactory(null, mockGetImageAssetSet,
                        mockGetFunction, mockEffectsOnCharacterFactory));
        assertThrows(IllegalArgumentException.class,
                () -> new CharacterVariableStatisticTypeFactory(mockColorShiftProviderHandler, null,
                        mockGetFunction, mockEffectsOnCharacterFactory));
        assertThrows(IllegalArgumentException.class,
                () -> new CharacterVariableStatisticTypeFactory(mockColorShiftProviderHandler,
                        mockGetImageAssetSet, null, mockEffectsOnCharacterFactory));
        assertThrows(IllegalArgumentException.class,
                () -> new CharacterVariableStatisticTypeFactory(mockColorShiftProviderHandler,
                        mockGetImageAssetSet, mockGetFunction, null));
    }

    @Test
    void testMake() {
        var output = factory.make(definition);

        assertNotNull(output);
        assertEquals(ID, output.id());
        assertEquals(NAME, output.getName());
        assertEquals(PLURAL_NAME, output.getPluralName());
        assertEquals(DESCRIPTION, output.getDescription());
        assertSame(mockImageAssetSet, output.imageAssetSet());
        assertEquals(new ArrayList<>() {{
            add(mockColorShiftProvider);
        }}, output.colorShiftProviders());
        assertEquals(CharacterVariableStatisticType.class.getCanonicalName(),
                output.getInterfaceName());
        verify(mockGetImageAssetSet, times(1)).apply(IMAGE_ASSET_SET_ID);
        verify(mockColorShiftProviderHandler, times(1)).read(WRITTEN_COLOR_SHIFT_PROVIDER);
        verify(mockGetFunction, times(1)).apply(ICON_FOR_CHARACTER_FUNCTION_ID);
        assertSame(mockRoundEndEffect, output.onRoundEnd());
        verify(mockEffectsOnCharacterFactory, times(1))
                .make(mockRoundEndEffectDefinition);
        assertSame(mockTurnStartEffect, output.onTurnStart());
        verify(mockEffectsOnCharacterFactory, times(1))
                .make(mockTurnStartEffectDefinition);
        assertSame(mockTurnEndEffect, output.onTurnEnd());
        verify(mockEffectsOnCharacterFactory, times(1))
                .make(mockTurnEndEffectDefinition);
    }

    @Test
    void testMakeWithInvalidParams() {
        var invalidImageAssetSetId = randomString();
        when(mockGetImageAssetSet.apply(invalidImageAssetSetId)).thenReturn(null);
        var invalidIconForCharacterFunctionId = randomString();
        when(mockGetFunction.apply(invalidIconForCharacterFunctionId))
                .thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> factory.make(null));
        assertThrows(IllegalArgumentException.class, () -> factory
                .make(new CharacterVariableStatisticTypeDefinition(null, NAME, PLURAL_NAME,
                        DESCRIPTION, IMAGE_ASSET_SET_ID, new String[]{WRITTEN_COLOR_SHIFT_PROVIDER},
                        ICON_FOR_CHARACTER_FUNCTION_ID, mockRoundEndEffectDefinition,
                        mockTurnStartEffectDefinition, mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> factory
                .make(new CharacterVariableStatisticTypeDefinition("", NAME, PLURAL_NAME,
                        DESCRIPTION, IMAGE_ASSET_SET_ID, new String[]{WRITTEN_COLOR_SHIFT_PROVIDER},
                        ICON_FOR_CHARACTER_FUNCTION_ID, mockRoundEndEffectDefinition,
                        mockTurnStartEffectDefinition, mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> factory
                .make(new CharacterVariableStatisticTypeDefinition(ID, null, PLURAL_NAME,
                        DESCRIPTION, IMAGE_ASSET_SET_ID, new String[]{WRITTEN_COLOR_SHIFT_PROVIDER},
                        ICON_FOR_CHARACTER_FUNCTION_ID, mockRoundEndEffectDefinition,
                        mockTurnStartEffectDefinition, mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> factory
                .make(new CharacterVariableStatisticTypeDefinition(ID, "", PLURAL_NAME, DESCRIPTION,
                        IMAGE_ASSET_SET_ID, new String[]{WRITTEN_COLOR_SHIFT_PROVIDER},
                        ICON_FOR_CHARACTER_FUNCTION_ID, mockRoundEndEffectDefinition,
                        mockTurnStartEffectDefinition, mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> factory
                .make(new CharacterVariableStatisticTypeDefinition(ID, NAME, null, DESCRIPTION,
                        IMAGE_ASSET_SET_ID, new String[]{WRITTEN_COLOR_SHIFT_PROVIDER},
                        ICON_FOR_CHARACTER_FUNCTION_ID, mockRoundEndEffectDefinition,
                        mockTurnStartEffectDefinition, mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> factory
                .make(new CharacterVariableStatisticTypeDefinition(ID, NAME, "", DESCRIPTION,
                        IMAGE_ASSET_SET_ID, new String[]{WRITTEN_COLOR_SHIFT_PROVIDER},
                        ICON_FOR_CHARACTER_FUNCTION_ID, mockRoundEndEffectDefinition,
                        mockTurnStartEffectDefinition, mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> factory
                .make(new CharacterVariableStatisticTypeDefinition(ID, NAME, PLURAL_NAME,
                        DESCRIPTION, null, new String[]{WRITTEN_COLOR_SHIFT_PROVIDER},
                        ICON_FOR_CHARACTER_FUNCTION_ID, mockRoundEndEffectDefinition,
                        mockTurnStartEffectDefinition, mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> factory
                .make(new CharacterVariableStatisticTypeDefinition(ID, NAME, PLURAL_NAME,
                        DESCRIPTION, "", new String[]{WRITTEN_COLOR_SHIFT_PROVIDER},
                        ICON_FOR_CHARACTER_FUNCTION_ID, mockRoundEndEffectDefinition,
                        mockTurnStartEffectDefinition, mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> factory
                .make(new CharacterVariableStatisticTypeDefinition(ID, NAME, PLURAL_NAME,
                        DESCRIPTION, invalidImageAssetSetId,
                        new String[]{WRITTEN_COLOR_SHIFT_PROVIDER},
                        ICON_FOR_CHARACTER_FUNCTION_ID, mockRoundEndEffectDefinition,
                        mockTurnStartEffectDefinition, mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> factory
                .make(new CharacterVariableStatisticTypeDefinition(ID, NAME, PLURAL_NAME,
                        DESCRIPTION, IMAGE_ASSET_SET_ID, new String[]{WRITTEN_COLOR_SHIFT_PROVIDER},
                        null, mockRoundEndEffectDefinition,
                        mockTurnStartEffectDefinition, mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> factory
                .make(new CharacterVariableStatisticTypeDefinition(ID, NAME, PLURAL_NAME,
                        DESCRIPTION, IMAGE_ASSET_SET_ID, new String[]{WRITTEN_COLOR_SHIFT_PROVIDER},
                        "", mockRoundEndEffectDefinition,
                        mockTurnStartEffectDefinition, mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> factory
                .make(new CharacterVariableStatisticTypeDefinition(ID, NAME, PLURAL_NAME,
                        DESCRIPTION, IMAGE_ASSET_SET_ID, new String[]{WRITTEN_COLOR_SHIFT_PROVIDER},
                        invalidIconForCharacterFunctionId, mockRoundEndEffectDefinition,
                        mockTurnStartEffectDefinition, mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> factory
                .make(new CharacterVariableStatisticTypeDefinition(ID, NAME, PLURAL_NAME,
                        DESCRIPTION, IMAGE_ASSET_SET_ID, new String[]{WRITTEN_COLOR_SHIFT_PROVIDER},
                        ICON_FOR_CHARACTER_FUNCTION_ID, null,
                        mockTurnStartEffectDefinition, mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> factory
                .make(new CharacterVariableStatisticTypeDefinition(ID, NAME, PLURAL_NAME,
                        DESCRIPTION, IMAGE_ASSET_SET_ID, new String[]{WRITTEN_COLOR_SHIFT_PROVIDER},
                        ICON_FOR_CHARACTER_FUNCTION_ID, mockRoundEndEffectDefinition,
                        null, mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> factory
                .make(new CharacterVariableStatisticTypeDefinition(ID, NAME, PLURAL_NAME,
                        DESCRIPTION, IMAGE_ASSET_SET_ID, new String[]{WRITTEN_COLOR_SHIFT_PROVIDER},
                        ICON_FOR_CHARACTER_FUNCTION_ID, mockRoundEndEffectDefinition,
                        mockTurnStartEffectDefinition, null)));
    }

    @Test
    void testSetName() {
        var output = factory.make(definition);
        var newName = randomString();

        output.setName(newName);

        assertEquals(newName, output.getName());
    }

    @Test
    void testSetNameWithInvalidParams() {
        var output = factory.make(definition);

        assertThrows(IllegalArgumentException.class, () -> output.setName(null));
        assertThrows(IllegalArgumentException.class, () -> output.setName(""));
    }

    @Test
    void testSetPluralName() {
        var output = factory.make(definition);
        var newPluralName = randomString();

        output.setPluralName(newPluralName);

        assertEquals(newPluralName, output.getPluralName());
    }

    @Test
    void testSetPluralNameWithInvalidParams() {
        var output = factory.make(definition);

        assertThrows(IllegalArgumentException.class, () -> output.setPluralName(null));
        assertThrows(IllegalArgumentException.class, () -> output.setPluralName(""));
    }

    @Test
    void testSetDescription() {
        var output = factory.make(definition);
        var newDescription = randomString();

        output.setDescription(newDescription);

        assertEquals(newDescription, output.getDescription());
    }

    @Test
    void testGetIcon() {
        var iconType = randomString();
        var character = mock(Character.class);
        var output = factory.make(definition);

        Pair<ImageAsset, Integer> iconForCharacter = output.getIcon(iconType, character);

        assertSame(mockIconForCharacter, iconForCharacter);
        verify(mockIconForCharacterFunction, times(1)).apply(Pair.of(iconType, character));
    }

    @Test
    void testGetInterfaceName() {
        assertEquals(Factory.class.getCanonicalName() + "<" +
                        CharacterVariableStatisticTypeDefinition.class.getCanonicalName() + "," +
                        CharacterVariableStatisticType.class.getCanonicalName() + ">",
                factory.getInterfaceName());
    }
}
