package core.services;

import dao.ChuuService;
import dao.entities.VoiceAnnouncement;

public class VoiceAnnounceService {
    private final ChuuService chuuService;

    public VoiceAnnounceService(ChuuService chuuService) {
        this.chuuService = chuuService;
    }

    public VoiceAnnouncement getVoiceAnnouncement(long guildId) {
        return this.chuuService.getGuildVoiceAnnouncement(guildId);
    }

    public void setVoiceAnnouncement(long guildId, Long channelId, boolean enabled) {
        this.chuuService.setGuildVoiceAnnouncement(guildId, new VoiceAnnouncement(channelId, enabled));
    }


}
