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
import java.util.HashMap;
import org.joml.Matrix3f;
import org.joml.Matrix3fc;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import static org.lwjgl.opengl.GL33C.*;

/**
 *
 * @author Cien
 */
public class GeometryProgram {
    
    public static final int MAX_AMOUNT_OF_LIGHTS = 8;
    
    public static final class PointLight {
        
        private final int index;
        
        private boolean enabled = false;
        private final Vector3f position = new Vector3f();
        private final Vector3f ambient = new Vector3f();
        private final Vector3f diffuse = new Vector3f();
        
        private final int enabledLocation;
        private final int positionLocation;
        private final int ambientLocation;
        private final int diffuseLocation;
        
        private boolean enabledRequiresUpdate = false;
        private boolean positionRequiresUpdate = false;
        private boolean ambientRequiresUpdate = false;
        private boolean diffuseRequiresUpdate = false;
        
        protected PointLight(int index) {
            this.index = index;
            this.enabledLocation = glGetUniformLocation(SHADER_PROGRAM, "lights["+index+"].enabled");
            this.positionLocation = glGetUniformLocation(SHADER_PROGRAM, "lights["+index+"].position");
            this.ambientLocation = glGetUniformLocation(SHADER_PROGRAM, "lights["+index+"].ambient");
            this.diffuseLocation = glGetUniformLocation(SHADER_PROGRAM, "lights["+index+"].diffuse");
        }

        protected int getIndex() {
            return index;
        }

        protected boolean isEnabled() {
            return enabled;
        }

        protected void setEnabled(boolean enabled) {
            this.enabled = enabled;
            this.enabledRequiresUpdate = true;
        }

        public Vector3fc getPosition() {
            return position;
        }
        
        public void setPosition(float x, float y, float z) {
            this.position.set(x, y, z);
            this.positionRequiresUpdate = true;
        }
        
        public void setPosition(Vector3fc position) {
            setPosition(position.x(), position.y(), position.z());
        }

        public Vector3fc getAmbient() {
            return ambient;
        }
        
        public void setAmbient(float r, float g, float b) {
            this.ambient.set(r, g, b);
            this.ambientRequiresUpdate = true;
        }
        
        public void setAmbient(Vector3fc ambient) {
            setAmbient(ambient.x(), ambient.y(), ambient.z());
        }
        
        public Vector3fc getDiffuse() {
            return diffuse;
        }
        
        public void setDiffuse(float r, float g, float b) {
            this.diffuse.set(r, g, b);
            this.diffuseRequiresUpdate = true;
        }
        
        public void setDiffuse(Vector3fc diffuse) {
            setDiffuse(diffuse.x(), diffuse.y(), diffuse.z());
        }
        
        protected void updateUniforms() {
            if (this.enabledRequiresUpdate) {
                glUniform1i(this.enabledLocation, (this.enabled ? 1 : 0));
                this.enabledRequiresUpdate = false;
            }
            if (this.positionRequiresUpdate) {
                glUniform3f(this.positionLocation, this.position.x(), this.position.y(), this.position.z());
                this.positionRequiresUpdate = false;
            }
            if (this.ambientRequiresUpdate) {
                glUniform3f(this.ambientLocation, this.ambient.x(), this.ambient.y(), this.ambient.z());
                this.positionRequiresUpdate = false;
            }
            if (this.diffuseRequiresUpdate) {
                glUniform3f(this.diffuseLocation, this.diffuse.x(), this.diffuse.y(), this.diffuse.z());
                this.diffuseRequiresUpdate = false;
            }
        }
    }
    
    public static final int SHADER_PROGRAM = ProgramCompiler.compile(
            """
            #version 330 core
            
            uniform mat4 projectionView;
            uniform mat4 model;
            uniform mat3 normalModel;
            
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
                normal = normalize(normalModel * vertexNormal);
                
                gl_Position = projectionView * pos;
            }
            """
            ,
            """
            #version 330 core
            
            struct PointLight {
                bool enabled;
                vec3 position;
                vec3 ambient;
                vec3 diffuse;
            };
            
            uniform vec4 color;
            uniform sampler2D tex;
            
            uniform bool lightingEnabled;
            
            uniform vec3 sunDirection;
            uniform vec3 sunAmbient;
            uniform vec3 sunDiffuse;
            
            uniform PointLight lights[MAX_AMOUNT_OF_LIGHTS];
            
            in vec3 position;
            in vec2 uv;
            in vec3 normal;
            
            layout (location = 0) out vec4 colorOutput;
            
            const float gamma = 2.2;
            
            void main() {
                vec4 textureColor = texture(tex, uv);
                colorOutput = textureColor * color;
                if (lightingEnabled) {
                    textureColor.rgb = pow(textureColor.rgb * color.rgb, vec3(gamma));
                    vec3 resultOutput = vec3(0.0);
                    
                    //sun
                    resultOutput += sunDiffuse * max(dot(normal, -sunDirection), 0.0) * textureColor.rgb;
                    resultOutput += sunAmbient * textureColor.rgb;
                    
                    //point lights
                    for (int i = 0; i < MAX_AMOUNT_OF_LIGHTS; i++) {
                        PointLight light = lights[i];
                        if (light.enabled) {
                            vec3 lightDir = normalize(light.position - position);
                            float distance = distance(light.position, position);
                            float attenuation = 1.0 / (distance*distance);
                            
                            resultOutput += light.diffuse * max(dot(normal, lightDir), 0.0) * attenuation * textureColor.rgb;
                            resultOutput += light.ambient * attenuation * textureColor.rgb;
                        }
                    }
                    
                    colorOutput = vec4(pow(resultOutput, vec3(1.0/gamma)), textureColor.a * color.a);
                }
            }
            """,
            new HashMap<>() {{
                put("MAX_AMOUNT_OF_LIGHTS", Integer.toString(MAX_AMOUNT_OF_LIGHTS));
            }}
    );
    
