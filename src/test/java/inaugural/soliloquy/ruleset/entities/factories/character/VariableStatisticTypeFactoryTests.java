package inaugural.soliloquy.ruleset.entities.factories.character;

import inaugural.soliloquy.ruleset.definitions.VariableStatisticTypeDefinition;
import inaugural.soliloquy.ruleset.definitions.EffectsOnCharacterDefinition;
import inaugural.soliloquy.ruleset.definitions.RoundEndEffectsOnCharacterDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import soliloquy.specs.common.entities.Action;
import soliloquy.specs.common.factories.Factory;
import soliloquy.specs.common.persistence.TypeHandler;
import soliloquy.specs.common.valueobjects.Pair;
import soliloquy.specs.gamestate.entities.Character;
import soliloquy.specs.graphics.assets.ImageAsset;
import soliloquy.specs.graphics.assets.ImageAssetSet;
import soliloquy.specs.graphics.renderables.colorshifting.ColorShift;
import soliloquy.specs.graphics.renderables.providers.ProviderAtTime;
import soliloquy.specs.ruleset.entities.actonroundendandcharacterturn.EffectsCharacterOnRoundOrTurnChange.EffectsOnCharacter;
import soliloquy.specs.ruleset.entities.actonroundendandcharacterturn.EffectsCharacterOnRoundOrTurnChange.RoundEndEffectsOnCharacter;
import soliloquy.specs.ruleset.entities.character.VariableStatisticType;

import java.util.function.Function;

