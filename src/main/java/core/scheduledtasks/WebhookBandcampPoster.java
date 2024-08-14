package core.scheduledtasks;

import club.minnced.discord.webhook.WebhookCluster;
import club.minnced.discord.webhook.receive.ReadonlyMessage;
import club.minnced.discord.webhook.send.WebhookEmbed;
import core.Chuu;
import core.apis.bandcamp.BandcampApi;
import dao.ChuuService;
import dao.webhook.Webhook;
import dao.webhook.WebhookTypeData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class WebhookBandcampPoster {

    private final ChuuService chuuService;

    public WebhookBandcampPoster(ChuuService chuuService) {
        this.chuuService = chuuService;
    }

    public void postWebhooks() {

        var webhooks = chuuService.obtainAllWebhooks();
        Chuu.getLogger().info("Posting webhooks | Posting to {} clients", webhooks.size());
        Map<Set<String>, List<Webhook<WebhookTypeData.BandcampReleases>>> collect = webhooks.stream().collect(Collectors.groupingBy(z -> new HashSet<>(z.data().genres())));


        List<CompletableFuture<ReadonlyMessage>> futures = new ArrayList<>();
        collect.forEach((strings, webhooks1) -> {
            try (WebhookCluster cluster = new WebhookCluster(webhooks1.size())) {
                for (Webhook<WebhookTypeData.BandcampReleases> hook : webhooks1) {
                    cluster.buildWebhook(hook.webhookId(), hook.url());
                }
                List<BandcampApi.Result> results = new BandcampApi().discoverReleases(strings.stream().toList());
                List<WebhookEmbed> embeds = results.stream().limit(10).map(subresult -> new WebhookEmbed(null, null, subresult.artistName(), null, subresult.imageUrl(), null, new WebhookEmbed.EmbedTitle(subresult.title(), subresult.url()), null, Collections.emptyList())).toList();
                futures.addAll(cluster.broadcast(embeds));
            }
        });
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }
}
