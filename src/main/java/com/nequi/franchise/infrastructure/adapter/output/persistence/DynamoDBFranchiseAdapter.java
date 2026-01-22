package com.nequi.franchise.infrastructure.adapter.output.persistence;

import com.nequi.franchise.domain.exception.OptimisticLockException;
import com.nequi.franchise.domain.model.Franchise;
import com.nequi.franchise.domain.model.Page;
import com.nequi.franchise.domain.port.output.FranchiseRepositoryPort;
import com.nequi.franchise.infrastructure.adapter.output.persistence.entity.FranchiseEntity;
import com.nequi.franchise.infrastructure.adapter.output.persistence.mapper.FranchiseMapper;
import com.nequi.franchise.infrastructure.util.CursorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DynamoDBFranchiseAdapter implements FranchiseRepositoryPort {

    private static final Logger LOGGER = LoggerFactory.getLogger(DynamoDBFranchiseAdapter.class);
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long RETRY_DELAY_MS = 100;

    private final DynamoDbAsyncTable<FranchiseEntity> franchiseTable;
    private final FranchiseMapper franchiseMapper;

    public DynamoDBFranchiseAdapter(
            DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient,
            FranchiseMapper franchiseMapper,
            @Value("${aws.dynamodb.table-name}") String tableName) {
        this.franchiseTable = dynamoDbEnhancedAsyncClient.table(tableName, TableSchema.fromBean(FranchiseEntity.class));
        this.franchiseMapper = franchiseMapper;
    }

    @Override
    public Mono<Franchise> save(Franchise franchise) {
        return saveWithRetry(franchise, 0);
    }

    private Mono<Franchise> saveWithRetry(Franchise franchise, int attemptNumber) {
        if (attemptNumber >= MAX_RETRY_ATTEMPTS) {
            LOGGER.error("Max retry attempts reached for franchise: {}", franchise.getId());
            return Mono.error(new OptimisticLockException(
                "Failed to save franchise after " + MAX_RETRY_ATTEMPTS + " attempts due to concurrent modifications"
            ));
        }

        FranchiseEntity entity = franchiseMapper.toEntity(franchise);
        
        return Mono.fromFuture(franchiseTable.putItem(entity))
                .thenReturn(franchise)
                .onErrorResume(throwable -> {
                    if (isOptimisticLockError(throwable)) {
                        LOGGER.warn("Optimistic lock conflict detected for franchise: {}, attempt: {}", 
                            franchise.getId(), attemptNumber + 1);
                        
                        return Mono.delay(java.time.Duration.ofMillis(RETRY_DELAY_MS * (attemptNumber + 1)))
                                .flatMap(delay -> findById(franchise.getId()))
                                .flatMap(latestFranchise -> {
                                    franchise.getBranches().clear();
                                    franchise.getBranches().addAll(latestFranchise.getBranches());
                                    return saveWithRetry(franchise, attemptNumber + 1);
                                });
                    }
                    return Mono.error(throwable);
                });
    }

    private boolean isOptimisticLockError(Throwable throwable) {
        if (throwable instanceof ConditionalCheckFailedException) {
            return true;
        }
        Throwable cause = throwable.getCause();
        return cause instanceof ConditionalCheckFailedException;
    }

    @Override
    public Mono<Franchise> findById(String id) {
        Key key = Key.builder().partitionValue(id).build();
        return Mono.fromFuture(franchiseTable.getItem(key))
                .map(franchiseMapper::toDomain);
    }

    @Override
    public Mono<Void> delete(String id) {
        Key key = Key.builder().partitionValue(id).build();
        return Mono.fromFuture(franchiseTable.deleteItem(key))
                .then();
    }

    @Override
    public Mono<Page<Franchise>> findAll(Integer pageSize, String cursor) {
        LOGGER.debug("Finding all franchises with pageSize: {} and cursor: {}", pageSize, cursor);
        
        Map<String, AttributeValue> exclusiveStartKey = null;
        if (cursor != null) {
            Map<String, String> decodedCursor = CursorUtil.decodeCursor(cursor);
            if (decodedCursor != null && decodedCursor.containsKey("id")) {
                exclusiveStartKey = new HashMap<>();
                exclusiveStartKey.put("id", AttributeValue.builder().s(decodedCursor.get("id")).build());
            }
        }

        ScanEnhancedRequest.Builder requestBuilder = ScanEnhancedRequest.builder()
                .limit(pageSize);
        
        if (exclusiveStartKey != null) {
            requestBuilder.exclusiveStartKey(exclusiveStartKey);
        }

        return Flux.from(franchiseTable.scan(requestBuilder.build()))
                .next()
                .flatMap(page -> {
                    List<Franchise> franchises = new ArrayList<>();
                    page.items().forEach(entity -> franchises.add(franchiseMapper.toDomain(entity)));

                    String nextCursor = null;
                    if (page.lastEvaluatedKey() != null && !page.lastEvaluatedKey().isEmpty()) {
                        AttributeValue lastKeyValue = page.lastEvaluatedKey().get("id");
                        if (lastKeyValue != null) {
                            Map<String, String> cursorData = new HashMap<>();
                            cursorData.put("id", lastKeyValue.s());
                            nextCursor = CursorUtil.encodeCursor(cursorData);
                        }
                    }

                    LOGGER.debug("Found {} franchises, hasMore: {}", franchises.size(), nextCursor != null);
                    return Mono.just(Page.of(franchises, nextCursor, pageSize));
                })
                .defaultIfEmpty(Page.empty());
    }
}
