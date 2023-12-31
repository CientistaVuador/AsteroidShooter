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
package cientistavuador.asteroidshooter.resources.mesh;

import java.util.Arrays;
import static org.lwjgl.opengl.GL33C.*;

/**
 *
 * @author Cien
 */
public class MeshData {

    public static final int SIZE = 3 + 2 + 3;

    //position (vec3), texture/uv (vec2), normal (vec3)
    private final float[] vertices;
    private final int[] indices;
    private int vao = 0;

    public MeshData(float[] vertices, int[] indices) {
        this.vertices = vertices;
        this.indices = indices;
    }

    public float[] getVertices() {
        return vertices;
    }

    public int getAmountOfVerticesComponents() {
        return vertices.length;
    }
    
    public int getAmountOfVertices() {
        return vertices.length / MeshData.SIZE;
    }
    
    public int[] getIndices() {
        return indices;
    }

    public int getAmountOfIndices() {
        return indices.length;
    }
    
    public boolean hasVAO() {
        return this.vao != 0;
    }

    public int getVAO() {
        if (this.vao == 0) {
            this.vao = glGenVertexArrays();
            glBindVertexArray(this.vao);
            
            int ebo = glGenBuffers();
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, getIndices(), GL_STATIC_DRAW);

            int vbo = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, vbo);
            glBufferData(GL_ARRAY_BUFFER, getVertices(), GL_STATIC_DRAW);
            
            //position
            glEnableVertexAttribArray(0);
            glVertexAttribPointer(0, 3, GL_FLOAT, false, MeshData.SIZE * Float.BYTES, 0);

            //texture
            glEnableVertexAttribArray(1);
            glVertexAttribPointer(1, 2, GL_FLOAT, false, MeshData.SIZE * Float.BYTES, (3 * Float.BYTES));

            //normal
            glEnableVertexAttribArray(2);
            glVertexAttribPointer(2, 3, GL_FLOAT, false, MeshData.SIZE * Float.BYTES, ((3 + 2) * Float.BYTES));

            glBindBuffer(GL_ARRAY_BUFFER, 0);

            glBindVertexArray(0);
        }
        return this.vao;
    }

    public void deleteVAO() {
        if (this.vao != 0) {
            glDeleteVertexArrays(this.vao);
            this.vao = 0;
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Arrays.hashCode(this.vertices);
        hash = 97 * hash + Arrays.hashCode(this.indices);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MeshData other = (MeshData) obj;
        if (!Arrays.equals(this.vertices, other.vertices)) {
            return false;
        }
        return Arrays.equals(this.indices, other.indices);
    }

}
