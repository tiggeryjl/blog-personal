package com.blog;

import cn.hutool.crypto.asymmetric.RSA;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

//@SpringBootTest
class BlogServerApplicationTests {

//    @Test
    void contextLoads() {

    }

//    @Test
    public void test1() {
        RSA rsa = new RSA();
        System.out.println("私钥：" + rsa.getPrivateKeyBase64());
        System.out.println("公钥：" + rsa.getPublicKeyBase64());
    }

}
