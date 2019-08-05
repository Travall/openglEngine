package game;

import engine.GameEngine;
import engine.IGameLogic;

import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) {
        try {
            boolean vSync = true;
//            TimeUnit.SECONDS.sleep(15);
            IGameLogic gameLogic = new DummyGame();
            GameEngine gameEng = new GameEngine("GAME", 600, 480, vSync, gameLogic);
            gameEng.start();
        } catch (Exception excp) {
            excp.printStackTrace();
            System.exit(-1);
        }
    }
}
