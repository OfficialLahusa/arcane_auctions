package com.lahusa.arcane_auctions.gui.screen;

import com.lahusa.arcane_auctions.ArcaneAuctions;
import com.lahusa.arcane_auctions.block.entity.ExperienceObeliskBlockEntity;
import com.lahusa.arcane_auctions.data.TransactionLogEntry;
import com.lahusa.arcane_auctions.gui.menu.ExperienceObeliskMenu;
import com.lahusa.arcane_auctions.net.ArcaneAuctionsPacketHandler;
import com.lahusa.arcane_auctions.net.ExperienceObeliskPermissionUpdateC2SPacket;
import com.lahusa.arcane_auctions.net.ExperienceObeliskTransactionC2SPacket;
import com.lahusa.arcane_auctions.util.ExperienceConverter;
import com.lahusa.arcane_auctions.util.NumberFormatter;
import com.lahusa.arcane_auctions.util.UserNameConverter;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class ExperienceObeliskScreen extends AbstractContainerScreen<ExperienceObeliskMenu> {
    private static final ResourceLocation BACKGROUND_LOCATION = ResourceLocation.fromNamespaceAndPath(ArcaneAuctions.MODID, "textures/gui/experience_obelisk.png");
    private static final ResourceLocation XP_OVERLAY_LOCATION = ResourceLocation.fromNamespaceAndPath(ArcaneAuctions.MODID, "textures/gui/xp_overlay.png");

    private final Inventory _inventory;
    private final Level _clientLevel;
    private EditBox _amountBox;

    private CycleButton<ExperienceObeliskBlockEntity.TransactionPermissions> _withdrawalPermissionButton;
    private CycleButton<ExperienceObeliskBlockEntity.TransactionPermissions> _depositPermissionButton;
    private CycleButton<ExperienceObeliskBlockEntity.TransactionPermissions> _logPermissionButton;

    /*
    0: Transaction
    1: Configuration
    2: Log
     */
    private int _selectedTab = 0;
    private final int _tabOffsetY;

    public ExperienceObeliskScreen(ExperienceObeliskMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 200;
        this.imageHeight = 116;
        this._clientLevel = menu.level;
        this._inventory = inv;
        this._tabOffsetY = (this.imageHeight - 3*26) / 2;
    }

    @Override
    protected void init() {
        super.init();
        initWidgets();
    }

    private ExperienceObeliskBlockEntity getBlockEntity() {
        BlockPos pos = menu.getBlockPos();
        assert pos != null;

        BlockEntity blockEntity = _clientLevel.getBlockEntity(pos);

        if (!(blockEntity instanceof ExperienceObeliskBlockEntity))
            throw new IllegalStateException("Missing experience obelisk block entity");

        return (ExperienceObeliskBlockEntity) blockEntity;
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics gfx, float partialTick, int mouseX, int mouseY) {
        // Draw unselected tabs
        for (int tabIdx = 0; tabIdx < 3; tabIdx++) {
            if(tabIdx == _selectedTab) continue;
            gfx.blit(BACKGROUND_LOCATION, this.leftPos-32+4, this.topPos+_tabOffsetY+tabIdx*26, 0, 116, 32, 26);
            gfx.renderItem(getTabIconItem(tabIdx).getDefaultInstance(), this.leftPos-32+4+8, this.topPos+_tabOffsetY+tabIdx*26+5);
        }

        // Draw main bg
        gfx.blit(BACKGROUND_LOCATION, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        if (_selectedTab == 0) {
            // Draw stored amount text field backdrop
            gfx.blit(BACKGROUND_LOCATION, this.leftPos+47, this.topPos+21, 64, 116, 106, 18);
        }

        // Draw selected tab
        gfx.blit(BACKGROUND_LOCATION, this.leftPos-32+4, this.topPos+_tabOffsetY+_selectedTab*26, 32, 116, 32, 26);
        gfx.renderItem(getTabIconItem(_selectedTab).getDefaultInstance(), this.leftPos-32+4+8, this.topPos+_tabOffsetY+_selectedTab*26+5);
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics gfx, int mouseX, int mouseY) {
        // Reset buttons
        for(Renderable renderable : renderables) {
            if (renderable instanceof AbstractButton button) {
                button.setFocused(false);
            }
        }

        Minecraft instance = Minecraft.getInstance();

        String titleText = this.title.getString();
        gfx.drawString(this.font, titleText, this.imageWidth / 2 - this.font.width(titleText) / 2, this.titleLabelY, 0x404040, false);

        ExperienceObeliskBlockEntity obeliskEntity = getBlockEntity();
        long xpPoints = obeliskEntity.getExperiencePoints();
        UUID owner = obeliskEntity.getOwner();
        boolean isOwner = owner != null && owner.equals(_inventory.player.getUUID());

        // Transaction tab
        if (_selectedTab == 0) {
            String xpText = NumberFormatter.longToString(xpPoints) + " stored";
            gfx.blit(XP_OVERLAY_LOCATION, this.imageWidth / 2 - (this.font.width(xpText) + 8) / 2, 6+20, 0, 0, 0,10, 10, 10, 10);
            gfx.drawString(instance.font, xpText, this.imageWidth / 2 - (this.font.width(xpText) + 8) / 2 + 8, 6+20, 0x80FF20);

            gfx.drawString(instance.font, "Deposit", this.imageWidth / 2 + 60 - (this.font.width("Deposit")) / 2, 30 + this.imageHeight / 2, 0x404040, false);
            gfx.drawString(instance.font, "Withdraw", this.imageWidth / 2 - 60 - (this.font.width("Withdraw")) / 2, 30 + this.imageHeight / 2, 0x404040, false);
        }
        // Configuration tab
        else if(_selectedTab == 1) {
            String ownerText = "Owner: ";

            // No owner set
            if (owner == null) {
                ownerText += "None";
            }
            // Client player is owner
            else if (isOwner) {
                ownerText += _inventory.player.getName().getString();
            }
            // Fetch username for UUID
            else {
                ownerText += UserNameConverter.getUserName(owner);
            }

            gfx.drawString(instance.font, ownerText, this.imageWidth / 2 - this.font.width(ownerText) / 2, 6+20, 0x404040, false);
        }
        // Log tab
        else if(_selectedTab == 2) {

            String logTitle = "Transaction Log (" + ExperienceObeliskBlockEntity.TRANSACTION_LOG_LENGTH + " most recent)";
            gfx.drawString(instance.font, logTitle, this.imageWidth / 2 - this.font.width(logTitle) / 2, 20, 0x404040, false);

            // Hide log when permissions are insufficient
            if (!obeliskEntity.mayViewLog(_inventory.player)) {
                String permissionError = "You don't have permission to access the log.";
                gfx.drawString(instance.font, permissionError,  this.imageWidth / 2 - this.font.width(permissionError) / 2, this.imageHeight / 2, 0xFF5555, false);
            }
            // Show log
            else {
                List<TransactionLogEntry> transactionLog = obeliskEntity.getTransactionLog();

                for (int i = 0; i < transactionLog.size(); i++) {
                    TransactionLogEntry entry = transactionLog.get(i);

                    gfx.drawString(instance.font, entry.username,  this.imageWidth / 3 - this.font.width(entry.username) / 2, 30 + 10 * i, 0x404040, false);
                    String amountStr = NumberFormatter.longToString(entry.amount);
                    gfx.drawString(instance.font, amountStr, 2 * this.imageWidth / 3 - this.font.width(amountStr) / 2, 30 + 10 * i, (entry.amount < 0) ? 0xFF5555 : 0x55FF55, false);
                }
            }
        }

        // Render tab tooltips
        for (int tabIdx = 0; tabIdx < 3; tabIdx++) {
            if (isPointOnTab(tabIdx, mouseX, mouseY)) {
                gfx.renderTooltip(instance.font, getTabTooltip(tabIdx), mouseX - this.leftPos, mouseY - this.topPos);
                break;
            }
        }
    }

    private Item getTabIconItem(int tabIdx) {
        return switch (tabIdx) {
            case 0 -> Items.EXPERIENCE_BOTTLE;
            case 1 -> Items.ENDER_EYE;
            case 2 -> Items.BOOK;
            default -> throw new IllegalArgumentException("tabIdx must be in range [0-2].");
        };
    }

    private Component getTabTooltip(int tabIdx) {
        return switch (tabIdx) {
            case 0 -> Component.literal("Transaction");
            case 1 -> Component.literal("Configuration");
            case 2 -> Component.literal("Log");
            default -> throw new IllegalArgumentException("tabIdx must be in range [0-2].");
        };
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            for (int tabIdx = 0; tabIdx < 3; tabIdx++) {
                if (isPointOnTab(tabIdx, mouseX, mouseY)) {
                    return true;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            for (int tabIdx = 0; tabIdx < 3; tabIdx++) {
                if (isPointOnTab(tabIdx, mouseX, mouseY)) {
                    changeTab(tabIdx);
                    return true;
                }
            }
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void changeTab(int tabIdx) {
        _selectedTab = tabIdx;
        initWidgets();
    }

    private boolean isPointOnTab(int tabIdx, double mouseX, double mouseY) {
        int xMin = this.leftPos-32+4;
        int yMin = this.topPos+_tabOffsetY+tabIdx*26;
        int xMax = xMin + 32;
        int yMax = yMin + 26;
        return mouseX >= xMin && mouseX <= xMax && mouseY >= yMin && mouseY <= yMax;
    }

    @Override
    protected void containerTick() {
        super.containerTick();
    }

    private void addAmountToBox(long value) {
        long prevValue = getAmountBoxValue();

        int multiplier = 1;

        // TODO: Shift for 1k multiplier
        if (hasShiftDown()) multiplier = 1000;
        if (hasControlDown()) multiplier = 1000000;

        prevValue += multiplier * value;

        _amountBox.setValue(NumberFormatter.longToString(prevValue));

        clampBoxAmount();
    }

    private void setBoxAmount(long value) {
        _amountBox.setValue(NumberFormatter.longToString(value));

        clampBoxAmount();
    }

    private void clampBoxAmount() {
        ExperienceObeliskBlockEntity obeliskEntity = getBlockEntity();

        // Determine min value by checking orbs stored in obelisk
        long minValue = -obeliskEntity.getExperiencePoints();

        // Determine max value by checking orbs stored in player
        Player player = _inventory.player;
        long maxValue = ExperienceConverter.getTotalCurrentXPPoints(player.experienceLevel, player.experienceProgress);

        // Clamp value in amount box to range
        long prevValue = getAmountBoxValue();

        //System.out.println("Min: " + minValue + ", Max: " + maxValue + ", Prev: " + prevValue + " (\"" + _amountBox.getValue() + ")\"");

        prevValue = Math.max(Math.min(prevValue, maxValue), minValue);

        _amountBox.setValue(NumberFormatter.longToString(prevValue));
        _amountBox.moveCursorToStart();
    }

    private long getAmountBoxValue() {
        long prevValue;

        try {
            prevValue = NumberFormatter.stringToLong(_amountBox.getValue());
        }
        catch (NumberFormatException e) {
            prevValue = 0;
        }

        return prevValue;
    }

    private void initWidgets() {
        clearWidgets();

        _depositPermissionButton = null;
        _withdrawalPermissionButton = null;
        _logPermissionButton = null;

        switch (_selectedTab) {
            case 0:
                initTransactionWidgets();
                break;
            case 1:
                initConfigurationWidgets();
                break;
            case 2:
                initLogWidgets();
                break;
            default:
                throw new IllegalStateException("_selectedTab must be in range [0-2].");
        }
    }

    private void initTransactionWidgets() {
        int buttonY = this.height / 2 + 6;

        Button reset = Button.builder(
                        Component.literal("0"),
                        (onPress) -> {setBoxAmount(0);}
                )
                .tooltip(Tooltip.create(Component.literal("Reset transaction amount field.")))
                .size(20, 20)
                .pos(this.width / 2 - 10, buttonY)
                .build();

        Button addOne = Button.builder(
                        Component.literal("1").withStyle(ChatFormatting.GREEN),
                        (onPress) -> {addAmountToBox(1);}
                )
                .tooltip(Tooltip.create(Component.literal("Deposit 1 carried experience points.\n[SHIFT] x1,000\n[CTRL] x1,000,000")))
                .size(20, 20)
                .pos(this.width / 2 + 20 - 10, buttonY)
                .build();

        Button removeOne = Button.builder(
                        Component.literal("1").withStyle(ChatFormatting.RED),
                        (onPress) -> {addAmountToBox(-1);}
                )
                .tooltip(Tooltip.create(Component.literal("Withdraw 1 stored experience points.\n[SHIFT] x1,000\n[CTRL] x1,000,000")))
                .size(20, 20)
                .pos(this.width / 2 - 20 - 10, buttonY)
                .build();

        Button addTen = Button.builder(
                        Component.literal("10").withStyle(ChatFormatting.GREEN),
                        (onPress) -> {addAmountToBox(10);}
                )
                .tooltip(Tooltip.create(Component.literal("Deposit 10 carried experience points.\n[SHIFT] x1,000\n[CTRL] x1,000,000")))
                .size(20, 20)
                .pos(this.width / 2 + 40 - 10, buttonY)
                .build();

        Button removeTen = Button.builder(
                        Component.literal("10").withStyle(ChatFormatting.RED),
                        (onPress) -> {addAmountToBox(-10);}
                )
                .tooltip(Tooltip.create(Component.literal("Withdraw 10 stored experience points.\n[SHIFT] x1,000\n[CTRL] x1,000,000")))
                .size(20, 20)
                .pos(this.width / 2 - 40 - 10, buttonY)
                .build();

        Button addHundred = Button.builder(
                        Component.literal("100").withStyle(ChatFormatting.GREEN),
                        (onPress) -> {addAmountToBox(100);}
                )
                .tooltip(Tooltip.create(Component.literal("Deposit 100 carried experience points.\n[SHIFT] x1,000\n[CTRL] x1,000,000")))
                .size(22, 20)
                .pos(this.width / 2 + 61 - 11, buttonY)
                .build();

        Button removeHundred = Button.builder(
                        Component.literal("100").withStyle(ChatFormatting.RED),
                        (onPress) -> {addAmountToBox(-100);}
                )
                .tooltip(Tooltip.create(Component.literal("Withdraw 100 stored experience points.\n[SHIFT] x1,000\n[CTRL] x1,000,000")))
                .size(22, 20)
                .pos(this.width / 2 - 61 - 11, buttonY)
                .build();

        Button addAll = Button.builder(
                        Component.literal(">>").withStyle(ChatFormatting.GREEN),
                        (onPress) -> {setBoxAmount(Long.MAX_VALUE);}
                )
                .tooltip(Tooltip.create(Component.literal("Deposit all carried experience points.")))
                .size(20, 20)
                .pos(this.width / 2 + 82 - 10, buttonY)
                .build();

        Button removeAll = Button.builder(
                        Component.literal("<<").withStyle(ChatFormatting.RED),
                        (onPress) -> {setBoxAmount(Long.MIN_VALUE);}
                )
                .tooltip(Tooltip.create(Component.literal("Withdraw all stored experience points.")))
                .size(20, 20)
                .pos(this.width / 2 - 82 - 10, buttonY)
                .build();

        Button confirmTransfer = Button.builder(
                        Component.literal("Transfer"),
                        (onPress) -> {
                            ArcaneAuctionsPacketHandler.INSTANCE.sendToServer(new ExperienceObeliskTransactionC2SPacket(menu.getBlockPos(), getAmountBoxValue()));
                            setBoxAmount(0);
                        }
                )
                .tooltip(Tooltip.create(Component.literal("Perform experience point transaction.")))
                .size(60, 20)
                .pos(this.width / 2 - 30, this.height / 2 + 30)
                .build();

        _amountBox = new EditBox(font, this.width / 2 - 30, this.height / 2 - 14, 60, 14, Component.literal(""));
        _amountBox.setHint(Component.literal("0"));
        _amountBox.setTooltip(Tooltip.create(Component.literal("Transaction amount\nNegative: Withdraw from obelisk to player.\nPositive: Deposit from player to obelisk.")));
        _amountBox.setFilter((val) -> {
            // Allow ""/"-"/"+" so a leading - sign can be added while editing
            if(val.isEmpty() || val.equals("-") || val.equals("+")) return true;

            // Otherwise only allow int-parseable strings
            try {
                long parsedVal = NumberFormatter.stringToLong(val);
            }
            catch (NumberFormatException e) {
                return false;
            }
            return true;
        });
        _amountBox.setResponder((val) -> {
            try {
                // Re-apply number formatting to ensure correct presentation
                long parsedVal = NumberFormatter.stringToLong(val);
                String formattedVal = NumberFormatter.longToString(parsedVal);

                if (!_amountBox.getValue().equals(formattedVal))
                    _amountBox.setValue(formattedVal);
            }
            catch (NumberFormatException e) {
                return;
            }
        });

        addRenderableWidget(reset);
        addRenderableWidget(addOne);
        addRenderableWidget(removeOne);
        addRenderableWidget(addTen);
        addRenderableWidget(removeTen);
        addRenderableWidget(addHundred);
        addRenderableWidget(removeHundred);
        addRenderableWidget(addAll);
        addRenderableWidget(removeAll);
        addRenderableWidget(confirmTransfer);

        addRenderableWidget(_amountBox);
    }

    private void initConfigurationWidgets() {
        ExperienceObeliskBlockEntity obeliskEntity = getBlockEntity();

        _depositPermissionButton = CycleButton.builder(ExperienceObeliskScreen::getCycleButtonLabel)
                .withValues(ExperienceObeliskBlockEntity.TransactionPermissions.values())
                .withInitialValue(obeliskEntity.getDepositPermissions())
                .create(this.width / 2 - 60, this.height / 2 - 20, 120, 20,
                        Component.literal("Deposit"),
                        this::onPermissionCycleButtonChange);

        _withdrawalPermissionButton = CycleButton.builder(ExperienceObeliskScreen::getCycleButtonLabel)
                .withValues(ExperienceObeliskBlockEntity.TransactionPermissions.values())
                .withInitialValue(obeliskEntity.getWithdrawPermissions())
                .create(this.width / 2 - 60, this.height / 2, 120, 20,
                        Component.literal("Withdraw"),
                        this::onPermissionCycleButtonChange);

        _logPermissionButton = CycleButton.builder(ExperienceObeliskScreen::getCycleButtonLabel)
                .withValues(ExperienceObeliskBlockEntity.TransactionPermissions.values())
                .withInitialValue(obeliskEntity.getDepositPermissions()) // TODO
                .create(this.width / 2 - 60, this.height / 2 + 20, 120, 20,
                        Component.literal("Log View"),
                        this::onPermissionCycleButtonChange);

        addRenderableWidget(_depositPermissionButton);
        addRenderableWidget(_withdrawalPermissionButton);
        addRenderableWidget(_logPermissionButton);
    }

    private static Component getCycleButtonLabel(ExperienceObeliskBlockEntity.TransactionPermissions value) {
        return Component.literal(value.name());
    }

    private void onPermissionCycleButtonChange(CycleButton<ExperienceObeliskBlockEntity.TransactionPermissions> cycleButton, ExperienceObeliskBlockEntity.TransactionPermissions value) {
        if (_depositPermissionButton == null || _withdrawalPermissionButton == null || _logPermissionButton == null) {
            return;
        }

        // Send permission update packet to server
        ArcaneAuctionsPacketHandler.INSTANCE.sendToServer(
                new ExperienceObeliskPermissionUpdateC2SPacket(
                        menu.getBlockPos(),
                        _withdrawalPermissionButton.getValue(),
                        _depositPermissionButton.getValue(),
                        _logPermissionButton.getValue())
        );
    }

    private void initLogWidgets() {

    }
}
