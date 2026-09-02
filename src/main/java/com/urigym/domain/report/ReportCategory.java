package com.urigym.domain.report;

public enum ReportCategory {
    /** Gym advertises a price that differs from what it actually charges. */
    PRICE_MISMATCH,
    FACILITY,
    STAFF,
    OTHER,
    /** General customer-service question, not a complaint against a gym. */
    INQUIRY
}
