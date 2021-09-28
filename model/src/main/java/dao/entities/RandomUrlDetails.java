package dao.entities;

import java.util.List;

public record RandomUrlDetails(String url, Long discordId, double avg, long count, List<RandomRating> ratings) {


}
