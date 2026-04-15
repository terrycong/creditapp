package com.creditapp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Global controller advice that adds build info to all templates.
 * This allows footer to display version and build date.
 */
@ControllerAdvice
public class BuildInfoAdvice {

    @Value("${creditapp.version:1.0.0}")
    private String version;

    @Value("${creditapp.build.date:unknown}")
    private String buildDate;

    @ModelAttribute("appVersion")
    public String getVersion() {
        return version;
    }

    @ModelAttribute("buildDate")
    public String getBuildDate() {
        return buildDate;
    }
}