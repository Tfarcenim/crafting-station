package tfar.craftingstation;

import net.minecraft.world.inventory.*;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import tfar.craftingstation.init.ModMenuTypes;
import tfar.craftingstation.network.PacketHandler;
import tfar.craftingstation.network.S2CLastRecipePacket;
import tfar.craftingstation.slot.BigSlot;
import tfar.craftingstation.slot.SlotFastCraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CraftingStationContainer extends AbstractContainerMenu {
    private static final Method GET_TILE_ENTITY_METHOD;

    static {
        Method doubleSlabsGetTileEntity1 = null;
        if (ModList.get().isLoaded("doubleslabs")) {
            try {
                Class<?> doubleSlabsFlags = Class.forName("cjminecraft.doubleslabs.api.Flags");
                doubleSlabsGetTileEntity1 = doubleSlabsFlags.getDeclaredMethod("getTileEntityAtPos", BlockPos.class, BlockGetter.class);
            } catch (ClassNotFoundException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
        GET_TILE_ENTITY_METHOD = doubleSlabsGetTileEntity1;
    }

    public final CraftingInventoryPersistant craftMatrix;
    public final ResultContainer craftResult = new ResultContainer();
    public final Level world;
    public final CraftingStationBlockEntity tileEntity;
    public final List<Pair<Integer, Integer>> containerStarts = new ArrayList<>();
    public final List<ItemStack> blocks = new ArrayList<>();

    public final List<Component> containerNames = new ArrayList<>();
    private final Player player;
    private ContainerData data;
    public Recipe<CraftingContainer> lastRecipe;
    public int subContainerSize = 0;
    public boolean hasSideContainers;
    protected Recipe<CraftingContainer> lastLastRecipe;

    protected DataSlot slot;

    private static BlockEntity getTileEntityAtPos(BlockPos pos, Level world) {
        try {
            return GET_TILE_ENTITY_METHOD != null ? (BlockEntity) GET_TILE_ENTITY_METHOD.invoke(null, pos, world) : world.getBlockEntity(pos);
        } catch (IllegalAccessException | InvocationTargetException ignored) {
            return world.getBlockEntity(pos);
        }
    }

    public CraftingStationContainer(int id, Inventory inv, BlockPos pos) {
        this(id, inv, pos,new SimpleContainerData(1));
    }


    public CraftingStationContainer(int id, Inventory inv, BlockPos pos, ContainerData data) {
        super(ModMenuTypes.crafting_station, id);
        this.player = inv.player;
        this.data = data;
        this.world = player.level;
        this.tileEntity = (CraftingStationBlockEntity) getTileEntityAtPos(pos, world);

        this.data = data;
        this.craftMatrix = new CraftingInventoryPersistant(this, tileEntity.input);
        this.hasSideContainers = false;

        addOwnSlots();

        if (Configs.Server.sideInventories.get()) {
            searchSideInventories(pos);
        }

        addPlayerSlots(inv);
        slotsChanged(craftMatrix);
        if (hasSideContainers) changeContainer(getCurrentContainer());
        addDataSlots(data);
    }

    //it goes crafting output slot | 0
    //crafting input slots | 1 to 9
    //side inventories (if any) | 10 to (9 + subContainerSize)
    //player inventory | (10 + subContainerSides)

    protected void searchSideInventories(BlockPos pos) {
        // detect te
        Direction accessDir = null;
        List<BlockEntity> tileEntities = new ArrayList<>();
        for (Direction dir : Direction.values()) {
            BlockPos neighbor = pos.relative(dir);

            BlockEntity te = world.getBlockEntity(neighbor);
            if (te != null && !(te instanceof CraftingStationBlockEntity)) {
                // if blacklisted, skip checks entirely
                if (ForgeRegistries.BLOCK_ENTITY_TYPES.tags().getTag(CraftingStation.blacklisted).contains(te.getType()))
                    continue;
                if (te instanceof Container container && !container.stillValid(player)) {
                    continue;
                }

                // try internal access first
                if (te.getCapability(ForgeCapabilities.ITEM_HANDLER, null).filter(IItemHandlerModifiable.class::isInstance).isPresent()) {
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
            for (BlockEntity tileEntity : tileEntities) {
                tileEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(this::accept);
            }
        }

        if (!tileEntities.isEmpty()) {
            addSideContainerSlots(tileEntities, accessDir, -125, 17);
        }
    }

    public static Recipe<CraftingContainer> findRecipe(CraftingContainer inv, Level world, Player player) {
        return world.getRecipeManager().getRecipeFor(RecipeType.CRAFTING,inv,world).stream().findFirst().orElse(null);
    }

    //                CraftingStation.LOGGER.error("Bad recipe found: " + recipe.getId().toString());
    //                CraftingStation.LOGGER.error(e.getMessage());
    //                player.sendMessage(new TranslatableComponent("text.crafting_station.error", recipe.getId().toString()).withStyle(ChatFormatting.DARK_RED), Util.NIL_UUID);

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

    private void addSideContainerSlots(List<BlockEntity> tes, Direction dir, int xPos, int yPos) {
        for (int i = 0; i < tes.size(); i++) {
            BlockEntity te = tes.get(i);
            containerNames.add(te instanceof MenuProvider menuProvider? menuProvider.getDisplayName() : te.getBlockState().getBlock().getName());
            final int number = i;
            te.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(h -> {
                int size = h.getSlots();
                this.subContainerSize += size;
                int offsetx = needsScroll() ? 0 : 8;
                for (int y = 0; y < (int) Math.ceil(size / 6d); y++)
                    for (int x = 0; x < 6; x++) {
                        int index = 6 * y + x;
                        if (index >= size) continue;
                        boolean hidden = y >= 9 || number != 0;
                        SlotItemHandler wrapper = new BigSlot(h, index, 18 * x + xPos + offsetx, 18 * y + yPos);
                        if (hidden) hideSlot(wrapper);
                        addSlot(wrapper);
                    }
            });
        }
        hasSideContainers = true;
    }

    public void hideSlot(Slot slot) {
        slot.y = Integer.MAX_VALUE;
    }

    void setCurrentContainer(int container) {
        data.set(0,container);
    }

     public int getCurrentContainer() {
        return data.get(0);
    }

    private void addPlayerSlots(Inventory playerInventory) {
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
    //@Override
    //public void setAll(List<ItemStack> p_190896_1_) {
    //    craftMatrix.setDoNotCallUpdates(true);
    //    super.setAll(p_190896_1_);
     //   craftMatrix.setDoNotCallUpdates(false);
     //   craftMatrix.onCraftMatrixChanged();
   // }

    @Override
    public void slotsChanged(Container inventory) {
        this.slotChangedCraftingGrid(world, player, craftMatrix, craftResult);
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Nonnull
    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {

        if (hasSideContainers) {

            return handleTransferWithSides(playerIn, index);
        } else {

            Slot slot = this.slots.get(index);

            if (slot == null || !slot.hasItem()) {
                return ItemStack.EMPTY;
            }

            ItemStack ret = slot.getItem().copy();
            ItemStack stack = slot.getItem().copy();

            boolean nothingDone;

            //is this the crafting output slot?
            if (index == 0) {

                // Try moving module -> player inventory
                nothingDone = !moveToPlayerInventory(stack);

                // Try moving module -> tile inventory
            }

            // Is the slot an input slot??
            else if (index < 10) {
                // Try moving module -> player inventory
                nothingDone = !moveToPlayerInventory(stack);

                // Try moving module -> tile inventory
            }
            // Is the slot from the tile?
            else {
                // try moving player -> modules
                nothingDone = !moveToCraftingStation(stack);

                // Try moving player -> tile inventory
            }

            if (nothingDone) {
                return ItemStack.EMPTY;
            }
            return notifySlotAfterTransfer(playerIn, stack, ret, slot);
        }
    }

    protected ItemStack handleTransferWithSides(Player player, int index) {
        Slot slot = this.slots.get(index);

        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack ret = slot.getItem().copy();
        ItemStack stack = ret.copy();

        boolean nothingDone;

        //is this the crafting output slot?
        if (index == 0) {

            nothingDone = !refillSideInventory(stack);
            // Try moving module -> player inventory
            nothingDone &= !moveToPlayerInventory(stack);

            // Try moving module -> tile inventory
            nothingDone &= !mergeItemStackMove(stack, 10, 10 + subContainerSize);
        }

        // Is the slot an input slot??
        else if (index < 10) {
            //try to refill side inventory
            nothingDone = !refillSideInventory(stack);
            // Try moving crafting station -> player inventory
            nothingDone &= !moveToPlayerInventory(stack);

            // Try moving crafting station -> side inventory
            nothingDone &= !moveToSideInventory(stack);
        }
        // Is the slot from the side inventories?
        else if (index < 10 + subContainerSize) {
            // Try moving crafting station -> preferred modules
            nothingDone = !moveToCraftingStation(stack);

            // Try moving module -> player inventory
            nothingDone &= !moveToPlayerInventory(stack);
        }
        // Slot is from the player inventory
        else if (index >= 10 + subContainerSize) {
            // try moving player -> modules
            nothingDone = !moveToCraftingStation(stack);

            // Try moving player -> crafting station inventory
            nothingDone &= !moveToSideInventory(stack);
        }
        // you violated some assumption or something. Shame on you.
        else {
            return ItemStack.EMPTY;
        }

        if (nothingDone) {
            return ItemStack.EMPTY;
        }
        return notifySlotAfterTransfer(player, stack, ret, slot);
    }

    protected void slotChangedCraftingGrid(Level world, Player player, CraftingContainer inv, ResultContainer result) {
        ItemStack itemstack = ItemStack.EMPTY;

        // if the recipe is no longer valid, update it
        if (lastRecipe == null || !lastRecipe.matches(inv, world)) {
            lastRecipe = findRecipe(inv, world, player);
        }

        // if we have a recipe, fetch its result
        if (lastRecipe != null) {
            itemstack = lastRecipe.assemble(inv);
        }
        // set the slot on both sides, client is for display/so the client knows about the recipe
        result.setItem(0, itemstack);

        // update recipe on server
        if (!world.isClientSide) {
            ServerPlayer entityplayermp = (ServerPlayer) player;

            // we need to sync to all players currently in the inventory
            List<ServerPlayer> relevantPlayers = getAllPlayersWithThisContainerOpen(this, entityplayermp.getLevel());

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

    private void syncResultToAllOpenWindows(final ItemStack stack, List<ServerPlayer> players) {
        players.forEach(otherPlayer -> {
            otherPlayer.containerMenu.setItem(0,this.getStateId(), stack);
            //otherPlayer.connection.sendPacket(new SPacketSetSlot(otherPlayer.openContainer.windowId, SLOT_RESULT, stack));
        });
    }

    private void syncRecipeToAllOpenWindows(final Recipe<CraftingContainer> lastRecipe, List<ServerPlayer> players) {
        players.forEach(otherPlayer -> {
            // safe cast since hasSameContainerOpen does class checks
            ((CraftingStationContainer) otherPlayer.containerMenu).lastRecipe = lastRecipe;
            PacketHandler.INSTANCE.sendTo(new S2CLastRecipePacket(lastRecipe), otherPlayer.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
        });
    }

    private List<ServerPlayer> getAllPlayersWithThisContainerOpen(CraftingStationContainer container, ServerLevel server) {
        return server.players().stream()
                .filter(player -> hasSameContainerOpen(container, player))
                .collect(Collectors.toList());
    }

    private boolean hasSameContainerOpen(CraftingStationContainer container, Player playerToCheck) {
        return playerToCheck instanceof ServerPlayer &&
                playerToCheck.containerMenu.getClass().isAssignableFrom(container.getClass()) &&
                this.sameGui((CraftingStationContainer) playerToCheck.containerMenu);
    }

    public boolean sameGui(CraftingStationContainer otherContainer) {
        return this.tileEntity == otherContainer.tileEntity;
    }

    @Nonnull
    protected ItemStack notifySlotAfterTransfer(Player player, @Nonnull ItemStack stack, @Nonnull ItemStack original, Slot slot) {
        // notify slot
        slot.onQuickCraft(stack, original);

        if (stack.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }

        // update slot we pulled from
        slot.set(stack);
        slot.onTake(player, stack);

        if (slot.hasItem() && slot.getItem().isEmpty()) {
            slot.set(ItemStack.EMPTY);
        }

        return original;
    }

    //return true if anything happened
    protected boolean moveToSideInventory(@Nonnull ItemStack itemstack) {
        return hasSideContainers && this.mergeItemStackMove(itemstack, 10, 10 + subContainerSize);
    }

    protected boolean moveToPlayerInventory(@Nonnull ItemStack itemstack) {
        return this.moveItemStackTo(itemstack, 10 + subContainerSize, this.slots.size(), false);
    }

    protected boolean refillSideInventory(@Nonnull ItemStack itemStack) {
        return this.mergeItemStackRefill(itemStack, 10, 10 + subContainerSize);
    }

    protected boolean moveToCraftingStation(@Nonnull ItemStack itemstack) {
        return this.moveItemStackTo(itemstack, 1, 10, false);
    }

    // Fix for a vanilla bug: doesn't take Slot.getMaxStackSize into account
    @Override
    protected boolean moveItemStackTo(@Nonnull ItemStack stack, int startIndex, int endIndex, boolean useEndIndex) {
        boolean didSomething = mergeItemStackRefill(stack, startIndex, endIndex);
        if (!stack.isEmpty()) didSomething |= mergeItemStackMove(stack, startIndex, endIndex);
        return didSomething;
    }

    // only refills items that are already present
    //return true if successful
    protected boolean mergeItemStackRefill(@Nonnull ItemStack stack, int startIndex, int endIndex) {
        if (stack.isEmpty()) return false;

        boolean didSomething = false;

        Slot targetSlot;
        ItemStack slotStack;

        if (stack.isStackable()) {

            for (int k = startIndex; k < endIndex; k++) {
                if (stack.isEmpty()) break;
                targetSlot = this.slots.get(k);
                slotStack = targetSlot.getItem();

                if (!slotStack.isEmpty()
                        && slotStack.getItem() == stack.getItem()
                        && ItemStack.tagMatches(stack, slotStack)
                        && this.canTakeItemForPickAll(stack, targetSlot)) {
                    int l = slotStack.getCount() + stack.getCount();
                    int limit = targetSlot.getMaxStackSize(stack);

                    if (l <= limit) {
                        stack.setCount(0);
                        slotStack.setCount(l);
                        targetSlot.setChanged();
                        didSomething = true;
                    } else if (slotStack.getCount() < limit) {
                        stack.shrink(limit - slotStack.getCount());
                        slotStack.setCount(limit);
                        targetSlot.setChanged();
                        didSomething = true;
                    }
                }
            }
        }
        return didSomething;
    }

    // only moves items into empty slots
    protected boolean mergeItemStackMove(@Nonnull ItemStack stack, int startIndex, int endIndex) {
        if (stack.isEmpty()) return false;

        boolean didSomething = false;

        for (int k = startIndex; k < endIndex; k++) {
            Slot targetSlot = this.slots.get(k);
            ItemStack slotStack = targetSlot.getItem();

            if (slotStack.isEmpty() && targetSlot.mayPlace(stack) && this.canTakeItemForPickAll(stack, targetSlot)) // Forge: Make sure to respect isItemValid in the slot.
            {
                int limit = targetSlot.getMaxStackSize(stack);
                ItemStack stack2 = stack.copy();
                if (stack2.getCount() > limit) {
                    stack2.setCount(limit);
                    stack.shrink(limit);
                } else {
                    stack.setCount(0);
                }
                targetSlot.set(stack2);
                targetSlot.setChanged();
                didSomething = true;

                if (stack.isEmpty()) {
                    break;
                }
            }
        }
        return didSomething;
    }


    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return slot.container != craftResult && super.canTakeItemForPickAll(stack, slot);
    }

    public void updateSlotPositions(int offset) {
        Pair<Integer, Integer> range = containerStarts.get(getCurrentContainer());
        int start = range.getLeft();
        for (int i = start; i < range.getRight(); i++) {
            Slot slot = slots.get(i);
            int index = (i - start) / 6 - offset;
            slot.y = (index >= 9 || index < 0) ? -10000 : 17 + 18 * index;
        }
    }

    public void changeContainer(int newContainer) {
        setCurrentContainer(newContainer);
        Pair<Integer, Integer> range = containerStarts.get(getCurrentContainer());
        int start = range.getLeft();
        int finish = range.getRight();
        for (int i = 10; i < subContainerSize + 10; i++) {
            Slot slot = this.slots.get(i);
            if (slot instanceof BigSlot && (i < start || i >= finish)) hideSlot(slot);
        }
        for (int i = start; i < finish; i++) {
            Slot slot = slots.get(i);
            int row = (i - start) / 6;
            int column = (i - start) % 6;
            slot.y = (row >= 9 || row < 0) ? -10000 : 17 + 18 * row;
            final int offsetx = needsScroll() ? 0 : 8;
            slot.x = 18 * column - 125 + offsetx;

        }
    }

    public void updateLastRecipeFromServer(Recipe<CraftingContainer> recipe) {
        lastRecipe = recipe;
        // if no recipe, set to empty to prevent ghost outputs when another player grabs the result
        this.craftResult.setItem(0, recipe != null ? recipe.assemble(craftMatrix) : ItemStack.EMPTY);
    }

    public boolean needsScroll() {
        return getSlotCount() > 54;
    }

    public int getRows() {
        return (int) Math.ceil((double) getSlotCount() / 6);
    }

    public int getSlotCount() {
        if (containerStarts.isEmpty()) return 0;
        if (getCurrentContainer() >= containerStarts.size()) {
            setCurrentContainer(containerStarts.size() - 1);
        }
        Pair<Integer, Integer> range = containerStarts.get(getCurrentContainer());
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