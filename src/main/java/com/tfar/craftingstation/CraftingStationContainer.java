package com.tfar.craftingstation;

import com.tfar.craftingstation.network.PacketHandler;
import com.tfar.craftingstation.network.S2CLastRecipePacket;
import com.tfar.craftingstation.slot.SlotFastCraft;
import com.tfar.craftingstation.slot.WrapperSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.CraftResultInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.SlotItemHandler;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CraftingStationContainer extends Container {
    private static final Method GET_TILE_ENTITY_METHOD;

    static {
        Method doubleSlabsGetTileEntity1 = null;
        if (ModList.get().isLoaded("doubleslabs")) {
            try {
                Class<?> doubleSlabsFlags = Class.forName("cjminecraft.doubleslabs.api.Flags");
                doubleSlabsGetTileEntity1 = doubleSlabsFlags.getDeclaredMethod("getTileEntityAtPos", BlockPos.class, IBlockReader.class);
            } catch (ClassNotFoundException | NoSuchMethodException e) {
                e.printStackTrace();
                doubleSlabsGetTileEntity1 = null;
            }
        }
        GET_TILE_ENTITY_METHOD = doubleSlabsGetTileEntity1;
    }

    public final CraftingInventoryPersistant craftMatrix;
    public final CraftResultInventory craftResult = new CraftResultInventory();
    public final World world;
    public final CraftingStationBlockEntity tileEntity;
    public final List<Pair<Integer, Integer>> containerStarts = new ArrayList<>();
    public final List<ItemStack> blocks = new ArrayList<>();

    public final List<ITextComponent> containerNames = new ArrayList<>();
    private final PlayerEntity player;
    public IRecipe<CraftingInventory> lastRecipe;
    public int subContainerSize = 0;
    public boolean hasSideContainers;
    public int currentContainer;
    protected IRecipe<CraftingInventory> lastLastRecipe;


    private static TileEntity getTileEntityAtPos(BlockPos pos, World world) {
        try {
            return GET_TILE_ENTITY_METHOD != null ? (TileEntity) GET_TILE_ENTITY_METHOD.invoke(null, pos, world) : world.getTileEntity(pos);
        } catch (IllegalAccessException | InvocationTargetException ignored) {
            return world.getTileEntity(pos);
        }
    }


    public CraftingStationContainer(int id, PlayerInventory inv, World world, BlockPos pos) {
        super(CraftingStation.Objects.crafting_station_container, id);

        this.world = world;
        this.player = inv.player;
        this.tileEntity = (CraftingStationBlockEntity) getTileEntityAtPos(pos, world);
        currentContainer = tileEntity.currentContainer;
        this.craftMatrix = new CraftingInventoryPersistant(this, tileEntity.input);
        this.hasSideContainers = false;

        addOwnSlots();

        // detect te
        Direction accessDir = null;
        List<TileEntity> tileEntities = new ArrayList<>();
        for (Direction dir : Direction.values()) {
            BlockPos neighbor = pos.offset(dir);

            TileEntity te = world.getTileEntity(neighbor);
            if (te != null && !(te instanceof CraftingStationBlockEntity)) {
                // if blacklisted, skip checks entirely
                if (CraftingStation.blacklisted.contains(te.getType()))
                    continue;
                if (te instanceof IInventory && !((IInventory) te).isUsableByPlayer(player)) {
                    continue;
                }

                // try internal access first
                if (te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).filter(IItemHandlerModifiable.class::isInstance).isPresent()) {
                    tileEntities.add(te);
                    blocks.add(new ItemStack(world.getBlockState(neighbor).getBlock()));
                }
                // try sided access else
                //      if(te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir.getOpposite())) {
                //        if(te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir.getOpposite()) instanceof IItemHandlerModifiable) {
                //          inventoryTE = te;
                //         accessDir = dir.getOpposite();
                //         break;
                //       }
                //   }
            }
        }

        if (!tileEntities.isEmpty()) {
            for (TileEntity tileEntity : tileEntities) {
                tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(this::accept);
            }
        }

        if (!tileEntities.isEmpty()) {
            addSideContainerSlots(tileEntities, accessDir, -125, 17);
        }
        addPlayerSlots(inv);
        onCraftMatrixChanged(craftMatrix);
        if (hasSideContainers) changeContainer(currentContainer);
    }

    public static IRecipe<CraftingInventory> findRecipe(CraftingInventory inv, World world, PlayerEntity player) {
        return world.getRecipeManager().getRecipes(IRecipeType.CRAFTING).values().stream().flatMap(recipe -> {
            try {
                return Util.streamOptional(IRecipeType.CRAFTING.matches(recipe, world, inv));
            } catch (Exception e) {
                CraftingStation.LOGGER.error("Bad recipe found: " + recipe.getId().toString());
                CraftingStation.LOGGER.error(e.getMessage());
                player.sendMessage(new TranslationTextComponent("text.crafting_station.error", recipe.getId().toString()).mergeStyle(TextFormatting.DARK_RED),Util.DUMMY_UUID);
                return null;
            }
        }).findFirst().orElse(null);
    }

    private void addOwnSlots() {
        // crafting result
        this.addSlot(new SlotFastCraft(this, this.craftMatrix, craftResult, 0, 124, 35, player));

        // crafting grid
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                addSlot(new Slot(craftMatrix, x + 3 * y, 30 + 18 * x, 17 + 18 * y));
            }
        }
    }

    private void addSideContainerSlots(List<TileEntity> tes, Direction dir, int xPos, int yPos) {
        for (int i = 0; i < tes.size(); i++) {
            TileEntity te = tes.get(i);
            containerNames.add(te instanceof INamedContainerProvider ? ((INamedContainerProvider) te).getDisplayName() : new TranslationTextComponent(te.getBlockState().getBlock().getTranslationKey()));
            final int number = i;
            te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(h -> {
                int size = h.getSlots();
                this.subContainerSize += size;
                int offsetx = needsScroll() ? 0 : 8;
                for (int y = 0; y < (int) Math.ceil(size / 6d); y++)
                    for (int x = 0; x < 6; x++) {
                        int index = 6 * y + x;
                        if (index >= size) continue;
                        boolean hidden = y >= 9 || number != 0;
                        WrapperSlot wrapper = new WrapperSlot(new SlotItemHandler(h, index, 18 * x + xPos + offsetx, 18 * y + yPos));
                        if (hidden) hideSlot(wrapper);
                        addSlot(wrapper);
                    }
            });
        }
        hasSideContainers = true;
    }

    public void hideSlot(Slot slot) {
        slot.yPos = Integer.MAX_VALUE;
    }

    @Override
    public void onContainerClosed(PlayerEntity player) {
        tileEntity.currentContainer = currentContainer;
        super.onContainerClosed(player);
    }

    private void addPlayerSlots(PlayerInventory playerInventory) {
        // inventory
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 9; x++) {
                addSlot(new Slot(playerInventory, 9 + x + 9 * y, 8 + 18 * x, 84 + 18 * y));
            }
        }

        // hotbar
        for (int x = 0; x < 9; x++) {
            addSlot(new Slot(playerInventory, x, 8 + 18 * x, 142));
        }
    }

    // update crafting
    //clientside only
    @Override
    public void setAll(List<ItemStack> p_190896_1_) {
        craftMatrix.setDoNotCallUpdates(true);
        super.setAll(p_190896_1_);
        craftMatrix.setDoNotCallUpdates(false);
        craftMatrix.onCraftMatrixChanged();
    }

    @Override
    public void onCraftMatrixChanged(IInventory inventory) {
        this.slotChangedCraftingGrid(world, player, craftMatrix, craftResult);
    }

    @Override
    public boolean canInteractWith(PlayerEntity player) {
        return true;
    }

    @Nonnull
    @Override
    public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
        Slot slot = this.inventorySlots.get(index);

        if (slot == null || !slot.getHasStack()) {
            return ItemStack.EMPTY;
        }

        ItemStack ret = slot.getStack().copy();
        ItemStack stack = slot.getStack().copy();

        boolean nothingDone = true;

        //is this the crafting slot?
        if (index == 0) {

            if (hasSideContainers)
                nothingDone = refillTileInventory(stack);
            // Try moving module -> player inventory
            nothingDone &= moveToPlayerInventory(stack);

            // Try moving module -> tile inventory
            if (hasSideContainers)
                nothingDone &= mergeItemStackMove(stack, 10, 10 + subContainerSize, false);
        }

        // Is the slot an input slot??
        else if (index < 10) {
            if (hasSideContainers)
                nothingDone = refillTileInventory(stack);
            // Try moving module -> player inventory
            nothingDone &= moveToPlayerInventory(stack);

            // Try moving module -> tile inventory
            if (hasSideContainers)
                nothingDone &= mergeItemStackMove(stack, 10, 10 + subContainerSize, false);
        }
        // Is the slot from the tile?
        else if (index < 10 + subContainerSize && hasSideContainers) {
            // Try moving tile -> preferred modules
            nothingDone = moveToCraftingStation(stack);

            // Try moving module -> player inventory
            nothingDone &= moveToPlayerInventory(stack);
        }
        // Slot is from the player inventory
        else if (index >= 10 + subContainerSize) {
            // try moving player -> modules
            nothingDone = moveToCraftingStation(stack);

            // Try moving player -> tile inventory
            if (hasSideContainers)
                nothingDone &= moveToTileInventory(stack);
        }
        // you violated some assumption or something. Shame on you.
        else {
            return ItemStack.EMPTY;
        }

        if (nothingDone) {
            return ItemStack.EMPTY;
        }
        return notifySlotAfterTransfer(playerIn, stack, ret, slot);
    }

    protected void slotChangedCraftingGrid(World world, PlayerEntity player, CraftingInventory inv, CraftResultInventory result) {
        ItemStack itemstack = ItemStack.EMPTY;

        // if the recipe is no longer valid, update it
        if (lastRecipe == null || !lastRecipe.matches(inv, world)) {
            lastRecipe = findRecipe(inv, world, player);
        }

        // if we have a recipe, fetch its result
        if (lastRecipe != null) {
            itemstack = lastRecipe.getCraftingResult(inv);
        }
        // set the slot on both sides, client is for display/so the client knows about the recipe
        result.setInventorySlotContents(0, itemstack);

        // update recipe on server
        if (!world.isRemote) {
            ServerPlayerEntity entityplayermp = (ServerPlayerEntity) player;

            // we need to sync to all players currently in the inventory
            List<ServerPlayerEntity> relevantPlayers = getAllPlayersWithThisContainerOpen(this, entityplayermp.getServerWorld());

            // sync result to all serverside inventories to prevent duplications/recipes being blocked
            // need to do this every time as otherwise taking items of the result causes desync
            syncResultToAllOpenWindows(itemstack, relevantPlayers);

            // if the recipe changed, update clients last recipe
            // this also updates the client side display when the recipe is added
            if (lastLastRecipe != lastRecipe) {
                syncRecipeToAllOpenWindows(lastRecipe, relevantPlayers);
                lastLastRecipe = lastRecipe;
            }
        }
    }

    private void syncResultToAllOpenWindows(final ItemStack stack, List<ServerPlayerEntity> players) {
        players.forEach(otherPlayer -> {
            otherPlayer.openContainer.putStackInSlot(0, stack);
            //otherPlayer.connection.sendPacket(new SPacketSetSlot(otherPlayer.openContainer.windowId, SLOT_RESULT, stack));
        });
    }

    private void syncRecipeToAllOpenWindows(final IRecipe lastRecipe, List<ServerPlayerEntity> players) {
        players.forEach(otherPlayer -> {
            // safe cast since hasSameContainerOpen does class checks
            ((CraftingStationContainer) otherPlayer.openContainer).lastRecipe = lastRecipe;
            PacketHandler.INSTANCE.sendTo(new S2CLastRecipePacket(lastRecipe), otherPlayer.connection.getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT);
        });
    }

    private List<ServerPlayerEntity> getAllPlayersWithThisContainerOpen(CraftingStationContainer container, ServerWorld server) {
        return server.getPlayers().stream()
                .filter(player -> hasSameContainerOpen(container, player))
                .collect(Collectors.toList());
    }

    private boolean hasSameContainerOpen(CraftingStationContainer container, PlayerEntity playerToCheck) {
        return playerToCheck instanceof ServerPlayerEntity &&
                playerToCheck.openContainer.getClass().isAssignableFrom(container.getClass()) &&
                this.sameGui((CraftingStationContainer) playerToCheck.openContainer);
    }

    public boolean sameGui(CraftingStationContainer otherContainer) {
        return this.tileEntity == otherContainer.tileEntity;
    }

    @Nonnull
    protected ItemStack notifySlotAfterTransfer(PlayerEntity player, @Nonnull ItemStack stack, @Nonnull ItemStack original, Slot slot) {
        // notify slot
        slot.onSlotChange(stack, original);

        if (stack.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }

        // update slot we pulled from
        slot.putStack(stack);
        slot.onTake(player, stack);

        if (slot.getHasStack() && slot.getStack().isEmpty()) {
            slot.putStack(ItemStack.EMPTY);
        }

        return original;
    }

    protected boolean moveToTileInventory(@Nonnull ItemStack itemstack) {
        return !this.mergeItemStack(itemstack, 10, 10 + subContainerSize, false);
    }

    protected boolean moveToPlayerInventory(@Nonnull ItemStack itemstack) {
        return !this.mergeItemStack(itemstack, 10 + subContainerSize, this.inventorySlots.size(), true);
    }

    protected boolean refillTileInventory(@Nonnull ItemStack itemStack) {
        return this.mergeItemStackRefill(itemStack, 10, 10 + subContainerSize, false);
    }

    protected boolean moveToCraftingStation(@Nonnull ItemStack itemstack) {
        return !this.mergeItemStack(itemstack, 1, 10, false);
    }

    // Fix for a vanilla bug: doesn't take Slot.getMaxStackSize into account
    @Override
    protected boolean mergeItemStack(@Nonnull ItemStack stack, int startIndex, int endIndex, boolean useEndIndex) {
        boolean ret = mergeItemStackRefill(stack, startIndex, endIndex, useEndIndex);
        if (!stack.isEmpty()) ret |= mergeItemStackMove(stack, startIndex, endIndex, useEndIndex);
        return ret;
    }

    // only refills items that are already present
    protected boolean mergeItemStackRefill(@Nonnull ItemStack stack, int startIndex, int endIndex, boolean useEndIndex) {
        if (stack.isEmpty()) return false;

        boolean flag1 = false;
        int k = startIndex;

        if (useEndIndex) {
            k = endIndex - 1;
        }

        Slot slot;
        ItemStack stack1;

        if (stack.isStackable()) {
            while (!stack.isEmpty() && ((!useEndIndex && (k < endIndex)) || (useEndIndex && (k >= startIndex)))) {
                slot = this.inventorySlots.get(k);
                stack1 = slot.getStack();

                if (!stack1.isEmpty()
                        && stack1.getItem() == stack.getItem()
                        && ItemStack.areItemStackTagsEqual(stack, stack1)
                        && this.canMergeSlot(stack, slot)) {
                    int l = stack1.getCount() + stack.getCount();
                    int limit = Math.min(stack.getMaxStackSize(), slot.getItemStackLimit(stack));

                    if (l <= limit) {
                        stack.setCount(0);
                        stack1.setCount(l);
                        slot.onSlotChanged();
                        flag1 = true;
                    } else if (stack1.getCount() < limit) {
                        stack.shrink(limit - stack1.getCount());
                        stack1.setCount(limit);
                        slot.onSlotChanged();
                        flag1 = true;
                    }
                }

                if (useEndIndex) {
                    --k;
                } else {
                    ++k;
                }
            }
        }
        return flag1;
    }

    // only moves items into empty slots
    protected boolean mergeItemStackMove(@Nonnull ItemStack stack, int startIndex, int endIndex, boolean useEndIndex) {
        if (stack.isEmpty()) return false;

        boolean flag1 = false;
        int k;

        if (useEndIndex) {
            k = endIndex - 1;
        } else {
            k = startIndex;
        }

        while (!useEndIndex && k < endIndex || useEndIndex && k >= startIndex) {
            Slot slot = this.inventorySlots.get(k);
            ItemStack itemstack1 = slot.getStack();

            if (itemstack1.isEmpty() && slot.isItemValid(stack) && this.canMergeSlot(stack, slot)) // Forge: Make sure to respect isItemValid in the slot.
            {
                int limit = slot.getItemStackLimit(stack);
                ItemStack stack2 = stack.copy();
                if (stack2.getCount() > limit) {
                    stack2.setCount(limit);
                    stack.shrink(limit);
                } else {
                    stack.setCount(0);
                }
                slot.putStack(stack2);
                slot.onSlotChanged();
                flag1 = true;

                if (stack.isEmpty()) {
                    break;
                }
            }

            if (useEndIndex) {
                --k;
            } else {
                ++k;
            }
        }


        return flag1;
    }


    @Override
    public boolean canMergeSlot(ItemStack stack, Slot slot) {
        return slot.inventory != craftResult && super.canMergeSlot(stack, slot);
    }

    public void updateSlotPositions(int offset) {
        Pair<Integer, Integer> range = containerStarts.get(currentContainer);
        int start = range.getLeft();
        for (int i = start; i < range.getRight(); i++) {
            Slot slot = inventorySlots.get(i);
            int index = (i - start) / 6 - offset;
            slot.yPos = (index >= 9 || index < 0) ? -10000 : 17 + 18 * index;
        }
    }

    public void changeContainer(int newContainer) {
        this.currentContainer = newContainer;
        Pair<Integer, Integer> range = containerStarts.get(currentContainer);
        int start = range.getLeft();
        int finish = range.getRight();
        for (int i = 10; i < subContainerSize + 10; i++) {
            Slot slot = this.inventorySlots.get(i);
            if (slot instanceof WrapperSlot && (i < start || i >= finish)) hideSlot(slot);
        }
        for (int i = start; i < finish; i++) {
            Slot slot = inventorySlots.get(i);
            int row = (i - start) / 6;
            int column = (i - start) % 6;
            slot.yPos = (row >= 9 || row < 0) ? -10000 : 17 + 18 * row;
            final int offsetx = needsScroll() ? 0 : 8;
            slot.xPos = 18 * column - 125 + offsetx;

        }
    }

    public void updateLastRecipeFromServer(IRecipe<CraftingInventory> recipe) {
        lastRecipe = recipe;
        // if no recipe, set to empty to prevent ghost outputs when another player grabs the result
        this.craftResult.setInventorySlotContents(0, recipe != null ? recipe.getCraftingResult(craftMatrix) : ItemStack.EMPTY);
    }

    public boolean needsScroll() {
        return getSlotCount() > 54;
    }

    public int getRows() {
        return (int) Math.ceil((double) getSlotCount() / 6);
    }

    public int getSlotCount() {
        if (containerStarts.isEmpty()) return 0;
        Pair<Integer, Integer> range = containerStarts.get(currentContainer);
        return range.getRight() - range.getLeft();
    }

    private void accept(IItemHandler handler) {
        if (containerStarts.size() == 0) {
            int left = 10;
            int right = handler.getSlots() + left;
            containerStarts.add(Pair.of(left, right));
            return;
        }

        int left = containerStarts.get(containerStarts.size() - 1).getRight();
        int right = handler.getSlots() + left;
        containerStarts.add(Pair.of(left, right));
    }

    public NonNullList<ItemStack> getRemainingItems() {
        return lastRecipe != null && lastRecipe.matches(craftMatrix, world) ? lastRecipe.getRemainingItems(craftMatrix) : craftMatrix.getStackList();
    }
}