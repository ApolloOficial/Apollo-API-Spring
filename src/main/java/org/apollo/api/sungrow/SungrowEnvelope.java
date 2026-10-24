package org.apollo.api.sungrow;

/** Campos comuns a toda resposta da Sungrow: req_serial_num / result_code / result_msg. */
interface SungrowEnvelope {

    /** "1" = sucesso. */
    String resultCode();

    String resultMsg();

    /** Preenchido so na resposta de erro de token: {"error":"invalid_token", ...}. */
    String error();

    String errorDescription();
}
