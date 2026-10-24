package org.apollo.api.sungrow;

interface SungrowEnvelope {
    String resultCode();

    String resultMsg();

    String error();

    String errorDescription();
}
