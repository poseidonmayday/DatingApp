package dating.model;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class DatingServiceTest {
    private DatingService service() {
        DatingService service = new DatingService();
        service.registerUser(3, "Вера", 30, "Казань");
        service.registerUser(1, "Анна", 18, "Москва");
        service.registerUser(4, "Глеб", 35, "Москва");
        service.registerUser(2, "Борис", 22, "Москва");
        return service;
    }

    private List<Integer> userIds(List<UserProfile> users) {
        return users.stream().map(UserProfile::getId).toList();
    }

    private List<Integer> recommendationIds(List<Recommendation> results) {
        return results.stream().map(result -> result.getUser().getId()).toList();
    }

    private void addInterests(DatingService service) {
        service.addInterest(1, "КИНО");
        service.addInterest(1, "книги");
        service.addInterest(2, "кино");
        service.addInterest(3, "кино");
        service.addInterest(3, "книги");
        service.addInterest(4, "кино");
    }

    @Test void shouldKeepUsersOrderedAndRejectDuplicateIdWithoutChanges() {
        DatingService service = service();
        List<AppEvent> events = service.getEventLog();
        assertEquals(List.of(1, 2, 3, 4), userIds(service.getUsers()));
        assertThrows(IllegalArgumentException.class, () -> service.registerUser(1, "Другая", 30, "Казань"));
        assertEquals("Анна", service.getUser(1).getName());
        assertEquals(events, service.getEventLog());
        assertEquals(4, service.getUsers().size());
        assertThrows(UnsupportedOperationException.class, () -> service.getUsers().clear());
    }

    @Test void shouldStorePhotosByOwnerAndGlobalId() {
        DatingService service = service();
        service.addPhoto(20, 1, "a.jpg");
        service.addPhoto(10, 1, "b.jpg");
        service.addPhoto(30, 2, "c.jpg");
        assertEquals(List.of(10, 20), service.getPhotos(1).stream().map(Photo::getId).toList());
        assertEquals(2, service.getPhoto(30).getOwnerId());
        assertTrue(service.getPhotos(3).isEmpty());
        assertThrows(UnsupportedOperationException.class, () -> service.getPhotos(1).clear());
        assertThrows(IllegalArgumentException.class, () -> service.addPhoto(10, 2, "another.jpg"));
        assertEquals(1, service.getPhoto(10).getOwnerId());
    }

    @Test void shouldRejectUnknownOwnerAndInvalidPhotoWithoutChanges() {
        DatingService service = service();
        int events = service.getEventLog().size();
        assertThrows(IllegalArgumentException.class, () -> service.addPhoto(10, 99, "a.jpg"));
        assertThrows(IllegalArgumentException.class, () -> service.addPhoto(10, 1, " "));
        assertThrows(IllegalArgumentException.class, () -> service.getPhoto(10));
        assertThrows(IllegalArgumentException.class, () -> service.getPhotos(99));
        assertTrue(service.getPhotos(1).isEmpty());
        assertEquals(events, service.getEventLog().size());
    }

    @Test void shouldAllowDifferentAuthorsAndRejectRepeatedLike() {
        DatingService service = service();
        service.addPhoto(10, 1, "a.jpg");
        service.likePhoto(3, 10);
        service.likePhoto(2, 10);
        assertEquals(List.of(2, 3), service.getLikes(10).stream().map(Like::getUserId).toList());
        assertTrue(service.hasLiked(2, 10));
        assertFalse(service.hasLiked(4, 10));
        List<AppEvent> events = service.getEventLog();
        assertThrows(IllegalStateException.class, () -> service.likePhoto(2, 10));
        assertEquals(2, service.getLikes(10).size());
        assertEquals(events, service.getEventLog());
        assertThrows(UnsupportedOperationException.class, () -> service.getLikes(10).clear());
    }

    @Test void shouldRejectSelfLikeAndUnknownReferences() {
        DatingService service = service();
        service.addPhoto(10, 1, "a.jpg");
        int events = service.getEventLog().size();
        assertThrows(IllegalArgumentException.class, () -> service.likePhoto(1, 10));
        assertThrows(IllegalArgumentException.class, () -> service.likePhoto(99, 10));
        assertThrows(IllegalArgumentException.class, () -> service.likePhoto(2, 99));
        assertThrows(IllegalArgumentException.class, () -> service.hasLiked(99, 10));
        assertThrows(IllegalArgumentException.class, () -> service.getLikes(99));
        assertTrue(service.getLikes(10).isEmpty());
        assertEquals(events, service.getEventLog().size());
    }

    @Test void shouldExposeSymmetricAcquaintancesAndCommonProfiles() {
        DatingService service = service();
        assertTrue(service.addAcquaintance(1, 2));
        service.addAcquaintance(3, 2);
        assertFalse(service.addAcquaintance(2, 1));
        assertTrue(service.areAcquainted(2, 1));
        assertEquals(List.of(2), userIds(service.getFriends(1)));
        assertEquals(List.of(1, 3), userIds(service.getFriends(2)));
        assertEquals(List.of(2), userIds(service.getMutualFriends(1, 3)));
        assertThrows(UnsupportedOperationException.class, () -> service.getFriends(1).clear());
        int events = service.getEventLog().size();
        assertThrows(IllegalArgumentException.class, () -> service.addAcquaintance(1, 99));
        assertThrows(IllegalArgumentException.class, () -> service.addAcquaintance(1, 1));
        assertEquals(events, service.getEventLog().size());
    }

    @Test void shouldCombineAgeAndCityFilterAndKeepIdOrder() {
        DatingService service = service();
        assertEquals(List.of(1, 2, 3, 4), userIds(service.filterUsers(ProfileFilter.all())));
        assertEquals(List.of(1, 2), userIds(service.filterUsers(new ProfileFilter(18, 22, " мОСКВА "))));
        assertEquals(List.of(3), userIds(service.filterUsers(new ProfileFilter(30, 30, ""))));
        assertTrue(service.filterUsers(new ProfileFilter(18, 120, "Самара")).isEmpty());
    }

    @Test void shouldRankByCommonInterestsThenIdAndExcludeSelfAndZeroMatches() {
        DatingService service = service();
        service.registerUser(5, "Дарья", 24, "Москва");
        service.addInterest(5, "спорт");
        addInterests(service);
        service.addAcquaintance(1, 2);
        List<Recommendation> results = service.matchByInterests(1, ProfileFilter.all());
        assertEquals(List.of(3, 2, 4), recommendationIds(results));
        assertEquals(List.of(2, 1, 1), results.stream().map(Recommendation::getScore).toList());
        assertEquals(List.of("кино", "книги"), results.getFirst().getReasons());
        assertThrows(UnsupportedOperationException.class, results::clear);
    }

    @Test void shouldApplyFiltersToInterestMatchesAndHandleNoInterests() {
        DatingService service = service();
        assertTrue(service.matchByInterests(1, ProfileFilter.all()).isEmpty());
        addInterests(service);
        assertEquals(List.of(2), recommendationIds(service.matchByInterests(1, new ProfileFilter(22, 25, "Москва"))));
        assertTrue(service.matchByInterests(1, new ProfileFilter(40, 50, "")).isEmpty());
    }

    @Test void shouldRankPeopleByMutualFriendsAndExcludeKnownAndIsolated() {
        DatingService service = service();
        service.registerUser(5, "Дарья", 24, "Москва");
        service.registerUser(6, "Егор", 26, "Казань");
        service.registerUser(7, "Жанна", 27, "Москва");
        service.addAcquaintance(1, 2);
        service.addAcquaintance(1, 3);
        service.addAcquaintance(4, 2);
        service.addAcquaintance(4, 3);
        service.addAcquaintance(5, 2);
        service.addAcquaintance(6, 3);
        List<Recommendation> results = service.recommendPeople(1, ProfileFilter.all());
        assertEquals(List.of(4, 5, 6), recommendationIds(results));
        assertEquals(List.of(2, 1, 1), results.stream().map(Recommendation::getScore).toList());
        assertEquals(List.of("#2 Борис", "#3 Вера"), results.getFirst().getReasons());
        assertFalse(recommendationIds(results).contains(7));
    }

    @Test void shouldFilterPeopleRecommendationsAndUpdateAfterNewConnection() {
        DatingService service = service();
        service.registerUser(5, "Дарья", 24, "Москва");
        service.addAcquaintance(1, 2);
        service.addAcquaintance(2, 4);
        service.addAcquaintance(2, 5);
        assertEquals(List.of(5), recommendationIds(service.recommendPeople(1, new ProfileFilter(18, 30, "Москва"))));
        service.addAcquaintance(1, 5);
        assertEquals(List.of(4), recommendationIds(service.recommendPeople(1, ProfileFilter.all())));
        assertTrue(service.recommendPeople(3, ProfileFilter.all()).isEmpty());
    }

    @Test void shouldRejectUnknownUsersAndMissingFiltersInQueriesAndChanges() {
        DatingService service = service();
        int events = service.getEventLog().size();
        assertThrows(IllegalArgumentException.class, () -> service.getUser(99));
        assertThrows(IllegalArgumentException.class, () -> service.addInterest(99, "кино"));
        assertThrows(IllegalArgumentException.class, () -> service.setSocialContact(99, "vk", "user"));
        assertThrows(IllegalArgumentException.class, () -> service.matchByInterests(99, ProfileFilter.all()));
        assertThrows(IllegalArgumentException.class, () -> service.recommendPeople(99, ProfileFilter.all()));
        assertThrows(IllegalArgumentException.class, () -> service.filterUsers(null));
        assertThrows(IllegalArgumentException.class, () -> service.matchByInterests(1, null));
        assertThrows(IllegalArgumentException.class, () -> service.recommendPeople(1, null));
        assertEquals(events, service.getEventLog().size());
    }

    @Test void shouldLogOnlyChangesWithSequentialNumbersAndProtectSnapshots() {
        DatingService service = service();
        List<AppEvent> before = service.getEventLog();
        service.addInterest(1, "кино");
        service.addInterest(1, "КИНО");
        service.setSocialContact(1, "vk", "anna");
        service.setSocialContact(1, "VK", "anna");
        service.addAcquaintance(1, 2);
        service.addAcquaintance(2, 1);
        assertEquals(7, service.getEventLog().size());
        assertEquals(4, before.size());
        assertThrows(UnsupportedOperationException.class, before::clear);
        for (int i = 0; i < service.getEventLog().size(); i++) {
            assertEquals(i + 1, service.getEventLog().get(i).getNumber());
        }
        service.filterUsers(ProfileFilter.all());
        service.matchByInterests(1, ProfileFilter.all());
        service.recommendPeople(1, ProfileFilter.all());
        assertEquals(7, service.getEventLog().size());
    }
}
