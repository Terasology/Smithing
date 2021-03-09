// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.smithing.system;

import com.google.common.base.Predicate;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.drops.grammar.DropGrammarComponent;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.inventory.InventoryComponent;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.registry.In;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.engine.world.block.BlockUri;
import org.terasology.multiBlock.Basic2DSizeFilter;
import org.terasology.multiBlock.Basic3DSizeFilter;
import org.terasology.multiBlock.BlockUriEntityFilter;
import org.terasology.multiBlock.MultiBlockCallback;
import org.terasology.multiBlock.MultiBlockFormRecipeRegistry;
import org.terasology.multiBlock.UniformBlockReplacementCallback;
import org.terasology.multiBlock.recipe.LayeredMultiBlockFormItemRecipe;
import org.terasology.multiBlock.recipe.SurroundMultiBlockFormItemRecipe;
import org.terasology.multiBlock.recipe.UniformMultiBlockFormItemRecipe;
import org.terasology.processing.system.AnyActivityFilter;
import org.terasology.processing.system.ToolTypeEntityFilter;
import org.terasology.processing.system.UseOnTopFilter;
import org.terasology.smithing.Smithing;
import org.terasology.smithing.component.CharcoalPitComponent;
import org.terasology.workstation.system.WorkstationRegistry;
import org.terasology.workstationCrafting.system.CraftingWorkstationProcessFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/*
* Establishes a system for registering new recipes
*/
@RegisterSystem
public class RegisterSmithingRecipes extends BaseComponentSystem {
    @In
    private WorkstationRegistry workstationRegistry;
    @In
    private MultiBlockFormRecipeRegistry multiBlockRecipeRegistry;
    @In
    private BlockManager blockManager;

    @Override
    public void initialise() {
        workstationRegistry.registerProcessFactory(Smithing.BASIC_SMITHING_PROCESS, new CraftingWorkstationProcessFactory());
        workstationRegistry.registerProcessFactory(Smithing.STANDARD_SMITHING_PROCESS, new CraftingWorkstationProcessFactory());

        addWorkstationRecipes();

        addMultiblockRecipes();
    }

    /*
    * Adds a new smithing recipe to the workstation
    */
    private void addWorkstationRecipes() {
        multiBlockRecipeRegistry.addMultiBlockFormItemRecipe(
                new UniformMultiBlockFormItemRecipe(
                        new ToolTypeEntityFilter("hammer"), new UseOnTopFilter(),
                        new BlockUriEntityFilter(new BlockUri("CoreAssets:CobbleStone")), new Basic3DSizeFilter(2, 1, 1, 1),
                        "Smithing:BasicSmithingStation",
                        new UniformBlockReplacementCallback<Void>(blockManager.getBlock("Smithing:BasicSmithingStation"))));
    }

    /*
    * Adds a new multiblock recipe
    */
    private void addMultiblockRecipes() {
        multiBlockRecipeRegistry.addMultiBlockFormItemRecipe(
                new SurroundMultiBlockFormItemRecipe(
                        new ToolTypeEntityFilter("hammer"), new BlockUriEntityFilter(new BlockUri("CoreAssets:Brick")),
                        new BlockUriEntityFilter(new BlockUri("Engine:Air")), new AllowableCharcoalPitSize(),
                        new AnyActivityFilter(), "Smithing:CharcoalPit", new CharcoalPitCallback()));

        final LayeredMultiBlockFormItemRecipe bloomeryRecipe = new LayeredMultiBlockFormItemRecipe(
                new ToolTypeEntityFilter("hammer"), new Basic2DSizeFilter(2, 2), new AnyActivityFilter(),
                "Smithing:Bloomery", null);
        bloomeryRecipe.addLayer(1, 1, new BlockUriEntityFilter(new BlockUri("Smithing:CopperStructure")));
        bloomeryRecipe.addLayer(2, 2, new BlockUriEntityFilter(new BlockUri("CoreAssets:Brick")));
        multiBlockRecipeRegistry.addMultiBlockFormItemRecipe(bloomeryRecipe);
    }
    
