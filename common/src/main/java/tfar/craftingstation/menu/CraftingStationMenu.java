package tfar.craftingstation.menu;

import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.crafting.*;
import tfar.craftingstation.CommonTagUtil;
import tfar.craftingstation.CraftingStation;
import tfar.craftingstation.ModIntegration;
import tfar.craftingstation.PersistantCraftingContainer;
import tfar.craftingstation.blockentity.CraftingStationBlockEntity;
import tfar.craftingstation.init.ModMenuTypes;
import tfar.craftingstation.network.S2CSideSetSideContainerSlot;
import tfar.craftingstation.platform.Services;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import tfar.craftingstation.util.SideContainerWrapper;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class CraftingStationMenu extends AbstractContainerMenu {

    public static final int MAX_SLOTS = 54;

    public final PersistantCraftingContainer craftMatrix;
    public final ResultContainer craftResult;
    public final Level world;
    public final CraftingStationBlockEntity tileEntity;

    public Map<Direction, ItemStack> blocks = new EnumMap<>(Direction.class);
    public Map<Direction, BlockEntity> blockEntityMap = new EnumMap<>(Direction.class);

    public final Map<Direction, Component> containerNames = new EnumMap<>(Direction.class);
    private final Player player;
    private final BlockPos pos;
    private int firstSlot;

    public CraftingStationMenu(int id, Inventory inv, BlockPos pos) {
        this(id, inv, new SimpleContainer(9),new ResultContainer(), pos);
    }


    public CraftingStationMenu(int id, Inventory inv, SimpleContainer inputContainer, ResultContainer outputContainer, BlockPos pos) {
        super(ModMenuTypes.crafting_station, id);
        this.player = inv.player;
        this.pos = pos;
        this.world = player.level();
        this.tileEntity = (CraftingStationBlockEntity) ModIntegration.getTileEntityAtPos(player.level(), pos);
        setCurrentContainer(tileEntity.getCurrentContainer());
        this.craftMatrix = new PersistantCraftingContainer(this, inputContainer);
        this.craftResult = outputContainer;

        addOwnSlots();

        if (Services.PLATFORM.getConfig().sideContainers()) {
            searchSideInventories();
        }

        addSideInventorySlots();
        addPlayerSlots(inv);
        slotsChanged(craftMatrix);
    }

    public static class SideContainerSlot extends Slot {


        private final CraftingStationMenu craftingStationMenu;

        public SideContainerSlot(int slot, int $$2, int $$3, CraftingStationMenu craftingStationMenu) {
            super(new SimpleContainer(0), slot, $$2, $$3);
            this.craftingStationMenu = craftingStationMenu;
        }


        @Override
        public ItemStack getItem() {
            return craftingStationMenu.getCurrentHandler().$getStack(getActualSlot());
        }

        @Override
        public ItemStack remove(int pAmount) {
            return craftingStationMenu.getCurrentHandler().$removeStack(getActualSlot(), pAmount);
        }

        @Override
        public boolean mayPlace(ItemStack $$0) {
            return craftingStationMenu.getCurrentHandler().$valid(getActualSlot());
        }

        @Override
        public void set(ItemStack $$0) {
            craftingStationMenu.getCurrentHandler().$setStack(getActualSlot(), $$0);
        }

        public int getActualSlot() {
            return getContainerSlot() + craftingStationMenu.firstSlot;
        }

    }

    public SideContainerWrapper getCurrentHandler() {
        return Services.PLATFORM.getWrapper(blockEntityMap.get(getSelectedContainer()));
    }

    protected void addSideInventorySlots() {
        int rows = 9;
        int cols = 6;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int index = col + cols * row;
                int xPos = (needsScroll() ? -125 : -117) + col * 18;
                int yPos = 17 + row * 18;
                addSlot(new SideContainerSlot(index, xPos, yPos, this));
            }
        }
    }

    public boolean hasSideContainers() {
        return !blocks.isEmpty();
    }

    public int subContainerSize() {
        return getCurrentHandler().$getSlotCount();
    }


    public Direction getSelectedContainer() {
        return currentContainer;
    }

    //it goes crafting output slot | 0
    //crafting input slots | 1 to 9
    //side inventories (if any) | 10 to (9 + subContainerSize)
    //player inventory | (10 + subContainerSides)

    protected void searchSideInventories() {
        // detect te
        for (Direction dir : Direction.values()) {
            BlockPos neighbor = pos.relative(dir);

            BlockEntity te = world.getBlockEntity(neighbor);
            if (te != null && !(te instanceof CraftingStationBlockEntity)) {
                // if blacklisted, skip checks entirely
                if (CommonTagUtil.isIn(CraftingStation.blacklisted, te.getType()))
                    continue;
                if (te instanceof Container container && !container.stillValid(player)) {
                    continue;
                }

                // try internal access first
                if (Services.PLATFORM.hasCapability(te)) {
                    blockEntityMap.put(dir, te);
                    blocks.put(dir, new ItemStack(world.getBlockState(neighbor).getBlock()));

                    Component name = null;
                    if (te instanceof MenuProvider menuProvider) {
                        name = menuProvider.getDisplayName();
                    } else if (te instanceof Nameable nameable) {
                        name = nameable.getDisplayName();
                    }

                    containerNames.put(dir, name== null ? te.getBlockState().getBlock().getName() : name);
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
    }


    private void addOwnSlots() {
        // crafting result
        this.addSlot(new ResultSlot(player, this.craftMatrix, craftResult, 0, 124, 35));

        // crafting grid
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                addSlot(new Slot(craftMatrix, x + 3 * y, 30 + 18 * x, 17 + 18 * y));
            }
        }
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
        slotChangedCraftingGrid(this, world, player, craftMatrix, craftResult, null);
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {

        if (hasSideContainers()) {

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
            nothingDone &= !mergeItemStackMove(stack, 10, 10 + subContainerSize());
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
        else if (index < 10 + Math.min(subContainerSize(),MAX_SLOTS)) {
            // Try moving crafting station -> preferred modules
            nothingDone = !moveToCraftingStation(stack);

            // Try moving module -> player inventory
            nothingDone &= !moveToPlayerInventory(stack);
        }
        // Slot is from the player inventory
        else if (index >= 10 + Math.min(subContainerSize(),MAX_SLOTS)) {
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

    protected static void slotChangedCraftingGrid(
            AbstractContainerMenu pMenu,
            Level pLevel,
            Player pPlayer,
            CraftingContainer pCraftSlots,
            ResultContainer pResultSlots,
            RecipeHolder<CraftingRecipe> pRecipe
    ) {
        if (!pLevel.isClientSide) {
            CraftingInput craftinginput = pCraftSlots.asCraftInput();
            ServerPlayer serverplayer = (ServerPlayer) pPlayer;
            ItemStack itemstack = ItemStack.EMPTY;
            Optional<RecipeHolder<CraftingRecipe>> optional = pLevel.getServer()
                    .getRecipeManager()
                    .getRecipeFor(RecipeType.CRAFTING, craftinginput, pLevel, pRecipe);
            if (optional.isPresent()) {
                RecipeHolder<CraftingRecipe> recipeholder = optional.get();
                CraftingRecipe craftingrecipe = recipeholder.value();
                if (pResultSlots.setRecipeUsed(pLevel, serverplayer, recipeholder)) {
                    ItemStack itemstack1 = craftingrecipe.assemble(craftinginput, pLevel.registryAccess());
                    if (itemstack1.isItemEnabled(pLevel.enabledFeatures())) {
                        itemstack = itemstack1;
                    }
                }
            }

            pResultSlots.setItem(0, itemstack);
            pMenu.setRemoteSlot(0, itemstack);
            serverplayer.connection.send(new ClientboundContainerSetSlotPacket(pMenu.containerId, pMenu.incrementStateId(), 0, itemstack));
        }
    }

    public boolean sameGui(CraftingStationMenu otherContainer) {
        return this.tileEntity == otherContainer.tileEntity;
    }

    protected ItemStack notifySlotAfterTransfer(Player player, ItemStack stack, ItemStack original, Slot slot) {
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
    protected boolean moveToSideInventory(ItemStack itemstack) {
        return hasSideContainers() && this.mergeItemStackMoveSideContainer(itemstack, 0, subContainerSize());
    }

    protected boolean moveToPlayerInventory(ItemStack itemstack) {
        return this.moveItemStackTo(itemstack, 10 + Math.min(subContainerSize(),MAX_SLOTS), this.slots.size(), false);
    }

    protected boolean refillSideInventory(ItemStack itemStack) {
        return this.mergeItemStackRefillSideContainer(itemStack, 0, subContainerSize());
    }

    protected boolean moveToCraftingStation(ItemStack itemstack) {
        return this.moveItemStackTo(itemstack, 1, 10, false);
    }

    // Fix for a vanilla bug: doesn't take Slot.getMaxStackSize into account
    @Override
    protected boolean moveItemStackTo(ItemStack stack, int startIndex, int endIndex, boolean useEndIndex) {
        boolean didSomething = mergeItemStackRefill(stack, startIndex, endIndex);
        if (!stack.isEmpty()) didSomething |= mergeItemStackMove(stack, startIndex, endIndex);
        return didSomething;
    }

    // only refills items that are already present
    //return true if successful
    protected boolean mergeItemStackRefill(ItemStack stack, int startIndex, int endIndex) {
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
                        && ItemStack.isSameItemSameComponents(stack, slotStack)
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
    protected boolean mergeItemStackMove(ItemStack stack, int startIndex, int endIndex) {
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

    // only moves items into empty slots
    protected boolean mergeItemStackMoveSideContainer(ItemStack stack, int startIndex, int endIndex) {
        if (stack.isEmpty()) return false;

        boolean didSomething = false;
        SideContainerWrapper sideContainerWrapper = getCurrentHandler();
        ItemStack remainder = stack.copy();
        for (int k = startIndex; k < endIndex; k++) {
            ItemStack slotStack = sideContainerWrapper.$getStack(k);
            if (slotStack.isEmpty() && sideContainerWrapper.$valid(k)){ // Forge: Make sure to respect isItemValid in the slot.
                remainder = sideContainerWrapper.$insert(k,remainder,false);
                didSomething = remainder != stack;

                if (remainder.isEmpty()) {
                    break;
                }
            }
        }

        if (didSomething) {
            stack.setCount(remainder.getCount());
        }

        return didSomething;
    }

    // only refills items that are already present
    //return true if successful
    protected boolean mergeItemStackRefillSideContainer(ItemStack stack, int startIndex, int endIndex) {
        if (stack.isEmpty()) return false;

        SideContainerWrapper sideContainerWrapper = getCurrentHandler();

        boolean didSomething = false;

        ItemStack slotStack;

        if (stack.isStackable()) {
            ItemStack remainder = stack.copy();

            for (int k = startIndex; k < endIndex; k++) {
                if (stack.isEmpty()) break;
                slotStack = sideContainerWrapper.$getStack(k);
                if (!slotStack.isEmpty() && ItemStack.isSameItemSameComponents(stack, slotStack)) {
                    remainder = sideContainerWrapper.$insert(k,remainder,false);
                    didSomething = remainder != stack;

                    if (remainder.isEmpty()) {
                        break;
                    }
                }
            }
            if (didSomething) {
                stack.setCount(remainder.getCount());
            }
        }
        return didSomething;
    }


    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return slot.container != craftResult && super.canTakeItemForPickAll(stack, slot);
    }

    public boolean needsScroll() {
        return getCurrentHandler().$getSlotCount() > MAX_SLOTS;
    }

    protected Direction currentContainer;

    public void setCurrentContainer(Direction currentContainer) {
        this.currentContainer = currentContainer;
    }


    public enum ButtonAction {
        CLEAR, TAB_0, TAB_1, TAB_2, TAB_3, TAB_4, TAB_5;
        static final ButtonAction[] VALUES = values();
    }

    @Override
    public boolean clickMenuButton(Player pPlayer, int id) {
        if (id < 0 || id >= ButtonAction.VALUES.length) return false;
        ButtonAction buttonAction = ButtonAction.VALUES[id];
        if (pPlayer instanceof ServerPlayer) {
            switch (buttonAction) {
                case CLEAR -> {
                    for (int i = 1; i < 10; i++) quickMoveStack(player, i);
                }
                case TAB_0, TAB_1, TAB_2, TAB_3, TAB_4, TAB_5 -> {
                    Direction direction = Direction.values()[id - 1];
                    if (blockEntityMap.get(direction) != null)
                        setCurrentContainer(direction);
                }
            }
        }
        return true;
    }

    @Override
    public void removed(Player $$0) {
        super.removed($$0);
        if (!$$0.level().isClientSide) {
            tileEntity.setCurrentContainer(currentContainer);
        }
    }

    public void setFirstSlot(int firstSlot) {
        this.firstSlot = firstSlot;
    }

    public int getFirstSlot() {
        return firstSlot;
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        if (hasSideContainers()) {
            syncSideContainers();
        }
    }


    public void syncSideContainers() {
        for (Map.Entry<Direction, BlockEntity> entry : blockEntityMap.entrySet()) {
            Direction direction = entry.getKey();
            BlockEntity blockEntity = entry.getValue();
            SideContainerWrapper wrapper = Services.PLATFORM.getWrapper(blockEntity);
            if (wrapper != null) {
                for (int i = 0; i < wrapper.$getSlotCount(); i++) {
                    Services.PLATFORM.sendToClient(new S2CSideSetSideContainerSlot(wrapper.$getStack(i), direction, i), (ServerPlayer) player);
                }
            }
        }
    }


    public void synchronizeSlotToRemote(int pSlotIndex, ItemStack pStack, Supplier<ItemStack> pSupplier) {
        if (!this.suppressRemoteUpdates) {
            ItemStack itemstack = this.remoteSlots.get(pSlotIndex);
            if (true) {
                ItemStack itemstack1 = pSupplier.get();
                this.remoteSlots.set(pSlotIndex, itemstack1);
                if (this.synchronizer != null) {
                    // Forge: Only synchronize a slot change if the itemstack actually changed in a way that is relevant to the client (i.e. share tag changed)
                    this.synchronizer.sendSlotChange(this, pSlotIndex, itemstack1);
                }
            }
        }
    }
}