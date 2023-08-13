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
import cientistavuador.asteroidshooter.geometry.Geometries;
import cientistavuador.asteroidshooter.shader.GeometryProgram;
import cientistavuador.asteroidshooter.texture.Textures;
import cientistavuador.asteroidshooter.util.Aab;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import static org.lwjgl.opengl.GL33C.*;

/**
 *
 * @author Cien
 */
public class LaserShot implements Aab {

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
    
    public static final float LASER_RENDER_SCALE = 0.025f;
    public static final float LASER_WIDTH = 0.025f;
    public static final float LASER_HEIGHT = 0.025f;
    
    public static final float SPEED = 2.0f;
    
    private final Spaceship spaceship;
    private final Vector3f position = new Vector3f();
    private final Vector3f direction = new Vector3f();
    
    private final Matrix4f model = new Matrix4f();
    
    public LaserShot(Spaceship spaceship, Vector3fc position, Vector3fc direction) {
        this.spaceship = spaceship;
        this.position.set(position);
        this.direction.set(direction);
    }

    public Spaceship getSpaceship() {
        return spaceship;
    }
    
    public Vector3fc getPosition() {
        return position;
    }
    
    public boolean shouldBeRemoved() {
        return !SCREEN_AAB.testAab(this);
    }
    
    public void loop(Matrix4f projectionView) {
        this.position.add(
                (float) (this.direction.x() * Main.TPF * SPEED),
                (float) (this.direction.y() * Main.TPF * SPEED),
                (float) (this.direction.z() * Main.TPF * SPEED)
        );
        
        this.model
                .identity()
                .translate(this.position)
                .scale(LASER_RENDER_SCALE);
        
        GeometryProgram.sendUniforms(projectionView, this.model, Textures.LASER);
        glDrawElements(GL_TRIANGLES, Geometries.LASER.getAmountOfIndices(), GL_UNSIGNED_INT, 0);
        
        Main.NUMBER_OF_DRAWCALLS++;
        Main.NUMBER_OF_VERTICES += Geometries.LASER.getAmountOfIndices();
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
