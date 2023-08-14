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
import static org.lwjgl.opengl.GL33C.*;
import static org.lwjgl.glfw.GLFW.*;

/**
 *
 * @author Cien
 */
public class MainMenu {
    
    private static final class ButtonAab implements Aab {
        private final Vector3f min = new Vector3f();
        private final Vector3f max = new Vector3f();

        public ButtonAab(Matrix4f matrix) {
            min.set(-0.5f, -0.5f, 0f);
            max.set(0.5f, 0.5f, 0f);
            
            matrix.transformProject(this.min);
            matrix.transformProject(this.max);
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
    
    private static final Matrix4f titleModel = new Matrix4f()
            .translate(0f, 0.70f, 2f)
            .scale(1.2f, 0.5f, 1f)
            ;
    
    private static final Matrix4f playModel = new Matrix4f()
            .translate(0f, 0.2f, 2f)
            .scale(0.70f, 0.40f, 1f)
            ;
    private static final ButtonAab playAab = new ButtonAab(MainMenu.playModel);
    
    private static final Matrix4f controlsModel = new Matrix4f()
            .translate(0f, -0.25f, 2f)
            .scale(0.70f, 0.40f, 1f)
            ;
    private static final ButtonAab controlsAab = new ButtonAab(MainMenu.controlsModel);
    
    private static final Matrix4f exitModel = new Matrix4f()
            .translate(0f, -0.70f, 2f)
            .scale(0.70f, 0.40f, 1f)
            ;
    private static final ButtonAab exitAab = new ButtonAab(MainMenu.exitModel);
    
    private boolean enabled = true;
    
    private boolean debugEnabled = false;
    
    private boolean playSignal = false;
    private boolean controlsSignal = false;
    private boolean exitSignal = false;
    
    public MainMenu() {
        
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
    
    public boolean playPressedSignal() {
        if (this.playSignal) {
            this.playSignal = false;
            return true;
        }
        return false;
    }
    
    public boolean controlsPressedSignal() {
        if (this.controlsSignal) {
            this.controlsSignal = false;
            return true;
        }
        return false;
    }
    
    public boolean exitPressedSignal() {
        if (this.exitSignal) {
            this.exitSignal = false;
            return true;
        }
        return false;
    }
    
    public void loop(Matrix4f projectionView) {
        glUseProgram(GeometryProgram.SHADER_PROGRAM);
        glBindVertexArray(Geometries.GUI.getVAO());
        
        GeometryProgram.sendUniforms(projectionView, MainMenu.titleModel, Textures.TITLE);
        glDrawElements(GL_TRIANGLES, Geometries.GUI.getAmountOfIndices(), GL_UNSIGNED_INT, 0);
        
        GeometryProgram.sendUniforms(projectionView, MainMenu.playModel, Textures.BUTTON);
        glDrawElements(GL_TRIANGLES, Geometries.GUI.getAmountOfIndices(), GL_UNSIGNED_INT, 0);
        
        GeometryProgram.sendUniforms(projectionView, MainMenu.controlsModel, Textures.BUTTON);
        glDrawElements(GL_TRIANGLES, Geometries.GUI.getAmountOfIndices(), GL_UNSIGNED_INT, 0);
        
        GeometryProgram.sendUniforms(projectionView, MainMenu.exitModel, Textures.BUTTON);
        glDrawElements(GL_TRIANGLES, Geometries.GUI.getAmountOfIndices(), GL_UNSIGNED_INT, 0);
        
        Main.NUMBER_OF_DRAWCALLS += 3;
        Main.NUMBER_OF_VERTICES += (Geometries.GUI.getAmountOfIndices() * 3);
        
        glBindVertexArray(0);
        glUseProgram(0);
        
        if (this.debugEnabled) {
            MainMenu.playAab.queueAabRender();
            MainMenu.controlsAab.queueAabRender();
            MainMenu.exitAab.queueAabRender();
        }
    }
    
    public void mouseCallback(long window, int button, int action, int mods) {
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
            
            if (MainMenu.playAab.testAab2D(mouse)) {
                this.playSignal = true;
            }
            if (MainMenu.controlsAab.testAab2D(mouse)) {
                this.controlsSignal = true;
            }
            if (MainMenu.exitAab.testAab2D(mouse)) {
                this.exitSignal = true;
            }
        }
    }
    
}