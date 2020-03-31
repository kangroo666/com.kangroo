package com.lagou.test;

import com.lagou.dao.UserDao;
import com.lagou.io.Resources;
import com.lagou.pojo.User;
import com.lagou.sqlSession.SqlSession;
import com.lagou.sqlSession.SqlSessionFactory;
import com.lagou.sqlSession.SqlSessionFactoryBuilder;
import org.dom4j.DocumentException;
import org.junit.Test;

import java.beans.IntrospectionException;
import java.beans.PropertyVetoException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.List;

public class IPsistenceTest {

    @Test
    public void test() throws PropertyVetoException, DocumentException, ClassNotFoundException, SQLException, NoSuchFieldException, IllegalAccessException, InvocationTargetException, IntrospectionException, InstantiationException {
        InputStream resourceAsStream = Resources.getResourceAsStream("sqlMapConfig.xml");
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(resourceAsStream);
        SqlSession sqlSession = sqlSessionFactory.openSession();

        User user = new User();
        user.setUsername("张三");
        user.setId(3L);
//        User user1 = sqlSession.selectOne("user.selectOne", user);
//        System.out.println(user1.toString());



//        List<User> userList = sqlSession.selectList("user.selectList");
//        System.out.println(userList.toString());
        UserDao userDao = sqlSession.getMapper(UserDao.class);
//        List<User> allUser = userDao.findAllUser();
//        System.out.println(allUser);

        User user2 = userDao.findByCondition(user);
        System.out.println(user2);

    }
}
