package engine.graph;

import engine.Utils;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.lwjgl.stb.STBImage.*;

public class HeightMapMesh {

    private static final int MAX_COLOUR = 255 * 255 * 255;
    private static final float STARTX = -0.5f;
    private static final float STARTZ = -0.5f;
    private final float minY;
    private final float maxY;
    private final Mesh mesh;

    public HeightMapMesh(int startX, int startZ, float minY, float maxY, int widthX, int widthZ, int textInc) throws Exception {
        this.minY = minY;
        this.maxY = maxY;

        float incx = getXLength() / (widthX - 1);
        float incz = getZLength() / (widthZ - 1);

        List<Float> positions = new ArrayList();
        List<Float> textCoords = new ArrayList();
        List<Integer> indices = new ArrayList();

        NoiseGenerator generator = new NoiseGenerator(startX,startZ, new Random().nextInt());

        for (int row = 0; row < widthZ; row++) {
            for (int col = 0; col < widthX; col++) {
                // Create vertex for current position
                positions.add(STARTX + col * incx); // x
                positions.add(generator.generateHeight(row,col)); //y
                positions.add(STARTZ + row * incz); //z

                // Set texture coordinates
                textCoords.add((float) textInc * (float) col / (float) widthX);
                textCoords.add((float) textInc * (float) row / (float) widthZ);

                // Create indices
                if (col < widthX - 1 && row < widthZ - 1) {
                    int leftTop = row * widthX + col;
                    int leftBottom = (row + 1) * widthX + col;
                    int rightBottom = (row + 1) * widthX + col + 1;
                    int rightTop = row * widthX + col + 1;

                    indices.add(leftTop);
                    indices.add(leftBottom);
                    indices.add(rightTop);

                    indices.add(rightTop);
                    indices.add(leftBottom);
                    indices.add(rightBottom);
                }
            }
        }
        float[] posArr = Utils.listToArray(positions);
        int[] indicesArr = indices.stream().mapToInt(i -> i).toArray();
        float[] textCoordsArr = Utils.listToArray(textCoords);
        float[] normalsArr = calcNormals(posArr, widthX, widthZ);

        this.mesh = new Mesh(posArr, textCoordsArr, normalsArr, indicesArr);

        BufferedImage gradient = Utils.createGradientImage(100,1,new Color(87,51,0,255),new Color(0,128,11,255),new Color(247,247,247,255));
        Texture texture = new Texture(gradient);

        Material material = new Material(texture);
        material.setTerrain(true);
        mesh.setMaterial(material);
    }

    public Mesh getMesh() {
        return mesh;
    }

    public static float getXLength() {
        return Math.abs(-STARTX * 2);
    }

    public static float getZLength() {
        return Math.abs(-STARTZ * 2);
    }

    private float[] calcNormals(float[] posArr, int width, int height) {
        Vector3f v0 = new Vector3f();
        Vector3f v1 = new Vector3f();
        Vector3f v2 = new Vector3f();
        Vector3f v3 = new Vector3f();
        Vector3f v4 = new Vector3f();
        Vector3f v12 = new Vector3f();
        Vector3f v23 = new Vector3f();
        Vector3f v34 = new Vector3f();
        Vector3f v41 = new Vector3f();
        List<Float> normals = new ArrayList<>();
        Vector3f normal = new Vector3f();
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                if (row > 0 && row < height - 1 && col > 0 && col < width - 1) {
                    int i0 = row * width * 3 + col * 3;
                    v0.x = posArr[i0];
                    v0.y = posArr[i0 + 1];
                    v0.z = posArr[i0 + 2];

                    int i1 = row * width * 3 + (col - 1) * 3;
                    v1.x = posArr[i1];
                    v1.y = posArr[i1 + 1];
                    v1.z = posArr[i1 + 2];
                    v1 = v1.sub(v0);

                    int i2 = (row + 1) * width * 3 + col * 3;
                    v2.x = posArr[i2];
                    v2.y = posArr[i2 + 1];
                    v2.z = posArr[i2 + 2];
                    v2 = v2.sub(v0);

                    int i3 = (row) * width * 3 + (col + 1) * 3;
                    v3.x = posArr[i3];
                    v3.y = posArr[i3 + 1];
                    v3.z = posArr[i3 + 2];
                    v3 = v3.sub(v0);

                    int i4 = (row - 1) * width * 3 + col * 3;
                    v4.x = posArr[i4];
                    v4.y = posArr[i4 + 1];
                    v4.z = posArr[i4 + 2];
                    v4 = v4.sub(v0);

                    v1.cross(v2, v12);
                    v12.normalize();

                    v2.cross(v3, v23);
                    v23.normalize();

                    v3.cross(v4, v34);
                    v34.normalize();

                    v4.cross(v1, v41);
                    v41.normalize();

                    normal = v12.add(v23).add(v34).add(v41);
                    normal.normalize();
                } else {
                    normal.x = 0;
                    normal.y = 1;
                    normal.z = 0;
                }
                normal.normalize();
                normals.add(normal.x);
                normals.add(normal.y);
                normals.add(normal.z);
            }
        }
        return Utils.listToArray(normals);
    }

    private float getHeight(int x, int z, int width, ByteBuffer buffer) {
        byte r = buffer.get(x * 4 + 0 + z * 4 * width);
        byte g = buffer.get(x * 4 + 1 + z * 4 * width);
        byte b = buffer.get(x * 4 + 2 + z * 4 * width);
        byte a = buffer.get(x * 4 + 3 + z * 4 * width);
        int argb = ((0xFF & a) << 24) | ((0xFF & r) << 16)
                | ((0xFF & g) << 8) | (0xFF & b);
        return this.minY + Math.abs(this.maxY - this.minY) * ((float) argb / (float) MAX_COLOUR);
    }

}