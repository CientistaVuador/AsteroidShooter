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
import cientistavuador.asteroidshooter.camera.PerspectiveCamera;
import cientistavuador.asteroidshooter.geometry.Geometries;
import cientistavuador.asteroidshooter.shader.GeometryProgram;
import cientistavuador.asteroidshooter.texture.Textures;
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
    
    private final PerspectiveCamera camera = new PerspectiveCamera();
    private final AsteroidController controller = new AsteroidController();
    
    private Game() {

    }

    public void start() {
        camera.setPosition(0, 0, 1);
        Asteroid asteroid = new Asteroid(this.controller);
        asteroid.getPosition().set(0f, 0f, 0f);
        controller.getAsterois().add(asteroid);
    }
    
    public void loop() {
        controller.loop(new Matrix4f(camera.getProjectionView()));
        
        Main.WINDOW_TITLE += " (DrawCalls: " + Main.NUMBER_OF_DRAWCALLS + ", Vertices: " + Main.NUMBER_OF_VERTICES + ")";
    }

    public void mouseCursorMoved(double x, double y) {
        
    }

    public void windowSizeChanged(int width, int height) {
        
    }

    public void keyCallback(long window, int key, int scancode, int action, int mods) {
        
    }

    public void mouseCallback(long window, int button, int action, int mods) {
        
    }
}
