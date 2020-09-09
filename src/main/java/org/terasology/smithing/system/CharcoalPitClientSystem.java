// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.smithing.system;

import org.terasology.engine.core.Time;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.engine.particles.components.ParticleEmitterComponent;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.nui.NUIManager;
import org.terasology.smithing.component.CharcoalPitComponent;
import org.terasology.smithing.event.OpenCharcoalPitRequest;
import org.terasology.smithing.ui.UICharcoalPit;

@RegisterSystem(value = RegisterMode.CLIENT)
public class CharcoalPitClientSystem extends BaseComponentSystem implements UpdateSubscriberSystem {
    @In
    private NUIManager nuiManager;
    @In
    private EntityManager entityManager;
    @In
    private Time time;

    private long lastUpdate;

    @Override
    public void initialise() {
    }

    @Override
    public void update(float delta) {
        long gameTimeInMs = time.getGameTimeInMs();
        if (gameTimeInMs > lastUpdate + 250) {
            for (EntityRef charcoalPit : entityManager.getEntitiesWith(CharcoalPitComponent.class,
                    ParticleEmitterComponent.class)) {
                ParticleEmitterComponent particles = charcoalPit.getComponent(ParticleEmitterComponent.class);
                particles.particleSpawnsLeft += 5;
                charcoalPit.saveComponent(particles);
            }

            lastUpdate = gameTimeInMs;
        }
    }

    @ReceiveEvent
    public void openCharcoalPitWindow(OpenCharcoalPitRequest event, EntityRef charcoalPit) {
        UICharcoalPit uiCharcoalPit = nuiManager.pushScreen("Smithing:CharcoalPit", UICharcoalPit.class);
        uiCharcoalPit.setCharcoalPit(charcoalPit);
    }
}