    /*
    * Creates the charcoal pit
    */
    private static final class CharcoalPitCallback implements MultiBlockCallback<Void> {
        @Override
        public Map<Vector3i, Block> getReplacementMap(BlockRegion region, Void designDetails) {
            BlockManager blockManager = CoreRegistry.get(BlockManager.class);
            Block brickBlock = blockManager.getBlock("CoreAssets:Brick");

            Vector3i min = region.getMin(new Vector3i());
            Vector3i max = region.getMax(new Vector3i());
            Vector3i size = region.getSize(new Vector3i());
            Vector3f center = region.center(new Vector3f());

            // Generate map of blocks
            Map<Vector3i, Block> result = new HashMap<>();

            // Fill up the non-top layer blocks
            BlockRegion nonTopLayer = new BlockRegion(min).setSize(size.x, size.y - 1, size.z);
            for (Vector3ic position : nonTopLayer) {
                result.put(new Vector3i(position), brickBlock);
            }

            // Fill up the internal blocks of top layer
            Block halfBlock = blockManager.getBlock("CoreAssets:Brick:Engine:HalfBlock");
            BlockRegion topLayerInternal = new BlockRegion(min).setSize(size.x, 1, size.z);
            for (Vector3ic position : topLayerInternal) {
                result.put(new Vector3i(position), halfBlock);
            }

            // Top layer sides
            for (int x = min.x() + 1; x < max.x(); x++) {
                result.put(new Vector3i(x, max.y(), min.z()), blockManager.getBlock("CoreAssets:Brick:Engine:HalfSlope.FRONT"));
                result.put(new Vector3i(x, max.y(), max.z()), blockManager.getBlock("CoreAssets:Brick:Engine:HalfSlope.BACK"));
            }
            for (int z = min.z() + 1; z < max.z(); z++) {
                result.put(new Vector3i(min.x(), max.y(), z), blockManager.getBlock("CoreAssets:Brick:Engine:HalfSlope.LEFT"));
                result.put(new Vector3i(max.x(), max.y(), z), blockManager.getBlock("CoreAssets:Brick:Engine:HalfSlope.RIGHT"));
            }

            // Top layer corners
            result.put(new Vector3i(min.x(), max.y(), min.z()), blockManager.getBlock("CoreAssets:Brick:Engine:HalfSlopeCorner.LEFT"));
            result.put(new Vector3i(max.x(), max.y(), max.z()), blockManager.getBlock("CoreAssets:Brick:Engine:HalfSlopeCorner.RIGHT"));
            result.put(new Vector3i(min.x(), max.y(), max.z()), blockManager.getBlock("CoreAssets:Brick:Engine:HalfSlopeCorner.BACK"));
            result.put(new Vector3i(max.x(), max.y(), min.z()), blockManager.getBlock("CoreAssets:Brick:Engine:HalfSlopeCorner.FRONT"));

            // Chimney
            result.put(new Vector3i((int) Math.ceil(center.x()), max.y(), (int) Math.ceil(center.z())), blockManager.getBlock("CoreAssets:Brick:StructuralResources:PillarBase"));

            return result;
        }

        @Override
        public void multiBlockFormed(BlockRegion region, EntityRef entity, Void designDetails) {
            Vector3i size = region.getSize(new Vector3i());
            int airBlockCount = (size.x - 2) * (size.y - 2) * (size.z - 2);

            // Setup minimum and maximum log count based on size of the multi-block
            CharcoalPitComponent charcoalPit = new CharcoalPitComponent();
            charcoalPit.minimumLogCount = 8 * airBlockCount;
            charcoalPit.maximumLogCount = 16 * airBlockCount;
            charcoalPit.inputSlotCount = airBlockCount;
            charcoalPit.outputSlotCount = airBlockCount;
            entity.addComponent(charcoalPit);

            // Setup inventory size based on size of the multi-block
            InventoryComponent inventory = new InventoryComponent(airBlockCount * 2);
            inventory.privateToOwner = false;
            entity.addComponent(inventory);

            // We drop CobbleStone equal to what was used minus top layer (it is rendered unusable in the process)
            int cobbleStoneCount = 2 * (size.x + size.z - 2) * (size.y - 1) + (size.x - 2) * (size.z - 2);

            DropGrammarComponent drop = new DropGrammarComponent();
            drop.blockDrops = Arrays.asList(cobbleStoneCount + "*CoreAssets:Brick");
            entity.addComponent(drop);
        }
    }

    /*
    * Defines the acceptable charcoal pit size
    */
    private static final class AllowableCharcoalPitSize implements Predicate<Vector3i> {
        @Override
        public boolean apply(Vector3i value) {
            // Minimum size 3x3x3
            return (value.x >= 3 && value.y >= 3 && value.z >= 3
                    // X and Z are odd to allow finding center block
                    && value.x % 2 == 1 && value.z % 2 == 1);
        }
    }
}
