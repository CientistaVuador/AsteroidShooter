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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Obj parser for blender generated obj files with triangulated faces and no materials.
 * @author Cien
 */
public class MeshResources {

    public static MeshData load(String name) {
        try {
            return new MeshResources(name).get();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    //IO
    private final String name;
    private int currentLine = 0;

    //parsing
    private float[] positions = new float[64];
    private float[] textures = new float[64];
    private float[] normals = new float[64];
    private int[] faces = new int[64];

    private int positionsIndex = 0;
    private int texturesIndex = 0;
    private int normalsIndex = 0;
    private int facesIndex = 0;
    
    //generate indices
    private int[] indices = new int[64];
    private int indicesIndex = 0;
    
    //generate vertices
    private float[] vertices = new float[64];
    private int verticesIndex = 0;

    private MeshResources(String name) {
        this.name = name;
    }

    public MeshData get() throws IOException {
        InputStream stream = MeshResources.class.getResourceAsStream(this.name);
        if (stream == null) {
            throw new IOException("'"+this.name+"' not found.");
        }
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        stream,
                        StandardCharsets.UTF_8)
        )) {
            String line;
            while ((line = reader.readLine()) != null) {
                this.currentLine++;
                processLine(line);
            }
        }
        generateIndices();
        generateVertices();
        return generateMeshData();
    }

    private void pushPosition(float x, float y, float z) {
        if ((this.positionsIndex + 3) > this.positions.length) {
            this.positions = Arrays.copyOf(this.positions, (this.positions.length * 2) + 3);
        }
        this.positions[this.positionsIndex + 0] = x;
        this.positions[this.positionsIndex + 1] = y;
        this.positions[this.positionsIndex + 2] = z;
        this.positionsIndex += 3;
    }

    private void pushTexture(float u, float v) {
        if ((this.texturesIndex + 2) > this.textures.length) {
            this.textures = Arrays.copyOf(this.textures, (this.textures.length * 2) + 2);
        }
        this.textures[this.texturesIndex + 0] = u;
        this.textures[this.texturesIndex + 1] = v;
        this.texturesIndex += 2;
    }

    private void pushNormal(float x, float y, float z) {
        if ((this.normalsIndex + 3) > this.normals.length) {
            this.normals = Arrays.copyOf(this.normals, (this.normals.length * 2) + 3);
        }
        this.normals[this.normalsIndex + 0] = x;
        this.normals[this.normalsIndex + 1] = y;
        this.normals[this.normalsIndex + 2] = z;
        this.normalsIndex += 3;
    }
    
    private void pushFaceIndices(int position, int texture, int normal) {
        if ((this.facesIndex + 3) > this.faces.length) {
            this.faces = Arrays.copyOf(this.faces, (this.faces.length * 2) + 3);
        }
        this.faces[this.facesIndex + 0] = position;
        this.faces[this.facesIndex + 1] = texture;
        this.faces[this.facesIndex + 2] = normal;
        this.facesIndex += 3;
    }

    private float parseFloat(String[] split, int splitIndex) {
        try {
            return Float.parseFloat(split[splitIndex]);
        } catch (NumberFormatException ex) {
            throw new RuntimeException("Float parse error, in " + this.name + ", line " + this.currentLine + ", argument " + (splitIndex - 1), ex);
        }
    }

    private int parseFaceInt(String[] split, int splitIndex, int superSplitIndex) {
        try {
            return Integer.parseInt(split[splitIndex]);
        } catch (NumberFormatException ex) {
            throw new RuntimeException("Integer parse error, in " + this.name + ", line " + this.currentLine + ", argument " + (superSplitIndex - 1) + ", index " + splitIndex, ex);
        }
    }

    private void processLine(String line) throws IOException {
        if (line.isBlank() || line.startsWith("#")) {
            return;
        }
        String[] split = line.split(Pattern.quote(" "));
        switch (split[0]) {
            case "v" -> {
                if (split.length != 4) {
                    throw new RuntimeException("In " + this.name + ", line " + this.currentLine + ", vertex position requires 3 values.");
                }
                pushPosition(
                        parseFloat(split, 1),
                        parseFloat(split, 2),
                        parseFloat(split, 3)
                );
            }
            case "vt" -> {
                if (split.length != 3) {
                    throw new RuntimeException("In " + this.name + ", line " + this.currentLine + ", vertex texture requires 2 arguments.");
                }
                pushTexture(
                        parseFloat(split, 1),
                        parseFloat(split, 2)
                );
            }
            case "vn" -> {
                if (split.length != 4) {
                    throw new RuntimeException("In " + this.name + ", line " + this.currentLine + ", vertex normal requires 3 arguments.");
                }
                pushNormal(
                        parseFloat(split, 1),
                        parseFloat(split, 2),
                        parseFloat(split, 3)
                );
            }
            case "f" -> {
                if (split.length != 4) {
                    throw new RuntimeException("In " + this.name + ", line " + this.currentLine + ", face requires 3 arguments.");
                }
                for (int i = 1; i < 4; i++) {
                    String[] subsplit = split[i].split(Pattern.quote("/"));
                    if (subsplit.length != 3) {
                        throw new RuntimeException("In " + this.name + ", line " + this.currentLine + ", argument " + (i - 1) + ", 3 values are required for the face argument.");
                    }
                    pushFaceIndices(
                            parseFaceInt(subsplit, 0, i) - 1,
                            parseFaceInt(subsplit, 1, i) - 1,
                            parseFaceInt(subsplit, 2, i) - 1
                    );
                }
            }
        }
    }
    
    private class PositionTextureNormal {
        public final int positionIndex;
        public final int textureIndex;
        public final int normalIndex;
        
        public PositionTextureNormal(int index) {
            this.positionIndex = MeshResources.this.faces[(index * 3) + 0];
            this.textureIndex = MeshResources.this.faces[(index * 3) + 1];
            this.normalIndex = MeshResources.this.faces[(index * 3) + 2];
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
            final PositionTextureNormal other = (PositionTextureNormal) obj;
            if (this.positionIndex != other.positionIndex) {
                return false;
            }
            if (this.textureIndex != other.textureIndex) {
                return false;
            }
            return this.normalIndex == other.normalIndex;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 53 * hash + this.positionIndex;
            hash = 53 * hash + this.textureIndex;
            hash = 53 * hash + this.normalIndex;
            return hash;
        }
    }
    
    private void generateIndices() {
        final Map<PositionTextureNormal, Integer> indicesMap = new HashMap<>();
        final int amountOfVertices = this.facesIndex / 3;
        for (int i = 0; i < amountOfVertices; i++) {
            PositionTextureNormal e = new PositionTextureNormal(i);
            Integer currentIndex = indicesMap.get(e);
            if (currentIndex == null) {
                indicesMap.put(e, i);
                currentIndex = i;
            }
            if (this.indicesIndex + 1 > this.indices.length) {
                this.indices = Arrays.copyOf(this.indices, (this.indices.length * 2) + 1);
            }
            this.indices[this.indicesIndex + 0] = currentIndex;
            this.indicesIndex++;
        }
    }
    
    private void pushVertex(int position, int texture, int normal) {
        float x = this.positions[(position * 3) + 0];
        float y = this.positions[(position * 3) + 1];
        float z = this.positions[(position * 3) + 2];
        float u = this.textures[(texture * 2) + 0];
        float v = this.textures[(texture * 2) + 1];
        float nx = this.normals[(normal * 3) + 0];
        float ny = this.normals[(normal * 3) + 1];
        float nz = this.normals[(normal * 3) + 2];
        
        if (this.verticesIndex + 8 > this.vertices.length) {
            this.vertices = Arrays.copyOf(this.vertices, (this.vertices.length * 2) + 8);
        }
        this.vertices[this.verticesIndex + 0] = x;
        this.vertices[this.verticesIndex + 1] = y;
        this.vertices[this.verticesIndex + 2] = z;
        this.vertices[this.verticesIndex + 3] = u;
        this.vertices[this.verticesIndex + 4] = v;
        this.vertices[this.verticesIndex + 5] = nx;
        this.vertices[this.verticesIndex + 6] = ny;
        this.vertices[this.verticesIndex + 7] = nz;
        this.verticesIndex += 8;
    }
    
    private void generateVertices() {
        int indexOffset = 0;
        for (int i = 0; i < this.indicesIndex; i++) {
            int index = this.indices[i];
            if (index != i) {
                this.indices[i] = this.indices[index];
                indexOffset++;
                continue;
            }
            pushVertex(
                    this.faces[(i * 3) + 0],
                    this.faces[(i * 3) + 1],
                    this.faces[(i * 3) + 2]
            );
            this.indices[i] = i - indexOffset;
        }
    }

    private MeshData generateMeshData() {
        return new MeshData(
                Arrays.copyOf(this.vertices, this.verticesIndex),
                Arrays.copyOf(this.indices, this.indicesIndex)
        );
    }

}
