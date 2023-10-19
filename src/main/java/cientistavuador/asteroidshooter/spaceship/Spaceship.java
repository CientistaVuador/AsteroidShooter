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
import cientistavuador.asteroidshooter.asteroid.DeathAsteroid;
import cientistavuador.asteroidshooter.geometry.Geometries;
import cientistavuador.asteroidshooter.shader.GeometryProgram;
import cientistavuador.asteroidshooter.sound.Sounds;
import cientistavuador.asteroidshooter.texture.Textures;
import cientistavuador.asteroidshooter.util.ALSourceUtil;
import cientistavuador.asteroidshooter.util.Aab;
import java.util.ArrayList;
import java.util.List;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.openal.AL10.AL_BUFFER;
import static org.lwjgl.openal.AL10.alGenSources;
import static org.lwjgl.openal.AL10.alSourcePlay;
import static org.lwjgl.openal.AL10.alSourcei;
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

    public static final float SPACESHIP_RENDER_SCALE = 0.02f;
    public static final float SPACESHIP_WIDTH = 0.12f;
    public static final float SPACESHIP_HEIGHT = 0.12f;

    public static final float SPACESHIP_SHOT_DELAY = 0.3f;
    public static final Vector2fc SPACESHIP_SHOT_LEFT_OFFSET = new Vector2f(-0.0225f, 0.09f);
    public static final Vector2fc SPACESHIP_SHOT_RIGHT_OFFSET = new Vector2f(0.0225f, 0.09f);
    public static final Vector2fc SPACESHIP_DEATH_ZONE_ALERT_OFFSET = new Vector2f(0f, -0.078f);
    public static final Vector2fc SPACESHIP_DEATH_ASTEROID_ALARM_OFFSET = new Vector2f(0f, -0.024f);
    public static final float SPACESHIP_DEATH_ASTEROID_ALARM_TIME = 1f;

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
    private boolean shotLeft = false;
    private GeometryProgram.PointLight deathZoneAlert = null;
    private GeometryProgram.PointLight deathAsteroidAlarm = null;
    private float deathAsteroidAlarmTime = 0.0f;

    public Spaceship() {

    }

    public boolean isAudioEnabled() {
        return audioEnabled;
    }

    public void setAudioEnabled(boolean audioEnabled) {
        this.audioEnabled = audioEnabled;
        for (LaserShot s : this.laserShots) {
            s.setAudioEnabled(audioEnabled);
        }
    }

    public boolean isFrozen() {
        return frozen;
    }

    public void setFrozen(boolean frozen) {
        this.frozen = frozen;
        for (LaserShot s : this.laserShots) {
            s.setFrozen(frozen);
        }
    }

    public void onSpaceshipRemoved() {
        for (LaserShot s : this.laserShots) {
            s.onLaserRemoved();
        }
        GeometryProgram.INSTANCE.unregisterPointLight(this.deathZoneAlert);
        GeometryProgram.INSTANCE.unregisterPointLight(this.deathAsteroidAlarm);
        this.deathZoneAlert = null;
        this.deathAsteroidAlarm = null;
    }

    public void onDeathAsteroidIncoming(Asteroid asteroid) {
        this.deathAsteroidAlarmTime = SPACESHIP_DEATH_ASTEROID_ALARM_TIME;
        if (this.deathAsteroidAlarm == null) {
            this.deathAsteroidAlarm = GeometryProgram.INSTANCE.registerPointLight();
        }
        if (this.audioEnabled) {
            int asteroidAlarm = alGenSources();
            alSourcei(asteroidAlarm, AL_BUFFER, Sounds.ALARM.getAudioBuffer());
            alSourcePlay(asteroidAlarm);
            ALSourceUtil.deleteWhenStopped(asteroidAlarm, null);
        }
    }

    public boolean shouldBeRemoved() {
        return this.dead;
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
    }

    public void loop(Matrix4f projectionView, AsteroidController asteroids) {
        float scaleX = 1f;
        float scaleY = 1f;

        int windowWidth = Main.WIDTH;
        int windowHeight = Main.HEIGHT;

        if (windowWidth != windowHeight) {
            if (windowWidth > windowHeight) {
                scaleX = windowHeight / ((float) windowWidth);
            } else {
                scaleY = windowWidth / ((float) windowHeight);
            }
        }

        if (this.testAab2D(AsteroidController.DEATH_ZONE)) {
            if (this.deathZoneAlert == null) {
                this.deathZoneAlert = GeometryProgram.INSTANCE.registerPointLight();
                if (this.deathZoneAlert != null) {
                    this.deathZoneAlert.setAmbient(0.0008f / 2f, 0.0008f / 2f, 0.0f);
                    this.deathZoneAlert.setDiffuse(0.0020f / 2f, 0.0020f / 2f, 0.0f);
                }
            }
            if (this.deathZoneAlert != null) {
                Vector3f pos = new Vector3f()
                        .set(SPACESHIP_DEATH_ZONE_ALERT_OFFSET.x(), SPACESHIP_DEATH_ZONE_ALERT_OFFSET.y(), 0.02f)
                        .rotateZ(this.rotation)
                        .add(this.position);
                this.deathZoneAlert.setPosition(pos);
            }
        } else {
            GeometryProgram.INSTANCE.unregisterPointLight(this.deathZoneAlert);
            this.deathZoneAlert = null;
        }

        if (this.deathAsteroidAlarm != null) {
            Vector3f pos = new Vector3f()
                    .set(SPACESHIP_DEATH_ASTEROID_ALARM_OFFSET.x(), SPACESHIP_DEATH_ASTEROID_ALARM_OFFSET.y(), 0.02f)
                    .rotateZ(this.rotation)
                    .add(this.position);
            this.deathAsteroidAlarm.setPosition(pos);
        }

        if (!this.frozen) {
            if (this.nextShot > 0f) {
                this.nextShot -= Main.TPF;
            }

            this.deathAsteroidAlarmTime -= Main.TPF;
            if (this.deathAsteroidAlarmTime <= 0f) {
                this.deathAsteroidAlarmTime = 0f;
                GeometryProgram.INSTANCE.unregisterPointLight(this.deathAsteroidAlarm);
                this.deathAsteroidAlarm = null;
            }
            if (this.deathAsteroidAlarm != null) {
                float power = this.deathAsteroidAlarmTime / SPACESHIP_DEATH_ASTEROID_ALARM_TIME;
                this.deathAsteroidAlarm.setAmbient(0.0032f * power, 0.0f, 0.0f);
                this.deathAsteroidAlarm.setDiffuse(0.0080f * power, 0.0f, 0.0f);
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
                    .scale(SPACESHIP_RENDER_SCALE * scaleX, SPACESHIP_RENDER_SCALE * scaleY, SPACESHIP_RENDER_SCALE) //.rotateZ(this.rotation)
                    .rotateZ(this.rotation);

            if (glfwGetKey(Main.WINDOW_POINTER, GLFW_KEY_SPACE) == GLFW_PRESS && this.nextShot <= 0f && !this.dead) {
                Vector2fc shotSide;
                if (this.shotLeft) {
                    shotSide = SPACESHIP_SHOT_LEFT_OFFSET;
                } else {
                    shotSide = SPACESHIP_SHOT_RIGHT_OFFSET;
                }
                this.shotLeft = !this.shotLeft;

                this.nextShot = SPACESHIP_SHOT_DELAY;
                LaserShot shot = new LaserShot(this,
                        new Vector3f()
                                .add(shotSide.x(), shotSide.y(), 0f)
                                .rotateZ(this.rotation)
                                .mul(scaleX, scaleY, 1f)
                                .add(this.position),
                        this.direction,
                        this.audioEnabled
                );
                shot.setFrozen(this.frozen);
                this.laserShots.add(shot);
            }

            if (glfwGetKey(Main.WINDOW_POINTER, GLFW_KEY_R) == GLFW_PRESS) {
                this.dead = true;
            }
        }

        GeometryProgram.INSTANCE.use();
        GeometryProgram.INSTANCE.setProjectionView(projectionView);
        GeometryProgram.INSTANCE.setTextureUnit(0);
        GeometryProgram.INSTANCE.setColor(1f, 1f, 1f, 1f);

        glActiveTexture(GL_TEXTURE0);

        //spaceship
        GeometryProgram.INSTANCE.setLightingEnabled(true);
        glBindVertexArray(Geometries.SPACESHIP.getVAO());
        glBindTexture(GL_TEXTURE_2D, Textures.SPACESHIP);
        GeometryProgram.INSTANCE.setModel(this.model);
        glDrawElements(GL_TRIANGLES, Geometries.SPACESHIP.getAmountOfIndices(), GL_UNSIGNED_INT, 0);
        glBindVertexArray(0);
        Main.NUMBER_OF_DRAWCALLS++;
        Main.NUMBER_OF_VERTICES += Geometries.SPACESHIP.getAmountOfIndices();

        //laser shots
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
            if (this.debugEnabled) {
                s.queueAabRender();
            }
        }
        glBindVertexArray(0);

        glUseProgram(0);

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
