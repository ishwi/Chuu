package ish.dtos;

public record AccessTokenDiscord(String accessToken, String tokenType, int expires, String refreshToken,
                                 String scope) {
}