import static inaugural.soliloquy.tools.collections.Collections.arrayOf;
import static inaugural.soliloquy.tools.collections.Collections.listOf;
import static inaugural.soliloquy.tools.random.Random.randomInt;
import static inaugural.soliloquy.tools.random.Random.randomString;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class VariableStatisticTypeFactoryTests {
    private final String ID = randomString();
    private final String NAME = randomString();
    private final String PLURAL_NAME = randomString();
    private final String DESCRIPTION = randomString();
    private final String IMAGE_ASSET_SET_ID = randomString();
    private final String WRITTEN_COLOR_SHIFT_PROVIDER = randomString();
    private final String ICON_FOR_CHARACTER_FUNCTION_ID = randomString();
    private final String ALTER_ACTION_ID = randomString();

    @Mock private ImageAssetSet mockImageAssetSet;
    @Mock private Function<String, ImageAssetSet> mockGetImageAssetSet;
    @Mock private ProviderAtTime<ColorShift> mockColorShiftProvider;
    @Mock private TypeHandler<ProviderAtTime<ColorShift>> mockColorShiftProviderHandler;
    @Mock private Pair<ImageAsset, Integer> mockIconForCharacter;
    @Mock private Function<Pair<String, Character>, Pair<ImageAsset, Integer>>
            mockIconForCharacterFunction;
    /** @noinspection rawtypes */
    @Mock private Function<String, Function> mockGetFunction;

    @Mock private Action<Object[]> mockAlterAction;
    @SuppressWarnings("rawtypes")
    @Mock private Function<String, Action> mockGetAction;

    @Mock private RoundEndEffectsOnCharacterDefinition mockRoundEndEffectDefinition;
    @Mock private EffectsOnCharacterDefinition mockTurnStartEffectDefinition;
    @Mock private EffectsOnCharacterDefinition mockTurnEndEffectDefinition;
    @Mock private RoundEndEffectsOnCharacter mockRoundEndEffect;
    @Mock private EffectsOnCharacter mockTurnStartEffect;
    @Mock private EffectsOnCharacter mockTurnEndEffect;
    @Mock private Factory<EffectsOnCharacterDefinition, EffectsOnCharacter>
            mockEffectsOnCharacterFactory;
    @Mock private Factory<RoundEndEffectsOnCharacterDefinition, RoundEndEffectsOnCharacter>
            mockRoundEndEffectsOnCharacterFactory;

    private VariableStatisticTypeDefinition definition;

    private Factory<VariableStatisticTypeDefinition, VariableStatisticType>
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
        mockGetFunction = (Function<String, Function>) mock(
                Function.class);
        when(mockGetFunction.apply(anyString()))
                .thenReturn(mockIconForCharacterFunction);

        //noinspection unchecked
        mockAlterAction = (Action<Object[]>) mock(Action.class);

        //noinspection unchecked,rawtypes
        mockGetAction = (Function<String, Action>) mock(Function.class);
        when(mockGetAction.apply(anyString())).thenReturn(mockAlterAction);

        mockTurnStartEffectDefinition = mock(EffectsOnCharacterDefinition.class);
        mockTurnEndEffectDefinition = mock(EffectsOnCharacterDefinition.class);

        mockTurnStartEffect = mock(EffectsOnCharacter.class);
        mockTurnEndEffect = mock(EffectsOnCharacter.class);

        //noinspection unchecked
        mockEffectsOnCharacterFactory =
                (Factory<EffectsOnCharacterDefinition, EffectsOnCharacter>) mock(Factory.class);
        when(mockEffectsOnCharacterFactory.make(mockTurnStartEffectDefinition))
                .thenReturn(mockTurnStartEffect);
        when(mockEffectsOnCharacterFactory.make(mockTurnEndEffectDefinition))
                .thenReturn(mockTurnEndEffect);

        mockRoundEndEffectDefinition = mock(RoundEndEffectsOnCharacterDefinition.class);
        mockRoundEndEffect = mock(RoundEndEffectsOnCharacter.class);

        //noinspection unchecked
        mockRoundEndEffectsOnCharacterFactory =
                (Factory<RoundEndEffectsOnCharacterDefinition, RoundEndEffectsOnCharacter>) mock(
                        Factory.class);
        when(mockRoundEndEffectsOnCharacterFactory.make(mockRoundEndEffectDefinition))
                .thenReturn(mockRoundEndEffect);

        definition =
                new VariableStatisticTypeDefinition(ID, NAME, PLURAL_NAME, DESCRIPTION,
                        IMAGE_ASSET_SET_ID, arrayOf(WRITTEN_COLOR_SHIFT_PROVIDER),
                        ICON_FOR_CHARACTER_FUNCTION_ID, ALTER_ACTION_ID,
                        mockRoundEndEffectDefinition, mockTurnStartEffectDefinition,
                        mockTurnEndEffectDefinition);

        factory = new VariableStatisticTypeFactory(mockColorShiftProviderHandler,
                mockGetImageAssetSet, mockGetFunction, mockGetAction,
                mockEffectsOnCharacterFactory, mockRoundEndEffectsOnCharacterFactory);
    }

    @Test
    void testConstructorWithInvalidParams() {
        assertThrows(IllegalArgumentException.class,
                () -> new VariableStatisticTypeFactory(null, mockGetImageAssetSet,
                        mockGetFunction, mockGetAction, mockEffectsOnCharacterFactory,
                        mockRoundEndEffectsOnCharacterFactory));
        assertThrows(IllegalArgumentException.class,
                () -> new VariableStatisticTypeFactory(mockColorShiftProviderHandler, null,
                        mockGetFunction, mockGetAction, mockEffectsOnCharacterFactory,
                        mockRoundEndEffectsOnCharacterFactory));
        assertThrows(IllegalArgumentException.class,
                () -> new VariableStatisticTypeFactory(mockColorShiftProviderHandler,
                        mockGetImageAssetSet, null, mockGetAction, mockEffectsOnCharacterFactory,
                        mockRoundEndEffectsOnCharacterFactory));
        assertThrows(IllegalArgumentException.class,
                () -> new VariableStatisticTypeFactory(mockColorShiftProviderHandler,
                        mockGetImageAssetSet, mockGetFunction, null,
                        mockEffectsOnCharacterFactory, mockRoundEndEffectsOnCharacterFactory));
        assertThrows(IllegalArgumentException.class,
                () -> new VariableStatisticTypeFactory(mockColorShiftProviderHandler,
                        mockGetImageAssetSet, mockGetFunction, mockGetAction, null,
                        mockRoundEndEffectsOnCharacterFactory));
    }

    @Test
    void testMake() {
        var mockCharacter = mock(Character.class);
        var amount = randomInt();

        var output = factory.make(definition);
        output.alter(mockCharacter, amount);

        assertNotNull(output);
        assertEquals(ID, output.id());
        assertEquals(NAME, output.getName());
        assertEquals(PLURAL_NAME, output.getPluralName());
        assertEquals(DESCRIPTION, output.getDescription());
        assertSame(mockImageAssetSet, output.imageAssetSet());
        assertEquals(listOf(mockColorShiftProvider), output.colorShiftProviders());
        assertEquals(VariableStatisticType.class.getCanonicalName(),
                output.getInterfaceName());
        verify(mockGetImageAssetSet).apply(IMAGE_ASSET_SET_ID);
        verify(mockColorShiftProviderHandler).read(WRITTEN_COLOR_SHIFT_PROVIDER);
        verify(mockGetFunction).apply(ICON_FOR_CHARACTER_FUNCTION_ID);
        verify(mockGetAction).apply(ALTER_ACTION_ID);
        verify(mockAlterAction).run(eq(arrayOf(mockCharacter, amount)));
        assertSame(mockRoundEndEffect, output.onRoundEnd());
        verify(mockRoundEndEffectsOnCharacterFactory).make(mockRoundEndEffectDefinition);
        assertSame(mockTurnStartEffect, output.onTurnStart());
        verify(mockEffectsOnCharacterFactory).make(mockTurnStartEffectDefinition);
        assertSame(mockTurnEndEffect, output.onTurnEnd());
        verify(mockEffectsOnCharacterFactory).make(mockTurnEndEffectDefinition);
    }

    @Test
    void testMakeWithInvalidParams() {
        var invalidImageAssetSetId = randomString();
        when(mockGetImageAssetSet.apply(invalidImageAssetSetId)).thenReturn(null);
        var invalidFunctionId = randomString();
        when(mockGetFunction.apply(invalidFunctionId)).thenReturn(null);
        var invalidActionId = randomString();
        when(mockGetAction.apply(invalidActionId)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> factory.make(null));
        assertThrows(IllegalArgumentException.class, () -> factory
                .make(new VariableStatisticTypeDefinition(null, NAME, PLURAL_NAME, DESCRIPTION,
                        IMAGE_ASSET_SET_ID, arrayOf(WRITTEN_COLOR_SHIFT_PROVIDER),
                        ICON_FOR_CHARACTER_FUNCTION_ID, ALTER_ACTION_ID,
                        mockRoundEndEffectDefinition, mockTurnStartEffectDefinition,
                        mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> factory
                .make(new VariableStatisticTypeDefinition("", NAME, PLURAL_NAME, DESCRIPTION,
                        IMAGE_ASSET_SET_ID, arrayOf(WRITTEN_COLOR_SHIFT_PROVIDER),
                        ICON_FOR_CHARACTER_FUNCTION_ID, ALTER_ACTION_ID,
                        mockRoundEndEffectDefinition, mockTurnStartEffectDefinition,
                        mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> factory
                .make(new VariableStatisticTypeDefinition(ID, null, PLURAL_NAME, DESCRIPTION,
                        IMAGE_ASSET_SET_ID, arrayOf(WRITTEN_COLOR_SHIFT_PROVIDER),
                        ICON_FOR_CHARACTER_FUNCTION_ID, ALTER_ACTION_ID,
                        mockRoundEndEffectDefinition, mockTurnStartEffectDefinition,
                        mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> factory
                .make(new VariableStatisticTypeDefinition(ID, "", PLURAL_NAME, DESCRIPTION,
                        IMAGE_ASSET_SET_ID, arrayOf(WRITTEN_COLOR_SHIFT_PROVIDER),
                        ICON_FOR_CHARACTER_FUNCTION_ID, ALTER_ACTION_ID,
                        mockRoundEndEffectDefinition, mockTurnStartEffectDefinition,
                        mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> factory
                .make(new VariableStatisticTypeDefinition(ID, NAME, null, DESCRIPTION,
                        IMAGE_ASSET_SET_ID, arrayOf(WRITTEN_COLOR_SHIFT_PROVIDER),
                        ICON_FOR_CHARACTER_FUNCTION_ID, ALTER_ACTION_ID,
                        mockRoundEndEffectDefinition, mockTurnStartEffectDefinition,
                        mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> factory
                .make(new VariableStatisticTypeDefinition(ID, NAME, "", DESCRIPTION,
                        IMAGE_ASSET_SET_ID, arrayOf(WRITTEN_COLOR_SHIFT_PROVIDER),
                        ICON_FOR_CHARACTER_FUNCTION_ID, ALTER_ACTION_ID,
                        mockRoundEndEffectDefinition, mockTurnStartEffectDefinition,
                        mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> factory
                .make(new VariableStatisticTypeDefinition(ID, NAME, PLURAL_NAME, DESCRIPTION, null,
                        arrayOf(WRITTEN_COLOR_SHIFT_PROVIDER), ICON_FOR_CHARACTER_FUNCTION_ID,
                        ALTER_ACTION_ID, mockRoundEndEffectDefinition,
                        mockTurnStartEffectDefinition, mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> factory
                .make(new VariableStatisticTypeDefinition(ID, NAME, PLURAL_NAME, DESCRIPTION, "",
                        arrayOf(WRITTEN_COLOR_SHIFT_PROVIDER), ICON_FOR_CHARACTER_FUNCTION_ID,
                        ALTER_ACTION_ID, mockRoundEndEffectDefinition,
                        mockTurnStartEffectDefinition, mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> factory
                .make(new VariableStatisticTypeDefinition(ID, NAME, PLURAL_NAME, DESCRIPTION,
                        invalidImageAssetSetId, arrayOf(WRITTEN_COLOR_SHIFT_PROVIDER),
                        ICON_FOR_CHARACTER_FUNCTION_ID, ALTER_ACTION_ID,
                        mockRoundEndEffectDefinition, mockTurnStartEffectDefinition,
                        mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> factory
                .make(new VariableStatisticTypeDefinition(ID, NAME, PLURAL_NAME, DESCRIPTION,
                        IMAGE_ASSET_SET_ID, arrayOf(WRITTEN_COLOR_SHIFT_PROVIDER), null,
                        ALTER_ACTION_ID, mockRoundEndEffectDefinition,
                        mockTurnStartEffectDefinition, mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> factory
                .make(new VariableStatisticTypeDefinition(ID, NAME, PLURAL_NAME, DESCRIPTION,
                        IMAGE_ASSET_SET_ID, arrayOf(WRITTEN_COLOR_SHIFT_PROVIDER), "",
                        ALTER_ACTION_ID, mockRoundEndEffectDefinition,
                        mockTurnStartEffectDefinition, mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> factory
                .make(new VariableStatisticTypeDefinition(ID, NAME, PLURAL_NAME, DESCRIPTION,
                        IMAGE_ASSET_SET_ID, arrayOf(WRITTEN_COLOR_SHIFT_PROVIDER),
                        invalidFunctionId, ALTER_ACTION_ID, mockRoundEndEffectDefinition,
                        mockTurnStartEffectDefinition, mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> factory
                .make(new VariableStatisticTypeDefinition(ID, NAME, PLURAL_NAME, DESCRIPTION,
                        IMAGE_ASSET_SET_ID, arrayOf(WRITTEN_COLOR_SHIFT_PROVIDER),
                        ICON_FOR_CHARACTER_FUNCTION_ID, null, mockRoundEndEffectDefinition,
                        mockTurnStartEffectDefinition, mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> factory
                .make(new VariableStatisticTypeDefinition(ID, NAME, PLURAL_NAME, DESCRIPTION,
                        IMAGE_ASSET_SET_ID, arrayOf(WRITTEN_COLOR_SHIFT_PROVIDER),
                        ICON_FOR_CHARACTER_FUNCTION_ID, "", mockRoundEndEffectDefinition,
                        mockTurnStartEffectDefinition, mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> factory
                .make(new VariableStatisticTypeDefinition(ID, NAME, PLURAL_NAME, DESCRIPTION,
                        IMAGE_ASSET_SET_ID, arrayOf(WRITTEN_COLOR_SHIFT_PROVIDER),
                        ICON_FOR_CHARACTER_FUNCTION_ID, invalidActionId,
                        mockRoundEndEffectDefinition, mockTurnStartEffectDefinition,
                        mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> factory
                .make(new VariableStatisticTypeDefinition(ID, NAME, PLURAL_NAME, DESCRIPTION,
                        IMAGE_ASSET_SET_ID, arrayOf(WRITTEN_COLOR_SHIFT_PROVIDER),
                        ICON_FOR_CHARACTER_FUNCTION_ID, ALTER_ACTION_ID, null,
                        mockTurnStartEffectDefinition, mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> factory
                .make(new VariableStatisticTypeDefinition(ID, NAME, PLURAL_NAME, DESCRIPTION,
                        IMAGE_ASSET_SET_ID, arrayOf(WRITTEN_COLOR_SHIFT_PROVIDER),
                        ICON_FOR_CHARACTER_FUNCTION_ID, ALTER_ACTION_ID,
                        mockRoundEndEffectDefinition, null, mockTurnEndEffectDefinition)));
        assertThrows(IllegalArgumentException.class, () -> factory
                .make(new VariableStatisticTypeDefinition(ID, NAME, PLURAL_NAME, DESCRIPTION,
                        IMAGE_ASSET_SET_ID, arrayOf(WRITTEN_COLOR_SHIFT_PROVIDER),
                        ICON_FOR_CHARACTER_FUNCTION_ID, ALTER_ACTION_ID,
                        mockRoundEndEffectDefinition, mockTurnStartEffectDefinition, null)));
    }

    @Test
    void testAlterWithInvalidParams() {
        var output = factory.make(definition);

        assertThrows(IllegalArgumentException.class, () -> output.alter(null, randomInt()));
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
        verify(mockIconForCharacterFunction).apply(Pair.of(iconType, character));
    }

    @Test
    void testGetInterfaceName() {
        assertEquals(Factory.class.getCanonicalName() + "<" +
                        VariableStatisticTypeDefinition.class.getCanonicalName() + "," +
                        VariableStatisticType.class.getCanonicalName() + ">",
                factory.getInterfaceName());
    }
}
