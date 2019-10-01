package org.motechproject.umurinzi.importer;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import org.eclipse.gemini.blueprint.service.importer.OsgiServiceLifecycleListener;
import org.motechproject.messagecampaign.domain.campaign.CampaignRecord;
import org.motechproject.messagecampaign.loader.CampaignJsonLoader;
import org.motechproject.messagecampaign.service.MessageCampaignService;
import org.springframework.stereotype.Component;

@Component
public class MessageCampaignImporter implements OsgiServiceLifecycleListener {

    private MessageCampaignService messageCampaignService;

    private CampaignJsonLoader campaignJsonLoader = new CampaignJsonLoader();

    @Override
    public void bind(Object o, Map map) {
        this.messageCampaignService = (MessageCampaignService) o;
        importMessageCampaigns();
    }

    @Override
    public void unbind(Object o, Map map) {
        this.messageCampaignService = null;
    }

    public void importMessageCampaigns() {
        InputStream campaigns = getClass().getResourceAsStream("/message-campaign.json");
        List<CampaignRecord> campaignRecords = campaignJsonLoader.loadCampaigns(campaigns);

        for (CampaignRecord campaignRecord : campaignRecords) {
            if (messageCampaignService.getCampaignRecord(campaignRecord.getName()) == null) {
                messageCampaignService.saveCampaign(campaignRecord);
            }
        }
    }

}
