package com.orderapp.repository.specification;

import com.orderapp.model.Client;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.orderapp.utils.Constants.ZERO;

public class ClientSpecification {

    public static Specification<Client> filterClients(BigDecimal minProfit, BigDecimal maxProfit,
                                                      String name, String email, String phoneNumber, int minKeywordLength) {
        return (root, query, cb) -> {
            if (query != null && query.getResultType() != Long.class) {
                root.fetch("suppliedOrders", JoinType.LEFT);
                root.fetch("consumedOrders", JoinType.LEFT);
            }

            List<Predicate> predicates = new ArrayList<>();

            if (minProfit != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("profit"), minProfit));
            }

            if (maxProfit != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("profit"), maxProfit));
            }

            addLikePredicate(name, "name", root, cb, predicates, minKeywordLength);
            addLikePredicate(email, "email", root, cb, predicates, minKeywordLength);
            addLikePredicate(phoneNumber, "phoneNumber", root, cb, predicates, minKeywordLength);

            return cb.and(predicates.toArray(new Predicate[ZERO]));
        };
    }

    private static void addLikePredicate(String param, String fieldName, Root<Client> root,
                                         CriteriaBuilder cb, List<Predicate> predicates, int minKeywordLength) {
        if (param != null) {
            if (param.trim().length() >= minKeywordLength) {
                String pattern = "%" + param.trim().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get(fieldName)), pattern));
            } else {
                throw new IllegalArgumentException(fieldName + " parameter should have a minimum of " + minKeywordLength + " characters.");
            }
        }
    }
}
