package com.nequi.franchise.infrastructure.adapter.output.persistence;

import com.nequi.franchise.domain.model.Franchise;
import com.nequi.franchise.infrastructure.adapter.output.persistence.entity.FranchiseEntity;
import com.nequi.franchise.infrastructure.adapter.output.persistence.mapper.FranchiseMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DynamoDBFranchiseAdapterTest {

    @Mock
    private DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient;

    @Mock
    private FranchiseMapper franchiseMapper;

    @Mock
    private DynamoDbAsyncTable<FranchiseEntity> franchiseTable;

    private DynamoDBFranchiseAdapter adapter;

    private Franchise franchise;
    private FranchiseEntity franchiseEntity;

    @BeforeEach
    void setUp() {
        when(dynamoDbEnhancedAsyncClient.table(anyString(), any(TableSchema.class)))
                .thenReturn(franchiseTable);

        adapter = new DynamoDBFranchiseAdapter(dynamoDbEnhancedAsyncClient, franchiseMapper, "test-table");

        franchise = Franchise.builder()
                .id("test-id")
                .name("Test Franchise")
                .branches(new ArrayList<>())
                .build();

        franchiseEntity = FranchiseEntity.builder()
                .id("test-id")
                .name("Test Franchise")
                .branches(new ArrayList<>())
                .build();
    }

    @Test
    void save_ShouldSaveFranchiseSuccessfully() {
        when(franchiseMapper.toEntity(any(Franchise.class))).thenReturn(franchiseEntity);
        when(franchiseMapper.toDomain(any(FranchiseEntity.class))).thenReturn(franchise);
        when(franchiseTable.putItem(any(FranchiseEntity.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        var result = adapter.save(franchise);

        StepVerifier.create(result)
                .expectNext(franchise)
                .verifyComplete();

        verify(franchiseMapper, times(1)).toEntity(franchise);
        verify(franchiseTable, times(1)).putItem(franchiseEntity);
    }

    @Test
    void findById_ShouldReturnFranchise() {
        when(franchiseMapper.toDomain(any(FranchiseEntity.class))).thenReturn(franchise);
        when(franchiseTable.getItem(any(Key.class)))
                .thenReturn(CompletableFuture.completedFuture(franchiseEntity));

        var result = adapter.findById("test-id");

        StepVerifier.create(result)
                .expectNext(franchise)
                .verifyComplete();

        verify(franchiseTable, times(1)).getItem(any(Key.class));
        verify(franchiseMapper, times(1)).toDomain(franchiseEntity);
    }

    @Test
    void delete_ShouldDeleteFranchise() {
        when(franchiseTable.deleteItem(any(Key.class)))
                .thenReturn(CompletableFuture.completedFuture(franchiseEntity));

        var result = adapter.delete("test-id");

        StepVerifier.create(result)
                .verifyComplete();

        verify(franchiseTable, times(1)).deleteItem(any(Key.class));
    }
}
