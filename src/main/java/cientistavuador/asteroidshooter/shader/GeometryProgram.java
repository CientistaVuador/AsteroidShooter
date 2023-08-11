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
package cientistavuador.asteroidshooter.shader;

import cientistavuador.asteroidshooter.util.BetterUniformSetter;
import cientistavuador.asteroidshooter.util.ProgramCompiler;
import org.joml.Matrix4f;
import static org.lwjgl.opengl.GL33C.*;

/**
 *
 * @author Cien
 */
public class GeometryProgram {
    
    public static final int SHADER_PROGRAM = ProgramCompiler.compile(
            """
            #version 330 core
            
            uniform mat4 projectionView;
            uniform mat4 model;
            
            layout (location = 0) in vec3 vertexPosition;
            layout (location = 1) in vec2 vertexUv;
            layout (location = 2) in vec3 vertexNormal;
            
            out vec3 position;
            out vec2 uv;
            out vec3 normal;
            
            void main() {
                vec4 pos = model * vec4(vertexPosition, 1.0);
                
                position = pos.xyz;
                uv = vertexUv;
                normal = vertexNormal;
                
                gl_Position = projectionView * pos;
            }
            """
            ,
            """
            #version 330 core
            
            uniform sampler2D tex;
            
            in vec3 position;
            in vec2 uv;
            in vec3 normal;
            
            layout (location = 0) out vec4 colorOutput;
            
            void main() {
                colorOutput = texture(tex, uv);
            }
            """
    );
    
    public static final BetterUniformSetter UNIFORMS = new BetterUniformSetter(SHADER_PROGRAM, 
            "projectionView", "model", "tex"
    );
    
    public static void sendUniforms(Matrix4f projectionView, Matrix4f model, int texture) {
        BetterUniformSetter.uniformMatrix4fv(UNIFORMS.locationOf("projectionView"), projectionView);
        BetterUniformSetter.uniformMatrix4fv(UNIFORMS.locationOf("model"), model);
        
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, texture);
        glUniform1i(UNIFORMS.locationOf("tex"), 0);
    }
    
    private GeometryProgram() {
        
    }
    
}
