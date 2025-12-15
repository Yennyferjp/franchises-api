    package com.yennyfer.franchises_api.model;

    import lombok.AllArgsConstructor;
    import lombok.Builder;
    import lombok.Getter;
    import lombok.NoArgsConstructor;
    import lombok.Setter;
    import org.springframework.data.annotation.Id;
    import org.springframework.data.relational.core.mapping.Column;
    import org.springframework.data.relational.core.mapping.Table;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Table("branch")
    public class Branch {

        @Id
        private Long id;

        @Column("name")
        private String name;

        @Column("address")
        private String address;

        @Column("franchise_id")
        private Long franchiseId;
    }
