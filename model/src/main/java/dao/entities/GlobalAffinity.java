package dao.entities;

public class GlobalAffinity extends Affinity {
    private final PrivacyMode privacyMode;

    public GlobalAffinity(long threshold, long matchingCount, long closeMatch, long trueMatching, long sampleSize, String ogLastFmId, String receivingLastFmId, PrivacyMode privacyMode) {
        super(threshold, matchingCount, closeMatch, trueMatching, sampleSize, ogLastFmId, receivingLastFmId);
        this.privacyMode = privacyMode;
    }

    public PrivacyMode getPrivacyMode() {
        return privacyMode;
    }
}
