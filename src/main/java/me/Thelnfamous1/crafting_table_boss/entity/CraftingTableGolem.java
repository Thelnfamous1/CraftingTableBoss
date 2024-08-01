package me.Thelnfamous1.crafting_table_boss.entity;

import me.Thelnfamous1.crafting_table_boss.CraftingTableBoss;
import me.Thelnfamous1.crafting_table_boss.mixin.MeleeAttackGoalAccessor;
import me.Thelnfamous1.crafting_table_boss.mixin.RangedAttackGoalAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.builder.ILoopType;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

public class CraftingTableGolem extends Monster implements IAnimatable, RangedAttackMob {
    private static final EntityDataAccessor<Byte> ACTIVE_ATTACK_TYPE = SynchedEntityData.defineId(CraftingTableGolem.class, EntityDataSerializers.BYTE);
    private static final AnimationBuilder WALK_ANIM = new AnimationBuilder().addAnimation("animation.CraftingTableGolem.walk", ILoopType.EDefaultLoopTypes.LOOP);
    private static final AnimationBuilder IDLE_ANIM = new AnimationBuilder().addAnimation("animation.CraftingTableGolem.idle", ILoopType.EDefaultLoopTypes.LOOP);
    private static final AnimationBuilder ATTACK_ANIM = new AnimationBuilder().addAnimation("animation.CraftingTableGolem.attack", ILoopType.EDefaultLoopTypes.PLAY_ONCE);

    private final AnimationFactory animationFactory = GeckoLibUtil.createFactory(this);
    private final ServerBossEvent bossEvent = (ServerBossEvent)(new ServerBossEvent(this.getDisplayName(), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS)).setDarkenScreen(true);
    private int attackTicker;

