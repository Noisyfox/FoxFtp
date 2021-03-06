package FTPSearcher;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;

/**
 * Servlet implementation class AdminSrv
 */
@WebServlet("/management/AdminSrv")
public class AdminSrv extends HttpServlet {
    // public static final String SESSION_ADMIN = "user_admin";
    private static final long serialVersionUID = 1L;

    public static final String ADMIN_ARGUMENT_REQUEST = "request";
    public static final String ADMIN_ARGUMENT_FTPDIR = "ftpdir";
    public static final String ADMIN_ARGUMENT_INDEXDIR = "indexdir";
    public static final String ADMIN_ARGUMENT_URLPREFIX = "urlprefix";
    public static final String ADMIN_REQUEST_UPDATESETTINGS = "updsettings";
    public static final String ADMIN_REQUEST_REINDEX = "reindex";

    /**
     * @see HttpServlet#HttpServlet()
     */
    public AdminSrv() {
        super();
    }

    @Override
    public void destroy() {
        ServiceStatusUtil.unregisterDrivers();
        super.destroy();
    }

    private void msg(PrintWriter out, boolean success, String message) {
        String json = Util.genJSON(success, message);
        if (json != null) {
            out.write(json);
        } else {
            out.write("{\"success\":false,\"message\":\"Inner error!\"}");
        }
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
        PrintWriter out = response.getWriter();

        // HttpSession state = ((HttpServletRequest) request).getSession();
        // String adminSession = (String) state.getAttribute(SESSION_ADMIN);
        // if (adminSession == null || !adminSession.equals("true")) {
        // PrintWriter out = response.getWriter();
        // out.println("<html><head>");
        // out.println("<title>First Servlet Hello</title>");
        // out.println("</head><body>");
        // out.println("坑爹呢！");
        // out.println("</body></html>");
        // out.close();
        // return;
        // }

        // 处理管理请求
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

        String rmsg = "命令成功完成！";

        String adminRequest = completedFormFields.getProperty(
                ADMIN_ARGUMENT_REQUEST, "").trim();
        if (adminRequest.isEmpty()) {
            msg(out, false, "请求不能为空！");
            return;
        }

        if (adminRequest.equals(ADMIN_REQUEST_UPDATESETTINGS)) {
            Properties currentProp = ServiceStatusUtil
                    .getServiceStatus();

            String path = completedFormFields.getProperty(
                    ADMIN_ARGUMENT_FTPDIR, "").trim();
            path = new File(path).getAbsolutePath();
            if (!path.isEmpty()) {
                currentProp.setProperty(ServiceStatusUtil.STATUS_FTP_PATH,
                        path);
            }
            path = completedFormFields.getProperty(ADMIN_ARGUMENT_INDEXDIR, "")
                    .trim();
            path = new File(path).getAbsolutePath();
            if (!path.isEmpty()) {
                currentProp.setProperty(ServiceStatusUtil.STATUS_INDEX_PATH,
                        path);
            }
            path = completedFormFields
                    .getProperty(ADMIN_ARGUMENT_URLPREFIX, "").trim();
            if (!path.isEmpty()) {
                currentProp.setProperty(ServiceStatusUtil.STATUS_URL_PREFIX,
                        path);
            }
            ServiceStatusUtil.saveServiceStatus(currentProp);
        } else if (adminRequest.equals(ADMIN_REQUEST_REINDEX)) {
            // 重建索引
            Date start = new Date();
            FileIndexer fi = new FileIndexer();
            String result = fi.reIndex();
            if (!result.isEmpty()) {
                msg(out, false, "重建索引失败！" + result);
                return;
            }
            Date end = new Date();
            rmsg += "耗时" + (end.getTime() - start.getTime()) + "毫秒";
        } else {
            msg(out, false, "未知的请求：" + adminRequest);
            return;
        }

        msg(out, true, rmsg);
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
