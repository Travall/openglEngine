package game;

import engine.*;
import engine.graph.*;
import engine.graph.lights.DirectionalLight;
import engine.items.GameItem;
import engine.items.SkyBox;
import engine.items.Terrain;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.List;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.*;

public class DummyGame implements IGameLogic {

    private static final float MOUSE_SENSITIVITY = 0.5f;

    private final Vector3f cameraInc;

    private final Renderer renderer;

    private final Camera camera;

    private Scene scene;

    private HUD hud;

    private float lightAngle;

    private static final float CAMERA_POS_STEP = 0.005f;

    private float cameraMovementInc = 1f;

    public DummyGame() {
        renderer = new Renderer();
        camera = new Camera();
        cameraInc = new Vector3f(0.0f, 0.0f, 0.0f);
        lightAngle = -90;
    }

    @Override
    public void init(Window window) throws Exception {
        renderer.init(window);

        scene = new Scene();

        float skyBoxScale = 50.0f;

        HeightMapMesh heightMapMesh = new HeightMapMesh(0, 0, 0, 1, 402,402, 40);

        GameItem terrainBlock = new GameItem(heightMapMesh.getMesh());
        terrainBlock.setPosition(0, 0f, 0);

        GameItem[] gameItems = new GameItem[1];

        gameItems[0] = terrainBlock;

        scene.setGameItems(gameItems);

        // Setup  SkyBox
        SkyBox skyBox = new SkyBox("/resources/models/skybox.obj", "/resources/textures/skybox.png");
        skyBox.setScale(skyBoxScale);
        scene.setSkyBox(skyBox);

        // Setup Lights
        setupLights();

        // Create HUD
        hud = new HUD("DEMO");

        camera.getPosition().z = 0.0f;
        camera.getPosition().y = -0.2f;
        camera.getRotation().x = 10.f;
    }

    private void setupLights() {
        SceneLight sceneLight = new SceneLight();
        scene.setSceneLight(sceneLight);

        // Ambient Light
        sceneLight.setAmbientLight(new Vector3f(0.3f, 0.3f, 0.3f));
        sceneLight.setSkyBoxLight(new Vector3f(0.5f, 0.5f, 0.5f));

        // Directional Light
        float lightIntensity = 1.0f;
        Vector3f lightPosition = new Vector3f(1, 1, 0);
        sceneLight.setDirectionalLight(new DirectionalLight(new Vector3f(1, 1, 1), lightPosition, lightIntensity));
    }

    @Override
    public void input(Window window, MouseInput mouseInput) {
        cameraInc.set(0, 0, 0);
        if (window.isKeyPressed(GLFW_KEY_W)) {
            cameraInc.z -= 0.5f;
        } else if (window.isKeyPressed(GLFW_KEY_S)) {
            cameraInc.z += 0.5f;
        } else {
            if(cameraInc.z > 0f) {
                cameraInc.z -= 0.001f;
            }
        }
        if (window.isKeyPressed(GLFW_KEY_A)) {
            cameraInc.x = -1;
        } else if (window.isKeyPressed(GLFW_KEY_D)) {
            cameraInc.x = 1;
        }
        if (window.isKeyPressed(GLFW_KEY_Q)) {
            cameraInc.y = -1;
        } else if (window.isKeyPressed(GLFW_KEY_E)) {
            cameraInc.y = 1;
        }

        if (window.isKeyPressed(GLFW_KEY_LEFT_SHIFT)) {
            cameraMovementInc = 5f;
        } else {
            cameraMovementInc = 1f;
        }

        if (window.isKeyPressed(GLFW_KEY_SPACE)) {
            scene.resetGameItems();
            HeightMapMesh heightMapMesh = null;
            try {
                heightMapMesh = new HeightMapMesh(0, 0, 0, 1, 402,402, 40);
            } catch (Exception e) {
                e.printStackTrace();
            }

            GameItem terrainBlock = new GameItem(heightMapMesh.getMesh());
            terrainBlock.setPosition(0, 0f, 0);

            GameItem[] gameItems = new GameItem[1];

            gameItems[0] = terrainBlock;

            scene.setGameItems(gameItems);
        }
    }

    @Override
    public void update(float interval, MouseInput mouseInput) {
        // Update camera based on mouse

        if (mouseInput.isRightButtonPressed()) {
            Vector2f rotVec = mouseInput.getDisplVec();
            camera.moveRotation(rotVec.x * MOUSE_SENSITIVITY, rotVec.y * MOUSE_SENSITIVITY, 0);

        }

        // Update camera position
        camera.movePosition(cameraInc.x * CAMERA_POS_STEP * cameraMovementInc, cameraInc.y * CAMERA_POS_STEP * cameraMovementInc, cameraInc.z * CAMERA_POS_STEP * cameraMovementInc);
        // Update directional light direction, intensity and colour
        SceneLight sceneLight = scene.getSceneLight();
        // Update directional light direction, intensity and colour
        DirectionalLight directionalLight = sceneLight.getDirectionalLight();
//        lightAngle += 1.1f;
//        if (lightAngle > 90) {
//            directionalLight.setIntensity(0);
//            if (lightAngle >= 360) {
//                lightAngle = -90;
//            }
//            sceneLight.getSkyBoxLight().set(0.3f, 0.3f, 0.4f);
//        } else if (lightAngle <= -80 || lightAngle >= 80) {
//            float factor = 1 - (float) (Math.abs(lightAngle) - 80) / 10.0f;
//            sceneLight.getSkyBoxLight().set(factor, factor, factor);
//            directionalLight.setIntensity(factor);
//            directionalLight.getColor().y = Math.max(factor, 0.9f);
//            directionalLight.getColor().z = Math.max(factor, 0.5f);
//        } else {
//            sceneLight.getSkyBoxLight().set(1, 1, 1);
//            directionalLight.setIntensity(1);
//            directionalLight.getColor().x = 1;
//            directionalLight.getColor().y = 1;
//            directionalLight.getColor().z = 1;
//        }
        lightAngle = 65;
        double angRad = Math.toRadians(lightAngle);
        directionalLight.getDirection().x = (float) Math.sin(angRad);
        directionalLight.getDirection().y = (float) Math.cos(angRad);
    }

    @Override
    public void render(Window window, float interval) {
        hud.updateSize(window);
        hud.setStatusText("FPS:" + Math.floor(1/interval));
        renderer.render(window, camera, scene, hud);
    }

    @Override
    public void cleanUp() {
        renderer.cleanUp();
        scene.cleanUp();
        if (hud != null) {
            hud.cleanUp();
        }
    }

}
