package com.sshakusora.simplytentsblacklist;

import com.mojang.logging.LogUtils;
import com.sshakusora.simplytentsblacklist.config.BlacklistConfig;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod(Simplytentsblacklist.MOD_ID)
public class Simplytentsblacklist {

    public static final String MOD_ID = "simplytentsblacklist";
    private static final Logger LOGGER = LogUtils.getLogger();

    public Simplytentsblacklist() {
        BlacklistConfig.register();
        LOGGER.info("Simply Tents Blacklist mod loaded!");
    }
}
