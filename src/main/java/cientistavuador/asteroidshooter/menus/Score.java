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

import cientistavuador.asteroidshooter.asteroid.Asteroid;
import cientistavuador.asteroidshooter.asteroid.DeathAsteroid;
import cientistavuador.asteroidshooter.sound.Sounds;
import cientistavuador.asteroidshooter.text.GLFontRenderer;
import cientistavuador.asteroidshooter.text.GLFontSpecification;
import cientistavuador.asteroidshooter.text.GLFontSpecifications;
import cientistavuador.asteroidshooter.util.ALSourceUtil;
import org.joml.Matrix4f;
import static org.lwjgl.openal.AL11.*;

/**
 *
 * @author Cien
 */
public class Score {

    public static final int DEFAULT_HIGHEST_SCORE = 31700;
    public static final int BASE_SCORE = 10000;

    public static final GLFontSpecification SCORE_FONT = GLFontSpecifications.TEKTUR_REGULAR_0_06_BLUISH_WHITE.withSize(0.08f);
    public static final GLFontSpecification HIGHEST_SCORE_FONT = GLFontSpecifications.TEKTUR_REGULAR_0_06_BLUISH_WHITE.withSize(0.04f);
    public static final GLFontSpecification SCORE_FONT_BLACK = SCORE_FONT.withColor(0f, 0f, 0f, 1f);
    public static final GLFontSpecification HIGHEST_SCORE_FONT_BLACK = HIGHEST_SCORE_FONT.withColor(0f, 0f, 0f, 1f);

    private int score = 0;
    private int highestScore = DEFAULT_HIGHEST_SCORE;
    private boolean defaultScoreBeaten = false;
    private boolean scoreBeaten = false;
    private boolean audioEnabled = true;

    public Score() {

    }

    public int getScore() {
        return score;
    }

    public int getHighestScore() {
        return highestScore;
    }

    public boolean isAudioEnabled() {
        return audioEnabled;
    }

    public void setAudioEnabled(boolean audioEnabled) {
        this.audioEnabled = audioEnabled;
    }

    public void onGameOver() {
        this.score = 0;
        this.scoreBeaten = false;
    }

    public void onAsteroidDestroyedBySpaceship(Asteroid asteroid, boolean criticalLaserHit) {
        int scoreValue = BASE_SCORE;

        if (asteroid instanceof DeathAsteroid) {
            scoreValue *= 8;
        }

        if (criticalLaserHit) {
            scoreValue *= 4;
        }

        this.score += scoreValue;
        if (this.score > this.highestScore) {
            this.highestScore = this.score;
            if (!this.scoreBeaten) {
                this.defaultScoreBeaten = true;
                this.scoreBeaten = true;

                if (this.audioEnabled) {
                    int whistle = alGenSources();
                    alSourcei(whistle, AL_BUFFER, Sounds.PARTY_WHISTLE.getAudioBuffer());
                    alSourcePlay(whistle);
                    ALSourceUtil.deleteWhenStopped(whistle, null);
                }
            }
        }
    }

    public void loop(Matrix4f projectionView) {
        String[] text = {
            "Score:\n" + this.score + "\n",
            "Highest Score:\n" + this.highestScore + (this.defaultScoreBeaten ? "" : " (By the creator)") + "\n"
        };
        GLFontRenderer.render(-0.985f, -0.005f,
                new GLFontSpecification[]{
                    SCORE_FONT_BLACK,
                    HIGHEST_SCORE_FONT_BLACK
                },
                text
        );
        GLFontRenderer.render(-0.99f, 0.0f,
                new GLFontSpecification[]{
                    SCORE_FONT,
                    HIGHEST_SCORE_FONT
                },
                text
        );
    }

}
