package Menu;

import General.Shared.*;

import General.MB;
import Menu.Dialogs.EnterLobbyName;
import Menu.Dialogs.EnterPlayerName;
import Server.DetectLobby;

import Server.Messages.ErrorMessage;
import Server.Messages.Message;
import Server.Messages.REST.CreateLobby;
import Server.Messages.REST.JoinLobby;
import Server.Messages.REST.LobbyInfo;
import Server.Server;

import javax.swing.*;
import java.awt.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;

import static Menu.DetailedLobbyView.BUTTON_WIDTH;
import static Menu.DetailedLobbyView.MARGIN;

/**
 * This class provides an lobby overview
 */
public class LobbyView extends MBPanel {

    /**
     * Address of selected Server
     */
    String serverAddress;
    /**
     * Lobbyinfo
     */
    LobbyInfo lobbyInfo;
    /**
     * Listview for lobbies
     */
    MBListView<LobbyListItem> list = new MBListView<>();
    /**
     * Spinner
     */
    MBSpinner spinner;

    /**
     * Buttons
     */
    MBButton back, join, create;
    /**
     * True if lobby is selected
     */
    static boolean selected = false;
    /**
     * Selected lobby
     */
    static int selectedLobby;
    /**
     * Player name
     */
    String playerName = "";

    /**
     * Created Lobby name
     */
    String createLobbyName = "";


    public LobbyView() {
    }

    public LobbyView (String address) {
        setupLayout();
        serverAddress = address;
        if (!MB.settings.playerName.isEmpty()) {
            playerName = MB.settings.playerName;
        }
    }

    /**
     * Setup the layout
     */
    public void setupLayout() {
        //The title
        MBLabel title = new MBLabel("Lobby Overview", SwingConstants.CENTER, MBLabel.H1);
        addComponent(title, () -> title.setBounds(
                getWidth() / 2 - 100,
                32,
                200,
                40));

        //Create scrollable listview
        list = new MBListView<>();
        MBScrollView scroll = new MBScrollView(list);
        addComponent(scroll, () -> scroll.setBounds(
                getWidth() / 2 - (getHeight() + MARGIN + BUTTON_WIDTH) / 2,
                96,
                getHeight(),
                getHeight() - 160));

        //loading spinner
        spinner = new MBSpinner();
        addComponent(spinner, () ->
            spinner.setBounds(
                    scroll.getX() + scroll.getWidth() / 2 - 50,
                    scroll.getY() + scroll.getHeight() / 2 - 50,
                    100,
                    100)
        );

        // The background
        MBBackground background = new MBBackground(Color.LIGHT_GRAY);
        addComponent(background, () -> background.setBounds(
                scroll.getX() - 4,
                scroll.getY() - 4,
                scroll.getWidth() + 8,
                scroll.getHeight() + 8
        ));

        // Join button
        join = new MBButton("Join");
        join.addActionListener( e -> {
            if (selected) {
                if (playerName.isEmpty()) {
                    showDialog(new EnterPlayerName(this), () -> {});
                }
                if (joinLobby() == 200) {
                    /**
                     * Call DetailedLobbyView
                     */
                } else {
                    toastError("Lobby konnte nicht", "beigetreten werden");
                }
            } else {
                toastError("Keine Lobby", "ausgewählt!");

            }
            selected = false;
        });

        addComponent(join, () -> join.setBounds(
                scroll.getX() + scroll.getWidth() + MARGIN,
                scroll.getY() - 4,
                DetailedLobbyView.BUTTON_WIDTH,
                DetailedLobbyView.BUTTON_HEIGHT
        ));

        // Create button
        create = new MBButton("Create");
        create.addActionListener( e -> {
            //set player name
                    showDialog(new EnterLobbyName(this), () -> {
                        if (!createLobbyName.isEmpty()) {
                            /**
                             *
                             * Call detailedLobbyView
                             * playerName
                             * createLobbyName
                             *
                             */
                             CreateLobby msg = new CreateLobby();
                             //msg.playerID = playerName;
                             msg.playerID = "TestSpieler";
                             msg.lobbyName = createLobbyName;
                             createLobby(msg);
                        }
                    });
            });

        addComponent(create, () -> create.setBounds(
                scroll.getX() + scroll.getWidth() + MARGIN,
                scroll.getY() + 46,
                DetailedLobbyView.BUTTON_WIDTH,
                DetailedLobbyView.BUTTON_HEIGHT
        ));


        // Back button
        back = new MBButton("Back");
        back.addActionListener(e -> {
            MB.show(new ServerView(), false);
            selected = false;
        });
        addComponent(back, () -> back.setBounds(
                scroll.getX() + scroll.getWidth() + MARGIN,
                scroll.getY() + scroll.getHeight() - DetailedLobbyView.BUTTON_HEIGHT + 4,
                DetailedLobbyView.BUTTON_WIDTH,
                DetailedLobbyView.BUTTON_HEIGHT
        ));
        addButtonGroup(back, join, create);

    }


