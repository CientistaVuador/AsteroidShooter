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
import cientistavuador.asteroidshooter.resources.mesh.MeshData;
import cientistavuador.asteroidshooter.shader.GeometryProgram;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import static org.lwjgl.opengl.GL33C.*;

/**
 *
 * @author Cien
 */
public class SpaceshipDebris {
    public static enum DebrisType {
        BODY(Geometries.DEBRIS_BODY),
        COCKPIT(Geometries.DEBRIS_COCKPIT),
        LEFTWING(Geometries.DEBRIS_LEFTWING),
        RIGHTWING(Geometries.DEBRIS_RIGHTWING)
        ;
        
        private final MeshData mesh;
        private DebrisType(MeshData mesh) {
            this.mesh = mesh;
        }

        public MeshData mesh() {
            return mesh;
        }
        
    }
    
    public static final float SPACESHIP_DEBRIS_SPEED = 0.4f;
    public static final float SPACESHIP_DEBRIS_RENDER_SCALE = 0.02f;
    public static final float SPACESHIP_DEBRIS_FADE_TIME = 3f;
    private final DebrisType type;
    
    private final SpaceshipController spaceshipController;
    private final Vector3f position = new Vector3f();
    private final Vector3f direction = new Vector3f();
    private final Matrix4f model = new Matrix4f();
    
    private final float rotationX = (float) Math.toRadians(Math.random() * 360f);
    private final float rotationY = (float) Math.toRadians(Math.random() * 360f);
    private float rotationZ = 0f;
    
    private float fadeTime = SPACESHIP_DEBRIS_FADE_TIME;
    private boolean frozen = false;

    public SpaceshipDebris(DebrisType type, SpaceshipController spaceshipController, float x, float y, float z, float dirX, float dirY, float dirZ) {
        this.type = type;
        this.spaceshipController = spaceshipController;
        this.position.set(x, y, z);
        this.direction.set(dirX, dirY, dirZ);
    }

    public DebrisType getType() {
        return type;
    }

    public SpaceshipController getSpaceshipController() {
        return spaceshipController;
    }

    public Vector3fc getPosition() {
        return position;
    }

    public Vector3fc getDirection() {
        return direction;
    }
    
    public boolean isFrozen() {
        return frozen;
    }

    protected void setFrozen(boolean frozen) {
        this.frozen = frozen;
    }
    
    public boolean shouldBeRemoved() {
        return this.fadeTime <= 0f;
    }
    
    public void loop() {
        if (!this.frozen) {
            this.fadeTime -= Main.TPF;
            
            float x = (float) (this.direction.x() * SPACESHIP_DEBRIS_SPEED * Main.TPF);
            float y = (float) (this.direction.y() * SPACESHIP_DEBRIS_SPEED * Main.TPF);
            float z = (float) (this.direction.z() * SPACESHIP_DEBRIS_SPEED * Main.TPF);
            this.position.add(x, y, z);

            this.rotationZ += (Main.TPF * 2f);
            if (this.rotationZ > Math.PI * 2f) {
                this.rotationZ = 0f;
            }
            this.model
                    .identity()
                    .translate(this.position)
                    .scale(SPACESHIP_DEBRIS_RENDER_SCALE)
                    .rotateX(this.rotationX)
                    .rotateY(this.rotationY)
                    .rotateZ(this.rotationZ);
        }
        
        float opacity = this.fadeTime / SPACESHIP_DEBRIS_FADE_TIME;
        GeometryProgram.INSTANCE.setColor(1f, 1f, 1f, opacity);
        GeometryProgram.INSTANCE.setModel(this.model);
        
        glBindVertexArray(this.type.mesh().getVAO());
        glDrawElements(GL_TRIANGLES, this.type.mesh().getAmountOfIndices(), GL_UNSIGNED_INT, 0);
        glBindVertexArray(0);

        Main.NUMBER_OF_DRAWCALLS++;
        Main.NUMBER_OF_VERTICES += this.type.mesh().getAmountOfIndices();
    }
    
}
