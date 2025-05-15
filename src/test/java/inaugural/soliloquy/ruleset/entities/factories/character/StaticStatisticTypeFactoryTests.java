package inaugural.soliloquy.ruleset.entities.factories.character;

import inaugural.soliloquy.ruleset.definitions.StaticStatisticTypeDefinition;
import inaugural.soliloquy.ruleset.definitions.EffectsOnCharacterDefinition;
import inaugural.soliloquy.ruleset.definitions.RoundEndEffectsOnCharacterDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import soliloquy.specs.common.persistence.TypeHandler;
import soliloquy.specs.graphics.assets.ImageAssetSet;
import soliloquy.specs.graphics.renderables.colorshifting.ColorShift;
import soliloquy.specs.graphics.renderables.providers.ProviderAtTime;
import soliloquy.specs.ruleset.entities.actonroundendandcharacterturn.EffectsCharacterOnRoundOrTurnChange.EffectsOnCharacter;
import soliloquy.specs.ruleset.entities.actonroundendandcharacterturn.EffectsCharacterOnRoundOrTurnChange.RoundEndEffectsOnCharacter;
import soliloquy.specs.ruleset.entities.character.StaticStatisticType;

import java.util.function.Function;

import static inaugural.soliloquy.tools.collections.Collections.listOf;
import static inaugural.soliloquy.tools.random.Random.randomString;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StaticStatisticTypeFactoryTests {
    private final String ID = randomString();
    private final String NAME = randomString();
    private final String DESCRIPTION = randomString();
    private final String IMAGE_ASSET_SET_ID = randomString();
    private final String WRITTEN_COLOR_SHIFT_PROVIDER = randomString();

    @Mock private ImageAssetSet mockImageAssetSet;
    @Mock private Function<String, ImageAssetSet> mockGetImageAssetSet;
    @Mock private ProviderAtTime<ColorShift> mockColorShiftProvider;
    @Mock private TypeHandler<ProviderAtTime<ColorShift>> mockColorShiftProviderHandler;

    @Mock private RoundEndEffectsOnCharacterDefinition mockRoundEndEffectDefinition;
    @Mock private EffectsOnCharacterDefinition mockTurnStartEffectDefinition;
    @Mock private EffectsOnCharacterDefinition mockTurnEndEffectDefinition;
    @Mock private RoundEndEffectsOnCharacter mockRoundEndEffect;
    @Mock private EffectsOnCharacter mockTurnStartEffect;
    @Mock private EffectsOnCharacter mockTurnEndEffect;
    @Mock private Function<EffectsOnCharacterDefinition, EffectsOnCharacter>
            mockEffectsOnCharacterFactory;
    @Mock private Function<RoundEndEffectsOnCharacterDefinition, RoundEndEffectsOnCharacter>
            mockRoundEndEffectsOnCharacterFactory;
    private StaticStatisticTypeDefinition definition;

    private Function<StaticStatisticTypeDefinition, StaticStatisticType> factory;

    @BeforeEach
    public void setUp() {
        lenient().when(mockGetImageAssetSet.apply(anyString())).thenReturn(mockImageAssetSet);

        lenient().when(mockColorShiftProviderHandler.read(anyString())).thenReturn(mockColorShiftProvider);

        lenient().when(mockEffectsOnCharacterFactory.apply(mockTurnStartEffectDefinition))
                .thenReturn(mockTurnStartEffect);
        lenient().when(mockEffectsOnCharacterFactory.apply(mockTurnEndEffectDefinition))
                .thenReturn(mockTurnEndEffect);

        mockRoundEndEffectDefinition = mock(RoundEndEffectsOnCharacterDefinition.class);
        mockRoundEndEffect = mock(RoundEndEffectsOnCharacter.class);

        lenient().when(mockRoundEndEffectsOnCharacterFactory.apply(mockRoundEndEffectDefinition))
                .thenReturn(mockRoundEndEffect);

        definition = new StaticStatisticTypeDefinition(ID, NAME, DESCRIPTION,
                IMAGE_ASSET_SET_ID, new String[]{WRITTEN_COLOR_SHIFT_PROVIDER},
                mockRoundEndEffectDefinition, mockTurnStartEffectDefinition,
                mockTurnEndEffectDefinition);

        factory = new StaticStatisticTypeFactory(mockColorShiftProviderHandler,
                mockGetImageAssetSet, mockEffectsOnCharacterFactory,
                mockRoundEndEffectsOnCharacterFactory);
    }

    @Test
    public void testConstructorWithInvalidArgs() {
        assertThrows(IllegalArgumentException.class,
                () -> new StaticStatisticTypeFactory(null, mockGetImageAssetSet,
                        mockEffectsOnCharacterFactory, mockRoundEndEffectsOnCharacterFactory));
        assertThrows(IllegalArgumentException.class,
                () -> new StaticStatisticTypeFactory(mockColorShiftProviderHandler, null,
                        mockEffectsOnCharacterFactory, mockRoundEndEffectsOnCharacterFactory));
        assertThrows(IllegalArgumentException.class,
                () -> new StaticStatisticTypeFactory(mockColorShiftProviderHandler,
                        mockGetImageAssetSet, null, mockRoundEndEffectsOnCharacterFactory));
        assertThrows(IllegalArgumentException.class,
                () -> new StaticStatisticTypeFactory(mockColorShiftProviderHandler,
                        mockGetImageAssetSet, mockEffectsOnCharacterFactory, null));
    }

    @Test
    public void testMake() {
        var output = factory.apply(definition);

        assertNotNull(output);
        assertEquals(ID, output.id());
        assertEquals(NAME, output.getName());
        assertEquals(DESCRIPTION, output.getDescription());
        assertSame(mockImageAssetSet, output.imageAssetSet());
        assertEquals(listOf(mockColorShiftProvider), output.colorShiftProviders());
        verify(mockGetImageAssetSet).apply(IMAGE_ASSET_SET_ID);
        verify(mockColorShiftProviderHandler).read(WRITTEN_COLOR_SHIFT_PROVIDER);
        assertSame(mockRoundEndEffect, output.onRoundEnd());
        verify(mockRoundEndEffectsOnCharacterFactory).apply(mockRoundEndEffectDefinition);
        assertSame(mockTurnStartEffect, output.onTurnStart());
        verify(mockEffectsOnCharacterFactory).apply(mockTurnStartEffectDefinition);
        assertSame(mockTurnEndEffect, output.onTurnEnd());
        verify(mockEffectsOnCharacterFactory).apply(mockTurnEndEffectDefinition);
    }

    @Test
    public void testMakeWithInvalidArgs() {
        var invalidImageAssetSetId = randomString();
        when(mockGetImageAssetSet.apply(invalidImageAssetSetId)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> factory.apply(null));
        assertThrows(IllegalArgumentException.class, () -> factory
                .apply(new StaticStatisticTypeDefinition(null, NAME, DESCRIPTION, IMAGE_ASSET_SET_ID,
                        new String[]{WRITTEN_COLOR_SHIFT_PROVIDER}, mockRoundEndEffectDefinition,
                        mockTurnStartEffectDefinition, mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> factory
                .apply(new StaticStatisticTypeDefinition("", NAME, DESCRIPTION, IMAGE_ASSET_SET_ID,
                        new String[]{WRITTEN_COLOR_SHIFT_PROVIDER}, mockRoundEndEffectDefinition,
                        mockTurnStartEffectDefinition, mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> factory
                .apply(new StaticStatisticTypeDefinition(ID, null, DESCRIPTION, IMAGE_ASSET_SET_ID,
                        new String[]{WRITTEN_COLOR_SHIFT_PROVIDER}, mockRoundEndEffectDefinition,
                        mockTurnStartEffectDefinition, mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> factory
                .apply(new StaticStatisticTypeDefinition(ID, "", DESCRIPTION, IMAGE_ASSET_SET_ID,
                        new String[]{WRITTEN_COLOR_SHIFT_PROVIDER}, mockRoundEndEffectDefinition,
                        mockTurnStartEffectDefinition, mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> factory
                .apply(new StaticStatisticTypeDefinition(ID, NAME, DESCRIPTION, null,
                        new String[]{WRITTEN_COLOR_SHIFT_PROVIDER}, mockRoundEndEffectDefinition,
                        mockTurnStartEffectDefinition, mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> factory
                .apply(new StaticStatisticTypeDefinition(ID, NAME, DESCRIPTION, "",
                        new String[]{WRITTEN_COLOR_SHIFT_PROVIDER}, mockRoundEndEffectDefinition,
                        mockTurnStartEffectDefinition, mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> factory
                .apply(new StaticStatisticTypeDefinition(ID, NAME, DESCRIPTION,
                        invalidImageAssetSetId, new String[]{WRITTEN_COLOR_SHIFT_PROVIDER},
                        mockRoundEndEffectDefinition, mockTurnStartEffectDefinition,
                        mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> factory
                .apply(new StaticStatisticTypeDefinition(ID, NAME, DESCRIPTION, IMAGE_ASSET_SET_ID,
                        new String[]{WRITTEN_COLOR_SHIFT_PROVIDER}, null,
                        mockTurnStartEffectDefinition, mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> factory
                .apply(new StaticStatisticTypeDefinition(ID, NAME, DESCRIPTION, IMAGE_ASSET_SET_ID,
                        new String[]{WRITTEN_COLOR_SHIFT_PROVIDER}, mockRoundEndEffectDefinition,
                        null, mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> factory
                .apply(new StaticStatisticTypeDefinition(ID, NAME, DESCRIPTION, IMAGE_ASSET_SET_ID,
                        new String[]{WRITTEN_COLOR_SHIFT_PROVIDER}, mockRoundEndEffectDefinition,
                        mockTurnStartEffectDefinition, null)));
    }

    @Test
    public void testSetName() {
        var output = factory.apply(definition);
        var newName = randomString();

        output.setName(newName);

        assertEquals(newName, output.getName());
    }

    @Test
    public void testSetNameWithInvalidArgs() {
        var output = factory.apply(definition);

        assertThrows(IllegalArgumentException.class, () -> output.setName(null));
        assertThrows(IllegalArgumentException.class, () -> output.setName(""));
    }

    @Test
    public void testSetDescription() {
        var output = factory.apply(definition);
        var newDescription = randomString();

        output.setDescription(newDescription);

        assertEquals(newDescription, output.getDescription());
    }
}
