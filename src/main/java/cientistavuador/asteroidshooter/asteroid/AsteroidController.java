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

import cientistavuador.asteroidshooter.geometry.Geometries;
import cientistavuador.asteroidshooter.shader.GeometryProgram;
import java.util.ArrayList;
import java.util.List;
import org.joml.Matrix4f;
import static org.lwjgl.opengl.GL33C.*;

/**
 *
 * @author Cien
 */
public class AsteroidController {

    private final List<Asteroid> asterois = new ArrayList<>();
    private boolean debugEnabled = false;

    public AsteroidController() {

    }

    public boolean isDebugEnabled() {
        return debugEnabled;
    }

    public void setDebugEnabled(boolean debugEnabled) {
        this.debugEnabled = debugEnabled;
    }
    
    public Asteroid spawnAsteroid() {
        final float xMax = 1f - (Asteroid.ASTEROID_WIDTH / 2f);
        final float xMin = -1f + (Asteroid.ASTEROID_WIDTH / 2f);
        final float yMin = 1.05f + (Asteroid.ASTEROID_HEIGHT / 2f);
        final float yMax = 1.05f + (Asteroid.ASTEROID_HEIGHT * 4f);
        Asteroid asteroid = new Asteroid(this);
        
        for (int i = 0; i < 5; i++) {
            float x = (float) Math.random();
            x *= (xMax - xMin);
            x += xMin;
            float y = (float) Math.random();
            y *= (yMax - yMin);
            y += yMin;

            asteroid.getPosition().set(x, y, 0f);
            
            boolean collision = false;
            for (Asteroid other : this.asterois) {
                if (asteroid.testAab(other)) {
                    collision = true;
                    break;
                }
            }
            if (!collision) {
                break;
            }
        }
        
        this.asterois.add(asteroid);
        return asteroid;
    }
    
    public void onAsteroidFall(Asteroid e) {
        this.asterois.remove(e);
    }

    public List<Asteroid> getAsterois() {
        return asterois;
    }

    public void loop(Matrix4f projectionView) {
        List<Asteroid> copy = new ArrayList<>(this.asterois);
        glUseProgram(GeometryProgram.SHADER_PROGRAM);
        glBindVertexArray(Geometries.ASTEROID);
        for (Asteroid a : copy) {
            if ((a.getPosition().y() + (Asteroid.ASTEROID_HEIGHT / 2f)) < -1f) {
                onAsteroidFall(a);
            }
            a.loop(projectionView);
            if (isDebugEnabled()) {
                a.queueAabRender();
            }
        }
        glBindVertexArray(0);
        glUseProgram(0);
    }

}
