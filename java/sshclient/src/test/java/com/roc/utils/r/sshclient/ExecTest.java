package com.roc.utils.r.sshclient;

import org.junit.Assert;
import org.junit.Test;

public class ExecTest {

	@Test
	public void test01 () {
		System.out.println("Hello world 1");
		Assert.assertTrue(1==Integer.parseInt("1"));
	}
	
	@Test
	public void test02 () {
		System.out.println("Hello world 2");
		Assert.assertTrue(1==Integer.parseInt("2"));
	}
}
