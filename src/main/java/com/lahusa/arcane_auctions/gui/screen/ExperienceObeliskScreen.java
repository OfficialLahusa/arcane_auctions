package com.lahusa.arcane_auctions.gui.screen;

import com.lahusa.arcane_auctions.ArcaneAuctions;
import com.lahusa.arcane_auctions.block.entity.ExperienceObeliskBlockEntity;
import com.lahusa.arcane_auctions.gui.menu.ExperienceObeliskMenu;
import com.lahusa.arcane_auctions.net.ArcaneAuctionsPacketHandler;
import com.lahusa.arcane_auctions.net.ExperienceObeliskTransactionC2SPacket;
import com.lahusa.arcane_auctions.util.ExperienceConverter;
import com.lahusa.arcane_auctions.util.NumberFormatter;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.commands.ExperienceCommand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.awt.font.NumericShaper;
import java.util.ArrayList;
import java.util.List;

public class ExperienceObeliskScreen extends AbstractContainerScreen<ExperienceObeliskMenu> {
    private static final ResourceLocation BACKGROUND_LOCATION = ResourceLocation.fromNamespaceAndPath(ArcaneAuctions.MODID, "textures/gui/experience_obelisk.png");
    private static final ResourceLocation XP_OVERLAY_LOCATION = ResourceLocation.fromNamespaceAndPath(ArcaneAuctions.MODID, "textures/gui/xp_overlay.png");

    private final Inventory _inventory;
    private final Level _clientLevel;
    private final List<Button> _buttons = new ArrayList<>();
    private EditBox _amountBox;

    public ExperienceObeliskScreen(ExperienceObeliskMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 200;
        this.imageHeight = 116;
        this._clientLevel = menu.level;
        this._inventory = inv;
    }

    @Override
    protected void init() {
        super.init();
        initWidgets();
    }

