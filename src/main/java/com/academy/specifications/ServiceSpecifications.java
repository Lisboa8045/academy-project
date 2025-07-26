package com.academy.specifications;

import com.academy.models.Tag;
import com.academy.models.service.Service;
import com.academy.models.service.ServiceStatusEnum;
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

    public static Specification<Service> nameOrTagMatches(String query) {
        return (root, cq, cb) -> {
            if (query == null || query.isBlank()) return null;

            String likePattern = "%" + query.toLowerCase() + "%";

            Predicate nameLike = cb.like(cb.lower(root.get("name")), likePattern);

            Join<Service, Tag> tagJoin = root.join("tags", JoinType.LEFT);
            Predicate tagLike = cb.like(cb.lower(tagJoin.get("name")), likePattern);

            if (cq != null) {
                cq.distinct(true); // Avoid returning the same service duplicated, if multiple matches
            }

            return cb.or(nameLike, tagLike);
        };
    }

    public static Specification<Service> hasDurationGreaterThanOrEqual(Integer minDuration) {
        return (root, query, cb) ->
                minDuration == null ? null : cb.greaterThanOrEqualTo(root.get("duration"), minDuration);
    }

    public static Specification<Service> hasDurationLessThanOrEqual(Integer maxDuration) {
        return (root, query, cb) ->
                maxDuration == null ? null : cb.lessThanOrEqualTo(root.get("duration"), maxDuration);
    }

    public static Specification<Service> canNegotiate(Boolean negotiable) {
        return (root, query, cb) ->
                Boolean.TRUE.equals(negotiable) ? cb.isTrue(root.get("negotiable")) : null;
    }

    public static Specification<Service> hasServiceType(String typeName) {
        return (root, query, cb) ->
                typeName == null || typeName.isBlank()
                        ? null
                        : cb.equal(cb.lower(root.get("serviceType").get("name")), typeName.toLowerCase());
    }

    public static Specification<Service> isEnabled(Boolean enabled) {
        return (root, query, cb) -> {
            if (enabled == null) return null;
            return cb.equal(root.get("enabled"), enabled);
        };
    }

    public static Specification<Service> statusMatches(String status) {
        return (root, query, cb) -> {
            if (status == null || status.isBlank()) return null;
            try {
                ServiceStatusEnum statusEnum = ServiceStatusEnum.valueOf(status.toUpperCase());
                return cb.equal(root.get("status"), statusEnum);
            } catch (IllegalArgumentException e) {
                return null;
            }
        };
    }
}
