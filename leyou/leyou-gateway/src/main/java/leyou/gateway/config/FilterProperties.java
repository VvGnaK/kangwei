package leyou.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * @author VvGnaK
 * @date 2020-03-14 19:04
 */
@ConfigurationProperties(prefix = "leyou.filter")
public class FilterProperties {
    private List<String> allowPaths;

    public List<String> getAllowPaths() {
        return allowPaths;
    }
    public void setAllowPaths(List<String> allowPaths) {
        this.allowPaths = allowPaths;
    }
}
