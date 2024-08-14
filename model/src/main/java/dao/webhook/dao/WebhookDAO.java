package dao.webhook.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dao.jooq.tables.Webhooks;
import dao.webhook.Webhook;
import dao.webhook.WebhookTypeData;
import org.jooq.Converter;
import org.jooq.DSLContext;
import org.jooq.Record6;
import org.jooq.impl.EnumConverter;
import org.jooq.tools.json.JSONArray;

import java.util.List;

public class WebhookDAO {


    private static Webhook<?> entityToDTO(Record6<Long, Long, Long, String, WebhookTypeData.WebhookType, String> record) {
        return switch (record.component5()) {
            case BANDCAMP_RELEASE -> {
                String s = record.component6();
                List<String> strings;
                try {
                    strings = new ObjectMapper().readerForListOf(String.class).readValue(s);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
                yield new Webhook<>(record.component1(), record.component2(), record.component3(), record.component4(), new WebhookTypeData.BandcampReleases(strings));
            }
        };
    }

    public boolean create(DSLContext dsl, Webhook<WebhookTypeData.BandcampReleases> webhook) {
        Converter<String, List> from = Converter.to(String.class, List.class, JSONArray::toJSONString);

        return dsl.insertInto(Webhooks.WEBHOOKS)
                       .columns(
                               Webhooks.WEBHOOKS.GUILD_ID,
                               Webhooks.WEBHOOKS.WEBHOOK_ID,
                               Webhooks.WEBHOOKS.CHANNEL_ID,
                               Webhooks.WEBHOOKS.URL,
                               Webhooks.WEBHOOKS.TYPE.convert(new EnumConverter<>(String.class, WebhookTypeData.WebhookType.class)),
                               Webhooks.WEBHOOKS.GENRE_RELEASES.convert(from)
                       )
                       .values(webhook.guildId(), webhook.webhookId(), webhook.channelId(), webhook.url(), webhook.data().type(), webhook.data().genres())
                       .execute() > 0;

    }

    public boolean delete(DSLContext dsl, String url) {
        return dsl.deleteFrom(Webhooks.WEBHOOKS).where(Webhooks.WEBHOOKS.URL.eq(url)).execute() > 0;
    }

    public List<Webhook<WebhookTypeData.BandcampReleases>> obtainAll(DSLContext dsl) {
        return dsl.select(Webhooks.WEBHOOKS.GUILD_ID,
                        Webhooks.WEBHOOKS.WEBHOOK_ID,
                        Webhooks.WEBHOOKS.CHANNEL_ID, Webhooks.WEBHOOKS.URL,
                        Webhooks.WEBHOOKS.TYPE.convert(new EnumConverter<>(String.class, WebhookTypeData.WebhookType.class)),
                        Webhooks.WEBHOOKS.GENRE_RELEASES)
                .from(Webhooks.WEBHOOKS)
                .where(Webhooks.WEBHOOKS.TYPE.eq(WebhookTypeData.WebhookType.BANDCAMP_RELEASE.toString()))
                .fetch().map(record -> switch (record.component5()) {
                    case BANDCAMP_RELEASE -> {
                        String s = record.component6();
                        List<String> strings;
                        try {
                            strings = new ObjectMapper().readerForListOf(String.class).readValue(s);
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                        yield new Webhook<>(record.component1(), record.component2(), record.component3(), record.component4(), new WebhookTypeData.BandcampReleases(strings));
                    }
                });

    }

    public List<Webhook<?>> obtainAllGuildWebhooks(DSLContext dsl, long guildId) {
        return dsl.select(Webhooks.WEBHOOKS.GUILD_ID,
                        Webhooks.WEBHOOKS.WEBHOOK_ID,
                        Webhooks.WEBHOOKS.CHANNEL_ID, Webhooks.WEBHOOKS.URL,
                        Webhooks.WEBHOOKS.TYPE.convert(new EnumConverter<>(String.class, WebhookTypeData.WebhookType.class)),
                        Webhooks.WEBHOOKS.GENRE_RELEASES)
                .from(Webhooks.WEBHOOKS)
                .where(Webhooks.WEBHOOKS.GUILD_ID.eq(guildId))
                .fetch().map(WebhookDAO::entityToDTO);

    }
}
