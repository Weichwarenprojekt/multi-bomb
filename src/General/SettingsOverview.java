package General;

import General.Shared.MBButton;
import General.Shared.MBLabel;
import General.Shared.MBPanel;
import Menu.Menu;

import javax.swing.*;
import java.awt.*;

/**
 * This class provides some settings like sound and graphics
 */
public class SettingsOverview extends MBPanel {
    /**
     * The button to change the screen state
     */
    private MBButton fullscreen;
    /**
     * The button to change the anti aliasing state
     */
    private MBButton antiAliasing;

    /**
     * Setup the layout
     */
    @Override
    public void beforeVisible() {
        // The title
        MBLabel title = new MBLabel("Settings", SwingConstants.CENTER, MBLabel.H1);
        addComponent(title, () -> title.setBounds(getWidth() / 2 - 100, 50, 200, 40));

        // The label for window mode
        MBLabel fullscreenLabel = new MBLabel("Window Mode:");
        addComponent(fullscreenLabel, () -> fullscreenLabel.setBounds(getWidth() / 2 - 156, 100, 140, 30));

        // The button for leaving or entering fullscreen
        fullscreen = new MBButton(MB.settings.fullscreen ? "Fullscreen" : "Window");
        fullscreen.addActionListener(e -> changeScreen());
        addComponent(fullscreen, () -> fullscreen.setBounds(getWidth() / 2 + 16, 100, 140, 30));

        // The label for anti aliasing
        MBLabel antiAliasingLabel = new MBLabel("Anti Aliasing:");
        addComponent(antiAliasingLabel, () -> antiAliasingLabel.setBounds(getWidth() / 2 - 156, 140, 140, 30));

        // The button for activating or deactivating anti aliasing
        antiAliasing = new MBButton(MB.settings.antiAliasing ? "Activated" : "Deactivated");
        antiAliasing.addActionListener(e -> changeAntiAliasing());
        addComponent(antiAliasing, () -> antiAliasing.setBounds(getWidth() / 2 + 16, 140, 140, 30));

        // The button for opening a lobby overview
        MBButton back = new MBButton("Back");
        back.addActionListener(e -> MB.show(new Menu()));
        addComponent(back, () -> back.setBounds(getWidth() / 2 - 70, 180, 140, 30));

        // Add the buttons to a group
        addButtonGroup(fullscreen, antiAliasing, back);
    }

    /**
     * Method that is executed when panel is visible
     */
    @Override
    public void afterVisible() {
        setupButtonGroup();
    }

    /**
     * Change the screen state (window mode or fullscreen)
     */
    public void changeScreen() {
        // Change the state
        MB.settings.fullscreen = !MB.settings.fullscreen;

        // Get the active window
        Window activeWindow = javax.swing.FocusManager.getCurrentManager().getActiveWindow();

        // Leave or enter fullscreen
        fullscreen.setText(MB.settings.fullscreen ? "Fullscreen" : "Window");
        if (MB.settings.fullscreen) {
            activeWindow.getGraphicsConfiguration().getDevice().setFullScreenWindow(MB.frame);
        } else {
            activeWindow.getGraphicsConfiguration().getDevice().setFullScreenWindow(null);
        }
        // Save the settings
        MB.settings.saveSettings();
    }

    /**
     * Activate anti aliasing
     */
    public void changeAntiAliasing() {
        // Change the state
        MB.settings.antiAliasing = !MB.settings.antiAliasing;
        antiAliasing.setText(MB.settings.antiAliasing ? "Activated" : "Deactivated");
        MB.frame.repaint();

        // Save the settings
        MB.settings.saveSettings();
    }
}
