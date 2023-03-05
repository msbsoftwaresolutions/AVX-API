package net.arvaux.core.util;

import com.google.common.collect.Lists;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;

public class UtilVector {

    public static Vector average(Vector... vectors) {
        return average(Lists.newArrayList(vectors));
    }

    public static Vector average(List<Vector> vectors) {
        if (vectors.size() == 0) {
            throw new IllegalArgumentException("Empty list!");
        }

        double x = 0, y = 0, z = 0;
        for (Vector vector : vectors) {
            x += vector.getX();
            y += vector.getY();
            z += vector.getZ();
        }

        x /= (double) vectors.size();
        y /= (double) vectors.size();
        z /= (double) vectors.size();

        return new Vector(x, y, z);
    }

    public static Vector between(Location from, Location to) {
        return to.clone().subtract(from).toVector();
    }

    public static Vector rotateVectorAxisX(Vector vector, double rotation) {
        double sinTheta = UtilMath.fastSin(rotation);
        double cosTheta = UtilMath.fastCos(rotation);
        return UtilVector.rotateVectorAxisX(vector, sinTheta, cosTheta);
    }

    public static Vector rotateVectorAxisX(Vector vector, int rotation) {
        double sinTheta = UtilMath.fastSin(rotation);
        double cosTheta = UtilMath.fastCos(rotation);
        return UtilVector.rotateVectorAxisX(vector, sinTheta, cosTheta);
    }

    public static Vector rotateVectorAxisX(Vector vector, double sinTheta, double cosTheta) {
        double y = vector.getY() * cosTheta - vector.getZ() * sinTheta;
        double z = vector.getZ() * cosTheta + vector.getY() * sinTheta;
        return vector.setY(y).setZ(z);
    }

    public static Vector rotateVectorAxisY(Vector vector, double rotation) {
        rotation = -rotation;
        double sinTheta = UtilMath.fastSin(rotation);
        double cosTheta = UtilMath.fastCos(rotation);
        return UtilVector.rotateVectorAxisY(vector, sinTheta, cosTheta);
    }

    public static Vector rotateVectorAxisY(Vector vector, int rotation) {
        rotation *= -1;
        double sinTheta = UtilMath.fastSin(rotation);
        double cosTheta = UtilMath.fastCos(rotation);
        return UtilVector.rotateVectorAxisY(vector, sinTheta, cosTheta);
    }

    public static Vector rotateVectorAxisY(Vector vector, double sinTheta, double cosTheta) {
        double x = vector.getX() * cosTheta + vector.getZ() * sinTheta;
        double z = vector.getZ() * cosTheta - vector.getX() * sinTheta;
        return vector.setX(x).setZ(z);
    }

    public static Vector rotateVectorAxisZ(Vector vector, double rotation) {
        double sinTheta = UtilMath.fastSin(rotation);
        double cosTheta = UtilMath.fastCos(rotation);
        return UtilVector.rotateVectorAxisZ(vector, sinTheta, cosTheta);
    }

    public static Vector rotateVectorAxisZ(Vector vector, int rotation) {
        double sinTheta = UtilMath.fastSin(rotation);
        double cosTheta = UtilMath.fastCos(rotation);
        return UtilVector.rotateVectorAxisZ(vector, sinTheta, cosTheta);
    }

    public static Vector rotateVectorAxisZ(Vector vector, double sinTheta, double cosTheta) {
        double x = vector.getX() * cosTheta - vector.getY() * sinTheta;
        double y = vector.getY() * cosTheta + vector.getX() * sinTheta;
        return vector.setY(y).setX(x);
    }

    public static double getYaw(Vector vector) {
        return -Math.toDegrees(Math.atan2(vector.getX(), vector.getZ()));
    }

    public static double getPitch(Vector vector) {
        return -Math.toDegrees(Math.asin(vector.getY() / vector.length()));
    }

    public static Vector getEyeLocationVector(Entity target) {
        return UtilVector.getDirectionalVector(target.getLocation().getYaw(), target.getLocation().getPitch());
    }

    public static Vector getDirection(Location location) {
        return getDirectionalVector(location.getYaw(), location.getPitch());
    }

    public static Vector getDirectionalVector(double yaw, double pitch) {
        double verticalCos = UtilMath.fastCos(pitch);
        double x = -UtilMath.fastSin(yaw) * verticalCos;
        double y = -UtilMath.fastSin(pitch);
        double z = UtilMath.fastCos(yaw) * verticalCos;
        return new Vector(x, y, z).normalize();
    }

    public static Vector getRepulsiveVector(Entity entity, Location repelFrom) {
        return UtilVector.getRepulsiveVector(entity.getLocation(), repelFrom);
    }

    public static Vector getRepulsiveVector(Location current, Location repelFrom) {
        if (current.equals(repelFrom)) {
            return new Vector(0, 0, 0);
        }

        return current.clone().subtract(repelFrom).toVector().normalize();
    }

