package dao;

import dao.webhook.Webhook;
import dao.webhook.WebhookTypeData;
import dao.webhook.dao.WebhookDAO;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WebhookDAOTest {

    private final CommonDatasource commonDatasource = new ChuuDatasource();

    private final WebhookDAO webhookDAO = new WebhookDAO();
    private DSLContext context;

    @BeforeEach
    void setup() throws SQLException {
        context = DSL.using(commonDatasource.getConnection());

    }

    @Test
    void createWebhook() {
        Webhook<WebhookTypeData.BandcampReleases> toInsert = new Webhook<>(-1L, -2L, -3L, "abcd",
                new WebhookTypeData.BandcampReleases(List.of("shoegaze", "vocaloid")));
        boolean b = webhookDAO.create(context, toInsert);
        assertTrue(b);
        var result = webhookDAO.obtainAll(context);
        assertEquals(1, result.size());
        var first = result.getFirst();
        Assertions.assertEquals(first, toInsert);
        assertTrue(webhookDAO.delete(context, toInsert.url()));
        result = webhookDAO.obtainAll(context);
        assertTrue(result.isEmpty());

    }

}
