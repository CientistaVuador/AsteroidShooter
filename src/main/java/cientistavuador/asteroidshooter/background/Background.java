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
package cientistavuador.asteroidshooter.background;

import cientistavuador.asteroidshooter.Main;
import cientistavuador.asteroidshooter.geometry.Geometries;
import cientistavuador.asteroidshooter.shader.BackgroundProgram;
import cientistavuador.asteroidshooter.texture.Textures;
import static org.lwjgl.opengl.GL33C.*;

/**
 *
 * @author Cien
 */
public class Background {

    public Background() {

    }

    public void loop() {
        float scaleX = 1f;
        float scaleY = 1f;

        int windowWidth = Main.WIDTH;
        int windowHeight = Main.HEIGHT;

        if (windowWidth != windowHeight) {
            if (windowWidth > windowHeight) {
                scaleY = windowHeight / ((float)windowWidth);
            } else {
                scaleX = windowWidth / ((float)windowHeight);
            }
        }
        
        glUseProgram(BackgroundProgram.SHADER_PROGRAM);
        BackgroundProgram.sendUniforms(scaleX, scaleY, Textures.PLANET_BACKGROUND);
        glBindVertexArray(Geometries.BACKGROUND.getVAO());

        glDrawElements(GL_TRIANGLES, Geometries.BACKGROUND.getAmountOfIndices(), GL_UNSIGNED_INT, 0);

        Main.NUMBER_OF_DRAWCALLS++;
        Main.NUMBER_OF_VERTICES += Geometries.BACKGROUND.getAmountOfIndices();

        glBindVertexArray(0);
        glUseProgram(0);
    }

}
