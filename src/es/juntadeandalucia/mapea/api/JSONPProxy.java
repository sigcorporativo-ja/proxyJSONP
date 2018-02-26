package es.juntadeandalucia.mapea.api;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.IOUtils;

import es.guadaltel.framework.ticket.Ticket;
import es.guadaltel.framework.ticket.TicketFactory;
import es.juntadeandalucia.mapea.bean.ProxyResponse;
import es.juntadeandalucia.mapea.builder.JSBuilder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Servlet implementation class JSONPProxy
 */
@WebServlet("/JSONPProxy")
public class JSONPProxy extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String AUTHORIZATION = "Authorization";

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public JSONPProxy() {
		super();
		// TODO Auto-generated constructor stub
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
				
				String encoding = this.getResponseEncoding(httpget);
				if (encoding == null) {
					encoding = "UTF-8";
				}
				
				InputStream responseStream = httpget.getResponseBodyAsStream();
				String responseContent = IOUtils.toString(responseStream,
						encoding);

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

	/**
	 * Gets the encoding of a response
	 */
	private String getResponseEncoding(GetMethod httpget) {
		String regexp = ".*charset\\=([^;]+).*";
		Boolean isCharset = null;
		String encoding = null;
		Header[] headerResponse = httpget.getResponseHeaders("Content-Type");
		for (Header header : headerResponse) {
			String contentType = header.getValue();
			if (!contentType.isEmpty()) {
				isCharset = Pattern.matches(regexp, contentType);
				if (isCharset) {
					encoding = contentType.replaceAll(regexp, "$1");
					break;
				}
			}
		}
		return encoding;
	}

}
