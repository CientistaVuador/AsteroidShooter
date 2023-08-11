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
import cientistavuador.asteroidshooter.texture.Textures;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import static org.lwjgl.opengl.GL33C.*;

/**
 *
 * @author Cien
 */
public class Asteroid {
    
    public static final float ASTEROID_SCALE = 0.3f;
    
    private final AsteroidController controller;
    private final Vector3f position = new Vector3f();
    private final Matrix4f model = new Matrix4f();
    
    private final float rotationX = (float) Math.toRadians(Math.random() * 360f);
    private final float rotationY = (float) Math.toRadians(Math.random() * 360f);
    private float rotationZ = 0f;
    
    public Asteroid(AsteroidController controller) {
        this.controller = controller;
    }

    public AsteroidController getController() {
        return controller;
    }
    
    public Vector3f getPosition() {
        return position;
    }
    
    public void loop(Matrix4f projectionView) {
        this.rotationZ += Main.TPF;
        if (this.rotationZ > Math.PI * 2f) {
            this.rotationZ = 0f;
        }
        this.model.identity().translate(this.position).scale(ASTEROID_SCALE).rotateX(this.rotationX).rotateY(this.rotationY).rotateZ(this.rotationZ);
        
        GeometryProgram.sendUniforms(projectionView, this.model, Textures.STONE);
        glDrawElements(GL_TRIANGLES, Geometries.ASTEROID_COUNT, GL_UNSIGNED_INT, 0);
        
        Main.NUMBER_OF_DRAWCALLS++;
        Main.NUMBER_OF_VERTICES += Geometries.ASTEROID_COUNT;
    }
    
}
