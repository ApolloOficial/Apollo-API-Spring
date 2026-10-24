package org.apollo.api.sungrow;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "apollo.sungrow")
public class SungrowProperties implements InitializingBean {
    public static final long MIN_POLL_INTERVAL_SECONDS = 300;

    private boolean enabled = false;
    private String baseUrl = "https://gateway.isolarcloud.com.hk";
    private String appkey = "";
    private String accessKey = "";
    private String redirectUri = "";
    private String lang = "_en_US";
    private long timeoutSeconds = 30;
    private long pollIntervalSeconds = MIN_POLL_INTERVAL_SECONDS;
    private long initialDelaySeconds = 30;
    private String brand = "Sungrow";
    private String deviceTimeZone = "America/Sao_Paulo";
    private String dataSource = "SENSOR";
    private String plantMode = "live";
    private int simulationStepMinutes = 5;
    private boolean simulationAlwaysSun = false;
    private String simulationFaultSerials = "";
    private String simulationWeakStrings = "";
    private int batchSize = 20;
    private long tokenRefreshMarginSeconds = 600;
    private String tokenPath = "/openapi/apiManage/token";
    private String refreshPath = "/openapi/apiManage/refreshToken";
    private String realTimePath = "/openapi/platform/getDeviceRealTimeData";

    @Override
    public void afterPropertiesSet() {
        if (!enabled) {
            return;
        }
        if (!isSimulated() && !"live".equals(plantMode)) {
            throw new IllegalStateException("apollo.sungrow.plant-mode must be live or simulated");
        }
        if (!"SENSOR".equals(dataSource) && !"SIMULATOR".equals(dataSource)) {
            throw new IllegalStateException("apollo.sungrow.data-source must be SENSOR or SIMULATOR");
        }
        if (!isSimulated()) {
            requireText("apollo.sungrow.appkey", appkey);
            requireText("apollo.sungrow.access-key", accessKey);
            requireText("apollo.sungrow.redirect-uri", redirectUri);
        }
        if ("SENSOR".equals(effectiveDataSource()) && pollIntervalSeconds < MIN_POLL_INTERVAL_SECONDS) {
            throw new IllegalStateException("apollo.sungrow.poll-interval-seconds must be at least "
                    + MIN_POLL_INTERVAL_SECONDS + " (Sungrow refreshes real-time data every ~5 minutes)");
        }
        if (pollIntervalSeconds < 1) {
            throw new IllegalStateException("apollo.sungrow.poll-interval-seconds must be at least 1");
        }
        if (batchSize < 1) {
            throw new IllegalStateException("apollo.sungrow.batch-size must be at least 1");
        }
    }

    public boolean isSimulated() {
        return "simulated".equals(plantMode);
    }

    public String effectiveDataSource() {
        return isSimulated() ? "SIMULATOR" : dataSource;
    }

    private static void requireText(String name, String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(name + " must be set when apollo.sungrow.enabled=true");
        }
    }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public String getAppkey() { return appkey; }
    public void setAppkey(String appkey) { this.appkey = appkey; }
    public String getAccessKey() { return accessKey; }
    public void setAccessKey(String accessKey) { this.accessKey = accessKey; }
    public String getRedirectUri() { return redirectUri; }
    public void setRedirectUri(String redirectUri) { this.redirectUri = redirectUri; }
    public String getLang() { return lang; }
    public void setLang(String lang) { this.lang = lang; }
    public long getTimeoutSeconds() { return timeoutSeconds; }
    public void setTimeoutSeconds(long timeoutSeconds) { this.timeoutSeconds = timeoutSeconds; }
    public long getPollIntervalSeconds() { return pollIntervalSeconds; }
    public void setPollIntervalSeconds(long pollIntervalSeconds) { this.pollIntervalSeconds = pollIntervalSeconds; }
    public long getInitialDelaySeconds() { return initialDelaySeconds; }
    public void setInitialDelaySeconds(long initialDelaySeconds) { this.initialDelaySeconds = initialDelaySeconds; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public String getDeviceTimeZone() { return deviceTimeZone; }
    public void setDeviceTimeZone(String deviceTimeZone) { this.deviceTimeZone = deviceTimeZone; }
    public String getDataSource() { return dataSource; }
    public void setDataSource(String dataSource) { this.dataSource = dataSource; }
    public String getPlantMode() { return plantMode; }
    public void setPlantMode(String plantMode) { this.plantMode = plantMode == null ? "live" : plantMode.trim().toLowerCase(java.util.Locale.ROOT); }
    public int getSimulationStepMinutes() { return simulationStepMinutes; }
    public void setSimulationStepMinutes(int simulationStepMinutes) { this.simulationStepMinutes = simulationStepMinutes; }
    public boolean isSimulationAlwaysSun() { return simulationAlwaysSun; }
    public void setSimulationAlwaysSun(boolean simulationAlwaysSun) { this.simulationAlwaysSun = simulationAlwaysSun; }
    public String getSimulationFaultSerials() { return simulationFaultSerials; }
    public void setSimulationFaultSerials(String simulationFaultSerials) { this.simulationFaultSerials = simulationFaultSerials; }
    public String getSimulationWeakStrings() { return simulationWeakStrings; }
    public void setSimulationWeakStrings(String simulationWeakStrings) { this.simulationWeakStrings = simulationWeakStrings; }
    public int getBatchSize() { return batchSize; }
    public void setBatchSize(int batchSize) { this.batchSize = batchSize; }
    public long getTokenRefreshMarginSeconds() { return tokenRefreshMarginSeconds; }
    public void setTokenRefreshMarginSeconds(long tokenRefreshMarginSeconds) { this.tokenRefreshMarginSeconds = tokenRefreshMarginSeconds; }
    public String getTokenPath() { return tokenPath; }
    public void setTokenPath(String tokenPath) { this.tokenPath = tokenPath; }
    public String getRefreshPath() { return refreshPath; }
    public void setRefreshPath(String refreshPath) { this.refreshPath = refreshPath; }
    public String getRealTimePath() { return realTimePath; }
    public void setRealTimePath(String realTimePath) { this.realTimePath = realTimePath; }
}