    public CraftingTableGolem(EntityType<? extends CraftingTableGolem> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.setHealth(this.getMaxHealth());
        this.xpReward = 50;
        this.getNavigation().setCanFloat(true);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new CTGMeleeAttackGoal(this, 1.0D, true, 10.0F));
        this.goalSelector.addGoal(1, new CTGRangedAttackGoal(this, 1.0D, 40, 20.0F, 10.0F));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0D, 1.0000001E-5F));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 0, false, false, LivingEntity::attackable));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ACTIVE_ATTACK_TYPE, (byte)AttackType.NONE.ordinal());
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> pKey) {
        super.onSyncedDataUpdated(pKey);
        if(ACTIVE_ATTACK_TYPE.equals(pKey)){
            this.attackTicker = 0;
        }
    }

    private void setActiveAttackType(AttackType attackType){
        this.entityData.set(ACTIVE_ATTACK_TYPE, (byte)attackType.ordinal());
    }

    private AttackType getActiveAttackType(){
        return AttackType.byOrdinal(this.entityData.get(ACTIVE_ATTACK_TYPE));
    }

    public static AttributeSupplier.Builder createCustomAttributes() {
        return createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 500.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3F)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.ATTACK_KNOCKBACK, 1.5D)
                .add(Attributes.ATTACK_DAMAGE, 30.0D);
    }

    @Override
    public void registerControllers(AnimationData animationData) {
        animationData.addAnimationController(new AnimationController<>(this, "controller", 0, this::selectAnimation));
    }

    private PlayState selectAnimation(AnimationEvent<CraftingTableGolem> event) {
        switch (this.getActiveAttackType()){
            case SMASH -> {
                event.getController().setAnimation(ATTACK_ANIM);
                return PlayState.CONTINUE;
            }
            case THROW -> {
                // no throw animation, just make it freeze
                return PlayState.STOP;
            }
            default -> {
                if(event.isMoving()){
                    event.getController().setAnimation(WALK_ANIM);
                } else{
                    event.getController().setAnimation(IDLE_ANIM);
                }
                return PlayState.CONTINUE;
            }
        }
    }

    @Override
    public AnimationFactory getFactory() {
        return this.animationFactory;
    }

    @Override
    public void baseTick() {
        super.baseTick();
        AttackType activeAttackType = this.getActiveAttackType();
        if(this.attackTicker < activeAttackType.getAttackDuration()){
            this.attackTicker++;
        }
        if(!this.level.isClientSide && this.attackTicker >= activeAttackType.getAttackDuration()) {
            if (activeAttackType != AttackType.NONE) {
                this.setActiveAttackType(AttackType.NONE);
            }
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        if (this.hasCustomName()) {
            this.bossEvent.setName(this.getDisplayName());
        }

    }

    @Override
    public void setCustomName(@Nullable Component pName) {
        super.setCustomName(pName);
        this.bossEvent.setName(this.getDisplayName());
    }

    @Override
    public void startSeenByPlayer(ServerPlayer pPlayer) {
        super.startSeenByPlayer(pPlayer);
        this.bossEvent.addPlayer(pPlayer);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer pPlayer) {
        super.stopSeenByPlayer(pPlayer);
        this.bossEvent.removePlayer(pPlayer);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();

        // boss stuff
        if (this.tickCount % 20 == 0) {
            this.heal(1.0F);
        }
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());

        // custom attack animation
        AttackType attackType = this.getActiveAttackType();
        if(attackType != AttackType.NONE && this.getActiveAttackType().getAttackPoint() == this.attackTicker){
            this.getActiveAttackType().performAttack(this);
        }
    }

    @Override
    public void makeStuckInBlock(BlockState pState, Vec3 pMotionMultiplier) {
    }

    @Override
    public boolean removeWhenFarAway(double pDistanceToClosestPlayer) {
        return false;
    }

    @Override
    protected float getSoundVolume() {
        return 4.0F;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return this.isAggressive() ? SoundEvents.WARDEN_ANGRY : SoundEvents.WARDEN_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return SoundEvents.WARDEN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WARDEN_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pPos, BlockState pState) {
        this.playSound(SoundEvents.WARDEN_STEP, 10.0F, 1.0F);
    }

    @Override
    protected boolean canRide(Entity pVehicle) {
        return false;
    }

    @Override
    public boolean canDisableShield() {
        return true;
    }

    @Override
    public float getWalkTargetValue(BlockPos pPos, LevelReader pLevel) {
        return 0.0F;
    }

    public void performSmashAttack(){
        AABB attackBox = this.createAttackBox();
        if(!this.level.isClientSide){
            List<LivingEntity> targets = this.level.getNearbyEntities(LivingEntity.class, TargetingConditions.DEFAULT, this, attackBox);
            targets.forEach(this::doHurtTarget);
        }
        if(!this.level.isClientSide && ForgeEventFactory.getMobGriefingEvent(this.level, this)){
            for(BlockPos blockPos : BlockPos.betweenClosed(Mth.floor(attackBox.minX), Mth.floor(attackBox.minY), Mth.floor(attackBox.minZ), Mth.floor(attackBox.maxX), Mth.floor(attackBox.maxY), Mth.floor(attackBox.maxZ))) {
                BlockState blockState = level.getBlockState(blockPos);
                if (!blockState.isAir() && CraftingTableGolem.canDestroy(blockState)) {
                    this.level.destroyBlock(blockPos, true, this);
                }
            }
        }
    }

    public static boolean canDestroy(BlockState blockState) {
        return !blockState.is(BlockTags.WITHER_IMMUNE) && !blockState.is(BlockTags.DRAGON_IMMUNE);
    }

    protected AABB createAttackBox() {
        double attackRadius = this.getBbWidth() * 0.5F;
        Vec3 baseOffset = new Vec3(0.0D, 0.0D, this.getBbWidth() * 0.5F).yRot(-this.getYHeadRot() * Mth.DEG_TO_RAD);
        Vec3 attackOffset = new Vec3(0.0D, 0.0D, attackRadius).yRot(-this.getYHeadRot() * Mth.DEG_TO_RAD);
        double attackSize = attackRadius * 2;
        return AABB.ofSize(this.position().add(0, attackRadius, 0).add(baseOffset).add(attackOffset), attackSize, attackSize, attackSize);
    }

    @Override
    public void performRangedAttack(LivingEntity pTarget, float pVelocity) {
        if (!this.isSilent()) {
            this.level.levelEvent(null, 1024, this.blockPosition(), 0);
        }
        double startX = this.getX();
        double startY = this.getEyeY() - CraftingTableBoss.THROWN_BLOCK.get().getHeight() * 0.5F;
        double startZ = this.getZ();
        double xDist = pTarget.getX() - startX;
        double yDist = (pTarget.getY() + (double)pTarget.getEyeHeight() * 0.5D) - startY;
        double zDist = pTarget.getZ() - startZ;
        ThrownBlock thrownBlock = new ThrownBlock(this.level, this, xDist, yDist, zDist, Blocks.CRAFTING_TABLE.defaultBlockState());
        thrownBlock.setPosRaw(startX, startY, startZ);
        this.level.addFreshEntity(thrownBlock);
    }

    public enum AttackType{
        NONE(0, 0, ctg -> {}),
        SMASH(Mth.floor(0.75F * 20), Mth.floor(1.5F * 20), CraftingTableGolem::performSmashAttack),
        THROW(0, 0, ctg -> {});

        private final int attackPoint;
        private final int attackDuration;
        private final Consumer<CraftingTableGolem> performAttack;

        AttackType(int attackPoint, int attackDuration, Consumer<CraftingTableGolem> performAttack) {
            this.attackPoint = attackPoint;
            this.attackDuration = attackDuration;
            this.performAttack = performAttack;
        }

        public int getAttackPoint() {
            return this.attackPoint;
        }

        public int getAttackDuration() {
            return this.attackDuration;
        }

        public void performAttack(CraftingTableGolem golem){
            this.performAttack.accept(golem);
        }

        public static AttackType byOrdinal(int pOrdinal) {
            if (pOrdinal < 0 || pOrdinal > values().length) {
                pOrdinal = 0;
            }

            return values()[pOrdinal];
        }
    }

    public static class CTGMeleeAttackGoal extends MeleeAttackGoal{

        private final CraftingTableGolem golem;
        private final float meleeAttackMaxDistSqr;

        public CTGMeleeAttackGoal(CraftingTableGolem pMob, double pSpeedModifier, boolean pFollowingTargetEvenIfNotSeen, float meleeAttackMaxDist) {
            super(pMob, pSpeedModifier, pFollowingTargetEvenIfNotSeen);
            this.golem = pMob;
            this.meleeAttackMaxDistSqr = meleeAttackMaxDist * meleeAttackMaxDist;
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.mob.getTarget();
            if(target != null && this.shouldUseMeleeAttacks(target)){
                return super.canUse();
            } else{
                return false;
            }
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = this.mob.getTarget();
            if(target != null && this.shouldUseMeleeAttacks(target)){
                return super.canContinueToUse();
            } else{
                return false;
            }
        }

        private boolean shouldUseMeleeAttacks(LivingEntity target){
            double distanceToTargetSqr = this.mob.distanceToSqr(target.getX(), target.getY(), target.getZ());
            return distanceToTargetSqr <= this.meleeAttackMaxDistSqr;
        }

        @Override
        protected void checkAndPerformAttack(LivingEntity pEnemy, double pDistToEnemySqr) {
            double attackReachSqr = this.getAttackReachSqr(pEnemy);
            if (pDistToEnemySqr <= attackReachSqr && ((MeleeAttackGoalAccessor)this).crafting_table_boss$getTicksUntilNextAttack() <= 0) {
                CraftingTableBoss.LOGGER.info("Starting {} attack for {}", AttackType.SMASH, this.mob);
                this.resetAttackCooldown();
                this.golem.setActiveAttackType(AttackType.SMASH);
            }
        }

        @Override
        protected void resetAttackCooldown() {
            ((MeleeAttackGoalAccessor)this).crafting_table_boss$setTicksUntilNextAttack(this.adjustedTickDelay(AttackType.SMASH.getAttackDuration() + 20));
        }
    }

    public static class CTGRangedAttackGoal extends RangedAttackGoal{

        private final float rangedAttackMinDistSqr;

        public CTGRangedAttackGoal(RangedAttackMob pRangedAttackMob, double pSpeedModifier, int pAttackInterval, float pAttackRadius, float rangedAttackMinDist) {
            super(pRangedAttackMob, pSpeedModifier, pAttackInterval, pAttackRadius);
            this.rangedAttackMinDistSqr = rangedAttackMinDist * rangedAttackMinDist;
        }

        @Override
        public boolean canUse() {
            LivingEntity target = ((RangedAttackGoalAccessor) this).crafting_table_boss$getMob().getTarget();
            if(target != null && this.shouldUseRangedAttacks(target)){
                return super.canUse();
            } else{
                return false;
            }
        }

        @Override
        public void start() {
            super.start();
            ((RangedAttackGoalAccessor) this).crafting_table_boss$getMob().setAggressive(true);
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = ((RangedAttackGoalAccessor) this).crafting_table_boss$getMob().getTarget();
            if(target != null && this.shouldUseRangedAttacks(target)){
                return super.canContinueToUse();
            } else{
                return false;
            }
        }

        @Override
        public void stop() {
            super.stop();
            ((RangedAttackGoalAccessor) this).crafting_table_boss$getMob().setAggressive(false);
            ((RangedAttackGoalAccessor) this).crafting_table_boss$getMob().getNavigation().stop();
        }

        private boolean shouldUseRangedAttacks(LivingEntity target){
            double distanceToTargetSqr = ((RangedAttackGoalAccessor) this).crafting_table_boss$getMob().distanceToSqr(target.getX(), target.getY(), target.getZ());
            return distanceToTargetSqr >= this.rangedAttackMinDistSqr;
        }
    }
}
