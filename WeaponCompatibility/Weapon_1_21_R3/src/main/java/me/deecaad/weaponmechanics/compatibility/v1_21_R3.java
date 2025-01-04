package me.deecaad.weaponmechanics.compatibility;

import me.deecaad.weaponmechanics.compatibility.scope.IScopeCompatibility;
import me.deecaad.weaponmechanics.compatibility.scope.Scope_1_21_R3;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.phys.Vec3;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public class v1_21_R3 implements IWeaponCompatibility {

    private final IScopeCompatibility scopeCompatibility;

    public v1_21_R3() {
        this.scopeCompatibility = new Scope_1_21_R3();
    }

    @NotNull @Override
    public IScopeCompatibility getScopeCompatibility() {
        return scopeCompatibility;
    }

    @Override
    public void modifyCameraRotation(Player player, float yaw, float pitch, boolean absolute) {
        player.setRotation(yaw, pitch);
        return;
        //TODO: Figure out proper way to handle recoil in 1.21.3
        // Solution below seems to cause issues causing "moved too quickly!" messages
        /*
        Vec3 motion = new Vec3(0, 0, 0);
        PositionMoveRotation rotation = new PositionMoveRotation(player.getLocation().toVector(), motion, yaw, pitch);
        
        ((CraftPlayer) player).getHandle().connection.send(
            new ClientboundPlayerPositionPacket(
                player.getEntityId(),
                rotation,
                Collections.emptySet()
            )
        );*/
    }

    @Override
    public void logDamage(org.bukkit.entity.LivingEntity victim, org.bukkit.entity.LivingEntity source, double health, double damage, boolean isMelee) {
        DamageSources factory = ((CraftLivingEntity) source).getHandle().damageSources();
        DamageSource damageSource;

        if (isMelee) {
            if (source instanceof CraftPlayer player) {
                damageSource = factory.playerAttack(player.getHandle());
            } else {
                damageSource = factory.mobAttack(((CraftLivingEntity) source).getHandle());
            }
        } else {
            damageSource = factory.thrown(null, ((CraftLivingEntity) source).getHandle());
        }

        LivingEntity nms = ((CraftLivingEntity) victim).getHandle();
        nms.combatTracker.recordDamage(damageSource, (float) health);
        nms.setLastHurtByMob(((CraftLivingEntity) source).getHandle());
        if (source instanceof Player)
            nms.setLastHurtByPlayer(((CraftPlayer) source).getHandle());
    }

    @Override
    public void setKiller(org.bukkit.entity.LivingEntity victim, Player killer) {
        ((CraftLivingEntity) victim).getHandle().lastHurtByMob = ((CraftPlayer) killer).getHandle();
    }

    @Override
    public void playHurtAnimation(org.bukkit.entity.LivingEntity victim) {
        victim.playHurtAnimation(0);
    }
}