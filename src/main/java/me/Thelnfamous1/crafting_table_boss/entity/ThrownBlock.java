package me.Thelnfamous1.crafting_table_boss.entity;

import me.Thelnfamous1.crafting_table_boss.CraftingTableBoss;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;

public class ThrownBlock extends AbstractHurtingProjectile implements IEntityAdditionalSpawnData {
    private static final String BLOCK_STATE_TAG_KEY = "BlockState";
    protected static final EntityDataAccessor<BlockPos> DATA_START_POS = SynchedEntityData.defineId(ThrownBlock.class, EntityDataSerializers.BLOCK_POS);
    public static final double VELOCITY_SCALE_FACTOR = 8000.0D;

    private BlockState blockState = Blocks.CRAFTING_TABLE.defaultBlockState();

    public ThrownBlock(EntityType<? extends ThrownBlock> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public ThrownBlock(Level pLevel, LivingEntity pShooter, double pOffsetX, double pOffsetY, double pOffsetZ, BlockState pState) {
        super(CraftingTableBoss.THROWN_BLOCK.get(), pShooter, pOffsetX, pOffsetY, pOffsetZ, pLevel);
        this.blockState = pState;
        this.setStartPos(this.blockPosition());
    }

    public void setStartPos(BlockPos pStartPos) {
        this.entityData.set(DATA_START_POS, pStartPos);
    }

    public BlockPos getStartPos() {
        return this.entityData.get(DATA_START_POS);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_START_POS, BlockPos.ZERO);
    }

    @Override
    protected void onHit(HitResult pResult) {
        super.onHit(pResult);
        this.playSound(this.blockState.getSoundType().getHitSound());
    }

    @Override
    protected void onHitEntity(EntityHitResult pResult) {
        super.onHitEntity(pResult);
        if (!this.level.isClientSide) {
            Entity hitEntity = pResult.getEntity();
            Entity owner = this.getOwner();
            hitEntity.hurt(DamageSource.thrown(this, owner), 8.0F);
        }
    }

    @Override
    public void tick() {
        if (this.blockState.isAir()) {
            this.discard();
        } else{
            super.tick();
        }
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        return false;
    }

    @Override
    protected boolean shouldBurn() {
        return false;
    }

    public BlockState getBlockState() {
        return this.blockState;
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.put(BLOCK_STATE_TAG_KEY, NbtUtils.writeBlockState(this.blockState));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        this.blockState = NbtUtils.readBlockState(pCompound.getCompound(BLOCK_STATE_TAG_KEY));

        if (this.blockState.isAir()) {
            this.blockState = Blocks.CRAFTING_TABLE.defaultBlockState();
        }
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        Entity entity = this.getOwner();
        int ownerId = entity == null ? 0 : entity.getId();
        buffer.writeVarInt(ownerId);
        buffer.writeVarInt(Block.getId(this.getBlockState()));
        buffer.writeShort((int)(Mth.clamp(this.xPower, -3.9D, 3.9D) * VELOCITY_SCALE_FACTOR));
        buffer.writeShort((int)(Mth.clamp(this.yPower, -3.9D, 3.9D) * VELOCITY_SCALE_FACTOR));
        buffer.writeShort((int)(Mth.clamp(this.zPower, -3.9D, 3.9D) * VELOCITY_SCALE_FACTOR));
    }

    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {
        Entity owner = this.level.getEntity(additionalData.readVarInt());
        if (owner != null) {
            this.setOwner(owner);
        }
        this.blockState = Block.stateById(additionalData.readVarInt());
        double xDelta = additionalData.readShort() / VELOCITY_SCALE_FACTOR;
        double yDelta = additionalData.readShort() / VELOCITY_SCALE_FACTOR;
        double zDelta = additionalData.readShort() / VELOCITY_SCALE_FACTOR;
        double d3 = Math.sqrt(xDelta * xDelta + yDelta * yDelta + zDelta * zDelta);
        if (d3 != 0.0D) {
            this.xPower = xDelta / d3 * 0.1D;
            this.yPower = yDelta / d3 * 0.1D;
            this.zPower = zDelta / d3 * 0.1D;
        }
        this.setStartPos(this.blockPosition());
    }
}
