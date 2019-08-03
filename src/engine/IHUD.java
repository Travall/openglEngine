package engine;

import engine.items.GameItem;

public interface IHUD {

    GameItem[] getGameItems();

    default void cleanUp() {
        GameItem[] gameItems = getGameItems();
        for (GameItem gameItem : gameItems) {
            gameItem.getMesh().cleanUp();
        }
    }
}
