package Game.Items;

import Game.Models.Field;
import Game.Models.Upgrades;
import General.Shared.MBPanel;
import Server.Messages.Socket.Map;

import java.awt.*;

/**
 * The base class for a usable item
 */
public abstract class Item {
    /**
     * The available items
     */
    public static final String BOMB = "Bomb";
    /**
     * The name of the item
     */
    public final String name;
    /**
     * The field that matches the item
     */
    public final Field field;

    /**
     * Constructor
     */
    public Item(String name, Field field) {
        this.name = name;
        this.field = field;
    }

    /**
     * Load the textures for all the items
     *
     * @param parent the image size depends on
     */
    public static void loadTextures(MBPanel parent) {
        Bomb.loadTextures(parent);
    }

    /**
     * Check if an item is passable
     *
     * @param onItem the on item state of the player
     * @param m      position
     * @param n      position
     * @return true if item is passable
     */
    public static boolean isPassable(OnItem onItem, int m, int n) {
        Item item = Map.items[m][n];
        if (item == null || (onItem.onItem && onItem.m == m && onItem.n == n)) {
            return true;
        } else if (item.name.equals(BOMB)) {
            return false;
        }
        return false;
    }

    /**
     * Get an item by the name
     *
     * @param name of the item
     * @return the corresponding item
     */
    public static Item getItem(String name) {
        return new Bomb();
    }

    /**
     * Check if an item is usable
     */
    public abstract boolean isUsable(int m, int n, Upgrades upgrades);

    /**
     * Handle the use of an item
     */
    public abstract Item use(int m, int n, Upgrades upgrades);

    /**
     * Draw a used item
     */
    public abstract Item draw(Graphics2D g, int m, int n);

    /**
     * Class that describes the player's position on an item
     */
    public static class OnItem {
        /**
         * True if the player is on an item
         */
        public boolean onItem = false;
        /**
         * The position of the item the player's on
         */
        public int m, n;

        /**
         * Set the new position
         *
         * @param m position
         * @param n position
         */
        public void setPosition(int m, int n) {
            this.m = m;
            this.n = n;
        }
    }
}
