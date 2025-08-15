package com.orderapp.utils;

import org.springframework.data.domain.Sort;

import static com.orderapp.utils.Constants.SORT_ORDER_DESC;

public class Utils {

    public static Sort.Direction parseSortDirection(String sortOrder) {
        return SORT_ORDER_DESC.equals(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC;
    }
}
