package com.ilcheese2.bubblelife.bubbles;

import com.ilcheese2.bubblelife.client.BubbleControllerClient;
import net.minecraft.Util;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class BubbleController extends ScheduledThreadPoolExecutor {

    protected Set<Bubble> bubbles = new HashSet<>();

    private boolean debounce = false;

    public BubbleController() {
        super(2);

        scheduleAtFixedRate(blockUntilComplete(() -> {
            if (!isPaused()) {
                if (debounce) {
                    debounce = false;
                    bubbles.forEach((bubble) -> bubble.resetCount(Util.getMillis()));
                }
                bubbles.forEach((bubble) -> bubble.tickInsides(Util.getMillis()));
            } else {
                debounce = true;
            }
        }), 0, 1, TimeUnit.MILLISECONDS);
    }

    private Runnable blockUntilComplete(Runnable runnable) {
        AtomicBoolean done = new AtomicBoolean(true);

        return () -> {
            if (done.get()) {
                done.set(false);
                runOnMainThread(() -> {
                    runnable.run();
                    done.set(true);
                });
            }
        };
    }

    protected abstract void runOnMainThread(Runnable runnable);

    protected abstract boolean isPaused();

    public boolean inBubble(Entity entity) {
        for (Bubble bubble : bubbles) {
            if (bubble.entities.contains(entity)) {
                return true;
            }
        }
        return false;
    }

    public double checkInteraction(Entity entity, Bubble exclude) {
        double minDistance = 1000.0;
        for (Bubble bubble : bubbles) {
            if (bubble == exclude) {
                continue;
            }
            if (bubble.entities.contains(entity)) {
                double distance = bubble.distanceTo(entity);
                if (distance < minDistance) {
                    minDistance = distance;
                }
            }
        }
        return minDistance;
    }


    public void addBubble(Bubble bubble) {
        bubbles.add(bubble);
    }

    public void removeBubble(Bubble bubble) {
        bubbles.remove(bubble);
    }

    public void clearBubbles() {
        bubbles.clear();
    }

    public Bubble ownsBubble(Player player) {
        for (Bubble bubble : bubbles) {
            if (bubble.owner != null && bubble.owner.equals(player.getUUID())) {
                return bubble;
            }
        }
        return null;
    }

    public boolean inBubblePosition(Vec3 pos) {
        for (Bubble bubble : bubbles) {
            if (bubble.getBoundingBox().contains(pos)) {
                return true;
            }
        }
        return false;
    }


    public static boolean inBubble(Entity entity, boolean isClient) { // uhh
        if (isClient) {
            return BubbleControllerClient.instance().inBubble(entity);
        } else {
            return BubbleControllerServer.instance().inBubble(entity);
        }
    }

    public static void addBubble(Bubble bubble, boolean isClient) {
        if (isClient) {
            BubbleControllerClient.instance().addBubble(bubble);
        } else {
            BubbleControllerServer.instance().addBubble(bubble);
        }
    }

    public static void removeBubble(Bubble bubble, boolean isClient) {
        if (isClient) {
            BubbleControllerClient.instance().removeBubble(bubble);
        } else {
            BubbleControllerServer.instance().removeBubble(bubble);
        }
    }

    public static double checkInteraction(Entity entity, Bubble exclude, boolean isClient) {
        if (isClient) {
            return BubbleControllerClient.instance().checkInteraction(entity, exclude);
        } else {
            return BubbleControllerServer.instance().checkInteraction(entity, exclude);
        }
    }

    public static void clearBubbles(boolean isClient) {
        if (isClient) {
            BubbleControllerClient.instance().clearBubbles();
        } else {
            BubbleControllerServer.instance().clearBubbles();
        }
    }

    public static Bubble ownsBubble(Player player, boolean isClient) {
        if (isClient) {
            return BubbleControllerClient.instance().ownsBubble(player);
        } else {
            return BubbleControllerServer.instance().ownsBubble(player);
        }
    }

    public static boolean inBubblePosition(Vec3 pos, boolean isClient) {
        if (isClient) {
            return BubbleControllerClient.instance().inBubblePosition(pos);
        } else {
            return BubbleControllerServer.instance().inBubblePosition(pos);
        }
    }
}
