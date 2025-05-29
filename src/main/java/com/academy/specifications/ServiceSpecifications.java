package com.academy.specifications;

import com.academy.models.Tag;
import com.academy.models.service.Service;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class ServiceSpecifications {

    public static Specification<Service> hasNameLike(String name) {
        return (root, query, cb) ->
                name == null ? null : cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    public static Specification<Service> hasPriceGreaterThanOrEqual(Double minPrice) {
        return (root, query, cb) ->
                minPrice == null ? null : cb.greaterThanOrEqualTo(root.get("price"), minPrice);
    }

    public static Specification<Service> hasPriceLessThanOrEqual(Double maxPrice) {
        return (root, query, cb) ->
                maxPrice == null ? null : cb.lessThanOrEqualTo(root.get("price"), maxPrice);
    }

    public static Specification<Service> hasAnyTagNameLike(List<String> tagNames) {
        return (root, query, cb) -> {
            if (tagNames == null || tagNames.isEmpty()) return null;

            Join<Service, Tag> tagJoin = root.join("tags", JoinType.INNER);

            Predicate[] predicates = tagNames.stream()
                    .map(tag -> cb.like(cb.lower(tagJoin.get("name")), "%" + tag.toLowerCase() + "%"))
                    .toArray(Predicate[]::new);

            if (query != null) {
                query.distinct(true); // Avoid returning the same service duplicated, if multiple matches
            }
            return cb.or(predicates);
        };
    }
}
