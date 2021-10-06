package com.whaleal.icefrog.core.io.resource;

import com.whaleal.icefrog.core.io.FileUtil;
import com.whaleal.icefrog.core.io.IoUtil;
import com.whaleal.icefrog.core.util.StrUtil;
import org.junit.Assert;
import org.junit.Test;

public class ResourceUtilTest {

	@Test
	public void readXmlTest(){
		final String str = ResourceUtil.readUtf8Str("test.xml");
		Assert.assertNotNull(str);

		Resource resource = new ClassPathResource("test.xml");
		final String xmlStr = resource.readUtf8Str();

		Assert.assertEquals(str, xmlStr);
	}

	@Test
	public void stringResourceTest(){
		final StringResource stringResource = new StringResource("testData", "test");
		Assert.assertEquals("test", stringResource.getName());
		Assert.assertArrayEquals("testData".getBytes(), stringResource.readBytes());
		Assert.assertArrayEquals("testData".getBytes(), IoUtil.readBytes(stringResource.getStream()));
	}

	@Test
	public void fileResourceTest(){
		final FileResource resource = new FileResource(FileUtil.file("test.xml"));
		Assert.assertEquals("test.xml", resource.getName());
		Assert.assertTrue(StrUtil.isNotEmpty(resource.readUtf8Str()));
	}
}
