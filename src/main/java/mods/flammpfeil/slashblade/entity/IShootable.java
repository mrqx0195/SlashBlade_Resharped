package mods.flammpfeil.slashblade.entity;

import net.minecraft.world.entity.Entity;

import javax.annotation.Nullable;

public interface IShootable {
    
    void shoot(double x, double y, double z, float velocity, float inaccuracy);
    
    @Nullable
    Entity getShooter();
    
    void setShooter(Entity shooter);
    
    double getDamage();
}
