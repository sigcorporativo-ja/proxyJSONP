package es.juntadeandalucia.mapea.api;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.IOUtils;

import es.juntadeandalucia.framework.ticket.Ticket;
import es.juntadeandalucia.framework.ticket.TicketFactory;
import es.juntadeandalucia.mapea.bean.ProxyResponse;
import es.juntadeandalucia.mapea.builder.JSBuilder;

/**
 * Servlet implementation class JSONPProxy
 */
@WebServlet("/proxyJSONP")
public class JSONPProxy extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String AUTHORIZATION = "Authorization";

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public JSONPProxy() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		ProxyResponse proxyResponse = new ProxyResponse();
		String url = request.getParameter("url");
		String callbackFn = request.getParameter("callback");
		String ticketParameter = request.getParameter("ticket");
		String tojson = request.getParameter("tojson");

		try {

			HttpClient client = new HttpClient();
			GetMethod httpget = new GetMethod(url);

			// sets ticket if the user specified one
			if (ticketParameter != null) {
				ticketParameter = ticketParameter.trim();
				if (!ticketParameter.isEmpty()) {
					Ticket ticket = TicketFactory.createInstance();
					try {
						Map<String, String> props = ticket
								.getProperties(ticketParameter);
						String user = props.get("user");
						String pass = props.get("pass");
						String userAndPass = user + ":" + pass;
						String encodedLogin = new String(
								org.apache.commons.codec.binary.Base64
										.encodeBase64(userAndPass.getBytes()));
						httpget.addRequestHeader(AUTHORIZATION, "Basic "
								+ encodedLogin);
					} catch (Exception e) {
						System.out
								.println("-------------------------------------------");
						System.out
								.println("EXCEPCTION THROWED BY PROXYREDIRECT CLASS");
						System.out.println("METHOD: doPost");
						System.out.println("TICKET VALUE: " + ticketParameter);
						System.out
								.println("-------------------------------------------");
					}
				}
			}

			client.executeMethod(httpget);

			int statusCode = httpget.getStatusCode();
			proxyResponse.setStatusCode(statusCode);
			if (statusCode == HttpStatus.SC_OK) {
				InputStream responseStream = httpget.getResponseBodyAsStream();
				String responseContent = IOUtils.toString(responseStream,
						"UTF-8");

				proxyResponse.setContent(responseContent);
			}
			proxyResponse.setHeaders(httpget.getResponseHeaders());
		} catch (HttpException e) {
			proxyResponse = this.error(url, e.getLocalizedMessage());
		} catch (IOException e) {
			proxyResponse = this.error(url, e.getLocalizedMessage());
		} catch (IllegalStateException e) {
			proxyResponse = this.error(url, e.getLocalizedMessage());
		}

		String finalResponse;

		if (tojson != null && tojson.equalsIgnoreCase("false")) {
			response.getWriter().println(proxyResponse.getContent());

		} else {

			finalResponse = JSBuilder.wrapCallback(proxyResponse.toJSON(),
					callbackFn);

			response.getWriter().println(finalResponse);
		}

	}

	/**
	 * Creates a response error using the specified message.
	 * 
	 * @param url
	 *            URL of the request
	 * @param message
	 *            message of the error
	 */
	private ProxyResponse error(String url, String message) {
		ProxyResponse proxyResponse = new ProxyResponse();
		proxyResponse.setError(true);
		proxyResponse.setErrorMessage(message);
		return proxyResponse;
	}

}
