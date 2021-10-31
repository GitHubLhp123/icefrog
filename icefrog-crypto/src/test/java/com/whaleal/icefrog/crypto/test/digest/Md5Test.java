package com.whaleal.icefrog.crypto.test.digest;

import com.whaleal.icefrog.crypto.digest.MD5;
import org.junit.Assert;
import org.junit.Test;

/**
 * MD5 单元测试
 *
 * @author Looly
 * @author wh
 */
public class Md5Test {

    @Test
    public void md5To16Test() {
        String hex16 = new MD5().digestHex16("中国");
        Assert.assertEquals(16, hex16.length());
        Assert.assertEquals("cb143acd6c929826", hex16);
    }
}
