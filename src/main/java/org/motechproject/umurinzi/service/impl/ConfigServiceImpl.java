package org.motechproject.umurinzi.service.impl;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.motechproject.config.core.constants.ConfigurationConstants;
import org.motechproject.event.MotechEvent;
import org.motechproject.event.listener.annotations.MotechListener;
import org.motechproject.umurinzi.domain.Config;
import org.motechproject.umurinzi.service.ConfigService;
import org.motechproject.server.config.SettingsFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

@Service("configService")
public class ConfigServiceImpl implements ConfigService {

    private static final String UMURINZI_CONFIG_JSON = "umurinzi-config.json";
    private static final String UMURINZI_CONFIG_FILE_PATH = "/" + ConfigurationConstants.RAW_DIR + "/" + UMURINZI_CONFIG_JSON;

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigServiceImpl.class);

    private SettingsFacade settingsFacade;
    private Config config;

    @Autowired
    public ConfigServiceImpl(@Qualifier("umurinziSettings") SettingsFacade settingsFacade) {
        this.settingsFacade = settingsFacade;
        loadConfig();
    }

    private synchronized void loadConfig() {
        try (InputStream is = settingsFacade.getRawConfig(UMURINZI_CONFIG_JSON)) {
            String jsonText = IOUtils.toString(is);
            Gson gson = new Gson();
            config = gson.fromJson(jsonText, Config.class);
        } catch (Exception e) {
            throw new JsonIOException("Malformed " + UMURINZI_CONFIG_JSON + " file? " + e.toString(), e);
        }
    }

    @MotechListener(subjects = { ConfigurationConstants.FILE_CHANGED_EVENT_SUBJECT })
    public void handleFileChanged(MotechEvent event) {
        String filePath = (String) event.getParameters().get(ConfigurationConstants.FILE_PATH);
        if (!StringUtils.isBlank(filePath) && filePath.endsWith(UMURINZI_CONFIG_FILE_PATH)) {
            LOGGER.info("{} has changed, reloading config.", UMURINZI_CONFIG_JSON);
            loadConfig();
        }
    }

    @Override
    public Config getConfig() {
        return config;
    }

    @Override
    public void updateConfig(Config config) {
        Gson gson = new Gson();
        String jsonText = gson.toJson(config, Config.class);
        ByteArrayResource resource = new ByteArrayResource(jsonText.getBytes());
        settingsFacade.saveRawConfig(UMURINZI_CONFIG_JSON, resource);
        loadConfig();
    }
}
