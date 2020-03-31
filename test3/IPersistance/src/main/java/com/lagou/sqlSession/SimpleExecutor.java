package com.lagou.sqlSession;

import com.lagou.config.BoundSql;
import com.lagou.pojo.Configuration;
import com.lagou.pojo.MappedStatement;
import com.lagou.utils.GenericTokenParser;
import com.lagou.utils.ParameterMapping;
import com.lagou.utils.ParameterMappingTokenHandler;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SimpleExecutor implements Executor {

    @Override
    public <E> List<E> query(Configuration configuration, MappedStatement mappedStatement, Object... params) throws SQLException, IllegalAccessException, NoSuchFieldException, ClassNotFoundException, IntrospectionException, InstantiationException, InvocationTargetException {
        //1.注册驱动，获取链接
        Connection connection = configuration.getDataSource().getConnection();

        //2.获取sql语句：select * from user where id=#{id} and username=#{username}
          //转换sql语句：select * from user where id=？ and username=？，转换过程中还需要对#{}中的值进行解析存储
        String sql = mappedStatement.getSql();
        BoundSql boundSql = getBoundSql(sql);

        //3.获取预处理对象：preparedStatement
        PreparedStatement preparedStatement = connection.prepareStatement(boundSql.getSqlText());

        //4.设置参数
          //获取到了参数的全路径
        String paramterType = mappedStatement.getParamterType();
        //获取参数类型
        Class<?> paramterTypeClass = getClassType(paramterType);
        //获取参数名称集合
        List<ParameterMapping> parameterMappingList = boundSql.getParameterMappingList();
        for (int i = 0; i < parameterMappingList.size(); i++) {
            ParameterMapping parameterMapping = parameterMappingList.get(i);
            String content = parameterMapping.getContent();

            //反射
            Field declaredField = paramterTypeClass.getDeclaredField(content);
            declaredField.setAccessible(true);
            Object o = declaredField.get(params[0]);

            preparedStatement.setObject(i+1,o);

        }

        //5.执行sql
        ResultSet resultSet = preparedStatement.executeQuery();
        //从mapper配置文件中获取结果字段全路径
        String resultType = mappedStatement.getResultType();
        //将全路径变成class对象
        Class<?> resultTypeClass = getClassType(resultType);

        ArrayList<Object> objects = new ArrayList<>();

        //6.返回封装结果集
        while (resultSet.next()){
            //将resultTypeClass实例化
            Object object = resultTypeClass.newInstance();
            //元数据
            ResultSetMetaData metaData = resultSet.getMetaData();
            for(int i=1; i<=metaData.getColumnCount();i++){
                //字段名
                String columnName = metaData.getColumnName(i);
                //字段值
                Object value = resultSet.getObject(columnName);

                //使用反射或者自省，根据数据库表和实体类的对应关系，完成封装
                //PropertyDescriptor是内省库中的一个类，可以对resultTypeClass类型中的columnName属性生成读写方法
                PropertyDescriptor propertyDescriptor = new PropertyDescriptor(columnName, resultTypeClass);
                Method writeMethod = propertyDescriptor.getWriteMethod();
                writeMethod.invoke(object,value);
            }
            objects.add(object);
        }


        return (List<E>)objects;
    }

    private Class<?> getClassType(String paramterType) throws ClassNotFoundException {
        if(paramterType!=null){
            Class<?> aClass = Class.forName(paramterType);
            return aClass;
        }
        return null;
    }

    /**
     * 完成对#{}的解析工作，1.使用？代替。 2.解析出#{}里边的值并存储
     * @return
     */
    private BoundSql getBoundSql(String sql) {
        //1.标记处理类：配置标记解析器来完成对占位符的解析处理
        ParameterMappingTokenHandler parameterMappingTokenHandler = new ParameterMappingTokenHandler();
        GenericTokenParser genericTokenParser = new GenericTokenParser("#{","}",parameterMappingTokenHandler);

        //2.解析出来的SQL
        String parseSql = genericTokenParser.parse(sql);
        //#{}里面解析出来的参数名称
        List<ParameterMapping> parameterMappings = parameterMappingTokenHandler.getParameterMappings();
        BoundSql boundSql = new BoundSql(parseSql,parameterMappings);
        return boundSql;
    }
}
