package org.motechproject.umurinzi.importer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.motechproject.messagecampaign.domain.campaign.CampaignRecord;
import org.motechproject.messagecampaign.loader.CampaignJsonLoader;
import org.motechproject.messagecampaign.service.MessageCampaignService;

public class MessageCampaignImporterTest {

    @InjectMocks
    private MessageCampaignImporter messageCampaignImporter = new MessageCampaignImporter();

    private CampaignJsonLoader campaignJsonLoader = new CampaignJsonLoader();

    @Mock
    private MessageCampaignService messageCampaignService;

    @Before
    public void setUp() {
        initMocks(this);
    }

    @Test
    public void shouldImportMessageCampaignRecords() throws IOException {
        Mockito.when(messageCampaignService.getCampaignRecord("TestScreening")).thenReturn(null);

        InputStream campaigns = getClass().getResourceAsStream("/message-campaign.json");
        List<CampaignRecord> campaignRecords = campaignJsonLoader.loadCampaigns(campaigns);
        campaigns.close();

        assertNotNull(campaignRecords);
        assertEquals(1, campaignRecords.size());

        messageCampaignImporter.importMessageCampaigns();

        Mockito.verify(messageCampaignService).saveCampaign(campaignRecords.get(0));
    }

    @Test
    public void shouldNotImportMessageCampaignRecordIfItAlreadyExist() throws IOException {
        InputStream campaigns = getClass().getResourceAsStream("/message-campaign.json");
        List<CampaignRecord> campaignRecords = campaignJsonLoader.loadCampaigns(campaigns);
        campaigns.close();

        assertNotNull(campaignRecords);
        assertEquals(1, campaignRecords.size());

        Mockito.when(messageCampaignService.getCampaignRecord("TestScreening")).thenReturn(campaignRecords.get(0));

        messageCampaignImporter.importMessageCampaigns();

        Mockito.verify(messageCampaignService, Mockito.never()).saveCampaign(Mockito.any(CampaignRecord.class));
    }
}
