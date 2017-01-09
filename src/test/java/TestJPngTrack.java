import junit.framework.TestCase;

import java.io.IOException;

/** tests main class */
public class TestJPngTrack extends TestCase {
    public void testTranslationFromPathReturnsAccurateResult()
            throws NullPointerException, IOException {
        String pathA = this.getClass().getClassLoader().getResource(
                "track_test_1a.png"
        ).toString();
        String pathB = this.getClass().getClassLoader().getResource(
                "track_test_1c.png"
        ).toString();
        float[] result = JPngTrack.translationFromPaths(
                pathA, pathB
        );
        // check that result is close enough
        assertEquals(result[0], 29);
        assertEquals(result[1], 26);
    }
}
