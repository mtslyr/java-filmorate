public interface FeedStorage {
    void addEvent(FeedEvent event);
    List<FeedEvent> getFeedByUserId(int userId);
}