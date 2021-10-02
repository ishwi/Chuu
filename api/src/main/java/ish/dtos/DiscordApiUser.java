package ish.dtos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.json.JSONPropertyIgnore;

public record DiscordApiUser(long id, String username, String avatar, String discriminator, long public_flags, long flags,
                             String banner, String banner_color, String accent_color,
                             String locale, boolean mfa_enabled, boolean premium_type) {
}
