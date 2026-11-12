package dating.model;

import org.junit.jupiter.api.Test;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class PhotoAndLikeTest {

    @Test
    void shouldStorePhotoPathWithoutOpeningFile() {
        Photo photo = new Photo(10, 2, " photos/My  Photo.jpg ");

        assertEquals(10, photo.getId());
        assertEquals(2, photo.getOwnerId());
        assertEquals("photos/My  Photo.jpg", photo.getPath());
    }

    @Test
    void shouldRejectInvalidPhotoData() {
        assertThrows(IllegalArgumentException.class,
                () -> new Photo(0, 2, "a.jpg"));

        assertThrows(IllegalArgumentException.class,
                () -> new Photo(1, -2, "a.jpg"));

        assertThrows(IllegalArgumentException.class,
                () -> new Photo(1, 2, " "));

        assertThrows(IllegalArgumentException.class,
                () -> new Photo(1, 2, null));
    }

    @Test
    void shouldCompareLikesByAuthorAndPhoto() {
        Like like = new Like(1, 10);

        assertEquals(1, like.getUserId());
        assertEquals(10, like.getPhotoId());

        assertEquals(new Like(1, 10), like);
        assertEquals(like.hashCode(), new Like(1, 10).hashCode());

        assertNotEquals(new Like(2, 10), like);
        assertNotEquals(new Like(1, 11), like);
        assertNotEquals(null, like);

        HashSet<Like> likes = new HashSet<>();

        assertTrue(likes.add(like));
        assertFalse(likes.add(new Like(1, 10)));
    }

    @Test
    void shouldRejectInvalidLikeIdentifiers() {
        assertThrows(IllegalArgumentException.class,
                () -> new Like(0, 1));

        assertThrows(IllegalArgumentException.class,
                () -> new Like(1, -1));
    }
}
