package engine.graph;

import engine.items.GameItem;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class TerrainMesh {

    private final int vaoId;

    private final List<Integer> vboIdList;

    private final int vertexCount;

    private Vector4f topC;
    private Vector4f middleC;
    private Vector4f bottomC;

    public TerrainMesh(float[] positions, float[] normals, int[] indices, Vector4f topC, Vector4f middleC, Vector4f bottomC) {
        FloatBuffer posBuffer = null;
        FloatBuffer vecNormalsBuffer = null;
        FloatBuffer colourBuffer = null;
        IntBuffer indicesBuffer = null;
        this.topC = topC;
        this.middleC = middleC;
        this.bottomC = bottomC;
        float[] interpolatedColours = new float[positions.length];
        try {
            vertexCount = indices.length;
            vboIdList = new ArrayList();

            vaoId = glGenVertexArrays();
            glBindVertexArray(vaoId);

            // Position VBO
            int vboId = glGenBuffers();
            vboIdList.add(vboId);
            posBuffer = MemoryUtil.memAllocFloat(positions.length);
            posBuffer.put(positions).flip();
            glBindBuffer(GL_ARRAY_BUFFER, vboId);
            glBufferData(GL_ARRAY_BUFFER, posBuffer, GL_STATIC_DRAW);
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

            // Vertex normals VBO
            vboId = glGenBuffers();
            vboIdList.add(vboId);
            vecNormalsBuffer = MemoryUtil.memAllocFloat(normals.length);
            if ( vecNormalsBuffer.capacity() > 0 ) {
                vecNormalsBuffer.put(normals).flip();
            } else {
                // Create empty structure
                vecNormalsBuffer = MemoryUtil.memAllocFloat(positions.length);
            }
            glBindBuffer(GL_ARRAY_BUFFER, vboId);
            glBufferData(GL_ARRAY_BUFFER, vecNormalsBuffer, GL_STATIC_DRAW);
            glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);

            int curIndex = 0;
            for(int i = 0; i < vertexCount; i++){
                Vector4f colour = interpolate(bottomC,topC,positions[i + 1]);
                interpolatedColours[3*curIndex + 0] = colour.x;
                interpolatedColours[3*curIndex + 1] = colour.y;
                interpolatedColours[3*curIndex + 2] = colour.z;
                interpolatedColours[3*curIndex +3] = colour.w;
                curIndex++;
            }

            vboId = glGenBuffers();
            vboIdList.add(vboId);
            colourBuffer = MemoryUtil.memAllocFloat(interpolatedColours.length);
            colourBuffer.put(interpolatedColours).flip();
            glBindBuffer(GL_ARRAY_BUFFER, vboId);
            glBufferData(GL_ARRAY_BUFFER, colourBuffer, GL_STATIC_DRAW);
            glVertexAttribPointer(2, 4, GL_FLOAT, false, 0, 0);

            // Index VBO
            vboId = glGenBuffers();
            vboIdList.add(vboId);
            indicesBuffer = MemoryUtil.memAllocInt(indices.length);
            indicesBuffer.put(indices).flip();
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboId);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW);

            glBindBuffer(GL_ARRAY_BUFFER, 0);
            glBindVertexArray(0);
        } finally {
            if (posBuffer != null) {
                MemoryUtil.memFree(posBuffer);
            }
            if (colourBuffer != null) {
                MemoryUtil.memFree(colourBuffer);
            }
            if (vecNormalsBuffer != null) {
                MemoryUtil.memFree(vecNormalsBuffer);
            }
            if (indicesBuffer != null) {
                MemoryUtil.memFree(indicesBuffer);
            }
        }
    }

    public int getVaoId() {
        return vaoId;
    }

    public int getVertexCount() {
        return vertexCount;
    }

    private void initRender() {
        // Draw the mesh
        glBindVertexArray(getVaoId());
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glEnableVertexAttribArray(2);
    }

    private void endRender() {
        // Restore state
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glDisableVertexAttribArray(2);
        glBindVertexArray(0);

    }

    public void render() {
        initRender();

        glDrawElements(GL_TRIANGLES, getVertexCount(), GL_UNSIGNED_INT, 0);

        endRender();
    }

    public void renderList(List<GameItem> gameItems, Consumer<GameItem> consumer) {
        initRender();

        for (GameItem gameItem : gameItems) {
            // Set up data requiered by gameItem
            consumer.accept(gameItem);
            // Render this game item
            glDrawElements(GL_TRIANGLES, getVertexCount(), GL_UNSIGNED_INT, 0);
        }

        endRender();
    }

    public void cleanUp() {
        glDisableVertexAttribArray(0);

        // Delete the VBOs
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        for (int vboId : vboIdList) {
            glDeleteBuffers(vboId);
        }

        // Delete the VAO
        glBindVertexArray(0);
        glDeleteVertexArrays(vaoId);
    }

    public void deleteBuffers() {
        glDisableVertexAttribArray(0);

        // Delete the VBOs
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        for (int vboId : vboIdList) {
            glDeleteBuffers(vboId);
        }

        // Delete the VAO
        glBindVertexArray(0);
        glDeleteVertexArrays(vaoId);
    }

    private Vector4f interpolate(Vector4f a, Vector4f b, float y) {
        float red = a.x + y * (b.x - a.x);
        float green = a.y + y * (b.y - a.y);
        float blue = a.z + y * (b.z - a.z);
        float alpha = a.w + y * (b.w - a.w);
        return new Vector4f(red,green,blue,alpha);
    }
}