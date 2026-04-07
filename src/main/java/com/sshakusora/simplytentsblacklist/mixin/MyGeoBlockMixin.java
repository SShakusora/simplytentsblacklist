package com.sshakusora.simplytentsblacklist.mixin;

import com.sappyeddie.simplytents.tent.block.MyGeoBlock;
import com.sshakusora.simplytentsblacklist.config.BlacklistConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(MyGeoBlock.class)
public class MyGeoBlockMixin {

    // 用于捕获当前处理的实体
    private static Entity currentEntity = null;

    // 用于捕获当前处理的方块状态和位置
    private static BlockState currentBlockState = null;
    private static BlockPos currentBlockPos = null;
    private static BlockEntity currentBlockEntity = null;

    /**
     * 检查方块是否在黑名单中（包括方块ID和方块Tag）
     */
    private static boolean isBlockBlacklisted(BlockState state) {
        if (state == null) return false;
        Block block = state.getBlock();
        ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(block);
        if (blockId == null) return false;
        String blockName = blockId.toString();

        // 检查方块ID黑名单
        if (BlacklistConfig.BLACKLIST_BLOCKS.get().contains(blockName)) {
            return true;
        }

        // 检查方块Tag黑名单
        for (String tagString : BlacklistConfig.BLACKLIST_BLOCK_TAGS.get()) {
            if (tagString.startsWith("#")) {
                tagString = tagString.substring(1);
            }
            ResourceLocation tagId = ResourceLocation.tryParse(tagString);
            if (tagId != null) {
                TagKey<Block> tagKey = TagKey.create(Registries.BLOCK, tagId);
                if (state.is(tagKey)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 检查实体的NBT中是否包含黑名单物品
     */
    private static boolean entityContainsBlacklistedItem(Entity entity) {
        if (entity == null) return false;
        CompoundTag entityTag = new CompoundTag();
        entity.save(entityTag);
        return containsBlacklistedItem(entityTag);
    }

    /**
     * 检查方块实体的NBT中是否包含黑名单物品
     */
    private static boolean blockEntityContainsBlacklistedItem(BlockEntity blockEntity) {
        if (blockEntity == null) return false;
        CompoundTag beTag = blockEntity.saveWithFullMetadata();
        return containsBlacklistedItem(beTag);
    }

    /**
     * 检查物品是否在黑名单物品Tag中
     */
    private static boolean isItemInBlacklistTag(String itemId) {
        if (itemId == null || itemId.isEmpty()) return false;

        Item item = ForgeRegistries.ITEMS.getValue(ResourceLocation.tryParse(itemId));
        if (item == null) return false;

        ItemStack stack = new ItemStack(item);

        for (String tagString : BlacklistConfig.BLACKLIST_ITEM_TAGS.get()) {
            if (tagString.startsWith("#")) {
                tagString = tagString.substring(1);
            }
            ResourceLocation tagId = ResourceLocation.tryParse(tagString);
            if (tagId != null) {
                TagKey<Item> tagKey = TagKey.create(Registries.ITEM, tagId);
                if (stack.is(tagKey)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 递归检查NBT中是否包含黑名单物品（包括物品ID和物品Tag）
     */
    private static boolean containsBlacklistedItem(CompoundTag tag) {
        if (tag == null) return false;

        List<String> blacklistItems = BlacklistConfig.BLACKLIST_ITEMS.get();

        // 检查id字段（物品ID）
        if (tag.contains("id")) {
            String id = tag.getString("id");
            // 检查物品ID黑名单
            if (blacklistItems.contains(id)) {
                return true;
            }
            // 检查物品Tag黑名单
            if (isItemInBlacklistTag(id)) {
                return true;
            }
        }

        // 检查Item字段（物品ID）
        if (tag.contains("Item")) {
            Tag itemTag = tag.get("Item");
            if (itemTag instanceof CompoundTag itemCompound) {
                if (itemCompound.contains("id")) {
                    String itemId = itemCompound.getString("id");
                    // 检查物品ID黑名单
                    if (blacklistItems.contains(itemId)) {
                        return true;
                    }
                    // 检查物品Tag黑名单
                    if (isItemInBlacklistTag(itemId)) {
                        return true;
                    }
                }
            }
        }

        // 递归检查所有子标签
        for (String key : tag.getAllKeys()) {
            Tag subTag = tag.get(key);
            if (subTag instanceof CompoundTag subCompound) {
                if (containsBlacklistedItem(subCompound)) {
                    return true;
                }
            } else if (subTag instanceof ListTag subList) {
                for (Tag listItem : subList) {
                    if (listItem instanceof CompoundTag listCompound) {
                        if (containsBlacklistedItem(listCompound)) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    /**
     * 捕获当前正在保存的实体
     */
    @Redirect(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;saveAsPassenger(Lnet/minecraft/nbt/CompoundTag;)Z"))
    private boolean captureEntity(Entity entity, CompoundTag tag) {
        currentEntity = entity;
        return entity.saveAsPassenger(tag);
    }

    /**
     * 拦截实体列表的add操作，检查黑名单
     * 如果实体包含黑名单物品，则不添加到列表（不收集该实体）
     */
    @Redirect(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/ListTag;add(Ljava/lang/Object;)Z", ordinal = 0))
    private boolean redirectEntityListAdd(ListTag list, Object tag) {
        // 检查实体是否包含黑名单物品
        if (currentEntity != null && entityContainsBlacklistedItem(currentEntity)) {
            // 实体包含黑名单物品，不添加到列表，保留在世界中
            currentEntity = null;
            return false; // 返回false表示未添加
        }

        // 实体不在黑名单中，正常添加
        currentEntity = null;
        return list.add((Tag) tag);
    }

    /**
     * 捕获当前正在处理的方块状态
     */
    @Redirect(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"))
    private BlockState captureBlockState(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        currentBlockState = state;
        currentBlockPos = pos;
        return state;
    }

    /**
     * 捕获当前正在处理的方块实体
     */
    @Redirect(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getBlockEntity(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/entity/BlockEntity;"))
    private BlockEntity captureBlockEntity(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        currentBlockEntity = be;
        return be;
    }

    /**
     * 拦截 removeBlockEntity 调用，防止黑名单方块的实体被提前移除
     */
    @Redirect(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;removeBlockEntity(Lnet/minecraft/core/BlockPos;)V"))
    private void redirectRemoveBlockEntity(Level level, BlockPos pos) {
        // 如果是黑名单方块或包含黑名单物品的方块实体，不移除方块实体
        if (currentBlockState != null && isBlockBlacklisted(currentBlockState)) {
            return;
        }
        if (currentBlockEntity != null && blockEntityContainsBlacklistedItem(currentBlockEntity)) {
            return;
        }
        level.removeBlockEntity(pos);
    }

    /**
     * 拦截方块列表的add操作，检查黑名单
     * 如果方块是黑名单方块，或方块实体包含黑名单物品，则不添加到列表
     */
    @Redirect(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/ListTag;add(Ljava/lang/Object;)Z", ordinal = 1))
    private boolean redirectBlockListAdd(ListTag list, Object tag) {
        // 检查方块是否在黑名单中
        if (currentBlockState != null && isBlockBlacklisted(currentBlockState)) {
            return false; // 返回false表示未添加
        }

        // 检查方块实体是否包含黑名单物品
        if (currentBlockEntity != null && blockEntityContainsBlacklistedItem(currentBlockEntity)) {
            return false;
        }
        return list.add((Tag) tag);
    }

    /**
     * 拦截blocksToRemove的add操作，确保黑名单方块不被添加到移除列表
     */
    @Redirect(method = "use", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 0))
    private boolean redirectBlocksToRemoveAdd(List<BlockPos> list, Object obj) {
        // 如果是黑名单方块或包含黑名单物品的方块实体，不添加到移除列表
        if (currentBlockState != null && isBlockBlacklisted(currentBlockState)) {
            currentBlockState = null;
            currentBlockPos = null;
            currentBlockEntity = null;
            return false;
        }
        if (currentBlockEntity != null && blockEntityContainsBlacklistedItem(currentBlockEntity)) {
            currentBlockState = null;
            currentBlockPos = null;
            currentBlockEntity = null;
            return false;
        }
        currentBlockState = null;
        currentBlockPos = null;
        currentBlockEntity = null;
        return list.add((BlockPos) obj);
    }
}
