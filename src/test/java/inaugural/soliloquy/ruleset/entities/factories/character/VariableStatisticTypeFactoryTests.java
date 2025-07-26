package inaugural.soliloquy.ruleset.entities.factories.character;

import inaugural.soliloquy.ruleset.definitions.VariableStatisticTypeDefinition;
import inaugural.soliloquy.ruleset.definitions.EffectsOnCharacterDefinition;
import inaugural.soliloquy.ruleset.definitions.RoundEndEffectsOnCharacterDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import soliloquy.specs.common.entities.Action;
import soliloquy.specs.common.persistence.TypeHandler;
import soliloquy.specs.common.valueobjects.Pair;
import soliloquy.specs.gamestate.entities.Character;
import soliloquy.specs.io.graphics.assets.ImageAsset;
import soliloquy.specs.io.graphics.assets.ImageAssetSet;
import soliloquy.specs.io.graphics.renderables.colorshifting.ColorShift;
import soliloquy.specs.ruleset.entities.actonroundendandcharacterturn.EffectsCharacterOnRoundOrTurnChange.EffectsOnCharacter;
import soliloquy.specs.ruleset.entities.actonroundendandcharacterturn.EffectsCharacterOnRoundOrTurnChange.RoundEndEffectsOnCharacter;
import soliloquy.specs.ruleset.entities.character.VariableStatisticType;

import java.util.function.Function;

import static inaugural.soliloquy.tools.collections.Collections.arrayOf;
import static inaugural.soliloquy.tools.collections.Collections.listOf;
import static inaugural.soliloquy.tools.random.Random.randomInt;
import static inaugural.soliloquy.tools.random.Random.randomString;
import static inaugural.soliloquy.tools.testing.Assertions.once;
import static inaugural.soliloquy.tools.testing.Mock.LookupAndEntitiesWithId;
import static inaugural.soliloquy.tools.testing.Mock.generateMockLookupFunctionWithId;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static soliloquy.specs.common.valueobjects.Pair.pairOf;

@ExtendWith(MockitoExtension.class)
public class VariableStatisticTypeFactoryTests {
    private final String ID = randomString();
    private final String NAME = randomString();
    private final String PLURAL_NAME = randomString();
    private final String DESCRIPTION = randomString();
    private final String IMAGE_ASSET_SET_ID = randomString();
    private final String WRITTEN_COLOR_SHIFT_PROVIDER = randomString();
    private final String ICON_FOR_CHARACTER_FUNCTION_ID = randomString();

    private final String ALTER_ACTION_ID = "alterActionId";//randomString();
    @SuppressWarnings("rawtypes") private final LookupAndEntitiesWithId<Action> MOCK_ACTION_AND_LOOKUP = generateMockLookupFunctionWithId(Action.class, ALTER_ACTION_ID);
    @SuppressWarnings("rawtypes") private final Action MOCK_ALTER_ACTION = MOCK_ACTION_AND_LOOKUP.entities.getFirst();
    @SuppressWarnings("rawtypes")
    private final Function<String, Action> MOCK_GET_ACTION = MOCK_ACTION_AND_LOOKUP.lookup;

    @Mock private ImageAssetSet mockImageAssetSet;
    @Mock private Function<String, ImageAssetSet> mockGetImageAssetSet;
    @Mock private ColorShift mockShift;
    @Mock private TypeHandler<ColorShift> mockShiftHandler;
    @Mock private Pair<ImageAsset, Integer> mockIconForCharacter;
    @Mock private Function<Pair<String, Character>, Pair<ImageAsset, Integer>>
            mockIconForCharacterFunction;
    /** @noinspection rawtypes */
    @Mock private Function<String, Function> mockGetFunction;


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

    private VariableStatisticTypeDefinition definition;

    private Function<VariableStatisticTypeDefinition, VariableStatisticType>
            factory;

