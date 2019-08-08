package engine;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Utils {

    public static String loadResource(String fileName) throws Exception {
        String result;
        try (InputStream in = Class.forName(Utils.class.getName()).getResourceAsStream(fileName);
             Scanner scanner = new Scanner(in, "UTF-8")) {
            result = scanner.useDelimiter("\\A").next();
        }
        return result;
    }

    public static List<String> readAllLines(String fileName) throws Exception {
        List<String> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(Class.forName(Utils.class.getName()).getResourceAsStream(fileName)))) {
            String line;
            while ((line = br.readLine()) != null) {
                list.add(line);
            }
        }
        return list;
    }

    public static float[] listToArray(List<Float> list) {
        int size = list != null ? list.size() : 0;
        float[] floatArr = new float[size];
        for (int i = 0; i < size; i++) {
            floatArr[i] = list.get(i);
        }
        return floatArr;
    }

    public static BufferedImage createGradientImage(int width, int height, Color gradient1, Color gradient2, Color gradient3) {

        BufferedImage gradientImage = new BufferedImage(width, height,BufferedImage.TYPE_INT_ARGB);
        GradientPaint firstGradient = new GradientPaint(0, 0, gradient1, width / 2, height, gradient2, true);
        GradientPaint secondGradient = new GradientPaint(width/2, 0, gradient2, width, height, gradient3, true);
        Graphics2D g2 = (Graphics2D) gradientImage.getGraphics();

        g2.setPaint(firstGradient);
        g2.fillRect(0, 0, width/2, height);
        g2.setPaint(secondGradient);
        g2.fillRect(width/2, 0, width, height);
        g2.dispose();

        return gradientImage;
    }

}
