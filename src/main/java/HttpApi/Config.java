package HttpApi;

import HttpApi.Models.JobTriggerBody;
import Utils.PojoConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Update by andrey on 23:41 01.03.2021
 */
public class Config {
    public final Logger logger = LoggerFactory.getLogger(Config.class);
    private final RequestSpecification spec;
    private final String specificationURL;
    private final RestAssuredConfig config = RestAssured.config()
            .objectMapperConfig(
                    ObjectMapperConfig.objectMapperConfig().jackson2ObjectMapperFactory(
                            (type, s) -> {
                                ObjectMapper mapper = new ObjectMapper();
                                mapper.registerModule(new JavaTimeModule());
                                mapper.enable(SerializationFeature.INDENT_OUTPUT);
                                return mapper;
                            }))
            .httpClient(HttpClientConfig.httpClientConfig()
                            .setParam("http.connection.timeout", 50000)
                            .setParam("http.socket.timeout", 50000)
                            .setParam("http.connection-manager.timeout", 50000)
            );

    public Config(String callBackChannel) {
        specificationURL = "https://ci.bll-i.co.uk/generic-webhook-trigger/invoke";
        spec = new RequestSpecBuilder()
                .setBaseUri(specificationURL)
                .setContentType(ContentType.JSON)
                .addFilter(new RequestLoggingFilter())
                .addFilter(new ResponseLoggingFilter())
                .build();
    }

    @SneakyThrows
    public void postResource(String tags, String text) {
        String ciUrl = specificationURL+"?token=Slack&branch=master";
        JobTriggerBody jobTriggerBody = new JobTriggerBody();
        jobTriggerBody.setTags(tags);
        jobTriggerBody.setCallBackCannelId(text);
        String response = RestAssured.given()
                .config(config)
//                .header("Authorization", "Bearer " + System.getenv("SLACK_BOT_TOKEN"))
                .header("Content-Type", "application/json")
                .spec(spec)
                .body(jobTriggerBody)
                .expect()
                .statusCode(200)
                .when()
                .post(ciUrl)
                .thenReturn().getBody().asString();
        logger.info("RESULT OPERATION:");
        logger.info(PojoConverter.convertStringToPrettyJsonString(response));
    }
}