    @BeforeEach
    public void setUp() {
        lenient().when(mockGetImageAssetSet.apply(anyString())).thenReturn(mockImageAssetSet);

        lenient().when(mockShiftHandler.read(anyString())).thenReturn(mockShift);

        lenient().when(mockIconForCharacterFunction.apply(any())).thenReturn(mockIconForCharacter);

        lenient().when(mockGetFunction.apply(anyString()))
                .thenReturn(mockIconForCharacterFunction);

        lenient().when(mockEffectsOnCharacterFactory.apply(mockTurnStartEffectDefinition))
                .thenReturn(mockTurnStartEffect);
        lenient().when(mockEffectsOnCharacterFactory.apply(mockTurnEndEffectDefinition))
                .thenReturn(mockTurnEndEffect);

        lenient().when(mockRoundEndEffectsOnCharacterFactory.apply(mockRoundEndEffectDefinition))
                .thenReturn(mockRoundEndEffect);

        definition =
                new VariableStatisticTypeDefinition(ID, NAME, PLURAL_NAME, DESCRIPTION,
                        IMAGE_ASSET_SET_ID, arrayOf(WRITTEN_COLOR_SHIFT_PROVIDER),
                        ICON_FOR_CHARACTER_FUNCTION_ID, ALTER_ACTION_ID,
                        mockRoundEndEffectDefinition, mockTurnStartEffectDefinition,
                        mockTurnEndEffectDefinition);

        factory = new VariableStatisticTypeFactory(mockShiftHandler,
                mockGetImageAssetSet, mockGetFunction, MOCK_GET_ACTION,
                mockEffectsOnCharacterFactory, mockRoundEndEffectsOnCharacterFactory);
    }

    @Test
    public void testConstructorWithInvalidArgs() {
        assertThrows(IllegalArgumentException.class,
                () -> new VariableStatisticTypeFactory(null, mockGetImageAssetSet,
                        mockGetFunction, MOCK_GET_ACTION, mockEffectsOnCharacterFactory,
                        mockRoundEndEffectsOnCharacterFactory));
        assertThrows(IllegalArgumentException.class,
                () -> new VariableStatisticTypeFactory(mockShiftHandler, null,
                        mockGetFunction, MOCK_GET_ACTION, mockEffectsOnCharacterFactory,
                        mockRoundEndEffectsOnCharacterFactory));
        assertThrows(IllegalArgumentException.class,
                () -> new VariableStatisticTypeFactory(mockShiftHandler,
                        mockGetImageAssetSet, null, MOCK_GET_ACTION, mockEffectsOnCharacterFactory,
                        mockRoundEndEffectsOnCharacterFactory));
        assertThrows(IllegalArgumentException.class,
                () -> new VariableStatisticTypeFactory(mockShiftHandler,
                        mockGetImageAssetSet, mockGetFunction, null,
                        mockEffectsOnCharacterFactory, mockRoundEndEffectsOnCharacterFactory));
        assertThrows(IllegalArgumentException.class,
                () -> new VariableStatisticTypeFactory(mockShiftHandler,
                        mockGetImageAssetSet, mockGetFunction, MOCK_GET_ACTION, null,
                        mockRoundEndEffectsOnCharacterFactory));
    }

