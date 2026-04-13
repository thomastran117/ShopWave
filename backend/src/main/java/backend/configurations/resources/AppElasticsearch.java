package backend.configurations.resources;

import backend.configurations.environment.EnvironmentSetting;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class AppElasticsearch {

    private final EnvironmentSetting env;

    public AppElasticsearch(EnvironmentSetting env) {
        this.env = env;
    }

    @Bean
    public ElasticsearchClient elasticsearchClient() {
        EnvironmentSetting.Elasticsearch es = env.getElasticsearch();

        RestClientBuilder builder = RestClient.builder(
                new HttpHost(es.getHost(), es.getPort(), es.getScheme()));

        if (!es.getApiKey().isBlank()) {
            builder.setDefaultHeaders(new org.apache.http.Header[]{
                    new BasicHeader("Authorization", "ApiKey " + es.getApiKey())
            });
        } else if (!es.getUsername().isBlank()) {
            BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY,
                    new UsernamePasswordCredentials(es.getUsername(), es.getPassword()));
            builder.setHttpClientConfigCallback(
                    httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider));
        }

        RestClientTransport transport = new RestClientTransport(builder.build(), new JacksonJsonpMapper());
        return new ElasticsearchClient(transport);
    }

    @Bean("searchExecutor")
    public Executor searchExecutor() {
        EnvironmentSetting.Elasticsearch.Executor cfg = env.getElasticsearch().getExecutor();
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(cfg.getCorePoolSize());
        executor.setMaxPoolSize(cfg.getMaxPoolSize());
        executor.setQueueCapacity(cfg.getQueueCapacity());
        executor.setThreadNamePrefix("search-async-");
        executor.initialize();
        return executor;
    }
}
