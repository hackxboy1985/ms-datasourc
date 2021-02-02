package com.mints.demo;


import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class proxy {

    public static Object getProxy(Class<?> clazz, Object target) {
        return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[]{clazz}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                Object ret = method.invoke(target, args);
                System.out.println("after execute method:" + method.getName());
                return ret;
            }
        });
    }
}
