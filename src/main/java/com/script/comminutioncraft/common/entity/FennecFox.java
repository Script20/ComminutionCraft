package com.script.comminutioncraft.common.entity;

import com.script.comminutioncraft.common.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import org.jspecify.annotations.Nullable;

/**
 * Fennec Fox (owner, 2026-11-22): a small desert fox. Wild ones run from players who aren't sneaking; sneak up and feed
 * one raw rabbit for a 1 in 3 chance to tame it. Tamed: follows its owner (teleporting when stuck or far behind), sits
 * and stands on right-click, and two tamed ones breed with raw rabbit.
 */
public class FennecFox extends TamableAnimal {
    public FennecFox(EntityType<? extends FennecFox> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 8.0).add(Attributes.MOVEMENT_SPEED, 0.3F);
    }

    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.FENNEC_FOX.get(), createAttributes().build());
    }

    /** On sand (anywhere rabbits may spawn), in daylight. */
    public static void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        event.register(ModEntities.FENNEC_FOX.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                FennecFox::checkSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }

    public static boolean checkSpawnRules(EntityType<FennecFox> type, LevelAccessor level, EntitySpawnReason reason, BlockPos pos,
                                          RandomSource random) {
        return level.getBlockState(pos.below()).is(BlockTags.RABBITS_SPAWNABLE_ON)
                && (EntitySpawnReason.ignoresLightRequirements(reason) || isBrightEnoughToSpawn(level, pos));
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(1, new TamableAnimal.TamableAnimalPanicGoal(1.5));
        goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
        // wild ones keep away from players who aren't sneaking
        goalSelector.addGoal(3, new AvoidEntityGoal<>(this, Player.class, player -> !isTame() && !player.isDiscrete(), 10.0F, 1.1, 1.5,
                EntitySelector.NO_CREATIVE_OR_SPECTATOR));
        goalSelector.addGoal(4, new TemptGoal(this, 0.6, this::isFood, true));
        goalSelector.addGoal(5, new FollowOwnerGoal(this, 1.0, 10.0F, 3.0F));
        goalSelector.addGoal(6, new BreedGoal(this, 0.8));
        goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.8));
        goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(9, new RandomLookAroundGoal(this));
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.RABBIT);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (isTame()) {
            if (!isOwnedBy(player)) {
                return super.mobInteract(player, hand);
            }
            if (isFood(stack) && getHealth() < getMaxHealth()) {
                if (!level().isClientSide()) {
                    feed(player, hand, stack, 1.0F, 1.0F);
                }
                return InteractionResult.SUCCESS;
            }
            InteractionResult result = super.mobInteract(player, hand); // breeding
            if (!result.consumesAction()) {
                setOrderedToSit(!isOrderedToSit());
                return InteractionResult.SUCCESS;
            }
            return result;
        }
        if (isFood(stack)) {
            if (!player.isDiscrete()) {
                return InteractionResult.PASS; // only a sneaking player gets close enough (and wild ones don't breed)
            }
            if (!level().isClientSide()) {
                usePlayerItem(player, hand, stack);
                tryToTame(player);
                setPersistenceRequired();
                playSound(SoundEvents.FOX_EAT, 1.0F, getVoicePitch());
            }
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    private void tryToTame(Player player) {
        if (random.nextInt(3) == 0 && !EventHooks.onAnimalTame(this, player)) {
            tame(player);
            setOrderedToSit(true);
            level().broadcastEntityEvent(this, (byte) 7); // hearts
        } else {
            level().broadcastEntityEvent(this, (byte) 6); // smoke
        }
    }

    /** Only two tamed foxes breed. */
    @Override
    public boolean canMate(Animal partner) {
        return isTame() && partner instanceof FennecFox fox && fox.isTame() && super.canMate(partner);
    }

    @Override
    public @Nullable FennecFox getBreedOffspring(ServerLevel level, AgeableMob partner) {
        FennecFox baby = ModEntities.FENNEC_FOX.get().create(level, EntitySpawnReason.BREEDING);
        if (baby != null && isTame()) {
            baby.setOwnerReference(getOwnerReference());
            baby.setTame(true, true);
        }
        return baby;
    }

    // ---- Sounds: a vanilla fox's, a little higher (it's smaller) ----

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return SoundEvents.FOX_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.FOX_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.FOX_DEATH;
    }

    @Override
    public float getVoicePitch() {
        return super.getVoicePitch() * 1.25F;
    }
}
