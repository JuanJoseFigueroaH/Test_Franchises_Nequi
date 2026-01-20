package com.nequi.franchise.infrastructure.adapter.output.persistence;

import com.nequi.franchise.domain.model.Franchise;
import com.nequi.franchise.domain.port.output.FranchiseRepositoryPort;
import com.nequi.franchise.infrastructure.adapter.output.persistence.entity.FranchiseEntity;
import com.nequi.franchise.infrastructure.adapter.output.persistence.mapper.FranchiseMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@Component
public class DynamoDBFranchiseAdapter implements FranchiseRepositoryPort {

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
        FranchiseEntity entity = franchiseMapper.toEntity(franchise);
        return Mono.fromFuture(franchiseTable.putItem(entity))
                .thenReturn(franchiseMapper.toDomain(entity));
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
}