    public static Vector getAttractiveVector(Location current, Location attractTowards) {
        return attractTowards.clone().subtract(current).toVector().normalize();
    }

    public static Vector getPropulsiveVector(Location from, Location target) {
        return target.subtract(from).toVector().normalize();
    }

    public static Vector getPropulsiveVector(double angle) {
        return new Vector(-UtilMath.fastSin((int) angle), 0, UtilMath.fastCos(angle)).normalize();
    }

    public static Vector getPropulsiveVector(int angle) {
        return new Vector(-UtilMath.fastSin(angle), 0, UtilMath.fastCos(angle)).normalize();
    }

    public static Vector multiplyWithHorizontalLimit(Vector vector, double multiplier, double limit) {
        double absX = Math.abs(vector.getX());
        double absZ = Math.abs(vector.getZ());
        double maxMultiplier = limit / Math.max(absX, absZ);
        return vector.multiply(Math.min(multiplier, maxMultiplier));
    }

    public static Vector getRandomVector(double size) {
        return getRandomVector().multiply(size);
    }

    public static Vector getRandomVector() {
        double x = ThreadLocalRandom.current().nextDouble(-0.5, 0.5);
        double y = ThreadLocalRandom.current().nextDouble(-0.5, 0.5);
        double z = ThreadLocalRandom.current().nextDouble(-0.5, 0.5);
        return new Vector(x, y, z).normalize();
    }

    public static Vector getRandomHorizontalVector() {
        double x = ThreadLocalRandom.current().nextDouble(-0.5, 0.5);
        double z = ThreadLocalRandom.current().nextDouble(-0.5, 0.5);
        return new Vector(x, 0.0, z).normalize();
    }

    public static Vector round(Vector vector) {
        return new Vector(Math.round(vector.getX()), Math.round(vector.getY()), Math.round(vector.getZ()));
    }

    public static Vector ceil(Vector vector) {
        return new Vector(Math.ceil(vector.getX()), Math.ceil(vector.getY()), Math.ceil(vector.getZ()));
    }

    public static Vector floor(Vector vector) {
        return new Vector(Math.floor(vector.getX()), Math.floor(vector.getY()), Math.floor(vector.getZ()));
    }


    public static void applyVelocity(net.minecraft.server.v1_8_R3.Entity entity, Vector vector) {
        UtilVector.applyVelocity(entity.getBukkitEntity(), vector);
    }


    public static void applyVelocity(Entity entity, Vector vector) {
        if (entity instanceof ArmorStand) {
            // Don't want to affect armor stands
            return;
        }

        entity.setVelocity(vector);
    }

    public enum RotationAxis {
        X {
            @Override
            public Vector rotate(Vector vector, int degrees) {
                return UtilVector.rotateVectorAxisX(vector, degrees);
            }
        },
        Y {
            @Override
            public Vector rotate(Vector vector, int degrees) {
                return UtilVector.rotateVectorAxisY(vector, degrees);
            }
        },
        Z {
            @Override
            public Vector rotate(Vector vector, int degrees) {
                return UtilVector.rotateVectorAxisZ(vector, degrees);
            }
        };

        public abstract Vector rotate(Vector vector, int degrees);
    }

    public interface VectorModification {

        void apply(Vector vector);
    }

    public static class VectorOffset implements VectorModification {

        private Vector vector;
        private Callable<Vector> provider;

        public VectorOffset(double x, double y, double z) {
            this(new Vector(x, y, z));
        }

        public VectorOffset(Callable<Vector> provider) {
            this.provider = provider;
        }

        public VectorOffset(Vector vector) {
            this.vector = vector;
        }

        @Override
        public void apply(Vector vector) {
            try {
                vector.add(this.provider == null ? this.vector : this.provider.call());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static class VectorRotation implements VectorModification {

        private RotationAxis direction;
        private int degrees;
        private Callable<Number> provider;

        public VectorRotation(RotationAxis direction, int degrees) {
            this.direction = direction;
            this.degrees = degrees;
        }

        public VectorRotation(RotationAxis direction, Callable<Number> provider) {
            this.direction = direction;
            this.provider = provider;
        }

        public void apply(Vector vector) {
            try {
                this.direction.rotate(vector, this.provider == null ? this.degrees : this.provider.call().intValue());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static class VectorMultiplier implements VectorModification {

        private double multiplier;

        public VectorMultiplier(double multiplier) {
            this.multiplier = multiplier;
        }

        @Override
        public void apply(Vector vector) {
            vector.multiply(this.multiplier);
        }
    }

    public static class VectorNoise implements VectorModification {

        private double size;

        public VectorNoise(double size) {
            this.size = size;
        }

        @Override
        public void apply(Vector vector) {
            vector.add(UtilVector.getRandomVector().multiply(UtilMath.randomWeightedDouble(this.size * 0.1, this.size, 10)));
        }
    }
}
