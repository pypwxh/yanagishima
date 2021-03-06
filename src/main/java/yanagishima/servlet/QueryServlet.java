package yanagishima.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.fluent.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yanagishima.config.YanagishimaConfig;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Singleton
public class QueryServlet extends HttpServlet {

	private static Logger LOGGER = LoggerFactory.getLogger(QueryServlet.class);

	private static final long serialVersionUID = 1L;

	private YanagishimaConfig yanagishimaConfig;

	private static final int LIMIT = 100;

	@Inject
	public QueryServlet(YanagishimaConfig yanagishimaConfig) {
		this.yanagishimaConfig = yanagishimaConfig;
	}

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		String datasource = Optional.ofNullable(request.getParameter("datasource")).get();
		String prestoCoordinatorServer = yanagishimaConfig
				.getPrestoCoordinatorServer(datasource);
		response.setContentType("application/json");
		PrintWriter writer = response.getWriter();
		String originalJson = Request.Get(prestoCoordinatorServer + "/v1/query")
				.execute().returnContent().asString(StandardCharsets.UTF_8);
		ObjectMapper mapper = new ObjectMapper();
		List<Map> list = mapper.readValue(originalJson, List.class);
		if(list.size() > LIMIT) {
			list.sort((a,b)-> String.class.cast(b.get("queryId")).compareTo(String.class.cast(a.get("queryId"))));
			String json = mapper.writeValueAsString(list.subList(0, LIMIT));
			writer.println(json);
		} else {
			writer.println(originalJson);
		}
	}

}
