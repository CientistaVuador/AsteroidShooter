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
import cientistavuador.asteroidshooter.spaceship.LaserShot;
import cientistavuador.asteroidshooter.spaceship.SpaceshipController;
import cientistavuador.asteroidshooter.util.Aab;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import static org.lwjgl.opengl.GL33C.*;

/**
 *
 * @author Cien
 */
public class Asteroid implements Aab {

    public static final float ASTEROID_RENDER_SCALE = 0.15f;
    public static final float ASTEROID_WIDTH = 0.12f;
    public static final float ASTEROID_HEIGHT = 0.12f;
    public static final float ASTEROID_SPEED = 0.25f;

    public static final float ASTEROID_MIN_HEALTH = 90f;
    public static final float ASTEROID_MAX_HEALTH = 140f;

    public static final float ASTEROID_HIT_TIME = 0.2f;

    private final AsteroidController controller;
    private final Matrix4f model = new Matrix4f();

    private final float rotationX = (float) Math.toRadians(Math.random() * 360f);
    private final float rotationY = (float) Math.toRadians(Math.random() * 360f);
    private float rotationZ = 0f;

    private final Vector3f initialPosition = new Vector3f();
    private final Vector3f finalPosition = new Vector3f();
    private float currentPosition = 0f;

    private final Vector3f position = new Vector3f();

    private final float initialHealth = (float) (ASTEROID_MIN_HEALTH + ((ASTEROID_MAX_HEALTH - ASTEROID_MIN_HEALTH) * Math.random()));
    private float health = this.initialHealth;

    private boolean frozen = false;
    private float hitTime = 0.0f;

    protected float speed = ASTEROID_SPEED;
    protected float rotationSpeed = 1f;

    protected Asteroid(AsteroidController controller, Vector3fc initialPosition, Vector3fc finalPosition) {
        this.initialPosition.set(initialPosition);
        this.finalPosition.set(finalPosition);
        this.position.set(initialPosition);
        this.controller = controller;
    }

    public boolean isFrozen() {
        return frozen;
    }

    protected void setFrozen(boolean frozen) {
        this.frozen = frozen;
    }

    public AsteroidController getController() {
        return controller;
    }

    public Vector3fc getInitialPosition() {
        return initialPosition;
    }

    public Vector3fc getFinalPosition() {
        return finalPosition;
    }

    public float getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(float currentPosition) {
        this.currentPosition = currentPosition;
    }

    public Vector3fc getPosition() {
        return position;
    }

    public float getHealth() {
        return health;
    }

    public void setHealth(float health) {
        this.health = health;
    }

    public float getInitialHealth() {
        return initialHealth;
    }

    public float getHitTime() {
        return hitTime;
    }

    public void setHitTime(float hitTime) {
        this.hitTime = hitTime;
    }

    public float getSpeed() {
        return speed;
    }

    public float getRotationSpeed() {
        return rotationSpeed;
    }

    public boolean shouldBeRemoved() {
        return this.currentPosition >= 1f || this.health <= 0f;
    }

    public void onLaserHit(LaserShot shot) {
        if (this.health <= 0f) {
            return;
        }
        this.hitTime = ASTEROID_HIT_TIME;

        float shotDamage = shot.getDamageWithFalloff();
        boolean criticalHit = shotDamage > this.initialHealth;

        this.health -= shot.getDamageWithFalloff();
        if (this.health <= 0f) {
            this.controller.onAsteroidDestroyed(this, shot, criticalHit);
        }
    }

    public void onAsteroidHitByAnotherAsteroid(Asteroid asteroid) {
        if (this.health <= 0f) {
            return;
        }
        this.health = 0f;
        this.controller.onAsteroidDestroyed(this, asteroid, false);
    }

    public void loop(SpaceshipController spaceshipController) {
        if (!this.frozen) {
            this.currentPosition += (float) (Main.TPF * this.speed);

            float x = (this.initialPosition.x() * this.currentPosition) + (this.finalPosition.x() * (1f - this.currentPosition));
            float y = (this.initialPosition.y() * this.currentPosition) + (this.finalPosition.y() * (1f - this.currentPosition));
            float z = (this.initialPosition.z() * this.currentPosition) + (this.finalPosition.z() * (1f - this.currentPosition));
            this.position.set(x, y, z);

            this.rotationZ += (Main.TPF * this.rotationSpeed);
            if (this.rotationZ > Math.PI * 2f) {
                this.rotationZ = 0f;
            }
            this.model
                    .identity()
                    .translate(this.position)
                    .scale(ASTEROID_RENDER_SCALE)
                    .rotateX(this.rotationX)
                    .rotateY(this.rotationY)
                    .rotateZ(this.rotationZ);

            if (spaceshipController.isSpaceshipAlive() && spaceshipController.getSpaceship().testAab2D(this)) {
                spaceshipController.getSpaceship().onAsteroidHit(this);
            }

            for (Asteroid s : this.controller.getAsteroids()) {
                if (!s.equals(this) && s.testAab2D(this)) {
                    this.onAsteroidHitByAnotherAsteroid(s);
                    s.onAsteroidHitByAnotherAsteroid(this);
                    break;
                }
            }
        }

        this.hitTime -= Main.TPF;
        if (this.hitTime < 0f) {
            this.hitTime = 0f;
        }
        float hitColor = this.hitTime / ASTEROID_HIT_TIME;
        if (hitColor != 0) {
            GeometryProgram.INSTANCE.setColor(1f, 1f - hitColor, 1f - hitColor, 1f);
        }
        GeometryProgram.INSTANCE.setModel(this.model);
        glDrawElements(GL_TRIANGLES, Geometries.ASTEROID.getAmountOfIndices(), GL_UNSIGNED_INT, 0);
        if (hitColor != 0) {
            GeometryProgram.INSTANCE.setColor(1f, 1f, 1f, 1f);
        }

        Main.NUMBER_OF_DRAWCALLS++;
        Main.NUMBER_OF_VERTICES += Geometries.ASTEROID.getAmountOfIndices();
    }

    @Override
    public void getMin(Vector3f min) {
        min.set(
                this.position.x() - (ASTEROID_WIDTH / 2f),
                this.position.y() - (ASTEROID_HEIGHT / 2f),
                this.position.z()
        );
    }

    @Override
    public void getMax(Vector3f max) {
        max.set(
                this.position.x() + (ASTEROID_WIDTH / 2f),
                this.position.y() + (ASTEROID_HEIGHT / 2f),
                this.position.z()
        );
    }

}