    private static final BetterUniformSetter UNIFORMS = new BetterUniformSetter(SHADER_PROGRAM, 
            "projectionView",
            "model",
            "color",
            "tex",
            "normalModel",
            "sunDirection",
            "sunAmbient",
            "sunDiffuse",
            "lightingEnabled"
    );
    
    public static final GeometryProgram INSTANCE = new GeometryProgram();
    
    private final Matrix4f projectionView = new Matrix4f();
    private final Matrix4f model = new Matrix4f();
    private final Matrix3f normalModel = new Matrix3f();
    private final Vector4f color = new Vector4f();
    private int textureUnit = 0;
    
    private boolean lightingEnabled = false;
    
    private final Vector3f sunDirection = new Vector3f();
    private final Vector3f sunAmbient = new Vector3f();
    private final Vector3f sunDiffuse = new Vector3f();
    
    private final PointLight[] lights = new PointLight[MAX_AMOUNT_OF_LIGHTS];
    
    private GeometryProgram() {
        for (int i = 0; i < this.lights.length; i++) {
            this.lights[i] = new PointLight(i);
        }
    }

    public void use() {
        glUseProgram(SHADER_PROGRAM);
    }
    
    public Matrix4fc getProjectionView() {
        return projectionView;
    }

    public Matrix4fc getModel() {
        return model;
    }
    
    public Matrix3fc getNormalModel() {
        return normalModel;
    }

    public int getTextureUnit() {
        return textureUnit;
    }

    public Vector4fc getColor() {
        return color;
    }

    public Vector3fc getSunDirection() {
        return sunDirection;
    }

    public Vector3fc getSunAmbient() {
        return sunAmbient;
    }

    public Vector3fc getSunDiffuse() {
        return sunDiffuse;
    }
    
    public PointLight registerPointLight() {
        for (int i = 0; i < this.lights.length; i++) {
            PointLight p = this.lights[i];
            if (!p.isEnabled()) {
                p.setEnabled(true);
                p.setPosition(0f, 0f, 0f);
                p.setDiffuse(0.8f, 0.8f, 0.8f);
                p.setAmbient(0.2f, 0.2f, 0.2f);
                return p;
            }
        }
        return null;
    }
    
    public void unregisterPointLight(PointLight p) {
        if (p == null) {
            return;
        }
        p.setEnabled(false);
    }
    
    public void updateLightsUniforms() {
        for (PointLight p:this.lights) {
            p.updateUniforms();
        }
    }

    public boolean isLightingEnabled() {
        return lightingEnabled;
    }
    
    public void setSunDirection(float x, float y, float z) {
        this.sunDirection.set(x, y, z).normalize();
        glUniform3f(UNIFORMS.locationOf("sunDirection"), this.sunDirection.x(), this.sunDirection.y(), this.sunDirection.z());
    }
    
    public void setSunDirection(Vector3f dir) {
        setSunDirection(dir.x(), dir.y(), dir.z());
    }
    
    public void setSunAmbient(float r, float g, float b) {
        this.sunAmbient.set(r, g, b);
        glUniform3f(UNIFORMS.locationOf("sunAmbient"), r, g, b);
    }
    
    public void setSunAmbient(Vector3f ambient) {
        setSunAmbient(ambient.x(), ambient.y(), ambient.z());
    }
    
    public void setSunDiffuse(float r, float g, float b) {
        this.sunDiffuse.set(r, g, b);
        glUniform3f(UNIFORMS.locationOf("sunDiffuse"), r, g, b);
    }
    
    public void setSunDiffuse(Vector3f diffuse) {
        setSunDiffuse(diffuse.x(), diffuse.y(), diffuse.z());
    }

    public void setLightingEnabled(boolean lightingEnabled) {
        this.lightingEnabled = lightingEnabled;
        glUniform1i(UNIFORMS.locationOf("lightingEnabled"), (lightingEnabled ? 1 : 0));
    }
    
    public void setProjectionView(Matrix4f projectionView) {
        this.projectionView.set(projectionView);
        BetterUniformSetter.uniformMatrix4fv(UNIFORMS.locationOf("projectionView"), projectionView);
    }
    
    public void setModel(Matrix4f model) {
        this.normalModel.set(this.model.set(model).invert().transpose());
        this.model.set(model);
        
        BetterUniformSetter.uniformMatrix4fv(UNIFORMS.locationOf("model"), this.model);
        BetterUniformSetter.uniformMatrix3fv(UNIFORMS.locationOf("normalModel"), this.normalModel);
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
