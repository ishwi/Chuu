package dao.webhook;

public record Webhook<T extends WebhookTypeData>(long guildId, long webhookId, long channelId, String url, T data) {
}
