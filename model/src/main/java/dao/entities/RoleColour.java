package dao.entities;

import java.awt.*;

public record RoleColour(long id, long guildId, Color color, int start, int end, long roleId) {
}
