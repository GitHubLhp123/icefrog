package com.whaleal.icefrog.db.sql;

import org.junit.Test;

public class SqlFormatterTest {

    @Test
    public void formatTest() {
        // issue#I3XS44@github
        // 测试是否空指针错误
        String sql = "(select 1 from dual) union all (select 1 from dual)";
        SqlFormatter.format(sql);
    }
}
