package inaugural.soliloquy.ruleset.entities.factories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import soliloquy.specs.common.factories.Factory;
import soliloquy.specs.common.persistence.TypeHandler;
import soliloquy.specs.graphics.assets.ImageAssetSet;
import soliloquy.specs.graphics.renderables.colorshifting.ColorShift;
import soliloquy.specs.graphics.renderables.providers.ProviderAtTime;
import soliloquy.specs.ruleset.definitions.CharacterStaticStatisticTypeDefinition;
import soliloquy.specs.ruleset.entities.CharacterStaticStatisticType;

import java.util.ArrayList;
import java.util.function.Function;

import static inaugural.soliloquy.tools.random.Random.randomString;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class CharacterStaticStatisticTypeFactoryTests {
    private final String ID = randomString();
    private final String NAME = randomString();
    private final String DESCRIPTION = randomString();
    private final String IMAGE_ASSET_SET_ID = randomString();
    private final String WRITTEN_COLOR_SHIFT_PROVIDER = randomString();
    private final CharacterStaticStatisticTypeDefinition DEFINITION =
            new CharacterStaticStatisticTypeDefinition(ID, NAME, DESCRIPTION,
                    IMAGE_ASSET_SET_ID, new String[]{WRITTEN_COLOR_SHIFT_PROVIDER});

    @Mock private ImageAssetSet mockImageAssetSet;
    @Mock private Function<String, ImageAssetSet> mockGetImageAssetSet;
    @Mock private ProviderAtTime<ColorShift> mockColorShiftProvider;
    @Mock private TypeHandler<ProviderAtTime<ColorShift>> mockColorShiftProviderHandler;

    private Factory<CharacterStaticStatisticTypeDefinition, CharacterStaticStatisticType> factory;

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

        factory = new CharacterStaticStatisticTypeFactory(mockColorShiftProviderHandler,
                mockGetImageAssetSet);
    }

    @Test
    void testConstructorWithInvalidParams() {
        assertThrows(IllegalArgumentException.class,
                () -> new CharacterStaticStatisticTypeFactory(null, mockGetImageAssetSet));
        assertThrows(IllegalArgumentException.class,
                () -> new CharacterStaticStatisticTypeFactory(mockColorShiftProviderHandler, null));
    }

    @Test
    void testMake() {
        CharacterStaticStatisticType output = factory.make(DEFINITION);

        assertNotNull(output);
        assertEquals(ID, output.id());
        assertEquals(NAME, output.getName());
        assertEquals(DESCRIPTION, output.getDescription());
        assertSame(mockImageAssetSet, output.imageAssetSet());
        assertEquals(new ArrayList<>() {{
            add(mockColorShiftProvider);
        }}, output.colorShiftProviders());
        assertEquals(CharacterStaticStatisticType.class.getCanonicalName(),
                output.getInterfaceName());
        verify(mockGetImageAssetSet, times(1)).apply(IMAGE_ASSET_SET_ID);
        verify(mockColorShiftProviderHandler, times(1)).read(WRITTEN_COLOR_SHIFT_PROVIDER);
    }

    @Test
    void testMakeWithInvalidParams() {
        String invalidImageAssetSetId = randomString();
        when(mockGetImageAssetSet.apply(invalidImageAssetSetId)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> factory.make(null));
        assertThrows(IllegalArgumentException.class, () -> factory
                .make(new CharacterStaticStatisticTypeDefinition(null, NAME, DESCRIPTION,
                        IMAGE_ASSET_SET_ID, new String[]{WRITTEN_COLOR_SHIFT_PROVIDER})));
        assertThrows(IllegalArgumentException.class, () -> factory
                .make(new CharacterStaticStatisticTypeDefinition("", NAME, DESCRIPTION,
                        IMAGE_ASSET_SET_ID, new String[]{WRITTEN_COLOR_SHIFT_PROVIDER})));
        assertThrows(IllegalArgumentException.class, () -> factory
                .make(new CharacterStaticStatisticTypeDefinition(ID, null, DESCRIPTION,
                        IMAGE_ASSET_SET_ID, new String[]{WRITTEN_COLOR_SHIFT_PROVIDER})));
        assertThrows(IllegalArgumentException.class, () -> factory
                .make(new CharacterStaticStatisticTypeDefinition(ID, "", DESCRIPTION,
                        IMAGE_ASSET_SET_ID, new String[]{WRITTEN_COLOR_SHIFT_PROVIDER})));
        assertThrows(IllegalArgumentException.class, () -> factory
                .make(new CharacterStaticStatisticTypeDefinition(ID, NAME, DESCRIPTION, null,
                        new String[]{WRITTEN_COLOR_SHIFT_PROVIDER})));
        assertThrows(IllegalArgumentException.class, () -> factory
                .make(new CharacterStaticStatisticTypeDefinition(ID, NAME, DESCRIPTION, "",
                        new String[]{WRITTEN_COLOR_SHIFT_PROVIDER})));
        assertThrows(IllegalArgumentException.class, () -> factory
                .make(new CharacterStaticStatisticTypeDefinition(ID, NAME, DESCRIPTION,
                        invalidImageAssetSetId, new String[]{WRITTEN_COLOR_SHIFT_PROVIDER})));
    }

    @Test
    void testSetName() {
        CharacterStaticStatisticType output = factory.make(DEFINITION);
        String newName = randomString();

        output.setName(newName);

        assertEquals(newName, output.getName());
    }

    @Test
    void testSetNameWithInvalidParams() {
        CharacterStaticStatisticType output = factory.make(DEFINITION);

        assertThrows(IllegalArgumentException.class, () -> output.setName(null));
        assertThrows(IllegalArgumentException.class, () -> output.setName(""));
    }

    @Test
    void testSetDescription() {
        CharacterStaticStatisticType output = factory.make(DEFINITION);
        String newDescription = randomString();

        output.setDescription(newDescription);

        assertEquals(newDescription, output.getDescription());
    }

    @Test
    void testGetInterfaceName() {
        assertEquals(Factory.class.getCanonicalName() + "<" +
                        CharacterStaticStatisticTypeDefinition.class.getCanonicalName() + "," +
                        CharacterStaticStatisticType.class.getCanonicalName() + ">",
                factory.getInterfaceName());
    }
}
