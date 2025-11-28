package tfar.craftingstation.blockentity;

import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import org.jetbrains.annotations.Nullable;
import tfar.craftingstation.init.ModBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import tfar.craftingstation.menu.CraftingStationMenu;
import tfar.craftingstation.platform.Services;

import java.util.Optional;


public class CraftingStationBlockEntity extends BlockEntity implements MenuProvider {

    public SimpleContainer input;

    public ResultContainer output;

    private Component customName;
    protected Direction currentContainer = Direction.DOWN;

    private final RecipeManager.CachedCheck<CraftingInput, ? extends CraftingRecipe> quickCheck;


    public CraftingStationBlockEntity(BlockPos pPos, BlockState pState) {
        super(ModBlockEntityTypes.crafting_station, pPos, pState);
        this.input = new SimpleContainer(9) {
            @Override
            public void setChanged() {
                super.setChanged();
                CraftingStationBlockEntity.this.setChanged();
            }

            @Override
            public void fromTag(ListTag pContainerNbt,HolderLookup.Provider pRegistries) {
                items.clear();
                for(int i = 0; i < pContainerNbt.size(); ++i) {
                    ItemStack itemstack = ItemStack.parseOptional(pRegistries, pContainerNbt.getCompound(i));
                    this.items.set(i, itemstack);
                }
            }

            @Override
            public ListTag createTag(HolderLookup.Provider pLevelRegistry) {
                ListTag listtag = new ListTag();

                for(int i = 0; i < this.getContainerSize(); ++i) {
                    ItemStack itemstack = this.getItem(i);
                    listtag.add(itemstack.saveOptional(pLevelRegistry));
                }
                return listtag;
            }
        };
        output = new ResultContainer();
        this.quickCheck = RecipeManager.createCheck(RecipeType.CRAFTING);
    }

    public void setCurrentContainer(Direction currentContainer) {
        this.currentContainer = currentContainer;
        setChanged();
    }

    public Direction getCurrentContainer() {
        return currentContainer;
    }

    @Override
    public void saveAdditional(CompoundTag tag,HolderLookup.Provider pRegistries) {
        ListTag compound = this.input.createTag(pRegistries);
        tag.put("inv", compound);
        if (this.customName != null) {
            tag.putString("CustomName", Component.Serializer.toJson(this.customName,pRegistries));
        }
        tag.putInt("dir",currentContainer.ordinal());
    }

    @Override
    public void loadAdditional(CompoundTag tag,HolderLookup.Provider pRegistries) {
        ListTag invTag = tag.getList("inv",Tag.TAG_COMPOUND);
        input.fromTag(invTag,pRegistries);
        if (tag.contains("CustomName", Tag.TAG_STRING)) {
            this.customName = Component.Serializer.fromJson(tag.getString("CustomName"),pRegistries);
        }
        currentContainer = Direction.values()[tag.getInt("dir")];
        super.loadAdditional(tag,pRegistries);
    }

    @Override
    public Component getDisplayName() {
        return getCustomName() != null ? getCustomName() : Component.translatable("title.crafting_station");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new CraftingStationMenu(id, playerInventory, input,output,worldPosition);
    }

    public void setCustomName(Component pName) {
        this.customName = pName;
    }

    //borrowed from TiC to fix a dupe bug
    public ItemStack calcResult(@Nullable Player player) {
        if (this.level == null || input.isEmpty()) {
            return ItemStack.EMPTY;
        }
        // assume empty unless we learn otherwise
        ItemStack result = ItemStack.EMPTY;
        if (!this.level.isClientSide && this.level.getServer() != null) {
            RecipeManager manager = this.level.getServer().getRecipeManager();

            CraftingInput craftingInventory = CraftingInput.of(3,3,input.items);

            // first, try the cached recipe
            Services.PLATFORM.forgeHooks$setCraftingPlayer(player);
            Optional<? extends RecipeHolder<? extends CraftingRecipe>> recipe = quickCheck.getRecipeFor(craftingInventory,level);
            // if it does not match, find a new recipe
            // note we intentionally have no player access during matches, that could lead to an unstable recipe
            if (recipe.isEmpty()) {
                recipe = manager.getRecipeFor(RecipeType.CRAFTING, craftingInventory, this.level);
            }

            // if we have a recipe, fetch its result
            if (recipe.isPresent()) {
                result = recipe.get().value().assemble(craftingInventory, level.registryAccess());

                // sync if the recipe is different
                /*if (recipe != lastRecipe) {
                    this.lastRecipe = recipe;
                    this.syncToRelevantPlayers(this::syncRecipe);
                }*/
            }
            Services.PLATFORM.forgeHooks$setCraftingPlayer(null);
        }
        /*else if (this.lastRecipe != null && this.lastRecipe.matches(this.craftingInventory, this.level)) {
            Services.PLATFORM.forgeHooks$setCraftingPlayer(player);
            result = this.lastRecipe.assemble(this.craftingInventory, level.registryAccess());
            Services.PLATFORM.forgeHooks$setCraftingPlayer(null);
        }*/
        return result;
    }

    public Component getCustomName() {
        return this.customName;
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        output.setItem(0,ItemStack.EMPTY);
    }

    @Override
    public void setChanged() {
        super.setChanged();
        output.setItem(0,calcResult(null));
        level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider pRegistries) {
        return saveWithoutMetadata(pRegistries);    // okay to send entire inventory on chunk load
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}

