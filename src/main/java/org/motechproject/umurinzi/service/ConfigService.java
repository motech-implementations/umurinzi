package org.motechproject.umurinzi.service;

import org.motechproject.umurinzi.domain.Config;

public interface ConfigService {

    Config getConfig();

    void updateConfig(Config config);
}
