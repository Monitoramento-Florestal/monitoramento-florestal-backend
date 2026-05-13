package com.example.arbor;

import com.example.arbor.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(JwtService.class)
class ArborApplicationTests {

	@Test
	void contextLoads() {
	}

}
