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
import cientistavuador.asteroidshooter.asteroid.Asteroid;
import cientistavuador.asteroidshooter.asteroid.AsteroidController;
import cientistavuador.asteroidshooter.geometry.Geometries;
import cientistavuador.asteroidshooter.shader.GeometryProgram;
import cientistavuador.asteroidshooter.texture.Textures;
import cientistavuador.asteroidshooter.util.Aab;
import java.util.ArrayList;
import java.util.List;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33C.*;

/**
 *
 * @author Cien
 */
public class Spaceship implements Aab {

    public static final Aab SCREEN_AAB = new Aab() {
        @Override
        public void getMin(Vector3f min) {
            min.set(-1f, -1f, 0f);
        }

        @Override
        public void getMax(Vector3f max) {
            max.set(1f, 1f, 0f);
        }
    };
    
    public static final float SPACESHIP_RENDER_SCALE = 0.2f;
    public static final float SPACESHIP_WIDTH = 0.14f;
    public static final float SPACESHIP_HEIGHT = 0.14f;

    public static final float SPACESHIP_SHOT_DELAY = 0.4f;

    public static final float SPEED = 0.8f;

    private final Matrix4f model = new Matrix4f();
    private final Vector3f position = new Vector3f();
    private final Vector3f direction = new Vector3f();

    private final List<LaserShot> laserShots = new ArrayList<>();

    private float cursorX = 0f;
    private float cursorY = 0f;
    
    private float rotation = 0f;
    private boolean debugEnabled = false;
    private float nextShot = 0f;
    private boolean frozen = false;
    private boolean dead = false;
    private boolean audioEnabled = true;
    
    public Spaceship() {

    }

    public boolean isAudioEnabled() {
        return audioEnabled;
    }

    public void setAudioEnabled(boolean audioEnabled) {
        this.audioEnabled = audioEnabled;
        for (LaserShot s:this.laserShots) {
            s.setAudioEnabled(audioEnabled);
        }
    }

    public boolean isFrozen() {
        return frozen;
    }

    public void setFrozen(boolean frozen) {
        this.frozen = frozen;
        for (LaserShot s:this.laserShots) {
            s.setFrozen(frozen);
        }
    }

    public boolean isDead() {
        return dead;
    }
    
    public boolean isDebugEnabled() {
        return debugEnabled;
    }

    public void setDebugEnabled(boolean debugEnabled) {
        this.debugEnabled = debugEnabled;
    }

    public Vector3fc getPosition() {
        return position;
    }

    public Vector3fc getDirection() {
        return direction;
    }

    public List<LaserShot> getLaserShots() {
        return laserShots;
    }
    
    public void onAsteroidHit(Asteroid s) {
        this.dead = true;
        for (LaserShot e:this.laserShots) {
            e.cleanup();
        }
    }

    public void loop(Matrix4f projectionView, AsteroidController asteroids) {
        if (!this.frozen) {
            if (this.nextShot > 0f) {
                this.nextShot -= Main.TPF;
            }

            float value = (float) (Main.TPF * SPEED);
            if (glfwGetKey(Main.WINDOW_POINTER, GLFW_KEY_W) == GLFW_PRESS) {
                this.position.add(0, value, 0);
                if (!this.testAab2D(SCREEN_AAB)) {
                    this.position.add(0, -value, 0);
                }
            }
            if (glfwGetKey(Main.WINDOW_POINTER, GLFW_KEY_A) == GLFW_PRESS) {
                this.position.add(-value, 0, 0);
                if (!this.testAab2D(SCREEN_AAB)) {
                    this.position.add(value, 0, 0);
                }
            }
            if (glfwGetKey(Main.WINDOW_POINTER, GLFW_KEY_S) == GLFW_PRESS) {
                this.position.add(0, -value, 0);
                if (!this.testAab2D(SCREEN_AAB)) {
                    this.position.add(0, value, 0);
                }
            }
            if (glfwGetKey(Main.WINDOW_POINTER, GLFW_KEY_D) == GLFW_PRESS) {
                this.position.add(value, 0, 0);
                if (!this.testAab2D(SCREEN_AAB)) {
                    this.position.add(-value, 0, 0);
                }
            }
            
            float cursorXPos = this.cursorX / Main.WIDTH;
            float cursorYPos = this.cursorY / Main.HEIGHT;
            cursorYPos = 1f - cursorYPos;
            cursorXPos = (cursorXPos - 0.5f) * 2f;
            cursorYPos = (cursorYPos - 0.5f) * 2f;

            this.direction.set(cursorXPos, cursorYPos, 0f).sub(this.position).normalize();
            if (!this.direction.isFinite()) {
                this.direction.set(0, 1, 0);
            }
            this.rotation = (float) Math.atan2(-this.direction.x(), this.direction.y());

            this.model
                    .identity()
                    .translate(this.position)
                    .scale(SPACESHIP_RENDER_SCALE)
                    .rotateZ(this.rotation); 
            
            if (glfwGetKey(Main.WINDOW_POINTER, GLFW_KEY_SPACE) == GLFW_PRESS && this.nextShot <= 0f && !this.dead) {
                this.nextShot = SPACESHIP_SHOT_DELAY;
                LaserShot shot = new LaserShot(this, this.position, this.direction, this.audioEnabled);
                shot.setFrozen(this.frozen);
                this.laserShots.add(shot);
            }
        }

        List<LaserShot> copy = new ArrayList<>(this.laserShots);
        glUseProgram(GeometryProgram.SHADER_PROGRAM);
        glBindVertexArray(Geometries.LASER.getVAO());
        for (LaserShot s : copy) {
            if (s.shouldBeRemoved()) {
                this.laserShots.remove(s);
                continue;
            }
            s.loop(projectionView, asteroids);
            if (this.debugEnabled) {
                s.queueAabRender();
            }
        }
        glBindVertexArray(0);
        glUseProgram(0);

        glUseProgram(GeometryProgram.SHADER_PROGRAM);
        GeometryProgram.sendUniforms(projectionView, this.model, Textures.SPACESHIP);
        glBindVertexArray(Geometries.SPACESHIP.getVAO());
        glDrawElements(GL_TRIANGLES, Geometries.SPACESHIP.getAmountOfIndices(), GL_UNSIGNED_INT, 0);
        glBindVertexArray(0);
        glUseProgram(0);

        Main.NUMBER_OF_DRAWCALLS++;
        Main.NUMBER_OF_VERTICES += Geometries.SPACESHIP.getAmountOfIndices();

        if (this.debugEnabled) {
            this.queueAabRender();
        }
    }

    public void mouseCursorMoved(double x, double y) {
        this.cursorX = (float) x;
        this.cursorY = (float) y;
    }

    @Override
    public void getMin(Vector3f min) {
        min.set(
                this.position.x() - (SPACESHIP_WIDTH / 2f),
                this.position.y() - (SPACESHIP_HEIGHT / 2f),
                this.position.z()
        );
    }

    @Override
    public void getMax(Vector3f max) {
        max.set(
                this.position.x() + (SPACESHIP_WIDTH / 2f),
                this.position.y() + (SPACESHIP_HEIGHT / 2f),
                this.position.z()
        );
    }
}
