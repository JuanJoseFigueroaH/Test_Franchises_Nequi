package com.nequi.franchise.infrastructure.observability;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;

import java.time.Duration;

@Component("dynamodb")
public class DynamoDBHealthIndicator implements ReactiveHealthIndicator {

    private final DynamoDbEnhancedAsyncClient dynamoDbClient;
    private final String tableName;

    public DynamoDBHealthIndicator(
            DynamoDbEnhancedAsyncClient dynamoDbClient,
            org.springframework.beans.factory.annotation.Value("${aws.dynamodb.table-name}") String tableName) {
        this.dynamoDbClient = dynamoDbClient;
        this.tableName = tableName;
    }

    @Override
    public Mono<Health> health() {
        return checkDynamoDBHealth()
                .timeout(Duration.ofSeconds(3))
                .onErrorResume(ex -> Mono.just(
                    Health.down()
                        .withDetail("error", ex.getMessage())
                        .withDetail("errorType", ex.getClass().getSimpleName())
                        .withDetail("table", tableName)
                        .build()
                ));
    }

    private Mono<Health> checkDynamoDBHealth() {
        return Mono.fromFuture(
                dynamoDbClient.dynamoDbClient()
                    .describeTable(DescribeTableRequest.builder()
                        .tableName(tableName)
                        .build())
            )
            .map(response -> Health.up()
                .withDetail("dynamodb", "available")
                .withDetail("table", tableName)
                .withDetail("status", response.table().tableStatus().toString())
                .withDetail("itemCount", response.table().itemCount())
                .build())
            .onErrorResume(ex -> Mono.just(
                Health.down()
                    .withDetail("dynamodb", "unavailable")
                    .withDetail("table", tableName)
                    .withDetail("error", ex.getMessage())
                    .build()
            ));
    }
}
