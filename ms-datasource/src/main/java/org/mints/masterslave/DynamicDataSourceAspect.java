package org.mints.masterslave;

import cn.hutool.core.collection.CollectionUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.mints.masterslave.datasource.DataSourceKey;
import org.mints.masterslave.logger.MsLogger;
import org.mints.masterslave.strategy.DsStrategy;
import org.springframework.core.annotation.Order;
import org.springframework.util.PatternMatchUtils;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.mints.masterslave.strategy.DsStrategy.DsStrategyStage.AOP_ANNOTATION_STAGE;
import static org.mints.masterslave.strategy.DsStrategy.DsStrategyStage.AOP_INVOKE_METHOD_STAGE;

@Aspect
//@Order(-1)
@Order(0)
public class DynamicDataSourceAspect {
    private static final MsLogger LOG = MsLogger.getLogger(DynamicDataSourceAspect.class);

    DsStrategy dsStrategy;


    public DynamicDataSourceAspect(DsStrategy dsStrategy){
        this.dsStrategy = dsStrategy;
    }

    @Pointcut(" execution( * org.springframework.data.repository.Repository+.*(..)) ")
    public void pointCut() {
    }

    /**
     * 执行方法前更换数据源
     * @param joinPoint        切点
     * @param targetDataSource 动态数据源
     */
    @Before("@annotation(targetDataSource)")
    public void doBefore(JoinPoint joinPoint, TargetDataSource targetDataSource) {
        Signature signature = joinPoint.getSignature();
        DataSourceKey dataSourceKey = targetDataSource.dataSourceKey();
        if (dataSourceKey == DataSourceKey.SLAVE) {
            //LOG.info(String.format("设置数据源为  %s", DataSourceKey.SLAVE));
            setDsStrategy(AOP_ANNOTATION_STAGE,true, signature.getName());
        } else {
            //LOG.info(String.format("使用默认数据源  %s", DataSourceKey.MASTER));
            setDsStrategy(AOP_ANNOTATION_STAGE,false, signature.getName());
        }
    }

    /**
     * 执行方法后清除数据源设置
     * @param joinPoint        切点
     * @param targetDataSource 动态数据源
     */
    @After("@annotation(targetDataSource)")
    public void doAfter(JoinPoint joinPoint, TargetDataSource targetDataSource) {
        //LOG.info(String.format("当前数据源  %s  执行清理方法", targetDataSource.dataSourceKey()));
        dsStrategy.clear(false,AOP_ANNOTATION_STAGE);
    }

    @Before(value = "pointCut()")
    public void doBeforeWithInvokeMethod(JoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        //获取当前切点方法对象
        Method method = methodSignature.getMethod();
        if (method.getDeclaringClass().isInterface()) {//判断是否为接口方法
            try {
                //LOG.info("invoke method:{}", method.getName());
                chooseDataSource(method.getName());
            } catch (Exception e) {
                LOG.error("[ms-ds]choose数据源异常：{}", e.getMessage(), e);
            }
        }
    }

    @After(value = "pointCut()")
    public void doAfterWithInvokeMethod(JoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        //获取当前切点方法对象
        Method method = methodSignature.getMethod();
        if (method.getDeclaringClass().isInterface()) {//判断是否为接口方法
            try {
                //LOG.info("invoke method:{}", method.getName());
                dsStrategy.clear(false,AOP_INVOKE_METHOD_STAGE);
            } catch (Exception e) {
                LOG.error("[ms-ds]clear数据源异常：{}", e.getMessage(), e);
            }
        }
    }

    private List<String> readMethodList = new ArrayList<String>();
    private List<String> writeMethodList = new ArrayList<String>();

    @PostConstruct
    void init(){
        readMethodList.addAll(CollectionUtil.newArrayList("query*", "use*", "get*", "count*", "find*", "list*", "search*", "exist*", "have*", "select*"));
        writeMethodList.addAll(CollectionUtil.newArrayList("save*", "add*", "create*", "insert*", "update*", "merge*", "del*","remove*","put*","write*"));
    }

    void chooseDataSource(String methodName){
        boolean isRead = true;
        if (isChooseReadDB(methodName)) {
            //选择slave数据源
            isRead = true;
        } else {
            //选择master数据源
            isRead = false;
        }
        setDsStrategy(AOP_INVOKE_METHOD_STAGE,isRead,methodName);
    }

    void setDsStrategy(DsStrategy.DsStrategyStage stage, boolean isRead, String methodName){
        dsStrategy.doStrategy(stage,isRead,methodName);
    }

    private boolean isChooseWriteDB(String methodName) {
        for (String mappedName : this.writeMethodList) {
            if (isMatch(methodName, mappedName)) {
                return true;
            }
        }
        return false;
    }

    private boolean isChooseReadDB(String methodName) {
        for (String mappedName : this.readMethodList) {
            if (isMatch(methodName, mappedName)) {
                return true;
            }
        }
        return false;
    }

    private boolean isMatch(String methodName, String mappedName) {
        return PatternMatchUtils.simpleMatch(mappedName, methodName);
    }
}

