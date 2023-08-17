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
import cientistavuador.asteroidshooter.shader.GeometryProgram;
import cientistavuador.asteroidshooter.texture.Textures;
import cientistavuador.asteroidshooter.util.Aab;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33C.*;

/**
 *
 * @author Cien
 */
public class ControlsMenu {
    private static final Matrix4f controlsModel = new Matrix4f()
            .translate(0f, -0.15f, 2f)
            .scale(1f, 1.5f, 1f)
            ;
    
    private static final Matrix4f backModel = new Matrix4f()
            .translate(0f, 0.80f, 2f)
            .scale(0.60f, 0.30f, 1f)
            ;
    
    private static final Aab backAab = new Aab() {
        private final Vector3f min = new Vector3f();
        private final Vector3f max = new Vector3f();
        
        {
            min.set(-0.5f, -0.5f, 0f);
            max.set(0.5f, 0.5f, 0f);
            
            ControlsMenu.backModel.transformProject(this.min);
            ControlsMenu.backModel.transformProject(this.max);
        }
        
        @Override
        public void getMin(Vector3f min) {
            min.set(this.min);
        }

        @Override
        public void getMax(Vector3f max) {
            max.set(this.max);
        }
    };
    
    private boolean enabled = true;
    
    private boolean debugEnabled = false;
    private boolean backButtonSignal = false;
    
    public ControlsMenu() {
        
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public boolean isDebugEnabled() {
        return debugEnabled;
    }

    public void setDebugEnabled(boolean debugEnabled) {
        this.debugEnabled = debugEnabled;
    }
    
    public boolean backButtonPressedSignal() {
        if (this.backButtonSignal) {
            this.backButtonSignal = false;
            return true;
        }
        return false;
    }
    
    public void forceBackButtonPressedSignal() {
        this.backButtonSignal = true;
    }
    
    public void loop(Matrix4f projectionView) {
        if (!this.enabled) {
            return;
        }
        
        glUseProgram(GeometryProgram.SHADER_PROGRAM);
        glBindVertexArray(Geometries.GUI.getVAO());
        
        GeometryProgram.sendUniforms(projectionView, ControlsMenu.controlsModel, Textures.CONTROLS);
        glDrawElements(GL_TRIANGLES, Geometries.GUI.getAmountOfIndices(), GL_UNSIGNED_INT, 0);
        
        GeometryProgram.sendUniforms(projectionView, ControlsMenu.backModel, Textures.BUTTON);
        glDrawElements(GL_TRIANGLES, Geometries.GUI.getAmountOfIndices(), GL_UNSIGNED_INT, 0);
        
        Main.NUMBER_OF_DRAWCALLS += 2;
        Main.NUMBER_OF_VERTICES += (Geometries.GUI.getAmountOfIndices() * 2);
        
        glBindVertexArray(0);
        glUseProgram(0);
        
        if (this.debugEnabled) {
            backAab.queueAabRender();
        }
    }
    
    public void mouseCallback(long window, int button, int action, int mods) {
        if (!this.enabled) {
            return;
        }
        
        if (button == GLFW_MOUSE_BUTTON_LEFT && action == GLFW_PRESS) {
            Aab mouse = new Aab() {
                private final Vector3f min = new Vector3f(Main.MOUSE_X, Main.MOUSE_Y, 0f);
                private final Vector3f max = min;
                
                @Override
                public void getMin(Vector3f min) {
                    min.set(this.min);
                }

                @Override
                public void getMax(Vector3f max) {
                    max.set(this.max);
                }
            };
            
            if (ControlsMenu.backAab.testAab2D(mouse)) {
                this.backButtonSignal = true;
            }
        }
    }
}
