package com.ilcheese2.bubblelife.bubbles;

import com.ilcheese2.bubblelife.DetachedTimes;
import com.ilcheese2.bubblelife.DetachedTimesAttachments;
import com.ilcheese2.bubblelife.DetachedTimesConfig;
import com.ilcheese2.bubblelife.client.BubbleControllerClient;
import com.ilcheese2.bubblelife.mixin.LevelAccessor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Bubble extends Entity {

    public List<Entity> entities = new ArrayList<>();
    public ItemStack item;
    public BubbleInfo info;
    @Nullable
    public UUID owner;
    private int ticksLeft;
    private static final EntityDataAccessor<BubbleInfo> BUBBLE_ITEM_ID = SynchedEntityData.defineId(Bubble.class, DetachedTimes.BUBBLE_INFO_SERIALIZER);

    public Bubble(Level level, BubbleInfo info, Player owner) {
        super(DetachedTimes.BUBBLE.get(), level);
        this.info = info;
        refreshDimensions();
        this.setBoundingBox(makeBoundingBox());
        if (owner != null) {
            this.owner = owner.getUUID();
        }
        ticksLeft = DetachedTimesConfig.BUBBLE_DURATION.get();
        if (!level.isClientSide) {
            this.entityData.set(BUBBLE_ITEM_ID, info);
        }
    }

    public Bubble(Level level) {
        this(level, new BubbleInfo(), null);
    }

    public Bubble(EntityType<Bubble> entityType, Level level) {
        this(level);
    }


    @Override
    public @NotNull EntityDimensions getDimensions(@NotNull Pose pose) {
        return EntityDimensions.fixed(2 * info.range(), 2 * info.range()); // range
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(BUBBLE_ITEM_ID, new BubbleInfo());
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (BUBBLE_ITEM_ID.id() == (key.id()) && level().isClientSide) {
            //this.info = this.getEntityData().get(BUBBLE_ITEM_ID).get(DetachedTimes.BUBBLE_INFO.get()) ;
            if (!this.info.equals(this.getEntityData().get(BUBBLE_ITEM_ID))) {
                this.info = this.getEntityData().get(BUBBLE_ITEM_ID);
                BubbleControllerClient.instance().updateShader();
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        ticksLeft--;

        if (!level().isClientSide) {
            if (ticksLeft <= 0 && false) {
                this.discard();
            }
        }
    }

    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag compound) {
        info = BubbleInfo.CODEC.parse(NbtOps.INSTANCE, compound.get("info")).result().orElse(new BubbleInfo());
        if (!level().isClientSide) {
            this.entityData.set(BUBBLE_ITEM_ID, info);
        }
        if (compound.contains("owner")) {
            owner = compound.getUUID("owner");
        }
        ticksLeft = compound.getInt("ticksLeft");
    }

    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag compound) {
        BubbleInfo.CODEC.encodeStart(NbtOps.INSTANCE, info).result().ifPresent(tag -> compound.put("info", tag));
        if (owner != null) {
            compound.putUUID("owner", owner);
        }
        compound.putInt("ticksLeft", ticksLeft);
    }

    @Override
    public void onAddedToLevel() {
        super.onAddedToLevel();
        BubbleController.addBubble(this, level().isClientSide);
    }

    @Override
    public void onClientRemoval() {
        BubbleController.removeBubble(this, level().isClientSide);
    }

    @Override
    public void remove(@NotNull RemovalReason reason) {
        super.remove(reason);
        BubbleController.removeBubble(this, level().isClientSide);
    }


    public void updateEntities() {
        if (!info.changesTime()) {
            entities.clear();
            return;
        }

        entities = this.level().getEntities(this, this.getBoundingBox(), entity -> {
            return !(entity instanceof Bubble) && !(entity instanceof Player && !DetachedTimesConfig.AFFECTS_PLAYERS.get()) && !(entity.getUUID().equals(owner)) && entity.distanceTo(this) <= info.range();
        });
    }

    private long lastMs;
    public float deltaTickResidual;

    public void resetCount(long time) {
        lastMs = time;
    }

    public void tickInsides(long time) {

        float deltaTicks = (float)(time - this.lastMs) / info.speed();
        lastMs = time;
        deltaTickResidual = deltaTickResidual + deltaTicks;
        int i = (int)deltaTickResidual;
        deltaTickResidual -= (float)i;

        List<TickingBlockEntity> blockEntities = null;

        if (i > 0) {
            var blockEntityList = ((LevelAccessor)level()).getBlockEntityTickers();
            blockEntities = blockEntityList.stream().filter((be) -> be.getPos().distToCenterSqr(getX(), getY(), getZ()) <= info.range() * info.range()).toList();
        }

        for (int j = 0; j < Math.min(10, i); j++) {
            updateEntities();
            blockEntities.forEach((tickingBlock) -> {
                if (tickingBlock.isRemoved()) {
                    return;
                }
                tickingBlock.tick();
            });
            for (Entity entity : entities) {

                if (distanceTo(entity) > BubbleController.checkInteraction(entity, this, level().isClientSide)) {
                    continue;
                }

                if (isRemoved()) {
                    BubbleController.removeBubble(this, level().isClientSide);
                    continue;
                }

                entity.setData(DetachedTimesAttachments.TICK_RESIDUAL, deltaTickResidual);
                entity.setOldPosAndRot();
                entity.tickCount++;
                entity.tick();
            }
        }
    }
}
