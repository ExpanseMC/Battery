package me.jellysquid.mods.lithium.common.world;

import com.google.common.collect.Lists;
import me.jellysquid.mods.lithium.common.entity.EntityClassGroup;
import me.jellysquid.mods.lithium.common.world.chunk.ClassGroupFilterableList;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ClassInstanceMultiMap;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.EntityGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import java.util.List;
import java.util.function.Predicate;

import static net.minecraft.world.entity.EntitySelector.NO_SPECTATORS;

public class WorldHelper {
    public interface MixinLoadTest {
    }

    public static final boolean CUSTOM_TYPE_FILTERABLE_LIST_DISABLED = !MixinLoadTest.class.isAssignableFrom(ClassInstanceMultiMap.class);


    /**
     * Partial [VanillaCopy] Classes overriding Entity.getHardCollisionBox(Entity other) or Entity.getCollisionBox()
     * The returned entity list is only used to call getCollisionBox and getHardCollisionBox. As most entities return null
     * for both of these methods, getting those is not necessary. This is why we only get entities when they overwrite
     * getCollisionBox
     *
     * @param entityView      the world
     * @param box             the box the entities have to collide with
     * @param collidingEntity the entity that is searching for the colliding entities
     * @return list of entities with collision boxes
     */
    public static List<Entity> getEntitiesWithCollisionBoxForEntity(EntityGetter entityView, AABB box, Entity collidingEntity) {
        if (CUSTOM_TYPE_FILTERABLE_LIST_DISABLED || collidingEntity != null && EntityClassGroup.MINECART_BOAT_LIKE_COLLISION.contains(collidingEntity.getClass()) || !(entityView instanceof Level)) {
            //use vanilla code when method_30949 (previously getHardCollisionBox(Entity other)) is overwritten, as every entity could be relevant as argument of getHardCollisionBox
            return entityView.getEntities(collidingEntity, box);
        } else {
            //only get entities that overwrite method_30948 (previously getCollisionBox)
            return getEntitiesOfClassGroup((Level) entityView, collidingEntity, EntityClassGroup.BOAT_SHULKER_LIKE_COLLISION, box, NO_SPECTATORS);
        }
    }

    /**
     * Method that allows getting entities of a class group.
     * [VanillaCopy] but custom combination of: get class filtered entities together with excluding one entity
     */
    public static List<Entity> getEntitiesOfClassGroup(Level world, Entity excluded, EntityClassGroup type, AABB box, Predicate<Entity> predicate) {
        world.getProfiler().incrementCounter("getEntities");

        int minChunkX = Mth.floor((box.minX - 2.0D) / 16.0D);
        int maxChunkX = Mth.ceil((box.maxX + 2.0D) / 16.0D);
        int minChunkZ = Mth.floor((box.minZ - 2.0D) / 16.0D);
        int maxChunkZ = Mth.ceil((box.maxZ + 2.0D) / 16.0D);

        List<Entity> entities = Lists.newArrayList();
        ChunkSource chunkManager = world.getChunkSource();

        for (int chunkX = minChunkX; chunkX < maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ < maxChunkZ; chunkZ++) {
                LevelChunk chunk = chunkManager.getChunk(chunkX, chunkZ, false);

                if (chunk != null) {
                    WorldHelper.getEntitiesOfClassGroup(chunk, excluded, type, box, entities, predicate);
                }
            }
        }

        return entities;
    }

    /**
     * Method that allows getting entities of a class group.
     * [VanillaCopy] but custom combination of: get class filtered entities together with excluding one entity
     */
    public static void getEntitiesOfClassGroup(LevelChunk worldChunk, Entity excluded, EntityClassGroup type, AABB box, List<Entity> out, Predicate<Entity> predicate) {
        ClassInstanceMultiMap<Entity>[] entitySections = worldChunk.getEntitySections();
        int minSectionY = Mth.floor((box.minY - 2.0D) / 16.0D);
        int maxSectionY = Mth.floor((box.maxY + 2.0D) / 16.0D);

        minSectionY = Mth.clamp(minSectionY, 0, entitySections.length - 1);
        maxSectionY = Mth.clamp(maxSectionY, 0, entitySections.length - 1);

        for (int sectionY = minSectionY; sectionY <= maxSectionY; ++sectionY) {
            //noinspection rawtypes
            for (Object entity : ((ClassGroupFilterableList) entitySections[sectionY]).getAllOfGroupType(type)) {
                if (entity != excluded && ((Entity) entity).getBoundingBox().intersects(box) && (predicate == null || predicate.test((Entity) entity))) {
                    out.add((Entity) entity);
                }
            }
        }
    }


    /**
     * [VanillaCopy] Method for getting entities by class but also exclude one entity
     */
    public static List<Entity> getEntitiesOfClass(Level world, Entity except, Class<? extends Entity> entityClass, AABB box) {
        world.getProfiler().incrementCounter("getEntities");

        int minChunkX = Mth.floor((box.minX - 2.0D) / 16.0D);
        int maxChunkX = Mth.ceil((box.maxX + 2.0D) / 16.0D);
        int minChunkZ = Mth.floor((box.minZ - 2.0D) / 16.0D);
        int maxChunkZ = Mth.ceil((box.maxZ + 2.0D) / 16.0D);

        List<Entity> entities = Lists.newArrayList();
        ChunkSource chunkManager = world.getChunkSource();

        for (int chunkX = minChunkX; chunkX < maxChunkX; ++chunkX) {
            for (int chunkZ = minChunkZ; chunkZ < maxChunkZ; ++chunkZ) {
                LevelChunk chunk = chunkManager.getChunk(chunkX, chunkZ, false);

                if (chunk != null) {
                    WorldHelper.getEntitiesOfClass(chunk, except, entityClass, box, entities);
                }
            }
        }

        return entities;
    }

    /**
     * [VanillaCopy] Method for getting entities by class but also exclude one entity
     */
    private static void getEntitiesOfClass(LevelChunk worldChunk, Entity excluded, Class<? extends Entity> entityClass, AABB box, List<Entity> out) {
        ClassInstanceMultiMap<Entity>[] entitySections = worldChunk.getEntitySections();
        int minChunkY = Mth.floor((box.minY - 2.0D) / 16.0D);
        int maxChunkY = Mth.floor((box.maxY + 2.0D) / 16.0D);
        minChunkY = Mth.clamp(minChunkY, 0, entitySections.length - 1);
        maxChunkY = Mth.clamp(maxChunkY, 0, entitySections.length - 1);

        for (int chunkY = minChunkY; chunkY <= maxChunkY; chunkY++) {
            for (Entity entity : entitySections[chunkY].find(entityClass)) {
                if (entity != excluded && entity.getBoundingBox().intersects(box)) {
                    out.add(entity);
                }
            }
        }
    }

    public static boolean areNeighborsWithinSameChunk(BlockPos pos) {
        int localX = pos.getX() & 15;
        int localY = pos.getY() & 15;
        int localZ = pos.getZ() & 15;

        return localX > 0 && localY > 0 && localZ > 0 && localX < 15 && localY < 15 && localZ < 15;
    }

    public static boolean areAllNeighborsOutOfBounds(BlockPos pos) {
        return pos.getY() < -1 || pos.getY() > 256;
    }
}
