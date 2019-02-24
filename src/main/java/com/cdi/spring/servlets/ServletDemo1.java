package com.cdi.spring.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.cdi.spring.impl.MyCdiBean1;

/**
 * Server ejemplo del Puente
 * @author walejandromt
 */
public class ServletDemo1 extends HttpServlet {

	@Inject
	private MyCdiBean1 cdi;
	
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		PrintWriter out = resp.getWriter();
		out.println("<html>");
		out.println("<body>");
		out.println("<h1>Hello Servlet Get</h1>");
		out.println("<h1>" + cdi.bean().getProp() + "</h1>");
		out.println("</body>");
		out.println("</html>");	
	}
}
