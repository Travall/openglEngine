package game;

import engine.items.GameItem;
import engine.IHUD;
import engine.items.TextItem;
import engine.Window;
import engine.graph.FontTexture;
import engine.graph.Material;
import engine.graph.Mesh;
import engine.graph.OBJLoader;
import org.joml.Vector4f;

import java.awt.*;

public class HUD implements IHUD {

    private static final Font FONT = new Font("Ubuntu", Font.PLAIN, 24);

    private static final String CHARSET = "ISO-8859-1";

    private final GameItem[] gameItems;

    private final TextItem statusTextItem;

    public HUD(String statusText) throws Exception {
        FontTexture fontTexture = new FontTexture(FONT, CHARSET);
        this.statusTextItem = new TextItem(statusText, fontTexture);
        this.statusTextItem.getMesh().getMaterial().setAmbientColour(new Vector4f(1, 1, 1, 1));

        // Create list that holds the items that compose the HUD
        gameItems = new GameItem[]{statusTextItem};
    }

    public void setStatusText(String statusText) {
        this.statusTextItem.setText(statusText);
    }

    @Override
    public GameItem[] getGameItems() {
        return gameItems;
    }

    public void updateSize(Window window) {
        this.statusTextItem.setPosition(2.5f, 2.5f, 0);
    }
}
