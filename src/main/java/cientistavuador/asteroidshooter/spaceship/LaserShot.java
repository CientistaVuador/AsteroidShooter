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
import cientistavuador.asteroidshooter.sound.Sounds;
import cientistavuador.asteroidshooter.util.ALSourceUtil;
import cientistavuador.asteroidshooter.util.Aab;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import static org.lwjgl.opengl.GL33C.*;
import static org.lwjgl.openal.AL11.*;

/**
 *
 * @author Cien
 */
public class LaserShot implements Aab {

    public static final float LASER_RENDER_SCALE = 0.030f;
    public static final float LASER_WIDTH = 0.030f;
    public static final float LASER_HEIGHT = 0.030f;

    public static final float LASER_SPEED = 2.5f;

    public static final float LASER_MIN_DAMAGE = 10f;
    public static final float LASER_MAX_DAMAGE = 25f;
    public static final float LASER_MAX_DAMAGE_WITH_FALLOFF = 10000f;

    private final Spaceship spaceship;
    private final Vector3f position = new Vector3f();
    private final Vector3f direction = new Vector3f();

    private final Matrix4f model = new Matrix4f();

    private final float damage = (float) (LASER_MIN_DAMAGE + ((LASER_MAX_DAMAGE - LASER_MIN_DAMAGE) * Math.random()));

    private final GeometryProgram.PointLight laserLight = new GeometryProgram.PointLight();

    private int laserShotAudioSource;
    private boolean hitAsteroidOrScreen = false;
    private boolean frozen = false;
    private boolean audioEnabled = true;

    protected LaserShot(Spaceship spaceship, Vector3fc position, Vector3fc direction, boolean audioEnabled) {
        this.spaceship = spaceship;
        this.position.set(position);
        this.direction.set(direction);
        this.audioEnabled = audioEnabled;
        if (audioEnabled) {
            this.laserShotAudioSource = alGenSources();
            alSourcei(this.laserShotAudioSource, AL_BUFFER, Sounds.LASER.getAudioBuffer());
            alSource3f(this.laserShotAudioSource, AL_POSITION, this.position.x(), this.position.y(), this.position.z());
            alSourcePlay(this.laserShotAudioSource);
            ALSourceUtil.deleteWhenStopped(this.laserShotAudioSource, () -> {
                this.laserShotAudioSource = 0;
            });
        } else {
            this.laserShotAudioSource = 0;
        }
        this.laserLight.setPosition(position);
        this.laserLight.setAmbient(0.0f, 0.008f, 0.0f);
        this.laserLight.setDiffuse(0.0f, 0.020f, 0.0f);
        GeometryProgram.INSTANCE.getLights().add(this.laserLight);
    }

    public boolean isAudioEnabled() {
        return audioEnabled;
    }

    protected void setAudioEnabled(boolean audioEnabled) {
        this.audioEnabled = audioEnabled;
    }

    public boolean isFrozen() {
        return frozen;
    }

    protected void setFrozen(boolean frozen) {
        this.frozen = frozen;
    }

    public Spaceship getSpaceship() {
        return spaceship;
    }

    public Vector3fc getPosition() {
        return position;
    }

    public float getDamage() {
        return damage;
    }

    public float getDamageWithFalloff() {
        float d = this.damage / this.position.distance(this.spaceship.getPosition());
        if (Float.isInfinite(d) || Float.isNaN(d)) {
            d = LASER_MAX_DAMAGE_WITH_FALLOFF;
        }
        d = Float.min(d, LASER_MAX_DAMAGE_WITH_FALLOFF);
        return d;
    }

    public void onLaserRemoved() {
        GeometryProgram.INSTANCE.getLights().remove(this.laserLight);
    }

    public boolean shouldBeRemoved() {
        this.hitAsteroidOrScreen = !Spaceship.SCREEN_AAB.testAab2D(this) || this.hitAsteroidOrScreen;
        return this.hitAsteroidOrScreen;
    }

    public void loop(AsteroidController asteroids) {
        if (this.hitAsteroidOrScreen) {
            return;
        }

        if (!this.frozen) {
            this.position.add((float) (this.direction.x() * Main.TPF * LASER_SPEED),
                    (float) (this.direction.y() * Main.TPF * LASER_SPEED),
                    (float) (this.direction.z() * Main.TPF * LASER_SPEED)
            );

            this.laserLight.setPosition(this.position);

            if (this.laserShotAudioSource != 0) {
                alSource3f(this.laserShotAudioSource, AL_VELOCITY, this.direction.x() * LASER_SPEED, this.direction.y() * LASER_SPEED, 0f);
                alSource3f(this.laserShotAudioSource, AL_POSITION, this.position.x(), this.position.y(), this.position.z());
            }

            this.model
                    .identity()
                    .translate(this.position)
                    .scale(LASER_RENDER_SCALE);

            for (Asteroid s : asteroids.getAsteroids()) {
                if (s.testAab2D(this)) {
                    this.hitAsteroidOrScreen = true;
                    if (this.audioEnabled) {
                        int hitAudio = alGenSources();
                        alSourcei(hitAudio, AL_BUFFER, Sounds.HIT.getAudioBuffer());
                        alSource3f(hitAudio, AL_POSITION, this.position.x(), this.position.y(), this.position.z());
                        alSourcePlay(hitAudio);
                        ALSourceUtil.deleteWhenStopped(hitAudio, null);
                    }
                    s.onLaserHit(this);
                    break;
                }
            }
        }

        GeometryProgram.INSTANCE.setModel(this.model);
        glDrawElements(GL_TRIANGLES, Geometries.LASER.getAmountOfIndices(), GL_UNSIGNED_INT, 0);

        Main.NUMBER_OF_DRAWCALLS++;
        Main.NUMBER_OF_VERTICES += Geometries.LASER.getAmountOfIndices();
    }

    @Override
    public void queueAabRender() {
        if (this.hitAsteroidOrScreen) {
            return;
        }
        Aab.super.queueAabRender();
    }

    @Override
    public void getMin(Vector3f min) {
        min.set(
                this.position.x() - (LASER_WIDTH / 2f),
                this.position.y() - (LASER_HEIGHT / 2f),
                this.position.z()
        );
    }

    @Override
    public void getMax(Vector3f max) {
        max.set(
                this.position.x() + (LASER_WIDTH / 2f),
                this.position.y() + (LASER_HEIGHT / 2f),
                this.position.z()
        );
    }

}
