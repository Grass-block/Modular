package me.gb2022.modular;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;


public class Debug {
    private static final Logger log = LogManager.getLogger("Debug");

    static {
        setEnabled(false);
    }

    public static void setEnabled(boolean enabled) {
        if (enabled) {
            Configurator.setLevel("Debug", Level.ALL);
        } else {
            Configurator.setLevel("Debug", Level.OFF);
        }
    }

    public static Logger log() {
        return log;
    }
}
