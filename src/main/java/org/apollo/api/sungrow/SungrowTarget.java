package org.apollo.api.sungrow;

import java.util.Set;

/** Inversor Sungrow ativo do Apollo e as posicoes (MPPT/entrada) de suas strings ativas. */
public record SungrowTarget(String serialNumber, int mpptCount, int stringsPerMppt,
                            Set<SungrowPointMapper.StringSlot> activeSlots) {

    public int stringCount() {
        return SungrowPointMapper.stringCount(mpptCount, stringsPerMppt);
    }
}
