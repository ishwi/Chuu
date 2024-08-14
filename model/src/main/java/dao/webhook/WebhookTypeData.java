package dao.webhook;

import java.util.List;

public sealed interface WebhookTypeData {
    WebhookType type();


    enum WebhookType {BANDCAMP_RELEASE}

    record BandcampReleases(List<String> genres) implements WebhookTypeData {

        @Override
        public WebhookType type() {
            return WebhookType.BANDCAMP_RELEASE;
        }
    }

}
