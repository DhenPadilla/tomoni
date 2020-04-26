//package com.template.schema;
//
//import com.google.common.collect.ImmutableList;
//import net.corda.core.schemas.MappedSchema;
//
//
///**
// * MappedSchema subclass representing the custom schema for the Insurance QueryableState.
// */
//
//public class JCTSchemaV1 extends MappedSchema {
//
//    /**
//     * The constructor of the MappedSchema requires the schemafamily, verison, and a list of all JPA entity classes for
//     * the Schema.
//     */
//    public JCTSchemaV1() {
//        super(JCTSchemaFamily.class, 1, ImmutableList.of(PersistentJCT.class, PersistentRecital.class));
//        // TODO - Add the Escrow as a one-to-one relationship to this schema family
//    }
//}
