package com.kfu.timetracking.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "permissions", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"resource", "operation"})
})
@Getter
@Setter
@NoArgsConstructor
public class Permission implements GrantedAuthority {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String resource;
    
    @Column(nullable = false)
    private String operation;
    
    @ManyToMany(mappedBy = "permissions", fetch = FetchType.LAZY)
    private Set<Role> roles = new HashSet<>();
    
    public Permission(String resource, String operation) {
        this.resource = resource;
        this.operation = operation;
    }
    
    @Override
    public String getAuthority() {
        return String.format("%s:%s", resource.toUpperCase(), operation.toUpperCase());
    }
}

