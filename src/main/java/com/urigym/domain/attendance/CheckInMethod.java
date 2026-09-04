package com.urigym.domain.attendance;

public enum CheckInMethod {
    /** Member types the phone number registered on their account. */
    PHONE,
    QR,
    // Held back until the hardware/vision integration is decided — see AttendanceService.checkIn.
    FACE,
    NFC
}
