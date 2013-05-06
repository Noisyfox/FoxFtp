package FTPSaercher;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class SearchFtp
 */
@WebServlet("/SearchFtp")
public class SearchFtp extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public SearchFtp() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("utf-8");// 汉字转码
		//  设定内容类型为HTML网页UTF-8编码 
		response.setContentType("text/html;charset=UTF-8");//  输出页面
		// PrintWriter out = response.getWriter();
		// out.println("<html><head>");
		// out.println("<title>First Servlet Hello</title>");
		// out.println("</head><body>");
		// out.println("Hello world my first Servlet!");
		// out.println("</body></html>");
		// out.close();

		// Take all completed form fields and add to a Properties object
		Properties completedFormFields = new Properties();
		Enumeration<?> pNames = request.getParameterNames();
		while (pNames.hasMoreElements()) {
			String propName = (String) pNames.nextElement();
			String value = request.getParameter(propName);
			if ((value != null) && (value.trim().length() > 0)) {
				completedFormFields.setProperty(propName, value);
			}
		}

		String inputStr = completedFormFields.getProperty("textbox", "").trim();
		String str = "";
		if (!inputStr.isEmpty()) {
			str = "喵呜~你刚刚输入的文字是：" + inputStr;
		}
		request.setAttribute("results", str);
		RequestDispatcher dispatcher = getServletContext()
				.getRequestDispatcher("/index.jsp");
		dispatcher.forward(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
