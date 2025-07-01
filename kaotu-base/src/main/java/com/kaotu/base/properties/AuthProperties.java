package com.kaotu.base.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;


@Data
@Component
@ConfigurationProperties(prefix = "kaotu.auth")
public class AuthProperties {
    private List<String> whitelist;
/*    public List<String> getWhitelist() {
        return whitelist;
    }
    public void setWhitelist(List<String> whitelist) {
        this.whitelist = whitelist;
    }*/

}
