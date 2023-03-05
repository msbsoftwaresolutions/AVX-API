package net.arvaux.core.util;

import net.minecraft.server.v1_8_R3.EntityInsentient;
import net.minecraft.server.v1_8_R3.PathfinderGoal;
import org.bukkit.util.Vector;

public class PathfinderGoalConstantMove extends PathfinderGoal {

    private EntityInsentient insentient;
    private Vector movement;

    public PathfinderGoalConstantMove(EntityInsentient insentient, Vector movement) {
        this.insentient = insentient;
        this.movement = movement.multiply(0.05);
    }

    @Override
    public boolean a() {
        return true;
    }

    @Override
    public void e() {
        insentient.move(movement.getX(), movement.getY(), movement.getZ());
    }
}
