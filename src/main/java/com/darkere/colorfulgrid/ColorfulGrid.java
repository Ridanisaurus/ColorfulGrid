package com.darkere.colorfulgrid;

import com.refinedmods.refinedstorage.api.network.grid.GridType;
import com.refinedmods.refinedstorage.block.BlockDirection;
import com.refinedmods.refinedstorage.block.GridBlock;
import com.refinedmods.refinedstorage.block.NetworkNodeBlock;
import com.refinedmods.refinedstorage.render.BakedModelOverrideRegistry;
import com.refinedmods.refinedstorage.render.model.FullbrightBakedModel;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.item.*;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(ColorfulGrid.MODID)
public class ColorfulGrid {
    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();
    public static final String MODID = "colorfulgrid";

    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
   // public static final RegistryObject<ColoredGridBlock> COLOREDGRID = BLOCKS.register("coloredgrid", () -> new ColoredGridBlock(GridType.NORMAL));
    public static final RegistryObject<ColoredGridBlock> COLOREDCRAFTINGGRID = BLOCKS.register("coloredgrid_crafting", () -> new ColoredGridBlock(GridType.CRAFTING));
  //  public static final RegistryObject<ColoredGridBlock> COLOREDPATTERNGRID = BLOCKS.register("coloredgrid_pattern", () -> new ColoredGridBlock(GridType.PATTERN));
  //  public static final RegistryObject<ColoredGridBlock> COLOREDFLUIDGRID = BLOCKS.register("coloredgrid_fluid", () -> new ColoredGridBlock(GridType.FLUID));

    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
 //   public static final RegistryObject<BlockItem> COLOREDGRIDITEM = ITEMS.register("coloredgrid", () -> new BlockItem(COLOREDGRID.get(), new Item.Properties()));
    public static final RegistryObject<BlockItem> COLOREDCRAFTINGGRIDITEM = ITEMS.register("coloredgrid_crafting", () -> new BlockItem(COLOREDCRAFTINGGRID.get(), new Item.Properties()));
  //  public static final RegistryObject<BlockItem> COLOREDPATTERNGRIDITEM = ITEMS.register("coloredgrid_pattern", () -> new BlockItem(COLOREDPATTERNGRID.get(), new Item.Properties()));
  //  public static final RegistryObject<BlockItem> COLOREDFLUIDGRIDITEM = ITEMS.register("coloredgrid_fluid", () -> new BlockItem(COLOREDFLUIDGRID.get(), new Item.Properties()));

    private final BakedModelOverrideRegistry bakedModelOverrideRegistry = new BakedModelOverrideRegistry();

    public ColorfulGrid() {
        BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        MinecraftForge.EVENT_BUS.register(this);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::datagen);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onModelBake);
        bakedModelOverrideRegistry.add(new ResourceLocation(MODID, "coloredgrid_crafting"), (base, registry) -> new FullbrightBakedModel(base, true,
            new ResourceLocation(MODID,"cutout/crafting/black"),
            new ResourceLocation(MODID,"cutout/crafting/blue"),
            new ResourceLocation(MODID,"cutout/crafting/brown"),
            new ResourceLocation(MODID,"cutout/crafting/cyan"),
            new ResourceLocation(MODID,"cutout/crafting/gray"),
            new ResourceLocation(MODID,"cutout/crafting/green"),
            new ResourceLocation(MODID,"cutout/crafting/light_blue"),
            new ResourceLocation(MODID,"cutout/crafting/light_gray"),
            new ResourceLocation(MODID,"cutout/crafting/lime"),
            new ResourceLocation(MODID,"cutout/crafting/magenta"),
            new ResourceLocation(MODID,"cutout/crafting/orange"),
            new ResourceLocation(MODID,"cutout/crafting/pink"),
            new ResourceLocation(MODID,"cutout/crafting/purple"),
            new ResourceLocation(MODID,"cutout/crafting/red"),
            new ResourceLocation(MODID,"cutout/crafting/white"),
            new ResourceLocation(MODID,"cutout/crafting/yellow")
        ));
    }

    public void datagen(GatherDataEvent event){
        event.getGenerator().addProvider(new BlockStateGenerator(event.getGenerator(),MODID, event.getExistingFileHelper()));
        event.getGenerator().addProvider(new ItemModelGenerator(event.getGenerator(),MODID, event.getExistingFileHelper()));
    }

    public void clientSetup(FMLClientSetupEvent event){
        RenderTypeLookup.setRenderLayer(COLOREDCRAFTINGGRID.get(), RenderType.getCutout());
    }

    public void onModelBake(ModelBakeEvent e) {
        for (ResourceLocation id : e.getModelRegistry().keySet()) {
            BakedModelOverrideRegistry.BakedModelOverrideFactory factory = this.bakedModelOverrideRegistry.get(new ResourceLocation(id.getNamespace(), id.getPath()));

            if (factory != null) {
                e.getModelRegistry().put(id, factory.create(e.getModelRegistry().get(id), e.getModelRegistry()));
            }
        }
    }

    @SubscribeEvent
    public void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        if (event.getWorld().isRemote) return;
        ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof DyeItem)) return;
        ServerWorld world = (ServerWorld) event.getWorld();
        BlockPos pos = event.getPos();
        BlockState state = world.getBlockState(pos);
        if (!(state.getBlock() instanceof GridBlock) && !(state.getBlock() instanceof ColoredGridBlock)) return;

        DyeColor color = DyeColor.getColor(stack);

        BlockState newState = null;
        if (state.getBlock() instanceof ColoredGridBlock) {
            newState = state;
            if (state.get(ColoredGridBlock.COLOR) == color) return;
        } else if (state.getBlock() instanceof GridBlock) {
            newState = transformToColoredGrid(state);
            world.destroyBlock(pos, false);
        }
        if (newState == null) return;

        world.setBlockState(pos, newState.with(ColoredGridBlock.COLOR, color));
        ForgeEventFactory.onBlockPlace(event.getEntity(), BlockSnapshot.create(world.func_234923_W_(), world, pos), event.getFace());
        event.setCanceled(true);
    }

    private BlockState transformToColoredGrid(BlockState state) {
        GridType type = getGridTypeViaReflection(state.getBlock());
        if (type == null) return null;
        return getStateForType(state, type);

    }

    private BlockState getStateForType(BlockState state, GridType type) {
        BlockState newState = null;
        switch (type) {
            case NORMAL:
 //               newState = COLOREDGRID.get().getDefaultState();
                break;
            case CRAFTING:
                newState = COLOREDCRAFTINGGRID.get().getDefaultState();
                break;
            case PATTERN:
    //            newState = COLOREDPATTERNGRID.get().getDefaultState();
                break;
            case FLUID:
//                newState = COLOREDFLUIDGRID.get().getDefaultState();
                break;
        }
        Boolean connected = state.get(NetworkNodeBlock.CONNECTED);
        Direction dir = state.get(BlockDirection.HORIZONTAL.getProperty());
        newState = newState.with(NetworkNodeBlock.CONNECTED, connected);
        return newState.with(BlockDirection.ANY.getProperty(), dir);
    }

    private GridType getGridTypeViaReflection(Block block) {
        Field type = null;
        try {
            type = GridBlock.class.getDeclaredField("type");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        if (type == null) return null;
        type.setAccessible(true);
        try {
            return (GridType) type.get(block);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
}