    @Override
    protected void renderBg(GuiGraphics gfx, float p_97788_, int p_97789_, int p_97790_) {
        Minecraft instance = Minecraft.getInstance();

        BlockPos pos = menu.getBlockPos();
        int xpPoints = 0;
        if (pos != null && _clientLevel.getBlockEntity(pos) instanceof ExperienceObeliskBlockEntity obeliskEntity) {
            xpPoints = obeliskEntity.getExperiencePoints();
        }

        // Draw unselected tabs
        int tabOffsetY = (this.imageHeight - 3*26) / 2;
        int selectedTab = xpPoints % 3;
        for (int tabIdx = 0; tabIdx < 3; tabIdx++) {
            if(tabIdx == selectedTab) continue;
            gfx.blit(BACKGROUND_LOCATION, this.leftPos-32+4, this.topPos+tabOffsetY+tabIdx*26, 0, 116, 32, 26);
            gfx.renderItem(getTabIconItem(tabIdx).getDefaultInstance(), this.leftPos-32+4+8, this.topPos+tabOffsetY+tabIdx*26+5);
        }

        // Draw main bg
        gfx.blit(BACKGROUND_LOCATION, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        // Draw selected tab
        gfx.blit(BACKGROUND_LOCATION, this.leftPos-32+4, this.topPos+tabOffsetY+selectedTab*26, 32, 116, 32, 26);
        gfx.renderItem(getTabIconItem(selectedTab).getDefaultInstance(), this.leftPos-32+4+8, this.topPos+tabOffsetY+selectedTab*26+5);
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics gfx, int mouseX, int mouseY) {
        //super.renderLabels(gfx, p_282681_, p_283686_);
        //p_281635_.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x404040, false);
        //p_281635_.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x404040, false);

        // Reset buttons
        for(Button button : _buttons) {
            button.setFocused(false);
        }

        Minecraft instance = Minecraft.getInstance();

        BlockPos pos = menu.getBlockPos();
        int xpPoints = 0;
        if (pos != null && _clientLevel.getBlockEntity(pos) instanceof ExperienceObeliskBlockEntity obeliskEntity) {
            xpPoints = obeliskEntity.getExperiencePoints();
        }

        String titleText = this.title.getString();
        gfx.drawString(this.font, titleText, this.imageWidth / 2 - this.font.width(titleText) / 2, this.titleLabelY, 0x404040, false);

        String xpText = NumberFormatter.intToString(xpPoints) + " stored";
        gfx.blit(XP_OVERLAY_LOCATION, this.imageWidth / 2 - (this.font.width(xpText) + 8) / 2, 6+20, 0, 0, 0,10, 10, 10, 10);
        gfx.drawString(instance.font, xpText, this.imageWidth / 2 - (this.font.width(xpText) + 8) / 2 + 8, 6+20, 0x80FF20);

        gfx.drawString(instance.font, "Deposit", this.imageWidth / 2 + 60 - (this.font.width("Deposit")) / 2, 30 + this.imageHeight / 2, 0x404040, false);
        gfx.drawString(instance.font, "Withdraw", this.imageWidth / 2 - 60 - (this.font.width("Withdraw")) / 2, 30 + this.imageHeight / 2, 0x404040, false);
    }

    private Item getTabIconItem(int tabIdx) {
        return switch (tabIdx) {
            case 0 -> Items.EXPERIENCE_BOTTLE;
            case 1 -> Items.ENDER_EYE;
            case 2 -> Items.BOOK;
            default -> throw new IllegalArgumentException("tabIdx must be in range [0-2].");
        };
    }

    @Override
    protected void containerTick() {
        super.containerTick();
    }

    private void addAmountToBox(int value) {
        int prevValue = getAmountBoxValue();

        int multiplier = 1;

        // TODO: Shift for 1k multiplier
        if (hasShiftDown()) multiplier = 1000;
        if (hasControlDown()) multiplier = 1000000;

        prevValue += multiplier * value;

        _amountBox.setValue(NumberFormatter.intToString(prevValue));

        clampBoxAmount();
    }

    private void setBoxAmount(int value) {
        _amountBox.setValue(NumberFormatter.intToString(value));

        clampBoxAmount();
    }

    private void clampBoxAmount() {
        // Determine min value by checking orbs stored in obelisk
        int minValue = 0;

        BlockPos pos = menu.getBlockPos();
        if (pos != null && _clientLevel.getBlockEntity(pos) instanceof ExperienceObeliskBlockEntity obeliskEntity) {
            minValue = -obeliskEntity.getExperiencePoints();
        }

        // Determine max value by checking orbs stored in player
        Player player = _inventory.player;
        int maxValue = ExperienceConverter.getTotalCurrentXPPoints(player.experienceLevel, player.experienceProgress);

        // Clamp value in amount box to range
        int prevValue = getAmountBoxValue();

        //System.out.println("Min: " + minValue + ", Max: " + maxValue + ", Prev: " + prevValue + " (\"" + _amountBox.getValue() + ")\"");

        prevValue = Math.max(Math.min(prevValue, maxValue), minValue);

        _amountBox.setValue(NumberFormatter.intToString(prevValue));
        _amountBox.moveCursorToStart();
    }

    private int getAmountBoxValue() {
        int prevValue;

        try {
            prevValue = NumberFormatter.stringToInt(_amountBox.getValue());
        }
        catch (NumberFormatException e) {
            prevValue = 0;
        }

        return prevValue;
    }

    private void initWidgets() {
        _buttons.clear();

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
                        (onPress) -> {setBoxAmount(Integer.MAX_VALUE);}
                )
                .tooltip(Tooltip.create(Component.literal("Deposit all carried experience points.")))
                .size(20, 20)
                .pos(this.width / 2 + 82 - 10, buttonY)
                .build();

        Button removeAll = Button.builder(
                        Component.literal("<<").withStyle(ChatFormatting.RED),
                        (onPress) -> {setBoxAmount(Integer.MIN_VALUE);}
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
                int parsedVal = NumberFormatter.stringToInt(val);
            }
            catch (NumberFormatException e) {
                return false;
            }
            return true;
        });
        _amountBox.setResponder((val) -> {
            try {
                // Re-apply number formatting to ensure correct presentation
                int parsedVal = NumberFormatter.stringToInt(val);
                String formattedVal = NumberFormatter.intToString(parsedVal);

                if (!_amountBox.getValue().equals(formattedVal))
                    _amountBox.setValue(formattedVal);
            }
            catch (NumberFormatException e) {
                return;
            }
        });

        _buttons.add(reset);
        _buttons.add(addOne);
        _buttons.add(removeOne);
        _buttons.add(addTen);
        _buttons.add(removeTen);
        _buttons.add(addHundred);
        _buttons.add(removeHundred);
        _buttons.add(addAll);
        _buttons.add(removeAll);
        _buttons.add(confirmTransfer);

        for(Button button : _buttons) {
            addRenderableWidget(button);
        }

        addRenderableWidget(_amountBox);
    }
}
