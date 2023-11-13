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
package cientistavuador.asteroidshooter.sound;

import cientistavuador.asteroidshooter.resources.audio.NativeAudio;

/**
 *
 * @author Cien
 */
public class Sounds {
    
    public static final NativeAudio LASER;
    public static final NativeAudio CLICK;
    public static final NativeAudio HIT;
    public static final NativeAudio EXPLOSION;
    public static final NativeAudio ROCK_HIT;
    public static final NativeAudio ALARM;
    public static final NativeAudio PARTY_WHISTLE;
    public static final NativeAudio SPACESHIP_EXPLOSION;
    public static final NativeAudio GAME_OVER;
    
    static {
        NativeAudio[] sounds = SoundLoader.load(new String[] {
            "laser.ogg",
            "click.ogg",
            "hit.ogg",
            "explosion.ogg",
            "rock_hit.ogg",
            "alarm.ogg",
            "whistle.ogg",
            "spaceship_explosion.ogg",
            "game_over.ogg"
        });
        
        LASER = sounds[0];
        CLICK = sounds[1];
        HIT = sounds[2];
        EXPLOSION = sounds[3];
        ROCK_HIT = sounds[4];
        ALARM = sounds[5];
        PARTY_WHISTLE = sounds[6];
        SPACESHIP_EXPLOSION = sounds[7];
        GAME_OVER = sounds[8];
    }
    
    public static void init() {
        
    }
    
    private Sounds() {
        
    }
    
}
