package General.Shared;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

public abstract class MBPanel extends JPanel {

    /**
     * The component sizer
     */
    private final ArrayList<ComponentResize> components = new ArrayList<>();

    /**
     * The buttons for the group
     */
    private MBButton[] buttons;

    /**
     * Setup the panels content
     */
    public abstract void beforeVisible();

    /**
     * This method is executed when the panel is visible
     */
    public abstract void afterVisible();

    /**
     * This class provides a general setup for a panel
     */
    public MBPanel() {
        // General stuff
        setLayout(null);
        setBackground(Color.white);
        beforeVisible();

        // Listen for resize events
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                for (ComponentResize component : components) {
                    component.resize();
                }
            }
        });
    }

    /**
     * Add a component and resize it
     */
    public void addComponent(JComponent component, ComponentResize componentSizer) {
        componentSizer.resize();
        components.add(componentSizer);
        add(component);
    }

    /**
     * Add buttons to a group to activate arrow key navigation
     *
     * @param buttons to be added to a group
     */
    public void addButtonGroup(MBButton... buttons) {
        this.buttons = buttons;
    }

    /**
     * Activate the arrow key navigation for the button group
     */
    public void setupButtonGroup() {
        // Setup the listeners for the buttons
        for (int i = 0; i < buttons.length; i++) {
            int index = i;
            buttons[i].addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                        int nextIndex = buttons.length - 1 > index ? index + 1 : 0;
                        buttons[nextIndex].requestFocusInWindow();
                    } else if (e.getKeyCode() == KeyEvent.VK_UP) {
                        int nextIndex = index > 0 ? index - 1 : buttons.length - 1;
                        buttons[nextIndex].requestFocusInWindow();
                    }
                }
            });
        }

        // Set the focus on the first button (delay is required because instant execution leads to flickering)
        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        buttons[0].requestFocus();
                    }
                },
                50
        );
    }

    /**
     * Interface for resize events
     */
    public interface ComponentResize {
        void resize();
    }
}