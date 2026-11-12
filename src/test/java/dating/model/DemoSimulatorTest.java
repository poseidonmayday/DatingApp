package dating.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Timeout(10)
class DemoSimulatorTest {

    private String snapshot(DatingService service) {
        StringBuilder text = new StringBuilder();

        for (UserProfile user : service.getUsers()) {
            int id = user.getId();

            text.append(id).append('|')
                    .append(user.getName()).append('|')
                    .append(user.getAge()).append('|')
                    .append(user.getCity()).append('|')
                    .append(user.getInterests()).append('|')
                    .append(user.getSocialContacts());

            text.append(
                    service.getFriends(id).stream()
                            .map(UserProfile::getId).toList()
            );

            for (Photo photo : service.getPhotos(id)) {
                text.append(photo.getId()).append(':')
                        .append(photo.getOwnerId()).append(':')
                        .append(photo.getPath());

                text.append(
                        service.getLikes(photo.getId()).stream()
                                .map(Like::getUserId).toList()
                );
            }

            for (Recommendation result :
                    service.matchByInterests(id, ProfileFilter.all())) {
                text.append('I')
                        .append(result.getUser().getId())
                        .append(result.getScore())
                        .append(result.getReasons());
            }

            for (Recommendation result :
                    service.recommendPeople(id, ProfileFilter.all())) {
                text.append('F')
                        .append(result.getUser().getId())
                        .append(result.getScore())
                        .append(result.getReasons());
            }
        }

        for (AppEvent event : service.getEventLog()) {
            text.append(event).append('\n');
        }

        return text.toString();
    }

    @Test
    void shouldReproduceAllDataQueriesAndEventsWithSameSeed() {
        DemoSimulator simulator = new DemoSimulator(25);

        DatingService first = simulator.run();
        DatingService second = simulator.run();

        assertNotSame(first, second);
        assertEquals(snapshot(first), snapshot(second));

        assertEquals(
                snapshot(first),
                snapshot(new DemoSimulator(25).run())
        );
    }

    @Test
    void shouldChangeDataWithAnotherSeedAndAcceptExtremeSeeds() {
        assertNotEquals(
                snapshot(new DemoSimulator(25).run()),
                snapshot(new DemoSimulator(26).run())
        );

        for (long seed : new long[]{
                0, -25, Long.MIN_VALUE, Long.MAX_VALUE
        }) {
            DemoSimulator simulator = new DemoSimulator(seed);

            assertEquals(seed, simulator.getSeed());
            assertEquals(8, simulator.run().getUsers().size());
        }
    }

    @Test
    void shouldCompleteKnownSimulationWithProfilesContactsPhotosLikesAndRecommendations() {
        DatingService service = new DemoSimulator(25).run();

        int photoCount = 0;
        int likeCount = 0;
        int directedConnections = 0;

        for (UserProfile user : service.getUsers()) {
            assertFalse(user.getInterests().isEmpty());
            assertEquals(2, user.getSocialContacts().size());

            photoCount += service.getPhotos(user.getId()).size();
            directedConnections += service.getFriends(user.getId()).size();

            for (Photo photo : service.getPhotos(user.getId())) {
                likeCount += service.getLikes(photo.getId()).size();
            }
        }

        assertEquals(8, service.getUsers().size());
        assertEquals(12, photoCount);
        assertEquals(11, directedConnections / 2);
        assertEquals(23, likeCount);

        assertEquals(29, service.getUser(1).getAge());
        assertEquals("Москва", service.getUser(1).getCity());

        Recommendation first =
                service.recommendPeople(1, ProfileFilter.all()).getFirst();

        assertEquals(4, first.getUser().getId());
        assertEquals(2, first.getScore());
    }

    @Test
    void shouldKeepReferencesUniquenessAndRankingCorrectAcross100Seeds() {
        for (long seed = 0; seed < 100; seed++) {
            DatingService service = new DemoSimulator(seed).run();
            HashSet<Integer> photoIds = new HashSet<>();

            for (UserProfile user : service.getUsers()) {
                int id = user.getId();

                for (UserProfile friend : service.getFriends(id)) {
                    assertNotEquals(id, friend.getId());
                    assertTrue(service.areAcquainted(friend.getId(), id));
                }

                for (Photo photo : service.getPhotos(id)) {
                    assertTrue(photoIds.add(photo.getId()));
                    assertEquals(id, photo.getOwnerId());

                    List<Like> likes =
                            service.getLikes(photo.getId());

                    assertEquals(
                            likes.size(),
                            new HashSet<>(likes).size()
                    );

                    for (Like like : likes) {
                        assertNotEquals(id, like.getUserId());
                        assertEquals(photo.getId(), like.getPhotoId());
                        assertNotNull(service.getUser(like.getUserId()));
                    }
                }

                List<Recommendation> results =
                        service.recommendPeople(
                                id, ProfileFilter.all()
                        );

                for (int i = 0; i < results.size(); i++) {
                    Recommendation result = results.get(i);
                    int candidate = result.getUser().getId();

                    assertNotEquals(id, candidate);
                    assertFalse(service.areAcquainted(id, candidate));

                    assertEquals(
                            service.getMutualFriends(id, candidate).size(),
                            result.getScore()
                    );

                    assertTrue(result.getScore() > 0);

                    if (i > 0) {
                        Recommendation previous = results.get(i - 1);

                        assertTrue(
                                previous.getScore() > result.getScore()
                                        || (previous.getScore() == result.getScore()
                                        && previous.getUser().getId() < candidate)
                        );
                    }
                }
            }
        }
    }
}
