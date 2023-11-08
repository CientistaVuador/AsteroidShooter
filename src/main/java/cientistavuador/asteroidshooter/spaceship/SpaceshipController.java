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
package cientistavuador.asteroidshooter.spaceship;

import cientistavuador.asteroidshooter.Main;
import cientistavuador.asteroidshooter.asteroid.AsteroidController;
import cientistavuador.asteroidshooter.geometry.Geometries;
import cientistavuador.asteroidshooter.shader.GeometryProgram;
import cientistavuador.asteroidshooter.texture.Textures;
import cientistavuador.asteroidshooter.util.Cursors;
import java.util.ArrayList;
import java.util.List;
import org.joml.Matrix4f;
import static org.lwjgl.opengl.GL33C.*;

/**
 *
 * @author Cien
 */
public class SpaceshipController {

    public static final float SPAWN_DELAY = 3f;

    private Spaceship spaceship = null;
    private final List<LaserShot> laserShots = new ArrayList<>();
    private boolean audioEnabled = true;
    private boolean debugEnabled = false;
    private boolean frozen = false;
    private float spawnDelayCounter = 0f;

    public SpaceshipController() {

    }
    
    private void createSpaceship() {
        this.spaceship = new Spaceship(this);
        this.spaceship.setAudioEnabled(this.audioEnabled);
        this.spaceship.setDebugEnabled(this.debugEnabled);
        this.spaceship.setFrozen(this.frozen);
    }

    public boolean isAudioEnabled() {
        return audioEnabled;
    }

    public void setAudioEnabled(boolean audioEnabled) {
        this.audioEnabled = audioEnabled;
        if (isSpaceshipAlive()) {
            this.spaceship.setAudioEnabled(audioEnabled);
        }
        for (LaserShot s : this.laserShots) {
            s.setAudioEnabled(audioEnabled);
        }
    }

    public boolean isFrozen() {
        return frozen;
    }

    public void setFrozen(boolean frozen) {
        this.frozen = frozen;
        if (isSpaceshipAlive()) {
            this.spaceship.setFrozen(frozen);
        }
        for (LaserShot s : this.laserShots) {
            s.setFrozen(frozen);
        }
    }

    public boolean isDebugEnabled() {
        return debugEnabled;
    }

    public void setDebugEnabled(boolean debugEnabled) {
        this.debugEnabled = debugEnabled;
        if (isSpaceshipAlive()) {
            this.spaceship.setDebugEnabled(debugEnabled);
        }
    }

    public List<LaserShot> getLaserShots() {
        return laserShots;
    }

    public Spaceship getSpaceship() {
        return spaceship;
    }

    public boolean isSpaceshipAlive() {
        return this.spaceship != null;
    }

    public boolean isSpaceshipDestroyed() {
        return this.spaceship == null;
    }

    public void loop(Matrix4f projectionView, AsteroidController asteroids) {
        if (!this.frozen) {
            this.spawnDelayCounter -= Main.TPF;
            if (this.spawnDelayCounter <= 0f) {
                this.spawnDelayCounter = 0f;
                if (isSpaceshipDestroyed() && asteroids.getAsteroids().isEmpty()) {
                    createSpaceship();
                }
            }
            
            if (isSpaceshipAlive()) {
                Cursors.setCursor(Cursors.StandardCursor.CROSSHAIR);
            }
        }
        
        if (isSpaceshipAlive()) {
            this.spaceship.loop(projectionView, asteroids);
            if (this.spaceship.shouldBeRemoved()) {
                this.spaceship.onSpaceshipRemoved();
                this.spaceship = null;
                this.spawnDelayCounter = SPAWN_DELAY;
            }
        }

        //laser shots
        GeometryProgram.INSTANCE.use();
        GeometryProgram.INSTANCE.setProjectionView(projectionView);
        GeometryProgram.INSTANCE.setTextureUnit(0);
        GeometryProgram.INSTANCE.setColor(1f, 1f, 1f, 1f);

        glActiveTexture(GL_TEXTURE0);

        GeometryProgram.INSTANCE.setLightingEnabled(false);
        List<LaserShot> copy = new ArrayList<>(this.laserShots);
        glBindVertexArray(Geometries.LASER.getVAO());
        glBindTexture(GL_TEXTURE_2D, Textures.LASER);
        for (LaserShot s : copy) {
            if (s.shouldBeRemoved()) {
                this.laserShots.remove(s);
                s.onLaserRemoved();
                continue;
            }
            s.loop(asteroids);
            if (isDebugEnabled()) {
                s.queueAabRender();
            }
        }
        glBindVertexArray(0);

        glUseProgram(0);
    }

    public void mouseCursorMoved(double x, double y) {
        if (isSpaceshipAlive()) {
            this.spaceship.mouseCursorMoved(x, y);
        }
    }

}
