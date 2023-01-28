package dao.entities;

import java.util.List;

public record UniqueWrapper<T>(int rows, long discordId, String lastFmId, List<T> uniqueData) {
}
