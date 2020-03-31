package com.lagou.pojo;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

public class Configuration {
    private DataSource dataSource;

    //key:statementId value:封装好的mappedStatement对象
    private Map<String, MappedStatement> MappedStatementMap= new HashMap<>();

    public Map<String, MappedStatement> getMappedStatementMap() {
        return MappedStatementMap;
    }

    public void setMappedStatementMap(Map<String, MappedStatement> mappedStatementMap) {
        MappedStatementMap = mappedStatementMap;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
}
