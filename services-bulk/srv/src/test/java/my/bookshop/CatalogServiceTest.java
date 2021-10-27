package my.bookshop;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
public class CatalogServiceTest {

	private static final String URI = "/api/catalog/$batch";
	private static final String CRLF = "\r\n";

	@Autowired
	private MockMvc mockMvc;

	@Test
	public void testCreateReviewByAdmin() throws Exception {
		mockMvc.perform(post(URI).contentType("multipart/mixed;boundary=batch_8194-cf13-1f56").content(readBatch()))
				.andExpect(status().isOk());
	}

	private String readBatch() throws IOException {
		InputStreamReader inputStreamReader = new InputStreamReader(
				this.getClass().getClassLoader().getResourceAsStream("batch.txt"), StandardCharsets.UTF_8);

		return new BufferedReader(inputStreamReader).lines().collect(Collectors.joining(CRLF));
	}
}
