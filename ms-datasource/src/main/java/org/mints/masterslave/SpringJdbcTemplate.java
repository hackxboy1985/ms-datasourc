//package org.mints.masterslave;
//
//import org.mints.masterslave.datasource.DynamicRoutingDataSource;
//import org.springframework.jdbc.core.JdbcTemplate;
//
//import javax.sql.DataSource;
//
//public class SpringJdbcTemplate  extends JdbcTemplate {
//
//    @Override
//    public DataSource getDataSource() {
//        // TODO Auto-generated method stub
//        DynamicRoutingDataSource router =  (DynamicRoutingDataSource) super.getDataSource();
//        DataSource acuallyDataSource = router.getAcuallyDataSource();
//        return acuallyDataSource;
//    }
//
//    public SpringJdbcTemplate(DataSource dataSource) {
//        super(dataSource);
//    }
//}
