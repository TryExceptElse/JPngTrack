import img.Img;

import java.io.IOException;

/**
 * Class handling detection of apparent motion between images
 */
public final class JPngTrack {
    /**
     * Method taking a pair of images, and returning the apparent
     * translation between the two.
     * This method assumes the two images actually are comparable
     * and are roughly identical except for their translation.
     * If this is not the case, nonsense values are likely to be
     * returned.
     * @param pathA: String
     * @param pathB: String
     * @return float[2]
     */
    public static float[] translationFromPaths(
            String pathA,
            String pathB
    ) throws IOException{
        return translationFromPaths(pathA, pathB, 0.f, 0.f);
    }

    /**
     * Method taking a pair of images, and returning the apparent
     * translation between the two.
     * This method assumes the two images actually are comparable
     * and are roughly identical except for their translation.
     * If this is not the case, nonsense values are likely to be
     * returned.
     * xGate and yGate may be passed to limit results to translations
     * smaller than passed x and y values. This helps avoid
     * nonsense results, and dramatically speeds results.
     * @param pathA: String
     * @param pathB: String
     * @param xGate: float
     * @param yGate: float
     * @return float[]
     */
    public static float[] translationFromPaths(
            String pathA,
            String pathB,
            float xGate,
            float yGate
    ) throws IOException{
        Img imgA = Img.fromPath(pathA);
        Img imgB = Img.fromPath(pathB);
        // if either gate is zero, consider it (nearly) unlimited on that axis.
        if (xGate == 0f){
            xGate = (float)imgA.getWidth() - 1;
        }
        if (yGate == 0f){
            yGate = (float)imgB.getHeight() - 1;
        }
        return imgA.appMotion(imgB, xGate, yGate);
    }
}
