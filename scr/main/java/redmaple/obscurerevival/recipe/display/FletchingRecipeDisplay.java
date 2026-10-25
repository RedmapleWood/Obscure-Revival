package redmaple.obscurerevival.recipe.display;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.display.RecipeDisplay;
import net.minecraft.recipe.display.SlotDisplay;

public record FletchingRecipeDisplay(
        SlotDisplay blueprint,
        SlotDisplay base,
        SlotDisplay addition,
        SlotDisplay result,
        SlotDisplay craftingStation
) implements RecipeDisplay {

    public static final MapCodec<FletchingRecipeDisplay> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    SlotDisplay.CODEC.fieldOf("blueprint").forGetter(FletchingRecipeDisplay::blueprint),
                    SlotDisplay.CODEC.fieldOf("base").forGetter(FletchingRecipeDisplay::base),
                    SlotDisplay.CODEC.fieldOf("addition").forGetter(FletchingRecipeDisplay::addition),
                    SlotDisplay.CODEC.fieldOf("result").forGetter(FletchingRecipeDisplay::result),
                    SlotDisplay.CODEC.fieldOf("crafting_station").forGetter(FletchingRecipeDisplay::craftingStation)
            ).apply(instance, FletchingRecipeDisplay::new)
    );

    public static final PacketCodec<RegistryByteBuf, FletchingRecipeDisplay> PACKET_CODEC = PacketCodec.tuple(
            SlotDisplay.PACKET_CODEC, FletchingRecipeDisplay::blueprint,
            SlotDisplay.PACKET_CODEC, FletchingRecipeDisplay::base,
            SlotDisplay.PACKET_CODEC, FletchingRecipeDisplay::addition,
            SlotDisplay.PACKET_CODEC, FletchingRecipeDisplay::result,
            SlotDisplay.PACKET_CODEC, FletchingRecipeDisplay::craftingStation,
            FletchingRecipeDisplay::new
    );

    public static final RecipeDisplay.Serializer<FletchingRecipeDisplay> SERIALIZER =
            new RecipeDisplay.Serializer<>(CODEC, PACKET_CODEC);

    @Override
    public RecipeDisplay.Serializer<FletchingRecipeDisplay> serializer() {
        return SERIALIZER;
    }
}