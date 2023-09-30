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
import org.joml.Vector4f;
import org.joml.Vector4fc;
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
            
            uniform vec4 color;
            uniform sampler2D tex;
            
            in vec3 position;
            in vec2 uv;
            in vec3 normal;
            
            layout (location = 0) out vec4 colorOutput;
            
            void main() {
                colorOutput = texture(tex, uv) * color;
            }
            """
    );
    
    private static final BetterUniformSetter UNIFORMS = new BetterUniformSetter(SHADER_PROGRAM, 
            "projectionView", "model", "color", "tex"
    );
    
    public static final GeometryProgram INSTANCE = new GeometryProgram();
    
    private final Matrix4f projectionView = new Matrix4f();
    private final Matrix4f model = new Matrix4f();
    private final Vector4f color = new Vector4f();
    private int textureUnit = 0;
    
    private GeometryProgram() {
        
    }

    public void use() {
        glUseProgram(SHADER_PROGRAM);
    }
    
    public Matrix4f getProjectionView() {
        return projectionView;
    }

    public Matrix4f getModel() {
        return model;
    }

    public int getTextureUnit() {
        return textureUnit;
    }

    public Vector4fc getColor() {
        return color;
    }
    
    public void setProjectionView(Matrix4f projectionView) {
        this.projectionView.set(projectionView);
        BetterUniformSetter.uniformMatrix4fv(UNIFORMS.locationOf("projectionView"), projectionView);
    }
    
    public void setModel(Matrix4f model) {
        this.model.set(model);
        BetterUniformSetter.uniformMatrix4fv(UNIFORMS.locationOf("model"), model);
    }
    
    public void setTextureUnit(int unit) {
        this.textureUnit = unit;
        glUniform1i(UNIFORMS.locationOf("tex"), unit);
    }
    
    public void setColor(float r, float g, float b, float a) {
        this.color.set(r, g, b, a);
        glUniform4f(UNIFORMS.locationOf("color"), r, g, b, a);
    }
    
    public void setColor(Vector4fc color) {
        setColor(color.x(), color.y(), color.z(), color.w());
    }
    
}
