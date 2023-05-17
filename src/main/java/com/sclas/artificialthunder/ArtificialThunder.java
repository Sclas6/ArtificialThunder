package com.sclas.artificialthunder;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RedstoneTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryObject;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(ArtificialThunder.MOD_ID)
public class ArtificialThunder
{
    public static final String MOD_ID="artificialthunder";

    public ArtificialThunder()
    {
        final IEventBus modEventBus=FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::setup);
        MinecraftForge.EVENT_BUS.register(this);

        BlockInit.BLOCKS.register(modEventBus);
    }
    @Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
    public static class BlockInit{
        private static final DeferredRegister<Block> BLOCKS=DeferredRegister.create(ForgeRegistries.BLOCKS,ArtificialThunder.MOD_ID);
        public static final RegistryObject<Block> SMILE_BLOCK=BLOCKS.register("thunder_gen",()->new SadBlock(Block.Properties
                .of(Material.STONE)
                .strength(4f,1200f)
                .requiresCorrectToolForDrops()
                .lightLevel((state)->15)));

        @SubscribeEvent
        public static void onRegisterItems(final RegistryEvent.Register<Item> event){
            final IForgeRegistry<Item> registry=event.getRegistry();
            BLOCKS.getEntries().stream().map(RegistryObject::get).forEach((block)->{
                final Item.Properties properties=new Item.Properties().tab(CreativeModeTab.TAB_REDSTONE);
                final BlockItem blockItem=new BlockItem(block,properties);
                blockItem.setRegistryName(block.getRegistryName());
                registry.register(blockItem);
            });
        }
    }
    public static class SadBlock extends Block{
        public static final BooleanProperty LIT = RedstoneTorchBlock.LIT;
        public boolean rSig = false;
        public SadBlock(Block.Properties properties){
            super(properties);
        }
        @Override
        public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
            ItemStack held = player.getItemInHand(hand);
            if (!world.isClientSide() && held.getItem() == Items.GUNPOWDER){
                //held.shrink(1);
                LightningBolt thunder=new LightningBolt(EntityType.LIGHTNING_BOLT,world);
                thunder.setPos(pos.getX(),pos.getY(),pos.getZ());
                world.addFreshEntity(thunder);

                return InteractionResult.CONSUME;
            }
            return super.use(state, world, pos, player, hand, hit);
        }
        @Override
        public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block,BlockPos fromPos, boolean isMoving){
            if(!world.isClientSide){
                if(world.hasNeighborSignal(pos)){
                    if(!rSig){
                        LightningBolt thunder=new LightningBolt(EntityType.LIGHTNING_BOLT,world);
                        thunder.setPos(pos.getX(),pos.getY(),pos.getZ());
                        world.addFreshEntity(thunder);
                    }
                    rSig=true;
                }else{
                    rSig=false;
                }
            }
        }

    }

    private void setup(final FMLCommonSetupEvent event)
    {
        // some preinit code

    }
}
