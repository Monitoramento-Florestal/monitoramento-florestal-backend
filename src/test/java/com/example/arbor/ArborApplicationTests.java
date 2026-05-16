package com.example.arbor;

import com.example.arbor.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "spring.main.lazy-initialization=true",
                "spring.jpa.open-in-view=false"
        }
)
@Import(JwtService.class)
class ArborApplicationTests {

	@Test
	void contextLoads() {
	}

}
