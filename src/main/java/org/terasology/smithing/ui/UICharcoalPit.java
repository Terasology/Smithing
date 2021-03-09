// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.smithing.ui;

import org.terasology.engine.core.Time;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.rendering.nui.CoreScreenLayer;
import org.terasology.engine.rendering.nui.NUIManager;
import org.terasology.engine.rendering.nui.layers.ingame.inventory.InventoryGrid;
import org.terasology.nui.UIWidget;
import org.terasology.nui.widgets.ActivateEventListener;
import org.terasology.nui.widgets.UIButton;
import org.terasology.nui.widgets.UILoadBar;
import org.terasology.smithing.component.CharcoalPitComponent;
import org.terasology.smithing.event.ProduceCharcoalRequest;
import org.terasology.smithing.system.CharcoalPitUtils;

public class UICharcoalPit extends CoreScreenLayer {
    private EntityRef charcoalPitEntity;
    private InventoryGrid input;
    private InventoryGrid output;
    private UIButton process;
    private UILoadBar burningProgress;

    @Override
    public void initialise() {
        input = find("input", InventoryGrid.class);
        output = find("output", InventoryGrid.class);

        InventoryGrid player = find("player", InventoryGrid.class);
        player.setTargetEntity(CoreRegistry.get(LocalPlayer.class).getCharacterEntity());
        player.setCellOffset(10);
        player.setMaxCellCount(30);

        process = find("process", UIButton.class);
        burningProgress = find("burningProgress", UILoadBar.class);
    }

    public void setCharcoalPit(final EntityRef entity) {
        this.charcoalPitEntity = entity;

        CharcoalPitComponent charcoalPit = entity.getComponent(CharcoalPitComponent.class);

        input.setTargetEntity(entity);
        input.setCellOffset(0);
        input.setMaxCellCount(charcoalPit.inputSlotCount);

        output.setTargetEntity(entity);
        output.setCellOffset(charcoalPit.inputSlotCount);
        output.setMaxCellCount(charcoalPit.outputSlotCount);

        process.setText("To Charcoal");
        process.subscribe(
                new ActivateEventListener() {
                    @Override
                    public void onActivated(UIWidget widget) {
                        entity.send(new ProduceCharcoalRequest());
                    }
                });
    }

    @Override
    public void update(float delta) {
        if (!charcoalPitEntity.exists()) {
            CoreRegistry.get(NUIManager.class).closeScreen(this);
            return;
        }

        super.update(delta);

        long worldTime = CoreRegistry.get(Time.class).getGameTimeInMs();

        CharcoalPitComponent charcoalPit = charcoalPitEntity.getComponent(CharcoalPitComponent.class);
        if (charcoalPit.burnFinishWorldTime > worldTime) {
            // It's burning wood now
            input.setVisible(false);
            process.setVisible(false);
            output.setVisible(false);
            burningProgress.setVisible(true);
            burningProgress.setValue(1f * (worldTime - charcoalPit.burnStartWorldTime) / (charcoalPit.burnFinishWorldTime - charcoalPit.burnStartWorldTime));
        } else {
            // It's not burning wood
            input.setVisible(true);
            output.setVisible(true);
            burningProgress.setVisible(false);

            int logCount = CharcoalPitUtils.getLogCount(charcoalPitEntity);

            process.setVisible(CharcoalPitUtils.canBurnCharcoal(logCount, charcoalPitEntity));
        }
    }

    @Override
    public boolean isModal() {
        return false;
    }
}
