package com.lagou.sqlSession;

import com.lagou.pojo.Configuration;
import com.lagou.pojo.MappedStatement;

import java.beans.IntrospectionException;
import java.lang.reflect.*;
import java.sql.SQLException;
import java.util.List;

public class DefaultSqlSession implements SqlSession {

    private Configuration configuration;

    public DefaultSqlSession(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public <E> List<E> selectList(String statementid, Object... params) throws SQLException, IllegalAccessException, NoSuchFieldException, ClassNotFoundException, InvocationTargetException, IntrospectionException, InstantiationException {
        //将要去完成simpleExecutor里的query方法的调用
        SimpleExecutor simpleExecutor = new SimpleExecutor();
        MappedStatement mappedStatement = configuration.getMappedStatementMap().get(statementid);
        List<Object> list = simpleExecutor.query(configuration, mappedStatement, params);
        return (List<E>) list;
    }

    @Override
    public <T> T selectOne(String statementid, Object... params) throws SQLException, IllegalAccessException, NoSuchFieldException, ClassNotFoundException, InstantiationException, IntrospectionException, InvocationTargetException {
        List<Object> objects = selectList(statementid, params);
        if(objects.size()==1){
            return (T) objects.get(0);
        }else{
            throw new RuntimeException("查询结果为空或者返回太多");
        }
    }

    @Override
    public <T> T getMapper(Class<?> mapperClass) {
        //使用JDK动态代理来为DAO生成代理对象，并返回结果
        Object proxyInstance = Proxy.newProxyInstance(DefaultSqlSession.class.getClassLoader(), new Class[]{mapperClass}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                //底层都i是要执行jdbc代码，//根据不同情况执行selectOne 和selectList
                //准备参数解决statmentid硬编码问题， 要通过调用方法的方法名和接口全限定名
                //statmentid=接口全限定名.方法名
                String methodName = method.getName();
                String classname = method.getDeclaringClass().getName();
                String statmentId = classname + "." + methodName;

                //准备参数2:params : args
                //获取当前被调用方法的返回值类型
                Type genericReturnType = method.getGenericReturnType();
                //判断是否进行了  泛型类型参数化（是否含有泛型：true就是有泛型，false就是没有泛型，）
                //这里有泛型就可认为是返回值为list
                if(genericReturnType instanceof ParameterizedType){
                    List<Object> objects = selectList(statmentId, args);

                    return objects;
                }

                return selectOne(statmentId, args);
            }
        });
        return (T) proxyInstance;
    }
}
