package ish.dtos;

public record UserDetails(long id, String username, String avatar, String discriminator, long public_flags, long flags,
                          String locale, boolean mfa_enabled) {
}
