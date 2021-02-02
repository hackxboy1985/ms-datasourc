package org.mints.masterslave.strategy;

public class DsStrategyFactory {

    public static DsStrategy create(String value) {
        if (DsStrategy.DsStrategyType.NORMAL_RW.name().equals(value)) {
            return new DsStrategyNormalRWSeparation();
        } else if (DsStrategy.DsStrategyType.TX_WRITE_FIRST.name().equals(value)) {
            return new DsStrategyTxWriteFirst();
        }
        throw new IllegalArgumentException("DsStrategyType参数异常");
    }

}
