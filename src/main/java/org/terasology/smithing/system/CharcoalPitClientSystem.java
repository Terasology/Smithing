/*
 * Copyright 2016 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.smithing.system;

import org.terasology.engine.core.Time;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.engine.particles.components.ParticleEmitterComponent;
import org.terasology.engine.registry.In;
import org.terasology.engine.rendering.nui.NUIManager;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;
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
            for (EntityRef charcoalPit : entityManager.getEntitiesWith(CharcoalPitComponent.class, ParticleEmitterComponent.class)) {
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
