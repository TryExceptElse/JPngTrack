import junit.framework.TestCase;

import java.io.IOException;

/** tests main class */
public class TestJPngTrack extends TestCase {
    public void testTranslationFromPathReturnsAccurateResult()
            throws NullPointerException, IOException {
        String pathA = this.getClass().getClassLoader().getResource(
                "track_test_1a.png"
        ).getPath();
        String pathB = this.getClass().getClassLoader().getResource(
                "track_test_1c.png"
        ).getPath();
        float[] result = JPngTrack.translationFromPaths(
                pathA, pathB, 52, 52
        );
        // check that result is close enough
        assertEquals(29f, result[0]);
        assertEquals(-26f, result[1]);
    }

    public void testTranslationOfTwoIdenticalImagesIsZero()
            throws NullPointerException, IOException {
        String pathA = this.getClass().getClassLoader().getResource(
                "track_test_1a.png"
        ).getPath();
        String pathB = this.getClass().getClassLoader().getResource(
                "track_test_1a.png"
        ).getPath();
        float[] result = JPngTrack.translationFromPaths(
                pathA, pathB, 52, 52
        );
        // check that result is close enough
        assertEquals(0f, result[0]);
        assertEquals(0f, result[1]);
    }
}
