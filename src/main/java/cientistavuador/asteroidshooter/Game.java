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
import cientistavuador.asteroidshooter.sound.Sounds;
import cientistavuador.asteroidshooter.spaceship.Spaceship;
import cientistavuador.asteroidshooter.ubo.CameraUBO;
import cientistavuador.asteroidshooter.ubo.UBOBindingPoints;
import java.nio.FloatBuffer;
import org.joml.Matrix4f;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.openal.AL11.*;
import static org.lwjgl.opengl.GL33C.*;
import org.lwjgl.system.MemoryStack;

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
    private final MainMenu mainMenu = new MainMenu();
    private final AudioButton audioButton = new AudioButton();
    private final ControlsMenu controlsMenu = new ControlsMenu();

    private final int clickAudioSource;
    
    private AsteroidController controller = null;
    private Spaceship spaceship = null;
    
    private Game() {
        this.clickAudioSource = alGenSources();
        alSourcei(this.clickAudioSource, AL_BUFFER, Sounds.CLICK.getAudioBuffer());
    }

    public void start() {
        this.camera.setUBO(CameraUBO.create(UBOBindingPoints.PLAYER_CAMERA));
        this.camera.setDimensions(2f, 2f);
        this.camera.setFarPlane(10f);
        this.camera.setNearPlane(-10f);
        this.camera.setPosition(0, 0, 0);
        this.camera.setFront(0f, 0f, -1f);

        this.audioButton.setDebugEnabled(true);
        this.mainMenu.setDebugEnabled(true);
        this.controlsMenu.setDebugEnabled(true);

        this.controlsMenu.setEnabled(false);
    }

    float counter = 0f;

    public void loop() {
        alListener3f(AL_POSITION, (float) this.camera.getPosition().x(), (float) this.camera.getPosition().y(), (float) this.camera.getPosition().z());
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.callocFloat(6);
            this.camera.getFront().get(buffer);
            buffer.position(3);
            this.camera.getUp().get(buffer);
            buffer.position(0);
            alListenerfv(AL_ORIENTATION, buffer);
        }
        
        this.camera.getUBO().updateUBO();

        Matrix4f cameraMatrix = new Matrix4f(camera.getProjectionView());

        if (this.spaceship != null) {
            if (!this.controller.isFrozen()) {
                this.counter += Main.TPF;
                if (this.counter > 1f) {
                    this.controller.spawnAsteroid();
                    this.counter = 0f;
                }
            }

            this.controller.loop(cameraMatrix, this.spaceship);
            this.spaceship.loop(cameraMatrix, this.controller);
            
            if (this.spaceship.isDead()) {
                this.spaceship = null;
                this.controller = null;
                
                this.mainMenu.setEnabled(true);
                this.audioButton.setEnabled(true);
            }
        }
        
        //menu
        this.mainMenu.loop(cameraMatrix);
        this.audioButton.loop(cameraMatrix);
        this.controlsMenu.loop(cameraMatrix);
        
        boolean buttonPressed = false;
        
        if (this.mainMenu.playPressedSignal()) {
            if (this.spaceship == null) {
                this.spaceship = new Spaceship();
                this.controller = new AsteroidController();
                
                this.spaceship.setDebugEnabled(true);
                this.controller.setDebugEnabled(true);
                
                this.spaceship.setAudioEnabled(this.audioButton.isAudioEnabled());
            }

            this.mainMenu.setEnabled(false);
            this.audioButton.setEnabled(false);

            this.controller.setFrozen(false);
            this.spaceship.setFrozen(false);
            
            buttonPressed = true;
        }

        if (this.mainMenu.controlsPressedSignal()) {
            this.mainMenu.setEnabled(false);
            this.controlsMenu.setEnabled(true);
            
            buttonPressed = true;
        }

        if (this.mainMenu.exitPressedSignal()) {
            System.exit(0);
        }

        if (this.controlsMenu.backButtonPressedSignal()) {
            this.mainMenu.setEnabled(true);
            this.controlsMenu.setEnabled(false);
            
            buttonPressed = true;
        }
        
        if (this.audioButton.buttonPressedSignal()) {
            if (this.spaceship != null) {
                this.spaceship.setAudioEnabled(this.audioButton.isAudioEnabled());
            }
            
            buttonPressed = true;
        }
        
        if (buttonPressed && this.audioButton.isAudioEnabled()) {
            if (alGetSourcei(this.clickAudioSource, AL_SOURCE_STATE) == AL_PLAYING) {
                alSourceStop(this.clickAudioSource);
            }
            alSourcePlay(this.clickAudioSource);
        }
        
        AabRender.renderQueue(camera);

        Main.WINDOW_TITLE += " (DrawCalls: " + Main.NUMBER_OF_DRAWCALLS + ", Vertices: " + Main.NUMBER_OF_VERTICES + ")";
    }

    public void mouseCursorMoved(double x, double y) {
        if (this.spaceship != null) {
            this.spaceship.mouseCursorMoved(x, y);
        }
    }

    public void windowSizeChanged(int width, int height) {

    }

    public void keyCallback(long window, int key, int scancode, int action, int mods) {
        if (key == GLFW_KEY_ESCAPE && action == GLFW_PRESS) {
            if (this.spaceship == null) {
                return;
            }
            
            boolean openMenu = true;
            if (this.mainMenu.isEnabled()) {
                this.mainMenu.forcePlayPressedSignal();
                openMenu = false;
            }
            if (this.controlsMenu.isEnabled()) {
                this.controlsMenu.forceBackButtonPressedSignal();
                openMenu = false;
            }
            if (openMenu) {
                this.mainMenu.setEnabled(true);
                this.audioButton.setEnabled(true);
                this.controller.setFrozen(true);
                this.spaceship.setFrozen(true);
            }
        }
    }

    public void mouseCallback(long window, int button, int action, int mods) {
        this.audioButton.mouseCallback(window, button, action, mods);
        this.mainMenu.mouseCallback(window, button, action, mods);
        this.controlsMenu.mouseCallback(window, button, action, mods);
    }
}
