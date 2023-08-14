/*
 * This is free and unencumbered software released into the public domain.
 *
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this software, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any
 * means.
 *
 * In jurisdictions that recognize copyright laws, the author or authors
 * of this software dedicate any and all copyright interest in the
 * software to the public domain. We make this dedication for the benefit
 * of the public at large and to the detriment of our heirs and
 * successors. We intend this dedication to be an overt act of
 * relinquishment in perpetuity of all present and future rights to this
 * software under copyright law.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 * For more information, please refer to <https://unlicense.org>
 */
package cientistavuador.asteroidshooter;

import cientistavuador.asteroidshooter.asteroid.Asteroid;
import cientistavuador.asteroidshooter.asteroid.AsteroidController;
import cientistavuador.asteroidshooter.camera.OrthoCamera;
import cientistavuador.asteroidshooter.camera.PerspectiveCamera;
import cientistavuador.asteroidshooter.debug.AabRender;
import cientistavuador.asteroidshooter.menus.AudioButton;
import cientistavuador.asteroidshooter.menus.ControlsMenu;
import cientistavuador.asteroidshooter.menus.MainMenu;
import cientistavuador.asteroidshooter.spaceship.Spaceship;
import cientistavuador.asteroidshooter.ubo.CameraUBO;
import cientistavuador.asteroidshooter.ubo.UBOBindingPoints;
import org.joml.Matrix4f;
import static org.lwjgl.opengl.GL33C.*;

/**
 *
 * @author Cien
 */
public class Game {

    private static final Game GAME = new Game();

    public static Game get() {
        return GAME;
    }
    
    private final OrthoCamera camera = new OrthoCamera();
    private final AsteroidController controller = new AsteroidController();
    private final Spaceship spaceship = new Spaceship();
    private final MainMenu mainMenu = new MainMenu();
    private final AudioButton audioButton = new AudioButton();
    private final ControlsMenu controlsMenu = new ControlsMenu();
    
    private Game() {
        
    }

    public void start() {
        camera.setUBO(CameraUBO.create(UBOBindingPoints.PLAYER_CAMERA));
        camera.setDimensions(2f, 2f);
        camera.setFarPlane(10f);
        camera.setNearPlane(-10f);
        camera.setPosition(0, 0, 0);
        camera.setFront(0f, 0f, -1f);
        
        controller.setDebugEnabled(true);
        spaceship.setDebugEnabled(true);
        audioButton.setDebugEnabled(true);
        mainMenu.setDebugEnabled(true);
        controlsMenu.setDebugEnabled(true);
    }
    
    float counter = 0f;
    
    public void loop() {
        counter += Main.TPF;
        if (counter > 1f) {
            this.controller.spawnAsteroid();
            counter = 0f;
        }
        camera.getUBO().updateUBO();
        
        Matrix4f cameraMatrix = new Matrix4f(camera.getProjectionView());
        
        controller.loop(cameraMatrix);
        spaceship.loop(cameraMatrix, this.controller);
        //mainMenu.loop(cameraMatrix);
        audioButton.loop(cameraMatrix);
        controlsMenu.loop(cameraMatrix);
        
        if (mainMenu.exitPressedSignal()) {
            System.exit(0);
        }
        
        AabRender.renderQueue(camera);
        
        Main.WINDOW_TITLE += " (DrawCalls: " + Main.NUMBER_OF_DRAWCALLS + ", Vertices: " + Main.NUMBER_OF_VERTICES + ")";
    }

    public void mouseCursorMoved(double x, double y) {
        this.spaceship.mouseCursorMoved(x, y);
    }

    public void windowSizeChanged(int width, int height) {
        
    }

    public void keyCallback(long window, int key, int scancode, int action, int mods) {
        
    }

    public void mouseCallback(long window, int button, int action, int mods) {
        this.audioButton.mouseCallback(window, button, action, mods);
        //this.mainMenu.mouseCallback(window, button, action, mods);
        this.controlsMenu.mouseCallback(window, button, action, mods);
    }
}
