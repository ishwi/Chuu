package dao.entities;

import java.util.List;

public record StolenCrownWrapper(long ogId, long quriedId, List<StolenCrown> list) {
}