    @Test
    public void testMake() {
        var alterCaptor = ArgumentCaptor.forClass(Object[].class);
        //noinspection unchecked
        doNothing().when(MOCK_ALTER_ACTION).run(any(), alterCaptor.capture());

        var mockCharacter = mock(Character.class);
        var amount = randomInt();

        var output = factory.apply(definition);
        output.alter(mockCharacter, amount);

        assertNotNull(output);
        assertEquals(ID, output.id());
        assertEquals(NAME, output.getName());
        assertEquals(PLURAL_NAME, output.getPluralName());
        assertEquals(DESCRIPTION, output.getDescription());
        assertSame(mockImageAssetSet, output.imageAssetSet());
        assertEquals(listOf(mockShift), output.colorShifts());
        verify(mockGetImageAssetSet).apply(IMAGE_ASSET_SET_ID);
        verify(mockShiftHandler).read(WRITTEN_COLOR_SHIFT_PROVIDER);
        verify(mockGetFunction).apply(ICON_FOR_CHARACTER_FUNCTION_ID);
        verify(MOCK_GET_ACTION).apply(ALTER_ACTION_ID);
        //noinspection unchecked
        verify(MOCK_ALTER_ACTION, once()).run(alterCaptor.capture());
        assertArrayEquals(arrayOf(mockCharacter, amount, output), alterCaptor.getValue());
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
        var invalidFunctionId = randomString();
        when(mockGetFunction.apply(invalidFunctionId)).thenReturn(null);
        var invalidActionId = randomString();
        when(MOCK_GET_ACTION.apply(invalidActionId)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> factory.apply(null));
        assertThrows(IllegalArgumentException.class, () -> factory
                .apply(new VariableStatisticTypeDefinition(null, NAME, PLURAL_NAME, DESCRIPTION,
                        IMAGE_ASSET_SET_ID, arrayOf(WRITTEN_COLOR_SHIFT_PROVIDER),
                        ICON_FOR_CHARACTER_FUNCTION_ID, ALTER_ACTION_ID,
                        mockRoundEndEffectDefinition, mockTurnStartEffectDefinition,
                        mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> factory
                .apply(new VariableStatisticTypeDefinition("", NAME, PLURAL_NAME, DESCRIPTION,
                        IMAGE_ASSET_SET_ID, arrayOf(WRITTEN_COLOR_SHIFT_PROVIDER),
                        ICON_FOR_CHARACTER_FUNCTION_ID, ALTER_ACTION_ID,
                        mockRoundEndEffectDefinition, mockTurnStartEffectDefinition,
                        mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> factory
                .apply(new VariableStatisticTypeDefinition(ID, null, PLURAL_NAME, DESCRIPTION,
                        IMAGE_ASSET_SET_ID, arrayOf(WRITTEN_COLOR_SHIFT_PROVIDER),
                        ICON_FOR_CHARACTER_FUNCTION_ID, ALTER_ACTION_ID,
                        mockRoundEndEffectDefinition, mockTurnStartEffectDefinition,
                        mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> factory
                .apply(new VariableStatisticTypeDefinition(ID, "", PLURAL_NAME, DESCRIPTION,
                        IMAGE_ASSET_SET_ID, arrayOf(WRITTEN_COLOR_SHIFT_PROVIDER),
                        ICON_FOR_CHARACTER_FUNCTION_ID, ALTER_ACTION_ID,
                        mockRoundEndEffectDefinition, mockTurnStartEffectDefinition,
                        mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> factory
                .apply(new VariableStatisticTypeDefinition(ID, NAME, null, DESCRIPTION,
                        IMAGE_ASSET_SET_ID, arrayOf(WRITTEN_COLOR_SHIFT_PROVIDER),
                        ICON_FOR_CHARACTER_FUNCTION_ID, ALTER_ACTION_ID,
                        mockRoundEndEffectDefinition, mockTurnStartEffectDefinition,
                        mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> factory
                .apply(new VariableStatisticTypeDefinition(ID, NAME, "", DESCRIPTION,
                        IMAGE_ASSET_SET_ID, arrayOf(WRITTEN_COLOR_SHIFT_PROVIDER),
                        ICON_FOR_CHARACTER_FUNCTION_ID, ALTER_ACTION_ID,
                        mockRoundEndEffectDefinition, mockTurnStartEffectDefinition,
                        mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> factory
                .apply(new VariableStatisticTypeDefinition(ID, NAME, PLURAL_NAME, DESCRIPTION, null,
                        arrayOf(WRITTEN_COLOR_SHIFT_PROVIDER), ICON_FOR_CHARACTER_FUNCTION_ID,
                        ALTER_ACTION_ID, mockRoundEndEffectDefinition,
                        mockTurnStartEffectDefinition, mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> factory
                .apply(new VariableStatisticTypeDefinition(ID, NAME, PLURAL_NAME, DESCRIPTION, "",
                        arrayOf(WRITTEN_COLOR_SHIFT_PROVIDER), ICON_FOR_CHARACTER_FUNCTION_ID,
                        ALTER_ACTION_ID, mockRoundEndEffectDefinition,
                        mockTurnStartEffectDefinition, mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> factory
                .apply(new VariableStatisticTypeDefinition(ID, NAME, PLURAL_NAME, DESCRIPTION,
                        invalidImageAssetSetId, arrayOf(WRITTEN_COLOR_SHIFT_PROVIDER),
                        ICON_FOR_CHARACTER_FUNCTION_ID, ALTER_ACTION_ID,
                        mockRoundEndEffectDefinition, mockTurnStartEffectDefinition,
                        mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> factory
                .apply(new VariableStatisticTypeDefinition(ID, NAME, PLURAL_NAME, DESCRIPTION,
                        IMAGE_ASSET_SET_ID, arrayOf(WRITTEN_COLOR_SHIFT_PROVIDER), null,
                        ALTER_ACTION_ID, mockRoundEndEffectDefinition,
                        mockTurnStartEffectDefinition, mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> factory
                .apply(new VariableStatisticTypeDefinition(ID, NAME, PLURAL_NAME, DESCRIPTION,
                        IMAGE_ASSET_SET_ID, arrayOf(WRITTEN_COLOR_SHIFT_PROVIDER), "",
                        ALTER_ACTION_ID, mockRoundEndEffectDefinition,
                        mockTurnStartEffectDefinition, mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> factory
                .apply(new VariableStatisticTypeDefinition(ID, NAME, PLURAL_NAME, DESCRIPTION,
                        IMAGE_ASSET_SET_ID, arrayOf(WRITTEN_COLOR_SHIFT_PROVIDER),
                        invalidFunctionId, ALTER_ACTION_ID, mockRoundEndEffectDefinition,
                        mockTurnStartEffectDefinition, mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> factory
                .apply(new VariableStatisticTypeDefinition(ID, NAME, PLURAL_NAME, DESCRIPTION,
                        IMAGE_ASSET_SET_ID, arrayOf(WRITTEN_COLOR_SHIFT_PROVIDER),
                        ICON_FOR_CHARACTER_FUNCTION_ID, null, mockRoundEndEffectDefinition,
                        mockTurnStartEffectDefinition, mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> factory
                .apply(new VariableStatisticTypeDefinition(ID, NAME, PLURAL_NAME, DESCRIPTION,
                        IMAGE_ASSET_SET_ID, arrayOf(WRITTEN_COLOR_SHIFT_PROVIDER),
                        ICON_FOR_CHARACTER_FUNCTION_ID, "", mockRoundEndEffectDefinition,
                        mockTurnStartEffectDefinition, mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> factory
                .apply(new VariableStatisticTypeDefinition(ID, NAME, PLURAL_NAME, DESCRIPTION,
                        IMAGE_ASSET_SET_ID, arrayOf(WRITTEN_COLOR_SHIFT_PROVIDER),
                        ICON_FOR_CHARACTER_FUNCTION_ID, invalidActionId,
                        mockRoundEndEffectDefinition, mockTurnStartEffectDefinition,
                        mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> factory
                .apply(new VariableStatisticTypeDefinition(ID, NAME, PLURAL_NAME, DESCRIPTION,
                        IMAGE_ASSET_SET_ID, arrayOf(WRITTEN_COLOR_SHIFT_PROVIDER),
                        ICON_FOR_CHARACTER_FUNCTION_ID, ALTER_ACTION_ID, null,
                        mockTurnStartEffectDefinition, mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> factory
                .apply(new VariableStatisticTypeDefinition(ID, NAME, PLURAL_NAME, DESCRIPTION,
                        IMAGE_ASSET_SET_ID, arrayOf(WRITTEN_COLOR_SHIFT_PROVIDER),
                        ICON_FOR_CHARACTER_FUNCTION_ID, ALTER_ACTION_ID,
                        mockRoundEndEffectDefinition, null, mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> factory
                .apply(new VariableStatisticTypeDefinition(ID, NAME, PLURAL_NAME, DESCRIPTION,
                        IMAGE_ASSET_SET_ID, arrayOf(WRITTEN_COLOR_SHIFT_PROVIDER),
                        ICON_FOR_CHARACTER_FUNCTION_ID, ALTER_ACTION_ID,
                        mockRoundEndEffectDefinition, mockTurnStartEffectDefinition, null)));
    }

    @Test
    public void testAlterWithInvalidArgs() {
        var output = factory.apply(definition);

        assertThrows(IllegalArgumentException.class, () -> output.alter(null, randomInt()));
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
    public void testSetPluralName() {
        var output = factory.apply(definition);
        var newPluralName = randomString();

        output.setPluralName(newPluralName);

        assertEquals(newPluralName, output.getPluralName());
    }

    @Test
    public void testSetPluralNameWithInvalidArgs() {
        var output = factory.apply(definition);

        assertThrows(IllegalArgumentException.class, () -> output.setPluralName(null));
        assertThrows(IllegalArgumentException.class, () -> output.setPluralName(""));
    }

    @Test
    public void testSetDescription() {
        var output = factory.apply(definition);
        var newDescription = randomString();

        output.setDescription(newDescription);

        assertEquals(newDescription, output.getDescription());
    }

    @Test
    public void testGetIcon() {
        var iconType = randomString();
        var character = mock(Character.class);
        var output = factory.apply(definition);

        Pair<ImageAsset, Integer> iconForCharacter = output.getIcon(iconType, character);

        assertSame(mockIconForCharacter, iconForCharacter);
        verify(mockIconForCharacterFunction).apply(pairOf(iconType, character));
    }
}
