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
package cientistavuador.asteroidshooter.asteroid;

import cientistavuador.asteroidshooter.Main;
import cientistavuador.asteroidshooter.geometry.Geometries;
import cientistavuador.asteroidshooter.shader.GeometryProgram;
import cientistavuador.asteroidshooter.spaceship.Spaceship;
import cientistavuador.asteroidshooter.texture.Textures;
import cientistavuador.asteroidshooter.util.ALSourceUtil;
import cientistavuador.asteroidshooter.util.Aab;
import java.util.ArrayList;
import java.util.List;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import static org.lwjgl.opengl.GL33C.*;
import static org.lwjgl.openal.AL11.*;

/**
 *
 * @author Cien
 */
public class AsteroidController {

    public static final int MIN_AMOUNT_OF_DEBRIS = 5;
    public static final int MAX_AMOUNT_OF_DEBRIS = 8;
    public static final float ASTEROID_SPAWN_TIME = 0.35f;

    public static final Aab DEATH_ZONE = new Aab() {

        private final Vector3f min = new Vector3f(-0.45f, -0.45f, 0.0f);
        private final Vector3f max = new Vector3f(0.45f, 0.45f, 0.0f);

        @Override
        public void getMin(Vector3f min) {
            min.set(this.min);
        }

        @Override
        public void getMax(Vector3f max) {
            max.set(this.max);
        }
    };

    private final List<Asteroid> asteroids = new ArrayList<>();
    private final List<AsteroidDebris> asteroidsDebris = new ArrayList<>();
    private boolean debugEnabled = false;
    private boolean audioEnabled = true;
    private boolean frozen = false;
    private float asteroidSpawnCounter = 0f;
    private float deathAsteroidCounter = -10f;

    public AsteroidController() {

    }

    public boolean isFrozen() {
        return frozen;
    }

    public void setFrozen(boolean frozen) {
        this.frozen = frozen;
        for (Asteroid s : this.asteroids) {
            s.setFrozen(frozen);
        }
        for (AsteroidDebris s : this.asteroidsDebris) {
            s.setFrozen(frozen);
        }
    }

    public boolean isAudioEnabled() {
        return audioEnabled;
    }

    public void setAudioEnabled(boolean audioEnabled) {
        this.audioEnabled = audioEnabled;
    }

    public boolean isDebugEnabled() {
        return debugEnabled;
    }

    public void setDebugEnabled(boolean debugEnabled) {
        this.debugEnabled = debugEnabled;
    }

    public Asteroid spawnAsteroid(Spaceship ship, boolean deathAsteroid) {
        float distance = 1.4f;
        if (deathAsteroid) {
            distance = 5f;
        }

        Asteroid asteroid = null;

        Vector3f initialPosition = new Vector3f();
        Vector3f finalPosition = new Vector3f();
        for (int i = 0; i < 5; i++) {
            initialPosition
                    .set((Math.random() * 2f) - 1f, (Math.random() * 2f) - 1f, 0)
                    .normalize(distance);

            if (deathAsteroid) {
                finalPosition
                        .set(ship.getPosition())
                        .sub(initialPosition)
                        .normalize(distance);
            } else {
                finalPosition
                        .set(initialPosition)
                        .negate()
                        .normalize()
                        .rotateZ((float) ((Math.random() - 0.5) * Math.PI))
                        .mul(distance);
            }

            if (deathAsteroid) {
                asteroid = new DeathAsteroid(this, initialPosition, finalPosition);
            } else {
                asteroid = new Asteroid(this, initialPosition, finalPosition);
            }

            boolean collision = false;
            for (Asteroid other : this.asteroids) {
                if (asteroid.testAab2D(other)) {
                    collision = true;
                    break;
                }
            }
            if (!collision) {
                break;
            }
        }

        if (asteroid == null) {
            throw new NullPointerException("Impossible NPE.");
        }

        asteroid.setFrozen(this.frozen);
        this.asteroids.add(asteroid);
        return asteroid;
    }

    public void createAsteroidExplosion(int debrisMultiplier, int audioBuffer, float pitch, float posX, float posY, float posZ) {
        if (this.audioEnabled && audioBuffer != 0) {
            int explosionAudio = alGenSources();
            alSourcei(explosionAudio, AL_BUFFER, audioBuffer);
            alSourcef(explosionAudio, AL_PITCH, pitch);
            alSource3f(explosionAudio, AL_POSITION, posX, posY, posZ);
            alSourcePlay(explosionAudio);
            ALSourceUtil.deleteWhenStopped(explosionAudio, null);
        }

        int amountOfDebris = (int) Math.floor(MIN_AMOUNT_OF_DEBRIS + ((MAX_AMOUNT_OF_DEBRIS - MIN_AMOUNT_OF_DEBRIS) * Math.random()));

        amountOfDebris *= debrisMultiplier;

        for (int i = 0; i < amountOfDebris; i++) {
            AsteroidDebris debris = new AsteroidDebris(this, posX, posY, posZ);
            this.asteroidsDebris.add(debris);
        }
    }
    
    public List<Asteroid> getAsteroids() {
        return asteroids;
    }

    public void loop(Matrix4f projectionView, Spaceship ship) {
        if (!this.frozen) {
            this.asteroidSpawnCounter += Main.TPF;
            if (this.asteroidSpawnCounter > 0.5f) {
                spawnAsteroid(ship, false);
                this.asteroidSpawnCounter = 0f;
            }

            if (DEATH_ZONE.testAab2D(ship)) {
                this.deathAsteroidCounter += Main.TPF;
                if (this.deathAsteroidCounter >= 2f) {
                    this.deathAsteroidCounter = 0f;
                    if (Math.random() <= 0.1f) {
                        ship.onDeathAsteroidIncoming(spawnAsteroid(ship, true));
                    }
                }
            } else {
                this.deathAsteroidCounter = 0f;
            }
        }

        if (isDebugEnabled()) {
            DEATH_ZONE.queueAabRender();
        }

        GeometryProgram.INSTANCE.use();
        GeometryProgram.INSTANCE.setProjectionView(projectionView);
        GeometryProgram.INSTANCE.setTextureUnit(0);
        GeometryProgram.INSTANCE.setColor(1f, 1f, 1f, 1f);
        GeometryProgram.INSTANCE.setLightingEnabled(true);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, Textures.STONE);

        glBindVertexArray(Geometries.ASTEROID.getVAO());
        
        Asteroid[] copy = this.asteroids.toArray(Asteroid[]::new);
        for (Asteroid a : copy) {
            if (a.shouldBeRemoved()) {
                this.asteroids.remove(a);
                continue;
            }
            a.loop(ship);
            if (isDebugEnabled()) {
                a.queueAabRender();
            }
        }

        AsteroidDebris[] debrisCopy = this.asteroidsDebris.toArray(AsteroidDebris[]::new);
        for (AsteroidDebris a : debrisCopy) {
            if (a.shouldBeRemoved()) {
                this.asteroidsDebris.remove(a);
                continue;
            }
            a.loop();
        }

        glBindVertexArray(0);
        glUseProgram(0);
    }

}
