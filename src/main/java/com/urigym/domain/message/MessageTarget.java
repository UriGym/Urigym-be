package com.urigym.domain.message;

public enum MessageTarget {
    /** Every member of the gym. */
    ALL,
    /** Only the gym members explicitly listed in the request. */
    SELECTED
}
