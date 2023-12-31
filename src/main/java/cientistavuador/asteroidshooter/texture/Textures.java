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
package cientistavuador.asteroidshooter.texture;

import static org.lwjgl.opengl.GL33C.*;

/**
 *
 * @author Cien
 */
public class Textures {
    
    public static final int STONE;
    public static final int SPACESHIP;
    public static final int LASER;
    public static final int AUDIO_OFF;
    public static final int AUDIO_ON;
    public static final int BUTTON;
    public static final int CONTROLS;
    public static final int TITLE;
    public static final int BUTTON_HOVER;
    public static final int PLANET_BACKGROUND;
    public static final int SPACESHIP_ICON;
    public static final int SPACESHIP_ICON_DESTROYED;
    
    static {
        int[] textures = TexturesLoader.load(
                "stone.png",
                "spaceship.png",
                "laser.png",
                "audio_off.png",
                "audio_on.png",
                "button.png",
                "controls.png",
                "title.png",
                "button_hover.png",
                "planet_background.png",
                "spaceship_icon.png",
                "spaceship_icon_destroyed.png"
        );
        
        STONE = textures[0];
        SPACESHIP = textures[1];
        LASER = textures[2];
        AUDIO_OFF = textures[3];
        AUDIO_ON = textures[4];
        BUTTON = textures[5];
        CONTROLS = textures[6];
        TITLE = textures[7];
        BUTTON_HOVER = textures[8];
        PLANET_BACKGROUND = textures[9];
        SPACESHIP_ICON = textures[10];
        SPACESHIP_ICON_DESTROYED = textures[11];
        
        glActiveTexture(GL_TEXTURE0);
        
        glBindTexture(GL_TEXTURE_2D, BUTTON);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        
        glBindTexture(GL_TEXTURE_2D, BUTTON_HOVER);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        
        glBindTexture(GL_TEXTURE_2D, CONTROLS);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        
        glBindTexture(GL_TEXTURE_2D, PLANET_BACKGROUND);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        
        glBindTexture(GL_TEXTURE_2D, SPACESHIP_ICON);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        
        glBindTexture(GL_TEXTURE_2D, SPACESHIP_ICON_DESTROYED);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        
        glBindTexture(GL_TEXTURE_2D, 0);
    }
    
    public static void init() {
        
    }
    
    private Textures() {
        
    }
}
