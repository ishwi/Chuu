package core.services.tags;

import dao.ChuuService;
import dao.entities.Genre;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public record TagCleaner(ChuuService db) {
    public List<String> cleanTags(Collection<String> strings) {
        if (strings.isEmpty()) {
            return Collections.emptyList();
        }
        Set<Genre> bannedTags = db.getBannedTags();
        return strings.stream().filter(o -> bannedTags.contains(new Genre(o))).toList();
    }
}
