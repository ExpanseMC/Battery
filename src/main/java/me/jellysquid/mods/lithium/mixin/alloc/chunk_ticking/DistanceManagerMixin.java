package me.jellysquid.mods.lithium.mixin.alloc.chunk_ticking;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.function.Predicate;
import net.minecraft.server.level.DistanceManager;
import net.minecraft.server.level.Ticket;
import net.minecraft.util.SortedArraySet;

@Mixin(DistanceManager.class)
public abstract class DistanceManagerMixin {
    @Shadow
    private long ticketTickCounter;

    @Shadow
    @Final
    private Long2ObjectOpenHashMap<SortedArraySet<Ticket<?>>> tickets;

    @Shadow
    @Final
    private DistanceManager.ChunkTicketTracker ticketTracker;

    @Shadow
    private static int getTicketLevelAt(SortedArraySet<Ticket<?>> sortedArraySet) {
        throw new UnsupportedOperationException();
    }

    /**
     * @reason Remove lambda allocation in every iteration
     * @author JellySquid
     */
    @Overwrite
    public void purgeStaleTickets() {
        ++this.ticketTickCounter;

        ObjectIterator<Long2ObjectMap.Entry<SortedArraySet<Ticket<?>>>> iterator =
                this.tickets.long2ObjectEntrySet().fastIterator();
        Predicate<Ticket<?>> predicate = (chunkTicket) -> chunkTicket.timedOut(this.ticketTickCounter);

        while (iterator.hasNext()) {
            Long2ObjectMap.Entry<SortedArraySet<Ticket<?>>> entry = iterator.next();
            SortedArraySet<Ticket<?>> value = entry.getValue();

            if (value.removeIf(predicate)) {
                this.ticketTracker.update(entry.getLongKey(), getTicketLevelAt(entry.getValue()), false);
            }

            if (value.isEmpty()) {
                iterator.remove();
            }
        }

    }
}
