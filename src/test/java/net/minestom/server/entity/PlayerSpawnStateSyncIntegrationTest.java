package net.minestom.server.entity;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.network.packet.server.play.HeldItemChangePacket;
import net.minestom.server.network.packet.server.play.JoinGamePacket;
import net.minestom.server.network.packet.server.play.PlayerAbilitiesPacket;
import net.minestom.server.network.packet.server.play.SetExperiencePacket;
import net.minestom.server.network.packet.server.play.UpdateHealthPacket;
import net.minestom.testing.Env;
import net.minestom.testing.EnvTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@EnvTest
public class PlayerSpawnStateSyncIntegrationTest {

    @Test
    void configTimeHealthAndExpAreSyncedOnSpawn(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();

        env.process().eventHandler().addListener(AsyncPlayerConfigurationEvent.class, event -> {
            var player = event.getPlayer();
            assertFalse(player.isActive());
            player.setHealth(7f);
            player.setFood(11);
            player.setFoodSaturation(3f);
            player.setExp(0.5f);
            player.setLevel(4);
        });

        var healthTracker = connection.trackIncoming(UpdateHealthPacket.class);
        var expTracker = connection.trackIncoming(SetExperiencePacket.class);

        var player = connection.connect(instance, new Pos(0, 40, 0));

        // Double check expected values
        assertEquals(7f, player.getHealth());
        assertEquals(11, player.getFood());
        assertEquals(3f, player.getFoodSaturation());
        assertEquals(0.5f, player.getExp());
        assertEquals(4, player.getLevel());

        // Check if the client receives the right values
        var health = healthTracker.collect();
        assertFalse(health.isEmpty(), "an UpdateHealthPacket should be sent on spawn");
        var lastHealth = health.getLast();
        assertEquals(7f, lastHealth.health());
        assertEquals(11, lastHealth.food());
        assertEquals(3f, lastHealth.foodSaturation());

        var exp = expTracker.collect();
        assertFalse(exp.isEmpty(), "a SetExperiencePacket should be sent on spawn");
        var lastExp = exp.getLast();
        assertEquals(0.5f, lastExp.percentage());
        assertEquals(4, lastExp.level());
    }

    @Test
    void configTimeAbilitiesAreSyncedOnSpawn(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();

        env.process().eventHandler().addListener(AsyncPlayerConfigurationEvent.class, event -> {
            var player = event.getPlayer();
            assertFalse(player.isActive());
            player.setInvulnerable(true);
            player.setAllowFlying(true);
            player.setInstantBreak(true);
            player.setFlyingSpeed(0.15f);
        });

        var abilitiesTracker = connection.trackIncoming(PlayerAbilitiesPacket.class);

        var player = connection.connect(instance, new Pos(0, 40, 0));

        // Double check expected values
        assertTrue(player.isInvulnerable());
        assertTrue(player.isAllowFlying());
        assertTrue(player.isInstantBreak());
        assertEquals(0.15f, player.getFlyingSpeed());

        // Check if the client receives the right values
        var abilities = abilitiesTracker.collect();
        assertFalse(abilities.isEmpty(), "a PlayerAbilitiesPacket should be sent on spawn");
        var last = abilities.getLast();
        byte expectedFlags = PlayerAbilitiesPacket.FLAG_INVULNERABLE
                | PlayerAbilitiesPacket.FLAG_ALLOW_FLYING
                | PlayerAbilitiesPacket.FLAG_INSTANT_BREAK;
        assertEquals(expectedFlags, last.flags());
        assertEquals(0.15f, last.flyingSpeed());
    }

    @Test
    void configTimeGameModeIsSyncedOnSpawn(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();

        env.process().eventHandler().addListener(AsyncPlayerConfigurationEvent.class, event -> {
            var player = event.getPlayer();
            assertFalse(player.isActive());
            player.setGameMode(GameMode.CREATIVE);
        });

        var joinTracker = connection.trackIncoming(JoinGamePacket.class);

        var player = connection.connect(instance, new Pos(0, 40, 0));

        // Double check expected values
        assertEquals(GameMode.CREATIVE, player.getGameMode());

        // Check if the client receives the right values
        var join = joinTracker.collect();
        assertFalse(join.isEmpty(), "a JoinGamePacket should be sent on spawn");
        assertEquals(GameMode.CREATIVE, join.getLast().gameMode());
    }

    @Test
    void configTimeHeldSlotIsSyncedOnSpawn(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();

        env.process().eventHandler().addListener(AsyncPlayerConfigurationEvent.class, event -> {
            var player = event.getPlayer();
            assertFalse(player.isActive());
            player.setHeldItemSlot((byte) 5);
        });

        var heldTracker = connection.trackIncoming(HeldItemChangePacket.class);

        var player = connection.connect(instance, new Pos(0, 40, 0));

        // Double check expected values
        assertEquals(5, player.getHeldSlot());

        // Check if the client receives the right values
        var held = heldTracker.collect();
        assertFalse(held.isEmpty(), "a HeldItemChangePacket should be sent on spawn");
        assertEquals(5, held.getLast().slot());
    }

    @Test
    void defaultStateOnSpawn(Env env) {
        var instance = env.createFlatInstance();
        var connection = env.createConnection();
        var player = connection.connect(instance, new Pos(0, 40, 0));

        assertEquals((float) player.getAttributeValue(Attribute.MAX_HEALTH), player.getHealth());
        assertEquals(20, player.getFood());
        assertEquals(5f, player.getFoodSaturation());
    }
}
