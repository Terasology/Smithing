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
package org.terasology.smithing.component;

import org.terasology.entitySystem.Component;
import org.terasology.network.Replicate;

public class CharcoalPitComponent implements Component {

    /** The time in milliseconds in-game when the charcoal pit begins to burn, otherwise holds the last time the charcoal pit began to burn */
    @Replicate
    public long burnStartWorldTime;

    /** The time in milliseconds in-game when the charcoal pit stops burning, otherwise holds the last time the charcoal pit finished burning */
    @Replicate
    public long burnFinishWorldTime;

    /** Minimum number of logs */
    @Replicate
    public int minimumLogCount;

    /** Maximum number of logs */
    @Replicate
    public int maximumLogCount;

    /** Number of input slots for fuel */
    @Replicate
    public int inputSlotCount;

    /** Number of output slots for charcoal */
    @Replicate
    public int outputSlotCount;
}