    /**
     * Method that is executed when panel is visible
     */
    @Override
    public void afterVisible() {
        ArrayList<LobbyListItem> lobbyCache = new ArrayList<>();
        //Thread to detect lobbies on server
        new Thread(() -> {
            while (true) {
                //get lobby infos from server
                lobbyInfo = DetectLobby.getLobbyInfo(serverAddress);
                if (lobbyInfo == null) {
                    toastError("Server nicht" , "erreichbar");
                    MB.show(new ServerView(), false);
                }
                LobbyInfo.SingleLobbyInfo[] lobbies = lobbyInfo.lobbies;
                //Create LobbyListItem for every lobby in LobbyInfo
                for (LobbyInfo.SingleLobbyInfo lobby : lobbies) {
                    LobbyListItem listItem = new LobbyListItem(lobby.name, lobby.players, lobby.gameMode, lobby.status);
                    lobbyCache.add(listItem);
                }
                //clear ListView and show new lobbies
                list.removeAllItems();
                lobbyCache.forEach(lobby -> list.addItem(lobby));
                lobbyCache.clear();
                //Set button visibility
                spinner.setVisible(false);
                back.setVisible(true);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void setPlayerName (String name) {
        this.playerName = name;
        MB.settings.playerName = name;
        MB.settings.saveSettings();
    }
    public void setCreateLobbyName (String name) {
        this.createLobbyName = name;
    }

    /**
     * JoinLobby Method
     */
    public int joinLobby () {
        //create JoinLobby message
        JoinLobby msg = new JoinLobby();
        msg.playerID = playerName;
        msg.lobbyName = lobbyInfo.lobbies[selectedLobby].name;


        HttpClient httpClient = HttpClient.newBuilder().build();
        HttpRequest request = HttpRequest.newBuilder().POST(
                HttpRequest.BodyPublishers.ofByteArray(msg.toJson().getBytes()))
                .uri(URI.create("http://" + serverAddress + ":" + Server.HTTP_PORT +"/lobby"))
                .build();

        int statusCode = 1;
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                ErrorMessage error = (ErrorMessage) Message.fromJson(response.body());
                toastError(error.error, Integer.toString(response.statusCode()));
            }
            statusCode = response.statusCode();

        } catch (Exception e) {
            e.printStackTrace();
            toastError("Nachricht konnte nicht", "an Server gesendet werden");
        }
        return statusCode;
    }

    public int createLobby(CreateLobby msg) {

        HttpClient httpClient = HttpClient.newBuilder().build();
        HttpRequest request = HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofByteArray(msg.toJson().getBytes())).uri(URI.create("http://" + serverAddress + ":" + Server.HTTP_PORT + "/lobby")).build();
        HttpResponse<String> response = null;

        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                ErrorMessage error = (ErrorMessage) Message.fromJson(response.body());
                toastError(error.error, Integer.toString(response.statusCode()));
            }

        } catch (Exception e) {
            e.printStackTrace();
            toastError("Nachricht konnte nicht", "an Server gesendet werden");

        }
        return response.statusCode();
    }




    private static class LobbyListItem extends MBListView.Item{
        /**
         * Properties of each lobby
         */
        String name, gameMode, status;
        /**
         * Count of connected players
         */
        int players;
        /**
         * Label for lobby name
         */
        MBLabel nameLabel;
        /**
         * Label for lobby description
         */
        MBLabel descriptionLabel;


        public LobbyListItem(String name, int players, String gameMode, String status) {
            super(name);

            this.name = name;
            this.gameMode = gameMode;
            this.status = status;
            this.players = players;

            setBounds(0,0,400,100);
            setLayout(null);

            //Description for lobby
            String description = "Players " + players + "/8 - Gamemode" + gameMode + " - Status " + status;

            this.nameLabel = new MBLabel(MBLabel.H2, name);
            this.descriptionLabel = new MBLabel(MBLabel.H2, description);
            add(nameLabel);
            add(descriptionLabel);
        }

        @Override
        public void onResize(int y, int width) {
            setBounds(0, y, width, 150);
            nameLabel.setBounds(20, 10, 400, 40);
            descriptionLabel.setBounds(20, 40, width , 40);
        }

        @Override
        public void onSelected(int index) {
            LobbyView.selected = true;
            LobbyView.selectedLobby = index;
        }
    }
}