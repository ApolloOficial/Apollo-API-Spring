package org.apollo.api.sungrow;

import java.util.Set;

public record SungrowTarget(String serialNumber, int mpptCount, int stringsPerMppt,
                            Set<SungrowPointMapper.StringSlot> activeSlots) {
    public int stringCount() {
        return SungrowPointMapper.stringCount(mpptCount, stringsPerMppt);
    }
}
