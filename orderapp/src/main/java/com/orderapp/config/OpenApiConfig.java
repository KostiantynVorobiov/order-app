package com.orderapp.config;

import com.orderapp.model.dto.OrderResponse;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.core.converter.ResolvedSchema;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.orderapp.utils.Constants.*;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Order Service API")
                        .version("1.0.0")
                        .description("API for making orders")
                        .contact(new Contact()
                                .name("Kostiantyn Vorobiov")
                                .email("kostiantyn.vorobiov.kv@gmail.com")))
                .components(new Components()
                        .addSchemas(ORDER_LIST_RESPONSE_SCHEMA, orderListResponseSchema())
                        .addSchemas(ERROR_RESPONSE_SCHEMA, errorResponseSchema())
                        .addSchemas(LONG_RESPONSE_SCHEMA, longResponseSchema())
                        .addSchemas(ORDER_RESPONSE_SCHEMA, orderResponseSchema())
                        .addSchemas(PAGE_CLIENT_RESPONSE_SCHEMA, pageClientResponseSchema())
                        .addSchemas(CLIENT_RESPONSE_SCHEMA, clientResponseSchema())
                        .addSchemas(CLIENT_REQUEST_SCHEMA, clientRequestSchema())
                );
    }

    private Schema<Object> errorResponseSchema() {
        ObjectSchema responseSchema = new ObjectSchema();
        responseSchema.addProperty("success", new BooleanSchema().example(false)
                .description("Always false in case of an error"));
        responseSchema.addProperty("message", new StringSchema().example("The message of an occurred error ")
                .description("Error message"));
        responseSchema.addProperty("data", new Schema<>().nullable(true).example(null)
                .description("Always null in error responses"));
        return responseSchema;
    }

    private Schema<Object> orderListResponseSchema() {
        ObjectSchema responseSchema = new ObjectSchema();
        responseSchema.addProperty("success", new BooleanSchema().example(true));
        responseSchema.addProperty("message", new StringSchema().example("The message of successfully processed data"));
        responseSchema.addProperty("data", new ArraySchema().items(getSchemaFromClass(OrderResponse.class).schema));
        return responseSchema;
    }

    private Schema<Object> getClientResponseSchemaManually() {
        ObjectSchema clientSchema = new ObjectSchema();
        clientSchema.addProperty("name", new StringSchema().example("Alec Hermann DDS"));
        clientSchema.addProperty("email", new StringSchema().example("basil.konopelski@hotmail.com"));
        clientSchema.addProperty("phoneNumber", new StringSchema().example("412-613-3711"));
        clientSchema.addProperty("active", new BooleanSchema().example(true));
        clientSchema.addProperty("inactiveAt", new StringSchema().example("null"));
        clientSchema.addProperty("createdAt", new StringSchema().example("2025-08-13T21:12:42.57488"));

        clientSchema.addProperty("suppliedOrders", new ArraySchema().items(getSchemaFromClass(OrderResponse.class).schema));
        clientSchema.addProperty("consumedOrders", new ArraySchema().items(getSchemaFromClass(OrderResponse.class).schema));

        clientSchema.addProperty("totalProfit", new NumberSchema().example(-452.30));
        return clientSchema;
    }

    private Schema<Object> longResponseSchema() {
        ObjectSchema responseSchema = new ObjectSchema();
        responseSchema.addProperty("success", new BooleanSchema().example(true));
        responseSchema.addProperty("message", new StringSchema().example("The message of successfully processed data"));
        responseSchema.addProperty("data", new IntegerSchema().example(10));
        return responseSchema;
    }

    private Schema<Object> orderResponseSchema() {
        ObjectSchema responseSchema = new ObjectSchema();
        responseSchema.addProperty("success", new BooleanSchema().example(true));
        responseSchema.addProperty("message", new StringSchema().example("The message of successfully processed data"));
        responseSchema.addProperty("data", getSchemaFromClass(OrderResponse.class).schema);
        return responseSchema;
    }

    private Schema<Object> pageClientResponseSchema() {
        ObjectSchema responseSchema = new ObjectSchema();
        responseSchema.addProperty("success", new BooleanSchema().example(true));
        responseSchema.addProperty("message", new StringSchema().example("The message of successfully processed data"));

        ObjectSchema pageSchema = new ObjectSchema();
        pageSchema.addProperty("content", new ArraySchema().items(getClientResponseSchemaManually()));
        pageSchema.addProperty("pageable", new ObjectSchema().addProperty("pageNumber", new IntegerSchema()));
        pageSchema.addProperty("totalElements", new IntegerSchema());
        pageSchema.addProperty("totalPages", new IntegerSchema());
        pageSchema.addProperty("size", new IntegerSchema());

        responseSchema.addProperty("data", pageSchema);
        return responseSchema;
    }

    private Schema<Object> clientResponseSchema() {
        ObjectSchema responseSchema = new ObjectSchema();
        responseSchema.addProperty("success", new BooleanSchema().example(true));
        responseSchema.addProperty("message", new StringSchema().example("The message of successfully processed data"));
        responseSchema.addProperty("data", getClientResponseSchemaManually());
        return responseSchema;
    }

    private Schema<Object> clientRequestSchema() {
        ObjectSchema responseSchema = new ObjectSchema();
        responseSchema.addProperty("name", new StringSchema().example("Bob Alison"));
        responseSchema.addProperty("email", new StringSchema().example("bob.alison@mail.com"));
        responseSchema.addProperty("phoneNumber", new StringSchema().example("333-555-7777"));
        return responseSchema;
    }

    private <T> ResolvedSchema getSchemaFromClass(Class<T> classForSchema) {
        return ModelConverters.getInstance()
                .resolveAsResolvedSchema(new AnnotatedType(classForSchema));
    }
}
