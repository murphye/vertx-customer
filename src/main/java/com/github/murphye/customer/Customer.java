package com.github.murphye.customer;

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Table(keyspace = "customer", name = "customer", readConsistency = "QUORUM", writeConsistency = "QUORUM")
@NoArgsConstructor
@Data
public final class Customer implements Serializable {

    @PartitionKey
    private UUID id;

    @Column
    @NonNull
    private String name;

    @Column
    @NonNull
    private String city;

    @Column
    @NonNull
    private String state;

    @Column
    @NonNull
    private String zipCode;

    public UUID getId() {
        if(id == null) {
            id = UUID.randomUUID();
        }
        return id;
    }
}
