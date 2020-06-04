package General.Shared;

import javax.swing.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.*;

public class MBListView<T extends MBListView.Item> extends JPanel {

    /**
     * The last width of the view
     */
    private int lastWidth = 0;
    /**
     * The list that contains the content
     */
    private final ArrayList<T> items = new ArrayList<>();
    /**
     * The comparator that describes how the list view should be sorted
     */
    private final Comparator<T> comparator;

    /**
     * Constructor
     */
    public MBListView() {
        this.comparator = Comparator.comparing(item -> item.name.toLowerCase());

        // Initialize the view
        setLayout(null);
        setOpaque(false);

        // Listen for resize events
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (lastWidth != getWidth()) {
                    resizeList();
                }
                lastWidth = getWidth();
            }
        });
    }

    /**
     * Resize the lists items
     */
    private void resizeList() {
        int height = 0;
        for (Item item : items) {
            item.onResize(height, getWidth());
            height += item.getHeight();
        }
        setSize(getWidth(), height);
    }

    /**
     * Add an item
     *
     * @param item to be added
     */
    public void addItem(T item) {
        // Add the item and sort the list
        items.add(item);
        items.sort(comparator);
        add(item);

        // Add a mouse listener to catch selections
        item.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
            }
            @Override
            public void mousePressed(MouseEvent e) {
                item.onSelected(items.indexOf(item));
            }
            @Override
            public void mouseReleased(MouseEvent e) {
            }
            @Override
            public void mouseEntered(MouseEvent e) {
            }
            @Override
            public void mouseExited(MouseEvent e) {
            }
        });

        // Rebuild the list
        resizeList();
    }

    /**
     * Remove item from the list
     *
     * @param index of the item
     */
    public void removeItem(int index) {
        // Remove the item
        remove(items.get(index));
        items.remove(index);

        // Rebuild the list
        resizeList();
    }

    /**
     * The base class for an item
     */
    public static abstract class Item extends JLabel {
        /**
         * The name of the item
         */
        public String name;

        /**
         * Constructor
         */
        public Item(String name) {
            this.name = name;
        }

        /**
         * Handle resize events
         *
         * @param y position
         * @param width of the list view
         */
        public abstract void onResize(int y, int width);


        /**
         * Handle selection of an item
         *
         * @param index of the selected item
         */
        public abstract void onSelected(int index);
    }
}
