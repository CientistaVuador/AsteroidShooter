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
package cientistavuador.asteroidshooter.util;

import java.awt.image.BufferedImage;
import java.util.Base64;

/**
 *
 * @author Cien
 */
public class ImageToBase64Icon {
    
    public static String toBase64Icon(BufferedImage image, boolean flipY) {
        if (image.getWidth() != 32 && image.getHeight() != 32) {
            throw new RuntimeException("Image must be 32x32");
        }
        
        byte[] data = new byte[32*32];
        for (int i = 0; i < data.length; i++) {
            int x = i % 32;
            int y = i / 32;
            if (flipY) {
                y = (image.getHeight() - 1) - y;
            }
            int rgb = image.getRGB(x, y);
            
            float alpha = Byte.toUnsignedInt((byte) (rgb >>> 24)) / 255f;
            float red = Byte.toUnsignedInt((byte) (rgb >>> 16)) / 255f;
            float green = Byte.toUnsignedInt((byte) (rgb >>> 8)) / 255f;
            float blue = Byte.toUnsignedInt((byte) (rgb >>> 0)) / 255f;
            
            if (alpha < 0.5f) {
                data[i] = 0;
            } else {
                int redBits = Math.min((int) (red * 7f), 7);
                int greenBits = Math.min((int) (green * 7f), 7);
                int blueBits = Math.min((int) (blue * 3f), 3);
                
                if (redBits == 0 && greenBits == 0 && blueBits == 0) {
                    redBits = 1;
                    greenBits = 1;
                    blueBits = 1;
                }
                
                data[i] = (byte)((redBits << 5) | (greenBits << 2) | (blueBits << 0));
            }
        }
        
        return Base64.getEncoder().encodeToString(data);
    }
    
    public static BufferedImage base64IconToImage(String s) {
        BufferedImage output = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        
        byte[] data = base64IconTo32BitRGBAImage(s);
        for (int i = 0; i < 32*32; i++) {
            int red = Byte.toUnsignedInt(data[(i * 4) + 0]);
            int green = Byte.toUnsignedInt(data[(i * 4) + 1]);
            int blue = Byte.toUnsignedInt(data[(i * 4) + 2]);
            int alpha = Byte.toUnsignedInt(data[(i * 4) + 3]);
            
            output.setRGB(i % 32, i / 32, (alpha << 24) | (red << 16) | (green << 8) | (blue << 0));
        }
        
        return output;
    }
    
    public static byte[] base64IconTo32BitRGBAImage(String s) {
        byte[] data = Base64.getDecoder().decode(s);
        if (data.length != 32*32) {
            throw new RuntimeException("Not a 32x32 icon.");
        }
        
        byte[] output = new byte[32*32*4];
        for (int i = 0; i < data.length; i++) {
            int pixelRGB = data[i];
            
            float red = ((pixelRGB >>> 5) & 0x07) / 7f;
            float green = ((pixelRGB >>> 2) & 0x07) / 7f;
            float blue = ((pixelRGB >>> 0) & 0x03) / 3f;
            float alpha = 1f;
            
            if (red == 0f && green == 0f && blue == 0f) {
                alpha = 0f;
            }
            
            output[(i * 4) + 0] = (byte) (red * 255f);
            output[(i * 4) + 1] = (byte) (green * 255f);
            output[(i * 4) + 2] = (byte) (blue * 255f);
            output[(i * 4) + 3] = (byte) (alpha * 255f);
        }
        
        return output;
    }
    
    private ImageToBase64Icon() {
        
    }
    
}
