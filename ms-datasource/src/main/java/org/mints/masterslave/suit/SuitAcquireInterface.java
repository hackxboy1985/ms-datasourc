package org.mints.masterslave.suit;


import org.mints.masterslave.entity.SuitDataSource;

import java.util.List;

public interface SuitAcquireInterface {

    SuitDataSource getSuitDataSource(String suitname);

    List<SuitDataSource> getSuitProducts();

}
