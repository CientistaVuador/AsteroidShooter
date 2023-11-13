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
package cientistavuador.asteroidshooter.menus;

import cientistavuador.asteroidshooter.Main;
import cientistavuador.asteroidshooter.geometry.Geometries;
import cientistavuador.asteroidshooter.shader.GUIProgram;
import cientistavuador.asteroidshooter.texture.Textures;
import org.joml.Matrix4f;
import static org.lwjgl.opengl.GL33C.*;

/**
 *
 * @author Cien
 */
public class SpaceshipLivesRender {
    
    public static final float SPACESHIP_LIFE_ICON_RENDER_SCALE = 0.2f;
    
    private final Matrix4f lifeRenderMatrix = new Matrix4f();
    
    public SpaceshipLivesRender() {
        
    }
    
    public void loop(Matrix4f projectionView, int lives) {
        //spaceship lives
        GUIProgram.INSTANCE.use();
        GUIProgram.INSTANCE.setProjectionView(projectionView);
        GUIProgram.INSTANCE.setTextureUnit(0);

        glActiveTexture(GL_TEXTURE0);
        glBindVertexArray(Geometries.GUI.getVAO());

        for (int i = 0; i < 3; i++) {
            setRenderMatrix(-1f + -0.1f + ((0.2f + 0.01f) * (i + 1)), 1f + -0.1f + -0.02f);
            GUIProgram.INSTANCE.setModel(this.lifeRenderMatrix);
            glBindTexture(GL_TEXTURE_2D, (lives <= i ? Textures.SPACESHIP_ICON_DESTROYED : Textures.SPACESHIP_ICON));
            glDrawElements(GL_TRIANGLES, Geometries.GUI.getAmountOfIndices(), GL_UNSIGNED_INT, 0);

            Main.NUMBER_OF_DRAWCALLS++;
            Main.NUMBER_OF_VERTICES += Geometries.GUI.getAmountOfIndices();
        }

        glBindVertexArray(0);
        glUseProgram(0);
    }
    
    private void setRenderMatrix(float x, float y) {
        this.lifeRenderMatrix.identity().translate(x, y, 2f).scale(SPACESHIP_LIFE_ICON_RENDER_SCALE);
    }
    
}
