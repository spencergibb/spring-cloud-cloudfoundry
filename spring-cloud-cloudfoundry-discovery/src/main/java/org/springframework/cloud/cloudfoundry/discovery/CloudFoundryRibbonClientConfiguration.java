package org.springframework.cloud.cloudfoundry.discovery;

import com.netflix.client.config.CommonClientConfigKey;
import com.netflix.client.config.IClientConfig;
import com.netflix.config.ConfigurationManager;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;
import com.netflix.loadbalancer.ServerList;
import org.cloudfoundry.client.lib.CloudFoundryClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * @author <A href="mailto:josh@joshlong.com">Josh Long</A>
 */
@Configuration
public class CloudFoundryRibbonClientConfiguration {

    protected static final String DEFAULT_NAMESPACE = "ribbon";
    protected static final String VALUE_NOT_SET = "__not__set__";

    @Value("${ribbon.client.name:client}")
    private String serviceId  ;

    @Bean
    @ConditionalOnMissingBean
    public ServerList<?> ribbonServerList(CloudFoundryClient cloudFoundryClient, IClientConfig config) {
        CloudFoundryServerList cloudFoundryServerList = new CloudFoundryServerList(cloudFoundryClient);
        cloudFoundryServerList.initWithNiwsConfig(config);
        return cloudFoundryServerList;
    }

    @PostConstruct
    public void postConstruct() {
        // FIXME: what should this be?
        setProp(this.serviceId, CommonClientConfigKey.DeploymentContextBasedVipAddresses.key(), this.serviceId);
        setProp(this.serviceId, CommonClientConfigKey.EnableZoneAffinity.key(), "true");
    }

    protected void setProp(String serviceId, String suffix, String value) {
        // how to set the namespace properly?
        String key = getKey(serviceId, suffix);
        DynamicStringProperty property = getProperty(key);
        if (property.get().equals(VALUE_NOT_SET)) {
            ConfigurationManager.getConfigInstance().setProperty(key, value);
        }
    }

    protected DynamicStringProperty getProperty(String key) {
        return DynamicPropertyFactory.getInstance().getStringProperty(key, VALUE_NOT_SET);
    }

    protected String getKey(String serviceId, String suffix) {
        return serviceId + "." + DEFAULT_NAMESPACE + "." + suffix;
    }
}