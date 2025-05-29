package sansam.shimbox.global.config;


import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import sansam.shimbox.global.common.BaseResponse;
import sansam.shimbox.global.exception.ErrorCode;
import sansam.shimbox.global.swagger.ApiErrorCodeExample;
import sansam.shimbox.global.swagger.ApiErrorCodeExamples;
import sansam.shimbox.global.swagger.ExampleHolder;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@OpenAPIDefinition(
        info = @Info(
                title = "ShimBox API Docs",
                description = "쉼박스 api 문서",
                version = "v1"
        )
)
@Configuration
public class SwaggerConfig {

    private static final String SECURITY_SCHEME_NAME = "JWT";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("Bearer")
                                        .bearerFormat("JWT")));
    }

    @Bean
    public OperationCustomizer customize() {
        return (operation, handlerMethod) -> {
            ApiErrorCodeExamples examplesAnn = handlerMethod.getMethodAnnotation(ApiErrorCodeExamples.class);
            if (examplesAnn != null) {
                generateErrorCodeResponseExample(operation, examplesAnn.value());
            } else {
                ApiErrorCodeExample exampleAnn = handlerMethod.getMethodAnnotation(ApiErrorCodeExample.class);
                if (exampleAnn != null) {
                    generateErrorCodeResponseExample(operation, exampleAnn.value());
                }
            }
            return operation;
        };
    }

    private void generateErrorCodeResponseExample(Operation operation, ErrorCode[] errorCodes) {
        ApiResponses responses = operation.getResponses();

        Map<Integer, List<ExampleHolder>> statusWithExampleHolders = Arrays.stream(errorCodes)
                .map(errorCode -> ExampleHolder.builder()
                        .holder(getSwaggerExample(errorCode))
                        .code(errorCode.getHttpStatus().value())
                        .name(errorCode.name())
                        .build())
                .collect(Collectors.groupingBy(ExampleHolder::getCode));

        addExamplesToResponses(responses, statusWithExampleHolders);
    }

    private void generateErrorCodeResponseExample(Operation operation, ErrorCode errorCode) {
        ApiResponses responses = operation.getResponses();

        ExampleHolder exampleHolder = ExampleHolder.builder()
                .holder(getSwaggerExample(errorCode))
                .code(errorCode.getHttpStatus().value())
                .name(errorCode.name())
                .build();

        addExamplesToResponses(responses, exampleHolder);
    }

    private Example getSwaggerExample(ErrorCode errorCode) {
        BaseResponse<Void> responseDto = BaseResponse.error(errorCode.getMessage(), errorCode.getHttpStatus());
        Example example = new Example();
        example.setValue(responseDto);
        return example;
    }

    private void addExamplesToResponses(ApiResponses responses, Map<Integer, List<ExampleHolder>> map) {
        map.forEach((status, holders) -> {
            Content content = new Content();
            MediaType mediaType = new MediaType();
            ApiResponse apiResponse = new ApiResponse();

            holders.forEach(holder -> mediaType.addExamples(holder.getName(), holder.getHolder()));
            content.addMediaType("application/json", mediaType);
            apiResponse.setContent(content);
            responses.addApiResponse(String.valueOf(status), apiResponse);
        });
    }

    private void addExamplesToResponses(ApiResponses responses, ExampleHolder holder) {
        Content content = new Content();
        MediaType mediaType = new MediaType();
        ApiResponse apiResponse = new ApiResponse();

        mediaType.addExamples(holder.getName(), holder.getHolder());
        content.addMediaType("application/json", mediaType);
        apiResponse.setContent(content);
        responses.addApiResponse(String.valueOf(holder.getCode()), apiResponse);
    }
}
