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
package cientistavuador.asteroidshooter.geometry;

import cientistavuador.asteroidshooter.resources.mesh.MeshData;
import cientistavuador.asteroidshooter.resources.mesh.MeshResources;
import java.util.ArrayDeque;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import static org.lwjgl.opengl.GL33C.*;

/**
 *
 * @author Cien
 */
public class GeometriesLoader {

    public static final boolean DEBUG_OUTPUT = true;

    public static int[] load(String... names) {
        if (names.length == 0) {
            if (DEBUG_OUTPUT) {
                System.out.println("No geometries to load.");
            }
            return new int[0];
        }

        if (DEBUG_OUTPUT) {
            System.out.println("Loading geometries...");
        }

        ArrayDeque<Future<MeshData>> futureDatas = new ArrayDeque<>();
        MeshData[] datas = new MeshData[names.length];

        for (int i = 0; i < datas.length; i++) {
            final int index = i;
            if (DEBUG_OUTPUT) {
                System.out.println("Loading geometry '" + names[index] + "' with index " + index);
            }
            futureDatas.add(CompletableFuture.supplyAsync(() -> {
                MeshData e = MeshResources.load(names[index]);
                if (DEBUG_OUTPUT) {
                    System.out.println("Finished loading geometry '" + names[index] + "' with index " + index + ": " + (e.getVertices().length / MeshData.SIZE) + " vertices, " + e.getIndices().length + " indices.");
                }
                return e;
            }));
        }

        Future<MeshData> future;
        int index = 0;
        while ((future = futureDatas.poll()) != null) {
            try {
                datas[index] = future.get();
                index++;
            } catch (InterruptedException | ExecutionException ex) {
                throw new RuntimeException(ex);
            }
        }

        int[] vaos = new int[datas.length * 2];

        for (int i = 0; i < vaos.length / 2; i++) {
            if (DEBUG_OUTPUT) {
                System.out.println("Sending geometry '" + names[i] + "', index " + i + " to the gpu.");
            }
            MeshData data = datas[i];

            int vao = glGenVertexArrays();
            glBindVertexArray(vao);

            int ebo = glGenBuffers();
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, data.getIndices(), GL_STATIC_DRAW);

            int vbo = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, vbo);
            glBufferData(GL_ARRAY_BUFFER, data.getVertices(), GL_STATIC_DRAW);

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
            vaos[(i * 2) + 0] = vao;
            vaos[(i * 2) + 1] = data.getIndices().length;
            if (DEBUG_OUTPUT) {
                System.out.println("Finished sending geometry '" + names[i] + "', index " + i + " to the gpu with object id "+vao+".");
            }
        }

        if (DEBUG_OUTPUT) {
            System.out.println("Finished loading geometries.");
        }
        return vaos;
    }

    private GeometriesLoader() {

    }
}
