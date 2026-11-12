package dating.model;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SocialGraphTest {

    private SocialGraph graph() {
        SocialGraph graph = new SocialGraph();
        for (int i = 1; i <= 5; i++) {
            graph.addUser(i);
        }
        return graph;
    }

    @Test
    void shouldCreateSymmetricConnectionWithoutDuplicates() {
        SocialGraph graph = graph();

        assertTrue(graph.connect(1, 2));
        assertTrue(graph.areConnected(1, 2));
        assertTrue(graph.areConnected(2, 1));
        assertFalse(graph.connect(2, 1));

        assertEquals(Set.of(2), graph.getFriends(1));
        assertEquals(Set.of(1), graph.getFriends(2));
    }

    @Test
    void shouldRejectUnknownAndSelfConnectionsWithoutPartialChanges() {
        SocialGraph graph = graph();

        assertThrows(IllegalArgumentException.class,
                () -> graph.connect(1, 99));
        assertThrows(IllegalArgumentException.class,
                () -> graph.connect(99, 1));
        assertThrows(IllegalArgumentException.class,
                () -> graph.connect(1, 1));

        assertTrue(graph.getFriends(1).isEmpty());

        assertThrows(IllegalArgumentException.class,
                () -> graph.getFriends(99));
        assertThrows(IllegalArgumentException.class,
                () -> graph.areConnected(99, 1));
        assertThrows(IllegalArgumentException.class,
                () -> graph.getMutualFriends(1, 99));
    }

    @Test
    void shouldFindAllMutualFriendsInIdOrder() {
        SocialGraph graph = graph();

        graph.connect(1, 3);
        graph.connect(1, 2);
        graph.connect(4, 2);
        graph.connect(4, 3);
        graph.connect(1, 4);

        assertEquals(
                List.of(2, 3),
                List.copyOf(graph.getMutualFriends(1, 4))
        );

        assertEquals(
                graph.getMutualFriends(1, 4),
                graph.getMutualFriends(4, 1)
        );
    }

    @Test
    void shouldReturnEmptyForIsolatedAndDisjointUsers() {
        SocialGraph graph = graph();

        graph.connect(1, 2);
        graph.connect(3, 4);

        assertTrue(graph.getFriends(5).isEmpty());
        assertTrue(graph.getMutualFriends(1, 3).isEmpty());
        assertFalse(graph.areConnected(1, 1));
    }

    @Test
    void shouldProtectGraphSnapshots() {
        SocialGraph graph = graph();

        graph.connect(1, 2);
        Set<Integer> before = graph.getFriends(1);

        assertThrows(UnsupportedOperationException.class,
                () -> before.add(3));

        graph.connect(1, 3);

        assertEquals(Set.of(2), before);
        assertEquals(Set.of(2, 3), graph.getFriends(1));
    }

    @Test
    void shouldRejectInvalidAndDuplicateVertices() {
        SocialGraph graph = graph();

        assertThrows(IllegalArgumentException.class,
                () -> graph.addUser(0));

        assertThrows(IllegalArgumentException.class,
                () -> graph.addUser(1));

        graph.connect(1, 2);

        assertTrue(graph.areConnected(1, 2));
    }
}
